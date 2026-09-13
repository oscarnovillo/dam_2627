package com.example.appxmlsucia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.appxmlsucia.model.Race
import com.example.appxmlsucia.repository.RaceRepository

// MALA PRACTICA: ViewModel que depende de singleton statico y no recibe dependencias inyectadas.
class AddEditRaceViewModel : ViewModel() {

    private var raceId: Int = -1

    private val _race = MutableLiveData<Race?>()
    val race: LiveData<Race?> = _race

    private val _saved = MutableLiveData<Boolean>()
    val saved: LiveData<Boolean> = _saved

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadRace(id: Int) {
        raceId = id
        if (id != -1) {
            // MALA PRACTICA: busqueda sincronica directa al singleton estatico.
            _race.value = RaceRepository.getById(id)
        } else {
            _race.value = null
        }
    }

    // MALA PRACTICA: devolver error como String en vez de un tipo sellado o UiText.
    fun saveRace(name: String, circuit: String, country: String, lapsStr: String) {
        _error.value = null

        if (name.isBlank() || circuit.isBlank() || country.isBlank() || lapsStr.isBlank()) {
            // MALA PRACTICA: string hardcodeado en el ViewModel.
            _error.value = "Completa todos los campos"
            return
        }

        val laps = lapsStr.toIntOrNull()
        if (laps == null || laps <= 0) {
            // MALA PRACTICA: string hardcodeado en el ViewModel.
            _error.value = "Vueltas invalidas"
            return
        }

        if (raceId != -1) {
            RaceRepository.update(Race(raceId, name, circuit, country, laps))
        } else {
            // MALA PRACTICA: id dummy 0; el repositorio lo reemplaza internamente.
            RaceRepository.add(Race(0, name, circuit, country, laps))
        }

        _saved.value = true
    }

    fun onSaveHandled() {
        _saved.value = false
    }
}
