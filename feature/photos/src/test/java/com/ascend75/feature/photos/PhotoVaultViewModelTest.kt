package com.ascend75.feature.photos

import androidx.fragment.app.FragmentActivity
import com.ascend75.core.crypto.BiometricAuthHelper
import com.ascend75.core.crypto.VaultFileStorage
import com.ascend75.core.database.dao.ChallengeDao
import com.ascend75.core.database.dao.DailyRecordDao
import com.ascend75.core.database.dao.ProgressPhotoDao
import com.ascend75.core.database.dao.TaskEntryDao
import com.ascend75.core.database.entities.ProgressPhotoEntity
import com.ascend75.core.datastore.AscendPreferencesDataSource
import com.ascend75.core.datastore.UserPreferences
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
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
    private val taskEntryDao = mockk<TaskEntryDao>(relaxed = true)
    private val progressPhotoDao = mockk<ProgressPhotoDao>(relaxed = true)
    private val challengeDao = mockk<ChallengeDao>(relaxed = true)
    private val dailyRecordDao = mockk<DailyRecordDao>(relaxed = true)
    private val preferencesDataSource = mockk<AscendPreferencesDataSource>()

    private val preferences = MutableStateFlow(UserPreferences(isBiometricEnabled = false))

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { preferencesDataSource.userPreferencesFlow } returns preferences
        every { challengeDao.observeActiveChallenge() } returns flowOf(null)
        every { progressPhotoDao.observeVaultPhotos() } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = PhotoVaultViewModel(
        vaultFileStorage = vaultFileStorage,
        biometricAuthHelper = biometricAuthHelper,
        taskEntryDao = taskEntryDao,
        progressPhotoDao = progressPhotoDao,
        challengeDao = challengeDao,
        dailyRecordDao = dailyRecordDao,
        preferencesDataSource = preferencesDataSource
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

        val inserted = slot<ProgressPhotoEntity>()
        coVerify { progressPhotoDao.insert(capture(inserted)) }
        assertEquals("photo-task", inserted.captured.taskEntryId)
        assertEquals(encryptedFile.absolutePath, inserted.captured.encryptedFilePath)
        assertEquals(encryptedFile.length(), inserted.captured.fileSizeBytes)
        assertEquals(expectedHash, inserted.captured.photoHash)
        assertEquals(64, inserted.captured.photoHash.length)

        coVerify { taskEntryDao.updateTaskCompletion("photo-task", true, any()) }

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
        coVerify(exactly = 0) { progressPhotoDao.insert(any()) }
    }

    @Test
    fun decryptedBytesAreReturnedForRenderingAndUnreadableFilesYieldNull() = runTest {
        coEvery { vaultFileStorage.readDecryptedPhoto(any()) } returns byteArrayOf(4, 5, 6)
        assertEquals(3, viewModel().readPhotoBytes("/vault/day-1.enc")?.size)

        coEvery { vaultFileStorage.readDecryptedPhoto(any()) } throws IllegalStateException("corrupt")
        assertNull(viewModel().readPhotoBytes("/vault/day-2.enc"))
    }
}
