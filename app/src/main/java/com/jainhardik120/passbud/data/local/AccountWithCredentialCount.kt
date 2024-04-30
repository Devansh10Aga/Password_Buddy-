package com.jainhardik120.passbud.data.local

data class AccountWithCredentialCount(
    val accountId: String,
    val accountName: String,
    val accountDescription: String,
    val encryptedCount: Int,
    val nonEncryptedCount: Int,
    val cardsCount: Int
)