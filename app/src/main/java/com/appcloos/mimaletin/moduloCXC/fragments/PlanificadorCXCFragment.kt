package com.appcloos.mimaletin.moduloCXC.fragments

import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.Context
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.core.view.MenuItemCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.appcloos.mimaletin.AdminSQLiteOpenHelper
import com.appcloos.mimaletin.R
import com.appcloos.mimaletin.databinding.FragmentPlanificadorCxcBinding
import com.appcloos.mimaletin.moduloCXC.fragments.planificadorCXCAdapter.PlanificadorCXCAdapter
import com.appcloos.mimaletin.moduloCXC.viewmodel.PlanificadorCxc


class PlanificadorCXCFragment : Fragment() {

    private lateinit var binding: FragmentPlanificadorCxcBinding
    private lateinit var conn: AdminSQLiteOpenHelper
    lateinit var ke_android: SQLiteDatabase
    var cod_usuario: String? = null
    var codEmpresa: String? = null
    var campo: String = ""
    lateinit var preferences: SharedPreferences
    private lateinit var adapter: PlanificadorCXCAdapter

    //declaracion de variables de texto
    lateinit var agencia: String
    lateinit var tipodoc: String
    lateinit var documento: String
    lateinit var tipodocv: String
    lateinit var ruta_parme: String
    lateinit var emision: String
    lateinit var recepcion: String
    lateinit var vence: String
    lateinit var vendedor: String
    lateinit var codcoord: String
    lateinit var fechamodifi: String
    lateinit var aceptadev: String
    lateinit var estatusdoc: String
    lateinit var enlaceEmpresa: String
    lateinit var nombreEmpresa: String
    lateinit var codigoSucursal: String
    var fecha_auxiliar: String = "0001-01-01 00:00:00"
    var fechavencedcto = ""
    var tienedcto = ""
    var codigoCliente = ""
    var nombreCliente = ""

    //declaracion de variables Double
    var contribesp: Double = 0.0
    var tipoprecio = 0.0
    var diascred = 0.0
    var dtotneto: Double = 0.0
    var dtotimpuest: Double = 0.0
    var dtotalfinal: Double = 0.0
    var dtotpagos: Double = 0.0
    var dtotdescuen: Double = 0.0
    var dFlete: Double = 0.0
    var dtotdev: Double = 0.0
    var dvndmtototal: Double = 0.0
    var dretencion: Double = 0.0
    var dretencioniva: Double = 0.0
    var bsiva: Double = 0.0
    var bsflete: Double = 0.0
    var bsretencion: Double = 0.0
    var bsretencioniva: Double = 0.0
    var tasadoc: Double = 0.00
    var montodcto = 0.00
    var cbsret = 0.00
    var cdret = 0.00
    var cbsretiva = 0.00
    var cdretiva = 0.00
    var cbsrparme = 0.00
    var cdrparme = 0.00
    var bsmtofte = 0.00
    var bsmtoiva = 0.00
    var cbsretflete = 0.00
    var cdretflete = 0.00
    var retmun_mto = 0.00
    var ll_commit = false
    var kti_negesp: Int = 0
    private lateinit var listaPlanificadorCxc: MutableList<PlanificadorCxc>

    private lateinit var searchView: SearchView
    private lateinit var menuItem: MenuItem
    private lateinit var searchManager: SearchManager

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
        preferences = requireActivity().getSharedPreferences("Preferences", Context.MODE_PRIVATE)
        cod_usuario = preferences.getString("cod_usuario", null)
        codEmpresa = preferences.getString("codigoEmpresa", null)

        /*if (preferences.getString("origin", null) == "Reten"){

            val bundle = Bundle()
            bundle.putString("cliente", preferences.getString("cliente", null))
            bundle.putString("nomCliente", preferences.getString("nomCliente", null))

            findNavController().navigate(
                R.id.action_moduloCXCFragment_to_edoCuentaClienteFragment,
                bundle)
        }*/
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentPlanificadorCxcBinding.inflate(layoutInflater)
        return binding.root
        //cargo el codigo del vendedor que viene desde el activity anterior
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        conn = AdminSQLiteOpenHelper(context, "ke_android", null)
        ke_android = conn.writableDatabase
        cargarEnlace()

        listaPlanificadorCxc = conn.getPlanificadorDocs(cod_usuario!!, null, codEmpresa!!)
        binding.rvPlanModulCXC.setHasFixedSize(true)
        binding.rvPlanModulCXC.layoutManager = LinearLayoutManager(requireContext())
        //cargarDocumentos("https://$enlaceEmpresa/webservice/planificador_V2.php?vendedor=$cod_usuario")

        adapter = PlanificadorCXCAdapter(
            planificadorCxc = listaPlanificadorCxc,
            onClickListener = { cliente, nomCliente -> irACXC(cliente, nomCliente) },
            DIAS_VALIDOS_BOLIVARES = conn.getConfigNum("DIAS_VALIDOS_BOLIVARES_DOCS", codEmpresa!!).toInt()
        )
        binding.rvPlanModulCXC.adapter = adapter
        //consultarDocs("")
    }

    private fun irACXC(cliente: String, nomCliente: String) {
        //Creacion de un Bundle que servira como contenedor para enviar datos al siguiente fragment
        val datosAEnviar = Bundle()
        //snackBar("Accedio al cliente: $nomCliente")

        val numero: Int? = null

        //Guardado del codigo de vendedor
        datosAEnviar.putString("cliente", cliente)
        datosAEnviar.putString("nomCliente", nomCliente)

        //Navegacion al fagment de Vendedor
        //El fragment vendedor de informacion mas detallada de un mendedor
        findNavController().navigate(
            R.id.action_moduloCXCFragment_to_edoCuentaClienteFragment, datosAEnviar
        )
    }

    private fun cargarEnlace() {
        val columnas = arrayOf(
            "kee_nombre," + "kee_url," + "kee_sucursal"
        )
        val cursor = ke_android.query(
            "ke_enlace",
            columnas,
            "kee_codigo = '$codEmpresa'",
            null,
            null,
            null,
            null
        )
        while (cursor.moveToNext()) {
            nombreEmpresa = cursor.getString(0)
            enlaceEmpresa = cursor.getString(1)
            codigoSucursal = cursor.getString(2)
        }
        cursor.close()
        ke_android.close()
    }

    private fun consultarDocs(text: String?) {
        listaPlanificadorCxc.clear()
        listaPlanificadorCxc = conn.getPlanificadorDocs(cod_usuario!!, text, codEmpresa!!)
        adapter.updateAdapter(listaPlanificadorCxc)
    }

    @SuppressLint("SoonBlockedPrivateApi")
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
                consultarDocs(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                //buscarItem(newText)
                consultarDocs(newText)
                return true
            }
        })
        super.onCreateOptionsMenu(menu, inflater)
    }
}