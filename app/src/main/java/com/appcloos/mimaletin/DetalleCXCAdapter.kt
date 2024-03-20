package com.appcloos.mimaletin

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.appcloos.mimaletin.databinding.ItemDetallesCxcBinding
import com.appcloos.mimaletin.model.CXC.ke_precobradocs
import kotlin.math.abs

class DetalleCXCAdapter(
    private var kePrecobradocs: List<ke_precobradocs>,
    private val context: Context,
    private val codEmpresa: String
) : RecyclerView.Adapter<DetalleCXCAdapter.DetalleCXCHolder>() {
    inner class DetalleCXCHolder(view: View) : RecyclerView.ViewHolder(view) {

        private var conn = AdminSQLiteOpenHelper(context, "ke_android", null)
        private val binding = ItemDetallesCxcBinding.bind(view)

        fun render(kePrecobradocs: ke_precobradocs) {
            val moneda = kePrecobradocs.tnetoddol == 0.00

            /*val tasaDia = conn.getCampoDoubleCamposVarios(
                "ke_precobradocs",
                "tasadiad",
                listOf("documento", "cxcndoc", "empresa"),
                listOf(kePrecobradocs.documento, kePrecobradocs.cxcndoc, codEmpresa)
            )

            val tasa = conn.getCampoDoubleCamposVarios(
                "ke_precobradocs",
                "tasadoc",
                listOf("documento", "cxcndoc", "empresa"),
                listOf(kePrecobradocs.documento, kePrecobradocs.cxcndoc, codEmpresa)
            )*/

            // 2024-01-25 Para el flete dolarizado
            val tasa = if (kePrecobradocs.dolarflete == 0) {
                binding.tvDolarFlete.isVisible = true
                kePrecobradocs.tasadoc
            } else {
                binding.tvDolarFlete.isVisible = false
                kePrecobradocs.tasadiad
            }

            val iva = kePrecobradocs.bsmtoiva - abs(kePrecobradocs.bsretiva)
            val flete = kePrecobradocs.bsmtofte - abs(kePrecobradocs.bsretfte)

            val ivaFlete = iva + flete

            binding.tvDoc.text = kePrecobradocs.documento

            if (moneda) {
                binding.tvTotal.text = "Total: " + kePrecobradocs.tnetodbs.round().formatoNumFull()
                binding.tvNeto.text =
                    "Neto: " + (kePrecobradocs.tnetodbs - ivaFlete).round().formatoNumFull()

                binding.tvIva.text = "IVA: " + iva.round().formatoNumFull()
                binding.tvFlete.text = "Flete: " + flete.round().formatoNumFull()

                binding.tvRetIva.text =
                    "Ret. IVA: " + kePrecobradocs.bsretiva.round().formatoNumFull()
                binding.tvRetFlete.text =
                    "Ret. Flete: " + kePrecobradocs.bsretfte.round().formatoNumFull()
            } else {
                binding.tvTotal.text = "Total: " + kePrecobradocs.tnetoddol.round().formatoNumFull()
                binding.tvNeto.text =
                    "Neto: " + (kePrecobradocs.tnetoddol - (ivaFlete / tasa)).round()
                        .formatoNumFull()

                binding.tvIva.text = "IVA: " + (iva / tasa).round().formatoNumFull()

                binding.tvFlete.text = "Flete: " + (flete / tasa).round().formatoNumFull()

                binding.tvRetIva.text =
                    "Ret. IVA: " + (kePrecobradocs.bsretiva / tasa).round().formatoNumFull()
                binding.tvRetFlete.text =
                    "Ret. Flete: " + (kePrecobradocs.bsretfte / tasa).round().formatoNumFull()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetalleCXCHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return DetalleCXCHolder(
            layoutInflater.inflate(
                R.layout.item_detalles_cxc, parent, false
            )
        )
    }

    override fun getItemCount(): Int = kePrecobradocs.size

    override fun onBindViewHolder(holder: DetalleCXCHolder, position: Int) {
        holder.render(kePrecobradocs[position])
    }
}
