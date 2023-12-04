package com.appcloos.mimaletin.ModuloReten

import android.app.SearchManager
import android.content.Context
import android.content.SharedPreferences
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SearchView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.MenuItemCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.appcloos.mimaletin.AdminSQLiteOpenHelper
import com.appcloos.mimaletin.Cliente
import com.appcloos.mimaletin.DialogClientesDatos
import com.appcloos.mimaletin.R
import com.appcloos.mimaletin.SeleccionarClientePedidoAdapter
import com.appcloos.mimaletin.databinding.FragmentSelectClienteRetenBinding


class SelectClienteRetenFragment : Fragment() {

    private lateinit var searchView: SearchView
    private lateinit var menuItem: MenuItem
    private lateinit var searchManager: SearchManager

    private var clientes: ArrayList<Cliente> = ArrayList()
    private var clienteList: List<Cliente> = clientes.toMutableList()

    private lateinit var preferences: SharedPreferences
    private lateinit var cod_usuario: String

    lateinit var ke_android: SQLiteDatabase
    private lateinit var conn: AdminSQLiteOpenHelper

    private lateinit var adapter: SeleccionarClientePedidoAdapter

    private lateinit var binding: FragmentSelectClienteRetenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSelectClienteRetenBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preferences = this.requireActivity()
            .getSharedPreferences("Preferences", AppCompatActivity.MODE_PRIVATE)
        cod_usuario = preferences.getString("cod_usuario", null).toString()

        conn = AdminSQLiteOpenHelper(requireContext(), "ke_android", null)

        ke_android = conn.writableDatabase

        buscarClintes("")


        //adapter = SeleccionarClientePedidoAdapter(clientes, onClickListener = { cliente, nomCliente -> irAReten(cliente, nomCliente) }, requireContext())
        //binding.rvMainSelctReten.adapter = adapter
    }

    private fun buscarClintes(Text: String?) {

        clientes.clear()

        val cursor: Cursor = if (Text.isNullOrEmpty()) {
            ke_android.rawQuery(
                "SELECT DISTINCT cliempre.codigo, cliempre.nombre, cliempre.contribespecial, cliempre.kne_activa, (SELECT count(ke_doccti.documento) FROM ke_doccti WHERE codcliente=cliempre.codigo AND ke_doccti.agencia != '002' AND estatusdoc != '2' AND (dtotalfinal - dtotpagos) > 0.00) AS CanidadDoc FROM cliempre INNER JOIN ke_doccti ON cliempre.codigo = ke_doccti.codcliente WHERE cliempre.status = '1' AND cliempre.vendedor = '$cod_usuario' AND ke_doccti.agencia != '002' ORDER BY nombre ASC;",
                null
            )
        } else {
            ke_android.rawQuery(
                "SELECT DISTINCT cliempre.codigo, cliempre.nombre, cliempre.contribespecial, cliempre.kne_activa, (SELECT COUNT(ke_doccti.documento) FROM ke_doccti WHERE codcliente=cliempre.codigo AND ke_doccti.agencia != '002' AND estatusdoc != '2' AND (dtotalfinal - dtotpagos) > 0.00) AS CanidadDoc FROM cliempre INNER JOIN ke_doccti ON cliempre.codigo = ke_doccti.codcliente WHERE cliempre.status = '1' AND cliempre.vendedor = '$cod_usuario' AND ke_doccti.agencia != '002' AND (nombre LIKE'%$Text%' OR codigo LIKE '%$Text%') ORDER BY nombre ASC;",
                null
            )
        }
        //println("SELECT DISTINCT codigo, nombre, contribespecial, kne_activa FROM cliempre INNER JOIN ke_doccti ON cliempre.codigo = ke_doccti.codcliente WHERE cliempre.status = '1' AND cliempre.vendedor = '99' AND ke_doccti.agencia != '002' AND (nombre LIKE'%$Text%' OR codigo LIKE '%$Text%') ORDER BY nombre ASC")

        //val cursorTasas: Cursor = ke_android.rawQuery("SELECT DISTINCT codigo, nombre, contribespecial, kne_activa FROM cliempre INNER JOIN ke_doccti ON cliempre.codigo = ke_doccti.codcliente WHERE cliempre.status = '1' AND cliempre.vendedor = '99' AND cliempre.contribespecial = '1' AND ke_doccti.agencia != '002' ORDER BY nombre ASC;", null)

        while (cursor.moveToNext()) {
            val cliente = Cliente()

            cliente.codigo = cursor.getString(0)
            cliente.nombre = cursor.getString(1)
            cliente.contribespecial = cursor.getDouble(2)
            cliente.kne_activa = cursor.getInt(3)
            cliente.status = cursor.getDouble(4)

            clientes.add(cliente)
        }
        cursor.close()
        binding.rvMainSelctReten.layoutManager = LinearLayoutManager(requireContext())
        adapter = SeleccionarClientePedidoAdapter(
            clientes,
            onClickListener = { cliente, nomCliente -> irAReten(cliente, nomCliente) },
            onLongClickListener = { cliente, nomCliente -> dialogCliente(cliente, nomCliente) },
            requireContext()
        )
        binding.rvMainSelctReten.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    private fun irAReten(cliente: String, nomCliente: String) {

        val args = Bundle()
        args.putString("cliente", cliente)
        args.putString("nomCliente", nomCliente)

        findNavController().navigate(
            R.id.action_selectClienteRetenFragment_to_edoCuentaClienteRetenFragment,
            args
        )
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_search, menu)

        menuItem = menu.findItem(R.id.search_view)

        searchView = MenuItemCompat.getActionView(menuItem) as SearchView
        searchView.isIconified = true

        searchManager = requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))



        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                //buscarItem(query)
                buscarClintes(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                //buscarItem(newText)
                buscarClintes(newText)
                return true
            }
        })

        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun dialogCliente(cliente: String, nomCliente: String) {
        val dialog = DialogClientesDatos(requireContext(), cliente, nomCliente)
        dialog.show()
    }


}