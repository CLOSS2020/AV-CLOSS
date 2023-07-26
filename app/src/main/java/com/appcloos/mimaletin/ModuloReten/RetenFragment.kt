package com.appcloos.mimaletin.ModuloReten

import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.text.InputFilter
import android.text.SpannableStringBuilder
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.GridView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appcloos.mimaletin.AdminSQLiteOpenHelper
import com.appcloos.mimaletin.CreacionPedidoActivity
import com.appcloos.mimaletin.DatePickerFragment
import com.appcloos.mimaletin.DialogRetencion
import com.appcloos.mimaletin.GridViewAdapter
import com.appcloos.mimaletin.R
import com.appcloos.mimaletin.Retenciones
import com.appcloos.mimaletin.classes.DecimalDigitsInputFilter
import com.appcloos.mimaletin.databinding.FragmentRetenBinding
import com.appcloos.mimaletin.databinding.FragmentSelectClienteRetenBinding
import com.appcloos.mimaletin.retencion.DetalleRetencion
import com.appcloos.mimaletin.retencion.DetalleRetencionAdapter
import com.appcloos.mimaletin.retencionesAdapter
import com.appcloos.mimaletin.viewmodel.CXC.ke_precobradocs
import com.appcloos.mimaletin.viewmodel.CXC.ke_precobranza
import com.google.android.material.textfield.TextInputLayout
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt


class RetenFragment : Fragment(), retencionesAdapter.RetHolder.QuantityListener {

    private lateinit var binding: FragmentRetenBinding

    private lateinit var preferences: SharedPreferences
    private lateinit var cod_usuario: String

    lateinit var ke_android: SQLiteDatabase
    private lateinit var conn: AdminSQLiteOpenHelper

    //lateinit var bt_aceptar: Button
    //lateinit var bt_agregar: Button
    //lateinit var btnDetalle: Button
    //lateinit var btnAgregarFoto: Button


    //lateinit var et_fecharetenciones: EditText
    //lateinit var et_refret: EditText
    //lateinit var et_montoret: EditText

    //lateinit var sp_tiposret: AutoCompleteTextView

    //lateinit var til_cxc_monto_main: TextInputLayout
    //lateinit var sp_documentos: Spinner

    lateinit var listaRetenciones: ArrayList<Retenciones>
    lateinit var listaTiposRet: ArrayList<String>
    lateinit var listaDocs: ArrayList<String>
    lateinit var listaDocsQuery: ArrayList<String>

    //lateinit var rv_retenciones: RecyclerView

    lateinit var adapter2: DetalleRetencionAdapter

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
    var nroCorrelativo = 0

    lateinit var codCliente : String

    var valor = 0.0

    private lateinit var imageUri: Uri

    var listaImagenes: ArrayList<Uri> = ArrayList()

    private lateinit var baseAdapter: GridViewAdapter

    private lateinit var gvFotos: GridView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentRetenBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preferences = this.requireActivity()
            .getSharedPreferences("Preferences", AppCompatActivity.MODE_PRIVATE)
        cod_usuario = preferences.getString("cod_usuario", null).toString()

        conn = AdminSQLiteOpenHelper(requireContext(), "ke_android", null, 24)

        ke_android = conn.writableDatabase

        listaRetenciones = ArrayList()
        listaTiposRet = ArrayList()
        listaDocs = ArrayList() // lista para indicar el doc al que se le aplica la ret

        preferences = requireActivity().getSharedPreferences("Preferences", AppCompatActivity.MODE_PRIVATE)
        cod_usuario   = preferences.getString("cod_usuario", null).toString()
        //codEmpresa    = preferences.getString("codigoEmpresa", null)


        //listaTiposRet.add(0, "Seleccione un tipo de retención")
        //listaTiposRet = intent.extras?.getStringArrayList("listatiposret") as ArrayList<String>
        listaTiposRet = requireArguments().getSerializable("listatiposret") as ArrayList<String>
        //la lista de los docs y a cual les voy a cargar retencion
        //listaDocs       = intent.extras?.getStringArrayList("listaDocs") as ArrayList<String>
        listaDocs       = requireArguments().getSerializable("listaDocs") as ArrayList<String>
        //listaDocsQuery  = intent.extras?.getStringArrayList("listaDocsQuery") as ArrayList<String>
        listaDocsQuery  = requireArguments().getSerializable("listaDocsQuery") as ArrayList<String>
        //codCliente      = intent.extras?.getString("cliente").toString()
        codCliente      = requireArguments().getString("cliente").toString()
        //println(listaDocs)
        activarRetenciones()
        //verficarRetYaGuardadas()

