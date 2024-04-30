package com.jainhardik120.passbud.ui

import android.content.res.Resources
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.jainhardik120.passbud.ui.snackbar.SnackbarManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class AppState(
    val navController: NavHostController,
    val snackBarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope,
    private val resources: Resources,
    private val snackbarManager: SnackbarManager,
) {
    init {
        coroutineScope.launch {
            snackbarManager.messages.collect { currentMessages ->
                if (currentMessages.isNotEmpty()) {
                    val message = currentMessages[0]
                    val text = if (message.messageResId != null) {
                        resources.getText(message.messageResId)
                    } else {
                        message.messageText
                    }
                    snackBarHostState.showSnackbar(text.toString())
                    snackbarManager.setMessageShown(message.id)
                }
            }
        }
    }
}


@Composable
fun rememberAppState(
    navController: NavHostController = rememberNavController(),
    snackBarHostState: SnackbarHostState = remember {
        SnackbarHostState()
    },
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    resources: Resources = resourcesAsComposable(),
) = remember(navController, snackBarHostState, coroutineScope, resources) {
    AppState(navController, snackBarHostState, coroutineScope, resources, SnackbarManager)
}

@Composable
@ReadOnlyComposable
private fun resourcesAsComposable(): Resources {
    LocalConfiguration.current
    return LocalContext.current.resources
}
