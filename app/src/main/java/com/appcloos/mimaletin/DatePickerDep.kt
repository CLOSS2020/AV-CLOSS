package com.appcloos.mimaletin

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.util.Calendar
import java.util.Date

class DatePickerDep(val listener: (day: Int, month: Int, year: Int) -> Unit) : DialogFragment(),
    DatePickerDialog.OnDateSetListener {
    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        listener(dayOfMonth, month + 1, year)

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calen: Calendar = Calendar.getInstance()


        /*WTF Java y Kotlin se la mamaron con empezar a contar los
        meses en 0 XD, osea hay que sumarle 1*/
        val day = calen.get(Calendar.DAY_OF_MONTH)
        val month = calen.get(Calendar.MONTH)
        val year = calen.get(Calendar.YEAR)
        val picker = DatePickerDialog(activity as Context, this, year, month, day)

        println(" fecha que es interna por variable:  $day, $month, $year")

        picker.datePicker.maxDate = calen.timeInMillis

        //calculo la fecha anterior de 3 dias (por los momentos, parametrizar despues)
        val date = Date()
        /*calen.time = date*/
        calen.add(Calendar.DATE, -20) //reduje los dias
        picker.datePicker.minDate = calen.timeInMillis
        return picker
    }
}