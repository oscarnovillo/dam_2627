package com.example.mvvmprueba.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mvvmprueba.databinding.ItemPoliticoBinding
import com.example.mvvmprueba.domain.model.Politico


class PoliticoAdapter (
    val delPolitico : (Politico) -> Unit,

)
    : ListAdapter<Politico, PoliticoAdapter.PoliticoViewHolder>(PoliticoDiffCallback()) {


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PoliticoViewHolder {
        val binding = ItemPoliticoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PoliticoViewHolder(binding,delPolitico)
    }

    override fun onBindViewHolder(
        holder: PoliticoViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }


    class PoliticoViewHolder(
        private val binding: ItemPoliticoBinding,
        private val onClickDelete : (Politico) -> Unit,

    ) : RecyclerView.ViewHolder(binding.root)
    {
        fun bind(politico: Politico) {

            with(binding) {
                tvNombrePolitico.text = politico.nombre

                root.setOnClickListener {

                    onClickDelete(politico)
                }
            }
        }

    }


}

class PoliticoDiffCallback : DiffUtil.ItemCallback<Politico>() {
    override fun areItemsTheSame(oldItem: Politico, newItem: Politico): Boolean =
        oldItem.nombre == newItem.nombre

    override fun areContentsTheSame(oldItem: Politico, newItem: Politico): Boolean =
        oldItem == newItem
}