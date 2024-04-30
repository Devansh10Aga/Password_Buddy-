package com.jainhardik120.passbud.ui.screen.home

import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jainhardik120.passbud.data.local.AccountWithCredentialCount
import com.jainhardik120.passbud.data.local.entities.Credential
import com.jainhardik120.passbud.domain.BiometricAuthStatus
import com.jainhardik120.passbud.domain.BiometricInfo
import com.jainhardik120.passbud.domain.CredentialsRepository
import com.jainhardik120.passbud.domain.KeyStatus
import com.jainhardik120.passbud.ui.biometrics.AuthContext
import com.jainhardik120.passbud.ui.snackbar.SnackbarManager
import com.jainhardik120.passbud.util.CryptoPurpose
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val credentialsRepository: CredentialsRepository
) : ViewModel() {

    private val _state = mutableStateOf(HomeState())
    val state: State<HomeState> = _state

    private var _pendingEncryptCredential: Credential? = null

    init {
        credentialsRepository.accountWithCount().onEach {
            _state.value = state.value.copy(accounts = it)
        }.launchIn(viewModelScope)
        viewModelScope.launch {
            val biometricInfo = credentialsRepository.getBiometricInfo()
            reduceBiometricInfo(biometricInfo)
        }
    }


    private suspend fun prepareAuthContext(
        purpose: CryptoPurpose,
        ivString: String? = null
    ): AuthContext {
        val cryptoObject = credentialsRepository.createCryptoObject(purpose, ivString)
        return AuthContext(
            purpose = purpose,
            cryptoObject = cryptoObject
        )
    }


    private fun showMessage(message: String) {
        SnackbarManager.showMessage(message)
    }


    fun onAuthSucceeded(cryptoObject: BiometricPrompt.CryptoObject?) {
        val pendingAuthContext = _state.value.authContext
        _state.value = _state.value.copy(authContext = null)
        viewModelScope.launch {
            pendingAuthContext?.let { authContext ->
                if (authContext.purpose == CryptoPurpose.Encryption) {
                    _pendingEncryptCredential?.let {
                        credentialsRepository.saveCredential(
                            it,
                            cryptoObject
                        )
                    }
                    _pendingEncryptCredential = null
                } else {
                    if (cryptoObject != null) {

                    }
                }
            }
        }
    }

    fun onAuthError(errorCode: Int, errString: String) {
        _pendingEncryptCredential = null
        when (errorCode) {
            BiometricPrompt.ERROR_USER_CANCELED,
            BiometricPrompt.ERROR_CANCELED,
            BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {

            }

            else -> {
                showMessage(errString)
            }
        }
        _state.value = _state.value.copy(authContext = null)
    }


    private fun reduceBiometricInfo(info: BiometricInfo) {
        _state.value =
            _state.value.copy(
                appStatus = if (info.biometricAuthStatus == BiometricAuthStatus.READY && info.keyStatus != KeyStatus.NOT_READY) {
                    AppStatus.READY
                } else if (info.biometricAuthStatus == BiometricAuthStatus.NOT_AVAILABLE) {
                    AppStatus.UNAVAILABLE
                } else {
                    AppStatus.NOT_READY
                }
            )
        if (info.keyStatus == KeyStatus.INVALIDATED) {
            SnackbarManager.showMessage("Your key was invalidated and app data is reset")
        }
    }

    fun createAccount(accountName: String, accountDescription: String) {
        viewModelScope.launch {
            credentialsRepository.createAccount(accountName, accountDescription)
        }
    }

    fun createAccountAndCard(
        accountName: String,
        cardName: String,
        cardDetails: String
    ) {
        viewModelScope.launch {
            try {
                val accountId = credentialsRepository.createAccount(accountName, "")
                _pendingEncryptCredential = Credential(
                    accountId,
                    "",
                    cardName,
                    cardDetails,
                    1,
                    true,
                    null
                )
                _state.value =
                    _state.value.copy(authContext = prepareAuthContext(CryptoPurpose.Encryption))
            } catch (e: Exception) {
                showMessage(e.message ?: "Error Occurred")
            }
        }
    }

    fun createAccountAndCredentials(
        accountName: String,
        accountDescription: String,
        username: String,
        password: String
    ) {
        viewModelScope.launch {
            try {
                val accountId = credentialsRepository.createAccount(accountName, accountDescription)
                credentialsRepository.saveCredential(
                    Credential(
                        accountId = accountId,
                        credentialId = "",
                        credentialKey = "Username",
                        credentialValue = username,
                        credentialType = 0,
                        isEncrypted = false,
                        encryptionIv = null
                    )
                )
                _pendingEncryptCredential = Credential(
                    accountId,
                    "",
                    "Password",
                    password,
                    0,
                    true,
                    null
                )
                _state.value =
                    _state.value.copy(authContext = prepareAuthContext(CryptoPurpose.Encryption))
            } catch (e: Exception) {
                showMessage(e.message ?: "Error Occurred")
            }
        }
    }

}

data class HomeState(
    val accounts: List<AccountWithCredentialCount> = emptyList(),
    val appStatus: AppStatus = AppStatus.READY,
    val authContext: AuthContext? = null
)

enum class AppStatus {
    READY,
    NOT_READY,
    UNAVAILABLE
}