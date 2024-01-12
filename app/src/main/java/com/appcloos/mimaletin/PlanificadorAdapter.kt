package com.appcloos.mimaletin

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.concurrent.TimeUnit

class PlanificadorAdapter(
    private val context: Context,
    private val listadocs: ArrayList<Documentos>
) : BaseAdapter() {
    private val inflater: LayoutInflater? = null
    override fun getCount(): Int {
        return listadocs.size
    }

    override fun getItem(i: Int): Any {
        return listadocs[i]
    }

    override fun getItemId(i: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View, viewGroup: ViewGroup): View {
        var convertView = convertView
        convertView = LayoutInflater.from(context).inflate(R.layout.item_planificador, null)
        val documentos = getItem(position) as Documentos
        val cons_item = convertView.findViewById<ConstraintLayout>(R.id.const_item)
        val tvp_codcliente = convertView.findViewById<View>(R.id.tvp_codcliente) as TextView
        val tvp_nombrecli = convertView.findViewById<View>(R.id.tvp_nombrecli) as TextView
        val tvp_estatus = convertView.findViewById<View>(R.id.tvp_estatus) as TextView
        val tvp_documento = convertView.findViewById<View>(R.id.tvp_ndoc) as TextView
        val tvp_vence = convertView.findViewById<View>(R.id.tvp_vence) as TextView
        val tvp_dias = convertView.findViewById<View>(R.id.tvp_dias) as TextView
        val estatus = documentos.estatusdoc
        val negociacionEspecial = documentos.ktiNegesp
        when (estatus) {
            "1" -> tvp_estatus.text = "Abonado"
            "0" -> tvp_estatus.text = "Pendiente"
        }
        try {
            var vence = ""
            vence = documentos.vence
            val sdf = SimpleDateFormat("yyyy-MM-dd")
            val hoy = Calendar.getInstance().time
            val vencimiento = sdf.parse(vence)
            val calendar = Calendar.getInstance()
            calendar.time = vencimiento
            if (negociacionEspecial == "1") {
                calendar.add(Calendar.DATE, 10)
                println("Entro al if de los documentos con negociacion especial")
                tvp_vence.text = documentos.vence
                tvp_vence.setTextColor(Color.parseColor("#172a8a"))
            } else if (negociacionEspecial == "0") {
                calendar.add(Calendar.DATE, 0)
                println("Entro al if de los documentos sin negociacion especial")
                tvp_vence.text = documentos.vence
            }
            val nuevoVencimiento = calendar.time
            val diff = hoy.time - nuevoVencimiento.time
            val time = TimeUnit.DAYS
            val diferencia = time.convert(diff, TimeUnit.MILLISECONDS)
            val difFechas = diferencia.toString().toInt()
            if (difFechas > 0) {
                cons_item.setBackgroundColor(Color.parseColor("#f2766d"))
                tvp_dias.text = difFechas.toString()
            } else if (difFechas >= -7) {
                cons_item.setBackgroundColor(Color.parseColor("#e0df99"))
                tvp_dias.text = difFechas.toString()
            } else if (difFechas < -7) {
                tvp_dias.text = difFechas.toString()
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
        tvp_codcliente.text = documentos.codcliente
        tvp_nombrecli.text = documentos.nombrecli
        tvp_documento.text = documentos.documento
        val diascred = Math.round(documentos.diascred).toInt()
        return convertView
    }
}
