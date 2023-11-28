package com.appcloos.mimaletin

import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.database.Cursor
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.appcloos.mimaletin.databinding.ActivitySincronizacionBinding
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.ActivityResult
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.Serializable
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.system.exitProcess

class SincronizacionActivity : AppCompatActivity(), Serializable {
    private var conn: AdminSQLiteOpenHelper? = null
    private var listapedido: ArrayList<Pedidos>? = null
    private var listalineas: ArrayList<Carrito>? = null
    private var appUpdateManager = AppUpdateManagerFactory.create(this)
    private var progressDialog: ProgressDialog? = null
    private var SINCRONIZO = false
    private var DESACTIVADO = false

    private lateinit var binding: ActivitySincronizacionBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySincronizacionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED //mantener la activity en vertical
        val preferences = getSharedPreferences("Preferences", MODE_PRIVATE)
        cod_usuario = preferences.getString("cod_usuario", null)
        //String nombre_usuario = preferences.getString("nombre_usuario", null);
        //nivelUsuario = preferences.getString("superves", "0");
        conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null, 12)
        val keAndroid = conn!!.writableDatabase
        cargarEnlace()
        checkForAppUpdate()
        setColors()
        val cursorsupervisor = keAndroid.rawQuery(
            "SELECT superves FROM usuarios WHERE vendedor ='$cod_usuario'",
            null
        )
        while (cursorsupervisor.moveToNext()) {
            nivelUsuario = cursorsupervisor.getString(0)
        }
        cursorsupervisor.close()
        binding.btnSync.setOnClickListener {
            checkForAppUpdate()
            varAuxError = false
            varAux = 0
            binding.tvAviso.textSize = 12f
            binding.tvAviso.typeface = Typeface.DEFAULT_BOLD
            binding.tvAviso.text =
                "Por favor, espere mientras los datos sincronizan. No abandone esta pantalla hasta que finalice el proceso"
            binding.tvAviso.setTextColor(Color.rgb(4, 98, 193))
            getFechas()

            //estoy aplicando la misma sincronización para el coordinador que para el vendedor, por el tema del progressdialog.
            if (nivelUsuario == "1") {
                //sincronizacion del vendedor
                progressDialog = ProgressDialog(this, setProgressDialogTheme(Constantes.AGENCIA))
                progressDialog!!.max = 100
                progressDialog!!.setMessage("Descargando datos...")
                progressDialog!!.setTitle("Sincronización en proceso")
                progressDialog!!.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
                progressDialog!!.show()
                Thread {
                    try {
                        while (progressDialog!!.progress <= progressDialog!!.max) {
                            Thread.sleep(200)
                        }
                    } catch (exception: Exception) {
                        exception.printStackTrace()
                    }
                }.start()
                sincronizacionVendedor()
            } else if (nivelUsuario == "0") {
                //sincronizacion del vendedor
                progressDialog = ProgressDialog(this, setProgressDialogTheme(Constantes.AGENCIA))
                progressDialog!!.max = 100
                progressDialog!!.setMessage("Descargando datos...")
                progressDialog!!.setTitle("Sincronización en proceso")
                progressDialog!!.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
                progressDialog!!.show()
                Thread {
                    try {
                        while (progressDialog!!.progress <= progressDialog!!.max) {
                            Thread.sleep(200)
                        }
                    } catch (exception: Exception) {
                        exception.printStackTrace()
                    }
                }.start()
                sincronizacionVendedor()
            }
        }
        binding.btnSubir.setOnClickListener {
            checkForAppUpdate()
            subirPedidos()
            cargarRecibo()
        }
        binding.btnSubirprecob.setOnClickListener {
            checkForAppUpdate()
            subirPrecob()
        }

        //bt_subirprecob.setVisibility(View.INVISIBLE);
        val objetoAux = ObjetoAux(this)
        objetoAux.descargaDesactivo(cod_usuario!!)
        SINCRONIZO = conn!!.sincronizoPriVez(cod_usuario!!)
        DESACTIVADO = conn!!.getCampoInt("usuarios", "desactivo", "vendedor", cod_usuario!!) == 0
        if (!conn!!.getConfigBoolUsuario(
                "APP_MODULO_CXC_USER",
                cod_usuario!!
            ) && SINCRONIZO && DESACTIVADO
        ) {
            binding.btnSubirprecob.setVisibility(View.VISIBLE)
        }
    }

    //IMPORTANTE hay 2 funciones de AnalisisError debido a que vollie trabaja de forma asincrona y en ocasiones actualiza primero la fecha con un error antes de realizar todos los procesos
    //Funcion que evalua la variable auxiliar de error
    //En caso de ser False (No tener error), el textView de aviso indicara que todo salio en orden
    //En caso de ser True (Hay error), el textView de aviso indicara que hay un error y ademas de atualizara la fecha de ultima sinroonizacion a una fecha 0 para no aceder
    //a ciertos modulos como catalogo y pedidos
    private fun analisisError() {
        println("La variable -> $varAuxError")
        if (varAuxError) {
            val keAndroid = conn!!.writableDatabase
            try {
                val contenedor = ContentValues()
                contenedor.put("ult_sinc", "0001-01-01")
                keAndroid.beginTransaction()
                keAndroid.update("usuarios", contenedor, "vendedor = ?", arrayOf(cod_usuario))
                //ke_android.rawQuery("UPDATE usuarios SET ult_sinc = '0001-01-01' WHERE vendedor = '" + cod_usuario + "'", null);
                //System.out.println("LLEGUE " + varAuxError);
                //System.out.println("UPDATE usuarios SET ult_sinc = '0001-01-01' WHERE vendedor = '" + cod_usuario + "'");
                keAndroid.setTransactionSuccessful()
            } catch (exception: Exception) {
                exception.printStackTrace()
                //System.out.println("NO LLEGUE");
            } finally {
                keAndroid.endTransaction()
                //ke_android.close();
            }
            binding.tvAviso.setTextColor(Color.rgb(232, 17, 35))
            binding.tvAviso.textSize = 14f
            binding.tvAviso.typeface = Typeface.DEFAULT_BOLD
            binding.tvAviso.text =
                "Se han detectado inconvenientes en la conexión, Por favor sincronice nuevamente"
            Toast.makeText(
                this,
                "Por favor sincronice nuevamente , se detectaron errores en la conexión",
                Toast.LENGTH_LONG
            ).show()
        } else {
            binding.tvAviso.setTextColor(Color.rgb(62, 197, 58))
            binding.tvAviso.textSize = 15f
            binding.tvAviso.typeface = Typeface.DEFAULT_BOLD
            binding.tvAviso.text = "Parametros al día"
            progressDialog!!.incrementProgressBy(100)
            Toast.makeText(this, "Parametros al día", Toast.LENGTH_LONG).show()
        }
        varAux++
    }

    //Segunda funcion que actua en todas las oportunidades de error de los llamados de la API
    //Su utilidad radica en la actualizacio de la ultima sincronizacion en caso de error con fecha 0
    private fun analisisError2() {

        //System.out.println("AAAAAAAAAAAAAAAAAAAAAA");
        val keAndroid = conn!!.writableDatabase
        try {
            val contenedor = ContentValues()
            contenedor.put("ult_sinc", "0001-01-01")
            keAndroid.beginTransaction()
            keAndroid.update("usuarios", contenedor, "vendedor = ?", arrayOf(cod_usuario))
            //ke_android.rawQuery("UPDATE usuarios SET ult_sinc = '0001-01-01' WHERE vendedor = '" + cod_usuario + "'", null);
            //System.out.println("LLEGUE " + varAuxError);
            //System.out.println("UPDATE usuarios SET ult_sinc = '0001-01-01' WHERE vendedor = '" + cod_usuario + "'");
            keAndroid.setTransactionSuccessful()
        } catch (exception: Exception) {
            exception.printStackTrace()
            //System.out.println("NO LLEGUE");
        } finally {
            keAndroid.endTransaction()
            //ke_android.close();
        }
        binding.tvAviso.setTextColor(Color.rgb(232, 17, 35))
        binding.tvAviso.textSize = 14f
        binding.tvAviso.typeface = Typeface.DEFAULT_BOLD
        binding.tvAviso.text =
            "Se han detectado inconvenientes en la conexión\nPor favor sincronice nuevamente"
        progressDialog!!.incrementProgressBy(100)

        //Toast.makeText(this, "Por favor sincronice nuevamente , se detectaron errores en la conexión", LENGTH_LONG).show();
        varAux++
    }

    private fun cargarEnlace() {
        val keAndroid = conn!!.writableDatabase
        val columnas = arrayOf("kee_nombre," + "kee_url," + "kee_sucursal")
        val cursor = keAndroid.query("ke_enlace", columnas, "1", null, null, null, null)
        while (cursor.moveToNext()) {
            nombreEmpresa = cursor.getString(0)
            enlaceEmpresa = cursor.getString(1)
            codigoSucursal = cursor.getString(2)
        }
        cursor.close()
    }

    private fun cargarRecibo() {
        conn = AdminSQLiteOpenHelper(this@SincronizacionActivity, "ke_android", null, 10)
        val keAndroid = conn!!.writableDatabase
        val cursor = keAndroid.rawQuery(
            "SELECT kcx_nrorecibo, kcx_codcli, kcx_codven, kcx_fechamodifi, kcx_monto FROM ke_cxc WHERE kcx_status = '0'",
            null
        )
        arrayRec = JSONArray()
        while (cursor.moveToNext()) {
            val objetoRecibo = JSONObject()
            try {
                val kcxNrorecibo = cursor.getString(0)
                val kcxCodcli = cursor.getString(1)
                val kcxCodven = cursor.getString(2)
                val kcxFechamodifi = cursor.getString(3)
                val kcxMonto = cursor.getInt(4)
                //char kcx_status = "1";
                objetoRecibo.put("kcx_nrorecibo", kcxNrorecibo)
                objetoRecibo.put("kcx_codcli", kcxCodcli)
                objetoRecibo.put("kcx_codven", kcxCodven)
                objetoRecibo.put("kcx_fechamodifi", kcxFechamodifi)
                objetoRecibo.put("kcx_monto", kcxMonto)
                //objetoRecibo.put("kcx_status", '1');
                arrayRec!!.put(objetoRecibo)
            } catch (e: JSONException) {
                e.printStackTrace()
                Toast.makeText(
                    this@SincronizacionActivity,
                    "Error al cargar el recibo$e",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        cursor.close()
        val jsonObject = JSONObject()
        try {
            jsonObject.put("Recibo", arrayRec)
        } catch (e: JSONException) {
            e.printStackTrace()
            // Toast.makeText(SincronizacionActivity.this, "Error al cargar el recibo" + e, Toast.LENGTH_SHORT).show();
        }
        val jsonStrREC = jsonObject.toString()
        try {
            insertarRecibo(jsonStrREC)
        } catch (exc: Exception) {
            exc.printStackTrace()
            // Toast.makeText(SincronizacionActivity.this, "Error al cargar el recibo" + exc, Toast.LENGTH_SHORT).show();
        }
    }

    private fun insertarRecibo(jsonrec: String) {
        val requestQueue = Volley.newRequestQueue(this@SincronizacionActivity)
        val stringRequest: StringRequest = object : StringRequest(
            Method.POST,
            "https://$enlaceEmpresa/$ambienteJob/Recibos_2.php",
            Response.Listener { response: String ->
                if (response.trim { it <= ' ' } == "OK") {
                    Toast.makeText(
                        this@SincronizacionActivity,
                        "Recibo(s) Subido",
                        Toast.LENGTH_LONG
                    ).show()
                    cambiarEstadoRecibo()
                }
            },
            Response.ErrorListener { error: VolleyError ->
                println("--Error-")
                error.printStackTrace()
                println("--Error-")
            }) {
            override fun getParams(): Map<String, String>? {
                val params: MutableMap<String, String> = HashMap()
                params["jsonrec"] = jsonrec
                params["agencia"] = codigoSucursal
                return params
            }
        }
        requestQueue.add(stringRequest)
    }

    private fun cambiarEstadoRecibo() {
        val keAndroid = conn!!.writableDatabase
        //System.out.println(arrayRec);
        for (i in 0 until arrayRec!!.length()) {
            try {
                val objetodeRecibo = arrayRec!!.getJSONObject(i)
                val codigoDelReciboEnArray = objetodeRecibo.getString("kcx_nrorecibo")
                keAndroid.execSQL("UPDATE ke_cxc SET kcx_status = '1' WHERE kcx_nrorecibo = '$codigoDelReciboEnArray'")
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }

    fun sincronizacionVendedor() {
        when (varAux) {
            0 -> bajarUsuario("https://" + enlaceEmpresa + "/" + ambienteJob + "/usuarios_V3.php?cod_usuario=" + cod_usuario!!.trim { it <= ' ' } + "&&agencia=" + codigoSucursal.trim { it <= ' ' })
            1 -> bajarVendedor("https://" + enlaceEmpresa + "/" + ambienteJob + "/listvend_V2.php?cod_usuario=" + cod_usuario!!.trim { it <= ' ' } + "&&agencia=" + codigoSucursal.trim { it <= ' ' })
            2 -> bajarGrupos("https://" + enlaceEmpresa + "/" + ambienteJob + "/grupos_V3.php?fecha_sinc=" + fecha_sinc_grupos!!.trim { it <= ' ' } + "&&agencia=" + codigoSucursal.trim { it <= ' ' })
            3 -> bajarConfig("https://" + enlaceEmpresa + "/" + ambienteJob + "/config_V3.php?agencia=" + codigoSucursal.trim { it <= ' ' })
            4 -> bajarSubGrupos("https://" + enlaceEmpresa + "/" + ambienteJob + "/subgrupos_V2.php?fecha_sinc=" + fecha_sinc_subgrupos!!.trim { it <= ' ' } + "&&agencia=" + codigoSucursal.trim { it <= ' ' })
            5 -> bajarSectores("https://" + enlaceEmpresa + "/" + ambienteJob + "/sectores_V2.php?fecha_sinc=" + fecha_sinc_sectores!!.trim { it <= ' ' } + "&&agencia=" + codigoSucursal.trim { it <= ' ' })
            6 -> bajarSubSectores("https://" + enlaceEmpresa + "/" + ambienteJob + "/subsectores_V2.php?fecha_sinc=" + fecha_sinc_subsectores + "&&agencia=" + codigoSucursal.trim { it <= ' ' })
            7 -> bajarClientes("https://" + enlaceEmpresa + "/" + ambienteJob + "/clientes_V4.php?cod_usuario=" + cod_usuario!!.trim { it <= ' ' } + "&&agencia=" + codigoSucursal.trim { it <= ' ' })
            8 -> bajarInfoPedidos("https://" + enlaceEmpresa + "/" + ambienteJob + "/obtenerdatospedidos_V3.php?cod_usuario=" + cod_usuario!!.trim { it <= ' ' } + "&&agencia=" + codigoSucursal.trim { it <= ' ' })
            9 -> bajarArticulos3("https://" + enlaceEmpresa + "/" + ambienteJob + "/articulos_V26.php?fecha_sinc=" + fecha_sinc_articulo!!.trim { it <= ' ' } + "&&agencia=" + codigoSucursal.trim { it <= ' ' })
            10 -> bajarKardex("https://" + enlaceEmpresa + "/" + ambienteJob + "/kardex_V2.php?fecha_sinc=" + fecha_sinc_articulo!!.trim { it <= ' ' } + "&&agencia=" + codigoSucursal.trim { it <= ' ' })
            11 -> subirLimite()
            12 -> actualizarLimites("https://" + enlaceEmpresa + "/" + ambienteJob + "/obtenerlimites_V3.php?cod_usuario=" + cod_usuario!!.trim { it <= ' ' } + "&&agencia=" + codigoSucursal.trim { it <= ' ' })
            13 -> bajarDocumentos("https://" + enlaceEmpresa + "/" + ambienteJob + "/planificador_V3.php?vendedor=" + cod_usuario!!.trim { it <= ' ' })
            14 -> bajarDatosExtra("https://" + enlaceEmpresa + "/" + ambienteJob + "/descarga_referencias.php?vendedor=" + cod_usuario!!.trim { it <= ' ' })
            15 ->  //BajarConfigExtra("https://" + enlaceEmpresa + "/" + ambienteJob + "/config_gen2.php?vendedor=" + cod_usuario.trim() + "&fecha_sinc=" + fechaSincronizar("ke_wcnf_conf"));
                bajarConfigExtra("https://" + enlaceEmpresa + "/" + ambienteJob + "/config_gen2.php?vendedor=" + cod_usuario!!.trim { it <= ' ' } + "&fecha_sinc=" + fechaSincronizar(
                    "ke_wcnf_conf"
                ))

            16 -> bajarBancos(
                "https://$enlaceEmpresa/webservice/bancos_V2.php?fecha_sinc=" + getFecha(
                    "listbanc"
                ) + "&&agencia=" + codigoSucursal.trim { it <= ' ' })

            17 -> bajarPromociones("https://$enlaceEmpresa/webservice/promociones_V2.php")
            18 -> bajarDescuentos("https://$enlaceEmpresa/webservice/descuentos.php" /*+"?fechamodifi=" + GetFecha("ke_tabdctos")*/)
            19 -> bajarDescuentosBancos("https://$enlaceEmpresa/webservice/descuento_bancos.php" /*+"fechamodifi=" + GetFecha("ke_tabdctosbcos")*/)
            20 -> {
                //IF que valida si hasta el momento no hay errores en la sincronizacion, en caso de haber no enviara la ultima sincronizacion
                if (!varAuxError) {
                    subirSincronizacion()
                }
            }

            21 ->  //Funcion que indica si el proceso de sincronizacion se hizo adecuadamente
                analisisError()
        }
    }

    private fun bajarDescuentosBancos(url: String) {
        println(url)
        val keAndroid = conn!!.writableDatabase
        val descuentoBancoNube = ArrayList<String>()
        val descuentoBancoNube2 = ArrayList<String>()
        val jsonObjectRequest =
            JsonObjectRequest(Request.Method.GET, url, null, { response: JSONObject ->
                try {
                    if (response.getString("status") == "0") {
                        val descuentoBancos = response.getJSONArray("descuento_bancos")
                        for (i in 0 until descuentoBancos.length()) {
                            val descuentoBanco = descuentoBancos.getJSONObject(i)
                            val dcobId = descuentoBanco.getString("dcob_id")
                            val bcoCodigo = descuentoBanco.getString("bco_codigo")
                            val fechamodifi = descuentoBanco.getString("fechamodifi")
                            descuentoBancoNube.add(dcobId)
                            descuentoBancoNube2.add(bcoCodigo)
                            val cv = ContentValues()
                            cv.put("dcob_id", dcobId)
                            cv.put("bco_codigo", bcoCodigo)
                            cv.put("fechamodifi", fechamodifi)
                            if (conn!!.validarExistenciaCamposVarios(
                                    "ke_tabdctosbcos", ArrayList(
                                        mutableListOf("dcob_id", "bco_codigo")
                                    ), ArrayList(listOf(dcobId, bcoCodigo))
                                )
                            ) {
                                //System.out.println("UPDATE " + documento);
                                conn!!.updateJSONCamposVarios(
                                    "ke_tabdctosbcos",
                                    cv,
                                    "dcob_id = ? AND bco_codigo = ?",
                                    arrayOf(dcobId, bcoCodigo)
                                )
                            } else {
                                //System.out.println("INSERT " + documento);
                                conn!!.insertJSON("ke_tabdctosbcos", cv)
                            }
                        }
                        conn!!.updateTablaAux("ke_tabdctosbcos")
                    }
                    eliminarRegistrosViejos(
                        descuentoBancoNube,
                        descuentoBancoNube2,
                        keAndroid,
                        "ke_tabdctosbcos",
                        "dcob_id",
                        "bco_codigo"
                    )
                    varAux++
                    sincronizacionVendedor()
                } catch (e: Exception) {
                    println("--Error--")
                    e.printStackTrace()
                    println("--Error--")
                    varAux++
                    varAuxError = true
                    analisisError2()
                    //-----
                    sincronizacionVendedor()
                }
            }) { error: VolleyError ->
                println("--Error--")
                error.printStackTrace()
                println("--Error--")
                varAux++
                varAuxError = true
                analisisError2()
                //-----
                sincronizacionVendedor()
            }
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(jsonObjectRequest)
    }

    private fun bajarDescuentos(url: String) {
        println(url)
        val keAndroid = conn!!.writableDatabase
        val descuentoNube = ArrayList<String?>()
        val jsonObjectRequest =
            JsonObjectRequest(Request.Method.GET, url, null, { response: JSONObject ->
                try {
                    if (response.getString("status") == "0") {
                        val descuentos = response.getJSONArray("descuentos")
                        for (i in 0 until descuentos.length()) {
                            val descuento = descuentos.getJSONObject(i)
                            val dcobId = descuento.getString("dcob_id")
                            val dcobPrc = descuento.getString("dcob_prc")
                            val dcobActivo = descuento.getString("dcob_activo")
                            val dcobMaxdvenc = descuento.getString("dcob_maxdvenc")
                            val dcobValefac = descuento.getString("dcob_valefac")
                            val dcobValene = descuento.getString("dcob_valene")
                            val dcobFchini = descuento.getString("dcob_fchini")
                            val dcobFchfin = descuento.getString("dcob_fchfin")
                            val fechamodifi = descuento.getString("fechamodifi")
                            val dcobValesiempre = descuento.getString("dcob_valesiempre")
                            val dcobValemon = descuento.getString("dcob_valemon")
                            descuentoNube.add(dcobId)
                            val cv = ContentValues()
                            cv.put("dcob_id", dcobId)
                            cv.put("dcob_prc", dcobPrc)
                            cv.put("dcob_activo", dcobActivo)
                            cv.put("dcob_maxdvenc", dcobMaxdvenc)
                            cv.put("dcob_valefac", dcobValefac)
                            cv.put("dcob_valene", dcobValene)
                            cv.put("dcob_fchini", dcobFchini)
                            cv.put("dcob_fchfin", dcobFchfin)
                            cv.put("fechamodifi", fechamodifi)
                            cv.put("dcob_valesiempre", dcobValesiempre)
                            cv.put("dcob_valemon", dcobValemon)
                            if (conn!!.validarExistencia("ke_tabdctos", "dcob_id", dcobId)) {
                                //System.out.println("UPDATE " + documento);
                                conn!!.updateJSON("ke_tabdctos", cv, "dcob_id", dcobId)
                            } else {
                                //System.out.println("INSERT " + documento);
                                conn!!.insertJSON("ke_tabdctos", cv)
                            }
                        }
                        conn!!.updateTablaAux("ke_tabdctos")
                    }
                    eliminarDocViejos(descuentoNube, keAndroid, "ke_tabdctos", "dcob_id")
                    varAux++
                    sincronizacionVendedor()
                } catch (e: Exception) {
                    println("--Error--")
                    e.printStackTrace()
                    println("--Error--")
                    varAux++
                    varAuxError = true
                    analisisError2()
                    //-----
                    sincronizacionVendedor()
                }
            }) { error: VolleyError ->
                println("--Error--")
                error.printStackTrace()
                println("--Error--")
                varAux++
                varAuxError = true
                analisisError2()
                //-----
                sincronizacionVendedor()
            }
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(jsonObjectRequest)
    }

    private fun bajarPromociones(url: String) {
        println(url)
        val keAndroid = conn!!.writableDatabase
        val imgNube = ArrayList<String?>()
        val jsonObjectRequest =
            JsonObjectRequest(Request.Method.GET, url, null, { response: JSONObject ->
                try {
                    if (response.getString("status") == "0") {
                        val imgs = response.getJSONArray("imgs")
                        for (i in 0 until imgs.length()) {
                            val img = imgs.getJSONObject(i)
                            val nombre = img.getString("nombre")
                            val enlace = img.getString("enlace")
                            val fechamodifi = img.getString("fechamodifi")
                            val ancho = img.getString("ancho")
                            val alto = img.getString("alto")
                            imgNube.add(nombre)
                            val cv = ContentValues()
                            cv.put("nombre", nombre)
                            cv.put("enlace", enlace)
                            cv.put("fechamodifi", fechamodifi)
                            cv.put("ancho", ancho)
                            cv.put("alto", alto)
                            if (conn!!.validarExistencia("img_carousel", "nombre", nombre)) {
                                //System.out.println("UPDATE " + documento);
                                conn!!.updateJSON("img_carousel", cv, "nombre= ?", nombre)
                            } else {
                                //System.out.println("INSERT " + documento);
                                conn!!.insertJSON("img_carousel", cv)
                            }
                        }
                        varAux++
                        sincronizacionVendedor()
                    } else {
                        varAux++
                        sincronizacionVendedor()
                    }
                    eliminarDocViejos(imgNube, keAndroid, "img_carousel", "nombre")
                } catch (e: Exception) {
                    println("--Error--")
                    e.printStackTrace()
                    println("--Error--")
                    varAux++
                    varAuxError = true
                    analisisError2()
                    //-----
                    sincronizacionVendedor()
                }
            }) { error: VolleyError ->
                println("--Error--")
                error.printStackTrace()
                println("--Error--")
                varAux++
                varAuxError = true
                analisisError2()
                //-----
                sincronizacionVendedor()
            }
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(jsonObjectRequest)
    }

    private fun bajarBancos(url: String) {
        val keAndroid = conn!!.writableDatabase
        val bancosNube = ArrayList<String?>()
        println(url)
        val jsonObjectRequest =
            JsonObjectRequest(Request.Method.GET, url, null, { response: JSONObject ->
                try {
                    if (response.getString("status") == "0") {
                        val bancos = response.getJSONArray("banco")
                        for (i in 0 until bancos.length()) {
                            val banco = bancos.getJSONObject(i)
                            val cuentanac = banco.getString("cuentanac")
                            val fechamodifi = banco.getString("fechamodifi")
                            val nombanco = banco.getString("nombanco")
                            val codbanco = banco.getString("codbanco")
                            val inactiva = banco.getString("inactiva")
                            bancosNube.add(codbanco)
                            val cv = ContentValues()
                            cv.put("cuentanac", cuentanac)
                            cv.put("fechamodifi", fechamodifi)
                            cv.put("nombanco", nombanco)
                            cv.put("codbanco", codbanco)
                            cv.put("inactiva", inactiva)
                            val qcodigoLocal = keAndroid.rawQuery(
                                "SELECT count(codbanco) FROM listbanc WHERE codbanco = '$codbanco';",
                                null
                            )
                            var codigoExistente = 0
                            if (qcodigoLocal.moveToFirst()) {
                                codigoExistente = qcodigoLocal.getInt(0)
                            }
                            qcodigoLocal.close()
                            if (codigoExistente > 0) {
                                //System.out.println("UPDATE " + documento);
                                keAndroid.update("listbanc", cv, "codbanco= ?", arrayOf(codbanco))
                            } else if (codigoExistente == 0) {
                                //System.out.println("INSERT " + documento);
                                keAndroid.insert("listbanc", null, cv)
                            }
                        }
                        keAndroid.delete("listbanc", "inactiva = ?", arrayOf("1"))
                        eliminarDocViejos(bancosNube, keAndroid, "listbanc", "codbanco")
                        try {
                            val fecha_limites = Calendar.getInstance()
                            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                            val fechaLimites = sdf.format(fecha_limites.time)
                            val cv = ContentValues()
                            cv.put("fchhn_ultmod", fechaLimites)
                            keAndroid.update("tabla_aux", cv, "tabla= ?", arrayOf("listbanc"))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        varAux++
                        sincronizacionVendedor()
                    } else {
                        varAux++
                        sincronizacionVendedor()
                    }
                } catch (e: Exception) {
                    println("--Error--")
                    e.printStackTrace()
                    println("--Error--")
                    varAux++
                    varAuxError = true
                    analisisError2()
                    //-----
                    sincronizacionVendedor()
                }
            }) { error: VolleyError ->
                println("--Error--")
                error.printStackTrace()
                println("--Error--")
                varAux++
                varAuxError = true
                analisisError2()
                //-----
                sincronizacionVendedor()
            }
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(jsonObjectRequest)
    }

    private fun bajarConfigExtra(url: String) {
        val keAndroid = conn!!.writableDatabase
        println("Config -->$url")

        //Fecha tomada para ser coloada en tabla auxiliar en caso de dar un error
        //String fecha_error = ObtenerFechaPreError("fchhn_ultmod");
        val ConfigNube = ArrayList<String?>()
        val jsonObjectRequest =
            JsonObjectRequest(Request.Method.GET, url, null, { response: JSONObject ->
                try {
                    if (response.getString("status") == "0") {
                        val config = response.getJSONArray("config")
                        for (i in 0 until config.length()) {
                            val configDatos = config.getJSONObject(i)
                            val cnfgValtxt = configDatos.getString("cnfg_valtxt")
                            val cnfgTtip = configDatos.getString("cnfg_ttip")
                            val cnfgValsino = configDatos.getString("cnfg_valsino")
                            val cnfgValnum = configDatos.getString("cnfg_valnum")
                            val cnfgLentxt = configDatos.getString("cnfg_lentxt")
                            val fechamodifi = configDatos.getString("fechamodifi")
                            val cnfgActiva = configDatos.getString("cnfg_activa")
                            val cnfgIdconfig = configDatos.getString("cnfg_idconfig")
                            val cnfgEtiq = configDatos.getString("cnfg_etiq")
                            val cnfgClase = configDatos.getString("cnfg_clase")
                            val cnfgTipo = configDatos.getString("cnfg_tipo")
                            val cnfgValfch = configDatos.getString("cnfg_valfch")
                            val username = configDatos.getString("username")
                            ConfigNube.add(cnfgIdconfig)
                            val cv = ContentValues()
                            cv.put("cnfg_valtxt", cnfgValtxt)
                            cv.put("cnfg_ttip", cnfgTtip)
                            cv.put("cnfg_valsino", cnfgValsino)
                            cv.put("cnfg_valnum", cnfgValnum)
                            cv.put("cnfg_lentxt", cnfgLentxt)
                            cv.put("fechamodifi", fechamodifi)
                            cv.put("cnfg_activa", cnfgActiva)
                            cv.put("cnfg_idconfig", cnfgIdconfig)
                            cv.put("cnfg_etiq", cnfgEtiq)
                            cv.put("cnfg_clase", cnfgClase)
                            cv.put("cnfg_tipo", cnfgTipo)
                            cv.put("cnfg_valfch", cnfgValfch)
                            cv.put("username", username)
                            val qcodigoLocal = keAndroid.rawQuery(
                                "SELECT count(cnfg_valtxt) FROM ke_wcnf_conf WHERE cnfg_idconfig = '$cnfgIdconfig';",
                                null
                            )
                            var codigoExistente = 0
                            if (qcodigoLocal.moveToFirst()) {
                                codigoExistente = qcodigoLocal.getInt(0)
                            }
                            qcodigoLocal.close()
                            if (codigoExistente > 0) {
                                //System.out.println("UPDATE " + documento);
                                keAndroid.update(
                                    "ke_wcnf_conf",
                                    cv,
                                    "cnfg_idconfig= ?",
                                    arrayOf(cnfgIdconfig)
                                )
                            } else if (codigoExistente == 0) {
                                //System.out.println("INSERT " + documento);
                                keAndroid.insert("ke_wcnf_conf", null, cv)
                            }
                        }
                        try {
                            val fechaLimites1 = Calendar.getInstance()
                            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                            val fechaLimites = sdf.format(fechaLimites1.time)
                            val cv = ContentValues()
                            cv.put("fchhn_ultmod", fechaLimites)
                            keAndroid.update("tabla_aux", cv, "tabla= ?", arrayOf("ke_wcnf_conf"))
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(this, "Ocurrio algo en Config 1", Toast.LENGTH_LONG)
                                .show()
                        }
                    }
                    keAndroid.delete("ke_wcnf_conf", "cnfg_activa= ?", arrayOf("0"))
                    eliminarDocViejos(ConfigNube, keAndroid, "ke_wcnf_conf", "cnfg_idconfig ")
                    varAux++
                    sincronizacionVendedor()
                } catch (e: JSONException) {
                    println("--Error--")
                    e.printStackTrace()
                    println("--Error--")
                    varAux++
                    varAuxError = true
                    analisisError2()
                    //-----
                    sincronizacionVendedor()
                }
            }) { error: VolleyError ->
                println("--Error--")
                error.printStackTrace()
                println("--Error--")
                varAux++
                varAuxError = true
                analisisError2()
                //-----
                sincronizacionVendedor()
            }
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(jsonObjectRequest)
    }

    private fun fechaSincronizar(tabla: String): String {
        var resultado = "0001-01-01T01:01:01"
        val keAndroid = conn!!.writableDatabase
        val fechaUltmod =
            keAndroid.rawQuery("SELECT fchhn_ultmod FROM tabla_aux WHERE tabla = '$tabla';", null)
        if (fechaUltmod.moveToFirst()) {
            resultado = fechaUltmod.getString(0)
        }
        fechaUltmod.close()
        return resultado
    }

    private fun bajarDatosExtra(url: String) {
        //System.out.println("Referencias -> " + URL);
        val refNube = ArrayList<String?>()
        val keAndroid = conn!!.writableDatabase
        val jsonObjectRequest =
            JsonObjectRequest(Request.Method.GET, url, null, { response: JSONObject ->
                try {
                    if (response.getString("referencias") != "null") {
                        var countRef = 0
                        val referencias = response.getJSONArray("referencias")
                        for (i in 0 until referencias.length()) {
                            val jsonObject = referencias.getJSONObject(i)
                            val bcoref = jsonObject.getString("bcoref")
                            val bcocod = jsonObject.getString("bcocod")
                            refNube.add(bcoref)
                            val qReferencias = ContentValues()
                            qReferencias.put("bcoref", bcoref)
                            qReferencias.put("bcocod", bcocod)
                            qReferencias.put("tiporef", "banc")
                            val qcodigoRef = keAndroid.rawQuery(
                                "SELECT count(bcoref) FROM ke_referencias WHERE bcoref = '$bcoref';",
                                null
                            )
                            var refExistente = 0
                            if (qcodigoRef.moveToFirst()) {
                                refExistente = qcodigoRef.getInt(0)
                            }
                            qcodigoRef.close()
                            if (refExistente > 0) {
                                //System.out.println("UPDATE " + bcoref);
                                keAndroid.update(
                                    "ke_referencias",
                                    qReferencias,
                                    "bcoref= ?",
                                    arrayOf(bcoref)
                                )
                            } else if (refExistente == 0) {
                                //System.out.println("INSERT " + bcoref);
                                keAndroid.insert("ke_referencias", null, qReferencias)
                            }
                            countRef++
                        }
                        //progressDialog.setMessage("Referencias:" + countRef);
                        eliminarDocViejos(refNube, keAndroid, "ke_referencias", "bcoref")
                        varAux++
                        sincronizacionVendedor()
                    } else if (response.getString("referencias") == "null") {

                        //progressDialog.setMessage("Referencias: Sin actualización" );
                        varAux++
                        sincronizacionVendedor()
                    }
                } catch (e: Exception) {
                    println("--Error--")
                    e.printStackTrace()
                    println("--Error--")
                    varAux++
                    varAuxError = true
                    analisisError2()
                    //-----
                    sincronizacionVendedor()
                }
            }) { error: VolleyError ->
                println("--Error--")
                error.printStackTrace()
                println("--Error--")
                varAux++
                varAuxError = true
                analisisError2()
                //-----
                sincronizacionVendedor()
            }
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(jsonObjectRequest)
    }

    private fun bajarDocumentos(url: String) {
        println("Documentos -> $url")
        //final ArrayList<String> documentosBDD = arrayDocumento();
        val documentosNube = ArrayList<String?>()
        progressDialog!!.setMessage("Sincronizando Documentos")
        binding.tvDocumentos.setTextColor(Color.rgb(41, 184, 214))
        binding.tvDocumentos.text = "Documentos: Sincronizando"
        //Fecha tomada para ser coloada en tabla auxiliar en caso de dar un error
        //String fecha_error = ObtenerFechaPreError("limites");
        val keAndroid = conn!!.writableDatabase
        val jsonObjectRequest =
            JsonObjectRequest(Request.Method.GET, url, null, { response: JSONObject ->
                try {
                    if (response.getString("documento") != "null") {
                        var countDoc = 0
                        val documentos = response.getJSONArray("documento")
                        for (i in 0 until documentos.length()) {
                            val jsonObject = documentos.getJSONObject(i)


//!documentosBDD.contains(documento)
                            val agencia = jsonObject.getString("agencia")
                            val tipodoc = jsonObject.getString("tipodoc")
                            val codigoCliente = jsonObject.getString("codcliente")
                            val nombreCliente = jsonObject.getString("nombrecli")
                            val documento = jsonObject.getString("documento")
                            val tipodocv = jsonObject.getString("tipodocv")
                            val contribesp = jsonObject.getDouble("contribesp")
                            val rutaParme = jsonObject.getString("ruta_parme")
                            val tipoprecio = jsonObject.getDouble("tipoprecio")
                            val emision = jsonObject.getString("emision")
                            val recepcion = jsonObject.getString("recepcion")
                            val vence = jsonObject.getString("vence")
                            val diascred = jsonObject.getDouble("diascred")
                            val estatusdoc = jsonObject.getString("estatusdoc")
                            val dtotneto = jsonObject.getDouble("dtotneto")
                            val dtotimpuest = jsonObject.getDouble("dtotimpuest")
                            val dtotalfinal = jsonObject.getDouble("dtotalfinal")
                            val dtotpagos = jsonObject.getDouble("dtotpagos")
                            val dtotdescuen = jsonObject.getDouble("dtotdescuen")
                            val dFlete = jsonObject.getDouble("dFlete")
                            val dtotdev = jsonObject.getDouble("dtotdev")
                            val dvndmtototal = jsonObject.getDouble("dvndmtototal")
                            val dretencion = jsonObject.getDouble("dretencion")
                            val dretencioniva = jsonObject.getDouble("dretencioniva")
                            val vendedor = jsonObject.getString("vendedor")
                            val codcoord = jsonObject.getString("codcoord")
                            val fechamodifi = jsonObject.getString("fechamodifi")
                            val aceptadev = jsonObject.getString("aceptadev")
                            val bsiva = jsonObject.getDouble("bsiva")
                            val bsflete = jsonObject.getDouble("bsflete")
                            val bsretencioniva = jsonObject.getDouble("bsretencioniva")
                            val bsretencion = jsonObject.getDouble("bsretencion")
                            val tasadoc = jsonObject.getDouble("tasadoc")
                            val montodcto = jsonObject.getDouble("mtodcto")
                            val fechavencedcto = jsonObject.getString("fchvencedcto")
                            val tienedcto = jsonObject.getString("tienedcto")
                            val cbsret = jsonObject.getDouble("cbsret")
                            val cdret = jsonObject.getDouble("cdret")
                            val cbsretiva = jsonObject.getDouble("cbsretiva")
                            val cdretiva = jsonObject.getDouble("cdretiva")
                            val cbsrparme = jsonObject.getDouble("cbsrparme")
                            val cdrparme = jsonObject.getDouble("cdrparme")
                            val bsmtoiva = jsonObject.getDouble("bsmtoiva")
                            val bsmtofte = jsonObject.getDouble("bsmtofte")
                            val cbsretflete = jsonObject.getDouble("cbsretflete")
                            val cdretflete = jsonObject.getDouble("cdretflete")
                            val retmunMto = jsonObject.getDouble("retmun_mto")
                            val ktiNegesp = jsonObject.getInt("kti_negesp")
                            documentosNube.add(documento)
                            val qDocumentosCab = ContentValues()
                            qDocumentosCab.put("agencia", agencia)
                            qDocumentosCab.put("tipodoc", tipodoc)
                            qDocumentosCab.put("documento", documento)
                            qDocumentosCab.put("tipodocv", tipodocv)
                            qDocumentosCab.put("codcliente", codigoCliente)
                            qDocumentosCab.put("nombrecli", nombreCliente)
                            qDocumentosCab.put("contribesp", contribesp)
                            qDocumentosCab.put("ruta_parme", rutaParme)
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
                            qDocumentosCab.put("retmun_mto", retmunMto)
                            qDocumentosCab.put("kti_negesp", ktiNegesp)
                            qDocumentosCab.put("cdrparme", cdrparme)
                            val qcodigoLocal = keAndroid.rawQuery(
                                "SELECT count(documento) FROM ke_doccti WHERE documento = '$documento';",
                                null
                            )
                            var codigoExistente = 0
                            if (qcodigoLocal.moveToFirst()) {
                                codigoExistente = qcodigoLocal.getInt(0)
                            }
                            qcodigoLocal.close()
                            if (codigoExistente > 0) {
                                //System.out.println("UPDATE " + documento);
                                keAndroid.update(
                                    "ke_doccti",
                                    qDocumentosCab,
                                    "documento= ?",
                                    arrayOf(documento)
                                )
                            } else if (codigoExistente == 0) {
                                //System.out.println("INSERT " + documento);
                                keAndroid.insert("ke_doccti", null, qDocumentosCab)
                            }
                            countDoc++
                        }
                        keAndroid.delete("ke_doccti", "estatusdoc= ?", arrayOf("2"))
                        eliminarDocViejos(documentosNube, keAndroid, "ke_doccti", "documento")
                        binding.tvDocumentos.setTextColor(Color.rgb(62, 197, 58))
                        binding.tvDocumentos.text = "Documentos: $countDoc"
                        progressDialog!!.setMessage("Documentos:$countDoc")
                        varAux++
                        progressDialog!!.incrementProgressBy(numBarraProgreso.toInt())
                        sincronizacionVendedor()
                    } else if (response.getString("documento") == "null") {
                        binding.tvDocumentos.setTextColor(Color.rgb(98, 117, 141))
                        binding.tvDocumentos.text = "Documentos: Sin actualización"
                        progressDialog!!.setMessage("Documentos: Sin actualización")
                        varAux++
                        progressDialog!!.incrementProgressBy(numBarraProgreso.toInt())
                        sincronizacionVendedor()
                    }
                } catch (e: Exception) {
                    //System.out.println("Error Bajar Documento -> " + e);
                    e.printStackTrace()
                }
            }) { error: VolleyError? ->
                error?.printStackTrace()
                //System.out.println("Este es el error -> "+error);
                //Ingreso de la fecha antes de ser actualizada
                //ActualizarFechaError(fecha_error);

                //--Manejo visual que indica al usuario del error--
                binding.tvDocumentos.setTextColor(Color.rgb(232, 17, 35))
                binding.tvDocumentos.text = "Documentos: No ha logrado sincronizar"
                progressDialog!!.setMessage("Documentos: No ha logrado sincronizar")
                varAux++
                progressDialog!!.incrementProgressBy(numBarraProgreso.toInt())
                varAuxError = true
                analisisError2()
                //-----
                sincronizacionVendedor()
            }
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(jsonObjectRequest)
    }

    private fun eliminarDocViejos(
        documentosNube: ArrayList<String?>,
        keAndroid: SQLiteDatabase,
        tabla: String,
        campo: String
    ) {
        val documentosBDD = arrayDocumento(tabla, campo)
        for (i in documentosBDD.indices) {
            if (!documentosNube.contains(documentosBDD[i])) {
                //System.out.println("DELETE " + documentosBDD.get(i));
                keAndroid.delete(tabla, "$campo = ?", arrayOf(documentosBDD[i]))
            }
        }
    }

    private fun arrayDocumento(tabla: String, campo: String): ArrayList<String> {
        val keAndroid = conn!!.writableDatabase
        val documentos = ArrayList<String>()
        val cursor = keAndroid.rawQuery("SELECT $campo FROM $tabla;", null)
        while (cursor.moveToNext()) {
            documentos.add(cursor.getString(0))
        }
        cursor.close()
        return documentos
    }

    private fun eliminarRegistrosViejos(
        documentosNube: ArrayList<String>,
        documentosNube2: ArrayList<String>,
        keAndroid: SQLiteDatabase,
        tabla: String,
        campo: String,
        campo2: String
    ) {
        val documentosBDD = arrayDocumento(tabla, campo)
        val documentosBDD2 = arrayDocumento(tabla, campo2)
        for (i in documentosBDD.indices) {
            if (!documentosNube.contains(documentosBDD[i]) || !documentosNube2.contains(
                    documentosBDD2[i]
                )
            ) {
                //System.out.println("DELETE " + documentosBDD.get(i));
                keAndroid.delete(
                    tabla, "$campo = ? AND $campo2 = ?", arrayOf(
                        documentosBDD[i], documentosBDD2[i]
                    )
                )

                //ke_android.rawQuery("DELETE FROM ke_tabdctosbcos WHERE dcob_id = '" + documentosBDD.get(i) + "' AND bco_codigo = '" + documentosBDD2.get(i) + "';", null);
            }
        }
    }

    private fun actualizarLimites(url: String) {
        //Fecha tomada para ser coloada en tabla auxiliar en caso de dar un error
        val fechaError = obtenerFechaPreError("limites")
        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
            Method.GET,
            url,
            null,
            { response: JSONObject ->
                try {
                    if (response.getString("limites") != "null") {
                        conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null, 8)
                        var keAndroid = conn!!.writableDatabase
                        val filas = DatabaseUtils.queryNumEntries(
                            keAndroid,
                            "ke_limitart"
                        ) //obtenemos las filas de la tabla articulos para comprobar si hay o no registros
                        val limites = response.getJSONArray("limites")
                        if (filas > 0) {
                            var jsonObject: JSONObject? = null //creamos un objeto json vacio
                            for (i in 0 until limites.length()) {
                                try {
                                    keAndroid.beginTransaction()
                                    jsonObject = limites.getJSONObject(i)
                                    ltrack = jsonObject.getString("kli_track").trim { it <= ' ' }
                                    lvendedor =
                                        jsonObject.getString("kli_codven").trim { it <= ' ' }
                                    lcliente = jsonObject.getString("kli_codcli").trim { it <= ' ' }
                                    larticulo =
                                        jsonObject.getString("kli_codart").trim { it <= ' ' }
                                    lcantidad = jsonObject.getInt("kli_cant")
                                    lfhizo =
                                        jsonObject.getString("kli_fechahizo").trim { it <= ' ' }
                                    lfvence =
                                        jsonObject.getString("kli_fechavence").trim { it <= ' ' }
                                    val actualizar = ContentValues()
                                    actualizar.put("kli_track", ltrack)
                                    actualizar.put("kli_codven", lvendedor)
                                    actualizar.put("kli_codcli", lcliente)
                                    actualizar.put("kli_codart", larticulo)
                                    actualizar.put("kli_cant", lcantidad)
                                    actualizar.put("kli_fechahizo", lfhizo)
                                    actualizar.put("kli_fechavence", lfvence)
                                    actualizar.put("status", "1")
                                    val hoy = LocalDateTime.now()
                                    val formatter =
                                        DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")
                                    val validar = hoy.minusDays(7)
                                    val fechaVal = validar.format(formatter)


                                    // ke_android.update("ke_limitart", actualizar, "kli_fechahizo > ?", new String[]{fechaVal});

                                    //actualizamos la fecha de la tabla de
                                    val fechaLimites1 = Calendar.getInstance()
                                    val sdf = SimpleDateFormat(
                                        "yyyy-MM-dd'T'HH:mm:ss",
                                        Locale.getDefault()
                                    )
                                    val fechaLimites = sdf.format(fechaLimites1.time)
                                    val actualizarFecha = ContentValues()
                                    actualizarFecha.put("fchhn_ultmod", fechaLimites)
                                    keAndroid.update(
                                        "tabla_aux",
                                        actualizarFecha,
                                        "tabla = ?",
                                        arrayOf("limites")
                                    )
                                    keAndroid.setTransactionSuccessful()
                                } catch (exception: Exception) {
                                    exception.printStackTrace()
                                } finally {
                                    keAndroid.endTransaction()
                                }
                                val codigoEnLocal = keAndroid.rawQuery(
                                    "SELECT count(kli_track), count(kli_codart) FROM ke_limitart WHERE kli_codart = '$larticulo' AND kli_track ='$ltrack'",
                                    null
                                )
                                codigoEnLocal.moveToFirst()
                                val trackExistente = codigoEnLocal.getInt(0)
                                val codigoExistente = codigoEnLocal.getInt(1)
                                codigoEnLocal.close()
                                if (trackExistente > 0 && codigoExistente > 0) {
                                    try {
                                        keAndroid.beginTransaction()
                                        jsonObject = limites.getJSONObject(i)
                                        ltrack =
                                            jsonObject.getString("kli_track").trim { it <= ' ' }
                                        lvendedor =
                                            jsonObject.getString("kli_codven").trim { it <= ' ' }
                                        lcliente =
                                            jsonObject.getString("kli_codcli").trim { it <= ' ' }
                                        larticulo =
                                            jsonObject.getString("kli_codart").trim { it <= ' ' }
                                        lcantidad = jsonObject.getInt("kli_cant")
                                        lfhizo =
                                            jsonObject.getString("kli_fechahizo").trim { it <= ' ' }
                                        lfvence = jsonObject.getString("kli_fechavence")
                                            .trim { it <= ' ' }
                                        val actualizar = ContentValues()
                                        actualizar.put("kli_track", ltrack)
                                        actualizar.put("kli_codven", lvendedor)
                                        actualizar.put("kli_codcli", lcliente)
                                        actualizar.put("kli_codart", larticulo)
                                        actualizar.put("kli_cant", lcantidad)
                                        actualizar.put("kli_fechahizo", lfhizo)
                                        actualizar.put("kli_fechavence", lfvence)
                                        actualizar.put("status", "1")
                                        val hoy = LocalDateTime.now()
                                        val formatter =
                                            DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")
                                        val validar = hoy.minusDays(7)
                                        val fechaVal = validar.format(formatter)


                                        //ke_android.update("ke_limitart", actualizar, "kli_fechahizo > ?", new String[]{fechaVal});

                                        //actualizamos la fecha de la tabla de
                                        val fechaLimites1 = Calendar.getInstance()
                                        val sdf = SimpleDateFormat(
                                            "yyyy-MM-dd'T'HH:mm:ss",
                                            Locale.getDefault()
                                        )
                                        val fechaLimites = sdf.format(fechaLimites1.time)
                                        val actualizarFecha = ContentValues()
                                        actualizarFecha.put("fchhn_ultmod", fechaLimites)
                                        //ke_android.update("tabla_aux", actualizarFecha, "tabla = ?", new String[]{"limites"});
                                        keAndroid.setTransactionSuccessful()
                                    } catch (exception: Exception) {
                                        exception.printStackTrace()
                                    } finally {
                                        keAndroid.endTransaction()
                                    }
                                } else {
                                    try {
                                        keAndroid.beginTransaction()
                                        jsonObject = limites.getJSONObject(i)
                                        ltrack =
                                            jsonObject.getString("kli_track").trim { it <= ' ' }
                                        lvendedor =
                                            jsonObject.getString("kli_codven").trim { it <= ' ' }
                                        lcliente =
                                            jsonObject.getString("kli_codcli").trim { it <= ' ' }
                                        larticulo =
                                            jsonObject.getString("kli_codart").trim { it <= ' ' }
                                        lcantidad = jsonObject.getInt("kli_cant")
                                        lfhizo =
                                            jsonObject.getString("kli_fechahizo").trim { it <= ' ' }
                                        lfvence = jsonObject.getString("kli_fechavence")
                                            .trim { it <= ' ' }
                                        val insertar = ContentValues()
                                        insertar.put("kli_track", ltrack)
                                        insertar.put("kli_codven", lvendedor)
                                        insertar.put("kli_codcli", lcliente)
                                        insertar.put("kli_codart", larticulo)
                                        insertar.put("kli_cant", lcantidad)
                                        insertar.put("kli_fechahizo", lfhizo)
                                        insertar.put("kli_fechavence", lfvence)
                                        insertar.put("status", "1")
                                        val hoy = LocalDateTime.now()
                                        val formatter =
                                            DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")
                                        val validar = hoy.minusDays(7)
                                        val fechaVal = validar.format(formatter)

                                        //INSERCION DE LOS REGISTROS
                                        keAndroid.insert("ke_limitart", null, insertar)

                                        //actualizamos la fecha de la tabla de
                                        val fechaLimites1 = Calendar.getInstance()
                                        val sdf = SimpleDateFormat(
                                            "yyyy-MM-dd'T'HH:mm:ss",
                                            Locale.getDefault()
                                        )
                                        val fechaLimites = sdf.format(fechaLimites1.time)
                                        val actualizarFecha = ContentValues()
                                        actualizarFecha.put("fchhn_ultmod", fechaLimites)
                                        keAndroid.update(
                                            "tabla_aux",
                                            actualizarFecha,
                                            "tabla = ?",
                                            arrayOf("limites")
                                        )
                                        keAndroid.setTransactionSuccessful()
                                    } catch (exception: Exception) {
                                        exception.printStackTrace()
                                    } finally {
                                        keAndroid.endTransaction()
                                    }
                                }
                            }
                            varAux++
                            progressDialog!!.incrementProgressBy(numBarraProgreso.toInt())
                            sincronizacionVendedor()
                        } else {
                            keAndroid = conn!!.writableDatabase
                            var jsonObject: JSONObject //creamos un objeto json vacio
                            for (i in 0 until limites.length()) {
                                try {
                                    keAndroid.beginTransaction()
                                    jsonObject = limites.getJSONObject(i)
                                    ltrack = jsonObject.getString("kli_track").trim { it <= ' ' }
                                    lvendedor =
                                        jsonObject.getString("kli_codven").trim { it <= ' ' }
                                    lcliente = jsonObject.getString("kli_codcli").trim { it <= ' ' }
                                    larticulo =
                                        jsonObject.getString("kli_codart").trim { it <= ' ' }
                                    lcantidad = jsonObject.getInt("kli_cant")
                                    lfhizo =
                                        jsonObject.getString("kli_fechahizo").trim { it <= ' ' }
                                    lfvence =
                                        jsonObject.getString("kli_fechavence").trim { it <= ' ' }
                                    val insertar = ContentValues()
                                    insertar.put("kli_track", ltrack)
                                    insertar.put("kli_codven", lvendedor)
                                    insertar.put("kli_codcli", lcliente)
                                    insertar.put("kli_codart", larticulo)
                                    insertar.put("kli_cant", lcantidad)
                                    insertar.put("kli_fechahizo", lfhizo)
                                    insertar.put("kli_fechavence", lfvence)
                                    insertar.put("status", "1")
                                    val hoy = LocalDateTime.now()
                                    val formatter =
                                        DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")
                                    val validar = hoy.minusDays(7)
                                    val fechaVal = validar.format(formatter)
                                    keAndroid.insert("ke_limitart", null, insertar)

                                    //actualizamos la fecha de la tabla de
                                    val fechaLimites1 = Calendar.getInstance()
                                    val sdf = SimpleDateFormat(
                                        "yyyy-MM-dd'T'HH:mm:ss",
                                        Locale.getDefault()
                                    )
                                    val fechaLimites = sdf.format(fechaLimites1.time)
                                    val actualizarFecha = ContentValues()
                                    actualizarFecha.put("fchhn_ultmod", fechaLimites)
                                    keAndroid.update(
                                        "tabla_aux",
                                        actualizarFecha,
                                        "tabla = ?",
                                        arrayOf("limites")
                                    )
                                    keAndroid.setTransactionSuccessful()
                                } catch (e: JSONException) {
                                    Toast.makeText(
                                        this@SincronizacionActivity,
                                        "Error 1",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } finally {
                                    keAndroid.endTransaction()
                                }
                            }
                            varAux++
                            progressDialog!!.incrementProgressBy(numBarraProgreso.toInt())
                            sincronizacionVendedor()
                        }
                    } else if (response.getString("limites") == "null") {
                        varAux++
                        progressDialog!!.incrementProgressBy(numBarraProgreso.toInt())
                        sincronizacionVendedor()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            { error: VolleyError? ->
                error?.printStackTrace()
                //Ingreso de la fecha antes de ser actualizada
                actualizarFechaError(fechaError)

                //--Manejo visual que indica al usuario del error--
                varAux++
                progressDialog!!.incrementProgressBy(numBarraProgreso.toInt())
                //varAuxError = true;
                //AnalisisError2();
                //------
                sincronizacionVendedor()
            }) {
            override fun getParams(): Map<String, String> {
                val parametros: MutableMap<String, String> = HashMap()
                parametros["cod_usuario"] = cod_usuario!!
                return parametros
            }
        }
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(jsonObjectRequest)
    }

    private fun subirLimite() {
        val keAndroid = conn!!.writableDatabase
        cursorLim =
            keAndroid.rawQuery("SELECT " + "kli_track, " + "kli_codven, " + "kli_codcli, " + "kli_codart, " + "kli_cant, " + "kli_fechahizo, " + "kli_fechavence " + "FROM ke_limitart" + " WHERE status = '1'  " + "AND kli_fechahizo >'" + fecha_sinc_limites + "'" + "AND kli_codven = '" + cod_usuario!!.trim { it <= ' ' } + "'",
                null)
        if (cursorLim.moveToFirst()) {
            cargarLimites()
        } else {
            //Toast.makeText(SincronizacionActivity.this, "Parametros al día", Toast.LENGTH_SHORT).show();
            varAux++
            progressDialog!!.incrementProgressBy(numBarraProgreso.toInt())
            sincronizacionVendedor()
        }
    }

    fun sincronizacionCoordinador() {
        binding.tvAviso.text =
            "Por favor, espere mientras los datos sincronizan. No abandone esta pantalla hasta que finalice el proceso"
        getFechas()
        bajarVendedor("https://" + enlaceEmpresa + "/" + ambienteJob + "/listvend.php?cod_usuario=" + cod_usuario!!.trim { it <= ' ' } + "&&agencia=" + codigoSucursal.trim { it <= ' ' })
        bajarArticulos("https://" + enlaceEmpresa + "/" + ambienteJob + "/articulos.php?fecha_sinc=" + fecha_sinc_articulo!!.trim { it <= ' ' } + "&&agencia=" + codigoSucursal.trim { it <= ' ' })
        bajarKardex("https://" + enlaceEmpresa + "/" + ambienteJob + "/kardex.php?fecha_sinc=" + fecha_sinc_articulo!!.trim { it <= ' ' } + "&&agencia=" + codigoSucursal.trim { it <= ' ' })
    }

    private fun bajarInfoPedidos(url: String?) {
        //System.out.println(URL);
        binding.tvPedidosact.setTextColor(Color.rgb(41, 184, 214))
        binding.tvPedidosact.text = "Pedidos Act.: Sincronizando."
        contadorpedidosactualizados = 0
        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
            Method.GET,
            url,
            null,
            { response: JSONObject ->  //a traves de un json array request, traemos la informacion que viene del webservice
                //System.out.println("Rspuestaaaaaaa ->" + response);
                try {
                    if (response.getString("pedidos") != "null") { // si la respuesta no viene vacia
                        //System.out.println("NO VINO NULA");
                        conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null, 1)
                        val keAndroid = conn!!.writableDatabase
                        val filas = DatabaseUtils.queryNumEntries(
                            keAndroid,
                            "ke_opti"
                        ) //obtenemos las filas de la tabla articulos para comprobar si hay o no registros
                        val pedidos = response.getJSONArray("pedidos")

                        //aqui valido las filas de la tabla de articulos en el telefono
                        if (filas > 0) {
                            var jsonObject: JSONObject //creamos un objeto json vacio
                            for (i in 0 until pedidos.length()) { /*pongo todo en el objeto segun lo que venga */
                                try {
                                    keAndroid.beginTransaction()
                                    jsonObject = pedidos.getJSONObject(i)
                                    nropedido =
                                        jsonObject.getString("kti_nroped").trim { it <= ' ' }
                                    fechamodifi =
                                        jsonObject.getString("fechamodifi").trim { it <= ' ' }
                                    numinterno = jsonObject.getString("kti_ndoc").trim { it <= ' ' }
                                    kti_status =
                                        jsonObject.getString("kti_status").trim { it <= ' ' }
                                    ke_pedstatus =
                                        jsonObject.getString("ke_pedstatus").trim { it <= ' ' }
                                    //System.out.println(nropedido);
                                    val actualizar = ContentValues()
                                    actualizar.put("kti_nroped", nropedido)
                                    actualizar.put("fechamodifi", fechamodifi)
                                    actualizar.put("kti_status", kti_status)
                                    actualizar.put("ke_pedstatus", ke_pedstatus)
                                    keAndroid.update(
                                        "ke_opti", actualizar, "kti_ndoc = ?", arrayOf(
                                            numinterno
                                        )
                                    )
                                    keAndroid.setTransactionSuccessful()
                                    contadorpedidosactualizados++
                                } catch (e: JSONException) {
                                    Toast.makeText(
                                        this@SincronizacionActivity,
                                        "Error 2",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } finally {
                                    keAndroid.endTransaction()
                                }
                            }
                            keAndroid.close()
                            binding.tvPedidosact.setTextColor(Color.rgb(62, 197, 58))
                            binding.tvPedidosact.text =
                                "Pedidos Act: " + contadorpedidosactualizados
                            varAux++
                            progressDialog!!.incrementProgressBy(numBarraProgreso.toInt())
                            progressDialog!!.setMessage("Pedidos act.$contadorpedidosactualizados")
                            sincronizacionVendedor()
                        } else {
                            binding.tvPedidosact.setTextColor(Color.rgb(98, 117, 141))
                            binding.tvPedidosact.text = "Pedidos Act: Sin actualización"
                            progressDialog!!.setMessage("Pedidos Act: Sin actualización")
                            varAux++
                            progressDialog!!.incrementProgressBy(numBarraProgreso.toInt())
                            sincronizacionVendedor()
                            // Toast.makeText(SincronizacionActivity.this,"Nada en el if del long", LENGTH_LONG).show();
                        }
                    } else if (response.getString("pedidos") == "null") {
                        binding.tvPedidosact.setTextColor(Color.rgb(98, 117, 141))
                        binding.tvPedidosact.text = "Pedidos Act: Sin actualización"
                        progressDialog!!.setMessage("Pedidos Act: Sin actualización")
                        varAux++
                        progressDialog!!.incrementProgressBy(numBarraProgreso.toInt())
                        sincronizacionVendedor()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            { error: VolleyError? ->
                error?.printStackTrace()
                //--Manejo visual que indica al usuario del error--
                binding.tvPedidosact.setTextColor(Color.rgb(232, 17, 35))
                binding.tvPedidosact.text = "Pedidos Act: No ha logrado sincronizar"
                progressDialog!!.setMessage("Pedidos No ha logrado sincronizar")
                varAux++
                progressDialog!!.incrementProgressBy(numBarraProgreso.toInt())
                //varAuxError = true;
                //AnalisisError2();
                //------
                sincronizacionVendedor()
            }) {
            override fun getParams(): Map<String, String>? {  //finalmente, estos son los parametros que le enviaremos al webservice, partiendo de las variables
                //donde estan guardados las fechas
                // parametros.put("fecha_sinc", fecha_sinc);
                return HashMap()
            }
        }
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(jsonObjectRequest) //esto es el request que se envia al url a traves de la conexion volley, (el stringrequest esta armado arriba)
    }

    private fun bajarUsuario(url: String) {
        //System.out.println("Usuario ->" + URL);
        //Fecha tomada para ser coloada en tabla auxiliar en caso de dar un error
        val fechaError = obtenerFechaPreError("usuarios")
        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
            Method.GET,
            url,
            null,
            { response: JSONObject ->  //a traves de un json array request, traemos la informacion que viene del webservice
                try {
                    if (response.getString("usuario") != "null") { // si la respuesta no viene vacia
                        conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null, 1)
                        var keAndroid = conn!!.writableDatabase
                        val filas = DatabaseUtils.queryNumEntries(
                            keAndroid,
                            "usuarios"
                        ) //obtenemos las filas de la tabla articulos para comprobar si hay o no registros
                        val usuario = response.getJSONArray("usuario")

                        //aqui valido las filas de la tabla de articulos en el telefono
                        if (filas > 0) {
                            var jsonObject: JSONObject //creamos un objeto json vacio
                            for (i in 0 until usuario.length()) { /*pongo todo en el objeto segun lo que venga */
                                try {
                                    keAndroid.beginTransaction()
                                    jsonObject = usuario.getJSONObject(i)
                                    nombre = jsonObject.getString("nombre").trim { it <= ' ' }
                                    username = jsonObject.getString("username").trim { it <= ' ' }
                                    password = jsonObject.getString("password").trim { it <= ' ' }
                                    vendedor = jsonObject.getString("vendedor").trim { it <= ' ' }
                                    almacen = jsonObject.getString("almacen").trim { it <= ' ' }
                                    desactivo = jsonObject.getDouble("desactivo")
                                    fechamodifi =
                                        jsonObject.getString("fechamodifi").trim { it <= ' ' }
                                    ualterprec = jsonObject.getDouble("ualterprec")
                                    val actualizar = ContentValues()
                                    actualizar.put("nombre", nombre)
                                    actualizar.put("username", username)
                                    actualizar.put("password", password)
                                    actualizar.put("vendedor", vendedor)
                                    actualizar.put("almacen", almacen)
                                    actualizar.put("desactivo", desactivo)
                                    actualizar.put("fechamodifi", fechamodifi)
                                    actualizar.put("ualterprec", ualterprec)
                                    keAndroid.update("usuarios", actualizar, null, null)


                                    //actualizamos la fecha de la tabla de
                                    val fechaUsuarios = Calendar.getInstance()
                                    val sdf = SimpleDateFormat(
                                        "yyyy-MM-dd'T'HH:mm:ss",
                                        Locale.getDefault()
                                    )
                                    val fechausuarios = sdf.format(fechaUsuarios.time)
                                    val actualizarFecha = ContentValues()
                                    actualizarFecha.put("fchhn_ultmod", fechausuarios)
                                    keAndroid.setTransactionSuccessful()
                                } catch (e: JSONException) {
                                    Toast.makeText(applicationContext, "Error 3", Toast.LENGTH_LONG)
                                        .show()
                                } finally {
                                    keAndroid.endTransaction()
                                }
                                val codigoEnLocal = keAndroid.rawQuery(
                                    "SELECT count(nombre) FROM usuarios WHERE vendedor = '$vendedor'",
                                    null
                                )
                                codigoEnLocal.moveToFirst()
                                val codigoExistente = codigoEnLocal.getInt(0)
                                codigoEnLocal.close()
                                if (codigoExistente > 0) {
                                    try {
                                        keAndroid.beginTransaction()
                                        jsonObject = usuario.getJSONObject(i)
                                        nombre = jsonObject.getString("nombre").trim { it <= ' ' }
                                        username =
                                            jsonObject.getString("username").trim { it <= ' ' }
                                        password =
                                            jsonObject.getString("password").trim { it <= ' ' }
                                        vendedor =
                                            jsonObject.getString("vendedor").trim { it <= ' ' }
                                        almacen = jsonObject.getString("almacen").trim { it <= ' ' }
                                        desactivo = jsonObject.getDouble("desactivo")
                                        fechamodifi =
                                            jsonObject.getString("fechamodifi").trim { it <= ' ' }
                                        ualterprec = jsonObject.getDouble("ualterprec")
                                        val actualizar = ContentValues()
                                        actualizar.put("nombre", nombre)
                                        actualizar.put("username", username)
                                        actualizar.put("password", password)
                                        actualizar.put("vendedor", vendedor)
                                        actualizar.put("almacen", almacen)
                                        actualizar.put("desactivo", desactivo)
                                        actualizar.put("fechamodifi", fechamodifi)
                                        actualizar.put("ualterprec", ualterprec)
                                        keAndroid.update("usuarios", actualizar, null, null)

                                        //actualizamos la fecha de la tabla de
                                        val fechaUsuarios = Calendar.getInstance()
                                        val sdf = SimpleDateFormat(
                                            "yyyy-MM-dd'T'HH:mm:ss",
                                            Locale.getDefault()
                                        )
                                        val fechausuarios = sdf.format(fechaUsuarios.time)
                                        val actualizarFecha = ContentValues()
                                        actualizarFecha.put("fchhn_ultmod", fechausuarios)
                                        keAndroid.setTransactionSuccessful()
                                    } catch (e: JSONException) {
                                        Toast.makeText(
                                            applicationContext,
                                            "Error 4",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } finally {
                                        keAndroid.endTransaction()
                                    }
                                } else {
                                    try {
                                        keAndroid.beginTransaction()
                                        jsonObject = usuario.getJSONObject(i)
                                        nombre = jsonObject.getString("nombre").trim { it <= ' ' }
                                        username =
                                            jsonObject.getString("username").trim { it <= ' ' }
                                        password =
                                            jsonObject.getString("password").trim { it <= ' ' }
                                        vendedor =
                                            jsonObject.getString("vendedor").trim { it <= ' ' }
                                        almacen = jsonObject.getString("almacen").trim { it <= ' ' }
                                        desactivo = jsonObject.getDouble("desactivo")
                                        fechamodifi =
                                            jsonObject.getString("fechamodifi").trim { it <= ' ' }
                                        ualterprec = jsonObject.getDouble("ualterprec")
                                        val insertar = ContentValues()
                                        insertar.put("nombre", nombre)
                                        insertar.put("username", username)
                                        insertar.put("password", password)
                                        insertar.put("vendedor", vendedor)
                                        insertar.put("almacen", almacen)
                                        insertar.put("desactivo", desactivo)
                                        insertar.put("fechamodifi", fechamodifi)
                                        insertar.put("ualterprec", ualterprec)
                                        keAndroid.insert("usuarios", null, insertar)

                                        //actualizamos la fecha de la tabla de
                                        val fechaUsuarios = Calendar.getInstance()
                                        val sdf = SimpleDateFormat(
                                            "yyyy-MM-dd'T'HH:mm:ss",
                                            Locale.getDefault()
                                        )
                                        val fechausuarios = sdf.format(fechaUsuarios.time)
                                        val actualizarFecha = ContentValues()
                                        actualizarFecha.put("fchhn_ultmod", fechausuarios)
                                        keAndroid.update(
                                            "tabla_aux",
                                            actualizarFecha,
                                            "tabla = 'usuarios'",
                                            null
                                        )
                                        keAndroid.setTransactionSuccessful()
                                    } catch (e: JSONException) {
                                        Toast.makeText(
                                            applicationContext,
                                            "Error 5",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } finally {
                                        keAndroid.endTransaction()
                                    }
                                }
                            }
                            keAndroid.close()

                            // tv_estadosync.setTextColor(Color.rgb(62,197,58));
                            //   tv_estadosync.setText("Subsectores Sincronizado");
                            progressDialog!!.setMessage("Usuario actualizado")
                            varAux++
                            progressDialog!!.incrementProgressBy(numBarraProgreso.toInt())
                            sincronizacionVendedor()
                        } else {

                            //si no hay nada, hago un insert
                            val conn =
                                AdminSQLiteOpenHelper(applicationContext, "ke_android", null, 1)
                            keAndroid = conn.writableDatabase
                            var jsonObject: JSONObject //creamos un objeto json vacio
                            for (i in 0 until usuario.length()) { /*pongo todo en el objeto segun lo que venga */
                                try {
                                    keAndroid.beginTransaction()
                                    jsonObject = usuario.getJSONObject(i)
                                    nombre = jsonObject.getString("nombre").trim { it <= ' ' }
                                    username = jsonObject.getString("username").trim { it <= ' ' }
                                    password = jsonObject.getString("password").trim { it <= ' ' }
                                    vendedor = jsonObject.getString("vendedor").trim { it <= ' ' }
                                    almacen = jsonObject.getString("almacen").trim { it <= ' ' }
                                    desactivo = jsonObject.getDouble("desactivo")
                                    fechamodifi =
                                        jsonObject.getString("fechamodifi").trim { it <= ' ' }
                                    ualterprec = jsonObject.getDouble("ualterprec")
                                    val insertar = ContentValues()
                                    insertar.put("nombre", nombre)
                                    insertar.put("username", username)
                                    insertar.put("password", password)
                                    insertar.put("vendedor", vendedor)
                                    insertar.put("almacen", almacen)
                                    insertar.put("desactivo", desactivo)
                                    insertar.put("fechamodifi", fechamodifi)
                                    insertar.put("ualterprec", ualterprec)
                                    keAndroid.insert("usuarios", null, insertar)

                                    //actualizamos la fecha de la tabla de
                                    val fechaUsuarios = Calendar.getInstance()
                                    val sdf = SimpleDateFormat(
                                        "yyyy-MM-dd'T'HH:mm:ss",
                                        Locale.getDefault()
                                    )
                                    val fechausuarios = sdf.format(fechaUsuarios.time)
                                    val actualizarFecha = ContentValues()
                                    actualizarFecha.put("fchhn_ultmod", fechausuarios)
                                    keAndroid.update(
                                        "tabla_aux",
                                        actualizarFecha,
                                        "tabla = 'usuarios'",
                                        null
                                    )
                                    keAndroid.setTransactionSuccessful()
                                } catch (e: JSONException) {
                                    Toast.makeText(applicationContext, "Error 6", Toast.LENGTH_LONG)
                                        .show()
                                } finally {
                                    keAndroid.endTransaction()
                                }
                            }
                            //    Toast.makeText(PrincipalActivity.this, "Subsectores descargados", Toast.LENGTH_SHORT).show();
                            keAndroid.close()
                            progressDialog!!.setMessage("Usuario: actualizado.")
                            varAux++
                            progressDialog!!.incrementProgressBy(numBarraProgreso.toInt())
                            sincronizacionVendedor()
                        }
                    } else if (response.getString("usuario") == "null") {
                        progressDialog!!.setMessage("Usuario: sin actualizar.")
                        varAux++
                        progressDialog!!.incrementProgressBy(numBarraProgreso.toInt())
                        sincronizacionVendedor()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { error: VolleyError? ->
                error?.printStackTrace()
                //Ingreso de la fecha antes de ser actualizada
                actualizarFechaError(fechaError)
                //--Manejo visual que indica al usuario del error--
                progressDialog!!.setMessage("Usuario: No ha logrado sincronizar.")
                varAux++
                progressDialog!!.incrementProgressBy(numBarraProgreso.toInt())
                //varAuxError = true;
                //AnalisisError2();
                //------
                sincronizacionVendedor()
            }) {
            override fun getParams(): Map<String, String> {  //finalmente, estos son los parametros que le enviaremos al webservice, partiendo de las variables
                //donde estan guardados las fechas
                // parametros.put("fecha_sinc", fecha_sinc);
                return HashMap()
            }
        }
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(jsonObjectRequest) //esto es el request que se envia al url a traves de la conexion volley, (el stringrequest esta armado arriba)
    }

    private fun bajarConfig(url: String) {
        //Fecha tomada para ser coloada en tabla auxiliar en caso de dar un error
        val fechaError = obtenerFechaPreError("config2")


        // tv_estadosync.setTextColor(Color.rgb(41,184,214));
        // tv_estadosync.setText("Sincronizando Co");
        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
            Method.GET,
            url,
            null,
            { response: JSONObject ->  //a traves de un json array request, traemos la informacion que viene del webservice
                try {
                    if (response.getString("config") != "null") { // si la respuesta no viene vacia
                        conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null, 1)
                        var keAndroid = conn!!.writableDatabase
                        val filas = DatabaseUtils.queryNumEntries(
                            keAndroid,
                            "config2"
                        ) //obtenemos las filas de la tabla articulos para comprobar si hay o no registros
                        val config = response.getJSONArray("config")

                        //aqui valido las filas de la tabla de articulos en el telefono
                        if (filas > 0) {
                            var jsonObject: JSONObject //creamos un objeto json vacio
                            for (i in 0 until config.length()) { /*pongo todo en el objeto segun lo que venga */
                                try {
                                    keAndroid.beginTransaction()
                                    jsonObject = config.getJSONObject(i)
                                    id_precio1 =
                                        jsonObject.getString("id_precio1").trim { it <= ' ' }
                                    id_precio2 =
                                        jsonObject.getString("id_precio2").trim { it <= ' ' }
                                    id_precio3 =
                                        jsonObject.getString("id_precio3").trim { it <= ' ' }
                                    id_precio4 =
                                        jsonObject.getString("id_precio4").trim { it <= ' ' }
                                    id_precio5 =
                                        jsonObject.getString("id_precio5").trim { it <= ' ' }
                                    id_precio6 =
                                        jsonObject.getString("id_precio6").trim { it <= ' ' }
                                    id_precio7 =
                                        jsonObject.getString("id_precio7").trim { it <= ' ' }
                                    val actualizar = ContentValues()
                                    actualizar.put("id_precio1", id_precio1)
                                    actualizar.put("id_precio2", id_precio2)
                                    actualizar.put("id_precio3", id_precio3)
                                    actualizar.put("id_precio4", id_precio4)
                                    actualizar.put("id_precio5", id_precio5)
                                    actualizar.put("id_precio6", id_precio6)
                                    actualizar.put("id_precio7", id_precio7)
                                    keAndroid.update("config2", actualizar, null, null)

                                    //actualizamos la fecha de la tabla de
                                    val fechaConfig = Calendar.getInstance()
                                    val sdf = SimpleDateFormat(
                                        "yyyy-MM-dd'T'HH:mm:ss",
                                        Locale.getDefault()
                                    )
                                    val fechaconfig = sdf.format(fechaConfig.time)
                                    val actualizarFecha = ContentValues()
                                    actualizarFecha.put("fchhn_ultmod", fechaconfig)
                                    keAndroid.update(
                                        "tabla_aux",
                                        actualizarFecha,
                                        "tabla = 'config2'",
                                        null
                                    )
                                    keAndroid.setTransactionSuccessful()
                                } catch (e: JSONException) {
                                    Toast.makeText(applicationContext, "Error 7", Toast.LENGTH_LONG)
                                        .show()
                                } finally {
                                    keAndroid.endTransaction()
                                }
                                val codigoEnLocal = keAndroid.rawQuery(
                                    "SELECT count(id_precio1) FROM config2 WHERE id_precio1 = '$id_precio1'",
                                    null
                                )
                                codigoEnLocal.moveToFirst()
                                val codigoExistente = codigoEnLocal.getInt(0)
                                codigoEnLocal.close()
                                if (codigoExistente > 0) {
                                    try {
                                        keAndroid.beginTransaction()
                                        jsonObject = config.getJSONObject(i)
                                        id_precio1 =
                                            jsonObject.getString("id_precio1").trim { it <= ' ' }
                                        id_precio2 =
                                            jsonObject.getString("id_precio2").trim { it <= ' ' }
                                        id_precio3 =
                                            jsonObject.getString("id_precio3").trim { it <= ' ' }
                                        id_precio4 =
                                            jsonObject.getString("id_precio4").trim { it <= ' ' }
                                        id_precio5 =
                                            jsonObject.getString("id_precio5").trim { it <= ' ' }
                                        id_precio6 =
                                            jsonObject.getString("id_precio6").trim { it <= ' ' }
                                        id_precio7 =
                                            jsonObject.getString("id_precio7").trim { it <= ' ' }
                                        val actualizar = ContentValues()
                                        actualizar.put("id_precio1", id_precio1)
                                        actualizar.put("id_precio2", id_precio2)
                                        actualizar.put("id_precio3", id_precio3)
                                        actualizar.put("id_precio4", id_precio4)
                                        actualizar.put("id_precio5", id_precio5)
                                        actualizar.put("id_precio6", id_precio6)
                                        actualizar.put("id_precio7", id_precio7)
                                        keAndroid.update("config2", actualizar, null, null)

                                        //actualizamos la fecha de la tabla de
                                        val fechaConfig = Calendar.getInstance()
                                        val sdf = SimpleDateFormat(
                                            "yyyy-MM-dd'T'HH:mm:ss",
                                            Locale.getDefault()
                                        )
                                        val fechaconfig = sdf.format(fechaConfig.time)
                                        val actualizarFecha = ContentValues()
                                        actualizarFecha.put("fchhn_ultmod", fechaconfig)
                                        keAndroid.update(
                                            "tabla_aux",
                                            actualizarFecha,
                                            "tabla = 'config2'",
                                            null
                                        )
                                        keAndroid.setTransactionSuccessful()
                                    } catch (e: JSONException) {
                                        Toast.makeText(
                                            applicationContext,
                                            "Error 8",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } finally {
                                        keAndroid.endTransaction()
                                    }
                                } else {
                                    try {
                                        keAndroid.beginTransaction()
                                        jsonObject = config.getJSONObject(i)
                                        id_precio1 =
                                            jsonObject.getString("id_precio1").trim { it <= ' ' }
                                        id_precio2 =
                                            jsonObject.getString("id_precio2").trim { it <= ' ' }
                                        id_precio3 =
                                            jsonObject.getString("id_precio3").trim { it <= ' ' }
                                        id_precio4 =
                                            jsonObject.getString("id_precio4").trim { it <= ' ' }
                                        id_precio5 =
                                            jsonObject.getString("id_precio5").trim { it <= ' ' }
                                        id_precio6 =
                                            jsonObject.getString("id_precio6").trim { it <= ' ' }
                                        id_precio7 =
                                            jsonObject.getString("id_precio7").trim { it <= ' ' }
                                        val insertar = ContentValues()
                                        insertar.put("id_precio1", id_precio1)
                                        insertar.put("id_precio2", id_precio2)
                                        insertar.put("id_precio3", id_precio3)
                                        insertar.put("id_precio4", id_precio4)
                                        insertar.put("id_precio5", id_precio5)
                                        insertar.put("id_precio6", id_precio6)
                                        insertar.put("id_precio7", id_precio7)
                                        keAndroid.insert("config2", null, insertar)

                                        //actualizamos la fecha de la tabla de
                                        val fechaConfig = Calendar.getInstance()
                                        val sdf = SimpleDateFormat(
                                            "yyyy-MM-dd'T'HH:mm:ss",
                                            Locale.getDefault()
                                        )
                                        val fechaconfig = sdf.format(fechaConfig.time)
                                        val actualizarFecha = ContentValues()
                                        actualizarFecha.put("fchhn_ultmod", fechaconfig)
                                        keAndroid.update(
                                            "tabla_aux",
                                            actualizarFecha,
                                            "tabla = 'config2'",
                                            null
                                        )
                                        keAndroid.setTransactionSuccessful()
                                    } catch (e: JSONException) {
                                        Toast.makeText(
                                            applicationContext,
                                            "Error 9",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } finally {
                                        keAndroid.endTransaction()
                                    }
                                }
                            }
                            keAndroid.close()
                            progressDialog!!.setMessage("config. actualizada")
                            varAux++
                            progressDialog!!.incrementProgressBy(numBarraProgreso.toInt())
                            sincronizacionVendedor()
                        } else {

                            //si no hay nada, hago un insert
                            val conn =
                                AdminSQLiteOpenHelper(applicationContext, "ke_android", null, 1)
                            keAndroid = conn.writableDatabase
                            var jsonObject: JSONObject //creamos un objeto json vacio
                            for (i in 0 until config.length()) { /*pongo todo en el objeto segun lo que venga */
                                try {
                                    keAndroid.beginTransaction()
                                    jsonObject = config.getJSONObject(i)
                                    id_precio1 =
                                        jsonObject.getString("id_precio1").trim { it <= ' ' }
                                    id_precio2 =
                                        jsonObject.getString("id_precio2").trim { it <= ' ' }
                                    id_precio3 =
                                        jsonObject.getString("id_precio3").trim { it <= ' ' }
                                    id_precio4 =
                                        jsonObject.getString("id_precio4").trim { it <= ' ' }
                                    id_precio5 =
                                        jsonObject.getString("id_precio5").trim { it <= ' ' }
                                    id_precio6 =
                                        jsonObject.getString("id_precio6").trim { it <= ' ' }
                                    id_precio7 =
                                        jsonObject.getString("id_precio7").trim { it <= ' ' }
                                    val insertar = ContentValues()
                                    insertar.put("id_precio1", id_precio1)
                                    insertar.put("id_precio2", id_precio2)
                                    insertar.put("id_precio3", id_precio3)
                                    insertar.put("id_precio4", id_precio4)
                                    insertar.put("id_precio5", id_precio5)
                                    insertar.put("id_precio6", id_precio6)
                                    insertar.put("id_precio7", id_precio7)
                                    keAndroid.insert("config2", null, insertar)

                                    //actualizamos la fecha de la tabla de
                                    val fechaConfig = Calendar.getInstance()
                                    val sdf = SimpleDateFormat(
                                        "yyyy-MM-dd'T'HH:mm:ss",
                                        Locale.getDefault()
                                    )
                                    val fechaconfig = sdf.format(fechaConfig.time)
                                    val actualizarFecha = ContentValues()
                                    actualizarFecha.put("fchhn_ultmod", fechaconfig)
                                    keAndroid.update(
                                        "tabla_aux",
                                        actualizarFecha,
                                        "tabla = 'config2'",
                                        null
                                    )
                                    keAndroid.setTransactionSuccessful()
                                } catch (e: JSONException) {
                                    Toast.makeText(
                                        applicationContext,
                                        "Error 10",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    println("Error 10 -> $e")
                                } finally {
                                    keAndroid.endTransaction()
                                }
                            }
                            //    Toast.makeText(PrincipalActivity.this, "Subsectores descargados", Toast.LENGTH_SHORT).show();
                            keAndroid.close()

                            // tv_estadosync.setTextColor(Color.rgb(62,197,58));
                            // tv_estadosync.setText("Subsectores Sincronizado");
                            progressDialog!!.setMessage("Configuración: Actualizando.")
                            varAux++
                            progressDialog!!.incrementProgressBy(numBarraProgreso.toInt())
                            sincronizacionVendedor()
                        }
                    } else if (response.getString("config") == "null") {
                        progressDialog!!.setMessage("Configuración: Sin Actualizar.")
                        varAux++
                        progressDialog!!.incrementProgressBy(numBarraProgreso.toInt())
                        sincronizacionVendedor()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { error: VolleyError? ->
                error?.printStackTrace()
                //Ingreso de la fecha antes de ser actualizada
                actualizarFechaError(fechaError)

                //--Manejo visual que indica al usuario del error--
                progressDialog!!.setMessage("Configuración: No ha logrado sincronizar.")
                varAux++
                progressDialog!!.incrementProgressBy(numBarraProgreso.toInt())
                //varAuxError = true;
                //AnalisisError2();
                //------
                sincronizacionVendedor()
            }) {
            override fun getParams(): Map<String, String>? {  //finalmente, estos son los parametros que le enviaremos al webservice, partiendo de las variables
                //donde estan guardados las fechas
                // parametros.put("fecha_sinc", fecha_sinc);
                return HashMap()
            }
        }
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(jsonObjectRequest) //esto es el request que se envia al url a traves de la conexion volley, (el stringrequest esta armado arriba)
    }

    private fun bajarVendedor(url: String) {
        //System.out.println("URL vndeodr -> " + URL);
        //Fecha tomada para ser coloada en tabla auxiliar en caso de dar un error
        val fechaError = obtenerFechaPreError("listvend")
        contadorvend = 0
        binding.tvVendedor.setTextColor(Color.rgb(41, 184, 214))
        binding.tvVendedor.text = "Vendedor: Sincronizando"
        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
            Method.GET,
            url,
            null,
            { response: JSONObject ->  //a traves de un json array request, traemos la informacion que viene del webservice
                try {
                    if (response.getString("vendedor") != "null") { // si la respuesta no viene vacia
                        conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null, 1)
                        var keAndroid = conn!!.writableDatabase
                        val filas = DatabaseUtils.queryNumEntries(
                            keAndroid,
                            "listvend"
                        ) //obtenemos las filas de la tabla articulos para comprobar si hay o no registros
                        val vendedorArray = response.getJSONArray("vendedor")

                        //aqui valido las filas de la tabla de articulos en el telefono
                        if (filas > 0) {
                            var jsonObject: JSONObject //creamos un objeto json vacio
                            for (i in 0 until vendedorArray.length()) { /*pongo todo en el objeto segun lo que venga */
                                try {
                                    keAndroid.beginTransaction()
                                    jsonObject = vendedorArray.getJSONObject(i)
                                    codigo = jsonObject.getString("codigo").trim { it <= ' ' }
                                    nombre = jsonObject.getString("nombre").trim { it <= ' ' }
                                    telefonos = jsonObject.getString("telefonos").trim { it <= ' ' }
                                    telefono_movil =
                                        jsonObject.getString("telefono_movil").trim { it <= ' ' }
                                    status = jsonObject.getDouble("status")
                                    superves = jsonObject.getDouble("superves")
                                    supervpor = jsonObject.getString("supervpor").trim { it <= ' ' }
                                    sector = jsonObject.getString("sector").trim { it <= ' ' }
                                    subcodigo = jsonObject.getString("subcodigo").trim { it <= ' ' }
                                    nivgcial = jsonObject.getDouble("nivgcial")
                                    fechamodifi = jsonObject.getString("fechamodifi")
                                    val actualizar = ContentValues()
                                    actualizar.put("codigo", codigo)
                                    actualizar.put("nombre", nombre)
                                    actualizar.put("telefonos", telefonos)
                                    actualizar.put("telefono_movil", telefono_movil)
                                    actualizar.put("status", status)
                                    actualizar.put("superves", superves)
                                    actualizar.put("supervpor", supervpor)
                                    actualizar.put("sector", sector)
                                    actualizar.put("subcodigo", subcodigo)
                                    actualizar.put("nivgcial", nivgcial)
                                    actualizar.put("fechamodifi", fechamodifi)
                                    keAndroid.update(
                                        "listvend",
                                        actualizar,
                                        "codigo = '$codigo'",
                                        null
                                    )


                                    //actualizamos la fecha de la tabla de
                                    val fechaListvend1 = Calendar.getInstance()
                                    val sdf = SimpleDateFormat(
                                        "yyyy-MM-dd'T'HH:mm:ss",
                                        Locale.getDefault()
                                    )
                                    val fechaListvend = sdf.format(fechaListvend1.time)
                                    val actualizarFecha = ContentValues()
                                    actualizarFecha.put("fchhn_ultmod", fechaListvend)
                                    keAndroid.update(
                                        "tabla_aux",
                                        actualizarFecha,
                                        "tabla = 'listvend'",
                                        null
                                    )
                                    keAndroid.setTransactionSuccessful()
                                } catch (e: JSONException) {
                                    Toast.makeText(
                                        applicationContext,
                                        "Error 11",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } finally {
                                    keAndroid.endTransaction()
                                }
                                val codigoEnLocal = keAndroid.rawQuery(
                                    "SELECT count(codigo) FROM listvend WHERE codigo = '$codigo'",
                                    null
                                )
                                codigoEnLocal.moveToFirst()
                                val codigoExistente = codigoEnLocal.getInt(0)
                                codigoEnLocal.close()
                                if (codigoExistente > 0) {
                                    try {
                                        keAndroid.beginTransaction()
                                        jsonObject = vendedorArray.getJSONObject(i)
                                        codigo = jsonObject.getString("codigo").trim { it <= ' ' }
                                        nombre = jsonObject.getString("nombre").trim { it <= ' ' }
                                        telefonos =
                                            jsonObject.getString("telefonos").trim { it <= ' ' }
                                        telefono_movil = jsonObject.getString("telefono_movil")
                                            .trim { it <= ' ' }
                                        status = jsonObject.getDouble("status")
                                        superves = jsonObject.getDouble("superves")
                                        supervpor =
                                            jsonObject.getString("supervpor").trim { it <= ' ' }
                                        sector = jsonObject.getString("sector").trim { it <= ' ' }
                                        subcodigo =
                                            jsonObject.getString("subcodigo").trim { it <= ' ' }
                                        nivgcial = jsonObject.getDouble("nivgcial")
                                        fechamodifi = jsonObject.getString("fechamodifi")
                                        val actualizar = ContentValues()
                                        actualizar.put("codigo", codigo)
                                        actualizar.put("nombre", nombre)
                                        actualizar.put("telefonos", telefonos)
                                        actualizar.put("telefono_movil", telefono_movil)
                                        actualizar.put("status", status)
                                        actualizar.put("superves", superves)
                                        actualizar.put("supervpor", supervpor)
                                        actualizar.put("sector", sector)
                                        actualizar.put("subcodigo", subcodigo)
                                        actualizar.put("nivgcial", nivgcial)
                                        actualizar.put("fechamodifi", fechamodifi)
                                        keAndroid.update(
                                            "listvend",
                                            actualizar,
                                            "codigo = '$codigo'",
                                            null
                                        )


                                        //actualizamos la fecha de la tabla de
                                        val fechaListvend1 = Calendar.getInstance()
                                        val sdf = SimpleDateFormat(
                                            "yyyy-MM-dd'T'HH:mm:ss",
                                            Locale.getDefault()
                                        )
                                        val fechaListvend = sdf.format(fechaListvend1.time)
                                        val actualizarFecha = ContentValues()
                                        actualizarFecha.put("fchhn_ultmod", fechaListvend)
                                        keAndroid.update(
                                            "tabla_aux",
                                            actualizarFecha,
                                            "tabla = 'listvend'",
                                            null
                                        )
                                        keAndroid.setTransactionSuccessful()
                                        contadorvend++
                                    } catch (e: JSONException) {
                                        Toast.makeText(
                                            applicationContext,
                                            "Error 12",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } finally {
                                        keAndroid.endTransaction()
                                    }
                                } else {
                                    try {
                                        keAndroid.beginTransaction()
                                        jsonObject = vendedorArray.getJSONObject(i)
                                        codigo = jsonObject.getString("codigo").trim { it <= ' ' }
                                        nombre = jsonObject.getString("nombre").trim { it <= ' ' }
                                        telefonos =
                                            jsonObject.getString("telefonos").trim { it <= ' ' }
                                        telefono_movil = jsonObject.getString("telefono_movil")
                                            .trim { it <= ' ' }
                                        status = jsonObject.getDouble("status")
                                        superves = jsonObject.getDouble("superves")
                                        supervpor =
                                            jsonObject.getString("supervpor").trim { it <= ' ' }
                                        sector = jsonObject.getString("sector").trim { it <= ' ' }
                                        subcodigo =
                                            jsonObject.getString("subcodigo").trim { it <= ' ' }
                                        nivgcial = jsonObject.getDouble("nivgcial")
                                        fechamodifi = jsonObject.getString("fechamodifi")
                                        val insertar = ContentValues()
                                        insertar.put("codigo", codigo)
                                        insertar.put("nombre", nombre)
                                        insertar.put("telefonos", telefonos)
                                        insertar.put("telefono_movil", telefono_movil)
                                        insertar.put("status", status)
                                        insertar.put("superves", superves)
                                        insertar.put("supervpor", supervpor)
                                        insertar.put("sector", sector)
                                        insertar.put("subcodigo", subcodigo)
                                        insertar.put("nivgcial", nivgcial)
                                        insertar.put("fechamodifi", fechamodifi)
                                        keAndroid.insert("listvend", null, insertar)

                                        //actualizamos la fecha de la tabla de
                                        val fechaListvend1 = Calendar.getInstance()
                                        val sdf = SimpleDateFormat(
                                            "yyyy-MM-dd'T'HH:mm:ss",
                                            Locale.getDefault()
                                        )
                                        val fechaListvend = sdf.format(fechaListvend1.time)
                                        val actualizarFecha = ContentValues()
                                        actualizarFecha.put("fchhn_ultmod", fechaListvend)
                                        keAndroid.update(
                                            "tabla_aux",
                                            actualizarFecha,
                                            "tabla = 'listvend'",
                                            null
                                        )
                                        keAndroid.setTransactionSuccessful()
                                        contadorvend++
                                    } catch (e: JSONException) {
                                        Toast.makeText(
                                            applicationContext,
                                            "Error 13",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } finally {
                                        keAndroid.endTransaction()
                                    }
                                }
                            }
                            //Toast.makeText(PrincipalActivity.this, "vendedor Descargado", Toast.LENGTH_SHORT).show();
                            keAndroid.close()
                            // Clientes.setEnabled(true);
                            binding.tvVendedor.setTextColor(Color.rgb(62, 197, 58))
                            binding.tvVendedor.text = "Vendedor: " + contadorvend
                            progressDialog!!.setMessage("Vendedor: $contadorvend")
                            varAux++
                            progressDialog!!.incrementProgressBy(numBarraProgreso.toInt())
                            sincronizacionVendedor()
                        } else {

                            //si no hay nada, hago un insert
                            val conn =
                                AdminSQLiteOpenHelper(applicationContext, "ke_android", null, 1)
                            keAndroid = conn.writableDatabase
                            var jsonObject: JSONObject //creamos un objeto json vacio
                            for (i in 0 until vendedorArray.length()) { /*pongo todo en el objeto segun lo que venga */
                                try {
                                    keAndroid.beginTransaction()
                                    jsonObject = vendedorArray.getJSONObject(i)
                                    codigo = jsonObject.getString("codigo").trim { it <= ' ' }
                                    nombre = jsonObject.getString("nombre").trim { it <= ' ' }
                                    telefonos = jsonObject.getString("telefonos").trim { it <= ' ' }
                                    telefono_movil =
                                        jsonObject.getString("telefono_movil").trim { it <= ' ' }
                                    status = jsonObject.getDouble("status")
                                    superves = jsonObject.getDouble("superves")
                                    supervpor = jsonObject.getString("supervpor").trim { it <= ' ' }
                                    sector = jsonObject.getString("sector").trim { it <= ' ' }
                                    subcodigo = jsonObject.getString("subcodigo").trim { it <= ' ' }
                                    nivgcial = jsonObject.getDouble("nivgcial")
                                    fechamodifi = jsonObject.getString("fechamodifi")
                                    val insertar = ContentValues()
                                    insertar.put("codigo", codigo)
                                    insertar.put("nombre", nombre)
                                    insertar.put("telefonos", telefonos)
                                    insertar.put("telefono_movil", telefono_movil)
                                    insertar.put("status", status)
                                    insertar.put("superves", superves)
                                    insertar.put("supervpor", supervpor)
                                    insertar.put("sector", sector)
                                    insertar.put("subcodigo", subcodigo)
                                    insertar.put("nivgcial", nivgcial)
                                    insertar.put("fechamodifi", fechamodifi)
                                    keAndroid.insert("listvend", null, insertar)

                                    //actualizamos la fecha de la tabla de la tabla
                                    val fechaListvend1 = Calendar.getInstance()
                                    val sdf = SimpleDateFormat(
                                        "yyyy-MM-dd'T'HH:mm:ss",
                                        Locale.getDefault()
                                    )
                                    val fechaListvend = sdf.format(fechaListvend1.time)
                                    val actualizarFecha = ContentValues()
                                    actualizarFecha.put("fchhn_ultmod", fechaListvend)
                                    keAndroid.update(
                                        "tabla_aux",
                                        actualizarFecha,
                                        "tabla = 'listvend'",
                                        null
                                    )
                                    keAndroid.setTransactionSuccessful()
                                    contadorvend++
                                } catch (e: JSONException) {
                                    Toast.makeText(
                                        applicationContext,
                                        "Error 14",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } finally {
                                    keAndroid.endTransaction()
                                }
                            }
                            // Toast.makeText(PrincipalActivity.this, "vendedor Descargado", Toast.LENGTH_SHORT).show();
                            keAndroid.close()
                            //  Clientes.setEnabled(true);
                            binding.tvVendedor.setTextColor(Color.rgb(62, 197, 58))
                            binding.tvVendedor.text = "Vendedor: " + contadorvend
                            progressDialog!!.setMessage("Vendedor: $contadorvend")
                            varAux++
                            progressDialog!!.incrementProgressBy(numBarraProgreso.toInt())
                            sincronizacionVendedor()
                        }
                    } else if (response.getString("vendedor") == "null") {

                        // Toast.makeText(getApplicationContext(), "No se recibieron mas datos", LENGTH_LONG).show(); /* si en la consulta no ncuentra nada
                        //es que el usuario o password estan incorrectos */

                        //    Clientes.setEnabled(true);
                        binding.tvVendedor.setTextColor(Color.rgb(98, 117, 141))
                        binding.tvVendedor.text = "Vendedor: Sin actualización"
                        progressDialog!!.setMessage("Vendedor: Sin actualización")
                        varAux++
                        progressDialog!!.incrementProgressBy(numBarraProgreso.toInt())
                        sincronizacionVendedor()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { error: VolleyError? ->
                error?.printStackTrace()
                //Ingreso de la fecha antes de ser actualizada
                actualizarFechaError(fechaError)

                //    Clientes.setEnabled(true);
                //--Manejo visual que indica al usuario del error--
                binding.tvVendedor.setTextColor(Color.rgb(232, 17, 35))
                binding.tvVendedor.text = "Vendedor: No ha logrado sincronizar"
                progressDialog!!.setMessage("Vendedor: No ha logrado sincronizar")
                varAux++
                progressDialog!!.incrementProgressBy(numBarraProgreso.toInt())
                //varAuxError = true;
                //AnalisisError2();
                //------
                sincronizacionVendedor()
            }) {
            override fun getParams(): Map<String, String>? {  //finalmente, estos son los parametros que le enviaremos al webservice, partiendo de las variables
                //donde estan guardados las fechas
                val parametros: MutableMap<String, String> = HashMap()
                parametros["cod_usuario"] = cod_usuario!!
                return parametros
            }
        }
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(jsonObjectRequest) //esto es el request que se envia al url a traves de la conexion volley, (el stringrequest esta armado arriba)
    }

    //------------------------------ METODO PARA OBTENER FECHA -------------------------------------
    private fun getFechas() {
        getFechaArticulo()
        getFechaCliempre()
        getFechaListvend()
        getFechaGrupos()
        getFechaSubGrupos()
        getFechaSectores()
        getFechaSubgrupos()
        getFechaSubsectores()
        getFechaLimites()
    }

    private fun getFechaArticulo() {
        val keAndroid = conn!!.writableDatabase
        val fechaUltmod =
            keAndroid.rawQuery("SELECT fchhn_ultmod FROM tabla_aux WHERE tabla = 'articulo'", null)
        fechaUltmod.moveToFirst()
        fecha_sinc_articulo = fechaUltmod.getString(0)
        fechaUltmod.close()
    }

    private fun getFechaCliempre() {
        val keAndroid = conn!!.writableDatabase
        val fechaUltmod =
            keAndroid.rawQuery("SELECT fchhn_ultmod FROM tabla_aux WHERE tabla = 'cliempre'", null)
        fechaUltmod.moveToFirst()
        fecha_sinc_cliempre = fechaUltmod.getString(0)
        fechaUltmod.close()
    }

    private fun getFechaListvend() {
        val keAndroid = conn!!.writableDatabase
        val fechaUltmod =
            keAndroid.rawQuery("SELECT fchhn_ultmod FROM tabla_aux WHERE tabla = 'listvend'", null)
        fechaUltmod.moveToFirst()
        fecha_sinc_listvend = fechaUltmod.getString(0)
        fechaUltmod.close()
    }

    private fun getFechaGrupos() {
        val keAndroid = conn!!.writableDatabase
        val fechaUltmod =
            keAndroid.rawQuery("SELECT fchhn_ultmod FROM tabla_aux WHERE tabla = 'grupos'", null)
        fechaUltmod.moveToFirst()
        fecha_sinc_grupos = fechaUltmod.getString(0)
        fechaUltmod.close()
    }

    private fun getFechaSubGrupos() {
        val keAndroid = conn!!.writableDatabase
        val fechaUltmod = keAndroid.rawQuery(
            "SELECT fchhn_ultmod FROM tabla_aux WHERE tabla = 'subgrupos'",
            null
        )
        fechaUltmod.moveToFirst()
        fecha_sinc_subgrupos = fechaUltmod.getString(0)
        fechaUltmod.close()
    }

    private fun getFechaSectores() {
        val keAndroid = conn!!.writableDatabase
        val fechaUltmod =
            keAndroid.rawQuery("SELECT fchhn_ultmod FROM tabla_aux WHERE tabla = 'sectores'", null)
        fechaUltmod.moveToFirst()
        fecha_sinc_sectores = fechaUltmod.getString(0)
        fechaUltmod.close()
    }

    private fun getFechaSubgrupos() {
        val keAndroid = conn!!.writableDatabase
        val fechaUltmod = keAndroid.rawQuery(
            "SELECT fchhn_ultmod FROM tabla_aux WHERE tabla = 'subgrupos'",
            null
        )
        fechaUltmod.moveToFirst()
        fecha_sinc_subgrupos = fechaUltmod.getString(0)
        fechaUltmod.close()
    }

    private fun getFechaSubsectores() {
        val keAndroid = conn!!.writableDatabase
        val fechaUltmod = keAndroid.rawQuery(
            "SELECT fchhn_ultmod FROM tabla_aux WHERE tabla = 'subsectores'",
            null
        )
        fechaUltmod.moveToFirst()
        fecha_sinc_subsectores = fechaUltmod.getString(0)
        fechaUltmod.close()
    }

    private fun getFechaLimites() {
        val keAndroid = conn!!.writableDatabase
        val fechaUltmod =
            keAndroid.rawQuery("SELECT fchhn_ultmod FROM tabla_aux WHERE tabla = 'limites'", null)
        fechaUltmod.moveToFirst()
        fecha_sinc_limites = fechaUltmod.getString(0)
        fechaUltmod.close()
    }

    //-------------------------------
    private fun getFecha(tabla: String): String {
        val keAndroid = conn!!.writableDatabase
        val fechaUltmod =
            keAndroid.rawQuery("SELECT fchhn_ultmod FROM tabla_aux WHERE tabla = '$tabla';", null)
        var fecha = "0001-01-01T01:01:01"
        if (fechaUltmod.moveToFirst()) {
            fecha = fechaUltmod.getString(0)
        }
        fechaUltmod.close()
        return fecha
    }

    private fun bajarArticulos(url: String) {
        //System.out.println("Este es el URL -> " + URL);
        progressDialog!!.setMessage("Sincronizando articulos")
        //OJO AQUI QUE HAY 2 UPDATE Y SI SE ELIMINA 1 SE JODE TODA LA VAINA
        binding.tvArticulos.setTextColor(Color.rgb(41, 184, 214))
        binding.tvArticulos.text = "Articulos: Sincronizando"

        //Fecha tomada para ser coloada en tabla auxiliar en caso de dar un error
        val fechaError = obtenerFechaPreError("articulo")
        contadorart = 0
        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
            Method.GET,
            url,
            null,
            { response: JSONObject ->  //a traves de un json array request, traemos la informacion que viene del webservice
                try {
                    if (response.getString("articulo") != "null") { // si la respuesta no viene vacia
                        conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null, 8)
                        var keAndroid = conn!!.writableDatabase
                        val filas = DatabaseUtils.queryNumEntries(
                            keAndroid,
                            "articulo"
                        ) //obtenemos las filas de la tabla articulos para comprobar si hay o no registros
                        val articulo = response.getJSONArray("articulo")

                        //aqui valido las filas de la tabla de articulos en el telefono
                        if (filas > 0) {
                            var jsonObject: JSONObject //creamos un objeto json vacio
                            for (i in 0 until articulo.length()) { /*pongo todo en el objeto segun lo que venga */
                                try {
                                    keAndroid.beginTransaction()
                                    jsonObject = articulo.getJSONObject(i)
                                    codigo = jsonObject.getString("codigo").trim { it <= ' ' }
                                    grupo = jsonObject.getString("grupo").trim { it <= ' ' }
                                    subgrupo = jsonObject.getString("subgrupo").trim { it <= ' ' }
                                    nombre = jsonObject.getString("nombre").trim { it <= ' ' }
                                    marca = jsonObject.getString("marca").trim { it <= ' ' }
                                    referencia =
                                        jsonObject.getString("referencia").trim { it <= ' ' }
                                    unidad = jsonObject.getString("unidad").trim { it <= ' ' }
                                    precio1 = jsonObject.getDouble("precio1")
                                    precio2 = jsonObject.getDouble("precio2")
                                    precio3 = jsonObject.getDouble("precio3")
                                    precio4 = jsonObject.getDouble("precio4")
                                    precio5 = jsonObject.getDouble("precio5")
                                    precio6 = jsonObject.getDouble("precio6")
                                    precio7 = jsonObject.getDouble("precio7")
                                    existencia = jsonObject.getDouble("existencia")
                                    fechamodifi = jsonObject.getString("fechamodifi")
                                    discont = jsonObject.getDouble("discont")
                                    vta_max = jsonObject.getDouble("vta_max")
                                    vta_min = jsonObject.getDouble("vta_min")
                                    dctotope = jsonObject.getDouble("dctotope")
                                    enpreventa =
                                        jsonObject.getString("enpreventa").trim { it <= ' ' }
                                    comprometido = jsonObject.getString("comprometido")
                                    vta_minenx = jsonObject.getString("vta_minenx")
                                    val vtaSolofac = jsonObject.getInt("vta_solofac")
                                    val vtaSolone = jsonObject.getInt("vta_solone")
                                    val actualizar = ContentValues()
                                    actualizar.put("codigo", codigo)
                                    actualizar.put("grupo", grupo)
                                    actualizar.put("subgrupo", subgrupo)
                                    actualizar.put("nombre", nombre)
                                    actualizar.put("referencia", referencia)
                                    actualizar.put("marca", marca)
                                    actualizar.put("unidad", unidad)
                                    actualizar.put("precio1", precio1)
                                    actualizar.put("precio2", precio2)
                                    actualizar.put("precio3", precio3)
                                    actualizar.put("precio4", precio4)
                                    actualizar.put("precio5", precio5)
                                    actualizar.put("precio6", precio6)
                                    actualizar.put("precio7", precio7)
                                    actualizar.put("discont", discont)
                                    actualizar.put("fechamodifi", fechamodifi)
                                    actualizar.put("existencia", existencia)
                                    actualizar.put("discont", discont)
                                    actualizar.put("vta_max", vta_max)
                                    actualizar.put("vta_min", vta_min)
                                    actualizar.put("dctotope", dctotope)
                                    actualizar.put("enpreventa", enpreventa)
                                    actualizar.put("comprometido", comprometido)
                                    actualizar.put("vta_minenx", vta_minenx)
                                    actualizar.put("vta_solofac", vtaSolofac)
                                    actualizar.put("vta_solone", vtaSolone)
                                    keAndroid.update(
                                        "articulo",
                                        actualizar,
                                        "codigo = ?",
                                        arrayOf(codigo)
                                    )


                                    //actualizamos la fecha de la tabla de
                                    val fechaArticulo1 = Calendar.getInstance()
                                    val sdf = SimpleDateFormat(
                                        "yyyy-MM-dd'T'HH:mm:ss",
                                        Locale.getDefault()
                                    )
                                    val fechaArticulo = sdf.format(fechaArticulo1.time)
                                    val actualizarFecha = ContentValues()
                                    actualizarFecha.put("fchhn_ultmod", fechaArticulo)
                                    keAndroid.update(
                                        "tabla_aux",
                                        actualizarFecha,
                                        "tabla = ?",
                                        arrayOf("articulo")
                                    )
                                    keAndroid.setTransactionSuccessful()
                                } catch (exception: Exception) {
                                    exception.printStackTrace()
                                } finally {
                                    keAndroid.endTransaction()
                                }
                                val codigoEnLocal = keAndroid.rawQuery(
                                    "SELECT count(codigo) FROM articulo WHERE codigo = '$codigo'",
                                    null
                                )
                                codigoEnLocal.moveToFirst()
                                val codigoExistente = codigoEnLocal.getInt(0)
                                //System.out.println("ESTE ES EL CODIGO: " + codigo);
                                //System.out.println("SELECT count(codigo) FROM articulo WHERE codigo = '" + codigo + "'");
                                if (codigoExistente > 0) {
                                    try {
                                        keAndroid.beginTransaction()
                                        jsonObject = articulo.getJSONObject(i)
                                        codigo = jsonObject.getString("codigo").trim { it <= ' ' }
                                        grupo = jsonObject.getString("grupo").trim { it <= ' ' }
                                        subgrupo =
                                            jsonObject.getString("subgrupo").trim { it <= ' ' }
                                        nombre = jsonObject.getString("nombre").trim { it <= ' ' }
                                        marca = jsonObject.getString("marca").trim { it <= ' ' }
                                        referencia =
                                            jsonObject.getString("referencia").trim { it <= ' ' }
                                        unidad = jsonObject.getString("unidad").trim { it <= ' ' }
                                        precio1 = jsonObject.getDouble("precio1")
                                        precio2 = jsonObject.getDouble("precio2")
                                        precio3 = jsonObject.getDouble("precio3")
                                        precio4 = jsonObject.getDouble("precio4")
                                        precio5 = jsonObject.getDouble("precio5")
                                        precio6 = jsonObject.getDouble("precio6")
                                        precio7 = jsonObject.getDouble("precio7")
                                        existencia = jsonObject.getDouble("existencia")
                                        fechamodifi = jsonObject.getString("fechamodifi")
                                        discont = jsonObject.getDouble("discont")
                                        vta_max = jsonObject.getDouble("vta_max")
                                        vta_min = jsonObject.getDouble("vta_min")
                                        dctotope = jsonObject.getDouble("dctotope")
                                        enpreventa =
                                            jsonObject.getString("enpreventa").trim { it <= ' ' }
                                        comprometido = jsonObject.getString("comprometido")
                                        vta_minenx = jsonObject.getString("vta_minenx")
                                        val vtaSolofac = jsonObject.getInt("vta_solofac")
                                        val vtaSolone = jsonObject.getInt("vta_solone")
                                        val actualizar = ContentValues()
                                        actualizar.put("codigo", codigo)
                                        actualizar.put("grupo", grupo)
                                        actualizar.put("subgrupo", subgrupo)
                                        actualizar.put("nombre", nombre)
                                        actualizar.put("referencia", referencia)
                                        actualizar.put("marca", marca)
                                        actualizar.put("unidad", unidad)
                                        actualizar.put("precio1", precio1)
                                        actualizar.put("precio2", precio2)
                                        actualizar.put("precio3", precio3)
                                        actualizar.put("precio4", precio4)
                                        actualizar.put("precio5", precio5)
                                        actualizar.put("precio6", precio6)
                                        actualizar.put("precio7", precio7)
                                        actualizar.put("discont", discont)
                                        actualizar.put("fechamodifi", fechamodifi)
                                        actualizar.put("existencia", existencia)
                                        actualizar.put("discont", discont)
                                        actualizar.put("vta_max", vta_max)
                                        actualizar.put("vta_min", vta_min)
                                        actualizar.put("dctotope", dctotope)
                                        actualizar.put("enpreventa", enpreventa)
                                        actualizar.put("comprometido", comprometido)
                                        actualizar.put("vta_minenx", vta_minenx)
                                        actualizar.put("vta_solofac", vtaSolofac)
                                        actualizar.put("vta_solone", vtaSolone)
                                        keAndroid.update(
                                            "articulo", actualizar, "codigo = ?", arrayOf(
                                                codigo
                                            )
                                        )

                                        //actualizamos la fecha de la tabla de
                                        val fechaArticulo1 = Calendar.getInstance()
                                        val sdf = SimpleDateFormat(
                                            "yyyy-MM-dd'T'HH:mm:ss",
                                            Locale.getDefault()
                                        )
                                        val fechaArticulo = sdf.format(fechaArticulo1.time)
                                        val actualizarFecha = ContentValues()
                                        actualizarFecha.put("fchhn_ultmod", fechaArticulo)
                                        keAndroid.update(
                                            "tabla_aux",
                                            actualizarFecha,
                                            "tabla = ?",
                                            arrayOf("articulo")
                                        )
                                        keAndroid.setTransactionSuccessful()
                                        contadorart++
                                    } catch (exception: Exception) {
                                        exception.printStackTrace()
                                    } finally {
                                        codigoEnLocal.close()
                                        keAndroid.endTransaction()
                                    }
                                    binding.tvArticulos.setTextColor(Color.rgb(62, 197, 58))
                                    binding.tvArticulos.text = "Articulos:" + contadorart
                                    progressDialog!!.setMessage("Articulos:$contadorart")
                                } else {
                                    try {
                                        keAndroid.beginTransaction()
                                        jsonObject = articulo.getJSONObject(i)
                                        codigo = jsonObject.getString("codigo").trim { it <= ' ' }
                                        grupo = jsonObject.getString("grupo").trim { it <= ' ' }
                                        subgrupo =
                                            jsonObject.getString("subgrupo").trim { it <= ' ' }
                                        nombre = jsonObject.getString("nombre").trim { it <= ' ' }
                                        marca = jsonObject.getString("marca").trim { it <= ' ' }
                                        referencia =
                                            jsonObject.getString("referencia").trim { it <= ' ' }
                                        unidad = jsonObject.getString("unidad").trim { it <= ' ' }
                                        precio1 = jsonObject.getDouble("precio1")
                                        precio2 = jsonObject.getDouble("precio2")
                                        precio3 = jsonObject.getDouble("precio3")
                                        precio4 = jsonObject.getDouble("precio4")
                                        precio5 = jsonObject.getDouble("precio5")
                                        precio6 = jsonObject.getDouble("precio6")
                                        precio7 = jsonObject.getDouble("precio7")
                                        existencia = jsonObject.getDouble("existencia")
                                        fechamodifi = jsonObject.getString("fechamodifi")
                                        discont = jsonObject.getDouble("discont")
                                        vta_max = jsonObject.getDouble("vta_max")
                                        vta_min = jsonObject.getDouble("vta_min")
                                        dctotope = jsonObject.getDouble("dctotope")
                                        enpreventa =
                                            jsonObject.getString("enpreventa").trim { it <= ' ' }
                                        comprometido = jsonObject.getString("comprometido")
                                        vta_minenx = jsonObject.getString("vta_minenx")
                                        val vtaSolofac = jsonObject.getInt("vta_solofac")
                                        val vtaSolone = jsonObject.getInt("vta_solone")
                                        val insertar = ContentValues()
                                        insertar.put("codigo", codigo)
                                        insertar.put("grupo", grupo)
                                        insertar.put("subgrupo", subgrupo)
                                        insertar.put("nombre", nombre)
                                        insertar.put("referencia", referencia)
                                        insertar.put("marca", marca)
                                        insertar.put("unidad", unidad)
                                        insertar.put("precio1", precio1)
                                        insertar.put("precio2", precio2)
                                        insertar.put("precio3", precio3)
                                        insertar.put("precio4", precio4)
                                        insertar.put("precio5", precio5)
                                        insertar.put("precio6", precio6)
                                        insertar.put("precio7", precio7)
                                        insertar.put("discont", discont)
                                        insertar.put("fechamodifi", fechamodifi)
                                        insertar.put("existencia", existencia)
                                        insertar.put("discont", discont)
                                        insertar.put("vta_max", vta_max)
                                        insertar.put("vta_min", vta_min)
                                        insertar.put("dctotope", dctotope)
                                        insertar.put("enpreventa", enpreventa)
                                        insertar.put("comprometido", comprometido)
                                        insertar.put("vta_minenx", vta_minenx)
                                        insertar.put("vta_solofac", vtaSolofac)
                                        insertar.put("vta_solone", vtaSolone)
                                        keAndroid.insert("articulo", null, insertar)

                                        //actualizamos la fecha de la tabla de
                                        val fechaArticulo1 = Calendar.getInstance()
                                        val sdf = SimpleDateFormat(
                                            "yyyy-MM-dd'T'HH:mm:ss",
                                            Locale.getDefault()
                                        )
                                        val fechaArticulo = sdf.format(fechaArticulo1.time)
                                        val actualizarFecha = ContentValues()
                                        actualizarFecha.put("fchhn_ultmod", fechaArticulo)
                                        keAndroid.update(
                                            "tabla_aux",
                                            actualizarFecha,
                                            "tabla = ?",
                                            arrayOf("articulo")
                                        )
                                        contadorart++
                                        keAndroid.setTransactionSuccessful()
                                        binding.tvArticulos.setTextColor(Color.rgb(62, 197, 58))
                                        binding.tvArticulos.text = "Articulos:" + contadorart
                                        progressDialog!!.setMessage("Articulos:$contadorart")
                                    } catch (e: JSONException) {
                                        e.printStackTrace()
                                    } finally {
                                        keAndroid.endTransaction()
                                    }
                                }
                            }
                            varAux++
                            progressDialog!!.setMessage("Articulos:$contadorart")
                            progressDialog!!.incrementProgressBy(numBarraProgreso.toInt())
                            sincronizacionVendedor()
                        } else {

                            //si no hay nada, hago un insert
                            val conn =
                                AdminSQLiteOpenHelper(applicationContext, "ke_android", null, 3)
                            keAndroid = conn.writableDatabase
                            var jsonObject: JSONObject //creamos un objeto json vacio
                            for (i in 0 until articulo.length()) { /*pongo todo en el objeto segun lo que venga */
                                try {
                                    keAndroid.beginTransaction()
                                    jsonObject = articulo.getJSONObject(i)
                                    codigo = jsonObject.getString("codigo").trim { it <= ' ' }
                                    grupo = jsonObject.getString("grupo").trim { it <= ' ' }
                                    subgrupo = jsonObject.getString("subgrupo").trim { it <= ' ' }
                                    nombre = jsonObject.getString("nombre").trim { it <= ' ' }
                                    marca = jsonObject.getString("marca").trim { it <= ' ' }
                                    referencia =
                                        jsonObject.getString("referencia").trim { it <= ' ' }
                                    unidad = jsonObject.getString("unidad").trim { it <= ' ' }
                                    precio1 = jsonObject.getDouble("precio1")
                                    precio2 = jsonObject.getDouble("precio2")
                                    precio3 = jsonObject.getDouble("precio3")
                                    precio4 = jsonObject.getDouble("precio4")
                                    precio5 = jsonObject.getDouble("precio5")
                                    precio6 = jsonObject.getDouble("precio6")
                                    precio7 = jsonObject.getDouble("precio7")
                                    existencia = jsonObject.getDouble("existencia")
                                    fechamodifi = jsonObject.getString("fechamodifi")
                                    discont = jsonObject.getDouble("discont")
                                    vta_max = jsonObject.getDouble("vta_max")
                                    vta_min = jsonObject.getDouble("vta_min")
                                    dctotope = jsonObject.getDouble("dctotope")
                                    enpreventa =
                                        jsonObject.getString("enpreventa").trim { it <= ' ' }
                                    comprometido = jsonObject.getString("comprometido")
                                    vta_minenx = jsonObject.getString("vta_minenx")
                                    val vtaSolofac = jsonObject.getInt("vta_solofac")
                                    val vtaSolone = jsonObject.getInt("vta_solone")
                                    val insertar = ContentValues()
                                    insertar.put("codigo", codigo)
                                    insertar.put("grupo", grupo)
                                    insertar.put("subgrupo", subgrupo)
                                    insertar.put("nombre", nombre)
                                    insertar.put("referencia", referencia)
                                    insertar.put("marca", marca)
                                    insertar.put("unidad", unidad)
                                    insertar.put("precio1", precio1)
                                    insertar.put("precio2", precio2)
                                    insertar.put("precio3", precio3)
                                    insertar.put("precio4", precio4)
                                    insertar.put("precio5", precio5)
                                    insertar.put("precio6", precio6)
                                    insertar.put("precio7", precio7)
                                    insertar.put("discont", discont)
                                    insertar.put("fechamodifi", fechamodifi)
                                    insertar.put("existencia", existencia)
                                    insertar.put("discont", discont)
                                    insertar.put("vta_max", vta_max)
                                    insertar.put("vta_min", vta_min)
                                    insertar.put("dctotope", dctotope)
                                    insertar.put("enpreventa", enpreventa)
                                    insertar.put("comprometido", comprometido)
                                    insertar.put("vta_minenx", vta_minenx)
                                    insertar.put("vta_solofac", vtaSolofac)
                                    insertar.put("vta_solone", vtaSolone)
                                    keAndroid.insert("articulo", null, insertar)

                                    //actualizamos la fecha de la tabla de
                                    val fechaArticulo1 = Calendar.getInstance()
                                    val sdf = SimpleDateFormat(
                                        "yyyy-MM-dd'T'HH:mm:ss",
                                        Locale.getDefault()
                                    )
                                    val fechaArticulo = sdf.format(fechaArticulo1.time)
                                    val actualizarFecha = ContentValues()
                                    actualizarFecha.put("fchhn_ultmod", fechaArticulo)
                                    keAndroid.update(
                                        "tabla_aux",
                                        actualizarFecha,
                                        "tabla = ?",
                                        arrayOf("articulo")
                                    )
                                    contadorart++
                                    keAndroid.setTransactionSuccessful()
                                } catch (e: JSONException) {
                                    e.printStackTrace()
                                    Toast.makeText(
                                        applicationContext,
                                        "Error 15",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } finally {
                                    keAndroid.endTransaction()
                                }
                            }
                            // Toast.makeText(PrincipalActivity.this, "Articulos descargados", Toast.LENGTH_SHORT).show();
                            keAndroid.close()
                            binding.tvArticulos.setTextColor(Color.rgb(62, 197, 58))
                            binding.tvArticulos.text = "Articulos: " + contadorart
                            progressDialog!!.setMessage("Articulos:$contadorart")
                            varAux++
                            progressDialog!!.incrementProgressBy(numBarraProgreso.toInt())
                            sincronizacionVendedor()
                        }
                    } else if (response.getString("articulo") == "null") {
                        //System.out.println("AQUI ANDA");

                        // Toast.makeText(getApplicationContext(), "No se recibieron mas datos", LENGTH_LONG).show(); /* si en la consulta no ncuentra nada
                        //es que el usuario o password estan incorrectos */
                        binding.tvArticulos.setTextColor(Color.rgb(98, 117, 141))
                        binding.tvArticulos.text = "Articulos: Sin actualización"
                        progressDialog!!.setMessage("Articulos: Sin actualización")
                        varAux++
                        progressDialog!!.incrementProgressBy(numBarraProgreso.toInt())
                        sincronizacionVendedor()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { error: VolleyError? ->
                error?.printStackTrace()
                //System.out.println("Este es el error -> "+error);
                //Ingreso de la fecha antes de ser actualizada
                actualizarFechaError(fechaError)

                //--Manejo visual que indica al usuario del error--
                binding.tvArticulos.setTextColor(Color.rgb(232, 17, 35))
                binding.tvArticulos.text = "Articulos: No ha logrado sincronizar"
                progressDialog!!.setMessage("Articulos: No ha logrado sincronizar")
                varAux++
                progressDialog!!.incrementProgressBy(numBarraProgreso.toInt())
                varAuxError = true
                analisisError2()
                //-----
                sincronizacionVendedor()
            }) {
            override fun getParams(): Map<String, String>? {  //finalmente, estos son los parametros que le enviaremos al webservice, partiendo de las variables
                //donde estan guardados las fechas
                val parametros: MutableMap<String, String> = HashMap()
                parametros["fecha_sinc"] = fecha_sinc_articulo!!
                return parametros
            }
        }
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(jsonObjectRequest) //esto es el request que se envia al url a traves de la conexion volley, (el stringrequest esta armado arriba)
    }

    private fun actualizarFechaError(fechaError: String) {
        val actualizarFecha = ContentValues()
        actualizarFecha.put("fchhn_ultmod", fechaError)
        val keAndroid = conn!!.writableDatabase
        keAndroid.update("tabla_aux", actualizarFecha, "tabla = 'articulo'", null)
        keAndroid.close()
    }

    private fun obtenerFechaPreError(tabla: String): String {
        conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null, 8)
        val keAndroid = conn!!.writableDatabase
        val cursor =
            keAndroid.rawQuery("SELECT fchhn_ultmod FROM tabla_aux WHERE tabla = '$tabla'", null)
        cursor.moveToFirst()
        val fechaError = cursor.getString(0)
        cursor.close()
        keAndroid.close()
        return fechaError
    }

    //Funcion que crea un JSON para indicarle a la ase de datos quien y cuando sincronizo
    private fun subirSincronizacion() {
        val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val fecha = dateFormat.format(Date.from(Instant.now()))

        //System.out.println("Fecha actual = " + fecha);
        val jsonObject = JSONObject()
        val sJsonObject = JSONObject()
        try {
            jsonObject.put("usuario", cod_usuario)
            val version = Constantes.VERSION_NAME + " " + Constantes.FECHA_VERSION
            jsonObject.put("version", version)
            jsonObject.put("fecha", fecha)
            sJsonObject.put("sincroonizacion", jsonObject)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        insertarSincronizacion(sJsonObject, fecha)
    }

    //Funcion que sube el JSON de sinronizacion a la base de datos
    private fun insertarSincronizacion(json: JSONObject, fecha: String) {
        println("Sincronizacion -> $json")
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST,
            "http://cloccidental.com:5001/sincroonizacion",
            json,
            { response: JSONObject? ->
                if (response != null) {
                    try {
                        val jsonObject = response.getJSONObject("estado")
                        if (jsonObject.getString("status") == "404") {
                            Toast.makeText(
                                this@SincronizacionActivity,
                                "Error 404",
                                Toast.LENGTH_LONG
                            ).show()
                            varAuxError = true
                        } else if (jsonObject.getString("status") == "200" && jsonObject.getString("usuario") != cod_usuario) {
                            Toast.makeText(
                                this@SincronizacionActivity,
                                "Error inesperado al sincronizar",
                                Toast.LENGTH_LONG
                            ).show()
                            varAuxError = true
                        } else if (jsonObject.getString("status") == "200" && jsonObject.getString("usuario") == cod_usuario) {
                            //System.out.println("Sincronizacion Exitosa");

                            //Guardado de la ultima sincronnizaion en la base de datos
                            val keAndroid = conn!!.writableDatabase
                            try {
                                keAndroid.beginTransaction()
                                val fechaBdd =
                                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(fecha)
                                val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                val strFechaBdd = formatter.format(fechaBdd)
                                val contenedor = ContentValues()
                                contenedor.put("ult_sinc", strFechaBdd)
                                contenedor.put("sinc_primera", 1)
                                keAndroid.update(
                                    "usuarios", contenedor, "vendedor = ?", arrayOf(
                                        cod_usuario
                                    )
                                )
                                keAndroid.setTransactionSuccessful()
                            } catch (exception: Exception) {
                                exception.printStackTrace()
                            } finally {
                                keAndroid.endTransaction()
                                keAndroid.close()
                            }
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }) { error: VolleyError? ->
            error?.printStackTrace()
            Toast.makeText(
                this@SincronizacionActivity,
                "No ha logrado sincronizar",
                Toast.LENGTH_LONG
            ).show()
            varAuxError = true
            analisisError2()
        }
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(jsonObjectRequest)
        varAux++
        sincronizacionVendedor()
    }

    private fun bajarArticulos3(URL: String) {
        //System.out.println("Este es el URL -> " + URL);
        progressDialog!!.setMessage("Sincronizando articulos")
        //OJO AQUI QUE HAY 2 UPDATE Y SI SE ELIMINA 1 SE JODE TODA LA VAINA
        binding.tvArticulos.setTextColor(Color.rgb(41, 184, 214))
        binding.tvArticulos.text = "Articulos: Sincronizando"

        //Fecha tomada para ser coloada en tabla auxiliar en caso de dar un error
        val fechaError = obtenerFechaPreError("articulo")
        contadorart = 0
        //Objeto que baja los articulos
        val jsonObjectRequest =
            JsonObjectRequest(Request.Method.GET, URL, null, { response: JSONObject ->
                try {
                    if (response.getString("articulo") != "null") {
                        val articulo = response.getJSONArray("articulo")
                        for (i in 0 until articulo.length()) {
                            val jsonObject = articulo.getJSONObject(i)
                            codigo = jsonObject.getString("codigo").trim { it <= ' ' }
                            grupo = jsonObject.getString("grupo").trim { it <= ' ' }
                            subgrupo = jsonObject.getString("subgrupo").trim { it <= ' ' }
                            nombre = jsonObject.getString("nombre").trim { it <= ' ' }
                            marca = jsonObject.getString("marca").trim { it <= ' ' }
                            referencia = jsonObject.getString("referencia").trim { it <= ' ' }
                            unidad = jsonObject.getString("unidad").trim { it <= ' ' }
                            precio1 = jsonObject.getDouble("precio1")
                            precio2 = jsonObject.getDouble("precio2")
                            precio3 = jsonObject.getDouble("precio3")
                            precio4 = jsonObject.getDouble("precio4")
                            precio5 = jsonObject.getDouble("precio5")
                            precio6 = jsonObject.getDouble("precio6")
                            precio7 = jsonObject.getDouble("precio7")
                            existencia = jsonObject.getDouble("existencia")
                            fechamodifi = jsonObject.getString("fechamodifi")
                            discont = jsonObject.getDouble("discont")
                            vta_max = jsonObject.getDouble("vta_max")
                            vta_min = jsonObject.getDouble("vta_min")
                            dctotope = jsonObject.getDouble("dctotope")
                            enpreventa = jsonObject.getString("enpreventa").trim { it <= ' ' }
                            comprometido = jsonObject.getString("comprometido")
                            vta_minenx = jsonObject.getString("vta_minenx")
                            val vtaSolofac = jsonObject.getInt("vta_solofac")
                            val vtaSolone = jsonObject.getInt("vta_solone")
                            val cv = ContentValues()
                            cv.put("codigo", codigo)
                            cv.put("grupo", grupo)
                            cv.put("subgrupo", subgrupo)
                            cv.put("nombre", nombre)
                            cv.put("referencia", referencia)
                            cv.put("marca", marca)
                            cv.put("unidad", unidad)
                            cv.put("precio1", precio1)
                            cv.put("precio2", precio2)
                            cv.put("precio3", precio3)
                            cv.put("precio4", precio4)
                            cv.put("precio5", precio5)
                            cv.put("precio6", precio6)
                            cv.put("precio7", precio7)
                            cv.put("discont", discont)
                            cv.put("fechamodifi", fechamodifi)
                            cv.put("existencia", existencia)
                            cv.put("discont", discont)
                            cv.put("vta_max", vta_max)
                            cv.put("vta_min", vta_min)
                            cv.put("dctotope", dctotope)
                            cv.put("enpreventa", enpreventa)
                            cv.put("comprometido", comprometido)
                            cv.put("vta_minenx", vta_minenx)
                            cv.put("vta_solofac", vtaSolofac)
                            cv.put("vta_solone", vtaSolone)
                            if (!conn!!.validarExistencia("articulo", "codigo", codigo!!)) {
                                conn!!.insertJSON("articulo", cv)
                                //System.out.println("INSERT ->" + codigo);
                            } else {
                                conn!!.updateJSON("articulo", cv, "codigo", codigo!!)
                                //System.out.println("UPDATE ->" + codigo);
                            }
                            contadorart++
                        }
                        conn!!.updateTablaAux("articulo")
                        binding.tvArticulos.setTextColor(Color.rgb(62, 197, 58))
                        binding.tvArticulos.text = "Articulos: " + contadorart
                        progressDialog!!.setMessage("Articulos:" + contadorart)
                        varAux++
                        progressDialog!!.incrementProgressBy(numBarraProgreso.toInt())
                        sincronizacionVendedor()
                    } else if (response.getString("articulo") == "null") {
                        binding.tvArticulos.setTextColor(Color.rgb(98, 117, 141))
                        binding.tvArticulos.text = "Articulos: Sin actualización"
                        progressDialog!!.setMessage("Articulos: Sin actualización")
                        varAux++
                        progressDialog!!.incrementProgressBy(numBarraProgreso.toInt())
                        sincronizacionVendedor()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    //System.out.println("Este es el error -> "+error);
                    //Ingreso de la fecha antes de ser actualizada
                    actualizarFechaError(fechaError)

                    //--Manejo visual que indica al usuario del error--
                    binding.tvArticulos.setTextColor(Color.rgb(232, 17, 35))
                    binding.tvArticulos.text = "Articulos: No ha logrado sincronizar"
                    progressDialog!!.setMessage("Articulos: No ha logrado sincronizar")
                    varAux++
                    progressDialog!!.incrementProgressBy(numBarraProgreso.toInt())
                    varAuxError = true
                    analisisError2()
                    //-----
                    sincronizacionVendedor()
                }
            }) { error: VolleyError ->
                error.printStackTrace()
                //System.out.println("Este es el error -> "+error);
                //Ingreso de la fecha antes de ser actualizada
                actualizarFechaError(fechaError)

                //--Manejo visual que indica al usuario del error--
                binding.tvArticulos.setTextColor(Color.rgb(232, 17, 35))
                binding.tvArticulos.text = "Articulos: No ha logrado sincronizar"
                progressDialog!!.setMessage("Articulos: No ha logrado sincronizar")
                varAux++
                progressDialog!!.incrementProgressBy(numBarraProgreso.toInt())
                varAuxError = true
                analisisError2()
                //-----
                sincronizacionVendedor()
            }
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(jsonObjectRequest)
    }

    private fun bajarArticulos2(url: String) {
        //Texto de la ventana de actualizacion de datos
        progressDialog!!.setMessage("Sincronizando articulos")
        binding.tvArticulos.setTextColor(Color.rgb(41, 184, 214))
        binding.tvArticulos.text = "Articulos: Sincronizando"
        //Contador de articulos actualizados
        contadorart = 0
        //Objeto que baja los articulos
        val jsonArrayRequest: JsonArrayRequest = object : JsonArrayRequest(
            url,
            { response: JSONArray? ->  //Funcion si  la respuesta de la API es existosa
                //Verificacion de que la repuesta de la API no sea nula
                if (response != null) {
                    //Variable que guarda la coexion
                    conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null, 8)
                    //Objeto que creara las sentencias SQL
                    val keAndroid = conn!!.writableDatabase
                    //Creacion del objeto JSON que contendra la descomposicion del array JSON enviada por el servidor
                    var jsonObject: JSONObject
                    //Descomposicion del array JSON enviada por el servidor
                    for (i in 0 until response.length()) {
                        //TRY para tratar los movimientos de los datos traidos por el JSON y guardarlos en variables
                        try {
                            //Guardado de una parte del JSON array en un JSON object
                            jsonObject = response.getJSONObject(i)
                            //Guardado en variables de los datos del JSON object
                            codigo = jsonObject.getString("codigo").trim { it <= ' ' }
                            grupo = jsonObject.getString("grupo").trim { it <= ' ' }
                            subgrupo = jsonObject.getString("subgrupo").trim { it <= ' ' }
                            nombre = jsonObject.getString("nombre").trim { it <= ' ' }
                            marca = jsonObject.getString("marca").trim { it <= ' ' }
                            referencia = jsonObject.getString("referencia").trim { it <= ' ' }
                            unidad = jsonObject.getString("unidad").trim { it <= ' ' }
                            precio1 = jsonObject.getDouble("precio1")
                            precio2 = jsonObject.getDouble("precio2")
                            precio3 = jsonObject.getDouble("precio3")
                            precio4 = jsonObject.getDouble("precio4")
                            precio5 = jsonObject.getDouble("precio5")
                            precio6 = jsonObject.getDouble("precio6")
                            precio7 = jsonObject.getDouble("precio7")
                            existencia = jsonObject.getDouble("existencia")
                            fechamodifi = jsonObject.getString("fechamodifi")
                            discont = jsonObject.getDouble("discont")
                            vta_max = jsonObject.getDouble("vta_max")
                            vta_min = jsonObject.getDouble("vta_min")
                            dctotope = jsonObject.getDouble("dctotope")
                            enpreventa = jsonObject.getString("enpreventa").trim { it <= ' ' }
                            comprometido = jsonObject.getString("comprometido")
                            vta_minenx = jsonObject.getString("vta_minenx")

                            //Creacion del objeto contedor (en si un array del tipo MAP que guarda el nombre del campo de la base de datos y el valor de la ariable que guardara en la misma)
                            val contenedor = ContentValues()
                            //LLenado del objeto contenedor
                            contenedor.put("codigo", codigo)
                            contenedor.put("grupo", grupo)
                            contenedor.put("subgrupo", subgrupo)
                            contenedor.put("nombre", nombre)
                            contenedor.put("referencia", referencia)
                            contenedor.put("marca", marca)
                            contenedor.put("unidad", unidad)
                            contenedor.put("precio1", precio1)
                            contenedor.put("precio2", precio2)
                            contenedor.put("precio3", precio3)
                            contenedor.put("precio4", precio4)
                            contenedor.put("precio5", precio5)
                            contenedor.put("precio6", precio6)
                            contenedor.put("precio7", precio7)
                            contenedor.put("discont", discont)
                            contenedor.put("fechamodifi", fechamodifi)
                            contenedor.put("existencia", existencia)
                            contenedor.put("discont", discont)
                            contenedor.put("vta_max", vta_max)
                            contenedor.put("vta_min", vta_min)
                            contenedor.put("dctotope", dctotope)
                            contenedor.put("enpreventa", enpreventa)
                            contenedor.put("comprometido", comprometido)
                            contenedor.put("vta_minenx", vta_minenx)

                            //Creacion del cursor que ejecutara la sentencia SQL que busca ver si el articulo existe o no en la base de datos, buscandolo por medio de su articulo
                            val codigoEnLocal = keAndroid.rawQuery(
                                "SELECT count(codigo), fechamodifi FROM articulo WHERE codigo = '$codigo'",
                                null
                            )
                            //Posicionamiento del cursor sobre su primer resultado
                            codigoEnLocal.moveToFirst()
                            //Guardado en variable del numero de coincidencias encontradas por la busqueda del codigo (1 para indicar que existe)
                            val codigoExistente = codigoEnLocal.getInt(0)
                            val sFechaBdd = codigoEnLocal.getString(1)
                            codigoEnLocal.close()
                            if (codigoExistente == 0) {
                                try {

                                    //System.out.println("El codigo: " + codigo + " hizo INSERT");
                                    keAndroid.beginTransaction()
                                    keAndroid.insert("articulo", null, contenedor)
                                    keAndroid.setTransactionSuccessful()
                                    contadorart++
                                } catch (exception: Exception) {
                                    exception.printStackTrace()
                                } finally {
                                    keAndroid.endTransaction()
                                }
                            } else {
                                val fechaBdd = parseFecha(sFechaBdd)
                                val fechaApi = parseFecha(fechamodifi)
                                if (fechaBdd!!.before(fechaApi)) {
                                    try {

                                        //System.out.println("El codigo: " + codigo + " hizo UPDATE");
                                        keAndroid.beginTransaction()
                                        keAndroid.update(
                                            "articulo", contenedor, "codigo = ?", arrayOf(
                                                codigo
                                            )
                                        )
                                        keAndroid.setTransactionSuccessful()
                                        contadorart++
                                    } catch (exception: Exception) {
                                        exception.printStackTrace()
                                    } finally {
                                        keAndroid.endTransaction()
                                    }
                                } //System.out.println("El codigo: " + codigo + " hizo OUT");
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                    keAndroid.close()
                    binding.tvArticulos.setTextColor(Color.rgb(62, 197, 58))
                    binding.tvArticulos.text = "Articulos: " + contadorart
                    progressDialog!!.setMessage("Articulos:" + contadorart)
                    varAux++
                    progressDialog!!.incrementProgressBy(numBarraProgreso.toInt())
                    sincronizacionVendedor()
                } else {
                    binding.tvArticulos.setTextColor(Color.rgb(98, 117, 141))
                    binding.tvArticulos.text = "Articulos: Sin actualización"
                    progressDialog!!.setMessage("Articulos: Sin actualización")
                    varAux++
                    progressDialog!!.incrementProgressBy(numBarraProgreso.toInt())
                    sincronizacionVendedor()
                }
            },
            Response.ErrorListener { error: VolleyError? ->
                error?.printStackTrace()
                binding.tvArticulos.setTextColor(Color.rgb(98, 117, 141))
                binding.tvArticulos.text = "Articulos: Sin actualización"
                progressDialog!!.setMessage("Articulos: Sin actualización")
                varAux++
                progressDialog!!.incrementProgressBy(numBarraProgreso.toInt())
                sincronizacionVendedor()
            }) {
            override fun getParams(): Map<String, String>? {
                //finalmente, estos son los parametros que le enviaremos al webservice, partiendo
                // de las variables
                //donde estan guardados las fechas
                val parametros: MutableMap<String, String> = HashMap()
                parametros["fecha_sinc"] = fecha_sinc_articulo!!
                return parametros
            }
        }
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(jsonArrayRequest) //esto es el request que se envia al url a traves de la
        // conexion volley, (el stringrequest esta armado arriba)
    }

    private fun calendario(keAndroid: SQLiteDatabase) {
        val fechaArticulo1 = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val fechaArticulo = sdf.format(fechaArticulo1.time)
        val actualizarFecha = ContentValues()
        actualizarFecha.put("fchhn_ultmod", fechaArticulo)
        keAndroid.update("tabla_aux", actualizarFecha, "tabla = ?", arrayOf("articulo"))
        contadorart++
    }

    private fun bajarClientes(url: String) {
        println("Cliente -> $url")
        //final ArrayList<String> documentosBDD = arrayDocumento();
        val clientesNube = ArrayList<String?>()
        progressDialog!!.setMessage("Sincronizando Documentos")
        binding.tvDocumentos.setTextColor(Color.rgb(41, 184, 214))
        binding.tvDocumentos.text = "Documentos: Sincronizando"
        //Fecha tomada para ser coloada en tabla auxiliar en caso de dar un error
        //String fecha_error = ObtenerFechaPreError("limites");
        val keAndroid = conn!!.writableDatabase
        val jsonObjectRequest =
            JsonObjectRequest(Request.Method.GET, url, null, { response: JSONObject ->
                try {
                    if (response.getString("clientes") != "null") {
                        var countDoc = 0
                        val clientes = response.getJSONArray("clientes")
                        for (i in 0 until clientes.length()) {
                            val jsonObject = clientes.getJSONObject(i)
                            try {
                                keAndroid.beginTransaction()
                                codigo = jsonObject.getString("codigo")
                                nombre = jsonObject.getString("nombre")
                                direccion = jsonObject.getString("direccion")
                                telefonos = jsonObject.getString("telefonos")
                                perscont = jsonObject.getString("perscont")
                                vendedor = jsonObject.getString("vendedor")
                                contribespecial = jsonObject.getDouble("contribespecial")
                                status = jsonObject.getDouble("status")
                                sector = jsonObject.getString("sector")
                                subcodigo = jsonObject.getString("subcodigo")
                                fechamodifi = jsonObject.getString("fechamodifi")
                                precio = jsonObject.getDouble("precio")
                                kne_activa = jsonObject.getString("kne_activa")
                                kne_mtomin = jsonObject.getDouble("kne_mtomin")
                                val noemifac = jsonObject.getInt("noemifac")
                                val noeminota = jsonObject.getInt("noeminota")
                                val fchultvta = jsonObject.getString("fchultvta")
                                val mtoultvta = jsonObject.getDouble("mtoultvta")
                                val prcdpagdia = jsonObject.getDouble("prcdpagdia")
                                val promdiasp = jsonObject.getDouble("promdiasp")
                                val riesgocrd = jsonObject.getDouble("riesgocrd")
                                val cantdocs = jsonObject.getDouble("cantdocs")
                                val totmtodocs = jsonObject.getDouble("totmtodocs")
                                val prommtodoc = jsonObject.getDouble("prommtodoc")
                                val diasultvta = jsonObject.getDouble("diasultvta")
                                val promdiasvta = jsonObject.getDouble("promdiasvta")
                                val limcred = jsonObject.getDouble("limcred")
                                val fchcrea = jsonObject.getString("fchcrea")
                                val email = jsonObject.getString("email")
                                clientesNube.add(codigo)
                                val contenedor = ContentValues()
                                contenedor.put("codigo", codigo)
                                contenedor.put("nombre", nombre)
                                contenedor.put("direccion", direccion)
                                contenedor.put("telefonos", telefonos)
                                contenedor.put("perscont", perscont)
                                contenedor.put("vendedor", vendedor)
                                contenedor.put("contribespecial", contribespecial)
                                contenedor.put("status", status)
                                contenedor.put("sector", sector)
                                contenedor.put("subcodigo", subcodigo)
                                contenedor.put("fechamodifi", fechamodifi)
                                contenedor.put("precio", precio)
                                contenedor.put("kne_activa", kne_activa)
                                contenedor.put("kne_mtomin", kne_mtomin)
                                contenedor.put("noemifac", noemifac)
                                contenedor.put("noeminota", noeminota)
                                contenedor.put("fchultvta", fchultvta)
                                contenedor.put("mtoultvta", mtoultvta)
                                contenedor.put("prcdpagdia", prcdpagdia)
                                contenedor.put("promdiasp", promdiasp)
                                contenedor.put("riesgocrd", riesgocrd)
                                contenedor.put("cantdocs", cantdocs)
                                contenedor.put("totmtodocs", totmtodocs)
                                contenedor.put("prommtodoc", prommtodoc)
                                contenedor.put("diasultvta", diasultvta)
                                contenedor.put("promdiasvta", promdiasvta)
                                contenedor.put("limcred", limcred)
                                contenedor.put("fchcrea", fchcrea)
                                contenedor.put("email", email)
                                val qcodigoLocal = keAndroid.rawQuery(
                                    "SELECT count(codigo) FROM cliempre WHERE codigo = '$codigo';",
                                    null
                                )
                                var codigoExistente = 0
                                if (qcodigoLocal.moveToFirst()) {
                                    codigoExistente = qcodigoLocal.getInt(0)
                                }
                                qcodigoLocal.close()
                                if (codigoExistente > 0) {
                                    //System.out.println("UPDATE " + documento);
                                    keAndroid.update(
                                        "cliempre",
                                        contenedor,
                                        "codigo= ?",
                                        arrayOf(codigo)
                                    )
                                } else if (codigoExistente == 0) {
                                    //System.out.println("INSERT " + documento);
                                    keAndroid.insert("cliempre", null, contenedor)
                                }
                                countDoc++
                                keAndroid.setTransactionSuccessful()
                            } catch (e: JSONException) {
                                Toast.makeText(applicationContext, "Error 16", Toast.LENGTH_LONG)
                                    .show()
                                e.printStackTrace()
                            } finally {
                                keAndroid.endTransaction()
                            }
                        }
                        keAndroid.delete("cliempre", "status= ?", arrayOf("2"))
                        eliminarDocViejos(clientesNube, keAndroid, "cliempre", "codigo")
                        binding.tvCliente.setTextColor(Color.rgb(62, 197, 58))
                        binding.tvCliente.text = "Clientes: $countDoc"
                        progressDialog!!.setMessage("Clientes: $countDoc")
                        varAux++
                        progressDialog!!.incrementProgressBy(numBarraProgreso.toInt())
                        sincronizacionVendedor()
                    } else if (response.getString("clientes") == "null") {
                        binding.tvCliente.setTextColor(Color.rgb(98, 117, 141))
                        binding.tvCliente.text = "Clientes: Sin actualización"
                        progressDialog!!.setMessage("Clientes: Sin actualización")
                        varAux++
                        progressDialog!!.incrementProgressBy(numBarraProgreso.toInt())
                        sincronizacionVendedor()
                    }
                } catch (e: Exception) {
                    //System.out.println("Error Bajar Documento -> " + e);
                    e.printStackTrace()
                }
            }) { error: VolleyError? ->
                error?.printStackTrace()
                //System.out.println("Este es el error -> "+error);
                //Ingreso de la fecha antes de ser actualizada
                //ActualizarFechaError(fecha_error);

                //--Manejo visual que indica al usuario del error--
                binding.tvCliente.setTextColor(Color.rgb(232, 17, 35))
                binding.tvCliente.text = "Clientes: No ha logrado sincronizar"
                progressDialog!!.setMessage("Clientes: No ha logrado sincronizar")
                varAux++
                progressDialog!!.incrementProgressBy(numBarraProgreso.toInt())
                varAuxError = true
                analisisError2()
                //-----
                sincronizacionVendedor()
            }
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(jsonObjectRequest)
    }

    //2023-06-07 Se comenta debido a que se va a mejorar para eliminar los clientes que cambian de vendedor, ya que estos permanecian con su antiguo vendedor
    /*private void BajarClientes(String URL) {
        //Fecha tomada para ser coloada en tabla auxiliar en caso de dar un error
        String fecha_error = ObtenerFechaPreError("cliempre");

        System.out.println("CLIENTES ->" + URL);

        contadorcli = 0;

        tv_cliente.setTextColor(Color.rgb(41, 184, 214));
        tv_cliente.setText("Clientes: Sincronizando.");


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(JSONObject response) { //a traves de un json array request, traemos la informacion que viene del webservice

                try {
                    if (!(response.getString("clientes").equals("null"))) { // si la respuesta no viene vacia

                        conn = new AdminSQLiteOpenHelper(getApplicationContext(), "ke_android", null, 1);
                        SQLiteDatabase ke_android = conn.getWritableDatabase();
                        long filas = DatabaseUtils.queryNumEntries(ke_android, "cliempre"); //obtenemos las filas de la tabla articulos para comprobar si hay o no registros

                        JSONArray clientes = response.getJSONArray("clientes");

                        //aqui valido las filas de la tabla de articulos en el telefono
                        if (filas > 0) {
                            JSONObject jsonObject = null; //creamos un objeto json vacio
                            for (int i = 0; i < clientes.length(); i++) { */
    /*pongo todo en el objeto segun lo que venga */ /*
                                try {

                                    ke_android.beginTransaction();

                                    jsonObject = clientes.getJSONObject(i);
                                    codigo = jsonObject.getString("codigo").trim();
                                    nombre = jsonObject.getString("nombre").trim();
                                    direccion = jsonObject.getString("direccion").trim();
                                    telefonos = jsonObject.getString("telefonos").trim();
                                    perscont = jsonObject.getString("perscont").trim();
                                    vendedor = jsonObject.getString("vendedor").trim();
                                    contribespecial = jsonObject.getDouble("contribespecial");
                                    status = jsonObject.getDouble("status");
                                    sector = jsonObject.getString("sector").trim();
                                    subcodigo = jsonObject.getString("subcodigo").trim();
                                    fechamodifi = jsonObject.getString("fechamodifi");
                                    precio = jsonObject.getDouble("precio");
                                    kne_activa = jsonObject.getString("kne_activa");
                                    kne_mtomin = jsonObject.getDouble("kne_mtomin");


                                    ContentValues actualizar = new ContentValues();
                                    actualizar.put("codigo", codigo);
                                    actualizar.put("nombre", nombre);
                                    actualizar.put("direccion", direccion);
                                    actualizar.put("telefonos", telefonos);
                                    actualizar.put("perscont", perscont);
                                    actualizar.put("vendedor", vendedor);
                                    actualizar.put("contribespecial", contribespecial);
                                    actualizar.put("status", status);
                                    actualizar.put("sector", sector);
                                    actualizar.put("subcodigo", subcodigo);
                                    actualizar.put("fechamodifi", fechamodifi);
                                    actualizar.put("precio", precio);
                                    actualizar.put("kne_activa", kne_activa);
                                    actualizar.put("kne_mtomin", kne_mtomin);


                                    ke_android.update("cliempre", actualizar, "codigo = '" + codigo + "'", null);

                                    //actualizamos la fecha de la tabla de
                                    Calendar fecha_cliempre = Calendar.getInstance();
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                    String fechaCliempre = sdf.format(fecha_cliempre.getTime());

                                    ContentValues actualizarFecha = new ContentValues();
                                    actualizarFecha.put("fchhn_ultmod", fechaCliempre);
                                    ke_android.update("tabla_aux", actualizarFecha, "tabla = 'cliempre'", null);

                                    ke_android.setTransactionSuccessful();


                                } catch (JSONException e) {
                                    Toast.makeText(getApplicationContext(), "Error 16", LENGTH_LONG).show();
                                } finally {
                                    ke_android.endTransaction();
                                }


                                Cursor codigo_en_local = ke_android.rawQuery("SELECT count(codigo) FROM cliempre WHERE codigo = '" + codigo + "'", null);
                                codigo_en_local.moveToFirst();
                                int codigo_existente = codigo_en_local.getInt(0);

                                if (codigo_existente > 0) {
                                    try {

                                        ke_android.beginTransaction();

                                        jsonObject = clientes.getJSONObject(i);
                                        codigo = jsonObject.getString("codigo").trim();
                                        nombre = jsonObject.getString("nombre").trim();
                                        direccion = jsonObject.getString("direccion").trim();
                                        telefonos = jsonObject.getString("telefonos").trim();
                                        perscont = jsonObject.getString("perscont").trim();
                                        vendedor = jsonObject.getString("vendedor").trim();
                                        contribespecial = jsonObject.getDouble("contribespecial");
                                        status = jsonObject.getDouble("status");
                                        sector = jsonObject.getString("sector").trim();
                                        subcodigo = jsonObject.getString("subcodigo").trim();
                                        fechamodifi = jsonObject.getString("fechamodifi");
                                        precio = jsonObject.getDouble("precio");
                                        kne_activa = jsonObject.getString("kne_activa");
                                        kne_mtomin = jsonObject.getDouble("kne_mtomin");


                                        ContentValues actualizar = new ContentValues();
                                        actualizar.put("codigo", codigo);
                                        actualizar.put("nombre", nombre);
                                        actualizar.put("direccion", direccion);
                                        actualizar.put("telefonos", telefonos);
                                        actualizar.put("perscont", perscont);
                                        actualizar.put("vendedor", vendedor);
                                        actualizar.put("contribespecial", contribespecial);
                                        actualizar.put("status", status);
                                        actualizar.put("sector", sector);
                                        actualizar.put("subcodigo", subcodigo);
                                        actualizar.put("fechamodifi", fechamodifi);
                                        actualizar.put("precio", precio);
                                        actualizar.put("kne_activa", kne_activa);
                                        actualizar.put("kne_mtomin", kne_mtomin);


                                        ke_android.update("cliempre", actualizar, "codigo = '" + codigo + "'", null);

                                        //actualizamos la fecha de la tabla de
                                        Calendar fecha_cliempre = Calendar.getInstance();
                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                        String fechaCliempre = sdf.format(fecha_cliempre.getTime());

                                        ContentValues actualizarFecha = new ContentValues();
                                        actualizarFecha.put("fchhn_ultmod", fechaCliempre);
                                        ke_android.update("tabla_aux", actualizarFecha, "tabla = 'cliempre'", null);
                                        ke_android.setTransactionSuccessful();
                                        contadorcli++;


                                    } catch (JSONException e) {
                                        Toast.makeText(getApplicationContext(), e.getMessage(), LENGTH_LONG).show();
                                    } finally {
                                        ke_android.endTransaction();
                                    }


                                } else {
                                    try {

                                        ke_android.beginTransaction();

                                        jsonObject = clientes.getJSONObject(i);
                                        codigo = jsonObject.getString("codigo").trim();
                                        nombre = jsonObject.getString("nombre").trim();
                                        direccion = jsonObject.getString("direccion").trim();
                                        telefonos = jsonObject.getString("telefonos").trim();
                                        perscont = jsonObject.getString("perscont").trim();
                                        vendedor = jsonObject.getString("vendedor").trim();
                                        contribespecial = jsonObject.getDouble("contribespecial");
                                        status = jsonObject.getDouble("status");
                                        sector = jsonObject.getString("sector").trim();
                                        subcodigo = jsonObject.getString("subcodigo").trim();
                                        fechamodifi = jsonObject.getString("fechamodifi");
                                        precio = jsonObject.getDouble("precio");
                                        kne_activa = jsonObject.getString("kne_activa");
                                        kne_mtomin = jsonObject.getDouble("kne_mtomin");

                                        ContentValues insertar = new ContentValues();
                                        insertar.put("codigo", codigo);
                                        insertar.put("nombre", nombre);
                                        insertar.put("direccion", direccion);
                                        insertar.put("telefonos", telefonos);
                                        insertar.put("perscont", perscont);
                                        insertar.put("vendedor", vendedor);
                                        insertar.put("contribespecial", contribespecial);
                                        insertar.put("status", status);
                                        insertar.put("sector", sector);
                                        insertar.put("subcodigo", subcodigo);
                                        insertar.put("fechamodifi", fechamodifi);
                                        insertar.put("precio", precio);
                                        insertar.put("kne_activa", kne_activa);
                                        insertar.put("kne_mtomin", kne_mtomin);

                                        ke_android.insert("cliempre", null, insertar);

                                        //actualizamos la fecha de la tabla de
                                        Calendar fecha_cliempre = Calendar.getInstance();
                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                        String fechaCliempre = sdf.format(fecha_cliempre.getTime());

                                        ContentValues actualizarFecha = new ContentValues();
                                        actualizarFecha.put("fchhn_ultmod", fechaCliempre);
                                        ke_android.update("tabla_aux", actualizarFecha, "tabla = 'cliempre'", null);

                                        ke_android.setTransactionSuccessful();
                                        contadorcli++;

                                    } catch (JSONException e) {
                                        Toast.makeText(getApplicationContext(), "Error 17", LENGTH_LONG).show();
                                    } finally {
                                        ke_android.endTransaction();
                                    }


                                }

                            }
                            //  Toast.makeText(PrincipalActivity.this, "Clientes Descargados", Toast.LENGTH_SHORT).show();
                            ke_android.close();

                            tv_cliente.setTextColor(Color.rgb(62, 197, 58));
                            tv_cliente.setText("Clientes: " + contadorcli);
                            progressDialog.setMessage("Clientes: " + contadorcli);
                            varAux++;
                            progressDialog.incrementProgressBy((int) numBarraProgreso);
                            sincronizacionVendedor();

                        } else {

                            //si no hay nada, hago un insert
                            AdminSQLiteOpenHelper conn = new AdminSQLiteOpenHelper(getApplicationContext(), "ke_android", null, 1);
                            ke_android = conn.getWritableDatabase();

                            JSONObject jsonObject = null; //creamos un objeto json vacio
                            for (int i = 0; i < clientes.length(); i++) { */
    /*pongo todo en el objeto segun lo que venga */ /*
                                try {

                                    ke_android.beginTransaction();

                                    jsonObject = clientes.getJSONObject(i);
                                    codigo = jsonObject.getString("codigo").trim();
                                    nombre = jsonObject.getString("nombre").trim();
                                    direccion = jsonObject.getString("direccion").trim();
                                    telefonos = jsonObject.getString("telefonos").trim();
                                    perscont = jsonObject.getString("perscont").trim();
                                    vendedor = jsonObject.getString("vendedor").trim();
                                    contribespecial = jsonObject.getDouble("contribespecial");
                                    status = jsonObject.getDouble("status");
                                    sector = jsonObject.getString("sector").trim();
                                    subcodigo = jsonObject.getString("subcodigo").trim();
                                    fechamodifi = jsonObject.getString("fechamodifi");
                                    precio = jsonObject.getDouble("precio");
                                    kne_activa = jsonObject.getString("kne_activa");
                                    kne_mtomin = jsonObject.getDouble("kne_mtomin");

                                    ContentValues insertar = new ContentValues();
                                    insertar.put("codigo", codigo);
                                    insertar.put("nombre", nombre);
                                    insertar.put("direccion", direccion);
                                    insertar.put("telefonos", telefonos);
                                    insertar.put("perscont", perscont);
                                    insertar.put("vendedor", vendedor);
                                    insertar.put("contribespecial", contribespecial);
                                    insertar.put("status", status);
                                    insertar.put("sector", sector);
                                    insertar.put("subcodigo", subcodigo);
                                    insertar.put("fechamodifi", fechamodifi);
                                    insertar.put("precio", precio);
                                    insertar.put("kne_activa", kne_activa);
                                    insertar.put("kne_mtomin", kne_mtomin);

                                    ke_android.insert("cliempre", null, insertar);

                                    //actualizamos la fecha de la tabla de
                                    Calendar fecha_cliempre = Calendar.getInstance();
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                    String fechaCliempre = sdf.format(fecha_cliempre.getTime());

                                    ContentValues actualizarFecha = new ContentValues();
                                    actualizarFecha.put("fchhn_ultmod", fechaCliempre);
                                    ke_android.update("tabla_aux", actualizarFecha, "tabla = 'cliempre'", null);
                                    ke_android.setTransactionSuccessful();
                                    contadorcli++;

                                } catch (JSONException e) {
                                    Toast.makeText(getApplicationContext(), "Error 18", LENGTH_LONG).show();
                                } finally {
                                    ke_android.endTransaction();
                                }


                            }
                            //  Toast.makeText(PrincipalActivity.this, "Clientes Descargados", Toast.LENGTH_SHORT).show();


                            ke_android.close();

                            tv_cliente.setTextColor(Color.rgb(62, 197, 58));
                            tv_cliente.setText("Clientes: " + contadorcli);
                            progressDialog.setMessage("Clientes: " + contadorcli);
                            varAux++;
                            progressDialog.incrementProgressBy((int) numBarraProgreso);
                            sincronizacionVendedor();
                        }
                    } else if (response.getString("clientes").equals("null")) {

                        //Toast.makeText(getApplicationContext(), "No se recibieron mas datos", LENGTH_LONG).show();
                        */
    /* si en la consulta no ncuentra nada
                        es que el usuario o password estan incorrectos */
    /*


                        tv_cliente.setTextColor(Color.rgb(98, 117, 141));
                        tv_cliente.setText("Clientes: Sin actualización.");
                        progressDialog.setMessage("Clientes: Sin actualización.");
                        varAux++;
                        progressDialog.incrementProgressBy((int) numBarraProgreso);
                        sincronizacionVendedor();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Ingreso de la fecha antes de ser actualizada
                ActualizarFechaError(fecha_error);

                //--Manejo visual que indica al usuario del error--
                tv_cliente.setTextColor(Color.rgb(232, 17, 35));
                tv_cliente.setText("Clientes: No ha logrado sincronizar.");
                progressDialog.setMessage("Clientes: No ha logrado sincronizar.");
                varAux++;
                progressDialog.incrementProgressBy((int) numBarraProgreso);
                varAuxError = true;
                AnalisisError2();
                //------
                sincronizacionVendedor();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {  //finalmente, estos son los parametros que le enviaremos al webservice, partiendo de las variables
                //donde estan guardados las fechas
                Map<String, String> parametros = new HashMap<String, String>();
                parametros.put("cod_usuario", cod_usuario);

                return parametros;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest); //esto es el request que se envia al url a traves de la conexion volley, (el stringrequest esta armado arriba)


    }*/
    private fun bajarGrupos(url: String) {
        //Fecha tomada para ser coloada en tabla auxiliar en caso de dar un error
        val fechaError = obtenerFechaPreError("grupos")
        binding.tvGrupos.setTextColor(Color.rgb(41, 184, 214))
        binding.tvGrupos.text = "Grupos: Sincronizando"
        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
            Method.GET,
            url,
            null,
            { response: JSONObject ->  //a traves de un json array request, traemos la informacion que viene del webservice
                try {
                    if (response.getString("grupos") != "null") { // si la respuesta no viene vacia
                        conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null, 1)
                        var keAndroid = conn!!.writableDatabase
                        val filas = DatabaseUtils.queryNumEntries(
                            keAndroid,
                            "grupos"
                        ) //obtenemos las filas de la tabla articulos para comprobar si hay o no registros
                        val grupos = response.getJSONArray("grupos")

                        //aqui valido las filas de la tabla de sectores en el telefono
                        if (filas > 0) {
                            var jsonObject: JSONObject //creamos un objeto json vacio
                            for (i in 0 until grupos.length()) { /*pongo todo en el objeto segun lo que venga */
                                try {
                                    keAndroid.beginTransaction()
                                    jsonObject = grupos.getJSONObject(i)
                                    codigo = jsonObject.getString("codigo").trim { it <= ' ' }
                                    nombre = jsonObject.getString("nombre").trim { it <= ' ' }
                                    fechamodifi = jsonObject.getString("fechamodifi")
                                    val actualizar = ContentValues()
                                    actualizar.put("codigo", codigo)
                                    actualizar.put("nombre", nombre)
                                    actualizar.put("fechamodifi", fechamodifi)
                                    keAndroid.update(
                                        "grupos",
                                        actualizar,
                                        "codigo = '$codigo'",
                                        null
                                    )
                                    //actualizamos la fecha de la tabla de
                                    val fechaGrupos = Calendar.getInstance()
                                    val sdf = SimpleDateFormat(
                                        "yyyy-MM-dd'T'HH:mm:ss",
                                        Locale.getDefault()
                                    )
                                    val fechagrupos = sdf.format(fechaGrupos.time)
                                    val actualizarFecha = ContentValues()
                                    actualizarFecha.put("fchhn_ultmod", fechagrupos)
                                    keAndroid.update(
                                        "tabla_aux",
                                        actualizarFecha,
                                        "tabla = 'grupos'",
                                        null
                                    )
                                    keAndroid.setTransactionSuccessful()
                                } catch (e: JSONException) {
                                    Toast.makeText(
                                        applicationContext,
                                        "Error 19",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } finally {
                                    keAndroid.endTransaction()
                                }
                                val codigoEnLocal = keAndroid.rawQuery(
                                    "SELECT count(codigo) FROM grupos WHERE codigo = '$codigo'",
                                    null
                                )
                                codigoEnLocal.moveToFirst()
                                val codigoExistente = codigoEnLocal.getInt(0)
                                codigoEnLocal.close()
                                if (codigoExistente > 0) {
                                    try {
                                        keAndroid.beginTransaction()
                                        jsonObject = grupos.getJSONObject(i)
                                        codigo = jsonObject.getString("codigo").trim { it <= ' ' }
                                        nombre = jsonObject.getString("nombre").trim { it <= ' ' }
                                        fechamodifi = jsonObject.getString("fechamodifi")
                                        val actualizar = ContentValues()
                                        actualizar.put("codigo", codigo)
                                        actualizar.put("nombre", nombre)
                                        actualizar.put("fechamodifi", fechamodifi)
                                        keAndroid.update(
                                            "grupos",
                                            actualizar,
                                            "codigo = '$codigo'",
                                            null
                                        )
                                        //actualizamos la fecha de la tabla de
                                        val fechaGrupos = Calendar.getInstance()
                                        val sdf = SimpleDateFormat(
                                            "yyyy-MM-dd'T'HH:mm:ss",
                                            Locale.getDefault()
                                        )
                                        val fechagrupos = sdf.format(fechaGrupos.time)
                                        val actualizarFecha = ContentValues()
                                        actualizarFecha.put("fchhn_ultmod", fechagrupos)
                                        keAndroid.update(
                                            "tabla_aux",
                                            actualizarFecha,
                                            "tabla = 'grupos'",
                                            null
                                        )
                                        keAndroid.setTransactionSuccessful()
                                    } catch (e: JSONException) {
                                        Toast.makeText(
                                            applicationContext,
                                            e.message,
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } finally {
                                        keAndroid.endTransaction()
                                    }
                                } else {
                                    try {
                                        keAndroid.beginTransaction()
                                        jsonObject = grupos.getJSONObject(i)
                                        codigo = jsonObject.getString("codigo").trim { it <= ' ' }
                                        nombre = jsonObject.getString("nombre").trim { it <= ' ' }
                                        fechamodifi = jsonObject.getString("fechamodifi")
                                        val insertar = ContentValues()
                                        insertar.put("codigo", codigo)
                                        insertar.put("nombre", nombre)
                                        insertar.put("fechamodifi", fechamodifi)
                                        keAndroid.insert("grupos", null, insertar)

                                        //actualizamos la fecha de la tabla de
                                        val fechaGrupos = Calendar.getInstance()
                                        val sdf = SimpleDateFormat(
                                            "yyyy-MM-dd'T'HH:mm:ss",
                                            Locale.getDefault()
                                        )
                                        val fechagrupos = sdf.format(fechaGrupos.time)
                                        val actualizarFecha = ContentValues()
                                        actualizarFecha.put("fchhn_ultmod", fechagrupos)
                                        keAndroid.update(
                                            "tabla_aux",
                                            actualizarFecha,
                                            "tabla = 'grupos'",
                                            null
                                        )
                                        keAndroid.setTransactionSuccessful()
                                    } catch (e: JSONException) {
                                        Toast.makeText(
                                            applicationContext,
                                            "Error 20",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } finally {
                                        keAndroid.endTransaction()
                                    }
                                }
                            }
                            /// Toast.makeText(PrincipalActivity.this, "grupos Descargados", Toast.LENGTH_SHORT).show();
                            keAndroid.close()
                            //  Clientes.setEnabled(true);
                            binding.tvGrupos.setTextColor(Color.rgb(62, 197, 58))
                            binding.tvGrupos.text = "Grupos: Sincronizado"
                            progressDialog!!.setMessage("Grupos act.")
                            varAux++
                            progressDialog!!.incrementProgressBy(numBarraProgreso.toInt())
                            sincronizacionVendedor()
                        } else {

                            //si no hay nada, hago un insert
                            val conn =
                                AdminSQLiteOpenHelper(applicationContext, "ke_android", null, 1)
                            keAndroid = conn.writableDatabase
                            var jsonObject: JSONObject //creamos un objeto json vacio
                            for (i in 0 until grupos.length()) { /*pongo todo en el objeto segun lo que venga */
                                try {
                                    keAndroid.beginTransaction()
                                    jsonObject = grupos.getJSONObject(i)
                                    codigo = jsonObject.getString("codigo").trim { it <= ' ' }
                                    nombre = jsonObject.getString("nombre").trim { it <= ' ' }
                                    fechamodifi = jsonObject.getString("fechamodifi")
                                    val insertar = ContentValues()
                                    insertar.put("codigo", codigo)
                                    insertar.put("nombre", nombre)
                                    insertar.put("fechamodifi", fechamodifi)
                                    keAndroid.insert("grupos", null, insertar)

                                    //actualizamos la fecha de la tabla de
                                    val fechaGrupos = Calendar.getInstance()
                                    val sdf = SimpleDateFormat(
                                        "yyyy-MM-dd'T'HH:mm:ss",
                                        Locale.getDefault()
                                    )
                                    val fechagrupos = sdf.format(fechaGrupos.time)
                                    val actualizarFecha = ContentValues()
                                    actualizarFecha.put("fchhn_ultmod", fechagrupos)
                                    keAndroid.update(
                                        "tabla_aux",
                                        actualizarFecha,
                                        "tabla = 'grupos'",
                                        null
                                    )
                                    keAndroid.setTransactionSuccessful()
                                } catch (e: JSONException) {
                                    Toast.makeText(
                                        applicationContext,
                                        "Error 21",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } finally {
                                    keAndroid.endTransaction()
                                }
                            }
                            //   Toast.makeText(PrincipalActivity.this, "grupos Descargados",
                            //   Toast.LENGTH_SHORT).show();
                            keAndroid.close()
                            // Clientes.setEnabled(true);
                            binding.tvGrupos.setTextColor(Color.rgb(62, 197, 58))
                            binding.tvGrupos.text = "Grupos: Sincronizado"
                            progressDialog!!.setMessage("Grupos act.")
                            varAux++
                            progressDialog!!.incrementProgressBy(numBarraProgreso.toInt())
                            sincronizacionVendedor()
                        }
                    } else if (response.getString("grupos") == "null") {

                        //Toast.makeText(getApplicationContext(), "No se recibieron mas datos", LENGTH_LONG).show();
                        /* si en la consulta no ncuentra nada
                    es que el usuario o password estan incorrectos */

                        //  Clientes.setEnabled(true);
                        binding.tvGrupos.setTextColor(Color.rgb(98, 117, 141))
                        binding.tvGrupos.text = "Grupos: Sin actualización"
                        progressDialog!!.setMessage("Grupos: Sin actualización")
                        varAux++
                        progressDialog!!.incrementProgressBy(numBarraProgreso.toInt())
                        sincronizacionVendedor()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { error: VolleyError? ->
                error?.printStackTrace()
                //Ingreso de la fecha antes de ser actualizada
                actualizarFechaError(fechaError)

                // Clientes.setEnabled(true);
                //--Manejo visual que indica al usuario del error--
                binding.tvGrupos.setTextColor(Color.rgb(232, 17, 35))
                binding.tvGrupos.text = "Grupos: No ha logrado sincronizar"
                progressDialog!!.setMessage("Grupos: No ha logrado sincronizar")
                varAux++
                progressDialog!!.incrementProgressBy(numBarraProgreso.toInt())
                varAuxError = true
                analisisError2()
                //------
                sincronizacionVendedor()
            }) {
            override fun getParams(): Map<String, String>? {
                //finalmente, estos son los parametros que le enviaremos al webservice,
                // partiendo de las variables
                //donde estan guardados las fechas
                return HashMap()
            }
        }
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(jsonObjectRequest)
        //esto es el request que se envia al url a traves de la conexion volley,
        // (el stringrequest esta armado arriba)
    }

    //-----------------------------------------------------------------------------------------------------------------------
    private fun bajarSubGrupos(url: String) {
        //Fecha tomada para ser coloada en tabla auxiliar en caso de dar un error
        val fechaError = obtenerFechaPreError("subgrupos")
        binding.tvSubgrupos.setTextColor(Color.rgb(41, 184, 214))
        binding.tvSubgrupos.text = "Info. Adcional: Sincronizando."
        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
            Method.GET,
            url,
            null,
            { response: JSONObject ->  //a traves de un json array request, traemos la informacion que viene del webservice
                try {
                    if (response.getString("subgrupo") != "null") { // si la respuesta no viene vacia
                        conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null, 1)
                        var keAndroid = conn!!.writableDatabase
                        val filas = DatabaseUtils.queryNumEntries(
                            keAndroid,
                            "subgrupos"
                        ) //obtenemos las filas de la tabla articulos para comprobar si hay o no registros
                        val subgrupoArray = response.getJSONArray("subgrupo")

                        //aqui valido las filas de la tabla de articulos en el telefono
                        if (filas > 0) {
                            var jsonObject: JSONObject //creamos un objeto json vacio
                            for (i in 0 until subgrupoArray.length()) { /*pongo todo en el objeto segun lo que venga */
                                try {
                                    keAndroid.beginTransaction()
                                    jsonObject = subgrupoArray.getJSONObject(i)
                                    codigo = jsonObject.getString("codigo").trim { it <= ' ' }
                                    subcodigo = jsonObject.getString("subcodigo").trim { it <= ' ' }
                                    nombre = jsonObject.getString("nombre").trim { it <= ' ' }
                                    fechamodifi = jsonObject.getString("fechamodifi")
                                    val actualizar = ContentValues()
                                    actualizar.put("codigo", codigo)
                                    actualizar.put("nombre", nombre)
                                    actualizar.put("subcodigo", subcodigo)
                                    actualizar.put("fechamodifi", fechamodifi)
                                    keAndroid.update(
                                        "subgrupos",
                                        actualizar,
                                        "codigo = '$codigo'",
                                        null
                                    )


                                    //actualizamos la fecha de la tabla de
                                    val fechaSubgrupos = Calendar.getInstance()
                                    val sdf = SimpleDateFormat(
                                        "yyyy-MM-dd'T'HH:mm:ss",
                                        Locale.getDefault()
                                    )
                                    val fechasubgrupos = sdf.format(fechaSubgrupos.time)
                                    val actualizarFecha = ContentValues()
                                    actualizarFecha.put("fchhn_ultmod", fechasubgrupos)
                                    keAndroid.update(
                                        "tabla_aux",
                                        actualizarFecha,
                                        "tabla = 'subgrupos'",
                                        null
                                    )
                                    keAndroid.setTransactionSuccessful()
                                } catch (e: JSONException) {
                                    Toast.makeText(
                                        applicationContext,
                                        "Error 22",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } finally {
                                    keAndroid.endTransaction()
                                }
                                val codigoEnLocal = keAndroid.rawQuery(
                                    "SELECT count(codigo) FROM subgrupos WHERE codigo = '$codigo'",
                                    null
                                )
                                codigoEnLocal.moveToFirst()
                                val codigoExistente = codigoEnLocal.getInt(0)
                                codigoEnLocal.close()
                                if (codigoExistente > 0) {
                                    try {
                                        keAndroid.beginTransaction()
                                        jsonObject = subgrupoArray.getJSONObject(i)
                                        codigo = jsonObject.getString("codigo").trim { it <= ' ' }
                                        subcodigo =
                                            jsonObject.getString("subcodigo").trim { it <= ' ' }
                                        nombre = jsonObject.getString("nombre").trim { it <= ' ' }
                                        fechamodifi = jsonObject.getString("fechamodifi")
                                        val actualizar = ContentValues()
                                        actualizar.put("codigo", codigo)
                                        actualizar.put("nombre", nombre)
                                        actualizar.put("subcodigo", subcodigo)
                                        actualizar.put("fechamodifi", fechamodifi)
                                        keAndroid.update(
                                            "subgrupos",
                                            actualizar,
                                            "codigo = '$codigo'",
                                            null
                                        )


                                        //actualizamos la fecha de la tabla de
                                        val fechaSubgrupos = Calendar.getInstance()
                                        val sdf = SimpleDateFormat(
                                            "yyyy-MM-dd'T'HH:mm:ss",
                                            Locale.getDefault()
                                        )
                                        val fechasubgrupos = sdf.format(fechaSubgrupos.time)
                                        val actualizarFecha = ContentValues()
                                        actualizarFecha.put("fchhn_ultmod", fechasubgrupos)
                                        keAndroid.update(
                                            "tabla_aux",
                                            actualizarFecha,
                                            "tabla = 'subgrupos'",
                                            null
                                        )
                                        keAndroid.setTransactionSuccessful()
                                    } catch (e: JSONException) {
                                        Toast.makeText(
                                            applicationContext,
                                            "Error 23",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } finally {
                                        keAndroid.endTransaction()
                                    }
                                } else {
                                    try {
                                        keAndroid.beginTransaction()
                                        jsonObject = subgrupoArray.getJSONObject(i)
                                        codigo = jsonObject.getString("codigo").trim { it <= ' ' }
                                        subcodigo =
                                            jsonObject.getString("subcodigo").trim { it <= ' ' }
                                        nombre = jsonObject.getString("nombre").trim { it <= ' ' }
                                        fechamodifi = jsonObject.getString("fechamodifi")
                                        val insertar = ContentValues()
                                        insertar.put("codigo", codigo)
                                        insertar.put("subcodigo", subcodigo)
                                        insertar.put("nombre", nombre)
                                        insertar.put("fechamodifi", fechamodifi)
                                        keAndroid.insert("subgrupos", null, insertar)

                                        //actualizamos la fecha de la tabla de
                                        val fechaSubgrupos = Calendar.getInstance()
                                        val sdf = SimpleDateFormat(
                                            "yyyy-MM-dd'T'HH:mm:ss",
                                            Locale.getDefault()
                                        )
                                        val fechasubgrupos = sdf.format(fechaSubgrupos.time)
                                        val actualizarFecha = ContentValues()
                                        actualizarFecha.put("fchhn_ultmod", fechasubgrupos)
                                        keAndroid.update(
                                            "tabla_aux",
                                            actualizarFecha,
                                            "tabla = 'subgrupos'",
                                            null
                                        )
                                        keAndroid.setTransactionSuccessful()
                                    } catch (e: JSONException) {
                                        Toast.makeText(
                                            applicationContext,
                                            "Error 24",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } finally {
                                        keAndroid.endTransaction()
                                    }
                                }
                            }
                            keAndroid.close()
                            //  Catalogo.setEnabled(true);
                            binding.tvSubgrupos.setTextColor(Color.rgb(62, 197, 58))
                            binding.tvSubgrupos.text = "Info. Adicional: Sincronizado."
                            progressDialog!!.setMessage("Info. Adicional: Sincronizado.")
                            varAux++
                            progressDialog!!.incrementProgressBy(numBarraProgreso.toInt())
                            sincronizacionVendedor()
                        } else {

                            //si no hay nada, hago un insert
                            val conn =
                                AdminSQLiteOpenHelper(applicationContext, "ke_android", null, 1)
                            keAndroid = conn.writableDatabase
                            var jsonObject: JSONObject //creamos un objeto json vacio
                            for (i in 0 until subgrupoArray.length()) { /*pongo todo en el objeto segun lo que venga */
                                try {
                                    keAndroid.beginTransaction()
                                    jsonObject = subgrupoArray.getJSONObject(i)
                                    codigo = jsonObject.getString("codigo").trim { it <= ' ' }
                                    subcodigo = jsonObject.getString("subcodigo").trim { it <= ' ' }
                                    nombre = jsonObject.getString("nombre").trim { it <= ' ' }
                                    fechamodifi = jsonObject.getString("fechamodifi")
                                    val insertar = ContentValues()
                                    insertar.put("codigo", codigo)
                                    insertar.put("subcodigo", subcodigo)
                                    insertar.put("nombre", nombre)
                                    insertar.put("fechamodifi", fechamodifi)
                                    keAndroid.insert("subgrupos", null, insertar)

                                    //actualizamos la fecha de la tabla de
                                    val fechaSubgrupos = Calendar.getInstance()
                                    val sdf = SimpleDateFormat(
                                        "yyyy-MM-dd'T'HH:mm:ss",
                                        Locale.getDefault()
                                    )
                                    val fechasubgrupos = sdf.format(fechaSubgrupos.time)
                                    val actualizarFecha = ContentValues()
                                    actualizarFecha.put("fchhn_ultmod", fechasubgrupos)
                                    keAndroid.update(
                                        "tabla_aux",
                                        actualizarFecha,
                                        "tabla = 'subgrupos'",
                                        null
                                    )
                                    keAndroid.setTransactionSuccessful()
                                } catch (e: JSONException) {
                                    Toast.makeText(
                                        applicationContext,
                                        "Error 25",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } finally {
                                    keAndroid.endTransaction()
                                }
                            }
                            //   Toast.makeText(PrincipalActivity.this, "SubGrupos descargados",
                            //   Toast.LENGTH_SHORT).show();
                            keAndroid.close()
                            // Catalogo.setEnabled(true);
                            binding.tvSubgrupos.setTextColor(Color.rgb(62, 197, 58))
                            binding.tvSubgrupos.text = "Info. Adicional: Sincronizado."
                            progressDialog!!.setMessage("Info. Adicional: Sincronizado.")
                            varAux++
                            progressDialog!!.incrementProgressBy(numBarraProgreso.toInt())
                            sincronizacionVendedor()
                        }
                    } else if (response.getString("subgrupo") == "null") {

                        //Toast.makeText(getApplicationContext(), "No se recibieron mas datos", LENGTH_LONG).show();
                        /* si en la consulta no ncuentra nada
                    es que el usuario o password estan incorrectos */

                        //    Catalogo.setEnabled(true);
                        binding.tvSubgrupos.setTextColor(Color.rgb(98, 117, 141))
                        binding.tvSubgrupos.text = "Info. Adicional: Sin actualización"
                        progressDialog!!.setMessage("Info. Adicional: Sin actualización.")
                        varAux++
                        progressDialog!!.incrementProgressBy(numBarraProgreso.toInt())
                        sincronizacionVendedor()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { error: VolleyError? ->
                if (error != null) {
                    error.printStackTrace()
                }
                //Ingreso de la fecha antes de ser actualizada
                actualizarFechaError(fechaError)

                //Catalogo.setEnabled(true);
                //--Manejo visual que indica al usuario del error--
                binding.tvSubgrupos.setTextColor(Color.rgb(232, 17, 35))
                binding.tvSubgrupos.text = "Info. Adicional: No ha logrado sincronizar"
                progressDialog!!.setMessage("Info. Adicional: No ha logrado sincronizar")
                varAux++
                progressDialog!!.incrementProgressBy(numBarraProgreso.toInt())
                varAuxError = true
                analisisError2()
                //------
                sincronizacionVendedor()
            }) {
            override fun getParams(): Map<String, String>? {
                //finalmente, estos son los parametros que le enviaremos al webservice,
                // partiendo de las variables
                //donde estan guardados las fechas
                //  parametros.put("fecha_sinc", fecha_sinc);
                return HashMap()
            }
        }
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(jsonObjectRequest)
        //esto es el request que se envia al url a traves de la conexion volley,
        // (el stringrequest esta armado arriba)
    }

    //---------------------------------------------------------------------------------------------------
    private fun bajarSectores(url: String) {
        //Fecha tomada para ser coloada en tabla auxiliar en caso de dar un error
        val fechaError = obtenerFechaPreError("sectores")
        binding.tvSector.setTextColor(Color.rgb(41, 184, 214))
        binding.tvSector.text = "Zona: Sincronizando."
        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
            Method.GET,
            url,
            null,
            { response: JSONObject ->  //a traves de un json array request, traemos la informacion
                // que viene del webservice
                try {
                    if (response.getString("sector") != "null") { // si la respuesta no viene vacia
                        conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null, 1)
                        var keAndroid = conn!!.writableDatabase
                        val filas = DatabaseUtils.queryNumEntries(
                            keAndroid,
                            "sectores"
                        ) //obtenemos las filas de la tabla articulos para comprobar si hay o no registros
                        val sector = response.getJSONArray("sector")

                        //aqui valido las filas de la tabla de sectores en el telefono
                        if (filas > 0) {
                            var jsonObject: JSONObject //creamos un objeto json vacio
                            for (i in 0 until sector.length()) { /*pongo todo en el objeto segun lo que venga */
                                try {
                                    keAndroid.beginTransaction()
                                    jsonObject = sector.getJSONObject(i)
                                    codigo = jsonObject.getString("codigo").trim { it <= ' ' }
                                    zona = jsonObject.getString("zona").trim { it <= ' ' }
                                    fechamodifi = jsonObject.getString("fechamodifi")
                                    val actualizar = ContentValues()
                                    actualizar.put("codigo", codigo)
                                    actualizar.put("zona", zona)
                                    actualizar.put("fechamodifi", fechamodifi)
                                    keAndroid.update(
                                        "sectores",
                                        actualizar,
                                        "codigo = '$codigo'",
                                        null
                                    )
                                    //actualizamos la fecha de la tabla de
                                    val fechaSectores1 = Calendar.getInstance()
                                    val sdf = SimpleDateFormat(
                                        "yyyy-MM-dd'T'HH:mm:ss",
                                        Locale.getDefault()
                                    )
                                    val fechaSectores = sdf.format(fechaSectores1.time)
                                    val actualizarFecha = ContentValues()
                                    actualizarFecha.put("fchhn_ultmod", fechaSectores)
                                    keAndroid.update(
                                        "tabla_aux",
                                        actualizarFecha,
                                        "tabla = 'sectores'",
                                        null
                                    )
                                    keAndroid.setTransactionSuccessful()
                                } catch (e: JSONException) {
                                    Toast.makeText(
                                        applicationContext,
                                        "Error 26",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } finally {
                                    keAndroid.endTransaction()
                                }
                                val codigoEnLocal = keAndroid.rawQuery(
                                    "SELECT count(codigo) FROM sectores WHERE codigo = '$codigo'",
                                    null
                                )
                                codigoEnLocal.moveToFirst()
                                val codigoExistente = codigoEnLocal.getInt(0)
                                codigoEnLocal.close()
                                if (codigoExistente > 0) {
                                    try {
                                        keAndroid.beginTransaction()
                                        jsonObject = sector.getJSONObject(i)
                                        codigo = jsonObject.getString("codigo").trim { it <= ' ' }
                                        zona = jsonObject.getString("zona").trim { it <= ' ' }
                                        fechamodifi = jsonObject.getString("fechamodifi")
                                        val actualizar = ContentValues()
                                        actualizar.put("codigo", codigo)
                                        actualizar.put("zona", zona)
                                        actualizar.put("fechamodifi", fechamodifi)
                                        keAndroid.update(
                                            "sectores",
                                            actualizar,
                                            "codigo = '$codigo'",
                                            null
                                        )
                                        //actualizamos la fecha de la tabla de
                                        val fechaSectores1 = Calendar.getInstance()
                                        val sdf = SimpleDateFormat(
                                            "yyyy-MM-dd'T'HH:mm:ss",
                                            Locale.getDefault()
                                        )
                                        val fechaSectores = sdf.format(fechaSectores1.time)
                                        val actualizarFecha = ContentValues()
                                        actualizarFecha.put("fchhn_ultmod", fechaSectores)
                                        keAndroid.update(
                                            "tabla_aux",
                                            actualizarFecha,
                                            "tabla = 'sectores'",
                                            null
                                        )
                                        keAndroid.setTransactionSuccessful()
                                    } catch (e: JSONException) {
                                        Toast.makeText(
                                            applicationContext,
                                            "Error 27",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } finally {
                                        keAndroid.endTransaction()
                                    }
                                } else {
                                    try {
                                        keAndroid.beginTransaction()
                                        jsonObject = sector.getJSONObject(i)
                                        codigo = jsonObject.getString("codigo").trim { it <= ' ' }
                                        zona = jsonObject.getString("zona").trim { it <= ' ' }
                                        fechamodifi = jsonObject.getString("fechamodifi")
                                        val insertar = ContentValues()
                                        insertar.put("codigo", codigo)
                                        insertar.put("zona", zona)
                                        insertar.put("fechamodifi", fechamodifi)
                                        keAndroid.insert("sectores", null, insertar)

                                        //actualizamos la fecha de la tabla de
                                        val fechaSectores1 = Calendar.getInstance()
                                        val sdf = SimpleDateFormat(
                                            "yyyy-MM-dd'T'HH:mm:ss",
                                            Locale.getDefault()
                                        )
                                        val fechaSectores = sdf.format(fechaSectores1.time)
                                        val actualizarFecha = ContentValues()
                                        actualizarFecha.put("fchhn_ultmod", fechaSectores)
                                        keAndroid.update(
                                            "tabla_aux",
                                            actualizarFecha,
                                            "tabla = 'sectores'",
                                            null
                                        )
                                        keAndroid.setTransactionSuccessful()
                                    } catch (e: JSONException) {
                                        Toast.makeText(
                                            applicationContext,
                                            "Error 28",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } finally {
                                        keAndroid.endTransaction()
                                    }
                                }
                            }
                            // Toast.makeText(PrincipalActivity.this, "Sectores Descargados",
                            // Toast.LENGTH_SHORT).show();
                            keAndroid.close()
                            binding.tvSector.setTextColor(Color.rgb(62, 197, 58))
                            binding.tvSector.text = "Zona: Sincronizado."
                            progressDialog!!.setMessage("Zona: Sincronizado.")
                            varAux++
                            progressDialog!!.incrementProgressBy(numBarraProgreso.toInt())
                            sincronizacionVendedor()
                        } else {

                            //si no hay nada, hago un insert
                            val conn =
                                AdminSQLiteOpenHelper(applicationContext, "ke_android", null, 3)
                            keAndroid = conn.writableDatabase
                            var jsonObject: JSONObject //creamos un objeto json vacio
                            for (i in 0 until sector.length()) { /*pongo todo en el objeto segun lo que venga */
                                try {
                                    keAndroid.beginTransaction()
                                    jsonObject = sector.getJSONObject(i)
                                    codigo = jsonObject.getString("codigo").trim { it <= ' ' }
                                    zona = jsonObject.getString("zona").trim { it <= ' ' }
                                    fechamodifi = jsonObject.getString("fechamodifi")
                                    val insertar = ContentValues()
                                    insertar.put("codigo", codigo)
                                    insertar.put("zona", zona)
                                    insertar.put("fechamodifi", fechamodifi)
                                    keAndroid.insert("sectores", null, insertar)

                                    //actualizamos la fecha de la tabla de
                                    val fechaSectores1 = Calendar.getInstance()
                                    val sdf = SimpleDateFormat(
                                        "yyyy-MM-dd'T'HH:mm:ss",
                                        Locale.getDefault()
                                    )
                                    val fechaSectores = sdf.format(fechaSectores1.time)
                                    val actualizarFecha = ContentValues()
                                    actualizarFecha.put("fchhn_ultmod", fechaSectores)
                                    keAndroid.update(
                                        "tabla_aux",
                                        actualizarFecha,
                                        "tabla = 'sectores'",
                                        null
                                    )
                                    keAndroid.setTransactionSuccessful()
                                } catch (e: JSONException) {
                                    Toast.makeText(
                                        applicationContext,
                                        "Error 29",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } finally {
                                    keAndroid.endTransaction()
                                }
                            }
                            // Toast.makeText(PrincipalActivity.this, "Sectores Descargados", Toast.LENGTH_SHORT).show();
                            keAndroid.close()
                            binding.tvSector.setTextColor(Color.rgb(62, 197, 58))
                            binding.tvSector.text = "Zona: Sincronizado."
                            progressDialog!!.setMessage("Zona: Sincronizado.")
                            varAux++
                            progressDialog!!.incrementProgressBy(numBarraProgreso.toInt())
                            sincronizacionVendedor()
                        }
                    } else if (response.getString("sector") == "null") {

                        //Toast.makeText(getApplicationContext(), "No se recibieron mas datos", LENGTH_LONG).show(); /* si en la consulta no ncuentra nada
                        // es que el usuario o password estan incorrectos */
                        binding.tvSector.setTextColor(Color.rgb(98, 117, 141))
                        binding.tvSector.text = "Zona: Sin actualización."
                        progressDialog!!.setMessage("Zona: Sin actualización.")
                        varAux++
                        progressDialog!!.incrementProgressBy(numBarraProgreso.toInt())
                        sincronizacionVendedor()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { error: VolleyError? ->
                error?.printStackTrace()
                //Ingreso de la fecha antes de ser actualizada
                actualizarFechaError(fechaError)

                //--Manejo visual que indica al usuario del error--
                binding.tvSector.setTextColor(Color.rgb(232, 17, 35))
                binding.tvSector.text = "Zona: No ha logrado sincronizar"
                progressDialog!!.setMessage("Zona: No ha logrado sincronizar")
                varAux++
                progressDialog!!.incrementProgressBy(numBarraProgreso.toInt())
                //varAuxError = true;
                //AnalisisError2();
                //------
                sincronizacionVendedor()
            }) {
            override fun getParams(): Map<String, String>? {
                //finalmente, estos son los parametros que le enviaremos al webservice, partiendo
                // de las variables
                //donde estan guardados las fechas
                val parametros: MutableMap<String, String> = HashMap()
                parametros["cod_usuario"] = cod_usuario!!
                return parametros
            }
        }
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(jsonObjectRequest)
        //esto es el request que se envia al url a traves de la conexion volley, (el stringrequest
        // esta armado arriba)
    }

    private fun bajarSubSectores(url: String) {
        //Fecha tomada para ser coloada en tabla auxiliar en caso de dar un error
        val fechaError = obtenerFechaPreError("subsectores")
        binding.tvSubsector.setTextColor(Color.rgb(41, 184, 214))
        binding.tvSubsector.text = "Ruta: Sincronizando."
        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
            Method.GET,
            url,
            null,
            { response: JSONObject ->  //a traves de un json array request, traemos la informacion
                // que viene del webservice
                try {
                    if (response.getString("subsector") != "null") { // si la respuesta no viene vacia
                        conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null, 1)
                        var keAndroid = conn!!.writableDatabase
                        val filas = DatabaseUtils.queryNumEntries(
                            keAndroid,
                            "subsectores"
                        ) //obtenemos las filas de la tabla articulos para comprobar si hay o no registros
                        val subsectorArray = response.getJSONArray("subsector")

                        //aqui valido las filas de la tabla de articulos en el telefono
                        if (filas > 0) {
                            var jsonObject: JSONObject //creamos un objeto json vacio
                            for (i in 0 until subsectorArray.length()) { /*pongo todo en el objeto segun lo que venga */
                                try {
                                    keAndroid.beginTransaction()
                                    jsonObject = subsectorArray.getJSONObject(i)
                                    codigo = jsonObject.getString("codigo").trim { it <= ' ' }
                                    subcodigo = jsonObject.getString("subcodigo").trim { it <= ' ' }
                                    subsector = jsonObject.getString("subsector").trim { it <= ' ' }
                                    fechamodifi = jsonObject.getString("fechamodifi")
                                    val actualizar = ContentValues()
                                    actualizar.put("codigo", codigo)
                                    actualizar.put("subsector", subsector)
                                    actualizar.put("subcodigo", subcodigo)
                                    actualizar.put("fechamodifi", fechamodifi)
                                    keAndroid.update(
                                        "subsectores",
                                        actualizar,
                                        "codigo = '$codigo'",
                                        null
                                    )
                                    //actualizamos la fecha de la tabla de
                                    val fechaSubsectores = Calendar.getInstance()
                                    val sdf = SimpleDateFormat(
                                        "yyyy-MM-dd'T'HH:mm:ss",
                                        Locale.getDefault()
                                    )
                                    val fechasubsectores = sdf.format(fechaSubsectores.time)
                                    val actualizarFecha = ContentValues()
                                    actualizarFecha.put("fchhn_ultmod", fechasubsectores)
                                    keAndroid.update(
                                        "tabla_aux",
                                        actualizarFecha,
                                        "tabla = 'subsectores'",
                                        null
                                    )
                                    keAndroid.setTransactionSuccessful()
                                } catch (e: JSONException) {
                                    Toast.makeText(
                                        applicationContext,
                                        "Error 30",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } finally {
                                    keAndroid.endTransaction()
                                }
                                val codigoEnLocal = keAndroid.rawQuery(
                                    "SELECT count(codigo) FROM subsectores WHERE codigo = '$codigo'",
                                    null
                                )
                                codigoEnLocal.moveToFirst()
                                val codigoExistente = codigoEnLocal.getInt(0)
                                codigoEnLocal.close()
                                if (codigoExistente > 0) {
                                    try {
                                        keAndroid.beginTransaction()
                                        jsonObject = subsectorArray.getJSONObject(i)
                                        codigo = jsonObject.getString("codigo").trim { it <= ' ' }
                                        subcodigo =
                                            jsonObject.getString("subcodigo").trim { it <= ' ' }
                                        subsector =
                                            jsonObject.getString("subsector").trim { it <= ' ' }
                                        fechamodifi = jsonObject.getString("fechamodifi")
                                        val actualizar = ContentValues()
                                        actualizar.put("codigo", codigo)
                                        actualizar.put("subsector", subsector)
                                        actualizar.put("subcodigo", subcodigo)
                                        actualizar.put("fechamodifi", fechamodifi)
                                        keAndroid.update(
                                            "subsectores",
                                            actualizar,
                                            "codigo = '$codigo'",
                                            null
                                        )
                                        //actualizamos la fecha de la tabla de
                                        val fechaSubsectores = Calendar.getInstance()
                                        val sdf = SimpleDateFormat(
                                            "yyyy-MM-dd'T'HH:mm:ss",
                                            Locale.getDefault()
                                        )
                                        val fechasubsectores = sdf.format(fechaSubsectores.time)
                                        val actualizarFecha = ContentValues()
                                        actualizarFecha.put("fchhn_ultmod", fechasubsectores)
                                        keAndroid.update(
                                            "tabla_aux",
                                            actualizarFecha,
                                            "tabla = 'subsectores'",
                                            null
                                        )
                                        keAndroid.setTransactionSuccessful()
                                    } catch (e: JSONException) {
                                        Toast.makeText(
                                            applicationContext,
                                            "Error 31",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } finally {
                                        keAndroid.endTransaction()
                                    }
                                } else {
                                    try {
                                        keAndroid.beginTransaction()
                                        jsonObject = subsectorArray.getJSONObject(i)
                                        codigo = jsonObject.getString("codigo").trim { it <= ' ' }
                                        subcodigo =
                                            jsonObject.getString("subcodigo").trim { it <= ' ' }
                                        subsector =
                                            jsonObject.getString("subsector").trim { it <= ' ' }
                                        fechamodifi = jsonObject.getString("fechamodifi")
                                        val insertar = ContentValues()
                                        insertar.put("codigo", codigo)
                                        insertar.put("subsector", subsector)
                                        insertar.put("subcodigo", subcodigo)
                                        insertar.put("fechamodifi", fechamodifi)
                                        keAndroid.insert("subsectores", null, insertar)

                                        //actualizamos la fecha de la tabla de
                                        val fechaSubsectores = Calendar.getInstance()
                                        val sdf = SimpleDateFormat(
                                            "yyyy-MM-dd'T'HH:mm:ss",
                                            Locale.getDefault()
                                        )
                                        val fechasubsectores = sdf.format(fechaSubsectores.time)
                                        val actualizarFecha = ContentValues()
                                        actualizarFecha.put("fchhn_ultmod", fechasubsectores)
                                        keAndroid.update(
                                            "tabla_aux",
                                            actualizarFecha,
                                            "tabla = 'subsectores'",
                                            null
                                        )
                                        keAndroid.setTransactionSuccessful()
                                    } catch (e: JSONException) {
                                        Toast.makeText(
                                            applicationContext,
                                            e.message,
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } finally {
                                        keAndroid.endTransaction()
                                    }
                                }
                            }
                            keAndroid.close()
                            binding.tvSubsector.setTextColor(Color.rgb(62, 197, 58))
                            binding.tvSubsector.text = "Ruta: Sincronizado."
                            progressDialog!!.setMessage("Ruta: Sincronizado.")
                            varAux++
                            progressDialog!!.incrementProgressBy(numBarraProgreso.toInt())
                            sincronizacionVendedor()
                        } else {

                            //si no hay nada, hago un insert
                            val conn =
                                AdminSQLiteOpenHelper(applicationContext, "ke_android", null, 1)
                            keAndroid = conn.writableDatabase
                            var jsonObject: JSONObject //creamos un objeto json vacio
                            for (i in 0 until subsectorArray.length()) { /*pongo todo en el objeto segun lo que venga */
                                try {
                                    keAndroid.beginTransaction()
                                    jsonObject = subsectorArray.getJSONObject(i)
                                    codigo = jsonObject.getString("codigo").trim { it <= ' ' }
                                    subcodigo = jsonObject.getString("subcodigo").trim { it <= ' ' }
                                    subsector = jsonObject.getString("subsector").trim { it <= ' ' }
                                    fechamodifi = jsonObject.getString("fechamodifi")
                                    val insertar = ContentValues()
                                    insertar.put("codigo", codigo)
                                    insertar.put("subsector", subsector)
                                    insertar.put("subcodigo", subcodigo)
                                    insertar.put("fechamodifi", fechamodifi)
                                    keAndroid.insert("subsectores", null, insertar)

                                    //actualizamos la fecha de la tabla de
                                    val fechaSubsectores = Calendar.getInstance()
                                    val sdf = SimpleDateFormat(
                                        "yyyy-MM-dd'T'HH:mm:ss",
                                        Locale.getDefault()
                                    )
                                    val fechasubsectores = sdf.format(fechaSubsectores.time)
                                    val actualizarFecha = ContentValues()
                                    actualizarFecha.put("fchhn_ultmod", fechasubsectores)
                                    keAndroid.update(
                                        "tabla_aux",
                                        actualizarFecha,
                                        "tabla = 'subsectores'",
                                        null
                                    )
                                    keAndroid.setTransactionSuccessful()
                                } catch (e: JSONException) {
                                    Toast.makeText(
                                        applicationContext,
                                        "Error 32",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } finally {
                                    keAndroid.endTransaction()
                                }
                            }
                            //Toast.makeText(PrincipalActivity.this, "Subsectores descargados",
                            // Toast.LENGTH_SHORT).show();
                            keAndroid.close()
                            binding.tvSubsector.setTextColor(Color.rgb(62, 197, 58))
                            binding.tvSubsector.text = "Ruta: Sincronizado."
                            progressDialog!!.setMessage("Ruta: Sincronizado.")
                            varAux++
                            progressDialog!!.incrementProgressBy(numBarraProgreso.toInt())
                            sincronizacionVendedor()
                        }
                    } else if (response.getString("subsector") == "null") {

                        //Toast.makeText(getApplicationContext(), "No se recibieron mas datos", LENGTH_LONG).show();
                        /* si en la consulta no ncuentra nada
                    es que el usuario o password estan incorrectos */
                        binding.tvSubsector.setTextColor(Color.rgb(98, 117, 141))
                        binding.tvSubsector.text = "Ruta: Sin actualización."
                        progressDialog!!.setMessage("Ruta: Sin actualización.")
                        varAux++
                        progressDialog!!.incrementProgressBy(numBarraProgreso.toInt())
                        sincronizacionVendedor()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { error: VolleyError? ->
                if (error != null) {
                    error.printStackTrace()
                }
                //Ingreso de la fecha antes de ser actualizada
                actualizarFechaError(fechaError)

                //--Manejo visual que indica al usuario del error--
                binding.tvSubsector.setTextColor(Color.rgb(232, 17, 35))
                binding.tvSubsector.text = "Ruta: No ha logrado sincronizar"
                progressDialog!!.setMessage("Ruta: No ha logrado sincronizar")
                varAux++
                progressDialog!!.incrementProgressBy(numBarraProgreso.toInt())
                //varAuxError = true;
                //AnalisisError2();
                //------
                sincronizacionVendedor()
            }) {
            override fun getParams(): Map<String, String>? {
                //finalmente, estos son los parametros que le enviaremos al webservice,
                // partiendo de las variables
                //donde estan guardados las fechas
                val parametros: MutableMap<String, String> = HashMap()
                parametros["fecha_sinc"] = fecha_sinc!!
                return parametros
            }
        }
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(jsonObjectRequest)
        //esto es el request que se envia al url a traves de la conexion volley, (el stringrequest esta
        // armado arriba)
    }

    private fun InsertarCorrelativo() {
        val keAndroid = conn!!.writableDatabase
        val cursor = keAndroid.rawQuery(
            "SELECT * FROM ke_correla WHERE kco_vendedor ='$cod_usuario'",
            null
        )
        if (cursor.moveToFirst()) {
            //System.out.println("EL VENDEDOR YA POSEE CORRELATIVOS");
        } else {
            val insertar = ContentValues()
            insertar.put("kco_numero", 0)
            insertar.put("kco_vendedor", cod_usuario)
            keAndroid.insert("ke_correla", null, insertar)
        }
        cursor.close()
    }

    fun subirPedidos() {
        //Inicializacion de la conexion
        val keAndroid = conn!!.writableDatabase
        //Ejecucion del query que busca los pedidos que no se hayan subido
        cursorti =
            keAndroid.rawQuery("SELECT kti_codcli, kti_codven, kti_docsol, kti_condicion, kti_tdoc, kti_ndoc, kti_tipprec, kti_nombrecli, kti_totneto, kti_fchdoc, kti_status, fechamodifi FROM ke_opti WHERE kti_status = '0'  AND kti_codven ='" + cod_usuario!!.trim { it <= ' ' } + "'",
                null)
        //If que analiza si existen pedidos aun no subidos
        if (cursorti.moveToFirst()) {
            //En el caso que si hayan pedidos por subir
            cargarPedidos()
        } else {
            //En el caso en el que no hayan pedidos por subir
            Toast.makeText(
                this@SincronizacionActivity,
                "No hay pedidos por cargar",
                Toast.LENGTH_SHORT
            ).show()
            binding.btnSubir.isEnabled = true
        }
    }

    private fun subirPrecob() {
        val keAndroid = conn!!.writableDatabase
        //Busqueda de datos en la base de datos del tlf para validar la existencia de nuevas cobranzas por subir
        val cursorpc =
            keAndroid.rawQuery("SELECT cxcndoc FROM ke_precobranza WHERE (edorec = '0' OR edorec = '9' OR edorec = '3') AND codvend ='" + cod_usuario!!.trim { it <= ' ' } + "'",
                null)
        //IF para que valida la existencia de esas nuevas obranzas con  ayuda del cursor
        if (cursorpc.moveToFirst()) {
            //Funcion para crear el JSON
            cargarPrecob()
        } else {
            //Mensaje en caso de no haber nuevas cobranzas por subir
            Toast.makeText(
                this@SincronizacionActivity,
                "No hay precobranza por cargar",
                Toast.LENGTH_SHORT
            ).show()
            binding.btnSubirprecob.isEnabled = true
        }
        cursorpc.close()
    }

    private fun cargarPrecob() {
        var contadorPrecob = 0
        //Boton para subir la precobranza
        binding.btnSubirprecob.isEnabled = false
        binding.btnSubirprecob.setBackgroundColor(Color.rgb(242, 238, 238))
        val keAndroid = conn!!.writableDatabase
        //Creacion del ursor y la sentencia SQL que ejecutara la busqueda de cada una de las abeeras de los documentos por cobrar
        val cursorRc: Cursor =
            keAndroid.rawQuery("SELECT * FROM ke_precobranza WHERE (edorec = '0' OR edorec = '9' OR edorec = '3') AND codvend ='" + cod_usuario!!.trim { it <= ' ' } + "'",
                null)
        //System.out.println("SELECT * FROM ke_precobranza WHERE (edorec = '0' OR edorec = '9' OR edorec = '3') AND codvend ='" + cod_usuario.trim() + "'");
        //Creacion del JSON Array que contendra las precobranzas
        arrayCH = JSONArray()
        //while que reorera todos los casos positivos de la sentencia SQL ejeutada
        while (cursorRc.moveToNext()) {
            //Creacion de los Objetos JSON Cabecera y SuperCabecera
            val objetoCabecera = JSONObject()
            val objetoSCabecera = JSONObject()
            //TRY para la creacion del JSON completo
            try {
                //Guardado en variables de los valores enontrados en la base de datos
                /*cxcndoc = cursorRc.getString(0);
                System.out.println("numero rec: " + cxcndoc);
                tiporecibo = cursorRc.getString(1);
                codvend = cursorRc.getString(2);
                nro_recibo = cursorRc.getString(3);
                kecxc_id = cursorRc.getString(4);
                tasadia = cursorRc.getDouble(5);
                fchrecibo = cursorRc.getString(6);
                clicontesp = cursorRc.getString(7);
                bsneto = cursorRc.getDouble(8);
                bsiva = cursorRc.getDouble(9);
                bsretiva = cursorRc.getDouble(10);
                bsflete = cursorRc.getDouble(11);
                bstotal = cursorRc.getDouble(12);
                dolneto = cursorRc.getDouble(13);
                doliva = cursorRc.getDouble(14);
                dolretiva = cursorRc.getDouble(15);
                dolflete = cursorRc.getDouble(16);
                doltotal = cursorRc.getDouble(17);
                moneda = cursorRc.getString(18);
                dctoaplic = cursorRc.getDouble(19);
                netocob = cursorRc.getDouble(20);
                efectivo = cursorRc.getDouble(21);
                bcoecod = cursorRc.getString(22);
                bcocod = cursorRc.getString(23);
                bconombre = cursorRc.getString(24);
                fchr_dep = cursorRc.getString(25);
                bcomonto = cursorRc.getDouble(26);
                bcoref = cursorRc.getString(27);
                edorec = cursorRc.getString(28);
                fchhr = cursorRc.getString(29);
                fchvigen = cursorRc.getString(30);
                bsretflete = cursorRc.getDouble(31);
                diasvigen = cursorRc.getDouble(32);
                retmun_sbi = cursorRc.getDouble(33);
                retmun_sbs = cursorRc.getDouble(34);
                reci_doc = cursorRc.getString(35);
                fechamodifi = cursorRc.getString(36);*/
                cxcndoc = cursorRc.getString(0)
                //Guardado de los valores en el objeto JSON de la Cabecera
                objetoCabecera.put("cxcndoc", cursorRc.getString(0))
                objetoCabecera.put("tiporecibo", cursorRc.getString(1))
                objetoCabecera.put("codvend", cursorRc.getString(2))
                objetoCabecera.put("nro_recibo", cursorRc.getString(3))
                objetoCabecera.put("kecxc_id", cursorRc.getString(4))
                objetoCabecera.put("tasadia", cursorRc.getDouble(5))
                objetoCabecera.put("fchrecibo", cursorRc.getString(6))
                objetoCabecera.put("clicontesp", cursorRc.getString(7))
                objetoCabecera.put("bsneto", cursorRc.getDouble(8))
                objetoCabecera.put("bsiva", cursorRc.getDouble(9))
                objetoCabecera.put("bsretiva", cursorRc.getDouble(10))
                objetoCabecera.put("bsflete", cursorRc.getDouble(11))
                objetoCabecera.put("bstotal", cursorRc.getDouble(12))
                objetoCabecera.put("dolneto", cursorRc.getDouble(13))
                objetoCabecera.put("doliva", cursorRc.getDouble(14))
                objetoCabecera.put("dolretiva", cursorRc.getDouble(15))
                objetoCabecera.put("dolflete", cursorRc.getDouble(16))
                objetoCabecera.put("doltotal", cursorRc.getDouble(17))
                objetoCabecera.put("moneda", cursorRc.getString(18))
                objetoCabecera.put("docdifcamb", cursorRc.getString(19))
                objetoCabecera.put("ddc_age", cursorRc.getString(20))
                objetoCabecera.put("ddc_tipo", cursorRc.getString(21))
                objetoCabecera.put("ddc_montobs", cursorRc.getDouble(22))
                objetoCabecera.put("ddc_doc", cursorRc.getString(23))
                objetoCabecera.put("dctoaplic", cursorRc.getDouble(24))
                objetoCabecera.put("netocob", cursorRc.getDouble(25))
                objetoCabecera.put("concepto", cursorRc.getString(26))
                objetoCabecera.put("efectivo", cursorRc.getDouble(27))
                objetoCabecera.put("bcoecod", cursorRc.getString(28))
                objetoCabecera.put("bcocod", cursorRc.getString(29))
                objetoCabecera.put("bconombre", cursorRc.getString(30))
                objetoCabecera.put("fchr_dep", cursorRc.getString(31))
                objetoCabecera.put("bcomonto", cursorRc.getDouble(32))
                objetoCabecera.put("bcoref", cursorRc.getString(33))
                objetoCabecera.put("pidvalid", cursorRc.getString(34))
                objetoCabecera.put("edorec", cursorRc.getString(35))
                objetoCabecera.put("edocomiv", cursorRc.getString(36))
                objetoCabecera.put("prccomiv", cursorRc.getDouble(37))
                objetoCabecera.put("mtocomiv", cursorRc.getDouble(38))
                objetoCabecera.put("fchr_pcomv", cursorRc.getString(39))
                objetoCabecera.put("codcoord", cursorRc.getString(40))
                objetoCabecera.put("edocomic", cursorRc.getString(41))
                objetoCabecera.put("prccomic", cursorRc.getDouble(42))
                objetoCabecera.put("mtocomic", cursorRc.getDouble(43))
                objetoCabecera.put("fchr_pcomc", cursorRc.getString(44))
                objetoCabecera.put("fchhr", cursorRc.getString(45))
                objetoCabecera.put("fchvigen", cursorRc.getString(46))
                objetoCabecera.put("bsretflete", cursorRc.getDouble(47))
                objetoCabecera.put("diasvigen", cursorRc.getDouble(48))
                objetoCabecera.put("retmun_sbi", cursorRc.getDouble(49))
                objetoCabecera.put("retmun_sbs", cursorRc.getDouble(50))
                objetoCabecera.put("comiaut", cursorRc.getString(51))
                objetoCabecera.put("comiautpor", cursorRc.getString(52))
                objetoCabecera.put("comiautfch", cursorRc.getString(53))
                objetoCabecera.put("reci_age", cursorRc.getString(54))
                objetoCabecera.put("reci_doc", cursorRc.getString(55))
                objetoCabecera.put("status", cursorRc.getString(56))
                objetoCabecera.put("fechamodifi", cursorRc.getString(57))
                objetoCabecera.put("cxcndoc_aux", cursorRc.getString(58))

                //Cursor que ejecuta un SQL para ver las lineas de una cobranza en speifico para armar las lineas del documento
                val cursorRl = keAndroid.rawQuery(
                    "SELECT * FROM ke_precobradocs WHERE cxcndoc ='$cxcndoc'",
                    null
                )
                //System.out.println("SELECT * FROM ke_precobradocs WHERE cxcndoc ='" + cxcndoc + "'");

                //Creacion del JSON Array que contendra todas las lineas de un documento
                arrayCL = JSONArray()
                //WHILE que reccorera todos los casos positivos de la sentencia anterior
                while (cursorRl.moveToNext()) {
                    //Creacion del Objeto JSON para las lineas del documento
                    val objetoLineas = JSONObject()
                    //Guardado de los valores de la base de datos en las variables
                    /*cxcndoc = cursorRl.getString(0);
                    agencia = cursorRl.getString(1);
                    tipodoc = cursorRl.getString(2);
                    documento = cursorRl.getString(3);
                    bscobro = cursorRl.getDouble(4);
                    prcdsctopp = cursorRl.getDouble(5);
                    nroret = cursorRl.getString(6);
                    fchemiret = cursorRl.getString(7);
                    bsretiva = cursorRl.getDouble(8);
                    refret = cursorRl.getString(9);
                    nroretfte = cursorRl.getString(10);
                    fchemirfte = cursorRl.getString(11);
                    bsmtofte = cursorRl.getDouble(12);
                    bsretfte = cursorRl.getDouble(13);
                    refretfte = cursorRl.getString(14);
                    bsmtoiva = cursorRl.getDouble(15);
                    retmun_bi = cursorRl.getDouble(16);
                    retmun_cod = cursorRl.getString(17);
                    retmun_nro = cursorRl.getString(18);
                    retmun_mto = cursorRl.getDouble(19);
                    retmun_fch = cursorRl.getString(20);
                    retmun_ref = cursorRl.getString(21);
                    diascalc = cursorRl.getDouble(22);*/

                    //Guardado de los valoes en la estructura del JSON de las lineas del documento
                    objetoLineas.put("cxcndoc", cursorRl.getString(0))
                    objetoLineas.put("agencia", cursorRl.getString(1))
                    objetoLineas.put("tipodoc", cursorRl.getString(2))
                    objetoLineas.put("documento", cursorRl.getString(3))
                    objetoLineas.put("bscobro", cursorRl.getDouble(4))
                    objetoLineas.put("prccobro", cursorRl.getDouble(5))
                    objetoLineas.put("prcdsctopp", cursorRl.getDouble(6))
                    objetoLineas.put("nroret", cursorRl.getString(7))
                    objetoLineas.put("fchemiret", cursorRl.getString(8))
                    objetoLineas.put("bsretiva", cursorRl.getDouble(9))
                    objetoLineas.put("refret", cursorRl.getString(10))
                    objetoLineas.put("nroretfte", cursorRl.getString(11))
                    objetoLineas.put("fchemirfte", cursorRl.getString(12))
                    objetoLineas.put("bsmtofte", cursorRl.getDouble(13))
                    objetoLineas.put("bsretfte", cursorRl.getDouble(14))
                    objetoLineas.put("refretfte", cursorRl.getString(15))
                    objetoLineas.put("pidvalid", cursorRl.getString(16))
                    objetoLineas.put("bsmtoiva", cursorRl.getDouble(17))
                    objetoLineas.put("retmun_bi", cursorRl.getDouble(18))
                    objetoLineas.put("retmun_cod", cursorRl.getString(19))
                    objetoLineas.put("retmun_nro", cursorRl.getString(20))
                    objetoLineas.put("retmun_mto", cursorRl.getDouble(21))
                    objetoLineas.put("retmun_fch", cursorRl.getString(22))
                    objetoLineas.put("retmun_ref", cursorRl.getString(23))
                    objetoLineas.put("diascalc", cursorRl.getDouble(24))
                    objetoLineas.put("prccomiv", cursorRl.getDouble(25))
                    objetoLineas.put("prccomic", cursorRl.getDouble(26))
                    objetoLineas.put("cxcndoc_aux", cursorRl.getString(27))
                    objetoLineas.put("tnetodbs", cursorRl.getDouble(28))
                    objetoLineas.put("tnetoddol", cursorRl.getDouble(29))
                    objetoLineas.put("fchrecibod", cursorRl.getString(30))
                    objetoLineas.put("kecxc_idd", cursorRl.getString(31))
                    objetoLineas.put("tasadiad", cursorRl.getDouble(32))
                    objetoLineas.put("afavor", cursorRl.getDouble(33))
                    //System.out.println("LA FECHA --> "+ cursorRl.getString(23));
                    //Guardado del objeto JSON Lineas en el JSON Array
                    arrayCL!!.put(objetoLineas)
                }
                cursorRl.close()

                //Insercion del array de las Lineas en el Objeto JSON de la cabecera
                objetoCabecera.put("Lineas", arrayCL)
                //Insercion del objeto cabecera en el Objeto JSON super cabecera
                objetoSCabecera.put("Cabecera", objetoCabecera)
                //Capturador de errores
            } catch (ex: Exception) {
                ex.printStackTrace()
                println("Error al cargar datos del recibo$ex")
                Toast.makeText(
                    this@SincronizacionActivity,
                    "Error al cargar datos del recibo$ex",
                    Toast.LENGTH_SHORT
                ).show()
            }
            //Insercion de la super cabeera en el array de la precobranza
            arrayCH!!.put(objetoSCabecera)
            contadorPrecob++
            //tv_pedidossubidos.setText("Cargando pedido: " + contadorPedidos +" de " + cursorti.getCount());
        }
        cursorRc.close()
        //Creacion de Objeto JSON que contendra todo
        val jsonREC = JSONObject() //vamos a hacer un solo objeto de tipo json
        try {
            //Guardado del array Cobranza con su asignacion de nombre dentro del JSON
            jsonREC.put("Cobranza", arrayCH)
        } catch (e: JSONException) {
            e.printStackTrace()
            // Toast.makeText(SincronizacionActivity.this, "Error al cargar el pedido" + e, Toast.LENGTH_SHORT).show();
        }

        try {
            //Funcion para el envio del JSON
            insertarCobranza(jsonREC)
        } catch (exc: Exception) {
            exc.printStackTrace()
            // Toast.makeText(SincronizacionActivity.this, "Error al cargar el pedido" + exc, Toast.LENGTH_SHORT).show();
        }
    }

    /* Pedidos version 2 */ //Funcion que arma el JSON
    private fun cargarPedidos() {
        var contadorPedidos = 0
        //Bloqueo del boton
        binding.btnSubir.isEnabled = false
        binding.btnSubir.setBackgroundColor(Color.rgb(242, 238, 238))
        //Inicializacion de la conexion y ejecucion del query
        val keAndroid = conn!!.writableDatabase
        cursorti =
            keAndroid.rawQuery("SELECT DISTINCT kti_codcli, kti_codven, kti_docsol, kti_condicion, kti_tdoc, kti_ndoc, kti_tipprec, kti_nombrecli, kti_totneto, kti_fchdoc, kti_status, fechamodifi, kti_negesp FROM ke_opti WHERE kti_status = '0'  AND kti_codven ='" + cod_usuario!!.trim { it <= ' ' } + "'",
                null)
        //Creacion del array JSON
        arrayTi = JSONArray()

        //While que recorreo todas las columnas del query anterior
        while (cursorti.moveToNext()) {
            //Creacion del objeto JSON para la abecera y la super cabecera
            val objetoSCabecera = JSONObject()
            val objetoCabecera = JSONObject()
            //Try para la ejecucion de la creacion del todo el JSON
            try {

                //Llenando las variables con cada campo del query enviado
                kti_codcli = cursorti.getString(0)
                kti_codven = cursorti.getString(1)
                kti_docsolicitado = cursorti.getString(2)
                kti_condicion = cursorti.getString(3)
                kti_tdoc = cursorti.getString(4)
                kti_ndoc = cursorti.getString(5)
                kti_tipprec = cursorti.getDouble(6)
                tmp_nombrecli = cursorti.getString(7)
                kti_totneto = cursorti.getDouble(8)
                kti_fchdoc = cursorti.getString(9)
                kti_status = "1"
                kti_fechamodifi = cursorti.getString(11)
                kti_negesp = cursorti.getString(12)

                //Creacion de diccionario JSON
                objetoCabecera.put("kti_codcli", kti_codcli)
                objetoCabecera.put("kti_codven", kti_codven)
                objetoCabecera.put("kti_docsol", kti_docsolicitado)
                objetoCabecera.put("kti_condicion", kti_condicion)
                objetoCabecera.put("kti_tdoc", kti_tdoc)
                objetoCabecera.put("kti_ndoc", kti_ndoc)
                objetoCabecera.put("kti_tipprec", kti_tipprec)
                objetoCabecera.put("kti_nombrecli", tmp_nombrecli)
                objetoCabecera.put("kti_totneto", kti_totneto)
                objetoCabecera.put("kti_fchdoc", kti_fchdoc)
                objetoCabecera.put("kti_status", kti_status)
                objetoCabecera.put("fechamodifi", kti_fechamodifi)
                objetoCabecera.put("kti_negesp", kti_negesp)
                //Query para la solicitud de las lineas del pedido que se esta procesando
                val cursormv = keAndroid.rawQuery(
                    "SELECT kmv_codart, kmv_nombre, kti_tipprec, kmv_cant, kti_tdoc, kti_ndoc, kmv_stot, kmv_artprec, kmv_dctolin FROM ke_opmv WHERE kti_ndoc ='$kti_ndoc'",
                    null
                )
                //Creacion del array JSON que contendra los objetos JSON para las lineas
                arrayMV = JSONArray()
                //While que recorreo todas las columnas del query anterior
                while (cursormv.moveToNext()) {
                    //Creacion del objeto JSON para las lineas
                    val objetoLineas = JSONObject()
                    kmv_codart = cursormv.getString(0)
                    kmv_nombre = cursormv.getString(1)
                    kti_tipprec = cursormv.getDouble(2)
                    kmv_cant = cursormv.getDouble(3)
                    kti_tdoc = cursormv.getString(4)
                    kti_ndoc = cursormv.getString(5)
                    kmv_stot = cursormv.getDouble(6)
                    kmv_artprec = cursormv.getDouble(7)
                    kmv_dctolin = cursormv.getDouble(8)
                    //Armado del Objeto JSON de las lineas
                    objetoLineas.put("kmv_codart", kmv_codart)
                    objetoLineas.put("kmv_nombre", kmv_nombre)
                    objetoLineas.put("kti_tipprec", kti_tipprec)
                    objetoLineas.put("kmv_cant", kmv_cant)
                    objetoLineas.put("kti_tdoc", kti_tdoc)
                    objetoLineas.put("kti_ndoc", kti_ndoc)
                    objetoLineas.put("kmv_stot", kmv_stot)
                    objetoLineas.put("kmv_artprec", kmv_artprec)
                    objetoLineas.put("kmv_dctolin", kmv_dctolin)
                    //Llenando el array JSON de los Objetos JSON de las lineas
                    arrayMV!!.put(objetoLineas)
                }
                cursormv.close()
                //Insercion del array de las Lineas en el Objeto JSON de la abecera
                objetoCabecera.put("Lineas", arrayMV)
                //Insercion del objeto cabecera en el Objeto JSON super cabecera
                objetoSCabecera.put("Cabecera", objetoCabecera)
                //Catch para la camtura de errores
            } catch (e: JSONException) {
                e.printStackTrace()
                Toast.makeText(
                    this@SincronizacionActivity,
                    "Error al cargar el pedido$e",
                    Toast.LENGTH_SHORT
                ).show()
            }
            //Insercion del objeto JSON super cabeera en el array JSON TI
            arrayTi!!.put(objetoSCabecera)
            //Suma de pedidos procesados y muestra en pantalla
            contadorPedidos++
            binding.tvSubidospedidos.text =
                "Cargando pedido: " + contadorPedidos + " de " + cursorti.getCount()
        }
        //System.out.println(arrayTi);
        //  System.out.println(arrayMV);
        //Objeto JSON que contiene todo el JSON
        val jsonPE = JSONObject() //vamos a hacer un solo objeto de tipo json
        try {
            //Creacion de Objeto JSON pedido que contiene todo el JSON
            jsonPE.put("Pedido", arrayTi)
        } catch (e: JSONException) {
            e.printStackTrace()
            // Toast.makeText(SincronizacionActivity.this, "Error al cargar el pedido" + e, Toast.LENGTH_SHORT).show();
        }
        val jsonStrPE = jsonPE.toString()
        try {
            //Envio de pedidos
            insertarPedido(jsonPE)
        } catch (exc: Exception) {
            exc.printStackTrace()
            // Toast.makeText(SincronizacionActivity.this, "Error al cargar el pedido" + exc, Toast.LENGTH_SHORT).show();
        }
    }

    private fun cargarLimites() {
        arrayLimite = JSONArray()
        val keAndroid = conn!!.writableDatabase
        cursorLim =
            keAndroid.rawQuery("SELECT " + "kli_track, " + "kli_codven, " + "kli_codcli, " + "kli_codart, " + "kli_cant, " + "kli_fechahizo, " + "kli_fechavence " + "FROM ke_limitart" + " WHERE status = '1'  " + "AND kli_fechahizo >'" + fecha_sinc_limites + "'" + "AND kli_codven = '" + cod_usuario!!.trim { it <= ' ' } + "'",
                null)
        while (cursorLim.moveToNext()) {
            val objetoLimite = JSONObject()
            try {
                val kliTrack = cursorLim.getString(0)
                val kliCodven = cursorLim.getString(1)
                val kliCodcli = cursorLim.getString(2)
                val kliCodart = cursorLim.getString(3)
                val kliCant = cursorLim.getInt(4)
                val kliFechahizo = cursorLim.getString(5)
                val kliFechavence = cursorLim.getString(6)
                objetoLimite.put("kli_track", kliTrack.trim { it <= ' ' })
                objetoLimite.put("kli_codven", kliCodven.trim { it <= ' ' })
                objetoLimite.put("kli_codcli", kliCodcli.trim { it <= ' ' })
                objetoLimite.put("kli_codart", kliCodart.trim { it <= ' ' })
                objetoLimite.put("kli_cant", kliCant)
                objetoLimite.put("kli_fechahizo", kliFechahizo.trim { it <= ' ' })
                objetoLimite.put("kli_fechavence", kliFechavence.trim { it <= ' ' })
                arrayLimite!!.put(objetoLimite)
            } catch (e: JSONException) {
                e.printStackTrace()
                Toast.makeText(
                    applicationContext,
                    "Error respaldando parámetros$e",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


        //System.out.println(arrayLimite);
        val jsonObject = JSONObject()
        try {
            jsonObject.put("Limites", arrayLimite)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        val jsonStrLim = jsonObject.toString()
        try {
            insertarLimites(jsonStrLim)
        } catch (exc: Exception) {
            exc.printStackTrace()
        }
    }

    private fun insertarLimites(jsonlim: String) {
        val requestQueue = Volley.newRequestQueue(applicationContext)
        val stringRequest: StringRequest = object : StringRequest(
            Method.POST,
            "https://$enlaceEmpresa/$ambienteJob/Limites.php",
            Response.Listener { response ->
                if (response.trim { it <= ' ' } == "OK") {
                    Toast.makeText(applicationContext, "Parámetros cargados.", Toast.LENGTH_LONG)
                        .show()
                    varAux++
                    progressDialog!!.incrementProgressBy(numBarraProgreso.toInt())
                    sincronizacionVendedor()
                }
            },
            Response.ErrorListener { error: VolleyError ->
                error.printStackTrace()
                varAux++
                progressDialog!!.incrementProgressBy(numBarraProgreso.toInt())
                //varAuxError = true;
                sincronizacionVendedor()
            }) {
            override fun getParams(): Map<String, String>? {
                val params: MutableMap<String, String> = HashMap()
                params["jsonlim"] = jsonlim
                params["agencia"] = codigoSucursal
                return params
            }
        }
        requestQueue.add(stringRequest)
    }

    private fun cambiarEstadoPedido(correlativo: String) {
        //Actualizacion del status del pedido
        val keAndroid = conn!!.writableDatabase
        keAndroid.execSQL("UPDATE ke_opti SET kti_status = '1' WHERE kti_ndoc = '$correlativo'")
    }

    private fun cambiarEstadoPrecob(correlativo: String) {
        //Actualizacion del status del pedido
        val keAndroid = conn!!.writableDatabase
        val cursor = keAndroid.rawQuery(
            "SELECT edorec, tiporecibo FROM ke_precobranza WHERE cxcndoc = '$correlativo';",
            null
        )
        if (cursor.moveToNext()) {
            if (cursor.getString(0) == "0") {
                keAndroid.execSQL("UPDATE ke_precobranza SET edorec = '1' WHERE cxcndoc = '$correlativo';")
                subirImgRet(correlativo, "1")
            } else if (cursor.getString(0) == "9") {
                keAndroid.execSQL("UPDATE ke_precobranza SET edorec = '10' WHERE cxcndoc = '$correlativo';")
                subirImgRet(correlativo, "10")
            } else if (cursor.getString(0) == "3") {
                keAndroid.execSQL("UPDATE ke_precobranza SET edorec = '4' WHERE cxcndoc = '$correlativo';")
            }
        } else {
            Toast.makeText(this, "Error al actualizar estatus", Toast.LENGTH_SHORT).show()
        }
        cursor.close()
    }

    private fun subirImgRet(correlativo: String, edorec: String) {
        var i = 0
        val keAndroid = conn!!.writableDatabase
        val cursor = keAndroid.rawQuery(
            "SELECT * FROM ke_retimg WHERE ke_retimg.cxcndoc = '$correlativo';",
            null
        )
        //System.out.println("SELECT * FROM ke_imgret WHERE ke_imgret.cxcndoc = '" + correlativo + "';");
        while (cursor.moveToNext()) {
            /*ke_imgret img = new ke_imgret(
                    cursor.getString(0),
                    cursor.getString(1),
                    nombre
            );*/
            val imagen = JSONObject()
            try {
                imagen.put("ret_nomimg", cursor.getString(2))
                imagen.put("cxcndoc", cursor.getString(0))
                imagen.put("ruta", cursor.getString(1))
                println(cursor.getString(1))
                println(imagen)
                enviarImgRet(imagen, correlativo, edorec)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            //System.out.println(img);
            i++
        }
        cursor.close()
    }

    private fun enviarImgRet(imgs: JSONObject, correlativo: String, edorec: String) {
        //System.out.println(imgs);
        val keAndroid = conn!!.writableDatabase
        //System.out.println("Imagen -->" + imgs);
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST,
            "https://www.cloccidental.com/webservice/ImagenesRetenciones.php",
            imgs,
            { response: JSONObject ->
                try {
                    if (response.getString("status") == "0") {
                        keAndroid.execSQL("UPDATE ke_precobranza SET edorec = '$edorec' WHERE cxcndoc = '$correlativo';")
                        keAndroid.execSQL("DELETE FROM ke_retimg WHERE cxcndoc = '$correlativo';")
                    } else {
                        keAndroid.execSQL("UPDATE ke_precobranza SET edorec = '0' WHERE cxcndoc = '$correlativo';")
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    keAndroid.execSQL("UPDATE ke_precobranza SET edorec = '0' WHERE cxcndoc = '$correlativo';")
                }
            }) { error: VolleyError ->
            //System.out.println("Error -->" + error);
            error.printStackTrace()
            keAndroid.execSQL("UPDATE ke_precobranza SET edorec = '0' WHERE cxcndoc = '$correlativo';")
        }
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(jsonObjectRequest)

        /*RequestQueue requestQueue = Volley.newRequestQueue(this);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://www.cloccidental.com/webservice/ImagenesRetenciones.php",
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            System.out.println(response);
                            if(response.equals("Subido")) {
                                System.out.println(response);

                            } else {
                                Toast.makeText(SincronizacionActivity.this, "Teléfono sin internet adecuada", LENGTH_LONG).show();
                                ke_android.execSQL("UPDATE ke_precobranza SET edorec = '0' WHERE cxcndoc = '" + correlativo + "';");
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(SincronizacionActivity.this, "Teléfono sin internet adecuada", LENGTH_LONG).show();
                    ke_android.execSQL("UPDATE ke_precobranza SET edorec = '0' WHERE cxcndoc = '" + correlativo + "';");
                }
            }){
                @Override
                public Map<String, String> getParams() {

                    HashMap<String, String> params = new HashMap<String, String>();
                    params.put("Content-Type", "application/json; charset=utf-8");
                    params.put("Host", "https://www.cloccidental.com/webservice/ImagenesRetenciones.php");
                    params.put("nombre", imgs.getNombre());
                    params.put("imagen", imgs.getRutafoto());
                    params.put("cxcndoc", imgs.getCxcndoc());

                    return params;
                }

                @Override
                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                    String responseString = "";
                    if (response != null) {
                        responseString = String.valueOf (response.statusCode);
                        // can get more details such as response.headers
                    }
                    return Response.success (responseString, HttpHeaderParser.parseCacheHeaders (response));
                }

            };

        stringRequest.setShouldCache(false);
        stringRequest.setRetryPolicy(new DefaultRetryPolicy (30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));


            requestQueue.add(stringRequest);

            try {
                System.out.println("EL REQUEST --> "+ stringRequest.getBody());
            }catch (Exception e){
                e.printStackTrace();
            }*/
    }

    /** */
    private fun insertarCobranza(jsoncxc: JSONObject?) {
        println(jsoncxc)
        //http://cloccidental.com:5001/precobranzas2
        val requestQueue = Volley.newRequestQueue(this@SincronizacionActivity)
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST,
            "https://15cc-45-186-203-254.ngrok.io/precobranzas2",
            jsoncxc,
            { response: JSONObject? ->
                if (response != null) {
                    try {
                        //Descomposiion del objeto JSON llamado "estado"
                        val jsonArray = response.getJSONArray("estado")
                        //Analicis y descomposicion del array JSON
                        for (i in 0 until jsonArray.length()) {
                            //Obtencion del objeto JSON del array
                            val jsonObject = jsonArray.getJSONObject(i)
                            //Obtencion de las variables "correlatvo" y "status" del Objeto JSON
                            val correlativo = jsonObject.getString("correlativo")
                            val status = jsonObject.getString("status")
                            //Analicis de la respuesta con la variable status
                            if (status == "200") {
                                //Funcion que cambia el status del pedido de 0 a 1
                                cambiarEstadoPrecob(correlativo)
                                //tv_pedidossubidos.setText("Pedido " + Correlativo + " Cargado Correctamente");
                                Toast.makeText(
                                    this@SincronizacionActivity,
                                    "Cobanza $correlativo Cargado Correctamente.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            if (status == "111") {
                                //Funcion que cambia el status del pedido de 0 a 1
                                cambiarEstadoPrecob(correlativo)
                                //tv_pedidossubidos.setText("Pedido" + Correlativo + " previamente cargado");
                                Toast.makeText(
                                    this@SincronizacionActivity,
                                    "Cobranza$correlativo previamente cargado. \nRecomendaión: Sincronizar nuevamente.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            if (status == "112") {
                                //tv_pedidossubidos.setText("Linea(s) del pedido" + Correlativo + " repetida(s)");
                                Toast.makeText(
                                    this@SincronizacionActivity,
                                    "Linea(s) de la Cobranza$correlativo repetida(s) \nRecomendación: Verificar el contenido del pedido.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            if (status == "403") {
                                //tv_pedidossubidos.setText("Correlativos del pedido" + Correlativo + " no concuerdan");
                                Toast.makeText(
                                    this@SincronizacionActivity,
                                    "Correlativos de la Cobanza$correlativo no concuerdan \nRecomendaión: Rehacer el pedido.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            if (status == "404") {
                                //tv_pedidossubidos.setText("Correlativos del pedido" + Correlativo + " no concuerdan");
                                Toast.makeText(
                                    this@SincronizacionActivity,
                                    "Error súbito.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                        //Mensaje final del proceso
                        binding.tvSubidospedidos.text = "Cobranzas Procesadas"
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                }
            },
            { error: VolleyError ->
                println("--Error--")
                error.printStackTrace()
                println("--Error--")
                Toast.makeText(
                    this@SincronizacionActivity,
                    "Sin conexión a Internet estable para subir recibos.",
                    Toast.LENGTH_SHORT
                ).show()
            })
        requestQueue.add(jsonObjectRequest)
    }

    private fun insertarPedido(jsonpe: JSONObject?) {
        //Muestra pantalla del json generado
        //System.out.println("pedido llegando: " + jsonpe);

        //http://cloccidental.com:5000/pedidos
        //Envio del JSON en la direccion dada
        val requestQueue = Volley.newRequestQueue(this@SincronizacionActivity)
        //Respuesta negativa del url
        //Respuesta positiva del url
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST,
            "https://15cc-45-186-203-254.ngrok.io/pedido",
            jsonpe,
            { response: JSONObject? ->
                if (response != null) {
                    try {
                        //Descomposiion del objeto JSON llamado "estado"
                        val jsonArray = response.getJSONArray("estado")
                        //Analicis y descomposicion del array JSON
                        for (i in 0 until jsonArray.length()) {
                            //Obtencion del objeto JSON del array
                            val jsonObject = jsonArray.getJSONObject(i)
                            //Obtencion de las variables "correlatvo" y "status" del Objeto JSON
                            val correlativo = jsonObject.getString("correlativo")
                            val status = jsonObject.getString("status")
                            //Analicis de la respuesta con la variable status
                            if (status == "200") {
                                //Funcion que cambia el status del pedido de 0 a 1
                                cambiarEstadoPedido(correlativo)
                                //tv_pedidossubidos.setText("Pedido " + Correlativo + " Cargado Correctamente");
                                Toast.makeText(
                                    this@SincronizacionActivity,
                                    "Pedido $correlativo Cargado Correctamente.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            if (status == "111") {
                                //Funcion que cambia el status del pedido de 0 a 1
                                cambiarEstadoPedido(correlativo)
                                //tv_pedidossubidos.setText("Pedido" + Correlativo + " previamente cargado");
                                Toast.makeText(
                                    this@SincronizacionActivity,
                                    "Pedido$correlativo previamente cargado. \nRecomendaión: Sincronizar nuevamente.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            if (status == "112") {
                                //tv_pedidossubidos.setText("Linea(s) del pedido" + Correlativo + " repetida(s)");
                                Toast.makeText(
                                    this@SincronizacionActivity,
                                    "Línea(s) del pedido$correlativo repetida(s) \nRecomendación: Verificar el contenido del pedido.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            if (status == "403") {
                                //tv_pedidossubidos.setText("Correlativos del pedido" + Correlativo + " no concuerdan");
                                Toast.makeText(
                                    this@SincronizacionActivity,
                                    "Correlativos del pedido$correlativo no concuerdan \nRecomendaión: Rehacer el pedido.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            if (status == "404") {
                                //tv_pedidossubidos.setText("Correlativos del pedido" + Correlativo + " no concuerdan");
                                Toast.makeText(
                                    this@SincronizacionActivity,
                                    "Error súbito.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                        //Mensaje final del proceso
                        binding.tvSubidospedidos.text = "Pedidos Procesados"
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        println("Error status-correlativo")
                    }
                }
            }) { error: VolleyError ->
            error.printStackTrace()
            Toast.makeText(
                this@SincronizacionActivity,
                "Sin conexión a Internet estable para subir pedidos.",
                Toast.LENGTH_SHORT
            ).show()
            binding.btnSubir.isEnabled = true
        }

        //Envio puesto en cola
        requestQueue.add(jsonObjectRequest)
    }

    /*******************tabla kardex */
    private fun bajarKardex(url: String) {
        val jsonObjectRequest: JsonObjectRequest = object :
            JsonObjectRequest(Method.GET, url, null, Response.Listener { response: JSONObject ->
                try {
                    if (response.getString("articulo") != "null") {
                        var jsonObject: JSONObject //creamos un objeto json vacio
                        val articulo = response.getJSONArray("articulo")
                        conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null, 1)
                        val keAndroid = conn!!.writableDatabase
                        keAndroid.delete("ke_kardex", "1", null)
                        for (i in 0 until articulo.length()) {
                            try {
                                keAndroid.beginTransaction()
                                jsonObject = articulo.getJSONObject(i)
                                codigoKardex = jsonObject.getString("codigo").trim { it <= ' ' }
                                cantidadKardex = jsonObject.getDouble("cantidad")
                                fechaKardex = jsonObject.getString("fecha")
                                val insertar = ContentValues()
                                insertar.put("kde_codart", codigoKardex)
                                insertar.put("kde_cantidad", cantidadKardex)
                                insertar.put("ke_fecha", fechaKardex)
                                keAndroid.insert("ke_kardex", null, insertar)
                                keAndroid.setTransactionSuccessful()
                            } catch (e: Exception) {
                                Toast.makeText(applicationContext, "Error 33", Toast.LENGTH_LONG)
                                    .show()
                            } finally {
                                keAndroid.endTransaction()
                            }
                        }
                        Toast.makeText(
                            applicationContext,
                            "Hay artículos nuevos/actualizados",
                            Toast.LENGTH_LONG
                        ).show()
                        progressDialog!!.setMessage("Kard: Actualizando.")
                        varAux++
                        progressDialog!!.incrementProgressBy(numBarraProgreso.toInt())
                        sincronizacionVendedor()
                    } else if (response.getString("articulo") == "null") {
                        progressDialog!!.setMessage("Kard: Sin Actualización.")
                        varAux++
                        progressDialog!!.incrementProgressBy(numBarraProgreso.toInt())
                        sincronizacionVendedor()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }, Response.ErrorListener { error: VolleyError? ->
                if (error != null) {
                    error.printStackTrace()
                }
                //--Manejo visual que indica al usuario del error--
                progressDialog!!.setMessage("Kard: No ha logrado sincronizar.")
                varAux++
                progressDialog!!.incrementProgressBy(numBarraProgreso.toInt())
                //varAuxError = true;
                //AnalisisError2();
                //------
                sincronizacionVendedor()
            }) {
            override fun getParams(): Map<String, String>? {  //finalmente, estos son los parametros que le enviaremos al webservice, partiendo de las variables
                //donde estan guardados las fechas
                // parametros.put("fecha_sinc", fecha_sinc);
                return HashMap()
            }
        }
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(jsonObjectRequest) //esto es el request que se envia al url a traves de la conexion volley, (el stringrequest esta armado arriba)
    }

    /*@Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), PrincipalActivity.class);
        startActivity(intent);
    }*/
    private fun checkForAppUpdate() {
        //appUpdateManager = AppUpdateManagerFactory.create(this);
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo: AppUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(
                    AppUpdateType.IMMEDIATE
                )
            ) {

                /*try {
                    appUpdateManager.startUpdateFlowForResult(
                            // Pass the intent that is returned by 'getAppUpdateInfo()'.
                            appUpdateInfo,

                            AppUpdateType.IMMEDIATE,

                            this,

                            MY_REQUEST_CODE);
                } catch (IntentSender.SendIntentException e) {
                    throw new RuntimeException(e);
                }*/
                try {
                    println("ACTUALIZACION")
                    appUpdateManager.startUpdateFlowForResult( // Pass the intent that is returned by 'getAppUpdateInfo()'.
                        appUpdateInfo,  // an activity result launcher registered via registerForActivityResult
                        AppUpdateType.IMMEDIATE,  // Or pass 'AppUpdateType.FLEXIBLE' to newBuilder() for
                        // flexible updates.
                        this, MY_REQUEST_CODE
                    )
                } catch (e: SendIntentException) {
                    throw RuntimeException(e)
                }
            } else {
                println("NO ACTUALIZACION")
            }
        }
    }

    private fun validarUpInApp() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo: AppUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                try {
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.IMMEDIATE,
                        this,
                        MY_REQUEST_CODE
                    )
                } catch (e: SendIntentException) {
                    throw RuntimeException(e)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MY_REQUEST_CODE) {
            when (resultCode) {
                RESULT_OK -> {
                    Toast.makeText(this, "Actualización Exitosa!", Toast.LENGTH_SHORT).show()
                }

                RESULT_CANCELED -> {
                    Toast.makeText(this, "Actualización Cancelada", Toast.LENGTH_SHORT).show()
                    finish()
                    exitProcess(0)
                }

                ActivityResult.RESULT_IN_APP_UPDATE_FAILED -> {
                    Toast.makeText(this, "Algo salio mal", Toast.LENGTH_SHORT).show()
                }

                else -> {
                    Toast.makeText(this, "Algo salio mal, sin datos", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        validarUpInApp()
    }

    companion object {
        private const val MY_REQUEST_CODE = 100
        var codigo: String? = null
        var grupo: String? = null
        var subgrupo: String? = null
        var nombre: String? = null
        var referencia: String? = null
        var marca: String? = null
        var unidad: String? = null
        var fecha_sinc: String? = null
        var fechamodifi: String? = null
        var cod_usuario: String? = null
        var direccion: String? = null
        var perscont: String? = null
        var telefonos: String? = null
        var vendedor: String? = null
        var sector: String? = null
        var subcodigo: String? = null
        var telefono_movil: String? = null
        var supervpor: String? = null
        var zona: String? = null
        var subsector: String? = null
        var id_precio1: String? = null
        var id_precio2: String? = null
        var id_precio3: String? = null
        var id_precio4: String? = null
        var id_precio5: String? = null
        var id_precio6: String? = null
        var id_precio7: String? = null
        var username: String? = null
        var password: String? = null
        var almacen: String? = null
        var kti_codcli: String? = null
        var kti_codven: String? = null
        var kti_docsolicitado: String? = null
        var kti_condicion: String? = null
        var kti_tdoc: String? = null
        var kti_ndoc: String? = null
        var tmp_nombrecli: String? = null
        var kti_fchdoc: String? = null
        var kti_status: String? = null
        var kmv_codart: String? = null
        var kmv_nombre: String? = null
        var kti_fechamodifi: String? = null
        var nropedido: String? = null
        var numinterno: String? = null
        var fechamodifidoc: String? = null
        var fecha_sinc_articulo: String? = null
        var fecha_sinc_cliempre: String? = null
        var fecha_sinc_grupos: String? = null
        var fecha_sinc_listvend: String? = null
        var fecha_sinc_usuarios: String? = null
        var ke_pedstatus: String? = null
        var fecha_sinc_sectores: String? = null
        var fecha_sinc_subsectores: String? = null
        var fecha_sinc_subgrupos: String? = null
        var kne_activa: String? = null
        var kti_negesp: String? = null
        var codigoKardex: String? = null
        var fechaKardex: String? = null
        var nivelUsuario: String? = null
        var fecha_sinc_limites: String? = null
        var ltrack: String? = null
        var lvendedor: String? = null
        var lcliente: String? = null
        var larticulo: String? = null
        var lfhizo: String? = null
        var lfvence: String? = null
        var nombreEmpresa = ""
        var enlaceEmpresa = ""
        var codigoSucursal = ""
        var ambienteJob = "webservice"
        var enpreventa: String? = null
        var comprometido: String? = null
        var vta_minenx: String? = null
        var cxcndoc: String? = null
        var tiporecibo: String? = null
        var codvend: String? = null
        var nro_recibo: String? = null
        var kecxc_id: String? = null
        var fchrecibo: String? = null
        var clicontesp: String? = null
        var moneda: String? = null
        var bcoecod: String? = null
        var bcocod: String? = null
        var bconombre: String? = null
        var fchr_dep: String? = null
        var bcoref: String? = null
        var edorec: String? = null
        var fchhr: String? = null
        var fchvigen: String? = null
        var agencia: String? = null
        var tipodoc: String? = null
        var documento: String? = null
        var nroret: String? = null
        var fchemiret: String? = null
        var refret: String? = null
        var nroretfte: String? = null
        var fchemirfte: String? = null
        var refretfte: String? = null
        var retmun_cod: String? = null
        var retmun_nro: String? = null
        var retmun_fch: String? = null
        var retmun_ref: String? = null
        var reci_doc: String? = null
        var precio1: Double? = null
        var precio2: Double? = null
        var precio3: Double? = null
        var precio4: Double? = null
        var precio5: Double? = null
        var precio6: Double? = null
        var precio7: Double? = null
        var existencia: Double? = null
        var discont: Double? = null
        var status: Double? = null
        var contribespecial: Double? = null
        var superves: Double? = null
        var nivgcial: Double? = null
        var desactivo: Double? = null
        var ualterprec: Double? = null
        var kti_tipprec: Double? = null
        var kti_totneto: Double? = null
        var kmv_cant: Double? = null
        var kmv_stot: Double? = null
        var kmv_artprec: Double? = null
        var precio: Double? = null
        var kne_mtomin: Double? = null
        var cantidadKardex: Double? = null
        var vta_max: Double? = null
        var vta_min: Double? = null
        var dctotope: Double? = null
        var kmv_dctolin: Double? = null
        var tasadia: Double? = null
        var bsneto: Double? = null
        var bsiva: Double? = null
        var bsretiva: Double? = null
        var bsflete: Double? = null
        var bstotal: Double? = null
        var dolneto: Double? = null
        var doliva: Double? = null
        var dolretiva: Double? = null
        var dolflete: Double? = null
        var doltotal: Double? = null
        var dctoaplic: Double? = null
        var netocob: Double? = null
        var efectivo: Double? = null
        var bcomonto: Double? = null
        var bsretflete: Double? = null
        var diasvigen: Double? = null
        var retmun_sbi: Double? = null
        var retmun_sbs: Double? = null
        var bscobro: Double? = null
        var prcdsctopp: Double? = null
        var bsmtofte: Double? = null
        var bsretfte: Double? = null
        var bsmtoiva: Double? = null
        var retmun_bi: Double? = null
        var retmun_mto: Double? = null
        var diascalc: Double? = null
        var contadorvend = 0
        var contadorart = 0
        var contadorcli = 0
        var contadorpedidosactualizados = 0
        var varAux = 0
        var lcantidad = 0
        var numBarraProgreso = (100 / 14).toDouble()
        var varAuxError = false
        lateinit var cursorti: Cursor
        var cursormv: Cursor? = null
        lateinit var cursorLim: Cursor
        var arrayTi: JSONArray? = null
        var arrayMV: JSONArray? = null
        var arrayLimite: JSONArray? = null
        var arrayRec: JSONArray? = null
        var arrayCH: JSONArray? = null
        var arrayCL: JSONArray? = null
        fun parseFecha(fecha: String?): Date? {
            val formato = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            var fechaDate: Date? = null
            try {
                fechaDate = formato.parse(fecha)
            } catch (ex: ParseException) {
                println("--Error-")
                ex.printStackTrace()
                println("--Error-")
            }
            return fechaDate
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
            btnSubir.setDrawableAgencia(Constantes.AGENCIA)
            btnSync.setDrawableAgencia(Constantes.AGENCIA)
            btnSubirprecob.setDrawableAgencia(Constantes.AGENCIA)
        }
    }

}