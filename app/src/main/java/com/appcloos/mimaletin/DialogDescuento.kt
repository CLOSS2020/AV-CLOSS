package com.appcloos.mimaletin

import android.app.Dialog
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class DialogDescuento {

    private lateinit var rvDetalle: RecyclerView
    lateinit var aceptar: Button
    private lateinit var conn: AdminSQLiteOpenHelper
    private lateinit var keAndroid: SQLiteDatabase
    val adapter: DescuentosAdapter = DescuentosAdapter()

    fun DialogDescuento(contexto: Context, datos: ArrayList<Descuentos>) {
        conn = AdminSQLiteOpenHelper(contexto, "ke_android", null)
        keAndroid = conn.writableDatabase

        // conf basica del dialogo
        val dialogo = Dialog(contexto)
        dialogo.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogo.setCancelable(false)
        dialogo.window?.setBackgroundDrawable(ColorDrawable(Color.GRAY))
        dialogo.setContentView(R.layout.dialog_descuentos)

        rvDetalle = dialogo.findViewById(R.id.rv_detalle_desc)
        aceptar = dialogo.findViewById(R.id.bt_detalle_acep)

        rvDetalle.layoutManager = LinearLayoutManager(contexto)
        adapter.DescuentosAdapter(contexto, datos)
        rvDetalle.adapter = adapter
        adapter.notifyDataSetChanged()

        // cerrar el dialogo
        aceptar.setOnClickListener {
            dialogo.dismiss()
        }
        // mostrar el dialogo
        dialogo.show()
    }
}
