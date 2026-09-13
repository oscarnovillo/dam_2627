package com.example.appxmlsucia.di

import com.example.appxmlsucia.data.repository.InMemoryRaceRepository
import com.example.appxmlsucia.domain.repository.RaceRepository
import com.example.appxmlsucia.presentation.addedit.AddEditRaceViewModel
import com.example.appxmlsucia.presentation.main.MainViewModel

// MALA PRACTICA: service locator global, luego deberia ser reemplazado por DI real (Koin/Hilt).
object AppModule {

    val raceRepository: RaceRepository = InMemoryRaceRepository

    fun provideMainViewModel(): MainViewModel = MainViewModel(raceRepository)

    fun provideAddEditRaceViewModel(raceId: Int): AddEditRaceViewModel =
        AddEditRaceViewModel(raceRepository, raceId)
}
