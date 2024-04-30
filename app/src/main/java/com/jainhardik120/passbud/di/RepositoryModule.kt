package com.jainhardik120.passbud.di

import com.jainhardik120.passbud.data.CredentialsRepositoryImpl
import com.jainhardik120.passbud.domain.CredentialsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindCredentialsRepository(credentialsRepositoryImpl: CredentialsRepositoryImpl): CredentialsRepository
}