package com.jainhardik120.passbud.ui.screen.account

import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.jainhardik120.passbud.domain.toBankCard
import com.jainhardik120.passbud.ui.biometrics.BiometricPromptContainer
import com.jainhardik120.passbud.ui.biometrics.createPromptInfo
import com.jainhardik120.passbud.ui.biometrics.rememberPromptContainerState
import com.jainhardik120.passbud.ui.navigation.AppRoutes
import com.jainhardik120.passbud.ui.screen.home.CustomAccountCreate

fun NavGraphBuilder.addAccountScreen(
    hostState: SnackbarHostState,
    navController: NavHostController
) {
    composable(
        AppRoutes.AccountScreen.route + "/{accountId}", arguments = listOf(
            navArgument("accountId") {
                type = NavType.StringType
                nullable = false
            }
        )
    ) {
        val viewModel = hiltViewModel<AccountViewModel>()
        AccountScreen(viewModel = viewModel, hostState, navController)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    viewModel: AccountViewModel,
    hostState: SnackbarHostState,
    navController: NavHostController
) {

    LaunchedEffect(key1 = Unit, block = {
        viewModel.uiEvent.collect {
            when (it) {
                is UiEvent.Navigate -> {
                    navController.navigate(it.route)
                }

                UiEvent.NavigateUp -> {
                    navController.navigateUp()
                }
            }
        }
    })

    val state by viewModel.state
    var isAccountEditShown by remember { mutableStateOf(false) }
    var isDeleteAccountShown by remember { mutableStateOf(false) }
    var isDeleteCredentialShown by remember { mutableStateOf(false) }
    var isNewCredentialPromptShown by remember { mutableStateOf(false) }
    val promptContainerState = rememberPromptContainerState()

    val dialogState = rememberCredentialPromptState(state.account.accountId)

    CredentialPromptContainer(dialogState) { credential ->
        viewModel.saveNewKey(credential)
    }

    if (isNewCredentialPromptShown) {
        AlertDialog(
            onDismissRequest = {
                isNewCredentialPromptShown = false
            },
            properties = DialogProperties(
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
                    Row(
                        Modifier.padding(vertical = 24.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "New Account",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    Column(
                        Modifier
                            .padding(horizontal = 32.dp)
                            .padding(bottom = 20.dp)
                    ) {
                        Button(
                            onClick = {
                                isNewCredentialPromptShown = false
                                dialogState.showPrompt(true, 0, null)
                            }, shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = 4.dp,
                                bottomEnd = 4.dp
                            ), modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Text Value")
                        }
                        Button(
                            onClick = {
                                isNewCredentialPromptShown = false
                                dialogState.showPrompt(true, 1, null)
                            }, shape = RoundedCornerShape(
                                bottomEnd = 16.dp,
                                bottomStart = 16.dp,
                                topEnd = 4.dp,
                                topStart = 4.dp
                            ), modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Bank Card")
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(key1 = state.account, block = {
        dialogState.updateAccountId(state.account.accountId)
    })

    var toBeDeletedCredential by remember {
        mutableStateOf("")
    }


    BiometricPromptContainer(
        promptContainerState,
        onAuthSucceeded = { cryptoObj ->
            viewModel.onAuthSucceeded(cryptoObj)
        },
        onAuthError = { authErr ->
            viewModel.onAuthError(authErr.errorCode, authErr.errString)
        }
    )

    if (isDeleteAccountShown) {
        AlertDialog(
            onDismissRequest = { isDeleteAccountShown = false },
            title = { Text(text = "Confirm Delete?") },
            text = {
                Text(text = "Deleting this account would also delete ${state.credentials.size} saved credentials. Are you sure you want to delete this account?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        isDeleteAccountShown = false
                        viewModel.deleteResource(true, state.account.accountId)
                    }) {
                    Text(text = "Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    isDeleteAccountShown = false
                }) {
                    Text(text = "Cancel")
                }
            })
    }
    if (isDeleteCredentialShown) {
        AlertDialog(
            onDismissRequest = { isDeleteCredentialShown = false },
            title = { Text(text = "Confirm Delete?") },
            text = {
                Text(text = "Are you sure you want to delete this credential?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        isDeleteCredentialShown = false
                        if (toBeDeletedCredential.isNotBlank()) {
                            viewModel.deleteResource(false, toBeDeletedCredential)
                        }
                    }) {
                    Text(text = "Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    isDeleteCredentialShown = false
                }) {
                    Text(text = "Cancel")
                }
            })
    }
    if (isAccountEditShown) {
        AlertDialog(
            onDismissRequest = {
                isAccountEditShown = false
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
                    CustomAccountCreate(
                        onCancel = { isAccountEditShown = false },
                        onConfirmCreate = { name, desc ->
                            isAccountEditShown = false
                            viewModel.updateAccountDetails(name, desc)
                        },
                        confirmButtonText = "Save",
                        initialName = state.account.accountName,
                        initialDescription = state.account.accountDescription
                    )
                }
            }
        }
    }

    state.authContext?.let { auth ->
        LaunchedEffect(key1 = auth) {
            val promptInfo = createPromptInfo(auth.purpose)
            promptContainerState.authenticate(promptInfo, auth.cryptoObject)
        }
    }

    state.shouldShowDeletePrompt?.let { request ->
        LaunchedEffect(key1 = request) {
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric verification")
                .setSubtitle(
                    "Verify yourself to delete this ${
                        if (request.isAccount) {
                            "account"
                        } else {
                            "credential"
                        }
                    }"
                )
                .setNegativeButtonText("Cancel")
                .build()
            promptContainerState.authenticate(promptInfo, null)
        }
    }



    Scaffold(topBar = {
        CenterAlignedTopAppBar(title = { Text(text = state.account.accountName) }, actions = {
            var menuExpanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
                IconButton(
                    onClick = { menuExpanded = !menuExpanded }
                ) {
                    Icon(Icons.Rounded.MoreVert, contentDescription = "More Icon")
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(text = { Text(text = "Delete Account") }, onClick = {
                        menuExpanded = false
                        isDeleteAccountShown = true
                    }, leadingIcon = {
                        Icon(Icons.Rounded.Delete, contentDescription = "Delete Icon")
                    })
                    DropdownMenuItem(text = { Text(text = "Edit Details") }, onClick = {
                        menuExpanded = false
                        isAccountEditShown = true
                    }, leadingIcon = {
                        Icon(Icons.Rounded.Edit, contentDescription = "Edit Icon")
                    })
                }
            }

        })
    }, floatingActionButton = {
        FloatingActionButton(onClick = {
            isNewCredentialPromptShown = true
        }) {
            Icon(Icons.Rounded.Add, contentDescription = "Add Icon")
        }
    }, snackbarHost = {
        SnackbarHost(
            hostState = hostState
        )
    }) { paddingValues ->
        Column(Modifier.padding(paddingValues)) {
            LazyColumn(content = {
                itemsIndexed(state.credentials) { index, item ->
                    Surface(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(enabled = item.first.isEncrypted && !item.second) {
                                viewModel.decryptCredentialAt(index)
                            },
                        color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
                    ) {
                        Row(Modifier.fillMaxWidth()) {

                            Column(
                                Modifier
                                    .padding(16.dp)
                                    .weight(1f)
                                    .fillMaxWidth()
                            ) {
                                Row(
                                    Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = item.first.credentialKey,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                                when {
                                    !item.first.isEncrypted -> {
                                        Text(text = item.first.credentialValue)
                                    }

                                    item.first.isEncrypted && !item.second -> {
                                        Text(text = "Tap to reveal")
                                    }

                                    item.first.isEncrypted && item.second -> {
                                        when (item.first.credentialType) {
                                            0 -> {
                                                Text(text = item.first.credentialValue)
                                            }

                                            1 -> {
                                                val card by remember(item.first.credentialValue) {
                                                    mutableStateOf(item.first.credentialValue.toBankCard())
                                                }
                                                card?.let { bankCard ->
                                                    Text(text = bankCard.cardNumber)
                                                    bankCard.validFrom?.let {
                                                        Text(text = it)
                                                    }
                                                    bankCard.validThru?.let {
                                                        Text(text = it)
                                                    }
                                                    bankCard.cvv?.let {
                                                        Text(text = it)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            val shouldShowOptions by remember(item) { mutableStateOf(!item.first.isEncrypted || item.second) }
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                var menuExpanded by remember { mutableStateOf(false) }
                                Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
                                    IconButton(enabled = shouldShowOptions, onClick = {
                                        menuExpanded = !menuExpanded
                                    }) {
                                        Icon(
                                            if (shouldShowOptions) {
                                                Icons.Rounded.MoreVert
                                            } else {
                                                Icons.Rounded.Lock
                                            },
                                            contentDescription = "Options Icon"
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = menuExpanded,
                                        onDismissRequest = { menuExpanded = false }) {
                                        DropdownMenuItem(
                                            text = { Text(text = "Delete") },
                                            onClick = {
                                                menuExpanded = false
                                                toBeDeletedCredential = item.first.credentialId
                                                isDeleteCredentialShown = true
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Rounded.Delete,
                                                    contentDescription = "Delete Icon"
                                                )
                                            })
                                        DropdownMenuItem(text = { Text(text = "Edit") }, onClick = {
                                            menuExpanded = false
                                            dialogState.showPrompt(
                                                false,
                                                item.first.credentialType,
                                                item.first
                                            )
                                        }, leadingIcon = {
                                            Icon(
                                                Icons.Rounded.Edit,
                                                contentDescription = "Edit Icon"
                                            )
                                        })
                                    }
                                }

                            }
                        }
                    }
                }
            }
            )
        }
    }
}



