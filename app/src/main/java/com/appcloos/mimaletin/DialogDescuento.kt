package com.appcloos.mimaletin

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.Window
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class DialogDescuento {

    lateinit var rv_detalle:RecyclerView
    lateinit var aceptar:Button
    private lateinit var conn: AdminSQLiteOpenHelper
    lateinit var ke_android: SQLiteDatabase
    val adapter: DescuentosAdapter = DescuentosAdapter()


   fun DialogDescuento(contexto: Context, datos: ArrayList<Descuentos>){
       conn         = AdminSQLiteOpenHelper(contexto, "ke_android", null, 19)
       ke_android   = conn.writableDatabase

       //conf basica del dialogo
       var dialogo: Dialog = Dialog(contexto)
       dialogo.requestWindowFeature(Window.FEATURE_NO_TITLE)
       dialogo.setCancelable(false)
       dialogo.window?.setBackgroundDrawable(ColorDrawable(Color.GRAY))
       dialogo.setContentView(R.layout.dialog_descuentos)

       rv_detalle = dialogo.findViewById(R.id.rv_detalle_desc)
       aceptar    = dialogo.findViewById(R.id.bt_detalle_acep)



       rv_detalle.layoutManager = LinearLayoutManager(contexto)
       adapter.DescuentosAdapter(contexto, datos)
       rv_detalle.adapter = adapter
       adapter.notifyDataSetChanged()


       //cerrar el dialogo
       aceptar.setOnClickListener(View.OnClickListener {
           dialogo.dismiss()

       })
       //mostrar el dialogo
       dialogo.show()
   }


}