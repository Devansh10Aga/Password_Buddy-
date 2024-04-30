package com.jainhardik120.passbud.di

import android.app.Application
import android.content.Context
import androidx.biometric.BiometricManager
import androidx.room.Room
import com.jainhardik120.passbud.data.crypto.CryptoEngine
import com.jainhardik120.passbud.data.local.CredentialsDatabase
import com.jainhardik120.passbud.data.local.KeyValueStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideCredentialsDatabase(app : Application):CredentialsDatabase{
        return Room.databaseBuilder(app, CredentialsDatabase::class.java, "credentials_database").fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideCryptoEngine() : CryptoEngine {
        return CryptoEngine()
    }


    @Provides
    fun provideBiometricManager(@ApplicationContext context: Context): BiometricManager {
        return BiometricManager.from(context)
    }

    @Provides
    @Singleton
    fun provideKeyValueStorage(@ApplicationContext context: Context): KeyValueStorage {
        val preferences = context.getSharedPreferences("simpleStorage", Context.MODE_PRIVATE)
        return KeyValueStorage(
            sharedPreferences = preferences
        )
    }

}


