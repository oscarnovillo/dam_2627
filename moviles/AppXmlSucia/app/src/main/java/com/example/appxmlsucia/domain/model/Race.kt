package com.example.appxmlsucia.domain.model

// Entidad pura de dominio. No depende de Android ni de ningun framework.
data class Race(
    val id: Int,
    val name: String,
    val circuit: String,
    val country: String,
    val laps: Int
)
