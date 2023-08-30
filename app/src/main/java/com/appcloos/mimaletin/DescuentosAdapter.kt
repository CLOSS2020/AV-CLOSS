package com.appcloos.mimaletin

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DescuentosAdapter : RecyclerView.Adapter<DescuentosAdapter.DescuentosHolder>() {
    lateinit var descuento: ArrayList<Descuentos>
    lateinit var context: Context


    fun DescuentosAdapter(context: Context, descuento: ArrayList<Descuentos>) {
        this.context = context
        this.descuento = descuento

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DescuentosHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return DescuentosHolder(
            layoutInflater.inflate(
                R.layout.item_detalle_descuento,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: DescuentosHolder, position: Int) {
        holder.bind(descuento[position])

    }

    override fun getItemCount(): Int {
        return descuento.size
    }

    class DescuentosHolder(val view: View) : RecyclerView.ViewHolder(view) {

        val nrodoc: TextView = view.findViewById(R.id.nro_doc_descuento)
        private val porcentaje: TextView = view.findViewById(R.id.por_doc_descuento)
        val monto: TextView = view.findViewById(R.id.mto_doc_descuento)

        fun bind(descuento: Descuentos) {
            val montoDesc = Math.round(descuento.cantdscto * 100.00) / 100.00
            val porcenDec = Math.round(descuento.pordscto * 100.00) / 100.00

            nrodoc.text = descuento.nrodoc
            porcentaje.text = porcenDec.toString()
            monto.text = montoDesc.toString()

        }
    }
}