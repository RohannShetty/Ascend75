package com.ascend75.core.crypto.di

import android.content.Context
import com.ascend75.core.crypto.BiometricAuthHelper
import com.ascend75.core.crypto.KeystoreManager
import com.ascend75.core.crypto.VaultFileStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CryptoModule {

    @Provides
    @Singleton
    fun provideKeystoreManager(): KeystoreManager = KeystoreManager()

    @Provides
    @Singleton
    fun provideVaultFileStorage(
        @ApplicationContext context: Context,
        keystoreManager: KeystoreManager
    ): VaultFileStorage = VaultFileStorage(context, keystoreManager)

    @Provides
    @Singleton
    fun provideBiometricAuthHelper(
        @ApplicationContext context: Context
    ): BiometricAuthHelper = BiometricAuthHelper(context)
}
