package com.lwg.challenge.feature.main

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.lwg.challenge.navigation.NavigateAction
import com.lwg.challenge.navigation.Route

class MainNavigator(
    private val backStack: NavBackStack<NavKey>,
) : NavigateAction {

    override fun navigateTo(route: Route) {
        backStack.add(route)
    }

    override fun popBackStack() {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
        }
    }

    override fun popTo(route: Route) {
        val index = backStack.indexOfLast { it == route }
        if (index >= 0) {
            while (backStack.lastIndex > index) {
                backStack.removeAt(backStack.lastIndex)
            }
        }
    }

    override fun switchTab(route: Route) {
        backStack.clear()
        backStack.add(route)
    }
}
