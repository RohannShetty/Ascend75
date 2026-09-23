package com.ascend75.feature.photos

import androidx.fragment.app.FragmentActivity
import com.ascend75.core.crypto.BiometricAuthHelper
import com.ascend75.core.crypto.VaultFileStorage
import com.ascend75.core.domain.model.UserPreferences
import com.ascend75.core.domain.repository.ChallengeRepository
import com.ascend75.core.domain.repository.DailyRecordRepository
import com.ascend75.core.domain.repository.SettingsRepository
import com.ascend75.core.domain.repository.TaskRepository
import com.ascend75.core.domain.repository.VaultRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.security.MessageDigest
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class PhotoVaultViewModelTest {

    private val vaultFileStorage = mockk<VaultFileStorage>()
    private val biometricAuthHelper = mockk<BiometricAuthHelper>(relaxed = true)
    private val taskRepository = mockk<TaskRepository>(relaxed = true)
    private val vaultRepository = mockk<VaultRepository>(relaxed = true)
    private val challengeRepository = mockk<ChallengeRepository>(relaxed = true)
    private val dailyRecordRepository = mockk<DailyRecordRepository>(relaxed = true)
    private val settingsRepository = mockk<SettingsRepository>()

    private val preferences = MutableStateFlow(UserPreferences(isBiometricEnabled = false))

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { settingsRepository.preferences } returns preferences
        every { challengeRepository.observeActiveChallenge() } returns flowOf(null)
        every { vaultRepository.observeVaultPhotos() } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = PhotoVaultViewModel(
        vaultFileStorage = vaultFileStorage,
        biometricAuthHelper = biometricAuthHelper,
        taskRepository = taskRepository,
        vaultRepository = vaultRepository,
        challengeRepository = challengeRepository,
        dailyRecordRepository = dailyRecordRepository,
        settingsRepository = settingsRepository
    )

    @Test
    fun theVaultStartsLockedAndOnlyUnlocksWhenAsked() = runTest {
        val vault = viewModel()

        assertTrue("Photos must never be visible before the gate is satisfied", vault.uiState.value.isLocked)

        vault.unlockVault()
        assertFalse(vault.uiState.value.isLocked)

        vault.lockVault()
        assertTrue("Locking must return the vault to the gated state", vault.uiState.value.isLocked)
    }

    @Test
    fun withTheGateDisabledUnlockingSkipsTheBiometricPrompt() = runTest {
        val vault = viewModel()
        val activity = mockk<FragmentActivity>(relaxed = true)

        vault.requestUnlock(activity)

        assertFalse(vault.uiState.value.isLocked)
        verify(exactly = 0) {
            biometricAuthHelper.promptBiometricAuth(any(), any(), any(), any(), any())
        }
    }

    @Test
    fun withTheGateEnabledButNoEnrolledCredentialUnlockingStillSucceeds() = runTest {
        preferences.value = UserPreferences(isBiometricEnabled = true)
        every { biometricAuthHelper.isBiometricOrPinAvailable() } returns false
        val vault = viewModel()

        vault.requestUnlock(mockk<FragmentActivity>(relaxed = true))

        assertFalse(vault.uiState.value.isLocked)
        verify(exactly = 0) {
            biometricAuthHelper.promptBiometricAuth(any(), any(), any(), any(), any())
        }
    }

    @Test
    fun withTheGateEnabledAndACredentialEnrolledTheSystemPromptIsRaised() = runTest {
        preferences.value = UserPreferences(isBiometricEnabled = true)
        every { biometricAuthHelper.isBiometricOrPinAvailable() } returns true
        val vault = viewModel()
        val activity = mockk<FragmentActivity>(relaxed = true)

        vault.requestUnlock(activity)

        assertTrue("The vault must stay closed until the prompt is answered", vault.uiState.value.isLocked)
        verify { biometricAuthHelper.promptBiometricAuth(activity, any(), any(), any(), any()) }

        vault.unlockVault()
        assertFalse(vault.uiState.value.isLocked)
    }

    @Test
    fun capturedPhotoIsEncryptedRecordedWithItsTrueSizeAndThePlaintextIsZeroed() = runTest {
        val encryptedFile = File.createTempFile("vault-capture", ".enc").apply {
            writeBytes(ByteArray(2048) { 9 })
            deleteOnExit()
        }
        coEvery { vaultFileStorage.saveEncryptedPhoto(any(), any()) } returns encryptedFile

        val vault = viewModel()
        vault.unlockVault()
        vault.startCapture()

        val plaintext = ByteArray(1024) { index -> (index % 97).toByte() }
        val expectedHash = MessageDigest.getInstance("SHA-256").digest(plaintext)
            .joinToString(separator = "") { byte -> String.format(Locale.US, "%02x", byte) }

        vault.saveCapturedPhoto("photo-task", dayNumber = 4, imageBytes = plaintext) { }

        coVerify {
            vaultRepository.addPhoto(
                id = any(),
                taskEntryId = "photo-task",
                encryptedFilePath = encryptedFile.absolutePath,
                photoHash = expectedHash,
                fileSizeBytes = encryptedFile.length(),
                capturedAt = any()
            )
        }
        assertEquals(64, expectedHash.length)

        coVerify { taskRepository.setCompletion("photo-task", true, any()) }

        assertTrue("Plaintext must be zeroed after encryption", plaintext.all { it == 0.toByte() })
        assertFalse(vault.uiState.value.isCapturing)
        assertTrue(vault.uiState.value.captureCompleted)
    }

    @Test
    fun aFailedEncryptionSurfacesTheErrorInsteadOfRecordingAPhoto() = runTest {
        coEvery { vaultFileStorage.saveEncryptedPhoto(any(), any()) } throws
            IllegalStateException("keystore unavailable")

        val vault = viewModel()
        vault.unlockVault()
        vault.startCapture()
        vault.saveCapturedPhoto("photo-task", dayNumber = 1, imageBytes = ByteArray(16)) { }

        assertNotNull(vault.uiState.value.errorMessage)
        assertFalse(vault.uiState.value.isCapturing)
        coVerify(exactly = 0) { vaultRepository.addPhoto(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun decryptedBytesAreReturnedForRenderingAndUnreadableFilesYieldNull() = runTest {
        coEvery { vaultFileStorage.readDecryptedPhoto(any()) } returns byteArrayOf(4, 5, 6)
        assertEquals(3, viewModel().readPhotoBytes("/vault/day-1.enc")?.size)

        coEvery { vaultFileStorage.readDecryptedPhoto(any()) } throws IllegalStateException("corrupt")
        assertNull(viewModel().readPhotoBytes("/vault/day-2.enc"))
    }
}
