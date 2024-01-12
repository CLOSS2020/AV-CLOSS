package com.appcloos.mimaletin

import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class PedidosArchivadosActivity : AppCompatActivity() {
    private var lvArchivados: ListView? = null
    var conn: AdminSQLiteOpenHelper? = null
    var keAndroid: SQLiteDatabase? = null
    private var listapedidos: ArrayList<Pedidos>? = null
    private var pedidosArchivadosAdapter: PedidosArchivadosAdapter? = null

    private lateinit var preferences: SharedPreferences
    private var codEmpresa: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pedidos_archivados)
        requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED // mantener la orientacion vertical
        conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null)
        keAndroid = conn!!.writableDatabase
        lvArchivados = findViewById(R.id.lv_archivados)
        preferences = getSharedPreferences("Preferences", MODE_PRIVATE)
        codEmpresa = preferences.getString("codigoEmpresa", null)
        val codUsuario = preferences.getString("cod_usuario", null)
        cargarPedidosArchivados(codUsuario)
    }

    private fun cargarPedidosArchivados(codUsuario: String?) {
        listapedidos = ArrayList()
        val tabla = "ke_opti"
        val consulta = arrayOf(
            "kti_ndoc," +
                "kti_nombrecli," +
                "kti_totneto," +
                "kti_nroped," +
                "datetime('now','start of month','-1 month') as principiomes," +
                "datetime('now','start of month','-1 day') as finalmes"
        )
        val condicion =
            "kti_codven = '$codUsuario' AND empresa = '$codEmpresa' AND kti_fchdoc BETWEEN principiomes AND finalmes"
        val cursor = keAndroid!!.query(tabla, consulta, condicion, null, null, null, null)
        while (cursor.moveToNext()) {
            val pedidos = Pedidos()
            pedidos.setNumeroDocumento(cursor.getString(0))
            pedidos.setNombreCliente(cursor.getString(1))
            pedidos.setTotalNeto(cursor.getDouble(2))
            pedidos.setNumeroPedido(cursor.getString(3))
            listapedidos!!.add(pedidos)
        }
        cursor.close()
        if (listapedidos != null) {
            pedidosArchivadosAdapter =
                PedidosArchivadosAdapter(this@PedidosArchivadosActivity, listapedidos)
            lvArchivados!!.adapter = pedidosArchivadosAdapter
            pedidosArchivadosAdapter!!.notifyDataSetChanged()
        } else {
            // en caso de que no llegue
            println("pedidos vacios")
        }
    }

    override fun getTheme(): Resources.Theme {
        val theme = super.getTheme()
        theme.applyStyle(setThemeAgencia(Constantes.AGENCIA), true)
        // you could also use a switch if you have many themes that could apply
        return theme
    }
}
