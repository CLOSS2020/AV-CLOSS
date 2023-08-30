package com.appcloos.mimaletin

/*import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.widget.Toast
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale*/

/*
class CxcUpWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {
    val conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null, 12)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val date: String = dateFormat.format(Date())
    override fun doWork(): Result {
        try {
            println("--Inicio del proceso--")
            val codUsuario = inputData.getString("codUsuario") ?: return Result.failure()

            //val JSON = cargarPrecob(codUsuario)

            //val aux = insertarCobranza(JSON)


            if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) == 0 || Calendar.getInstance().get(Calendar.HOUR_OF_DAY) == 2 || Calendar.getInstance().get(Calendar.HOUR_OF_DAY) == 4 ||Calendar.getInstance().get(Calendar.HOUR_OF_DAY) == 5) {
                var aux = probando()

                while (!aux){
                    aux = probando()
                }

                */
/*if (aux) {
                    return Result.success()
                } else {
                    return Result.retry()
                }*//*

                return Result.success()
            } else {
                return Result.success()
            }


        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure()
        }
    }

    private fun probando(): Boolean {
        val objetoCabecera = JSONObject()

        objetoCabecera.put("cxcndoc", 1)
        objetoCabecera.put("fecha", date)

        println("Fehca -> $date")

        var retorno = true
        val requestQueue = Volley.newRequestQueue(applicationContext)
        val jsonObjectRequest = JsonObjectRequest(Request.Method.POST,
            "http://cloccidental.com:5001/probando",
            objetoCabecera,
            { response ->
                if (response != null) {
                    try {
                        //Descomposiion del objeto JSON llamado "estado"
                        val jsonArray = response.getJSONObject("estado")
                        //Analicis y descomposicion del array JSON
                        for (i in 0 until jsonArray.length()) {
                            //Obtencion del objeto JSON del array
                            //val jsonObject = jsonArray.getJSONObject(i)
                            //Obtencion de las variables "correlatvo" y "status" del Objeto JSON
                            //val Correlativo = jsonObject.getString("correlativo")
                            val status = jsonArray.getString("status")
                            //Analicis de la respuesta con la variable status
                            if (status == "200") {
                                retorno = true
                            }
                            if (status == "111") {
                                retorno = true
                            }
                            if (status == "112") {
                                retorno = false
                            }
                            if (status == "403") {
                                retorno = false
                            }
                            if (status == "404") {
                                retorno = false
                            }
                        }
                        //Mensaje final del proceso
                    } catch (ex: java.lang.Exception) {
                        ex.printStackTrace()
                    }
                }
            },
            Response.ErrorListener { error ->
                println("--Error--")
                retorno = false
                error.printStackTrace()
                if (error?.networkResponse == null) {
                    return@ErrorListener
                }
                val body: String
                //get status code here
                val statusCode = error.networkResponse.statusCode.toString()
                //get response body and parse with appropriate encoding
                val UTF_8 = StandardCharsets.UTF_8
                body = String(error.networkResponse.data, UTF_8)
                println(body)
                println("--Error--")
                Toast.makeText(
                    applicationContext, "Error de red en carga de recibos", Toast.LENGTH_SHORT
                ).show()
            })

        jsonObjectRequest.retryPolicy = DefaultRetryPolicy(
            10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        requestQueue.add(jsonObjectRequest)
        return retorno
    }

    private fun cargarPrecob(codUsuario: String): JSONObject {
        var contadorPrecob = 0
        //Boton para subir la precobranza
        val ke_android: SQLiteDatabase = conn.writableDatabase
        //Creacion del ursor y la sentencia SQL que ejecutara la busqueda de cada una de las abeeras de los documentos por cobrar
        var cursorRc: Cursor? = null
        cursorRc = ke_android.rawQuery(
            "SELECT * FROM ke_precobranza WHERE (edorec = '0' OR edorec = '9' OR edorec = '3') AND codvend ='$codUsuario';",
            null
        )
        //System.out.println("SELECT * FROM ke_precobranza WHERE (edorec = '0' OR edorec = '9') AND codvend ='" + cod_usuario.trim() + "'");
        //Creacion del JSON Array que contendra las precobranzas
        val arrayCH = JSONArray()
        //while que reorera todos los casos positivos de la sentencia SQL ejeutada
        while (cursorRc.moveToNext()) {
            //Creacion de los Objetos JSON Cabecera y SuperCabecera
            val objetoCabecera = JSONObject()
            val objetoSCabecera = JSONObject()
            //TRY para la creacion del JSON completo
            try {
                //Guardado en variables de los valores enontrados en la base de datos

                val cxcndoc = cursorRc.getString(0)
                //Guardado de los valores en el objeto JSON de la Cabecera
                objetoCabecera.put("cxcndoc", cursorRc.getString(0))
                objetoCabecera.put("tiporecibo", cursorRc.getString(1))
                objetoCabecera.put("codvend", cursorRc.getString(2))
                objetoCabecera.put("nro_recibo", cursorRc.getString(3))
                objetoCabecera.put("kecxc_id", cursorRc.getString(4))
                objetoCabecera.put("tasadia", cursorRc.getString(5))
                objetoCabecera.put("fchrecibo", cursorRc.getString(6))
                objetoCabecera.put("clicontesp", cursorRc.getString(7))
                objetoCabecera.put("bsneto", cursorRc.getString(8))
                objetoCabecera.put("bsiva", cursorRc.getString(9))
                objetoCabecera.put("bsretiva", cursorRc.getString(10))
                objetoCabecera.put("bsflete", cursorRc.getString(11))
                objetoCabecera.put("bstotal", cursorRc.getString(12))
                objetoCabecera.put("dolneto", cursorRc.getString(13))
                objetoCabecera.put("doliva", cursorRc.getString(14))
                objetoCabecera.put("dolretiva", cursorRc.getString(15))
                objetoCabecera.put("dolflete", cursorRc.getString(16))
                objetoCabecera.put("doltotal", cursorRc.getString(17))
                objetoCabecera.put("moneda", cursorRc.getString(18))
                objetoCabecera.put("docdifcamb", cursorRc.getString(19))
                objetoCabecera.put("ddc_age", cursorRc.getString(20))
                objetoCabecera.put("ddc_tipo", cursorRc.getString(21))
                objetoCabecera.put("ddc_montobs", cursorRc.getString(22))
                objetoCabecera.put("ddc_doc", cursorRc.getString(23))
                objetoCabecera.put("dctoaplic", cursorRc.getString(24))
                objetoCabecera.put("netocob", cursorRc.getString(25))
                objetoCabecera.put("concepto", cursorRc.getString(26))
                objetoCabecera.put("efectivo", cursorRc.getString(27))
                objetoCabecera.put("bcoecod", cursorRc.getString(28))
                objetoCabecera.put("bcocod", cursorRc.getString(29))
                objetoCabecera.put("bconombre", cursorRc.getString(30))
                objetoCabecera.put("fchr_dep", cursorRc.getString(31))
                objetoCabecera.put("bcomonto", cursorRc.getString(32))
                objetoCabecera.put("bcoref", cursorRc.getString(33))
                objetoCabecera.put("pidvalid", cursorRc.getString(34))
                objetoCabecera.put("edorec", cursorRc.getString(35))
                objetoCabecera.put("edocomiv", cursorRc.getString(36))
                objetoCabecera.put("prccomiv", cursorRc.getString(37))
                objetoCabecera.put("mtocomiv", cursorRc.getString(38))
                objetoCabecera.put("fchr_pcomv", cursorRc.getString(39))
                objetoCabecera.put("codcoord", cursorRc.getString(40))
                objetoCabecera.put("edocomic", cursorRc.getString(41))
                objetoCabecera.put("prccomic", cursorRc.getString(42))
                objetoCabecera.put("mtocomic", cursorRc.getString(43))
                objetoCabecera.put("fchr_pcomc", cursorRc.getString(44))
                objetoCabecera.put("fchhr", cursorRc.getString(45))
                objetoCabecera.put("fchvigen", cursorRc.getString(46))
                objetoCabecera.put("bsretflete", cursorRc.getString(47))
                objetoCabecera.put("diasvigen", cursorRc.getString(48))
                objetoCabecera.put("retmun_sbi", cursorRc.getString(49))
                objetoCabecera.put("retmun_sbs", cursorRc.getString(50))
                objetoCabecera.put("comiaut", cursorRc.getString(51))
                objetoCabecera.put("comiautpor", cursorRc.getString(52))
                objetoCabecera.put("comiautfch", cursorRc.getString(53))
                objetoCabecera.put("reci_age", cursorRc.getString(54))
                objetoCabecera.put("reci_doc", cursorRc.getString(55))
                objetoCabecera.put("status", cursorRc.getString(56))
                objetoCabecera.put("fechamodifi", cursorRc.getString(57))
                objetoCabecera.put("cxcndoc_aux", cursorRc.getString(58))

                //Cursor que ejecuta un SQL para ver las lineas de una cobranza en speifico para armar las lineas del documento
                val cursorRl = ke_android.rawQuery(
                    "SELECT * FROM ke_precobradocs WHERE cxcndoc ='$cxcndoc'", null
                )

                //Creacion del JSON Array que contendra todas las lineas de un documento
                val arrayCL = JSONArray()
                //WHILE que reccorera todos los casos positivos de la sentencia anterior
                while (cursorRl.moveToNext()) {
                    //Creacion del Objeto JSON para las lineas del documento
                    val objetoLineas = JSONObject()
                    //Guardado de los valores de la base de datos en las variables

                    //Guardado de los valoes en la estructura del JSON de las lineas del documento
                    objetoLineas.put("cxcndoc", cursorRl.getString(0))
                    objetoLineas.put("agencia", cursorRl.getString(1))
                    objetoLineas.put("tipodoc", cursorRl.getString(2))
                    objetoLineas.put("documento", cursorRl.getString(3))
                    objetoLineas.put("bscobro", cursorRl.getString(4))
                    objetoLineas.put("prccobro", cursorRl.getString(5))
                    objetoLineas.put("prcdsctopp", cursorRl.getString(6))
                    objetoLineas.put("nroret", cursorRl.getString(7))
                    objetoLineas.put("fchemiret", cursorRl.getString(8))
                    objetoLineas.put("bsretiva", cursorRl.getString(9))
                    objetoLineas.put("refret", cursorRl.getString(10))
                    objetoLineas.put("nroretfte", cursorRl.getString(11))
                    objetoLineas.put("fchemirfte", cursorRl.getString(12))
                    objetoLineas.put("bsmtofte", cursorRl.getString(13))
                    objetoLineas.put("bsretfte", cursorRl.getString(14))
                    objetoLineas.put("refretfte", cursorRl.getString(15))
                    objetoLineas.put("pidvalid", cursorRl.getString(16))
                    objetoLineas.put("bsmtoiva", cursorRl.getString(17))
                    objetoLineas.put("retmun_bi", cursorRl.getString(18))
                    objetoLineas.put("retmun_cod", cursorRl.getString(19))
                    objetoLineas.put("retmun_nro", cursorRl.getString(20))
                    objetoLineas.put("retmun_mto", cursorRl.getString(21))
                    objetoLineas.put("retmun_fch", cursorRl.getString(22))
                    objetoLineas.put("retmun_ref", cursorRl.getString(23))
                    objetoLineas.put("diascalc", cursorRl.getString(24))
                    objetoLineas.put("prccomiv", cursorRl.getString(25))
                    objetoLineas.put("prccomic", cursorRl.getString(26))
                    objetoLineas.put("cxcndoc_aux", cursorRl.getString(27))
                    objetoLineas.put("tnetodbs", cursorRl.getString(28))
                    objetoLineas.put("tnetoddol", cursorRl.getString(29))
                    objetoLineas.put("fchrecibod", cursorRl.getString(30))
                    objetoLineas.put("kecxc_idd", cursorRl.getString(31))
                    objetoLineas.put("tasadiad", cursorRl.getString(32))
                    objetoLineas.put("afavor", cursorRl.getString(33))
                    //Guardado del objeto JSON Lineas en el JSON Array
                    arrayCL.put(objetoLineas)
                }
                cursorRl.close()

                //Insercion del array de las Lineas en el Objeto JSON de la cabecera
                objetoCabecera.put("Lineas", arrayCL)
                //Insercion del objeto cabecera en el Objeto JSON super cabecera
                objetoSCabecera.put("Cabecera", objetoCabecera)
                //Capturador de errores
            } catch (ex: java.lang.Exception) {
                ex.printStackTrace()
                println("Error SILENCIOSO al cargar datos del recibo$ex")
            }
            //Insercion de la super cabeera en el array de la precobranza
            arrayCH.put(objetoSCabecera)
            contadorPrecob++
        }
        cursorRc.close()
        //Creacion de Objeto JSON que contendra todo
        val jsonREC = JSONObject() //vamos a hacer un solo objeto de tipo json
        try {
            //Guardado del array Cobranza con su asignacion de nombre dentro del JSON
            jsonREC.put("Cobranza", arrayCH)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return jsonREC
    }


    fun insertarCobranza(jsoncxc: JSONObject): Boolean {
        println(jsoncxc)
        var retorno = true

        val requestQueue = Volley.newRequestQueue(applicationContext)
        val jsonObjectRequest = JsonObjectRequest(Request.Method.POST,
            "https://f253-45-186-201-166.ngrok.io/precobranzas2",
            jsoncxc,
            { response ->
                if (response != null) {
                    try {
                        //Descomposiion del objeto JSON llamado "estado"
                        val jsonArray = response.getJSONArray("estado")
                        //Analicis y descomposicion del array JSON
                        for (i in 0 until jsonArray.length()) {
                            //Obtencion del objeto JSON del array
                            val jsonObject = jsonArray.getJSONObject(i)
                            //Obtencion de las variables "correlatvo" y "status" del Objeto JSON
                            val Correlativo = jsonObject.getString("correlativo")
                            val status = jsonObject.getString("status")
                            //Analicis de la respuesta con la variable status
                            if (status == "200") {
                                retorno = true
                            }
                            if (status == "111") {
                                retorno = true
                            }
                            if (status == "112") {
                                retorno = false
                            }
                            if (status == "403") {
                                retorno = false
                            }
                            if (status == "404") {
                                retorno = false
                            }
                        }
                        //Mensaje final del proceso
                    } catch (ex: java.lang.Exception) {
                        ex.printStackTrace()
                    }
                }
            },
            Response.ErrorListener { error ->
                println("--Error--")
                retorno = false
                error.printStackTrace()
                if (error?.networkResponse == null) {
                    return@ErrorListener
                }
                val body: String
                //get status code here
                val statusCode = error.networkResponse.statusCode.toString()
                //get response body and parse with appropriate encoding
                val UTF_8 = StandardCharsets.UTF_8
                body = String(error.networkResponse.data, UTF_8)
                println(body)
                println("--Error--")
                Toast.makeText(
                    applicationContext, "Error de red en carga de recibos", Toast.LENGTH_SHORT
                ).show()
            })
        requestQueue.add(jsonObjectRequest)
        return retorno
    }
}*/
