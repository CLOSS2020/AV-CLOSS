package com.appcloos.mimaletin.moduloCXC.fragments

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
import com.appcloos.mimaletin.databinding.FragmentEdoGenCuentaBinding
import com.appcloos.mimaletin.moduloCXC.fragments.edoGenCuentaAdapter.EdoGenCuentaAdapter
import com.appcloos.mimaletin.moduloCXC.viewmodel.EdoGeneralCxc


/**

 */
class EdoGenCuentaFragment : Fragment() {

    lateinit var preferences: SharedPreferences
    private lateinit var conn: AdminSQLiteOpenHelper
    lateinit var ke_android: SQLiteDatabase
    var cod_usuario: String? = null
    lateinit var nombreEmpresa: String
    lateinit var enlaceEmpresa: String
    lateinit var codigoSucursal: String
    private lateinit var binding: FragmentEdoGenCuentaBinding
    private lateinit var listaEstadoGen: MutableList<EdoGeneralCxc>
    private lateinit var adapter: EdoGenCuentaAdapter
    var ll_commit = false

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

    var kti_negesp: Int = 0

    private lateinit var searchView: SearchView
    private lateinit var menuItem: MenuItem
    private lateinit var searchManager: SearchManager

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentEdoGenCuentaBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferences = requireActivity().getSharedPreferences("Preferences", Context.MODE_PRIVATE)
        cod_usuario = preferences.getString("cod_usuario", null)

        conn = AdminSQLiteOpenHelper(context, "ke_android", null)
        ke_android = conn.writableDatabase
        cargarEnlace()

        listaEstadoGen = conn.getEdoGenCuenta(cod_usuario!!, null)
        binding.rvEstadoGencxc.setHasFixedSize(true)
        binding.rvEstadoGencxc.layoutManager = LinearLayoutManager(requireContext())

