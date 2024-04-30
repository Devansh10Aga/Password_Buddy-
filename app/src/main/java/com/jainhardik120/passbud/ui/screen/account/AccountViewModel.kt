package com.jainhardik120.passbud.ui.screen.account

import android.util.Log
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jainhardik120.passbud.data.local.entities.Credential
import com.jainhardik120.passbud.domain.CredentialsRepository
import com.jainhardik120.passbud.ui.biometrics.AuthContext
import com.jainhardik120.passbud.ui.snackbar.SnackbarManager
import com.jainhardik120.passbud.util.CryptoPurpose
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val credentialsRepository: CredentialsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private fun sendUiEvent(event: UiEvent) {
        viewModelScope.launch {
            _uiEvent.send(event)
        }
    }

    private val _state = mutableStateOf(AccountState())
    val state: State<AccountState> = _state
    private var _decryptIndex = -1

    private var pendingEncryptionCredential: Credential? = null

    init {
        val accountId = savedStateHandle.get<String>("accountId")
        Log.d("TAG", "accountId: $accountId")
        if (accountId == null) {
            sendUiEvent(UiEvent.NavigateUp)
        } else {
            credentialsRepository.getAccountDetails(accountId).onEach {
                if (it == null) {
                    sendUiEvent(UiEvent.NavigateUp)
                } else {
                    _state.value = _state.value.copy(account = it)
                }
            }.launchIn(viewModelScope)
            credentialsRepository.getAccountCredentials(accountId).onEach { credentialList ->
                _state.value =
                    _state.value.copy(credentials = credentialList.map { Pair(it, false) })
            }.launchIn(viewModelScope)
        }
    }


    fun saveNewKey(credential: Credential) {
        viewModelScope.launch {
            if (credential.isEncrypted) {
                try {
                    pendingEncryptionCredential = credential
                    _state.value =
                        _state.value.copy(authContext = prepareAuthContext(CryptoPurpose.Encryption))
                } catch (e: Exception) {
                    showMessage(e.message ?: "Error Occurred")
                }
            } else {
                saveCredential(credential)
            }
        }
    }

    fun deleteResource(isAccount: Boolean, resourceId: String) {
        _state.value =
            _state.value.copy(shouldShowDeletePrompt = DeleteRequest(isAccount, resourceId))
    }

    fun onAuthSucceeded(cryptoObject: BiometricPrompt.CryptoObject?) {
        val pendingAuthContext = _state.value.authContext
        val pendingDeleteRequest = _state.value.shouldShowDeletePrompt
        _state.value = _state.value.copy(authContext = null, shouldShowDeletePrompt = null)
        viewModelScope.launch {
            pendingAuthContext?.let { authContext ->
                if (authContext.purpose == CryptoPurpose.Encryption) {
                    pendingEncryptionCredential?.let {
                        saveCredential(it, cryptoObject)
                    }
                    pendingEncryptionCredential = null
                } else {
                    if (cryptoObject != null) {
                        decryptCredential(cryptoObject)
                    }
                    _decryptIndex = -1
                }
            }
            pendingDeleteRequest?.let {
                println("Delete Request Approved")
                if (it.isAccount) {
                    credentialsRepository.deleteAccount(it.deleteObjectId)
                } else {
                    credentialsRepository.deleteCredential(it.deleteObjectId)
                }
            }
        }
    }

    private suspend fun decryptCredential(cryptoObject: BiometricPrompt.CryptoObject) {
        try {
            val decrypted = credentialsRepository.decryptCredential(
                _state.value.credentials[_decryptIndex].first.credentialValue,
                cryptoObject
            )
            _state.value =
                _state.value.copy(credentials = _state.value.credentials.mapIndexed { index, credential ->
                    if (index != _decryptIndex) credential else Pair(
                        credential.first.copy(
                            credentialValue = decrypted
                        ), true
                    )
                })
        } catch (e: Exception) {
            println(e.printStackTrace())
            showMessage(e.message ?: "Error Occurred")
        }
    }

    private suspend fun saveCredential(
        credential: Credential,
        cryptoObject: BiometricPrompt.CryptoObject? = null
    ) {
        try {
            credentialsRepository.saveCredential(
                credential, cryptoObject
            )
        } catch (e: Exception) {
            showMessage(e.message ?: "Error Occurred")
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

    fun updateAccountDetails(name: String, description: String) {
        viewModelScope.launch {
            credentialsRepository.updateAccountDetails(
                _state.value.account.copy(
                    accountName = name,
                    accountDescription = description
                )
            )
        }
    }


    private fun showMessage(message: String) {
        SnackbarManager.showMessage(message)
    }

    fun onAuthError(errorCode: Int, errString: String) {
        when (errorCode) {
            BiometricPrompt.ERROR_USER_CANCELED,
            BiometricPrompt.ERROR_CANCELED,
            BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {

            }

            else -> {
                showMessage(errString)
            }
        }
        if (_state.value.authContext?.purpose == CryptoPurpose.Decryption) {
            _decryptIndex = -1
        }
        _state.value = _state.value.copy(authContext = null, shouldShowDeletePrompt = null)
    }

    fun decryptCredentialAt(index: Int) {
        _decryptIndex = index
        viewModelScope.launch {
            try {
                _state.value =
                    _state.value.copy(
                        authContext = prepareAuthContext(
                            CryptoPurpose.Decryption,
                            _state.value.credentials[index].first.encryptionIv
                        )
                    )
            } catch (e: Exception) {
                e.printStackTrace()
                println(e.printStackTrace())
                showMessage(e.message ?: "Error Occurred")
            }
        }
    }
}


