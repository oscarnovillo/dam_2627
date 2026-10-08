package com.example.mvvmprueba.ui.main

import com.example.mvvmprueba.domain.model.Politico

data class MainState(
    val presidente: String,
    val politicos : List<Politico>,
    val error : String? = null,
)