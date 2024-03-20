package com.appcloos.mimaletin

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DescuentosAdapter(
    var descuento: ArrayList<Descuentos>,
    val context: Context,
    private val descuentoIsSelected: (Boolean, Int) -> Unit
) : RecyclerView.Adapter<DescuentosAdapter.DescuentosHolder>() {

    fun onRefresh(newList: ArrayList<Descuentos>) {
        descuento = newList
        notifyDataSetChanged()
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
        holder.bind(descuento[position], descuentoIsSelected)
    }

    override fun getItemCount(): Int {
        return descuento.size
    }

    class DescuentosHolder(val view: View) : RecyclerView.ViewHolder(view) {

        val nrodoc: TextView = view.findViewById(R.id.tvNroDoc)
        private val porcentaje: TextView = view.findViewById(R.id.tvDocDescuento)
        val monto: TextView = view.findViewById(R.id.tvMtoDocDescuento)
        private val isSelected: CheckBox = view.findViewById(R.id.cbSelect)
        private val llMainDescuento: LinearLayout = view.findViewById(R.id.llMainDescuento)

        fun bind(descuento: Descuentos, descuentoIsSelected: (Boolean, Int) -> Unit) {
            val montoDesc = descuento.cantdscto.round()
            val porcenDec = descuento.pordscto.round()

            nrodoc.text = descuento.nrodoc
            porcentaje.text = porcenDec.toString()
            monto.text = montoDesc.toString()

            isSelected.isChecked = descuento.isSelected

            llMainDescuento.visibility = if (descuento.show){
                View.VISIBLE
            }else{
                View.GONE
            }

            isSelected.setOnClickListener {
                descuentoIsSelected(isSelected.isChecked, absoluteAdapterPosition)
            }

            llMainDescuento.setOnClickListener {
                isSelected.isChecked != isSelected.isChecked
                descuentoIsSelected(isSelected.isChecked, absoluteAdapterPosition)
            }
        }
    }
}
