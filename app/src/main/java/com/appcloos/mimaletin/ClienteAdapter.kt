package com.appcloos.mimaletin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.appcloos.mimaletin.databinding.ItemClienteBinding
import com.appcloos.mimaletin.model.cliente.ClientesKt

class ClienteAdapter(
    private var listacliente: List<ClientesKt> = emptyList(),
    private val onClickListener: (Int) -> Unit
) : RecyclerView.Adapter<ClienteAdapter.ViewHolderDatos>() {

    fun updateAdapter(newList: List<ClientesKt>) {
        listacliente = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderDatos {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cliente, parent, false)
        return ViewHolderDatos(view)
    }

    override fun onBindViewHolder(holder: ViewHolderDatos, position: Int) {
        holder.asignarDatos(listacliente[position], onClickListener)
    }

    override fun getItemCount(): Int {
        return listacliente.size
    }

    class ViewHolderDatos(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val binding = ItemClienteBinding.bind(itemView)

        fun asignarDatos(clientes: ClientesKt, onClickListener: (Int) -> Unit) {
            binding.apply {
                textCodcliente.text = "Código: " + clientes.codigo
                textNombrecliente.text = clientes.nombre
                textDireccion.text = "Dirección: " + clientes.direccion
            }

            if ((clientes.diasultvta > clientes.promdiasvta) ||
                (clientes.prcdpagdia < 50.0) ||
                (clientes.riesgocrd > 10.0) ||
                (clientes.diasultvta > 40.0)
            ) {
                alertRed(binding.mainContainer)
            } else if ((clientes.email.isEmpty()) ||
                (clientes.perscont.isEmpty()) ||
                (clientes.telefonos.isEmpty())
            ) {
                alertYellow(binding.mainContainer)
            } else {
                normalColor(binding.mainContainer)
            }

            binding.mainContainer.setOnClickListener {
                onClickListener(absoluteAdapterPosition)
            }
        }

        private fun alertRed(view: ConstraintLayout) {
            view.apply {
                setBackgroundColor(color(R.color.errorColor))
            }
        }

        private fun alertYellow(view: ConstraintLayout) {
            view.apply {
                setBackgroundColor(color(R.color.warningColor))
            }
        }

        private fun normalColor(view: ConstraintLayout) {
            view.apply {
                setBackgroundColor(color(R.color.basicStyleRecicleView))
            }
        }
    }
}
