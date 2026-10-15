package com.arsinde.flexkioskapp.feature.home.presentation

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.arsinde.flexkioskapp.feature.home.api.Home
import org.koin.androidx.compose.koinViewModel

/**
 * The start screen's contribution to the application navigation graph.
 *
 * It differs from a regular feature section in that it raises no back action: the start screen sits at
 * the base of the back stack, and the system back gesture leaves the application. Passing a navigator
 * here would offer a button that cannot do anything.
 */
val homeSection: EntryProviderScope<NavKey>.() -> Unit = {
    entry<Home> {
        val viewModel = koinViewModel<HomeViewModel>()
        HomeScreen(
            state = viewModel.container.stateFlow.collectAsStateWithLifecycle().value,
            onRefresh = viewModel::onRefresh,
        )
    }
}