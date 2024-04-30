package com.jainhardik120.passbud.ui.biometrics

import androidx.biometric.BiometricPrompt
import com.jainhardik120.passbud.util.CryptoPurpose

fun createPromptInfo(purpose: CryptoPurpose): BiometricPrompt.PromptInfo {
    return if (purpose == CryptoPurpose.Encryption) {
        BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric verification")
            .setSubtitle("Verify yourself to encrypt and store data")
            .setNegativeButtonText("Cancel")
            .build()
    } else {
        BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric verification")
            .setSubtitle("Verify yourself to view credentials")
            .setNegativeButtonText("Cancel")
            .build()
    }
}