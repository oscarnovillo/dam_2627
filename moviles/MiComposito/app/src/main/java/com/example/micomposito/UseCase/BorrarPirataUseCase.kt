package com.example.micomposito.UseCase

import com.example.micomposito.domain.Pirata
import jakarta.inject.Inject

class BorrarPirataUseCase @Inject constructor() {

    operator fun invoke(piratas: List<Pirata>, nombre:String) =


        piratas.filterNot { elemento -> elemento.nombre == nombre }

}