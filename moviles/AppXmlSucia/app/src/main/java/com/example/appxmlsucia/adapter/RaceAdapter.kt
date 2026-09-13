package com.example.appxmlsucia.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appxmlsucia.R
import com.example.appxmlsucia.model.Race

// MALA PRACTICA: adapter que accede directo a Views con findViewById,
// y que ademas expone callbacks en lugar de eventos unidireccionales bien tipados.
class RaceAdapter(
    // MALA PRACTICA: callbacks planos en vez de ViewModel/Actions.
    private val onEditClick: (Race) -> Unit,
    private val onDeleteClick: (Race) -> Unit
) : RecyclerView.Adapter<RaceAdapter.RaceViewHolder>() {

    private var races: List<Race> = emptyList()

    // MALA PRACTICA: actualizar la lista con notifyDataSetChanged en vez de DiffUtil.
    fun submitList(newList: List<Race>) {
        races = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RaceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_race, parent, false)
        return RaceViewHolder(view)
    }

    override fun onBindViewHolder(holder: RaceViewHolder, position: Int) {
        holder.bind(races[position])
    }

    override fun getItemCount(): Int = races.size

    inner class RaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        // MALA PRACTICA: findViewById en cada ViewHolder. Deberia usarse ViewBinding.
        private val tvName: TextView = itemView.findViewById(R.id.tvRaceName)
        private val tvCircuit: TextView = itemView.findViewById(R.id.tvRaceCircuit)
        private val tvCountry: TextView = itemView.findViewById(R.id.tvRaceCountry)
        private val tvLaps: TextView = itemView.findViewById(R.id.tvRaceLaps)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)

        fun bind(race: Race) {
            tvName.text = race.name
            tvCircuit.text = race.circuit
            tvCountry.text = race.country
            // MALA PRACTICA: concatenacion directa de string sin recursos ni formato.
            tvLaps.text = "Vueltas: " + race.laps

            // Click corto -> editar.
            itemView.setOnClickListener { onEditClick(race) }

            // Click en borrar -> disparar callback.
            btnDelete.setOnClickListener { onDeleteClick(race) }
        }
    }
}
