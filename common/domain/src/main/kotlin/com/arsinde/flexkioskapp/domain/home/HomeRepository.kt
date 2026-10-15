package com.arsinde.flexkioskapp.domain.home

interface HomeRepository {
    suspend fun load(): HomeModel?
}