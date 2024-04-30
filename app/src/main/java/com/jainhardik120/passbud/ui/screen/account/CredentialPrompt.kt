package com.jainhardik120.passbud.ui.screen.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.jainhardik120.passbud.data.local.entities.Credential
import com.jainhardik120.passbud.domain.BankCard
import com.jainhardik120.passbud.domain.toBankCard
import com.jainhardik120.passbud.ui.PasswordGenerator
import com.jainhardik120.passbud.ui.screen.home.CustomTextField

@Composable
fun UsernamePasswordAccount(
    onCancel: () -> Unit,
    onConfirmCreate: (String, String, Boolean) -> Unit,
    initialName: String = "",
    initialPass: String = "",
    shouldShowEncryptButton: Boolean = true,
    confirmButtonText: String = "Create"
) {
    var username by remember { mutableStateOf(initialName) }
    var password by remember { mutableStateOf(initialPass) }
    var isEncrypted by remember {
        mutableStateOf(true)
    }
    val spacing = 20.dp
    Column(
        Modifier
            .padding(spacing)
            .fillMaxWidth()
    ) {
        CustomTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text(text = "Key") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(spacing))
        CustomTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(text = "Value") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        if (shouldShowEncryptButton) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Encrypt")
                Spacer(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
                Switch(checked = isEncrypted, onCheckedChange = { isEncrypted = it })

            }
        }
        PasswordGenerator(onPasswordGenerated = { password = it }, shouldGeneratePassword = false)
        Spacer(modifier = Modifier.height(spacing))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            OutlinedButton(onClick = { onCancel() }) {
                Text(text = "Cancel")
            }
            Spacer(modifier = Modifier.width(spacing))
            Button(
                onClick = {
                    onConfirmCreate(
                        username,
                        password,
                        isEncrypted
                    )
                },
                enabled = username.isNotEmpty() && password.isNotEmpty()
            ) {
                Text(text = confirmButtonText)
            }
        }
    }
}