        adapter = EdoGenCuentaAdapter(edogencxc = listaEstadoGen,
            onClickListener = { cliente, nomCliente -> irACXC(cliente, nomCliente) })
        binding.rvEstadoGencxc.adapter = adapter
        //cargarDocumentos("https://$enlaceEmpresa/webservice/planificador_V2.php?vendedor=$cod_usuario")
        //adapter = EdoGenCuentaAdapter(edogencxc = listaEstadoGen, onClickListener = { cliente, nomCliente -> irACXC(cliente, nomCliente) })
        //binding.rvEstadoGencxc.adapter = adapter
    }

    private fun cargarEnlace() {

        val columnas = arrayOf(
            "kee_nombre," + "kee_url," + "kee_sucursal"
        )
        val cursor = ke_android.query("ke_enlace", columnas, "1", null, null, null, null)
        while (cursor.moveToNext()) {
            nombreEmpresa = cursor.getString(0)
            enlaceEmpresa = cursor.getString(1)
            codigoSucursal = cursor.getString(2)
        }
        cursor.close()
        ke_android.close()
    }

    /*private fun cargarDocumentos(URL: String) {
        ke_android = conn.readableDatabase
        //println("hasta aca todo bien $URL")
        val jsonArrayRequest: JsonArrayRequest = JsonArrayRequest(Request.Method.GET, // method
            URL, // url
            null, // json request
            { response -> // response listener


                if (response != null) {
                    //println("si hubo respuesta")
                    ll_commit = false
                    ke_android.beginTransaction()
                    var jsonObject: JSONObject? = null
                    try {

                        // loop through the array elements
                        for (i in 0 until response.length()) {
                            jsonObject = response.getJSONObject(i)
                            agencia = jsonObject.getString("agencia").trim()
                            tipodoc = jsonObject.getString("tipodoc").trim()
                            codigoCliente = jsonObject.getString("codcliente").trim()
                            nombreCliente = jsonObject.getString("nombrecli").trim()
                            documento = jsonObject.getString("documento").trim()
                            tipodocv = jsonObject.getString("tipodocv").trim()
                            contribesp = jsonObject.getDouble("contribesp")
                            ruta_parme = jsonObject.getString("ruta_parme").trim()
                            tipoprecio = jsonObject.getDouble("tipoprecio")
                            emision = jsonObject.getString("emision").trim()
                            recepcion = jsonObject.getString("recepcion").trim()
                            vence = jsonObject.getString("vence").trim()
                            diascred = jsonObject.getDouble("diascred")
                            estatusdoc = jsonObject.getString("estatusdoc").trim()
                            dtotneto = jsonObject.getDouble("dtotneto")
                            dtotimpuest = jsonObject.getDouble("dtotimpuest")
                            dtotalfinal = jsonObject.getDouble("dtotalfinal")
                            dtotpagos = jsonObject.getDouble("dtotpagos")
                            dtotdescuen = jsonObject.getDouble("dtotdescuen")
                            dFlete = jsonObject.getDouble("dFlete")
                            dtotdev = jsonObject.getDouble("dtotdev")
                            dvndmtototal = jsonObject.getDouble("dvndmtototal")
                            dretencion = jsonObject.getDouble("dretencion")
                            dretencioniva = jsonObject.getDouble("dretencioniva")
                            vendedor = jsonObject.getString("vendedor").trim()
                            codcoord = jsonObject.getString("codcoord").trim()
                            fechamodifi = jsonObject.getString("fechamodifi").trim()
                            aceptadev = jsonObject.getString("aceptadev").trim()
                            bsiva = jsonObject.getDouble("bsiva")
                            bsflete = jsonObject.getDouble("bsflete")
                            bsretencioniva = jsonObject.getDouble("bsretencioniva")
                            bsretencion = jsonObject.getDouble("bsretencion")
                            tasadoc = jsonObject.getDouble("tasadoc")
                            montodcto = jsonObject.getDouble("mtodcto")
                            fechavencedcto = jsonObject.getString("fchvencedcto")
                            tienedcto = jsonObject.getString("tienedcto")
                            cbsret = jsonObject.getDouble("cbsret")
                            cdret = jsonObject.getDouble("cdret")
                            cbsretiva = jsonObject.getDouble("cbsretiva")
                            cdretiva = jsonObject.getDouble("cdretiva")
                            cbsrparme = jsonObject.getDouble("cbsrparme")
                            cdrparme = jsonObject.getDouble("cdrparme")
                            bsmtoiva = jsonObject.getDouble("bsmtoiva")
                            bsmtofte = jsonObject.getDouble("bsmtofte")
                            cbsretflete = jsonObject.getDouble("cbsretflete")
                            cdretflete = jsonObject.getDouble("cdretflete")
                            retmun_mto = jsonObject.getDouble("retmun_mto")
                            kti_negesp = jsonObject.getInt("kti_negesp")

                            val qDocumentosCab = ContentValues()
                            qDocumentosCab.put("agencia", agencia)
                            qDocumentosCab.put("tipodoc", tipodoc)
                            qDocumentosCab.put("documento", documento)
                            qDocumentosCab.put("tipodocv", tipodocv)
                            qDocumentosCab.put("codcliente", codigoCliente)
                            qDocumentosCab.put("nombrecli", nombreCliente)
                            qDocumentosCab.put("contribesp", contribesp)
                            qDocumentosCab.put("ruta_parme", ruta_parme)
                            qDocumentosCab.put("tipoprecio", tipoprecio)
                            qDocumentosCab.put("emision", emision)
                            qDocumentosCab.put("recepcion", recepcion)
                            qDocumentosCab.put("vence", vence)
                            qDocumentosCab.put("diascred", diascred)
                            qDocumentosCab.put("estatusdoc", estatusdoc)
                            qDocumentosCab.put("dtotneto", dtotneto)
                            qDocumentosCab.put("dretencion", dretencion)
                            qDocumentosCab.put("dretencioniva", dretencioniva)
                            qDocumentosCab.put("dtotimpuest", dtotimpuest)
                            qDocumentosCab.put("dtotalfinal", dtotalfinal)
                            qDocumentosCab.put("dtotpagos", dtotpagos)
                            qDocumentosCab.put("dtotdescuen", dtotdescuen)
                            qDocumentosCab.put("dFlete", dFlete)
                            qDocumentosCab.put("dtotdev", dtotdev)
                            qDocumentosCab.put("dvndmtototal", dvndmtototal)
                            qDocumentosCab.put("vendedor", vendedor)
                            qDocumentosCab.put("codcoord", codcoord)
                            qDocumentosCab.put("fechamodifi", fechamodifi)
                            qDocumentosCab.put("aceptadev", aceptadev)
                            qDocumentosCab.put("bsiva", bsiva)
                            qDocumentosCab.put("bsflete", bsflete)
                            qDocumentosCab.put("bsretencion", bsretencion)
                            qDocumentosCab.put("bsretencioniva", bsretencioniva)
                            qDocumentosCab.put("tasadoc", tasadoc)
                            qDocumentosCab.put("mtodcto", montodcto)
                            qDocumentosCab.put("fchvencedcto", fechavencedcto)
                            qDocumentosCab.put("tienedcto", tienedcto)
                            qDocumentosCab.put("cbsret", cbsret)
                            qDocumentosCab.put("cdret", cdret)
                            qDocumentosCab.put("cbsretiva", cbsretiva)
                            qDocumentosCab.put("cdretiva", cdretiva)
                            qDocumentosCab.put("cbsrparme", cbsrparme)
                            qDocumentosCab.put("bsmtoiva", bsmtoiva)
                            qDocumentosCab.put("bsmtofte", bsmtofte)
                            qDocumentosCab.put("cbsretflete", cbsretflete)
                            qDocumentosCab.put("cdretflete", cdretflete)
                            qDocumentosCab.put("retmun_mto", retmun_mto)
                            qDocumentosCab.put("kti_negesp", kti_negesp)
                            qDocumentosCab.put("cdrparme", cdrparme)

                            val qcodigoLocal: Cursor = ke_android.rawQuery(
                                "SELECT count(documento) FROM ke_doccti WHERE documento ='$documento'",
                                null
                            )
                            qcodigoLocal.moveToFirst()
                            //variable para obtener el conteo de documentos que ya esten en el telf
                            val codigoExistente = qcodigoLocal.getInt(0)

                            qcodigoLocal.close()

                            if (codigoExistente > 0) {
                                ke_android.update(
                                    "ke_doccti", qDocumentosCab, "documento= ?", arrayOf(documento)
                                )
                            } else if (codigoExistente == 0) {
                                ke_android.insert("ke_doccti", null, qDocumentosCab)
                            }
                            ll_commit = true

                        }

                    } catch (ex: Exception) {
                        println(ex.message)
                        ll_commit = false

                        if (!ll_commit) return@JsonArrayRequest


                    }
                    if (ll_commit) {
                        ke_android.setTransactionSuccessful()
                        ke_android.endTransaction()

                        val fechaActualizada: String
                        val qfechaDocs: ContentValues = ContentValues()

                        val fecha_modif: Calendar = Calendar.getInstance()
                        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        fechaActualizada = sdf.format(fecha_modif.time)
                        qfechaDocs.put("fchhn_ultmod", fechaActualizada)
                        ke_android.update(
                            "tabla_aux", qfechaDocs, "tabla = ?", arrayOf("ke_doccti")
                        )
                        //consulto los documentos ya con la data actualizada
                        consultarEstadoClientes("")

                    } else if (!ll_commit) {
                        ke_android.endTransaction()
                    }

                }
            }, { error -> // error listener
                //
            })

        val requestQueue: RequestQueue = Volley.newRequestQueue(activity)
        requestQueue.add(jsonArrayRequest)
    }*/

    private fun consultarEstadoClientes(text: String?) {
        listaEstadoGen.clear()
        listaEstadoGen = conn.getEdoGenCuenta(cod_usuario!!, text)
        adapter.updateAdapter(listaEstadoGen)
    }

    private fun irACXC(cliente: String, nomCliente: String) {
        //Creacion de un Bundle que servira como contenedor para enviar datos al siguiente fragment
        val datosAEnviar = Bundle()

        //Guardado del codigo de vendedor
        datosAEnviar.putString("cliente", cliente)
        datosAEnviar.putString("nomCliente", nomCliente)

        //Navegacion al fagment de Vendedor
        //El fragment vendedor de informacion mas detallada de un mendedor
        findNavController().navigate(
            R.id.action_moduloCXCFragment_to_edoCuentaClienteFragment, datosAEnviar
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
                consultarEstadoClientes(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                //buscarItem(newText)
                consultarEstadoClientes(newText)
                return true
            }
        })
        super.onCreateOptionsMenu(menu, inflater)
    }
    /*private fun buscarItem(Text: String?) {
        if (Text != null) {
            if (Text.isNotEmpty()) {
                val estadoFiltrado = listaEstadoGen.filter { listaEstadoGen ->
                    listaEstadoGen.nomcliente.lowercase().contains(Text.toString().lowercase())
                }
                adapter.actualizarFact(estadoFiltrado)
            } else {
                adapter.actualizarFact(listaEstadoGen)
            }
        } else {
            adapter.actualizarFact(listaEstadoGen)
        }
    }*/

}