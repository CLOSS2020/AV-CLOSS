package com.appcloos.mimaletin.moduloCXC.fragments

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.appcloos.mimaletin.R
import com.appcloos.mimaletin.databinding.ItemPlanificadorCxcBinding
import com.appcloos.mimaletin.moduloCXC.viewmodel.PlanificadorCxc
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

class PlanificadorCXCAdapter(
    private var planificadorCxc: List<PlanificadorCxc>,
    private val onClickListener: (String, String) -> Unit,
    private var DIAS_VALIDOS_BOLIVARES: Int,
    private val context: Context
) :
    RecyclerView.Adapter<PlanificadorCXCAdapter.PlanificadorCXCHolder>() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val date: String = dateFormat.format(Date())

    inner class PlanificadorCXCHolder(view: View) : RecyclerView.ViewHolder(view) {

        private lateinit var edoPedi: String
        private lateinit var edoFact: String

        private val binding = ItemPlanificadorCxcBinding.bind(view)

        fun render(planCXC: PlanificadorCxc, onClickListener: (String, String) -> Unit) {

            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateNow = formatter.format(Date())

            val date1 = SimpleDateFormat(
                "yyyy-MM-dd", Locale.getDefault()
            ).parse(planCXC.fechaVencimiento)

            val date2 = SimpleDateFormat(
                "yyyy-MM-dd", Locale.getDefault()
            ).parse(dateNow)


            val diff = date1!!.time - date2!!.time
            val segundos = diff / 1000
            val minutos = segundos / 60
            val horas = minutos / 60
            val dias = horas / 24

            if (date1 == date2) {
                binding.tvDocVence.text = "Vence Hoy"
                binding.tvDocVence.setTextColor(Color.rgb(255, 255, 255))
                binding.tvDocVence.setBackgroundResource(R.drawable.border_radius_error)
                binding.tvFechaVencimientoModulCXC.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.redColor
                    )
                )
            } else if (date1 > date2) {
                when (dias.toInt()) {
                    in 4..7 -> {
                        binding.tvDocVence.text = "Vence en $dias días"
                        binding.tvDocVence.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.lightGreenColor
                            )
                        )
                        binding.tvFechaVencimientoModulCXC.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.yellowColor
                            )
                        )
                        binding.tvDocVence.setBackgroundResource(R.drawable.border_radius)
                    }

                    in 2..3 -> {
                        binding.tvDocVence.text = "Vence en $dias días"
                        binding.tvDocVence.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.yellowColor
                            )
                        )
                        binding.tvFechaVencimientoModulCXC.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.redColor
                            )
                        )
                        binding.tvDocVence.setBackgroundResource(R.drawable.border_radius)
                    }

                    1 -> {
                        binding.tvDocVence.text = "Vence en $dias días"
                        binding.tvDocVence.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.orangeColor
                            )
                        )
                        binding.tvFechaVencimientoModulCXC.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.redColor
                            )
                        )
                        binding.tvDocVence.setBackgroundResource(R.drawable.border_radius)
                    }

                    else -> {
                        binding.tvDocVence.text = "Por vencer"
                        binding.tvDocVence.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.greenColor
                            )
                        )
                        binding.tvFechaVencimientoModulCXC.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.primaryLightColor
                            )
                        )
                        binding.tvDocVence.setBackgroundResource(R.drawable.border_radius)
                    }
                }
            } else {
                binding.tvDocVence.text = "Vencido"
                binding.tvDocVence.setTextColor(ContextCompat.getColor(context, R.color.redColor))
                binding.tvFechaVencimientoModulCXC.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.redColor
                    )
                )
                binding.tvDocVence.setBackgroundResource(R.drawable.border_radius)
            }

            //2023-07-14 se comento ya que ahora notas y facturas pueden llevar bolivares y dolares
            /*if (planCXC.doc.indexOf('E') != -1) {
                edoPedi = "N/E $."
                binding.tvEdoPediModulCXC.setTextColor(Color.rgb(117, 117, 117))
            }else{
                *//*if (planCXC.diascred != 15){
                    if (planCXC.diascred == 25 && diasVencidos(planCXC.fechaVencimiento)){
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
                    if (!(diasVencidos(planCXC.fechaRecepcion))){
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
            //2023-06-05 Se comento para sustituir traquin por tipo de factura (si es una factura que se puede pagar en bolivares o no)
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

            edoPedi = if (planCXC.doc.indexOf('E') != -1) {
                "N/E"
            } else {
                "FAC"
            }

            edoFact = when (planCXC.edoFact) {
                "0" -> "Pendiente por pagar"
                "1" -> "Abonado"
                "2" -> "Pagado"
                else -> "No identificado"
            }

            when (planCXC.edoFact) {
                "0" -> {
                    edoFact = "Pendiente por pagar"
                    binding.tvEdoFactModulCXC.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.redColor
                        )
                    )
                }

                "1" -> {
                    edoFact = "Abonado"
                    binding.tvEdoFactModulCXC.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.yellowColor
                        )
                    )
                }

                "2" -> {
                    edoFact = "Pagado"
                    binding.tvEdoFactModulCXC.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.greenColor
                        )
                    )
                }

                else -> {
                    edoFact = "No identificado"
                    binding.tvEdoFactModulCXC.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.blackColor
                        )
                    )
                }
            }

            var mtoDeuda = planCXC.monto
            mtoDeuda = Math.round(mtoDeuda * 100.00) / 100.00

            /*if (planCXC.fechaVencimiento <= date){
                binding.tvDocVence.text = "Vencido"
                binding.tvDocVence.setTextColor(Color.RED)
            } else {
                binding.tvDocVence.text = "Por Vencer"
                binding.tvDocVence.setTextColor(Color.rgb(63, 197, 39))
            }*/



            binding.tvDocModulCXC.text = "Nº: ${planCXC.doc}"
            //binding.tvDocVence.text         = if (planCXC.fechaVencimiento <= date) "Vencido" else "Por Vencer"
            binding.tvClienteModulCXC.text = planCXC.cliente
            binding.tvMontoModulCXC.text = "$ ${mtoDeuda}"
            binding.tvFechaVencimientoModulCXC.text = "Vencimiento: ${planCXC.fechaVencimiento}"
            binding.tvFechaRecepcionModulCXC.text = "Recepción: ${planCXC.fechaRecepcion}"
            binding.tvEdoFactModulCXC.text = "Cuenta:   $edoFact"
            binding.tvEdoPediModulCXC.text = "DOC: $edoPedi"

            binding.clMainModulCXC.setOnClickListener {
                onClickListener(
                    planCXC.codcliente,
                    planCXC.cliente
                )
            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanificadorCXCHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return PlanificadorCXCHolder(
            layoutInflater.inflate(
                R.layout.item_planificador_cxc,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: PlanificadorCXCHolder, position: Int) {
        holder.render(planificadorCxc[position], onClickListener)
    }

    override fun getItemCount(): Int = planificadorCxc.size

    fun actualizarFact(planificadorCxc: List<PlanificadorCxc>) {
        this.planificadorCxc = planificadorCxc
        notifyDataSetChanged()
    }

    private fun diferenciaFehca(fecha: String): Int {
        val fecha2 = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(fecha)
        val fecha1 = Date(System.currentTimeMillis())
        val milisegundos = fecha2!!.time - fecha1.time
        val segundos: Long = milisegundos / 1000
        val minutos = segundos / 60
        val horas = minutos / 60
        val dias = horas / 24

        return dias.toInt()
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