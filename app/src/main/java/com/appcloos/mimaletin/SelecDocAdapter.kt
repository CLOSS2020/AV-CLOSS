package com.appcloos.mimaletin

import android.content.Context
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.ceil

class SelecDocAdapter: RecyclerView.Adapter<SelecDocAdapter.DocHolder>() {

    lateinit var documentos: ArrayList<Documentos>
    lateinit var context: Context
    var listaSelec: ArrayList<String> = ArrayList()
    lateinit var quantityListener: DocHolder.QuantityListener
    private var estadosCheck: SparseBooleanArray = SparseBooleanArray()



    fun SelecDocAdapter(context: Context, documentos: ArrayList<Documentos>, quantityListener: DocHolder.QuantityListener){
        this.context          = context
        this.documentos       = documentos
        this.quantityListener = quantityListener
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return DocHolder(layoutInflater.inflate(R.layout.item_check_docs, parent, false))
    }

    override fun onBindViewHolder(holder: DocHolder, position: Int) {
        holder.bind(documentos.get(position))
        holder.isSeleccionado.tag = position


        holder.isSeleccionado.isChecked = estadosCheck.get(holder.isSeleccionado.getTag() as Int)
        holder.isSeleccionado.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked -> // Cuando se hace clic en la casilla de verificación, guarda su propio logotipo (subíndice) y el estado en la colección
            estadosCheck.put(buttonView.tag as Int, isChecked)
            var codigo = documentos.get(holder.isSeleccionado.getTag() as Int).documento

            if (holder.isSeleccionado.isChecked && !listaSelec.contains(codigo)){
                listaSelec.add("\'"+codigo+"\'")

            }else {
                listaSelec.remove("\'"+codigo+"\'")

            }
            quantityListener.onQuantityChange(listaSelec)
        })

    }

    override fun getItemCount(): Int {
        return documentos.size
    }


    class DocHolder(val view: View):RecyclerView.ViewHolder(view) {

        //aqui va la identificacion de los elementos ubicados en el item
        var nroDoc         = view.findViewById<TextView>(R.id.tv_nrodoc)
        var tipoDocs       = view.findViewById<TextView>(R.id.tv_tipodocC)
        var montoTot       = view.findViewById<TextView>(R.id.tv_montototal)
        var fechaRecibido  = view.findViewById<TextView>(R.id.tv_fecharecepcion)
        var fechaVence     = view.findViewById<TextView>(R.id.tv_fechavence)
        val isSeleccionado = view.findViewById<CheckBox>(R.id.cb_sel_doc)
        val tv_montodebe   = view.findViewById<TextView>(R.id.tv_montodebe)
        val tv_status      = view.findViewById<TextView>(R.id.tv_status)

        fun bind(documentos: Documentos){
            var tipodeDocumentoVisual = ""
            var tipodeDocumento = documentos.tipodocv.toString()

            if(tipodeDocumento.equals("FAC")){
                tipodeDocumentoVisual = "FACTURA"

            }else if (tipodeDocumento.equals("N/E")){
                tipodeDocumentoVisual = "NOTA DE ENTREGA"
            }

            //asginacion de valores
            nroDoc.text = "Nº " + documentos.documento
            tipoDocs.text = tipodeDocumentoVisual
            var montoTotal: Double = documentos.dtotalfinal
            montoTotal = Math.round(montoTotal * 100.00)/100.00

            montoTot.text = "Total: ${(ceil((documentos.dtotalfinal) * 100) / 100)} $"
            tv_montodebe.text = "Deuda: ${(ceil((documentos.dtotalfinal - documentos.dtotpagos) * 100) / 100)} $"

            fechaRecibido.text = "F. Recibido: " + documentos.recepcion
            fechaVence.text    = "F. Vence: " + documentos.vence

            tv_status.text = when (documentos.estatusdoc) {
                "0" -> "Sin pagos"
                "1" -> "Abonado"
                "2" -> "Pagado"
                else -> {
                    "No Identificado"
                }
            }
        }

        public interface QuantityListener{
            fun onQuantityChange(listaChange:ArrayList<String>){

            }
        }



    }
}