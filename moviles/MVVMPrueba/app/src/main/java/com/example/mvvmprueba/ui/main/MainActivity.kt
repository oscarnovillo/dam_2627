package com.example.mvvmprueba.ui.main

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mvvmprueba.R
import com.example.mvvmprueba.data.Politicos
import com.example.mvvmprueba.databinding.ActivityMainBinding
import com.example.mvvmprueba.di.AppModule
import com.example.mvvmprueba.domain.model.Politico

class MainActivity : AppCompatActivity() {

    private val binding : ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private val viewModel : MainViewModel by viewModels { MainViewModelFactory(
        AppModule.damePresidenteUseCase,
        AppModule.cargarPoliticiosUseCase,
        AppModule.addPoliticoUseCase,
        AppModule.delPoliticoUseCase,
    ) }

    private lateinit var adapter : PoliticoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewModel.handleCargaLista()

        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        configRecycler()

        observarEstado()

        setupEventos()


    }

    private fun configRecycler() {
        binding.listado.layoutManager = LinearLayoutManager(this)

        adapter = PoliticoAdapter({ politico->
            viewModel.handleDelete(politico)
        })
        binding.listado.adapter = adapter





    }


    private fun setupEventos() {
        with(binding) {
            button.setOnClickListener {
                // no se cambian interfaz fuera del observable
                //binding.textView.text = viewModel.damePresidente()

                viewModel.handleDamePresidente()
                viewModel.handleCargaLista()
            }

            buttonAdd.setOnClickListener {
                var p = Politico(nombre = "kiko", partido = "rivera")

                viewModel.handleAdd(p)
            }
        }
    }

    private fun observarEstado() {
        viewModel.state.observe(this,{ state ->
           state?.presidente.let{
                binding.textView.text = it
            }

            state?.politicos.let {
                adapter.submitList(it)
            }

            state?.error?.let{ error ->
                Toast.makeText(this,error,Toast.LENGTH_SHORT).show()
                viewModel.handleLimpiarError()
            }
        })


    }
}