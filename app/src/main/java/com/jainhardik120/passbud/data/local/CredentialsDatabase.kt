package com.jainhardik120.passbud.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jainhardik120.passbud.data.local.entities.Credential
import com.jainhardik120.passbud.data.local.entities.CredentialAccount

@Database(entities = [Credential::class, CredentialAccount::class], version = 1, exportSchema = false)
abstract class CredentialsDatabase : RoomDatabase() {
    abstract val dao : CredentialsDao
}