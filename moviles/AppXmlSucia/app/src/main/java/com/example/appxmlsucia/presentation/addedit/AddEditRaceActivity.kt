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
import com.example.appxmlsucia.presentation.common.NavArgs

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

        raceId = intent.getIntExtra(NavArgs.EXTRA_RACE_ID, -1)

        viewModel.race.observe(this) { race ->
            if (race != null) {
                etRaceName.setText(race.name)
                etRaceCircuit.setText(race.circuit)
                etRaceCountry.setText(race.country)
                etRaceLaps.setText(race.laps.toString())
                btnSaveRace.setText(R.string.button_update)
            } else {
                btnSaveRace.setText(R.string.button_add)
            }
        }

        viewModel.errorMessage.observe(this) { errorResId ->
            errorResId?.let {
                Toast.makeText(this, getString(it), Toast.LENGTH_SHORT).show()
                viewModel.onErrorMessageHandled()
            }
        }

        viewModel.saveCompleted.observe(this) { isSaved ->
            if (isSaved == true) {
                viewModel.onSaveCompletedHandled()
                val feedbackRes = if (raceId != -1) R.string.race_updated else R.string.race_added
                Toast.makeText(this, getString(feedbackRes), Toast.LENGTH_SHORT).show()
                finish()
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
