package com.appcloos.mimaletin

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.appcloos.mimaletin.databinding.ItemDetallesCxcBinding
import com.appcloos.mimaletin.model.CXC.ke_precobradocs

class DetalleCXCAdapter(
    private var kePrecobradocs: List<ke_precobradocs>, private val context: Context
) : RecyclerView.Adapter<DetalleCXCAdapter.DetalleCXCHolder>() {
    inner class DetalleCXCHolder(view: View) : RecyclerView.ViewHolder(view) {

        private var conn = AdminSQLiteOpenHelper(context, "ke_android", null, 26)
        private val binding = ItemDetallesCxcBinding.bind(view)

        fun render(kePrecobradocs: ke_precobradocs) {
            val moneda = kePrecobradocs.tnetoddol == 0.00
            val tasa =
                conn.getCampoDouble("ke_doccti", "tasadoc", "documento", kePrecobradocs.documento)

            val iva = kePrecobradocs.bsmtoiva - kePrecobradocs.bsretiva
            val flete = kePrecobradocs.bsmtofte - kePrecobradocs.bsretfte

            val ivaFlete = iva + flete

            binding.tvDoc.text = kePrecobradocs.documento

            if (moneda) {
                binding.tvTotal.text =
                    "Total: " + ObjetoUtils.valorReal(kePrecobradocs.tnetodbs).toString()
                binding.tvNeto.text =
                    "Neto: " + ObjetoUtils.valorReal(kePrecobradocs.tnetodbs - ivaFlete).toString()

                binding.tvIva.text = "IVA: " + ObjetoUtils.valorReal(iva).toString()
                binding.tvFlete.text = "Flete: " + ObjetoUtils.valorReal(flete).toString()

                binding.tvRetIva.text =
                    "Ret. IVA: " + ObjetoUtils.valorReal(kePrecobradocs.bsretiva).toString()
                binding.tvRetFlete.text =
                    "Ret. Flete: " + ObjetoUtils.valorReal(kePrecobradocs.bsretfte).toString()

            } else {
                binding.tvTotal.text =
                    "Total: " + ObjetoUtils.valorReal(kePrecobradocs.tnetoddol).toString()
                binding.tvNeto.text =
                    "Neto: " + ObjetoUtils.valorReal(kePrecobradocs.tnetoddol - (ivaFlete / tasa))
                        .toString()

                binding.tvIva.text = "IVA: " + ObjetoUtils.valorReal(iva / tasa).toString()
                binding.tvFlete.text = "Flete: " + ObjetoUtils.valorReal(flete / tasa).toString()

                binding.tvRetIva.text =
                    "Ret. IVA: " + ObjetoUtils.valorReal(kePrecobradocs.bsretiva / tasa).toString()
                binding.tvRetFlete.text =
                    "Ret. Flete: " + ObjetoUtils.valorReal(kePrecobradocs.bsretfte / tasa)
                        .toString()
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