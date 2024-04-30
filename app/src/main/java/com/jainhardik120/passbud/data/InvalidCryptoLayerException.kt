package com.jainhardik120.passbud.data

import com.jainhardik120.passbud.data.crypto.ValidationResult

class InvalidCryptoLayerException(validationResult: ValidationResult) : Exception() {

    val isKeyPermanentlyInvalidated =
        validationResult == ValidationResult.KEY_PERMANENTLY_INVALIDATED

    val isKeyInitFailed = validationResult == ValidationResult.KEY_INIT_FAIL
}