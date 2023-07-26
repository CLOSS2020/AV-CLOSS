package com.appcloos.mimaletin

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.ContentValues
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.roundToInt


class CXCActivity : AppCompatActivity() {
    //2023-06-19 Variable global para la activity Numerica que dice los dias que son vigentes antes de que el recibo de cobro en efectivo se anule por antiguedad (Valor default 4 dias)
    //private var APP_W_VIGENCIA_EFECTIVO : Double = 4.0

    private lateinit var fbt_addcxc: FloatingActionButton //var de floatacbt.
    private lateinit var rv_cxc: RecyclerView //variable para el RecyclerV.
    private lateinit var preferences: SharedPreferences // preferences para cargar los datos de la princ.
    private var cod_usuario: String? = ""
    private var codEmpresa: String? = ""
    lateinit var cursorCobranza: Cursor
    private lateinit var conn: AdminSQLiteOpenHelper
    lateinit var ke_android: SQLiteDatabase
    lateinit var listCobranza: ArrayList<CXC>
    lateinit var fab_adddep: ExtendedFloatingActionButton
    //2023-04-25 Se comenta el boton de recibo de cobro para que los recibos hechos sean a tras del planificador
    //lateinit var fab_newcxc: ExtendedFloatingActionButton

    //animaciones
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

    //bool
    private var clicked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cxcactivity)

        //instanciamiento del conector a la bdd
        conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null, 13)

        //APP_W_VIGENCIA_EFECTIVO = conn.getConfigNum("APP_W_VIGENCIA_EFECTIVO")

        /* val builder = StrictMode.VmPolicy.Builder()
         StrictMode.setVmPolicy(builder.build())
         builder.detectFileUriExposure()*/
        //declaracion del boton en el Layout
        fbt_addcxc = findViewById<FloatingActionButton>(R.id.fbt_addcxc)
        fab_adddep = findViewById<ExtendedFloatingActionButton>(R.id.fab_adddeposit)
        //2023-04-25 Se comenta el boton de recibo de cobro para que los recibos hechos sean a tras del planificador
        //fab_newcxc  = findViewById<ExtendedFloatingActionButton>(R.id.fab_addcxc)

        //dclaracion del RecyclerView
        rv_cxc = findViewById<RecyclerView>(R.id.rv_cxc)
        preferences = getSharedPreferences("Preferences", MODE_PRIVATE)
        cod_usuario = preferences.getString("cod_usuario", null)
        codEmpresa = preferences.getString("codigoEmpresa", null)
        listCobranza = ArrayList<CXC>()



        fbt_addcxc.setOnClickListener {
            onAddButtonClicked()
        }

        fab_adddep.setOnClickListener {
            iraCreacionDeposito()
        }
