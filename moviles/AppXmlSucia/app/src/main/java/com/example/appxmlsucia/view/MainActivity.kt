package com.example.appxmlsucia.view

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appxmlsucia.R
import com.example.appxmlsucia.adapter.RaceAdapter
import com.example.appxmlsucia.viewmodel.MainViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    // MALA PRACTICA: findViewById en la Activity. Se mantiene a proposito como didactica.
    private lateinit var rvRaces: RecyclerView
    private lateinit var fabAddRace: FloatingActionButton

    // BUENA PRACTICA: delegado viewModels() para obtener el ViewModel.
    private val viewModel: MainViewModel by viewModels()

    // MALA PRACTICA: callback en el adapter en vez de Actions bien tipadas.
    private val adapter = RaceAdapter(
        onEditClick = { race -> openAddEdit(race.id) },
        onDeleteClick = { race -> confirmDelete(race) }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

        rvRaces = findViewById(R.id.rvRaces)
        fabAddRace = findViewById(R.id.fabAddRace)

        rvRaces.layoutManager = LinearLayoutManager(this)
        rvRaces.adapter = adapter

        // BUENA PRACTICA: observar LiveData desde el ViewModel.
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
        // MALA PRACTICA: startActivity directo sin Navigation Component y con clave hardcodeada.
        val intent = Intent(this, AddEditRaceActivity::class.java)
        intent.putExtra(EXTRA_RACE_ID, raceId)
        startActivity(intent)
    }

    private fun confirmDelete(race: com.example.appxmlsucia.model.Race) {
        // MALA PRACTICA: strings hardcodeados en codigo.
        AlertDialog.Builder(this)
            .setTitle("Borrar carrera")
            .setMessage("Seguro que queres borrar " + race.name + "?")
            .setPositiveButton("Si") { _, _ ->
                viewModel.deleteRace(race.id)
            }
            .setNegativeButton("No", null)
            .show()
    }

    companion object {
        // MALA PRACTICA: clave de extra publica y hardcodeada.
        const val EXTRA_RACE_ID = "race_id"
    }
}
