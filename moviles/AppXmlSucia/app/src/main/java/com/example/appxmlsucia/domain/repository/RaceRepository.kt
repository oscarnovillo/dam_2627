package com.example.appxmlsucia.domain.repository

import com.example.appxmlsucia.domain.model.Race

// Contrato del repositorio en la capa de dominio.
// La capa de dominio no conoce implementaciones: solo define el contrato.
interface RaceRepository {
    fun getAll(): List<Race>
    fun getById(id: Int): Race?
    fun add(race: Race)
    fun update(race: Race)
    fun delete(id: Int)
}
