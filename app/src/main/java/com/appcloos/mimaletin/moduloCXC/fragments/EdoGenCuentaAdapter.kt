package com.appcloos.mimaletin.moduloCXC.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.appcloos.mimaletin.R
import com.appcloos.mimaletin.databinding.ItemEstadoGeneralBinding
import com.appcloos.mimaletin.moduloCXC.viewmodel.EdoGeneralCxc
import com.appcloos.mimaletin.moduloCXC.viewmodel.PlanificadorCxc
import kotlin.math.abs

class EdoGenCuentaAdapter(private var edogencxc: List<EdoGeneralCxc>, private val onClickListener:(String, String) -> Unit):
    RecyclerView.Adapter<EdoGenCuentaAdapter.EdoGenCuentaHolder>() {

    inner class EdoGenCuentaHolder(view: View): RecyclerView.ViewHolder(view){

        private val binding = ItemEstadoGeneralBinding.bind(view)

        fun render(edoGen: EdoGeneralCxc, onClickListener: (String, String) -> Unit){

            val fechaVencimiento = if (edoGen.fechaVencimiento.toInt() > 0){
                "${edoGen.fechaVencimiento} días"
            } else {
                "Por vencer"
            }

            //asignacion de variables a los campos de texto a mostrar
            binding.tvCodcliEstGen.text  = edoGen.codigocliente
            binding.tvNomclieEstGen.text = edoGen.nomcliente
            binding.tvDeudaEstGen.text   = edoGen.montoTotal.toString()
            binding.tvFechaEstGen.text   = fechaVencimiento
            binding.tvLimiteEstGen.text  = edoGen.limite.toString()
            binding.tvSaldoEstGen.text   = edoGen.saldo.toString()

            binding.itemEdoGenCuenta.setOnClickListener { onClickListener(edoGen.codigocliente, edoGen.nomcliente) }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EdoGenCuentaHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return EdoGenCuentaHolder(layoutInflater.inflate(
            R.layout.item_estado_general,
            parent,
            false))
    }

    override fun onBindViewHolder(holder: EdoGenCuentaHolder, position: Int) {
        holder.render(edogencxc[position], onClickListener)
    }

    override fun getItemCount(): Int = edogencxc.size

    fun actualizarFact(edogencxc: List<EdoGeneralCxc>) {
        this.edogencxc = edogencxc
        notifyDataSetChanged()
    }
}