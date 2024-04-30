package com.jainhardik120.passbud.data.local.entities

import androidx.room.Entity

@Entity(tableName = "credential_table", primaryKeys = ["accountId", "credentialId"])
data class Credential(
    val accountId : String,
    val credentialId : String,
    val credentialKey : String,
    val credentialValue : String,
    val credentialType : Int,
    val isEncrypted : Boolean,
    val encryptionIv : String?
)