package com.arsinde.flexkioskapp.feature.home.presentation

import com.arsinde.flexkioskapp.domain.home.HomeUseCase
import com.arsinde.flexkioskapp.presentation.BaseViewModel
import java.io.IOException
import kotlinx.coroutines.CancellationException
import org.orbitmvi.orbit.syntax.Syntax

class HomeViewModel(
    private val useCase: HomeUseCase,
) : BaseViewModel<
    HomeState,
    HomeSideEffect,
>(
    initialState = HomeState(),
) {
    init {
        intent { load() }
    }

    fun onRefresh() {
        intent { load() }
    }

    private suspend fun Syntax<HomeState, HomeSideEffect>.load() {
        reduce { state.copy(isLoading = true, error = null) }
        try {
            val model = useCase()
            reduce { state.copy(isLoading = false, model = model, error = null) }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (failure: Exception) {
            reduce {
                state.copy(
                    isLoading = false,
                    error = if (failure is IOException) {
                        HomeError.Network
                    } else {
                        HomeError.Unknown
                    },
                )
            }
        }
    }
}