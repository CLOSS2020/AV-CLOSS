package com.appcloos.mimaletin

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.content.res.Resources
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.view.ContextThemeWrapper
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appcloos.mimaletin.databinding.ActivityCxcactivityBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class CXCActivity : AppCompatActivity() {
    // 2023-06-19 Variable global para la activity Numerica que dice los dias que son vigentes antes
    // de que el recibo de cobro en efectivo se anule por antiguedad (Valor default 4 dias)
    // private var APP_W_VIGENCIA_EFECTIVO : Double = 4.0

    private val requestCodeResponse = 200

    private lateinit var rvCxc: RecyclerView // variable para el RecyclerV.
    private lateinit var preferences: SharedPreferences // preferences para cargar los datos de la princ.
    private var codUsuario: String? = ""
    private var codEmpresa: String? = ""
    private lateinit var enlaceEmpresa: String
    private lateinit var cursorCobranza: Cursor
    private lateinit var conn: AdminSQLiteOpenHelper
    private lateinit var keAndroid: SQLiteDatabase
    private lateinit var listCobranza: ArrayList<CXC>
    // 2023-04-25 Se comenta el boton de recibo de cobro para que los recibos hechos sean a tras del planificador
    // lateinit var fab_newcxc: ExtendedFloatingActionButton

    // animaciones
    private val rotateOpen: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.rotate_open_anim
        )
    }
    private val rotateClose: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.rotate_close_anim
        )
    }
    private val fromBottom: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.from_bottom_anim
        )
    }
    private val toBottom: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.to_bottom_anim
        )
    }

    var RESULT_LOAD_DOCUMENT = 1

    // bool
    private var clicked = false

    private lateinit var binding: ActivityCxcactivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCxcactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Edicion de los colores del Bar de arriba de notificacion de las app y el bar de abajo de los 3 botones
        windowsColor(Constantes.AGENCIA)
        setColors()

        // instanciamiento del conector a la bdd
        conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null)

        // APP_W_VIGENCIA_EFECTIVO = conn.getConfigNum("APP_W_VIGENCIA_EFECTIVO")

        /* val builder = StrictMode.VmPolicy.Builder()
         StrictMode.setVmPolicy(builder.build())
         builder.detectFileUriExposure()*/
        // declaracion del boton en el Layout
        // 2023-04-25 Se comenta el boton de recibo de cobro para que los recibos hechos sean a tras del planificador
        // fab_newcxc  = findViewById<ExtendedFloatingActionButton>(R.id.fab_addcxc)

        // dclaracion del RecyclerView
        rvCxc = findViewById(R.id.rv_cxc)
        preferences = getSharedPreferences("Preferences", MODE_PRIVATE)
        codUsuario = preferences.getString("cod_usuario", null)
        codEmpresa = preferences.getString("codigoEmpresa", null)
        enlaceEmpresa = conn.getCampoStringCamposVarios(
            "ke_enlace", "kee_url", listOf("kee_codigo"), listOf(codEmpresa!!)
        )
        listCobranza = ArrayList()

        binding.fbtAddcxc.setOnClickListener {
            onAddButtonClicked()
        }

        binding.fabAdddeposit.setOnClickListener {
            iraCreacionDeposito()
        }
