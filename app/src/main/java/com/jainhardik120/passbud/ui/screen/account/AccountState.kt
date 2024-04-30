package com.jainhardik120.passbud.ui.screen.account

import com.jainhardik120.passbud.data.local.entities.Credential
import com.jainhardik120.passbud.data.local.entities.CredentialAccount
import com.jainhardik120.passbud.ui.biometrics.AuthContext

data class AccountState(
    val credentials: List<Pair<Credential, Boolean>> = emptyList(),
    val account: CredentialAccount = CredentialAccount("", "", ""),
    val authContext: AuthContext? = null,
    val shouldShowDeletePrompt: DeleteRequest? = null
)

