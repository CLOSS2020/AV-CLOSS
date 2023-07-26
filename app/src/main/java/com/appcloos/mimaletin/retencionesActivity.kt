/*
* PABLO CASTILLO
* 17/05/2022
* ACTIVITY PARA COLOCAR FECHAS Y DATOS A LAS RETENCIONES
* SEGUN SEA EL CASO
* */

package com.appcloos.mimaletin

import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*


class retencionesActivity : AppCompatActivity(), retencionesAdapter.RetHolder.QuantityListener {
    //componentes

    private lateinit var conn: AdminSQLiteOpenHelper
    lateinit var ke_android: SQLiteDatabase

    lateinit var bt_aceptar: Button
    lateinit var bt_agregar: Button
    lateinit var btnAgregarFoto: Button

    lateinit var preferences: SharedPreferences

    lateinit var et_fecharetenciones: EditText
    lateinit var et_refret: EditText
    lateinit var et_montoret: EditText

    lateinit var sp_tiposret: Spinner
    lateinit var sp_documentos: Spinner

    lateinit var listaRetenciones: ArrayList<Retenciones>
    lateinit var listaTiposRet: ArrayList<String>
    lateinit var listaDocs: ArrayList<String>

    lateinit var rv_retenciones: RecyclerView
    val adapter: retencionesAdapter = retencionesAdapter()

    /*Esta variable servirá para determinar que retenciones
    * deben ser registradas en */
    var variableBandera: String? = ""
    var correlativoRetencion = ""
    var refRet = ""
    var montoRet: Double? = 0.00
    var tipoRetSeleccionada = ""
    var fechaParaCorrelativo = ""
    var fechaRet = ""
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
        ke_android = conn.writableDatabase
        /* //EditTexts de iva
         et_nroretiva     = findViewById(R.id.et_nroretiva)*/
        et_fecharetenciones = findViewById(R.id.et_fecharetenciones)
        et_montoret = findViewById(R.id.et_montoret)
        et_refret = findViewById(R.id.et_refret)


        sp_tiposret = findViewById(R.id.sp_tiposret)
        sp_documentos = findViewById(R.id.sp_documentos)
        bt_aceptar = findViewById(R.id.bt_aceptar)
        bt_agregar = findViewById(R.id.bt_agregarret)
        //btnAgregarFoto = findViewById(R.id.bt_agregarfoto)

        rv_retenciones = findViewById(R.id.rv_retenciones)

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
        bt_aceptar.setOnClickListener(View.OnClickListener {
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

        })

        et_fecharetenciones.setOnClickListener { showDatePickerDialog() }

        bt_agregar.setOnClickListener(View.OnClickListener {
            addRetencion()
        })

