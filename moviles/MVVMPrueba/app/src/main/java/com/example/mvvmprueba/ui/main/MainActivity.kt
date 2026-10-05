package com.example.mvvmprueba.ui.main

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mvvmprueba.R
import com.example.mvvmprueba.databinding.ActivityMainBinding
import com.example.mvvmprueba.di.AppModule

class MainActivity : AppCompatActivity() {

    private val binding : ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private val viewModel : MainViewModel by lazy { AppModule.provideMainViewModel() }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()



        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        observarEstado()

        setupEventos()


    }

    private fun setupEventos() {
        binding.button.setOnClickListener {
        // no se cambian interfaz fuera del observable
            //binding.textView.text = viewModel.damePresidente()

            viewModel.handleDamePresidente()
        }
    }

    private fun observarEstado() {
        viewModel.state.observe(this,{
            it?.presidente.let{
                binding.textView.text = it
            }

            it?.error.let{ error ->
                Toast.makeText(this,error,Toast.LENGTH_SHORT).show()
            }
        })


    }
}