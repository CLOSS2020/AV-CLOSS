package com.appcloos.mimaletin

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.TextView
import androidx.core.graphics.createBitmap
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appcloos.mimaletin.viewmodel.CXC.ke_precobradocs
import com.appcloos.mimaletin.viewmodel.CXC.ke_precobranza

class DialogRetencion {

    lateinit var aceptar: Button
    lateinit var monto: TextView
    lateinit var fecha: TextView
    lateinit var id: TextView
    lateinit var moneda: TextView
    lateinit var banco: TextView
    lateinit var referencia: TextView
    lateinit var cliente: TextView
    lateinit var rv_docs: RecyclerView
    lateinit var adapter: SimpleDocsRetAdapter
    //var myBitmap: Bitmap? = null
    var myBitmap: Bitmap = createBitmap(1000,1000)

    var nombanco = ""
    var nomcliente = ""


    fun DialogRetencion(
        contexto: Context,
        kePrecobranza: ke_precobranza,
        kePrecobradocs: ArrayList<ke_precobradocs>,
    ) {

        var conn = AdminSQLiteOpenHelper(contexto, "ke_android", null, 28)
        var ke_android: SQLiteDatabase = conn.writableDatabase

        val dialogo = Dialog(contexto,android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialogo.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogo.setCancelable(false)
        dialogo.window?.setBackgroundDrawable(ColorDrawable(Color.GRAY))
        dialogo.setContentView(R.layout.dialog_recibo_ok)

        aceptar    = dialogo.findViewById(R.id.bt_aceptardiag)
        monto      = dialogo.findViewById(R.id.tv_montodiag)
        fecha      = dialogo.findViewById(R.id.tv_fechadiag)
        id         = dialogo.findViewById(R.id.tv_prcid)
        moneda     = dialogo.findViewById(R.id.tv_monedadiag)
        banco      = dialogo.findViewById(R.id.tv_bancodiag)
        referencia = dialogo.findViewById(R.id.tv_refdiag)
        cliente    = dialogo.findViewById(R.id.tv_clientediag)

        aceptar.setBackgroundColor(Color.rgb(1, 76, 131))
        aceptar.setTextColor(Color.WHITE)

        moneda.text = "Bs."
        fecha.text = kePrecobranza.fchrecibo
        id.text = kePrecobranza.cxcndoc

        val montoRet = if (kePrecobranza.bsretiva == 0.0){
            "${kePrecobranza.bsretflete}"
        }else if (kePrecobranza.bsretflete == 0.0) {
            "${kePrecobranza.bsretiva}"
        }else{
            "${kePrecobranza.bsretiva} / ${kePrecobranza.bsretflete}"
        }

        monto.text = montoRet
        banco.text = "Retención"
        referencia.text = "${kePrecobradocs[0].refret} ${if (kePrecobradocs[0].refret.isEmpty() || kePrecobradocs[0].refretfte.isEmpty()) "" else "/"} ${kePrecobradocs[0].refretfte}"

        val buscaCliente: Cursor =
            ke_android.rawQuery("SELECT nombrecli FROM ke_doccti WHERE documento = '${kePrecobradocs[0].documento}'", null);

        while (buscaCliente.moveToNext()){
            nomcliente = buscaCliente.getString(0)
        }

        buscaCliente.close()

        cliente.text = nomcliente

        rv_docs = dialogo.findViewById(R.id.rv_docs_recibo)
        rv_docs.layoutManager = LinearLayoutManager(contexto)
        adapter = SimpleDocsRetAdapter(contexto, kePrecobradocs)
        rv_docs.adapter = adapter
        adapter.notifyDataSetChanged()

        aceptar.setOnClickListener {
            dialogo.dismiss()

            val menucxc = Intent(contexto, CXCActivity::class.java)
            contexto.startActivity(menucxc)
            (contexto as Activity).finish()
        }


        dialogo.show()
    }

}