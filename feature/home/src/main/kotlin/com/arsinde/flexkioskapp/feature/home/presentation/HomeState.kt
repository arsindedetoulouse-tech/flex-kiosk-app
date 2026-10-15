package com.arsinde.flexkioskapp.feature.home.presentation

import com.arsinde.flexkioskapp.domain.home.HomeModel

data class HomeState(
    val isLoading: Boolean = false,
    val model: HomeModel? = null,
    val error: HomeError? = null,
)

/**
 * Failure of the home screen the user can act on.
 *
 * The state carries the kind of failure instead of the text of the exception, so the presentation
 * decides what to show and a failure message of the implementation never becomes part of the UI.
 */
enum class HomeError {
    Unknown,
    Network,
}

sealed interface HomeSideEffect