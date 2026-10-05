package com.example.mvvmprueba.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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