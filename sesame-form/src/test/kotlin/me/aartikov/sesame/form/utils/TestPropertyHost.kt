package me.aartikov.sesame.form.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.TestCoroutineScope
import me.aartikov.sesame.property.PropertyHost

open class TestPropertyHost(
    override val propertyHostScope: CoroutineScope = TestCoroutineScope()
) : PropertyHost