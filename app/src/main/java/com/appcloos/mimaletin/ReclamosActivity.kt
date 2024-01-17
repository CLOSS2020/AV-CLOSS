package com.appcloos.mimaletin

import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.InputType
import android.util.Base64
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.Button
import android.widget.EditText
import android.widget.GridView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.appcloos.mimaletin.databinding.ActivityReclamosBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Hashtable
import java.util.Locale

class ReclamosActivity : AppCompatActivity() {
    // DECLARACION DE TODOS LOS ELEMENTOS USADOS EN EL ACTIVITY Y DE LAS VARIABLES.
    lateinit var conn: AdminSQLiteOpenHelper
    var codigoCliente: String? = null
    var nombreCliente: String? = null
    var documento: String? = null
    var codSeleccionado: String? = null
    var motivoSeleccionado: String? = null
    var codigoTipo: String? = null
    var nomWeb: String? = null
    var nomRecl: String? = null
    var helpRec: String? = null
    var fechaMod: String? = null
    var listalineas: ArrayList<Lineas>? = null
    var listalineasdoc: ArrayList<Lineas>? = null
    var lineasTmpAdapter: LineasTmpAdapter? = null
    var lineasAdapter: LineasAdapter? = null
    var PICK_IMAGE_MULTIPLE = 1
    var posicionClasif = 0
    var imageUri: Uri? = null
    var listaImagenes: MutableList<Uri?> = ArrayList()
    var listaImagenesTabla: List<Uri> = ArrayList()
    var gridfotos: GridView? = null
    var baseAdapter: GridViewAdapter? = null
    var listaBase64Imagenes: MutableList<String> = ArrayList()
    var URL_UPLOAD_IMAGENES = "https://www.cloccidental.com/webservice/ImagenesReclamos.php"
    var listaCodigos = ArrayList<String>()
    var listaMotivos = ArrayList<String>()
    var codigosClasif = arrayOf<String>()
    var motivosClasif = arrayOf<String>()
    private var ll_commit: Boolean? = null
    private var arrayTi: JSONArray? = null

    private var cantidadADevolver1: Double = 0.0

