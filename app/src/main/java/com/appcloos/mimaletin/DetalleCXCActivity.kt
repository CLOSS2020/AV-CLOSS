package com.appcloos.mimaletin

import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.appcloos.mimaletin.databinding.ActivityDetalleCxcactivityBinding
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class DetalleCXCActivity : AppCompatActivity() {

    //var codigoRecibo = intent.getStringExtra("codigoRecibo") as String

    private lateinit var binding: ActivityDetalleCxcactivityBinding
    private lateinit var keAndroid: SQLiteDatabase
    private lateinit var conn: AdminSQLiteOpenHelper

    //val bundle = intent.extras
    //val dato = bundle?.getString("codigoRecibo")

    lateinit var preferences: SharedPreferences
    lateinit var cxcndoc: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleCxcactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferences = getSharedPreferences("Preferences", MODE_PRIVATE)
        cxcndoc = preferences.getString("recibo", null).toString()

        conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null, 26)
        keAndroid = conn.writableDatabase

        val cursor =
            keAndroid.rawQuery("SELECT * FROM ke_precobranza WHERE cxcndoc = '$cxcndoc';", null)

        if (cursor.moveToNext()) {

            val fechaEmision: String = if (cursor.getString(1) == "D") {
                val ldt: LocalDateTime = LocalDateTime.parse(
                    cursor.getString(6), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                )
                val writingFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss a")
                ldt.format(writingFormatter)
            } else {
                val readingFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val date = LocalDate.parse(cursor.getString(6), readingFormatter)
                val writingFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                date.format(writingFormatter)
            }



            binding.tvCod.text = cxcndoc

            binding.tvTipoRecibo.text = when (cursor.getString(1)) {
                "W" -> "Recibo de Cobro"
                "D" -> "Axeno de Deposito"
                else -> "No Identificado"
            }

            binding.tvEmision.text = "Emisión: $fechaEmision"

            binding.tvTipoMoneda.text =
                "Moneda del Recibo: ${if (cursor.getString(18) == "2") "Dolar" else "Bolívar"}"

            binding.tvBanco.text = cursor.getString(29)

            val cursorBanco = keAndroid.rawQuery(
                "SELECT nombanco FROM listbanc WHERE codbanco = '${
                    cursor.getString(29)
                }';", null
            )

            if (cursorBanco.moveToNext()) {
                binding.tvBanco.text = "${cursor.getString(29)} ${cursorBanco.getString(0)}"
                binding.tvReferencia.text = "REF: ${cursor.getString(33)}"
                binding.tvMonto.text =
                    "Monto: ${cursor.getString(32)} ${if (cursor.getString(18) == "2") "$" else "Bs."}"
            } else {
                binding.tvBanco.text = "Efectivo"
                binding.tvMonto.text = "Monto: ${cursor.getString(27)} $"
            }
            cursorBanco.close()

        } else {
            Toast.makeText(this, "Recibo no Existe", Toast.LENGTH_SHORT).show()
        }

        cursor.close()
        //println("Intent -> $dato")
        println("Intent -> $cxcndoc")


    }
}