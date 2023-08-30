package com.appcloos.mimaletin

import android.content.ContentValues
import android.content.SharedPreferences
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.text.Editable
import android.text.SpannableStringBuilder
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.appcloos.mimaletin.databinding.ActivityCreacionDepositoBinding
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList


class creacionDepositoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreacionDepositoBinding
    private lateinit var preferences: SharedPreferences // preferences para cargar los datos de la princ.
    private var cod_usuario: String? = ""
    private var codEmpresa: String? = ""

    private lateinit var conn: AdminSQLiteOpenHelper
    lateinit var ke_android: SQLiteDatabase
    lateinit var nroDeposito:String

    var codigoBancoDep = ""
    var CorrelativoTexto:String = ""
    var nroCorrelativo  = 0
    var tipoDoc = "PRC"
    var sumaTotal = 0.00
    var fechaActual = ""

    lateinit var listaBancosDep         :ArrayList<Bancos>
    lateinit var recibosSelecc          :ArrayList<String>
    lateinit var listaInfoBancos        :ArrayList<String>
    lateinit var listaRecibos           :ArrayList<CXC>

    var enlaceEmpresa = ""; var codigoSucursal = ""; var nombreEmpresa = ""
    var fecha_auxiliar = "0000-00-00"; var fechaDep = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreacionDepositoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        conn            = AdminSQLiteOpenHelper(getApplicationContext(), "ke_android", null, 18)
        ke_android      = conn.writableDatabase

        preferences     = getSharedPreferences("Preferences", MODE_PRIVATE)
        cod_usuario     = preferences.getString("cod_usuario", null)
        codEmpresa      = preferences.getString("codigoEmpresa", null)

        cargarEnlace()

        listaBancosDep  = ArrayList()
        recibosSelecc   = ArrayList()
        listaInfoBancos = ArrayList()
        listaRecibos    = ArrayList()
        recibosSelecc  = intent.getStringArrayListExtra("listRecibos") as ArrayList<String>

        // -- query de los bancos
        //getBancos("USD")
        cargarBancosDep("USD")
        // --

        sumarSaldos(recibosSelecc)


        var cursorCorrelativo = ke_android.rawQuery("SELECT MAX(kcor_numero) FROM ke_corprec WHERE kcor_vendedor ='" +cod_usuario+ "'", null)
        //----
        if(cursorCorrelativo.moveToFirst()){
            println("YA HAY CORRELATIVOS")
            nroCorrelativo    = cursorCorrelativo.getInt(0)
            nroCorrelativo += 1
            CorrelativoTexto  = nroCorrelativo.toString()
            CorrelativoTexto  = "0000$CorrelativoTexto"

        } else{
            nroCorrelativo    = cursorCorrelativo.getInt(0)
            nroCorrelativo += 1
            CorrelativoTexto  = nroCorrelativo.toString()
            CorrelativoTexto  = "0000$CorrelativoTexto"
        }

         //generacion del correlativo completo
        nroDeposito  = generarNroPrecobranza()
        fechaActual = getFechaHoy()
        fechaDep  = fechaActual

        supportActionBar?.title = "REC: " + nroDeposito
        recibosSelecc.joinToString(separator = ",")

        binding.spDepBanco.setOnItemClickListener { parent, view, position, id ->
            if(position != 0){
                codigoBancoDep   = listaBancosDep.get(position-1).codbanco

                if (listaBancosDep.get(position-1).codbanco == "100"){
                    val editable: Editable = SpannableStringBuilder(nroDeposito)
                    binding.etDepRef.text = editable
                    binding.etDepRef.isEnabled = false
                }else{
                    val editable: Editable = SpannableStringBuilder("")
                    binding.etDepRef.text = editable
                    binding.etDepRef.isEnabled = true
                }


            } else if(position == 0){
                codigoBancoDep = ""
            }
        }


        binding.btDepProc.setOnClickListener {
            procesarDeposito()
        }
    }

    private fun getFechaHoy():String{
        var fechaHoy:String
        var fechaSinConvertir: Calendar = Calendar.getInstance()
        var sdf: SimpleDateFormat       = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        fechaHoy                        = sdf.format(fechaSinConvertir.time)
        return fechaHoy
    }


    private fun procesarDeposito() {

        var ll_commit = false

        val listaCabeceraNueva: ArrayList<CXC> = ArrayList()
        val listaLineasNueva:   ArrayList<CXC> = ArrayList()

        val referenciaBanco   = binding.etDepRef.text.toString().uppercase()
        val montoTotal:Double = binding.tvDepMontot.text.toString().toDouble()

        //valido la fecha
        if(fechaDep == "" || fechaDep.equals(null)){
            Toast.makeText(this, "Debe elegir la fecha del depósito", Toast.LENGTH_SHORT).show()
            return
        }

        //valido el banco
        if(codigoBancoDep == "" || codigoBancoDep.equals(null)){
            Toast.makeText(this, "Debe elegir el banco", Toast.LENGTH_SHORT).show()
            return
        }

        // valido la referencia del depósito
        if(referenciaBanco == "" || referenciaBanco.equals(null)){
            Toast.makeText(this, "Debe introducir la referencia del deposito", Toast.LENGTH_SHORT).show()
            return
        }

        //2023-07-06 Verificacion de referencia bancaria en deposito
        var numVerificador = 0
        //numVerificador += verificacionReferencia(referenciaBanco, "ke_precobranza", codigoBancoDep)
        numVerificador += verificacionReferencia(referenciaBanco, "ke_referencias", codigoBancoDep)

        if(numVerificador != 0){
            Toast.makeText(this, "Referencia y banco utilizados previamente.", Toast.LENGTH_SHORT).show()
            return
        }

        val reten= ArrayList<String>()

        val dep = CXC()
        dep.id_recibo = nroDeposito
        dep.tipoRecibo = "D"
        dep.codigoVend = cod_usuario.toString()
        dep.fchrecibo  = fechaDep
        dep.clicontesp = "" //esto lo jalo de  la lista de docs?
        dep.moneda     = "2"
        dep.bcomonto   = montoTotal
        dep.bcoref     = referenciaBanco
        dep.bcocod     = codigoBancoDep
        dep.edorec     = "0"
        dep.fchhr      = fechaActual


        //RECORRO LAS LINEAS DE LOS NUEVOS DOCS.
        //for(i in listaRecibos.indices){
        println("Recibos -> ${recibosSelecc.toString().replace("[","").replace("]", "")}")
            val cursorH: Cursor = ke_android.rawQuery("SELECT * FROM ke_precobradocs WHERE cxcndoc IN (" + recibosSelecc.toString().replace("[","").replace("]", "") + ")", null)

            while (cursorH.moveToNext()){
                println("recibos que estan llegando ${cursorH.getString(0)}")
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

                linea.id_recibo     = nroDeposito
                linea.agencia       = cursorH.getString(1)
                linea.tipodoc       = cursorH.getString(2)
                linea.documento     = cursorH.getString(3)
                linea.bscobro       = cursorH.getDouble(4)
                linea.prccobro      = cursorH.getDouble(5)
                linea.prcdsctopp    = cursorH.getDouble(6)
                linea.nroret        = cursorH.getString(7)
                linea.fchemiret     = cursorH.getString(8)
                linea.bsretiva      = cursorH.getDouble(9)
                linea.refret        = cursorH.getString(10)
                linea.nroretfte     = cursorH.getString(11)
                linea.fchemirfte    = cursorH.getString(12)
                linea.bsmtofte      = cursorH.getDouble(13)
                linea.bsretfte      = cursorH.getDouble(14)
                linea.refretfte     = cursorH.getString(15)
                linea.pidvalid      = cursorH.getString(16)
                linea.bsmtoiva      = cursorH.getDouble(17)
                linea.retmun_bi     = cursorH.getDouble(18)
                linea.retmun_cod    = cursorH.getString(19)
                linea.retmun_nro    = cursorH.getString(20)
                linea.retmun_mto    = cursorH.getDouble(21)
                linea.retmun_fch    = cursorH.getString(22)
                linea.retmun_ref    = cursorH.getString(23)
                linea.diascalc      = cursorH.getDouble(24)
                linea.prccomiv      = cursorH.getDouble(25)
                linea.prccomic      = cursorH.getDouble(26)
                linea.cxcndoc_aux   = cursorH.getString(27)
                linea.tnetodbs      = cursorH.getDouble(28)
                linea.tnetoddol     = cursorH.getDouble(29)
                linea.fchrecibo     = cursorH.getString(30)
                linea.kecxc_id      = right(cursorH.getString(0), 8)
                linea.tasadia       = cursorH.getDouble(32)
                linea.afavor        = cursorH.getDouble(33)
                reten.add(cursorH.getString(34))

                listaLineasNueva.add(linea)
            }
        cursorH.close()

        //}

        //estos dependen de las lineas totales
        dep.bsneto       = valorReal(listaRecibos.sumOf { it.bsneto })
        dep.bsretiva     = valorReal(listaRecibos.sumOf { it.bsretiva })
        dep.bsiva        = valorReal(listaRecibos.sumOf { it.bsiva })
        dep.bsflete      = valorReal(listaRecibos.sumOf { it.bsflete })
        dep.dolflete     = valorReal(listaRecibos.sumOf { it.dolflete })
        dep.bstotal      = valorReal(listaRecibos.sumOf { it.bstotal })
        dep.dolneto      = valorReal(listaRecibos.sumOf { it.dolneto })
        dep.doliva       = valorReal(listaRecibos.sumOf { it.doliva })
        dep.doltotal     = valorReal(listaRecibos.sumOf { it.doltotal })
        dep.netocob      = valorReal(listaRecibos.sumOf { it.netocob })
        dep.bsretflete   = valorReal(listaRecibos.sumOf { it.bsretflete })
        dep.retmun_sbi   = valorReal(listaRecibos.sumOf { it.retmun_sbi })
        dep.retmun_sbs   = valorReal(listaRecibos.sumOf { it.retmun_sbi })
        var fechaVigen   = fechaSuma(fechaActual, 15)
        dep.fchvigen     = fechaVigen
        dep.moneda       = "2"
        dep.tasadia      = 0.00
        listaCabeceraNueva.add(dep)

        // proceso de inserción
        try {
            // inicio la transacción
            ke_android.beginTransaction()

            var qcabecera:ContentValues = ContentValues()
            var qlineas:  ContentValues = ContentValues()

            for( i in listaCabeceraNueva.indices){
                qcabecera.put("cxcndoc", listaCabeceraNueva[i].id_recibo)
                qcabecera.put("tiporecibo", listaCabeceraNueva[i].tipoRecibo)
                qcabecera.put("codvend", listaCabeceraNueva[i].codigoVend)
                qcabecera.put("tiporecibo", listaCabeceraNueva[i].tipoRecibo)
                qcabecera.put("fchrecibo", listaCabeceraNueva[i].fchrecibo)
                qcabecera.put("bsneto", listaCabeceraNueva[i].bsneto)
                qcabecera.put("bsiva", listaCabeceraNueva[i].bsiva)
                qcabecera.put("bsretiva", listaCabeceraNueva[i].bsretiva)
                qcabecera.put("bsflete", listaCabeceraNueva[i].bsflete)
                qcabecera.put("bstotal", listaCabeceraNueva[i].bstotal)
                qcabecera.put("dolneto", listaCabeceraNueva[i].dolneto)
                qcabecera.put("doliva", listaCabeceraNueva[i].doliva)
                // qcabecera.put("dolretiva", listaReciboPrCabecera[i].dolretiva)
                qcabecera.put("dolflete", listaCabeceraNueva[i].dolflete)
                qcabecera.put("doltotal", listaCabeceraNueva[i].doltotal)
                qcabecera.put("moneda", listaCabeceraNueva[i].moneda)
                qcabecera.put("bcocod", listaCabeceraNueva[i].bcocod)
                qcabecera.put("bcomonto", listaCabeceraNueva[i].bcomonto)
                qcabecera.put("bcoref", listaCabeceraNueva[i].bcoref)
                qcabecera.put("edorec", listaCabeceraNueva[i].edorec)
                qcabecera.put("fchvigen", listaCabeceraNueva[i].fchvigen)
                qcabecera.put("bsretflete", listaCabeceraNueva[i].bsretflete)
                qcabecera.put("fechamodifi", getFechaHoy())

                for(j in listaLineasNueva.indices){
                    qlineas.put("cxcndoc", listaLineasNueva[j].id_recibo)
                    qlineas.put("agencia",   listaLineasNueva[j].agencia)
                    qlineas.put("tipodoc",listaLineasNueva[j].tipodoc)
                    qlineas.put("documento", listaLineasNueva[j].documento)
                    qlineas.put("bscobro", listaLineasNueva[j].bscobro)
                    println("bs cobrados en la linea ${j} : ${listaLineasNueva[j].bscobro}")
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
                    println("Numero $j")
                    //qlineas.put("monto_aux_pdf", listaRecibos[j].efectivo)
                    ke_android.insert("ke_precobradocs", null, qlineas)
                }

                ke_android.insert("ke_precobranza", null, qcabecera)

                var qcorrelativo: ContentValues = ContentValues()
                qcorrelativo.put("kcor_numero", nroCorrelativo)
                qcorrelativo.put("kcor_vendedor", cod_usuario)

                ke_android.insert("ke_corprec", null, qcorrelativo)

                //mato los recibos
                var estado = ContentValues()
                estado.put("edorec", "9")
                estado.put("fechamodifi", getFechaHoy())
                estado.put("reci_doc", right(listaCabeceraNueva[i].id_recibo, 8))
                ke_android.update("ke_precobranza", estado,"cxcndoc IN (" + recibosSelecc.toString().replace("[","").replace("]", "") + ")", null)


                ll_commit = true

            }

        }catch (exception: SQLException){
            println(exception.message)
            ll_commit = false

            ke_android.endTransaction()
            if(!ll_commit){
                return
            }
        }

        if(ll_commit) {
            ke_android.setTransactionSuccessful()
            ke_android.endTransaction()
            var listadatos: ArrayList<CXC> = ArrayList()
            listadatos.add(dep)
            /* crear comprobante de deposito  */

            var dialog: DialogAnexo = DialogAnexo()
            dialog.DialogAnexo(this, listadatos)

            Toast.makeText(this, "Depósito creado", Toast.LENGTH_SHORT).show()

        }else{
            ke_android.endTransaction()
        }

    }
    private fun valorReal(monto:Double):Double{
        var valor:Double = 0.00
        valor = monto
        valor = Math.round(valor*100.00)/100.00
        return valor
    }



    private fun fechaSuma(fechaOld:String, cantDias:Long):String{
        var fechaNueva = ""
        var diasAdicional = cantDias

        // de string a fecha
        var fechaActual:String = fechaOld
        var fechaNow = LocalDate.parse(fechaActual, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        var fechaNew = fechaNow.plusDays(diasAdicional)

        // de fecha a String (la nueva)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        fechaNueva = fechaNew.format(formatter)

        return fechaNueva
    }


    private fun sumarSaldos(listaRecibosSel: ArrayList<String>) {
        sumaTotal = 0.00
        listaRecibos = ArrayList()

        println("${listaRecibosSel}")
        var cobranza: CXC
        var query     = arrayOf("cxcndoc," + "efectivo,"+ "bsneto," + "bsretiva,"+ "bsiva," + "bsflete," + "dolflete," +
        "bstotal," + "dolneto," + "doliva," + "doltotal," + "netocob," + "bsretflete," + "retmun_sbi," + "retmun_sbs")
        var tabla     = "ke_precobranza"
        var condicion = "cxcndoc IN (" +listaRecibosSel.toString().replace("[","").replace("]", "") + ")"

        var cursorRec: Cursor = ke_android.query(tabla, query, condicion, null,null,null, null)

        while (cursorRec.moveToNext()){
            cobranza = CXC()
            cobranza.id_recibo = cursorRec.getString(0)
            cobranza.efectivo  = cursorRec.getDouble(1)
            cobranza.bsneto    = cursorRec.getDouble(2)
            cobranza.bsretiva = cursorRec.getDouble(3)
            cobranza.bsiva = cursorRec.getDouble(4)
            cobranza.bsflete = cursorRec.getDouble(5)
            cobranza.dolflete = cursorRec.getDouble(6)
            cobranza.bstotal  = cursorRec.getDouble(7)
            cobranza.dolneto = cursorRec.getDouble(8)
            cobranza.doliva = cursorRec.getDouble(9)
            cobranza.doltotal = cursorRec.getDouble(10)
            cobranza.netocob   = cursorRec.getDouble(11)
            cobranza.bsretflete = cursorRec.getDouble(12)
            cobranza.retmun_sbi = cursorRec.getDouble(13)
            cobranza.retmun_sbs = cursorRec.getDouble(14)
            println("recibos añadidos a la lista ${cobranza.id_recibo}")
            listaRecibos.add(cobranza)

        }
        //coloco la suma total del monto de los recs.
        sumaTotal = listaRecibos.sumOf { it.efectivo }
        binding.tvDepMontot.text = sumaTotal.toString()


        binding.rvContenidoDep.layoutManager = LinearLayoutManager(this)
        val adapter = RecsAdapter()
        adapter.RecsAdapter(this, listaRecibos)
        binding.rvContenidoDep.adapter = adapter

    }

    private fun cargarEnlace() {
        ke_android = conn.writableDatabase
        var columnas = arrayOf("kee_nombre," + "kee_url," + "kee_sucursal")
        var cursorE: Cursor
        var condicion = "kee_codigo ='" + codEmpresa + "'"
        cursorE = ke_android.query("ke_enlace", columnas, condicion, null,null,null,null)

        while(cursorE.moveToNext()){
            nombreEmpresa  = cursorE.getString(0)
            enlaceEmpresa  = cursorE.getString(1)
            codigoSucursal = cursorE.getString(2)
        }
    }


    private fun getBancos(monedaBanco:String){
        //descargarBancos("https://"+ enlaceEmpresa + "/webservice/bancos.php?fecha_sinc=" + fecha_auxiliar.trim() + "&&agencia=" + codigoSucursal.trim(), monedaBanco)
    }

    private fun descargarBancos(URL:String, monedaBanco: String){

        var ll_commit = false

        var codbanco  = ""
        var nombanco  = ""
        var cuentanac = 0.00
        var inactiva  = 0.00
        var fechamodifiBan = ""

        conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null, 14)
        ke_android = conn.readableDatabase

        val jsonArrayRequest: JsonArrayRequest
        // println("hasta aca todo bien")
        jsonArrayRequest = JsonArrayRequest(
                Request.Method.GET, // method
                URL, // url
                null, // json request
                { response -> // response listener
                    if (response != null) {
                        println("si hubo respuesta")
                        ll_commit = false
                        ke_android.beginTransaction()
                        var jsonObject: JSONObject? = null
                        try {

                            // loop through the array elements
                            for (i in 0 until response.length()) {
                                jsonObject      = response.getJSONObject(i);
                                codbanco        = jsonObject.getString("codbanco")
                                nombanco        = jsonObject.getString("nombanco")
                                cuentanac       = jsonObject.getDouble("cuentanac")
                                inactiva        = jsonObject.getDouble("inactiva")
                                fechamodifiBan  = jsonObject.getString("fechamodifi")

                                var qBancos: ContentValues = ContentValues()
                                qBancos.put("codbanco", codbanco)
                                qBancos.put("nombanco", nombanco)
                                qBancos.put("cuentanac", cuentanac)

                                var qcodigoLocal: Cursor
                                qcodigoLocal = ke_android.rawQuery("SELECT count(codbanco) FROM listbanc WHERE codbanco ='" + codbanco + "'", null)
                                qcodigoLocal.moveToFirst()
                                //variable para obtener el conteo de documentos que ya esten en el telf
                                var codigoExistente = qcodigoLocal.getInt(0)

                                if (codigoExistente > 0) {
                                    ke_android.update("listbanc", qBancos, "codbanco= ?", arrayOf(codbanco)
                                    )
                                } else if (codigoExistente == 0) {
                                    ke_android.insert("listbanc", null, qBancos)
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
                            cargarBancosDep(monedaBanco)



                        }else if(!ll_commit){
                            ke_android.endTransaction()
                        }
                    }
                },
                {error -> // error listener
                    //
                }
        )
        var requestQueue: RequestQueue = Volley.newRequestQueue(applicationContext)
        requestQueue.add(jsonArrayRequest)
        cargarBancosDep(monedaBanco)

    }

    private fun cargarBancosDep(Moneda: String) {
        listaInfoBancos.clear()
        listaBancosDep.clear()

        ke_android = conn.writableDatabase
        var bancos:Bancos
        var moneda:Double = 0.00

        if(Moneda == "USD"){
            moneda = 2.00

        }else if(Moneda == "BSS"){
            moneda = 1.00
        }

        var cursorBancos:Cursor = ke_android.rawQuery("SELECT DISTINCT codbanco, nombanco,cuentanac, inactiva, fechamodifi FROM listbanc WHERE inactiva = 0 AND cuentanac = $moneda AND codbanco != '99'", null)
        while(cursorBancos.moveToNext()){
            bancos = Bancos()
            bancos.codbanco     = cursorBancos.getString(0)
            bancos.nombanco     = cursorBancos.getString(1)
            bancos.cuentanac    = cursorBancos.getDouble(2)
            bancos.inactiva     = cursorBancos.getDouble(3)
            bancos.fechamodifi  = cursorBancos.getString(4)
            listaBancosDep.add(bancos)

        }
        binding.spDepBanco.setText("Seleccione un banco...")
        actualizarBancos()
        var adapterBancos: ArrayAdapter<CharSequence>
        adapterBancos = ArrayAdapter(this, R.layout.spinner_bancos, listaInfoBancos as List<CharSequence>)
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
        var fechaHoy: Date
        fechaHoy = Date(Calendar.getInstance().timeInMillis)

        var formatoFecha: SimpleDateFormat
        formatoFecha = SimpleDateFormat("yyMM")

        var fecha = formatoFecha.format(fechaHoy)

        var correlativo = right(CorrelativoTexto, 4)
        correlativo = "$cod_usuario-$tipoDoc-$fecha$correlativo"

        return correlativo
    }

    private fun generarNroAnexo(): String {
        val fechaHoy = Date(Calendar.getInstance().timeInMillis)

        val formatoFecha = SimpleDateFormat("yyMM", Locale.getDefault())

        val fecha = formatoFecha.format(fechaHoy)

        var correlativo = right(CorrelativoTexto, 4)
        correlativo = "$fecha$correlativo"

        return correlativo
    }

    private fun right(valor:String, longitud:Int):String {
        return valor.substring(valor.length - longitud)
    }

    private fun verificacionReferencia(referencia: String, tabla: String, codigoBanco: String): Int {
        val cursor = ke_android.rawQuery("SELECT COUNT(*) FROM $tabla WHERE bcoref = '$referencia' AND bcoref != '' AND bcocod = '$codigoBanco';", null)
        if(cursor.moveToFirst()){
            val resultEncontrado = cursor.getInt(0)
            if(resultEncontrado > 0){
                return 1
            }
        }
        cursor.close()
        return 0
    }



}