    private val mOnNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.iclinearec -> {
                    val dialogolineas = AlertDialog.Builder(
                        ContextThemeWrapper(
                            this@ReclamosActivity,
                            setAlertDialogTheme(Constantes.AGENCIA)
                        )
                    )
                    // dialogolineas.setTitle("Elige el o los articulos a devolver");
                    val title = TextView(this@ReclamosActivity)
                    title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
                    title.typeface = Typeface.DEFAULT_BOLD
                    val lp = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    lp.setMargins(0, 20, 0, 30)
                    title.setPadding(0, 30, 0, 40)
                    title.layoutParams = lp
                    title.text = "Elige el o los articulos a devolver"
                    title.gravity = Gravity.CENTER
                    dialogolineas.setCustomTitle(title)
                    val lineasdoc = ListView(this@ReclamosActivity)
                    lineasdoc.setHeaderDividersEnabled(true)
                    cargarLineasDoc()
                    lineasAdapter = LineasAdapter(this@ReclamosActivity, listalineasdoc!!)
                    lineasdoc.adapter = lineasAdapter
                    lineasAdapter!!.notifyDataSetChanged()
                    dialogolineas.setView(lineasdoc)
                    lineasdoc.onItemClickListener =
                        OnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
                            val pid = listalineasdoc!![position].getPid()
                            val nombre = listalineasdoc!![position].getNombre()
                            val codigo = listalineasdoc!![position].getCodigo()
                            val cantidad = listalineasdoc!![position].getCantidad()
                            val precioFin = listalineasdoc!![position].getDpreciofin()
                            val cajatexto = EditText(
                                ContextThemeWrapper(
                                    this@ReclamosActivity,
                                    setEditTextTheme(Constantes.AGENCIA)
                                )
                            )
                            cajatexto.inputType = InputType.TYPE_CLASS_NUMBER
                            val dialogocantidad = AlertDialog.Builder(
                                ContextThemeWrapper(
                                    this@ReclamosActivity,
                                    setAlertDialogTheme(Constantes.AGENCIA)
                                )
                            )
                            dialogocantidad.setTitle("Selecciona la cantidad a devolver")
                            dialogocantidad.setView(cajatexto)
                            dialogocantidad.setPositiveButton("Aceptar") { _: DialogInterface?, _: Int ->
                                if (cajatexto.text.toString().isEmpty()) {
                                    Toast.makeText(
                                        this@ReclamosActivity,
                                        "Debes agregar una cantidad",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    cantidadADevolver1 =
                                        java.lang.Double.valueOf(cajatexto.text.toString())
                                    if (cantidadADevolver1 <= cantidad && cantidadADevolver1 > 0) {
                                        val lnMontodev = cantidadADevolver1 * precioFin
                                        guardarLineaEnTemp(
                                            nroDev,
                                            documento,
                                            pid,
                                            codigo,
                                            cantidad,
                                            cantidadADevolver1,
                                            nombre,
                                            lnMontodev,
                                            precioFin
                                        )
                                        cargarLineasDoc()
                                        lineasAdapter =
                                            LineasAdapter(this@ReclamosActivity, listalineasdoc!!)
                                        sumaNeto()
                                        lineasdoc.adapter = lineasAdapter
                                        lineasAdapter!!.notifyDataSetChanged()
                                        dialogolineas.setView(lineasdoc)
                                    } else if (cantidadADevolver1 > cantidad || cantidadADevolver1 <= 0) {
                                        Toast.makeText(
                                            this@ReclamosActivity,
                                            "Cantidad invalida",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                            val ventanalineas = dialogocantidad.create()
                            ventanalineas.show()

                            // Cambio de color a los botones del alert
                            val pbutton: Button =
                                ventanalineas.getButton(DialogInterface.BUTTON_POSITIVE)
                            pbutton.apply {
                                setTextColor(colorTextAgencia(Constantes.AGENCIA))
                            }
                        }

                    /*dialogolineas.setPositiveButton("Devolución Completa", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    agregarDocCompleto();
                                    CargarLineasDoc();
                                    lineasAdapter = new LineasAdapter(ReclamosActivity.this, listalineasdoc);
                                    SumaNeto();
                                    lineasdoc.setAdapter(lineasAdapter);
                                    lineasAdapter.notifyDataSetChanged();
                                }
                            });*/
                    val dialogodoc = dialogolineas.create()
                    dialogodoc.show()
                    return@OnNavigationItemSelectedListener true
                }

                R.id.icfotos -> {
                    abrirGaleria()
                    return@OnNavigationItemSelectedListener true
                }

                R.id.icnota -> {
                    val textomotivo = EditText(
                        ContextThemeWrapper(
                            this@ReclamosActivity,
                            setEditTextTheme(Constantes.AGENCIA)
                        )
                    )
                    textomotivo.setText(NotaReclamo)
                    textomotivo.inputType = InputType.TYPE_CLASS_TEXT
                    textomotivo.filters = arrayOf<InputFilter>(LengthFilter(250))
                    textomotivo.isSingleLine = false
                    textomotivo.setLines(6)
                    textomotivo.maxLines = 8
                    textomotivo.isHorizontalScrollBarEnabled = false
                    textomotivo.gravity = Gravity.START or Gravity.TOP
                    val builderNota = AlertDialog.Builder(
                        ContextThemeWrapper(
                            this@ReclamosActivity,
                            setAlertDialogTheme(Constantes.AGENCIA)
                        )
                    )
                    builderNota.setTitle("Introduce una Nota complementaria")
                    builderNota.setMessage(
                        "Cuentas con un máximo de 250 caracteres para detallar el motivo del reclamo."
                    )
                    builderNota.setView(textomotivo)
                    builderNota.setPositiveButton("Guardar") { _: DialogInterface?, _: Int ->
                        NotaReclamo = textomotivo.text.toString()
                        Toast.makeText(this@ReclamosActivity, "Motivo guardado", Toast.LENGTH_SHORT)
                            .show()
                    }
                    val dialogo = builderNota.create()
                    dialogo.show()
                    // Cambio de color a los botones del alert
                    val pbutton: Button = dialogo.getButton(DialogInterface.BUTTON_POSITIVE)
                    pbutton.apply {
                        setTextColor(colorTextAgencia(Constantes.AGENCIA))
                    }

                    return@OnNavigationItemSelectedListener true
                }

                R.id.icprocesar -> {
                    procesarReclamo(montoDev)
                    return@OnNavigationItemSelectedListener true
                }

                R.id.icclasif -> {
                    cargarCodigos()
                    listaCodigos = ArrayList()
                    listaMotivos = ArrayList()
                    codigosClasif = arrayOf()
                    motivosClasif = arrayOf()
                    val keAndroid = conn.writableDatabase
                    val cursorClasif = keAndroid.rawQuery(
                        "SELECT kdv_codclasif, kdv_nomclaweb FROM ke_tiporecl WHERE empresa = '$codEmpresa'",
                        null
                    )
                    listaCodigos.add("0")
                    listaMotivos.add("-Elija una Opción--")
                    while (cursorClasif.moveToNext()) {
                        listaCodigos.add(cursorClasif.getString(0))
                        listaMotivos.add(cursorClasif.getString(1))
                    }
                    cursorClasif.close()
                    codigosClasif = listaCodigos.toTypedArray()
                    motivosClasif = listaMotivos.toTypedArray()
                    val builder = AlertDialog.Builder(
                        ContextThemeWrapper(
                            this@ReclamosActivity,
                            setAlertDialogTheme(Constantes.AGENCIA)
                        )
                    )
                    builder.setTitle("Elige el motivo del reclamo")
                    builder.setSingleChoiceItems(
                        motivosClasif,
                        posicionClasif
                    ) { dialog: DialogInterface, which: Int ->
                        codSeleccionado = codigosClasif[which]
                        motivoSeleccionado = motivosClasif[which]
                        posicionClasif = which
                        // Toast.makeText(ReclamosActivity.this, "Elegiste " + motivoSeleccionado + "con codigo " + codSeleccionado,  Toast.LENGTH_SHORT).show();
                        dialog.dismiss()
                    }
                    builder.show()
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        }

    private lateinit var binding: ActivityReclamosBinding

    private lateinit var preferences: SharedPreferences
    private var codEmpresa: String? = null

    private var APP_RECLAMO_MONTO_MINIMO: Double = 15.0

    private var APP_RECLAMO_FOTOS_MINIMAS: Int = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReclamosBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED // mantener la activity en vertical

        preferences = getSharedPreferences("Preferences", MODE_PRIVATE)
        codEmpresa = preferences.getString("codigoEmpresa", null)

        // de la actividad de documentos, traigo los datos necesarios
        val intent = intent
        codigoCliente = intent.getStringExtra("codigoCliente")
        nombreCliente = intent.getStringExtra("nombreCliente")
        documento = intent.getStringExtra("documentoP")
        cod_usuario = intent.getStringExtra("cod_usuario")
        tipoDoc = "RCL"
        NotaReclamo = ""
        montoDev = 0.00
        posicionClasif = 0
        println(cod_usuario)

        setColors()

        // instancia del objeto de conexion a base de datos
        conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null)

        APP_RECLAMO_MONTO_MINIMO = conn.getConfigNum("APP_RECLAMO_MONTO_MINIMO", codEmpresa!!)
        APP_RECLAMO_FOTOS_MINIMAS =
            conn.getConfigNum("APP_RECLAMO_FOTOS_MINIMAS", codEmpresa!!).toInt()

        val keAndroid = conn.writableDatabase
        // valido el numero de la dev para obtener el nuevo correlativo
        val cursor = keAndroid.rawQuery(
            "SELECT MAX(kdev_numero) FROM ke_correladev WHERE kdev_vendedor ='$cod_usuario' AND empresa = '$codEmpresa'",
            null
        )
        if (cursor.moveToFirst()) {
            nroCorrelativo = cursor.getInt(0)
            nroCorrelativo += 1
            // CorrelativoTexto = String.valueOf(nroCorrelativo);
            CorrelativoTexto = "0000$nroCorrelativo"
            generarNumeroDevolucion(cod_usuario)
            supportActionBar!!.title = "Devo.:$nroDev"
        }
        cursor.close()
        cargarEnlace()
        URL_UPLOAD_IMAGENES = "https://$enlaceEmpresa/webservice/ImagenesReclamos.php"
        binding.tvClientedoc.text = "Cliente: $codigoCliente $nombreCliente"
        binding.tvDocumentodoc.text = "Nº DOC: $documento"
        binding.tvMontodev.text = "Monto Devolución: $$montoDev"
        cargarLineasDocumento(
            "https://$enlaceEmpresa/webservice/lineasdocs.php?documento=" + documento!!.trim {
                it <= ' '
            } + "&&agencia=" + codigoSucursal.trim { it <= ' ' }
        )
        consultarLineas()
        lineasTmpAdapter = LineasTmpAdapter(this@ReclamosActivity, listalineas)
        binding.lvLineasR.adapter = lineasTmpAdapter
        lineasTmpAdapter!!.notifyDataSetChanged()
        binding.menunavRec.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        gridfotos = findViewById(R.id.gridfotos)
        cargarCodigos()
        binding.lvLineasR.setOnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
            // obtengo los valores que me interesan de la lista en la posicion elegida
            val pid = listalineas!![position].getPid()
            val codigo = listalineas!![position].getCodigo()
            val cantidad = listalineas!![position].getCantidad()
            val precioFin = listalineas!![position].getDpreciofin()
            val cajacantidad =
                EditText(
                    ContextThemeWrapper(
                        this@ReclamosActivity,
                        setEditTextTheme(Constantes.AGENCIA)
                    )
                )
            cajacantidad.inputType = InputType.TYPE_CLASS_NUMBER
            val builder = AlertDialog.Builder(
                ContextThemeWrapper(
                    this@ReclamosActivity,
                    setAlertDialogTheme(Constantes.AGENCIA)
                )
            )
            builder.setTitle("Mensaje del Sistema")
            builder.setMessage("Por favor, selecciona una opción: ")
            builder.setNegativeButton("Borrar articulo") { _: DialogInterface?, _: Int ->
                keAndroid.beginTransaction()
                try {
                    keAndroid.execSQL(
                        "DELETE FROM ke_devlmtmp WHERE kdel_pid ='$pid' AND kdel_codart = '$codigo' AND empresa = '$codEmpresa'"
                    )
                    keAndroid.setTransactionSuccessful()
                    keAndroid.endTransaction()
                    sumaNeto()
                    consultarLineas()
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    keAndroid.endTransaction()
                }
            }
            builder.setPositiveButton("Modificar Cantidad") { _: DialogInterface?, _: Int ->
                val dialogocantidad = AlertDialog.Builder(
                    ContextThemeWrapper(
                        this@ReclamosActivity,
                        setAlertDialogTheme(Constantes.AGENCIA)
                    )
                ) // aqui lo llamo igual
                dialogocantidad.setTitle("Introduce una cantidad")
                dialogocantidad.setView(cajacantidad)
                dialogocantidad.setPositiveButton("Aceptar") { _: DialogInterface?, _: Int ->
                    val cantidadNueva = cajacantidad.text.toString().toInt()
                    println(cantidadNueva)
                    if (cantidadNueva <= 0) {
                        Toast.makeText(
                            this@ReclamosActivity,
                            "Debes introducir una cantidad mayor a 0",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        if (cantidadNueva > cantidad) {
                            Toast.makeText(
                                this@ReclamosActivity,
                                "Cantidad inválida.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            val cantidadN = cantidadNueva.toDouble()
                            val lnMtolinea = cantidadN * precioFin
                            try {
                                keAndroid.beginTransaction()
                                keAndroid.execSQL(
                                    "UPDATE ke_devlmtmp SET kdel_cantdev = $cantidadN, kdel_mtolinea =$lnMtolinea WHERE kdel_pid ='$pid' and kdel_codart ='$codigo' AND empresa = '$codEmpresa'"
                                )
                                keAndroid.setTransactionSuccessful()
                            } catch (ex: Exception) {
                                ex.printStackTrace()
                            } finally {
                                keAndroid.endTransaction()
                            }
                            sumaNeto()
                            consultarLineas()
                        }
                    }
                }
                val dialogocant = dialogocantidad.create()
                dialogocant.show()

                // Cambio de color a los botones del alert
                val pbutton: Button = dialogocant.getButton(DialogInterface.BUTTON_POSITIVE)
                pbutton.apply {
                    setTextColor(colorTextAgencia(Constantes.AGENCIA))
                }
            }
            val alertDialog = builder.create()
            alertDialog.show()

            // Cambio de color a los botones del alert
            val pbutton: Button = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE)
            pbutton.apply {
                setTextColor(colorTextAgencia(Constantes.AGENCIA))
            }

            val nbutton: Button = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
            nbutton.apply {
                setTextColor(colorTextAgencia(Constantes.AGENCIA))
            }
        }
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
        // ke_android.close();
    }

    private fun generarNumeroDevolucion(codUsuario: String?) {
        val fechaHoy = Date(Calendar.getInstance().timeInMillis)
        val formatofecha = SimpleDateFormat("yyMM", Locale.getDefault())
        val fecha = formatofecha.format(fechaHoy)
        CorrelativoTexto = right(CorrelativoTexto, 4)
        nroDev = codUsuario!!.trim { it <= ' ' } + "-" + tipoDoc + "-" + fecha + CorrelativoTexto
    }

    private fun right(valor: String?, longitud: Int): String {
        return valor!!.substring(valor.length - longitud)
    }

    private fun cargarCodigos() {
        cargarTiposReclamos(
            "https://" + enlaceEmpresa + "/webservice/obtenertiposclasif_V2.php" + "?agencia=" + codigoSucursal.trim { it <= ' ' }
        )
    }

    private fun cargarTiposReclamos(url: String) {
        val jsonArrayRequest: JsonArrayRequest =
            object : JsonArrayRequest(
                url,
                Response.Listener { response: JSONArray? ->
                    if (response != null) {
                        conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null)
                        val keAndroid = conn.writableDatabase
                        var jsonObject: JSONObject // creamos un objeto json vacio
                        ll_commit = false
                        keAndroid.beginTransaction()
                        keAndroid.delete("ke_tiporecl", "empresa = ?", arrayOf(codEmpresa!!))
                        for (i in 0 until response.length()) {
                            try {
                                // obtengo de la respuesta los datos en un json object
                                jsonObject = response.getJSONObject(i)
                                // preparo los campos para las operaciones
                                codigoTipo = jsonObject.getString("kdv_codclasif").trim { it <= ' ' }
                                nomWeb = jsonObject.getString("kdv_nomclaweb").trim { it <= ' ' }
                                nomRecl = jsonObject.getString("kdv_nomclasif").trim { it <= ' ' }
                                helpRec = jsonObject.getString("kdv_hlpclasif").trim { it <= ' ' }
                                fechaMod = jsonObject.getString("fechamodifi").trim { it <= ' ' }

                                val cv = ContentValues()
                                cv.put("kdv_codclasif", codigoTipo)
                                cv.put("kdv_nomclaweb", nomWeb)
                                cv.put("kdv_nomclasif", nomRecl)
                                cv.put("kdv_hlpclasif", helpRec)
                                cv.put("fechamodifi", fechaMod)
                                cv.put("empresa", codEmpresa)

                                keAndroid.insert("ke_tiporecl", null, cv)
                                ll_commit = true
                            } catch (e: Exception) {
                                println("Error de inserción: $e")
                                ll_commit = false
                                return@Listener
                            }
                        }
                        if (ll_commit!!) {
                            keAndroid.setTransactionSuccessful()
                            keAndroid.endTransaction()
                        } else {
                            keAndroid.endTransaction()
                        }
                    }
                },
                Response.ErrorListener { error: VolleyError ->
                    println("--Error--")
                    error.printStackTrace()
                    println("--Error--")
                }
            ) {
                override fun getParams(): Map<String, String> {
                    // parametros.put("documento", documento);
                    return HashMap()
                }
            }
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(
            jsonArrayRequest
        ) // esto es el request que se envia al url a traves de la conexion volley, (el stringrequest esta armado arriba)
    }

    // metodo para abrir la galeria y traer las fotos
    private fun abrirGaleria() {
        val intent = Intent()
        intent.type = "image/jpeg"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(intent, "SELECCIONA LAS IMAGENES"),
            PICK_IMAGE_MULTIPLE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {
            try {
                val clipData = data.clipData
                if (resultCode == RESULT_OK && requestCode == PICK_IMAGE_MULTIPLE) {
                    if (clipData == null) {
                        imageUri = data.data
                        listaImagenes.add(imageUri)
                        println(listaImagenes)
                    } else {
                        for (i in 0 until clipData.itemCount) {
                            listaImagenes.add(clipData.getItemAt(i).uri)
                            println(listaImagenes)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Algo salio mal", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, "No se seleccionó una imágen", Toast.LENGTH_LONG).show()
        }
        // super.onActivityResult(requestCode, resultCode, data);
        baseAdapter = GridViewAdapter(this@ReclamosActivity, listaImagenes)
        gridfotos!!.adapter = baseAdapter
    }

    private fun agregarDocCompleto() {
        val keAndroid = conn!!.writableDatabase
        keAndroid.beginTransaction()
        val cursor = keAndroid.rawQuery(
            "SELECT documento, pid, codigo, cantidad, nombre, dmontoneto, dpreciofin FROM ke_doclmv WHERE documento='$documento' AND empresa = '$codEmpresa'",
            null
        )
        try {
            keAndroid.execSQL("DELETE FROM ke_devlmtmp WHERE empresa = '$codEmpresa'")
            while (cursor.moveToNext()) {
                val lcPid = cursor.getString(1)
                val lcCodigo = cursor.getString(2)
                val lnCantidad = cursor.getDouble(3)
                val nombre = cursor.getString(4)
                val mtonetolinea = cursor.getDouble(5)
                val preciofinal = cursor.getDouble(6)
                val cv = ContentValues()
                cv.put("kdel_referencia", nroDev)
                cv.put("kdel_documento", documento)
                cv.put("kdel_pid", lcPid)
                cv.put("kdel_codart", lcCodigo)
                cv.put("kdel_cantdev", lnCantidad)
                cv.put("kdel_cantped", lnCantidad)
                cv.put("kdel_nombre", nombre)
                cv.put("kdel_mtolinea", mtonetolinea)
                cv.put("kdel_preciofin", preciofinal)
                cv.put("empresa", codEmpresa)

                keAndroid.insert("ke_devlmtmp", null, cv)
                Toast.makeText(
                    this@ReclamosActivity,
                    "¡Articulo(s) agregado(s)!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            cursor.close()
            keAndroid.setTransactionSuccessful()
            keAndroid.endTransaction()
            sumaNeto()
        } catch (ex: Exception) {
            Toast.makeText(this@ReclamosActivity, "Error al guardar la tabla", Toast.LENGTH_SHORT)
                .show()
            keAndroid.endTransaction()
        }
    }

    private fun sumaNeto() {
        val keAndroid = conn.writableDatabase
        val cursor = keAndroid.rawQuery(
            "SELECT SUM(kdel_mtolinea) FROM ke_devlmtmp WHERE empresa = '$codEmpresa'",
            null
        )
        if (cursor.moveToFirst()) {
            montoDev = cursor.getDouble(0)
            montoDev = montoDev.valorReal()
            binding.tvMontodev.text = "$" + montoDev
        } else {
            binding.tvMontodev.text = "$0.00"
        }
        cursor.close()
        keAndroid.close()
    }

    private fun procesarReclamo(totneto: Double) {
        var llCommit: Boolean
        val keAndroid = conn.writableDatabase
        val cursor = keAndroid.rawQuery(
            "SELECT kdel_preciofin, kdel_mtolinea, kdel_pid, kdel_codart, kdel_cantdev, kdel_cantped, kdel_nombre FROM ke_devlmtmp WHERE empresa = '$codEmpresa'",
            null
        )

        if (totneto <= APP_RECLAMO_MONTO_MINIMO) {
            Toast.makeText(
                this@ReclamosActivity,
                "El monto debe ser mayor a ${APP_RECLAMO_MONTO_MINIMO.toTwoDecimals()}",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        if (cursor.count > 0) {
            println("Codigo seleccionado: $codSeleccionado")
            if (codSeleccionado == null ||
                codSeleccionado!!.trim { it <= ' ' } == "0" ||
                codSeleccionado!!.trim { it <= ' ' } == ""
            ) {
                Toast.makeText(
                    this@ReclamosActivity,
                    "Debes elegir el motivo del reclamo",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                if (listaImagenes.size < APP_RECLAMO_FOTOS_MINIMAS) {
                    Toast.makeText(
                        this@ReclamosActivity,
                        "Debes añadir un minimo de $APP_RECLAMO_FOTOS_MINIMAS imágenes al reclamo",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    keAndroid.beginTransaction()
                    try {
                        val cursorti = keAndroid.rawQuery(
                            "SELECT agencia, tipodoc, codcliente, tipoprecio, vendedor, codcoord, nombrecli FROM ke_doccti WHERE documento = '$documento' AND empresa = '$codEmpresa'",
                            null
                        )
                        while (cursorti.moveToNext()) {
                            agencia = cursorti.getString(0)
                            tipodoc = cursorti.getString(1)
                            codigoCliente = cursorti.getString(2)
                            tipoprecio = cursorti.getDouble(3)
                            vendedor = cursorti.getString(4)
                            codcoord = cursorti.getString(5)
                            nombreCliente = cursorti.getString(6)
                        }
                        cursorti.close()

                        // generamos la fecha para la creacion y la primera actualización de fechamodifi
                        val fechaTabla = Date(Calendar.getInstance().timeInMillis)
                        val formatoFechaTabla =
                            SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault())

                        val cvCabecera = ContentValues()
                        cvCabecera.put("krti_ndoc", nroDev)
                        cvCabecera.put("krti_docfac", documento)
                        cvCabecera.put("krti_codcli", codigoCliente)
                        cvCabecera.put("krti_nombrecli", nombreCliente)
                        cvCabecera.put("krti_status", "0")
                        cvCabecera.put("krti_tipfac", tipodocv)
                        cvCabecera.put("krti_totneto", totneto)
                        cvCabecera.put("krti_agefac", agencia!!.trim { it <= ' ' })
                        cvCabecera.put("krti_tipfac", tipodoc)
                        cvCabecera.put("krti_tipprec", tipoprecio)
                        cvCabecera.put("krti_notas", NotaReclamo.trim { it <= ' ' })
                        cvCabecera.put("krti_codvend", vendedor!!.trim { it <= ' ' })
                        cvCabecera.put("krti_codcoor", codcoord!!.trim { it <= ' ' })
                        cvCabecera.put("krti_fchdoc", formatoFechaTabla.format(fechaTabla))
                        cvCabecera.put("fechamodifi", formatoFechaTabla.format(fechaTabla))
                        cvCabecera.put("kdv_codclasif", codSeleccionado)
                        cvCabecera.put("empresa", codEmpresa)

                        while (cursor.moveToNext()) {
                            val cvLineas = ContentValues()
                            cvLineas.put("krti_ndoc", nroDev)
                            cvLineas.put("krmv_tipprec", tipoprecio)
                            cvLineas.put("krmv_pid", cursor.getString(2).trim { it <= ' ' })
                            cvLineas.put(
                                "krmv_codart",
                                cursor.getString(3).trim { it <= ' ' }
                            )
                            cvLineas.put("krmv_cant", cursor.getDouble(4))
                            cvLineas.put(
                                "krmv_nombre",
                                cursor.getString(6).trim { it <= ' ' }
                            )
                            cvLineas.put("krmv_artprec", cursor.getDouble(0))
                            cvLineas.put("krmv_stot", cursor.getDouble(1))
                            cvLineas.put("fechamodifi", formatoFechaTabla.format(fechaTabla))
                            cvLineas.put("empresa", codEmpresa)

                            // insertamos las lineas del reclamo
                            keAndroid.insert("ke_rcllmv", null, cvLineas)
                        }
                        // insertamos la cabercera del reclamo
                        keAndroid.insert("ke_rclcti", null, cvCabecera)

                        // aumentamos el correlativo en la tabla de correlativos de reclamos
                        val aumentarCorrelatiodev = ContentValues()
                        aumentarCorrelatiodev.put("kdev_numero", nroCorrelativo)
                        aumentarCorrelatiodev.put("kdev_vendedor", cod_usuario)
                        aumentarCorrelatiodev.put("empresa", codEmpresa)

                        // insertamos el correlativo
                        keAndroid.insert("ke_correladev", null, aumentarCorrelatiodev)

                        // limpiamos la tabla de temporal
                        keAndroid.delete("ke_devlmtmp", "empresa = ?", arrayOf(codEmpresa!!))

                        // insertamos las imagenes en la tabla de imagenes
                        val imagenesreclamo = ContentValues()
                        for (i in listaImagenes.indices) {
                            imagenesreclamo.put("krti_ndoc", nroDev)
                            imagenesreclamo.put("kircl_rutafoto", listaImagenes[i].toString())
                            imagenesreclamo.put("empresa", codEmpresa)
                            keAndroid.insert("ke_imgrcl", null, imagenesreclamo)
                        }

                        // y actualizamos la fecha y el campo de aceptadev en la tabla de los documentos:
                        val bloquearReclamo = ContentValues()
                        val fechaModif = Calendar.getInstance()
                        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        val fechaModificado = sdf.format(fechaModif.time)
                        bloquearReclamo.put("aceptadev", "0")
                        bloquearReclamo.put("fechamodifi", fechaModificado)
                        bloquearReclamo.put("empresa", codEmpresa)
                        keAndroid.update(
                            "ke_doccti",
                            bloquearReclamo,
                            "documento='$documento' AND empresa = '$codEmpresa'",
                            null
                        )
                        llCommit = true /*si se dió bien, deberia andar correctamente*/
                    } catch (ex: Exception) {
                        println("--Error--")
                        ex.printStackTrace()
                        println("--Error--")
                        llCommit = false // al haber exception, tira a falso
                        keAndroid.endTransaction()
                        Toast.makeText(this@ReclamosActivity, ex.toString(), Toast.LENGTH_LONG)
                            .show()
                    }
                    if (llCommit) { // si las acciones se ejecutaron bien, hago commit
                        keAndroid.setTransactionSuccessful()
                        keAndroid.endTransaction()
                        Toast.makeText(
                            this@ReclamosActivity,
                            "Reclamo creado exitosamente",
                            Toast.LENGTH_LONG
                        ).show()
                        subirReclamo()
                        subirImagenes(nroDev)
                        finish()
                    } else { // pero si no, hago rollback
                        return
                    }
                }
            }
        } else {
            Toast.makeText(
                this@ReclamosActivity,
                "Por favor, agrega artículos al reclamo",
                Toast.LENGTH_SHORT
            ).show()
        }
        cursor.close()
    }

    private fun subirReclamo() {
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
                "krti_notas," +
                "kdv_codclasif"
        )
        val condicion = "krti_status = '0' AND krti_ndoc = '$nroDev' AND empresa = '$codEmpresa'"
        val cursor = keAndroid.query("ke_rclcti", campos, condicion, null, null, null, null)
        if (cursor.count > 0) {
            cargarReclamos()
        } else {
            Toast.makeText(this@ReclamosActivity, "No hay Reclamos por cargar.", Toast.LENGTH_SHORT)
                .show()
        }
        cursor.close()
    }

    private fun cargarReclamos() {
        var contadorReclamos = 0
        val keAndroid = conn!!.writableDatabase
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
                "krti_notas," +
                "kdv_codclasif"
        )
        val condicion = "krti_status = '0' AND krti_ndoc = '$nroDev' AND empresa = '$codEmpresa'"
        val cursorti = keAndroid.query("ke_rclcti", campos, condicion, null, null, null, null)
        arrayTi = JSONArray()
        val arrayMV = JSONArray()
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
                objetoCabecera.put("kdv_codclasif", codSeleccionado)
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
                    arrayMV.put(objetoLineas)
                }
                cursormv.close()
            } catch (ex: Exception) {
                ex.printStackTrace()
                Toast.makeText(
                    this@ReclamosActivity,
                    "Error al cargar los Reclamos$ex",
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
        println(jsonStrRCL)
        println("https://$enlaceEmpresa/webservice/Reclamos.php")
        try {
            insertarReclamo(jsonStrRCL)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun insertarReclamo(jsonStrRCL: String) {
        // genero un request queue y luego un strig request
        val requestQueue = Volley.newRequestQueue(this@ReclamosActivity)
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
                        this@ReclamosActivity,
                        "Reclamo(s) Subido(s)",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            Response.ErrorListener { error: VolleyError ->
                println("--Error--")
                error.printStackTrace()
                println("--Error--")
                Toast.makeText(this@ReclamosActivity, "Error en la subida", Toast.LENGTH_SHORT)
                    .show()
            }
        ) {
            override fun getParams(): Map<String, String>? {
                val params: MutableMap<String, String> = HashMap()
                params["jsonrcl"] = jsonStrRCL
                params["agencia"] = codigoSucursal
                println(params)
                return params
            }
        }
        requestQueue.add(stringRequest) // importante añadir el string request al request queue
    }

    private fun cambiarEstadoReclamo() {
        val keAndroid = conn!!.writableDatabase
        println(arrayTi)
        for (i in 0 until arrayTi!!.length()) {
            try {
                val objetodeCabeza = arrayTi!!.getJSONObject(i)
                val codigoDelReclamoenArray = objetodeCabeza.getString("krti_ndoc")
                keAndroid.execSQL(
                    "UPDATE ke_rclcti SET krti_status = '1' WHERE krti_ndoc ='$codigoDelReclamoenArray' AND empresa = '$codEmpresa'"
                )
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    private fun guardarLineaEnTemp(
        nroDev: String?,
        documento: String?,
        pid: String,
        codigo: String,
        cantidad: Double,
        cantidadADevolver: Double?,
        nombre: String,
        montoDevlinea: Double,
        precioFin: Double
    ) {
        val keAndroid = conn!!.writableDatabase
        keAndroid.beginTransaction()
        try {
            val guardarlinea = ContentValues()
            guardarlinea.put("kdel_referencia", nroDev)
            guardarlinea.put("kdel_documento", documento)
            guardarlinea.put("kdel_pid", pid)
            guardarlinea.put("kdel_codart", codigo)
            guardarlinea.put("kdel_mtolinea", montoDevlinea)
            guardarlinea.put("kdel_cantdev", cantidadADevolver)
            guardarlinea.put("kdel_cantped", cantidad)
            guardarlinea.put("kdel_nombre", nombre)
            guardarlinea.put("kdel_preciofin", precioFin)
            guardarlinea.put("empresa", codEmpresa)

            keAndroid.insert("ke_devlmtmp", null, guardarlinea)
            keAndroid.setTransactionSuccessful()
            keAndroid.endTransaction()
            Toast.makeText(this@ReclamosActivity, "¡artículo agregado!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            keAndroid.endTransaction()
        }
    }

    private fun cargarLineasDoc() {
        cargarLineasDocumento(
            "https://$enlaceEmpresa/webservice/lineasdocs.php?documento=" + documento!!.trim {
                it <= ' '
            } + "&&agencia=" + codigoSucursal.trim { it <= ' ' }
        )
        consultarLineasDoc()
    }

    private fun consultarLineasDoc() {
        val keAndroid = conn.writableDatabase
        var lineas: Lineas
        listalineasdoc = ArrayList()
        val cursor = keAndroid.rawQuery(
            "SELECT pid, codigo, nombre, cantidad, dmontoneto, dpreciofin  FROM ke_doclmv WHERE documento ='$documento' AND empresa = '$codEmpresa' AND pid NOT IN " +
                "(SELECT kdel_pid FROM ke_devlmtmp WHERE empresa = '$codEmpresa')",
            null
        )

        // Cursor cursor = ke_android.rawQuery("SELECT pid, codigo, nombre, cantidad, dmontoneto, dpreciofin  FROM ke_doclmv WHERE documento ='" + documento + "'", null);
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
        cursor.close()
    }

    // este metodo se acciona al pulsar agregar y llama a un webservice
    private fun cargarLineasDocumento(url: String) {
        val jsonArrayRequest: JsonArrayRequest =
            object : JsonArrayRequest(
                url,
                Response.Listener { response: JSONArray? ->
                    if (response != null) {
                        conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null)
                        val keAndroid = conn!!.writableDatabase
                        var jsonObject: JSONObject // creamos un objeto json vacio
                        ll_commit = false
                        keAndroid.beginTransaction()
                        for (i in 0 until response.length()) {
                            try {
                                // obtengo de la respuesta los datos en un json object
                                jsonObject = response.getJSONObject(i)
                                // preparo los campos para las operaciones
                                val agencia = jsonObject.getString("agencia").trim { it <= ' ' }
                                val tipodoc = jsonObject.getString("tipodoc").trim { it <= ' ' }
                                val documento = jsonObject.getString("documento").trim { it <= ' ' }
                                val tipodocv = jsonObject.getString("tipodocv").trim { it <= ' ' }
                                val grupo = jsonObject.getString("grupo").trim { it <= ' ' }
                                val subgrupo = jsonObject.getString("subgrupo").trim { it <= ' ' }
                                val origen = jsonObject.getDouble("origen")
                                val codigo = jsonObject.getString("codigo").trim { it <= ' ' }
                                val codhijo = jsonObject.getString("codhijo").trim { it <= ' ' }
                                val pid = jsonObject.getString("pid").trim { it <= ' ' }
                                val nombre = jsonObject.getString("nombre").trim { it <= ' ' }
                                val cantidad = jsonObject.getDouble("cantidad")
                                val cntdevuelt = jsonObject.getDouble("cntdevuelt")
                                val vndcntdevuelt = jsonObject.getDouble("vndcntdevuelt")
                                val dvndmtototal = jsonObject.getDouble("dvndmtototal")
                                val dpreciofin = jsonObject.getDouble("dpreciofin")
                                val dpreciounit = jsonObject.getDouble("dpreciounit")
                                val dmontoneto = jsonObject.getDouble("dmontoneto")
                                val dmontototal = jsonObject.getDouble("dmontototal")
                                val timpueprc = jsonObject.getDouble("timpueprc")
                                val unidevuelt = jsonObject.getDouble("unidevuelt")
                                val fechadoc = jsonObject.getString("fechadoc").trim { it <= ' ' }
                                val vendedor = jsonObject.getString("vendedor").trim { it <= ' ' }
                                val codcoord = jsonObject.getString("codcoord").trim { it <= ' ' }
                                val fechamodifi = jsonObject.getString("fechamodifi").trim { it <= ' ' }

                                val cv = ContentValues()
                                cv.put("agencia", agencia)
                                cv.put("tipodoc", tipodoc)
                                cv.put("documento", documento)
                                cv.put("tipodocv", tipodocv)
                                cv.put("grupo", grupo)
                                cv.put("subgrupo", subgrupo)
                                cv.put("origen", origen)
                                cv.put("codigo", codigo)
                                cv.put("codhijo", codhijo)
                                cv.put("pid", pid)
                                cv.put("nombre", nombre)
                                cv.put("cantidad", cantidad)
                                cv.put("cntdevuelt", cntdevuelt)
                                cv.put("vndcntdevuelt", vndcntdevuelt)
                                cv.put("dvndmtototal", dvndmtototal)
                                cv.put("dpreciofin", dpreciofin)
                                cv.put("dpreciounit", dpreciounit)
                                cv.put("dmontoneto", dmontoneto)
                                cv.put("dmontototal", dmontototal)
                                cv.put("timpueprc", timpueprc)
                                cv.put("unidevuelt", unidevuelt)
                                cv.put("fechadoc", fechadoc)
                                cv.put("vendedor", vendedor)
                                cv.put("codcoord", codcoord)
                                cv.put("fechamodifi", fechamodifi)
                                cv.put("empresa", codEmpresa)

                                if (conn.validarExistenciaCamposVarios(
                                        "ke_doclmv",
                                        ArrayList(
                                            mutableListOf("pid", "empresa")
                                        ),
                                        arrayListOf(pid, codEmpresa!!)
                                    )
                                ) {
                                    conn.updateJSONCamposVarios(
                                        "ke_doclmv",
                                        cv,
                                        "pid = ? AND empresa = ?",
                                        arrayOf(pid, codEmpresa!!)
                                    )
                                } else {
                                    conn.insertJSON("ke_doclmv", cv)
                                }

                                ll_commit = true
                            } catch (e: Exception) {
                                println("Error de inserción: $e")
                                ll_commit = false
                                return@Listener
                            }
                        }
                        if (ll_commit!!) {
                            keAndroid.setTransactionSuccessful()
                            keAndroid.endTransaction()
                            consultarLineas()
                        } else {
                            keAndroid.endTransaction()
                        }
                    }
                },
                Response.ErrorListener { error: VolleyError ->
                    println("--Error--")
                    error.printStackTrace()
                    println("--Error--")
                }
            ) {
                override fun getParams(): Map<String, String>? {
                    val parametros: MutableMap<String, String> = HashMap()
                    parametros["documento"] = documento!!
                    return parametros
                }
            }
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(
            jsonArrayRequest
        ) // esto es el request que se envia al url a traves de la conexion volley, (el stringrequest esta armado arriba)
    }

    private fun consultarLineas() {
        val keAndroid = conn.writableDatabase
        var lineas: Lineas
        listalineas = ArrayList()
        val cursor = keAndroid.rawQuery(
            "SELECT kdel_pid, kdel_codart, kdel_nombre, kdel_cantped, kdel_mtolinea, kdel_cantdev, kdel_preciofin FROM ke_devlmtmp WHERE kdel_documento='$documento' AND empresa = '$codEmpresa'",
            null
        )
        while (cursor.moveToNext()) {
            lineas = Lineas()
            lineas.setPid(cursor.getString(0))
            lineas.setCodigo(cursor.getString(1))
            lineas.setNombre(cursor.getString(2))
            lineas.setCantidad(cursor.getDouble(3))
            lineas.setDmontototal(cursor.getDouble(4))
            lineas.setCntdevuelt(cursor.getDouble(5))
            lineas.setDpreciofin(cursor.getDouble(6))
            listalineas!!.add(lineas)
        }
        cursor.close()
        lineasTmpAdapter = LineasTmpAdapter(this@ReclamosActivity, listalineas)
        binding.lvLineasR.adapter = lineasTmpAdapter
        lineasTmpAdapter!!.notifyDataSetChanged()
    }

    private fun subirImagenes(nrodev: String?) {
        val keAndroid = conn.writableDatabase
        var codigoRclImg = ""
        var rutafoto: String
        listaBase64Imagenes.clear()
        // preparo los parametros del query
        val tabla = "ke_imgrcl"
        val columnas = arrayOf(
            "krti_ndoc, " +
                "kircl_rutafoto"
        )
        val condicion = "krti_ndoc='$nrodev' AND empresa = '$codEmpresa'"

        // genero un cursor en base al query
        val cursor = keAndroid.query(tabla, columnas, condicion, null, null, null, null)
        while (cursor.moveToNext()) {
            codigoRclImg = cursor.getString(0)
            rutafoto = cursor.getString(1)
            // listaImagenesTabla.add(Uri.parse(rutafoto));
        }
        cursor.close()
        for (i in listaImagenes.indices) {
            try {
                val `is` = this@ReclamosActivity.contentResolver.openInputStream(
                    listaImagenes[i]!!
                )
                var bitmap = BitmapFactory.decodeStream(`is`)
                bitmap = redimensionarImagen(bitmap, 500f, 500f)
                val cadena = convertirUriToBase64(bitmap)
                enviarImagenes(codigoRclImg + "_" + i, cadena, nroDev)
                bitmap.recycle()
            } catch (e: IOException) {
                Toast.makeText(this@ReclamosActivity, "Imagen muy pequeña", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    // mtodo para redimensionar/reescalar la imagen
    /*
     * ESTE METODO SE ESTA REVISANDO PUESTO QUE ES PROBABLE QUE ESTE GENERANDO PROBLEMAS
     * PARA LA CARGA DE LOS RECLAMOS.*/
    private fun redimensionarImagen(bitmap: Bitmap, anchoNuevo: Float, altoNuevo: Float): Bitmap {
        val ancho = bitmap.width
        val alto = bitmap.height
        try {
            if (ancho > alto) {
                return if (ancho > anchoNuevo || alto > altoNuevo) {
                    val escalaAncho = anchoNuevo / ancho
                    val escalaAlto = altoNuevo / alto
                    val matrix = Matrix()
                    matrix.postScale(escalaAncho, escalaAlto)
                    Bitmap.createBitmap(bitmap, 0, 0, ancho, alto, matrix, false)
                } else {
                    bitmap
                }
            } else if (alto > ancho) {
                return if (ancho > anchoNuevo || alto > altoNuevo) {
                    val escalaAncho = anchoNuevo / ancho
                    val escalaAlto = altoNuevo / alto
                    val matrix = Matrix()
                    matrix.postScale(escalaAncho, escalaAlto)
                    Bitmap.createBitmap(bitmap, 0, 0, ancho, alto, matrix, false)
                } else {
                    bitmap
                }
                // si los anchos y altos son iguales
            } else if (alto == ancho) {
                return if (ancho > anchoNuevo || alto > altoNuevo) {
                    val escalaAncho = anchoNuevo / ancho
                    val escalaAlto = altoNuevo / alto
                    val matrix = Matrix()
                    matrix.postScale(escalaAncho, escalaAlto)
                    Bitmap.createBitmap(bitmap, 0, 0, ancho, alto, matrix, false)
                } else {
                    bitmap
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return bitmap
    }

    private fun enviarImagenes(nombre: String, cadena: String, docReclamo: String?) {
        val requestQueue = Volley.newRequestQueue(this)
        val stringRequest: StringRequest = object : StringRequest(
            Method.POST,
            URL_UPLOAD_IMAGENES,
            Response.Listener { response: String ->
                if (response == "Subido") {
                    println(response)
                }
            },
            Response.ErrorListener { error: VolleyError ->
                println("--Error--")
                error.printStackTrace()
                println("--Error--")
            }
        ) {
            override fun getParams(): Map<String, String>? {
                val params: MutableMap<String, String> = Hashtable()
                params["nombre"] = nombre
                params["imagen"] = cadena
                params["reclamo"] = docReclamo!!
                return params
            }
        }
        requestQueue.add(stringRequest)
    }

    private fun convertirUriToBase64(bitmap: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val bytes = baos.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    override fun onResume() {
        consultarLineas()
        // NotaReclamo = "";
        super.onResume()
    }

    companion object {
        var cod_usuario: String? = null
        var NotaReclamo = ""
        var krti_ndoc: String? = null
        var krti_status: String? = null
        var krti_codcli: String? = null
        var krti_docfac: String? = null
        var krti_nombrecli: String? = null
        var krti_docdev: String? = null
        var krti_docnc: String? = null
        var krti_fchdoc: String? = null
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
        var fechamodifiOP: String? = null
        var fechamodifilin: String? = null
        var enlaceEmpresa = ""
        var nombreEmpresa = ""
        var codigoSucursal = ""
        var krti_totneto: Double? = null
        var krmv_tipprec: Double? = null
        var krmv_cant: Double? = null
        var krmv_stot: Double? = null
        var krmv_artprec: Double? = null
        var krti_tipprec: Double? = null
        var totnetodef: Double? = null
        var cantdef: Double? = null
        var stotdef: Double? = null
        private var agencia: String? = null
        private var tipodoc: String? = null
        private var tipodocv: String? = null
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
        private var CorrelativoTexto: String? = null
        private var nroDev: String? = null
        private var tipoDoc: String? = null
        private var origen: Double? = null
        private var cantidad: Double? = null
        private var cntdevuelt: Double? = null
        private var vndcntdevuelt: Double? = null
        private var dvndmtototal: Double? = null
        private var dpreciofin: Double? = null
        private var dpreciounit: Double? = null
        private var dmontoneto: Double? = null
        private var dmontototal: Double? = null
        private var timpueprc: Double? = null
        private var unidevuelt: Double? = null
        private var montoDev = 0.00
        private var tipoprecio: Double? = null
        private var nroCorrelativo = 0
    }

    override fun getTheme(): Resources.Theme {
        val theme = super.getTheme()
        theme.applyStyle(setThemeAgencia(Constantes.AGENCIA), true)
        // you could also use a switch if you have many themes that could apply
        return theme
    }

    private fun setColors() {
        binding.apply {
            menunavRec.setBackgroundColor(menunavRec.colorAgencia(Constantes.AGENCIA))
            lvLineasR.setBackgroundResource(lvLineasR.colorListaReclamo(Constantes.AGENCIA))
            menunavRec.itemTextColor = menunavRec.colorIconReclamo(Constantes.AGENCIA)
            menunavRec.itemIconTintList = menunavRec.colorIconReclamo(Constantes.AGENCIA)
        }
    }
}