//2023-04-25 Se comenta el boton de recibo de cobro para que los recibos hechos sean a tras del planificador
        /*fab_newcxc.setOnClickListener {
            iraCreacionCobranza()
        }*/
        validarVigenciaCxc()
        cargarCobranzas()

        if (checkPermission()) {
            //Toast.makeText(this, "Permiso aceptado", Toast.LENGTH_SHORT).show()
        } else {
            requestPermissions()
        }

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
            200
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 200) {
            if (grantResults.size > 0) {
                val writeStorage = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val readStorage = grantResults[1] == PackageManager.PERMISSION_GRANTED

                if (writeStorage && readStorage) {
                    //Toast.makeText(this, "Permisos concedidos", Toast.LENGTH_SHORT).show()
                } else {
                    //Toast.makeText(this, "Permisos rechazados", Toast.LENGTH_SHORT).show()
                    //finish()
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
            //2023-04-25 Se comenta el boton de recibo de cobro para que los recibos hechos sean a tras del planificador
            //fab_newcxc.startAnimation(fromBottom)
            fab_adddep.startAnimation(fromBottom)
            fbt_addcxc.startAnimation(rotateOpen)

        } else {
            //2023-04-25 Se comenta el boton de recibo de cobro para que los recibos hechos sean a tras del planificador
            //fab_newcxc.startAnimation(toBottom)
            fab_adddep.startAnimation(toBottom)
            fbt_addcxc.startAnimation(rotateClose)
        }
    }

    private fun setVisibility(clicked: Boolean) {
        if (!clicked) {
            //2023-04-25 Se comenta el boton de recibo de cobro para que los recibos hechos sean a tras del planificador
            //fab_newcxc.visibility = View.VISIBLE
            fab_adddep.visibility = View.VISIBLE

        } else {
            //2023-04-25 Se comenta el boton de recibo de cobro para que los recibos hechos sean a tras del planificador
            //fab_newcxc.visibility = View.INVISIBLE
            fab_adddep.visibility = View.INVISIBLE

        }
    }

    //funcion para ir a la ventana de creacion de cobranza
    private fun iraCreacionCobranza() {
        val intent = Intent(this, creacionCobranzaActivity::class.java).apply {
            intent.putExtra("codigoUsuario", cod_usuario)
            intent.putExtra("codigoEmpresa", codEmpresa)
        }
        startActivity(intent)
    }

    private fun iraCreacionDeposito() {
        val intent = Intent(this, depositoActivity::class.java).apply {
            intent.putExtra("codigoUsuario", cod_usuario)
            intent.putExtra("codigoEmpresa", codEmpresa)
        }
        startActivity(intent)
    }

    //
    private fun cargarCobranzas() {
        listCobranza = ArrayList()
        listCobranza.clear()
        ke_android = conn.writableDatabase

        var tabla = "ke_precobranza"
        var columna = arrayOf(
            "cxcndoc," + "fchrecibo," + "edorec," + "bcomonto," + "efectivo, " + "moneda," + "tiporecibo",
            "bsretiva",
            "bsretflete"
        )
        //var seleccion = "codvend='${cod_usuario}' AND edorec != 3 AND edorec != 8"
        var seleccion = "codvend='${cod_usuario}'"

        cursorCobranza = ke_android.query(tabla, columna, seleccion, null, null, null, null)

        while (cursorCobranza.moveToNext()) {
            val cursor = ke_android.rawQuery(
                "SELECT ke_doccti.nombrecli FROM ke_doccti INNER JOIN ke_precobradocs ON ke_precobradocs.documento = ke_doccti.documento WHERE ke_precobradocs.cxcndoc = '${
                    cursorCobranza.getString(
                        0
                    )
                }';",
                null
            )

            cursor.moveToFirst()

            val cobranza: CXC = CXC()
            cobranza.id_recibo = cursorCobranza.getString(0)
            cobranza.fchrecibo = cursorCobranza.getString(1)
            cobranza.edorec = cursorCobranza.getString(2)
            cobranza.bcomonto = cursorCobranza.getDouble(3)
            cobranza.efectivo = cursorCobranza.getDouble(4)
            cobranza.moneda = cursorCobranza.getString(5)
            cobranza.tipoRecibo = cursorCobranza.getString(6)
            try {
                cobranza.cliente =
                    if (cobranza.tipoRecibo == "D") "Anexo de Deposito" else cursor.getString(0)
            } catch (e: Exception) {
                cobranza.cliente = "No identificado"
            }
            cobranza.bsretiva = cursorCobranza.getDouble(7)
            cobranza.bsretflete = cursorCobranza.getDouble(8)

            //2023-06-15 vericifarVijenciaCXC sirve para revisar los recibos de cobro W que sean en efectivo y de no ser cambiado su edorec (que se anexxe a un deposito) este sera anulado en los dias que tenga dentro
            cobranza.edorec = verificarVijenciaCXC(
                cobranza.fchrecibo,
                cobranza.efectivo,
                cobranza.id_recibo,
                cobranza.edorec
            )

            listCobranza.add(cobranza)
            cursor.close()

        }

        rv_cxc.layoutManager = LinearLayoutManager(this)
        val adapter = CXCAdapter(
            listCobranza,
            this,
            onClickListener = { codigoRecibo -> mensajeCXC(codigoRecibo) })
        rv_cxc.adapter = adapter
        adapter.notifyDataSetChanged()

    }

    private fun verificarVijenciaCXC(
        fchrecibo: String,
        efectivo: Double,
        idRecibo: String,
        edorec: String,
    ): String {
        return if ((compararFecha(fchrecibo) > 0) && (efectivo > 0) && (edorec != "9" && edorec != "10")) {
            println("entro")
            conn.UpReciboCobroStatus(idRecibo)
            "3"
        } else {
            edorec
        }
    }

    private fun mensajeCXC(codigoRecibo: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Opciones del Recibo")
        builder.setMessage("Recibo: $codigoRecibo")
        //builder.setPositiveButton("OK", DialogInterface.OnClickListener(function = x))

        builder.setPositiveButton("Detalles") { dialog, which ->
            irADestalleCXC(codigoRecibo)
        }

        val tabla = "ke_precobranza"
        val columna =
            arrayOf("edorec")
        val seleccion = "cxcndoc='${codigoRecibo}'"

        val cursorRecibo: Cursor =
            ke_android.query(tabla, columna, seleccion, null, null, null, null)

        var estado = ""

        if (cursorRecibo.moveToFirst()) {
            estado = cursorRecibo.getString(0)
        }

        cursorRecibo.close()

        //estado == "1" || estado == "10"
        if (estado == "0") {
            builder.setNegativeButton("Borrar") { dialog, which ->

                val subbuilder = AlertDialog.Builder(this)
                subbuilder.setTitle("Borrado de Recibo")
                subbuilder.setMessage("¿Está seguro de querer borrar el recibo $codigoRecibo?")

                subbuilder.setPositiveButton("Si") { dialog, which ->
                    borrarRecibo(codigoRecibo)
                }

                subbuilder.setNeutralButton("No") { dialog, which ->

                }

                subbuilder.show()
            }
        }

        /*builder.setNeutralButton("Generar Documento PDF") { dialog, which ->
            crearPDF(codigoRecibo)
        }*/
        builder.show()
    }

    private fun irADestalleCXC(codigoRecibo: String) {
        val editor: SharedPreferences.Editor = preferences.edit()
        editor.putString("recibo", codigoRecibo)
        editor.apply()
        val intent = Intent(this, DetalleCXCActivity::class.java)
        startActivity(intent)
    }

    private fun borrarRecibo(codigoRecibo: String) {
        ke_android.delete("ke_precobranza", "cxcndoc = '$codigoRecibo'", null)
        ke_android.delete("ke_precobradocs", "cxcndoc = '$codigoRecibo'", null)
        ke_android.delete("ke_retimg", "cxcndoc = '$codigoRecibo'", null)
        clearAdapter()
    }

    /*TODO: Este proceso debe colocarse solo para los recibos que ya esten subidos */
    private fun crearPDF(codigoRecibo: String) {

        val tabla = "ke_precobranza"
        val columna =
            arrayOf("edorec")
        val seleccion = "cxcndoc='${codigoRecibo}'"

        val cursorRecibo: Cursor =
            ke_android.query(tabla, columna, seleccion, null, null, null, null)

        var estado = ""

        if (cursorRecibo.moveToFirst()) {
            estado = cursorRecibo.getString(0)
        }

        cursorRecibo.close()

        //estado == "1" || estado == "10"
        if (estado == "1" || estado == "10") {
            val listaDocumentos = arrayListOf<String>()
            val nombreEmpresa = "COMERCIALIZADORA LA OCCIDENTAL, C.A."
            val rifEmpresa = "RIF: J-405584017"
            val dirEmpresa1 = "CALLE 18 CON AV GOAJIRA VIA EL MOJAN, LOCALGALPON 3, ZONA"
            val dirEmpresa2 = "INDUSTRIAL NORTE, COMPLEJO PARQUE INDUSTRIAL NORTE,"
            val dirEmpresa3 = "MARACAIBO ZULIA POSTAL 4001"
            val tipoDoc = "Precobranza"
            var subTipoDoc = ""
            var vendedorRecibo = ""
            val nombreCliente = arrayListOf<String>()
            val codCliente = arrayListOf<String>()
            var codigoCliente = ""
            var fechaRecibo = ""
            var montoRecibo = 0.00
            var numeroRecibo = ""
            var monedaRecibo = ""
            var tipoPago = ""
            var tasaDia = ""
            var ke_cxc_id = ""
            var fechaTasa = ""
            var moneda = ""
            var banco = ""
            var codBanco = ""
            var nomBanco = ""
            var refBanco = ""
            var totalCobrado = ""
            var monedaSigno = ""
            val tasaDoc = arrayListOf<Double>()
            val cobradoBS = arrayListOf<Double>()
            val cobradoDol = arrayListOf<Double>()
            val retenFleBS = arrayListOf<Double>()
            val retenIvaBS = arrayListOf<Double>()
            val retenRef = arrayListOf<String>()
            var ivaBS = arrayListOf<Double>()
            var fleteBS = arrayListOf<Double>()
            var reten = arrayListOf<Int>()
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
            var totalEfectivo = 0.0
            var tipoRecibo = ""
            val firstApiFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val format = SimpleDateFormat("dd/MM/yyy", Locale.getDefault())

            val tabla = "ke_precobranza"
            val columna =
                arrayOf("cxcndoc," + "codvend," + "fchrecibo," + "bcomonto," + "moneda," + "efectivo," + "tasadia," + "kecxc_id," + "moneda," + "bcocod," + "bcoref," + "bstotal," + "doltotal, tiporecibo, (bsretiva + bsretflete)")
            val seleccion = "cxcndoc='${codigoRecibo}'"

            val cursorRecibo: Cursor =
                ke_android.query(tabla, columna, seleccion, null, null, null, null)

            if (cursorRecibo.moveToFirst()) {


                vendedorRecibo = cursorRecibo.getString(1)
                monedaRecibo = cursorRecibo.getString(4)
                tasaDia = cursorRecibo.getString(6)
                ke_cxc_id = cursorRecibo.getString(7)
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
                        //totalCobrado = cursorRecibo.getString(11)
                        monedaSigno = "Bs."
                    }

                    "2" -> {
                        moneda = "Dólar"
                        //totalCobrado = cursorRecibo.getString(12)
                        monedaSigno = "$"
                    }

                    else -> {
                        moneda = "No identificado"
                        //totalCobrado = "No identificado"
                        monedaSigno = "N/I"
                    }
                }


                val tablaT = "kecxc_tasas"
                val columnaT = arrayOf("kecxc_fchyhora")
                val seleccionT = "kecxc_id='${ke_cxc_id}'"

                val cursorTasa: Cursor =
                    ke_android.query(tablaT, columnaT, seleccionT, null, null, null, null)

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


                //IF para validar si el documento fue pagado con efectivo o transferencia

                if (cursorRecibo.getDouble(5) > 0.0) {
                    //Llenando el valor total con el fectivo dado
                    //montoRecibo = cursorRecibo.getDouble(3)
                    tipoPago = "Efectivo"
                    totalCobrado = cursorRecibo.getString(5)

                } else if (cursorRecibo.getDouble(3) > 0.0) {
                    //Llenando el valor total con la transferencia realizada
                    //montoRecibo = cursorRecibo.getDouble(5)
                    tipoPago = "Transferencia"

                    totalCobrado = cursorRecibo.getString(3)

                } else if (cursorRecibo.getDouble(14) > 0.0) {
                    //Llenando el valor total con la transferencia realizada
                    //montoRecibo = cursorRecibo.getDouble(5)
                    tipoPago = "Retencion"

                    totalCobrado = cursorRecibo.getString(14)
                } else {
                    montoRecibo = 0.0
                }

                if (banco != "") {
                    val tablaBanco = "listbanc"
                    val columnaBanco = arrayOf("nombanco")
                    val seleccionBanco = "codbanco='${banco}'"

                    val cursorBanco: Cursor = ke_android.query(
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

                val cursorDocs = ke_android.rawQuery(
                    "SELECT ke_doccti.nombrecli, ke_precobradocs.documento, ke_doccti.codcliente, ke_doccti.tasadoc, ke_precobradocs.tnetodbs, ke_precobradocs.tnetoddol, ke_precobradocs.bsretiva, ke_precobradocs.bsretfte, ke_precobradocs.bsmtoiva, ke_precobradocs.bsmtofte, ke_precobradocs.refret, ke_precobradocs.refretfte, ke_precobradocs.reten, ke_precobradocs.prcdsctopp, ke_precobradocs.afavor, ke_precobradocs.kecxc_idd, ke_precobradocs.fchemiret, ke_precobradocs.fchemirfte  FROM ke_doccti INNER JOIN ke_precobradocs ON ke_precobradocs.documento = ke_doccti.documento WHERE ke_precobradocs.cxcndoc = '${codigoRecibo}';",
                    null
                )
                println("SELECT ke_doccti.nombrecli, ke_precobradocs.documento, ke_doccti.codcliente, ke_doccti.tasadoc, ke_precobradocs.tnetodbs, ke_precobradocs.tnetoddol, ke_precobradocs.bsretiva, ke_precobradocs.bsretfte, ke_precobradocs.bsmtoiva, ke_precobradocs.bsmtofte, ke_precobradocs.refret, ke_precobradocs.refretfte, ke_precobradocs.reten FROM ke_doccti INNER JOIN ke_precobradocs ON ke_precobradocs.documento = ke_doccti.documento WHERE ke_precobradocs.cxcndoc = '${codigoRecibo}';")
                while (cursorDocs.moveToNext()) {
                    listaDocumentos.add(cursorDocs.getString(1))
                    //nombreCliente.add(cursorDocs.getString(0))
                    codCliente.add(cursorDocs.getString(2))
                    tasaDoc.add(cursorDocs.getDouble(3))
                    cobradoBS.add(cursorDocs.getDouble(4))
                    cobradoDol.add(cursorDocs.getDouble(5))
                    retenIvaBS.add(cursorDocs.getDouble(6))
                    retenFleBS.add(cursorDocs.getDouble(7))
                    ivaBS.add(cursorDocs.getDouble(8))
                    fleteBS.add(cursorDocs.getDouble(9))

                    if (tipoRecibo == "R") {
                        retenRef.add(cursorDocs.getString(10))
                        retenRef.add(cursorDocs.getString(11))
                    }

                    reten.add(cursorDocs.getInt(12))
                    descuento.add(cursorDocs.getDouble(13))
                    aFavor.add(cursorDocs.getDouble(14))
                    docPrevio.add(cod_usuario + "-PRC-" + cursorDocs.getString(15))
                    emiRetIva.add(cursorDocs.getString(16))
                    emiRetFle.add(cursorDocs.getString(17))


                    /*if (tipoRecibo == "W"){
                        cobradoDol.add(cursorDocs.getDouble(5))
                    } else if (tipoRecibo == "D"){
                        cobradoDol.add(cursorDocs.getDouble(8))
                    }*/

                    //Para acortar el nombre del cliente
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
            //conf inicial de la pag
            val myInfo = PdfDocument.PageInfo.Builder(612, 792, 1).create()
            val pagina: PdfDocument.Page = reciboPDF.startPage(myInfo)
            val canvas = pagina.canvas
            //del obj paint
            paint.textAlign = Paint.Align.LEFT
            paint.textSize = 16f
            paint.color = Color.BLACK
            paint.typeface = Typeface.createFromAsset(this.assets, "font/arialbd.ttf")


            //CABECERA
            //imagen del la cabecera
            val bmp = BitmapFactory.decodeResource(this.resources, R.drawable.plantillasello)
            val scaledBitmap = Bitmap.createScaledBitmap(bmp, 612, 792, false)
            canvas.drawBitmap(scaledBitmap, 0f, 0f, paint)

            //ICONO DE LA EMPRESA
            val iconEmpresa = BitmapFactory.decodeResource(this.resources, R.drawable.clo_negro)
            val scaledBtmpEmpresa = Bitmap.createScaledBitmap(iconEmpresa, 100, 100, false)
            canvas.drawBitmap(scaledBtmpEmpresa, 5f, 15f, paint)


            //titulos de la cabecera
            canvas.drawText(nombreEmpresa, 105f, 35f, paint)

            //RIF Empresa
            paint.textSize = 10f
            canvas.drawText(rifEmpresa, 105f, 45f, paint)

            //Direccion Empresa
            paint.typeface = Typeface.createFromAsset(this.assets, "font/arial.ttf")
            canvas.drawText(dirEmpresa1, 105f, 55f, paint)
            canvas.drawText(dirEmpresa2, 105f, 65f, paint)
            canvas.drawText(dirEmpresa3, 105f, 75f, paint)

            //Tipo de documento
            paint.textAlign = Paint.Align.RIGHT
            paint.textSize = 15f
            paint.color = Color.rgb(7, 4, 97)
            paint.typeface = Typeface.createFromAsset(this.assets, "font/arialbd.ttf")
            canvas.drawText(tipoDoc, 597f, 60f, paint)

            //Subtipo de documento
            paint.textSize = 13f
            canvas.drawText(subTipoDoc, 597f, 75f, paint)

            //Numero de Rrecibo
            paint.color = Color.RED
            paint.textSize = 16f
            canvas.drawText("REC: $codigoRecibo", 597f, 100f, paint)

            //Fecha de creacion
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

            //Tasa
            if (tipoRecibo == "W") {
                paint.textSize = 14f
                canvas.drawText("Tasa: $tasaDia Bs./$", 597f, 130f, paint)
            }

            //Fecha Tasa
            if (tipoRecibo == "W") {
                canvas.drawText("de $fechaTasa", 597f, 145f, paint)
            }

            //Tipo de moneda
            if (tipoRecibo == "W") {
                paint.textSize = 12f
                canvas.drawText("Moneda del Recibo: $moneda", 597f, 160f, paint)
            } else if (tipoRecibo == "D" || tipoRecibo == "R") {
                paint.textSize = 12f
                canvas.drawText("Moneda del Recibo: $moneda", 597f, 130f, paint)
            }

            paint.textAlign = Paint.Align.LEFT
            //Marco de los datos del deposito/transferencia
            val iconMarco = BitmapFactory.decodeResource(this.resources, R.drawable.marco)
            val scaledBtmpMarco = Bitmap.createScaledBitmap(iconMarco, 360, 60, false)
            canvas.drawBitmap(scaledBtmpMarco, 15f, 126f, paint)

            paint.textSize = 11f
            paint.typeface = Typeface.createFromAsset(this.assets, "font/arialbd.ttf")
            canvas.drawText("[ Datos del Depósito o Transferencia ]", 30f, 131f, paint)

            //Datos del deposito/transferencia
            paint.textSize = 13f
            paint.typeface = Typeface.createFromAsset(this.assets, "font/arial.ttf")
            if (tipoRecibo == "R") {
                canvas.drawText(
                    "$codBanco $nomBanco ${if (retenRef[0].isEmpty()) "" else " Ret. IVA "}${if (retenRef[0].isEmpty() || retenRef[1].isEmpty()) "" else "/"}${if (retenRef[1].isEmpty()) "" else " Ret. Flete"}",
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
                    "${if (retenRef[0].isEmpty()) "" else " ${retenRef[0]} "}${if (retenRef[0].isEmpty() || retenRef[1].isEmpty()) "" else "/"}${if (retenRef[1].isEmpty()) "" else " ${retenRef[1]}"}",
                    75f,
                    170f,
                    paint
                )
                //canvas.drawText("${retenRef[0]} / ${retenRef[1]}", 75f, 170f, paint)

            }



            paint.textSize = 11f
            paint.typeface = Typeface.createFromAsset(this.assets, "font/arialbd.ttf")
            canvas.drawText("Recibo de Precobranza Generado y Subido", 30f, 220f, paint)


            paint.textSize = 14f
            paint.typeface = Typeface.createFromAsset(this.assets, "font/arial.ttf")
            canvas.drawText("Monto Total", 400f, 220f, paint)


            paint.typeface = Typeface.createFromAsset(this.assets, "font/arialbd.ttf")
            canvas.drawText("$monedaSigno $totalCobrado", 500f, 220f, paint)

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
                    canvas.drawText("Neto", 155f, 245f, paint)
                    canvas.drawText("IVA", 210f, 245f, paint)
                    canvas.drawText("Flete", 265f, 245f, paint)
                    canvas.drawText("Dto %", 320f, 245f, paint)
                    //canvas.drawText("A Favor", 375f, 245f, paint)
                }
            } else {
                canvas.drawText("Ret. IVA", 100f, 245f, paint)
                canvas.drawText("Ret. Flete", 155f, 245f, paint)
                canvas.drawText("Emi. Ret. I.", 215f, 245f, paint)
                canvas.drawText("Emi. Ret. F.", 285f, 245f, paint)
            }


            var counter = 265f


            //ESTAS INTENTANDO QUE LA SUMA DEL DOCUMENTO EN BS Y DOLARES ESTE BIEN EN LA MONEDA QUE SE SOLICITA
            for (i in listaDocumentos.indices) {
                val valorCobrado = if (moneda == "Dólar") cobradoDol[i] else cobradoBS[i]
                val ivaCobrado = validarMoneda(
                    moneda,
                    ivaBS[i] - verificarReten(reten[i], "cbsretiva", listaDocumentos[i]),
                    tasaDoc[i]
                )
                val fleteCobrado = validarMoneda(
                    moneda,
                    fleteBS[i] - verificarReten(reten[i], "cbsretflete", listaDocumentos[i]),
                    tasaDoc[i]
                )
                //val neto = redondeo(valorCobrado - ivaCobrado - fleteCobrado)
                val neto =
                    redondeo(redondeo(valorCobrado) - redondeo(ivaCobrado) - redondeo(fleteCobrado))

                //variables totaltes
                totalCobrad += valorCobrado
                totalNeto += neto
                totalIva += ivaCobrado
                totalFlete += fleteCobrado
                totalDescuento += descuento[i]
                totalAFavor += aFavor[i]
                totalRetFle += retenFleBS[i]
                totalRetIva += retenIvaBS[i]

                //solo aplica para anexos de deposito
                //totalEfectivo += conn.getEfectivoDoc(docPrevio[i])

                //println("En neto --> ${valorCobrado - (ivaBS[i] - abs(retenIvaBS[i])) - (fleteBS[i] - abs(retenFleBS[i]))}")
                //println("En neto --> $valorCobrado - (${ivaBS[i]} - abs(retenIvaBS[i]) - (fleteBS[i] - abs(retenFleBS[i])))")

                if (tipoRecibo != "R") {
                    canvas.drawText(listaDocumentos[i], 25f, counter, paint)


                    if (tipoRecibo == "D") {
                        canvas.drawText(
                            redondeo(valorCobrado).toString(),
                            100f,
                            counter,
                            paint
                        )
                        canvas.drawText(
                            "${codCliente[i]} ${nombreCliente[i]}",
                            155f,
                            counter,
                            paint
                        )
                    } else {
                        canvas.drawText(redondeo(valorCobrado).toString(), 100f, counter, paint)
                        canvas.drawText(neto.toString(), 155f, counter, paint)
                        canvas.drawText(redondeo(ivaCobrado).toString(), 210f, counter, paint)
                        canvas.drawText(redondeo(fleteCobrado).toString(), 265f, counter, paint)
                        canvas.drawText(redondeo(descuento[i]).toString(), 320f, counter, paint)
                        //canvas.drawText(redondeo(aFavor[i]).toString(), 375f, counter, paint)
                    }
                } else {
                    canvas.drawText(listaDocumentos[i], 25f, counter, paint)
                    canvas.drawText(retenIvaBS[i].toString(), 100f, counter, paint)
                    canvas.drawText(retenFleBS[i].toString(), 155f, counter, paint)
                    canvas.drawText(emiRetIva[i], 215f, counter, paint)
                    canvas.drawText(emiRetFle[i], 285f, counter, paint)
                }

                //canvas.drawText("${codCliente[i]} ${nombreCliente[i]}", 200f, counter, paint)
                counter = counter.plus(15f)
            }
            //canvas.drawBitmap(scaledBtmpLinea, 5f, counter - 13, paint)
            canvas.drawBitmap(scaledBtmpLineaD, 18f, counter - 13, paint)
            if (tipoRecibo == "W" || tipoRecibo == "R") {
                canvas.drawBitmap(scaledBtmpLinea, 5f, counter - 13 + 20, paint)
                canvas.drawText("Total", 25f, counter + 3, paint)
                if (tipoRecibo == "W") {
                    canvas.drawText(redondeo(totalCobrad).toString(), 100f, counter + 3, paint)
                    canvas.drawText(redondeo(totalNeto).toString(), 155f, counter + 3, paint)
                    canvas.drawText(redondeo(totalIva).toString(), 210f, counter + 3, paint)
                    canvas.drawText(redondeo(totalFlete).toString(), 265f, counter + 3, paint)
                    canvas.drawText(
                        redondeo(totalDescuento / listaDocumentos.size).toString(),
                        320f,
                        counter + 3,
                        paint
                    )
                    canvas.drawText(
                        "Saldo a Favor: ${redondeo(totalAFavor)}",
                        375f,
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

                if (tipoRecibo == "W") {
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
                }

            } else {
                canvas.drawBitmap(scaledBtmpLinea, 5f, counter - 13 + 20, paint)
                canvas.drawText("Total", 25f, counter + 3, paint)
                canvas.drawText(redondeo(totalCobrad).toString(), 100f, counter + 3, paint)
            }


            if (tipoRecibo == "W" && reten[0] == 0) {
                paint.textSize = 14f
                paint.typeface = Typeface.createFromAsset(this.assets, "font/arialbd.ttf")
                canvas.drawText(
                    "Los montos vistos en el detalle se le están aplicando retenciones",
                    25f,
                    730f,
                    paint
                )
            }

            paint.textSize = 14f
            paint.typeface = Typeface.createFromAsset(this.assets, "font/arialbd.ttf")
            canvas.drawText(
                "*Va sin Enmienda*",
                25f,
                760f,
                paint
            )


            //NOMBRE DEL CLIENTE retencion
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
            numeroRecibo =
                "RECIBO_NRO_$codigoRecibo.pdf" //este sera el nombre del documento al momento de crearlo y guardarlo en el almacenamiento


            val path = getExternalFilesDir(null)!!.absoluteFile.toString() + "/" + numeroRecibo
            val file = File(path)

            val ruta =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                    .toString()

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
        if (reten == 1) {
            return 0.0
        } else {
            val cursor = ke_android.rawQuery(
                "SELECT $campo FROM ke_doccti WHERE documento = '$documento'",
                null
            )
            val regreso: Double
            if (cursor.moveToFirst()) {
                regreso = cursor.getDouble(0)
            } else {
                regreso = 0.0
            }
            cursor.close()
            return regreso
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
        //val ruta = "$rutaRaiz/$nombreArchivo"
        //println("La ruta -> $ruta")
        //val file = File(ruta.substring(1))

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

                val sharingFile: File = File(file.path)

                val outputPdfUri = FileProvider.getUriForFile(
                    this,
                    this.packageName + ".provider",
                    file
                )

                shareIntent.putExtra(Intent.EXTRA_STREAM, outputPdfUri)

                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                //Write Permission might not be necessary
                //Write Permission might not be necessary
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
                //intentShare.type = "*/*"
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
                println("error")
                println("Error -> $e")
            }
        }


    }


    private fun clearAdapter() {
        cargarCobranzas()
    }

    private fun validarVigenciaCxc() {
        ke_android = conn.writableDatabase
        var tabla = "ke_precobranza"
        var columna = arrayOf("cxcndoc," + "fchvigen")
        var seleccion =
            "fchvigen < DATE('now') AND codvend='${cod_usuario}' AND edorec != 3 AND edorec != 8"

        cursorCobranza = ke_android.query(tabla, columna, seleccion, null, null, null, null)

        while (cursorCobranza.moveToNext()) {
            var idRecibo = cursorCobranza.getString(0)
            var cvEdo: ContentValues = ContentValues()
            cvEdo.put("edorec", "3")

            ke_android.update("ke_precobranza", cvEdo, "cxcndoc ='$idRecibo}'", null)
        }
    }


    private fun getFechaHoy(): String {
        var fechaHoy: String
        var fechaSinConvertir: Calendar = Calendar.getInstance()
        var sdf: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        fechaHoy = sdf.format(fechaSinConvertir.time)
        return fechaHoy
    }


    override fun onResume() {
        super.onResume()
        //clearAdapter()


    }


    fun compararFecha(fchrecibo: String): Int {

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val current = LocalDateTime.now().format(formatter)

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val firstDate: Date = sdf.parse(current) as Date
        val secondDate: Date = sumarDias(
            conn.getConfigNum("APP_W_VIGENCIA_EFECTIVO").toInt(),
            sdf.parse(fchrecibo) as Date
        )

        //vence > fecha = 1
        //vence = fecha = 0
        //vence < fecha = -1

        println(firstDate.compareTo(secondDate))

        return firstDate.compareTo(secondDate)

    }

    private fun sumarDias(dias: Int, fecha: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = fecha
        calendar.add(Calendar.DAY_OF_YEAR, dias)
        return calendar.time
    }


}