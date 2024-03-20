package com.appcloos.mimaletin.moduloCXC.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.core.view.isVisible
import com.appcloos.mimaletin.Constantes
import com.appcloos.mimaletin.Documentos
import com.appcloos.mimaletin.R
import com.appcloos.mimaletin.colorAgencia
import com.appcloos.mimaletin.databinding.ItemCheckDocsCxcBinding
import com.appcloos.mimaletin.noRepeatList
import com.appcloos.mimaletin.round
import com.appcloos.mimaletin.setDrawableAgencia
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.ceil

// todo: Cambio Importante
// 2024-01-09 se decidio cambiar a un ListView debido a que el RecicleView repetia el comportamiento
// de las checkbox chekeadas como true a otras checkbox no chekeadas

class EdoCuentaClienteAdapterList(
    private var documentos: ArrayList<Documentos>,
    private var quantityListener: EdoCuentaClienteAdapter.QuantityListener,
    private var docsViejos: ArrayList<String>,
    private var listaDocsSeleccionados: ArrayList<String>,
    private var numViejo: Int = 0,
    private var numNuevo: Int = 0,
    private var DIAS_VALIDOS_BOLIVARES: Int
) : BaseAdapter() {

    private var listaSelec: ArrayList<String> = ArrayList()

    override fun getCount(): Int = documentos.size

    override fun getItem(p0: Int): Any = documentos[p0]

    override fun getItemId(p0: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val documento = getItem(position) as Documentos

        val convertView =
            LayoutInflater.from(parent!!.context).inflate(R.layout.item_check_docs_cxc, null)

        val binding = ItemCheckDocsCxcBinding.bind(convertView!!)

        setColors(binding)

        listaSelec.noRepeatList()

        // 2023-06-08 if que verifica que los documentos que fueron seleccionados previamente permanescan chequeados y agregados a la nueva lista
        if (listaDocsSeleccionados.indexOf("\'${documento.documento}\'") != -1) {
            binding.cbSelDoc.isChecked = true
            listaSelec.add("\'${documento.documento}\'")
        }

        // 2023-06-08 if que valida que si fueron chequeados todos los documentos vencidos todos los doscumentos van a estar desbloqueados para ser chequeados
        if (numViejo == docsViejos.size) {
            binding.cbSelDoc.isEnabled = true
            binding.cbSelDoc.isVisible = true
        } else {
            // 2023-06-08 if que inavilita y deschequea los CheckBox de los documentos no vencidos en caso de que todos los documentos vencidos no este chequeada
            if (docsViejos.indexOf(documento.documento) == -1) {
                listaSelec.remove("\'${documento.documento}\'")
                binding.cbSelDoc.isChecked = false
                binding.cbSelDoc.isEnabled = false
                binding.cbSelDoc.isVisible = false
            }
        }

        // 2023-06-08 if que habilita los CheckBox de los documentos vencidos sin importar el caso
        if (docsViejos.indexOf(documento.documento) != -1) {
            binding.cbSelDoc.isEnabled = true
            binding.cbSelDoc.isVisible = true
        }

        binding.tvMontototal.text = "Total: ${(ceil((documento.dtotalfinal) * 100) / 100)} $"
        binding.tvMontodebe.text =
                // a lo pagado se le suma la devolucion debido a que se genero una nota de credito
                // Ademas se suman las retenciones ya pagadas para que la deuda baje
                // (dtotpagos solo refleja dinero y no retenciones que son papel)
                // y todo lo demas resta a la deuda original para saber lo que de verdad se paga
            "Deuda: ${(documento.dtotalfinal - ((documento.dtotpagos + documento.dtotdev) + documento.dretencion)).round()} $"
        binding.tvNrodoc.text = documento.documento

        // 2023-07-14 se comento ya que ahora notas y facturas pueden llevar bolivares y dolares
        /*binding.tvTipodocC.text = if (documento.documento.indexOf('E') != -1) {
            "NOTA DE ENTREGA $."
        } else {
         */
        /*if (documento.diascred != 15.0){
                if (documento.diascred == 25.0 && diasVencidos(documento.vence)){
                    "FACTURA $."
                }else if(documento.diascred != 25.0){
                    "FACTURA $."
                }else{
                    "FACTURA Bs."
                }
            }else{
                if (compararFecha(documento.vence) < 0){
                    "FACTURA $."
                }else{
                    "FACTURA Bs."
                }
            }*/
        /*

            if(documento.diascred.toInt() >= DIAS_VALIDOS_BOLIVARES){
                if (!(diasVencidos(documento.recepcion))){
                    "FACTURA $."
                }else{
                    "FACTURA $/Bs."
                }
            }else{
                if (compararFecha(documento.vence) < 0){
                    "FACTURA $."
                }else{
                    "FACTURA $/Bs."
                }

            }

         */
        /*if (compararFecha(documento.vence) >= 0 && documento.diascred == 15.0) {
                "FACTURA Bs."
            } else {
                compararFecha(documento.vence)
                "FACTURA $."
            }*/
        /*

        }*/

        binding.tvTipodocC.text = if (documento.documento.indexOf('E') != -1) {
            "NOTA DE ENTREGA"
        } else {
            "FACTURA"
        }

        binding.tvFecharecepcion.text =
                // 2024-02-27 El estado de entrega del documento
            when (documento.edoentrega) {
                0 -> "Facturado"
                1 -> "En tránsito"
                2 -> "Recepción: ${documento.recepcion} (${diferenciaFehca(documento.recepcion)} días)"
                else -> "No identificado"
            }

        binding.tvFechavencimiento.visibility = when (documento.edoentrega) {
            in 0..1 -> View.GONE
            2 -> View.VISIBLE
            else -> View.VISIBLE
        }

        binding.tvFechavencimiento.text =
            "Vencecimiento: ${documento.vence} (${diferenciaFehca(documento.vence)} días)"

        binding.tvStatus.text = when (documento.estatusdoc) {
            "0" -> "Sin pagos"
            "1" -> "Abonado"
            "2" -> "Pagado"
            else -> {
                "No Identificado"
            }
        }

        //binding.tvReclamo.isVisible = documento.dtotdev > 0.0
        binding.tvReclamo.visibility = if (documento.dtotdev > 0.0) View.VISIBLE else View.GONE
        binding.tvDolarFlete.visibility = if (documento.dolarflete > 0) View.VISIBLE else View.GONE

        binding.cbSelDoc.setOnCheckedChangeListener { _, isChecked ->

            listaSelec = listaSelec.noRepeatList()

            if (isChecked) {
                listaSelec.add("\'${documento.documento}\'")
                if (docsViejos.indexOf(documento.documento) != -1) {
                    numViejo++
                } else {
                    numNuevo++
                }
            } else {
                listaSelec.remove("\'${documento.documento}\'")

                if (docsViejos.indexOf(documento.documento) != -1) {
                    numViejo--
                } else {
                    numNuevo--
                }

                if (numViejo != docsViejos.size) {
                    var i = 0
                    while (i < listaSelec.size) {
                        val lValue: String = listaSelec[i]
                        var myString = StringBuilder(lValue)
                        myString = myString.deleteCharAt(0).deleteCharAt(myString.length - 1)
                        if (docsViejos.indexOf(myString.toString()) == -1) {
                            listaSelec.remove(lValue)
                            i--
                        }
                        i++
                    }
                }
            }
            quantityListener.onQuantityChange(listaSelec, numViejo, numNuevo)
        }

        return binding.root
    }

    private fun setColors(binding: ItemCheckDocsCxcBinding) {
        binding.clParent.setDrawableAgencia(Constantes.AGENCIA)
        binding.tvMontodebe.setTextColor(binding.tvMontodebe.colorAgencia(Constantes.AGENCIA))
    }

    private fun diferenciaFehca(fecha: String?): String {
        val fecha1 = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(fecha)
        val fecha2 = Date(System.currentTimeMillis())
        val milisegundos = fecha2.time - fecha1!!.time
        val segundos: Long = milisegundos / 1000
        val minutos = segundos / 60
        val horas = minutos / 60
        val dias = horas / 24
        return dias.toString()
    }
}
