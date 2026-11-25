package com.example.examenreclycerview

data class EstadoAlumnos(
    val alumnos: List<Alumno> = emptyList(),
    val alumnoSeleccionadoId: Int? = null,
    val nombre: String = "",
    val apellidos: String = "",
    val curso: String = "1º DAM",
    val edad: String = "",
    val email: String = "",
    val activo: Boolean = true,
    val mensaje: String? = null
)
