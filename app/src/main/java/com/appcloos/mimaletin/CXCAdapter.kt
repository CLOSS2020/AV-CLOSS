package com.appcloos.mimaletin

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale


class CXCAdapter(
    var cobranza: ArrayList<CXC>,
    var context: Context,
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

        private val idCobranza: TextView = view.findViewById(R.id.idCxc)
        private val fchcob: TextView = view.findViewById(R.id.tv_fecharec_cxc)
        private val montocob: TextView = view.findViewById(R.id.tv_monto_cxc_rec)
        private val stado: TextView = view.findViewById(R.id.tv_stat_cxc)
        private val clCxcRec: ConstraintLayout = view.findViewById(R.id.cl_cxc_recibo)
        val tipoDoc: TextView = view.findViewById(R.id.tvTipoRecibo)
        private val nombreCli: TextView = view.findViewById(R.id.tvNombreCli)


        private var fechaPago = ""

        fun bind(cobranza: CXC, onClickListener: (String) -> Unit) {

            println(cobranza.id_recibo + " " + cobranza.edorec)

            clCxcRec.setOnClickListener { onClickListener(cobranza.id_recibo) }

            idCobranza.text = "Codigo: " + cobranza.id_recibo
            fechaPago = cobranza.fchrecibo
            nombreCli.text = "Cliente: ${cobranza.cliente}"
            val monedaSel = cobranza.moneda

            fchcob.text = "Fecha: " + formatoFecha(fechaPago, cobranza.tipoRecibo)

            if (cobranza.efectivo > 0.00) {
                montocob.text = "Monto: ${cobranza.efectivo}$  (Monto Efectivo)"

            } else if (cobranza.bcomonto > 0.00) {

                if (monedaSel == "1") {
                    montocob.text = "Monto: " + cobranza.bcomonto.toString() + "Bs."
                } else if (monedaSel == "2") {
                    montocob.text = "Monto: " + cobranza.bcomonto.toString() + "$."
                }

            } else if (cobranza.bsretiva > 0.00 || cobranza.bsretflete > 0.00) {
                montocob.text =
                    "Monto:" +
                            (if (cobranza.bsretiva <= 0.00) "" else " (Ret IVA) ${cobranza.bsretiva} Bs. ") +
                            (if (cobranza.bsretiva <= 0.00 || cobranza.bsretflete <= 0.00) "" else "/") +
                            if (cobranza.bsretflete <= 0.00) "" else " (Ret Fle) ${cobranza.bsretflete} Bs."
            }


            when (cobranza.edorec) {
                "0" -> {
                    stado.text = "Estado: Por subir"
                    stado.setTextColor(Color.RED)
                    //stado.setBackgroundColor(Color.rgb(255, 255, 255))
                }

                "1" -> {
                    stado.text = "Estado: Subido"
                    stado.setTextColor(Color.rgb(63, 197, 39))
                    //stado.setBackgroundColor(Color.rgb(255, 255, 255))
                }

                "3" -> {
                    stado.text = "Estado: Anulado"
                    stado.setTextColor(Color.rgb(255, 255, 255))
                    stado.setBackgroundColor(Color.rgb(0, 0, 0))
                }

                "4" -> {
                    stado.text = "Estado: Anulado - Subido"
                    stado.setTextColor(Color.rgb(255, 255, 255))
                    stado.setBackgroundColor(Color.rgb(0, 0, 0))
                }

                "9" -> {
                    stado.text = "Estado: Anexo Creado"
                    stado.setTextColor(Color.rgb(240, 167, 50))
                    //stado.setBackgroundColor(Color.rgb(255, 255, 255))
                }

                "10" -> {
                    stado.text = "Estado: Actualizado"
                    stado.setTextColor(Color.rgb(35, 169, 242))
                    //stado.setBackgroundColor(Color.rgb(255, 255, 255))
                }

                else -> {
                    stado.text = "Estado: No Identificado"
                    stado.setTextColor(Color.rgb(0, 0, 0))
                    //stado.setBackgroundColor(Color.rgb(255, 255, 255))
                }
            }

            tipoDoc.text = when (cobranza.tipoRecibo) {
                "R" -> "Retención"
                "D" -> "Deposito"
                "W" -> "Recibo de Cobro"
                else -> "No Identificado"
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