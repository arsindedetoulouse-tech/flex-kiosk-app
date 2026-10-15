package com.arsinde.flexkioskapp.domain.home

/**
 * Model of the home domain.
 *
 * Models live in the shared domain module so that several features can describe the same subject
 * without depending on each other's feature modules.
 */
data class HomeModel(
    val id: String,
)