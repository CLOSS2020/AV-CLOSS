package com.appcloos.mimaletin

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class DialogAnexo {

    lateinit var aceptar: Button
    lateinit var monto: TextView
    lateinit var fecha: TextView
    lateinit var id: TextView
    lateinit var banco: TextView
    lateinit var referencia: TextView
    private lateinit var rvDocs: RecyclerView
    var nombanco = ""
    var nomcliente = ""

    private lateinit var textView69: TextView
    private lateinit var textView71: TextView
    private lateinit var textView74: TextView
    private lateinit var textView76: TextView
    private lateinit var textView78: TextView
    private lateinit var textView80: TextView

    private lateinit var conn: AdminSQLiteOpenHelper
    private lateinit var keAndroid: SQLiteDatabase
    val adapter: SimpleDocsAdapter = SimpleDocsAdapter()


    fun DialogAnexo(contexto: Context, datos: ArrayList<CXC>, codEmpresa: String?) {
        conn = AdminSQLiteOpenHelper(contexto, "ke_android", null)
        keAndroid = conn.writableDatabase

        val dialogo = Dialog(contexto, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialogo.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogo.setCancelable(false)
        dialogo.window?.setBackgroundDrawable(ColorDrawable(Color.GRAY))
        dialogo.setContentView(R.layout.dialog_deposito_ok)

        // instanciamiento de los elementos
        aceptar = dialogo.findViewById(R.id.bt_aceptar_anexo)
        monto = dialogo.findViewById(R.id.tv_monto_anexo)
        fecha = dialogo.findViewById(R.id.tv_fecha_anexo)
        id = dialogo.findViewById(R.id.tv_id_anexo)
        banco = dialogo.findViewById(R.id.tv_banco_anexo)
        referencia = dialogo.findViewById(R.id.tv_ref_anexo)

        textView69 = dialogo.findViewById(R.id.textView69)
        textView71 = dialogo.findViewById(R.id.textView71)
        textView74 = dialogo.findViewById(R.id.textView74)
        textView76 = dialogo.findViewById(R.id.textView76)
        textView78 = dialogo.findViewById(R.id.textView78)
        textView80 = dialogo.findViewById(R.id.textView80)

        setColors(codEmpresa)

        //variables
        var rfecha = ""
        var rmonto = 0.00
        var rid = ""
        var rbanco = ""
        var rref = ""
        val listadocs: ArrayList<CXC> = ArrayList()

        for (i in datos.indices) {
            rid = datos[i].id_recibo
            rmonto = datos[i].bcomonto
            rfecha = datos[i].fchrecibo
            rbanco = datos[i].bcocod
            rref = datos[i].bcoref

        }

        val buscaBanco: Cursor =
            keAndroid.rawQuery("SELECT nombanco FROM listbanc WHERE codbanco = '${rbanco}'", null)

        while (buscaBanco.moveToNext()) {
            nombanco = buscaBanco.getString(0)
        }
        buscaBanco.close()
        // colocación de los datos
        id.text = rid
        fecha.text = rfecha
        monto.text = rmonto.toString()
        banco.text = nombanco
        referencia.text = rref


        val buscarDocs: Cursor = keAndroid.rawQuery(
            "SELECT documento, nombrecli FROM ke_precobradocs WHERE cxcndoc = '${rid}'",
            null
        )

        while (buscarDocs.moveToNext()) {
            val cxcdocs = CXC()
            cxcdocs.documento = buscarDocs.getString(0)
            cxcdocs.cliente = buscarDocs.getString(1)
            listadocs.add(cxcdocs)
        }
        buscarDocs.close()

        //mostrar los docs en el pago
        rvDocs = dialogo.findViewById(R.id.rv_docs_deposito)
        rvDocs.layoutManager = LinearLayoutManager(contexto)
        adapter.SimpleDocsAdapter(contexto, listadocs)
        rvDocs.adapter = adapter
        adapter.notifyDataSetChanged()


        //cerrar el dialogo
        aceptar.setOnClickListener {
            dialogo.dismiss()

            val menucxc = Intent(contexto, CXCActivity::class.java)
            contexto.startActivity(menucxc)
            (contexto as Activity).finish()
        }

        dialogo.show()
    }

    private fun setColors(codEmpresa: String?) {
        textView69.setTextColor(textView69.colorTextAgencia(codEmpresa))
        textView71.setTextColor(textView71.colorTextAgencia(codEmpresa))
        textView74.setTextColor(textView74.colorTextAgencia(codEmpresa))
        textView76.setTextColor(textView76.colorTextAgencia(codEmpresa))
        textView78.setTextColor(textView78.colorTextAgencia(codEmpresa))
        textView80.setTextColor(textView80.colorTextAgencia(codEmpresa))

        aceptar.setBackgroundColor(aceptar.colorButtonAgencia(codEmpresa))
    }

}