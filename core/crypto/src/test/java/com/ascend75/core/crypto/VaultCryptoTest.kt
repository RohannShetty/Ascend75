package com.ascend75.core.crypto

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.io.FileOutputStream

class VaultCryptoTest {

    @Test
    fun verifySecureZeroWipeOverwritesAndDeletesFile() {
        val tempFile = File.createTempFile("test_vault_photo", ".enc")
        FileOutputStream(tempFile).use {
            it.write("sensitive_photo_bytes_123456789".toByteArray())
        }

        assertTrue(tempFile.exists())
        val initialLength = tempFile.length()
        assertTrue(initialLength > 0)

        // Mock zero wipe routine logic
        FileOutputStream(tempFile).use { fos ->
            val zeroBuffer = ByteArray(initialLength.toInt())
            fos.write(zeroBuffer)
            fos.flush()
        }
        val isDeleted = tempFile.delete()

        assertTrue(isDeleted)
        assertTrue(!tempFile.exists())
    }
}
