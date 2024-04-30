package com.jainhardik120.passbud.ui.screen.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.jainhardik120.passbud.R
import com.jainhardik120.passbud.domain.BankCard
import com.jainhardik120.passbud.ui.PasswordGenerator
import com.jainhardik120.passbud.ui.biometrics.BiometricPromptContainer
import com.jainhardik120.passbud.ui.biometrics.createPromptInfo
import com.jainhardik120.passbud.ui.biometrics.rememberPromptContainerState
import com.jainhardik120.passbud.ui.navigation.AppRoutes
import kotlinx.coroutines.launch

fun NavGraphBuilder.addHomeScreen(hostState: SnackbarHostState, navigate: (String) -> Unit) {
    composable(
        AppRoutes.Home.route
    ) {
        val viewModel = hiltViewModel<HomeViewModel>()
        HomeScreen(viewModel = viewModel, navigate, hostState)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel, navigate: (String) -> Unit, hostState: SnackbarHostState) {
    var sheetExpanded by remember { mutableStateOf(false) }
    val state by viewModel.state
    val promptContainerState = rememberPromptContainerState()
    BiometricPromptContainer(
        promptContainerState,
        onAuthSucceeded = { cryptoObj ->
            viewModel.onAuthSucceeded(cryptoObj)
        },
        onAuthError = { authErr ->
            viewModel.onAuthError(authErr.errorCode, authErr.errString)
        }
    )

    state.authContext?.let { auth ->
        LaunchedEffect(key1 = auth) {
            promptContainerState.authenticate(createPromptInfo(auth.purpose), auth.cryptoObject)
        }
    }

    val topAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(topBar = {

        LargeTopAppBar(title = {
            Text(
                text = LocalContext.current.resources.getString(
                    R.string.app_name
                )
            )
        }, scrollBehavior = topAppBarScrollBehavior)
    }, floatingActionButton = {
        FloatingActionButton(onClick = { sheetExpanded = !sheetExpanded }) {
            Icon(Icons.Rounded.Add, contentDescription = "Add Icon")
        }
    }, snackbarHost = {
        SnackbarHost(
            hostState = hostState
        )
    }, modifier = Modifier
        .fillMaxSize()
        .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        Column(Modifier.padding(paddingValues)) {
            AccountCreationDialog(
                isShown = sheetExpanded,
                changeVisibility = { sheetExpanded = it },
                viewModel::createAccount,
                viewModel::createAccountAndCredentials,
                viewModel::createAccountAndCard
            )
            when (state.appStatus) {
                AppStatus.READY -> {
                    LazyColumn(content = {
                        itemsIndexed(state.accounts) { _, item ->
                            Surface(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable {
                                        navigate(AppRoutes.AccountScreen.withArgs(item.accountId))
                                    }, color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Column(
                                    Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth()
                                ) {
                                    Text(
                                        text = item.accountName,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))

                                    fun buildAccompanyingText(
                                        encryptedCount: Int,
                                        nonEncryptedCount: Int,
                                        cardsCount: Int
                                    ): String {
                                        val counts = mutableListOf<String>()
                                        if (encryptedCount > 0) {
                                            counts.add("$encryptedCount Locked Value")
                                        }
                                        if (nonEncryptedCount > 0) {
                                            counts.add("$nonEncryptedCount Open Value")
                                        }
                                        if (cardsCount > 0) {
                                            counts.add("$cardsCount Bank Card")
                                        }
                                        if (counts.isEmpty()) {
                                            return "No Credentials"
                                        }
                                        return counts.joinToString(", ")
                                    }

                                    val accompanyingText by remember(
                                        item.encryptedCount,
                                        item.nonEncryptedCount,
                                        item.cardsCount
                                    ) {
                                        mutableStateOf(
                                            buildAccompanyingText(
                                                item.encryptedCount,
                                                item.nonEncryptedCount,
                                                item.cardsCount
                                            )
                                        )
                                    }
                                    Text(accompanyingText)
                                }
                            }
                        }
                    })
                }

                AppStatus.NOT_READY -> {
                    Text(text = "Service not available at the moment")
                }

                AppStatus.UNAVAILABLE -> {
                    Text(text = "Service not available in your device")
                }
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AccountCreationDialog(
    isShown: Boolean,
    changeVisibility: (Boolean) -> Unit,
    createAccount: (String, String) -> Unit,
    createAccountAndCredentials: (String, String, String, String) -> Unit,
    createAccountAndCard: (String, String, String) -> Unit
) {
    if (isShown) {
        var selectedAccountType: AccountTypes by remember { mutableStateOf(AccountTypes.UsernamePassword) }
        val horizontalPagerState = rememberPagerState()
        val scope = rememberCoroutineScope()

        @Composable
        fun CustomButton(shape: RoundedCornerShape, accountType: AccountTypes) {
            Button(onClick = {
                selectedAccountType = accountType
                scope.launch {
                    horizontalPagerState.animateScrollToPage(1)
                }
            }, shape = shape, modifier = Modifier.fillMaxWidth()) {
                Text(text = accountType.displayName)
            }
        }

        fun hideSheet() {
            changeVisibility(false)
        }
        AlertDialog(
            onDismissRequest = {
                changeVisibility(false)
            },
            properties = DialogProperties(
                dismissOnBackPress = horizontalPagerState.currentPage == 0,
                dismissOnClickOutside = horizontalPagerState.currentPage == 0,
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
                    HorizontalPager(
                        state = horizontalPagerState,
                        pageCount = 2,
                        modifier = Modifier.fillMaxWidth(),
                        userScrollEnabled = false
                    ) { page ->
                        when (page) {
                            0 -> {
                                Column(
                                    Modifier
                                        .padding(horizontal = 32.dp)
                                        .padding(bottom = 20.dp)
                                ) {
                                    CustomButton(
                                        shape = RoundedCornerShape(
                                            topStart = 16.dp,
                                            topEnd = 16.dp,
                                            bottomStart = 4.dp,
                                            bottomEnd = 4.dp
                                        ), accountType = AccountTypes.UsernamePassword
                                    )
                                    CustomButton(
                                        shape = RoundedCornerShape(4.dp),
                                        accountType = AccountTypes.ATMCard
                                    )
                                    CustomButton(
                                        shape = RoundedCornerShape(
                                            bottomEnd = 16.dp,
                                            bottomStart = 16.dp,
                                            topEnd = 4.dp,
                                            topStart = 4.dp
                                        ), accountType = AccountTypes.Custom
                                    )
                                }
                            }

                            1 -> {
                                when (selectedAccountType) {
                                    AccountTypes.ATMCard -> {
                                        ATMCardCreate(onCancel = {
                                            hideSheet()
                                        }, onConfirmCreate = { n1, n2, c ->
                                            createAccountAndCard(n1, n2, c)
                                            hideSheet()
                                        })
                                    }

                                    AccountTypes.Custom -> {
                                        CustomAccountCreate(
                                            onCancel = {
                                                hideSheet()
                                            },
                                            onConfirmCreate = { name, desc ->
                                                createAccount(name, desc)
                                                hideSheet()
                                            }
                                        )
                                    }

                                    AccountTypes.UsernamePassword -> {
                                        UsernamePasswordAccount(
                                            onCancel = { hideSheet() },
                                            onConfirmCreate = { name, desc, username, pass ->
                                                createAccountAndCredentials(
                                                    name,
                                                    desc,
                                                    username,
                                                    pass
                                                )
                                                hideSheet()
                                            }
                                        )
                                    }
                                }
                            }

                            else -> {

                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomAccountCreate(
    onCancel: () -> Unit,
    onConfirmCreate: ((String, String) -> Unit),
    confirmButtonText: String = "Create",
    initialName: String = "",
    initialDescription: String = "",
) {
    var accountName by remember { mutableStateOf("") }
    var accountDescription by remember { mutableStateOf("") }
    LaunchedEffect(key1 = Unit, block = {
        accountName = initialName
        accountDescription = initialDescription
    })
    val spacing = 20.dp
    Column(
        Modifier
            .padding(spacing)
            .fillMaxWidth()
    ) {
        CustomTextField(
            value = accountName,
            onValueChange = { accountName = it },
            label = { Text(text = "Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(spacing))
        CustomTextField(
            value = accountDescription,
            onValueChange = { accountDescription = it },
            label = { Text(text = "Description (optional)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(spacing))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            OutlinedButton(onClick = { onCancel() }) {
                Text(text = "Cancel")
            }
            Spacer(modifier = Modifier.width(spacing))
            Button(
                onClick = { onConfirmCreate(accountName, accountDescription) },
                enabled = accountName.isNotEmpty()
            ) {
                Text(text = confirmButtonText)
            }
        }
    }

}

@Composable
fun ATMCardCreate(
    onCancel: () -> Unit,
    onConfirmCreate: ((String, String, String) -> Unit)
) {
    var accountName by remember { mutableStateOf("") }
    var cardName by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var validFrom by remember { mutableStateOf("") }
    var validThru by remember { mutableStateOf("") }
    val spacing = 20.dp
    val validInputRegex: Regex = "[0-9]+".toRegex()
    Column(
        Modifier
            .padding(spacing)
            .fillMaxWidth()
    ) {
        CustomTextField(
            value = accountName,
            onValueChange = { accountName = it },
            label = { Text(text = "Account Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(spacing))
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
                        accountName,
                        cardName,
                        BankCard(cardNumber, validFrom, validThru, cvv).formatToString()
                    )
                },
                enabled = accountName.isNotEmpty() && cardName.isNotEmpty() && cardNumber.isNotEmpty()
            ) {
                Text(text = "Create")
            }
        }
    }
}


@Composable
fun UsernamePasswordAccount(
    onCancel: () -> Unit,
    onConfirmCreate: (String, String, String, String) -> Unit
) {
    var accountName by remember { mutableStateOf("") }
    var accountDescription by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val spacing = 20.dp
    Column(
        Modifier
            .padding(spacing)
            .fillMaxWidth()
    ) {
        CustomTextField(
            value = accountName,
            onValueChange = { accountName = it },
            label = { Text(text = "Account Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(spacing))
        CustomTextField(
            value = accountDescription,
            onValueChange = { accountDescription = it },
            label = { Text(text = "Description (optional)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(spacing))
        CustomTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text(text = "Username") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(spacing))
        CustomTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(text = "Password") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        PasswordGenerator(onPasswordGenerated = { password = it })
        Spacer(modifier = Modifier.height(spacing))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            OutlinedButton(onClick = { onCancel() }) {
                Text(text = "Cancel")
            }
            Spacer(modifier = Modifier.width(spacing))
            Button(
                onClick = {
                    onConfirmCreate(
                        accountName,
                        accountDescription,
                        username,
                        password
                    )
                },
                enabled = accountName.isNotEmpty() && username.isNotEmpty() && password.isNotEmpty()
            ) {
                Text(text = "Create")
            }
        }
    }
}

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    label: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    placeholder: @Composable (() -> Unit)? = null,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        label = label,
        isError = isError,
        placeholder = placeholder,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines
    )
}

@Preview(showBackground = true)
@Composable
fun CustomAccountPreview() {
    CustomAccountCreate(onCancel = {}, onConfirmCreate = { _, _ -> })
}

@Preview(showBackground = true)
@Composable
fun UsernamePasswordPreview() {
    UsernamePasswordAccount(onCancel = {}, onConfirmCreate = { _, _, _, _ -> })
}

sealed class AccountTypes(val displayName: String) {
    object UsernamePassword : AccountTypes("Username & Password")
    object ATMCard : AccountTypes("ATM Card")
    object Custom : AccountTypes("Custom")
}