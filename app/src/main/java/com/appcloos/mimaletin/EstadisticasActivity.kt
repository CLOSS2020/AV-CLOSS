package com.appcloos.mimaletin

import android.content.ContentValues
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.database.SQLException
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuItemCompat
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.appcloos.mimaletin.databinding.ActivityEstadisticasBinding
import org.json.JSONException
import org.json.JSONObject

class EstadisticasActivity : AppCompatActivity() {
    private lateinit var conn: AdminSQLiteOpenHelper
    lateinit var listavendedores: ListView
    private var vendedoresAdapter: VendedoresAdapter? = null
    private var listadeestadisticas: ArrayList<Estadistica>? = null
    private lateinit var cod_usuario: String
    private lateinit var codEmpresa: String

    private lateinit var binding: ActivityEstadisticasBinding

    // TableRow rw_porcentaje;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEstadisticasBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED // mantener la activity en vertical
        conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null)
        listavendedores = findViewById(R.id.listaVendedores)
        val preferences = getSharedPreferences("Preferences", MODE_PRIVATE)
        cod_usuario = preferences.getString("cod_usuario", null).toString()
        codEmpresa = preferences.getString("codigoEmpresa", null).toString()
        cargarEnlace()
        validarTipodeUsuario(cod_usuario)
        consultarVendedores(campo, cod_usuario)
        vendedoresAdapter = VendedoresAdapter(this@EstadisticasActivity, listadeestadisticas)
        listavendedores.adapter = vendedoresAdapter
        binding.refreshlay.setOnRefreshListener {
            actualizarLista()
            binding.refreshlay.isRefreshing = false
        }
        listavendedores.setOnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
            val codigoVend = listadeestadisticas!![position].getVendedor()
            val nombreVendedor = listadeestadisticas!![position].getNombrevend()
            iraDetalleVendedor(codigoVend, nombreVendedor)
        }
        val objetoAux = ObjetoAux(this)
        objetoAux.descargaDesactivo(cod_usuario, codEmpresa)
    }

    private fun iraDetalleVendedor(codigoVend: String, nombreVendedor: String) {
        val intent = Intent(applicationContext, DetalleVendedorActivity::class.java)
        intent.putExtra("codigoVend", codigoVend)
        intent.putExtra("nombreVend", nombreVendedor)
        intent.putExtra("codigoEmpresa", codEmpresa)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.menu_estadisticas_main, menu)
        val menuItem = menu.findItem(R.id.search_view_estadisticas)
        val buscadorVendedores = MenuItemCompat.getActionView(menuItem) as SearchView
        buscadorVendedores.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(busqueda: String): Boolean {
                buscarVendedor(busqueda)
                return false
            }

            override fun onQueryTextChange(busqueda: String): Boolean {
                buscarVendedor(busqueda)
                return false
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

    private fun buscarVendedor(busqueda: String) {
        val keAndroid = conn.writableDatabase
        var estadistica: Estadistica
        listadeestadisticas = ArrayList()
        val cursor = keAndroid.rawQuery(
            "SELECT vendedor, nombrevend, prcmeta, fecha_estad FROM ke_estadc01 " +
                "WHERE $campo = '$cod_usuario' AND " +
                "(vendedor LIKE '%$busqueda%' OR nombrevend LIKE '%$busqueda%') AND " +
                "empresa = '$codEmpresa' " +
                "ORDER BY prcmeta desc",
            null
        )
        while (cursor.moveToNext()) {
            estadistica = Estadistica()
            estadistica.setVendedor(cursor.getString(0))
            estadistica.setNombrevend(cursor.getString(1))
            var prcmeta = cursor.getDouble(2)
            prcmeta = prcmeta.valorReal()
            estadistica.setPrcmeta(prcmeta)
            estadistica.setFecha_estad(cursor.getString(3))
            listadeestadisticas!!.add(estadistica)
        }
        cursor.close()
        keAndroid.close()
        vendedoresAdapter = VendedoresAdapter(this@EstadisticasActivity, listadeestadisticas)
        listavendedores!!.adapter = vendedoresAdapter
        vendedoresAdapter!!.notifyDataSetChanged()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemid = item.itemId
        if (itemid == R.id.sync_estad) {
            fecha
            bajarEstadisticas(
                "https://$enlaceEmpresa/webservice/estadisticas_V2.php?campo=$campo&&cod_usuario=$cod_usuario&&fecha_sinc=$fechaEstadis"
            )
        }
        return super.onOptionsItemSelected(item)
    }

    private val fecha: Unit
        get() {
            // Calendar hoy = Calendar.getInstance();
            // SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            // fechaEstadis = sdf.format(hoy.getTime());
            fechaEstadis = "0001-01-01"
        }

    private fun cargarEnlace() {
        val keAndroid = conn.writableDatabase
        val columnas = arrayOf(
            "kee_nombre," +
                "kee_url," +
                "kee_sucursal"
        )
        val cursor = keAndroid.query(
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
        keAndroid.close()
    }

    private fun bajarEstadisticas(url: String) {
        val jsonArrayRequest: JsonObjectRequest = object : JsonObjectRequest(
            url,
            Response.Listener { response: JSONObject ->
                println(response)
                if (response.getString("estadisticas") != "null") {
                    Toast.makeText(
                        applicationContext,
                        "Descargando Estadísticas",
                        Toast.LENGTH_SHORT
                    ).show()

                    val estadisticas = response.getJSONArray("estadisticas")

                    for (i in 0 until estadisticas.length()) {
                        try {
                            val jsonObject = estadisticas.getJSONObject(i)

                            val codcoord = jsonObject.getString("codcoord")
                            val nomcoord = jsonObject.getString("nomcoord")
                            val vendedor = jsonObject.getString("vendedor")
                            val nombrevend = jsonObject.getString("nombrevend")
                            val cntpedidos = jsonObject.getDouble("cntpedidos")
                            val mtopedidos = jsonObject.getDouble("mtopedidos")
                            val cntfacturas = jsonObject.getDouble("cntfacturas")
                            val mtofacturas = jsonObject.getDouble("mtofacturas")
                            val metavend = jsonObject.getDouble("metavend")
                            val prcmeta = jsonObject.getDouble("prcmeta")
                            val cntclientes = jsonObject.getDouble("cntclientes")
                            val clivisit = jsonObject.getDouble("clivisit")
                            val prcvisitas = jsonObject.getDouble("prcvisitas")
                            val lomMontovtas = jsonObject.getDouble("lom_montovtas")
                            val lomPrcvtas = jsonObject.getDouble("lom_prcvtas")
                            val lomPrcvisit = jsonObject.getDouble("lom_prcvisit")
                            val rlomMontovtas = jsonObject.getDouble("rlom_montovtas")
                            val rlomPrcvtas = jsonObject.getDouble("rlom_prcvtas")
                            val rlomPrcvisit = jsonObject.getDouble("rlom_prcvisit")
                            val fechaEstad = jsonObject.getString("fecha_estad")
                            val ppgdolTotneto = jsonObject.getDouble("ppgdol_totneto")
                            val devdolTotneto = jsonObject.getDouble("devdol_totneto")
                            val defdolTotneto = jsonObject.getDouble("defdol_totneto")
                            val totdolcob = jsonObject.getDouble("totdolcob")
                            // val cntrecl = jsonObject.getDouble("cntrecl")
                            // val mtorecl = jsonObject.getDouble("mtorecl")

                            val cv = ContentValues()
                            cv.put("codcoord", codcoord)
                            cv.put("nomcoord", nomcoord)
                            cv.put("vendedor", vendedor)
                            cv.put("nombrevend", nombrevend)
                            cv.put("cntpedidos", cntpedidos)
                            cv.put("mtopedidos", mtopedidos)
                            cv.put("cntfacturas", cntfacturas)
                            cv.put("mtofacturas", mtofacturas)
                            cv.put("metavend", metavend)
                            cv.put("prcmeta", prcmeta)
                            cv.put("cntclientes", cntclientes)
                            cv.put("clivisit", clivisit)
                            cv.put("prcvisitas", prcvisitas)
                            cv.put("lom_montovtas", lomMontovtas)
                            cv.put("lom_prcvtas", lomPrcvtas)
                            cv.put("lom_prcvisit", lomPrcvisit)
                            cv.put("rlom_montovtas", rlomMontovtas)
                            cv.put("rlom_prcvtas", rlomPrcvtas)
                            cv.put("rlom_prcvisit", rlomPrcvisit)
                            cv.put("fecha_estad", fechaEstad)
                            cv.put("ppgdol_totneto", ppgdolTotneto)
                            cv.put("devdol_totneto", devdolTotneto)
                            cv.put("defdol_totneto", defdolTotneto)
                            cv.put("totdolcob", totdolcob)
                            // cv.put("cntrecl", cntrecl)
                            // cv.put("mtorecl", mtorecl)
                            cv.put("empresa", codEmpresa)

                            if (conn.validarExistenciaCamposVarios(
                                    "ke_estadc01",
                                    ArrayList(
                                        mutableListOf("vendedor", "empresa")
                                    ),
                                    arrayListOf(vendedor, codEmpresa)
                                )
                            ) {
                                conn.updateJSONCamposVarios(
                                    "ke_estadc01",
                                    cv,
                                    "vendedor = ? AND empresa = ?",
                                    arrayOf(vendedor, codEmpresa)
                                )
                            } else {
                                conn.insertJSON("ke_estadc01", cv)
                            }

                            Toast.makeText(
                                this@EstadisticasActivity,
                                "Datos actualizados.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } catch (e: SQLException) {
                            Toast.makeText(
                                this@EstadisticasActivity,
                                "Error en la insercion en $e",
                                Toast.LENGTH_LONG
                            ).show()
                        } catch (e: JSONException) {
                            Toast.makeText(
                                this@EstadisticasActivity,
                                "Error en la insercion en $e",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } // aca cierra el for

                    actualizarLista()
                } else {
                    Toast.makeText(
                        this@EstadisticasActivity,
                        "Sin actualización",
                        Toast.LENGTH_LONG
                    ).show()
                }
            },
            Response.ErrorListener { error: VolleyError? ->
                error!!.printStackTrace()
                Toast.makeText(
                    this@EstadisticasActivity,
                    "Sin actualización",
                    Toast.LENGTH_LONG
                ).show()
            }
        ) {
            override fun getParams(): Map<String, String> {
                return HashMap()
            }
        }
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(
            jsonArrayRequest
        ) // esto es el request que se envia al url a traves de la conexion volley, (el stringrequest esta armado arriba
    }

    private fun actualizarLista() {
        consultarVendedores(campo, cod_usuario)
        vendedoresAdapter = VendedoresAdapter(this@EstadisticasActivity, listadeestadisticas)
        listavendedores!!.adapter = vendedoresAdapter
        vendedoresAdapter!!.notifyDataSetChanged()
    }

    private fun validarTipodeUsuario(codUsuario: String?) {
        var tipodeUsuario = ""
        val keAndroid = conn.writableDatabase
        val cursorusu = keAndroid.rawQuery(
            "SELECT superves FROM usuarios WHERE vendedor = '$codUsuario' AND empresa = '$codEmpresa'",
            null
        )
        while (cursorusu.moveToNext()) {
            tipodeUsuario = cursorusu.getString(0)
        }
        cursorusu.close()
        when (tipodeUsuario) {
            "0" -> campo = "vendedor"
            "1" -> campo = "codcoord"
        }
    }

    // el metodo para consultar los vendedores segun el coordinador
    private fun consultarVendedores(campo: String?, codUsuario: String?) {
        val keAndroid = conn.writableDatabase
        var estadistica: Estadistica
        listadeestadisticas = ArrayList()
        val cursor = keAndroid.rawQuery(
            "SELECT vendedor, nombrevend, prcmeta, fecha_estad FROM ke_estadc01 " +
                "WHERE $campo = '$codUsuario' AND empresa = '$codEmpresa' ORDER BY prcmeta desc",
            null
        )
        while (cursor.moveToNext()) {
            estadistica = Estadistica()
            estadistica.setVendedor(cursor.getString(0))
            estadistica.setNombrevend(cursor.getString(1))
            var prcmeta = cursor.getDouble(2)
            prcmeta = prcmeta.valorReal()
            estadistica.setPrcmeta(prcmeta)
            estadistica.setFecha_estad(cursor.getString(3))
            listadeestadisticas!!.add(estadistica)
        }
        cursor.close()
        keAndroid.close()
    }

    companion object {
        var campo: String? = null
        var fechaEstadis: String? = null
        var nombrevend: String? = null
        var codcoord: String? = null
        var nomcoord: String? = null
        var vendedor: String? = null
        var fecha_estad: String? = null
        var nombreEmpresa = ""
        var enlaceEmpresa = ""
        var codigoSucursal = ""
        var ppgdol_totneto: String? = null
        var devdol_totneto: String? = null
        var defdol_totneto: String? = null
        var totdolcob: String? = null
        var cntpedidos: Double? = null
        var mtopedidos: Double? = null
        var cntfacturas: Double? = null
        var mtofacturas: Double? = null
        var metavend: Double? = null
        var prcmeta: Double? = null
        var cntclientes: Double? = null
        var clivisit: Double? = null
        var prcvisitas: Double? = null
        var lom_montovtas: Double? = null
        var lom_prcvtas: Double? = null
        var lom_prcvisit: Double? = null
        var rlom_montovtas: Double? = null
        var rlom_prcvtas: Double? = null
        var rlom_prcvisit: Double? = null
        var codigoVend: Double? = null
    }

    override fun getTheme(): Resources.Theme {
        val theme = super.getTheme()
        theme.applyStyle(setThemeAgencia(Constantes.AGENCIA), true)
        // you could also use a switch if you have many themes that could apply
        return theme
    }
}
