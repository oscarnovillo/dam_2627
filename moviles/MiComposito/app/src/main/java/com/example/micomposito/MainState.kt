package com.example.micomposito

import androidx.compose.foundation.text.input.TextFieldState
import com.example.micomposito.domain.Pirata

data class MainState(

    val textStatePirata: TextFieldState,

    val loading: Boolean = false,
    val texto : String = "",
    val nombre: String = "",
    val pirata : Pirata,


    )
