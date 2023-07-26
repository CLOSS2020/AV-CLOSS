package com.appcloos.mimaletin.retencion

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.appcloos.mimaletin.R
import com.appcloos.mimaletin.databinding.ItemDetalleRetencionBinding


class DetalleRetencionAdapter(var listReten: ArrayList<DetalleRetencion>):
    RecyclerView.Adapter<DetalleRetencionAdapter.DetalleRetencionHolder>() {

    inner class DetalleRetencionHolder(view: View): RecyclerView.ViewHolder(view) {

        private val binding = ItemDetalleRetencionBinding.bind(view)

        fun render(retencion: DetalleRetencion) {
            binding.numeroDoc.text = retencion.documento
            binding.retIVA.text = retencion.retIva
            binding.retFlete.text = retencion.retFlete
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetalleRetencionHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return DetalleRetencionHolder(layoutInflater.inflate(
            R.layout.item_detalle_retencion,
            parent,
            false))
    }

    override fun getItemCount(): Int {
        println("Numero magico --> ${ listReten.size }")
        return listReten.size
    }

    override fun onBindViewHolder(holder: DetalleRetencionHolder, position: Int) {
        holder.render(listReten[position])
    }

}