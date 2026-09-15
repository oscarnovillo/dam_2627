package com.example.appxmlsucia.presentation.addedit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.appxmlsucia.R
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

    private val _errorMessage = MutableLiveData<Int?>()
    val errorMessage: LiveData<Int?> = _errorMessage

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

    fun saveRace(name: String, circuit: String, country: String, lapsStr: String) {
        if (name.isBlank() || circuit.isBlank() || country.isBlank() || lapsStr.isBlank()) {
            _errorMessage.value = R.string.error_fill_fields
            return
        }

        val laps = lapsStr.toIntOrNull()
        if (laps == null || laps <= 0) {
            _errorMessage.value = R.string.error_invalid_laps
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

    fun onErrorMessageHandled() {
        _errorMessage.value = null
    }
}
