package com.appcloos.mimaletin

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecsAdapter : RecyclerView.Adapter<RecsAdapter.RecHolder>() {

    lateinit var cobranza: ArrayList<CXC>
    lateinit var context: Context

    fun RecsAdapter(context: Context, cobranza: ArrayList<CXC>) {
        this.context = context
        this.cobranza = cobranza
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return RecHolder(layoutInflater.inflate(R.layout.item_resumen_efectivos, parent, false))
    }

    override fun onBindViewHolder(holder: RecHolder, position: Int) {
        holder.bind(cobranza[position])

    }

    override fun getItemCount(): Int {
        return cobranza.size
    }

    class RecHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val idcxc = view.findViewById<TextView>(R.id.tv_id_resumen)
        val mtocxc = view.findViewById<TextView>(R.id.tv_efec_resumen)

        fun bind(cobranza: CXC) {
            idcxc.text = "ID: ${cobranza.id_recibo}"
            mtocxc.text = "$ ${cobranza.efectivo}"
        }
    }

}