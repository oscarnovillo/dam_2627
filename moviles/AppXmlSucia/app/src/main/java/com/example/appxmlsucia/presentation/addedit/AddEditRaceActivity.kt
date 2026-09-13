package com.example.appxmlsucia.presentation.addedit

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.example.appxmlsucia.R
import com.example.appxmlsucia.di.AppModule
import com.example.appxmlsucia.presentation.main.MainActivity

class AddEditRaceActivity : AppCompatActivity() {

    // MALA PRACTICA: findViewById directo. Se mantiene a proposito como didactica.
    private lateinit var etRaceName: EditText
    private lateinit var etRaceCircuit: EditText
    private lateinit var etRaceCountry: EditText
    private lateinit var etRaceLaps: EditText
    private lateinit var btnSaveRace: Button

    // MALA PRACTICA: ViewModel instanciado manualmente via service locator global.
    private val viewModel: AddEditRaceViewModel by lazy {
        AppModule.provideAddEditRaceViewModel(raceId)
    }

    private var raceId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_race)

        // MALA PRACTICA DIDACTICA pero funcional: respetar insets con updatePadding.
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

        etRaceName = findViewById(R.id.etRaceName)
        etRaceCircuit = findViewById(R.id.etRaceCircuit)
        etRaceCountry = findViewById(R.id.etRaceCountry)
        etRaceLaps = findViewById(R.id.etRaceLaps)
        btnSaveRace = findViewById(R.id.btnSaveRace)

        // MALA PRACTICA: clave de extra hardcodeada (en una app real se usa la constante del companion).
        raceId = intent.getIntExtra("race_id", -1)

        viewModel.race.observe(this) { race ->
            if (race != null) {
                etRaceName.setText(race.name)
                etRaceCircuit.setText(race.circuit)
                etRaceCountry.setText(race.country)
                etRaceLaps.setText(race.laps.toString())
                // MALA PRACTICA: setear texto de boton con valor hardcodeado en codigo.
                btnSaveRace.text = "Actualizar"
            } else {
                // MALA PRACTICA: setear texto de boton con valor hardcodeado en codigo.
                btnSaveRace.text = "Agregar"
            }
        }

        viewModel.saveCompleted.observe(this) { isSaved ->
            if (isSaved == true) {
                viewModel.onSaveCompletedHandled()
                finish()
            } else if (isSaved == false) {
                // MALA PRACTICA: Toast con string hardcodeado.
                Toast.makeText(this, "Completa todos los campos correctamente", Toast.LENGTH_SHORT).show()
            }
        }

        btnSaveRace.setOnClickListener {
            viewModel.saveRace(
                etRaceName.text.toString().trim(),
                etRaceCircuit.text.toString().trim(),
                etRaceCountry.text.toString().trim(),
                etRaceLaps.text.toString().trim()
            )
        }
    }
}
