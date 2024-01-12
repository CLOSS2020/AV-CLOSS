package com.appcloos.mimaletin.ModuloReten

import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.appcloos.mimaletin.AdminSQLiteOpenHelper
import com.appcloos.mimaletin.Documentos
import com.appcloos.mimaletin.R
import com.appcloos.mimaletin.databinding.FragmentEdoCuentaClienteRetenBinding
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale


class EdoCuentaClienteRetenFragment : Fragment(), EdoCuentaClienteRetenAdapter.QuantityListener {

    private lateinit var binding: FragmentEdoCuentaClienteRetenBinding

    private lateinit var cliente: String
    private lateinit var nomCliente: String

    lateinit var ke_android: SQLiteDatabase
    private lateinit var conn: AdminSQLiteOpenHelper

    lateinit var enlaceEmpresa: String
    lateinit var nombreEmpresa: String
    lateinit var codigoSucursal: String
    private var fechaAuxiliar: String = "0001-01-01T00:00:00"

    var listaDocsSeleccionados: ArrayList<String> = ArrayList()

    lateinit var preferences: SharedPreferences
    private var cod_usuario: String? = ""
    private var codEmpresa: String? = ""

    lateinit var listadocs: ArrayList<Documentos>
    lateinit var docsViejos: ArrayList<String>

    private lateinit var adapter: EdoCuentaClienteRetenAdapter

    var ll_commit = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cliente = arguments?.getString("cliente")!!
        nomCliente = arguments?.getString("nomCliente")!!

        preferences = this.requireActivity()
            .getSharedPreferences("Preferences", AppCompatActivity.MODE_PRIVATE)
        cod_usuario = preferences.getString("cod_usuario", null)
        codEmpresa = preferences.getString("codigoEmpresa", null)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentEdoCuentaClienteRetenBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        conn = AdminSQLiteOpenHelper(context, "ke_android", null)

        buscarDocumentosCliente(cliente)

        binding.tvNombreCliente.text = nomCliente

