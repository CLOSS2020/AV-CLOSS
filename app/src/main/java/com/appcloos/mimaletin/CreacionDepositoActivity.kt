package com.appcloos.mimaletin

import android.content.ContentValues
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.appcloos.mimaletin.databinding.ActivityCreacionDepositoBinding
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale


class CreacionDepositoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreacionDepositoBinding
    private lateinit var preferences: SharedPreferences // preferences para cargar los datos de la princ.
    private var codUsuario: String? = ""
    private var codEmpresa: String? = ""

    private lateinit var conn: AdminSQLiteOpenHelper
    private lateinit var keAndroid: SQLiteDatabase
    private lateinit var nroDeposito: String

    private var codigoBancoDep = ""
    private var correlativoTexto: String = ""
    var nroCorrelativo = 0
    var tipoDoc = "PRC"
    private var sumaTotal = 0.00
    private var fechaActual = ""

    private lateinit var listaBancosDep: ArrayList<Bancos>
    private lateinit var recibosSelecc: ArrayList<String>
    private lateinit var listaInfoBancos: ArrayList<String>
    private lateinit var listaRecibos: ArrayList<CXC>

    var enlaceEmpresa = ""
    var codigoSucursal = ""
    var nombreEmpresa = ""
    var fecha_auxiliar = "0000-00-00"
    private var fechaDep = ""

    private var listaImagenes: MutableList<Uri> = mutableListOf()

    private var requestCodeImg = random()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreacionDepositoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Edicion de los colores del Bar de arriba de notificacion de las app y el bar de abajo de los 3 botones


        conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null)
        keAndroid = conn.writableDatabase

        preferences = getSharedPreferences("Preferences", MODE_PRIVATE)
        codUsuario = preferences.getString("cod_usuario", null)
        codEmpresa = preferences.getString("codigoEmpresa", null)

        windowsColor(Constantes.AGENCIA)
        setColors()

        cargarEnlace()

        listaBancosDep = ArrayList()
        recibosSelecc = ArrayList()
        listaInfoBancos = ArrayList()
        listaRecibos = ArrayList()
        recibosSelecc = intent.getStringArrayListExtra("listRecibos") as ArrayList<String>

        // -- query de los bancos
        //getBancos("USD")
        cargarBancosDep("USD")
        // --

        sumarSaldos(recibosSelecc)


        val cursorCorrelativo = keAndroid.rawQuery(
            "SELECT MAX(kcor_numero) FROM ke_corprec WHERE kcor_vendedor ='$codUsuario' AND empresa = '$codEmpresa'",
            null
        )
        //----
        if (cursorCorrelativo.moveToFirst()) {
            nroCorrelativo = cursorCorrelativo.getInt(0)
            nroCorrelativo += 1
            correlativoTexto = nroCorrelativo.toString()
            correlativoTexto = "0000$correlativoTexto"

        } else {
            nroCorrelativo = cursorCorrelativo.getInt(0)
            nroCorrelativo += 1
            correlativoTexto = nroCorrelativo.toString()
            correlativoTexto = "0000$correlativoTexto"
        }
        cursorCorrelativo.close()
        //generacion del correlativo completo
        nroDeposito = generarNroPrecobranza()
        fechaActual = getFechaHoy()
        fechaDep = fechaActual

        supportActionBar?.title = "REC: $nroDeposito"
        recibosSelecc.joinToString(separator = ",")

        binding.spDepBanco.setOnItemClickListener { parent, view, position, id ->
            if (position != 0) {
                codigoBancoDep = listaBancosDep[position - 1].codbanco

                if (listaBancosDep[position - 1].codbanco == "100") {
                    val editable: Editable = SpannableStringBuilder(nroDeposito)
                    binding.etDepRef.text = editable
                    binding.etDepRef.isEnabled = false
                } else {
                    val editable: Editable = SpannableStringBuilder("")
                    binding.etDepRef.text = editable
                    binding.etDepRef.isEnabled = true
                }


            } else if (position == 0) {
                codigoBancoDep = ""
            }
        }

        binding.btDepProc.setOnClickListener {
            procesarDeposito()
        }

        binding.btnFoto.setOnClickListener {
            listaImagenes.clear()
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_GET_CONTENT
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                type = "image/jpeg"
            }

            val shareIntent = Intent.createChooser(sendIntent, "SELECCIONA LAS IMAGENES")
            startActivityForResult(shareIntent, requestCodeImg)
        }

        binding.btnFoto.setOnLongClickListener {
            if (listaImagenes.isNotEmpty()) {
                dialogImg()
            }
            true
        }
    }

    private fun setColors() {

        binding.apply {
            tvDepMontot.setDrawableCobranzaAgencia(Constantes.AGENCIA)
            btDepProc.setBackgroundColor(btDepProc.colorButtonAgencia(Constantes.AGENCIA))

            btnFoto.setColorModelVariant(Constantes.AGENCIA)

            tilDepSpbanco.setColorModel(Constantes.AGENCIA)
            tilDepRef.setColorModel(Constantes.AGENCIA)

        }
    }

    private fun getFechaHoy(): String {
        val fechaHoy: String
        val fechaSinConvertir: Calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        fechaHoy = sdf.format(fechaSinConvertir.time)
        return fechaHoy
    }


    private fun procesarDeposito() {

        var llCommit = false

        val listaCabeceraNueva: ArrayList<CXC> = ArrayList()
        val listaLineasNueva: ArrayList<CXC> = ArrayList()

        val referenciaBanco = binding.etDepRef.text.toString().uppercase()
        val montoTotal: Double = binding.tvDepMontot.text.toString().toDouble()

        val contadorImg = 1

        if (listaImagenes.size < contadorImg) {
            toast("Debe incluir un minimo de $contadorImg imágen")
            return
        }

        //valido la fecha
        if (fechaDep.isEmpty()) {
            Toast.makeText(this, "Debe elegir la fecha del depósito", Toast.LENGTH_SHORT).show()
            return
        }

        //valido el banco
        if (codigoBancoDep.isEmpty()) {
            Toast.makeText(this, "Debe elegir el banco", Toast.LENGTH_SHORT).show()
            return
        }

        // valido la referencia del depósito
        if (referenciaBanco.isEmpty()) {
            Toast.makeText(this, "Debe introducir la referencia del deposito", Toast.LENGTH_SHORT)
                .show()
            return
        }

        //2023-07-06 Verificacion de referencia bancaria en deposito
        var numVerificador = 0
        //numVerificador += verificacionReferencia(referenciaBanco, "ke_precobranza", codigoBancoDep)
        numVerificador += verificacionReferencia(referenciaBanco, "ke_referencias", codigoBancoDep)

        if (numVerificador != 0) {
            Toast.makeText(this, "Referencia y banco utilizados previamente.", Toast.LENGTH_SHORT)
                .show()
            return
        }

        val reten = ArrayList<String>()

        val dep = CXC()
        dep.id_recibo = nroDeposito
        dep.tipoRecibo = "D"
        dep.codigoVend = codUsuario.toString()
        dep.fchrecibo = fechaDep
        dep.clicontesp = "" //esto lo jalo de  la lista de docs?
        dep.moneda = "2"
        dep.bcomonto = montoTotal
        dep.bcoref = referenciaBanco
        dep.bcocod = codigoBancoDep
        dep.edorec = "0"
        dep.fchhr = fechaActual


        //RECORRO LAS LINEAS DE LOS NUEVOS DOCS.
        //for(i in listaRecibos.indices){
        val cursorH: Cursor = keAndroid.rawQuery(
            "SELECT * FROM ke_precobradocs WHERE empresa = '$codEmpresa' AND cxcndoc IN (" + recibosSelecc.toString()
                .replace("[", "").replace("]", "") + ")", null
        )

        while (cursorH.moveToNext()) {
            val linea = CXC()
            /*linea.id_recibo = nroDeposito
            linea.agencia    =  cursorH.getString(1)
            linea.tipodoc    =  cursorH.getString(2)
            linea.documento  =  cursorH.getString(3)
            linea.bscobro    =  cursorH.getDouble(4)
            linea.nroret     =  cursorH.getString(5)
            linea.fchemiret  =  cursorH.getString(6)
            linea.bsretiva   =  cursorH.getDouble(7)
            linea.refret     =  cursorH.getString(8)
            linea.nroretfte  =  cursorH.getString(9)
            linea.fchemirfte =  cursorH.getString(10)
            linea.bsretfte   =  cursorH.getDouble(11)
            linea.refretfte  =  cursorH.getString(12)
            linea.bsmtoiva   =  cursorH.getDouble(13)
            linea.retmun_bi  =  cursorH.getDouble(14)
            linea.retmun_cod =  cursorH.getString(15)
            linea.retmun_nro =  cursorH.getString(16)
            linea.retmun_mto =  cursorH.getDouble(17)
            linea.retmun_fch =  cursorH.getString(18)
            linea.retmun_ref =  cursorH.getString(19)
            linea.fchrecibo  =  cursorH.getString(20)
            linea.kecxc_id   =  right(cursorH.getString(0), 8)
            linea.tasadia    =  cursorH.getDouble(22)
            linea.tnetoddol  =  cursorH.getDouble(23)*/

            linea.id_recibo = nroDeposito
            linea.agencia = cursorH.getString(1)
            linea.tipodoc = cursorH.getString(2)
            linea.documento = cursorH.getString(3)
            linea.bscobro = cursorH.getDouble(4)
            linea.prccobro = cursorH.getDouble(5)
            linea.prcdsctopp = cursorH.getDouble(6)
            linea.nroret = cursorH.getString(7)
            linea.fchemiret = cursorH.getString(8)
            linea.bsretiva = cursorH.getDouble(9)
            linea.refret = cursorH.getString(10)
            linea.nroretfte = cursorH.getString(11)
            linea.fchemirfte = cursorH.getString(12)
            linea.bsmtofte = cursorH.getDouble(13)
            linea.bsretfte = cursorH.getDouble(14)
            linea.refretfte = cursorH.getString(15)
            linea.pidvalid = cursorH.getString(16)
            linea.bsmtoiva = cursorH.getDouble(17)
            linea.retmun_bi = cursorH.getDouble(18)
            linea.retmun_cod = cursorH.getString(19)
            linea.retmun_nro = cursorH.getString(20)
            linea.retmun_mto = cursorH.getDouble(21)
            linea.retmun_fch = cursorH.getString(22)
            linea.retmun_ref = cursorH.getString(23)
            linea.diascalc = cursorH.getDouble(24)
            linea.prccomiv = cursorH.getDouble(25)
            linea.prccomic = cursorH.getDouble(26)
            linea.cxcndoc_aux = cursorH.getString(27)
            linea.tnetodbs = cursorH.getDouble(28)
            linea.tnetoddol = cursorH.getDouble(29)
            linea.fchrecibo = cursorH.getString(30)
            linea.kecxc_id = right(cursorH.getString(0), 8)
            linea.tasadia = cursorH.getDouble(32)
            linea.afavor = cursorH.getDouble(33)
            reten.add(cursorH.getString(34))

            listaLineasNueva.add(linea)
        }
        cursorH.close()

        //}

        //estos dependen de las lineas totales
        dep.bsneto = valorReal(listaRecibos.sumOf { it.bsneto })
        dep.bsretiva = valorReal(listaRecibos.sumOf { it.bsretiva })
        dep.bsiva = valorReal(listaRecibos.sumOf { it.bsiva })
        dep.bsflete = valorReal(listaRecibos.sumOf { it.bsflete })
        dep.dolflete = valorReal(listaRecibos.sumOf { it.dolflete })
        dep.bstotal = valorReal(listaRecibos.sumOf { it.bstotal })
        dep.dolneto = valorReal(listaRecibos.sumOf { it.dolneto })
        dep.doliva = valorReal(listaRecibos.sumOf { it.doliva })
        dep.doltotal = valorReal(listaRecibos.sumOf { it.doltotal })
        dep.netocob = valorReal(listaRecibos.sumOf { it.netocob })
        dep.bsretflete = valorReal(listaRecibos.sumOf { it.bsretflete })
        dep.retmun_sbi = valorReal(listaRecibos.sumOf { it.retmun_sbi })
        dep.retmun_sbs = valorReal(listaRecibos.sumOf { it.retmun_sbi })
        val fechaVigen = fechaSuma(fechaActual, 15)
        dep.fchvigen = fechaVigen
        dep.moneda = "2"
        dep.tasadia = 0.00
        listaCabeceraNueva.add(dep)

        // proceso de inserción
        try {
            // inicio la transacción
            keAndroid.beginTransaction()

            val qcabecera = ContentValues()
            val qlineas = ContentValues()

            for (i in listaCabeceraNueva.indices) {
                qcabecera.put("cxcndoc", listaCabeceraNueva[i].id_recibo)
                qcabecera.put("tiporecibo", listaCabeceraNueva[i].tipoRecibo)
                qcabecera.put("codvend", listaCabeceraNueva[i].codigoVend)
                qcabecera.put("tiporecibo", listaCabeceraNueva[i].tipoRecibo)
                qcabecera.put("fchrecibo", listaCabeceraNueva[i].fchrecibo)
                qcabecera.put("clicontesp", listaCabeceraNueva[i].clicontesp)
                qcabecera.put("bsneto", listaCabeceraNueva[i].bsneto)
                qcabecera.put("bsiva", listaCabeceraNueva[i].bsiva)
                qcabecera.put("bsretiva", listaCabeceraNueva[i].bsretiva)
                qcabecera.put("bsflete", listaCabeceraNueva[i].bsflete)
                qcabecera.put("bstotal", listaCabeceraNueva[i].bstotal)
                qcabecera.put("dolneto", listaCabeceraNueva[i].dolneto)
                qcabecera.put("doliva", listaCabeceraNueva[i].doliva)
                qcabecera.put("dolretiva", listaCabeceraNueva[i].dolretiva)
                qcabecera.put("dolflete", listaCabeceraNueva[i].dolflete)
                qcabecera.put("doltotal", listaCabeceraNueva[i].doltotal)
                qcabecera.put("moneda", listaCabeceraNueva[i].moneda)
                qcabecera.put("netocob", listaCabeceraNueva[i].netocob)
                //qcabecera.put("efectivo", listaCabeceraNueva[i].efectivo)
                qcabecera.put("bcocod", listaCabeceraNueva[i].bcocod)
                qcabecera.put("bcomonto", listaCabeceraNueva[i].bcomonto)
                qcabecera.put("bcoref", listaCabeceraNueva[i].bcoref)
                qcabecera.put("edorec", listaCabeceraNueva[i].edorec)
                qcabecera.put("fchvigen", listaCabeceraNueva[i].fchvigen)
                qcabecera.put("bsretflete", listaCabeceraNueva[i].bsretflete)
                qcabecera.put("fechamodifi", getFechaHoy())
                qcabecera.put("empresa", codEmpresa)

                for (j in listaLineasNueva.indices) {
                    qlineas.put("cxcndoc", listaLineasNueva[j].id_recibo)
                    qlineas.put("agencia", listaLineasNueva[j].agencia)
                    qlineas.put("tipodoc", listaLineasNueva[j].tipodoc)
                    qlineas.put("documento", listaLineasNueva[j].documento)
                    qlineas.put("bscobro", listaLineasNueva[j].bscobro)
                    qlineas.put("prcdsctopp", listaLineasNueva[j].prcdsctopp)
                    qlineas.put("nroret", listaLineasNueva[j].nroret)
                    qlineas.put("fchemiret", listaLineasNueva[j].fchemiret)
                    qlineas.put("bsretiva", listaLineasNueva[j].bsretiva)
                    qlineas.put("refret", listaLineasNueva[j].refret)
                    qlineas.put("nroretfte", listaLineasNueva[j].nroretfte)
                    qlineas.put("fchemirfte", listaLineasNueva[j].fchemirfte)
                    qlineas.put("bsmtofte", listaLineasNueva[j].bsmtofte)
                    qlineas.put("bsretfte", listaLineasNueva[j].bsretfte)
                    qlineas.put("refretfte", listaLineasNueva[j].refretfte)
                    qlineas.put("bsmtoiva", listaLineasNueva[j].bsmtoiva)
                    qlineas.put("fchrecibod", listaLineasNueva[j].fchrecibo)
                    qlineas.put("kecxc_idd", listaLineasNueva[j].kecxc_id)
                    qlineas.put("tasadiad", listaLineasNueva[j].tasadia)
                    qlineas.put("tnetoddol", listaLineasNueva[j].tnetoddol)
                    qlineas.put("afavor", listaLineasNueva[j].afavor)
                    qlineas.put("reten", reten[j])
                    val cliente = conn.getCampoStringCamposVarios(
                        "ke_doccti",
                        "codcliente",
                        listOf("documento", "empresa"),
                        listOf(listaLineasNueva[j].documento, codEmpresa!!)
                    )
                    qlineas.put("codcliente", cliente)
                    qlineas.put(
                        "nombrecli",
                        conn.getCampoStringCamposVarios(
                            "cliempre",
                            "nombre",
                            listOf("codigo", "empresa"),
                            listOf(cliente, codEmpresa!!)
                        )
                    )
                    qlineas.put(
                        "tasadoc",
                        conn.getCampoDoubleCamposVarios(
                            "ke_doccti",
                            "tasadoc",
                            listOf("documento", "empresa"),
                            listOf(listaLineasNueva[j].documento, codEmpresa!!)
                        )
                    )
                    //2023-10-19 esto es mal pero debido a la falta de tiempo va asi
                    qlineas.put(
                        "cbsretiva",
                        conn.getCampoDoubleCamposVarios(
                            "ke_doccti",
                            "cbsretiva",
                            listOf("documento", "empresa"),
                            listOf(listaLineasNueva[j].documento, codEmpresa!!)
                        )
                    )
                    qlineas.put(
                        "cbsretflete",
                        conn.getCampoDoubleCamposVarios(
                            "ke_doccti",
                            "cbsretflete",
                            listOf("documento", "empresa"),
                            listOf(listaLineasNueva[j].documento, codEmpresa!!)
                        )
                    )
                    qlineas.put("empresa", codEmpresa)

                    //qlineas.put("monto_aux_pdf", listaRecibos[j].efectivo)
                    keAndroid.insert("ke_precobradocs", null, qlineas)
                }

                keAndroid.insert("ke_precobranza", null, qcabecera)

                conn.saveImg(
                    listaImagenes,
                    nroDeposito,
                    this,
                    codEmpresa!!
                ) // <-- Guardando imagenes

                val qcorrelativo = ContentValues()
                qcorrelativo.put("kcor_numero", nroCorrelativo)
                qcorrelativo.put("kcor_vendedor", codUsuario)
                qcorrelativo.put("empresa", codEmpresa)

                keAndroid.insert("ke_corprec", null, qcorrelativo)

                //mato los recibos
                val estado = ContentValues()
                estado.put("edorec", "9")
                estado.put("fechamodifi", getFechaHoy())
                estado.put("reci_doc", right(listaCabeceraNueva[i].id_recibo, 8))
                keAndroid.update(
                    "ke_precobranza",
                    estado,
                    "empresa = '$codEmpresa' AND cxcndoc IN (" + recibosSelecc.toString()
                        .replace("[", "")
                        .replace("]", "") + ")",
                    null
                )


                llCommit = true

            }

        } catch (exception: SQLException) {
            println(exception.message)
            llCommit = false

            keAndroid.endTransaction()
            if (!llCommit) {
                return
            }
        }

        if (llCommit) {
            keAndroid.setTransactionSuccessful()
            keAndroid.endTransaction()
            val listadatos: ArrayList<CXC> = ArrayList()
            listadatos.add(dep)
            /* crear comprobante de deposito  */

            val dialog = DialogAnexo()
            dialog.DialogAnexo(this, listadatos, codEmpresa)

            Toast.makeText(this, "Depósito creado", Toast.LENGTH_SHORT).show()

        } else {
            keAndroid.endTransaction()
        }

    }

    private fun valorReal(monto: Double): Double {
        var valor: Double = monto
        valor = Math.round(valor * 100.00) / 100.00
        return valor
    }


    private fun fechaSuma(fechaOld: String, cantDias: Long): String {
        val fechaNueva: String

        // de string a fecha
        val fechaActual: String = fechaOld
        val fechaNow =
            LocalDate.parse(fechaActual, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        val fechaNew = fechaNow.plusDays(cantDias)

        // de fecha a String (la nueva)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        fechaNueva = fechaNew.format(formatter)

        return fechaNueva
    }


    private fun sumarSaldos(listaRecibosSel: ArrayList<String>) {
        sumaTotal = 0.00
        listaRecibos = ArrayList()

        var cobranza: CXC
        val query = arrayOf(
            "cxcndoc," + "efectivo," + "bsneto," + "bsretiva," + "bsiva," + "bsflete," + "dolflete," +
                    "bstotal," + "dolneto," + "doliva," + "doltotal," + "netocob," + "bsretflete," +
                    "retmun_sbi," + "retmun_sbs"
        )
        val tabla = "ke_precobranza"
        val condicion =
            "empresa = '$codEmpresa' AND cxcndoc IN (" + listaRecibosSel.toString().replace("[", "")
                .replace("]", "") + ")"

        val cursorRec: Cursor = keAndroid.query(tabla, query, condicion, null, null, null, null)

        while (cursorRec.moveToNext()) {
            cobranza = CXC()
            cobranza.id_recibo = cursorRec.getString(0)
            cobranza.efectivo = cursorRec.getDouble(1)
            cobranza.bsneto = cursorRec.getDouble(2)
            cobranza.bsretiva = cursorRec.getDouble(3)
            cobranza.bsiva = cursorRec.getDouble(4)
            cobranza.bsflete = cursorRec.getDouble(5)
            cobranza.dolflete = cursorRec.getDouble(6)
            cobranza.bstotal = cursorRec.getDouble(7)
            cobranza.dolneto = cursorRec.getDouble(8)
            cobranza.doliva = cursorRec.getDouble(9)
            cobranza.doltotal = cursorRec.getDouble(10)
            cobranza.netocob = cursorRec.getDouble(11)
            cobranza.bsretflete = cursorRec.getDouble(12)
            cobranza.retmun_sbi = cursorRec.getDouble(13)
            cobranza.retmun_sbs = cursorRec.getDouble(14)
            cobranza.cliente = conn.getCampoStringCamposVarios(
                "ke_precobradocs",
                "nombrecli",
                arrayListOf("cxcndoc", "empresa"),
                arrayListOf(cobranza.id_recibo, codEmpresa!!)
            )
            listaRecibos.add(cobranza)

        }
        cursorRec.close()
        //coloco la suma total del monto de los recs.
        sumaTotal = listaRecibos.sumOf { it.efectivo }
        binding.tvDepMontot.text = sumaTotal.toString()


        binding.rvContenidoDep.layoutManager = LinearLayoutManager(this)
        val adapter = RecsAdapter()
        adapter.RecsAdapter(this, listaRecibos)
        binding.rvContenidoDep.adapter = adapter

    }

    private fun cargarEnlace() {
        keAndroid = conn.writableDatabase
        val columnas = arrayOf("kee_nombre," + "kee_url," + "kee_sucursal")
        val cursorE: Cursor
        val condicion = "kee_codigo ='$codEmpresa'"
        cursorE = keAndroid.query("ke_enlace", columnas, condicion, null, null, null, null)

        while (cursorE.moveToNext()) {
            nombreEmpresa = cursorE.getString(0)
            enlaceEmpresa = cursorE.getString(1)
            codigoSucursal = cursorE.getString(2)
        }
        cursorE.close()
    }


    private fun getBancos(monedaBanco: String) {
        //descargarBancos("https://"+ enlaceEmpresa + "/webservice/bancos.php?fecha_sinc=" + fecha_auxiliar.trim() + "&&agencia=" + codigoSucursal.trim(), monedaBanco)
    }

    private fun descargarBancos(URL: String, monedaBanco: String) {

        var llCommit: Boolean

        var codbanco: String
        var nombanco: String
        var cuentanac: Double
        var inactiva = 0.00
        var fechamodifiBan = ""

        conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null)
        keAndroid = conn.readableDatabase

        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET, // method
            URL, // url
            null, // json request
            { response -> // response listener
                if (response != null) {
                    llCommit = false
                    keAndroid.beginTransaction()
                    var jsonObject: JSONObject?
                    try {

                        // loop through the array elements
                        for (i in 0 until response.length()) {
                            jsonObject = response.getJSONObject(i)
                            codbanco = jsonObject.getString("codbanco")
                            nombanco = jsonObject.getString("nombanco")
                            cuentanac = jsonObject.getDouble("cuentanac")
                            inactiva = jsonObject.getDouble("inactiva")
                            fechamodifiBan = jsonObject.getString("fechamodifi")

                            val qBancos = ContentValues()
                            qBancos.put("codbanco", codbanco)
                            qBancos.put("nombanco", nombanco)
                            qBancos.put("cuentanac", cuentanac)
                            qBancos.put("empresa", codEmpresa)

                            val qcodigoLocal: Cursor = keAndroid.rawQuery(
                                "SELECT count(codbanco) FROM listbanc " +
                                        "WHERE codbanco ='$codbanco' AND empresa = '$codEmpresa'",
                                null
                            )
                            qcodigoLocal.moveToFirst()
                            //variable para obtener el conteo de documentos que ya esten en el telf
                            val codigoExistente = qcodigoLocal.getInt(0)
                            qcodigoLocal.close()

                            if (codigoExistente > 0) {
                                keAndroid.update(
                                    "listbanc",
                                    qBancos,
                                    "codbanco= ? AND empresa = ?",
                                    arrayOf(codbanco, codEmpresa!!)
                                )
                            } else if (codigoExistente == 0) {
                                keAndroid.insert("listbanc", null, qBancos)
                            }
                            llCommit = true

                        }

                    } catch (ex: Exception) {
                        println("--Error--")
                        ex.printStackTrace()
                        llCommit = false
                        if (!llCommit) return@JsonArrayRequest
                    }
                    if (llCommit) {
                        keAndroid.setTransactionSuccessful()
                        keAndroid.endTransaction()
                        cargarBancosDep(monedaBanco)


                    } else if (!llCommit) {
                        keAndroid.endTransaction()
                    }
                }
            },
            { error -> // error listener
                println("--Error--")
                error.printStackTrace()
            }
        )
        val requestQueue: RequestQueue = Volley.newRequestQueue(applicationContext)
        requestQueue.add(jsonArrayRequest)
        cargarBancosDep(monedaBanco)

    }

    private fun cargarBancosDep(monedaSelec: String) {
        listaInfoBancos.clear()
        listaBancosDep.clear()

        keAndroid = conn.writableDatabase
        var bancos: Bancos
        var moneda = 0.00

        if (monedaSelec == "USD") {
            moneda = 2.00

        } else if (monedaSelec == "BSS") {
            moneda = 1.00
        }

        val cursorBancos: Cursor = keAndroid.rawQuery(
            "SELECT DISTINCT codbanco, nombanco,cuentanac, inactiva, fechamodifi FROM listbanc " +
                    "WHERE inactiva = 0 AND cuentanac = $moneda AND empresa = '$codEmpresa'",
            null
        )
        while (cursorBancos.moveToNext()) {
            bancos = Bancos()
            bancos.codbanco = cursorBancos.getString(0)
            bancos.nombanco = cursorBancos.getString(1)
            bancos.cuentanac = cursorBancos.getDouble(2)
            bancos.inactiva = cursorBancos.getDouble(3)
            bancos.fechamodifi = cursorBancos.getString(4)
            listaBancosDep.add(bancos)

        }
        cursorBancos.close()
        binding.spDepBanco.setText("Seleccione un banco...")
        actualizarBancos()
        val adapterBancos: ArrayAdapter<CharSequence> =
            ArrayAdapter(this, R.layout.spinner_bancos, listaInfoBancos as List<CharSequence>)
        binding.spDepBanco.setAdapter(adapterBancos)

        adapterBancos.notifyDataSetChanged()

    }

    private fun actualizarBancos() {

        listaInfoBancos = ArrayList()
        listaInfoBancos.add("Seleccione un banco...")

        binding.spDepBanco.listSelection = 0

        for (i in listaBancosDep.indices) {
            listaInfoBancos.add(listaBancosDep[i].nombanco)
        }
    }


    private fun generarNroPrecobranza(): String {
        val fechaHoy = Date(Calendar.getInstance().timeInMillis)

        val formatoFecha = SimpleDateFormat("yyMM", Locale.getDefault())

        val fecha = formatoFecha.format(fechaHoy)

        var correlativo = right(correlativoTexto, 4)
        correlativo = "$codUsuario-$tipoDoc-$fecha$correlativo"

        return correlativo
    }

    private fun generarNroAnexo(): String {
        val fechaHoy = Date(Calendar.getInstance().timeInMillis)

        val formatoFecha = SimpleDateFormat("yyMM", Locale.getDefault())

        val fecha = formatoFecha.format(fechaHoy)

        var correlativo = right(correlativoTexto, 4)
        correlativo = "$fecha$correlativo"

        return correlativo
    }

    private fun right(valor: String, longitud: Int): String {
        return valor.substring(valor.length - longitud)
    }

    private fun verificacionReferencia(
        referencia: String,
        tabla: String,
        codigoBanco: String
    ): Int {
        val cursor = keAndroid.rawQuery(
            "SELECT COUNT(*) FROM $tabla " +
                    "WHERE bcoref = '$referencia' AND bcoref != '' AND bcocod = '$codigoBanco' AND empresa = '$codEmpresa';",
            null
        )
        if (cursor.moveToFirst()) {
            val resultEncontrado = cursor.getInt(0)
            if (resultEncontrado > 0) {
                return 1
            }
        }
        cursor.close()
        return 0
    }

    private fun dialogImg() {
        val builder = AlertDialog.Builder(this)
        val customView = LayoutInflater.from(this).inflate(R.layout.custom_dialog_list_img, null)
        builder.setView(customView)
        val adapterDialog = DialogImgAdapter(listaImagenes) {
            listaImagenes.removeAt(it)
        }
        adapterDialog.updateAdapter(listaImagenes)
        val btnAceptar = customView.findViewById<Button>(R.id.btnAceptar)
        val rvlistaImg = customView.findViewById<RecyclerView>(R.id.rvListaImg)

        rvlistaImg.apply {
            adapter = adapterDialog
            layoutManager = GridLayoutManager(context, 1)
            setHasFixedSize(true)
        }

        val creacion = builder.create()
        creacion.show()
        btnAceptar.setBackgroundColor(btnAceptar.colorAgencia(Constantes.AGENCIA))
        btnAceptar.setOnClickListener { _: View? -> creacion.dismiss() }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if ((requestCode == this.requestCodeImg) && (resultCode == RESULT_OK)) {
            try {
                lateinit var imageUri: Uri
                val clipData = data!!.clipData
                if (clipData == null) {
                    imageUri = data.data!!
                    listaImagenes.add(imageUri)
                } else {
                    for (i in 0 until clipData.itemCount) {
                        listaImagenes.add(clipData.getItemAt(i).uri)
                    }
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Algo salió mal", Toast.LENGTH_LONG).show()
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun getTheme(): Resources.Theme {
        val theme = super.getTheme()
        theme.applyStyle(setThemeAgencia(Constantes.AGENCIA), true)
        // you could also use a switch if you have many themes that could apply
        return theme
    }


}