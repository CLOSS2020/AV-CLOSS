package com.appcloos.mimaletin

import android.content.Context
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.ceil
import kotlin.math.roundToInt

class SelecDocAdapter : RecyclerView.Adapter<SelecDocAdapter.DocHolder>() {

    lateinit var documentos: ArrayList<Documentos>
    lateinit var context: Context
    private var listaSelec: ArrayList<String> = ArrayList()
    private lateinit var quantityListener: DocHolder.QuantityListener
    private var estadosCheck: SparseBooleanArray = SparseBooleanArray()

    fun SelecDocAdapter(
        context: Context,
        documentos: ArrayList<Documentos>,
        quantityListener: DocHolder.QuantityListener
    ) {
        this.context = context
        this.documentos = documentos
        this.quantityListener = quantityListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return DocHolder(layoutInflater.inflate(R.layout.item_check_docs, parent, false))
    }

    override fun onBindViewHolder(holder: DocHolder, position: Int) {
        holder.bind(documentos[position])
        holder.isSeleccionado.tag = position

        holder.isSeleccionado.isChecked = estadosCheck.get(holder.isSeleccionado.tag as Int)
        holder.isSeleccionado.setOnCheckedChangeListener { buttonView, isChecked ->
            // Cuando se hace clic en la casilla de verificación, guarda su propio logotipo
            // (subíndice) y el estado en la colección
            estadosCheck.put(buttonView.tag as Int, isChecked)
            val codigo = documentos[holder.isSeleccionado.tag as Int].documento

            if (holder.isSeleccionado.isChecked && !listaSelec.contains(codigo)) {
                listaSelec.add("\'" + codigo + "\'")
            } else {
                listaSelec.remove("\'" + codigo + "\'")
            }
            quantityListener.onQuantityChange(listaSelec)
        }
    }

    override fun getItemCount(): Int {
        return documentos.size
    }

    class DocHolder(val view: View) : RecyclerView.ViewHolder(view) {

        // aqui va la identificacion de los elementos ubicados en el item
        private var nroDoc: TextView = view.findViewById(R.id.tv_nrodoc)
        private var tipoDocs: TextView = view.findViewById(R.id.tv_tipodocC)
        private var montoTot: TextView = view.findViewById(R.id.tvMontototal)
        private var fechaRecibido: TextView = view.findViewById(R.id.tv_fecharecepcion)
        private var fechaVence: TextView = view.findViewById(R.id.tv_fechavence)
        val isSeleccionado: CheckBox = view.findViewById(R.id.cbSelDoc)
        private val tvMontodebe: TextView = view.findViewById(R.id.tvMontodebe)
        private val tvStatus: TextView = view.findViewById(R.id.tv_status)

        fun bind(documentos: Documentos) {
            var tipodeDocumentoVisual = ""
            val tipodeDocumento = documentos.tipodocv.toString()

            if (tipodeDocumento == "FAC") {
                tipodeDocumentoVisual = "FACTURA"
            } else if (tipodeDocumento == "N/E") {
                tipodeDocumentoVisual = "NOTA DE ENTREGA"
            }

            // asginacion de valores
            nroDoc.text = "Nº " + documentos.documento
            tipoDocs.text = tipodeDocumentoVisual
            var montoTotal: Double = documentos.dtotalfinal
            montoTotal = (montoTotal * 100.00).roundToInt() / 100.00

            montoTot.text = "Total: ${(ceil((documentos.dtotalfinal) * 100) / 100)} $"
            tvMontodebe.text =
                "Deuda: ${(ceil((documentos.dtotalfinal - documentos.dtotpagos) * 100) / 100)} $"

            fechaRecibido.text = "F. Recibido: " + documentos.recepcion
            fechaVence.text = "F. Vence: " + documentos.vence

            tvStatus.text = when (documentos.estatusdoc) {
                "0" -> "Sin pagos"
                "1" -> "Abonado"
                "2" -> "Pagado"
                else -> {
                    "No Identificado"
                }
            }
        }

        interface QuantityListener {
            fun onQuantityChange(listaChange: ArrayList<String>) {
            }
        }
    }
}
