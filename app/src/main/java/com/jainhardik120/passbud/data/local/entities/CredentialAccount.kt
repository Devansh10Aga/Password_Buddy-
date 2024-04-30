package com.jainhardik120.passbud.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName= "credentials_account_table")
data class CredentialAccount(
    @PrimaryKey
    val accountId : String,
    val accountName: String,
    val accountDescription : String
)