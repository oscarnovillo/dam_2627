package com.example.myapplication.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {


    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

      //setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)

        // 3. Pasar la raíz del binding a setContentView
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val t = binding.filledTextField
        t.editText?.setText("Nuevo texto")

        val caja = binding.txcajaTexto
        caja.setText("oscar")

        val s = t.editText?.text ?: "por defecto"

        val button : Button = binding.button
        button.setOnClickListener {
            t.editText?.setText("Nuevo texto")
        }


        val btLAnzar = binding.buttonLanzaPantalla
        btLAnzar.setOnClickListener {

            val intent = Intent(this, DetalleActivity::class.java)
            intent.putExtra("id", 1)
            startActivity(intent)
        }

    }


    private fun openAddEdit(raceId: Int) {
        // MALA PRACTICA: startActivity directo sin Navigation Component.

    }
}