package me.aartikov.androidarchitecture.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import me.aartikov.lib.core.navigation.NavigationMessage
import me.aartikov.lib.core.property.PropertyHost
import me.aartikov.lib.core.property.command

open class BaseViewModel : ViewModel(), PropertyHost {

    override val propertyHostScope get() = viewModelScope

    val navigate = command<NavigationMessage>()
    val showError = command<String>()

    protected fun showError(e: Throwable) {
        showError(e.message ?: "Error")
    }
}