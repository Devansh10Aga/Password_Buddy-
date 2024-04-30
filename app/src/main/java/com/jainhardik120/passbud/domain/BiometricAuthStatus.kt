package com.jainhardik120.passbud.domain

enum class BiometricAuthStatus {
    READY,
    NOT_AVAILABLE,
    TEMPORARY_NOT_AVAILABLE,
    AVAILABLE_BUT_NOT_ENROLLED
}