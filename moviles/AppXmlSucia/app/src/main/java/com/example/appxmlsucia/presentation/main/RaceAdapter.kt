package com.example.appxmlsucia.presentation.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.appxmlsucia.databinding.ItemRaceBinding
import com.example.appxmlsucia.domain.model.Race

class RaceAdapter(
    private val onAction: (RaceAction) -> Unit
) : ListAdapter<Race, RaceAdapter.RaceViewHolder>(RaceDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RaceViewHolder {
        val binding = ItemRaceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RaceViewHolder(binding, onAction)
    }

    override fun onBindViewHolder(holder: RaceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class RaceViewHolder(
        private val binding: ItemRaceBinding,
        private val onAction: (RaceAction) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(race: Race) {
            binding.tvRaceName.text = race.name
            binding.tvRaceCircuit.text = race.circuit
            binding.tvRaceCountry.text = race.country
            binding.tvRaceLaps.text = binding.root.context.getString(
                com.example.appxmlsucia.R.string.race_laps_format,
                race.laps
            )

            binding.root.setOnClickListener { onAction(RaceAction.Edit(race)) }
            binding.btnDelete.setOnClickListener { onAction(RaceAction.Delete(race)) }
        }
    }
}

class RaceDiffCallback : DiffUtil.ItemCallback<Race>() {
    override fun areItemsTheSame(oldItem: Race, newItem: Race): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Race, newItem: Race): Boolean =
        oldItem == newItem
}
