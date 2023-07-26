package com.appcloos.mimaletin

import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.Menu
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuItemCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.appcloos.mimaletin.databinding.ActivitySeleccionarClientePedidoBinding


class SeleccionarClientePedidoActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySeleccionarClientePedidoBinding

    private var clientes: ArrayList<Cliente> = ArrayList()
    private var clienteList: List<Cliente> = clientes.toMutableList()

    private lateinit var preferences: SharedPreferences
    private lateinit var cod_usuario: String

    lateinit var ke_android: SQLiteDatabase
    private lateinit var conn: AdminSQLiteOpenHelper

    private lateinit var adapter: SeleccionarClientePedidoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeleccionarClientePedidoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferences = getSharedPreferences("Preferences", MODE_PRIVATE)
        cod_usuario = preferences.getString("cod_usuario", null).toString()

        supportActionBar!!.title = "Selecione un Cliente"

        conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null, 24)

        ke_android = conn.writableDatabase

        buscarArticulo("")

        println("Cantidad de lineas -> ${clientes.size}")

        binding.rvMainPedido.layoutManager = LinearLayoutManager(this)
        adapter = SeleccionarClientePedidoAdapter(
            clientes,
            onClickListener = { cliente, nomCliente -> irACXC(cliente, nomCliente) },
            this
        )
        binding.rvMainPedido.adapter = adapter


    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.menu_catalogo, menu)
        val menuItem = menu.findItem(R.id.search_view_catalogo)
        val buscador = MenuItemCompat.getActionView(menuItem) as SearchView
        buscador.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(busqueda: String): Boolean {
                println("lo que ingreso -> $busqueda")
                buscarArticulo(busqueda)
                return true
            }

            override fun onQueryTextChange(busqueda: String): Boolean {
                println("lo que saco -> $busqueda")
                buscarArticulo(busqueda)
                return true
            }
        })

        return super.onCreateOptionsMenu(menu)
    }

    private fun buscarArticulo(busqueda: String) {
        if (busqueda == "") {
            clientes = ArrayList()
            // System.out.println("IMPRIMIENDO EL NOMBRE " + busqueda);

            val cursorca = ke_android.rawQuery(
                "SELECT codigo, nombre, contribespecial, kne_activa FROM cliempre WHERE status = '1' AND vendedor = '$cod_usuario' ORDER BY nombre ASC",
                null
            )

            while (cursorca.moveToNext()) {
                val cliente = Cliente()

                cliente.codigo = cursorca.getString(0)
                cliente.nombre = cursorca.getString(1)
                cliente.contribespecial = cursorca.getDouble(2)
                cliente.kne_activa = conn.getDeudaClienteNum(cliente.codigo)

                clientes.add(cliente)
            }
            cursorca.close()
            //ke_android.close();
            adapter = SeleccionarClientePedidoAdapter(
                clientes,
                onClickListener = { cliente, nomCliente -> irACXC(cliente, nomCliente) },
                this
            )
            binding.rvMainPedido.adapter = adapter
            adapter.notifyDataSetChanged()
        } else {
            clientes = ArrayList()
            // System.out.println("IMPRIMIENDO EL NOMBRE " + busqueda);

            val cursorca = ke_android.rawQuery(
                "SELECT codigo, nombre, contribespecial, kne_activa FROM cliempre WHERE status = '1' AND vendedor = '$cod_usuario' AND (nombre LIKE '%$busqueda%' OR codigo LIKE '%$busqueda%') ORDER BY nombre ASC",
                null
            )

            while (cursorca.moveToNext()) {
                val cliente = Cliente()

                cliente.codigo = cursorca.getString(0)
                cliente.nombre = cursorca.getString(1)
                cliente.contribespecial = cursorca.getDouble(2)
                cliente.kne_activa = conn.getDeudaClienteNum(cliente.codigo)

                clientes.add(cliente)
            }
            cursorca.close()
            //ke_android.close();
            adapter = SeleccionarClientePedidoAdapter(
                clientes,
                onClickListener = { cliente, nomCliente -> irACXC(cliente, nomCliente) },
                this
            )
            binding.rvMainPedido.adapter = adapter
            adapter.notifyDataSetChanged()
        }
    }


    private fun irACXC(cliente: String, nomCliente: String) {

        if (false) {
            Toast.makeText(this, "El cliente tiene una deuda pendiente", Toast.LENGTH_SHORT).show()
        } else {
            val intent = Intent(this, CreacionPedidoActivity::class.java)
            intent.putExtra("cod_cliente", cliente)
            intent.putExtra("nombre_cliente", nomCliente)
            startActivity(intent)
        }

    }

    private fun guardarClintes() {
        val cursorTasas: Cursor = ke_android.rawQuery(
            "SELECT codigo, nombre, contribespecial, kne_activa FROM cliempre WHERE status = '1' AND vendedor = '$cod_usuario' ORDER BY nombre ASC",
            null
        )

        while (cursorTasas.moveToNext()) {
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