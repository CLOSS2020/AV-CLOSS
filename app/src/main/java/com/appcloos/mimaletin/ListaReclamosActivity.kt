package com.appcloos.mimaletin

import android.content.ContentValues
import android.content.DialogInterface
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ListaReclamosActivity : AppCompatActivity() {
    private lateinit var listareclamo: ArrayList<Reclamo>
    private var listalineasrcl: ArrayList<Reclamo>? = null
    private lateinit var conn: AdminSQLiteOpenHelper
    var codigoCliente: String? = null
    var nombreCliente: String? = null
    var documento: String? = null
    lateinit var listaReclamos: RecyclerView
    private var reclamosAdapter: ReclamosAdapter? = null
    private lateinit var preferences: SharedPreferences
    var keAndroid1: SQLiteDatabase? = null
    private var arrayTi: JSONArray? = null
    private var arrayMV: JSONArray? = null
    private var lineasReclamosAdapter: lineasReclamosAdapter? = null
    private var llCommit = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_reclamos)
        requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED // mantener la activity en vertical

        // establecemos los detalles de la conexion a la base de datos
        conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null)

        // enlazo la parte logica a la grafica del recyclerview
        listaReclamos = findViewById(R.id.lv_reclamoslist)
        listaReclamos.layoutManager = LinearLayoutManager(this)

        // PREFERENCIAS PARA TRAER POR EJEMPLO,EL CODIGO DEL USUARIO ACTUAL
        preferences = getSharedPreferences("Preferences", MODE_PRIVATE)
        cod_usuario = preferences.getString("cod_usuario", null)
        codEmpresa = preferences.getString("codigoEmpresa", null)

        // enlaceEmpresa = conn.getCampoString("ke_enlace", "kee_url", "kee_codigo", codEmpresa!!)

        cargarEnlace()

        // llamo al metodo para consultar si hay reclamos creados (seran mostrados en recycleview)
        consultarReclamos()

        // preparamos el adapter pasandole la lista que usará luego de la consulta y el contexto
        reclamosAdapter =
            ReclamosAdapter(listareclamo) { position -> onItemClick(position) }
        listaReclamos.adapter = reclamosAdapter
        reclamosAdapter!!.notifyDataSetChanged() // para refrescar el RecyclerView
        fecha_sinc = conn.getFecha("ke_rclcti", codEmpresa!!)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.menu_listareclamos, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun cargarEnlace() {
        val keAndroid = conn.writableDatabase
        val columnas = arrayOf(
            "kee_nombre," +
                "kee_url"
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
        }
        cursor.close()
        keAndroid.close()
    }

    // metodo para consultar los reclamos creados (2021-12-10)-- revisar si es conveniente un switch
    private fun consultarReclamos() {
        val keAndroid = conn.writableDatabase
        var reclamo: Reclamo
        listareclamo = ArrayList()

        // preparamos los campos y la condicion para el query de los reclamos
        val campos = arrayOf(
            "krti_ndoc, " +
                "krti_status, " +
                "krti_codcli, " +
                "krti_docfac, " +
                "krti_nombrecli, " +
                "krti_docdev, " +
                "krti_totneto, " +
                "krti_totnetodef, " +
                "krti_docnc, " +
                "krti_fchdoc," +
                "fechamodifi"
        )
        val condicion =
            "krti_status != '9' AND krti_codvend='$cod_usuario' AND empresa = '$codEmpresa'"
        val cursor = keAndroid.query("ke_rclcti", campos, condicion, null, null, null, null)
        while (cursor.moveToNext()) {
            /*estas variables son para validar si vienen vacias
            o que se debe reflejar en el estatus...
             */
            val estatusEval = cursor.getString(1).trim { it <= ' ' }
            val nroDevEval = cursor.getString(5)
            val notaCEval = cursor.getString(8)
            var reclamoStatus = ""
            when (estatusEval) {
                "0" -> reclamoStatus = "Por Subir"
                "1" -> reclamoStatus = "Subido"
                "2" -> reclamoStatus = "En revisión"
                "3" -> reclamoStatus = "Rechazado"
                "4" -> reclamoStatus = "Esp. Mercancía"
                "5" -> reclamoStatus = "Procesado"
            }
            val reclamoDev: String = nroDevEval ?: "Pendiente"
            val reclamoNotac: String = notaCEval ?: "Pendiente"

            // creamos un objeto reclamo y agregamos valores.
            reclamo = Reclamo()
            reclamo.setNdoc(cursor.getString(0))
            reclamo.setStatus(reclamoStatus)
            reclamo.setCodcli(cursor.getString(2))
            reclamo.setDocfac(cursor.getString(3))
            reclamo.setNombrecli(cursor.getString(4))
            reclamo.setDocdev(reclamoDev)
            reclamo.setTotneto(cursor.getDouble(6))
            reclamo.setTotnetodef(cursor.getDouble(7))
            reclamo.setDocnc(reclamoNotac)
            reclamo.setFechadoc(cursor.getString(9))
            reclamo.setFechamodifi(cursor.getString(10))
            listareclamo.add(reclamo) // añado los campos a una lista
        }
        cursor.close()
        keAndroid.close()

        // Cursor cursor = ke_android.rawQuery("SELECT * FROM ke_rclcti WHERE krti_codven ='" + cod_usuario.trim() + "'", null);
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.subir_reclamos -> subirReclamos()
            R.id.actu_reclamos -> actualizarReclamos(
                "https://$enlaceEmpresa/webservice/obtenerdatosreclamos.php?cod_usuario=$cod_usuario&&fecha_sinc=$fecha_sinc"
            )
        }
        return super.onOptionsItemSelected(item)
    }

    private fun actualizarReclamos(url: String) {
        val jsonArrayRequest: JsonArrayRequest = object : JsonArrayRequest(
            url,
            { response: JSONArray? -> // a traves de un json array request, traemos la informacion que viene del webservice
                if (response != null) { // si la respuesta no viene vacia
                    conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null)
                    val keAndroid = conn!!.writableDatabase
                    var jsonObjecthead: JSONObject // creamos un objeto json vacio para la cabecera
                    var jsonObjectlineas: JSONObject // creamos un objeto json vacio para las lineas
                    llCommit = false
                    keAndroid.beginTransaction()
                    for (i in 0 until response.length()) {
                        /*pongo todo en el objeto segun lo que venga */
                        try {
                            jsonObjecthead = response.getJSONObject(i)
                            codigorcl = jsonObjecthead.getString("krti_ndoc")
                            codclasif = jsonObjecthead.getString("kdv_codclasif")
                            agenc = jsonObjecthead.getString("krti_agenc")
                            tipnc = jsonObjecthead.getString("krti_tipnc")
                            docnc = jsonObjecthead.getString("krti_docnc")
                            agedev = jsonObjecthead.getString("krti_agedev")
                            tipdev = jsonObjecthead.getString("krti_tipdev")
                            docdev = jsonObjecthead.getString("krti_docdev")
                            status = jsonObjecthead.getString("krti_status")
                            totnetodef = jsonObjecthead.getDouble("krti_totnetodef")
                            fechamodifiOP = jsonObjecthead.getString("fechacabecera")
                            for (j in 0 until response.length()) {
                                jsonObjectlineas = response.getJSONObject(j)
                                pid = jsonObjectlineas.getString("krmv_pid")
                                cantdef = jsonObjectlineas.getDouble("krmv_cantdef")
                                stotdef = jsonObjectlineas.getDouble("krmv_stotdef")
                                fechamodifilin = jsonObjectlineas.getString("fechalineas")
                                val actualizarLineas = ContentValues()
                                actualizarLineas.put("krmv_cantdef", cantdef)
                                actualizarLineas.put("krmv_stotdef", stotdef)
                                actualizarLineas.put("fechamodifi", fechamodifilin)
                                keAndroid.update(
                                    "ke_rcllmv",
                                    actualizarLineas,
                                    "krti_ndoc ='$codigorcl' AND " +
                                        "krmv_pid ='$pid' AND " +
                                        "empresa = '$codEmpresa'",
                                    null
                                )
                            }
                            val actualizarCabeceras = ContentValues()
                            actualizarCabeceras.put("krti_agenc", agenc)
                            actualizarCabeceras.put("krti_tipnc", tipnc)
                            actualizarCabeceras.put("krti_docnc", docnc)
                            actualizarCabeceras.put("krti_agedev", agedev)
                            actualizarCabeceras.put("krti_tipdev", tipdev)
                            actualizarCabeceras.put("krti_docdev", docdev)
                            actualizarCabeceras.put("krti_status", status)
                            actualizarCabeceras.put("krti_totnetodef", totnetodef)
                            actualizarCabeceras.put("kdv_codclasif", codclasif)
                            actualizarCabeceras.put("fechamodifi", fechamodifiOP)
                            keAndroid.update(
                                "ke_rclcti",
                                actualizarCabeceras,
                                "krti_ndoc ='$codigorcl' AND empresa = '$codEmpresa'",
                                null
                            )
                            llCommit = true

                            // actualizamos la fecha de la tabla de
                            val fechaReclamos = Calendar.getInstance()
                            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                            val fechareclamos = sdf.format(fechaReclamos.time)
                            val actualizarFecha = ContentValues()
                            actualizarFecha.put("fchhn_ultmod", fechareclamos)
                            keAndroid.update(
                                "tabla_aux",
                                actualizarFecha,
                                "tabla = 'ke_rclcti' AND empresa = '$codEmpresa'",
                                null
                            )
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            keAndroid.endTransaction()
                            llCommit = false
                        }
                    }
                    if (llCommit) {
                        keAndroid.setTransactionSuccessful()
                        keAndroid.endTransaction()
                        Toast.makeText(
                            this@ListaReclamosActivity,
                            "Reclamos Actualizados",
                            Toast.LENGTH_LONG
                        ).show()
                        listaReclamos.adapter = null
                        listaReclamos.adapter = reclamosAdapter
                        reclamosAdapter!!.notifyDataSetChanged()
                        finish()
                        overridePendingTransition(0, 0)
                        startActivity(intent)
                        overridePendingTransition(0, 0) // para refrescar el RecyclerView
                    } else if (!llCommit) {
                        keAndroid.endTransaction()
                        Toast.makeText(
                            this@ListaReclamosActivity,
                            "Error en la actualización",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            },
            { error: VolleyError? ->
                error!!.printStackTrace()
                Toast.makeText(
                    this@ListaReclamosActivity,
                    "Sin Actualización",
                    Toast.LENGTH_LONG
                ).show()
            }
        ) {
            override fun getParams(): Map<String, String> { // finalmente, estos son los parametros que le enviaremos al webservice, partiendo de las variables
                // donde estan guardados las fechas
                // parametros.put("fecha_sinc", fecha_sinc);
                return HashMap()
            }
        }
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(
            jsonArrayRequest
        ) // esto es el request que se envia al url a traves de la conexion volley, (el stringrequest esta armado arriba)
    }

    private fun subirReclamos() {
        val keAndroid = conn.writableDatabase
        val campos = arrayOf(
            "krti_ndoc, " +
                "krti_status, " +
                "krti_codcli, " +
                "krti_docfac, " +
                "krti_nombrecli, " +
                "krti_totneto, " +
                "krti_fchdoc," +
                "fechamodifi," +
                "krti_agefac," +
                "krti_tipfac," +
                "krti_codvend," +
                "krti_codcoor," +
                "krti_tipprec," +
                "krti_notas"
        )
        val condicion =
            "krti_status = '0' AND krti_codvend = '$cod_usuario' AND empresa = '$codEmpresa'"
        val cursor = keAndroid.query("ke_rclcti", campos, condicion, null, null, null, null)
        if (cursor.count > 0) {
            cargarReclamos()
        } else {
            Toast.makeText(
                this@ListaReclamosActivity,
                "No hay Reclamos por cargar.",
                Toast.LENGTH_SHORT
            ).show()
        }
        cursor.close()
    }

    private fun cargarReclamos() {
        var contadorReclamos = 0
        val keAndroid = conn.writableDatabase
        val campos = arrayOf(
            "krti_ndoc, " +
                "krti_status, " +
                "krti_codcli, " +
                "krti_docfac, " +
                "krti_nombrecli, " +
                "krti_totneto, " +
                "krti_fchdoc," +
                "fechamodifi," +
                "krti_agefac," +
                "krti_tipfac," +
                "krti_codvend," +
                "krti_codcoor," +
                "krti_tipprec," +
                "krti_notas"
        )
        val condicion =
            "krti_status = '0' AND krti_codvend = '$cod_usuario' AND empresa = '$codEmpresa'"
        val cursorti = keAndroid.query("ke_rclcti", campos, condicion, null, null, null, null)
        arrayTi = JSONArray()
        arrayMV = JSONArray()
        while (cursorti.moveToNext()) {
            val objetoCabecera = JSONObject()
            try {
                krti_ndoc = cursorti.getString(0)
                krti_status = cursorti.getString(1)
                krti_codcli = cursorti.getString(2)
                krti_docfac = cursorti.getString(3)
                krti_nombrecli = cursorti.getString(4)
                krti_totneto = cursorti.getDouble(5)
                krti_fchdoc = cursorti.getString(6)
                fechamodifi = cursorti.getString(7)
                krti_agefac = cursorti.getString(8)
                krti_tipfac = cursorti.getString(9)
                krti_codvend = cursorti.getString(10)
                krti_codcoor = cursorti.getString(11)
                krti_tipprec = cursorti.getDouble(12)
                krti_notas = cursorti.getString(13)
                objetoCabecera.put("krti_ndoc", krti_ndoc)
                objetoCabecera.put("krti_status", krti_status)
                objetoCabecera.put("krti_codcli", krti_codcli)
                objetoCabecera.put("krti_docfac", krti_docfac)
                objetoCabecera.put("krti_nombrecli", krti_nombrecli)
                objetoCabecera.put("krti_totneto", krti_totneto)
                objetoCabecera.put("krti_fchdoc", krti_fchdoc)
                objetoCabecera.put("fechamodifi", fechamodifi)
                objetoCabecera.put("fechamodifi", fechamodifi)
                objetoCabecera.put("krti_agefac", krti_agefac)
                objetoCabecera.put("krti_tipfac", krti_tipfac)
                objetoCabecera.put("krti_codvend", krti_codvend)
                objetoCabecera.put("krti_codcoor", krti_codcoor)
                objetoCabecera.put("krti_tipprec", krti_tipprec)
                objetoCabecera.put("krti_notas", krti_notas)
                val camposLineas = arrayOf(
                    "krti_ndoc," +
                        "krmv_tipprec," +
                        "krmv_codart," +
                        "krmv_nombre," +
                        "krmv_cant," +
                        "krmv_artprec," +
                        "krmv_stot," +
                        "krmv_pid," +
                        "fechamodifi"
                )
                val condicionLineas = "krti_ndoc = '$krti_ndoc' AND empresa = '$codEmpresa'"
                val cursormv = keAndroid.query(
                    "ke_rcllmv",
                    camposLineas,
                    condicionLineas,
                    null,
                    null,
                    null,
                    null
                )
                while (cursormv.moveToNext()) {
                    val objetoLineas = JSONObject()
                    krmv_tipprec = cursormv.getDouble(1)
                    krmv_codart = cursormv.getString(2)
                    krmv_nombre = cursormv.getString(3)
                    krmv_cant = cursormv.getDouble(4)
                    krmv_artprec = cursormv.getDouble(5)
                    krmv_stot = cursormv.getDouble(6)
                    krmv_pid = cursormv.getString(7)
                    fechamodifi = cursormv.getString(8)
                    objetoLineas.put("krti_ndoc", krti_ndoc)
                    objetoLineas.put("krmv_codart", krmv_codart)
                    objetoLineas.put("krmv_nombre", krmv_nombre)
                    objetoLineas.put("krmv_cant", krmv_cant)
                    objetoLineas.put("krmv_artprec", krmv_artprec)
                    objetoLineas.put("krmv_stot", krmv_stot)
                    objetoLineas.put("krmv_pid", krmv_pid)
                    objetoLineas.put("fechamodifi", fechamodifi)
                    objetoLineas.put("krmv_tipprec", krmv_tipprec)
                    arrayMV!!.put(objetoLineas)
                }
                cursormv.close()
            } catch (ex: Exception) {
                ex.printStackTrace()
                Toast.makeText(
                    this@ListaReclamosActivity,
                    "Error al cargar los Reclamos $ex",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
            arrayTi!!.put(objetoCabecera)
            contadorReclamos++
        }
        cursorti.close()
        val jsonRCL = JSONObject() // vamos a hacer un solo objeto de tipo json
        try {
            jsonRCL.put("Cabecera", arrayTi)
            jsonRCL.put("Lineas", arrayMV)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        val jsonStrRCL = jsonRCL.toString()
        try {
            insertarReclamo(jsonStrRCL)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun insertarReclamo(jsonStrRCL: String) {
        // genero un request queue y luego un strig request
        val requestQueue = Volley.newRequestQueue(this@ListaReclamosActivity)
        // el string request llamara al webservice
        val stringRequest: StringRequest = object : StringRequest(
            Method.POST,
            "https://$enlaceEmpresa/webservice/Reclamos.php",
            Response.Listener { response: String ->
                if (response.trim {
                        it <= ' '
                    } == "OK"
                ) { // si la respuesta obtenida es igual a ok, entonces cambio el estado del reclamo
                    cambiarEstadoReclamo()
                    Toast.makeText(
                        this@ListaReclamosActivity,
                        "Reclamo(s) Subido(s)",
                        Toast.LENGTH_SHORT
                    ).show()
                    listaReclamos.adapter = reclamosAdapter
                    reclamosAdapter!!.notifyDataSetChanged() // para refrescar el RecyclerView
                    finish()
                    overridePendingTransition(0, 0)
                    startActivity(intent)
                    overridePendingTransition(0, 0) // para refrescar el RecyclerView*/
                }
            },
            Response.ErrorListener { error: VolleyError ->
                error.printStackTrace()
                Toast.makeText(this@ListaReclamosActivity, "Error en la subida", Toast.LENGTH_SHORT)
                    .show()
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["jsonrcl"] = jsonStrRCL
                return params
            }
        }
        requestQueue.add(stringRequest) // importante añadir el string request al request queue
    }

    // si los reclamos subieron bien, hago el cambio de estado de los que subieron
    private fun cambiarEstadoReclamo() {
        val keAndroid = conn!!.writableDatabase
        println(arrayTi)
        for (i in 0 until arrayTi!!.length()) {
            try {
                val objetodeCabeza = arrayTi!!.getJSONObject(i)
                val codigoDelReclamoenArray = objetodeCabeza.getString("krti_ndoc")
                keAndroid.execSQL(
                    "UPDATE ke_rclcti SET krti_status = '1' " +
                        "WHERE krti_ndoc ='$codigoDelReclamoenArray' AND empresa = '$codEmpresa'"
                )
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    override fun onResume() {
        consultarReclamos()
        super.onResume()
    }

    private fun onItemClick(position: Int) {
        val keAndroid = conn.writableDatabase
        llCommit = false
        val docFactura = listareclamo[position].getDocfac()
        val docStatus = listareclamo[position].getStatus()
        val codReclamo = listareclamo[position].getNdoc()
        val documentoNC = listareclamo[position].getDocnc()
        val documentoDEV = listareclamo[position].getDocdev()
        val montodef = listareclamo[position].getTotnetodef()

        val ventana = AlertDialog.Builder(
            ContextThemeWrapper(
                this@ListaReclamosActivity,
                setAlertDialogTheme(Constantes.AGENCIA)
            )
        )
        ventana.setTitle("Opciones")
        ventana.setMessage("Por favor, selecciona una opción")
        ventana.setPositiveButton("Ver más información") { _: DialogInterface?, _: Int ->
            val dialogolineas = AlertDialog.Builder(
                ContextThemeWrapper(
                    this@ListaReclamosActivity,
                    setAlertDialogTheme(Constantes.AGENCIA)
                )
            )
            dialogolineas.setTitle("Detalles del Reclamo")
            /*dialogolineas.setMessage("Nº NC: " + documentoNC +"\n"+
                                     "Nº DEV: " + documentoDEV + "\n" +
                                     "Monto def: " + montodef + "\n");*/
            val lineasrcl = ListView(this@ListaReclamosActivity)
            cargarLineasRCL(codReclamo)
            lineasReclamosAdapter =
                lineasReclamosAdapter(this@ListaReclamosActivity, listalineasrcl)
            lineasrcl.adapter = lineasReclamosAdapter
            lineasReclamosAdapter!!.notifyDataSetChanged()
            dialogolineas.setView(lineasrcl)
            val dialogodoc = dialogolineas.create()
            dialogodoc.show()
        }
        ventana.setNeutralButton("Borrar Reclamo") { _: DialogInterface?, _: Int ->
            if ((docStatus == "Por Subir")) {
                // SI EL RECLAMO NO HA SIDO SUBIDO, LO BORRO Y DESBLOQUEO EL DOCUMENTO
                val subventana = AlertDialog.Builder(
                    ContextThemeWrapper(
                        this@ListaReclamosActivity,
                        setAlertDialogTheme(Constantes.AGENCIA)
                    )
                )
                subventana.setTitle("Mensaje de confirmación")
                subventana.setMessage("¿Estás seguro de borrar el reclamo?")
                subventana.setPositiveButton(
                    "Si"
                ) { _: DialogInterface?, _: Int ->
                    keAndroid.beginTransaction()
                    try {
                        // ACA EL METODO DE BORRADO
                        borrarReclamo(codReclamo)
                        // actualizamos de nuevo el registro del documento donde acepta o no devoluciones (solo si no se han subido reclamos pertenecientes a ese doc).
                        val actualizarAceptarDev = ContentValues()
                        actualizarAceptarDev.put("aceptadev", "0")
                        keAndroid.update(
                            "ke_doccti",
                            actualizarAceptarDev,
                            "documento = '$docFactura' AND empresa = '$codEmpresa'",
                            null
                        )
                        llCommit = true
                        Toast.makeText(
                            this@ListaReclamosActivity,
                            "Reclamo borrado",
                            Toast.LENGTH_LONG
                        ).show()
                        reclamosAdapter!!.notifyDataSetChanged() // para refrescar el RecyclerView
                        finish()
                        overridePendingTransition(0, 0)
                        startActivity(intent)
                        overridePendingTransition(0, 0) // para refrescar el RecyclerView
                    } catch (e: Exception) {
                        keAndroid.endTransaction()
                        e.printStackTrace()
                        llCommit = false
                        if (!llCommit) {
                            return@setPositiveButton
                        }
                    }
                    if (true.also { llCommit = it }) {
                        keAndroid.setTransactionSuccessful()
                        keAndroid.endTransaction()
                    }
                }
                subventana.setNegativeButton(
                    "No"
                ) { _: DialogInterface?, _: Int ->
                    Toast.makeText(
                        this@ListaReclamosActivity,
                        "Eliminación cancelada",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                val dialogo2 = subventana.create()
                dialogo2.show()

                val pbutton: Button = dialogo2.getButton(DialogInterface.BUTTON_POSITIVE)
                pbutton.apply {
                    setTextColor(colorTextAgencia(Constantes.AGENCIA))
                }

                val nbutton: Button = dialogo2.getButton(DialogInterface.BUTTON_NEGATIVE)
                nbutton.apply {
                    setTextColor(colorTextAgencia(Constantes.AGENCIA))
                }
            } else {
                Toast.makeText(
                    this@ListaReclamosActivity,
                    "El reclamo ya no puede ser borrado",
                    Toast.LENGTH_LONG
                ).show()
                /*AlertDialog.Builder subventana = new AlertDialog.Builder(ListaReclamosActivity.this);
                subventana.setTitle("Mensaje de confirmación");
                subventana.setMessage("¿Estás seguro de borrar el reclamo?, solo se borrara del dispositivo");

                subventana.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ke_android.beginTransaction();
                        try{
                            //ACA EL METODO DE BORRADO
                            BorrarReclamo(codReclamo);
                            ll_commit = true;
                            Toast.makeText(ListaReclamosActivity.this, "Reclamo borrado", LENGTH_LONG).show();
                            reclamosAdapter.notifyDataSetChanged(); //para refrescar el RecyclerView
                            finish();
                            overridePendingTransition(0, 0);
                            startActivity(getIntent());
                            overridePendingTransition(0, 0);//para refrescar el RecyclerView



                        }catch (Exception e){
                            ke_android.endTransaction();
                            e.printStackTrace();
                            ll_commit = false;

                            if(!ll_commit){
                                return;
                            }

                        }
                        if(ll_commit = true){
                            ke_android.setTransactionSuccessful();
                            ke_android.endTransaction();
                        }

                    }
                });

                subventana.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(ListaReclamosActivity.this, "Eliminación cancelada", Toast.LENGTH_SHORT).show();
                    }
                });

                AlertDialog dialogo2 = subventana.create();
                dialogo2.show();*/
            }
        }
        val dialogo = ventana.create()
        dialogo.show()

        val pbutton: Button = dialogo.getButton(DialogInterface.BUTTON_POSITIVE)
        pbutton.apply {
            setTextColor(colorTextAgencia(Constantes.AGENCIA))
        }

        val nbutton: Button = dialogo.getButton(DialogInterface.BUTTON_NEUTRAL)
        nbutton.apply {
            setTextColor(colorTextAgencia(Constantes.AGENCIA))
        }
    }

    private fun cargarLineasRCL(codReclamo: String) {
        val keAndroid = conn.writableDatabase
        var reclamo: Reclamo
        listalineasrcl = ArrayList()
        val tabla = "ke_rcllmv"
        val columnas = arrayOf(
            "krmv_codart, krmv_nombre, krmv_cant, krmv_stot, krmv_stotdef, krmv_cantdef"
        )
        val condicion = "krti_ndoc ='$codReclamo' AND empresa = '$codEmpresa'"
        val cursor = keAndroid.query(tabla, columnas, condicion, null, null, null, null)
        while (cursor.moveToNext()) {
            reclamo = Reclamo()
            reclamo.setCodart(cursor.getString(0))
            reclamo.setNombre(cursor.getString(1))
            reclamo.setCant(cursor.getDouble(2))
            reclamo.setStot(cursor.getDouble(3))
            reclamo.setStotdef(cursor.getDouble(4))
            reclamo.setCantdef(cursor.getDouble(5))
            listalineasrcl!!.add(reclamo)
        }
        cursor.close()
    }

    private fun borrarReclamo(codReclamo: String) {
        val keAndroid = conn.writableDatabase
        llCommit = false
        keAndroid.beginTransaction()
        try {
            // borro las lineas pero cambio la cabecera a 9 (anulado/borrado)
            keAndroid.execSQL(
                "UPDATE ke_rclcti SET krti_status = '9' " +
                    "WHERE krti_ndoc = '$codReclamo' AND empresa = '$codEmpresa'"
            )
            keAndroid.execSQL("DELETE FROM ke_rcllmv WHERE krti_ndoc = '$codReclamo' AND empresa = '$codEmpresa'")
            // si se efectuo correctamente, digo que el commit fue verdadero
            llCommit = true
        } catch (e: Exception) {
            keAndroid.endTransaction()
            e.printStackTrace()
            llCommit = false // de producirse un error, digo que el commit es falso
            if (!llCommit) {
                keAndroid.endTransaction()
                return // si el commit es falso, cancelo la transacción y regreso (me salgo del proceso)
            }
        }
        if (true.also {
                llCommit = it
            }
        ) { // si es verdadero, digo que la transaccion fue satisfactoria.
            keAndroid.setTransactionSuccessful()
            keAndroid.endTransaction()
            println("Reclamo borrado")
        }
    }

    companion object {
        var cod_usuario: String? = null
        var codEmpresa: String? = null
        var krti_ndoc: String? = null
        var krti_status: String? = null
        var krti_codcli: String? = null
        var krti_docfac: String? = null
        var krti_nombrecli: String? = null
        var krti_docdev: String? = null
        var krti_docnc: String? = null
        var krti_fchdoc: String? = null
        var fechamodifi: String? = null
        var krmv_codart: String? = null
        var krmv_nombre: String? = null
        var krmv_pid: String? = null
        var krti_agefac: String? = null
        var krti_tipfac: String? = null
        var krti_codvend: String? = null
        var krti_codcoor: String? = null
        var krti_notas: String? = null
        var fecha_sinc: String? = null
        var codigorcl: String? = null
        var status: String? = null
        var tipnc: String? = null
        var docnc: String? = null
        var docdev: String? = null
        var tipdev: String? = null
        var agenc: String? = null
        var agedev: String? = null
        var codclasif: String? = null
        var pid: String? = null
        var fechamodifiOP: String? = null
        var fechamodifilin: String? = null
        var nombreEmpresa = ""
        var enlaceEmpresa = ""
        var krti_totneto: Double? = null
        var krmv_tipprec: Double? = null
        var krmv_cant: Double? = null
        var krmv_stot: Double? = null
        var krmv_artprec: Double? = null
        var krti_tipprec: Double? = null
        var totnetodef: Double? = null
        var cantdef: Double? = null
        var stotdef: Double? = null
    }

    override fun getTheme(): Resources.Theme {
        val theme = super.getTheme()
        theme.applyStyle(setThemeAgencia(Constantes.AGENCIA), true)
        // you could also use a switch if you have many themes that could apply
        return theme
    }
}
