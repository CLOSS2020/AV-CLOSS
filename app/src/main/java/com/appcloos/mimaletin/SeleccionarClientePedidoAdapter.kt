package com.appcloos.mimaletin

import android.content.Context
import android.content.res.Configuration
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.appcloos.mimaletin.databinding.ItemClientePedidoBinding
import com.appcloos.mimaletin.domain.ClienteAlertauseCase


class SeleccionarClientePedidoAdapter(
    private var clientes: List<Cliente>,
    private val onClickListener: (String, String) -> Unit,
    private val onLongClickListener: (String, String) -> Unit,
    private val context: Context,
    private val codEmpresa: String
) : RecyclerView.Adapter<SeleccionarClientePedidoAdapter.SeleccionarClientePedidoHolder>() {

    var nightModeFlags: Int = context.resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK

    inner class SeleccionarClientePedidoHolder(val view: View) : RecyclerView.ViewHolder(view) {

        private var conn: AdminSQLiteOpenHelper =
            AdminSQLiteOpenHelper(context, "ke_android", null)
        var keAndroid: SQLiteDatabase = conn.writableDatabase

        val binding = ItemClientePedidoBinding.bind(view)

        fun render(
            cliente: Cliente,
            onClickListener: (String, String) -> Unit,
            onLongClickListener: (String, String) -> Unit
        ) {

            when (cliente.contribespecial) {
                1.0 -> {
                    binding.tvContrioEspe.text = " Activo"
                    binding.tvContrioEspe.setTextColor(Color.rgb(63, 197, 39))
                }

                0.0 -> {
                    binding.tvContrioEspe.text = " No Activo"
                    if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                        binding.tvContrioEspe.setTextColor(Color.rgb(201, 200, 200))
                    } else {
                        binding.tvContrioEspe.setTextColor(Color.rgb(64, 64, 64))
                    }
                    //binding.tvContrioEspe.setTextColor(Color.rgb(0, 0, 0))
                }

                else -> {
                    binding.tvContrioEspe.text = " No identificado"
                    if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                        binding.tvContrioEspe.setTextColor(Color.rgb(201, 200, 200))
                    } else {
                        binding.tvContrioEspe.setTextColor(Color.rgb(64, 64, 64))
                    }
                    //binding.tvContrioEspe.setTextColor(Color.rgb(0, 0, 0))
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
            binding.tvDocsVencidos.apply {
                text = cliente.kne_activa.toString()
                if (cliente.kne_activa > 0.0) {
                    alertError()
                } else {
                    alertNormal()
                }
            }



            binding.tvCodigoCliente.text = cliente.codigo
            binding.tvNombreCliente.text = cliente.nombre

            if (cliente.status == null) {

                val conn = AdminSQLiteOpenHelper(context, "ke_android", null)

                val diasPedidos =
                    conn.getCampoIntCamposVarios("cliempre", "diasultvta", listOf("codigo", "empresa"), listOf(cliente.codigo, codEmpresa))

                binding.tvPedidoDiasMain.text = "Dias sin hacer pedidos: "
                binding.tvPedidoDias.text = if (diasPedidos <= 0) "0" else diasPedidos.toString()
                diasSinPedido(diasPedidos, binding.tvPedidoDias)
            } else {
                binding.tvPedidoDiasMain.text = "Documentos: "
                binding.tvPedidoDias.text = cliente.status.toInt().toString()
            }

            //Icono de advertensia
            val comparar = ClienteAlertauseCase().comparar(cliente.codigo, context, codEmpresa)
            val colorIcon = ClienteAlertauseCase().compararIcon(cliente.codigo, context, codEmpresa)
            binding.ivAlerta.apply {
                isVisible = comparar
                setColorFilter(color(colorIcon))
            }





            binding.clMainSlecClientePedido.apply {
                setOnClickListener { onClickListener(cliente.codigo!!, cliente.nombre!!) }
                setOnLongClickListener {
                    onLongClickListener(cliente.codigo!!, cliente.nombre!!)
                    true
                }
            }
            /*binding.clMainSlecClientePedido.setOnClickListener {
                onClickListener(
                    cliente.codigo!!, cliente.nombre!!
                )
            }*/

        }

        private fun diasSinPedido(diasPedidos: Int, view: TextView) {
            view.apply {
                when (diasPedidos) {
                    in 0..8 -> {
                        setTextColor(color(R.color.greenColor))
                        setBackgroundResource(R.drawable.border_radius)
                    }

                    in 9..17 -> {
                        setTextColor(color(R.color.lightGreenColor))
                        setBackgroundResource(R.drawable.border_radius)
                    }

                    in 18..26 -> {
                        setTextColor(color(R.color.yellowColor))
                        setBackgroundResource(R.drawable.border_radius)
                    }

                    in 27..35 -> {
                        setTextColor(color(R.color.orangeColor))
                        setBackgroundResource(R.drawable.border_radius)
                    }

                    in 36..45 -> {
                        setTextColor(color(R.color.redColor))
                        setBackgroundResource(R.drawable.border_radius)
                    }

                    else -> {
                        setTextColor(color(R.color.white))
                        setBackgroundResource(R.drawable.border_radius_error)
                    }
                }
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
                R.layout.item_cliente_pedido, parent, false
            )
        )
    }

    override fun getItemCount(): Int = clientes.size

    override fun onBindViewHolder(holder: SeleccionarClientePedidoHolder, position: Int) {
        holder.render(clientes[position], onClickListener, onLongClickListener)
    }

    fun actualizarClientes(clientes: List<Cliente>) {
        this.clientes = clientes
        notifyDataSetChanged()
    }
}