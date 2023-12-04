package com.appcloos.mimaletin

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.content.res.Resources
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.appcloos.mimaletin.databinding.ActivityDepositoBinding

class DepositoActivity : AppCompatActivity(), EfectivosAdapter.RecHolder.QuantityListener {

    private lateinit var preferences: SharedPreferences // preferences para cargar los datos de la princ.
    private var codUsuario: String? = ""
    private var codEmpresa: String? = ""
    private lateinit var conn: AdminSQLiteOpenHelper
    private lateinit var keAndroid: SQLiteDatabase
    private lateinit var cursorCobranza: Cursor
    private lateinit var listRecibos: ArrayList<CXC>
    private lateinit var listaRecsSeleccionados: ArrayList<String>
    private lateinit var binding: ActivityDepositoBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDepositoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Edicion de los colores del Bar de arriba de notificacion de las app y el bar de abajo de los 3 botones
        windowsColor(Constantes.AGENCIA)
        setColors()
        supportActionBar!!.title = "Seleccione un Deposito"

        preferences = getSharedPreferences("Preferences", MODE_PRIVATE)
        codUsuario = preferences.getString("cod_usuario", null)
        codEmpresa = preferences.getString("codigoEmpresa", null)
        listRecibos = ArrayList()
        listaRecsSeleccionados = ArrayList()

        println(codUsuario)

        conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null)
        cargarRecibos()

        binding.btSigte.setOnClickListener {
            iraCreacionDeposito()
        }
    }

    private fun iraCreacionDeposito() {
        val intent = Intent(applicationContext, CreacionDepositoActivity::class.java)
        intent.putExtra("cod_usuario", codUsuario)
        intent.putExtra("codigoEmpresa", codEmpresa)
        intent.putStringArrayListExtra("listRecibos", listaRecsSeleccionados)
        startActivity(intent)
    }

    private fun cargarRecibos() {
        keAndroid = conn.writableDatabase

        val tabla = "ke_precobranza"
        val columna = arrayOf("cxcndoc," + "fchrecibo," + "edorec," + "efectivo")
        val seleccion = "codvend='${codUsuario}' AND edorec != '3' AND edorec != '4' " +
                "AND edorec !='8' AND edorec !='9' AND edorec !='10' AND efectivo > 0.00 "

        cursorCobranza = keAndroid.query(tabla, columna, seleccion, null, null, null, null)

        while (cursorCobranza.moveToNext()) {
            val cobranza = CXC()
            cobranza.id_recibo = cursorCobranza.getString(0)
            cobranza.fchrecibo = cursorCobranza.getString(1)
            cobranza.edorec = cursorCobranza.getString(2)
            cobranza.efectivo = cursorCobranza.getDouble(3)
            listRecibos.add(cobranza)

        }

        println(listRecibos)
        binding.rvRecefect.layoutManager = LinearLayoutManager(applicationContext)
        val adapter = EfectivosAdapter()
        adapter.EfectivosAdapter(applicationContext, listRecibos, this)
        binding.rvRecefect.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    override fun onQuantityChange(listaChange: ArrayList<String>) {
        //Toast.makeText(this, listaChange.toString(), Toast.LENGTH_LONG).show()
        println(listaChange.toString())
        evaluarLista(listaChange)


    }

    override fun onResume() {
        super.onResume()
        listaRecsSeleccionados.clear()
        onQuantityChange(listaRecsSeleccionados)


    }

    private fun evaluarLista(listaChange: ArrayList<String>) {

        if (listaChange.isEmpty()) {
            binding.btSigte.visibility = View.INVISIBLE
            listaRecsSeleccionados = listaChange

        } else {
            binding.btSigte.visibility = View.VISIBLE
            listaRecsSeleccionados = listaChange
        }
    }


    override fun getTheme(): Resources.Theme {
        val theme = super.getTheme()
        theme.applyStyle(setThemeAgencia(Constantes.AGENCIA), true)
        // you could also use a switch if you have many themes that could apply
        return theme
    }

    private fun setColors() {
        binding.apply {
            tvNombreCliente.setDrawableHeadAgencia(Constantes.AGENCIA)
            btSigte.backgroundTintList =
                ColorStateList.valueOf(btSigte.colorAgencia(Constantes.AGENCIA))
        }

    }

}