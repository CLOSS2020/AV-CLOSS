package com.appcloos.mimaletin.moduloCXC.fragments.edoGenCuentaAdapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.appcloos.mimaletin.R
import com.appcloos.mimaletin.moduloCXC.viewmodel.EdoGeneralCxc

class EdoGenCuentaAdapter(
    private var edogencxc: List<EdoGeneralCxc>,
    private val onClickListener: (String, String) -> Unit
) : RecyclerView.Adapter<EdoGenCuentaHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EdoGenCuentaHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return EdoGenCuentaHolder(
            layoutInflater.inflate(
                R.layout.item_estado_general,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: EdoGenCuentaHolder, position: Int) {
        holder.render(edogencxc[position], onClickListener)
    }

    override fun getItemCount(): Int = edogencxc.size

    fun updateAdapter(edogencxc: List<EdoGeneralCxc>) {
        this.edogencxc = edogencxc
        notifyDataSetChanged()
    }
}
