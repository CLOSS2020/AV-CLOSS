package com.appcloos.mimaletin

import android.icu.text.DateFormat
import android.icu.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ObjetoUtils {

    fun formatoFechaHoraShow(fechaString: String): String {

        val fechaDate: Date =
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(fechaString)

        val dateFormat: DateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.getDefault())

        return dateFormat.format(fechaDate)

    }

}