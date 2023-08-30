/*
* PABLO CASTILLO
* 17/05/2022
* ACTIVITY PARA COLOCAR FECHAS Y DATOS A LAS RETENCIONES
* SEGUN SEA EL CASO
* */

package com.appcloos.mimaletin

import android.content.SharedPreferences
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import java.text.FieldPosition
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class RetencionesActivity : AppCompatActivity(), RetencionesAdapter.RetHolder.QuantityListener {
    //componentes

    private lateinit var conn: AdminSQLiteOpenHelper
    lateinit var keAndroid: SQLiteDatabase

    private lateinit var btAceptar: Button
    private lateinit var btAgregar: Button
    lateinit var btnAgregarFoto: Button

    lateinit var preferences: SharedPreferences

    private lateinit var etFecharetenciones: EditText
    private lateinit var etRefret: EditText
    private lateinit var etMontoret: EditText
    private lateinit var tilCxcMontoMain: TextInputLayout

    private lateinit var spTiposret: AutoCompleteTextView

    private lateinit var spDocumentos: AutoCompleteTextView

    private lateinit var listaRetenciones: ArrayList<Retenciones>
    lateinit var listaTiposRet: ArrayList<String>
    lateinit var listaDocs: ArrayList<String>

    private lateinit var rvRetenciones: RecyclerView
    val adapter: RetencionesAdapter = RetencionesAdapter()

    /*Esta variable servirá para determinar que retenciones
    * deben ser registradas en */
    private var variableBandera: String? = ""
    private var correlativoRetencion = ""
    private var refRet = ""
    private var montoRet: Double? = 0.00
    var tipoRetSeleccionada = ""
    private var fechaParaCorrelativo = ""
    private var fechaRet = ""
    var nroDoc = ""

    var valor = 0.0

    /*private lateinit var imageUri: Uri

    var listaImagenes: ArrayList<Uri> = ArrayList()

    private lateinit var baseAdapter: GridViewAdapter

    private lateinit var gvFotos: GridView*/


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_retenciones)

        conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null, 18)
        keAndroid = conn.writableDatabase
        /* //EditTexts de iva
         et_nroretiva     = findViewById(R.id.et_nroretiva)*/
        etFecharetenciones = findViewById(R.id.et_fecharetenciones)
        etMontoret = findViewById(R.id.et_montoret)
        etRefret = findViewById(R.id.et_refret)


        spTiposret = findViewById(R.id.sp_tiposret)
        spDocumentos = findViewById(R.id.sp_documentos)
        btAceptar = findViewById(R.id.bt_aceptar)
        btAgregar = findViewById(R.id.bt_agregarret)
        //btnAgregarFoto = findViewById(R.id.bt_agregarfoto)

        rvRetenciones = findViewById(R.id.rv_retenciones)

        tilCxcMontoMain = findViewById(R.id.til_cxc_monto_main)

        //gvFotos = findViewById(R.id.gv_fotos)

        listaRetenciones = ArrayList()
        listaTiposRet = ArrayList()
        listaDocs = ArrayList() // lista para indicar el doc al que se le aplica la ret

        preferences = getSharedPreferences("Preferences", MODE_PRIVATE)
        //cod_usuario   = preferences.getString("cod_usuario", null)
        //codEmpresa    = preferences.getString("codigoEmpresa", null)


        //listaTiposRet.add(0, "Seleccione un tipo de retención")
        listaTiposRet = intent.extras?.getStringArrayList("listatiposret") as ArrayList<String>
        //la lista de los docs y a cual les voy a cargar retencion
        listaDocs = intent.extras?.getStringArrayList("listaDocs") as ArrayList<String>
        println(listaDocs)
        activarRetenciones()
        verficarRetYaGuardadas()

        //listener del boton
        btAceptar.setOnClickListener {
            if (listaRetenciones.size > 0) {
                guardarRetenciones()
            } else {
                /*Toast.makeText(
                    applicationContext,
                    "Debes agregar al menos una retención",
                    Toast.LENGTH_SHORT
                ).show()*/
                listaRetenciones.clear()
                guardarRetenciones()
            }

        }

        etFecharetenciones.setOnClickListener { showDatePickerDialog() }

        btAgregar.setOnClickListener {
            addRetencion()
        }

        //seleccion del tipo de retencion
        /*spTiposret.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long,
            ) {
                tipoRetSeleccionada = listaTiposRet[position]
                cambioRetencion()
            }
        }*/

        spTiposret.onItemClickListener =
            AdapterView.OnItemClickListener { adapterView, view, position, id ->
                tipoRetSeleccionada = listaTiposRet[position]
                cambioRetencion()
            }

        /*spDocumentos.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long,
            ) {
                nroDoc = listaDocs[position]
                println("ELNUEMRO $nroDoc")
                cambioRetencion()
            }
        }*/

        spDocumentos.onItemClickListener =
            AdapterView.OnItemClickListener { adapterView, view, position, id ->
                nroDoc = listaDocs[position]
                println("ELNUEMRO $nroDoc")
                cambioRetencion()
            }


