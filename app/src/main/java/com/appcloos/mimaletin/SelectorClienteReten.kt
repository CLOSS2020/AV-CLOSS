package com.appcloos.mimaletin

import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.appcloos.mimaletin.databinding.ActivitySelectorClienteRetenBinding
import com.appcloos.mimaletin.moduloCXC.ModuloCXCActivity

class SelectorClienteReten : AppCompatActivity() {

    private lateinit var binding: ActivitySelectorClienteRetenBinding

    private var clientes: ArrayList<Cliente> = ArrayList()
    private var clienteList:List<Cliente> = clientes.toMutableList()

    private lateinit var preferences: SharedPreferences
    private lateinit var cod_usuario: String

    lateinit var ke_android: SQLiteDatabase
    private lateinit var conn: AdminSQLiteOpenHelper

    private lateinit var adapter: SeleccionarClientePedidoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectorClienteRetenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferences = getSharedPreferences("Preferences", MODE_PRIVATE)
        cod_usuario = preferences.getString("cod_usuario", null).toString()

        supportActionBar!!.title = "Selecione un Cliente"

        conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null, 24)

        ke_android = conn.writableDatabase

        buscarClintes()

        println("Cantidad de lineas -> ${clientes.size}")

        binding.rvMainSelctReten.layoutManager = LinearLayoutManager(this)
        adapter = SeleccionarClientePedidoAdapter(clientes, onClickListener = { cliente, nomCliente -> irAReten(cliente, nomCliente) }, this)
        binding.rvMainSelctReten.adapter = adapter
    }

    private fun irAReten(cliente: String, nomCliente: String) {
        val editor: SharedPreferences.Editor = preferences.edit()
        editor.putString("cliente", cliente)
        editor.putString("nomCliente", nomCliente)
        editor.putString("origin", "Reten")
        editor.putString("cod_usuario", cod_usuario)
        editor.apply()

        val intent = Intent(this, ModuloCXCActivity::class.java)
        startActivity(intent)
    }

    private fun buscarClintes() {
        val cursorTasas: Cursor = ke_android.rawQuery("SELECT codigo, nombre, contribespecial, kne_activa FROM cliempre WHERE status = '1' AND vendedor = '$cod_usuario' AND contribespecial = '1' ORDER BY nombre ASC", null)

        while (cursorTasas.moveToNext()){
            val cliente = Cliente()

            cliente.codigo = cursorTasas.getString(0)
            cliente.nombre = cursorTasas.getString(1)
            cliente.contribespecial = cursorTasas.getDouble(2)
            cliente.kne_activa = cursorTasas.getInt(3)

            clientes.add(cliente)
        }
        cursorTasas.close()
    }

}