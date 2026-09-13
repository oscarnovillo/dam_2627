package com.example.appxmlsucia.presentation.main

import com.example.appxmlsucia.domain.model.Race

sealed class RaceAction {
    data class Edit(val race: Race) : RaceAction()
    data class Delete(val race: Race) : RaceAction()
}
