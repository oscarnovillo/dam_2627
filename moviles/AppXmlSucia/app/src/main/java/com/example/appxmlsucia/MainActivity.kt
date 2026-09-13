package com.example.appxmlsucia

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    // MALA PRACTICA: logica de negocio y estado de UI directamente en la Activity.
    private lateinit var rvRaces: RecyclerView
    private lateinit var fabAddRace: FloatingActionButton
    private val adapter = RaceAdapter()

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

        // MALA PRACTICA: findViewById en la Activity.
        rvRaces = findViewById(R.id.rvRaces)
        fabAddRace = findViewById(R.id.fabAddRace)

        rvRaces.layoutManager = LinearLayoutManager(this)
        rvRaces.adapter = adapter

        // MALA PRACTICA: cargar datos directo desde un singleton estatico.
        adapter.submitList(RaceRepository.getAll())

        fabAddRace.setOnClickListener {
            // MALA PRACTICA: startActivity directo sin Navigation Component y con extras hardcodeados.
            val intent = Intent(this, AddEditRaceActivity::class.java)
            intent.putExtra("race_id", -1)
            startActivity(intent)
        }
    }

    // MALA PRACTICA: recargar toda la lista desde el repositorio al volver.
    override fun onResume() {
        super.onResume()
        adapter.submitList(RaceRepository.getAll())
    }
}
