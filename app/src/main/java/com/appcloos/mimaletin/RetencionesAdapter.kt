package com.appcloos.mimaletin

import android.content.Context
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CompoundButton
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class retencionesAdapter: RecyclerView.Adapter<retencionesAdapter.RetHolder>() {

    lateinit var retenciones: ArrayList<Retenciones>
    lateinit var context: Context
    var listaSelec: ArrayList<Retenciones> = ArrayList()
    lateinit var quantityListener: RetHolder.QuantityListener
    private var estadosCheck: SparseBooleanArray = SparseBooleanArray()



    fun retencionAdapter(context: Context, retenciones:ArrayList<Retenciones>, quantityListener: RetHolder.QuantityListener){
        this.context = context
        this.retenciones = retenciones
        this.quantityListener = quantityListener
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RetHolder {
    val layoutInflater = LayoutInflater.from(parent.context)

    return RetHolder(layoutInflater.inflate(R.layout.item_retenciones, parent,false))

    }

    override fun onBindViewHolder(holder: RetHolder, position: Int) {
        holder.bind(retenciones.get(position))
       //holder.isSeleccionado.tag = position

        //holder.isSeleccionado.isClickable = estadosCheck.get(holder.isSeleccionado.getTag() as Int)
        holder.isSeleccionado.setOnClickListener(View.OnClickListener {
            var codigo = retenciones[position].tiporet
            listaSelec.remove(retenciones[position])
            quantityListener.onQuantityChange(listaSelec)
            val adapter:retencionesAdapter = retencionesAdapter()
            adapter.notifyDataSetChanged()
        })
    }


    override fun getItemCount(): Int {
        return retenciones.size

    }

    class RetHolder(val view: View):RecyclerView.ViewHolder(view){
        var correlativo    = view.findViewById<TextView>(R.id.tv_correlativoret)
        var isSeleccionado = view.findViewById<ImageButton>(R.id.bt_borrar)
        var fecharet       = view.findViewById<TextView>(R.id.tv_fecharet)
        var refret         = view.findViewById<TextView>(R.id.tv_refret)
        var montoret       = view.findViewById<TextView>(R.id.tv_montoret)
        var tiporet        = view.findViewById<TextView>(R.id.tv_tiporet)

        fun bind(retenciones: Retenciones){
            var montoRet: Double = retenciones.montoret
            montoRet = Math.round(montoRet*100.00)/100.00

            correlativo.text = "CORR.: " + retenciones.nroret
            fecharet.text    = "FECHA: " + retenciones.fecharet
            refret.text      = "REF: "   + retenciones.refret
            tiporet.text     = "TIPO: "  + retenciones.tiporet
            montoret.text    = "MONTO: ${montoRet.toString()} Bs."

        }


        public interface QuantityListener{
            fun onQuantityChange(listaChange:ArrayList<Retenciones>){

            }
        }
    }


}