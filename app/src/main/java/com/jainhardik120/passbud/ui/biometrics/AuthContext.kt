package com.jainhardik120.passbud.ui.biometrics

import androidx.biometric.BiometricPrompt
import com.jainhardik120.passbud.util.CryptoPurpose

data class AuthContext(
    val purpose: CryptoPurpose,
    val cryptoObject: BiometricPrompt.CryptoObject
)
