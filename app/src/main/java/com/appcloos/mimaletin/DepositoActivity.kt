package com.appcloos.mimaletin

import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.appcloos.mimaletin.databinding.ActivityDepositoBinding

class depositoActivity : AppCompatActivity(), EfectivosAdapter.RecHolder.QuantityListener {

    private lateinit var preferences: SharedPreferences // preferences para cargar los datos de la princ.
    private var cod_usuario: String? = ""
    private var codEmpresa: String? = ""
    private lateinit var conn: AdminSQLiteOpenHelper
    lateinit var ke_android: SQLiteDatabase
    lateinit var cursorCobranza: Cursor
    lateinit var listRecibos: ArrayList<CXC>
    lateinit var listaRecsSeleccionados: ArrayList<String>
    private lateinit var binding: ActivityDepositoBinding



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDepositoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar!!.title = "Seleccione un Deposito"

        preferences = getSharedPreferences("Preferences", MODE_PRIVATE)
        cod_usuario = preferences.getString("cod_usuario", null)
        codEmpresa  = preferences.getString("codigoEmpresa", null)
        listRecibos = ArrayList<CXC>()
        listaRecsSeleccionados = ArrayList()

        println(cod_usuario)

        conn = AdminSQLiteOpenHelper(getApplicationContext(), "ke_android", null, 18)
        cargarRecibos()

        binding.btSigte.setOnClickListener {
            iraCreacionDeposito()
        }
    }

    private fun iraCreacionDeposito() {
        val intent = Intent(applicationContext, creacionDepositoActivity ::class.java)
        intent.putExtra("cod_usuario", cod_usuario)
        intent.putExtra("codigoEmpresa", codEmpresa)
        intent.putStringArrayListExtra("listRecibos", listaRecsSeleccionados)
        startActivity(intent)
    }

    private fun cargarRecibos() {
        ke_android = conn.writableDatabase

        var tabla = "ke_precobranza"
        var columna = arrayOf("cxcndoc," + "fchrecibo,"+ "edorec," + "efectivo")
        var seleccion = "codvend='${cod_usuario}' AND edorec != '3' AND edorec != '4' AND edorec !='8' AND edorec !='9' AND edorec !='10' AND efectivo > 0.00 "

        cursorCobranza = ke_android.query(tabla, columna, seleccion, null,null,null,null)

        while(cursorCobranza.moveToNext()){
            var cobranza: CXC   = CXC()
            cobranza.id_recibo  = cursorCobranza.getString(0)
            cobranza.fchrecibo  = cursorCobranza.getString(1)
            cobranza.edorec     = cursorCobranza.getString(2)
            cobranza.efectivo   = cursorCobranza.getDouble(3)
            listRecibos.add(cobranza)

        }

        println(listRecibos)
        binding.rvRecefect.layoutManager = LinearLayoutManager(applicationContext)
        val adapter = EfectivosAdapter()
        adapter.EfectivosAdapter(applicationContext, listRecibos, this)
        binding.rvRecefect.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    override fun onQuantityChange(listaChange:ArrayList<String>){
        //Toast.makeText(this, listaChange.toString(), Toast.LENGTH_LONG).show()
        println(listaChange.toString())
        evaluarLista(listaChange)


    }

    override fun onResume() {
        super.onResume()
        listaRecsSeleccionados.clear()
        onQuantityChange(listaRecsSeleccionados)


    }
    private fun evaluarLista(listaChange:ArrayList<String>) {

        if (listaChange.isEmpty()){
            binding.btSigte.visibility = View.INVISIBLE
            listaRecsSeleccionados     = listaChange

        }else{
            binding.btSigte.visibility = View.VISIBLE
            listaRecsSeleccionados = listaChange
        }
    }

}