//        btnAgregarFoto.setOnClickListener {
//            val select = Intent()
//            select.type = "image/*"
//            select.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
//            select.action = Intent.ACTION_GET_CONTENT
//            startActivityForResult(
//                Intent.createChooser(select, "SELECCIONA LAS IMAGENES"),
//                1
//            )
//        }

    }

    /*override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            val clipData = data!!.clipData
            if (resultCode == RESULT_OK && requestCode == 1) {
                if (clipData == null) {
                    imageUri = data.data!!
                    listaImagenes.add(imageUri)
                    println(listaImagenes)
                } else {
                    for (i in 0 until clipData.itemCount) {
                        listaImagenes.add(clipData.getItemAt(i).uri)
                        println(listaImagenes)
                    }
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Algo salió mal", Toast.LENGTH_LONG).show()
        }
        //super.onActivityResult(requestCode, resultCode, data);
        baseAdapter = GridViewAdapter(this, listaImagenes)
        gvFotos.adapter = baseAdapter
    }*/

    private fun cambioRetencion() {
        var valorRetencion = ""
        when (tipoRetSeleccionada) {
            "iva" -> {
                valorRetencion = "cbsretiva"
            }

            "flete" -> {
                valorRetencion = "cbsretflete"
            }

            "parme" -> {
                valorRetencion = "cbsrparme"
            }
        }
        try {

            if (nroDoc.isEmpty()) {
                nroDoc = listaDocs[0]
            }

            val cursor: Cursor = keAndroid.rawQuery(
                "SELECT $valorRetencion FROM ke_doccti WHERE documento= '$nroDoc'",
                null
            )

            cursor.moveToFirst()

            valor = cursor.getDouble(0)

            cursor.close()

            tilCxcMontoMain.hint = "El monto requerido $valor Bs."
        } catch (e: Exception) {
            println("--Error--")
            e.printStackTrace()
            println("--Error--")
            tilCxcMontoMain.hint = "Monto (En Bss)"
        }


    }

    private fun addRetencion() {

        //valido que todo esté bien
        if (fechaRet == "" ||
            etRefret.text.toString() == "" ||
            fechaParaCorrelativo == "" ||
            etMontoret.text.toString() == ""
        ) {

            Toast.makeText(
                applicationContext,
                "Debe llenar todos los datos necesarios",
                Toast.LENGTH_SHORT
            ).show()

        } else {
            montoRet = etMontoret.text.toString().toDouble()
            refRet = etRefret.text.toString()
            if (montoRet!! >= valor - 1 && montoRet!! <= valor + 1) {
                if (refRet.length == 14) {
                    //si todo bien, debo añadir cada ret a la lista
                    correlativoRetencion = "$fechaParaCorrelativo$refRet"
                    val retenciones = Retenciones()

                    retenciones.fecharet = fechaRet
                    retenciones.nroret = correlativoRetencion
                    retenciones.montoret = montoRet as Double
                    retenciones.tiporet = tipoRetSeleccionada
                    retenciones.refret = refRet
                    retenciones.nrodoc = nroDoc

                    listaRetenciones.add(retenciones)
                    //limpio los campos
                    etMontoret.text.clear()
                    etRefret.text.clear()
                    etFecharetenciones.text.clear()
                    Toast.makeText(applicationContext, "Retención agregada", Toast.LENGTH_SHORT)
                        .show()

                    rvRetenciones.layoutManager = LinearLayoutManager(applicationContext)
                    adapter.retencionAdapter(applicationContext, listaRetenciones, this)
                    rvRetenciones.adapter = adapter
                    adapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(
                        applicationContext,
                        "La referencia de la retencion debe de tener 14 caracteres",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    applicationContext,
                    "Monto de la retencion invalida",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }

    }

    private fun showDatePickerDialog() {
        val datePicker = DatePickerFragment(
            "retencionesActivity",
            nroDoc,
            this
        ) { day, month, year -> onDateSelected(day, month, year) }
        datePicker.show(supportFragmentManager, "datePicker")

    }

    private fun onDateSelected(day: Int, month: Int, year: Int) {
        val fechaMostrar = "$year-$month-$day"

        //et_fecharetenciones.setText("$fechaMostrar")
        //en formato para query de tasas
        fechaRet = ""
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date: Date = formatter.parse(fechaMostrar)!!
        val formatNuevo = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formatNuevoVista = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        fechaRet = formatNuevo.format(date)

        etFecharetenciones.setText("Fecha: ${formatNuevoVista.format(date)}")

        val formatoParaCorrelativo = SimpleDateFormat("yyyyMM", Locale.getDefault())
        fechaParaCorrelativo = formatoParaCorrelativo.format(date)

    }


    private fun activarRetenciones() {
        if (listaTiposRet.size > 0) {
            val adapter: ArrayAdapter<CharSequence> = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                listaTiposRet as List<CharSequence>
            )
            spTiposret.setAdapter(adapter)
            adapter.notifyDataSetChanged()
        }

        if (listaDocs.size > 0) {
            val adapter: ArrayAdapter<CharSequence> = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                listaDocs as List<CharSequence>
            )
            spDocumentos.setAdapter(adapter)
            adapter.notifyDataSetChanged()
        }

    }


    private fun guardarRetenciones() {

        //var listaRetCadena:Set<String> = listaRetenciones.groupBy { it.tiporet }.keys
        for (i in listaRetenciones) {
            println(
                "nro doc: ${i.nrodoc}, " +
                        "tipo ${i.tiporet}, " +
                        "nroret ${i.nroret}, " +
                        "refret ${i.refret}  " +
                        "fecha ${i.fecharet}  " +
                        "monto ${i.montoret}"
            )

        }
        val bundle = Bundle()
        bundle.putSerializable("listaRetenciones", listaRetenciones)
        intent.putExtras(bundle)
        setResult(RESULT_OK, intent)
        finish()

    }

    private fun evaluarRetaPagar(): String {
        var valorRetorno = "0"

        when (variableBandera) {
            "iva" -> valorRetorno = "0"
            "flete" -> valorRetorno = "1"
            "todas" -> valorRetorno = "2"
            "todasyparme" -> valorRetorno = "3"
        }
        println(valorRetorno)
        return valorRetorno
    }

    override fun onQuantityChange(position: Int) {
        println(position)
        adapter.notifyItemRemoved(position)
        listaRetenciones.removeAt(position)

        //evaluarLista(listaChange)

    }

    private fun evaluarLista(listaChange: ArrayList<Retenciones>) {
        listaRetenciones = if (listaChange.isEmpty()) {

            listaChange

        } else {

            listaChange
        }
        rvRetenciones.layoutManager = LinearLayoutManager(applicationContext)
        adapter.retencionAdapter(applicationContext, listaRetenciones, this)
        rvRetenciones.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    private fun verficarRetYaGuardadas() {

        val bundle: Bundle = intent.extras!!
        listaRetenciones = bundle.getSerializable("listaRetenciones") as ArrayList<Retenciones>
        if (listaRetenciones.size > 0) {

            rvRetenciones.layoutManager = LinearLayoutManager(applicationContext)
            adapter.retencionAdapter(applicationContext, listaRetenciones, this)
            rvRetenciones.adapter = adapter
            adapter.notifyDataSetChanged()
        }

    }


}





