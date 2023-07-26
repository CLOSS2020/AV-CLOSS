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

class EfectivosAdapter: RecyclerView.Adapter<EfectivosAdapter.RecHolder>() {

    //variables
    lateinit var recibos: ArrayList<CXC>
    lateinit var context: Context
    var listaSelec: ArrayList<String> = ArrayList()
    lateinit var quantityListener: RecHolder.QuantityListener
    private var estadosCheck: SparseBooleanArray = SparseBooleanArray()


    fun EfectivosAdapter(context: Context, recibos: ArrayList<CXC>, quantityListener: EfectivosAdapter.RecHolder.QuantityListener){
        this.context = context
        this.recibos = recibos
        this.quantityListener = quantityListener

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EfectivosAdapter.RecHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return EfectivosAdapter.RecHolder(layoutInflater.inflate(R.layout.item_recibos_efectivos, parent, false))
    }


    override fun onBindViewHolder(holder: RecHolder, position: Int) {
        holder.bind(recibos.get(position))
        holder.isSeleccionado.tag = position

        holder.isSeleccionado.isChecked = estadosCheck.get(holder.isSeleccionado.getTag() as Int)
        holder.isSeleccionado.setOnCheckedChangeListener (CompoundButton.OnCheckedChangeListener{ buttonView, isChecked ->
            estadosCheck.put(buttonView.tag as Int, isChecked)
            var codigo = recibos.get(holder.isSeleccionado.getTag() as Int).id_recibo

            if (holder.isSeleccionado.isChecked && !listaSelec.contains(codigo)){
                listaSelec.add("\'"+codigo+"\'")

            }else {
                listaSelec.remove("\'"+codigo+"\'")

            }
            quantityListener.onQuantityChange(listaSelec)

        })



    }

    override fun getItemCount(): Int {
        return recibos.size
    }

    class RecHolder(val view: View): RecyclerView.ViewHolder(view){

        var nroRec         = view.findViewById<TextView>(R.id.tv_id_recibo_efectivo)
        var montoRec       = view.findViewById<TextView>(R.id.tv_monto_recibo_efectivo)
        var fechaRec       = view.findViewById<TextView>(R.id.tv_fecha_recibo_efectivo)
        var tipoRec         = view.findViewById<TextView>(R.id.tv_tipo_recibo)
        val isSeleccionado = view.findViewById<CheckBox>(R.id.cb_recibo_selec_efectivo)


        fun bind(recibos: CXC){

            nroRec.text   = "N° ${recibos.id_recibo}"
            montoRec.text = "$" + recibos.efectivo.toString()
            fechaRec.text = recibos.fchrecibo

            if(recibos.efectivo > 0.00){
                tipoRec.text = "EFC"
            }
        }

        public interface QuantityListener{
            fun onQuantityChange(listaChange:ArrayList<String>){

            }
        }
    }

}