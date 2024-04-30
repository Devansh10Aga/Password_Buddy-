package com.jainhardik120.passbud.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.jainhardik120.passbud.data.local.entities.Credential
import com.jainhardik120.passbud.data.local.entities.CredentialAccount
import kotlinx.coroutines.flow.Flow

@Dao
interface CredentialsDao {

    @Query("SELECT * FROM credentials_account_table ORDER BY accountName")
    fun getAllAccounts(): Flow<List<CredentialAccount>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createAccount(account: CredentialAccount)

    @Upsert
    suspend fun upsertCredential(credential: Credential)

    @Query("SELECT * FROM credentials_account_table WHERE accountId=:accountId")
    fun getAccountDetails(accountId: String): Flow<CredentialAccount?>

    @Query("SELECT * FROM credential_table WHERE accountId=:accountId ORDER BY credentialKey")
    fun getAccountCredentials(accountId: String): Flow<List<Credential>>

    @Query(
        "SELECT a.accountId, a.accountName, a.accountDescription," +
                " COUNT(CASE WHEN c.isEncrypted = 1 AND c.credentialType=0 THEN 1 ELSE NULL END) AS encryptedCount, " +
                " COUNT(CASE WHEN c.isEncrypted = 0 THEN 1 ELSE NULL END) AS nonEncryptedCount , " +
                " COUNT(CASE WHEN c.credentialType = 1 THEN 1 ELSE NULL END) AS cardsCount " +
                " FROM credentials_account_table a LEFT JOIN credential_table c ON a.accountId = c.accountId GROUP BY a.accountId"
    )
    fun getAllAccountsWithCredentialCount(): Flow<List<AccountWithCredentialCount>>

    @Update
    suspend fun updateAccount(account: CredentialAccount)

    @Query("DELETE FROM credentials_account_table WHERE accountId= :accountId")
    suspend fun deleteAccount(accountId: String)

    @Query("DELETE FROM credential_table WHERE accountId = :accountId")
    suspend fun deleteAccountCredentials(accountId: String)

    @Query("DELETE FROM credential_table WHERE credentialId = :credentialId")
    suspend fun deleteCredential(credentialId: String)

    @Query("DELETE FROM credential_table")
    suspend fun deleteAllCredentials()

    @Query("DELETE FROM credentials_account_table")
    suspend fun deleteAllAccounts()

}

