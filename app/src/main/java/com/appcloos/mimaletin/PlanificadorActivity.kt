package com.appcloos.mimaletin

import android.content.ContentValues
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuItemCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class PlanificadorActivity : AppCompatActivity() {
    var conn: AdminSQLiteOpenHelper? = null
    var listaplanificador: ListView? = null
    private var planificadorAdapter: PlanificadorAdapter? = null
    var listadocs: ArrayList<Documentos>? = null
    var swipeRefreshLayout: SwipeRefreshLayout? = null
    var cod_usuario: String? = null
    var campo: String? = null
    private var ll_commit: Boolean? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_planificador)
        requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT //mantener la activity en vertical
        conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null)
        //identifico en el layout el listview
        cargarEnlace()
        listaplanificador = findViewById(R.id.listaPlanificador)
        //cargo el codigo del vendedor que viene desde el activity anterior
        val preferences = getSharedPreferences("Preferences", MODE_PRIVATE)
        cod_usuario = preferences.getString("cod_usuario", null)
        //identifico el swipe refresh layout
        swipeRefreshLayout = findViewById(R.id.refreshplanificador)

        //tengo que validar si el usuario que inicio sesión es coordinador o vendedor
        validarTipodeUsuario(cod_usuario)
        consultarDocumentos(campo, cod_usuario)
        planificadorAdapter = PlanificadorAdapter(this@PlanificadorActivity, listadocs!!)
        listaplanificador!!.setAdapter(planificadorAdapter)
        swipeRefreshLayout!!.setOnRefreshListener(OnRefreshListener {
            actualizarLista()
            swipeRefreshLayout!!.setRefreshing(false)
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.menu_planificador, menu)
        val menuItem = menu.findItem(R.id.search_view_docs)
        val buscadorDocs = MenuItemCompat.getActionView(menuItem) as SearchView
        buscadorDocs.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(busqueda: String): Boolean {
                BuscarDocumentos(busqueda)
                return false
            }

            override fun onQueryTextChange(busqueda: String): Boolean {
                BuscarDocumentos(busqueda)
                return false
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

    private fun BuscarDocumentos(busqueda: String) {
        val ke_android = conn!!.writableDatabase
        var documentos: Documentos? = null
        listadocs = ArrayList()
        val cursor = ke_android.rawQuery(
            "SELECT codcliente, nombrecli, estatusdoc, documento, vence, diascred, kti_negesp FROM ke_doccti WHERE " + campo + "='" + cod_usuario + "' " +
                    "AND (nombrecli LIKE'%" + busqueda + "%' OR documento LIKE '%" + busqueda + "%') AND (estatusdoc = '0' OR estatusdoc= '1') ORDER BY vence asc",
            null
        )
        while (cursor.moveToNext()) {
            documentos = Documentos()
            documentos.codcliente = cursor.getString(0)
            documentos.nombrecli = cursor.getString(1)
            documentos.estatusdoc = cursor.getString(2)
            documentos.documento = cursor.getString(3)
            documentos.vence = cursor.getString(4)
            documentos.diascred = cursor.getDouble(5)
            documentos.ktiNegesp = cursor.getString(6)
            listadocs!!.add(documentos)
        }
        ke_android.close()
        planificadorAdapter = PlanificadorAdapter(this@PlanificadorActivity, listadocs!!)
        listaplanificador!!.adapter = planificadorAdapter
        planificadorAdapter!!.notifyDataSetChanged()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemid = item.itemId
        when (itemid) {
            R.id.sync_docs -> bajarDocumentos("https://" + enlaceEmpresa + "/Rest/planificador.php?campo=" + campo + "&&vendedor=" + cod_usuario + "&&agencia=" + codigoSucursal.trim { it <= ' ' })
        }
        return super.onOptionsItemSelected(item)
    }

    private fun cargarEnlace() {
        val ke_android = conn!!.writableDatabase
        val columnas = arrayOf(
            "kee_nombre," +
                    "kee_url," +
                    "kee_sucursal"
        )
        val cursor = ke_android.query("ke_enlace", columnas, "1", null, null, null, null)
        while (cursor.moveToNext()) {
            nombreEmpresa = cursor.getString(0)
            enlaceEmpresa = cursor.getString(1)
            codigoSucursal = cursor.getString(2)
        }
        ke_android.close()
    }

    private fun bajarDocumentos(URL: String) {
        val jsonArrayRequest: JsonArrayRequest =
            object : JsonArrayRequest(URL, Response.Listener { response ->
                if (response != null) {
                    conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null)
                    val ke_android = conn!!.writableDatabase
                    var jsonObject: JSONObject? = null //creamos un objeto json vacio
                    ll_commit = false
                    ke_android.beginTransaction()
                    for (i in 0 until response.length()) {
                        try {

                            //obtengo de la respuesta los datos en un json object
                            jsonObject = response.getJSONObject(i)
                            //preparo los campos para las operaciones
                            agencia = jsonObject.getString("agencia").trim { it <= ' ' }
                            tipodoc = jsonObject.getString("tipodoc").trim { it <= ' ' }
                            documento = jsonObject.getString("documento").trim { it <= ' ' }
                            tipodocv = jsonObject.getString("tipodocv").trim { it <= ' ' }
                            codigoCliente = jsonObject.getString("codcliente").trim { it <= ' ' }
                            nombreCliente = jsonObject.getString("nombrecli").trim { it <= ' ' }
                            contribesp = jsonObject.getDouble("contribesp")
                            ruta_parme = jsonObject.getString("ruta_parme").trim { it <= ' ' }
                            tipoprecio = jsonObject.getDouble("tipoprecio")
                            emision = jsonObject.getString("emision").trim { it <= ' ' }
                            recepcion = jsonObject.getString("recepcion").trim { it <= ' ' }
                            vence = jsonObject.getString("vence").trim { it <= ' ' }
                            diascred = jsonObject.getDouble("diascred")
                            estatusdoc = jsonObject.getString("estatusdoc").trim { it <= ' ' }
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
                            vendedor = jsonObject.getString("vendedor").trim { it <= ' ' }
                            codcoord = jsonObject.getString("codcoord").trim { it <= ' ' }
                            fechamodifi = jsonObject.getString("fechamodifi").trim { it <= ' ' }
                            aceptadev = jsonObject.getString("aceptadev").trim { it <= ' ' }
                            kti_negesp = jsonObject.getString("kti_negesp").trim { it <= ' ' }
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
                            qDocumentosCab.put("kti_negesp", kti_negesp)
                            val qcodigoLocal = ke_android.rawQuery(
                                "SELECT count(documento) FROM ke_doccti WHERE documento ='" + documento + "'",
                                null
                            )
                            qcodigoLocal.moveToFirst()
                            val codigoExiste = qcodigoLocal.getInt(0)
                            if (codigoExiste > 0) {
                                ke_android.update(
                                    "ke_doccti", qDocumentosCab, "documento = ?", arrayOf(
                                        documento
                                    )
                                )
                            } else if (codigoExiste == 0) {
                                ke_android.insert("ke_doccti", null, qDocumentosCab)
                            }
                            ll_commit = true
                        } catch (e: Exception) {
                            println("Error de inserción: $e")
                            ll_commit = false
                            if (!ll_commit!!) {
                                return@Listener
                            }
                        }
                    }
                    if (ll_commit!!) {
                        ke_android.setTransactionSuccessful()
                        ke_android.endTransaction()
                        actualizarLista()
                    } else if (!ll_commit!!) {
                        ke_android.endTransaction()
                    }
                }
            }, Response.ErrorListener { }) {
                @Throws(AuthFailureError::class)
                override fun getParams(): Map<String, String>? {
                    val parametros: MutableMap<String, String> = HashMap()
                    parametros["fecha_sinc"] = fechaDocs!!
                    parametros["codigo_cli"] = codigoCliente!!
                    return parametros
                }
            }
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(jsonArrayRequest) //esto es el request que se envia al url a traves de la conexion volley, (el stringrequest esta armado arriba)
    }

    private fun actualizarLista() {
        consultarDocumentos(campo, cod_usuario)
        planificadorAdapter = PlanificadorAdapter(this@PlanificadorActivity, listadocs!!)
        listaplanificador!!.adapter = planificadorAdapter
        planificadorAdapter!!.notifyDataSetChanged()
    }

    private fun consultarDocumentos(campo: String?, cod_usuario: String?) {
        val ke_android = conn!!.writableDatabase
        var documentos: Documentos? = null
        listadocs = ArrayList()
        val cursor = ke_android.rawQuery(
            "SELECT codcliente, nombrecli, estatusdoc, documento, vence, diascred, kti_negesp FROM ke_doccti WHERE $campo= '$cod_usuario' AND (estatusdoc = '0' OR estatusdoc= '1') ORDER BY vence asc",
            null
        )
        while (cursor.moveToNext()) {
            documentos = Documentos()
            documentos.codcliente = cursor.getString(0)
            documentos.nombrecli = cursor.getString(1)
            documentos.estatusdoc = cursor.getString(2)
            documentos.documento = cursor.getString(3)
            documentos.vence = cursor.getString(4)
            documentos.diascred = cursor.getDouble(5)
            documentos.ktiNegesp = cursor.getString(6)
            listadocs!!.add(documentos)
        }
        ke_android.close()
    }

    private fun validarTipodeUsuario(cod_usuario: String?) {
        var tipodeUsuario = ""
        val ke_android = conn!!.writableDatabase
        val cursorusu = ke_android.rawQuery(
            "SELECT superves FROM usuarios WHERE vendedor ='$cod_usuario'",
            null
        )
        while (cursorusu.moveToNext()) {
            tipodeUsuario = cursorusu.getString(0)
        }
        when (tipodeUsuario) {
            "0" -> campo = "vendedor"
            "1" -> campo = "codcoord"
        }
    }

    companion object {
        private var codigoCliente: String? = null
        private var nombreCliente: String? = null
        private var agencia: String? = null
        private var tipodoc: String? = null
        private var documento: String? = null
        private var tipodocv: String? = null
        private var ruta_parme: String? = null
        private var emision: String? = null
        private var recepcion: String? = null
        private var vence: String? = null
        private var estatusdoc: String? = null
        private val grupo: String? = null
        private val subgrupo: String? = null
        private val codhijo: String? = null
        private val pid: String? = null
        private val codigo: String? = null
        private val nombre: String? = null
        private val fechadoc: String? = null
        private var vendedor: String? = null
        private var codcoord: String? = null
        private var fechamodifi: String? = null
        private var aceptadev: String? = null
        private val fechaDocs: String? = null
        private var kti_negesp: String? = null
        private var nombreEmpresa = ""
        private var enlaceEmpresa = ""
        private var codigoSucursal = ""
        var contribesp: Double? = null
        var tipoprecio: Double? = null
        var diascred: Double? = null
        var dtotneto: Double? = null
        var dtotimpuest: Double? = null
        var dtotalfinal: Double? = null
        var dtotpagos: Double? = null
        var dtotdescuen: Double? = null
        var dFlete: Double? = null
        var dtotdev: Double? = null
        var dvndmtototal: Double? = null
        var dretencion: Double? = null
        var dretencioniva: Double? = null
        var origen: Double? = null
        var cantidad: Double? = null
        var cntdevuelt: Double? = null
        var vndcntdevuelt: Double? = null
        var dpreciofin: Double? = null
        var dpreciounit: Double? = null
        var dmontoneto: Double? = null
        var dmontototal: Double? = null
        var timpueprc: Double? = null
        var unidevuelt: Double? = null
    }
}