package com.arsinde.flexkioskapp.domain.home

/**
 * Application entry point of the home domain.
 *
 * The ViewModel talks to this use case and never to the repository directly, so business rules added
 * here stay out of the presentation layer.
 */
class HomeUseCase(
    private val repository: HomeRepository,
) {
    suspend operator fun invoke(): HomeModel? = repository.load()
}