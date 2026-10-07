package com.example.appxmlsucia.presentation.main

import com.example.appxmlsucia.domain.model.Race

data class MainState(
    val races : List<Race> = ArrayList(),
    val error : String?  = null,
)
