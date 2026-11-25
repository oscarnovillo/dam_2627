package com.example.examenreclycerview

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.examenreclycerview.databinding.ActivityAlumnosBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class AlumnosActivity : AppCompatActivity() {

    private lateinit var viewModel: AlumnosViewModel
    private lateinit var adapter: AlumnoAdapter

   private lateinit var binding : ActivityAlumnosBinding

    // Evita que pintar el estado vuelva a enviar cambios al ViewModel.
    private var pintandoEstado = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
            binding = ActivityAlumnosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[AlumnosViewModel::class.java]

        val recycler = findViewById<RecyclerView>(R.id.rvAlumnos)
        recycler.layoutManager = LinearLayoutManager(this)
        adapter = AlumnoAdapter(viewModel::seleccionarAlumno)
        recycler.adapter = adapter


        binding.btnAAdirAlumno.setOnClickListener {
            val nuevoAlumno = Alumno(
                id = 0,
                nombre = binding.tilNombre.editText?.text.toString().trim(),
                apellidos = binding.tilApellidos.editText?.text.toString().trim(),
                curso = binding.tilCurso.editText?.text.toString().trim(),
                edad = binding.tilEdad.editText?.text.toString().trim().toIntOrNull() ?: 0,
                email = binding.tilEmail.editText?.text.toString().trim(),
                activo = binding.cbActivo.isChecked
            )
            viewModel.anadirAlumno(nuevoAlumno)
        }

        binding.btnBorrarAlumno.setOnClickListener {
            viewModel.borrarAlumnoSeleccionado()
        }

        viewModel.estado.observe(this) { estado ->
            mostrarEstado(estado)
            estado.mensaje?.let { mensaje ->
                Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
                viewModel.consumirMensaje()
            }
        }
    }



    private fun mostrarEstado(estado: EstadoAlumnos) {
        adapter.actualizar(estado)

    }

    private fun texto(view: TextView): String =
        view.text?.toString().orEmpty()

    private fun asignar(campo: TextInputEditText, valor: String) {
        if (texto(campo) != valor) campo.setText(valor)
    }

    private fun asignar(campo: AutoCompleteTextView, valor: String) {
        if (texto(campo) != valor) campo.setText(valor, false)
    }
}
