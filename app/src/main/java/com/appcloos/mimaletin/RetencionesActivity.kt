/*
* PABLO CASTILLO
* 17/05/2022
* ACTIVITY PARA COLOCAR FECHAS Y DATOS A LAS RETENCIONES
* SEGUN SEA EL CASO
* */

package com.appcloos.mimaletin

import android.content.SharedPreferences
import android.content.res.Resources
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.text.InputFilter
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.appcloos.mimaletin.classes.DecimalDigitsInputFilter
import com.appcloos.mimaletin.databinding.ActivityRetencionesBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class RetencionesActivity : AppCompatActivity(), RetencionesAdapter.RetHolder.QuantityListener {
    //componentes

    private lateinit var conn: AdminSQLiteOpenHelper
    lateinit var keAndroid: SQLiteDatabase

    lateinit var btnAgregarFoto: Button

    lateinit var preferences: SharedPreferences


    private lateinit var listaRetenciones: ArrayList<Retenciones>
    private lateinit var listaTiposRet: ArrayList<String>
    lateinit var listaDocs: ArrayList<String>

    val adapter: RetencionesAdapter = RetencionesAdapter()

    /*Esta variable servirá para determinar que retenciones
    * deben ser registradas en */
    private var variableBandera: String? = ""
    private var correlativoRetencion = ""
    private var refRet = ""
    private var montoRet: Double? = 0.00
    private var tipoRetSeleccionada = ""
    private var fechaParaCorrelativo = ""
    private var fechaRet = ""
    private var nroDoc = ""

    var valor = 0.0

    private var codEmpresa: String? = null

    /*private lateinit var imageUri: Uri

    var listaImagenes: ArrayList<Uri> = ArrayList()

    private lateinit var baseAdapter: GridViewAdapter

    private lateinit var gvFotos: GridView*/

    private lateinit var binding: ActivityRetencionesBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRetencionesBinding.inflate(layoutInflater)
        setColors()
        setContentView(binding.root)

        conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null)
        keAndroid = conn.writableDatabase/* //EditTexts de iva
         et_nroretiva     = findViewById(R.id.et_nroretiva)*/


        //btnAgregarFoto = findViewById(R.id.bt_agregarfoto)


        //gvFotos = findViewById(R.id.gv_fotos)

        listaRetenciones = ArrayList()
        listaTiposRet = ArrayList()
        listaDocs = ArrayList() // lista para indicar el doc al que se le aplica la ret

        preferences = getSharedPreferences("Preferences", MODE_PRIVATE)
        //cod_usuario   = preferences.getString("cod_usuario", null)
        codEmpresa = preferences.getString("codigoEmpresa", null)


        //listaTiposRet.add(0, "Seleccione un tipo de retención")
        listaTiposRet = intent.extras?.getStringArrayList("listatiposret") as ArrayList<String>
        //la lista de los docs y a cual les voy a cargar retencion
        listaDocs = intent.extras?.getStringArrayList("listaDocs") as ArrayList<String>
        println(listaDocs)
        activarRetenciones()
        verficarRetYaGuardadas()

        //listener del boton
        binding.btAceptar.setOnClickListener {
            if (listaRetenciones.size > 0) {
                guardarRetenciones()
            } else {/*Toast.makeText(
                    applicationContext,
                    "Debes agregar al menos una retención",
                    Toast.LENGTH_SHORT
                ).show()*/
                listaRetenciones.clear()
                guardarRetenciones()
            }

        }

        binding.etFecharetenciones.setOnClickListener { showDatePickerDialog() }

        binding.btAgregarret.setOnClickListener {
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

        binding.spTiposret.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
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

        binding.spDocumentos.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
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

        binding.etMontoret.filters = arrayOf<InputFilter>(DecimalDigitsInputFilter(10, 2))
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
                "SELECT $valorRetencion FROM ke_doccti WHERE documento= '$nroDoc'", null
            )

            cursor.moveToFirst()

            valor = cursor.getDouble(0)

            cursor.close()

            binding.tilCxcMontoMain.hint = "El monto requerido $valor Bs."
        } catch (e: Exception) {
            println("--Error--")
            e.printStackTrace()
            println("--Error--")
            binding.tilCxcMontoMain.hint = "Monto (En Bss)"
        }


    }

    private fun addRetencion() {

        //valido que todo esté bien
        if (fechaRet == "" || binding.etRefret.text.toString() == "" || fechaParaCorrelativo == "" || binding.etMontoret.text.toString() == "") {

            Toast.makeText(
                applicationContext, "Debe llenar todos los datos necesarios", Toast.LENGTH_SHORT
            ).show()

        } else {
            montoRet = binding.etMontoret.text.toString().toDouble()
            refRet = binding.etRefret.text.toString()

            listaRetenciones.forEach { list ->
                if (list.refret == refRet) {
                    Toast.makeText(this, "Referencia previamente utilizada", Toast.LENGTH_SHORT)
                        .show()
                    return
                }
            }

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
                    binding.etMontoret.text!!.clear()
                    binding.etRefret.text!!.clear()
                    binding.etFecharetenciones.text!!.clear()
                    Toast.makeText(applicationContext, "Retención agregada", Toast.LENGTH_SHORT)
                        .show()

                    binding.rvRetenciones.layoutManager = LinearLayoutManager(applicationContext)
                    adapter.retencionAdapter(applicationContext, listaRetenciones, this)
                    binding.rvRetenciones.adapter = adapter
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
                    applicationContext, "Monto de la retencion invalida", Toast.LENGTH_SHORT
                ).show()
            }

        }

    }

    private fun showDatePickerDialog() {
        val datePicker = DatePickerFragment(
            "retencionesActivity", nroDoc, this
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

        binding.etFecharetenciones.setText("Fecha: ${formatNuevoVista.format(date)}")

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
            binding.spTiposret.setAdapter(adapter)
            adapter.notifyDataSetChanged()
        }

        if (listaDocs.size > 0) {
            val adapter: ArrayAdapter<CharSequence> = ArrayAdapter(
                this, android.R.layout.simple_spinner_dropdown_item, listaDocs as List<CharSequence>
            )
            binding.spDocumentos.setAdapter(adapter)
            adapter.notifyDataSetChanged()
        }

    }


    private fun guardarRetenciones() {

        //var listaRetCadena:Set<String> = listaRetenciones.groupBy { it.tiporet }.keys
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

    override fun onQuantityChange(listaChange: Int) {
        println(listaChange)
        adapter.notifyItemRemoved(listaChange)
        listaRetenciones.removeAt(listaChange)

        //evaluarLista(listaChange)

    }

    private fun evaluarLista(listaChange: ArrayList<Retenciones>) {
        listaRetenciones = if (listaChange.isEmpty()) {

            listaChange

        } else {

            listaChange
        }
        binding.rvRetenciones.layoutManager = LinearLayoutManager(applicationContext)
        adapter.retencionAdapter(applicationContext, listaRetenciones, this)
        binding.rvRetenciones.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    private fun verficarRetYaGuardadas() {

        val bundle: Bundle = intent.extras!!
        listaRetenciones = bundle.getSerializable("listaRetenciones") as ArrayList<Retenciones>
        if (listaRetenciones.size > 0) {

            binding.rvRetenciones.layoutManager = LinearLayoutManager(applicationContext)
            adapter.retencionAdapter(applicationContext, listaRetenciones, this)
            binding.rvRetenciones.adapter = adapter
            adapter.notifyDataSetChanged()
        }

    }

    fun setColors() {
        binding.apply {
            tilDoc.setColorModel(Constantes.AGENCIA)
            tilTipoRet.setColorModel(Constantes.AGENCIA)
            textInputLayout2.setColorModel(Constantes.AGENCIA)
            tilCxcMontoMain.setColorModel(Constantes.AGENCIA)
            tilCxcRefMain.setColorModel(Constantes.AGENCIA)

            btAceptar.setBackgroundColor(btAceptar.colorButtonAgencia(Constantes.AGENCIA))

            btAgregarret.setColorModelVariant(Constantes.AGENCIA)

        }
    }

    override fun getTheme(): Resources.Theme {
        val theme = super.getTheme()
        theme.applyStyle(setThemeAgencia(Constantes.AGENCIA), true)
        // you could also use a switch if you have many themes that could apply
        return theme
    }


}





