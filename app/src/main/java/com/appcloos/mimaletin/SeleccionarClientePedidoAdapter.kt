package com.appcloos.mimaletin

import android.content.Context
import android.database.Cursor
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.appcloos.mimaletin.databinding.ItemClientePedidoBinding


class SeleccionarClientePedidoAdapter(
    private var clientes: List<Cliente>,
    private val onClickListener: (String, String) -> Unit,
    private val context: Context
) :
    RecyclerView.Adapter<SeleccionarClientePedidoAdapter.SeleccionarClientePedidoHolder>() {


    inner class SeleccionarClientePedidoHolder(val view: View) : RecyclerView.ViewHolder(view) {

        private var conn: AdminSQLiteOpenHelper = AdminSQLiteOpenHelper(context, "ke_android", null, 24)
        var ke_android = conn.writableDatabase

        val binding = ItemClientePedidoBinding.bind(view)

        fun render(cliente: Cliente, onClickListener: (String, String) -> Unit) {

            when (cliente.contribespecial) {
                1.0 -> {
                    binding.tvContrioEspe.text = " Activo"
                    binding.tvContrioEspe.setTextColor(Color.rgb(63, 197, 39))
                }
                0.0 -> {
                    binding.tvContrioEspe.text = " No Activo"
                    binding.tvContrioEspe.setTextColor(Color.rgb(0, 0, 0))
                }
                else -> {
                    binding.tvContrioEspe.text = " No identificado"
                    binding.tvContrioEspe.setTextColor(Color.rgb(0, 0, 0))
                }
            }

            /*when (cliente.kne_activa) {
                1 -> {
                    binding.tvDocsVencidos.text = " Aplica"
                    binding.tvDocsVencidos.setTextColor(Color.rgb(0, 163, 240))
                }
                0 -> {
                    binding.tvDocsVencidos.text = " No Aplica"
                    binding.tvDocsVencidos.setTextColor(Color.rgb(0, 0, 0))
                }
                else -> {
                    binding.tvDocsVencidos.text = " No identificado"
                    binding.tvDocsVencidos.setTextColor(Color.rgb(0, 0, 0))
                }
            }*/

            binding.tvDocsVencidos.text = cliente.kne_activa.toString()

            binding.tvCodigoCliente.text = cliente.codigo
            binding.tvNombreCliente.text = cliente.nombre

            if (cliente.status == null){
                val cursorTasas: Cursor = ke_android.rawQuery("SELECT ROUND(JULIANDAY('now') - JULIANDAY(kti_fchdoc)) FROM ke_opti WHERE kti_codcli = '${cliente.codigo}'", null)

                binding.tvPedidoDiasMain.text = "Dias sin hacer pedidos: "
                binding.tvPedidoDias.text = if (cursorTasas.moveToFirst()) cursorTasas.getString(0) else "No encontrado"
            }else{
                binding.tvPedidoDiasMain.text = "Documentos: "
                binding.tvPedidoDias.text = cliente.status.toInt().toString()
            }



            binding.clMainSlecClientePedido.setOnClickListener {
                onClickListener(
                    cliente.codigo!!,
                    cliente.nombre!!
                )
            }

        }

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): SeleccionarClientePedidoHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return SeleccionarClientePedidoHolder(
            layoutInflater.inflate(
                R.layout.item_cliente_pedido,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = clientes.size

    override fun onBindViewHolder(holder: SeleccionarClientePedidoHolder, position: Int) {
        holder.render(clientes[position], onClickListener)
    }

    fun actualizarClientes(clientes: List<Cliente>) {
        this.clientes = clientes
        notifyDataSetChanged()
    }
}