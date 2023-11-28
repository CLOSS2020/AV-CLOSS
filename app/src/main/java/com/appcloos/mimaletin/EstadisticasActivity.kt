package com.appcloos.mimaletin

import android.content.ContentValues
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.database.DatabaseUtils
import android.database.SQLException
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ListView
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuItemCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.appcloos.mimaletin.databinding.ActivityEstadisticasBinding
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class EstadisticasActivity : AppCompatActivity() {
    var conn: AdminSQLiteOpenHelper? = null
    lateinit var listavendedores: ListView
    private var vendedoresAdapter: VendedoresAdapter? = null
    private var listadeestadisticas: ArrayList<Estadistica>? = null

    private lateinit var binding: ActivityEstadisticasBinding

    //TableRow rw_porcentaje;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEstadisticasBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED //mantener la activity en vertical
        conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null, 4)
        listavendedores = findViewById(R.id.listaVendedores)
        val preferences = getSharedPreferences("Preferences", MODE_PRIVATE)
        cod_usuario = preferences.getString("cod_usuario", null)
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
        objetoAux.descargaDesactivo(cod_usuario!!)
    }

    private fun iraDetalleVendedor(codigoVend: String, nombreVendedor: String) {
        val intent = Intent(applicationContext, DetalleVendedorActivity::class.java)
        intent.putExtra("codigoVend", codigoVend)
        intent.putExtra("nombreVend", nombreVendedor)
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
        val keAndroid = conn!!.writableDatabase
        var estadistica: Estadistica
        listadeestadisticas = ArrayList()
        val cursor = keAndroid.rawQuery(
            "SELECT vendedor, nombrevend, prcmeta, fecha_estad FROM ke_estadc01 WHERE $campo='$cod_usuario' AND (vendedor LIKE '%$busqueda%' OR nombrevend LIKE '%$busqueda%')  ORDER BY prcmeta desc",
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
            bajarEstadisticas("https://" + enlaceEmpresa + "/webservice/estadisticas.php?campo=" + campo + "&&cod_usuario=" + cod_usuario!!.trim { it <= ' ' } + "&&fecha_sinc=" + fechaEstadis + "&&agencia=" + codigoSucursal.trim { it <= ' ' })
        }
        return super.onOptionsItemSelected(item)
    }

    private val fecha: Unit
        get() {
            //Calendar hoy = Calendar.getInstance();
            //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            //fechaEstadis = sdf.format(hoy.getTime());
            fechaEstadis = "0001-01-01"
        }

    private fun cargarEnlace() {
        val keAndroid = conn!!.writableDatabase
        val columnas = arrayOf(
            "kee_nombre," +
                    "kee_url," +
                    "kee_sucursal"
        )
        val cursor = keAndroid.query("ke_enlace", columnas, "1", null, null, null, null)
        while (cursor.moveToNext()) {
            nombreEmpresa = cursor.getString(0)
            enlaceEmpresa = cursor.getString(1)
            codigoSucursal = cursor.getString(2)
        }
        cursor.close()
        keAndroid.close()
    }

    private fun bajarEstadisticas(url: String) {
        val jsonArrayRequest: JsonArrayRequest = object : JsonArrayRequest(
            url,
            Response.Listener { response: JSONArray? ->
                println(response)
                if (response != null) {
                    Toast.makeText(
                        applicationContext,
                        "Descargando Estadísticas",
                        Toast.LENGTH_SHORT
                    ).show()
                    val keAndroid = conn!!.writableDatabase
                    val filas = DatabaseUtils.queryNumEntries(keAndroid, "ke_estadc01")
                    if (filas > 0) {
                        var jsonObject: JSONObject?  //creamos un objeto json vacio
                        for (i in 0 until response.length()) {
                            try {
                                keAndroid.beginTransaction()
                                jsonObject = response.getJSONObject(i)
                                codcoord = jsonObject.getString("codcoord").trim { it <= ' ' }
                                nomcoord = jsonObject.getString("nomcoord").trim { it <= ' ' }
                                vendedor = jsonObject.getString("vendedor").trim { it <= ' ' }
                                nombrevend = jsonObject.getString("nombrevend").trim { it <= ' ' }
                                cntpedidos = jsonObject.getDouble("cntpedidos")
                                mtopedidos = jsonObject.getDouble("mtopedidos")
                                cntfacturas = jsonObject.getDouble("cntfacturas")
                                mtofacturas = jsonObject.getDouble("mtofacturas")
                                metavend = jsonObject.getDouble("metavend")
                                prcmeta = jsonObject.getDouble("prcmeta")
                                cntclientes = jsonObject.getDouble("cntclientes")
                                clivisit = jsonObject.getDouble("clivisit")
                                prcvisitas = jsonObject.getDouble("prcvisitas")
                                lom_montovtas = jsonObject.getDouble("lom_montovtas")
                                lom_prcvtas = jsonObject.getDouble("lom_prcvtas")
                                lom_prcvisit = jsonObject.getDouble("lom_prcvisit")
                                rlom_montovtas = jsonObject.getDouble("rlom_montovtas")
                                rlom_prcvtas = jsonObject.getDouble("rlom_prcvtas")
                                rlom_prcvisit = jsonObject.getDouble("rlom_prcvisit")
                                fecha_estad = jsonObject.getString("fecha_estad")
                                ppgdol_totneto = jsonObject.getString("ppgdol_totneto")
                                devdol_totneto = jsonObject.getString("devdol_totneto")
                                defdol_totneto = jsonObject.getString("defdol_totneto")
                                totdolcob = jsonObject.getString("totdolcob")
                                val actualizar = ContentValues()
                                actualizar.put("codcoord", codcoord)
                                actualizar.put("nomcoord", nomcoord)
                                actualizar.put("vendedor", vendedor)
                                actualizar.put("nombrevend", nombrevend)
                                actualizar.put("cntpedidos", cntpedidos)
                                actualizar.put("mtopedidos", mtopedidos)
                                actualizar.put("cntfacturas", cntfacturas)
                                actualizar.put("mtofacturas", mtofacturas)
                                actualizar.put("metavend", metavend)
                                actualizar.put("prcmeta", prcmeta)
                                actualizar.put("cntclientes", cntclientes)
                                actualizar.put("clivisit", clivisit)
                                actualizar.put("prcvisitas", prcvisitas)
                                actualizar.put("lom_montovtas", lom_montovtas)
                                actualizar.put("lom_prcvtas", lom_prcvtas)
                                actualizar.put("lom_prcvisit", lom_prcvisit)
                                actualizar.put("rlom_montovtas", rlom_montovtas)
                                actualizar.put("rlom_prcvtas", rlom_prcvtas)
                                actualizar.put("rlom_prcvisit", rlom_prcvisit)
                                actualizar.put("fecha_estad", fecha_estad)
                                actualizar.put("ppgdol_totneto", ppgdol_totneto)
                                actualizar.put("devdol_totneto", devdol_totneto)
                                actualizar.put("defdol_totneto", defdol_totneto)
                                actualizar.put("totdolcob", totdolcob)
                                keAndroid.update(
                                    "ke_estadc01", actualizar, "vendedor = ?", arrayOf(
                                        vendedor
                                    )
                                )
                                keAndroid.setTransactionSuccessful()
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
                            } finally {
                                keAndroid.endTransaction()
                            }
                            val vendedorExistente = keAndroid.rawQuery(
                                "SELECT count(vendedor) FROM ke_estadc01 WHERE vendedor ='$vendedor'",
                                null
                            )
                            vendedorExistente.moveToFirst()
                            val vendedorExistente1 = vendedorExistente.getInt(0)
                            vendedorExistente.close()
                            if (vendedorExistente1 > 0) {
                                try {
                                    keAndroid.beginTransaction()
                                    jsonObject = response.getJSONObject(i)
                                    codcoord = jsonObject.getString("codcoord").trim { it <= ' ' }
                                    nomcoord = jsonObject.getString("nomcoord").trim { it <= ' ' }
                                    vendedor = jsonObject.getString("vendedor").trim { it <= ' ' }
                                    nombrevend =
                                        jsonObject.getString("nombrevend").trim { it <= ' ' }
                                    cntpedidos = jsonObject.getDouble("cntpedidos")
                                    mtopedidos = jsonObject.getDouble("mtopedidos")
                                    cntfacturas = jsonObject.getDouble("cntfacturas")
                                    mtofacturas = jsonObject.getDouble("mtofacturas")
                                    metavend = jsonObject.getDouble("metavend")
                                    prcmeta = jsonObject.getDouble("prcmeta")
                                    cntclientes = jsonObject.getDouble("cntclientes")
                                    clivisit = jsonObject.getDouble("clivisit")
                                    prcvisitas = jsonObject.getDouble("prcvisitas")
                                    lom_montovtas = jsonObject.getDouble("lom_montovtas")
                                    lom_prcvtas = jsonObject.getDouble("lom_prcvtas")
                                    lom_prcvisit = jsonObject.getDouble("lom_prcvisit")
                                    rlom_montovtas = jsonObject.getDouble("rlom_montovtas")
                                    rlom_prcvtas = jsonObject.getDouble("rlom_prcvtas")
                                    rlom_prcvisit = jsonObject.getDouble("rlom_prcvisit")
                                    fecha_estad = jsonObject.getString("fecha_estad")
                                    ppgdol_totneto = jsonObject.getString("ppgdol_totneto")
                                    devdol_totneto = jsonObject.getString("devdol_totneto")
                                    defdol_totneto = jsonObject.getString("defdol_totneto")
                                    totdolcob = jsonObject.getString("totdolcob")
                                    val actualizar = ContentValues()
                                    actualizar.put("codcoord", codcoord)
                                    actualizar.put("nomcoord", nomcoord)
                                    actualizar.put("vendedor", vendedor)
                                    actualizar.put("nombrevend", nombrevend)
                                    actualizar.put("cntpedidos", cntpedidos)
                                    actualizar.put("mtopedidos", mtopedidos)
                                    actualizar.put("cntfacturas", cntfacturas)
                                    actualizar.put("mtofacturas", mtofacturas)
                                    actualizar.put("metavend", metavend)
                                    actualizar.put("prcmeta", prcmeta)
                                    actualizar.put("cntclientes", cntclientes)
                                    actualizar.put("clivisit", clivisit)
                                    actualizar.put("prcvisitas", prcvisitas)
                                    actualizar.put("lom_montovtas", lom_montovtas)
                                    actualizar.put("lom_prcvtas", lom_prcvtas)
                                    actualizar.put("lom_prcvisit", lom_prcvisit)
                                    actualizar.put("rlom_montovtas", rlom_montovtas)
                                    actualizar.put("rlom_prcvtas", rlom_prcvtas)
                                    actualizar.put("rlom_prcvisit", rlom_prcvisit)
                                    actualizar.put("fecha_estad", fecha_estad)
                                    actualizar.put("ppgdol_totneto", ppgdol_totneto)
                                    actualizar.put("devdol_totneto", devdol_totneto)
                                    actualizar.put("defdol_totneto", defdol_totneto)
                                    actualizar.put("totdolcob", totdolcob)
                                    keAndroid.update(
                                        "ke_estadc01", actualizar, "vendedor = ?", arrayOf(
                                            vendedor
                                        )
                                    )
                                    keAndroid.setTransactionSuccessful()
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
                                } finally {
                                    keAndroid.endTransaction()
                                }
                            } else {
                                try {
                                    keAndroid.beginTransaction()
                                    jsonObject = response.getJSONObject(i)
                                    codcoord = jsonObject.getString("codcoord").trim { it <= ' ' }
                                    nomcoord = jsonObject.getString("nomcoord").trim { it <= ' ' }
                                    vendedor = jsonObject.getString("vendedor").trim { it <= ' ' }
                                    nombrevend =
                                        jsonObject.getString("nombrevend").trim { it <= ' ' }
                                    cntpedidos = jsonObject.getDouble("cntpedidos")
                                    mtopedidos = jsonObject.getDouble("mtopedidos")
                                    cntfacturas = jsonObject.getDouble("cntfacturas")
                                    mtofacturas = jsonObject.getDouble("mtofacturas")
                                    metavend = jsonObject.getDouble("metavend")
                                    prcmeta = jsonObject.getDouble("prcmeta")
                                    cntclientes = jsonObject.getDouble("cntclientes")
                                    clivisit = jsonObject.getDouble("clivisit")
                                    prcvisitas = jsonObject.getDouble("prcvisitas")
                                    lom_montovtas = jsonObject.getDouble("lom_montovtas")
                                    lom_prcvtas = jsonObject.getDouble("lom_prcvtas")
                                    lom_prcvisit = jsonObject.getDouble("lom_prcvisit")
                                    rlom_montovtas = jsonObject.getDouble("rlom_montovtas")
                                    rlom_prcvtas = jsonObject.getDouble("rlom_prcvtas")
                                    rlom_prcvisit = jsonObject.getDouble("rlom_prcvisit")
                                    fecha_estad = jsonObject.getString("fecha_estad")
                                    ppgdol_totneto = jsonObject.getString("ppgdol_totneto")
                                    devdol_totneto = jsonObject.getString("devdol_totneto")
                                    defdol_totneto = jsonObject.getString("defdol_totneto")
                                    totdolcob = jsonObject.getString("totdolcob")
                                    val insertar = ContentValues()
                                    insertar.put("codcoord", codcoord)
                                    insertar.put("nomcoord", nomcoord)
                                    insertar.put("vendedor", vendedor)
                                    insertar.put("nombrevend", nombrevend)
                                    insertar.put("cntpedidos", cntpedidos)
                                    insertar.put("mtopedidos", mtopedidos)
                                    insertar.put("cntfacturas", cntfacturas)
                                    insertar.put("mtofacturas", mtofacturas)
                                    insertar.put("metavend", metavend)
                                    insertar.put("prcmeta", prcmeta)
                                    insertar.put("cntclientes", cntclientes)
                                    insertar.put("clivisit", clivisit)
                                    insertar.put("prcvisitas", prcvisitas)
                                    insertar.put("lom_montovtas", lom_montovtas)
                                    insertar.put("lom_prcvtas", lom_prcvtas)
                                    insertar.put("lom_prcvisit", lom_prcvisit)
                                    insertar.put("rlom_montovtas", rlom_montovtas)
                                    insertar.put("rlom_prcvtas", rlom_prcvtas)
                                    insertar.put("rlom_prcvisit", rlom_prcvisit)
                                    insertar.put("fecha_estad", fecha_estad)
                                    insertar.put("ppgdol_totneto", ppgdol_totneto)
                                    insertar.put("devdol_totneto", devdol_totneto)
                                    insertar.put("defdol_totneto", defdol_totneto)
                                    insertar.put("totdolcob", totdolcob)
                                    keAndroid.insert("ke_estadc01", null, insertar)
                                    keAndroid.setTransactionSuccessful()
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
                                } finally {
                                    keAndroid.endTransaction()
                                }
                            }
                        } //aca cierra el for
                    } else { // aqui cierran las filas
                        var jsonObject: JSONObject //creamos un objeto json vacio
                        for (i in 0 until response.length()) {
                            try {
                                keAndroid.beginTransaction()
                                jsonObject = response.getJSONObject(i)
                                codcoord = jsonObject.getString("codcoord").trim { it <= ' ' }
                                nomcoord = jsonObject.getString("nomcoord").trim { it <= ' ' }
                                vendedor = jsonObject.getString("vendedor").trim { it <= ' ' }
                                nombrevend = jsonObject.getString("nombrevend").trim { it <= ' ' }
                                cntpedidos = jsonObject.getDouble("cntpedidos")
                                mtopedidos = jsonObject.getDouble("mtopedidos")
                                cntfacturas = jsonObject.getDouble("cntfacturas")
                                mtofacturas = jsonObject.getDouble("mtofacturas")
                                metavend = jsonObject.getDouble("metavend")
                                prcmeta = jsonObject.getDouble("prcmeta")
                                cntclientes = jsonObject.getDouble("cntclientes")
                                clivisit = jsonObject.getDouble("clivisit")
                                prcvisitas = jsonObject.getDouble("prcvisitas")
                                lom_montovtas = jsonObject.getDouble("lom_montovtas")
                                lom_prcvtas = jsonObject.getDouble("lom_prcvtas")
                                lom_prcvisit = jsonObject.getDouble("lom_prcvisit")
                                rlom_montovtas = jsonObject.getDouble("rlom_montovtas")
                                rlom_prcvtas = jsonObject.getDouble("rlom_prcvtas")
                                rlom_prcvisit = jsonObject.getDouble("rlom_prcvisit")
                                fecha_estad = jsonObject.getString("fecha_estad")
                                ppgdol_totneto = jsonObject.getString("ppgdol_totneto")
                                devdol_totneto = jsonObject.getString("devdol_totneto")
                                defdol_totneto = jsonObject.getString("defdol_totneto")
                                totdolcob = jsonObject.getString("totdolcob")
                                val insertar = ContentValues()
                                insertar.put("codcoord", codcoord)
                                insertar.put("nomcoord", nomcoord)
                                insertar.put("vendedor", vendedor)
                                insertar.put("nombrevend", nombrevend)
                                insertar.put("cntpedidos", cntpedidos)
                                insertar.put("mtopedidos", mtopedidos)
                                insertar.put("cntfacturas", cntfacturas)
                                insertar.put("mtofacturas", mtofacturas)
                                insertar.put("metavend", metavend)
                                insertar.put("prcmeta", prcmeta)
                                insertar.put("cntclientes", cntclientes)
                                insertar.put("clivisit", clivisit)
                                insertar.put("prcvisitas", prcvisitas)
                                insertar.put("lom_montovtas", lom_montovtas)
                                insertar.put("lom_prcvtas", lom_prcvtas)
                                insertar.put("lom_prcvisit", lom_prcvisit)
                                insertar.put("rlom_montovtas", rlom_montovtas)
                                insertar.put("rlom_prcvtas", rlom_prcvtas)
                                insertar.put("rlom_prcvisit", rlom_prcvisit)
                                insertar.put("fecha_estad", fecha_estad)
                                insertar.put("ppgdol_totneto", ppgdol_totneto)
                                insertar.put("devdol_totneto", devdol_totneto)
                                insertar.put("defdol_totneto", defdol_totneto)
                                insertar.put("totdolcob", totdolcob)
                                keAndroid.insert("ke_estadc01", null, insertar)
                                keAndroid.setTransactionSuccessful()
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
                            } finally {
                                keAndroid.endTransaction()
                            }
                        }
                        keAndroid.close()
                    }
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
            }) {
            override fun getParams(): Map<String, String> {
                return HashMap()
            }
        }
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(jsonArrayRequest) //esto es el request que se envia al url a traves de la conexion volley, (el stringrequest esta armado arriba
    }

    private fun actualizarLista() {
        consultarVendedores(campo, cod_usuario)
        vendedoresAdapter = VendedoresAdapter(this@EstadisticasActivity, listadeestadisticas)
        listavendedores!!.adapter = vendedoresAdapter
        vendedoresAdapter!!.notifyDataSetChanged()
    }

    private fun validarTipodeUsuario(codUsuario: String?) {
        var tipodeUsuario = ""
        val keAndroid = conn!!.writableDatabase
        val cursorusu = keAndroid.rawQuery(
            "SELECT superves FROM usuarios WHERE vendedor ='$codUsuario'",
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

    //el metodo para consultar los vendedores segun el coordinador
    private fun consultarVendedores(campo: String?, codUsuario: String?) {
        val keAndroid = conn!!.writableDatabase
        var estadistica: Estadistica
        listadeestadisticas = ArrayList()
        val cursor = keAndroid.rawQuery(
            "SELECT vendedor, nombrevend, prcmeta, fecha_estad FROM ke_estadc01 WHERE $campo= '$codUsuario' ORDER BY prcmeta desc",
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
        var cod_usuario: String? = null
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