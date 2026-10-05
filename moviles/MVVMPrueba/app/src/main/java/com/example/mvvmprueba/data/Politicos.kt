package com.example.mvvmprueba.data

import com.example.mvvmprueba.domain.model.Politico

object Politicos {


   private val politicos = mutableListOf(
       Politico("fernando","BASURILLAS",0),
       Politico("MAriCarmen","Panteras Grises",0,)
   )


    fun damePresidente() = politicos[1]

}