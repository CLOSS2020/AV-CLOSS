package com.appcloos.mimaletin

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.appcloos.mimaletin.databinding.ItemCxcMainBinding
import java.text.SimpleDateFormat
import java.util.Locale


class CXCAdapter(
    var cobranza: ArrayList<CXC>,
    private val codEmpresa: String,
    private val onClickListener: (String) -> Unit,
) : RecyclerView.Adapter<CXCAdapter.CXCHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CXCHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return CXCHolder(layoutInflater.inflate(R.layout.item_cxc_main, parent, false))
    }

    override fun onBindViewHolder(holder: CXCHolder, position: Int) {
        holder.bind(cobranza[position], onClickListener)

    }

    override fun getItemCount(): Int {
        return cobranza.size
    }


    inner class CXCHolder(val view: View) : RecyclerView.ViewHolder(view) {

        private val binding: ItemCxcMainBinding = ItemCxcMainBinding.bind(view)

        private var fechaPago = ""

        fun bind(cobranza: CXC, onClickListener: (String) -> Unit) {

            setColors(codEmpresa)

            binding.clCxcRecibo.setOnClickListener { onClickListener(cobranza.id_recibo) }

            binding.idCxc.text = "Codigo: " + cobranza.id_recibo
            fechaPago = cobranza.fchrecibo
            binding.tvNombreCli.text = "Cliente: ${cobranza.cliente}"
            val monedaSel = cobranza.moneda

            binding.tvFecharecCxc.text = "Fecha: " + formatoFecha(fechaPago, cobranza.tipoRecibo)

            if (cobranza.efectivo > 0.00) {
                binding.tvMontoCxcRec.text = "Monto: ${cobranza.efectivo}$  (Monto Efectivo)"

            } else if (cobranza.bcomonto > 0.00) {

                if (monedaSel == "1") {
                    binding.tvMontoCxcRec.text = "Monto: " + cobranza.bcomonto.toString() + "Bs."
                } else if (monedaSel == "2") {
                    binding.tvMontoCxcRec.text = "Monto: " + cobranza.bcomonto.toString() + "$."
                }

            } else if (cobranza.bsretiva > 0.00 || cobranza.bsretflete > 0.00) {
                binding.tvMontoCxcRec.text =
                    "Monto:" +
                            (if (cobranza.bsretiva <= 0.00) "" else " (Ret IVA) ${cobranza.bsretiva} Bs. ") +
                            (if (cobranza.bsretiva <= 0.00 || cobranza.bsretflete <= 0.00) "" else "/") +
                            if (cobranza.bsretflete <= 0.00) "" else " (Ret Fle) ${cobranza.bsretflete} Bs."
            }


            when (cobranza.edorec) {
                "0" -> {
                    binding.tvStatCxc.text = "Estado: Por subir"
                    binding.tvStatCxc.setTextColor(Color.RED)
                    //stado.setBackgroundColor(Color.rgb(255, 255, 255))
                }

                "1" -> {
                    binding.tvStatCxc.text = "Estado: Subido"
                    binding.tvStatCxc.setTextColor(Color.rgb(63, 197, 39))
                    //stado.setBackgroundColor(Color.rgb(255, 255, 255))
                }

                "3" -> {
                    binding.tvStatCxc.text = "Estado: Anulado"
                    binding.tvStatCxc.setTextColor(Color.rgb(255, 255, 255))
                    binding.tvStatCxc.setBackgroundColor(Color.rgb(0, 0, 0))
                }

                "4" -> {
                    binding.tvStatCxc.text = "Estado: Anulado - Subido"
                    binding.tvStatCxc.setTextColor(Color.rgb(255, 255, 255))
                    binding.tvStatCxc.setBackgroundColor(Color.rgb(0, 0, 0))
                }

                "9" -> {
                    binding.tvStatCxc.text = "Estado: Anexo Creado"
                    binding.tvStatCxc.setTextColor(Color.rgb(240, 167, 50))
                    //stado.setBackgroundColor(Color.rgb(255, 255, 255))
                }

                "10" -> {
                    binding.tvStatCxc.text = "Estado: Actualizado"
                    binding.tvStatCxc.setTextColor(Color.rgb(35, 169, 242))
                    //stado.setBackgroundColor(Color.rgb(255, 255, 255))
                }

                else -> {
                    binding.tvStatCxc.text = "Estado: No Identificado"
                    binding.tvStatCxc.setTextColor(Color.rgb(0, 0, 0))
                    //stado.setBackgroundColor(Color.rgb(255, 255, 255))
                }
            }

            binding.tvTipoRecibo.text = when (cobranza.tipoRecibo) {
                "R" -> "Retención"
                "D" -> "Deposito"
                "W" -> "Recibo de Cobro"
                else -> "No Identificado"
            }


        }

        private fun setColors(codEmpresa: String) {
            binding.apply {
                tvNombreCli.setBackgroundColor(tvNombreCli.cxcBackgroundCliente(codEmpresa))
                tvTipoRecibo.setBackgroundColor(tvTipoRecibo.cxcBackgroundDatos(codEmpresa))
                tvFecharecCxc.setBackgroundColor(tvFecharecCxc.cxcBackgroundDatos(codEmpresa))
            }
        }

    }

    private fun formatoFecha(fechaPago: String, tipoRecibo: String): String {
        return if (tipoRecibo == "D") {
            val dt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = dt.parse(fechaPago)

            val dt1 = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            dt1.format(date)
        } else {
            val dt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = dt.parse(fechaPago)

            val dt1 = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            dt1.format(date)
        }
    }


}