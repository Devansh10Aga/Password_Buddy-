package com.jainhardik120.passbud.data

import android.util.Base64
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import com.jainhardik120.passbud.data.crypto.CryptoEngine
import com.jainhardik120.passbud.data.crypto.ValidationResult
import com.jainhardik120.passbud.data.local.AccountWithCredentialCount
import com.jainhardik120.passbud.data.local.CredentialsDao
import com.jainhardik120.passbud.data.local.CredentialsDatabase
import com.jainhardik120.passbud.data.local.entities.Credential
import com.jainhardik120.passbud.data.local.entities.CredentialAccount
import com.jainhardik120.passbud.domain.BiometricAuthStatus
import com.jainhardik120.passbud.domain.BiometricInfo
import com.jainhardik120.passbud.domain.CredentialsRepository
import com.jainhardik120.passbud.domain.KeyStatus
import com.jainhardik120.passbud.util.CryptoPurpose
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CredentialsRepositoryImpl @Inject constructor(
    private val biometricManager: BiometricManager,
    private val cryptoEngine: CryptoEngine,
    credentialsDatabase: CredentialsDatabase,
) : CredentialsRepository {

    private val dao: CredentialsDao = credentialsDatabase.dao
    private val requiredAuthenticators: Int = BiometricManager.Authenticators.BIOMETRIC_STRONG
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO

    override suspend fun deleteAccount(accountId: String) {
        dao.deleteAccountCredentials(accountId)
        dao.deleteAccount(accountId)
    }

    override suspend fun deleteCredential(credentialId: String) {
        dao.deleteCredential(credentialId)
    }

    override suspend fun updateAccountDetails(account: CredentialAccount) {
        dao.updateAccount(account)
    }

    override fun retrieveAccountsList(): Flow<List<CredentialAccount>> {
        return dao.getAllAccounts()
    }

    override suspend fun createAccount(name: String, description: String): String {
        val accountId = UUID.randomUUID().toString()
        dao.createAccount(CredentialAccount(accountId, name, description))
        return accountId
    }

    override fun accountWithCount(): Flow<List<AccountWithCredentialCount>> {
        return dao.getAllAccountsWithCredentialCount()
    }

    override fun getAccountDetails(accountId: String): Flow<CredentialAccount?> {
        return dao.getAccountDetails(accountId)
    }

    override fun getAccountCredentials(accountId: String): Flow<List<Credential>> {
        return dao.getAccountCredentials(accountId)
    }

    override suspend fun getBiometricInfo(): BiometricInfo = withContext(dispatcher) {
        val biometricAuthStatus = readBiometricAuthStatus()
        val cryptoValidationResult = checkInternalWithCrypto()
        BiometricInfo(
            biometricAuthStatus = biometricAuthStatus,
            keyStatus = when (cryptoValidationResult) {
                ValidationResult.OK -> KeyStatus.READY
                ValidationResult.KEY_INIT_FAIL,
                ValidationResult.VALIDATION_FAILED -> KeyStatus.NOT_READY

                ValidationResult.KEY_PERMANENTLY_INVALIDATED -> {
                    when (cryptoEngine.generateKeyWithResult()) {
                        ValidationResult.OK -> {
                            KeyStatus.INVALIDATED
                        }

                        else -> {
                            KeyStatus.NOT_READY
                        }
                    }
                }
            }
        )
    }

    override suspend fun decryptCredential(
        encryptedValue: String,
        cryptoObject: BiometricPrompt.CryptoObject
    ): String {
        validateCryptoLayer()
        val encryptedDecoded = Base64.decode(encryptedValue, Base64.DEFAULT)
        return cryptoEngine.decrypt(encryptedDecoded, cryptoObject)
    }

    override suspend fun saveCredential(
        credential: Credential,
        cryptoObject: BiometricPrompt.CryptoObject?
    ) = withContext(dispatcher) {
        if (credential.isEncrypted && cryptoObject == null) {
            throw Error("CryptoObject not passed")
        }
        val credentialId = credential.credentialId.ifBlank { UUID.randomUUID().toString() }
        var modifiedCredential = credential.copy(credentialId = credentialId)
        if (modifiedCredential.isEncrypted) {
            validateCryptoLayer()
            val encryptedData =
                cryptoEngine.encrypt(modifiedCredential.credentialValue, cryptoObject!!)
            modifiedCredential = modifiedCredential.copy(
                credentialValue = Base64.encodeToString(
                    encryptedData.data,
                    Base64.DEFAULT
                ),
                encryptionIv = Base64.encodeToString(encryptedData.iv, Base64.DEFAULT)
            )
        }
        dao.upsertCredential(modifiedCredential)
    }

    override suspend fun createCryptoObject(
        purpose: CryptoPurpose,
        ivString: String?
    ): BiometricPrompt.CryptoObject = withContext(dispatcher) {
        val iv: ByteArray? = when (purpose) {
            CryptoPurpose.Encryption -> null
            CryptoPurpose.Decryption -> {
                Base64.decode(ivString, Base64.DEFAULT)
            }
        }
        cryptoEngine.createCryptoObject(purpose, iv)
    }

    private suspend fun checkInternalWithCrypto(): ValidationResult = withContext(dispatcher) {
        val validationResult = cryptoEngine.validate()
        when (validationResult) {
            ValidationResult.KEY_PERMANENTLY_INVALIDATED,
            ValidationResult.KEY_INIT_FAIL -> {
                clearCryptoAndData()
            }

            else -> {}
        }
        validationResult
    }


    private suspend fun clearCryptoAndData() {
        cryptoEngine.clear()
        dao.deleteAllCredentials()
        dao.deleteAllAccounts()
    }

    private suspend fun validateCryptoLayer() {
        val status = checkInternalWithCrypto()
        if (status != ValidationResult.OK) {
            throw InvalidCryptoLayerException(status)
        }
    }

    private fun readBiometricAuthStatus() =
        when (biometricManager.canAuthenticate(requiredAuthenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricAuthStatus.READY
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricAuthStatus.NOT_AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricAuthStatus.TEMPORARY_NOT_AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricAuthStatus.AVAILABLE_BUT_NOT_ENROLLED
            else -> BiometricAuthStatus.NOT_AVAILABLE
        }
}