        //listener del boton
        binding.btAceptar.setOnClickListener {
            if (listaRetenciones.size > 0) {
                guardarRetenciones()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Debes agregar al menos una retención",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }

        binding.etFecharetenciones.setOnClickListener { showDatePickerDialog() }

        binding.btAgregarret.setOnClickListener {
            addRetencion()
        }

        //seleccion del tipo de retencion
        /*sp_tiposret.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
            AdapterView.OnItemClickListener { parent, view, position, id ->
                tipoRetSeleccionada = listaTiposRet[position]
                cambioRetencion()
            }

        /*sp_documentos.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
                //println("ELNUEMRO $nroDoc")
                cambioRetencion()
            }
        }*/


        binding.btAgregarfoto.setOnClickListener {
            val select = Intent()
            select.type = "image/jpeg"
            select.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            select.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(
                Intent.createChooser(select, "SELECCIONA LAS IMAGENES"),
                1
            )
        }

        binding.btnDetalle.setOnClickListener {
            detalleRetencion()
        }

        binding.etMontoret.filters = arrayOf<InputFilter>(DecimalDigitsInputFilter(100, 2))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            val clipData = data!!.clipData
            if (resultCode == AppCompatActivity.RESULT_OK && requestCode == 1) {
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
            Toast.makeText(requireContext(), "Algo salió mal", Toast.LENGTH_LONG).show()
        }
        //super.onActivityResult(requestCode, resultCode, data);
        baseAdapter = GridViewAdapter(requireContext(), listaImagenes)
        binding.gvFotos.adapter = baseAdapter
    }

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

            /*if (nroDoc.isEmpty()) {
                nroDoc = listaDocs[0]
            }*/

            val cursor: Cursor = ke_android.rawQuery(
                "SELECT SUM($valorRetencion) FROM ke_doccti WHERE documento IN (${
                    listaDocsQuery.toString().replace("[", "").replace("]", "")
                })",
                null
            )

            cursor.moveToFirst()

            valor = (cursor.getDouble(0) * 100.00).roundToInt() / 100.00

            cursor.close()

            binding.tilCxcMontoMain.hint = "El monto requerido $valor Bs."
        } catch (e: Exception) {
            binding.tilCxcMontoMain.hint = "Monto (En Bss)"
        }


    }

    private fun addRetencion() {

        //valido que todo esté bien
        if (fechaRet.isEmpty() ||
            binding.etRefret.text.toString().isEmpty() ||
            fechaParaCorrelativo.isEmpty() ||
            binding.etMontoret.text.toString().isEmpty() ||
            binding.spTiposret.text.toString().isEmpty()
        ) {

            Toast.makeText(
                requireContext(),
                "Debe llenar todos los datos necesarios",
                Toast.LENGTH_SHORT
            ).show()

        } else {


            montoRet = binding.etMontoret.text.toString().toDouble()
            refRet = binding.etRefret.text.toString()

            for (retencion in listaRetenciones) {
                if (retencion.tiporet == tipoRetSeleccionada) {
                    Toast.makeText(
                        requireContext(),
                        "Ya existe una retencion de ${retencion.tiporet}",
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }
            }

            for (retencion in listaRetenciones) {
                if (retencion.refret == refRet) {
                    Toast.makeText(
                        requireContext(),
                        "Ya existe una retencion con esa referencia",
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }
            }


            if (montoRet!! >= valor - 1 && montoRet!! <= valor + 1) {
                if (refRet.length == 14) {
                    //si todo bien, debo añadir cada ret a la lista
                    correlativoRetencion = "$fechaParaCorrelativo$refRet"
                    val retenciones: Retenciones = Retenciones()

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
                    Toast.makeText(requireContext(), "Retención agregada", Toast.LENGTH_SHORT)
                        .show()

                    binding.rvRetenciones.layoutManager = LinearLayoutManager(requireContext())
                    adapter.retencionAdapter(requireContext(), listaRetenciones, this)
                    binding.rvRetenciones.adapter = adapter
                    adapter.notifyDataSetChanged()

                    binding.etFecharetenciones.text = SpannableStringBuilder("")
                    binding.spTiposret.text = SpannableStringBuilder("")
                    binding.etMontoret.text = SpannableStringBuilder("")
                    binding.etRefret.text = SpannableStringBuilder("")

                    binding.tilCxcMontoMain.hint = "Monto (En Bss)"


                } else {
                    Toast.makeText(
                        requireContext(),
                        "La referencia de la retencion debe de tener 14 caracteres",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "Monto de la retencion invalida",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }

    }

    private fun showDatePickerDialog() {
        val datePicker = DatePickerFragment(
            "retencionesActivity",
            nroDoc(),
            requireContext()
        ) { day, month, year -> onDateSelected(day, month, year) }
        datePicker.show(requireActivity().supportFragmentManager, "datePicker")

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

        binding.etFecharetenciones.setText("Fecha: ${formatNuevoVista.format(date)}")

        var formatoParaCorrelativo: SimpleDateFormat = SimpleDateFormat("yyyyMM")
        fechaParaCorrelativo = formatoParaCorrelativo.format(date)

    }


    private fun activarRetenciones() {
        if (listaTiposRet.size > 0) {
            val adapter: ArrayAdapter<CharSequence> = ArrayAdapter(
                requireContext(),
                R.layout.spinner_bancos,
                listaTiposRet as List<CharSequence>
            )
            binding.spTiposret.setAdapter(adapter)
            binding.spTiposret.isFocusableInTouchMode = false
            binding.spTiposret.isCursorVisible = false
            binding.spTiposret.listSelection = 1
            adapter.notifyDataSetChanged()
        }

        /*if (listaDocs.size > 0) {
            var adapter: ArrayAdapter<CharSequence>
            adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                listaDocs as List<CharSequence>
            )
            sp_documentos.adapter = adapter
            adapter.notifyDataSetChanged()
        }*/

    }


    private fun guardarRetenciones() {

        //var listaRetCadena:Set<String> = listaRetenciones.groupBy { it.tiporet }.keys
        for (i in listaRetenciones) {
            println("nro doc: ${i.nrodoc}, tipo ${i.tiporet}, nroret ${i.nroret}, refret ${i.refret}  fecha ${i.fecharet}  monto ${i.montoret}")

        }

        if (listaImagenes.size == 0){
            Toast.makeText(requireContext(), "Ingrese una imagen de la retención", Toast.LENGTH_SHORT).show()
            return
        }

        val kePrecobranza = ke_precobranza()
        val kePrecobradocs = arrayListOf<ke_precobradocs>()

        val numCXC = generarCorrelativo()

        kePrecobranza.cxcndoc     = numCXC
        kePrecobranza.tiporecibo  = "R"
        kePrecobranza.codvend     = cod_usuario
        kePrecobranza.fechamodifi = fecha("full")
        kePrecobranza.fchrecibo   = fecha("noFull") //<------------------Preguntar que fecha va
        kePrecobranza.clicontesp  = contriEspecial(codCliente)
        kePrecobranza.moneda      = "1"
        kePrecobranza.bsretflete  = grabarRet("flete") //<----------------- preguntar si aqui se coloca la totalidad del pago del cliente, o la suma de los fletes traida directamenten de ke_doccti
        kePrecobranza.bsretiva    = grabarRet("iva") //<----------------- preguntar si aqui se coloca la totalidad del pago del cliente, o la suma de los fletes traida directamenten de ke_doccti
        kePrecobranza.fchvigen    = fechaSuma(cantDias = 999)

        for (documento in listaDocs){
            val kePrecobradoc = ke_precobradocs()
            kePrecobradoc.cxcndoc    = numCXC
            kePrecobradoc.agencia    = "001" //<------------------OJO
            kePrecobradoc.tipodoc    = "FAC" //<------------------OJO
            kePrecobradoc.documento  = documento

            for (retencion in listaRetenciones){
                if (retencion.tiporet == "iva"){
                    kePrecobradoc.nroret    = retencion.nroret
                    kePrecobradoc.fchemiret = retencion.fecharet
                    kePrecobradoc.bsretiva  = pagarRet(documento, "cbsretiva")
                    kePrecobradoc.bsmtoiva  = pagarRet(documento, "bsiva")
                    kePrecobradoc.refret    = retencion.refret
                } else if (retencion.tiporet == "flete") {
                    kePrecobradoc.nroretfte  = retencion.nroret
                    kePrecobradoc.fchemirfte = retencion.fecharet
                    kePrecobradoc.bsretfte   = pagarRet(documento, "cbsretflete")
                    kePrecobradoc.bsmtofte  = pagarRet(documento, "bsflete")
                    kePrecobradoc.refretfte  = retencion.refret
                }
            }

            kePrecobradocs.add(kePrecobradoc)

        }

        println(kePrecobranza)
        println(kePrecobradocs)

        ke_android.beginTransaction()

        try {

            val cabecera = ContentValues()
            cabecera.put("cxcndoc",kePrecobranza.cxcndoc)
            cabecera.put("tiporecibo",kePrecobranza.tiporecibo)
            cabecera.put("codvend",kePrecobranza.codvend)
            cabecera.put("fechamodifi",kePrecobranza.fechamodifi)
            cabecera.put("fchrecibo",kePrecobranza.fchrecibo)
            cabecera.put("clicontesp",kePrecobranza.clicontesp)
            cabecera.put("moneda",kePrecobranza.moneda)
            cabecera.put("bsretflete",kePrecobranza.bsretflete)
            cabecera.put("bsretiva",kePrecobranza.bsretiva)
            cabecera.put("fchvigen",kePrecobranza.fchvigen)

            for (kePrecobradoc in kePrecobradocs){
                val lineas = ContentValues()

                lineas.put("cxcndoc",kePrecobradoc.cxcndoc)
                lineas.put("agencia",kePrecobradoc.agencia)
                lineas.put("tipodoc",kePrecobradoc.tipodoc)
                lineas.put("documento",kePrecobradoc.documento)
                lineas.put("nroret",kePrecobradoc.nroret)
                lineas.put("fchemiret",kePrecobradoc.fchemiret)
                lineas.put("bsretiva",kePrecobradoc.bsretiva)
                lineas.put("refret",kePrecobradoc.refret)
                lineas.put("nroretfte",kePrecobradoc.nroretfte)
                lineas.put("fchemirfte",kePrecobradoc.fchemirfte)
                lineas.put("bsretfte",kePrecobradoc.bsretfte)
                lineas.put("refretfte",kePrecobradoc.refretfte)
                lineas.put("bsmtofte",kePrecobradoc.bsmtofte)
                lineas.put("bsmtoiva",kePrecobradoc.bsmtoiva)

                ke_android.insert("ke_precobradocs", null, lineas)
            }

            ke_android.execSQL("UPDATE ke_corprec SET kcor_numero = '$nroCorrelativo' WHERE kcor_vendedor = '$cod_usuario';")

            ke_android.insert("ke_precobranza", null, cabecera)


            //insertamos las imagenes en la tabla de imagenes


            val imagenesreclamo = ContentValues()
            for (i in listaImagenes.indices) {

                val img = requireActivity().contentResolver.openInputStream(listaImagenes[i])
                var bitmap = BitmapFactory.decodeStream(img)
                bitmap = redimensionarImagen(bitmap, 1000f, 1000f)
                val cadena: String = convertirUriToBase64(bitmap)!!

                imagenesreclamo.put("cxcndoc", numCXC)
                imagenesreclamo.put("ruta", cadena)
                imagenesreclamo.put("ret_nomimg", numCXC + "_" + i)

                bitmap.recycle()
                ke_android.insert("ke_retimg", null, imagenesreclamo)

            }

            ke_android.setTransactionSuccessful()
        }catch (e: Exception){
            e.printStackTrace()
            Toast.makeText(requireContext(), "No se puedo crear la retención", Toast.LENGTH_SHORT).show()
            return
        }finally {
            ke_android.endTransaction()
        }



        val dialog: DialogRetencion = DialogRetencion()
        dialog.DialogRetencion(requireContext(), kePrecobranza, kePrecobradocs)

        //2023-05-24 Se comento ya que de aqui se guardara como un nuevo recibo de cobro de tipo R en lugar de estar dentro de un tipo W
        /*var bundle: Bundle = Bundle()
        bundle.putSerializable("listaRetenciones", listaRetenciones)
        intent.putExtras(bundle)
        setResult(RESULT_OK, intent)
        finish()*/

    }


    private fun pagarRet(documento: String, retencion: String): Double {
        var montoRet = 0.0
        val cursor = ke_android.rawQuery("SELECT $retencion FROM ke_doccti WHERE documento = '$documento'",null)
        if (cursor.moveToNext()){
            montoRet = cursor.getDouble(0)
        }
        cursor.close()

        return montoRet
    }

    private fun grabarRet(tipoRet: String): Double {
        var monto = 0.0
        for (retencion in listaRetenciones){
            if (retencion.tiporet == tipoRet){
                monto = retencion.montoret
            }
        }
        return monto
    }

    private fun fecha(validador: String): String {
        return if (validador == "full"){
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            sdf.format(Date())
        } else {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            sdf.format(Date())
        }

    }

    private fun contriEspecial(codCliente: String): String {
        var contriEspecial = ""
        val cursor = ke_android.rawQuery("SELECT contribespecial FROM cliempre WHERE codigo = '$codCliente'", null)

        if (cursor.moveToNext()){
            contriEspecial = cursor.getString(0)
        }

        cursor.close()

        return contriEspecial
    }

    private fun evaluarRetaPagar(): String {
        var valorRetorno = "0"

        when (variableBandera) {
            "iva" -> valorRetorno = "0"
            "flete" -> valorRetorno = "1"
            "todas" -> valorRetorno = "2"
            "todasyparme" -> valorRetorno = "3"
        }
        //println(valorRetorno)
        return valorRetorno
    }

    override fun onQuantityChange(listaChange: ArrayList<Retenciones>) {
        //println(listaChange)
        evaluarLista(listaChange)

    }

    private fun evaluarLista(listaChange: ArrayList<Retenciones>) {
        if (listaChange.isEmpty()) {

            listaRetenciones = listaChange

        } else {

            listaRetenciones = listaChange
        }
        binding.rvRetenciones.layoutManager = LinearLayoutManager(requireContext())
        adapter.retencionAdapter(requireContext(), listaRetenciones, this)
        binding.rvRetenciones.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    /*private fun verficarRetYaGuardadas() {

        var bundle: Bundle = Bundle()
        bundle = intent.extras!!
        listaRetenciones = bundle.getSerializable("listaRetenciones") as ArrayList<Retenciones>
        if (listaRetenciones.size > 0) {

            rv_retenciones.layoutManager = LinearLayoutManager(requireContext())
            adapter.retencionAdapter(requireContext(), listaRetenciones, this)
            rv_retenciones.adapter = adapter
            adapter.notifyDataSetChanged()
        }

    }*/


    fun nroDoc(): String {
        val cursor = ke_android.rawQuery(
            "SELECT emision FROM ke_doccti WHERE documento IN (${
                listaDocsQuery.toString().replace("[", "").replace("]", "")
            }) ORDER BY emision DESC", null
        )
        //println("SELECT emision FROM ke_doccti WHERE documento IN (${listaDocsQuery.toString().replace("[", "").replace("]", "")}) ORDER BY emision ASC")
        if (cursor.moveToFirst()) {
            println("Fecha --> ${cursor.getString(0)}")
            return cursor.getString(0)
        } else {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            println("Fecha --> ${LocalDateTime.now().format(formatter)}")
            return LocalDateTime.now().format(formatter)
        }
        cursor.close()

    }

    fun detalleRetencion() {
        val listReten: ArrayList<DetalleRetencion> = arrayListOf()

        val cursor = ke_android.rawQuery(
            "SELECT documento, cbsretiva, cbsretflete FROM ke_doccti WHERE documento IN (${
                listaDocsQuery.toString().replace("[", "").replace("]", "")
            }) ORDER BY emision DESC", null
        )
        println(
            "SELECT documento, cbsretiva, cbsretflete FROM ke_doccti WHERE documento IN (${
                listaDocsQuery.toString().replace("[", "").replace("]", "")
            }) ORDER BY emision DESC"
        )
        while (cursor.moveToNext()) {
            val detalleRetencion =
                DetalleRetencion(cursor.getString(0), cursor.getString(1), cursor.getString(2))
            listReten.add(detalleRetencion)
        }
        cursor.close()

        val dialogo = Dialog(requireContext())

        dialogo.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogo.setCancelable(false)
        dialogo.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        dialogo.setContentView(R.layout.dialog_retencion)

        val rv_detalle = dialogo.findViewById<RecyclerView>(R.id.rvDetalleRetencion)
        val aceptar = dialogo.findViewById<Button>(R.id.btAetalleAcep)


        println("Mi lista ---> $listReten")
        rv_detalle.layoutManager = LinearLayoutManager(requireContext())
        adapter2 = DetalleRetencionAdapter(listReten)
        rv_detalle.adapter = adapter2
        adapter2.notifyDataSetChanged()


        //cerrar el dialogo
        aceptar.setOnClickListener(View.OnClickListener {
            dialogo.dismiss()

        })
        //mostrar el dialogo
        dialogo.show()

    }

    fun generarCorrelativo() : String{
        var correlativo = ""
        val cursorCorrelativo = ke_android.rawQuery("SELECT MAX(kcor_numero) FROM ke_corprec WHERE kcor_vendedor ='$cod_usuario'", null)

        if(cursorCorrelativo.moveToFirst()){
            nroCorrelativo    = cursorCorrelativo.getInt(0) + 1
            var CorrelativoTexto  = nroCorrelativo.toString()
            CorrelativoTexto  = "0000$CorrelativoTexto"

            val fechaHoy = Date(Calendar.getInstance().timeInMillis)

            val formatoFecha = SimpleDateFormat("yyMM", Locale.getDefault())

            val fecha = formatoFecha.format(fechaHoy)

            correlativo = CreacionPedidoActivity.right(CorrelativoTexto, 4)
            correlativo = "$cod_usuario-PRC-$fecha$correlativo"


        }
        cursorCorrelativo.close()

        return correlativo
    }

    private fun getDropboxIMGSize(uri: Uri, dimension: String): Int {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeStream(
            requireActivity().contentResolver.openInputStream(uri),
            null,
            options
        )
        return if (dimension == "alto"){
            options.outHeight
        } else {
            options.outWidth
        }


    }

    private fun convertirUriToBase64(bitmap: Bitmap): String? {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val bytes = baos.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    private fun redimensionarImagen(bitmap: Bitmap, anchoNuevo: Float, altoNuevo: Float): Bitmap? {
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
                //si los anchos y altos son iguales
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
            }//UPDATE usuarios SET ult_sinc = '2023-05-30 08:37:25', version = '1.1.1' WHERE vendedor = 'G98';
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return bitmap
    }

    private fun fechaSuma(fechaOld:String = getFechaHoy(), cantDias:Long):String{
        val fechaNueva :String
        //2023-04-03 Comentado por usar muchas variables, ahora se usan los parametros obtenidos de la funcion
        //val diasAdicional = cantDias

        // de string a fecha
        //2023-04-03 Comentado por usar muchas variables, ahora se usan los parametros obtenidos de la funcion
        //var fechaActual:String = fechaOld
        val fechaNow = LocalDate.parse(fechaOld, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        val fechaNew = fechaNow.plusDays(cantDias)

        // de fecha a String (la nueva)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        fechaNueva = fechaNew.format(formatter)

        return fechaNueva
    }

    private fun getFechaHoy():String{
        val fechaHoy:String
        val fechaSinConvertir: Calendar = Calendar.getInstance()
        val sdf: SimpleDateFormat       = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        fechaHoy                        = sdf.format(fechaSinConvertir.time)
        return fechaHoy
    }

}