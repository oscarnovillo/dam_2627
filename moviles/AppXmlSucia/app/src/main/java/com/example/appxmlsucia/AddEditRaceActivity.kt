package com.example.appxmlsucia

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

class AddEditRaceActivity : AppCompatActivity() {

    // MALA PRACTICA: logica de negocio y validacion directamente en la Activity.
    private var raceId: Int = -1

    private lateinit var etRaceName: EditText
    private lateinit var etRaceCircuit: EditText
    private lateinit var etRaceCountry: EditText
    private lateinit var etRaceLaps: EditText
    private lateinit var btnSaveRace: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_race)

        // BUENA PRACTICA: respetar los insets del sistema sin pisar el padding del XML.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(
                left = v.paddingLeft + systemBars.left,
                top = v.paddingTop + systemBars.top,
                right = v.paddingRight + systemBars.right,
                bottom = v.paddingBottom + systemBars.bottom
            )
            insets
        }

        // MALA PRACTICA: findViewById directo.
        etRaceName = findViewById(R.id.etRaceName)
        etRaceCircuit = findViewById(R.id.etRaceCircuit)
        etRaceCountry = findViewById(R.id.etRaceCountry)
        etRaceLaps = findViewById(R.id.etRaceLaps)
        btnSaveRace = findViewById(R.id.btnSaveRace)

        // MALA PRACTICA: clave de extra hardcodeada.
        raceId = intent.getIntExtra("race_id", -1)

        if (raceId != -1) {
            // Modo edicion: buscar carrera y popular campos.
            val race = RaceRepository.getById(raceId)
            if (race != null) {
                etRaceName.setText(race.name)
                etRaceCircuit.setText(race.circuit)
                etRaceCountry.setText(race.country)
                etRaceLaps.setText(race.laps.toString())
                // MALA PRACTICA: setear texto de boton con valor hardcodeado en codigo.
                btnSaveRace.text = "Actualizar"
            }
        } else {
            // MALA PRACTICA: setear texto de boton con valor hardcodeado en codigo.
            btnSaveRace.text = "Agregar"
        }

        btnSaveRace.setOnClickListener {
            saveRace()
        }
    }

    private fun saveRace() {
        // MALA PRACTICA: validacion de negocio en la Activity con textos hardcodeados.
        val name = etRaceName.text.toString().trim()
        val circuit = etRaceCircuit.text.toString().trim()
        val country = etRaceCountry.text.toString().trim()
        val lapsStr = etRaceLaps.text.toString().trim()

        if (name.isEmpty() || circuit.isEmpty() || country.isEmpty() || lapsStr.isEmpty()) {
            // MALA PRACTICA: string hardcodeado.
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val laps = lapsStr.toIntOrNull()
        if (laps == null || laps <= 0) {
            Toast.makeText(this, "Vueltas invalidas", Toast.LENGTH_SHORT).show()
            return
        }

        if (raceId != -1) {
            val updated = Race(raceId, name, circuit, country, laps)
            RaceRepository.update(updated)
            Toast.makeText(this, "Carrera actualizada", Toast.LENGTH_SHORT).show()
        } else {
            val newRace = Race(0, name, circuit, country, laps)
            RaceRepository.add(newRace)
            Toast.makeText(this, "Carrera agregada", Toast.LENGTH_SHORT).show()
        }

        finish()
    }
}
