package com.appcloos.mimaletin.moduloCXC.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.appcloos.mimaletin.Constantes
import com.appcloos.mimaletin.Documentos
import com.appcloos.mimaletin.R
import com.appcloos.mimaletin.colorAgencia
import com.appcloos.mimaletin.databinding.ItemCheckDocsCxcBinding
import com.appcloos.mimaletin.setDrawableAgencia
import com.appcloos.mimaletin.toTwoDecimals
import com.appcloos.mimaletin.valorReal
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.ceil


class EdoCuentaClienteAdapter(
    private var documentos: ArrayList<Documentos>,
    private var quantityListener: QuantityListener,
    private var docsViejos: ArrayList<String>,
    private var listaDocsSeleccionados: ArrayList<String>,
    private var numViejo: Int = 0,
    private var numNuevo: Int = 0,
    private var DIAS_VALIDOS_BOLIVARES: Int
) : RecyclerView.Adapter<EdoCuentaClienteAdapter.EdoCuentaClienteHolder>() {

    var listaSelec: ArrayList<String> = ArrayList()

    inner class EdoCuentaClienteHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val binding = ItemCheckDocsCxcBinding.bind(view)

        fun render(documento: Documentos) {

            setColors()

            //2023-06-08 if que verifica que los documentos que fueron seleccionados previamente permanescan chequeados y agregados a la nueva lista
            if (listaDocsSeleccionados.indexOf("\'${documento.documento}\'") != -1) {
                binding.cbSelDoc.isChecked = true
                listaSelec.add("\'${documento.documento}\'")
            }

            //2023-06-08 if que valida que si fueron chequeados todos los documentos vencidos todos los doscumentos van a estar desbloqueados para ser chequeados
            if (numViejo == docsViejos.size) {
                binding.cbSelDoc.isEnabled = true
                binding.cbSelDoc.isVisible = true
            } else {
                //2023-06-08 if que inavilita y deschequea los CheckBox de los documentos no vencidos en caso de que todos los documentos vencidos no este chequeada
                if (docsViejos.indexOf(documento.documento) == -1) {
                    listaSelec.remove("\'${documento.documento}\'")
                    binding.cbSelDoc.isChecked = false
                    binding.cbSelDoc.isEnabled = false
                    binding.cbSelDoc.isVisible = false
                }
            }

            //2023-06-08 if que habilita los CheckBox de los documentos vencidos sin importar el caso
            if (docsViejos.indexOf(documento.documento) != -1) {
                binding.cbSelDoc.isEnabled = true
                binding.cbSelDoc.isVisible = true
            }

            binding.tvMontototal.text = "Total: ${(ceil((documento.dtotalfinal) * 100) / 100)} $"
            binding.tvMontodebe.text = //a lo pagado se le suma la devolucion debido a que se genero una nota de credito
                //Ademas se suman las retenciones ya pagadas para que la deuda baje
                    // (dtotpagos solo refleja dinero y no retenciones que son papel)
                //y todo lo demas resta a la deuda original para saber lo que de verdad se paga
                "Deuda: ${(documento.dtotalfinal - ((documento.dtotpagos + documento.dtotdev) + documento.dretencion)).valorReal()} $"
            binding.tvNrodoc.text = documento.documento

            //2023-07-14 se comento ya que ahora notas y facturas pueden llevar bolivares y dolares
            /*binding.tvTipodocC.text = if (documento.documento.indexOf('E') != -1) {
                "NOTA DE ENTREGA $."
            } else {
                *//*if (documento.diascred != 15.0){
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
                }*//*

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

                *//*if (compararFecha(documento.vence) >= 0 && documento.diascred == 15.0) {
                    "FACTURA Bs."
                } else {
                    compararFecha(documento.vence)
                    "FACTURA $."
                }*//*

            }*/

            binding.tvTipodocC.text = if (documento.documento.indexOf('E') != -1) {
                "NOTA DE ENTREGA"
            } else {
                "FACTURA"
            }

            binding.tvFecharecepcion.text =
                "Recepción: ${documento.recepcion} (${diferenciaFehca(documento.recepcion)} días)"
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

            binding.tvReclamo.isVisible = documento.dtotdev > 0.0

            binding.cbSelDoc.setOnCheckedChangeListener { _, isChecked ->
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
        }

        private fun setColors() {
            binding.clParent.setDrawableAgencia(Constantes.AGENCIA)
            binding.tvMontodebe.setTextColor(binding.tvMontodebe.colorAgencia(Constantes.AGENCIA))
        }

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EdoCuentaClienteHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return EdoCuentaClienteHolder(
            layoutInflater.inflate(
                R.layout.item_check_docs_cxc, parent, false
            )
        )
    }

    override fun getItemCount(): Int = documentos.size

    override fun onBindViewHolder(holder: EdoCuentaClienteHolder, position: Int) {
        holder.render(documentos[position])
    }

    interface QuantityListener {
        fun onQuantityChange(listaChange: ArrayList<String>, numViejo: Int, numNuevo: Int)
    }

    private fun compararFecha(fechaRecepcion: String): Int {

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val current = LocalDateTime.now().format(formatter)

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val firstDate: Date = sdf.parse(fechaRecepcion) as Date
        val secondDate: Date = sdf.parse(current) as Date
        println("Comparacion de la fecha ${firstDate.compareTo(secondDate)}")

        //recepcion > fecha = 1
        //recepcion = fecha = 0
        //recepcion < fecha = -1

        return firstDate.compareTo(secondDate)

    }

    private fun diasVencidos(fechaRecepcion: String): Boolean {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = sdf.parse(fechaRecepcion)
        val cal = Calendar.getInstance()
        cal.time = date!!
        cal.add(Calendar.DATE, +DIAS_VALIDOS_BOLIVARES)

        val newDate: Date = cal.time

        var inActiveDate = "0000-00-00"

        try {
            inActiveDate = sdf.format(newDate)
            println(inActiveDate)
        } catch (e1: ParseException) {

            // TODO Auto-generated catch block
            e1.printStackTrace()
        }

        return compararFecha(inActiveDate) >= 0

    }
}