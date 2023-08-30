package com.appcloos.mimaletin

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RetencionesAdapter : RecyclerView.Adapter<RetencionesAdapter.RetHolder>() {

    lateinit var retenciones: ArrayList<Retenciones>
    lateinit var context: Context
    private var listaSelec: ArrayList<Retenciones> = ArrayList()
    private lateinit var quantityListener: RetHolder.QuantityListener
    //private var estadosCheck: SparseBooleanArray = SparseBooleanArray()


    fun retencionAdapter(
        context: Context,
        retenciones: ArrayList<Retenciones>,
        quantityListener: RetHolder.QuantityListener
    ) {
        this.context = context
        this.retenciones = retenciones
        this.quantityListener = quantityListener
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RetHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        return RetHolder(layoutInflater.inflate(R.layout.item_retenciones, parent, false))

    }

    override fun onBindViewHolder(holder: RetHolder, position: Int) {
        holder.bind(retenciones[position])
        //holder.isSeleccionado.tag = position

        //holder.isSeleccionado.isClickable = estadosCheck.get(holder.isSeleccionado.getTag() as Int)
        holder.isSeleccionado.setOnClickListener {
            /*var codigo = retenciones[position].tiporet
            listaSelec.remove(retenciones[position])
            println(retenciones[position])
            println(listaSelec)*/
            quantityListener.onQuantityChange(position)
            /*val adapter = RetencionesAdapter()
            adapter.notifyDataSetChanged()*/
        }
    }


    override fun getItemCount(): Int {
        return retenciones.size

    }

    class RetHolder(val view: View) : RecyclerView.ViewHolder(view) {
        var correlativo: TextView = view.findViewById(R.id.tv_correlativoret)
        var isSeleccionado: ImageButton = view.findViewById(R.id.bt_borrar)
        private var fecharet: TextView = view.findViewById(R.id.tv_fecharet)
        var refret: TextView = view.findViewById(R.id.tv_refret)
        private var montoret: TextView = view.findViewById(R.id.tv_montoret)
        private var tiporet: TextView = view.findViewById(R.id.tv_tiporet)

        fun bind(retenciones: Retenciones) {
            var montoRet: Double = retenciones.montoret
            montoRet = Math.round(montoRet * 100.00) / 100.00

            correlativo.text = "CORR.: " + retenciones.nroret
            fecharet.text = "FECHA: " + retenciones.fecharet
            refret.text = "REF: " + retenciones.refret
            tiporet.text = "TIPO: " + retenciones.tiporet
            montoret.text = "MONTO: $montoRet Bs."

        }


        interface QuantityListener {
            fun onQuantityChange(listaChange: Int) {

            }
        }
    }


}