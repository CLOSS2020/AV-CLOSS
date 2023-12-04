package com.appcloos.mimaletin

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.appcloos.mimaletin.model.CXC.ke_precobradocs

class SimpleDocsRetAdapter(
    context: Context,
    private val kePrecobradocs: ArrayList<ke_precobradocs>
) : RecyclerView.Adapter<SimpleDocsRetAdapter.SimpleDocsRetHolder>() {

    private var conn: AdminSQLiteOpenHelper = AdminSQLiteOpenHelper(context, "ke_android", null)
    var keAndroid: SQLiteDatabase = conn.writableDatabase

    inner class SimpleDocsRetHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val nrodoc: TextView = view.findViewById(R.id.tv_nrodoc_sdocs)
        val cliente: TextView = view.findViewById(R.id.tv_nocli_sdocs)
        lateinit var nomcliente: String

        fun bind(kePrecobradocs: ke_precobradocs) {
            nrodoc.text = kePrecobradocs.documento

            val buscaCliente: Cursor = keAndroid.rawQuery(
                "SELECT nombrecli FROM ke_doccti WHERE documento = '${kePrecobradocs.documento}'",
                null
            )

            while (buscaCliente.moveToNext()) {
                nomcliente = buscaCliente.getString(0)
            }

            buscaCliente.close()

            cliente.text = nomcliente
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleDocsRetHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return SimpleDocsRetHolder(layoutInflater.inflate(R.layout.item_simple_docs, parent, false))
    }

    override fun getItemCount(): Int {
        return kePrecobradocs.size
    }

    override fun onBindViewHolder(holder: SimpleDocsRetHolder, position: Int) {
        holder.bind(kePrecobradocs[position])
    }
}