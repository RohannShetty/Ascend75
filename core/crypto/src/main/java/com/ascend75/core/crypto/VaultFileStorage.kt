package com.ascend75.core.crypto

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaultFileStorage @Inject constructor(
    @ApplicationContext private val context: Context,
    private val keystoreManager: KeystoreManager
) {
    private val vaultDir: File
        get() {
            val dir = File(context.filesDir, "vault")
            if (!dir.exists()) dir.mkdirs()
            return dir
        }

    fun saveEncryptedPhoto(filename: String, imageBytes: ByteArray): File {
        val (iv, ciphertext) = keystoreManager.encryptBytes(imageBytes)
        val targetFile = File(vaultDir, "$filename.enc")

        FileOutputStream(targetFile).use { fos ->
            // Store 12-byte IV length, then IV, then ciphertext
            fos.write(iv.size)
            fos.write(iv)
            fos.write(ciphertext)
        }
        return targetFile
    }

    fun readDecryptedPhoto(file: File): ByteArray {
        FileInputStream(file).use { fis ->
            val ivSize = fis.read()
            val iv = ByteArray(ivSize)
            fis.read(iv)
            val ciphertext = fis.readBytes()
            return keystoreManager.decryptBytes(iv, ciphertext)
        }
    }

    /**
     * Overwrites file contents with random bytes and zeros before deleting from disk.
     */
    fun secureZeroWipe(file: File) {
        if (file.exists() && file.isFile) {
            val length = file.length()
            if (length > 0) {
                FileOutputStream(file).use { fos ->
                    val zeroBuffer = ByteArray(4096)
                    var written = 0L
                    while (written < length) {
                        val toWrite = minOf(zeroBuffer.size.toLong(), length - written).toInt()
                        fos.write(zeroBuffer, 0, toWrite)
                        written += toWrite
                    }
                    fos.flush()
                }
            }
            file.delete()
        }
    }

    fun wipeAllVaultFiles() {
        vaultDir.listFiles()?.forEach { file ->
            secureZeroWipe(file)
        }
    }
}
