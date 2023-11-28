package com.appcloos.mimaletin

import android.content.Context
import android.icu.text.DateFormat
import android.icu.text.SimpleDateFormat
import android.widget.Toast
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class ObjetoUtils {

    fun formatoFechaHoraShow(fechaString: String): String {

        val fechaDate: Date =
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(fechaString)

        val dateFormat: DateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.getDefault())

        return dateFormat.format(fechaDate)

    }

    private fun compararFecha(fechaRecepcion: String): Int {

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val current = LocalDateTime.now().format(formatter)

        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val firstDate: Date = sdf.parse(fechaRecepcion) as Date
        val secondDate: Date = sdf.parse(current) as Date
        println("Comparacion de la fecha ${firstDate.compareTo(secondDate)}")

        //recepcion > fecha = 1
        //recepcion = fecha = 0
        //recepcion < fecha = -1

        return firstDate.compareTo(secondDate)

    }

    private fun compararFechaFull(fechaRecepcion: String): Int {

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val current = LocalDateTime.now().format(formatter)

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        val firstDate = sdf.parse(fechaRecepcion)
        val secondDate = sdf.parse(current)
        println("Comparacion de la fecha ${firstDate.compareTo(secondDate)}")

        //recepcion > fecha = 1
        //recepcion = fecha = 0
        //recepcion < fecha = -1

        return firstDate.compareTo(secondDate)

    }

    fun valorReal(monto: Double): Double = (monto * 100.00).roundToInt() / 100.00

    companion object {
        fun valorReal(monto: Double): Double = (monto * 100.00).roundToInt() / 100.00

        fun formatoFechaShow(fechaString: String): String {
            val fechaDate: Date =
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(fechaString)
            val dateFormat: DateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            return dateFormat.format(fechaDate)
        }

        fun formatoFecha(fechaString: String): Date =
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(fechaString)

        fun getDateNow(): String {
            val formatter = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return formatter.format(Date())
        }

        fun restarFechas(date1: Long, date2: Long, formatoTiempo: String): Long {
            val diff = date1 - date2
            val segundos = diff / 1000
            val minutos = segundos / 60
            val horas = minutos / 60
            val dias = horas / 24
            return when (formatoTiempo) {
                "dias" -> dias
                "horas" -> horas
                "minutos" -> minutos
                "segundos" -> segundos
                "milisegundos" -> diff
                else -> 0
            }
        }

        fun showError(context:Context, error: String){
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
        }
    }


}