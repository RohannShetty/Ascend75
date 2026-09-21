package com.ascend75.core.crypto

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

/**
 * Exercises the on-disk vault framing (1-byte IV length, IV, ciphertext) end to end. The Keystore
 * cipher is replaced with an identity transform because AndroidKeyStore is unavailable off-device;
 * what is under test here is the file layout, not AES itself.
 */
@RunWith(RobolectricTestRunner::class)
class VaultFileStorageTest {

    private val keystoreManager = mockk<KeystoreManager>()
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun setUp() {
        every { keystoreManager.encryptBytes(any()) } answers {
            val plaintext = firstArg<ByteArray>()
            ByteArray(12) { 7 } to plaintext.copyOf()
        }
        every { keystoreManager.decryptBytes(any(), any()) } answers { secondArg() }
    }

    private fun storage() = VaultFileStorage(context, keystoreManager)

    @Test
    fun savedPhotoIsWrittenIntoThePrivateVaultDirectoryAndReadsBackUnchanged() = runBlocking {
        val original = ByteArray(4096) { index -> (index % 251).toByte() }

        val encrypted = storage().saveEncryptedPhoto("unit-test", original)

        assertTrue("Vault file must exist on disk", encrypted.exists())
        assertEquals("unit-test.enc", encrypted.name)
        assertEquals("vault", encrypted.parentFile?.name)
        assertTrue(
            "Ciphertext must not be stored in a world-readable location",
            encrypted.absolutePath.startsWith(context.filesDir.absolutePath)
        )

        assertArrayEquals(original, storage().readDecryptedPhoto(encrypted))
    }

    @Test
    fun wipingTheVaultRemovesEveryEncryptedFile() = runBlocking {
        val vault = storage()
        vault.saveEncryptedPhoto("day-1", ByteArray(1024) { 1 })
        vault.saveEncryptedPhoto("day-2", ByteArray(2048) { 2 })
        assertEquals(2, vault.listVaultFiles().size)

        vault.wipeAllVaultFiles()

        assertTrue("Wipe must leave no vault files behind", vault.listVaultFiles().isEmpty())
    }

    @Test
    fun wipingASingleFileOverwritesAndUnlinksIt() = runBlocking {
        val vault = storage()
        val encrypted = vault.saveEncryptedPhoto("day-3", ByteArray(512) { 3 })

        vault.secureZeroWipe(encrypted)

        assertTrue(!File(encrypted.absolutePath).exists())
    }
}