        //seleccion del tipo de retencion
        sp_tiposret.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long,
            ) {
                tipoRetSeleccionada = listaTiposRet.get(position)
                cambioRetencion()
            }
        }

        sp_documentos.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long,
            ) {
                nroDoc = listaDocs.get(position)
                println("ELNUEMRO $nroDoc")
                cambioRetencion()
            }
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

            if (nroDoc.length == 0) {
                nroDoc = listaDocs[0]
            }

            val cursor: Cursor = ke_android.rawQuery(
                "SELECT $valorRetencion FROM ke_doccti WHERE documento= '$nroDoc'",
                null
            )

            cursor.moveToFirst()

            valor = cursor.getDouble(0)

            cursor.close()

            et_montoret.hint = "El monto requerido $valor Bs."
        } catch (e: Exception) {
            et_montoret.hint = "Monto (En Bss)"
        }


    }

    private fun addRetencion() {

        //valido que todo esté bien
        if (fechaRet == "" || et_refret.text.toString() == "" || fechaParaCorrelativo == "" || et_montoret.text.toString() == ""
        ) {

            Toast.makeText(
                applicationContext,
                "Debe llenar todos los datos necesarios",
                Toast.LENGTH_SHORT
            ).show()

        } else {
            montoRet = et_montoret.text.toString().toDouble()
            refRet = et_refret.text.toString()
            if (montoRet!! >= valor - 1 && montoRet!! <= valor + 1) {
                if (refRet.length == 14) {
                    //si todo bien, debo añadir cada ret a la lista
                    correlativoRetencion = "$fechaParaCorrelativo$refRet"
                    var retenciones: Retenciones = Retenciones()

                    retenciones.fecharet = fechaRet
                    retenciones.nroret = correlativoRetencion
                    retenciones.montoret = montoRet as Double
                    retenciones.tiporet = tipoRetSeleccionada
                    retenciones.refret = refRet
                    retenciones.nrodoc = nroDoc

                    listaRetenciones.add(retenciones)
                    //limpio los campos
                    et_montoret.text.clear()
                    et_refret.text.clear()
                    et_fecharetenciones.text.clear()
                    Toast.makeText(applicationContext, "Retención agregada", Toast.LENGTH_SHORT)
                        .show()

                    rv_retenciones.layoutManager = LinearLayoutManager(applicationContext)
                    adapter.retencionAdapter(applicationContext, listaRetenciones, this)
                    rv_retenciones.adapter = adapter
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

    fun onDateSelected(day: Int, month: Int, year: Int) {
        var fechaMostrar = "$year-$month-$day"

        //et_fecharetenciones.setText("$fechaMostrar")
        //en formato para query de tasas
        fechaRet = ""
        var formatter: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        var date: Date = formatter.parse(fechaMostrar)
        var formatNuevo: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        var formatNuevoVista: SimpleDateFormat = SimpleDateFormat("dd/MM/yyyy")
        fechaRet = formatNuevo.format(date)

        et_fecharetenciones.setText("Fecha: ${formatNuevoVista.format(date)}")

        var formatoParaCorrelativo: SimpleDateFormat = SimpleDateFormat("yyyyMM")
        fechaParaCorrelativo = formatoParaCorrelativo.format(date)

    }


    private fun activarRetenciones() {
        if (listaTiposRet.size > 0) {
            var adapter: ArrayAdapter<CharSequence>
            adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                listaTiposRet as List<CharSequence>
            )
            sp_tiposret.adapter = adapter
            adapter.notifyDataSetChanged()
        }

        if (listaDocs.size > 0) {
            var adapter: ArrayAdapter<CharSequence>
            adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                listaDocs as List<CharSequence>
            )
            sp_documentos.adapter = adapter
            adapter.notifyDataSetChanged()
        }

    }


    private fun guardarRetenciones() {

        //var listaRetCadena:Set<String> = listaRetenciones.groupBy { it.tiporet }.keys
        for (i in listaRetenciones) {
            println("nro doc: ${i.nrodoc}, tipo ${i.tiporet}, nroret ${i.nroret}, refret ${i.refret}  fecha ${i.fecharet}  monto ${i.montoret}")

        }
        var bundle: Bundle = Bundle()
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

    override fun onQuantityChange(listaChange: ArrayList<Retenciones>) {
        println(listaChange)
        evaluarLista(listaChange)

    }

    private fun evaluarLista(listaChange: ArrayList<Retenciones>) {
        if (listaChange.isEmpty()) {

            listaRetenciones = listaChange

        } else {

            listaRetenciones = listaChange
        }
        rv_retenciones.layoutManager = LinearLayoutManager(applicationContext)
        adapter.retencionAdapter(applicationContext, listaRetenciones, this)
        rv_retenciones.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    private fun verficarRetYaGuardadas() {

        var bundle: Bundle = Bundle()
        bundle = intent.extras!!
        listaRetenciones = bundle.getSerializable("listaRetenciones") as ArrayList<Retenciones>
        if (listaRetenciones.size > 0) {

            rv_retenciones.layoutManager = LinearLayoutManager(applicationContext)
            adapter.retencionAdapter(applicationContext, listaRetenciones, this)
            rv_retenciones.adapter = adapter
            adapter.notifyDataSetChanged()
        }

    }


}





