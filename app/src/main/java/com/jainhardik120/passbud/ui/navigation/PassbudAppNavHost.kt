package com.jainhardik120.passbud.ui.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.jainhardik120.passbud.ui.screen.account.addAccountScreen
import com.jainhardik120.passbud.ui.screen.home.addHomeScreen

@Composable
fun PassbudAppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    startDestination: String = AppRoutes.Home.route,
    hostState: SnackbarHostState
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination
    ) {
        addHomeScreen(hostState = hostState) { navController.navigate(it) }
        addAccountScreen(hostState, navController )
    }
}