package com.example.mvvmprueba.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mvvmprueba.di.AppModule
import com.example.mvvmprueba.domain.useCases.DamePresidenteUseCase

class MainViewModel(val damePresidenteUseCase: DamePresidenteUseCase) : ViewModel() {


    private val _state = MutableLiveData<MainState>(MainState("inicio"))


    val state : LiveData<MainState> = _state



    // eventos no devuelve nada
    fun handleDamePresidente(): Unit {

        var presi = damePresidenteUseCase.damePresidente()


        _state.value = _state.value?.copy(presidente = presi.toString()) ?: MainState(presi.toString())
    }


    //

}

class MainViewModelFactory(private val damePresidenteUseCase: DamePresidenteUseCase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(damePresidenteUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}