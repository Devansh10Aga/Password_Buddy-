package com.jainhardik120.passbud.ui.biometrics

import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.AuthenticationCallback
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.jainhardik120.passbud.util.findActivity

@Composable
fun BiometricPromptContainer(
    state: BiometricPromptContainerState,
    onAuthSucceeded: (cryptoObject: BiometricPrompt.CryptoObject?) -> Unit = {},
    onAuthError: (AuthError) -> Unit = {},
) {
    val callback = remember(state) {
        object : AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                state.resetShowFlag()
                onAuthError(AuthError(errorCode, errString.toString()))
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                state.resetShowFlag()
                onAuthSucceeded(result.cryptoObject)
            }
        }
    }

    val showPrompt: Boolean by state.isPromptToShow
    if (showPrompt) {
        val activity = LocalContext.current.findActivity()
        LaunchedEffect(key1 = state.cryptoObject) {
            val prompt = BiometricPrompt(activity!!, callback)
            if (state.cryptoObject == null) {
                prompt.authenticate(state.promptInfo!!)
            } else {
                prompt.authenticate(state.promptInfo!!, state.cryptoObject!!)
            }
        }
    }

}

class BiometricPromptContainerState {
    private var _cryptoObject: BiometricPrompt.CryptoObject? = null
    private var _promptInfo: PromptInfo? = null

    var promptInfo: PromptInfo?
        get() = _promptInfo
        private set(value) {
            _promptInfo = value
        }

    var cryptoObject: BiometricPrompt.CryptoObject?
        get() = _cryptoObject
        private set(value) {
            _cryptoObject = value
        }

    private val _isPromptToShow = mutableStateOf(false)
    val isPromptToShow: State<Boolean> = _isPromptToShow

    fun authenticate(promptInfo: PromptInfo, cryptoObject: BiometricPrompt.CryptoObject?) {
        this.promptInfo = promptInfo
        this.cryptoObject = cryptoObject
        _isPromptToShow.value = true
    }

    fun resetShowFlag() {
        _isPromptToShow.value = false
    }
}

@Composable
fun rememberPromptContainerState(): BiometricPromptContainerState = remember {
    BiometricPromptContainerState()
}