@Composable
fun ATMCardCreate(
    onCancel: () -> Unit,
    onConfirmCreate: ((String, String) -> Unit),
    name: String = "",
    number: String = "",
    from: String = "",
    to: String = "",
    icvv: String = "",
    confirmButtonText: String = "Create"
) {
    var cardName by remember { mutableStateOf(name) }
    var cardNumber by remember { mutableStateOf(number) }
    var cvv by remember { mutableStateOf(icvv) }
    var validFrom by remember { mutableStateOf(from) }
    var validThru by remember { mutableStateOf(to) }
    val spacing = 20.dp
    val validInputRegex: Regex = "[0-9]+".toRegex()
    Column(
        Modifier
            .padding(spacing)
            .fillMaxWidth()
    ) {
        CustomTextField(
            value = cardName,
            onValueChange = { cardName = it },
            label = { Text(text = "Card Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(spacing))
        Row {
            CustomTextField(
                value = cardNumber,
                onValueChange = {
                    if (it.isEmpty() || (it.length <= 16 && validInputRegex.matches(it))) {
                        cardNumber = it
                    }
                },
                label = { Text(text = "Number") },
                modifier = Modifier
                    .weight(6f)
                    .fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
            )
            Spacer(modifier = Modifier.width(spacing))
            CustomTextField(
                value = cvv,
                onValueChange = {
                    if (it.isEmpty() || (it.length <= 3 && validInputRegex.matches(it))) {
                        cvv = it
                    }
                },
                label = { Text(text = "CVV") },
                modifier = Modifier
                    .weight(3f)
                    .fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                singleLine = true,
                placeholder = { Text(text = "123") }
            )
        }

        Spacer(modifier = Modifier.height(spacing))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {

            CustomTextField(
                value = validFrom,
                onValueChange = {
                    if (it.isEmpty() || (it.length <= 4 && validInputRegex.matches(it))) {
                        validFrom = it
                    }
                },
                label = { Text(text = "Valid From") },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                singleLine = true,
                placeholder = { Text(text = "MMYY") }
            )
            Spacer(modifier = Modifier.width(spacing))
            CustomTextField(
                value = validThru,
                onValueChange = {
                    if (it.isEmpty() || (it.length <= 4 && validInputRegex.matches(it))) {
                        validThru = it
                    }
                },
                label = { Text(text = "Valid Thru") },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                singleLine = true,
                placeholder = { Text(text = "MMYY") }
            )

        }
        Spacer(modifier = Modifier.height(spacing))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            OutlinedButton(onClick = { onCancel() }) {
                Text(text = "Cancel")
            }
            Spacer(modifier = Modifier.width(spacing))
            Button(
                onClick = {
                    onConfirmCreate(
                        cardName,
                        BankCard(cardNumber, validFrom, validThru, cvv).formatToString()
                    )
                },
                enabled = cardName.isNotEmpty() && cardNumber.isNotEmpty()
            ) {
                Text(text = confirmButtonText)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CredentialPromptContainer(state: CredentialPrompt, onConfirm: (Credential) -> Unit) {
    if (state.isPromptToShow.value) {
        AlertDialog(
            onDismissRequest = {
                state.resetShowFlag()
            },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            ),
            modifier = Modifier.padding(14.dp)
        ) {
            Surface(
                shape = AlertDialogDefaults.shape,
                color = AlertDialogDefaults.containerColor,
                tonalElevation = AlertDialogDefaults.TonalElevation,
            ) {
                Column {
                    when (state.type) {
                        0 -> {
                            UsernamePasswordAccount(
                                onCancel = { state.resetShowFlag() },
                                onConfirmCreate = { key, value, isEncrypted ->
                                    if (!state.isNew) {
                                        state.credential?.let {
                                            onConfirm(
                                                it.copy(
                                                    credentialKey = key,
                                                    credentialValue = value
                                                )
                                            )
                                        }
                                    } else {
                                        onConfirm(
                                            Credential(
                                                credentialType = 0,
                                                credentialKey = key,
                                                credentialValue = value,
                                                accountId = state.accountId,
                                                credentialId = "",
                                                isEncrypted = isEncrypted,
                                                encryptionIv = null
                                            )
                                        )
                                    }
                                    state.resetShowFlag()
                                },
                                initialName = if (state.isNew) {
                                    ""
                                } else {
                                    state.credential?.credentialKey ?: ""
                                },
                                initialPass = if (state.isNew) {
                                    ""
                                } else {
                                    state.credential?.credentialValue ?: ""
                                },
                                confirmButtonText = if (state.isNew) {
                                    "Create"
                                } else {
                                    "Save"
                                }, shouldShowEncryptButton = state.isNew
                            )
                        }

                        1 -> {
                            val bankCard = remember {
                                if (!state.isNew) {
                                    state.credential?.credentialValue?.toBankCard() ?: BankCard(
                                        "",
                                        "",
                                        "",
                                        ""
                                    )
                                } else {
                                    BankCard("", "", "", "")
                                }
                            }
                            ATMCardCreate(
                                onCancel = { state.resetShowFlag() },
                                onConfirmCreate = { key, value ->
                                    state.credential?.let {
                                        onConfirm(
                                            it.copy(
                                                credentialKey = key,
                                                credentialValue = value
                                            )
                                        )
                                    }
                                    if (state.isNew) {
                                        onConfirm(
                                            Credential(
                                                credentialType = 1,
                                                credentialKey = key,
                                                credentialValue = value,
                                                accountId = state.accountId,
                                                credentialId = "",
                                                isEncrypted = true,
                                                encryptionIv = null
                                            )
                                        )
                                    }
                                    state.resetShowFlag()
                                },
                                name = if (state.isNew) {
                                    ""
                                } else {
                                    state.credential?.credentialKey ?: ""
                                },
                                number = bankCard.cardNumber,
                                from = bankCard.validFrom ?: "",
                                to = bankCard.validThru ?: "",
                                icvv = bankCard.cvv ?: "",
                                confirmButtonText = if (state.isNew) {
                                    "Create"
                                } else {
                                    "Save"
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

class CredentialPrompt(var accountId: String) {
    private var _isNew: Boolean = true
    private var _type: Int = 0
    private var _credential: Credential? = null

    var isNew: Boolean
        get() = _isNew
        private set(value) {
            _isNew = value
        }

    var type: Int
        get() = _type
        private set(value) {
            _type = value
        }

    var credential: Credential?
        get() = _credential
        private set(value) {
            _credential = value
        }

    private val _isPromptToShow = mutableStateOf(false)
    val isPromptToShow: State<Boolean> = _isPromptToShow

    fun updateAccountId(accountId: String) {
        this.accountId = accountId
    }

    fun showPrompt(isNew: Boolean, type: Int, credential: Credential? = null) {
        this.isNew = isNew
        this.type = type
        this.credential = credential
        _isPromptToShow.value = true
    }

    fun resetShowFlag() {
        _isPromptToShow.value = false
    }
}

@Composable
fun rememberCredentialPromptState(accountId: String): CredentialPrompt = remember {
    CredentialPrompt(accountId)
}