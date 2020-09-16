package me.aartikov.androidarchitecture.base

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.createViewModelLazy
import androidx.lifecycle.LifecycleOwner
import me.aartikov.lib.data_binding.PropertyObserver
import me.aartikov.lib.navigation.FragmentNavigationMessageDispatcher
import me.aartikov.lib.widget.WidgetObserver
import kotlin.reflect.KClass

open class BaseScreen<VM : BaseViewModel>(
    @LayoutRes contentLayoutId: Int,
    vmClass: KClass<VM>
) : Fragment(contentLayoutId), PropertyObserver, WidgetObserver {

    override val propertyObserverLifecycleOwner: LifecycleOwner get() = viewLifecycleOwner
    override val widgetObserverLifecycleOwner: LifecycleOwner get() = viewLifecycleOwner
    val vm: VM by createViewModelLazy(vmClass, { viewModelStore })

    private val navigationMessageDispatcher = FragmentNavigationMessageDispatcher(this)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.navigationMessages bind { navigationMessageDispatcher.dispatch(it) }
        vm.showErrorCommand bind {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }
    }
}