package me.aartikov.sesame.form.control

import kotlinx.coroutines.CoroutineScope
import me.aartikov.sesame.localizedstring.LocalizedString
import me.aartikov.sesame.property.PropertyHost
import me.aartikov.sesame.property.command
import me.aartikov.sesame.property.computed
import me.aartikov.sesame.property.state

class InputControl(
    override val propertyHostScope: CoroutineScope,
    initialText: String = "",
    val singleLine: Boolean = true,
    val maxLength: Int = Int.MAX_VALUE,
    val keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    val filter: ((Char) -> Boolean)? = null,
    val formatter: InputFormatter? = null
) : ValidatableControl<String>, PropertyHost {

    var text: String by state(initialText)
    var visible: Boolean by state(true)
    var enabled: Boolean by state(true)
    var hasFocus: Boolean by state(false)
    override var error: LocalizedString? by state(null)

    val scrollToIt = command<Unit>()

    override val value by ::text

    override val skipInValidation by computed(::visible, ::enabled) { visible, enabled ->
        !visible || !enabled
    }

    fun onTextChanged(text: String) {
        this.text = text
    }

    fun onFocusChanged(hasFocus: Boolean) {
        this.hasFocus = hasFocus
    }

    override fun requestFocus() {
        scrollToIt()
        hasFocus = true
    }
}

fun PropertyHost.InputControl(
    initialText: String = "",
    singleLine: Boolean = true,
    maxLength: Int = Int.MAX_VALUE,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    filter: ((Char) -> Boolean)? = null,
    formatter: InputFormatter? = null
): InputControl {

    return InputControl(
        propertyHostScope,
        initialText,
        singleLine,
        maxLength,
        keyboardOptions,
        filter,
        formatter
    )
}