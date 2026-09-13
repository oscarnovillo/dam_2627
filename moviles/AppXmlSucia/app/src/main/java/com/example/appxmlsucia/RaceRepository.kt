package com.example.appxmlsucia

// MALA PRACTICA: singleton mutable en memoria con datos precargados.
// En una app real esto deberia venir de una base de datos, API, etc.
object RaceRepository {

    private val races = mutableListOf(
        Race(1, "Gran Premio de Argentina", "Autodromo Juan y Oscar Galvez", "Argentina", 72),
        Race(2, "Gran Premio de Espania", "Circuit de Barcelona-Catalunya", "Espania", 66),
        Race(3, "Gran Premio de Monaco", "Circuit de Monaco", "Monaco", 78),
        Race(4, "Gran Premio de Italia", "Autodromo Nazionale Monza", "Italia", 53)
    )

    private var nextId = races.maxOfOrNull { it.id }?.plus(1) ?: 1

    fun getAll(): List<Race> = races.toList()

    fun getById(id: Int): Race? = races.find { it.id == id }

    fun add(race: Race) {
        val toAdd = race.copy(id = nextId++)
        races.add(toAdd)
    }

    fun update(race: Race) {
        val index = races.indexOfFirst { it.id == race.id }
        if (index != -1) {
            races[index] = race
        }
    }

    fun delete(id: Int) {
        races.removeAll { it.id == id }
    }
}
