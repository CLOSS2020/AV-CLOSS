package com.appcloos.mimaletin.moduloCXC.fragments.edoGenCuentaAdapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.appcloos.mimaletin.Constantes
import com.appcloos.mimaletin.databinding.ItemEstadoGeneralBinding
import com.appcloos.mimaletin.diferenciaFehca
import com.appcloos.mimaletin.moduloCXC.viewmodel.EdoGeneralCxc
import com.appcloos.mimaletin.setDrawableHeadVariantAgencia

class EdoGenCuentaHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val binding = ItemEstadoGeneralBinding.bind(view)

    fun render(edoGen: EdoGeneralCxc, onClickListener: (String, String) -> Unit) {
        val fechaVencimiento = if (edoGen.fechaVencimiento.diferenciaFehca().toInt() > 0) {
            "${edoGen.fechaVencimiento.diferenciaFehca()} días"
        } else {
            "Por vencer"
        }

        binding.tvCodcliEstGen.setDrawableHeadVariantAgencia(Constantes.AGENCIA)

        // asignacion de variables a los campos de texto a mostrar
        binding.tvCodcliEstGen.text = edoGen.codigocliente
        binding.tvNomclieEstGen.text = edoGen.nomcliente
        binding.tvDeudaEstGen.text = edoGen.montoTotal.toString()
        binding.tvFechaEstGen.text = fechaVencimiento
        binding.tvLimiteEstGen.text = edoGen.limite.toString()
        binding.tvSaldoEstGen.text = edoGen.saldo.toString()

        binding.itemEdoGenCuenta.setOnClickListener {
            onClickListener(
                edoGen.codigocliente,
                edoGen.nomcliente
            )
        }
    }
}
