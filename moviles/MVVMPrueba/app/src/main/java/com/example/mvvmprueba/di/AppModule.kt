package com.example.mvvmprueba.di

import com.example.mvvmprueba.domain.useCases.DamePresidenteUseCase
import com.example.mvvmprueba.ui.main.MainViewModel


// MALA PRACTICA: service locator global, luego deberia ser reemplazado por DI real (Koin/Hilt).
object AppModule {
    fun provideMainViewModel(): MainViewModel  = MainViewModel(damePresidenteUseCase)

    val damePresidenteUseCase: DamePresidenteUseCase = DamePresidenteUseCase()
//
//    fun provideMainViewModel(): MainViewModel = MainViewModel(raceRepository)
//
//    fun provideAddEditRaceViewModel(raceId: Int): AddEditRaceViewModel =
//        AddEditRaceViewModel(raceRepository, raceId)
}
