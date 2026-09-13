package com.example.appxmlsucia.presentation.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.appxmlsucia.domain.model.Race
import com.example.appxmlsucia.domain.repository.RaceRepository

class MainViewModel(
    private val raceRepository: RaceRepository
) : ViewModel() {

    private val _races = MutableLiveData<List<Race>>()
    val races: LiveData<List<Race>> = _races

    init {
        loadRaces()
    }

    fun loadRaces() {
        // MALA PRACTICA: carga sincronica directa al repositorio in-memory.
        // En una app real se usaria Flow/StateFlow y corrutinas con Dispatchers.IO.
        _races.value = raceRepository.getAll()
    }

    fun deleteRace(id: Int) {
        raceRepository.delete(id)
        loadRaces()
    }
}
