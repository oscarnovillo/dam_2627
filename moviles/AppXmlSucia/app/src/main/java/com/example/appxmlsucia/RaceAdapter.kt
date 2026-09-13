package com.example.appxmlsucia

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// MALA PRACTICA: adapter que accede directo a Views con findViewById,
// crea intents, lanza activities y tiene logica de negocio mezclada.
class RaceAdapter : RecyclerView.Adapter<RaceAdapter.RaceViewHolder>() {

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

        // MALA PRACTICA: findViewById en cada binding. Deberia usarse ViewBinding.
        private val tvName: TextView = itemView.findViewById(R.id.tvRaceName)
        private val tvCircuit: TextView = itemView.findViewById(R.id.tvRaceCircuit)
        private val tvCountry: TextView = itemView.findViewById(R.id.tvRaceCountry)
        private val tvLaps: TextView = itemView.findViewById(R.id.tvRaceLaps)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)

        fun bind(race: Race) {
            // Datos mostrados
            tvName.text = race.name
            tvCircuit.text = race.circuit
            tvCountry.text = race.country
            // MALA PRACTICA: concatenacion directa de string sin recursos ni formato.
            tvLaps.text = "Vueltas: " + race.laps

            // MALA PRACTICA: colores hardcodeados en el adapter.
            itemView.setBackgroundColor(Color.parseColor("#FFF3E0"))
            tvName.setTextColor(Color.parseColor("#D32F2F"))

            // Click en item -> editar
            itemView.setOnClickListener {
                // MALA PRACTICA: startActivity con extras hardcodeados dentro del adapter.
                val intent = Intent(itemView.context, AddEditRaceActivity::class.java)
                intent.putExtra("race_id", race.id)
                itemView.context.startActivity(intent)
            }

            // Click en borrar -> logica de negocio en el adapter (otra mala practica)
            btnDelete.setOnClickListener {
                val builder = AlertDialog.Builder(itemView.context)
                // MALA PRACTICA: textos hardcodeados.
                builder.setTitle("Borrar carrera")
                builder.setMessage("Seguro que queres borrar " + race.name + "?")
                builder.setPositiveButton("Si") { _, _ ->
                    RaceRepository.delete(race.id)
                    submitList(RaceRepository.getAll())
                }
                builder.setNegativeButton("No", null)
                builder.show()
            }
        }
    }
}
