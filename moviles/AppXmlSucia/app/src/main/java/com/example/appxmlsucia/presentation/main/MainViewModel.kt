package com.example.appxmlsucia.presentation.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.appxmlsucia.domain.model.Race
import com.example.appxmlsucia.domain.repository.RaceRepository

class MainViewModel(
    private val raceRepository: RaceRepository
) : ViewModel() {

    private val _races = MutableLiveData(MainState())
    val races: LiveData<MainState> = _races

    init {
        loadRaces()
    }

    fun loadRaces() {
        // MALA PRACTICA: carga sincronica directa al repositorio in-memory.
        // En una app real se usaria Flow/StateFlow y corrutinas con Dispatchers.IO.
//        _races.value = MainState(
//            races = raceRepository.getAll(),
//            error = _races.value?.error,
//        )
        _races.value = _races.value?.copy(races= raceRepository.getAll())
    }

    fun deleteRace(id: Int) {
        if (id %2 == 0) {
            _races.value = _races.value?.copy(error= "estoy cansado")

        }
            else{
        raceRepository.delete(id)
        loadRaces()}
    }

    fun limpiarError() {
        _races.value = _races.value?.copy(error= null)
    }
}
