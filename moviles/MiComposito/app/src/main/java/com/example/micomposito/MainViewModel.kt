package com.example.micomposito

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.micomposito.domain.Pirata
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class MainViewModel : ViewModel() {

    init {
        //cargar datos
    }

    fun onBorrarPirata(pirata: Pirata) {

        _state.update {
            it.copy(
                piratas = _state.value.piratas.filterNot { elemento -> elemento.nombre == pirata.nombre },
                error = "probando ${pirata.nombre}"
            )
        }
    }

    fun onAddPirata() {
        Pirata(apellido = _state.value.textStatePirata.text as String, nombre = "")
    }

    fun onChangePirata(pirata: Pirata) {
        viewModelScope.launch {
//            _state.update { it.copy(loading = true) }
//            delay(4000.milliseconds)

            _state.update {
                it.copy(
                    texto = pirata.nombre, pirata = pirata, loading = false
                )
            }
        }
    }

    fun limpiarError() {
        _state.update {
            it.copy(
                error = null
            )
        }
    }

    private val _state = MutableStateFlow(
        MainState(
            textStatePirata = TextFieldState("Monkey D."),
            pirata = Pirata("luffy", "Monkey D.")
        )
    )

    val state: StateFlow<MainState> = _state.asStateFlow()


    class MainViewModelFactory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST") return MainViewModel(
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}