package com.appcloos.mimaletin

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DescuentosAdapter: RecyclerView.Adapter<DescuentosAdapter.DescuentosHolder>() {
    lateinit var descuento: ArrayList<Descuentos>
    lateinit var context : Context


    fun DescuentosAdapter(context : Context, descuento: ArrayList<Descuentos>){
        this.context = context
        this.descuento = descuento

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DescuentosAdapter.DescuentosHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return DescuentosAdapter.DescuentosHolder(layoutInflater.inflate(R.layout.item_detalle_descuento, parent, false))
    }

    override fun onBindViewHolder(holder: DescuentosAdapter.DescuentosHolder, position: Int) {
        holder.bind(descuento.get(position))

    }

    override fun getItemCount(): Int {
        return descuento.size
    }

    class DescuentosHolder(val view: View):RecyclerView.ViewHolder(view){

        val nrodoc     = view.findViewById<TextView>(R.id.nro_doc_descuento)
        val porcentaje = view.findViewById<TextView>(R.id.por_doc_descuento)
        val monto      = view.findViewById<TextView>(R.id.mto_doc_descuento)

        fun bind(descuento: Descuentos){
            var montoDesc = Math.round(descuento.cantdscto * 100.00)/100.00
            var porcenDec = Math.round(descuento.pordscto * 100.00)/100.00

            nrodoc.text     = descuento.nrodoc
            porcentaje.text = porcenDec.toString()
            monto.text      = montoDesc.toString()

        }
    }
}