package com.example.examenreclycerview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.color.MaterialColors

class AlumnoAdapter(
    private val onSeleccionar: (Int?) -> Unit
) : RecyclerView.Adapter<AlumnoAdapter.ViewHolder>() {

    private var alumnos: List<Alumno> = emptyList()
    private var seleccionadoId: Int? = null

    fun actualizar(estado: EstadoAlumnos) {
        alumnos = estado.alumnos
        seleccionadoId = estado.alumnoSeleccionadoId
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alumno_temp, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val alumno = alumnos[position]

        holder.nombre.text = "${alumno.nombre} ${alumno.apellidos}"
        holder.curso.text = "${alumno.curso} · ${alumno.edad} años"
        holder.email.text = alumno.email
        holder.activo.text = if (alumno.activo) "Activo" else "Inactivo"

        val colorAttr = if (alumno.activo) {
            com.google.android.material.R.attr.colorOnPrimary
        } else {
            com.google.android.material.R.attr.colorErrorContainer
        }
        holder.activo.setTextColor(
            MaterialColors.getColor(holder.itemView, colorAttr)
        )

        val seleccionado = alumno.id == seleccionadoId
        holder.check.setOnCheckedChangeListener(null)
        holder.check.isChecked = seleccionado
        holder.card.isChecked = seleccionado

        holder.check.setOnCheckedChangeListener { _, marcado ->
            if (marcado) {
                onSeleccionar(alumno.id)
            } else if (alumno.id == seleccionadoId) {
                onSeleccionar(null)
            }
        }

        holder.card.setOnClickListener {
            onSeleccionar(if (seleccionado) null else alumno.id)
        }
    }

    override fun getItemCount(): Int = alumnos.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: MaterialCardView = view.findViewById(R.id.cardAlumno)
        val nombre: TextView = view.findViewById(R.id.tvNombre)
        val curso: TextView = view.findViewById(R.id.tvCurso)
        val email: TextView = view.findViewById(R.id.tvEmail)
        val activo: TextView = view.findViewById(R.id.tvActivo)
        val check: MaterialCheckBox = view.findViewById(R.id.cbSeleccionado)
    }
}
