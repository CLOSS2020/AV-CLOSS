package com.appcloos.mimaletin

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.appcloos.mimaletin.databinding.ItemDocumentosBinding

class DocumentosAdapter(
    private val context: Context,
    private val listdocumentos: ArrayList<Documentos>,
    private val onClickListener: (Int) -> Unit
) : RecyclerView.Adapter<DocumentosAdapter.DocumentosHolder>() {
    inner class DocumentosHolder(val view: View) : RecyclerView.ViewHolder(view) {

        private val binding: ItemDocumentosBinding = ItemDocumentosBinding.bind(view)

        fun bind(documentos: Documentos, onClickListener: (Int) -> Unit) {
            binding.imageView2.setImageDrawable(colorAgencia())
            binding.lbEstatus.text = when (documentos.getEstatusdoc()) {
                "0" -> "Pendiente p. pagar"
                "1" -> "Abonado"
                "2" -> "Totalmente Pago"
                "3" -> "Anulado"
                else -> "No Identificado"
            }
            var mtoFinal = documentos.getDtotalfinal()
            mtoFinal = mtoFinal.valorReal()
            binding.lbNrodoc.text = documentos.getDocumento()
            binding.lbTipodocv.text = documentos.getTipodocv()
            binding.lbMontototal.text = "$mtoFinal$"
            binding.lbEmision.text = documentos.getEmision()
            binding.lbRecepcion.text = documentos.getRecepcion()
            binding.lbAceptadev.text = documentos.getAceptadev()

            binding.clMain.setOnClickListener { onClickListener(absoluteAdapterPosition) }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocumentosHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return DocumentosHolder(layoutInflater.inflate(R.layout.item_documentos, parent, false))
    }

    override fun onBindViewHolder(holder: DocumentosHolder, position: Int) {
        holder.bind(listdocumentos[position], onClickListener)
    }

    override fun getItemCount(): Int = listdocumentos.size



    /*override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
        val documentos = getItem(position) as Documentos
        val convertView: View = LayoutInflater.from(context).inflate(R.layout.item_documentos, null)
        val lbNrodoc = convertView.findViewById<TextView>(R.id.lb_nrodoc)
        val lbTipodocv = convertView.findViewById<TextView>(R.id.lb_tipodocv)
        val lbEstatus = convertView.findViewById<TextView>(R.id.lb_estatus)
        val lbMontototal = convertView.findViewById<TextView>(R.id.lb_montototal)
        val lbEmision = convertView.findViewById<TextView>(R.id.lb_emision)
        val lbRecepcion = convertView.findViewById<TextView>(R.id.lb_recepcion)
        val lbAceptadev = convertView.findViewById<TextView>(R.id.lb_aceptadev)
        val imageView2 = convertView.findViewById<ImageView>(R.id.imageView2)
        imageView2.setImageDrawable(colorAgencia())
        when (documentos.getEstatusdoc()) {
            "0" -> estatusMostrar = "Pendiente p. pagar"
            "1" -> estatusMostrar = "Abonado"
            "2" -> estatusMostrar = "Totalmente Pago"
            "3" -> estatusMostrar = "Anulado"
        }
        var mtoFinal = documentos.getDtotalfinal()
        mtoFinal = mtoFinal.valorReal()
        lbNrodoc.text = documentos.getDocumento()
        lbTipodocv.text = documentos.getTipodocv()
        lbEstatus.text = estatusMostrar
        lbMontototal.text = "$mtoFinal$"
        lbEmision.text = documentos.getEmision()
        lbRecepcion.text = documentos.getRecepcion()
        lbAceptadev.text = documentos.getAceptadev()
        return convertView
    }*/

    private fun colorAgencia(): Drawable? {
        return if (Constantes.AGENCIA == Constantes.WOKIN) {
            ContextCompat.getDrawable(context, R.drawable.ic_wokin_baseline_article_24)
        } else {
            ContextCompat.getDrawable(context, R.drawable.ic_baseline_article_24)
        }
    }

}