        binding.btnMain.setOnClickListener { irAReten(listaDocsSeleccionados) }
    }

    private fun buscarDocumentosCliente(codigoCliente: String?) {
        ke_android = conn.writableDatabase
        listadocs = ArrayList()
        docsViejos = ArrayList()
        val cursorDocs = ke_android.rawQuery(
            "SELECT documento, tipodocv, estatusdoc, dtotalfinal, emision, recepcion, dtotneto, dtotimpuest, dtotdescuen, aceptadev, recepcion, vence, agencia, dFlete, bsflete, dtotpagos, diascred FROM ke_doccti WHERE codcliente ='$codigoCliente' AND estatusdoc != '2' AND (dtotalfinal - dtotpagos) > 0.00 AND agencia != '002'",
            null
        )

        /*val cursorDocs = ke_android.rawQuery(
            "SELECT ke_doccti.documento, ke_doccti.tipodocv, ke_doccti.estatusdoc, ke_doccti.dtotalfinal, ke_doccti.emision, ke_doccti.recepcion, ke_doccti.dtotneto, ke_doccti.dtotimpuest, ke_doccti.dtotdescuen, ke_doccti.aceptadev, ke_doccti.recepcion, ke_doccti.vence, ke_doccti.agencia, ke_doccti.dFlete, ke_doccti.bsflete, ke_doccti.dtotpagos, ke_doccti.diascred FROM ke_doccti INNER JOIN ke_precobradocs ON ke_doccti.documento = ke_precobradocs.documento WHERE ke_doccti.codcliente ='$codigoCliente' AND ke_doccti.estatusdoc != '2' AND (ke_doccti.dtotalfinal - ke_doccti.dtotpagos) > 0.00 AND ke_doccti.agencia != '002' AND ke_precobradocs.reten != '1'",
            null
        )*/

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
            listadocs.add(documentos)

            //2023-06-07 IF para guardar un documento viejo en otro array
            if (compararFecha(documentos.vence) < 0) {
                docsViejos.add(documentos.documento)
            }
        }

        //println(docsViejos)

        cursorDocs.close()

        binding.rvEdoCuenta.layoutManager = LinearLayoutManager(requireContext())
        //2023-06-08 Cada vez que se seleccion un CheckBox del adapter se repintara para decidir que CheckBox se puede Chekear (solo docs vencidos, o docs vencidos y docs no vencidos)
        //listadocs              = Son todos los documentos del Cliente que esten en la base de datos sin ninguna alteracion
        //docsViejos             = Son todos los documentos de la base de datos que ya estan vencidos
        //listaDocsSeleccionados = Son todos los documentos que fueron chequeados en el adapter
        //numViejo               = Es la cantidad de documentos viejos seleccionados en el adapter
        //numNuevo               = Es la cantidad de documentos no vencidos seleccionados en el adapter
        adapter = EdoCuentaClienteRetenAdapter(
            documentos = listadocs,
            this,
            docsViejos = docsViejos,
            listaDocsSeleccionados = listaDocsSeleccionados,
            DIAS_VALIDOS_BOLIVARES = conn.getConfigNum("DIAS_VALIDOS_BOLIVARES_DOCS", codEmpresa!!).toInt()
        )
        binding.rvEdoCuenta.adapter = adapter

    }

    fun compararFecha(fechaVencimiento: String): Int {

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val current = LocalDateTime.now().format(formatter)

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val firstDate: Date = sdf.parse(fechaVencimiento) as Date
        val secondDate: Date = sdf.parse(current) as Date


        //vence > fecha = 1
        //vence = fecha = 0
        //vence < fecha = -1

        return firstDate.compareTo(secondDate)

    }

    private fun irAReten(listaDocsSeleccionados: ArrayList<String>) {

        val listatiposret:  ArrayList<String> = arrayListOf("iva", "flete")
        val listaDocs:  ArrayList<String> = arrayListOf()

        for (doc in listaDocsSeleccionados){
            if (doc.indexOf('E') > 0){
                Toast.makeText(requireContext(), "Tiene una nota de entrega entre los documentos seleccionados", Toast.LENGTH_SHORT).show()
                return
            }
            listaDocs.add(doc.replace("'",""))
        }

        val cursor = ke_android.rawQuery("SELECT * FROM ke_doccti WHERE documento IN (${listaDocsSeleccionados.toString().replace("[", "").replace("]", "")})", null)

        var bsretencion: Double
        var bsretencioniva: Double
        var bsretencionFlete: Double

        var documento: String

        var cbsret: Double
        var cbsretiva: Double
        var cbsretflete: Double

        while(cursor.moveToNext()){
            bsretencion = cursor.getDouble(31)
            bsretencioniva = cursor.getDouble(32)
            bsretencionFlete = bsretencion - bsretencioniva

            documento = cursor.getString(2)

            cbsret = cursor.getDouble(37)
            cbsretiva = cursor.getDouble(39)
            cbsretflete = cursor.getDouble(43)

            if ((bsretencioniva > 0.00) && (bsretencionFlete > 0.00)){
                Toast.makeText(requireContext(), "El documento $documento ya fue pagado", Toast.LENGTH_SHORT).show()
                return
            }

            if (cbsretiva <= 0.0){
                listatiposret.removeIf { it == "iva" }
            }

            if (cbsretflete <= 0.0){
                listatiposret.removeIf { it == "flete" }
            }

            if (listatiposret.isEmpty()){
                Toast.makeText(requireContext(), "Hay documentos que ya pagaron Iva y otros que Pagaron flete", Toast.LENGTH_SHORT).show()
                return
            }

        }

        cursor.close()

        val listaAux = listaDocsSeleccionados

        //2023-06-08 Variables que cuentan los documentos vencidos (numerico) y los documentos no vencidos (novencidos).
        var numerico = 0
        var novencidos = 0


        //2023-06-08 For que recorre todos los documentos seleccionados para ser contados como vencidos y no vencidos
        for (i in listaAux.indices) {

            //2023-06-08 el StringBuilder sirve para eliminar las comillas que traen los documentos seleccionados
            var MyString = StringBuilder(listaAux[i])
            MyString = MyString.deleteCharAt(0).deleteCharAt(MyString.length - 1)

            //2023-06-08 El if sumara 1 a la variable que cuenta los doc vencidos y no vencidos
            if (docsViejos.indexOf(MyString.toString()) != -1) {
                numerico++
            } else {
                novencidos++
            }

        }

        //2023-06-08 if que valida que se hayan seleccionado todos los docs vencidos y algunos no vencidos para ser pagados; o un numero menor al total de vencidos sin seleccionar alguno de los no vencidos
        if ((numerico == docsViejos.size) || (numerico <= docsViejos.size && novencidos == 0)) {
            val args = Bundle()
            args.putString("cod_usuario", cod_usuario)
            args.putString("codigoEmpresa", codEmpresa)
            args.putString("cliente",cliente)
            args.putSerializable("listaDocs", listaDocs)
            args.putSerializable("listaDocsQuery", listaDocsSeleccionados)
            args.putSerializable("listatiposret", listatiposret)

            findNavController().navigate(R.id.action_edoCuentaClienteRetenFragment_to_retenFragment, args)
        } else {
            Toast.makeText(requireContext(), "Debe pagar los documentos viejos", Toast.LENGTH_SHORT)
                .show()
        }


    }

    override fun onQuantityChange(
        listaChange: ArrayList<String>,
        numViejo: Int,
        numNuevo: Int,
    ) {
        binding.btnMain.isVisible = listaChange.size > 0
        //binding.btnReten.isVisible = listaChange.size > 0
        listaDocsSeleccionados = listaChange

    }

}