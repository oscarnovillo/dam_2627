package com.example.mvvmprueba.domain.useCases

import com.example.mvvmprueba.data.Politicos
import com.example.mvvmprueba.domain.model.Politico

class DelPoliticoUseCase {

    fun delPolitico(p: Politico) = Politicos.del(p)
}