package com.example.appxmlsucia.data.repository

import com.example.appxmlsucia.domain.model.Race
import com.example.appxmlsucia.domain.repository.RaceRepository

// MALA PRACTICA: base de datos estatica mutable en memoria.
// Esto es un singleton mutable que sobrevive a los ciclos de vida de la app.
// En una app real deberia usarse Room, DataStore, API remota, etc.
object InMemoryRaceRepository : RaceRepository {

    private val races = mutableListOf(
        Race(1, "Gran Premio de Argentina", "Autodromo Juan y Oscar Galvez", "Argentina", 72),
        Race(2, "Gran Premio de Espania", "Circuit de Barcelona-Catalunya", "Espania", 66),
        Race(3, "Gran Premio de Monaco", "Circuit de Monaco", "Monaco", 78),
        Race(4, "Gran Premio de Italia", "Autodromo Nazionale Monza", "Italia", 53)
    )

    private var nextId = races.maxOfOrNull { it.id }?.plus(1) ?: 1

    override fun getAll(): List<Race> = races.toList()

    override fun getById(id: Int): Race? = races.find { it.id == id }

    override fun add(race: Race) {
        val toAdd = race.copy(id = nextId++)
        races.add(toAdd)
    }

    override fun update(race: Race) {
        val index = races.indexOfFirst { it.id == race.id }
        if (index != -1) {
            races[index] = race
        }
    }

    override fun delete(id: Int) {
        races.removeAll { it.id == id }
    }
}
