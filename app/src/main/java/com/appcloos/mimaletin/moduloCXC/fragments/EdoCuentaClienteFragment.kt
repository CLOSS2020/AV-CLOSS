package com.appcloos.mimaletin.moduloCXC.fragments

import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.appcloos.mimaletin.AdminSQLiteOpenHelper
import com.appcloos.mimaletin.Constantes
import com.appcloos.mimaletin.CxcReportActivity
import com.appcloos.mimaletin.Documentos
import com.appcloos.mimaletin.R
import com.appcloos.mimaletin.colorButtonAgencia
import com.appcloos.mimaletin.databinding.FragmentEdoCuentaClienteBinding
import com.appcloos.mimaletin.noRepeatList
import com.appcloos.mimaletin.setDrawableHeadAgencia
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class EdoCuentaClienteFragment : Fragment(), EdoCuentaClienteAdapter.QuantityListener {

    private lateinit var binding: FragmentEdoCuentaClienteBinding

    private lateinit var cliente: String
    private lateinit var nomCliente: String

    lateinit var ke_android: SQLiteDatabase
    private lateinit var conn: AdminSQLiteOpenHelper

    lateinit var enlaceEmpresa: String
    lateinit var nombreEmpresa: String
    lateinit var codigoSucursal: String
    private var fechaAuxiliar: String = "0001-01-01T00:00:00"

    private var listaDocsSeleccionados: ArrayList<String> = ArrayList()

    lateinit var preferences: SharedPreferences
    private var cod_usuario: String? = ""
    private var codEmpresa: String? = ""

    lateinit var listadocs: ArrayList<Documentos>
    private lateinit var docsViejos: ArrayList<String>

    private lateinit var adapter: EdoCuentaClienteAdapterList

    var ll_commit = false

    var flag = false
    var num = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
        cliente = arguments?.getString("cliente")!!
        nomCliente = arguments?.getString("nomCliente")!!

        preferences = this.requireActivity()
            .getSharedPreferences("Preferences", AppCompatActivity.MODE_PRIVATE)
        cod_usuario = preferences.getString("cod_usuario", null)
        codEmpresa = preferences.getString("codigoEmpresa", null)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentEdoCuentaClienteBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        conn = AdminSQLiteOpenHelper(context, "ke_android", null)
        cargarEnlace()
        // cargarDocumentos("https://$enlaceEmpresa/webservice/documentos.php?fecha_sinc=${fechaAuxiliar.trim()}&codigo_cli=${cliente.trim()}&agencia=${codigoSucursal.trim()}")
        buscarDocumentosCliente(cliente)

        setColors()

        binding.tvNombreCliente.text = nomCliente

        binding.btnMain.setOnClickListener { irAPrecobranza(listaDocsSeleccionados) }

        // binding.btnReten.setOnClickListener { irARetencion(listaDocsSeleccionados) }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigate(
                        R.id.action_edoCuentaClienteFragment_to_moduloCXCFragment
                    )
                }
            }
        )
    }

    private fun setColors() {
        binding.apply {
            tvNombreCliente.setDrawableHeadAgencia(Constantes.AGENCIA)
            btnMain.setBackgroundColor(btnMain.colorButtonAgencia(Constantes.AGENCIA))
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemid = item.itemId

        if (itemid == R.id.home) {
            validarSalida()
        }
        // return super.onOptionsItemSelected(item);
        return true
    }

    private fun validarSalida(): AlertDialog {
        val dialog = AlertDialog.Builder(requireContext()).setTitle("Salir")
            .setMessage("¿Está seguro de desear salir?").setCancelable(true)
            .setPositiveButton("Si", null).setNegativeButton("No", null).show()

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener { v: View? ->
            dialog.dismiss()
            findNavController().popBackStack()
        }
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
            .setOnClickListener { v: View? -> dialog.dismiss() }

        return dialog
    }

    private fun irAPrecobranza(listaDocsSeleccionados: ArrayList<String>) {
        val listaAux = listaDocsSeleccionados
        println("pre $listaDocsSeleccionados")
        // 2023-06-08 Variables que cuentan los documentos vencidos (numerico) y los documentos no vencidos (novencidos).
        var numerico = 0
        var novencidos = 0

        // 2023-06-08 For que recorre todos los documentos seleccionados para ser contados como vencidos y no vencidos
        for (i in listaAux.indices) {
            // 2023-06-08 el StringBuilder sirve para eliminar las comillas que traen los documentos seleccionados
            var MyString = StringBuilder(listaAux[i])
            MyString = MyString.deleteCharAt(0).deleteCharAt(MyString.length - 1)

            // 2023-06-08 El if sumara 1 a la variable que cuenta los doc vencidos y no vencidos
            if (docsViejos.indexOf(MyString.toString()) != -1) {
                numerico++
            } else {
                novencidos++
            }
        }

        // 2023-06-08 if que valida que se hayan seleccionado todos los docs vencidos y algunos no vencidos para ser pagados; o un numero menor al total de vencidos sin seleccionar alguno de los no vencidos
        if ((numerico == docsViejos.size) || (numerico <= docsViejos.size && novencidos == 0)) {
            val intent = Intent(requireContext(), CxcReportActivity::class.java)
            intent.putExtra("cod_usuario", cod_usuario)
            intent.putExtra("codigoEmpresa", codEmpresa)
            intent.putStringArrayListExtra("listaDocs", listaDocsSeleccionados)
            // println("Lista del intent ${noRepeatList(listaDocsSeleccionados)}")
            startActivity(intent)
        } else {
            Toast.makeText(requireContext(), "Debe pagar los documentos viejos", Toast.LENGTH_SHORT)
                .show()
        }
    }

    /*private fun irARetencion(listaDocsSeleccionados: ArrayList<String>) {

        val listatiposret: ArrayList<String> = arrayListOf("iva", "flete")
        val listaDocs: ArrayList<String> = arrayListOf()

        for (doc in listaDocsSeleccionados) {
            if (doc.indexOf('E') > 0) {
                Toast.makeText(
                    requireContext(),
                    "Tiene una nota de entrega entre los documentos seleccionados",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
            listaDocs.add(doc.replace("'", ""))
        }

        val cursor = ke_android.rawQuery(
            "SELECT * FROM ke_doccti WHERE documento IN (${
                listaDocsSeleccionados.toString().replace("[", "").replace("]", "")
            })", null
        )

        var bsretencion: Double
        var bsretencioniva: Double
        var bsretencionFlete: Double

        var documento: String

        var cbsret: Double
        var cbsretiva: Double
        var cbsretflete: Double

        while (cursor.moveToNext()) {
            bsretencion = cursor.getDouble(31)
            bsretencioniva = cursor.getDouble(32)
            bsretencionFlete = bsretencion - bsretencioniva

            documento = cursor.getString(2)

            cbsret = cursor.getDouble(37)
            cbsretiva = cursor.getDouble(39)
            cbsretflete = cursor.getDouble(43)

            if ((bsretencioniva > 0.00) && (bsretencionFlete > 0.00)) {
                Toast.makeText(
                    requireContext(), "El documento $documento ya fue pagado", Toast.LENGTH_SHORT
                ).show()
                return
            }

            if (cbsretiva <= 0.0) {
                listatiposret.removeIf { it == "iva" }
            }

            if (cbsretflete <= 0.0) {
                listatiposret.removeIf { it == "flete" }
            }

            if (listatiposret.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Hay documentos que ya pagaron Iva y otros que Pagaron flete",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

        }

        cursor.close()

        val intent = Intent(requireContext(), RetencionesActivity::class.java)
        intent.putExtra("cod_usuario", cod_usuario)
        intent.putExtra("codigoEmpresa", codEmpresa)
        intent.putExtra("cliente", cliente)
        intent.putStringArrayListExtra("listaDocs", listaDocs)
        intent.putStringArrayListExtra("listaDocsQuery", listaDocsSeleccionados)
        intent.putStringArrayListExtra("listatiposret", listatiposret)
        startActivity(intent)
    }*/

    /*private fun cargarDocumentos(URL: String) {//<-------------------------------------te quedaste bajando los documentos para luego hacer el recycle como el de crear cobranza de pablo
        ke_android = conn.readableDatabase

        val jsonArrayRequest = JsonArrayRequest(Request.Method.GET, // method
            URL, // url
            null, // json request
            { response -> // response listener


                if (response != null) {

                    ll_commit = false
                    ke_android.beginTransaction()
                    var jsonObject: JSONObject
                    try {

                        // loop through the array elements
                        for (i in 0 until response.length()) {
                            jsonObject = response.getJSONObject(i)
                            val agencia = jsonObject.getString("agencia").trim()
                            val tipodoc = jsonObject.getString("tipodoc").trim()
                            val documento = jsonObject.getString("documento").trim()
                            val tipodocv = jsonObject.getString("tipodocv").trim()
                            val contribesp = jsonObject.getDouble("contribesp")
                            val ruta_parme = jsonObject.getString("ruta_parme").trim()
                            val tipoprecio = jsonObject.getDouble("tipoprecio")
                            val emision = jsonObject.getString("emision").trim()
                            val recepcion = jsonObject.getString("recepcion").trim()
                            val vence = jsonObject.getString("vence").trim()
                            val diascred = jsonObject.getDouble("diascred")
                            val estatusdoc = jsonObject.getString("estatusdoc").trim()
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
                            val vendedor = jsonObject.getString("vendedor").trim()
                            val codcoord = jsonObject.getString("codcoord").trim()
                            val fechamodifi = jsonObject.getString("fechamodifi").trim()
                            val aceptadev = jsonObject.getString("aceptadev").trim()
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
                            val retmun_mto = jsonObject.getDouble("retmun_mto")

                            val qDocumentosCab = ContentValues()
                            qDocumentosCab.put("agencia", agencia)
                            qDocumentosCab.put("tipodoc", tipodoc)
                            qDocumentosCab.put("documento", documento)
                            qDocumentosCab.put("tipodocv", tipodocv)
                            qDocumentosCab.put("codcliente", cliente)
                            qDocumentosCab.put("nombrecli", nomCliente)
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

                            val qcodigoLocal: Cursor = ke_android.rawQuery(
                                "SELECT count(documento) FROM ke_doccti WHERE documento ='$documento'",
                                null
                            )
                            qcodigoLocal.moveToFirst()
                            //variable para obtener el conteo de documentos que ya esten en el telf
                            val codigoExistente = qcodigoLocal.getInt(0)

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

                        val fechaActualizada: String
                        val qfechaDocs = ContentValues()

                        val fechaModif: Calendar = Calendar.getInstance()
                        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        fechaActualizada = sdf.format(fechaModif.time)
                        qfechaDocs.put("fchhn_ultmod", fechaActualizada)
                        ke_android.update(
                            "tabla_aux", qfechaDocs, "tabla = ?", arrayOf("ke_doccti")
                        )
                        //consulto los documentos ya con la data actualizada
                        buscarDocumentosCliente(cliente)

                    } else if (!ll_commit) {
                        ke_android.endTransaction()
                    }

                }
            }, { error -> // error listener
                //
            })

        val requestQueue: RequestQueue = Volley.newRequestQueue(requireContext())
        requestQueue.add(jsonArrayRequest)
    }*/

    private fun buscarDocumentosCliente(codigoCliente: String?) {
        ke_android = conn.writableDatabase
        listadocs = ArrayList()
        docsViejos = ArrayList()
        val cursorDocs = ke_android.rawQuery(
            "SELECT documento, tipodocv, estatusdoc, dtotalfinal, emision, recepcion, dtotneto, dtotimpuest, dtotdescuen, aceptadev, recepcion, vence, agencia, dFlete, bsflete, dtotpagos, diascred, dtotdev, dretencion, dolarflete, edoentrega FROM ke_doccti " +
                "WHERE codcliente ='$codigoCliente' AND estatusdoc != '2' AND (dtotalfinal - (dtotpagos + dtotdev)) > 0.00 AND empresa = '$codEmpresa'",
            null
        )

        while (cursorDocs.moveToNext()) {
            val documentos = Documentos()
            documentos.documento = cursorDocs.getString(0)
            documentos.tipodocv = cursorDocs.getString(1)
            documentos.estatusdoc = cursorDocs.getString(2)
            documentos.dtotalfinal = cursorDocs.getDouble(3)
            documentos.emision = cursorDocs.getString(4)
            documentos.recepcion = cursorDocs.getString(5)
            documentos.dtotneto = cursorDocs.getDouble(6)
            documentos.dtotimpuest = cursorDocs.getDouble(7)
            documentos.dtotdescuen = cursorDocs.getDouble(8)
            documentos.aceptadev = cursorDocs.getString(9)
            documentos.recepcion = cursorDocs.getString(10)
            documentos.vence = cursorDocs.getString(11)
            documentos.agencia = cursorDocs.getString(12)
            documentos.dFlete = cursorDocs.getDouble(13)
            documentos.bsflete = cursorDocs.getDouble(14)
            documentos.dtotpagos = cursorDocs.getDouble(15)
            documentos.diascred = cursorDocs.getDouble(16)
            documentos.dtotdev = cursorDocs.getDouble(17)
            documentos.dretencion = cursorDocs.getDouble(18)
            documentos.dolarflete = cursorDocs.getInt(19)
            documentos.edoentrega = cursorDocs.getInt(20)
            listadocs.add(documentos)

            // 2023-06-07 IF para guardar un documento viejo en otro array
            if (compararFecha(documentos.vence) < 0) {
                docsViejos.add(documentos.documento)
            }
        }

        // println(docsViejos)

        cursorDocs.close()

        // binding.rvEdoCuenta.layoutManager = LinearLayoutManager(requireContext())
        // 2023-06-08 Cada vez que se seleccion un CheckBox del adapter se repintara para decidir que CheckBox se puede Chekear (solo docs vencidos, o docs vencidos y docs no vencidos)
        // listadocs              = Son todos los documentos del Cliente que esten en la base de datos sin ninguna alteracion
        // docsViejos             = Son todos los documentos de la base de datos que ya estan vencidos
        // listaDocsSeleccionados = Son todos los documentos que fueron chequeados en el adapter
        // numViejo               = Es la cantidad de documentos viejos seleccionados en el adapter
        // numNuevo               = Es la cantidad de documentos no vencidos seleccionados en el adapter
        adapter = EdoCuentaClienteAdapterList(
            documentos = listadocs,
            this,
            docsViejos = docsViejos,
            listaDocsSeleccionados = listaDocsSeleccionados,
            DIAS_VALIDOS_BOLIVARES = conn.getConfigNum("DIAS_VALIDOS_BOLIVARES_DOCS", codEmpresa!!)
                .toInt()
        )
        binding.rvEdoCuenta.adapter = adapter

        /*binding.rvEdoCuenta.setOnItemClickListener { adapterView, view, i, l ->

        }*/
    }

    private fun cargarEnlace() {
        ke_android = conn.writableDatabase
        val columnas = arrayOf("kee_nombre," + "kee_url," + "kee_sucursal")
        val cursorE: Cursor = ke_android.query(
            "ke_enlace",
            columnas,
            "kee_codigo = '$codEmpresa'",
            null,
            null,
            null,
            null
        )

        while (cursorE.moveToNext()) {
            nombreEmpresa = cursorE.getString(0)
            enlaceEmpresa = cursorE.getString(1)
            codigoSucursal = cursorE.getString(2)
        }
        cursorE.close()
        ke_android.close()
    }

    override fun onQuantityChange(listaChange: ArrayList<String>, numViejo: Int, numNuevo: Int) {
        listaDocsSeleccionados = listaChange.noRepeatList()

        // binding.btnMain.isVisible = listaDocsSeleccionados.size > 0

        binding.btnMain.visibility = if (listaDocsSeleccionados.size > 0) {
            View.VISIBLE
        } else {
            View.GONE
        }

        // binding.btnReten.isVisible = listaChange.size > 0

        // 2023-06-08 Cada vez que se seleccion un CheckBox del adapter se repintara para decidir que CheckBox se puede Chekear (solo docs vencidos, o docs vencidos y docs no vencidos)
        // listadocs              = Son todos los documentos del Cliente que esten en la base de datos sin ninguna alteracion
        // docsViejos             = Son todos los documentos de la base de datos que ya estan vencidos
        // listaDocsSeleccionados = Son todos los documentos que fueron chequeados en el adapter
        // numViejo               = Es la cantidad de documentos viejos seleccionados en el adapter
        // numNuevo               = Es la cantidad de documentos no vencidos seleccionados en el adapter
        adapter = EdoCuentaClienteAdapterList(
            documentos = listadocs,
            this,
            docsViejos = docsViejos,
            listaDocsSeleccionados = listaDocsSeleccionados,
            numViejo = numViejo,
            numNuevo = numNuevo,
            DIAS_VALIDOS_BOLIVARES = conn.getConfigNum("DIAS_VALIDOS_BOLIVARES_DOCS", codEmpresa!!)
                .toInt()
        )
        // adapter.listaDocsSeleccionados = listaDocsSeleccionados
        binding.rvEdoCuenta.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_empty, menu)

        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun compararFecha(fechaVencimiento: String): Int {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val current = LocalDateTime.now().format(formatter)

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val firstDate: Date = sdf.parse(fechaVencimiento)
        val secondDate: Date = sdf.parse(current)

        // vence > fecha = 1
        // vence = fecha = 0
        // vence < fecha = -1

        return firstDate.compareTo(secondDate)
    }

    private fun noRepeatList(list: ArrayList<String>): ArrayList<String> {
        val returnList = ArrayList<String>()
        val newList = list.distinct().toList()
        newList.forEach {
            returnList.add(it)
        }
        return returnList
    }
}
