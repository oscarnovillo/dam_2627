package com.example.appxmlsucia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.appxmlsucia.model.Race
import com.example.appxmlsucia.repository.RaceRepository

// MALA PRACTICA: el ViewModel depende directamente del singleton statico RaceRepository.
// En una app real se inyectaria el repositorio por constructor (DI).
class MainViewModel : ViewModel() {

    private val _races = MutableLiveData<List<Race>>()
    val races: LiveData<List<Race>> = _races

    init {
        loadRaces()
    }

    fun loadRaces() {
        // MALA PRACTICA: cargar datos sincronicamente desde un singleton mutable en memoria.
        _races.value = RaceRepository.getAll()
    }

    fun deleteRace(id: Int) {
        // MALA PRACTICA: logica de borrado delegada al repositorio mutable estatico.
        RaceRepository.delete(id)
        loadRaces()
    }
}
