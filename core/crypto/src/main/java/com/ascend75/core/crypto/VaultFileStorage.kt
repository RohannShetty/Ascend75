package com.ascend75.core.crypto

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.DataInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
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

    suspend fun saveEncryptedPhoto(filename: String, imageBytes: ByteArray): File = withContext(Dispatchers.IO) {
        val (iv, ciphertext) = keystoreManager.encryptBytes(imageBytes)
        val targetFile = File(vaultDir, "$filename.enc")

        FileOutputStream(targetFile).use { fos ->
            // Layout: 1-byte IV length, then IV, then ciphertext
            fos.write(iv.size)
            fos.write(iv)
            fos.write(ciphertext)
        }
        targetFile
    }

    suspend fun readDecryptedPhoto(file: File): ByteArray = withContext(Dispatchers.IO) {
        DataInputStream(FileInputStream(file)).use { input ->
            val ivSize = input.readUnsignedByte()
            val iv = ByteArray(ivSize)
            input.readFully(iv)
            val ciphertext = input.readBytes()
            keystoreManager.decryptBytes(iv, ciphertext)
        }
    }

    /** Overwrites file contents with zeros before deleting from disk. */
    suspend fun secureZeroWipe(file: File) = withContext(Dispatchers.IO) {
        zeroFillAndDelete(file)
    }

    suspend fun wipeAllVaultFiles() = withContext(Dispatchers.IO) {
        vaultDir.listFiles()?.forEach { file -> zeroFillAndDelete(file) }
    }

    suspend fun listVaultFiles(): List<File> = withContext(Dispatchers.IO) {
        vaultDir.listFiles()?.toList().orEmpty()
    }

    private fun zeroFillAndDelete(file: File) {
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
}
