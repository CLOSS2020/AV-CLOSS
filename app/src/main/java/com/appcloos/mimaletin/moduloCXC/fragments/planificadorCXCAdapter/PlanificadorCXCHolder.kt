package com.appcloos.mimaletin.moduloCXC.fragments.planificadorCXCAdapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.appcloos.mimaletin.ObjetoUtils
import com.appcloos.mimaletin.R
import com.appcloos.mimaletin.color
import com.appcloos.mimaletin.databinding.ItemPlanificadorCxcBinding
import com.appcloos.mimaletin.formatoFechaShow
import com.appcloos.mimaletin.moduloCXC.viewmodel.PlanificadorCxc

class PlanificadorCXCHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val binding = ItemPlanificadorCxcBinding.bind(view)

    private val context = binding.root.context

    fun render(planCXC: PlanificadorCxc, onClickListener: (String, String) -> Unit) {
        // Variables que guardaran texto y colores
        val edoFact: String
        val tvDocVenceText: String
        val tvDocVenceTextColor: Int
        val tvDocVenceBackground: Int
        val tvFechaVencimientoModulCXCTextColor: Int
        val tvEdoFactModulCXCTextColor: Int

        val dateNow = ObjetoUtils.getDateNow()

        // Formateo de la fecha de Vencimiento del Documento
        val date1 = ObjetoUtils.formatoFecha(planCXC.fechaVencimiento)

        // Formateo de la fecha Hoy
        val date2 = ObjetoUtils.formatoFecha(dateNow)

        // Dias de diferencia entre la fecha de Vencimiento del Documento y la fecha hoy
        val dias = ObjetoUtils.restarFechas(date1.time, date2.time, "dias")

        if (date1 == date2) {
            tvDocVenceText = "Vence Hoy"
            tvDocVenceTextColor = R.color.white
            tvDocVenceBackground = R.drawable.border_radius_error
            tvFechaVencimientoModulCXCTextColor = R.color.redColor
        } else if (date1 > date2) {
            tvDocVenceBackground = R.drawable.border_radius
            when (dias.toInt()) {
                in 4..7 -> {
                    tvDocVenceText = "Vence en $dias días"
                    tvDocVenceTextColor = R.color.lightGreenColor
                    tvFechaVencimientoModulCXCTextColor = R.color.yellowColor
                }

                in 2..3 -> {
                    tvDocVenceText = "Vence en $dias días"
                    tvDocVenceTextColor = R.color.yellowColor
                    tvFechaVencimientoModulCXCTextColor = R.color.redColor
                }

                1 -> {
                    tvDocVenceText = "Vence en $dias días"
                    tvDocVenceTextColor = R.color.orangeColor
                    tvFechaVencimientoModulCXCTextColor = R.color.redColor
                }

                else -> {
                    tvDocVenceText = "Por vencer"
                    tvDocVenceTextColor = R.color.greenColor
                    tvFechaVencimientoModulCXCTextColor = R.color.primaryLightColor
                }
            }
        } else {
            tvDocVenceText = "Vencido"
            tvDocVenceTextColor = R.color.redColor
            tvDocVenceBackground = R.drawable.border_radius
            tvFechaVencimientoModulCXCTextColor = R.color.redColor
        }

        // 2023-10-13 Apply sirve para indicar que todo lo que se haga deltro de las llaves le afecta
        // a la view binding.tvDocVence

        // 2023-07-14 se comento ya que ahora notas y facturas pueden llevar bolivares y dolares
        /*if (planCXC.doc.indexOf('E') != -1) {
            edoPedi = "N/E $."
            binding.tvEdoPediModulCXC.setTextColor(Color.rgb(117, 117, 117))
        }else{
         *//*if (planCXC.diascred != 15){
                    if (planCXC.diascred == 25 && diasVencidos(planCXC.fechaVencimiento, DIAS_VALIDOS_BOLIVARES)){
                        edoPedi = "FAC $."
                        binding.tvEdoPediModulCXC.setTextColor(Color.rgb(117, 117, 117))
                    }else if(planCXC.diascred != 25){
                        edoPedi = "FAC $."
                        binding.tvEdoPediModulCXC.setTextColor(Color.rgb(117, 117, 117))
                    }else{
                        edoPedi = "FAC $/Bs."
                        if (compararFecha(planCXC.fechaVencimiento) == 0){
                            binding.tvEdoPediModulCXC.setTextColor(Color.rgb(255, 194, 34))
                        } else if (compararFecha(planCXC.fechaVencimiento) < 0) {
                            binding.tvEdoPediModulCXC.setTextColor(Color.rgb(255, 3, 35))
                        } else if (compararFecha(planCXC.fechaVencimiento) > 0) {
                            binding.tvEdoPediModulCXC.setTextColor(Color.rgb(96, 203, 64))
                        }
                    }
                }else{
                    if (compararFecha(planCXC.fechaVencimiento) < 0){
                        edoPedi = "FAC $."
                        binding.tvEdoPediModulCXC.setTextColor(Color.rgb(117, 117, 117))
                    }else{
                        edoPedi = "FAC $/Bs."
                        if (compararFecha(planCXC.fechaVencimiento) == 0){
                            binding.tvEdoPediModulCXC.setTextColor(Color.rgb(255, 194, 34))
                        } else if (compararFecha(planCXC.fechaVencimiento) < 0) {
                            binding.tvEdoPediModulCXC.setTextColor(Color.rgb(255, 3, 35))
                        } else if (compararFecha(planCXC.fechaVencimiento) > 0) {
                            binding.tvEdoPediModulCXC.setTextColor(Color.rgb(96, 203, 64))
                        }
                    }
                }*//*

                if(planCXC.diascred >= DIAS_VALIDOS_BOLIVARES){
                    if (!(diasVencidos(planCXC.fechaRecepcion, DIAS_VALIDOS_BOLIVARES))){
                        edoPedi = "FAC $."
                        binding.tvEdoPediModulCXC.setTextColor(Color.rgb(117, 117, 117))
                    }else{
                        edoPedi = "FAC $/Bs."
                        if (compararFecha(planCXC.fechaVencimiento) == 0){
                            binding.tvEdoPediModulCXC.setTextColor(Color.rgb(255, 194, 34))
                        } else if (compararFecha(planCXC.fechaVencimiento) < 0) {
                            binding.tvEdoPediModulCXC.setTextColor(Color.rgb(255, 3, 35))
                        } else if (compararFecha(planCXC.fechaVencimiento) > 0) {
                            binding.tvEdoPediModulCXC.setTextColor(Color.rgb(16, 124, 65))
                        }
                    }
                }else{
                    if (compararFecha(planCXC.fechaVencimiento) < 0){
                        edoPedi = "FAC $."
                        binding.tvEdoPediModulCXC.setTextColor(Color.rgb(117, 117, 117))
                    }else{
                        edoPedi = "FAC $/Bs."
                        if (compararFecha(planCXC.fechaVencimiento) == 0){
                            binding.tvEdoPediModulCXC.setTextColor(Color.rgb(255, 194, 34))
                        } else if (compararFecha(planCXC.fechaVencimiento) < 0) {
                            binding.tvEdoPediModulCXC.setTextColor(Color.rgb(255, 3, 35))
                        } else if (compararFecha(planCXC.fechaVencimiento) > 0) {
                            binding.tvEdoPediModulCXC.setTextColor(Color.rgb(16, 124, 65))
                        }
                    }

                }

         *//*if (planCXC.diascred == 15){
                    edoPedi = "FAC Bs."
                    if (compararFecha(planCXC.fechaVencimiento) == 0){
                        binding.tvEdoPediModulCXC.setTextColor(Color.rgb(255, 194, 34))
                    } else if (compararFecha(planCXC.fechaVencimiento) < 0) {
                        binding.tvEdoPediModulCXC.setTextColor(Color.rgb(255, 3, 35))
                    } else if (compararFecha(planCXC.fechaVencimiento) > 0) {
                        binding.tvEdoPediModulCXC.setTextColor(Color.rgb(16, 124, 65))
                    }
                }else{
                    edoPedi = "FAC $."
                    binding.tvEdoPediModulCXC.setTextColor(Color.rgb(117, 117, 117))
                }*//*

            }*/
        // 2023-06-05 Se comento para sustituir traquin por tipo de factura (si es una factura que se puede pagar en bolivares o no)
        /*edoPedi = when (planCXC.edoPedi) {
            "01" -> "Esperando por Aprobación"
            "12" -> "Ya impreso"
            "17" -> "En Proceso de embalaje"
            "20" -> "En proceso de Etiquetado"
            "25" -> "Listo Para facturar"
            "80" -> "Facturado"
            "82" -> "Esperando orden de salida"
            "85" -> "Entregado al cliente"
            else -> "No identificado"

        }*/

        val edoPedi: String = if (planCXC.doc.indexOf('E') == -1) {
            context.getString(R.string.fac)
        } else {
            context.getString(R.string.n_e)
        }

        when (planCXC.edoFact) {
            "0" -> {
                edoFact = context.getString(R.string.por_pagar)
                tvEdoFactModulCXCTextColor = R.color.redColor
            }

            "1" -> {
                edoFact = context.getString(R.string.abonado)
                tvEdoFactModulCXCTextColor = R.color.yellowColor
            }

            "2" -> {
                edoFact = context.getString(R.string.pagado)
                tvEdoFactModulCXCTextColor = R.color.greenColor
            }

            else -> {
                edoFact = context.getString(R.string.no_identificado)
                tvEdoFactModulCXCTextColor = R.color.blackColor
            }
        }

        binding.tvReclamo.visibility = if (planCXC.reclamo) View.VISIBLE else View.INVISIBLE
        binding.tvDolarFlete.visibility = if (planCXC.dolarFlete) View.VISIBLE else View.INVISIBLE

        "${context.getString(R.string.plancxc_numero_doc)}: ${planCXC.doc}".also {
            binding.tvDocModulCXC.text = it
        }

        binding.tvClienteModulCXC.text = planCXC.cliente
        "$ ${ObjetoUtils.valorReal(planCXC.monto)}".also { binding.tvMontoModulCXC.text = it }

        // Fecha de vencimiento del documento
        val plancxcVencimiento =
            "${context.getString(R.string.plancxc_vencimiento)}: ${planCXC.fechaVencimiento.formatoFechaShow()}"

        "${context.getString(R.string.plancxc_recepcion)}: ${planCXC.fechaRecepcion.formatoFechaShow()}".also {
            binding.tvFechaRecepcionModulCXC.text = it
        }

        // Abonado o Por Pagar
        val plancxcCuenta = "${context.getString(R.string.plancxc_cuenta)}: $edoFact"

        "${context.getString(R.string.plancxc_doc)}: $edoPedi".also {
            binding.tvEdoPediModulCXC.text = it
        }

        binding.tvDocVence.apply {
            text = tvDocVenceText
            // Color es una funcion de extension de las View
            setTextColor(color(tvDocVenceTextColor))
            setBackgroundResource(tvDocVenceBackground)
        }

        binding.tvFechaVencimientoModulCXC.apply {
            text = plancxcVencimiento
            // Color es una funcion de extension de las View
            setTextColor(color(tvFechaVencimientoModulCXCTextColor))
        }

        binding.tvEdoFactModulCXC.apply {
            text = plancxcCuenta
            // Color es una funcion de extension de las View
            setTextColor(color(tvEdoFactModulCXCTextColor))
        }

        binding.clMainModulCXC.setOnClickListener {
            onClickListener(
                planCXC.codcliente, planCXC.cliente
            )
        }
    }

    /*private fun diferenciaFehca(fecha: String): Int {
        val fecha2 = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(fecha)
        val fecha1 = Date(System.currentTimeMillis())
        val milisegundos = fecha2!!.time - fecha1.time
        val segundos: Long = milisegundos / 1000
        val minutos = segundos / 60
        val horas = minutos / 60
        val dias = horas / 24

        return dias.toInt()
    }*/

    /*private fun compararFecha(fechaRecepcion: String): Int {

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

    }*/

    /*private fun diasVencidos(fechaRecepcion: String, DIAS_VALIDOS_BOLIVARES: Int): Boolean {
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

    }*/
}
