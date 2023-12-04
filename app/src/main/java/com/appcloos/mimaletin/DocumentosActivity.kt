/* Activity: DocumentosActivity
 *  Objetivo: visualizar los documentos por cliente
 *  Autor   : PCV SEP 2021*/
package com.appcloos.mimaletin

import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.appcloos.mimaletin.databinding.ActivityDocumentosBinding
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class DocumentosActivity : AppCompatActivity() {
    var conn: AdminSQLiteOpenHelper? = null
    private var llCommit: Boolean? = null
    private lateinit var documentosAdapter: DocumentosAdapter
    var listainfo: ArrayList<String>? = null
    private var permisos: ArrayList<String>? = null
    lateinit var listadocs: ArrayList<Documentos>
    private var lineasAdapter: LineasAdapter? = null
    private var listalineasdoc: ArrayList<Lineas>? = null
    var keAndroid1: SQLiteDatabase? = null

    private lateinit var binding: ActivityDocumentosBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDocumentosBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED //mantener la activity en vertical
        conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null)
        conn!!.writableDatabase
        cargarEnlace()
        val intent = intent
        codigoCliente = intent.getStringExtra("codigoCliente")
        nombreCliente = intent.getStringExtra("nombreCliente")
        cod_usuario = intent.getStringExtra("cod_usuario")
        codigoEmpresa = intent.getStringExtra("codigoEmpresa")
        permisos = ArrayList()
        supportActionBar!!.title = nombreCliente
        getfechaDocs()
        evaluacionDeCargas()
        cargarModulos()
        println("codigo de la empresa: $codigoEmpresa")
        println("Permisos que estan llegando en la ventana de documentos: $permisos")
        documentosAdapter =
            DocumentosAdapter(this@DocumentosActivity, listadocs) { position -> verDoc(position) }
        binding.lvDocumentos.layoutManager = LinearLayoutManager(this)
        binding.lvDocumentos.adapter = documentosAdapter
        documentosAdapter.notifyDataSetChanged()


        //al presionar en un documento, abro un alertdialog
        /* binding.lvDocumentos.setOnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
             println(permisos)
             val documentoP = listadocs[position].getDocumento()
             val totnetoP = (listadocs[position].getDtotneto()).valorReal()
             val totimpuestP = (listadocs[position].getDtotimpuest()).valorReal()
             val totalfinalP = (listadocs[position].getDtotalfinal()).valorReal()
             val totdescup = (listadocs[position].getDtotdescuen()).valorReal()
             val aceptaDevoluciones = listadocs[position].getAceptadev()
             val estadoDoc = listadocs[position].getEstatusdoc()
             println(estadoDoc)
             if (!permisos!!.contains("REC001")) {
                 val ventana =
                     AlertDialog.Builder(ContextThemeWrapper(this, setAlertDialogTheme(Constantes.AGENCIA)))
                 ventana.setTitle("Doc Nº: $documentoP")
                 ventana.setMessage(
                     """
     Monto Neto  :       $totnetoP$
     Monto IVA   :       $totimpuestP$
     Descuentos  :       $totdescup$
     Monto Total :       $totalfinalP$

     """.trimIndent()
                 )
                 val dialogo = ventana.create() //creamos el dialogo en base a la ventana diseñada
                 dialogo.show() //mostrar el dialogo
                 val messageText = dialogo.findViewById<TextView>(android.R.id.message)
                 messageText!!.gravity = Gravity.END
             } else {
                 val ventana =
                     AlertDialog.Builder(ContextThemeWrapper(this, setAlertDialogTheme(Constantes.AGENCIA)))
                 ventana.setTitle("Doc Nº: $documentoP")
                 ventana.setMessage(
                     """
     Monto Neto  :       $totnetoP$
     Monto IVA   :       $totimpuestP$
     Descuentos  :       $totdescup$
     Monto Total :       $totalfinalP$

     """.trimIndent()
                 )
                 ventana.setNeutralButton("Generar Reclamo") { _: DialogInterface?, _: Int ->
                     if (estadoDoc == "2") {
                         Toast.makeText(
                             this@DocumentosActivity,
                             "Este documento ya no acepta devoluciones",
                             Toast.LENGTH_LONG
                         ).show()
                     } else {
                         if (aceptaDevoluciones == "0") {
                             Toast.makeText(
                                 this@DocumentosActivity,
                                 "Este documento ya no acepta devoluciones",
                                 Toast.LENGTH_LONG
                             ).show()
                         } else if (aceptaDevoluciones == "1") {
                             iraReclamos(documentoP, codigoCliente, nombreCliente)
                         }
                     }
                 }
                 val dialogo = ventana.create() //creamos el dialogo en base a la ventana diseñada
                 dialogo.show() //mostrar el dialogo
                 val nbutton: Button = dialogo.getButton(DialogInterface.BUTTON_NEUTRAL)
                 nbutton.apply {
                     setTextColor(colorTextAgencia(Constantes.AGENCIA))
                 }

                 val messageText = dialogo.findViewById<TextView>(android.R.id.message)
                 messageText!!.gravity = Gravity.END
             }
         }*/
        vaciarTmp()
    }

    private fun verDoc(position: Int) {
        println(permisos)
        val documentoP = listadocs[position].getDocumento()
        val totnetoP = (listadocs[position].getDtotneto()).valorReal()
        val totimpuestP = (listadocs[position].getDtotimpuest()).valorReal()
        val totalfinalP = (listadocs[position].getDtotalfinal()).valorReal()
        val totdescup = (listadocs[position].getDtotdescuen()).valorReal()
        val aceptaDevoluciones = listadocs[position].getAceptadev()
        val estadoDoc = listadocs[position].getEstatusdoc()
        println(estadoDoc)
        if (!permisos!!.contains("REC001")) {
            val ventana =
                AlertDialog.Builder(
                    ContextThemeWrapper(
                        this,
                        setAlertDialogTheme(Constantes.AGENCIA)
                    )
                )
            ventana.setTitle("Doc Nº: $documentoP")
            ventana.setMessage(
                """
    Monto Neto  :       $totnetoP$
    Monto IVA   :       $totimpuestP$
    Descuentos  :       $totdescup$
    Monto Total :       $totalfinalP$
    
    """.trimIndent()
            )
            val dialogo = ventana.create() //creamos el dialogo en base a la ventana diseñada
            dialogo.show() //mostrar el dialogo
            val messageText = dialogo.findViewById<TextView>(android.R.id.message)
            messageText!!.gravity = Gravity.END
        } else {
            val ventana =
                AlertDialog.Builder(
                    ContextThemeWrapper(
                        this,
                        setAlertDialogTheme(Constantes.AGENCIA)
                    )
                )
            ventana.setTitle("Doc Nº: $documentoP")
            ventana.setMessage(
                """
    Monto Neto  :       $totnetoP$
    Monto IVA   :       $totimpuestP$
    Descuentos  :       $totdescup$
    Monto Total :       $totalfinalP$
    
    """.trimIndent()
            )
            ventana.setNeutralButton("Generar Reclamo") { _: DialogInterface?, _: Int ->
                if (estadoDoc == "2") {
                    Toast.makeText(
                        this@DocumentosActivity,
                        "Este documento ya no acepta devoluciones",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    if (aceptaDevoluciones == "0") {
                        Toast.makeText(
                            this@DocumentosActivity,
                            "Este documento ya no acepta devoluciones",
                            Toast.LENGTH_LONG
                        ).show()
                    } else if (aceptaDevoluciones == "1") {
                        iraReclamos(documentoP, codigoCliente, nombreCliente)
                    }
                }
            }
            val dialogo = ventana.create() //creamos el dialogo en base a la ventana diseñada
            dialogo.show() //mostrar el dialogo
            val nbutton: Button = dialogo.getButton(DialogInterface.BUTTON_NEUTRAL)
            nbutton.apply {
                setTextColor(colorTextAgencia(Constantes.AGENCIA))
            }

            val messageText = dialogo.findViewById<TextView>(android.R.id.message)
            messageText!!.gravity = Gravity.END
        }
    }

    private fun cargarModulos() {
        val keAndroid = conn!!.writableDatabase
        val cursor = keAndroid.rawQuery(
            "SELECT kmo_codigo FROM ke_modulos WHERE kmo_status = '1' AND ked_codigo='$codigoEmpresa'",
            null
        )
        while (cursor.moveToNext()) {
            permisos!!.add(cursor.getString(0))
            println("PERMISOS$permisos")
        }
        cursor.close()
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

    // metodo para determinar que debo traerme de la nube
    private fun evaluacionDeCargas() {
        val keAndroid = conn!!.writableDatabase
        //si el cliente no tiene documentos, por primera vez debo simplemente traerme todo, de lo contrario, debere validad por fecha
        val fechaAuxiliar = "0001-01-01 00:00:00"
        val columnacli = arrayOf("count(documento)")
        val condicioncli = "codcliente = '$codigoCliente'"
        val cursorcli =
            keAndroid.query("ke_doccti", columnacli, condicioncli, null, null, null, null)
        if (cursorcli.moveToFirst()) {
            if (cursorcli.getInt(0) > 0) {
                println("llego al if")
                println(cursorcli.getString(0))
                cargarCabeceraDocuemntosCliente("https://" + enlaceEmpresa + "/webservice/documentos.php?fecha_sinc=" + fechaDocs!!.trim { it <= ' ' } + "&&codigo_cli=" + codigoCliente!!.trim { it <= ' ' } + "&&agencia=" + codigoSucursal.trim { it <= ' ' })
                consultarDocs()
            } else if (cursorcli.getInt(0) == 0) {
                println("llego al else del principio")
                cargarCabeceraDocuemntosCliente("https://" + enlaceEmpresa + "/webservice/documentos.php?fecha_sinc=" + fechaAuxiliar.trim { it <= ' ' } + "&&codigo_cli=" + codigoCliente!!.trim { it <= ' ' } + "&&agencia=" + codigoSucursal.trim { it <= ' ' })
                consultarDocs()
            }
        }
        cursorcli.close()
    }

    private fun vaciarTmp() {
        val keAndroid = conn!!.writableDatabase
        keAndroid.beginTransaction()
        try {
            keAndroid.execSQL("DELETE FROM ke_devlmtmp")
            keAndroid.setTransactionSuccessful()
            keAndroid.endTransaction()
        } catch (e: Exception) {
            keAndroid.endTransaction()
        }
    }

    private fun iraReclamos(documentoP: String, codigoCliente: String?, nombreCliente: String?) {
        vaciarTmp()
        val intent = Intent(applicationContext, ReclamosActivity::class.java)
        //coloco los datos que necesito llevarme al siguiente Activity
        intent.putExtra("documentoP", documentoP)
        intent.putExtra("codigoCliente", codigoCliente)
        intent.putExtra("nombreCliente", nombreCliente)
        intent.putExtra("cod_usuario", cod_usuario)
        startActivity(intent) // inicio la actividad
    }

    private fun iraDetalles(documentoP: String) {
        val builder = AlertDialog.Builder(ContextThemeWrapper(this, R.style.AlertDialogCustom))
        builder.setTitle("Detalle del Doc. $documentoP")
        val lvDetalledoc = ListView(this@DocumentosActivity)
        verLineasDocumento(documentoP)
        lineasAdapter = LineasAdapter(this@DocumentosActivity, listalineasdoc)
        lvDetalledoc.adapter = lineasAdapter
        lineasAdapter!!.notifyDataSetChanged()
        builder.setView(lvDetalledoc)
        val dialog = builder.create()
        dialog.show()
    }

    private fun verLineasDocumento(documentoP: String) {
        cargarLineasDocumento("https://" + enlaceEmpresa + "/webservice/lineasdocs.php?documento=" + documentoP.trim { it <= ' ' } + "&&agencia=" + codigoSucursal.trim { it <= ' ' })
        consultarLineasDoc()
    }

    private fun consultarLineasDoc() {
        val keAndroid = conn!!.writableDatabase
        var lineas: Lineas
        listalineasdoc = ArrayList()
        val cursor = keAndroid.rawQuery(
            "SELECT pid, codigo, nombre, cantidad, dmontoneto, dpreciofin  FROM ke_doclmv WHERE documento ='" + documento + "' AND pid NOT IN " +
                    "(SELECT kdel_pid FROM ke_devlmtmp)", null
        )
        while (cursor.moveToNext()) {
            lineas = Lineas()
            lineas.setPid(cursor.getString(0))
            lineas.setCodigo(cursor.getString(1))
            lineas.setNombre(cursor.getString(2))
            lineas.setCantidad(cursor.getDouble(3))
            lineas.setDmontototal(cursor.getDouble(4))
            lineas.setDpreciofin(cursor.getDouble(5))
            listalineasdoc!!.add(lineas)
        }
        lineasAdapter = LineasAdapter(this@DocumentosActivity, listalineasdoc)
        lineasAdapter!!.notifyDataSetChanged()
        cursor.close()
    }

    private fun cargarLineasDocumento(url: String) {
        val jsonArrayRequest: JsonArrayRequest =
            object : JsonArrayRequest(url, Response.Listener { response: JSONArray? ->
                if (response != null) {
                    conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null)
                    val keAndroid = conn!!.writableDatabase
                    var jsonObject: JSONObject //creamos un objeto json vacio
                    llCommit = false
                    keAndroid.beginTransaction()
                    for (i in 0 until response.length()) {
                        try {

                            //obtengo de la respuesta los datos en un json object
                            jsonObject = response.getJSONObject(i)
                            //preparo los campos para las operaciones
                            agencia = jsonObject.getString("agencia").trim { it <= ' ' }
                            tipodoc = jsonObject.getString("tipodoc").trim { it <= ' ' }
                            documento = jsonObject.getString("documento").trim { it <= ' ' }
                            tipodocv = jsonObject.getString("tipodocv").trim { it <= ' ' }
                            grupo = jsonObject.getString("grupo").trim { it <= ' ' }
                            subgrupo = jsonObject.getString("subgrupo").trim { it <= ' ' }
                            origen = jsonObject.getDouble("origen")
                            codigo = jsonObject.getString("codigo").trim { it <= ' ' }
                            codhijo = jsonObject.getString("codhijo").trim { it <= ' ' }
                            pid = jsonObject.getString("pid").trim { it <= ' ' }
                            nombre = jsonObject.getString("nombre").trim { it <= ' ' }
                            cantidad = jsonObject.getDouble("cantidad")
                            cntdevuelt = jsonObject.getDouble("cntdevuelt")
                            vndcntdevuelt = jsonObject.getDouble("vndcntdevuelt")
                            dvndmtototal = jsonObject.getDouble("dvndmtototal")
                            dpreciofin = jsonObject.getDouble("dpreciofin")
                            dpreciounit = jsonObject.getDouble("dpreciounit")
                            dmontoneto = jsonObject.getDouble("dmontoneto")
                            dmontototal = jsonObject.getDouble("dmontototal")
                            timpueprc = jsonObject.getDouble("timpueprc")
                            unidevuelt = jsonObject.getDouble("unidevuelt")
                            fechadoc = jsonObject.getString("fechadoc").trim { it <= ' ' }
                            vendedor = jsonObject.getString("vendedor").trim { it <= ' ' }
                            codcoord = jsonObject.getString("codcoord").trim { it <= ' ' }
                            fechamodifi = jsonObject.getString("fechamodifi").trim { it <= ' ' }
                            val qDocumentosLin = ContentValues()
                            qDocumentosLin.put("agencia", agencia)
                            qDocumentosLin.put("tipodoc", tipodoc)
                            qDocumentosLin.put("documento", documento)
                            qDocumentosLin.put("tipodocv", tipodocv)
                            qDocumentosLin.put("grupo", grupo)
                            qDocumentosLin.put("subgrupo", subgrupo)
                            qDocumentosLin.put("origen", origen)
                            qDocumentosLin.put("codigo", codigo)
                            qDocumentosLin.put("codhijo", codhijo)
                            qDocumentosLin.put("pid", pid)
                            qDocumentosLin.put("nombre", nombre)
                            qDocumentosLin.put("cantidad", cantidad)
                            qDocumentosLin.put("cntdevuelt", cntdevuelt)
                            qDocumentosLin.put("vndcntdevuelt", vndcntdevuelt)
                            qDocumentosLin.put("dvndmtototal", dvndmtototal)
                            qDocumentosLin.put("dpreciofin", dpreciofin)
                            qDocumentosLin.put("dpreciounit", dpreciounit)
                            qDocumentosLin.put("dmontoneto", dmontoneto)
                            qDocumentosLin.put("dmontototal", dmontototal)
                            qDocumentosLin.put("timpueprc", timpueprc)
                            qDocumentosLin.put("unidevuelt", unidevuelt)
                            qDocumentosLin.put("fechadoc", fechadoc)
                            qDocumentosLin.put("vendedor", vendedor)
                            qDocumentosLin.put("codcoord", codcoord)
                            qDocumentosLin.put("fechamodifi", fechamodifi)
                            val qcodigoLocal = keAndroid.rawQuery(
                                "SELECT count(pid) FROM ke_doclmv WHERE pid ='$pid'",
                                null
                            )
                            qcodigoLocal.moveToFirst()
                            val codigoExiste = qcodigoLocal.getInt(0)
                            if (codigoExiste > 0) {
                                keAndroid.update(
                                    "ke_doclmv",
                                    qDocumentosLin,
                                    "pid = ?",
                                    arrayOf(pid)
                                )
                            } else if (codigoExiste == 0) {
                                keAndroid.insert("ke_doclmv", null, qDocumentosLin)
                            }
                            llCommit = true
                            qcodigoLocal.close()
                        } catch (e: Exception) {
                            println("Error de inserción: $e")
                            llCommit = false
                            if (!llCommit!!) {
                                return@Listener
                            }
                        }
                    }
                    if (llCommit!!) {
                        keAndroid.setTransactionSuccessful()
                        keAndroid.endTransaction()
                    } else if (!llCommit!!) {
                        keAndroid.endTransaction()
                    }
                }
            }, Response.ErrorListener { error: VolleyError ->
                println("--Error--")
                error.printStackTrace()
            }) {
                override fun getParams(): Map<String, String> {
                    val parametros: MutableMap<String, String> = HashMap()
                    parametros["documento"] = documento!!
                    return parametros
                }
            }
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(jsonArrayRequest) //esto es el request que se envia al url a traves de la conexion volley, (el stringrequest esta armado arriba)
    }

    private fun consultarDocs() {
        val keAndroid = conn!!.writableDatabase
        var documentos: Documentos
        listadocs = ArrayList()
        val cursor = keAndroid.rawQuery(
            "SELECT documento, tipodocv, estatusdoc, dtotalfinal, emision, recepcion, dtotneto, dtotimpuest, dtotdescuen, aceptadev FROM ke_doccti WHERE codcliente ='$codigoCliente'",
            null
        )
        while (cursor.moveToNext()) {
            documentos = Documentos()
            documentos.setDocumento(cursor.getString(0))
            documentos.setTipodocv(cursor.getString(1))
            documentos.setEstatusdoc(cursor.getString(2))
            documentos.setDtotalfinal(cursor.getDouble(3))
            documentos.setEmision(cursor.getString(4))
            documentos.setRecepcion(cursor.getString(5))
            documentos.setDtotneto(cursor.getDouble(6))
            documentos.setDtotimpuest(cursor.getDouble(7))
            documentos.setDtotdescuen(cursor.getDouble(8))
            documentos.setAceptadev(cursor.getString(9))
            listadocs.add(documentos)
        }
        documentosAdapter =
            DocumentosAdapter(this@DocumentosActivity, listadocs) { position -> verDoc(position) }
        binding.lvDocumentos.layoutManager = LinearLayoutManager(this)
        binding.lvDocumentos.adapter = documentosAdapter
        documentosAdapter.notifyDataSetChanged()
        //ke_android.close();
        cursor.close()
    }

    private fun getfechaDocs() {
        val keAndroid = conn!!.writableDatabase
        val fechaUltmod = keAndroid.rawQuery(
            "SELECT fchhn_ultmod FROM tabla_aux WHERE tabla = 'ke_doccti'",
            null
        )
        fechaUltmod.moveToFirst()
        fechaDocs = fechaUltmod.getString(0)
        fechaUltmod.close()
    }

    private fun cargarCabeceraDocuemntosCliente(url: String) {
        val jsonArrayRequest: JsonArrayRequest =
            object : JsonArrayRequest(url, Response.Listener { response: JSONArray? ->
                if (response != null) {
                    conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null)
                    val keAndroid = conn!!.writableDatabase
                    var jsonObject: JSONObject //creamos un objeto json vacio
                    llCommit = false
                    keAndroid.beginTransaction()
                    for (i in 0 until response.length()) {
                        try {

                            //obtengo de la respuesta los datos en un json object
                            jsonObject = response.getJSONObject(i)
                            //preparo los campos para las operaciones
                            agencia = jsonObject.getString("agencia").trim { it <= ' ' }
                            tipodoc = jsonObject.getString("tipodoc").trim { it <= ' ' }
                            documento = jsonObject.getString("documento").trim { it <= ' ' }
                            tipodocv = jsonObject.getString("tipodocv").trim { it <= ' ' }
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
                            bsiva = jsonObject.getDouble("bsiva")
                            bsflete = jsonObject.getDouble("bsflete")
                            bsretencioniva = jsonObject.getDouble("bsretencioniva")
                            bsretencion = jsonObject.getDouble("bsretencion")
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
                            qDocumentosCab.put("bsiva", bsiva)
                            qDocumentosCab.put("bsflete", bsflete)
                            qDocumentosCab.put("bsretencion", bsretencion)
                            qDocumentosCab.put("bsretencioniva", bsretencioniva)
                            val qcodigoLocal = keAndroid.rawQuery(
                                "SELECT count(documento) FROM ke_doccti WHERE documento ='$documento'",
                                null
                            )
                            qcodigoLocal.moveToFirst()
                            val codigoExiste = qcodigoLocal.getInt(0)
                            qcodigoLocal.close()
                            if (codigoExiste > 0) {
                                keAndroid.update(
                                    "ke_doccti", qDocumentosCab, "documento = ?", arrayOf(
                                        documento
                                    )
                                )
                            } else if (codigoExiste == 0) {
                                keAndroid.insert("ke_doccti", null, qDocumentosCab)
                            }
                            llCommit = true
                        } catch (e: Exception) {
                            println("Error de inserción: $e")
                            llCommit = false
                            if (!llCommit!!) {
                                return@Listener
                            }
                        }
                    }
                    if (llCommit!!) {
                        keAndroid.setTransactionSuccessful()
                        keAndroid.endTransaction()

                        //si todo se dió bien, preparo la fecha para actualizar la tabla de docs
                        val fechaModif = Calendar.getInstance()
                        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        val fechaActualizada = sdf.format(fechaModif.time)
                        val qfechaDocs = ContentValues()
                        qfechaDocs.put("fchhn_ultmod", fechaActualizada)

                        //y actualizo
                        keAndroid.update(
                            "tabla_aux",
                            qfechaDocs,
                            "tabla = ?",
                            arrayOf("ke_doccti")
                        )
                        consultarDocs()
                    } else if (!llCommit!!) {
                        keAndroid.endTransaction()
                    }
                }
            }, Response.ErrorListener { error: VolleyError ->
                println("--Error--")
                error.printStackTrace()
            }) {
                override fun getParams(): Map<String, String> {
                    val parametros: MutableMap<String, String> = HashMap()
                    parametros["fecha_sinc"] = fechaDocs!!
                    parametros["codigo_cli"] = codigoCliente!!
                    return parametros
                }
            }
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(jsonArrayRequest) //esto es el request que se envia al url a traves de la conexion volley, (el stringrequest esta armado arriba)
    }

    override fun onResume() {
        evaluacionDeCargas()
        consultarDocs()
        super.onResume()
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
        private var grupo: String? = null
        private var subgrupo: String? = null
        private var codhijo: String? = null
        private var pid: String? = null
        private var codigo: String? = null
        private var nombre: String? = null
        private var fechadoc: String? = null
        private var vendedor: String? = null
        private var codcoord: String? = null
        private var fechamodifi: String? = null
        private var aceptadev: String? = null
        private var fechaDocs: String? = null
        private var cod_usuario: String? = null
        private var codigoEmpresa: String? = ""
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
        var bsiva: Double? = null
        var bsflete: Double? = null
        var bsretencion: Double? = null
        var bsretencioniva: Double? = null
    }

    override fun getTheme(): Resources.Theme {
        val theme = super.getTheme()
        theme.applyStyle(setThemeAgencia(Constantes.AGENCIA), true)
        // you could also use a switch if you have many themes that could apply
        return theme
    }
}