package com.example.appxmlsucia

// Modelo simple de carrera. Toda la app usa este data class directamente.
data class Race(
    val id: Int,
    val name: String,
    val circuit: String,
    val country: String,
    val laps: Int
)
