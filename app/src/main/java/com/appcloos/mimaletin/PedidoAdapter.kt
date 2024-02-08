package com.appcloos.mimaletin

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.appcloos.mimaletin.databinding.ItemPedidoBinding

class PedidoAdapter(private val context: Context, private val listpedidos: ArrayList<Pedidos>) :
    BaseAdapter() {
    private val inflater: LayoutInflater? = null
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
        val pedidos = getItem(position) as Pedidos
        val convertView: View = LayoutInflater.from(context).inflate(R.layout.item_pedido, null)

        val binding = ItemPedidoBinding.bind(convertView)

        binding.tvTipoDoc.text = if (pedidos.docSolicitado == "1") {
            "FAC"
        } else {
            "N/E"
        }

        var montoNeto = pedidos.totalNeto
        montoNeto = montoNeto * 100.00 / 100.00
        var montoNetoconDcto = pedidos.totalNetoDcto
        montoNetoconDcto = montoNetoconDcto * 100.00 / 100.00
        val diferencia = montoNeto - montoNetoconDcto
        if (diferencia > 0.0) {
            binding.txtMontonetodcto.visibility = View.VISIBLE
            binding.txtMontonetodcto.text = "Monto con Dscto: $$montoNetoconDcto"
        }
        binding.txtCodinternopedido.text = "COD: " + pedidos.numeroDocumento
        binding.txtNumeroped.text = "Nº " + pedidos.numeroPedido
        binding.txtCodclienteped.text = "Cliente: " + pedidos.codigoCliente
        binding.txtNombrecliente.text = pedidos.nombreCliente
        binding.txtMontonetoped.text = "Monto Neto: $" + pedidos.totalNeto
        binding.txtEstatusped.text = "Estatus: " + pedidos.estatus
        binding.txtFechapedcreado.text = "Fecha de Creación: " + pedidos.fechaDocumento

        binding.tvDolarFlete.text = if (pedidos.dolarflete) {
            "FD"
        } else {
            ""
        }

        return binding.root
    }
}
