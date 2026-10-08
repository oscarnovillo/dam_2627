package com.example.mvvmprueba.di

import com.example.mvvmprueba.domain.useCases.AddPoliticoUseCase
import com.example.mvvmprueba.domain.useCases.CargarPoliticiosUseCase
import com.example.mvvmprueba.domain.useCases.DamePresidenteUseCase
import com.example.mvvmprueba.domain.useCases.DelPoliticoUseCase
import com.example.mvvmprueba.ui.main.MainViewModel


// MALA PRACTICA: service locator global, luego deberia ser reemplazado por DI real (Koin/Hilt).
object AppModule {


    val damePresidenteUseCase: DamePresidenteUseCase = DamePresidenteUseCase()
    val addPoliticoUseCase: AddPoliticoUseCase = AddPoliticoUseCase()
    val delPoliticoUseCase: DelPoliticoUseCase = DelPoliticoUseCase()
    val cargarPoliticiosUseCase: CargarPoliticiosUseCase = CargarPoliticiosUseCase()
//
//    fun provideMainViewModel(): MainViewModel = MainViewModel(raceRepository)
//
//    fun provideAddEditRaceViewModel(raceId: Int): AddEditRaceViewModel =
//        AddEditRaceViewModel(raceRepository, raceId)
}
