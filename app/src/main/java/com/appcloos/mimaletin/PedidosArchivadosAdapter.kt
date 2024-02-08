package com.appcloos.mimaletin

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class PedidosArchivadosAdapter(
    private val context: Context,
    private val listpedidos: ArrayList<Pedidos>
) : BaseAdapter() {
    override fun getCount(): Int {
        return listpedidos.size
    }

    override fun getItem(i: Int): Any {
        return listpedidos[i]
    }

    override fun getItemId(i: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // indicamos el item que va a inflar el adapter mediante el convertview
        var convertView = convertView
        convertView = LayoutInflater.from(context).inflate(R.layout.item_pedidos_archivados, null)
        val pedidos = getItem(position) as Pedidos
        val textCorrelativo = convertView.findViewById<TextView>(R.id.tv_ndoc)
        val textNroPedido = convertView.findViewById<TextView>(R.id.tv_nroped)
        val textNombreCliente = convertView.findViewById<TextView>(R.id.tv_nombrecli)
        val textMontoPedido = convertView.findViewById<TextView>(R.id.tv_monto)
        textCorrelativo.text = pedidos.numeroDocumento
        textNroPedido.text = pedidos.numeroPedido
        textNombreCliente.text = "Cliente: " + pedidos.nombreCliente
        textMontoPedido.text = "Monto Neto: $" + pedidos.totalNeto
        return convertView
    }
}
