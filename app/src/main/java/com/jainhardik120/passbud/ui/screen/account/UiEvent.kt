package com.jainhardik120.passbud.ui.screen.account

sealed class UiEvent {
    data class Navigate(val route: String) : UiEvent()
    object NavigateUp : UiEvent()
}