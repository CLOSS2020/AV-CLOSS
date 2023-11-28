package com.appcloos.mimaletin.dialogChangeAccount.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.appcloos.mimaletin.R
import com.appcloos.mimaletin.dialogChangeAccount.model.keDataconex

class DialogChangeAccountAdapter(
    private var listaEmpresa: List<keDataconex> = emptyList(),
    private val onClick: (Int) -> Unit
) : RecyclerView.Adapter<DialogChangeAccountHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DialogChangeAccountHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return DialogChangeAccountHolder(
            layoutInflater.inflate(
                R.layout.item_empresas, parent, false
            )
        )
    }

    override fun getItemCount(): Int = listaEmpresa.size

    override fun onBindViewHolder(holder: DialogChangeAccountHolder, position: Int) {
        holder.render(listaEmpresa[position], onClick)
    }

}