package com.jainhardik120.passbud.domain

import androidx.biometric.BiometricPrompt
import com.jainhardik120.passbud.data.local.AccountWithCredentialCount
import com.jainhardik120.passbud.data.local.entities.Credential
import com.jainhardik120.passbud.data.local.entities.CredentialAccount
import com.jainhardik120.passbud.util.CryptoPurpose
import kotlinx.coroutines.flow.Flow

interface CredentialsRepository {

    suspend fun getBiometricInfo(): BiometricInfo

    fun retrieveAccountsList(): Flow<List<CredentialAccount>>

    fun accountWithCount(): Flow<List<AccountWithCredentialCount>>

    fun getAccountDetails(accountId: String): Flow<CredentialAccount?>

    fun getAccountCredentials(accountId: String): Flow<List<Credential>>

    suspend fun deleteAccount(accountId: String)

    suspend fun deleteCredential(credentialId : String)

    suspend fun updateAccountDetails(account: CredentialAccount)

    suspend fun createAccount(
        name: String,
        description: String
    ): String

    suspend fun createCryptoObject(
        purpose: CryptoPurpose,
        ivString: String?
    ): BiometricPrompt.CryptoObject

    suspend fun saveCredential(
        credential: Credential,
        cryptoObject: BiometricPrompt.CryptoObject? = null
    )

    suspend fun decryptCredential(
        encryptedValue: String,
        cryptoObject: BiometricPrompt.CryptoObject
    ): String
}


