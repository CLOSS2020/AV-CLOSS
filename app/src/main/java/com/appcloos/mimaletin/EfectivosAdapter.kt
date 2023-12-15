package com.appcloos.mimaletin

import android.content.Context
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EfectivosAdapter : RecyclerView.Adapter<EfectivosAdapter.RecHolder>() {

    //variables
    lateinit var recibos: ArrayList<CXC>
    lateinit var context: Context
    private var listaSelec: ArrayList<String> = ArrayList()
    private lateinit var quantityListener: RecHolder.QuantityListener
    private var estadosCheck: SparseBooleanArray = SparseBooleanArray()


    fun EfectivosAdapter(
        context: Context,
        recibos: ArrayList<CXC>,
        quantityListener: RecHolder.QuantityListener
    ) {
        this.context = context
        this.recibos = recibos
        this.quantityListener = quantityListener

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return RecHolder(
            layoutInflater.inflate(
                R.layout.item_recibos_efectivos,
                parent,
                false
            )
        )
    }


    override fun onBindViewHolder(holder: RecHolder, position: Int) {
        holder.bind(recibos[position])
        holder.isSeleccionado.tag = position

        holder.isSeleccionado.isChecked = estadosCheck.get(holder.isSeleccionado.tag as Int)
        holder.isSeleccionado.setOnCheckedChangeListener { buttonView, isChecked ->
            estadosCheck.put(buttonView.tag as Int, isChecked)
            val codigo = recibos[holder.isSeleccionado.tag as Int].id_recibo

            if (holder.isSeleccionado.isChecked && !listaSelec.contains(codigo)) {
                listaSelec.add("\'" + codigo + "\'")

            } else {
                listaSelec.remove("\'" + codigo + "\'")

            }
            quantityListener.onQuantityChange(listaSelec)

        }


    }

    override fun getItemCount(): Int {
        return recibos.size
    }

    class RecHolder(val view: View) : RecyclerView.ViewHolder(view) {

        private var nroRec: TextView = view.findViewById(R.id.tv_id_recibo_efectivo)
        private var montoRec: TextView = view.findViewById(R.id.tv_monto_recibo_efectivo)
        private var fechaRec: TextView = view.findViewById(R.id.tv_fecha_recibo_efectivo)
        private var tipoRec: TextView = view.findViewById(R.id.tv_tipo_recibo)
        private var nombreCliente: TextView = view.findViewById(R.id.tvNombreInfo)
        val isSeleccionado: CheckBox = view.findViewById(R.id.cb_recibo_selec_efectivo)


        fun bind(recibos: CXC) {

            nroRec.text = "N° ${recibos.id_recibo}"
            montoRec.text = "$" + recibos.efectivo.toString()
            fechaRec.text = recibos.fchrecibo
            nombreCliente.text = recibos.cliente

            isSeleccionado.buttonTintList = isSeleccionado.setColorCheckBox(Constantes.AGENCIA)

            if (recibos.efectivo > 0.00) {
                tipoRec.text = "EFC"
            }
        }

        interface QuantityListener {
            fun onQuantityChange(listaChange: ArrayList<String>) {

            }
        }
    }

}