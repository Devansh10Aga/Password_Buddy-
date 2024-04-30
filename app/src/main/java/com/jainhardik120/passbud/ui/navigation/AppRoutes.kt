package com.jainhardik120.passbud.ui.navigation

sealed class AppRoutes(val route : String){
    object Home : AppRoutes(route = "home")
    object AccountScreen : AppRoutes(route = "account_display")
    fun withArgs(vararg args: String):String{
        return buildString {
            append(route)
            args.forEach { arg->
                append("/$arg")
            }
        }
    }
}

