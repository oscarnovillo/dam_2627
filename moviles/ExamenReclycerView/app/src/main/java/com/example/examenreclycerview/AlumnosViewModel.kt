package com.example.examenreclycerview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AlumnosViewModel : ViewModel() {

    private val _estado = MutableLiveData(
        EstadoAlumnos(
            alumnos = listOf(
                Alumno(
                    1, "Lucía", "Álvarez García", "1º DAM", 18,
                    "lucia@iesquevedo.es", true
                ),
                Alumno(
                    2, "Marco", "Castillo Ruiz", "2º ASIR", 19,
                    "marco@iesquevedo.es", true
                ),
                Alumno(
                    3, "Sofía", "Pérez Martín", "1º SMR", 17,
                    "sofia@iesquevedo.es", false
                )
            )
        )
    )

    val estado: LiveData<EstadoAlumnos> = _estado

    private fun actual(): EstadoAlumnos =
        _estado.value ?: EstadoAlumnos()

    private fun actualizar(nuevoEstado: EstadoAlumnos) {
        _estado.value = nuevoEstado
    }

    fun cambiarFormulario(
        nombre: String,
        apellidos: String,
        curso: String,
        edad: String,
        email: String,
        activo: Boolean
    ) {
        actualizar(
            actual().copy(
                nombre = nombre,
                apellidos = apellidos,
                curso = curso,
                edad = edad,
                email = email,
                activo = activo,
                mensaje = null
            )
        )
    }

    fun seleccionarAlumno(id: Int?) {
        actualizar(actual().copy(
            alumnoSeleccionadoId = id,
            mensaje = null
        ))
    }

    fun anadirAlumno(alumno: Alumno): Boolean {
        val estado = actual()

       actualizar(
            estado.copy(
                alumnos = estado.alumnos + alumno,
                alumnoSeleccionadoId = null,
                mensaje = "Alumno añadido.",
            )
        )
        return true
    }

    fun borrarAlumnoSeleccionado(): Boolean {
        val estado = actual()
        val id = estado.alumnoSeleccionadoId

        if (id == null || estado.alumnos.none { it.id == id }) {
            actualizar(estado.copy(mensaje = "Selecciona un alumno."))
            return false
        }

        actualizar(
            estado.copy(
                alumnos = estado.alumnos.filterNot { it.id == id },
                alumnoSeleccionadoId = null,
                mensaje = "Alumno borrado."
            )
        )
        return true
    }

    fun consumirMensaje() {
        val estado = actual()
        if (estado.mensaje != null) {
            actualizar(estado.copy(mensaje = null))
        }
    }
}
