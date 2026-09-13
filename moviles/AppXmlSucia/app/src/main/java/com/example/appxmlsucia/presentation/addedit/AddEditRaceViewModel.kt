package com.example.appxmlsucia.presentation.addedit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.appxmlsucia.domain.model.Race
import com.example.appxmlsucia.domain.repository.RaceRepository

class AddEditRaceViewModel(
    private val raceRepository: RaceRepository,
    private val raceId: Int
) : ViewModel() {

    private val _race = MutableLiveData<Race?>()
    val race: LiveData<Race?> = _race

    private val _saveCompleted = MutableLiveData<Boolean>()
    val saveCompleted: LiveData<Boolean> = _saveCompleted

    init {
        loadRace()
    }

    private fun loadRace() {
        if (raceId != -1) {
            _race.value = raceRepository.getById(raceId)
        } else {
            _race.value = null
        }
    }

    // MALA PRACTICA: strings de error hardcodeados en el ViewModel.
    // Deberian vivir en strings.xml y mappearse como UiText.
    fun saveRace(name: String, circuit: String, country: String, lapsStr: String) {
        if (name.isBlank() || circuit.isBlank() || country.isBlank() || lapsStr.isBlank()) {
            _saveCompleted.value = false
            return
        }

        val laps = lapsStr.toIntOrNull()
        if (laps == null || laps <= 0) {
            _saveCompleted.value = false
            return
        }

        if (raceId != -1) {
            raceRepository.update(Race(raceId, name, circuit, country, laps))
        } else {
            raceRepository.add(Race(0, name, circuit, country, laps))
        }

        _saveCompleted.value = true
    }

    fun onSaveCompletedHandled() {
        _saveCompleted.value = false
    }
}
