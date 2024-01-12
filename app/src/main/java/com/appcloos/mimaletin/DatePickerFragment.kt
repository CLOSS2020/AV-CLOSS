package com.appcloos.mimaletin

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.widget.DatePicker
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DatePickerFragment(
    private val actividad: String,
    private val nroDoc: String? = null,
    private val contexto: Context? = null,
    val listener: (day: Int, month: Int, year: Int) -> Unit,
) : DialogFragment(), DatePickerDialog.OnDateSetListener {

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
        val picker =
            DatePickerDialog(
                activity as Context,
                setThemeDateFragment(Constantes.AGENCIA),
                this,
                year,
                month,
                day
            )

        picker.datePicker.maxDate = calen.timeInMillis

        // calculo la fecha anterior de 3 dias (por los momentos, parametrizar despues)
        Date() /*calen.time = date*/
        when (actividad) {
            "cxcReportActivity" -> {
                calen.add(Calendar.DATE, -2) // reduje los dias
                picker.datePicker.minDate = calen.timeInMillis
            }

            "retencionesActivity" -> {
                if (nroDoc == null) {
                    dismiss()
                    Toast.makeText(contexto, "Selecione un documento", Toast.LENGTH_SHORT).show()
                } else {
                    val conn = AdminSQLiteOpenHelper(contexto, "ke_android", null)
                    val keAndroid: SQLiteDatabase = conn.writableDatabase

                    val cursor = keAndroid.rawQuery(
                        "SELECT emision FROM ke_doccti WHERE documento ='$nroDoc'",
                        null
                    )

                    if (cursor.moveToFirst()) {
                        val date1 = SimpleDateFormat(
                            "yyyy-MM-dd",
                            Locale.getDefault()
                        ).parse(cursor.getString(0))
                        cursor.close()

                        val calendar: Calendar = Calendar.getInstance()
                        calendar.time = date1!! // Configuramos la fecha que se recibe

                        picker.datePicker.minDate = calendar.timeInMillis
                    } else {
                        dismiss()
                        Toast.makeText(
                            contexto,
                            "El documento seleccionado No posee fecha de emision",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            else -> {
            }
        }

        return picker
    }
}
