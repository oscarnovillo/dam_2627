package com.example.appxmlsucia.presentation.main

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appxmlsucia.R
import com.example.appxmlsucia.di.AppModule
import com.example.appxmlsucia.presentation.addedit.AddEditRaceActivity
import com.example.appxmlsucia.presentation.common.NavArgs
import com.example.appxmlsucia.domain.model.Race
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    // MALA PRACTICA: findViewById en la Activity. Se mantiene a proposito como didactica.
    private lateinit var rvRaces: RecyclerView
    private lateinit var fabAddRace: FloatingActionButton

    // MALA PRACTICA: ViewModel instanciado manualmente via service locator global.
    // Deberia inyectarse con by viewModels() + Hilt/Koin.
    private val viewModel: MainViewModel by lazy { AppModule.provideMainViewModel() }

    private val adapter = RaceAdapter { action ->
        when (action) {
            is RaceAction.Edit -> openAddEdit(action.race.id)
            is RaceAction.Delete -> confirmDelete(action.race)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

        rvRaces = findViewById(R.id.rvRaces)
        fabAddRace = findViewById(R.id.fabAddRace)

        rvRaces.layoutManager = LinearLayoutManager(this)
        rvRaces.adapter = adapter

        viewModel.races.observe(this) { races ->
            adapter.submitList(races)
        }

        fabAddRace.setOnClickListener {
            openAddEdit(-1)
        }
    }

    override fun onResume() {
        super.onResume()
        // MALA PRACTICA: recargar lista manualmente desde onResume por falta de observabilidad real del repositorio.
        viewModel.loadRaces()
    }

    private fun openAddEdit(raceId: Int) {
        // MALA PRACTICA: startActivity directo sin Navigation Component.
        val intent = Intent(this, AddEditRaceActivity::class.java)
        intent.putExtra(NavArgs.EXTRA_RACE_ID, raceId)
        startActivity(intent)
    }

    private fun confirmDelete(race: Race) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_delete_title))
            .setMessage(getString(R.string.dialog_delete_message, race.name))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                viewModel.deleteRace(race.id)
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }

}
