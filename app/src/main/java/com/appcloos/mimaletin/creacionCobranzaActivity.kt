/*  para la seleccion del cliente y los documentos a registrar la cobranza */
package com.appcloos.mimaletin

import android.content.ContentValues
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class creacionCobranzaActivity : AppCompatActivity(), SelecDocAdapter.DocHolder.QuantityListener {

    private lateinit var spinnerClientes: Spinner
    private lateinit var conn: AdminSQLiteOpenHelper
    lateinit var ke_android: SQLiteDatabase
    lateinit var listacliente: ArrayList<Cliente>
    lateinit var listaDocsSeleccionados:ArrayList<String>
    lateinit var cursorCliente: Cursor
    lateinit var preferences:SharedPreferences
    private var cod_usuario:String? = ""
    private var codEmpresa:String? = ""
    lateinit var listaInfoClientes: ArrayList<String>
    var codigoCliente = ""
    var nombreCliente = ""
    var ll_commit = false
    lateinit var tv_cli: TextView
    lateinit  var documentos: Documentos
    lateinit var cursorDocs:Cursor
    lateinit var listadocs: ArrayList<Documentos>
    lateinit var bt_siguiente: Button
    private lateinit var rv_docs: RecyclerView
    val adapter: SelecDocAdapter = SelecDocAdapter()

    //declaracion de variables de texto
    lateinit var agencia:String; lateinit var tipodoc:String; lateinit var documento:String; lateinit var tipodocv: String; lateinit var ruta_parme:String; lateinit var emision:String
    lateinit var recepcion:String; lateinit var vence:String; lateinit var vendedor:String; lateinit var codcoord:String
    lateinit var fechamodifi:String; lateinit var aceptadev:String; lateinit var estatusdoc:String; lateinit var enlaceEmpresa:String; lateinit var nombreEmpresa:String; lateinit var codigoSucursal:String
    var fecha_auxiliar:String = "0001-01-01 00:00:00"; var fechavencedcto = "";var tienedcto = "";

    //declaracion de variables Double
    var contribesp:Double = 0.0; var tipoprecio = 0.0; var diascred = 0.0; var dtotneto: Double = 0.0; var dtotimpuest:Double = 0.0; var dtotalfinal:Double = 0.0
    var dtotpagos:Double = 0.0; var dtotdescuen: Double = 0.0; var dFlete:Double = 0.0; var dtotdev:Double = 0.0; var dvndmtototal:Double = 0.0; var dretencion:Double = 0.0;
    var dretencioniva:Double = 0.0; var bsiva:Double = 0.0; var bsflete:Double = 0.0; var bsretencion:Double = 0.0; var bsretencioniva:Double= 0.0; var tasadoc:Double = 0.00;
    var montodcto = 0.00;   var cbsret = 0.00; var cdret = 0.00; var cbsretiva = 0.00; var cdretiva = 0.00; var cbsrparme = 0.00; var cdrparme = 0.00; var bsmtofte = 0.00;
    var bsmtoiva = 0.00; var cbsretflete = 0.00; var cdretflete = 0.00; var retmun_mto = 0.00;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_creacion_cobranza)
        //inicialización de elementos
        conn         = AdminSQLiteOpenHelper(applicationContext, "ke_android", null)
        ke_android   = conn.writableDatabase
        preferences  = getSharedPreferences("Preferences", MODE_PRIVATE)
        cod_usuario  = preferences.getString("cod_usuario", null)
        codEmpresa   = preferences.getString("codigoEmpresa", null)
        tv_cli       = findViewById<TextView>(R.id.tv_cli)
        rv_docs      = findViewById(R.id.rv_docscli)
        bt_siguiente =  findViewById(R.id.bt_siguiente_cli)
        listaDocsSeleccionados = ArrayList()


        cargarEnlace()//cargo los datos de la empresa

        spinnerClientes = findViewById<Spinner>(R.id.sp_clientes_cxc)
        println(cod_usuario)//valid.

        cargarClientes()//cargo la lista de clientes



        spinnerClientes.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                if(position != 0){
                    listaDocsSeleccionados = ArrayList()

                    codigoCliente = listacliente.get(position-1).codigo
                    nombreCliente = listacliente.get(position-1).nombre

                    tv_cli.text = nombreCliente
                    cargarDocumentos("https://"+ enlaceEmpresa + "/webservice/documentos.php?fecha_sinc=" + fecha_auxiliar.trim() +"&&codigo_cli=" + codigoCliente.trim()+ "&&agencia=" + codigoSucursal.trim())
                    cargarSaldosFavor("https://$enlaceEmpresa/webservice/saldosFavor.php?codigo_cli=${codigoCliente.trim()}")
                    println("$fecha_auxiliar $codigoCliente $codigoSucursal")
                    buscarDocumentosCliente(codigoCliente)
                }
            }
        }


        bt_siguiente.setOnClickListener(View.OnClickListener {
            irAPrecobranza(listaDocsSeleccionados)
        })
    }

    private fun cargarSaldosFavor(URL: String) {
        ke_android = conn.readableDatabase
        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET,URL,null,
            { response ->
                if (response != null) {
                    if (response.getString("saldo") != "null" || response.getString("status") != "1") {
                        var saldos = response.getJSONObject("saldo")

                        val codcliente  = saldos.getString("codcliente")
                        val id_recibo   = saldos.getString("id_recibo")
                        val moneda      = saldos.getString("moneda")
                        val montocli    = saldos.getString("montocli")
                        val estado      = saldos.getString("estado")
                        val id_reciboap = saldos.getString("id_reciboap")
                        val agencia     = saldos.getString("agencia")
                        val tipodoc     = saldos.getString("tipodoc")
                        val documento   = saldos.getString("documento")
                        val fchregdif   = saldos.getString("fchregdif")
                        val usuario     = saldos.getString("usuario")
                        val edoweb      = saldos.getString("edoweb")
                        val fechamodifi = saldos.getString("fechamodifi")

                        var qSaldosCab: ContentValues = ContentValues()
                        qSaldosCab.put("codcliente", codcliente)
                        qSaldosCab.put("id_recibo", id_recibo)
                        qSaldosCab.put("moneda", moneda)
                        qSaldosCab.put("montocli", montocli)
                        qSaldosCab.put("estado", estado)
                        qSaldosCab.put("id_reciboap", id_reciboap)
                        qSaldosCab.put("agencia", agencia)
                        qSaldosCab.put("tipodoc", tipodoc)
                        qSaldosCab.put("documento", documento)
                        qSaldosCab.put("fchregdif", fchregdif)
                        qSaldosCab.put("usuario", usuario)
                        qSaldosCab.put("edoweb", edoweb)
                        qSaldosCab.put("fechamodifi", fechamodifi)

                        var qcodigoLocal: Cursor = ke_android.rawQuery("SELECT COUNT(codcliente) FROM ke_mtopendcli WHERE codcliente = '$codcliente';", null)
                        qcodigoLocal.moveToFirst()
                        //variable para obtener el conteo de documentos que ya esten en el telf
                        var codigoExistente = qcodigoLocal.getInt(0)

                        qcodigoLocal.close()

                        if (codigoExistente > 0) {
                            ke_android.update(
                                "ke_mtopendcli", qSaldosCab, "codcliente= ?", arrayOf(codcliente)
                            )
                        } else if (codigoExistente == 0) {
                            ke_android.insert("ke_mtopendcli", null, qSaldosCab)
                        }
                    }
                }
            },
            { error ->
                Toast.makeText(applicationContext, "Error al descargar saldos a favor", Toast.LENGTH_SHORT).show()
                println("Error -> $error")
            })

        var requestQueue: RequestQueue = Volley.newRequestQueue(this)
        requestQueue.add(jsonObjectRequest)
    }

    private fun irAPrecobranza(listaDocsSeleccionados: ArrayList<String>) {
        val intent = Intent(applicationContext, CxcReportActivity::class.java)
        intent.putExtra("cod_usuario", cod_usuario)
        intent.putExtra("codigoEmpresa", codEmpresa)
        intent.putStringArrayListExtra("listaDocs", listaDocsSeleccionados)
        startActivity(intent)
    }

    private fun cargarEnlace(){
        ke_android = conn.writableDatabase
        var columnas = arrayOf("kee_nombre," + "kee_url," + "kee_sucursal")
        var cursorE:Cursor
        cursorE = ke_android.query("ke_enlace", columnas, "1", null,null,null,null)

        while(cursorE.moveToNext()){
            nombreEmpresa  = cursorE.getString(0)
            enlaceEmpresa  = cursorE.getString(1)
            codigoSucursal = cursorE.getString(2)
        }
        cursorE.close()
    }


    private fun buscarDocumentosCliente(codigoCliente: String?) {
        ke_android = conn.writableDatabase
        listadocs = ArrayList()
        cursorDocs = ke_android.rawQuery("SELECT documento, tipodocv, estatusdoc, dtotalfinal, emision, recepcion, dtotneto, dtotimpuest, dtotdescuen, aceptadev, recepcion, vence, agencia, dFlete, bsflete, dtotpagos FROM ke_doccti WHERE codcliente ='" + codigoCliente + "' AND estatusdoc != '2'", null)

        while (cursorDocs.moveToNext()) {
            documentos = Documentos()
            documentos.setDocumento(cursorDocs.getString(0))
            documentos.setTipodocv(cursorDocs.getString(1))
            documentos.setEstatusdoc(cursorDocs.getString(2))
            documentos.setDtotalfinal(cursorDocs.getDouble(3))
            documentos.setEmision(cursorDocs.getString(4))
            documentos.setRecepcion(cursorDocs.getString(5))
            documentos.setDtotneto(cursorDocs.getDouble(6))
            documentos.setDtotimpuest(cursorDocs.getDouble(7))
            documentos.setDtotdescuen(cursorDocs.getDouble(8))
            documentos.setAceptadev(cursorDocs.getString(9))
            documentos.setRecepcion(cursorDocs.getString(10))
            documentos.setVence(cursorDocs.getString(11))
            documentos.setAgencia(cursorDocs.getString(12))
            documentos.setdFlete(cursorDocs.getDouble(13))
            documentos.setBsflete(cursorDocs.getDouble(14))
            documentos.setDtotpagos(cursorDocs.getDouble(15))
            listadocs.add(documentos)
        }

        rv_docs.layoutManager = LinearLayoutManager(applicationContext)
        adapter.SelecDocAdapter(applicationContext, listadocs, this)
        rv_docs.adapter = adapter
        adapter.notifyDataSetChanged()

    }

    //funcion para consultar los clientes y añadirlos al Spinner
    private fun cargarClientes() {
        ke_android = conn.readableDatabase
        var cliente: Cliente
        listacliente = ArrayList()
        cursorCliente = ke_android.rawQuery("SELECT codigo, nombre FROM cliempre WHERE vendedor ='"+ cod_usuario.toString().trim() +"' ORDER BY nombre ASC", null)

        while (cursorCliente.moveToNext()){
            cliente = Cliente()
            cliente.codigo = cursorCliente.getString(0)
            cliente.nombre = cursorCliente.getString(1)
            listacliente.add(cliente)
        }
        ke_android.close()
        obtenerinfoclientes()
        var adapterSpinner: ArrayAdapter<CharSequence>
        adapterSpinner = ArrayAdapter(this,R.layout.spinner_pagos_clientes, listaInfoClientes as List<CharSequence>)
        spinnerClientes.adapter = adapterSpinner
        adapterSpinner.notifyDataSetChanged()

    }
    //funcion para adicionar los clientes en la lista que sera agregada al Spinner
    private fun obtenerinfoclientes() {
        listaInfoClientes = ArrayList()
        listaInfoClientes.add("Seleccione un Cliente...")
        for (i in listacliente.indices) {
            listaInfoClientes.add(listacliente[i].getCodigo() + ": " + listacliente[i].getNombre().trim())
        }
    }

    //funcion para cargar los documentos
    private fun cargarDocumentos(URL:String){
        ke_android = conn.readableDatabase
        val jsonArrayRequest: JsonArrayRequest
        println("hasta aca todo bien $URL")
        jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET, // method
            URL, // url
            null, // json request
            { response -> // response listener


                if (response != null) {
                    println("si hubo respuesta")
                    ll_commit = false
                    ke_android.beginTransaction()
                    var jsonObject: JSONObject? = null
                    try {

                        // loop through the array elements
                        for (i in 0 until response.length()) {
                            jsonObject      = response.getJSONObject(i)
                            agencia         = jsonObject.getString("agencia").trim()
                            tipodoc         = jsonObject.getString("tipodoc").trim()
                            documento       = jsonObject.getString("documento").trim()
                            tipodocv        = jsonObject.getString("tipodocv").trim()
                            contribesp      = jsonObject.getDouble("contribesp")
                            ruta_parme      = jsonObject.getString("ruta_parme").trim()
                            tipoprecio      = jsonObject.getDouble("tipoprecio")
                            emision         = jsonObject.getString("emision").trim()
                            recepcion       = jsonObject.getString("recepcion").trim()
                            vence           = jsonObject.getString("vence").trim()
                            diascred        = jsonObject.getDouble("diascred")
                            estatusdoc      = jsonObject.getString("estatusdoc").trim()
                            dtotneto        = jsonObject.getDouble("dtotneto")
                            dtotimpuest     = jsonObject.getDouble("dtotimpuest")
                            dtotalfinal     = jsonObject.getDouble("dtotalfinal")
                            dtotpagos       = jsonObject.getDouble("dtotpagos")
                            dtotdescuen     = jsonObject.getDouble("dtotdescuen")
                            dFlete          = jsonObject.getDouble("dFlete")
                            dtotdev         = jsonObject.getDouble("dtotdev")
                            dvndmtototal    = jsonObject.getDouble("dvndmtototal")
                            dretencion      = jsonObject.getDouble("dretencion")
                            dretencioniva   = jsonObject.getDouble("dretencioniva")
                            vendedor        = jsonObject.getString("vendedor").trim()
                            codcoord        = jsonObject.getString("codcoord").trim()
                            fechamodifi     = jsonObject.getString("fechamodifi").trim()
                            aceptadev       = jsonObject.getString("aceptadev").trim()
                            bsiva           = jsonObject.getDouble("bsiva")
                            bsflete         = jsonObject.getDouble("bsflete")
                            bsretencioniva  = jsonObject.getDouble("bsretencioniva")
                            bsretencion     = jsonObject.getDouble("bsretencion")
                            tasadoc         = jsonObject.getDouble("tasadoc")
                            montodcto       = jsonObject.getDouble("mtodcto")
                            fechavencedcto  = jsonObject.getString("fchvencedcto")
                            tienedcto       = jsonObject.getString("tienedcto")
                            cbsret          = jsonObject.getDouble("cbsret")
                            cdret           = jsonObject.getDouble("cdret")
                            cbsretiva       = jsonObject.getDouble("cbsretiva")
                            cdretiva        = jsonObject.getDouble("cdretiva")
                            cbsrparme       = jsonObject.getDouble("cbsrparme")
                            cdrparme        = jsonObject.getDouble("cdrparme")
                            bsmtoiva        = jsonObject.getDouble("bsmtoiva")
                            bsmtofte        = jsonObject.getDouble("bsmtofte")
                            cbsretflete     = jsonObject.getDouble("cbsretflete")
                            cdretflete      = jsonObject.getDouble("cdretflete")
                            retmun_mto      = jsonObject.getDouble("retmun_mto")

                            var qDocumentosCab: ContentValues = ContentValues()
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

                            var qcodigoLocal: Cursor
                            qcodigoLocal = ke_android.rawQuery(
                                "SELECT count(documento) FROM ke_doccti WHERE documento ='" + documento + "'",
                                null
                            )
                            qcodigoLocal.moveToFirst()
                            //variable para obtener el conteo de documentos que ya esten en el telf
                            var codigoExistente = qcodigoLocal.getInt(0)

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

                        var fechaActualizada:String
                        var qfechaDocs:ContentValues = ContentValues()

                        var fecha_modif: Calendar = Calendar.getInstance()
                        var sdf: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        fechaActualizada = sdf.format(fecha_modif.time)
                        qfechaDocs.put("fchhn_ultmod", fechaActualizada)
                        ke_android.update("tabla_aux", qfechaDocs, "tabla = ?", arrayOf("ke_doccti"))
                        //consulto los documentos ya con la data actualizada
                        buscarDocumentosCliente(codigoCliente)

                    }else if(!ll_commit){
                        ke_android.endTransaction()
                    }

                }
            },
            {error -> // error listener
                //
            }
        )

        var requestQueue: RequestQueue = Volley.newRequestQueue(this)
        requestQueue.add(jsonArrayRequest)
    }

    override fun onQuantityChange(listaChange:ArrayList<String>){
        //Toast.makeText(this, listaChange.toString(), Toast.LENGTH_LONG).show()
        println(listaChange.toString())
        evaluarLista(listaChange)
    }

    override fun onResume() {
        super.onResume()
        listaDocsSeleccionados.clear()
        onQuantityChange(listaDocsSeleccionados)


    }

    private fun evaluarLista(listaChange:ArrayList<String>) {
        if (listaChange.isEmpty()){
            bt_siguiente.visibility  = View.INVISIBLE
            bt_siguiente.isEnabled   = false
            bt_siguiente.isClickable = false
            listaDocsSeleccionados = listaChange

        }else{
            bt_siguiente.visibility  = View.VISIBLE
            bt_siguiente.isEnabled   = true
            bt_siguiente.isClickable = true
            listaDocsSeleccionados = listaChange
        }
    }



}