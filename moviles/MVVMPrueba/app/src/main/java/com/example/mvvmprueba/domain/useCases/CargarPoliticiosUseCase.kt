package com.example.mvvmprueba.domain.useCases

import com.example.mvvmprueba.data.Politicos

class CargarPoliticiosUseCase {

    fun cargarLista() = Politicos.dameLista()
}