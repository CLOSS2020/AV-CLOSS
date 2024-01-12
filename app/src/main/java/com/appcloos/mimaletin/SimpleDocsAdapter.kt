package com.appcloos.mimaletin

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SimpleDocsAdapter : RecyclerView.Adapter<SimpleDocsAdapter.SDocsHolder>() {
    lateinit var cobranza: ArrayList<CXC>
    lateinit var context: Context

    fun SimpleDocsAdapter(context: Context, cobranza: ArrayList<CXC>) {
        this.context = context
        this.cobranza = cobranza
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SDocsHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return SDocsHolder(
            layoutInflater.inflate(
                R.layout.item_simple_docs,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: SDocsHolder, position: Int) {
        holder.bind(cobranza[position])
    }

    override fun getItemCount(): Int {
        return cobranza.size
    }

    class SDocsHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val nrodoc: TextView = view.findViewById(R.id.tv_nrodoc_sdocs)
        val cliente: TextView = view.findViewById(R.id.tv_nocli_sdocs)

        fun bind(cobranza: CXC) {
            nrodoc.text = cobranza.documento
            cliente.text = cobranza.cliente
        }
    }
}
