package com.appcloos.mimaletin

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/*
private lateinit var adapter: DescuentosAdapter
private lateinit var rvDetalle: RecyclerView

fun DialogDescuento(
    contexto: Context,
    datos: ArrayList<Descuentos>,
    descuentoIsSelected: (Boolean, Int) -> Unit
) {
    //val rvDetalle: RecyclerView
    val aceptar: Button

    // conf basica del dialogo
    val dialogo = Dialog(contexto)
    dialogo.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialogo.setCancelable(false)
    dialogo.window?.setBackgroundDrawable(ColorDrawable(Color.GRAY))
    dialogo.setContentView(R.layout.dialog_descuentos)

    rvDetalle = dialogo.findViewById(R.id.rv_detalle_desc)
    aceptar = dialogo.findViewById(R.id.bt_detalle_acep)

    rvDetalle.layoutManager = LinearLayoutManager(contexto)
    adapter = DescuentosAdapter(datos, contexto) { a, b ->
        descuentoIsSelected(a, b)
//        refresh()
        //adapter.notifyDataSetChanged()
    }
    rvDetalle.adapter = adapter
//    refresh()
    //adapter.notifyDataSetChanged()

    // cerrar el dialogo
    aceptar.setOnClickListener {
        dialogo.dismiss()
    }
    // mostrar el dialogo
    dialogo.show()
}

//fun refresh() {
//    rvDetalle.removeAllViewsInLayout()
//    adapter.notifyDataSetChanged()
//}
*/