// 2023-04-25 Se comenta el boton de recibo de cobro para que los recibos hechos sean a tras del planificador
        /*fab_newcxc.setOnClickListener {
            iraCreacionCobranza()
        }*/
        // validarVigenciaCxc()
        cargarCobranzas()

        if (checkPermission()) {
            // Toast.makeText(this, "Permiso aceptado", Toast.LENGTH_SHORT).show()
        } else {
            requestPermissions()
        }

        val objetoAux = ObjetoAux(this)
        objetoAux.descargaDesactivo(codUsuario!!, codEmpresa!!, enlaceEmpresa)

        // Retroceder Activity
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val intent = Intent(applicationContext, PrincipalActivity::class.java)
                    startActivity(intent)
                }
            }
        )
    }

    private fun checkPermission(): Boolean {
        val permissionUno =
            ContextCompat.checkSelfPermission(applicationContext, WRITE_EXTERNAL_STORAGE)
        val permissionDos =
            ContextCompat.checkSelfPermission(applicationContext, READ_EXTERNAL_STORAGE)

        return permissionUno == PackageManager.PERMISSION_GRANTED && permissionDos == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE),
            requestCodeResponse
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == requestCodeResponse) {
            if (grantResults.isNotEmpty()) {
                val writeStorage = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val readStorage = grantResults[1] == PackageManager.PERMISSION_GRANTED

                if (writeStorage && readStorage) {
                    // Toast.makeText(this, "Permisos concedidos", Toast.LENGTH_SHORT).show()
                } else {
                    // Toast.makeText(this, "Permisos rechazados", Toast.LENGTH_SHORT).show()
                    // finish()
                }
            }
        }
    }

    private fun onAddButtonClicked() {
        setVisibility(clicked)
        setAnimation(clicked)

        clicked = !clicked
    }

    private fun setAnimation(clicked: Boolean) {
        if (!clicked) {
            // 2023-04-25 Se comenta el boton de recibo de cobro para que los recibos hechos sean a tras del planificador
            // fab_newcxc.startAnimation(fromBottom)
            binding.fabAdddeposit.startAnimation(fromBottom)
            binding.fbtAddcxc.startAnimation(rotateOpen)
        } else {
            // 2023-04-25 Se comenta el boton de recibo de cobro para que los recibos hechos sean a tras del planificador
            // fab_newcxc.startAnimation(toBottom)
            binding.fabAdddeposit.startAnimation(toBottom)
            binding.fbtAddcxc.startAnimation(rotateClose)
        }
    }

    private fun setVisibility(clicked: Boolean) {
        if (!clicked) {
            // 2023-04-25 Se comenta el boton de recibo de cobro para que los recibos hechos sean a tras del planificador
            // fab_newcxc.visibility = View.VISIBLE
            binding.fabAdddeposit.visibility = View.VISIBLE
        } else {
            // 2023-04-25 Se comenta el boton de recibo de cobro para que los recibos hechos sean a tras del planificador
            // fab_newcxc.visibility = View.INVISIBLE
            binding.fabAdddeposit.visibility = View.INVISIBLE
        }
    }

    // funcion para ir a la ventana de creacion de cobranza
    private fun iraCreacionCobranza() {
        val intent = Intent(this, creacionCobranzaActivity::class.java).apply {
            intent.putExtra("codigoUsuario", codUsuario)
            intent.putExtra("codigoEmpresa", codEmpresa)
        }
        startActivity(intent)
    }

    private fun iraCreacionDeposito() {
        val intent = Intent(this, DepositoActivity::class.java).apply {
            intent.putExtra("codigoUsuario", codUsuario)
            intent.putExtra("codigoEmpresa", codEmpresa)
        }
        startActivity(intent)
    }

    //
    private fun cargarCobranzas() {
        listCobranza = ArrayList()
        listCobranza.clear()
        keAndroid = conn.writableDatabase

        val tabla = "ke_precobranza"
        val columna = arrayOf(
            "cxcndoc," + "fchrecibo," + "edorec," + "bcomonto," + "efectivo, " + "moneda," + "tiporecibo",
            "bsretiva",
            "bsretflete"
        )
        // var seleccion = "codvend='${cod_usuario}' AND edorec != 3 AND edorec != 8"
        val seleccion = "codvend='$codUsuario' AND empresa = '$codEmpresa'"

        cursorCobranza =
            keAndroid.query(tabla, columna, seleccion, null, null, null, "cxcndoc DESC")

        while (cursorCobranza.moveToNext()) {
            val cursor = keAndroid.rawQuery(
                "SELECT nombrecli FROM ke_precobradocs WHERE cxcndoc = '${
                    cursorCobranza.getString(0)
                }' AND empresa = '$codEmpresa';",
                null
            )

            val cobranza = CXC()
            cobranza.id_recibo = cursorCobranza.getString(0)
            cobranza.fchrecibo = cursorCobranza.getString(1)
            cobranza.edorec = cursorCobranza.getString(2)
            cobranza.bcomonto = cursorCobranza.getDouble(3)
            cobranza.efectivo = cursorCobranza.getDouble(4)
            cobranza.moneda = cursorCobranza.getString(5)
            cobranza.tipoRecibo = cursorCobranza.getString(6)
            if (cursor.moveToFirst()) {
                try {
                    cobranza.cliente =
                        if (cobranza.tipoRecibo == "D") "Anexo de Deposito" else cursor.getString(0)
                } catch (e: Exception) {
                    e.printStackTrace()
                    cobranza.cliente = "No identificado"
                }
            } else {
                cobranza.cliente = "No identificado"
            }
            cobranza.bsretiva = cursorCobranza.getDouble(7)
            cobranza.bsretflete = cursorCobranza.getDouble(8)

            // 2023-06-15 vericifarVijenciaCXC sirve para revisar los recibos de cobro W que sean en efectivo y
            // de no ser cambiado su edorec (que se anexxe a un deposito) este sera anulado en los dias que tenga dentro
            cobranza.edorec = verificarVijenciaCXC(
                cobranza.fchrecibo, cobranza.efectivo, cobranza.id_recibo, cobranza.edorec
            )

            listCobranza.add(cobranza)
            cursor.close()
        }

        rvCxc.layoutManager = LinearLayoutManager(this)
        val adapter = CXCAdapter(
            listCobranza,
            codEmpresa!!,
            onClickListener = { codigoRecibo -> mensajeCXC(codigoRecibo) }
        )
        rvCxc.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    private fun verificarVijenciaCXC(
        fchrecibo: String,
        efectivo: Double,
        idRecibo: String,
        edorec: String,
    ): String {
        val fechaCompara = compararFecha(fchrecibo)
        println(fechaCompara > 0)
        println(efectivo > 0.0)
        println(edorec != "9")
        println(edorec != "10")
        return if (fechaCompara > 0 &&
            (efectivo > 0.0) &&
            (edorec != "9" && edorec != "10")
        ) {
            conn.upReciboCobroStatus(idRecibo, codEmpresa!!)
            "3"
        } else {
            edorec
        }
    }

    private fun mensajeCXC(codigoRecibo: String) {
        val builder = AlertDialog.Builder(
            ContextThemeWrapper(
                this,
                setAlertDialogTheme(Constantes.AGENCIA)
            )
        )
        builder.setTitle("Opciones del Recibo")
        builder.setMessage("Recibo: $codigoRecibo")
        // builder.setPositiveButton("OK", DialogInterface.OnClickListener(function = x))

        builder.setPositiveButton("Detalles") { _, _ ->
            irADestalleCXC(codigoRecibo)
        }

        val tabla = "ke_precobranza"
        val columna = arrayOf("edorec")
        val seleccion = "cxcndoc='$codigoRecibo' AND empresa = '$codEmpresa'"

        val cursorRecibo: Cursor =
            keAndroid.query(tabla, columna, seleccion, null, null, null, null)

        var estado = ""

        if (cursorRecibo.moveToFirst()) {
            estado = cursorRecibo.getString(0)
        }

        cursorRecibo.close()

        // estado == "1" || estado == "10"
        if (estado == "0") {
            builder.setNegativeButton("Borrar") { _, _ ->

                val subbuilder = AlertDialog.Builder(
                    ContextThemeWrapper(
                        this,
                        setAlertDialogTheme(Constantes.AGENCIA)
                    )
                )
                subbuilder.setTitle("Borrado de Recibo")
                subbuilder.setMessage("¿Está seguro de querer borrar el recibo $codigoRecibo?")

                subbuilder.setPositiveButton("Si") { _, _ ->
                    borrarRecibo(codigoRecibo)
                }

                subbuilder.setNeutralButton("No") { _, _ ->
                }

                val subventana = subbuilder.create()
                subventana.show()

                val pbutton: Button = subventana.getButton(DialogInterface.BUTTON_POSITIVE)
                pbutton.apply {
                    setTextColor(colorTextAgencia(Constantes.AGENCIA))
                }

                val nbutton: Button = subventana.getButton(DialogInterface.BUTTON_NEUTRAL)
                nbutton.apply {
                    setTextColor(colorTextAgencia(Constantes.AGENCIA))
                }
            }
        }

        builder.setNeutralButton("Generar Documento PDF") { _, _ ->
            // toast("Le di al boton")
            val validarCampo = conn.getCampoStringCamposVarios(
                "ke_precobradocs",
                "codcliente",
                listOf("cxcndoc", "empresa"),
                listOf(codigoRecibo, codEmpresa!!)
            )
            if (false /*validarCampo == ""*/) {
                toast("El recibo no debe ser de antes de la actualización 23.10.2023")
            } else {
                try {
                    crearPDF(codigoRecibo)
                } catch (e: Exception) {
                    e.printStackTrace()
                    toast("No se puedo crear el recibo")
                }
            }
        }

        val ventana = builder.create()
        ventana.show()

        val pbutton: Button = ventana.getButton(DialogInterface.BUTTON_POSITIVE)
        pbutton.apply {
            setTextColor(colorTextAgencia(Constantes.AGENCIA))
        }

        val nbutton: Button = ventana.getButton(DialogInterface.BUTTON_NEGATIVE)
        nbutton.apply {
            setTextColor(colorTextAgencia(Constantes.AGENCIA))
        }

        val nebutton: Button = ventana.getButton(DialogInterface.BUTTON_NEUTRAL)
        nebutton.apply {
            setTextColor(colorTextAgencia(Constantes.AGENCIA))
        }
    }

    private fun irADestalleCXC(codigoRecibo: String) {
        val editor: SharedPreferences.Editor = preferences.edit()
        editor.putString("recibo", codigoRecibo)
        editor.apply()
        val intent = Intent(this, DetalleCXCActivity::class.java)
        startActivity(intent)
    }

    private fun borrarRecibo(codigoRecibo: String) {
        keAndroid.delete(
            "ke_precobranza",
            "cxcndoc = '$codigoRecibo' AND empresa = '$codEmpresa'",
            null
        )
        keAndroid.delete(
            "ke_precobradocs",
            "cxcndoc = '$codigoRecibo' AND empresa = '$codEmpresa'",
            null
        )
        keAndroid.delete("ke_retimg", "cxcndoc = '$codigoRecibo' AND empresa = '$codEmpresa'", null)
        clearAdapter()
    }

    /*Este proceso debe colocarse solo para los recibos que ya esten subidos */
    private fun crearPDF(codigoRecibo: String) {
        val tabla = "ke_precobranza"
        val columna = arrayOf("edorec")
        val seleccion = "cxcndoc='$codigoRecibo' AND empresa = '$codEmpresa'"

        val cursorRecibo: Cursor =
            keAndroid.query(tabla, columna, seleccion, null, null, null, null)

        var estado = ""

        if (cursorRecibo.moveToFirst()) {
            estado = cursorRecibo.getString(0)
        }

        cursorRecibo.close()

        // estado == "1" || estado == "10"
        if (true) {
            val listaDocumentos = arrayListOf<String>()
            val nombreEmpresa = nombreEmpresa(codEmpresa)
            val rifEmpresa = rifEmpresa(codEmpresa)
            val dirEmpresa1 = "CALLE 18 CON AV GOAJIRA VIA EL MOJAN, LOCALGALPON 3, ZONA"
            val dirEmpresa2 = "INDUSTRIAL NORTE, COMPLEJO PARQUE INDUSTRIAL NORTE,"
            val dirEmpresa3 = "MARACAIBO ZULIA POSTAL 4001"
            val direccion = direccionEmpresa(codEmpresa)
            val tipoDoc = "Precobranza"
            val subTipoDoc: String
            var vendedorRecibo = ""
            val nombreCliente = arrayListOf<String>()
            val codCliente = arrayListOf<String>()
            var fechaRecibo = ""
            var montoRecibo = 0.00
            var monedaRecibo: String
            var tipoPago = ""
            val tasaDia: String
            val keCxcId: String
            var fechaTasa = ""
            val moneda: String
            val banco: String
            val codBanco: String
            var nomBanco = ""
            val refBanco: String
            var totalCobrado = 0.0
            val monedaSigno: String
            val tasaDoc = arrayListOf<Double>()
            val cobradoBS = arrayListOf<Double>()
            val cobradoDol = arrayListOf<Double>()
            val retenFleBS = arrayListOf<Double>()
            val retenIvaBS = arrayListOf<Double>()
            val retenRef = arrayListOf<String>()
            val ivaBS = arrayListOf<Double>()
            val fleteBS = arrayListOf<Double>()
            val dolarFlete = arrayListOf<Int>()
            val tasaDiad = arrayListOf<Double>()
            val reten = arrayListOf<Int>()
            val descuento = arrayListOf<Double>()
            val aFavor = arrayListOf<Double>()
            val docPrevio = arrayListOf<String>()
            val emiRetIva = arrayListOf<String>()
            val emiRetFle = arrayListOf<String>()
            var totalCobrad = 0.0
            var totalNeto = 0.0
            var totalIva = 0.0
            var totalFlete = 0.0
            var totalDescuento = 0.0
            var totalAFavor = 0.0
            var totalRetFle = 0.0
            var totalRetIva = 0.0
            val tipoRecibo: String

            val tabla = "ke_precobranza"
            val columna = arrayOf(
                "cxcndoc," + "codvend," + "fchrecibo," + "bcomonto," + "moneda," + "efectivo," + "tasadia," + "kecxc_id," + "moneda," + "bcocod," + "bcoref," + "bstotal," + "doltotal, tiporecibo, (bsretiva + bsretflete)"
            )
            val seleccion = "cxcndoc='$codigoRecibo' AND empresa = '$codEmpresa'"

            val cursorRecibo: Cursor =
                keAndroid.query(tabla, columna, seleccion, null, null, null, null)

            if (cursorRecibo.moveToFirst()) {
                vendedorRecibo = cursorRecibo.getString(1)
                monedaRecibo = cursorRecibo.getString(4)
                tasaDia = cursorRecibo.getString(6)
                keCxcId = cursorRecibo.getString(7)
                banco = cursorRecibo.getString(9)
                refBanco = cursorRecibo.getString(10)
                tipoRecibo = cursorRecibo.getString(13)

                subTipoDoc = when (tipoRecibo) {
                    "W" -> "Recibo de Cobro"
                    "D" -> "Anexo de Deposito"
                    "R" -> "Recibo de Retención"
                    else -> "No identificado"
                }

                when (cursorRecibo.getString(8)) {
                    "1" -> {
                        moneda = "Bolívares"
                        // totalCobrado = cursorRecibo.getString(11)
                        monedaSigno = "Bs."
                    }

                    "2" -> {
                        moneda = "Dólar"
                        // totalCobrado = cursorRecibo.getString(12)
                        monedaSigno = "$"
                    }

                    else -> {
                        moneda = "No identificado"
                        // totalCobrado = "No identificado"
                        monedaSigno = "N/I"
                    }
                }

                val tablaT = "kecxc_tasas"
                val columnaT = arrayOf("kecxc_fchyhora")
                val seleccionT = "kecxc_id='$keCxcId' AND empresa = '$codEmpresa'"

                val cursorTasa: Cursor =
                    keAndroid.query(tablaT, columnaT, seleccionT, null, null, null, null)

                if (cursorTasa.moveToFirst()) {
                    val ldt: LocalDateTime = LocalDateTime.parse(
                        cursorTasa.getString(0),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    )
                    val writingFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss a")
                    fechaTasa = ldt.format(writingFormatter)
                } else {
                    "No identificado"
                }

                cursorTasa.close()

                if (tipoRecibo == "W" || tipoRecibo == "R") {
                    val readingFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    val date = LocalDate.parse(cursorRecibo.getString(2), readingFormatter)
                    val writingFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                    fechaRecibo = date.format(writingFormatter)
                } else if (tipoRecibo == "D") {
                    val ldt: LocalDateTime = LocalDateTime.parse(
                        cursorRecibo.getString(2),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    )
                    val writingFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss a")
                    fechaRecibo = ldt.format(writingFormatter)
                }

                // IF para validar si el documento fue pagado con efectivo o transferencia

                if (cursorRecibo.getDouble(5) > 0.0) {
                    // Llenando el valor total con el fectivo dado
                    // montoRecibo = cursorRecibo.getDouble(3)
                    tipoPago = "Efectivo"
                    totalCobrado = cursorRecibo.getDouble(5)
                } else if (cursorRecibo.getDouble(3) > 0.0) {
                    // Llenando el valor total con la transferencia realizada
                    // montoRecibo = cursorRecibo.getDouble(5)
                    tipoPago = "Transferencia"

                    totalCobrado = cursorRecibo.getDouble(3)
                } else if (cursorRecibo.getDouble(14) > 0.0) {
                    // Llenando el valor total con la transferencia realizada
                    // montoRecibo = cursorRecibo.getDouble(5)
                    tipoPago = "Retencion"

                    totalCobrado = cursorRecibo.getDouble(14)
                } else {
                    montoRecibo = 0.0
                }

                if (banco != "") {
                    val tablaBanco = "listbanc"
                    val columnaBanco = arrayOf("nombanco")
                    val seleccionBanco = "codbanco='$banco' AND empresa = '$codEmpresa'"

                    val cursorBanco: Cursor = keAndroid.query(
                        tablaBanco,
                        columnaBanco,
                        seleccionBanco,
                        null,
                        null,
                        null,
                        null
                    )

                    if (cursorBanco.moveToNext()) {
                        nomBanco = cursorBanco.getString(0)
                        codBanco = banco
                    } else {
                        nomBanco = "No Identificado"
                        codBanco = "N/I"
                    }

                    cursorBanco.close()
                } else {
                    if (tipoRecibo == "W") {
                        nomBanco = "Efectivo"
                    } else if (tipoRecibo == "R") {
                        nomBanco = "Retención"
                    }
                    codBanco = ""
                }

                val cursorDocs = keAndroid.rawQuery(
                    "SELECT nombrecli, documento, codcliente, tasadoc, tnetodbs, tnetoddol, bsretiva, bsretfte, bsmtoiva, bsmtofte, refret, refretfte, reten, prcdsctopp, afavor, kecxc_idd, fchemiret, fchemirfte, dolarflete, tasadiad FROM ke_precobradocs WHERE cxcndoc = '$codigoRecibo' AND empresa = '$codEmpresa';",
                    null
                )
                while (cursorDocs.moveToNext()) {
                    listaDocumentos.add(cursorDocs.getString(1))
                    // nombreCliente.add(cursorDocs.getString(0))
                    codCliente.add(cursorDocs.getString(2))
                    tasaDoc.add(cursorDocs.getDouble(3))
                    cobradoBS.add(cursorDocs.getDouble(4))
                    cobradoDol.add(cursorDocs.getDouble(5))
                    retenIvaBS.add(cursorDocs.getDouble(6))
                    retenFleBS.add(cursorDocs.getDouble(7))
                    ivaBS.add(cursorDocs.getDouble(8))
                    fleteBS.add(cursorDocs.getDouble(9))
                    dolarFlete.add(cursorDocs.getInt(18))
                    tasaDiad.add(cursorDocs.getDouble(19))

                    if (tipoRecibo == "R") {
                        retenRef.add(cursorDocs.getString(10))
                        retenRef.add(cursorDocs.getString(11))
                    }

                    reten.add(cursorDocs.getInt(12))
                    descuento.add(cursorDocs.getDouble(13))
                    aFavor.add(cursorDocs.getDouble(14))
                    docPrevio.add(codUsuario + "-PRC-" + cursorDocs.getString(15))
                    emiRetIva.add(cursorDocs.getString(16))
                    emiRetFle.add(cursorDocs.getString(17))

                    /*if (tipoRecibo == "W"){
                        cobradoDol.add(cursorDocs.getDouble(5))
                    } else if (tipoRecibo == "D"){
                        cobradoDol.add(cursorDocs.getDouble(8))
                    }*/

                    // Para acortar el nombre del cliente
                    if (cursorDocs.getString(0).length > 50 && tipoRecibo == "D") {
                        var cliente = ""
                        for (i in 0..49) {
                            cliente += cursorDocs.getString(0)[i].toString()
                        }
                        nombreCliente.add("$cliente...")
                    } else if (cursorDocs.getString(0).length > 90 && tipoRecibo != "D") {
                        var cliente = ""
                        val ult = 89
                        val ini = 0
                        for (i in ini..ult) {
                            cliente += cursorDocs.getString(0)[i].toString()
                        }
                        nombreCliente.add("$cliente...")
                    } else {
                        nombreCliente.add(cursorDocs.getString(0))
                    }
                }
                cursorDocs.close()
            } else {
                Toast.makeText(this, "No hay datos del recibo", Toast.LENGTH_SHORT).show()

                return
            }
            cursorRecibo.close()

            if (monedaRecibo == "1") {
                monedaRecibo = "Bs."
            } else if (monedaRecibo == "2") {
                monedaRecibo = "$"
            }

            val reciboPDF = PdfDocument()
            val paint = Paint()
            // conf inicial de la pag
            val myInfo = PdfDocument.PageInfo.Builder(612, 792, 1).create()
            val pagina: PdfDocument.Page = reciboPDF.startPage(myInfo)
            val canvas = pagina.canvas
            // del obj paint
            paint.textAlign = Paint.Align.LEFT
            paint.textSize = 16f
            paint.color = Color.BLACK
            paint.typeface = Typeface.createFromAsset(this.assets, "font/arialbd.ttf")

            // CABECERA
            // imagen del la cabecera
            val bmp = BitmapFactory.decodeResource(this.resources, plantillaPDF(codEmpresa))
            val scaledBitmap = Bitmap.createScaledBitmap(bmp, 612, 792, false)
            canvas.drawBitmap(scaledBitmap, 0f, 0f, paint)

            // ICONO DE LA EMPRESA
            val iconEmpresa = BitmapFactory.decodeResource(this.resources, logoPDF(codEmpresa))
            val scaledBtmpEmpresa = Bitmap.createScaledBitmap(iconEmpresa, 100, 100, false)
            canvas.drawBitmap(scaledBtmpEmpresa, 5f, 15f, paint)

            // titulos de la cabecera
            canvas.drawText(nombreEmpresa, 105f, 35f, paint)

            // RIF Empresa
            paint.textSize = 10f
            canvas.drawText(rifEmpresa, 105f, 45f, paint)

            // Direccion Empresa
            paint.typeface = Typeface.createFromAsset(this.assets, "font/arial.ttf")
            paint.typeface = Typeface.createFromAsset(this.assets, "font/arial.ttf")
            var y = 55f
            direccion.forEach { direc ->
                canvas.drawText(direc, 105f, y, paint)
                y += 10
            }
            // canvas.drawText(direccion, 105f, 55f, paint)
            // canvas.drawText(dirEmpresa2, 105f, 65f, paint)
            // canvas.drawText(dirEmpresa3, 105f, 75f, paint)

            // Tipo de documento
            paint.textAlign = Paint.Align.RIGHT
            paint.textSize = 15f
            paint.color = Color.rgb(7, 4, 97)
            paint.typeface = Typeface.createFromAsset(this.assets, "font/arialbd.ttf")
            canvas.drawText(tipoDoc, 597f, 60f, paint)

            // Subtipo de documento
            paint.textSize = 13f
            canvas.drawText(subTipoDoc, 597f, 75f, paint)

            // Numero de Rrecibo
            paint.color = Color.RED
            paint.textSize = 16f
            canvas.drawText(
                "${if (estado == "0") "*" else ""}REC:${if (estado == "0") "*" else ""} $codigoRecibo ${if (estado == "0") "*" else ""}",
                597f,
                100f,
                paint
            )

            // Fecha de creacion
            paint.textSize = 12f
            paint.color = Color.BLACK
            paint.typeface = Typeface.createFromAsset(this.assets, "font/arialbd.ttf")
            if (tipoRecibo == "W" || tipoRecibo == "R") {
                canvas.drawText("Emisión", 517f, 115f, paint)
            } else if (tipoRecibo == "D") {
                canvas.drawText("Emisión", 450f, 115f, paint)
            }

            paint.typeface = Typeface.createFromAsset(this.assets, "font/arial.ttf")
            canvas.drawText(fechaRecibo, 597f, 115f, paint)

            // Tasa
            if (tipoRecibo == "W") {
                paint.textSize = 14f
                canvas.drawText("Tasa: $tasaDia Bs./$", 597f, 130f, paint)
            }

            // Fecha Tasa
            if (tipoRecibo == "W") {
                canvas.drawText("de $fechaTasa", 597f, 145f, paint)
            }

            // Tipo de moneda
            if (tipoRecibo == "W") {
                paint.textSize = 12f
                canvas.drawText("Moneda del Recibo: $moneda", 597f, 160f, paint)
            } else if (tipoRecibo == "D" || tipoRecibo == "R") {
                paint.textSize = 12f
                canvas.drawText("Moneda del Recibo: $moneda", 597f, 130f, paint)
            }

            paint.textAlign = Paint.Align.LEFT
            // Marco de los datos del deposito/transferencia
            val iconMarco = BitmapFactory.decodeResource(this.resources, R.drawable.marco)
            val scaledBtmpMarco = Bitmap.createScaledBitmap(iconMarco, 360, 60, false)
            canvas.drawBitmap(scaledBtmpMarco, 15f, 126f, paint)

            paint.textSize = 11f
            paint.typeface = Typeface.createFromAsset(this.assets, "font/arialbd.ttf")
            canvas.drawText("[ Datos del Depósito o Transferencia ]", 30f, 131f, paint)

            // Datos del deposito/transferencia
            paint.textSize = 13f
            paint.typeface = Typeface.createFromAsset(this.assets, "font/arial.ttf")
            if (tipoRecibo == "R") {
                canvas.drawText(
                    "$codBanco $nomBanco " + (if (retenRef[0].isEmpty()) "" else " Ret. IVA ") + (if (retenRef[0].isEmpty() || retenRef[1].isEmpty()) "" else "/") + (if (retenRef[1].isEmpty()) "" else " Ret. Flete"),
                    40f,
                    150f,
                    paint
                )
            } else {
                canvas.drawText(
                    "$codBanco $nomBanco",
                    40f,
                    150f,
                    paint
                )
            }

            if (refBanco != "") {
                canvas.drawText("REF:", 40f, 170f, paint)

                paint.textSize = 16f
                canvas.drawText(refBanco, 75f, 170f, paint)
            } else if (tipoRecibo == "R") {
                canvas.drawText("REF:", 40f, 170f, paint)

                paint.textSize = 16f

                canvas.drawText(
                    (if (retenRef[0].isEmpty()) "" else " ${retenRef[0]} ") + (if (retenRef[0].isEmpty() || retenRef[1].isEmpty()) "" else "/") + if (retenRef[1].isEmpty()) "" else " ${retenRef[1]}",
                    75f,
                    170f,
                    paint
                )
                // canvas.drawText("${retenRef[0]} / ${retenRef[1]}", 75f, 170f, paint)
            }

            paint.textSize = 11f
            paint.typeface = Typeface.createFromAsset(this.assets, "font/arialbd.ttf")
            canvas.drawText(
                "Recibo de Precobranza Generado ${if (estado == "0") "" else "y Subido"}",
                30f,
                220f,
                paint
            )

            paint.textSize = 14f
            paint.typeface = Typeface.createFromAsset(this.assets, "font/arial.ttf")
            canvas.drawText("Monto Total", 400f, 220f, paint)

            paint.typeface = Typeface.createFromAsset(this.assets, "font/arialbd.ttf")
            canvas.drawText(
                "${if (estado == "0") "*" else ""} $monedaSigno ${totalCobrado.toTwoDecimals()} ${if (estado == "0") "*" else ""}",
                475f,
                220f,
                paint
            )

            val iconLinea = BitmapFactory.decodeResource(this.resources, R.drawable.linea)
            val scaledBtmpLinea = Bitmap.createScaledBitmap(iconLinea, 595, 5, false)
            canvas.drawBitmap(scaledBtmpLinea, 5f, 230f, paint)

            val iconLineaD = BitmapFactory.decodeResource(this.resources, R.drawable.linea_d)
            val scaledBtmpLineaD = Bitmap.createScaledBitmap(iconLineaD, 570, 5, false)
            canvas.drawBitmap(scaledBtmpLineaD, 18f, 247.5f, paint)

            paint.textSize = 12f
            paint.typeface = Typeface.createFromAsset(this.assets, "font/arial.ttf")
            canvas.drawText("Documento", 25f, 245f, paint)
            if (tipoRecibo != "R") {
                canvas.drawText("Mto Cob", 100f, 245f, paint)
                if (tipoRecibo == "D") {
                    canvas.drawText("Cliente", 155f, 245f, paint)
                } else {
                    canvas.drawText("Neto", 190f, 245f, paint)
                    canvas.drawText("IVA", 257f, 245f, paint)
                    canvas.drawText("Flete", 308f, 245f, paint)
                    canvas.drawText("Ret. IVA", 357f, 245f, paint)
                    canvas.drawText("Ret. Fle", 415f, 245f, paint)
                    canvas.drawText("Dto %", 480f, 245f, paint)
                    // canvas.drawText("A Favor", 375f, 245f, paint)
                }
            } else {
                canvas.drawText("Ret. IVA", 100f, 245f, paint)
                canvas.drawText("Ret. Flete", 155f, 245f, paint)
                canvas.drawText("Emi. Ret. I.", 215f, 245f, paint)
                canvas.drawText("Emi. Ret. F.", 285f, 245f, paint)
            }

            var counter = 265f

            // ESTAS INTENTANDO QUE LA SUMA DEL DOCUMENTO EN BS Y DOLARES ESTE BIEN EN LA MONEDA QUE SE SOLICITA
            for (i in listaDocumentos.indices) {
                val tasaAUsar = if (dolarFlete[i] == 0) tasaDoc[i] else tasaDiad[i]
                val fd = if (dolarFlete[i] == 0) "" else "FD"

                val campoWhere: List<String> = listOf("cxcndoc", "documento", "empresa")
                val respuestaWhere: List<String> =
                    listOf(codigoRecibo, listaDocumentos[i], codEmpresa!!)
                val retenIvaBD = conn.getCampoDoubleCamposVarios(
                    "ke_precobradocs",
                    "bsretiva",
                    campoWhere,
                    respuestaWhere
                ) // <--------- ya los buscastes anteriormente
                val retenFleteBD = conn.getCampoDoubleCamposVarios(
                    "ke_precobradocs",
                    "bsretfte",
                    campoWhere,
                    respuestaWhere
                )

                val valorCobrado = if (moneda == "Dólar") cobradoDol[i] else cobradoBS[i]

                val tasa = if (dolarFlete[i] == 0) tasaDoc[i] else tasaDiad[i]

                val ivaACobrado =
                    if (moneda == "Dólar") redondeo(ivaBS[i] / tasa) else redondeo(ivaBS[i])
                val fleteACobrado =
                    if (moneda == "Dólar") redondeo(fleteBS[i] / tasaAUsar) else redondeo(fleteBS[i])
                val retIvaACobrado = if (moneda == "Dólar") {
                    redondeo(retenIvaBD / tasa)
                } else {
                    redondeo(
                        retenIvaBD
                    )
                }
                val retFleteACobrado = if (moneda == "Dólar") {
                    redondeo(retenFleteBD / tasa)
                } else {
                    redondeo(
                        retenFleteBD
                    )
                }

                val neto = redondeo(
                    redondeo(valorCobrado) - ivaACobrado - fleteACobrado - retIvaACobrado - retFleteACobrado
                )

                val ivaCobrado = ivaACobrado + retIvaACobrado
                val fleteCobrado = fleteACobrado + retFleteACobrado

                // variables totaltes
                totalCobrad += valorCobrado
                totalNeto += neto
                totalIva += ivaCobrado
                totalFlete += fleteCobrado
                totalDescuento += descuento[i]
                totalAFavor += aFavor[i]
                totalRetFle += retFleteACobrado
                totalRetIva += retIvaACobrado

                // solo aplica para anexos de deposito
                // totalEfectivo += conn.getEfectivoDoc(docPrevio[i])

                // println("En neto --> ${valorCobrado - (ivaBS[i] - abs(retenIvaBS[i])) - (fleteBS[i] - abs(retenFleBS[i]))}")
                // println("En neto --> $valorCobrado - (${ivaBS[i]} - abs(retenIvaBS[i]) - (fleteBS[i] - abs(retenFleBS[i])))")

                if (tipoRecibo != "R") {
                    canvas.drawText(listaDocumentos[i], 25f, counter, paint)

                    if (tipoRecibo == "D") {
                        canvas.insertarNumPDF(valorCobrado, 100f, counter, paint)
                        canvas.drawText(
                            "${codCliente[i]} ${nombreCliente[i]}",
                            155f,
                            counter,
                            paint
                        )
                        // Para saber si estoy al final del ciclo voy a agregar una linea extra de saldos a favor
                        if (i == listaDocumentos.size - 1) {
                            counter = counter.plus(15f)
                            canvas.insertarNumPDF(aFavor.sumOf { it }, 100f, counter, paint)
                            canvas.drawText(
                                "Saldos a favor",
                                155f,
                                counter,
                                paint
                            )
                        }
                    } else {
                        // Compruebo si la retencion la muestro en Bs. o Dolares
                        val retenIva =
                            if (monedaSigno == "Bs.") retenIvaBS[i] else retenIvaBS[i] / tasa
                        val retenFlete =
                            if (monedaSigno == "Bs.") retenFleBS[i] else retenFleBS[i] / tasa

                        canvas.insertarNumPDF(valorCobrado, 105f, counter, paint)
                        canvas.insertarNumPDF(neto, 175f, counter, paint)
                        canvas.insertarNumPDF(ivaCobrado, 235f, counter, paint)
                        canvas.insertarNumPDF(fleteCobrado, 295f, counter, paint)
                        canvas.insertarNumPDF(retenIva, 360f, counter, paint)
                        canvas.insertarNumPDF(retenFlete, 415f, counter, paint)
                        canvas.insertarNumPDF(descuento[i], 470f, counter, paint)
                        canvas.drawText(fd, 525f, counter, paint)

                        /*canvas.drawText(redondeo(valorCobrado).toString(), 100f, counter, paint)
                        canvas.drawText(neto.toString(), 155f, counter, paint)
                        canvas.drawText(redondeo(ivaCobrado).toString(), 210f, counter, paint)
                        canvas.drawText(redondeo(fleteCobrado).toString(), 265f, counter, paint)
                        canvas.drawText(redondeo(if (monedaSigno == "Bs.") retenIvaBS[i] else retenIvaBS[i] / tasaDoc[i]).toString(), 320f, counter, paint)
                        canvas.drawText(redondeo(if (monedaSigno == "Bs.") retenFleBS[i] else retenFleBS[i] / tasaDoc[i]).toString(), 375f, counter, paint)
                        canvas.drawText(redondeo(descuento[i]).toString(), 430f, counter, paint)*/
                        // canvas.drawText(redondeo(aFavor[i]).toString(), 375f, counter, paint)
                    }
                } else {
                    canvas.drawText(listaDocumentos[i], 25f, counter, paint)
                    canvas.drawText(retenIvaBS[i].toString(), 100f, counter, paint)
                    canvas.drawText(retenFleBS[i].toString(), 155f, counter, paint)
                    canvas.drawText(emiRetIva[i], 215f, counter, paint)
                    canvas.drawText(emiRetFle[i], 285f, counter, paint)
                }

                // canvas.drawText("${codCliente[i]} ${nombreCliente[i]}", 200f, counter, paint)
                counter = counter.plus(15f)
            }
            // canvas.drawBitmap(scaledBtmpLinea, 5f, counter - 13, paint)
            canvas.drawBitmap(scaledBtmpLineaD, 18f, counter - 13, paint)
            if (tipoRecibo == "W" || tipoRecibo == "R") {
                canvas.drawBitmap(scaledBtmpLinea, 5f, counter - 13 + 20, paint)
                canvas.drawText("Total", 25f, counter + 3, paint)
                if (tipoRecibo == "W") {
                    canvas.insertarNumPDF(totalCobrad, 105f, counter + 3, paint)
                    canvas.insertarNumPDF(totalNeto, 175f, counter + 3, paint)
                    canvas.insertarNumPDF(totalIva, 235f, counter + 3, paint)
                    canvas.insertarNumPDF(totalFlete, 295f, counter + 3, paint)
                    canvas.insertarNumPDF(totalRetIva, 360f, counter + 3, paint)
                    canvas.insertarNumPDF(totalRetFle, 415f, counter + 3, paint)
                    canvas.insertarNumPDF(
                        totalDescuento / listaDocumentos.size,
                        470f,
                        counter + 3,
                        paint
                    )
                    canvas.drawText(
                        "A Favor: ${totalAFavor.round()}",
                        525f,
                        counter + 3,
                        paint
                    )
                } else {
                    canvas.drawText(redondeo(totalRetIva).toString(), 100f, counter + 3, paint)
                    canvas.drawText(redondeo(totalRetFle).toString(), 155f, counter + 3, paint)
                }

                paint.textSize = 11f
                paint.typeface = Typeface.createFromAsset(this.assets, "font/arialbd.ttf")
                canvas.drawText("Cliente : ", 25f, counter + 25, paint)
                paint.typeface = Typeface.createFromAsset(this.assets, "font/arial.ttf")
                canvas.drawText(codCliente[0], 75f, counter + 25, paint)
                canvas.drawText(nombreCliente[0], 25f, counter + 40, paint)

                paint.typeface = Typeface.createFromAsset(this.assets, "font/arialbd.ttf")
                canvas.drawText("Vendedor : ", 25f, counter + 55, paint)
                paint.typeface = Typeface.createFromAsset(this.assets, "font/arial.ttf")
                canvas.drawText(codUsuario!!, 90f, counter + 55, paint)
                canvas.drawText(
                    conn.getCampoStringCamposVarios(
                        "listvend",
                        "nombre",
                        listOf("codigo", "empresa"),
                        listOf(codUsuario!!, codEmpresa!!)
                    ),
                    25f,
                    counter + 70,
                    paint
                )

                // Deuda pendiente, eliminado por que aun no saca bien los calculos
                /*if (tipoRecibo == "W") {
                    paint.typeface = Typeface.createFromAsset(this.assets, "font/arial.ttf")
                    canvas.drawText(
                        "Deuda Pendiente " + redondeo(
                            conn.getDeudaClienteTotal(
                                codCliente[0],
                                monedaSigno,
                                tasaDia.toDouble(),
                                totalCobrado.toDouble()
                            )
                        ).toString() + "$", 375f, counter + 25, paint
                    )
                }*/
            } else {
                canvas.drawBitmap(scaledBtmpLinea, 5f, counter - 13 + 20, paint)
                canvas.drawText("Total", 25f, counter + 3, paint)
                canvas.insertarNumPDF(totalCobrad + totalAFavor, 100f, counter + 3, paint)
            }

            // 2023-11-06 se comento por que no cuadra con las notas
            /*if (tipoRecibo == "W" && reten[0] == 0) {
                paint.textSize = 14f
                paint.typeface = Typeface.createFromAsset(this.assets, "font/arialbd.ttf")
                canvas.drawText(
                    "Los montos vistos en el detalle se le están aplicando retenciones",
                    25f,
                    730f,
                    paint
                )
            }*/

            paint.textSize = 14f
            paint.typeface = Typeface.createFromAsset(this.assets, "font/arialbd.ttf")
            canvas.drawText(
                "*Va sin Enmienda*",
                455f,
                counter + 25,
                paint
            )

            // NOMBRE DEL CLIENTE retencion
//        paint.textAlign = Paint.Align.LEFT
//        paint.color = Color.BLACK
//
//        paint.typeface = Typeface.createFromAsset(this.assets, "font/arialbd.ttf")
//        canvas.drawText("Cliente: ", 30f, 90f, paint)
//        paint.typeface = Typeface.createFromAsset(this.assets, "font/arial.ttf")
//        canvas.drawText(nombreCliente, 30f, 100f, paint)
//
//        //Monto del Recibo
//        paint.typeface = Typeface.createFromAsset(this.assets, "font/arialbd.ttf")
//        canvas.drawText("Monto Pagado: ", 30f, 120f, paint)
//        paint.typeface = Typeface.createFromAsset(this.assets, "font/arial.ttf")
//        canvas.drawText(monedaRecibo + montoRecibo, 120f, 120f, paint)
//
//        //Fecha del Recibo
//        paint.typeface = Typeface.createFromAsset(this.assets, "font/arialbd.ttf")
//        canvas.drawText("Fecha del Recibo: ", 30f, 130f, paint)
//        paint.typeface = Typeface.createFromAsset(this.assets, "font/arial.ttf")
//        canvas.drawText(fechaRecibo, 130f, 130f, paint)
//
//
//        //vendedor
//        paint.typeface = Typeface.createFromAsset(this.assets, "font/arialbd.ttf")
//        canvas.drawText("Vendedor: ", 30f, 140f, paint)
//        paint.typeface = Typeface.createFromAsset(this.assets, "font/arial.ttf")
//        canvas.drawText(vendedorRecibo, 100f, 140f, paint)
//
//        //Recta que separa cabecera de las lineas
//        canvas.drawRect(0f, 160f, 300f, 162f, paint)
//
//        //Documentos en el recibo:
//        paint.typeface = Typeface.createFromAsset(this.assets, "font/arialbd.ttf")
//        canvas.drawText("Doc: ", 30f, 180f, paint)
//        paint.typeface = Typeface.createFromAsset(this.assets, "font/arial.ttf")
//        var counter = 190f

//        for(i in listaDocumentos.indices){
//            println("Documento que se imprimen en el pdf: "+ listaDocumentos[i])
//            canvas.drawText(listaDocumentos[i], 30f, counter, paint)
//            counter = counter.plus(10f)
//        }

            reciboPDF.finishPage(pagina)
            val numeroRecibo = "RECIBO_NRO_$codigoRecibo.pdf"
            // este sera el nombre del documento al momento de crearlo y guardarlo en el almacenamiento

            val path = getExternalFilesDir(null)!!.absoluteFile.toString() + "/" + numeroRecibo
            val file = File(path)

            try {
                reciboPDF.writeTo(FileOutputStream(file))

                Toast.makeText(this.applicationContext, "PDF Generado", Toast.LENGTH_LONG).show()
            } catch (e: IOException) {
                Toast.makeText(this.applicationContext, "PDF No Generado", Toast.LENGTH_LONG).show()
                println("error en ${e.printStackTrace()}")
            }

            reciboPDF.close()
            abrirRecibo(file)
        } else {
            Toast.makeText(this, "Debe subir el recibo ", Toast.LENGTH_SHORT).show()
        }
    }

    private fun verificarReten(reten: Int, campo: String, documento: String): Double {
        return if (reten == 1) {
            0.0 // <- retorno
        } else {
            val cursor = keAndroid.rawQuery(
                "SELECT $campo FROM ke_precobradocs WHERE documento = '$documento' AND empresa = '$codEmpresa'",
                null
            )
            val regreso: Double = if (cursor.moveToFirst()) {
                cursor.getDouble(0) // <- valor adquirido por "regreso"
            } else {
                0.0 // <- valor adquirido por "regreso"
            }
            cursor.close()
            regreso // <- retorno
        }
    }

    private fun redondeo(valor: Double): Double {
        return (valor * 100.00).roundToInt() / 100.00
    }

    private fun validarMoneda(moneda: String, valor: Double, tasa: Double): Double {
        return if (moneda == "Dólar") {
            (valor / tasa)
        } else {
            valor
        }
    }

    private fun abrirRecibo(file: File) {
        // val ruta = "$rutaRaiz/$nombreArchivo"
        // println("La ruta -> $ruta")
        // val file = File(ruta.substring(1))

        if (!file.exists()) {
            Toast.makeText(
                this.applicationContext,
                "Este archivo no existe o fue cambiado de lugar.",
                Toast.LENGTH_LONG
            ).show()
        } else {
            try {
                val builder = VmPolicy.Builder()
                StrictMode.setVmPolicy(builder.build())

                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "application/pdf"

                val outputPdfUri = FileProvider.getUriForFile(
                    this,
                    this.packageName + ".provider",
                    file
                )

                shareIntent.putExtra(Intent.EXTRA_STREAM, outputPdfUri)

                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                // Write Permission might not be necessary
                // Write Permission might not be necessary
                shareIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

                startActivity(Intent.createChooser(shareIntent, "Share PDF using.."))

                /*println("antes del intent")
                val share = Intent()
                share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                share.action = Intent.ACTION_SEND
                share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file))
                share.type = "application/pdf"
                println("antes del intent2 ")
                startActivity(share)
                println("despues del intent")*/

                /* println("1")
                 val intentShare = Intent(Intent.ACTION_SEND)
                 println("2")*/
                // intentShare.type = "*/*"
                /*println("3")
                intentShare.addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                println("4")
                intentShare.putExtra(Intent.EXTRA_STREAM, file)
                println("5")
                startActivity(Intent.createChooser(intentShare, "Compartir Archivo..."))
                println("6")*/
            } catch (e: Exception) {
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun clearAdapter() {
        cargarCobranzas()
    }

    private fun validarVigenciaCxc() {
        keAndroid = conn.writableDatabase
        val tabla = "ke_precobranza"
        val columna = arrayOf("cxcndoc," + "fchvigen")
        val seleccion =
            "fchvigen < DATE('now') AND codvend='$codUsuario' AND empresa = '$codEmpresa' AND edorec != 3 AND edorec != 8"

        cursorCobranza = keAndroid.query(tabla, columna, seleccion, null, null, null, null)

        while (cursorCobranza.moveToNext()) {
            val idRecibo = cursorCobranza.getString(0)
            val cvEdo = ContentValues()
            cvEdo.put("edorec", "3")

            keAndroid.update(
                "ke_precobranza",
                cvEdo,
                "cxcndoc ='$idRecibo' AND empresa = '$codEmpresa'",
                null
            )
        }
    }

    private fun getFechaHoy(): String {
        val fechaHoy: String
        val fechaSinConvertir: Calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        fechaHoy = sdf.format(fechaSinConvertir.time)
        return fechaHoy
    }

    private fun compararFecha(fchrecibo: String): Int {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val current = LocalDateTime.now().format(formatter)

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val firstDate: Date = sdf.parse(current) as Date
        val secondDate: Date = sumarDias(
            conn.getConfigNum("APP_W_VIGENCIA_EFECTIVO", codEmpresa!!).toInt(),
            sdf.parse(fchrecibo) as Date
        )

        // vence > fecha = 1
        // vence = fecha = 0
        // vence < fecha = -1

        return firstDate.compareTo(secondDate)
    }

    private fun sumarDias(dias: Int, fecha: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = fecha
        calendar.add(Calendar.DAY_OF_YEAR, dias)
        return calendar.time
    }

    override fun getTheme(): Resources.Theme {
        val theme = super.getTheme()
        theme.applyStyle(setThemeAgencia(Constantes.AGENCIA), true)
        // you could also use a switch if you have many themes that could apply
        return theme
    }

    private fun setColors() {
        binding.apply {
            fbtAddcxc.backgroundTintList =
                ColorStateList.valueOf(fbtAddcxc.colorAgencia(Constantes.AGENCIA))
            fabAdddeposit.setBackgroundColor(fabAdddeposit.colorAgencia(Constantes.AGENCIA))
            fbtAddcxc.setRippleColor(ColorStateList.valueOf(fbtAddcxc.colorTextAgencia(Constantes.AGENCIA)))
            fbtAddcxc.imageTintList = fbtAddcxc.colorIconReclamo(Constantes.AGENCIA)
            fabAdddeposit.rippleColor =
                ColorStateList.valueOf(fabAdddeposit.colorTextAgencia(Constantes.AGENCIA))
        }
    }
}
