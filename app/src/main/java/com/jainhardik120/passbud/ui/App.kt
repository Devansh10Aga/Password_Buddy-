package com.jainhardik120.passbud.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.jainhardik120.passbud.ui.navigation.AppRoutes
import com.jainhardik120.passbud.ui.navigation.PassbudAppNavHost
import com.jainhardik120.passbud.ui.theme.PassbudTheme


@Composable
fun App() {
    PassbudTheme(darkTheme = false, dynamicColor = false) {
        val appState = rememberAppState()
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            PassbudAppNavHost(
                navController = appState.navController,
                startDestination = AppRoutes.Home.route,
                hostState = appState.snackBarHostState
            )
        }
    }
}