package com.appcloos.mimaletin

import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.icu.text.DecimalFormat
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.InputFilter
import android.text.SpannableStringBuilder
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.appcloos.mimaletin.classes.DecimalDigitsInputFilter
import com.appcloos.mimaletin.databinding.ActivityCxcReportBinding
import com.appcloos.mimaletin.viewmodel.CXC.ke_precobradocs
import com.appcloos.mimaletin.viewmodel.CXC.ke_precobranza
import org.json.JSONObject
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.math.roundToInt

class cxcReportActivity : AppCompatActivity() {
    private var APP_NOTA_ENTREGA_BS = false
    private var APP_PORCENTAJE_COMPLEMENTO = 0.9
    //declaracion de variables--
    //viewbinding
    private lateinit var binding: ActivityCxcReportBinding
    //DB
    private lateinit var conn: AdminSQLiteOpenHelper
    lateinit var ke_android: SQLiteDatabase
    lateinit var preferences: SharedPreferences
    //Strings
    lateinit var nroPrecobranza:String ; var fechaQuery:String = ""; var cod_usuario:String? = ""; var codEmpresa:String?  = ""; var nombreEmpresa:String = "";  var codigoSucursal :String = "";
    var enlaceEmpresa :String = ""; var fecha_auxiliar:String = "0001-01-01" ; var fechaActual = ""; var idTasa:String = ""; var fechaTasa:String = ""; var fechayHoraTasa:String = "";
    var fechamodifitasa:String = ""; var usuarioTasa:String = ""; var ipTasa:String = ""; var idTasaSeleccionada:String=""; var fechayHoraSelecc:String = ""; var codbanco:String = "";
    var nombanco:String =""; var fechamodifiBan:String = ""; var CorrelativoTexto:String = ""; var tipoDoc = "PRC"; var codigoCliente = ""; var codigoBancoCompleto = ""; var nombreBancoCompleto = "";
    var codigoBancoImpuesto = ""; var referenciaPrincipal = ""; var referenciaImp = ""; var referenciaCm = "";   var tasaId = "";  var banderaRetenciones:String = "";
    var tipodeTransaccionPrincipal = ""; var codigoBancoComplemento = ""; var monedaSeleccionadaPr = "";  var monedaSeleccionadaCm = "2"; var nroComplemento = ""; var CorrelativoTextoCom = ""
    var fechatasaH = "";

    //Integers
    var request_code = 1; var nroCorrelativo  = 0; var nroCorrelativoCom = 0;  var tasaFecha = "";

    var listBankDesc = listOf("98", "99", "206")
    //Doubles
    var bsNeto = 0.00; var dnetoTotal = 0.00 ; var bsIvaTotal = 0.00; var bsFleteTotal = 0.00; var bsretencionIvaTotal  = 0.00; var bsRetencionTotal = 0.00; var montoaPagar = 0.00;
    var dtotimpuest  = 0.00; var dFlete  = 0.00; var dretencion = 0.00; var dretencioniva  = 0.00; var dmontoTotal = 0.00; var bsmontoTotal  = 0.00; var tasaNormal = 0.00;
    var tasaInterB = 0.00; var descuentoTotal = 0.00; var dmontoRetFlete  = 0.00; var bsmontoRetFlete = 0.00; var montominimo = 0.00; var cdretencion = 0.00;
    var cdretencioniva  = 0.00; var cbsretparme = 0.00; var cdretparme = 0.00; var retenpagado  = 0.00; var cbsretencioniva  = 0.00; var montoMinimoRec = 0.00; var montoMinimoImp = 0.00;
    var montoMinimoComp  = 0.00; var montoI = 0.00; var montoC = 0.00; var montoRec = 0.00; var saldo = 0.00; var montoNuevoIva = 0.00; var montoNuevoFlete = 0.00;var tasaCambio:Double = 0.00;
    var tasaCambioSeleccionadaPrincipal:Double =0.00; var tasaCambioComplemento:Double = 0.00; var cuentanac:Double = 0.00; var inactiva:Double = 0.00; var tasaInterbancaria = 0.00
    var netoRestante = 0.00; var ivaRestante = 0.00; var fleteRestante = 0.00; var cantidadDeDescuento = 0.00

    //Booleans
    var ll_commit:Boolean = false
    var pagaRetenciones:Boolean = false

    val formatoNum = DecimalFormat("0.00")

    //listas--
    //listas string
    lateinit var listaDocsSeleccionados:ArrayList<String>
    lateinit var listaInfoTasas        :ArrayList<String>
    lateinit var listaInfoBancosImp    : ArrayList<String>
    lateinit var listaInfoBancosCm     : ArrayList<String>
    lateinit var listaInfoBancos       : ArrayList<String>
    lateinit var tipoDocsaPagar        : ArrayList<String>
    lateinit var listaTiposRet         : ArrayList<String>
    //tipo tasas
    lateinit var listaTasas            :ArrayList<tasas>
    // tipo bancos
    lateinit var listaBancos           :ArrayList<Bancos>
    lateinit var listaBancosImp        :ArrayList<Bancos>
    lateinit var listaBancosCm         :ArrayList<Bancos>
    //tipo docs
    lateinit var listaDocumentos       : ArrayList<Documentos>
    //tipo descuentos
    lateinit var listaDescuentos       : ArrayList<Descuentos>
    //tipo retenciones
    lateinit var listaRetGuardada      : ArrayList<Retenciones>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCxcReportBinding.inflate(layoutInflater) //initializing the binding class
        setContentView(binding.root)

        //inst. conexion
        conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null, 18)

        APP_NOTA_ENTREGA_BS = conn.getConfigBool("APP_NOTA_ENTREGA_BS")
        APP_PORCENTAJE_COMPLEMENTO = conn.getConfigNum("APP_PORCENTAJE_COMPLEMENTO")

        //cargar preferences
        preferences  = getSharedPreferences("Preferences", MODE_PRIVATE)
        cod_usuario  = preferences.getString("cod_usuario", null)
        codEmpresa   = preferences.getString("codigoEmpresa", null)
        //cargar datos
        cargarEnlace()
        fechaActual = getFechaHoy()
        bajarDocsConDesc("https://"+ enlaceEmpresa + "/webservice/bajardescuentos.php?fecha_sinc=" + fecha_auxiliar.trim() +"&&codigo_cli=" + codigoCliente.trim()+ "&&agencia=" + codigoSucursal.trim())
        //Listas de Array
        listaTasas          = ArrayList()
        listaInfoTasas      = ArrayList()
        listaBancos         = ArrayList()
        listaBancosImp      = ArrayList()
        listaInfoBancos     = ArrayList()
        listaDocumentos     = ArrayList()
        tipoDocsaPagar      = ArrayList()
        listaInfoBancosImp  = ArrayList()
        listaDescuentos     = ArrayList()
        listaRetGuardada    = ArrayList()
        listaInfoBancosCm   = ArrayList()
        listaBancosCm       = ArrayList()
        listaTiposRet       = ArrayList()

        //carga de los docs previamente adjuntados
        listaDocsSeleccionados  = intent.getStringArrayListExtra("listaDocs") as ArrayList<String>
        //validacion del correlativo para la cobranza
        val cursorCorrelativo = ke_android.rawQuery("SELECT MAX(kcor_numero) FROM ke_corprec WHERE kcor_vendedor ='$cod_usuario'", null)
        //----
        if(cursorCorrelativo.moveToFirst()){
            nroCorrelativo    = cursorCorrelativo.getInt(0)
            nroCorrelativo   += 1
            CorrelativoTexto  = nroCorrelativo.toString()
            CorrelativoTexto  = "0000$CorrelativoTexto"

            nroCorrelativoCom   = nroCorrelativo + 1
            CorrelativoTextoCom = nroCorrelativoCom.toString()
            CorrelativoTextoCom = "0000$CorrelativoTextoCom"

        } else{
            nroCorrelativo    = cursorCorrelativo.getInt(0)
            nroCorrelativo+=1
            CorrelativoTexto  = nroCorrelativo.toString()
            CorrelativoTexto  = "0000$CorrelativoTexto"

            nroCorrelativoCom   = nroCorrelativo + 1
            CorrelativoTextoCom = nroCorrelativoCom.toString()
            CorrelativoTextoCom = "0000$CorrelativoTextoCom"
        }
        cursorCorrelativo.close()

        //generacion del correlativo completo
        nroPrecobranza          = generarNroPrecobranza()

        //generacion del nro del complemento
        nroComplemento          = generarNroComplemento()

        supportActionBar?.title = "REC: $nroPrecobranza"
        listaDocsSeleccionados.joinToString(separator = ",")

        //query de los bancos
        //getBancos("USD")
        //cargarBancosMain("BSS")

        //coloco por defecto opciones seleccionadas
        binding.rbCxcDivisasMain.isChecked  = true
        binding.rbCxcCompMain.isChecked     = true
        binding.rbCxcTransfMain.isChecked   = true

        //calculo automatico, mejorar despues .-
        if(binding.rbCxcDivisasMain.isChecked){
            cargarBancosMain("USD")
            binding.rbCxcEfectivoMain.visibility = View.VISIBLE
            cargarSaldos("USD", listaDocsSeleccionados)
            monedaSeleccionadaPr = "2"

        }
        if(binding.rbCxcCompMain.isChecked){
            //TBD
        }else{
            binding.cbCxcComplemento.visibility = View.INVISIBLE
        }

        //---------------------
        validarRetenciones()

        //Radio button bss principal
        binding.rbCxcBssMain.setOnClickListener(
            View.OnClickListener {
                if(binding.rbCxcBssMain.isChecked){
                    if(tasaCambioSeleccionadaPrincipal == 0.00){
                        Toast.makeText(this, "Debes seleccionar una fecha de pago", Toast.LENGTH_SHORT).show()
                        binding.rbCxcDivisasMain.isChecked = true
                        binding.rbCxcTransfMain.isChecked  = true
                        //Cuando se cambia la moneda se deja en blanco la variable que guarda el banco
                        codigoBancoCompleto = ""
                        binding.spCxcBancoMain.listSelection = 0

                    }else{
                        cargarBancosMain("BSS")
                        binding.rbCxcEfectivoMain.visibility = View.INVISIBLE
                        binding.cbCxcDescuentos.visibility   = View.INVISIBLE
                        cargarSaldos("BSS", listaDocsSeleccionados, !binding.cbExcReten.isChecked)
                        monedaSeleccionadaPr = "1"
                        binding.rbCxcTransfMain.isChecked  = true
                    }

                }else{
                    cargarBancosMain("USD")
                    binding.rbCxcEfectivoMain.visibility = View.VISIBLE
                    monedaSeleccionadaPr = "2"
                    binding.rbCxcTransfMain.isChecked  = true
                }
                mostrandoComplemento()
            }
        )
        //Radio button divisas principal
        binding.rbCxcDivisasMain.setOnClickListener(
            View.OnClickListener {
                if(binding.rbCxcDivisasMain.isChecked){
                    cargarBancosMain("USD")
                    binding.rbCxcEfectivoMain.visibility = View.VISIBLE
                    binding.cbCxcDescuentos.visibility   = View.VISIBLE
                    cargarSaldos("USD", listaDocsSeleccionados, !binding.cbExcReten.isChecked)
                    monedaSeleccionadaPr = "2"
                    //Cuando se cambia la moneda se deja en blanco la variable que guarda el banco
                    codigoBancoCompleto = ""
                    binding.spCxcBancoMain.listSelection = 0
                }
                mostrandoComplemento()
            }
        )

        //datepicker de la fecha de pago
        binding.dpFecharec.setOnClickListener {
            showDatePickerDialog()
            mostrandoComplemento()
            //retenciones()
        }

        //boton procesar
        binding.btCxcProcesar.setOnClickListener(View.OnClickListener {
            procesamientodeDatos()
            //procesar2()
        })

        //boton retencion
        binding.btCxcRetenciones.setOnClickListener(View.OnClickListener {
            iraRetenciones()
        })

        binding.cbCxcDescuentos.setOnClickListener {

            if (binding.cbCxcDescuentos.isChecked) {
                cargarSaldos("USD", listaDocsSeleccionados, !binding.cbExcReten.isChecked)
                recalcularComplemento()
                pagaRetenciones = !binding.cbExcReten.isChecked
            } else {
                cargarSaldos("USD", listaDocsSeleccionados, !binding.cbExcReten.isChecked)
                recalcularComplemento()
                pagaRetenciones = !binding.cbExcReten.isChecked
            }
            mostrandoComplemento()

        }


        //seleccion de bancos Main
        binding.spCxcBancoMain.setOnItemClickListener { parent, view, position, id ->
            if (position != 0) {
                codigoBancoCompleto = listaBancos[position - 1].codbanco
                nombreBancoCompleto = listaBancos[position - 1].nombanco

                if (binding.rbCxcDivisasMain.isChecked) {
                    calcularDescuentos("USD")
                    cargarSaldos("USD", listaDocsSeleccionados, !binding.cbExcReten.isChecked)
                }

            } else if (position == 0) {
                codigoBancoCompleto = ""
                cargarSaldos("USD", listaDocsSeleccionados, !binding.cbExcReten.isChecked)

                if (binding.rbCxcDivisasMain.isChecked) {
                    calcularDescuentos("USD")
                    cargarSaldos("USD", listaDocsSeleccionados, !binding.cbExcReten.isChecked)
                }
            }
            mostrandoComplemento()
        }


        //seleccion de bancos Complemento
        binding.spCxcBancoCom.setOnItemClickListener { parent, view, position, id ->
            if(position != 0){
                codigoBancoComplemento   = listaBancosCm.get(position-1).codbanco


            } else if( position == 0){
                codigoBancoComplemento = ""

            }
        }

        binding.rbCxcAbonoMain.setOnClickListener(View.OnClickListener {
            if(binding.rbCxcAbonoMain.isChecked){

                binding.cbCxcComplemento.visibility = View.INVISIBLE
                binding.cbCxcComplemento.isChecked  = false

                val editable: Editable = SpannableStringBuilder("")

                binding.etCxcMontoCom.text = editable
                binding.etCxcRefCom.text = editable

                binding.tvCxcComplemento.visibility = View.INVISIBLE
                binding.rbCxcBssCom.visibility      = View.INVISIBLE
                binding.rbCxcDivisasCom.visibility  = View.INVISIBLE
                binding.rbCxcTransfCom.visibility   = View.INVISIBLE
                binding.rbCxcEfectivoCom.visibility = View.INVISIBLE
                binding.tilBancoCom.visibility      = View.INVISIBLE
                binding.spCxcBancoCom.visibility    = View.INVISIBLE
                binding.tilRefCom.visibility        = View.INVISIBLE
                binding.etCxcRefCom.visibility      = View.INVISIBLE
                binding.tilMontoCom.visibility      = View.INVISIBLE
                binding.etCxcMontoCom.visibility    = View.INVISIBLE
                binding.tvPrecioMostrarComplemento.visibility = View.INVISIBLE


            }else{
                binding.cbCxcComplemento.visibility = View.VISIBLE
            }
            mostrandoComplemento()
        })
        binding.rbCxcCompMain.setOnClickListener(View.OnClickListener {
            if(binding.rbCxcCompMain.isChecked){
                binding.cbCxcComplemento.visibility = View.VISIBLE
                binding.cbCxcComplemento.isChecked  = false
            }else{
                binding.cbCxcComplemento.visibility = View.INVISIBLE
            }
            mostrandoComplemento()
        })

        binding.rbCxcEfectivoMain.setOnClickListener{

            if(binding.rbCxcEfectivoMain.isChecked){
                binding.tilCxcSpbanco.visibility   = View.INVISIBLE
                binding.tilCxcRefMain.visibility   = View.INVISIBLE
                codigoBancoCompleto = ""
                referenciaPrincipal = ""
                cargarSaldos("USD", listaDocsSeleccionados, !binding.cbExcReten.isChecked)


            }else{

                binding.tilCxcRefMain.visibility   = View.VISIBLE
                binding.tilCxcSpbanco.visibility   = View.VISIBLE

                if(binding.rbCxcDivisasMain.isChecked){
                    cargarSaldos("USD", listaDocsSeleccionados, !binding.cbExcReten.isChecked)
                }

            }

            mostrandoComplemento()

        }

        binding.rbCxcTransfMain.setOnClickListener{

            binding.tilCxcMontoMain.visibility = View.VISIBLE
            binding.tilCxcRefMain.visibility   = View.VISIBLE
            binding.tilCxcSpbanco.visibility   = View.VISIBLE
            if(binding.rbCxcDivisasMain.isChecked){
                cargarSaldos("USD", listaDocsSeleccionados, !binding.cbExcReten.isChecked)
            }
            mostrandoComplemento()

        }

        binding.rbCxcTransfCom.setOnClickListener{
            binding.tilBancoCom.visibility  = View.VISIBLE
            binding.tilRefCom.visibility    = View.VISIBLE
            binding.rbCxcBssCom.visibility  = View.VISIBLE

        }

        binding.rbCxcEfectivoCom.setOnClickListener {
            binding.tilBancoCom.visibility  = View.INVISIBLE
            binding.tilRefCom.visibility    = View.INVISIBLE
            codigoBancoComplemento = ""
            referenciaCm = ""
            binding.rbCxcBssCom.visibility = View.INVISIBLE
            binding.rbCxcBssCom.isChecked  = false
        }

        binding.btVerDetDescuento.setOnClickListener {
            cargarDetalleDescuentos()
        }



        //activar complemento
        binding.cbCxcComplemento.setOnClickListener(View.OnClickListener {


            if(binding.cbCxcComplemento.isChecked){
                //hago visible las cosas
                binding.tvCxcComplemento.visibility = View.VISIBLE
                binding.rbCxcBssCom.visibility      = View.VISIBLE
                binding.rbCxcDivisasCom.visibility  = View.VISIBLE
                binding.rbCxcTransfCom.visibility   = View.VISIBLE
                binding.rbCxcEfectivoCom.visibility = View.VISIBLE
                binding.tilBancoCom.visibility      = View.VISIBLE
                binding.spCxcBancoCom.visibility    = View.VISIBLE
                binding.tilRefCom.visibility        = View.VISIBLE
                binding.etCxcRefCom.visibility      = View.VISIBLE
                binding.tilMontoCom.visibility      = View.VISIBLE
                binding.etCxcMontoCom.visibility    = View.VISIBLE
                //ejecuto la carga de bancos (por defecto en USD)
                cargarBancosCom("USD")
                binding.rbCxcDivisasCom.isChecked  = true
                binding.rbCxcTransfCom.isChecked   = true
                if(binding.rbCxcDivisasMain.isChecked){
                    cargarSaldos("USD", listaDocsSeleccionados, !binding.cbExcReten.isChecked)
                }
                binding.tvPrecioMostrarComplemento.visibility = View.VISIBLE


            }else{
                //Para poner texto en blanco en un edittext
                val editable: Editable = SpannableStringBuilder("")

                binding.etCxcMontoCom.text = editable
                binding.etCxcRefCom.text = editable

                binding.tvCxcComplemento.visibility = View.INVISIBLE
                binding.rbCxcBssCom.visibility      = View.INVISIBLE
                binding.rbCxcDivisasCom.visibility  = View.INVISIBLE
                binding.rbCxcTransfCom.visibility   = View.INVISIBLE
                binding.rbCxcEfectivoCom.visibility = View.INVISIBLE
                binding.tilBancoCom.visibility      = View.INVISIBLE
                binding.spCxcBancoCom.visibility    = View.INVISIBLE
                binding.tilRefCom.visibility        = View.INVISIBLE
                binding.etCxcRefCom.visibility      = View.INVISIBLE
                binding.tilMontoCom.visibility      = View.INVISIBLE
                binding.etCxcMontoCom.visibility    = View.INVISIBLE
                binding.tvPrecioMostrarComplemento.visibility = View.INVISIBLE
                //binding.rbCxcTransfCom.isChecked   = false
                if(binding.rbCxcDivisasMain.isChecked){
                    cargarSaldos("USD", listaDocsSeleccionados, !binding.cbExcReten.isChecked)
                }
            }
            recalcularComplemento()
            mostrandoComplemento()

        })

        binding.rbCxcDivisasCom.setOnClickListener(View.OnClickListener {
            if(binding.rbCxcDivisasCom.isChecked){
                cargarBancosCom("USD")
                monedaSeleccionadaCm = "2"
                binding.rbCxcEfectivoCom.visibility = View.VISIBLE
                //cargarSaldos("USD", listaDocsSeleccionados, pagaRetenciones)

                recalcularComplemento()
                mostrandoComplemento()

            }else{
                //cargarBancosCom("BSS")
                monedaSeleccionadaCm = "1"
                //cargarSaldos("BSS", listaDocsSeleccionados, pagaRetenciones)
                recalcularComplemento()
            }
        })

        binding.rbCxcBssCom.setOnClickListener(View.OnClickListener {
            if(binding.rbCxcBssCom.isChecked){
                if (tasaCambioSeleccionadaPrincipal == 0.00){
                    Toast.makeText(this, "Debes seleccionar una fecha de pago", Toast.LENGTH_SHORT).show()
                    binding.rbCxcDivisasCom.isChecked = true
                    binding.rbCxcDivisasCom.isChecked  = true
                } else {
                    cargarBancosCom("BSS")
                    monedaSeleccionadaCm = "1"
                    binding.rbCxcEfectivoCom.visibility = View.INVISIBLE
                    binding.rbCxcEfectivoCom.isChecked = false
                    binding.rbCxcTransfCom.isChecked = true
                    recalcularComplemento()
                    mostrandoComplemento()
                }

            }else{
                //cargarBancosCom("USD")
                monedaSeleccionadaCm = "2"
                recalcularComplemento()
            }
            //Funcion para recalcular lo que se debe de poner en complemento
            mostrandoComplemento()
        })

        binding.cbExcReten.setOnClickListener {
            if (listaRetGuardada.size > 0){
                binding.cbExcReten.isChecked = false
                Toast.makeText(this, "Ya agregó retenciones", Toast.LENGTH_SHORT).show()
            }else{
                retenciones()
                mostrandoComplemento()
            }

        }

        binding.etCxcMontoMain.addTextChangedListener {
            mostrandoComplemento()
        }
        binding.etCxcMontoMain.filters = arrayOf<InputFilter>(DecimalDigitsInputFilter(100, 2))
        binding.etCxcMontoCom.filters = arrayOf<InputFilter>(DecimalDigitsInputFilter(100, 2))
    }

    fun procesar2(){
        //FALTA DESCUENTO EN CADA DOCUMENTO
        //FALTA RETENCION EN CADA DOCUMENTO

        val cabecera = ke_precobranza()
        val lineas = ke_precobradocs()

        val fechaRegistro = fechaQuery
        //Variables de Botones (Boolean)
        // Variables Moneda
        val monedaDivisa  = binding.rbCxcDivisasMain.isChecked
        val monedaBs      = binding.rbCxcBssMain.isChecked

        //Variables Tipo de Pago
        val pagoCompleto  = binding.rbCxcCompMain.isChecked
        val pagoAbono     = binding.rbCxcAbonoMain.isChecked

        //Variable Complemento
        val complemento   = binding.cbCxcComplemento.isChecked

        //Tipo de metodo de pago
        val transferencia = binding.rbCxcTransfMain.isChecked
        val efectivo      = binding.rbCxcEfectivoMain.isChecked

        //Excluir retenciones
        val excReten      = binding.cbExcReten.isChecked
        val darDescu      = binding.cbCxcDescuentos.isChecked

        //Variables Numericas
        val neto      = binding.tvCxcNeto.text.toString().toDouble()
        val iva       = binding.tvCxcIva.text.toString().toDouble()
        val flete     = binding.tvCxcFlete.text.toString().toDouble()
        val retencion = binding.tvCxcReten.text.toString().toDouble()
        val descuento = binding.tvCxcDctos.text.toString().toDouble()
        val total     = binding.tvCxcTotal.text.toString().toDouble()

        var pago      = binding.etCxcMontoMain.text.toString().toDouble()

        val montoMinimo = if (pagoCompleto) total else (iva + flete)
        val bancoRef    = if (transferencia) binding.etCxcRefMain.text.toString() else ""
        val banco       = if (transferencia) codigoBancoCompleto else ""

        if (validacionesPrincipales(fechaRegistro, pago, bancoRef, banco, transferencia, efectivo)){
            return
        }


        if (complemento){

            val cabeceraCom = ke_precobranza()
            val lineasCom = ke_precobradocs()

            val monedaDivisaCom  = binding.rbCxcDivisasCom.isChecked
            val monedaBsCom      = binding.rbCxcBssCom.isChecked

            val transferenciaCom = binding.rbCxcTransfCom.isChecked
            val efectivoCom      = binding.rbCxcEfectivoCom.isChecked

            val pagoCom      = binding.etCxcMontoCom.text.toString().toDouble()

            val bancoRefCom    = if (transferencia) binding.etCxcRefCom.text.toString() else ""
            val bancoCom       = if (transferencia) codigoBancoComplemento else ""

            if(validacionesComplemento(pagoCom, bancoRefCom, bancoCom, transferencia, efectivoCom, montoMinimo, pago, monedaDivisaCom)){
                return
            }


        }else{
            if (montoMinimo > pago){
                Toast.makeText(this, "El monto que registra es insuficiente para el pago", Toast.LENGTH_SHORT).show()
                return
            }

            cabecera.cxcndoc    = nroPrecobranza
            cabecera.tiporecibo = "W"
            cabecera.codvend    = cod_usuario.toString()
            cabecera.kecxc_id   = tasaId
            cabecera.tasadia    = tasaCambioSeleccionadaPrincipal
            cabecera.fchrecibo  = fechatasaH
            cabecera.clicontesp = listaDocumentos[0].contribesp.toString()
            cabecera.moneda     = monedaSeleccionadaPr
            if (efectivo){
                cabecera.efectivo   = pago
                cabecera.fchvigen   = fechaSuma(fechaActual, 60)
                cabecera.diasvigen  = 60.0
            }else{
                cabecera.bcocod     = codigoBancoCompleto
                cabecera.bconombre  = nombreBancoCompleto.trimEnd()
                cabecera.bcomonto   = pago
                cabecera.bcoref     = bancoRef
                cabecera.fchvigen   = fechaSuma(fechaActual, 5)
                cabecera.diasvigen  = 5.0
            }
            cabecera.edorec     = "0"
            cabecera.fchhr      = fechaActual
            cabecera.fechamodifi = getFechaHoy()


            //EMpezando a llenar lineas

            lineas.cxcndoc      = nroPrecobranza
            lineas.agencia      = listaDocumentos[0].agencia
            lineas.tipodoc      = listaDocumentos[0].tipodoc
            lineas.documento    = listaDocumentos[0].documento
            lineas.fchrecibod   = getFechaNow()
            lineas.kecxc_idd    = tasaId
            lineas.tasadiad     = tasaCambioSeleccionadaPrincipal

            if (listaDocumentos[0].tipodocv == "FAC"){
                //IF que valida que lo pagado por el cliente por el IVA sea 0, indicando que ya fue pagado
                if(listaDocumentos[0].bsiva - listaDocumentos[0].bsmtoiva > 0.00){
                    //Calculando el pago del IVA en bolivares y dolares, tomando en cuenta si paga o no retencion
                    val restaIvaDol = if (excReten) listaDocumentos[0].dtotimpuest else listaDocumentos[0].dtotimpuest - listaDocumentos[0].cdretencioniva
                    val restaIvaBss = if (excReten) listaDocumentos[0].bsiva else listaDocumentos[0].bsiva - listaDocumentos[0].cbsretencioniva

                    //IF que valida en que moneda se paga el documento
                    if(monedaDivisa){// Caso de que la moneda sea en dolares
                        //Restando IVA en dolares del pago
                        pago -= restaIvaDol

                        lineas.bscobro      += restaIvaBss  //Adicionando lo cobrado del IVA al cobro total en bolivares del documento
                        lineas.tnetoddol    += restaIvaDol  //Adicionando lo cobrado del IVA al cobro total en dolares del documento
                        lineas.bsmtoiva     = restaIvaBss   //Monto de lo cobrado del IVA del documento en bolivares

                        cabecera.doliva     += restaIvaDol  //Adicionando lo cobrado del IVA en dolates en la cabecera
                        cabecera.bsiva      += restaIvaBss
                    }else{// Caso de que la moneda sea en bolivares
                        //Restando FLete en bolivares del pago
                        pago -= restaIvaBss

                        lineas.bscobro      += restaIvaBss  //Adicionando lo cobrado del IVA al cobro total en bolivares del documento
                        lineas.tnetodbs     += restaIvaBss  //Adicionando lo cobrado del IVA al cobro total en bolivares del documento
                        lineas.bsmtoiva     = restaIvaBss   //Monto de lo cobrado del IVA del documento en bolivares

                        cabecera.doliva     += restaIvaDol  //Adicionando lo cobrado del IVA en dolates en la cabecera
                        cabecera.bsiva      += restaIvaBss
                    }
                }

                //IF que valida que lo pagado por el cliente por el flete sea 0, indicando que ya fue pagado
                if (listaDocumentos[0].bsflete - listaDocumentos[0].bsmtofte > 0){
                    //Calculando el pago del flete en bolivares y dolares, tomando en cuenta si paga o no retencion
                    val restaFleteDol = if (excReten) listaDocumentos[0].dFlete else listaDocumentos[0].dFlete - listaDocumentos[0].cdretflete
                    val restaFleteBss = if (excReten) listaDocumentos[0].bsflete else listaDocumentos[0].bsflete - listaDocumentos[0].cbsretflete

                    //IF que valida en que moneda se paga el documento
                    if (monedaDivisa){ // Caso de que la moneda sea en dolares
                        //Restando FLete en dolares del pago
                        pago -= restaFleteDol

                        lineas.bscobro      += restaFleteBss    //Adicionando lo cobrado del flete al cobro total en bolivares del documento
                        lineas.tnetoddol    += restaFleteDol    //Adicionando lo cobrado del flete al cobro total en dolares del documento
                        lineas.bsmtofte     = restaFleteBss     //Monto de lo cobrado del flete del documento en bolivares

                        cabecera.dolflete   += restaFleteDol    //Adicionando lo cobrado del flete en dolates en la cabecera
                        cabecera.bsflete    += restaFleteBss

                    }else{ // Caso de que la moneda sea en bolivares
                        //Restando FLete en bolivares del pago
                        pago -= restaFleteBss

                        lineas.bscobro      += restaFleteBss    //Adicionando lo cobrado del flete al cobro total en bolivares del documento
                        lineas.tnetodbs     += restaFleteBss    //Adicionando lo cobrado del flete al cobro total en bolivares del documento
                        lineas.bsmtofte     = restaFleteBss     //Monto de lo cobrado del flete del documento en bolivares

                        cabecera.dolflete   += restaFleteDol    //Adicionando lo cobrado del flete en dolates en la cabecera
                        cabecera.bsflete    += restaFleteBss
                    }
                }

            } else { // En el caso de ser N/E solo paga flete
                //IF que valida que lo pagado por el cliente por el flete sea 0, indicando que ya fue pagado
                if (listaDocumentos[0].bsflete - listaDocumentos[0].bsmtofte > 0){
                    //Calculando el pago del flete en bolivares y dolares, tomando en cuenta si paga o no retencion
                    val restaFleteDol = if (excReten) listaDocumentos[0].dFlete else listaDocumentos[0].dFlete - listaDocumentos[0].cdretflete
                    val restaFleteBss = if (excReten) listaDocumentos[0].bsflete else listaDocumentos[0].bsflete - listaDocumentos[0].cbsretflete

                    //IF que valida en que moneda se paga el documento
                    if (monedaDivisa){ // Caso de que la moneda sea en dolares
                        //Restando FLete en dolares del pago
                        pago -= restaFleteDol

                        lineas.bscobro      += restaFleteBss    //Adicionando lo cobrado del flete al cobro total en bolivares del documento
                        lineas.tnetoddol    += restaFleteDol    //Adicionando lo cobrado del flete al cobro total en dolares del documento
                        lineas.bsmtofte     = restaFleteBss     //Monto de lo cobrado del flete del documento en bolivares

                        cabecera.dolflete   += restaFleteDol    //Adicionando lo cobrado del flete en dolates en la cabecera
                        cabecera.bsflete    += restaFleteBss

                    }else{ // Caso de que la moneda sea en bolivares
                        //Restando FLete en bolivares del pago
                        pago -= restaFleteBss

                        lineas.bscobro      += restaFleteBss    //Adicionando lo cobrado del flete al cobro total en bolivares del documento
                        lineas.tnetodbs     += restaFleteBss    //Adicionando lo cobrado del flete al cobro total en bolivares del documento
                        lineas.bsmtofte     = restaFleteBss     //Monto de lo cobrado del flete del documento en bolivares

                        cabecera.dolflete   += restaFleteDol    //Adicionando lo cobrado del flete en dolates en la cabecera
                        cabecera.bsflete    += restaFleteBss
                    }
                }
            }

            //Pago del neto
            if (pago > 0.00 && neto > 0.00){
                if (monedaDivisa){
                    if (pago >= neto){
                        lineas.bscobro      += neto * listaDocumentos[0].tasadoc
                        lineas.tnetoddol    += neto

                        cabecera.dolneto    = neto
                        cabecera.bsneto     = neto * tasaCambioSeleccionadaPrincipal

                        pago -= neto
                    }else{
                        lineas.bscobro      += pago * listaDocumentos[0].tasadoc
                        lineas.tnetoddol    += pago

                        cabecera.dolneto    = pago
                        cabecera.bsneto     = pago * tasaCambioSeleccionadaPrincipal

                        pago -= pago
                    }
                }else{
                    if (pago >= neto) {
                        lineas.bscobro      += (neto / tasaCambioSeleccionadaPrincipal) * listaDocumentos[0].tasadoc //Se maneja asi para cambiar los bolivares calculados a la tasa de cuando se paga a la tasa original del documento
                        lineas.tnetodbs     += neto

                        //OOOOOOOOOOOOOOOOOOOOOOOJJJJJJJJJJJJJJJJJJJJOOOOOOOOOOOOOOOOOOOOOOOOOO
                        //DEBERIA SER BSNETO
                        cabecera.dolneto    = (neto / tasaCambioSeleccionadaPrincipal)
                        cabecera.bsneto     = neto

                        pago -= neto
                    }else{
                        lineas.bscobro      += (pago / tasaCambioSeleccionadaPrincipal) * listaDocumentos[0].tasadoc //Se maneja asi para cambiar los bolivares calculados a la tasa de cuando se paga a la tasa original del documento
                        lineas.tnetodbs     += pago

                        cabecera.dolneto    = (pago / tasaCambioSeleccionadaPrincipal)
                        cabecera.bsneto     = pago

                        pago -= pago
                    }
                }
            }

            cabecera.bstotal = (cabecera.bsiva + cabecera.bsflete + cabecera.bsneto)
            cabecera.doltotal = (cabecera.doliva + cabecera.dolflete + cabecera.dolneto)

            cabecera.netocob = if (monedaDivisa){
                cabecera.doltotal - cabecera.dolflete - cabecera.doliva
            } else {
                cabecera.bstotal - cabecera.bsflete - cabecera.bsiva
            }

        }
    }

    private fun validacionesComplemento(pagoCom: Double, bancoRefCom: String, bancoCom: String, transferencia: Boolean, efectivoCom: Boolean, montoMinimo: Double, pago: Double, monedaDivisaCom: Boolean): Boolean {
        if(pagoCom.equals(null) || pagoCom == 0.00){
            Toast.makeText(this, "Ingrese un valor valido en el monto a registrar en complemento", Toast.LENGTH_SHORT).show()
            return true
        }

        if ((bancoRefCom == "") && transferencia){
            Toast.makeText(this, "Debe ingresar una referencia bancaria en complemento", Toast.LENGTH_SHORT).show()
            return true
        }

        if (bancoCom == "" && transferencia){
            Toast.makeText(this, "Debe serleccionar un banco en complemento", Toast.LENGTH_SHORT).show()
            return true
        }

        //If que valida que si la cobranza es en tranferencia no se repita el banco y la referencia (para complemento)
        if(!efectivoCom){
            val cursor = ke_android.rawQuery("SELECT COUNT(*) FROM ke_precobranza WHERE bcoref = '${bancoRefCom.uppercase()}' AND bcoref != '' AND bcocod = '$codigoBancoComplemento';", null)
            if(cursor.moveToFirst()){
                val resultEncontrado = cursor.getInt(0)
                if(resultEncontrado > 0){
                    Toast.makeText(this, "Ya se realizó un pago con este banco y esta referencia en complemento", Toast.LENGTH_SHORT).show()
                    return true
                }
            }
            cursor.close()
        }

        if (montoMinimo > (pago + pagoCom)){
            Toast.makeText(this, "El monto que registra es insuficiente para el pago", Toast.LENGTH_SHORT).show()
            return true
        }

        val diferencia = if (monedaDivisaCom) true else ((montoMinimo * APP_PORCENTAJE_COMPLEMENTO) <= pago)

        //si la diferencia entre el monto reportado y el monto minimo es mayor a un 10%, no se permite el complemento
        if(!diferencia) {
            Toast.makeText(this, "Monto Excedido para complemento", Toast.LENGTH_SHORT).show()
            return true
        }

        return false
    }

    private fun validacionesPrincipales(fechaRegistro: String, pago: Double, bancoRef: String, banco: String, transferencia: Boolean, efectivo: Boolean): Boolean {
        if (fechaRegistro == ""){
            Toast.makeText(this, "Seleccione una fecha", Toast.LENGTH_SHORT).show()
            return true
        }

        if(pago == 0.00){
            Toast.makeText(this, "Ingrese un valor valido en el monto a registrar", Toast.LENGTH_SHORT).show()
            return true
        }

        if ((bancoRef == "") && transferencia){
            Toast.makeText(this, "Debe ingresar una referencia bancaria", Toast.LENGTH_SHORT).show()
            return true
        }

        if (banco == "" && transferencia){
            Toast.makeText(this, "Debe seleccionar un banco", Toast.LENGTH_SHORT).show()
            return true
        }

        //If que valida que si la cobranza es en tranferencia no se repita el banco y la referencia
        if(!efectivo){
            val cursor = ke_android.rawQuery("SELECT COUNT(*) FROM ke_precobranza WHERE bcoref = '${binding.etCxcRefMain.text.toString().uppercase()}' AND bcoref != '' AND bcocod = '$codigoBancoCompleto';", null)
            if(cursor.moveToFirst()){
                val resultEncontrado = cursor.getInt(0)
                if(resultEncontrado > 0){
                    Toast.makeText(this, "Ya se realizó un pago con este banco y esta referencia", Toast.LENGTH_SHORT).show()
                    return true
                }
            }
            cursor.close()
        }
        return false
    }



    private fun verificarSiHayDescuentos(documento:String):Boolean {
        val cursorDesc:Cursor = ke_android.rawQuery("SELECT edodcto FROM ke_precobdcto WHERE documento = '${documento}'", null)
        if(cursorDesc.count > 0){
            cursorDesc.close()
            return true
        }else{
            cursorDesc.close()
            return false
        }
    }

    private fun cargarBancosCom(Moneda:String){

        listaInfoBancosCm.clear()
        listaBancosCm.clear()

        ke_android = conn.writableDatabase
        var bancos:Bancos
        var moneda:Double = 0.00

        if(Moneda == "USD"){
            moneda = 2.00

        }else if(Moneda == "BSS"){
            moneda = 1.00
        }

        val cursorBancos:Cursor = ke_android.rawQuery("SELECT DISTINCT codbanco, nombanco,cuentanac, inactiva, fechamodifi FROM listbanc WHERE inactiva = 0 AND cuentanac = $moneda", null)
        while(cursorBancos.moveToNext()){
            bancos = Bancos()
            bancos.codbanco     = cursorBancos.getString(0)
            bancos.nombanco     = cursorBancos.getString(1)
            bancos.cuentanac    = cursorBancos.getDouble(2)
            bancos.inactiva     = cursorBancos.getDouble(3)
            bancos.fechamodifi  = cursorBancos.getString(4)
            listaBancosCm.add(bancos)

        }
        binding.spCxcBancoCom.setText("Seleccione un banco...")
        actualizarBancosCm()
        var adapterBancos: ArrayAdapter<CharSequence>
        adapterBancos = ArrayAdapter(this, R.layout.spinner_bancos, listaInfoBancosCm as List<CharSequence>)
        binding.spCxcBancoCom.setAdapter(adapterBancos)
        adapterBancos.notifyDataSetChanged()

        cursorBancos.close()

    }
    //Funcion que actua en cada evento que se realice para saber si se excluyen las retenciones o no
    private fun retenciones(){
        if(binding.cbExcReten.isChecked){

            if (binding.rbCxcBssMain.isChecked){
                cargarSaldos("BSS", listaDocsSeleccionados, false)
                recalcularComplemento()
                pagaRetenciones = false

            }else if(binding.rbCxcDivisasMain.isChecked){
                cargarSaldos("USD", listaDocsSeleccionados, false)
                recalcularComplemento()
                pagaRetenciones = false
            }
            binding.tvCxcReten.setTextColor(Color.BLACK)

        }else{

            if (binding.rbCxcBssMain.isChecked){
                cargarSaldos("BSS", listaDocsSeleccionados, true)
                recalcularComplemento()
                pagaRetenciones = true

            } else if(binding.rbCxcDivisasMain.isChecked) {
                cargarSaldos("USD", listaDocsSeleccionados, true)
                recalcularComplemento()
                pagaRetenciones = true
            }
            binding.tvCxcReten.setTextColor(Color.RED)

        }
    }
    private fun mostrandoComplemento() {
        var montoComple = "0.0"

        if (binding.cbCxcComplemento.isChecked){
            val montoEscrito: Double = if(binding.etCxcMontoMain.text.toString() == "") 0.0 else binding.etCxcMontoMain.text.toString().toDouble()
            val montoDado : Double = if(binding.tvCxcTotal.text.toString() == "") 0.0 else binding.tvCxcTotal.text.toString().toDouble()


            //var diferencia = false

            var diferencia = if (binding.rbCxcDivisasCom.isChecked) {
                true
            } else {
                ((valorReal(montoDado * APP_PORCENTAJE_COMPLEMENTO) <= montoEscrito))
            }

            if(!diferencia){
                //TE QUEDASTE VALIDANDO COMPLEMENTO
                binding.tvPrecioMostrarComplemento.text = "Monto Complementario supera el ${Math.round((1 - APP_PORCENTAJE_COMPLEMENTO) * 100)}%"
                binding.tvPrecioMostrarComplemento.setBackgroundColor(Color.RED)
            } else {
                if (binding.rbCxcDivisasMain.isChecked){ //En el caso de que este seleccionado divisas

                    if (binding.rbCxcDivisasCom.isChecked){ //En el caso de que este seleccionado divisas en el prinipal, y divisas en complemento

                        montoComple = "${((montoDado - montoEscrito) * 100.0).roundToInt() / 100.0} $"

                    } else if (binding.rbCxcBssCom.isChecked){ //En el caso de que este seleccionado divisas en el prinipal, y bolivares en complemento

                        montoComple = "${(((((montoDado - montoEscrito) * tasaCambioSeleccionadaPrincipal) + 0.01 ) * 100.0).roundToInt() / 100.0)} Bs."

                    }

                } else if (binding.rbCxcBssMain.isChecked){ //En el caso de que este seleccionado bolivares

                    if (binding.rbCxcDivisasCom.isChecked){ //En el caso de que este seleccionado bolivares en el prinipal, y divisas en complemento

                        montoComple = "${(((montoDado - montoEscrito) / tasaCambioSeleccionadaPrincipal) * 100.0).roundToInt() / 100.0} $"

                    } else if (binding.rbCxcBssCom.isChecked){ //En el caso de que este seleccionado bolivvares en el prinipal, y bolivares en complemento

                        montoComple = "${((montoDado - montoEscrito) * 100.0).roundToInt() / 100.0} Bs."

                    }

                }

                binding.tvPrecioMostrarComplemento.text = "Monto a pagar: $montoComple"
                binding.tvPrecioMostrarComplemento.setBackgroundColor(Color.rgb(1, 76, 131))
            }


        }


    }


    private fun cargarDetalleDescuentos() {
        var listaDesc: ArrayList<Descuentos> = ArrayList()
        listaDesc = listaDescuentos

        /*for (i in listaDesc.indices){
            println("${listaDesc[i].nrodoc}")
        }*/

        var dialog: DialogDescuento = DialogDescuento()
        dialog.DialogDescuento(this, listaDesc)
    }




    //  -- esto va a una funcion
    private fun recalcularComplemento(){
        val montototal = binding.tvCxcTotal.text.toString().toDouble()
        var montoReciboCom       = 0.00
        //  -- esto va a una funcion
        if(binding.rbCxcDivisasMain.isChecked){
            //si el monto es en dolares, saco el 10% del mismo
            montoReciboCom = montototal * valorReal(1 - APP_PORCENTAJE_COMPLEMENTO)
            montoReciboCom = valorReal(montoReciboCom)

            if(binding.rbCxcDivisasCom.isChecked){
                binding.etCxcMontoCom.hint = ""
                binding.etCxcMontoCom.hint = "Monto máximo: $montoReciboCom"
                //binding.etCxcMontoCom.hint = "Monto Complementario"
            }else{
                montoReciboCom = valorReal(montoReciboCom * tasaCambioSeleccionadaPrincipal)
                binding.etCxcMontoCom.hint = ""
                binding.etCxcMontoCom.hint = "Monto máximo: $montoReciboCom"
                //binding.etCxcMontoCom.hint = "Monto Complementario"
            }

        }else{
            //si el monto es en bss, saco el 10% del mismo
            montoReciboCom = montototal * valorReal(1 - APP_PORCENTAJE_COMPLEMENTO)
            montoReciboCom = valorReal(montoReciboCom)

            if(binding.rbCxcDivisasCom.isChecked){
                binding.etCxcMontoCom.hint = ""
                montoReciboCom /= tasaCambioSeleccionadaPrincipal
                montoReciboCom = valorReal(montoReciboCom)
                binding.etCxcMontoCom.hint = "Monto máximo: $montoReciboCom"
                //binding.etCxcMontoCom.hint = "Monto Complementario"
            }else{

                binding.etCxcMontoCom.hint = ""
                binding.etCxcMontoCom.hint = "Monto máximo: $montoReciboCom"
                //binding.etCxcMontoCom.hint = "Monto Complementario"
            }
        }
    }

    private fun procesamientodeDatos() {
        var netocobrado = 0.00
        val ignore: Boolean
        val ignorecm: Boolean

        val retennn = if (binding.cbExcReten.isChecked) 1 else 0
        //val diferencialCambiario = if (binding.cbDocDifCambio.isChecked) 1 else 0

        /*if (existReten(listaDocumentos)){
            return
        }*/

        //Ventana emergente que le indica al vendedor a la hora de precionar el boton que le mande una foto de las retenciones del documento a su analista
        /*if (binding.tvCxcReten.text.toString().toDouble() > 0.00 && !binding.cbExcReten.isChecked){
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Retenciones")
            builder.setMessage("Recuerde subir las imágenes de las retenciones de forma nítida a su respectivo analista de cobranza")

            builder.setPositiveButton("OK") { dialog, which ->

            }
            builder.show()

        }*/
        //Ventana emergente que le indica al vendedor que se comunique con su analista debido a que la suma de las retenciones da en negativo
        if (binding.tvCxcReten.text.toString().toDouble() < 0.00){
            val builder = AlertDialog.Builder(this)
            builder.setTitle(Html.fromHtml("<font color='#FF0000'>Advertencia</font>"))
            builder.setMessage(Html.fromHtml("<font color='#FF0000'>Comuníquese con su Analista de Cobranza para tratar este caso<br>Causa: Retención en negativo</font>"))

            builder.setPositiveButton("OK") { dialog, which ->

            }
            builder.show()

        }

        //If para validar que se selecciono la fecha
        if(fechaQuery.equals("")){
            Toast.makeText(this, "Debe seleccionar una fecha de pago", Toast.LENGTH_SHORT).show()
            return
        }

        //2023-06-21 se cambio retenciones para otro modulo
        if(!binding.cbExcReten.isChecked && listaRetGuardada.size < 1){
            Toast.makeText(this, "Debe agregar los comprobantes de retención", Toast.LENGTH_SHORT).show()
            return
        }
        //Guarda si el boton de efectivo principal esta marcado
        ignore = binding.rbCxcEfectivoMain.isChecked
        //Guarda si el boton de efectivo complementario esta marcado
        ignorecm = binding.rbCxcEfectivoCom.isChecked

        //If que verifica si el ID del banco esta vacio
        if (codigoBancoCompleto == "" && !ignore){
            //Si ignore es true significa que se pago en efectivo y continua el proceso con normalidad
            Toast.makeText(this, "Debes Seleccionar un banco", Toast.LENGTH_SHORT).show()
            return
        }

        //Valida que la cantidad introducida para la transaccion sea mayor a 0
        if(binding.etCxcMontoMain.text.toString() == "" || binding.etCxcMontoMain.toString().equals(null) || binding.etCxcMontoMain.text.toString().toDouble() == 0.00){
            Toast.makeText(this, "Debes introducir una cantidad mayor a 0", Toast.LENGTH_SHORT).show()
            return
        }
        //Valida que lo puesto en la referencia bancaria no este en blanco
        if((binding.etCxcRefMain.text.toString() == "" || binding.etCxcRefMain.text.toString().equals(null)) && !ignore){
            //Si ignore es false significa que no se ha puesto en banco para terminar la transaccion
            Toast.makeText(this, "Debes introducir la referencia bancaria principal", Toast.LENGTH_SHORT).show()
            return
        }

        /*if(binding.etCxcRefMain.text.toString().equals("") || binding.etCxcRefMain.text.toString().equals(null)){
            //Si ignore es true significa que se pago en efectivo y continua el proceso con normalidad
            if(ignore == true){
                //sigue el proceso
            }
            //Si ignore es false significa que no se ha puesto en banco para terminar la transaccion
            else{
                Toast.makeText(this, "Debes introducir la referencia bancaria principal", Toast.LENGTH_SHORT).show()
                return
            }

            //todo: 2023-04-03 posible sustitucion del if de arriba
            *//*if(!ignore){
                //Si ignore es false significa que no se ha puesto en banco para terminar la transaccion
                Toast.makeText(this, "Debes introducir la referencia bancaria principal", Toast.LENGTH_SHORT).show()
                return
            }
            //Si ignore es true significa que se pago en efectivo y continua el proceso con normalidad

            //sigue el proceso*//*

        }*/
        var numVerificador = 0
        var numVerificadorComple = 0
        //If que valida que si la cobranza es en tranferencia no se repita el banco y la referencia
        if(!ignore){
            //numVerificador += verificacionReferencia(binding.etCxcRefMain.text.toString().uppercase(), "ke_precobranza", codigoBancoCompleto)
            numVerificador += verificacionReferencia(binding.etCxcRefMain.text.toString().uppercase(), "ke_referencias", codigoBancoCompleto)
            /*val cursor = ke_android.rawQuery("SELECT COUNT(*) FROM ke_precobranza WHERE bcoref = '${binding.etCxcRefMain.text.toString().uppercase()}' AND bcoref != '' AND bcocod = '$codigoBancoCompleto';", null)
            if(cursor.moveToFirst()){
                val resultEncontrado = cursor.getInt(0)
                if(resultEncontrado > 0){
                    Toast.makeText(this, "Ya se realizó un pago con este banco y esta referencia", Toast.LENGTH_SHORT).show()
                    return
                }
            }
            cursor.close()*/
        }
        //If que valida que si la cobranza es en tranferencia no se repita el banco y la referencia (para complemento)
        if(!ignorecm && binding.cbCxcComplemento.isChecked){
            //numVerificadorComple += verificacionReferencia(binding.etCxcRefCom.text.toString().uppercase(), "ke_precobranza", codigoBancoComplemento)
            numVerificadorComple += verificacionReferencia(binding.etCxcRefCom.text.toString().uppercase(), "ke_referencias", codigoBancoComplemento)
            /*val cursor = ke_android.rawQuery("SELECT COUNT(*) FROM ke_precobranza WHERE bcoref = '${binding.etCxcRefCom.text.toString().uppercase()}' AND bcoref != '' AND bcocod = '$codigoBancoComplemento';", null)
            if(cursor.moveToFirst()){
                val resultEncontrado = cursor.getInt(0)
                if(resultEncontrado > 0){
                    Toast.makeText(this, "Ya se realizó un pago con este banco y esta referencia", Toast.LENGTH_SHORT).show()
                    return
                }
            }
            cursor.close()*/
        }

        if(numVerificador != 0){
            Toast.makeText(this, "Pago Principal: Referencia y banco utilizados previamente.", Toast.LENGTH_SHORT).show()
            return
        }
        if(numVerificadorComple != 0){
            Toast.makeText(this, "Pago Complemento: Referencia y banco utilizados previamente.", Toast.LENGTH_SHORT).show()
            return
        }





        referenciaPrincipal = binding.etCxcRefMain.text.toString().trim().uppercase()
        montoRec            = binding.etCxcMontoMain.text.toString().toDouble()


        //declaro variables auxiliares
        var ivaTot   = binding.tvCxcIva.text.toString().toDouble()
        var fleteTot = binding.tvCxcFlete.text.toString().toDouble()
        //2023-06-07 Nueva variable para llevar el total de las retenciones para ser restada con iva y flete para que el abono sea pagado sin retenciones
        val retTot   = if(binding.cbExcReten.isChecked) 0.0 else binding.tvCxcReten.text.toString().toDouble() // <----------------------- matar retencion TODO

        //dependiendo del tipo de pago, elijo ya sea monto minimo total o si es abono, la
        //cantidad minima que seria flete + iva (de estar pagos, asumo un valor de 1)

        if(listaDocumentos.size > 1){

            binding.rbCxcAbonoMain.visibility = View.INVISIBLE
            binding.rbCxcAbonoMain.isChecked  = false

            if(binding.rbCxcCompMain.isChecked){
                montoMinimoRec = binding.tvCxcTotal.text.toString().toDouble() - 0.5

            }
            // Lo que el cliente da es menor al total y complemento NO esta marcado
            if(montoRec < montoMinimoRec && !binding.cbCxcComplemento.isChecked){//PUEDE ENTRAR AQUI EN ALGUN CASO?
                //Toast.makeText(this, "El monto del recibo no debe ser menor a la suma del IVA y el Flete", Toast.LENGTH_SHORT).show()
                if(binding.rbCxcCompMain.isChecked){
                    Toast.makeText(this, "El monto del recibo no debe ser menor al Total", Toast.LENGTH_SHORT).show()

                }
                else if (binding.rbCxcAbonoMain.isChecked){
                    Toast.makeText(this, "El monto del recibo no debe ser menor a la suma del IVA y el Flete", Toast.LENGTH_SHORT).show()
                }
            }
            // Lo que el cliente da es menor al total y complemento SI esta marcado
            // 2023-07-06 se coloco un or que suma la cantidad previamente restada a la variable montoMinimoRec debido a un caso donde no entre a ningun if
            // esto es debido a que si se paga un precio cercano al precio total del documento pero ligeramente menor no entra a ningun condicional ejemplo
            // total = 92.25
            // ingresado = 90
            // total restado 8.75 <-- (montoMinimoRec)
            // REDUNDANTE
            else if((montoRec < montoMinimoRec && binding.cbCxcComplemento.isChecked) || (montoRec < (montoMinimoRec + 0.5) && binding.cbCxcComplemento.isChecked) ){
                //si el monto del recibo es menor al monto minimo pero el complemento esta marcado
                //var diferencia = ((montoMinimoRec / montoRec) * 100) - 100
                // 2023-03-29 se comenta por la necesidad de que al seleccionar divisa estas no se les sera aplicada el 10%
                //val diferencia = (valorReal(montoMinimoRec * 0.9) <= montoRec)
                //println(diferencia)

                var diferencia = if (binding.rbCxcDivisasCom.isChecked) {
                    true
                } else {
                    ((valorReal(montoMinimoRec * APP_PORCENTAJE_COMPLEMENTO) <= montoRec))
                }

                //si la diferencia entre el monto reportado y el monto minimo es mayor a un 10%, no se permite el complemento
                if(!diferencia){
                    Toast.makeText(this, "Monto Excedido para complemento", Toast.LENGTH_SHORT).show()
                    return

                    //de manera contraria, si
                }else if(diferencia){

                    //valido montos del complemento
                    if(binding.etCxcMontoCom.text.toString().equals("") || binding.etCxcMontoCom.text.toString().equals(null) ){

                        Toast.makeText(this, "Monto de complemento no puede estar vacío", Toast.LENGTH_SHORT).show()
                        return

                    }

                    //valido montos en 0 y banco vacio del complemento, asi como tambien
                    //la referencia.
                    if(binding.etCxcRefCom.text.toString().equals("") || binding.etCxcRefCom.text.toString().equals(null) ){
                        if(ignorecm == true){

                        }else{
                            Toast.makeText(this, "Referencia del complemento no puede estar vacía", Toast.LENGTH_SHORT).show()
                            return
                        }

                    }
                    if(codigoBancoComplemento.equals("")){
                        if(ignorecm == true){

                        }else{
                            Toast.makeText(this, "Debes seleccionar un banco para el complemento", Toast.LENGTH_SHORT).show()
                            return
                        }


                    }
                    var montoRecComp =  binding.etCxcMontoCom.text.toString().toDouble()

                    var complementoMontoStandard = if(binding.rbCxcBssCom.isChecked){ // if que valida que moneda se seleccionó en complemento
                        // Si se selecciona bolivares hara la conversion a dolares
                        (montoRecComp / tasaCambioSeleccionadaPrincipal)
                    } else{
                        // Si se selecciona dolares la moneda permanecera y se guardara en otra variable
                        montoRecComp
                    }

                    var montoPrinciStandard = if(binding.rbCxcBssMain.isChecked){// if que valida que moneda se seleccionó en principal
                        // Si se selecciona bolivares hara la conversion a dolares
                        montoRec / tasaCambioSeleccionadaPrincipal
                    } else{
                        // Si se selecciona dolares la moneda permanecera y se guardara en otra variable
                        montoRec
                    }

                    var montoComprar = if(binding.rbCxcBssMain.isChecked){// if que valida que moneda se seleccionó en principal
                        // Si se selecciona bolivares hara la conversion a dolares
                        montoMinimoRec / tasaCambioSeleccionadaPrincipal
                    } else{
                        // Si se selecciona dolares la moneda permanecera y se guardara en otra variable
                        montoMinimoRec
                    }
                    //Posiuble ERROR encontrado, no se transforma la moneda
                    if((complementoMontoStandard + montoPrinciStandard) < montoComprar){
                        Toast.makeText(this, "Montos insuficientes para completar ambos recibos 1", Toast.LENGTH_SHORT).show()
                        return
                    }else{

                        referenciaCm = binding.etCxcRefCom.text.toString().trim().uppercase()
                        if(referenciaCm.equals("") && !ignorecm){
                            Toast.makeText(this, "Falta la referencia bancaria de complemento", Toast.LENGTH_SHORT).show()
                            return
                        }

                        var ll_commit = false
                        ke_android = conn.writableDatabase

                        //listas con el tipo de datos para los recibos
                        var listaReciboPrCabecera:ArrayList<CXC>   = ArrayList()
                        var listaReciboPrLineas:  ArrayList<CXC>   = ArrayList()
                        var listaReciboCmCabecera:ArrayList<CXC>   = ArrayList()
                        var listaReciboCmLineas:  ArrayList<CXC>   = ArrayList()

                        listaDocumentos.sortBy { it.fechaDocs }//llenado de datos de cabecera
                        var cxc = CXC()
                        cxc.id_recibo  = nroPrecobranza
                        cxc.tipoRecibo = "W"
                        cxc.codigoVend = cod_usuario.toString()
                        cxc.kecxc_id   = tasaId
                        cxc.tasadia    = tasaCambioSeleccionadaPrincipal
                        cxc.fchrecibo  = fechatasaH
                        cxc.clicontesp = "" //esto lo jalo de  la lista de docs?
                        cxc.moneda     = monedaSeleccionadaPr
                        if(ignore){
                            cxc.bcocod     = ""
                            cxc.bcoref     = ""
                            cxc.efectivo   = montoRec
                            cxc.fchvigen   = fechaSuma(fechaActual, 60)
                        }else{
                            cxc.bcocod     = codigoBancoCompleto
                            cxc.bcomonto   = montoRec
                            cxc.bcoref     = referenciaPrincipal
                            cxc.fchvigen   = fechaSuma(fechaActual, 5)
                        }
                        cxc.edorec     = "0"
                        cxc.fchhr      = fechaActual

                        //genero cabecera del complemento
                        var comp = CXC()
                        comp.id_recibo  = nroComplemento
                        comp.tipoRecibo = "W"
                        comp.codigoVend = cod_usuario.toString()
                        comp.kecxc_id   = tasaId
                        comp.tasadia    = tasaCambioSeleccionadaPrincipal
                        comp.fchrecibo  = fechatasaH
                        comp.clicontesp = "" //esto lo jalo de  la lista de docs?
                        comp.moneda     = monedaSeleccionadaCm

                        if(ignorecm){
                            comp.bcocod     = ""
                            comp.bcoref     = ""
                            comp.efectivo   = montoRecComp
                            comp.fchvigen   = fechaSuma(fechaActual, 60)
                        }else{
                            comp.bcocod     = codigoBancoComplemento
                            comp.bcoref     = referenciaCm
                            comp.bcomonto   = montoRecComp
                            comp.fchvigen   = fechaSuma(fechaActual, 5)
                        }

                        comp.edorec     = "0"
                        comp.fchhr      = fechaActual


                        //en este ciclo lleno la lista de precobradocs
                        for(i in listaDocumentos.indices){
                            var cxclineas = CXC()

                            cxclineas.id_recibo = nroPrecobranza
                            cxclineas.agencia   = listaDocumentos[i].agencia
                            cxclineas.tipodoc   = listaDocumentos[i].tipodoc
                            cxclineas.documento = listaDocumentos[i].documento
                            listaReciboPrLineas.add(cxclineas)

                        }

                        //en este ciclo, agrego las retenciones
                        for (i in listaReciboPrLineas.indices){
                            if(listaReciboPrLineas[i].agencia.equals("002") && (listaReciboPrLineas[i].documento == listaDocumentos[i].documento)){
                                listaDocumentos[i].cbsretflete = 0.00

                            }

                            //llenado de campos de retenciones
                            //retenciones fase 1
                            for (j in listaRetGuardada.indices){
                                if(listaRetGuardada[j].nrodoc == listaReciboPrLineas[i].documento){
                                    if(listaRetGuardada[j].tiporet == "iva"){
                                        //si es de iva
                                        listaReciboPrLineas[i].nroret    = listaRetGuardada[j].nroret
                                        listaReciboPrLineas[i].fchemiret = listaRetGuardada[j].fecharet
                                        listaReciboPrLineas[i].bsretiva  = - listaRetGuardada[j].montoret
                                        listaReciboPrLineas[i].refret    = listaRetGuardada[j].refret

                                    }

                                    if(listaRetGuardada[j].tiporet == "flete"){
                                        //si es de flete
                                        listaReciboPrLineas[i].nroretfte    = listaRetGuardada[j].nroret
                                        listaReciboPrLineas[i].fchemirfte   = listaRetGuardada[j].fecharet
                                        listaReciboPrLineas[i].bsretfte     = - listaRetGuardada[j].montoret
                                        listaReciboPrLineas[i].refretfte    = listaRetGuardada[j].refret
                                    }

                                    if(listaRetGuardada[j].tiporet == "parme"){
                                        //si es de parme
                                        listaReciboPrLineas[i].retmun_nro    = listaRetGuardada[j].nroret
                                        listaReciboPrLineas[i].retmun_fch    = listaRetGuardada[j].fecharet
                                        listaReciboPrLineas[i].retmun_mto    = - listaRetGuardada[j].montoret
                                        listaReciboPrLineas[i].retmun_cod    = listaRetGuardada[j].refret
                                    }
                                }
                            }


                            //descuento ivas y fletes ( de haberlos)
                            if(listaReciboPrLineas[i].documento == listaDocumentos[i].documento && listaDocumentos[i].tipodocv == "FAC"){

                                if(listaDocumentos[i].bsiva - listaDocumentos[i].bsmtoiva <= 0.00 ){
                                    listaReciboPrLineas[i].bsmtoiva = 0.00
                                    listaReciboPrLineas[i].doliva   = 0.00

                                }else{


                                    //descuento ivas del monto del recibo original .-
                                    var restaIvadol = if (binding.cbExcReten.isChecked) listaDocumentos[i].dtotimpuest else listaDocumentos[i].dtotimpuest - listaDocumentos[i].cdretencioniva
                                    //2023-04-03 comentario por no tener en cuenta cuando se escluyen retenciones
                                    //var restaIvadol = listaDocumentos[i].dtotimpuest - listaDocumentos[i].cdretencioniva
                                    var restaIvabss = if (binding.cbExcReten.isChecked) listaDocumentos[i].bsiva else listaDocumentos[i].bsiva - listaDocumentos[i].cbsretencioniva
                                    //var restaIvabss = listaDocumentos[i].bsiva - listaDocumentos[i].cbsretencioniva

                                    if(binding.rbCxcDivisasMain.isChecked){
                                        //hago el descuento del iva del nmonto de pago
                                        montoRec -= restaIvadol

                                        if(montoRec < 0.00){
                                            Toast.makeText(this, "Monto insuficiente", Toast.LENGTH_SHORT).show()
                                            return
                                        }

                                        listaReciboPrLineas[i].bscobro  += (if (binding.cbExcReten.isChecked) listaDocumentos[i].bsiva else listaDocumentos[i].bsiva - listaDocumentos[i].cbsretencioniva)
                                        listaReciboPrLineas[i].bsmtoiva = listaDocumentos[i].bsiva
                                        listaReciboPrLineas[i].doliva   = if (binding.cbExcReten.isChecked) listaDocumentos[i].dtotimpuest else listaDocumentos[i].dtotimpuest - listaDocumentos[i].cdretencioniva
                                        //2023-04-03 comentado por mal calculo?
                                        //listaReciboPrLineas[i].tnetoddol  += listaReciboPrLineas[i].doliva
                                        listaReciboPrLineas[i].tnetoddol  += (if (binding.cbExcReten.isChecked) listaDocumentos[i].dtotimpuest else listaDocumentos[i].dtotimpuest - listaDocumentos[i].cdretencioniva)

                                    }
                                    else{
                                        montoRec -= restaIvabss
                                        if(montoRec < 0.00){
                                            Toast.makeText(this, "Monto insuficiente", Toast.LENGTH_SHORT).show()
                                            return
                                        }
                                        listaReciboPrLineas[i].bscobro  += ((if (binding.cbExcReten.isChecked) listaDocumentos[i].bsiva else listaDocumentos[i].bsiva - listaDocumentos[i].cbsretencioniva))
                                        listaReciboPrLineas[i].bsmtoiva = listaDocumentos[i].bsiva
                                        listaReciboPrLineas[i].doliva   = if (binding.cbExcReten.isChecked) listaDocumentos[i].dtotimpuest else listaDocumentos[i].dtotimpuest - listaDocumentos[i].cdretencioniva
                                        listaReciboPrLineas[i].tnetodbs += (if (binding.cbExcReten.isChecked) listaDocumentos[i].bsiva else listaDocumentos[i].bsiva - listaDocumentos[i].cbsretencioniva)
                                        //listaReciboPrLineas[i].tnetodbs += listaDocumentos[i].bsiva

                                    }

                                }

                                //descuento del flete de los documentos
                                if(listaDocumentos[i].bsflete - listaDocumentos[i].bsmtofte <= 1){
                                    listaReciboPrLineas[i].bsmtofte = 0.00
                                    listaReciboPrLineas[i].dolflete = 0.00

                                }else{

                                    if(binding.rbCxcDivisasMain.isChecked){

                                        //si aqui ya llega el monto en 0 o menos
                                        if(montoRec <= 0.00){
                                            Toast.makeText(this, "Monto insuficiente", Toast.LENGTH_SHORT).show()
                                            return
                                        }
                                        //descuento del monto del recibo, el flete
                                        montoRec -= if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dFlete - listaDocumentos[i].cdretflete

                                        //de no llegar a menos de cero, lo agrego al monto cobrado en bss
                                        if(binding.cbExcReten.isChecked){
                                            listaReciboPrLineas[i].bscobro  += listaDocumentos[i].bsflete
                                        }else{
                                            listaReciboPrLineas[i].bscobro  += listaDocumentos[i].bsflete - listaDocumentos[i].cbsretflete
                                        }
                                        //2023-04-04 Repeticion inecesaria
                                        //listaReciboPrLineas[i].bscobro  += valorReal((listaDocumentos[i].bsflete - listaDocumentos[i].cbsretflete))
                                        listaReciboPrLineas[i].bsmtofte = listaDocumentos[i].bsflete
                                        listaReciboPrLineas[i].dolflete = (if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dFlete - listaDocumentos[i].cdretflete)
//2023-04-04 Actualizado para incluir excluir retenciones
                                        /*if(binding.rbCxcDivisasMain.isChecked == true){
                                            listaReciboPrLineas[i].tnetoddol  += listaDocumentos[i].dFlete
                                        }else{
                                            listaReciboPrLineas[i].tnetodbs += listaDocumentos[i].bsflete

                                        }*/

                                        if(binding.rbCxcDivisasMain.isChecked){
                                            listaReciboPrLineas[i].tnetoddol  += (if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dFlete - listaDocumentos[i].cdretflete)
                                        }else{
                                            listaReciboPrLineas[i].tnetodbs += (if (binding.cbExcReten.isChecked) listaDocumentos[i].bsflete else listaDocumentos[i].bsflete - listaDocumentos[i].cbsretflete)
                                            //listaReciboPrLineas[i].tnetodbs += listaDocumentos[i].bsiva
                                        }
                                        println("")


                                    }else{

                                        //si aqui ya llega el monto en 0 o menos
                                        if(montoRec <= 0.00){
                                            Toast.makeText(this, "Monto insuficiente", Toast.LENGTH_SHORT).show()
                                            return
                                        }
                                        //descuento del monto del recibo, el flete
                                        montoRec -= if (binding.cbExcReten.isChecked) listaDocumentos[i].bsflete else listaDocumentos[i].bsflete - listaDocumentos[i].cbsretflete

                                        //de no llegar a menos de cero, lo agrego al monto cobrado en bss
                                        //2023-0404 Repeticion inecesaria
                                        //listaReciboPrLineas[i].bscobro  += valorReal((listaDocumentos[i].bsflete - listaDocumentos[i].cbsretflete))
                                        if(binding.cbExcReten.isChecked){
                                            listaReciboPrLineas[i].bscobro  += listaDocumentos[i].bsflete
                                        }else{
                                            listaReciboPrLineas[i].bscobro  += listaDocumentos[i].bsflete - listaDocumentos[i].cbsretflete
                                        }
                                        listaReciboPrLineas[i].dolflete = if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dFlete - listaDocumentos[i].cdretflete

                                        listaReciboPrLineas[i].bsmtofte = listaDocumentos[i].bsflete

                                        if(binding.rbCxcDivisasMain.isChecked){
                                            listaReciboPrLineas[i].tnetoddol  += (if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dtotimpuest - listaDocumentos[i].cdretflete)
                                        }else{
                                            listaReciboPrLineas[i].tnetodbs += (if (binding.cbExcReten.isChecked) listaDocumentos[i].bsflete else listaDocumentos[i].bsflete - listaDocumentos[i].cbsretflete)
                                            //listaReciboPrLineas[i].tnetodbs += listaDocumentos[i].bsiva
                                            println("")
                                        }

                                    }

                                }

                            }else{
                                if(listaDocumentos[i].bsflete - listaDocumentos[i].bsmtofte <= 1){
                                    listaReciboPrLineas[i].bsmtofte = 0.00
                                    listaReciboPrLineas[i].dolflete = 0.00
                                }else{

                                    var fleteaCobrar = if (binding.cbExcReten.isChecked) listaDocumentos[i].bsflete else listaDocumentos[i].bsflete - listaDocumentos[i].cbsretflete
                                    listaReciboPrLineas[i].bscobro  += fleteaCobrar

                                    if(binding.rbCxcDivisasMain.isChecked){
                                        montoRec -= if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dFlete - listaDocumentos[i].cdretflete
                                        listaReciboPrLineas[i].bsmtofte = listaDocumentos[i].bsflete
                                        listaReciboPrLineas[i].dolflete = if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dFlete - listaDocumentos[i].cdretflete
                                        listaReciboPrLineas[i].tnetoddol  += if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dFlete - listaDocumentos[i].cdretflete
                                    }else{
                                        montoRec -= if (binding.cbExcReten.isChecked) listaDocumentos[i].bsflete else listaDocumentos[i].bsflete - listaDocumentos[i].cbsretflete
                                        listaReciboPrLineas[i].bsmtofte = listaDocumentos[i].bsflete
                                        listaReciboPrLineas[i].dolflete = if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dFlete - listaDocumentos[i].cdretflete
                                        listaReciboPrLineas[i].tnetodbs += if (binding.cbExcReten.isChecked) listaDocumentos[i].bsflete else listaDocumentos[i].bsflete - listaDocumentos[i].cbsretflete


                                    }

                                }
                            }

                        }

                        //llenado de los netos OJOOOOO
                        for (i in listaReciboPrLineas.indices){

                            if(montoRec < 1){
                                // borré el return
                            }else{
                                if(binding.rbCxcDivisasMain.isChecked){
                                    var netoTV = binding.tvCxcNeto.text.toString().toDouble()

                                    if(netoTV > 0.00){
                                        var netoRealenDoc = listaDocumentos[i].netorestante

                                        if( montoRec >= netoRealenDoc){
                                            var bscobrado = netoRealenDoc * listaDocumentos[i].tasadoc

                                            listaReciboPrLineas[i].bscobro += bscobrado


                                            netocobrado =  listaDocumentos[i].netorestante
                                            listaReciboPrLineas[i].dolneto = netocobrado

                                            listaReciboPrLineas[i].tnetoddol += netocobrado
                                            montoRec -= netocobrado
                                            listaReciboPrLineas[i].ispagadoTotal = "1"
                                            //DESCUESTO DEBERIA IR POR AQUI CREO
                                        }

                                        else if(netoRealenDoc > montoRec && montoRec > 0) {

                                            var cobroAbono = montoRec * listaDocumentos[i].tasadoc

                                            listaReciboPrLineas[i].bscobro += cobroAbono
                                            netocobrado = montoRec
                                            listaReciboPrLineas[i].dolneto = valorReal(netocobrado)
                                            montoRec -= netocobrado
                                            listaReciboPrLineas[i].tnetoddol += netocobrado
                                            montoRec= valorReal(montoRec)

                                            listaReciboPrLineas[i].ispagadoTotal = "0"
                                            //DESCUESTO DEBERIA IR POR AQUI CREO
                                        }

                                    }
                                }

                                if(binding.rbCxcBssMain.isChecked){
                                    var netoRealenDoc = listaDocumentos[i].netorestante
                                    netoRealenDoc = valorReal(netoRealenDoc)

                                    //si el monto del recibo cubre el monto del documento
                                    if( montoRec > (netoRealenDoc * tasaCambioSeleccionadaPrincipal)){
                                        var bscobrado = valorReal(netoRealenDoc * listaDocumentos[i].tasadoc)
                                        listaReciboPrLineas[i].bscobro += bscobrado
                                        netocobrado = listaDocumentos[i].netorestante
                                        listaReciboPrLineas[i].dolneto = netocobrado
                                        listaReciboPrLineas[i].bsneto = valorReal(netoRealenDoc * tasaCambioSeleccionadaPrincipal)
                                        listaReciboPrLineas[i].tnetodbs  += valorReal(netoRealenDoc * tasaCambioSeleccionadaPrincipal)
                                        listaReciboPrLineas[i].ispagadoTotal = "1"
                                        montoRec -= valorReal(netoRealenDoc * tasaCambioSeleccionadaPrincipal)
                                        print("")

                                    }
                                    //si el monto del recibo no cubre el monto completo del documento (se comporta como un abono)
                                    else if((netoRealenDoc * tasaCambioSeleccionadaPrincipal) > montoRec && montoRec > 0) {
                                        netocobrado = montoRec / tasaCambioSeleccionadaPrincipal
                                        var cobroAbono = netocobrado * listaDocumentos[i].tasadoc
                                        listaReciboPrLineas[i].bscobro += valorReal(cobroAbono)
                                        var dolneto = netocobrado
                                        listaReciboPrLineas[i].dolneto    = valorReal(dolneto)
                                        listaReciboPrLineas[i].bsneto = montoRec
                                        listaReciboPrLineas[i].tnetodbs  += montoRec
                                        listaReciboPrLineas[i].ispagadoTotal = "0"
                                        montoRec -= montoRec
                                    }
                                }

                                //las lineas del recibo del complemento
                                /*for (i in listaReciboPrLineas.indices){
                                    //valido si fue pagado completo
                                    if(listaReciboPrLineas[i].ispagadoTotal.equals("1")){


                                    }else{
                                        //de no estar pago completo, le aplico el complemento
                                        var complineas = CXC()
                                        complineas.id_recibo = nroComplemento
                                        complineas.agencia   = listaReciboPrLineas[i].agencia
                                        complineas.tipodoc   = listaReciboPrLineas[i].tipodoc
                                        complineas.documento = listaReciboPrLineas[i].documento
                                        listaReciboCmLineas.add(complineas)
                                    }

                                }*/

                                //los bss del neto cobrado
                                /*recorrido de lista de descuentos para asignar cantidad según nro doc. (de tener)*/
                                for(j in listaDescuentos.indices){
                                    if(listaDescuentos[j].nrodoc == listaDocumentos[i].documento){
                                        listaReciboPrLineas[i].prcdsctopp = listaDescuentos[j].pordscto
                                    }
                                }

                            }
                        }

                        //recorrido del complemento
                        //tambien voy a pagar el neto pero con complemento de los documentos ya pagos
                        //las lineas del recibo del complemento
                        //EL PROBLEM ESTA POR AQUI ERROR
                        for (i in listaReciboPrLineas.indices){
                            //valido si fue pagado completo
                            if(listaReciboPrLineas[i].ispagadoTotal.equals("1")){


                            }else{
                                //de no estar pago completo, le aplico el complemento
                                var complineas = CXC()
                                complineas.id_recibo = nroComplemento
                                complineas.agencia   = listaReciboPrLineas[i].agencia
                                complineas.tipodoc   = listaReciboPrLineas[i].tipodoc
                                complineas.documento = listaReciboPrLineas[i].documento
                                listaReciboCmLineas.add(complineas)
                            }

                        }

                        //llenado neto del complemento
                        for (i in listaReciboCmLineas.indices) {

                            if(montoRecComp <= 0){

                            }else {
                                if(binding.rbCxcDivisasCom.isChecked){
                                    for(j in listaDocumentos.indices){
                                        if(listaDocumentos[j].documento == listaReciboCmLineas[i].documento){
                                            var netoRealenDoc = listaDocumentos[j].netorestante

                                            if(montoRecComp > netoRealenDoc){

                                                var bscobrado = netoRealenDoc * listaDocumentos[j].tasadoc

                                                listaReciboCmLineas[i].bscobro += bscobrado


                                                netocobrado =  listaDocumentos[j].netorestante
                                                listaReciboCmLineas[i].dolneto = netocobrado
                                                montoRecComp -= netocobrado
                                                listaReciboCmLineas[i].tnetoddol += netocobrado

                                            }else if(netoRealenDoc > montoRecComp && montoRecComp > 0) {


                                                var cobroAbono = montoRecComp * listaDocumentos[j].tasadoc

                                                listaReciboCmLineas[i].bscobro += cobroAbono
                                                netocobrado = montoRecComp
                                                listaReciboCmLineas[i].dolneto = valorReal(netocobrado)
                                                montoRecComp -= netocobrado
                                                montoRecComp  = valorReal(montoRecComp)
                                                listaReciboCmLineas[i].tnetoddol += netocobrado

                                            }

                                        }else{
                                            //do nothing papa
                                        }
                                    }

                                }

                                if(binding.rbCxcBssCom.isChecked){
                                    for(j in listaDocumentos.indices){
                                        if(listaReciboCmLineas[i].documento == listaDocumentos[j].documento){
                                            var netoRealenDoc = listaDocumentos[j].netorestante
                                            netoRealenDoc = valorReal(netoRealenDoc)

                                            //si el monto del recibo cubre el monto del documento
                                            if( montoRecComp > (netoRealenDoc * tasaCambioSeleccionadaPrincipal)){
                                                var bscobrado = valorReal(netoRealenDoc * listaDocumentos[j].tasadoc)
                                                listaReciboCmLineas[i].bscobro += bscobrado

                                                netocobrado = listaDocumentos[j].netorestante
                                                listaReciboCmLineas[i].dolneto = netocobrado
                                                listaReciboCmLineas[i].bsneto = valorReal(netoRealenDoc * tasaCambioSeleccionadaPrincipal)
                                                listaReciboCmLineas[i].tnetodbs += valorReal(netoRealenDoc * tasaCambioSeleccionadaPrincipal)
                                                montoRecComp -= valorReal(netoRealenDoc * listaDocumentos[j].tasadoc)
                                            }

                                            //si el monto del recibo no cubre el monto completo del documento (se comporta como un abono)
                                            else if((netoRealenDoc * tasaCambioSeleccionadaPrincipal) > montoRecComp && montoRecComp > 0) {
                                                var dolneto = (montoRecComp / tasaCambioSeleccionadaPrincipal)
                                                var cobroAbono = valorReal(dolneto) * listaDocumentos[j].tasadoc
                                                listaReciboCmLineas[i].bscobro += valorReal(cobroAbono)
                                                listaReciboCmLineas[i].dolneto = valorReal(dolneto)
                                                listaReciboCmLineas[i].bsneto = montoRecComp
                                                listaReciboCmLineas[i].tnetodbs  += montoRecComp
                                                montoRecComp -= montoRecComp
                                            }
                                        }
                                    }
                                }
                            }
                        }



                        /*esto va a ser resultado de la suma de los campos de la lista (ke_precobranza)
                         de detalles */
                        var difReteIva   = valorReal(listaReciboPrLineas.sumOf { it.bsmtoiva }) + valorReal(listaReciboPrLineas.sumOf { it.bsretiva })
                        var difRetyFlete = valorReal(listaReciboPrLineas.sumOf { it.bsmtofte }) + valorReal(listaReciboPrLineas.sumOf { it.bsretfte })
                        var netoReal   = valorReal(listaReciboPrLineas.sumOf { it.bscobro }) - difReteIva - difRetyFlete //--revisar si esto es necesario
                        cxc.bsneto     = valorReal(listaReciboPrLineas.sumOf { it.bsneto })
                        cxc.bsretiva   = valorReal(listaReciboPrLineas.sumOf { it.bsretiva })
                        cxc.bsiva      = valorReal(listaReciboPrLineas.sumOf { it.bsmtoiva })
                        cxc.bsflete    = valorReal(listaReciboPrLineas.sumOf { it.bsmtofte })

                        /* sumo los bss en total y los redondeo al momento de guardarlo */
                        var bssumaTotal  = valorReal(listaReciboPrLineas.sumOf { it.bscobro })

                        //cxc.bstotal    = valorReal(bssumaTotal)
                        cxc.bstotal    = valorReal(listaReciboPrLineas.sumOf { it.bsneto } + (listaReciboPrLineas.sumOf { it.bsmtoiva } + listaReciboPrLineas.sumOf { it.bsretiva }) + (listaReciboPrLineas.sumOf { it.bsmtofte } + listaReciboPrLineas.sumOf { it.bsretfte }))
                        //cxc.dolneto    = valorReal(listaDocumentos.sumOf{it.dtotneto }) //<-------------------- Revisar si es necesario
                        cxc.dolneto    = valorReal(listaReciboPrLineas.sumOf{it.dolneto })
                        cxc.doliva     = valorReal(listaReciboPrLineas.sumOf { it.doliva })
                        //cxc.dolretiva  = valorReal(listaDocumentos.sumOf { it.cdretencioniva })
                        cxc.dolflete   = valorReal(listaReciboPrLineas.sumOf{it.dolflete})
                        cxc.doltotal = valorReal(listaReciboPrLineas.sumOf{it.dolneto} + listaReciboPrLineas.sumOf {it.doliva} + listaReciboPrLineas.sumOf{it.dolflete})
                        cxc.netocob    = if(monedaSeleccionadaPr == "2") listaReciboPrLineas.sumOf{it.dolneto} else listaReciboPrLineas.sumOf{it.bsneto}
                        //2023-07-14 se comento devido a que es un calculo errado, debido a que se trata flete con la tasa del dia y no con la tasa del documento
                        /*var doltotal   = binding.etCxcMontoMain.text.toString().toDouble()
                        if(binding.rbCxcDivisasMain.isChecked){
                            cxc.doltotal   = valorReal(doltotal)
                            cxc.netocob    = valorReal(doltotal - listaReciboPrLineas.sumOf{it.dolflete} - listaReciboPrLineas.sumOf{it.doliva})

                        }
                        else{
                            doltotal /= tasaCambioSeleccionadaPrincipal
                            cxc.doltotal   = valorReal(doltotal)
                            cxc.netocob    = valorReal(doltotal - listaReciboPrLineas.sumOf{it.dolflete} - listaReciboPrLineas.sumOf{it.doliva})
                        }*/
                        cxc.bsretflete = valorReal(listaReciboPrLineas.sumOf { it.bsretfte })
                        cxc.retmun_sbi = 0.00//definir
                        cxc.retmun_sbs = 0.00//definir
                        //var fechaVigen = fechaSuma(fechaActual, 3)
                        //println("ojo")
                        //cxc.fchvigen   = fechaVigen
                        cxc.moneda     = monedaSeleccionadaPr
                        cxc.tasadia    = tasaCambioSeleccionadaPrincipal
                        listaReciboPrCabecera.add(cxc)

                        try{

                            // inicio la transacción
                            ke_android.beginTransaction()
                            var qcabecera:ContentValues = ContentValues()
                            var qlineas:  ContentValues = ContentValues()

                            for (i in listaReciboPrCabecera.indices){
                                qcabecera.put("cxcndoc", listaReciboPrCabecera[i].id_recibo)
                                qcabecera.put("tiporecibo", listaReciboPrCabecera[i].tipoRecibo)
                                qcabecera.put("codvend", listaReciboPrCabecera[i].codigoVend)
                                qcabecera.put("tiporecibo", listaReciboPrCabecera[i].tipoRecibo)
                                qcabecera.put("kecxc_id", listaReciboPrCabecera[i].kecxc_id)
                                qcabecera.put("tasadia", listaReciboPrCabecera[i].tasadia)
                                qcabecera.put("fchrecibo", listaReciboPrCabecera[i].fchrecibo)
                                qcabecera.put("bsneto", listaReciboPrCabecera[i].bsneto)
                                qcabecera.put("bsiva", listaReciboPrCabecera[i].bsiva)
                                qcabecera.put("bsretiva", listaReciboPrCabecera[i].bsretiva)
                                qcabecera.put("bsflete", listaReciboPrCabecera[i].bsflete)
                                qcabecera.put("bstotal", listaReciboPrCabecera[i].bstotal)
                                qcabecera.put("dolneto", listaReciboPrCabecera[i].dolneto)
                                qcabecera.put("doliva", listaReciboPrCabecera[i].doliva)
                                // qcabecera.put("dolretiva", listaReciboPrCabecera[i].dolretiva)
                                qcabecera.put("dolflete", listaReciboPrCabecera[i].dolflete)
                                qcabecera.put("doltotal", listaReciboPrCabecera[i].doltotal)
                                qcabecera.put("moneda", listaReciboPrCabecera[i].moneda)

                                if(ignore == true){
                                    qcabecera.put("efectivo", listaReciboPrCabecera[i].efectivo)
                                }else{
                                    qcabecera.put("bcocod", listaReciboPrCabecera[i].bcocod)
                                    qcabecera.put("bcomonto", listaReciboPrCabecera[i].bcomonto)
                                    qcabecera.put("bcoref", listaReciboPrCabecera[i].bcoref)
                                }

                                qcabecera.put("edorec", listaReciboPrCabecera[i].edorec)
                                qcabecera.put("fchvigen", listaReciboPrCabecera[i].fchvigen)
                                qcabecera.put("bsretflete", listaReciboPrCabecera[i].bsretflete)
                                qcabecera.put("netocob", listaReciboPrCabecera[i].netocob)
                                qcabecera.put("fechamodifi", getFechaHoy())
                                //qcabecera.put("docdifcamb",diferencialCambiario)

                                for(j in listaReciboPrLineas.indices){
                                    qlineas.put("cxcndoc", listaReciboPrLineas[j].id_recibo)
                                    qlineas.put("agencia",   listaReciboPrLineas[j].agencia)
                                    qlineas.put("tipodoc",   listaReciboPrLineas[j].tipodoc)
                                    qlineas.put("documento", listaReciboPrLineas[j].documento)
                                    qlineas.put("bscobro", listaReciboPrLineas[j].bscobro)
                                    qlineas.put("prcdsctopp", listaReciboPrLineas[j].prcdsctopp)
                                    qlineas.put("nroret", listaReciboPrLineas[j].nroret)
                                    qlineas.put("fchemiret", listaReciboPrLineas[j].fchemiret)
                                    qlineas.put("bsretiva", listaReciboPrLineas[j].bsretiva)
                                    qlineas.put("refret", listaReciboPrLineas[j].refret)
                                    qlineas.put("nroretfte", listaReciboPrLineas[j].nroretfte)
                                    qlineas.put("fchemirfte", listaReciboPrLineas[j].fchemirfte)
                                    qlineas.put("bsmtofte", listaReciboPrLineas[j].bsmtofte)
                                    qlineas.put("bsretfte", listaReciboPrLineas[j].bsretfte)
                                    qlineas.put("refretfte", listaReciboPrLineas[j].refretfte)
                                    qlineas.put("bsmtoiva", listaReciboPrLineas[j].bsmtoiva)
                                    qlineas.put("tnetoddol", listaReciboPrLineas[j].tnetoddol)
                                    qlineas.put("tnetodbs", listaReciboPrLineas[j].tnetodbs)
                                    qlineas.put("fchrecibod", getFechaNow())
                                    qlineas.put("kecxc_idd", listaReciboPrCabecera[i].kecxc_id)
                                    qlineas.put("tasadiad", listaReciboPrCabecera[i].tasadia)
                                    qlineas.put("reten", retennn)
                                    ke_android.insert("ke_precobradocs", null, qlineas)
                                }
                            }
                            //guardaSaldoFavor2(binding.rbCxcDivisasMain, binding.rbCxcCompMain, binding.etCxcMontoMain.text.toString().toDouble(), 0.00,  binding.tvCxcTotal, listaReciboPrLineas[listaReciboPrLineas.size-1].id_recibo, listaReciboPrLineas[listaReciboPrLineas.size-1].documento)
                            ke_android.insert("ke_precobranza", null, qcabecera)
                            var qcorrelativo: ContentValues = ContentValues()
                            qcorrelativo.put("kcor_numero", nroCorrelativo)
                            qcorrelativo.put("kcor_vendedor", cod_usuario)

                            ke_android.insert("ke_corprec", null, qcorrelativo)
                            ll_commit = true

                        }catch (exception: SQLException){

                            exception.printStackTrace()
                            ll_commit = false

                            ke_android.endTransaction()
                            if(!ll_commit){
                                return
                            }
                        }


                        //preparacion para el guardado de datos
                        var netoCmReal   = valorReal(listaReciboCmLineas.sumOf { it.bscobro })
                        comp.bsneto      = valorReal(listaReciboCmLineas.sumOf { it.bsneto })

                        /* sumo los bss en total y los redondeo al momento de guardarlo */
                        var bssumaTotalCm  = valorReal(listaReciboCmLineas.sumOf { it.bscobro })

                        comp.bstotal  = comp.bsneto
                        comp.dolneto     = valorReal(listaReciboCmLineas.sumOf{it.dolneto } )
                        var doltotalCm   = binding.etCxcMontoCom.text.toString().toDouble()

                        comp.doltotal   = comp.dolneto
                        comp.netocob    = if(monedaSeleccionadaCm == "2") listaReciboCmLineas.sumOf{it.dolneto} else listaReciboCmLineas.sumOf{it.bsneto}
                        /*if(binding.rbCxcDivisasCom.isChecked){
                            comp.doltotal   = valorReal(doltotalCm)
                            comp.netocob    = doltotalCm

                        }else{
                            doltotalCm = doltotalCm / tasaCambioSeleccionadaPrincipal
                            comp.doltotal    = valorReal(doltotalCm)
                            comp.netocob     = doltotalCm
                        }*/

                        //var fechaVigenCm = fechaSuma(fechaActual, 3)
                        //comp.fchvigen    = fechaVigenCm
                        comp.moneda      = monedaSeleccionadaCm
                        comp.tasadia     = tasaCambioSeleccionadaPrincipal
                        listaReciboCmCabecera.add(comp)

                        try{

                            var qcabecera:   ContentValues = ContentValues()
                            var qlineas:     ContentValues = ContentValues()
                            var qdescuentos: ContentValues = ContentValues()

                            for (i in listaReciboCmCabecera.indices){

                                qcabecera.put("cxcndoc", listaReciboCmCabecera[i].id_recibo)
                                qcabecera.put("tiporecibo", listaReciboCmCabecera[i].tipoRecibo)
                                qcabecera.put("codvend", listaReciboCmCabecera[i].codigoVend)
                                qcabecera.put("tiporecibo", listaReciboCmCabecera[i].tipoRecibo)
                                qcabecera.put("kecxc_id", listaReciboCmCabecera[i].kecxc_id)
                                qcabecera.put("tasadia", listaReciboCmCabecera[i].tasadia)
                                qcabecera.put("fchrecibo", listaReciboCmCabecera[i].fchrecibo)
                                qcabecera.put("bsneto", listaReciboCmCabecera[i].bsneto)
                                qcabecera.put("bstotal", listaReciboCmCabecera[i].bstotal)
                                qcabecera.put("dolneto", listaReciboCmCabecera[i].dolneto)
                                qcabecera.put("doltotal", listaReciboCmCabecera[i].doltotal)
                                qcabecera.put("moneda", listaReciboCmCabecera[i].moneda)
                                if(ignorecm == true){
                                    qcabecera.put("efectivo", listaReciboCmCabecera[i].efectivo)
                                }else{
                                    qcabecera.put("bcocod", listaReciboCmCabecera[i].bcocod)
                                    qcabecera.put("bcomonto", listaReciboCmCabecera[i].bcomonto)
                                    qcabecera.put("bcoref", listaReciboCmCabecera[i].bcoref)
                                }
                                qcabecera.put("edorec", listaReciboCmCabecera[i].edorec)
                                qcabecera.put("fchvigen", listaReciboCmCabecera[i].fchvigen)
                                qcabecera.put("netocob", listaReciboCmCabecera[i].netocob)
                                qcabecera.put("fechamodifi", getFechaHoy())
                                //qcabecera.put("docdifcamb",diferencialCambiario)

                                for(j in listaReciboCmLineas.indices){
                                    qlineas.put("cxcndoc", listaReciboCmLineas[j].id_recibo)
                                    qlineas.put("agencia",   listaReciboCmLineas[j].agencia)
                                    qlineas.put("tipodoc",   listaReciboCmLineas[j].tipodoc)
                                    qlineas.put("documento", listaReciboCmLineas[j].documento)
                                    qlineas.put("bscobro", listaReciboCmLineas[j].bscobro)
                                    qlineas.put("tnetoddol", listaReciboCmLineas[j].tnetoddol)
                                    qlineas.put("tnetodbs",  listaReciboCmLineas[j].tnetodbs)
                                    qlineas.put("fchrecibod", getFechaNow())
                                    qlineas.put("kecxc_idd", listaReciboCmCabecera[i].kecxc_id)
                                    qlineas.put("tasadiad", listaReciboCmCabecera[i].tasadia)
                                    qlineas.put("reten", 1)
                                    ke_android.insert("ke_precobradocs", null, qlineas)
                                }

                            }
                            //ke_android.execSQL("UPDATE ke_precobradocs SET afavor= ${guardaSaldoFavor(montoRec, binding.rbCxcDivisasCom, binding.rbCxcCompMain, (binding.etCxcMontoCom.text.toString().toDouble() + binding.etCxcMontoCom.text.toString().toDouble()) , binding.tvCxcTotal)} WHERE cxcndoc='${listaReciboCmLineas[listaReciboCmLineas.size-1].id_recibo}' AND documento = '${listaReciboCmLineas[listaReciboCmLineas.size-1].documento}';")
                            guardaSaldoFavor2(binding.rbCxcDivisasCom, binding.rbCxcCompMain, binding.etCxcMontoMain.text.toString().toDouble(), binding.etCxcMontoCom.text.toString().toDouble(),  binding.tvCxcTotal, listaReciboCmLineas[listaReciboCmLineas.size-1].id_recibo, listaReciboCmLineas[listaReciboCmLineas.size-1].documento, binding.rbCxcDivisasCom.isChecked)
                            ke_android.insert("ke_precobranza", null, qcabecera)

                            //si hay descuentos, los inserto tambien
                            if(listaDescuentos.size > 0){
                                for(i in listaDocumentos.indices){
                                    for(j in listaDescuentos.indices){
                                        if(listaDocumentos[i].documento == listaDescuentos[j].nrodoc){
                                            qdescuentos.put("agencia", listaDocumentos[i].agencia)
                                            qdescuentos.put("tipodoc", listaDocumentos[i].tipodoc)
                                            qdescuentos.put("documento", listaDescuentos[j].nrodoc)
                                            qdescuentos.put("prcdctoaplic", listaDescuentos[j].pordscto)
                                            qdescuentos.put("montodctodol", listaDescuentos[j].cantdscto)
                                            qdescuentos.put("tasadoc", listaDocumentos[i].tasadoc)
                                            qdescuentos.put("codcliente", listaDocumentos[i].codcliente)
                                            qdescuentos.put("fchvigen", listaDocumentos[i].vence)
                                            qdescuentos.put("fechamodifi", fechaActual)
                                            //inserción de descuentos de tenerlos
                                            ke_android.insert("ke_precobdcto", null, qdescuentos)
                                        }
                                    }
                                }
                            }


                            var qcorrelativo: ContentValues = ContentValues()
                            qcorrelativo.put("kcor_numero", nroCorrelativoCom)
                            qcorrelativo.put("kcor_vendedor", cod_usuario)

                            ke_android.insert("ke_corprec", null, qcorrelativo)
                            ll_commit = true

                        }catch(exception: SQLException){
                            exception.printStackTrace()
                            ll_commit = false

                            ke_android.endTransaction()
                            if(!ll_commit){
                                return
                            }
                        }

                        if(ll_commit){
                            ke_android.setTransactionSuccessful()
                            ke_android.endTransaction()

                            var listadatos: ArrayList<CXC> = ArrayList()
                            listadatos.add(cxc)
                            listadatos[0].cliente = codigoCliente

                            var dialog: DialogRecibo = DialogRecibo()
                            dialog.DialogRecibo(this, listadatos)

                            Toast.makeText(this, "RECIBO CREADO", Toast.LENGTH_SHORT).show()
                            // finish()
                        }else{
                            ke_android.endTransaction()
                        }

                    }

                }


            }
            // Lo que el clienta da es mayor o igual total y no esta marcado complemento
            else if (montoRec >= montoMinimoRec && !binding.cbCxcComplemento.isChecked){
                var ll_commit = false
                ke_android = conn.writableDatabase

                //listas con el tipo de datos para los recibos
                var listaReciboPrCabecera:ArrayList<CXC>  = ArrayList()
                var listaReciboPrLineas: ArrayList<CXC>   = ArrayList()

                listaDocumentos.sortBy { it.fechaDocs }//llenado de datos de cabecera
                var cxc = CXC()
                cxc.id_recibo  = nroPrecobranza
                cxc.tipoRecibo = "W"
                cxc.codigoVend = cod_usuario.toString()
                cxc.kecxc_id   = tasaId
                cxc.tasadia    = tasaCambioSeleccionadaPrincipal
                cxc.fchrecibo  = fechatasaH
                cxc.clicontesp = listaDocumentos[0].contribesp.toString() //esto lo jalo de  la lista de docs? <--- PREGUNTAR, se puede obtener
                cxc.moneda     = monedaSeleccionadaPr
                //IF que valida y llena los datos para cuando se selecciona efectivo o transferencia
                if(ignore){
                    cxc.bcocod     = ""
                    cxc.bcoref     = ""
                    cxc.efectivo   = montoRec
                    cxc.fchvigen   = fechaSuma(fechaActual, 60)
                }else{
                    cxc.bcocod     = codigoBancoCompleto
                    cxc.bcomonto   = montoRec
                    cxc.bcoref     = referenciaPrincipal
                    cxc.fchvigen   = fechaSuma(fechaActual, 5)
                }

                cxc.edorec     = "0"
                cxc.fchhr      = fechaActual


                //en este ciclo lleno la lista de precobradocs
                for(i in listaDocumentos.indices){
                    val cxclineas = CXC()

                    cxclineas.id_recibo = nroPrecobranza
                    cxclineas.agencia   = listaDocumentos[i].agencia
                    cxclineas.tipodoc   = listaDocumentos[i].tipodoc
                    cxclineas.documento = listaDocumentos[i].documento
                    listaReciboPrLineas.add(cxclineas)

                }

                //en este ciclo, agrego las retenciones
                for (i in listaReciboPrLineas.indices){
                    if(listaReciboPrLineas[i].agencia.equals("002") && (listaReciboPrLineas[i].documento == listaDocumentos[i].documento)){

                        listaDocumentos[i].cbsretflete = 0.00

                    }

                    //llenado de campos de retenciones
                    //retenciones fase 2
                    for (j in listaRetGuardada.indices){
                        if(listaRetGuardada[j].nrodoc == listaReciboPrLineas[i].documento){
                            if(listaRetGuardada[j].tiporet == "iva"){
                                //si es de iva
                                listaReciboPrLineas[i].nroret    = listaRetGuardada[j].nroret
                                listaReciboPrLineas[i].fchemiret = listaRetGuardada[j].fecharet
                                listaReciboPrLineas[i].bsretiva  = - listaRetGuardada[j].montoret
                                listaReciboPrLineas[i].refret    = listaRetGuardada[j].refret
                            }

                            if(listaRetGuardada[j].tiporet == "flete"){
                                //si es de flete
                                listaReciboPrLineas[i].nroretfte    = listaRetGuardada[j].nroret
                                listaReciboPrLineas[i].fchemirfte   = listaRetGuardada[j].fecharet
                                listaReciboPrLineas[i].bsretfte     = - listaRetGuardada[j].montoret
                                listaReciboPrLineas[i].refretfte    = listaRetGuardada[j].refret
                            }

                            if(listaRetGuardada[j].tiporet == "parme"){
                                //si es de parme
                                listaReciboPrLineas[i].retmun_nro    = listaRetGuardada[j].nroret
                                listaReciboPrLineas[i].retmun_fch    = listaRetGuardada[j].fecharet
                                listaReciboPrLineas[i].retmun_mto    = - listaRetGuardada[j].montoret
                                listaReciboPrLineas[i].retmun_cod    = listaRetGuardada[j].refret
                            }
                        }
                    }

                    //descuento ivas y fletes ( de haberlos)
                    if(listaReciboPrLineas[i].documento == listaDocumentos[i].documento && listaDocumentos[i].tipodocv == "FAC"){

                        if(listaDocumentos[i].bsiva - listaDocumentos[i].bsmtoiva <= 0.00){
                            listaReciboPrLineas[i].bsmtoiva = 0.00
                            listaReciboPrLineas[i].doliva   = 0.00

                        }else{

                            //descuento ivas del monto del recibo original .-
                            var restaIvadol = if (binding.cbExcReten.isChecked) listaDocumentos[i].dtotimpuest else listaDocumentos[i].dtotimpuest - listaDocumentos[i].cdretencioniva
                            //2023-04-03 comentario por no tener en cuenta cuando se escluyen retenciones
                            //var restaIvadol = listaDocumentos[i].dtotimpuest - listaDocumentos[i].cdretencioniva
                            var restaIvabss = if (binding.cbExcReten.isChecked) listaDocumentos[i].bsiva else listaDocumentos[i].bsiva - listaDocumentos[i].cbsretencioniva
                            //var restaIvabss = listaDocumentos[i].bsiva - listaDocumentos[i].cbsretencioniva

                            if(binding.rbCxcDivisasMain.isChecked){
                                //hago el descuento del iva del nmonto de pago
                                montoRec -= restaIvadol

                                if(montoRec < 0.00){
                                    Toast.makeText(this, "Monto insuficiente", Toast.LENGTH_SHORT).show()
                                    return
                                }

                                listaReciboPrLineas[i].bscobro  += (if (binding.cbExcReten.isChecked) listaDocumentos[i].bsiva else listaDocumentos[i].bsiva - listaDocumentos[i].cbsretencioniva)
                                listaReciboPrLineas[i].bsmtoiva = listaDocumentos[i].bsiva
                                listaReciboPrLineas[i].doliva   = listaDocumentos[i].dtotimpuest - listaDocumentos[i].cdretencioniva
                                //2023-04-03 comentado por mal calculo?
                                //listaReciboPrLineas[i].tnetoddol  += listaReciboPrLineas[i].doliva
                                listaReciboPrLineas[i].tnetoddol  += (if (binding.cbExcReten.isChecked) listaDocumentos[i].dtotimpuest else listaDocumentos[i].dtotimpuest - listaDocumentos[i].cdretencioniva)

                            }
                            else{
                                montoRec -= restaIvabss
                                if(montoRec < 0.00){
                                    Toast.makeText(this, "Monto insuficiente", Toast.LENGTH_SHORT).show()
                                    return
                                }
                                listaReciboPrLineas[i].bscobro  += ((if (binding.cbExcReten.isChecked) listaDocumentos[i].bsiva else listaDocumentos[i].bsiva - listaDocumentos[i].cbsretencioniva))
                                listaReciboPrLineas[i].bsmtoiva = listaDocumentos[i].bsiva
                                listaReciboPrLineas[i].doliva   = listaDocumentos[i].dtotimpuest - listaDocumentos[i].cdretencioniva
                                listaReciboPrLineas[i].tnetodbs += (if (binding.cbExcReten.isChecked) listaDocumentos[i].bsiva else listaDocumentos[i].bsiva - listaDocumentos[i].cbsretencioniva)
                                //listaReciboPrLineas[i].tnetodbs += listaDocumentos[i].bsiva

                            }



                            if(listaDocumentos[i].bsflete - listaDocumentos[i].bsmtofte <= 1){
                                listaReciboPrLineas[i].bsmtofte = 0.00
                                listaReciboPrLineas[i].dolflete = 0.00
                            }else{
                                if(binding.cbExcReten.isChecked){
                                    listaReciboPrLineas[i].bscobro  += listaDocumentos[i].bsflete
                                }else{
                                    listaReciboPrLineas[i].bscobro  += listaDocumentos[i].bsflete - listaDocumentos[i].cbsretflete
                                }

                                if(binding.rbCxcDivisasMain.isChecked){
                                    montoRec -= if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dFlete - listaDocumentos[i].cdretflete
                                    listaReciboPrLineas[i].bsmtofte = listaDocumentos[i].bsflete
                                    listaReciboPrLineas[i].dolflete = if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dFlete - listaDocumentos[i].cdretflete
                                    listaReciboPrLineas[i].tnetoddol  += if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dFlete - listaDocumentos[i].cdretflete

                                }else{
                                    montoRec -= if (binding.cbExcReten.isChecked) listaDocumentos[i].bsflete else listaDocumentos[i].bsflete - listaDocumentos[i].cbsretflete
                                    listaReciboPrLineas[i].bsmtofte = listaDocumentos[i].bsflete
                                    listaReciboPrLineas[i].dolflete = if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dFlete - listaDocumentos[i].cdretflete
                                    listaReciboPrLineas[i].tnetodbs  += if (binding.cbExcReten.isChecked) listaDocumentos[i].bsflete else listaDocumentos[i].bsflete - listaDocumentos[i].cbsretflete

                                }

                            }

                        }
                    }else{
                        if(listaDocumentos[i].bsflete - listaDocumentos[i].bsmtofte <= 1){
                            listaReciboPrLineas[i].bsmtofte = 0.00
                            listaReciboPrLineas[i].dolflete = 0.00
                        }else{

                            var fleteaCobrar = if (binding.cbExcReten.isChecked) listaDocumentos[i].bsflete else listaDocumentos[i].bsflete - listaDocumentos[i].cbsretflete
                            listaReciboPrLineas[i].bscobro  += fleteaCobrar

                            if(binding.rbCxcDivisasMain.isChecked){
                                montoRec -= if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dFlete - listaDocumentos[i].cdretflete
                                listaReciboPrLineas[i].bsmtofte = listaDocumentos[i].bsflete
                                listaReciboPrLineas[i].dolflete = if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dFlete - listaDocumentos[i].cdretflete
                                listaReciboPrLineas[i].tnetoddol  += if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dFlete - listaDocumentos[i].cdretflete

                            }else{
                                montoRec -= if (binding.cbExcReten.isChecked) listaDocumentos[i].bsflete else listaDocumentos[i].bsflete - listaDocumentos[i].cbsretflete
                                listaReciboPrLineas[i].bsmtofte = listaDocumentos[i].bsflete
                                listaReciboPrLineas[i].dolflete = if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dFlete - listaDocumentos[i].cdretflete
                                listaReciboPrLineas[i].tnetodbs  += if (binding.cbExcReten.isChecked) listaDocumentos[i].bsflete else listaDocumentos[i].bsflete - listaDocumentos[i].cbsretflete

                            }

                        }
                    }
                }

                //llenado de los netos
                for (i in listaReciboPrLineas.indices){

                    if(montoRec < 1){
                        // borré el return
                    }
                    else{
                        if(binding.rbCxcDivisasMain.isChecked){
                            var netoTV = binding.tvCxcNeto.text.toString().toDouble()

                            if(netoTV > 0.00){
                                var netoRealenDoc = listaDocumentos[i].netorestante

                                if( montoRec >= netoRealenDoc){
                                    var bscobrado = netoRealenDoc * listaDocumentos[i].tasadoc
                                    bscobrado     = bscobrado

                                    listaReciboPrLineas[i].bscobro += bscobrado

                                    netocobrado =  if (binding.tvCxcDctos.text.toString().toDouble() > 0.0) valorReal(listaDocumentos[i].netorestante - (listaDocumentos[i].netorestante * 0.04)) else listaDocumentos[i].netorestante // >---------------------------------------------------------------- AQUI
                                    listaReciboPrLineas[i].dolneto = netocobrado
                                    montoRec -= netocobrado
                                    listaReciboPrLineas[i].tnetoddol += netocobrado
                                }

                                else if(netoRealenDoc > montoRec && montoRec > 0) {

                                    var cobroAbono = montoRec * listaDocumentos[i].tasadoc

                                    listaReciboPrLineas[i].bscobro += cobroAbono
                                    netocobrado = montoRec
                                    listaReciboPrLineas[i].dolneto = valorReal(netocobrado)
                                    montoRec -= netocobrado
                                    montoRec= valorReal(montoRec)
                                    listaReciboPrLineas[i].tnetoddol += netocobrado
                                }

                            }
                        }

                        if(binding.rbCxcBssMain.isChecked){
                            var netoRealenDoc = listaDocumentos[i].netorestante
                            netoRealenDoc = valorReal(netoRealenDoc)

                            //si el monto del recibo cubre el monto del documento
                            if( montoRec > (netoRealenDoc * tasaCambioSeleccionadaPrincipal)){
                                var bscobrado = valorReal(netoRealenDoc * listaDocumentos[i].tasadoc)
                                listaReciboPrLineas[i].bscobro += bscobrado
                                netocobrado = listaDocumentos[i].netorestante
                                listaReciboPrLineas[i].dolneto = netocobrado
                                montoRec -= valorReal(netoRealenDoc * listaDocumentos[i].tasadoc)
                                listaReciboPrLineas[i].bsneto = valorReal(netoRealenDoc * tasaCambioSeleccionadaPrincipal)
                                listaReciboPrLineas[i].tnetodbs  += valorReal(netoRealenDoc * tasaCambioSeleccionadaPrincipal)

                            }
                            //si el monto del recibo no cubre el monto completo del documento (se comporta como un abono)
                            else if((netoRealenDoc * tasaCambioSeleccionadaPrincipal) > montoRec && montoRec > 0) {
                                netocobrado = montoRec / tasaCambioSeleccionadaPrincipal
                                var cobroAbono = netocobrado * listaDocumentos[i].tasadoc
                                listaReciboPrLineas[i].bscobro += valorReal(cobroAbono)
                                var dolneto = netocobrado
                                listaReciboPrLineas[i].dolneto    = valorReal(dolneto)
                                listaReciboPrLineas[i].bsneto = montoRec
                                listaReciboPrLineas[i].tnetodbs  += montoRec
                                montoRec -= montoRec

                            }
                        }

                        //los bss del neto cobrado
                        /*recorrido de lista de descuentos para asignar cantidad según nro doc. (de tener)*/
                        for(j in listaDescuentos.indices){
                            if(listaDescuentos[j].nrodoc == listaDocumentos[i].documento){
                                listaReciboPrLineas[i].prcdsctopp = listaDescuentos[j].pordscto
                            }
                        }

                    }
                }

                /*esto va a ser resultado de la suma de los campos de la lista (ke_precobranza)
                 de detalles */

                var difReteIva   = valorReal(listaReciboPrLineas.sumOf { it.bsmtoiva }) + valorReal(listaReciboPrLineas.sumOf { it.bsretiva })
                var difRetyFlete = valorReal(listaReciboPrLineas.sumOf { it.bsmtofte }) + valorReal(listaReciboPrLineas.sumOf { it.bsretfte })
                var netoReal   = valorReal(listaReciboPrLineas.sumOf { it.bscobro }) - difReteIva - difRetyFlete //--revisar si esto es necesario
                cxc.bsneto     = valorReal(listaReciboPrLineas.sumOf { it.bsneto })
                cxc.bsretiva   = valorReal(listaReciboPrLineas.sumOf { it.bsretiva })
                cxc.bsiva      = valorReal(listaReciboPrLineas.sumOf { it.bsmtoiva })
                cxc.bsflete    = valorReal(listaReciboPrLineas.sumOf { it.bsmtofte })

                /* sumo los bss en total y los redondeo al momento de guardarlo */
                var bssumaTotal  = valorReal(listaReciboPrLineas.sumOf { it.bscobro })

                //cxc.bstotal    = valorReal(bssumaTotal)
                cxc.bstotal    = valorReal(listaReciboPrLineas.sumOf { it.bsneto } + (listaReciboPrLineas.sumOf { it.bsmtoiva } + listaReciboPrLineas.sumOf { it.bsretiva }) + (listaReciboPrLineas.sumOf { it.bsmtofte } + listaReciboPrLineas.sumOf { it.bsretfte }))
                //cxc.dolneto    = valorReal(listaDocumentos.sumOf{it.dtotneto }) //<-------------------- Revisar si es necesario
                cxc.dolneto    = valorReal(listaReciboPrLineas.sumOf{it.dolneto })
                cxc.doliva     = valorReal(listaReciboPrLineas.sumOf { it.doliva })
                //cxc.dolretiva  = valorReal(listaDocumentos.sumOf { it.cdretencioniva })
                cxc.dolflete   = valorReal(listaReciboPrLineas.sumOf{it.dolflete})
                cxc.doltotal = valorReal(listaReciboPrLineas.sumOf{it.dolneto} + listaReciboPrLineas.sumOf {it.doliva} + listaReciboPrLineas.sumOf{it.dolflete})
                cxc.netocob    = if(monedaSeleccionadaPr == "2") listaReciboPrLineas.sumOf{it.dolneto} else listaReciboPrLineas.sumOf{it.bsneto}
                //2023-07-14 se comento devido a que es un calculo errado, debido a que se trata flete con la tasa del dia y no con la tasa del documento
                /*var doltotal   = binding.etCxcMontoMain.text.toString().toDouble()
                if(binding.rbCxcDivisasMain.isChecked){
                    cxc.doltotal   = valorReal(doltotal)
                    cxc.netocob    = valorReal(doltotal - listaReciboPrLineas.sumOf{it.dolflete} - listaReciboPrLineas.sumOf{it.doliva})

                }
                else{
                    doltotal /= tasaCambioSeleccionadaPrincipal
                    cxc.doltotal   = valorReal(doltotal)
                    cxc.netocob    = valorReal(doltotal - listaReciboPrLineas.sumOf{it.dolflete} - listaReciboPrLineas.sumOf{it.doliva})
                }*/
                cxc.bsretflete = valorReal(listaReciboPrLineas.sumOf { it.bsretfte })
                cxc.retmun_sbi = 0.00//definir
                cxc.retmun_sbs = 0.00//definir
                //var fechaVigen = fechaSuma(fechaActual, 3)
                //println("ojo")
                //cxc.fchvigen   = fechaVigen
                cxc.moneda     = monedaSeleccionadaPr
                cxc.tasadia    = tasaCambioSeleccionadaPrincipal
                listaReciboPrCabecera.add(cxc)

                try{

                    // inicio la transacción
                    ke_android.beginTransaction()
                    var qcabecera:ContentValues = ContentValues()
                    var qlineas:  ContentValues = ContentValues()
                    var qdescuentos: ContentValues = ContentValues()

                    for (i in listaReciboPrCabecera.indices){
                        qcabecera.put("cxcndoc", listaReciboPrCabecera[i].id_recibo)
                        qcabecera.put("tiporecibo", listaReciboPrCabecera[i].tipoRecibo)
                        qcabecera.put("codvend", listaReciboPrCabecera[i].codigoVend)
                        qcabecera.put("tiporecibo", listaReciboPrCabecera[i].tipoRecibo)
                        qcabecera.put("kecxc_id", listaReciboPrCabecera[i].kecxc_id)
                        qcabecera.put("tasadia", listaReciboPrCabecera[i].tasadia)
                        qcabecera.put("fchrecibo", listaReciboPrCabecera[i].fchrecibo)
                        qcabecera.put("bsneto", listaReciboPrCabecera[i].bsneto)
                        qcabecera.put("bsiva", listaReciboPrCabecera[i].bsiva)
                        qcabecera.put("bsretiva", listaReciboPrCabecera[i].bsretiva)
                        qcabecera.put("bsflete", listaReciboPrCabecera[i].bsflete)
                        qcabecera.put("bstotal", listaReciboPrCabecera[i].bstotal)
                        qcabecera.put("dolneto", valorReal(listaReciboPrCabecera[i].dolneto))
                        qcabecera.put("doliva", listaReciboPrCabecera[i].doliva)
                        // qcabecera.put("dolretiva", listaReciboPrCabecera[i].dolretiva)
                        qcabecera.put("dolflete", listaReciboPrCabecera[i].dolflete)
                        qcabecera.put("doltotal", listaReciboPrCabecera[i].doltotal)
                        qcabecera.put("moneda", listaReciboPrCabecera[i].moneda)
                        if(ignore == true){
                            qcabecera.put("efectivo", listaReciboPrCabecera[i].efectivo)
                        }else{
                            qcabecera.put("bcocod", listaReciboPrCabecera[i].bcocod)
                            qcabecera.put("bcomonto", listaReciboPrCabecera[i].bcomonto)
                            qcabecera.put("bcoref", listaReciboPrCabecera[i].bcoref)
                        }
                        qcabecera.put("edorec", listaReciboPrCabecera[i].edorec)
                        qcabecera.put("fchvigen", listaReciboPrCabecera[i].fchvigen)
                        qcabecera.put("bsretflete", listaReciboPrCabecera[i].bsretflete)
                        qcabecera.put("netocob", listaReciboPrCabecera[i].netocob)
                        qcabecera.put("fechamodifi", getFechaHoy())
                        //qcabecera.put("docdifcamb",diferencialCambiario)

                        for(j in listaReciboPrLineas.indices){
                            qlineas.put("cxcndoc", listaReciboPrLineas[j].id_recibo)
                            qlineas.put("agencia",   listaReciboPrLineas[j].agencia)
                            qlineas.put("tipodoc",   listaReciboPrLineas[j].tipodoc)
                            qlineas.put("documento", listaReciboPrLineas[j].documento)
                            qlineas.put("bscobro", listaReciboPrLineas[j].bscobro)
                            qlineas.put("prcdsctopp", listaReciboPrLineas[j].prcdsctopp)
                            qlineas.put("nroret", listaReciboPrLineas[j].nroret)
                            qlineas.put("fchemiret", listaReciboPrLineas[j].fchemiret)
                            qlineas.put("bsretiva", listaReciboPrLineas[j].bsretiva)
                            qlineas.put("refret", listaReciboPrLineas[j].refret)
                            qlineas.put("nroretfte", listaReciboPrLineas[j].nroretfte)
                            qlineas.put("fchemirfte", listaReciboPrLineas[j].fchemirfte)
                            qlineas.put("bsmtofte", listaReciboPrLineas[j].bsmtofte)
                            qlineas.put("bsretfte", listaReciboPrLineas[j].bsretfte)
                            qlineas.put("refretfte", listaReciboPrLineas[j].refretfte)
                            qlineas.put("bsmtoiva", listaReciboPrLineas[j].bsmtoiva)
                            qlineas.put("tnetoddol", valorReal(listaReciboPrLineas[j].tnetoddol))
                            qlineas.put("tnetodbs", valorReal(listaReciboPrLineas[j].tnetodbs))
                            qlineas.put("fchrecibod", getFechaNow())
                            qlineas.put("kecxc_idd", listaReciboPrCabecera[i].kecxc_id)
                            qlineas.put("tasadiad", listaReciboPrCabecera[i].tasadia)
                            qlineas.put("reten", retennn)

                            ke_android.insert("ke_precobradocs", null, qlineas)
                        }
                    }

                    //ke_android.execSQL("UPDATE ke_precobradocs SET afavor= ${guardaSaldoFavor(montoRec, binding.rbCxcDivisasMain, binding.rbCxcCompMain, binding.etCxcMontoMain.text.toString().toDouble(), binding.tvCxcTotal)} WHERE cxcndoc='${listaReciboPrLineas[listaReciboPrLineas.size-1].id_recibo}' AND documento = '${listaReciboPrLineas[listaReciboPrLineas.size-1].documento}';")
                    guardaSaldoFavor2(binding.rbCxcDivisasMain, binding.rbCxcCompMain, binding.etCxcMontoMain.text.toString().toDouble(), 0.00,  binding.tvCxcTotal, listaReciboPrLineas[listaReciboPrLineas.size-1].id_recibo, listaReciboPrLineas[listaReciboPrLineas.size-1].documento, true)
                    ke_android.insert("ke_precobranza", null, qcabecera)

                    //si hay descuentos, los inserto tambien
                    if(listaDescuentos.size > 0){
                        for(i in listaDocumentos.indices){
                            for(j in listaDescuentos.indices){
                                if(listaDocumentos[i].documento == listaDescuentos[j].nrodoc){
                                    qdescuentos.put("agencia", listaDocumentos[i].agencia)
                                    qdescuentos.put("tipodoc", listaDocumentos[i].tipodoc)
                                    qdescuentos.put("documento", listaDescuentos[j].nrodoc)
                                    qdescuentos.put("prcdctoaplic", listaDescuentos[j].pordscto)
                                    qdescuentos.put("montodctodol", listaDescuentos[j].cantdscto)
                                    qdescuentos.put("tasadoc", listaDocumentos[i].tasadoc)
                                    qdescuentos.put("codcliente", listaDocumentos[i].codcliente)
                                    qdescuentos.put("fchvigen", listaDocumentos[i].vence)
                                    qdescuentos.put("fechamodifi", fechaActual)
                                    //inserción de descuentos de tenerlos
                                    ke_android.insert("ke_precobdcto", null, qdescuentos)
                                }
                            }
                        }
                    }

                    var qcorrelativo: ContentValues = ContentValues()
                    qcorrelativo.put("kcor_numero", nroCorrelativo)
                    qcorrelativo.put("kcor_vendedor", cod_usuario)

                    ke_android.insert("ke_corprec", null, qcorrelativo)


                    ll_commit = true
                }catch (exception: SQLException){

                    exception.printStackTrace()
                    ll_commit = false

                    ke_android.endTransaction()
                    if(!ll_commit){
                        return
                    }
                }

                if(ll_commit){
                    ke_android.setTransactionSuccessful()
                    ke_android.endTransaction()

                    var listadatos: ArrayList<CXC> = ArrayList()
                    listadatos.add(cxc)
                    listadatos[0].cliente = codigoCliente

                    var dialog: DialogRecibo = DialogRecibo()
                    dialog.DialogRecibo(this, listadatos)

                    Toast.makeText(this, "RECIBO CREADO", Toast.LENGTH_SHORT).show()
                    // finish()
                }else{
                    ke_android.endTransaction()
                }

            }

        }
        else if(listaDocumentos.size == 1){
            //2023-03-27 se comento la linea de codigo debido a que al inicio de codigo ya verifica si se seleccionoun solo documento o varios para colocar abono
            //binding.rbCxcAbonoMain.visibility = View.VISIBLE

            if(binding.rbCxcCompMain.isChecked){
                montoMinimoRec = binding.tvCxcTotal.text.toString().toDouble() - 0.5

            }
            else if (binding.rbCxcAbonoMain.isChecked){
                //si se presiona abono, el monto minimo sera la suma del flete mas el iva
                montoMinimoRec = valorReal(ivaTot  + fleteTot - retTot - 0.01)
                // de no haber iva y flete, el monto minimo se hace aut. en 1 (bs o $)
                if(montoMinimoRec == 0.00){
                    montoMinimoRec = 1.0
                }
            }

            if(montoRec < montoMinimoRec && !binding.cbCxcComplemento.isChecked){

                if(binding.rbCxcCompMain.isChecked){
                    Toast.makeText(this, "El monto del recibo no debe ser menor al Total", Toast.LENGTH_SHORT).show()

                }
                else if (binding.rbCxcAbonoMain.isChecked){
                    Toast.makeText(this, "El monto del recibo no debe ser menor a la suma del IVA y el Flete", Toast.LENGTH_SHORT).show()
                }
                return
            }
            // 2023-07-06 se coloco un or que suma la cantidad previamente restada a la variable montoMinimoRec debido a un caso donde no entre a ningun if
            // esto es debido a que si se paga un precio cercano al precio total del documento pero ligeramente menor no entra a ningun condicional ejemplo
            // total = 92.25
            // ingresado = 90
            // total restado 8.75 <-- (montoMinimoRec)
            // REDUNDANTE
            else if((montoRec < montoMinimoRec && binding.cbCxcComplemento.isChecked) || (montoRec < (montoMinimoRec + 0.5) && binding.cbCxcComplemento.isChecked) ){
                //Solo aplica cuando se paga COMPLETO
                // en este caso es solo un documento en el cual se va a pagar una parte con el recibo principal
                // y el resto sera pagado mediante un recibo de complemento que solo afecta al neto.

                //si el monto del recibo es menor al monto minimo pero el complemento esta marcado
                //val diferencia = (valorReal(montoMinimoRec * 0.9) <= montoRec)

                var diferencia = if (binding.rbCxcDivisasCom.isChecked) {
                    true
                }
                else {
                    ((valorReal(montoMinimoRec * APP_PORCENTAJE_COMPLEMENTO) <= montoRec))
                }

                //si la diferencia entre el monto reportado y el monto minimo es mayor a un 10%, no se permite el complemento
                if(!diferencia){
                    Toast.makeText(this, "Monto Excedido para complemento", Toast.LENGTH_SHORT).show()
                    return

                    //de manera contraria, si
                }
                else if(diferencia){

                    //valido montos del complemento
                    if(binding.etCxcMontoCom.text.toString().equals("") || binding.etCxcMontoCom.text.toString().equals(null) ){
                        Toast.makeText(this, "Monto de complemento no puede estar vacío", Toast.LENGTH_SHORT).show()
                        return
                    }
                    //valido montos en 0 y banco vacio del complemento, asi como tambien
                    //la referencia.
                    // Si encuentra la referencia bancaria del complemento vacia entrara aqui
                    if(binding.etCxcRefCom.text.toString().equals("") || binding.etCxcRefCom.text.toString().equals(null) ){
                        // Si la variable ignorecm es true significa que se selecciono efectivo como forma de pago, y continua
                        if(ignorecm){

                        }
                        else{ // Si la variable ignorecm es false significa que se selecciono transferencia, lanzara este mensaje y parará
                            Toast.makeText(this, "La referencia del complemento no puede estar vacía", Toast.LENGTH_SHORT).show()
                            return
                        }
                    }
                    // Si encuentra el codigo de banco del complemento vacia entrara aqui
                    if(codigoBancoComplemento.equals("")){
                        // Si la variable ignorecm es true significa que se selecciono efectivo como forma de pago, y continua
                        if(ignorecm){

                        }
                        else{// Si la variable ignorecm es false significa que se selecciono transferencia, lanzara este mensaje y parará
                            Toast.makeText(this, "Debes seleccionar un banco para el complemento", Toast.LENGTH_SHORT).show()
                            return
                        }
                    }
                    //Monto ingresado en complemento
                    var montoRecComp =  binding.etCxcMontoCom.text.toString().toDouble()
                    var complementoMontoStandard = 0.00
                    var montoComprar = 0.00

                    var montoPrinciStandard =  binding.etCxcMontoMain.text.toString().toDouble()

                    if(binding.rbCxcBssCom.isChecked){ // if que valida que moneda se seleccionó en complemento
                        // Si se selecciona bolivares hara la conversion a dolares
                        complementoMontoStandard = (montoRecComp / tasaCambioSeleccionadaPrincipal)
                    }
                    else{
                        // Si se selecciona dolares la moneda permanecera y se guardara en otra variable
                        complementoMontoStandard = montoRecComp
                    }

                    if(binding.rbCxcBssMain.isChecked){// if que valida que moneda se seleccionó en principal
                        // Si se selecciona bolivares hara la conversion a dolares
                        montoPrinciStandard = montoRec / tasaCambioSeleccionadaPrincipal
                    }
                    else{
                        // Si se selecciona dolares la moneda permanecera y se guardara en otra variable
                        montoPrinciStandard = montoRec
                    }

                    if(binding.rbCxcBssMain.isChecked){// if que valida que moneda se seleccionó en principal
                        // Si se selecciona bolivares hara la conversion a dolares
                        montoComprar = montoMinimoRec / tasaCambioSeleccionadaPrincipal
                    }
                    else{
                        // Si se selecciona dolares la moneda permanecera y se guardara en otra variable
                        montoComprar = montoMinimoRec
                    }

                    // IF que valida que la suma del principal y del complemento sean mayores al monto minimo requerido
                    if((complementoMontoStandard + montoPrinciStandard) < montoComprar){
                        Toast.makeText(this, "Montos insuficientes para completar ambos recibos 2", Toast.LENGTH_SHORT).show()
                        return
                    }
                    else{

                        referenciaCm = binding.etCxcRefCom.text.toString().uppercase()
                        //hace algo?
                        /*if(referenciaCm.equals("")){

                        }*/
                        //-----Inicio Tipo 5----- Probando 96-PRC-23040105
                        var ll_commit = false
                        ke_android = conn.writableDatabase

                        //listas con el tipo de datos para los recibos
                        var listaReciboPrCabecera:ArrayList<CXC>   = ArrayList()
                        var listaReciboPrLineas:  ArrayList<CXC>   = ArrayList()
                        var listaReciboCmCabecera:ArrayList<CXC>   = ArrayList()
                        var listaReciboCmLineas:  ArrayList<CXC>   = ArrayList()

                        //por que ordena el array si se supone que es un array de 1?
                        listaDocumentos.sortBy { it.fechaDocs }//llenado de datos de cabecera
                        var cxc = CXC()
                        cxc.id_recibo  = nroPrecobranza
                        cxc.tipoRecibo = "W"
                        cxc.codigoVend = cod_usuario.toString()
                        cxc.kecxc_id   = tasaId
                        cxc.tasadia    = tasaCambioSeleccionadaPrincipal
                        cxc.fchrecibo  = fechatasaH
                        cxc.clicontesp = listaDocumentos[0].contribesp.toString() //ATENCION, hay que ver como funciona cuando es mas de un documento
                        cxc.moneda     = monedaSeleccionadaPr
                        if(ignore){
                            cxc.bcocod     = ""
                            cxc.bcoref     = ""
                            cxc.efectivo   = montoRec
                            cxc.fchvigen   = fechaSuma(fechaActual, 60)
                        }
                        else{
                            cxc.bcocod     = codigoBancoCompleto
                            cxc.bcomonto   = montoRec
                            cxc.bcoref     = referenciaPrincipal
                            cxc.fchvigen   = fechaSuma(fechaActual, 5)
                        }

                        cxc.edorec     = "0"
                        cxc.fchhr      = fechaActual
                        //genero cabecera del complemento
                        var comp = CXC()
                        comp.id_recibo  = nroComplemento
                        comp.tipoRecibo = "W"
                        comp.codigoVend = cod_usuario.toString()
                        comp.kecxc_id   = tasaId
                        comp.tasadia    = tasaCambioSeleccionadaPrincipal
                        comp.fchrecibo  = fechatasaH
                        comp.clicontesp = listaDocumentos[0].contribesp.toString() //ATENCION, hay que ver como funciona cuando es mas de un documento
                        comp.moneda     = monedaSeleccionadaCm
                        if(ignorecm){
                            comp.bcocod     = ""
                            comp.bcoref     = ""
                            comp.efectivo   = montoRecComp
                            comp.fchvigen   = fechaSuma(fechaActual, 60)

                        }
                        else{
                            comp.bcocod     = codigoBancoComplemento
                            comp.bcoref     = referenciaCm
                            comp.bcomonto   = montoRecComp
                            comp.fchvigen   = fechaSuma(fechaActual, 5)
                        }
                        comp.edorec     = "0"
                        comp.fchhr      = fechaActual


                        //en este ciclo lleno la lista de precobradocs
                        for(i in listaDocumentos.indices){
                            var cxclineas = CXC()

                            cxclineas.id_recibo = nroPrecobranza
                            cxclineas.agencia   = listaDocumentos[i].agencia
                            cxclineas.tipodoc   = listaDocumentos[i].tipodoc
                            cxclineas.documento = listaDocumentos[i].documento
                            listaReciboPrLineas.add(cxclineas)

                        }

                        //en este ciclo, agrego las retenciones
                        // Porque pote retencion de flete en 0?
                        // de ser el caso que el cliente es contribuyente especial, pòrque no validar eso?
                        for (i in listaReciboPrLineas.indices){
                            if(listaReciboPrLineas[i].agencia.equals("002") && (listaReciboPrLineas[i].documento == listaDocumentos[i].documento)){
                                listaDocumentos[i].cbsretflete = 0.00

                            }

                            //llenado de campos de retenciones
                            //retenciones fase 3
                            for (j in listaRetGuardada.indices){
                                if(listaRetGuardada[j].nrodoc == listaReciboPrLineas[i].documento){
                                    if(listaRetGuardada[j].tiporet == "iva"){
                                        //si es de iva
                                        listaReciboPrLineas[i].nroret    = listaRetGuardada[j].nroret
                                        listaReciboPrLineas[i].fchemiret = listaRetGuardada[j].fecharet
                                        listaReciboPrLineas[i].bsretiva  = - listaRetGuardada[j].montoret
                                        listaReciboPrLineas[i].refret    = listaRetGuardada[j].refret
                                    }

                                    if(listaRetGuardada[j].tiporet == "flete"){
                                        //si es de flete
                                        listaReciboPrLineas[i].nroretfte    = listaRetGuardada[j].nroret
                                        listaReciboPrLineas[i].fchemirfte   = listaRetGuardada[j].fecharet
                                        listaReciboPrLineas[i].bsretfte     = - listaRetGuardada[j].montoret
                                        listaReciboPrLineas[i].refretfte    = listaRetGuardada[j].refret
                                    }

                                    if(listaRetGuardada[j].tiporet == "parme"){
                                        //si es de parme
                                        listaReciboPrLineas[i].retmun_nro    = listaRetGuardada[j].nroret
                                        listaReciboPrLineas[i].retmun_fch    = listaRetGuardada[j].fecharet
                                        listaReciboPrLineas[i].retmun_mto    = - listaRetGuardada[j].montoret
                                        listaReciboPrLineas[i].retmun_cod    = listaRetGuardada[j].refret
                                    }
                                }
                            }


                            //descuento ivas y fletes ( de haberlos)
                            if(listaReciboPrLineas[i].documento == listaDocumentos[i].documento && listaDocumentos[i].tipodocv == "FAC"){
                                //Aunque es el array de las lineas bsmtoiva y doliva se van para la cabecera
                                if(listaDocumentos[i].bsiva - listaDocumentos[i].bsmtoiva <= 0.00 ){
                                    listaReciboPrLineas[i].bsmtoiva = 0.00
                                    listaReciboPrLineas[i].doliva   = 0.00

                                }else{

                                    //descuento ivas del monto del recibo original .-
                                    var restaIvadol = if (binding.cbExcReten.isChecked) listaDocumentos[i].dtotimpuest else listaDocumentos[i].dtotimpuest - listaDocumentos[i].cdretencioniva
                                    //2023-04-03 comentario por no tener en cuenta cuando se escluyen retenciones
                                    //var restaIvadol = listaDocumentos[i].dtotimpuest - listaDocumentos[i].cdretencioniva
                                    var restaIvabss = if (binding.cbExcReten.isChecked) listaDocumentos[i].bsiva else listaDocumentos[i].bsiva - listaDocumentos[i].cbsretencioniva
                                    //var restaIvabss = listaDocumentos[i].bsiva - listaDocumentos[i].cbsretencioniva

                                    if(binding.rbCxcDivisasMain.isChecked){
                                        //hago el descuento del iva del nmonto de pago
                                        montoRec -= restaIvadol

                                        if(montoRec < 0.00){
                                            Toast.makeText(this, "Monto insuficiente", Toast.LENGTH_SHORT).show()
                                            return
                                        }

                                        listaReciboPrLineas[i].bscobro  += (if (binding.cbExcReten.isChecked) listaDocumentos[i].bsiva else listaDocumentos[i].bsiva - listaDocumentos[i].cbsretencioniva)
                                        listaReciboPrLineas[i].bsmtoiva = listaDocumentos[i].bsiva
                                        listaReciboPrLineas[i].doliva   = if (binding.cbExcReten.isChecked) listaDocumentos[i].dtotimpuest else listaDocumentos[i].dtotimpuest - listaDocumentos[i].cdretencioniva
                                        //2023-04-03 comentado por mal calculo?
                                        //listaReciboPrLineas[i].tnetoddol  += listaReciboPrLineas[i].doliva
                                        listaReciboPrLineas[i].tnetoddol  += (if (binding.cbExcReten.isChecked) listaDocumentos[i].dtotimpuest else listaDocumentos[i].dtotimpuest - listaDocumentos[i].cdretencioniva)

                                    }
                                    else{
                                        montoRec -= restaIvabss
                                        if(montoRec < 0.00){
                                            Toast.makeText(this, "Monto insuficiente", Toast.LENGTH_SHORT).show()
                                            return
                                        }
                                        listaReciboPrLineas[i].bscobro  += ((if (binding.cbExcReten.isChecked) listaDocumentos[i].bsiva else listaDocumentos[i].bsiva - listaDocumentos[i].cbsretencioniva))
                                        listaReciboPrLineas[i].bsmtoiva = listaDocumentos[i].bsiva
                                        listaReciboPrLineas[i].doliva   = if (binding.cbExcReten.isChecked) listaDocumentos[i].dtotimpuest else listaDocumentos[i].dtotimpuest - listaDocumentos[i].cdretencioniva
                                        listaReciboPrLineas[i].tnetodbs += (if (binding.cbExcReten.isChecked) listaDocumentos[i].bsiva else listaDocumentos[i].bsiva - listaDocumentos[i].cbsretencioniva)
                                        //listaReciboPrLineas[i].tnetodbs += listaDocumentos[i].bsiva
                                    }
                                }

                                //descuento del flete de los documentos
                                // 1 para indicar que si le falta 1bs de flete ignorarlo?
                                if(listaDocumentos[i].bsflete - listaDocumentos[i].bsmtofte <= 1){
                                    listaReciboPrLineas[i].bsmtofte = 0.00
                                    listaReciboPrLineas[i].dolflete = 0.00

                                }else{



                                    if(binding.rbCxcDivisasMain.isChecked){

                                        //si aqui ya llega el monto en 0 o menos
                                        // Pienso que es redundante por que antes se dijo que si no cumple con la suma de iva y flete no pasa
                                        if(montoRec <= 0.00){
                                            Toast.makeText(this, "Monto insuficiente", Toast.LENGTH_SHORT).show()
                                            return
                                        }
                                        //descuento del monto del recibo, el flete
                                        montoRec -= if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dFlete - listaDocumentos[i].cdretflete

                                        //de no llegar a menos de cero, lo agrego al monto cobrado en bss
                                        // por que el boton de excluir retenciones si esta declarado aqui y no en IVA
                                        if(binding.cbExcReten.isChecked){
                                            listaReciboPrLineas[i].bscobro  += listaDocumentos[i].bsflete
                                        }else{
                                            listaReciboPrLineas[i].bscobro  += listaDocumentos[i].bsflete - listaDocumentos[i].cbsretflete
                                        }
                                        listaReciboPrLineas[i].bsmtofte = listaDocumentos[i].bsflete
                                        listaReciboPrLineas[i].dolflete = if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dFlete - listaDocumentos[i].cdretflete
                                        listaReciboPrLineas[i].tnetoddol +=  if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dFlete - listaDocumentos[i].cdretflete

                                    }else{

                                        //si aqui ya llega el monto en 0 o menos
                                        if(montoRec <= 0.00){
                                            Toast.makeText(this, "Monto insuficiente", Toast.LENGTH_SHORT).show()
                                            return
                                        }
                                        //descuento del monto del recibo, el flete
                                        montoRec -= if (binding.cbExcReten.isChecked) listaDocumentos[i].bsflete else listaDocumentos[i].bsflete - listaDocumentos[i].cbsretflete
                                        if(montoRec < 0.00){
                                            Toast.makeText(this, "Monto insuficiente", Toast.LENGTH_SHORT).show()
                                            return
                                        }
                                        //de no llegar a menos de cero, lo agrego al monto cobrado en bss

                                        listaReciboPrLineas[i].bscobro  += ((if (binding.cbExcReten.isChecked) listaDocumentos[i].bsflete else listaDocumentos[i].bsflete - listaDocumentos[i].cbsretflete))
                                        listaReciboPrLineas[i].bsmtofte = listaDocumentos[i].bsflete
                                        listaReciboPrLineas[i].dolflete = if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dFlete - listaDocumentos[i].cdretflete
                                        listaReciboPrLineas[i].tnetodbs  += if (binding.cbExcReten.isChecked) listaDocumentos[i].bsflete else listaDocumentos[i].bsflete - listaDocumentos[i].cbsretflete

                                    }

                                }

                            }else{
                                if(listaDocumentos[i].bsflete - listaDocumentos[i].bsmtofte <= 1){
                                    listaReciboPrLineas[i].bsmtofte = 0.00
                                    listaReciboPrLineas[i].dolflete = 0.00
                                }else{



                                    var fleteaCobrar = if (binding.cbExcReten.isChecked) listaDocumentos[i].bsflete else listaDocumentos[i].bsflete - listaDocumentos[i].cbsretflete
                                    listaReciboPrLineas[i].bscobro  += fleteaCobrar

                                    if(binding.rbCxcDivisasMain.isChecked){
                                        montoRec -= if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dFlete - listaDocumentos[i].cdretflete
                                        listaReciboPrLineas[i].bsmtofte = listaDocumentos[i].bsflete
                                        listaReciboPrLineas[i].dolflete = if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dFlete - listaDocumentos[i].cdretflete
                                        listaReciboPrLineas[i].tnetoddol +=  if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dFlete - listaDocumentos[i].cdretflete
                                    }else{
                                        montoRec -= if (binding.cbExcReten.isChecked) listaDocumentos[i].bsflete else listaDocumentos[i].bsflete - listaDocumentos[i].cbsretflete
                                        listaReciboPrLineas[i].bsmtofte = listaDocumentos[i].bsflete
                                        listaReciboPrLineas[i].dolflete = if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dFlete - listaDocumentos[i].cdretflete
                                        listaReciboPrLineas[i].tnetodbs  += if (binding.cbExcReten.isChecked) listaDocumentos[i].bsflete else listaDocumentos[i].bsflete - listaDocumentos[i].cbsretflete
                                    }

                                }

                            }
                        }

                        //llenado de los netos
                        for (i in listaReciboPrLineas.indices){


                            if(montoRec < 1){
                                // borré el return
                            }
                            else{
                                if(binding.rbCxcDivisasMain.isChecked){
                                    var netoTV = binding.tvCxcNeto.text.toString().toDouble()

                                    if(netoTV > 0.00){
                                        var netoRealenDoc = listaDocumentos[i].netorestante

                                        if( montoRec >= netoRealenDoc){
                                            var bscobrado = netoRealenDoc * listaDocumentos[i].tasadoc
                                            bscobrado     = bscobrado

                                            listaReciboPrLineas[i].bscobro += bscobrado


                                            netocobrado =  listaDocumentos[i].netorestante
                                            listaReciboPrLineas[i].dolneto = netocobrado
                                            montoRec -= netocobrado
                                            listaReciboPrLineas[i].ispagadoTotal = "1"
                                            listaReciboPrLineas[i].tnetoddol += netocobrado
                                        }

                                        else if(netoRealenDoc > montoRec && montoRec > 0) {

                                            var cobroAbono = montoRec * listaDocumentos[i].tasadoc

                                            listaReciboPrLineas[i].bscobro += cobroAbono
                                            netocobrado = montoRec
                                            listaReciboPrLineas[i].dolneto = valorReal(netocobrado)
                                            montoRec -= netocobrado
                                            montoRec= valorReal(montoRec)
                                            listaReciboPrLineas[i].ispagadoTotal = "0"
                                            listaReciboPrLineas[i].tnetoddol += netocobrado
                                        }

                                    }
                                }

                                if(binding.rbCxcBssMain.isChecked){
                                    var netoRealenDoc = listaDocumentos[i].netorestante
                                    netoRealenDoc = valorReal(netoRealenDoc)

                                    //si el monto del recibo cubre el monto del documento
                                    if( montoRec >= (netoRealenDoc * tasaCambioSeleccionadaPrincipal)){
                                        var bscobrado = valorReal(netoRealenDoc * listaDocumentos[i].tasadoc)
                                        listaReciboPrLineas[i].bscobro += bscobrado
                                        netocobrado = listaDocumentos[i].netorestante
                                        listaReciboPrLineas[i].dolneto = netocobrado
                                        listaReciboPrLineas[i].bsneto = valorReal(netoRealenDoc * tasaCambioSeleccionadaPrincipal)
                                        listaReciboPrLineas[i].tnetodbs  += valorReal(netoRealenDoc * tasaCambioSeleccionadaPrincipal)
                                        listaReciboPrLineas[i].ispagadoTotal = "1"
                                        montoRec -= valorReal(netoRealenDoc * tasaCambioSeleccionadaPrincipal)

                                    }
                                    //si el monto del recibo no cubre el monto completo del documento (se comporta como un abono)
                                    else if((netoRealenDoc * tasaCambioSeleccionadaPrincipal) > montoRec && montoRec > 0) {
                                        netocobrado = montoRec / tasaCambioSeleccionadaPrincipal
                                        var cobroAbono = netocobrado * listaDocumentos[i].tasadoc
                                        listaReciboPrLineas[i].bscobro += valorReal(cobroAbono)
                                        var dolneto = netocobrado
                                        listaReciboPrLineas[i].dolneto    = valorReal(dolneto)
                                        listaReciboPrLineas[i].bsneto = montoRec
                                        listaReciboPrLineas[i].tnetodbs  += montoRec
                                        listaReciboPrLineas[i].ispagadoTotal = "0"
                                        montoRec -= montoRec
                                    }
                                }



                                /*recorrido de lista de descuentos para asignar cantidad según nro doc. (de tener)*/
                                for(j in listaDescuentos.indices){
                                    if(listaDescuentos[j].nrodoc == listaDocumentos[i].documento){
                                        listaReciboPrLineas[i].prcdsctopp = listaDescuentos[j].pordscto
                                    }
                                }

                            }
                        }

                        //recorrido del complemento
                        //tambien voy a pagar el neto pero con complemento de los documentos ya pagos
                        //las lineas del recibo del complemento
                        for (i in listaReciboPrLineas.indices){
                            //valido si fue pagado completo
                            if(listaReciboPrLineas[i].ispagadoTotal.equals("1")){


                            }
                            else{
                                //de no estar pago completo, le aplico el complemento
                                var complineas = CXC()
                                complineas.id_recibo = nroComplemento
                                complineas.agencia   = listaReciboPrLineas[i].agencia
                                complineas.tipodoc   = listaReciboPrLineas[i].tipodoc
                                complineas.documento = listaReciboPrLineas[i].documento
                                listaReciboCmLineas.add(complineas)
                            }

                        }

                        //llenado neto del complemento
                        for (i in listaReciboCmLineas.indices) {

                            if(montoRecComp <= 0){

                            }else {
                                if(binding.rbCxcDivisasCom.isChecked){
                                    for(j in listaDocumentos.indices){
                                        if(listaDocumentos[j].documento == listaReciboCmLineas[i].documento){
                                            var netoRealenDoc = listaDocumentos[j].netorestante

                                            if(montoRecComp > netoRealenDoc){

                                                var bscobrado = netoRealenDoc * listaDocumentos[j].tasadoc
                                                // 2023-03-27 muy redundante
                                                //bscobrado     = bscobrado

                                                listaReciboCmLineas[j].bscobro += bscobrado


                                                netocobrado =  listaDocumentos[j].netorestante
                                                listaReciboCmLineas[i].dolneto = netocobrado
                                                listaReciboCmLineas[i].tnetoddol += netocobrado
                                                montoRecComp -= netocobrado

                                            }else if(netoRealenDoc >montoRecComp && montoRecComp > 0) {

                                                var cobroAbono = montoRecComp * listaDocumentos[j].tasadoc

                                                listaReciboCmLineas[i].bscobro += cobroAbono
                                                netocobrado = montoRecComp
                                                listaReciboCmLineas[i].dolneto = valorReal(netocobrado)
                                                montoRecComp  = valorReal(montoRecComp)
                                                listaReciboCmLineas[i].tnetoddol += netocobrado
                                                montoRecComp -= netocobrado

                                            }

                                        }else{
                                            //do nothing papa
                                        }
                                    }

                                }

                                if(binding.rbCxcBssCom.isChecked){
                                    for(j in listaDocumentos.indices){
                                        if(listaReciboCmLineas[i].documento == listaDocumentos[j].documento){
                                            var netoRealenDoc = listaDocumentos[j].netorestante
                                            netoRealenDoc = valorReal(netoRealenDoc)

                                            //si el monto del recibo cubre el monto del documento
                                            if( montoRecComp > (netoRealenDoc * tasaCambioSeleccionadaPrincipal)){
                                                var bscobrado = valorReal(netoRealenDoc * listaDocumentos[j].tasadoc)
                                                listaReciboCmLineas[i].bscobro += bscobrado

                                                netocobrado = listaDocumentos[j].netorestante
                                                listaReciboCmLineas[i].dolneto = netocobrado
                                                listaReciboCmLineas[i].bsneto = valorReal(netoRealenDoc * tasaCambioSeleccionadaPrincipal)
                                                listaReciboCmLineas[i].tnetodbs += valorReal(netoRealenDoc * tasaCambioSeleccionadaPrincipal)
                                                montoRecComp -= valorReal(netoRealenDoc * listaDocumentos[j].tasadoc)
                                            }

                                            //si el monto del recibo no cubre el monto completo del documento (se comporta como un abono)
                                            else if((netoRealenDoc * tasaCambioSeleccionadaPrincipal) > montoRecComp && montoRecComp > 0) {


                                                var dolneto = (montoRecComp / tasaCambioSeleccionadaPrincipal)
                                                var cobroAbono = valorReal(dolneto) * listaDocumentos[j].tasadoc
                                                listaReciboCmLineas[i].bscobro += valorReal(cobroAbono)
                                                listaReciboCmLineas[i].dolneto = valorReal(dolneto)
                                                listaReciboCmLineas[i].bsneto = montoRecComp
                                                listaReciboCmLineas[i].tnetodbs  += montoRecComp
                                                montoRecComp -= montoRecComp

                                            }
                                        }
                                    }
                                }
                            }
                        }



                        /*esto va a ser resultado de la suma de los campos de la lista (ke_precobranza)
                         de detalles */
                        var difReteIva   = valorReal(listaReciboPrLineas.sumOf { it.bsmtoiva }) + valorReal(listaReciboPrLineas.sumOf { it.bsretiva })
                        var difRetyFlete = valorReal(listaReciboPrLineas.sumOf { it.bsmtofte }) + valorReal(listaReciboPrLineas.sumOf { it.bsretfte })
                        var netoReal   = valorReal(listaReciboPrLineas.sumOf { it.bscobro }) - difReteIva - difRetyFlete //--revisar si esto es necesario
                        cxc.bsneto     = valorReal(listaReciboPrLineas.sumOf { it.bsneto })
                        cxc.bsretiva   = valorReal(listaReciboPrLineas.sumOf { it.bsretiva })
                        cxc.bsiva      = valorReal(listaReciboPrLineas.sumOf { it.bsmtoiva })
                        cxc.bsflete    = valorReal(listaReciboPrLineas.sumOf { it.bsmtofte })

                        /* sumo los bss en total y los redondeo al momento de guardarlo */
                        var bssumaTotal  = valorReal(listaReciboPrLineas.sumOf { it.bscobro })

                        //cxc.bstotal    = valorReal(bssumaTotal)
                        cxc.bstotal    = valorReal(listaReciboPrLineas.sumOf { it.bsneto } + (listaReciboPrLineas.sumOf { it.bsmtoiva } + listaReciboPrLineas.sumOf { it.bsretiva }) + (listaReciboPrLineas.sumOf { it.bsmtofte } + listaReciboPrLineas.sumOf { it.bsretfte }))
                        //cxc.dolneto    = valorReal(listaDocumentos.sumOf{it.dtotneto }) //<-------------------- Revisar si es necesario
                        cxc.dolneto    = valorReal(listaReciboPrLineas.sumOf{it.dolneto })
                        cxc.doliva     = valorReal(listaReciboPrLineas.sumOf { it.doliva })
                        //cxc.dolretiva  = valorReal(listaDocumentos.sumOf { it.cdretencioniva })
                        cxc.dolflete   = valorReal(listaReciboPrLineas.sumOf{it.dolflete})
                        cxc.doltotal = valorReal(listaReciboPrLineas.sumOf{it.dolneto} + listaReciboPrLineas.sumOf {it.doliva} + listaReciboPrLineas.sumOf{it.dolflete})
                        cxc.netocob    = if(monedaSeleccionadaPr == "2") listaReciboPrLineas.sumOf{it.dolneto} else listaReciboPrLineas.sumOf{it.bsneto}
                        //2023-07-14 se comento devido a que es un calculo errado, debido a que se trata flete con la tasa del dia y no con la tasa del documento
                        /*var doltotal   = binding.etCxcMontoMain.text.toString().toDouble()
                        if(binding.rbCxcDivisasMain.isChecked){
                            cxc.doltotal   = valorReal(doltotal)
                            cxc.netocob    = valorReal(doltotal - listaReciboPrLineas.sumOf{it.dolflete} - listaReciboPrLineas.sumOf{it.doliva})

                        }
                        else{
                            doltotal /= tasaCambioSeleccionadaPrincipal
                            cxc.doltotal   = valorReal(doltotal)
                            cxc.netocob    = valorReal(doltotal - listaReciboPrLineas.sumOf{it.dolflete} - listaReciboPrLineas.sumOf{it.doliva})
                        }*/
                        cxc.bsretflete = valorReal(listaReciboPrLineas.sumOf { it.bsretfte })
                        cxc.retmun_sbi = 0.00//definir
                        cxc.retmun_sbs = 0.00//definir
                        //var fechaVigen = fechaSuma(fechaActual, 3)
                        //println("ojo")
                        //cxc.fchvigen   = fechaVigen
                        cxc.moneda     = monedaSeleccionadaPr
                        cxc.tasadia    = tasaCambioSeleccionadaPrincipal
                        listaReciboPrCabecera.add(cxc)

                        try{

                            // inicio la transacción
                            ke_android.beginTransaction()
                            var qcabecera:ContentValues = ContentValues()
                            var qlineas:  ContentValues = ContentValues()

                            for (i in listaReciboPrCabecera.indices){
                                qcabecera.put("cxcndoc", listaReciboPrCabecera[i].id_recibo)
                                qcabecera.put("tiporecibo", listaReciboPrCabecera[i].tipoRecibo)
                                qcabecera.put("codvend", listaReciboPrCabecera[i].codigoVend)
                                qcabecera.put("tiporecibo", listaReciboPrCabecera[i].tipoRecibo)
                                qcabecera.put("kecxc_id", listaReciboPrCabecera[i].kecxc_id)
                                qcabecera.put("tasadia", listaReciboPrCabecera[i].tasadia)
                                qcabecera.put("fchrecibo", listaReciboPrCabecera[i].fchrecibo)
                                qcabecera.put("bsneto", listaReciboPrCabecera[i].bsneto)
                                qcabecera.put("bsiva", listaReciboPrCabecera[i].bsiva)
                                qcabecera.put("bsretiva", listaReciboPrCabecera[i].bsretiva)
                                qcabecera.put("bsflete", listaReciboPrCabecera[i].bsflete)
                                qcabecera.put("bstotal", listaReciboPrCabecera[i].bstotal)
                                qcabecera.put("dolneto", listaReciboPrCabecera[i].dolneto)
                                qcabecera.put("doliva", listaReciboPrCabecera[i].doliva)
                                //qcabecera.put("dolretiva", listaReciboPrCabecera[i].dolretiva)
                                qcabecera.put("dolflete", listaReciboPrCabecera[i].dolflete)
                                qcabecera.put("doltotal", listaReciboPrCabecera[i].doltotal)
                                qcabecera.put("moneda", listaReciboPrCabecera[i].moneda)
                                if(ignore == true){
                                    qcabecera.put("efectivo", listaReciboPrCabecera[i].efectivo)
                                }else{
                                    qcabecera.put("bcocod", listaReciboPrCabecera[i].bcocod)
                                    qcabecera.put("bcomonto", listaReciboPrCabecera[i].bcomonto)
                                    qcabecera.put("bcoref", listaReciboPrCabecera[i].bcoref)
                                }
                                qcabecera.put("edorec", listaReciboPrCabecera[i].edorec)
                                qcabecera.put("fchvigen", listaReciboPrCabecera[i].fchvigen)
                                qcabecera.put("bsretflete", listaReciboPrCabecera[i].bsretflete)
                                qcabecera.put("netocob", listaReciboPrCabecera[i].netocob)
                                qcabecera.put("fechamodifi", getFechaHoy())
                                //qcabecera.put("docdifcamb",diferencialCambiario)

                                for(j in listaReciboPrLineas.indices){
                                    qlineas.put("cxcndoc", listaReciboPrLineas[j].id_recibo)
                                    qlineas.put("agencia",   listaReciboPrLineas[j].agencia)
                                    qlineas.put("tipodoc",   listaReciboPrLineas[j].tipodoc)
                                    qlineas.put("documento", listaReciboPrLineas[j].documento)
                                    qlineas.put("bscobro", listaReciboPrLineas[j].bscobro)
                                    qlineas.put("prcdsctopp", listaReciboPrLineas[j].prcdsctopp)
                                    qlineas.put("nroret", listaReciboPrLineas[j].nroret)
                                    qlineas.put("fchemiret", listaReciboPrLineas[j].fchemiret)
                                    qlineas.put("bsretiva", listaReciboPrLineas[j].bsretiva)
                                    qlineas.put("refret", listaReciboPrLineas[j].refret)
                                    qlineas.put("nroretfte", listaReciboPrLineas[j].nroretfte)
                                    qlineas.put("fchemirfte", listaReciboPrLineas[j].fchemirfte)
                                    qlineas.put("bsmtofte", listaReciboPrLineas[j].bsmtofte)
                                    qlineas.put("bsretfte", listaReciboPrLineas[j].bsretfte)
                                    qlineas.put("refretfte", listaReciboPrLineas[j].refretfte)
                                    qlineas.put("bsmtoiva", listaReciboPrLineas[j].bsmtoiva)
                                    qlineas.put("tnetoddol", listaReciboPrLineas[j].tnetoddol)
                                    qlineas.put("tnetodbs", listaReciboPrLineas[j].tnetodbs)
                                    qlineas.put("fchrecibod", getFechaNow())
                                    qlineas.put("kecxc_idd", listaReciboPrCabecera[i].kecxc_id)
                                    qlineas.put("tasadiad", listaReciboPrCabecera[i].tasadia)
                                    qlineas.put("reten", retennn)
                                    ke_android.insert("ke_precobradocs", null, qlineas)
                                }
                            }
                            //ke_android.execSQL("UPDATE ke_precobradocs SET afavor= ${guardaSaldoFavor(montoRec, binding.rbCxcDivisasMain, binding.rbCxcCompMain, binding.etCxcMontoMain.text.toString().toDouble(), binding.tvCxcTotal)} WHERE cxcndoc='${listaReciboPrLineas[listaReciboPrLineas.size-1].id_recibo}' AND documento = '${listaReciboPrLineas[listaReciboPrLineas.size-1].documento}';")
                            ke_android.insert("ke_precobranza", null, qcabecera)


                            var qcorrelativo: ContentValues = ContentValues()
                            qcorrelativo.put("kcor_numero", nroCorrelativo)
                            qcorrelativo.put("kcor_vendedor", cod_usuario)

                            ke_android.insert("ke_corprec", null, qcorrelativo)
                            ll_commit = true

                        }catch (exception: SQLException){

                            exception.printStackTrace()
                            ll_commit = false

                            ke_android.endTransaction()
                            if(!ll_commit){
                                return
                            }
                        }



                        //preparacion para el guardado de datos
                        var netoCmReal   = valorReal(listaReciboCmLineas.sumOf { it.bscobro })
                        comp.bsneto      = valorReal(listaReciboCmLineas.sumOf { it.bsneto })

                        /* sumo los bss en total y los redondeo al momento de guardarlo */
                        var bssumaTotalCm  = valorReal(listaReciboCmLineas.sumOf { it.bscobro })

                        comp.bstotal  = comp.bsneto
                        comp.dolneto     = valorReal(listaReciboCmLineas.sumOf{it.dolneto } )
                        var doltotalCm   = binding.etCxcMontoCom.text.toString().toDouble()

                        comp.doltotal   = comp.dolneto
                        comp.netocob    = if(monedaSeleccionadaCm == "2") listaReciboCmLineas.sumOf{it.dolneto} else listaReciboCmLineas.sumOf{it.bsneto}
                        /*if(binding.rbCxcDivisasCom.isChecked){
                            comp.doltotal   = valorReal(doltotalCm)
                            comp.netocob    = doltotalCm

                        }else{
                            doltotalCm = doltotalCm / tasaCambioSeleccionadaPrincipal
                            comp.doltotal    = valorReal(doltotalCm)
                            comp.netocob     = doltotalCm
                        }*/

                        //var fechaVigenCm = fechaSuma(fechaActual, 3)
                        //comp.fchvigen    = fechaVigenCm
                        comp.moneda      = monedaSeleccionadaCm
                        comp.tasadia     = tasaCambioSeleccionadaPrincipal
                        listaReciboCmCabecera.add(comp)

                        try{

                            var qcabecera: ContentValues = ContentValues()
                            var qlineas:   ContentValues = ContentValues()

                            for (i in listaReciboCmCabecera.indices){

                                qcabecera.put("cxcndoc", listaReciboCmCabecera[i].id_recibo)
                                qcabecera.put("tiporecibo", listaReciboCmCabecera[i].tipoRecibo)
                                qcabecera.put("codvend", listaReciboCmCabecera[i].codigoVend)
                                qcabecera.put("tiporecibo", listaReciboCmCabecera[i].tipoRecibo)
                                qcabecera.put("kecxc_id", listaReciboCmCabecera[i].kecxc_id)
                                qcabecera.put("tasadia", listaReciboCmCabecera[i].tasadia)
                                qcabecera.put("fchrecibo", listaReciboCmCabecera[i].fchrecibo)
                                qcabecera.put("bsneto", listaReciboCmCabecera[i].bsneto)
                                qcabecera.put("bstotal", listaReciboCmCabecera[i].bstotal)
                                qcabecera.put("dolneto", listaReciboCmCabecera[i].dolneto)
                                qcabecera.put("doltotal", listaReciboCmCabecera[i].doltotal)
                                qcabecera.put("moneda", listaReciboCmCabecera[i].moneda)
                                if(ignorecm == true){
                                    qcabecera.put("efectivo", listaReciboCmCabecera[i].efectivo)
                                }else{
                                    qcabecera.put("bcocod", listaReciboCmCabecera[i].bcocod)
                                    qcabecera.put("bcomonto", listaReciboCmCabecera[i].bcomonto)
                                    qcabecera.put("bcoref", listaReciboCmCabecera[i].bcoref)
                                }
                                qcabecera.put("edorec", listaReciboCmCabecera[i].edorec)
                                qcabecera.put("fchvigen", listaReciboCmCabecera[i].fchvigen)
                                qcabecera.put("netocob", listaReciboCmCabecera[i].netocob)
                                qcabecera.put("fechamodifi", getFechaHoy())
                                //qcabecera.put("docdifcamb",diferencialCambiario)

                                for(j in listaReciboCmLineas.indices){
                                    qlineas.put("cxcndoc", listaReciboCmLineas[j].id_recibo)
                                    qlineas.put("agencia",   listaReciboCmLineas[j].agencia)
                                    qlineas.put("tipodoc",   listaReciboCmLineas[j].tipodoc)
                                    qlineas.put("documento", listaReciboCmLineas[j].documento)
                                    qlineas.put("bscobro", listaReciboCmLineas[j].bscobro)
                                    qlineas.put("tnetoddol", listaReciboCmLineas[j].tnetoddol)
                                    qlineas.put("tnetodbs", listaReciboCmLineas[j].tnetodbs)
                                    qlineas.put("fchrecibod", getFechaNow())
                                    qlineas.put("kecxc_idd", listaReciboCmCabecera[i].kecxc_id)
                                    qlineas.put("tasadiad", listaReciboCmCabecera[i].tasadia)
                                    qlineas.put("reten", 1)
                                    ke_android.insert("ke_precobradocs", null, qlineas)
                                }

                            }
                            //ke_android.execSQL("UPDATE ke_precobradocs SET afavor= ${guardaSaldoFavor(montoRecComp, binding.rbCxcDivisasCom, binding.rbCxcCompMain, (binding.etCxcMontoCom.text.toString().toDouble() + binding.etCxcMontoCom.text.toString().toDouble()) , binding.tvCxcTotal)} WHERE cxcndoc='${listaReciboCmLineas[listaReciboCmLineas.size-1].id_recibo}' AND documento = '${listaReciboCmLineas[listaReciboCmLineas.size-1].documento}';")
                            guardaSaldoFavor2(binding.rbCxcDivisasCom, binding.rbCxcCompMain, binding.etCxcMontoMain.text.toString().toDouble(), binding.etCxcMontoCom.text.toString().toDouble(),  binding.tvCxcTotal, listaReciboCmLineas[listaReciboCmLineas.size-1].id_recibo, listaReciboCmLineas[listaReciboCmLineas.size-1].documento, binding.rbCxcDivisasCom.isChecked)
                            ke_android.insert("ke_precobranza", null, qcabecera)

                            var qcorrelativo: ContentValues = ContentValues()
                            qcorrelativo.put("kcor_numero", nroCorrelativoCom)
                            qcorrelativo.put("kcor_vendedor", cod_usuario)

                            ke_android.insert("ke_corprec", null, qcorrelativo)
                            ll_commit = true


                        }catch(exception: SQLException){
                            exception.printStackTrace()
                            ll_commit = false

                            ke_android.endTransaction()
                            if(!ll_commit){
                                return
                            }
                        }

                        if(ll_commit){
                            ke_android.setTransactionSuccessful()
                            ke_android.endTransaction()

                            var listadatos: ArrayList<CXC> = ArrayList()
                            listadatos.add(cxc)
                            listadatos[0].cliente = codigoCliente

                            var dialog: DialogRecibo = DialogRecibo()
                            dialog.DialogRecibo(this, listadatos)

                            Toast.makeText(this, "RECIBO CREADO", Toast.LENGTH_SHORT).show()
                            // finish()
                        }else{
                            ke_android.endTransaction()
                        }
                    }
                }
            }
            else if(montoRec >= montoMinimoRec && !binding.cbCxcComplemento.isChecked){
                //-----Inicio de la ruta buena-----
                var ll_commit = false
                ke_android = conn.writableDatabase

                //listas con el tipo de datos para los recibos
                var listaReciboPrCabecera:ArrayList<CXC>  = ArrayList()
                var listaReciboPrLineas: ArrayList<CXC>   = ArrayList()

                listaDocumentos.sortBy { it.fechaDocs }//llenado de datos de cabecera
                var cxc = CXC()
                cxc.id_recibo  = nroPrecobranza
                cxc.tipoRecibo = "W"
                cxc.codigoVend = cod_usuario.toString()
                cxc.kecxc_id   = tasaId
                cxc.tasadia    = tasaCambioSeleccionadaPrincipal
                cxc.fchrecibo  = fechatasaH
                cxc.clicontesp = listaDocumentos[0].contribesp.toString() //esto lo jalo de  la lista de docs?
                cxc.moneda     = monedaSeleccionadaPr
                if(ignore){// posible problema de la suma de fechas
                    cxc.bcocod     = ""
                    cxc.bcoref     = ""
                    cxc.efectivo   = montoRec
                    cxc.fchvigen   = fechaSuma(fechaActual, 60)
                }
                else{
                    cxc.bcocod     = codigoBancoCompleto
                    cxc.bcomonto   = montoRec
                    cxc.bcoref     = referenciaPrincipal
                    cxc.fchvigen   = fechaSuma(fechaActual, 5)
                }
                cxc.edorec     = "0"
                cxc.fchhr      = fechaActual


                //en este ciclo lleno la lista de precobradocs
                for(i in listaDocumentos.indices){
                    var cxclineas = CXC()

                    cxclineas.id_recibo = nroPrecobranza
                    cxclineas.agencia   = listaDocumentos[i].agencia
                    cxclineas.tipodoc   = listaDocumentos[i].tipodoc
                    cxclineas.documento = listaDocumentos[i].documento
                    listaReciboPrLineas.add(cxclineas)

                }

                //en este ciclo, agrego las retenciones
                for (i in listaReciboPrLineas.indices){
                    if(listaReciboPrLineas[i].agencia.equals("002") && (listaReciboPrLineas[i].documento == listaDocumentos[i].documento)){
                        listaDocumentos[i].cbsretflete = 0.00

                    }

                    //llenado de campos de retenciones
                    //retenciones fase 4
                    for (j in listaRetGuardada.indices){
                        if(listaReciboPrLineas[i].tipodoc == "N/E"){

                        }
                        else{
                            if(listaRetGuardada[j].nrodoc == listaReciboPrLineas[i].documento){
                                if(listaRetGuardada[j].tiporet == "iva"){
                                    //si es de iva
                                    listaReciboPrLineas[i].nroret    = listaRetGuardada[j].nroret
                                    listaReciboPrLineas[i].fchemiret = listaRetGuardada[j].fecharet
                                    listaReciboPrLineas[i].bsretiva  = - listaRetGuardada[j].montoret
                                    listaReciboPrLineas[i].refret    = listaRetGuardada[j].refret
                                }

                                if(listaRetGuardada[j].tiporet == "flete"){
                                    //si es de flete
                                    listaReciboPrLineas[i].nroretfte    = listaRetGuardada[j].nroret
                                    listaReciboPrLineas[i].fchemirfte   = listaRetGuardada[j].fecharet
                                    listaReciboPrLineas[i].bsretfte     = - listaRetGuardada[j].montoret
                                    listaReciboPrLineas[i].refretfte    = listaRetGuardada[j].refret
                                }

                                if(listaRetGuardada[j].tiporet == "parme"){
                                    //si es de parme
                                    listaReciboPrLineas[i].retmun_nro    = listaRetGuardada[j].nroret
                                    listaReciboPrLineas[i].retmun_fch    = listaRetGuardada[j].fecharet
                                    listaReciboPrLineas[i].retmun_mto    = - listaRetGuardada[j].montoret
                                    listaReciboPrLineas[i].retmun_cod    = listaRetGuardada[j].refret
                                }
                            }
                        }

                    }


                    //descuento ivas y fletes ( de haberlos)
                    if(listaReciboPrLineas[i].documento == listaDocumentos[i].documento && listaDocumentos[i].tipodocv == "FAC"){

                        if(listaDocumentos[i].bsiva - listaDocumentos[i].bsmtoiva <= 0.00 ){
                            listaReciboPrLineas[i].bsmtoiva = 0.00
                            listaReciboPrLineas[i].doliva   = 0.00

                        }
                        else{

                            //descuento ivas del monto del recibo original .-
                            var restaIvadol = if (binding.cbExcReten.isChecked) listaDocumentos[i].dtotimpuest else listaDocumentos[i].dtotimpuest - listaDocumentos[i].cdretencioniva
                            //2023-04-03 comentario por no tener en cuenta cuando se escluyen retenciones
                            //var restaIvadol = listaDocumentos[i].dtotimpuest - listaDocumentos[i].cdretencioniva
                            var restaIvabss = if (binding.cbExcReten.isChecked) listaDocumentos[i].bsiva else listaDocumentos[i].bsiva - listaDocumentos[i].cbsretencioniva
                            //var restaIvabss = listaDocumentos[i].bsiva - listaDocumentos[i].cbsretencioniva

                            if(binding.rbCxcDivisasMain.isChecked){
                                //hago el descuento del iva del nmonto de pago
                                montoRec -= restaIvadol

                                if(montoRec < 0.00){
                                    Toast.makeText(this, "Monto insuficiente", Toast.LENGTH_SHORT).show()
                                    return
                                }

                                listaReciboPrLineas[i].bscobro  += if (binding.cbExcReten.isChecked) listaDocumentos[i].bsiva else listaDocumentos[i].bsiva - listaDocumentos[i].cbsretencioniva
                                listaReciboPrLineas[i].bsmtoiva = listaDocumentos[i].bsiva
                                listaReciboPrLineas[i].doliva   = if (binding.cbExcReten.isChecked) listaDocumentos[i].dtotimpuest else listaDocumentos[i].dtotimpuest - listaDocumentos[i].cdretencioniva
                                //2023-04-03 comentado por mal calculo?
                                //listaReciboPrLineas[i].tnetoddol  += listaReciboPrLineas[i].doliva
                                listaReciboPrLineas[i].tnetoddol  += if (binding.cbExcReten.isChecked) listaDocumentos[i].dtotimpuest else listaDocumentos[i].dtotimpuest - listaDocumentos[i].cdretencioniva

                            }
                            else{
                                montoRec -= restaIvabss
                                if(montoRec < 0.00){
                                    Toast.makeText(this, "Monto insuficiente", Toast.LENGTH_SHORT).show()
                                    return
                                }
                                listaReciboPrLineas[i].bscobro  += if (binding.cbExcReten.isChecked) listaDocumentos[i].bsiva else listaDocumentos[i].bsiva - listaDocumentos[i].cbsretencioniva
                                listaReciboPrLineas[i].bsmtoiva = listaDocumentos[i].bsiva
                                listaReciboPrLineas[i].doliva   = if (binding.cbExcReten.isChecked) listaDocumentos[i].dtotimpuest else listaDocumentos[i].dtotimpuest - listaDocumentos[i].cdretencioniva
                                listaReciboPrLineas[i].tnetodbs += if (binding.cbExcReten.isChecked) listaDocumentos[i].bsiva else listaDocumentos[i].bsiva - listaDocumentos[i].cbsretencioniva
                                //listaReciboPrLineas[i].tnetodbs += listaDocumentos[i].bsiva

                            }
                        }

                        //descuento del flete de los documentos
                        if(listaDocumentos[i].bsflete - listaDocumentos[i].bsmtofte <= 1){
                            listaReciboPrLineas[i].bsmtofte = 0.00
                            listaReciboPrLineas[i].dolflete = 0.00

                        }
                        else{



                            if(binding.rbCxcDivisasMain.isChecked){

                                //si aqui ya llega el monto en 0 o menos
                                if(montoRec <= 0.00){
                                    Toast.makeText(this, "Monto insuficiente", Toast.LENGTH_SHORT).show()
                                    return
                                }
                                //descuento del monto del recibo, el flete
                                montoRec -= valorReal((if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dFlete - listaDocumentos[i].cdretflete))

                                //de no llegar a menos de cero, lo agrego al monto cobrado en bss
                                //si el boton esta presionado, entonces agrego el flete completo, de no estarlo, agrego el flete menos el monto de la retención.
                                listaReciboPrLineas[i].bscobro  += if (binding.cbExcReten.isChecked) listaDocumentos[i].bsflete else listaDocumentos[i].bsflete - listaDocumentos[i].cbsretflete
                                /*if(binding.cbExcReten.isChecked){
                                    listaReciboPrLineas[i].bscobro  += listaDocumentos[i].bsflete
                                }
                                else{
                                    listaReciboPrLineas[i].bscobro  += listaDocumentos[i].bsflete - listaDocumentos[i].cbsretflete
                                }*/

                                listaReciboPrLineas[i].bsmtofte = listaDocumentos[i].bsflete
                                listaReciboPrLineas[i].dolflete = if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dFlete - listaDocumentos[i].cdretflete
                                listaReciboPrLineas[i].tnetoddol += if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dFlete - listaDocumentos[i].cdretflete

                            }
                            else{

                                //si aqui ya llega el monto en 0 o menos
                                if(montoRec <= 0.00){
                                    Toast.makeText(this, "Monto insuficiente", Toast.LENGTH_SHORT).show()
                                    return
                                }
                                //descuento del monto del recibo, el flete
                                montoRec -= valorReal(if (binding.cbExcReten.isChecked) listaDocumentos[i].bsflete else listaDocumentos[i].bsflete - listaDocumentos[i].cbsretflete)
                                if(montoRec < 0.00){
                                    Toast.makeText(this, "Monto insuficiente", Toast.LENGTH_SHORT).show()
                                    return
                                }
                                //de no llegar a menos de cero, lo agrego al monto cobrado en bss

                                listaReciboPrLineas[i].bscobro  += if (binding.cbExcReten.isChecked) listaDocumentos[i].bsiva else listaDocumentos[i].bsiva - listaDocumentos[i].cbsretencioniva
                                listaReciboPrLineas[i].bsmtofte = listaDocumentos[i].bsflete
                                listaReciboPrLineas[i].dolflete = if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dFlete - listaDocumentos[i].cdretflete
                                listaReciboPrLineas[i].tnetodbs  += if (binding.cbExcReten.isChecked) listaDocumentos[i].bsflete else listaDocumentos[i].bsflete - listaDocumentos[i].cbsretflete

                            }

                        }

                    }
                    else{
                        if(listaDocumentos[i].bsflete - listaDocumentos[i].bsmtofte <= 1){
                            listaReciboPrLineas[i].bsmtofte = 0.00
                            listaReciboPrLineas[i].dolflete = 0.00
                        }
                        else{



                            var fleteaCobrar = valorReal(if (binding.cbExcReten.isChecked) listaDocumentos[i].bsflete else listaDocumentos[i].bsflete - listaDocumentos[i].cbsretflete)
                            listaReciboPrLineas[i].bscobro  += fleteaCobrar

                            if(binding.rbCxcDivisasMain.isChecked){
                                montoRec -= valorReal(if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dFlete - listaDocumentos[i].cdretflete)
                                listaReciboPrLineas[i].bsmtofte  = listaDocumentos[i].bsflete
                                listaReciboPrLineas[i].dolflete  = if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dFlete - listaDocumentos[i].cdretflete

                                //2023-03-28 Se cambio ya que se deben de guardar los dolares, ya que se selecciono divisa, y lo estaba guardando en bolivares
                                //listaReciboPrLineas[i].tnetodbs  += listaDocumentos[i].bsflete
                                listaReciboPrLineas[i].tnetoddol += if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dFlete - listaDocumentos[i].cdretflete
                            }
                            else{
                                montoRec -= valorReal(if (binding.cbExcReten.isChecked) listaDocumentos[i].bsflete else listaDocumentos[i].bsflete - listaDocumentos[i].cbsretflete)
                                listaReciboPrLineas[i].bsmtofte  = listaDocumentos[i].bsflete
                                listaReciboPrLineas[i].dolflete  = if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dFlete - listaDocumentos[i].cdretflete
                                listaReciboPrLineas[i].tnetodbs  += if (binding.cbExcReten.isChecked) listaDocumentos[i].bsflete else listaDocumentos[i].bsflete - listaDocumentos[i].cbsretflete
                                //print("a")
                            }
                        }
                    }
                }
                montoRec = valorReal(montoRec)
                //llenado de los netos
                for (i in listaReciboPrLineas.indices){
                    if(montoRec < 1){
                        // borré el return
                    }
                    else{
                        if(binding.rbCxcDivisasMain.isChecked){
                            var netoTV = binding.tvCxcNeto.text.toString().toDouble()

                            if(netoTV > 0.00){
                                var netoRealenDoc = listaDocumentos[i].netorestante

                                if( montoRec >= (netoRealenDoc - binding.tvCxcDctos.text.toString().toDouble())){
                                    var bscobrado = netoRealenDoc * listaDocumentos[i].tasadoc

                                    listaReciboPrLineas[i].bscobro += valorReal(bscobrado)


                                    // 2023-06-01 se comento para poder aplicar el descuento a tnetoddol
                                    //netocobrado =  listaDocumentos[i].netorestante
                                    netocobrado = if (binding.tvCxcDctos.text.toString().toDouble() > 0.0) valorReal(listaDocumentos[i].netorestante - (listaDocumentos[i].netorestante * 0.04)) else listaDocumentos[i].netorestante
                                    listaReciboPrLineas[i].dolneto = valorReal(netocobrado)
                                    listaReciboPrLineas[i].tnetoddol += netocobrado
                                    //2023-05-11 Sele agrego que al netocobrado (es decir lo que el cliente dio luego de ser cobrado iva, flete y el neto) se le reste ademas el descuento
                                    //para que si aun queda un poco de dinero sobrante se guarde futuramente como saldo a favor
                                    //montoRec -= netocobrado
                                    montoRec -= (netocobrado - binding.tvCxcDctos.text.toString().toDouble())
                                }

                                else if((netoRealenDoc - binding.tvCxcDctos.text.toString().toDouble()) > montoRec && montoRec > 0) {

                                    var cobroAbono = montoRec * listaDocumentos[i].tasadoc

                                    listaReciboPrLineas[i].bscobro += cobroAbono
                                    var netocobrado = montoRec
                                    listaReciboPrLineas[i].dolneto = valorReal(netocobrado)
                                    listaReciboPrLineas[i].tnetoddol += netocobrado
                                    montoRec -= netocobrado
                                    montoRec= valorReal(montoRec)

                                }
                            }
                        }

                        if(binding.rbCxcBssMain.isChecked){
                            var netoRealenDoc = listaDocumentos[i].netorestante
                            netoRealenDoc = valorReal(netoRealenDoc)

                            //si el monto del recibo cubre el monto del documento
                            if( montoRec >= (netoRealenDoc * tasaCambioSeleccionadaPrincipal)){
                                var bscobrado = valorReal(netoRealenDoc * listaDocumentos[i].tasadoc)
                                listaReciboPrLineas[i].bscobro += bscobrado
                                netocobrado = listaDocumentos[i].netorestante
                                listaReciboPrLineas[i].dolneto = netocobrado
                                montoRec -= valorReal(netoRealenDoc * listaDocumentos[i].tasadoc)
                                listaReciboPrLineas[i].bsneto = valorReal(netoRealenDoc * tasaCambioSeleccionadaPrincipal)
                                listaReciboPrLineas[i].tnetodbs  += valorReal(netoRealenDoc * tasaCambioSeleccionadaPrincipal)

                            }
                            //si el monto del recibo no cubre el monto completo del documento (se comporta como un abono)
                            else if((netoRealenDoc * tasaCambioSeleccionadaPrincipal) > montoRec && montoRec > 0) {
                                netocobrado = montoRec / tasaCambioSeleccionadaPrincipal
                                var cobroAbono = netocobrado * listaDocumentos[i].tasadoc
                                listaReciboPrLineas[i].bscobro += valorReal(cobroAbono)
                                var dolneto = netocobrado
                                listaReciboPrLineas[i].dolneto    = valorReal(dolneto)
                                listaReciboPrLineas[i].bsneto = montoRec
                                listaReciboPrLineas[i].tnetodbs  += montoRec
                                montoRec -= montoRec

                            }
                        }

                        //los bss del neto cobrado
                        /*recorrido de lista de descuentos para asignar cantidad según nro doc. (de tener)*/
                        for(j in listaDescuentos.indices){
                            if(listaDescuentos[j].nrodoc == listaDocumentos[i].documento){
                                listaReciboPrLineas[i].prcdsctopp = listaDescuentos[j].pordscto
                            }
                        }
                    }
                }

                /*esto va a ser resultado de la suma de los campos de la lista (ke_precobranza)
                 de detalles */

                var difReteIva   = valorReal(listaReciboPrLineas.sumOf { it.bsmtoiva }) + valorReal(listaReciboPrLineas.sumOf { it.bsretiva })
                var difRetyFlete = valorReal(listaReciboPrLineas.sumOf { it.bsmtofte }) + valorReal(listaReciboPrLineas.sumOf { it.bsretfte })
                var netoReal   = valorReal(listaReciboPrLineas.sumOf { it.bscobro }) - difReteIva - difRetyFlete //--revisar si esto es necesario
                cxc.bsneto     = valorReal(listaReciboPrLineas.sumOf { it.bsneto })
                cxc.bsretiva   = valorReal(listaReciboPrLineas.sumOf { it.bsretiva })
                cxc.bsiva      = valorReal(listaReciboPrLineas.sumOf { it.bsmtoiva })
                cxc.bsflete    = valorReal(listaReciboPrLineas.sumOf { it.bsmtofte })

                /* sumo los bss en total y los redondeo al momento de guardarlo */
                var bssumaTotal  = valorReal(listaReciboPrLineas.sumOf { it.bscobro })

                //cxc.bstotal    = valorReal(bssumaTotal)
                cxc.bstotal    = valorReal(listaReciboPrLineas.sumOf { it.bsneto } + (listaReciboPrLineas.sumOf { it.bsmtoiva } + listaReciboPrLineas.sumOf { it.bsretiva }) + (listaReciboPrLineas.sumOf { it.bsmtofte } + listaReciboPrLineas.sumOf { it.bsretfte }))
                //cxc.dolneto    = valorReal(listaDocumentos.sumOf{it.dtotneto }) //<-------------------- Revisar si es necesario
                cxc.dolneto    = valorReal(listaReciboPrLineas.sumOf{it.dolneto })
                cxc.doliva     = valorReal(listaReciboPrLineas.sumOf { it.doliva })
                //cxc.dolretiva  = valorReal(listaDocumentos.sumOf { it.cdretencioniva })
                cxc.dolflete   = valorReal(listaReciboPrLineas.sumOf{it.dolflete})
                cxc.doltotal = valorReal(listaReciboPrLineas.sumOf{it.dolneto} + listaReciboPrLineas.sumOf {it.doliva} + listaReciboPrLineas.sumOf{it.dolflete})
                cxc.netocob    = if(monedaSeleccionadaPr == "2") listaReciboPrLineas.sumOf{it.dolneto} else listaReciboPrLineas.sumOf{it.bsneto}
                //2023-07-14 se comento devido a que es un calculo errado, debido a que se trata flete con la tasa del dia y no con la tasa del documento
                /*var doltotal   = binding.etCxcMontoMain.text.toString().toDouble()
                if(binding.rbCxcDivisasMain.isChecked){
                    cxc.doltotal   = valorReal(doltotal)
                    cxc.netocob    = valorReal(doltotal - listaReciboPrLineas.sumOf{it.dolflete} - listaReciboPrLineas.sumOf{it.doliva})

                }
                else{
                    doltotal /= tasaCambioSeleccionadaPrincipal
                    cxc.doltotal   = valorReal(doltotal)
                    cxc.netocob    = valorReal(doltotal - listaReciboPrLineas.sumOf{it.dolflete} - listaReciboPrLineas.sumOf{it.doliva})
                }*/
                cxc.bsretflete = valorReal(listaReciboPrLineas.sumOf { it.bsretfte })
                cxc.retmun_sbi = 0.00//definir
                cxc.retmun_sbs = 0.00//definir
                //var fechaVigen = fechaSuma(fechaActual, 3)
                //println("ojo")
                //cxc.fchvigen   = fechaVigen
                cxc.moneda     = monedaSeleccionadaPr
                cxc.tasadia    = tasaCambioSeleccionadaPrincipal
                listaReciboPrCabecera.add(cxc)

                try{

                    // inicio la transacción
                    ke_android.beginTransaction()
                    var qcabecera:ContentValues = ContentValues()
                    var qlineas:  ContentValues = ContentValues()

                    for (i in listaReciboPrCabecera.indices){
                        qcabecera.put("cxcndoc", listaReciboPrCabecera[i].id_recibo)
                        qcabecera.put("tiporecibo", listaReciboPrCabecera[i].tipoRecibo)
                        qcabecera.put("codvend", listaReciboPrCabecera[i].codigoVend)
                        qcabecera.put("tiporecibo", listaReciboPrCabecera[i].tipoRecibo)
                        qcabecera.put("kecxc_id", listaReciboPrCabecera[i].kecxc_id)
                        qcabecera.put("tasadia", listaReciboPrCabecera[i].tasadia)
                        qcabecera.put("fchrecibo", listaReciboPrCabecera[i].fchrecibo)
                        qcabecera.put("bsneto", listaReciboPrCabecera[i].bsneto)
                        qcabecera.put("bsiva", listaReciboPrCabecera[i].bsiva)
                        qcabecera.put("bsretiva", listaReciboPrCabecera[i].bsretiva)
                        qcabecera.put("bsflete", listaReciboPrCabecera[i].bsflete)
                        qcabecera.put("bstotal", listaReciboPrCabecera[i].bstotal)
                        qcabecera.put("dolneto", listaReciboPrCabecera[i].dolneto)
                        qcabecera.put("doliva", listaReciboPrCabecera[i].doliva)
                        // qcabecera.put("dolretiva", listaReciboPrCabecera[i].dolretiva)
                        qcabecera.put("dolflete", listaReciboPrCabecera[i].dolflete)
                        qcabecera.put("doltotal", listaReciboPrCabecera[i].doltotal)
                        qcabecera.put("moneda", listaReciboPrCabecera[i].moneda)
                        if(ignore == true){
                            qcabecera.put("efectivo", listaReciboPrCabecera[i].efectivo)
                        }
                        else{
                            qcabecera.put("bcocod", listaReciboPrCabecera[i].bcocod)
                            qcabecera.put("bcomonto", listaReciboPrCabecera[i].bcomonto)
                            qcabecera.put("bcoref", listaReciboPrCabecera[i].bcoref)
                        }
                        qcabecera.put("edorec", listaReciboPrCabecera[i].edorec)
                        qcabecera.put("fchvigen", listaReciboPrCabecera[i].fchvigen)
                        qcabecera.put("bsretflete", listaReciboPrCabecera[i].bsretflete)
                        qcabecera.put("netocob", listaReciboPrCabecera[i].netocob)
                        qcabecera.put("fechamodifi", getFechaHoy())
                        //qcabecera.put("docdifcamb",diferencialCambiario)


                        for(j in listaReciboPrLineas.indices){
                            qlineas.put("cxcndoc", listaReciboPrLineas[j].id_recibo)
                            qlineas.put("agencia",   listaReciboPrLineas[j].agencia)
                            qlineas.put("tipodoc",   listaReciboPrLineas[j].tipodoc)
                            qlineas.put("documento", listaReciboPrLineas[j].documento)
                            qlineas.put("bscobro", valorReal(listaReciboPrLineas[j].bscobro))
                            qlineas.put("prcdsctopp", listaReciboPrLineas[j].prcdsctopp)
                            qlineas.put("nroret", listaReciboPrLineas[j].nroret)
                            qlineas.put("fchemiret", listaReciboPrLineas[j].fchemiret)
                            qlineas.put("bsretiva", listaReciboPrLineas[j].bsretiva)
                            qlineas.put("refret", listaReciboPrLineas[j].refret)
                            qlineas.put("nroretfte", listaReciboPrLineas[j].nroretfte)
                            qlineas.put("fchemirfte", listaReciboPrLineas[j].fchemirfte)
                            qlineas.put("bsmtofte", listaReciboPrLineas[j].bsmtofte)
                            qlineas.put("bsretfte", listaReciboPrLineas[j].bsretfte)
                            qlineas.put("refretfte", listaReciboPrLineas[j].refretfte)
                            qlineas.put("bsmtoiva", listaReciboPrLineas[j].bsmtoiva)
                            qlineas.put("tnetoddol", valorReal(listaReciboPrLineas[j].tnetoddol))
                            qlineas.put("tnetodbs", valorReal(listaReciboPrLineas[j].tnetodbs))
                            qlineas.put("fchrecibod", getFechaNow())
                            qlineas.put("kecxc_idd", listaReciboPrCabecera[i].kecxc_id)
                            qlineas.put("tasadiad", listaReciboPrCabecera[i].tasadia)
                            qlineas.put("reten", retennn)
                            ke_android.insert("ke_precobradocs", null, qlineas)
                        }
                    }

                    //val favor = guardaSaldoFavor(montoRec, binding.rbCxcDivisasMain, binding.rbCxcCompMain, binding.etCxcMontoMain.text.toString().toDouble(), binding.tvCxcTotal)
                    //ke_android.execSQL("UPDATE ke_precobradocs SET afavor= '$favor' WHERE cxcndoc='${listaReciboPrLineas[listaReciboPrLineas.size-1].id_recibo}' AND documento = '${listaReciboPrLineas[listaReciboPrLineas.size-1].documento}';")
                    guardaSaldoFavor2(binding.rbCxcDivisasMain, binding.rbCxcCompMain, binding.etCxcMontoMain.text.toString().toDouble(), 0.00,  binding.tvCxcTotal, listaReciboPrLineas[listaReciboPrLineas.size-1].id_recibo, listaReciboPrLineas[listaReciboPrLineas.size-1].documento, true)
                    ke_android.insert("ke_precobranza", null, qcabecera)

                    var qcorrelativo: ContentValues = ContentValues()
                    qcorrelativo.put("kcor_numero", nroCorrelativo)
                    qcorrelativo.put("kcor_vendedor", cod_usuario)

                    ke_android.insert("ke_corprec", null, qcorrelativo)
                    ll_commit = true

                }
                catch (exception: SQLException){

                    exception.printStackTrace()
                    ll_commit = false

                    ke_android.endTransaction()
                    if(!ll_commit){
                        return
                    }
                }

                if(ll_commit){
                    ke_android.setTransactionSuccessful()
                    ke_android.endTransaction()

                    var listadatos: ArrayList<CXC> = ArrayList()
                    listadatos.add(cxc)
                    listadatos[0].cliente = codigoCliente

                    var dialog: DialogRecibo = DialogRecibo()
                    dialog.DialogRecibo(this, listadatos)

                    Toast.makeText(this, "RECIBO CREADO", Toast.LENGTH_SHORT).show()
                    // finish()
                }
                else{
                    ke_android.endTransaction()
                }
            }
        }
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

    private fun guardaSaldoFavor(
        montoRec: Double,
        btnDolar: RadioButton,
        btnCompleto: RadioButton,
        Ingreso: Double,
        Total: TextView,
    ): Double {
        return if (btnDolar.isChecked && btnCompleto.isChecked && (Ingreso > Total.text.toString().toDouble())){
            valorReal(montoRec)
        }else{
            0.00
        }
    }

    /*
        2023-05-16 ASDA
    * Funcion para calcular el saldo a favor siempre y cuando se page en divisas como pago principal
    * en el caso de haber un complemento, tanto principal como complementario deben de estar en divisas
    * */
    private fun guardaSaldoFavor2(
        btnDolar: RadioButton,      //Boton de la moneda divisa en pago principal
        btnCompleto: RadioButton,   // Boton de la moneda divisa en pago principal
        Ingreso: Double,            // Monto colocado por el cliente en el Pago Principal
        IngresoCom: Double,         // Monto colocado por el cliente en el Pago Complementario (si es un pago sin complemento su valor es 0.00)
        Total: TextView,            // Monto total del o los Documentos seleccionados
        cxcndoc: String,            // Numero del correlativo de la ultima precobranza que se va a realizar
        documento: String,          // Numero del ultimo documento que se va a pagar en la transaccion
        btnDolarCom: Boolean,        // Valor booleano que indica si el complemento se paga en divisas (si no se paga complemento por defecto sera true)
    ) {
        // En esta variable se guarda en Divisa el monto colocado en el EditText del pago principal (de haber seleccionado bolivares estos seran convertidos)
        val ingresoPrincipal = if (binding.rbCxcDivisasMain.isChecked) Ingreso else Ingreso / tasaCambioSeleccionadaPrincipal
        // En esta variable se guarda en Divisa el monto colocado en el EditText del pago complementario (de haber seleccionado bolivares estos seran convertidos)
        val ingresoComplemento = if (!binding.cbCxcComplemento.isChecked) 0.00 else (if (binding.rbCxcDivisasCom.isChecked) IngresoCom else IngresoCom / tasaCambioSeleccionadaPrincipal )
        // En esta variable se guarda el Total del o los documentos a pagar en Divisa (de haber seleccionado bolivares estos seran convertidos)
        val totalReal = if (binding.rbCxcDivisasMain.isChecked) Total.text.toString().toDouble() else Total.text.toString().toDouble() / tasaCambioSeleccionadaPrincipal
        /*
        * If que valida
        * que como pago principal se selecciono Divisa
        * que sea un pago Completo
        * que en caso de haber un complemento este sea en divisas tambien (Divisa como principal y como complemento)
        * que el pago principal y complmentario, juntos superen el total de la suma del o los documentos a pagar
        * */
        if (btnDolar.isChecked && btnCompleto.isChecked && btnDolarCom && ((ingresoPrincipal + ingresoComplemento) > totalReal)){
            /*
            * La sentencia SQL
            * guarda el monto excesido del pago a travez de la supa del pago principal y complementario, menos el total del o los documentos a pagar
            *
            * Lo guarga en el ultimo documento a pagar
            * selecciona el correlativo del pago principal (en caso de no haber complemento), o el correlativo del complemento (en caso de haber un complemento)
            * selecciona siempre el ultimo documento de la lista de documentos seleccionados a pagar
            * */
            ke_android.execSQL("UPDATE ke_precobradocs SET afavor= '${valorReal((ingresoPrincipal + ingresoComplemento) - totalReal)}' WHERE cxcndoc='$cxcndoc' AND documento = '$documento';")
        }
    }

    private fun existReten(listaDocumentos: ArrayList<Documentos>): Boolean {
        //println("Documentos --> ${listaDocsSeleccionados}")

        //println("Correlativo --> $CorrelativoTexto")
        //println("Retenciones --> ${listaRetGuardada[0].tiporet} ${listaRetGuardada[0].fecharet} ${listaRetGuardada[0].nrodoc} ${listaRetGuardada[0].montoret} ${listaRetGuardada[0].nroret} ${listaRetGuardada[0].refret}")
        //println("Retenciones --> ${listaRetGuardada[1].tiporet}")
        //println("Retenciones --> ${listaRetGuardada[2].tiporet}")

        val documentosRet = listaDocsSeleccionados
        val rentenciones  = listaRetGuardada
        rentenciones.sortBy { it.nrodoc }
        documentosRet.sort()
        //println("Ordenados --> $documentosRet")
        //println("Ordenados2 --> ${rentenciones[0].nrodoc}, ${rentenciones[1].nrodoc}, ${rentenciones[2].nrodoc}")

        var retIva = 0
        var retFlete = 0

        for (retencion in rentenciones){
            if (retencion.tiporet == "iva"){
                retIva++
            }

            if (retencion.tiporet == "flete"){
                retFlete++
            }
        }

        if (((retIva != 0) && (retIva != listaDocsSeleccionados.size)) || ((retFlete != 0) && (retFlete != listaDocsSeleccionados.size))){
            println("Retencion --> $retIva")
            println("Flete --> $retFlete")
            println("Validacion errada")
            Toast.makeText(this, "Falto una retencion", Toast.LENGTH_SHORT).show()
            return false
        }else{
            println("Retencion --> $retIva")
            println("Flete --> $retFlete")
            println("Validacion exitosa")
            return true
        }
    }

    private fun fechaSuma(fechaOld:String, cantDias:Long):String{
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

    //2023-04-03 PROBAR posible sustitucion de la funcion fechaSuma(fechaOld:String, cantDias:Long)-----------------------------------------------------
    /*private fun fechaSum(fechaOld: String, cantDias: Long): String {
        val fechaNew = LocalDate.parse(fechaOld, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).plusDays(cantDias)
        // de fecha a String (la nueva)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        fechaNew.format(formatter)

        return fechaNew.toString()
    }*/

    private fun valorReal(monto:Double):Double{
        return Math.round(monto*100.00)/100.00
    }

    private fun bajarDocsConDesc(URL: String){
        var agencia      = ""
        var tipodoc      = ""
        var documento    = ""
        var codcliente   = ""
        var edodcto      = ""
        var prcdctoaplic = 0.00
        var montodctodol = 0.00
        var tasadoc      = 0.00
        var fechamodifi  = ""

        var ll_commit_dc = false
        val jsonArrayRequest = JsonArrayRequest(Request.Method.GET,
            URL,
            null,
            {
                    response ->
                if(response != null){
                    ll_commit_dc = false
                    ke_android.beginTransaction()

                    var jsonObject:JSONObject? = null
                    try{
                        for(i in 0 until response.length()){
                            jsonObject      = response.getJSONObject(i)
                            agencia         = jsonObject.getString("agencia").trim()
                            tipodoc         = jsonObject.getString("tipodoc").trim()
                            documento       = jsonObject.getString("documento").trim()
                            codcliente      = jsonObject.getString("codcliente").trim()
                            edodcto         = jsonObject.getString("edodcto").trim()
                            prcdctoaplic    = jsonObject.getDouble("prcdctoaplic")
                            montodctodol    = jsonObject.getDouble("montodctodol")
                            tasadoc         = jsonObject.getDouble("tasadoc")
                            fechamodifi     = jsonObject.getString("fechamodifi").trim()

                            val qDescuentos: ContentValues = ContentValues()
                            qDescuentos.put("agencia",      agencia)
                            qDescuentos.put("tipodoc",      tipodoc)
                            qDescuentos.put("documento",    documento)
                            qDescuentos.put("codcliente",   codcliente)
                            qDescuentos.put("edodcto",      edodcto)
                            qDescuentos.put("prcdctoaplic", prcdctoaplic)
                            qDescuentos.put("montodctodol", montodctodol)
                            qDescuentos.put("tasadoc",      tasadoc)
                            qDescuentos.put("fechamodifi",  fechamodifi)

                            val qcodigoLocal:Cursor = ke_android.rawQuery("SELECT count(documento) FROM ke_precobdcto WHERE documento ='${documento}'", null)
                            qcodigoLocal.moveToFirst()

                            val codigoExistente = qcodigoLocal.getInt(0)
                            qcodigoLocal.close()

                            if(codigoExistente > 0){
                                ke_android.update("ke_precobdcto", qDescuentos, "documento = ?", arrayOf(documento))
                            }
                            else if(codigoExistente == 0){
                                ke_android.insert("ke_precobdcto", null, qDescuentos)
                            }

                        }
                        ll_commit_dc = true

                    }
                    catch(ex: Exception){
                        ex.printStackTrace()
                        ll_commit_dc = false
                        if (!ll_commit_dc) return@JsonArrayRequest
                    }

                }
                if(ll_commit_dc){
                    ke_android.setTransactionSuccessful()
                    ke_android.endTransaction()

                }
                else if(!ll_commit_dc){
                    ke_android.endTransaction()
                }

            },
            {error ->

            }
        )
        val requestQueue: RequestQueue = Volley.newRequestQueue(this)
        requestQueue.add(jsonArrayRequest)
    }

    private fun calcularDescuentos(moneda:String){


        if(binding.cbCxcDescuentos.isChecked){
            binding.btVerDetDescuento.visibility = View.VISIBLE
            var porcentajeAsignado = 0.00
            cantidadDeDescuento = 0.00
            listaDescuentos = ArrayList()
            var descuentos:Descuentos

            /*variable que devolvera el descuento total que
            se pueden aplicar a todos los docs*/
            descuentoTotal     = 0.00
            var fechaVence     = ""
            var nrodocumento   = ""
            var montonetoDol   = 0.00
            var montonetoBs    = 0.00
            var tieneDescPrev  = false
            /*Por cada documento, voy a buscar la fecha de vencimiento
            y determinar cada descuento*/
            for (i in listaDocumentos.indices){

                tieneDescPrev   = verificarSiHayDescuentos(listaDocumentos[i].documento)
                if(tieneDescPrev == true){
                    //significa que ya tiene un descuento aplicado.
                }else{
                    descuentos      = Descuentos()
                    nrodocumento    = listaDocumentos[i].documento
                    fechaVence      = listaDocumentos[i].vence
                    montonetoDol    = listaDocumentos[i].netorestante
                    montonetoBs     = montonetoDol * tasaCambioSeleccionadaPrincipal

                    val fechaConvertidaVence = LocalDate.parse(fechaVence)
                    val fechaHoy             = LocalDate.now()
                    val diasDiferencia       = ChronoUnit.DAYS.between(fechaHoy, fechaConvertidaVence)


                    //si la diferencia de dias es positiva, puede aplicar al descuento
                    if(diasDiferencia > 0){
                        //si la moneda es $$, el descuento es del 10%
                        if(moneda.equals("USD")){

                            val fechaEmisionS = listaDocumentos[i].emision
                            val fechaLimiteS  = "2022-11-01"
                            val sdf = SimpleDateFormat("yyyy-MM-dd")

                            val fechaEmic:   Date   = sdf.parse(fechaEmisionS)
                            val fechaLimite: Date   = sdf.parse(fechaLimiteS)

                            val comparativa = fechaEmic.compareTo(fechaLimite)

                            if(comparativa >= 0){
                                //si la emision es mayor o igual al 1er de noviembre (no hay descuentos)
                                if((listBankDesc.contains(codigoBancoCompleto)) && binding.cbCxcComplemento.isChecked == false){
                                    // ------- OJO AQUI DESCUENTO ---------
                                    porcentajeAsignado  = 0.04
                                    cantidadDeDescuento = 4.0
                                }

                                var cantDescuento    = montonetoDol * porcentajeAsignado
                                //var descuentoUni     = montonetoDol - cantDescuento
                                descuentoTotal      += cantDescuento
                                descuentoTotal       = Math.round(descuentoTotal*100.00)/100.00

                                descuentos.nrodoc    = nrodocumento
                                descuentos.cantdscto = cantDescuento
                                descuentos.pordscto  = cantidadDeDescuento
                                listaDescuentos.add(descuentos)


                            }else if(comparativa < 0){
                                // si la emisión es menor  al 1ero de noviembre (Si hay descuentos)
                                porcentajeAsignado = 0.0
                                cantidadDeDescuento = 0.0
                                //Si los pagos son por zelle, el descuento pasa a ser de 12,7%
                                if((listBankDesc.contains(codigoBancoCompleto)) && binding.cbCxcComplemento.isChecked == false){
                                    porcentajeAsignado  = 0.04
                                    cantidadDeDescuento = 4.0
                                }


                                var cantDescuento    = montonetoDol * porcentajeAsignado
                                //var descuentoUni     = montonetoDol - cantDescuento
                                descuentoTotal      += cantDescuento
                                descuentoTotal       = Math.round(descuentoTotal*100.00)/100.00

                                descuentos.nrodoc    = nrodocumento
                                descuentos.cantdscto = cantDescuento
                                descuentos.pordscto  = cantidadDeDescuento
                                listaDescuentos.add(descuentos)

                            }

                        } else if (moneda.equals("BSS")){

                        }

                    }else if(diasDiferencia <= 0){

                        var cantDescuento    = 0.00
                        descuentoTotal      += 0.00

                        if((listBankDesc.contains(codigoBancoCompleto)) && binding.cbCxcComplemento.isChecked == false){
                            porcentajeAsignado   = 0.04
                            cantidadDeDescuento  = 4.0
                            var cantDescuento    = montonetoDol * porcentajeAsignado
                            //var descuentoUni     = montonetoDol - cantDescuento
                            descuentoTotal      += cantDescuento
                            descuentoTotal       = Math.round(descuentoTotal*100.00)/100.00

                            descuentos.nrodoc    = nrodocumento
                            descuentos.cantdscto = cantDescuento
                            descuentos.pordscto  = cantidadDeDescuento
                            listaDescuentos.add(descuentos)

                        }else{
                            //
                        }
                    }
                }

            }

            if(descuentoTotal > 0.00){
                binding.cbCxcDescuentos.visibility = View.VISIBLE
                binding.cbCxcDescuentos.isEnabled = true
                binding.tvCxcDctos.text = descuentoTotal.toString()
                binding.btVerDetDescuento.visibility = View.VISIBLE

            }else if(descuentoTotal <= 0.00){
                binding.cbCxcDescuentos.visibility = View.INVISIBLE
                binding.cbCxcDescuentos.isEnabled = false
                binding.tvCxcDctos.text = "0.00"
                binding.btVerDetDescuento.visibility = View.INVISIBLE
            }

        }else{
            binding.btVerDetDescuento.visibility = View.INVISIBLE
        }

    }

    private fun validarRetenciones(){

        var cantretsiva   = 0
        var cantretsfte   = 0
        var cantretsparme = 0
        var cdretflete    = 0.00
        var cursor:Cursor = ke_android.rawQuery("SELECT contribespecial FROM cliempre WHERE codigo= '$codigoCliente'", null)
        var esConEspecial = "0"

        if(cursor.moveToFirst()){
            esConEspecial = cursor.getString(0)

        }



        if(esConEspecial.equals("1")){


            for(i in listaDocumentos.indices){
                //estos son los campos que muestran el monto que se tienen que pagar
                cdretencion     = listaDocumentos[i].cdretencion
                cdretencioniva  = listaDocumentos[i].cdretencioniva
                cdretparme      = listaDocumentos[i].cdretparme
                cdretflete      = listaDocumentos[i].cdretflete
                //estos son los cmapos que contienen pagos (de estar pagados)
                dretencion      = listaDocumentos[i].dretencion
                dretencioniva   = listaDocumentos[i].dretencioniva

                //resto para determinar que montos se encuentran pagados
                var montoretPagado      = listaDocumentos[i].bsretencion
                var isRetpagado:Double  = listaDocumentos[i].cbsretencion.minus(listaDocumentos[i].bsretencion)
                var isretivaPagado      = listaDocumentos[i].cbsretencioniva.minus(listaDocumentos[i].bsretencioniva)


                // valido que ya estas ret hayan sido pagadas
                //var hayretParme:Double  =   montoretPagado - cbsretencioniva - bsmontoRetFlete
                //var hayretFlete:Double  =   montoretPagado - cbsretencioniva - cbsretparme

                var hayretParme:Double  =   listaDocumentos[i].cbsretparme
                var hayretFlete:Double  =   listaDocumentos[i].cbsretflete



                if(isRetpagado <= 0.00){
                    //si los montos son cero, no paga retenciones
                    pagaRetenciones = false

                }else{

                    if(isretivaPagado > 0.00){
                        //contabilizo si tiene ret de este tipo
                        cantretsiva +=1
                        pagaRetenciones = true
                    }

                    if(hayretFlete > 0.00){
                        //contabilizo si tiene ret de este tipo
                        cantretsfte +=1
                        pagaRetenciones = true
                    }

                    if(hayretParme > 0.00){
                        //contabilizo si tiene ret de este tipo
                        cantretsparme +=1
                        pagaRetenciones = true
                    }

                }

            } //final del ciclo


            if(cantretsiva > 0){
                listaTiposRet.add("iva")
            }
            if(cantretsfte > 0){
                listaTiposRet.add("flete")
            }
            if(cantretsparme > 0){
                listaTiposRet.add("parme")
            }

            if(listaTiposRet.size > 0){
                binding.btCxcRetenciones.visibility = View.VISIBLE
                binding.btCxcRetenciones.isEnabled = true

            }else{
                // bt_retenciones.visibility = View.INVISIBLE
                //bt_retenciones.isEnabled = false
            }

        }
        /*else{
            println("AAAAAAAAAAAAAAAAAAAAAAAAAA")
            for(i in listaDocumentos.indices){
                println("BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB")
                //estos son los campos que muestran el monto que se tienen que pagar
                cdretencion     = listaDocumentos[i].cdretencion
                cdretencioniva  = listaDocumentos[i].cdretencioniva
                cdretparme      = listaDocumentos[i].cdretparme
                cdretflete      = listaDocumentos[i].cdretflete
                //estos son los cmapos que contienen pagos (de estar pagados)
                dretencion      = listaDocumentos[i].dretencion
                dretencioniva   = listaDocumentos[i].dretencioniva

                //resto para determinar que montos se encuentran pagados
                var isRetpagado      = (Math.round(fleteRestante * 100.00)/100.00)

                println("Retencion de flete $isRetpagado")

                if(isRetpagado <= 0.00){
                    println("CCCCCCCCCCCCCCCCCCCCC")
                    //si los montos son cero, no paga retenciones
                    pagaRetenciones = false

                }else{
                    println("DDDDDDDDDDDDDDDDDDDDDDDD")

                    if(isRetpagado > 0.00){
                        println("EEEEEEEEEEEEEEEEEEEEEEEEEEE")
                        //contabilizo si tiene ret de este tipo
                        cantretsfte +=1
                        pagaRetenciones = true
                    }


                }

            } //final del ciclo


            if(cantretsfte > 0){
                println("IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII")
                listaTiposRet.add("flete")
            }

            if(listaTiposRet.size > 0){
                println("KKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK")
                println("al menos una retencion va a ser pagada")
                binding.btCxcRetenciones.visibility = View.VISIBLE
                binding.btCxcRetenciones.isEnabled = true

            }else{
                println("LLLLLLLLLLLLLLLLLLLLLLLLL")
                // bt_retenciones.visibility = View.INVISIBLE
                //bt_retenciones.isEnabled = false
            }
        }*/

        cursor.close()

    }

    //picker dialog para la fecha
    private fun showDatePickerDialog(){
        val datePicker = DatePickerFragment("cxcReportActivity") { day, month, year -> onDateSelected(day, month, year) }

        datePicker.show(supportFragmentManager, "datePicker")

    }

    //funcion para seleccionar la fecha y guardarla
    fun onDateSelected(day: Int, month:Int, year:Int){
        var fechaMostrar = "$year-$month-$day"


        //en formato para query de tasas
        fechaQuery = ""
        var formatter:SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        var date:Date =  formatter.parse(fechaMostrar)
        var formatNuevo:SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        var formatNuevoVista:SimpleDateFormat = SimpleDateFormat("dd/MM/yyyy")
        fechaQuery = formatNuevo.format(date)

        binding.dpFecharec.setText("Fecha: ${formatNuevoVista.format(date)}")

        var formatoHorafec:SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        fechatasaH = fechaQuery

        buscarTasas()

        if(tasaCambioSeleccionadaPrincipal == 0.00){
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Alerta de validación de datos")
            builder.setMessage("La fecha seleccionada no posee tasa de cambio.")
            builder.apply {
                setPositiveButton("Ok",
                    DialogInterface.OnClickListener { dialog, id ->

                    })
            }
            builder.create()
            fechaQuery = ""
            return
        }

        if(binding.rbCxcDivisasMain.isChecked){
            cargarSaldos("USD", listaDocsSeleccionados, !binding.cbExcReten.isChecked)
        }else{
            cargarSaldos("BSS", listaDocsSeleccionados, !binding.cbExcReten.isChecked)
        }


    }

    private fun generarNroPrecobranza(): String {
        val fechaHoy = Date(Calendar.getInstance().timeInMillis)

        val formatoFecha = SimpleDateFormat("yyMM", Locale.getDefault())

        val fecha = formatoFecha.format(fechaHoy)

        var correlativo = right(CorrelativoTexto, 4)
        correlativo = "$cod_usuario-$tipoDoc-$fecha$correlativo"
        return correlativo
    }

    private fun generarNroComplemento():String {
        val fechaHoy = Date(Calendar.getInstance().timeInMillis)

        val formatoFecha = SimpleDateFormat("yyMM", Locale.getDefault())

        val fecha = formatoFecha.format(fechaHoy)

        var correlativo = right(CorrelativoTextoCom, 4)
        correlativo = "$cod_usuario-PRC-$fecha$correlativo"
        return correlativo
    }

    //funcion para retornar el valor del correlativo acortado a 4 caracteres.
    private fun right(valor:String, longitud:Int):String {
        return valor.substring(valor.length - longitud)
    }

    private fun montoaFavor():Double{
        var montoTotalAFavor = 0.00

        val query = arrayOf("sum(montocli)")
        val tabla = "ke_mtopendcli"
        val condicion = "moneda ='1' AND codcliente='${codigoCliente}' AND estado = '0' AND edoweb ='1'"

        val cursorMto:Cursor = ke_android.query(tabla, query, condicion, null,null,null, null)

        //si no esta vacio, lo recorro
        /*if(cursorMto.count > 0){
            while(cursorMto.moveToNext()){
                montoTotalAFavor = cursorMto.getDouble(0)
            }
        }*/
        if(cursorMto.moveToFirst()){
            montoTotalAFavor = cursorMto.getDouble(0)
        }
        cursorMto.close()
        //retorno el valor
        return montoTotalAFavor
    }


    private fun cargarSaldos(moneda:String, listadocs:ArrayList<String>, pagaret:Boolean = true) {
        //cada vez que se recalcule, se debe volver a colocar en cero los montos
        ivaRestante     = 0.00
        netoRestante    = 0.00
        fleteRestante   = 0.00
        var netoresta   = 0.00
        //esta variable es prod. del query para saber si tiene saldo a favor y DEBE ser siempre menor
        //al monto neto.
        var saldoAFavor = montoaFavor()

        //variables para los bss
        var ivaRestantebss   = 0.00
        var netoRestantebss  = 0.00
        var fleteRestantebss = 0.00
        var netorestabss     = 0.00
        //las retenciones
        var retencionRestante = 0.00
        var retencionRestantebss = 0.00
        var retencionRestantedoc = 0.00

        listaDocumentos = ArrayList()

        //query para cargar los montos del documento
        var documentos:Documentos
        val query     = arrayOf("documento," + "contribesp,"+ "ruta_parme," +"vence,"+ "tipodocv,"+
                "diascred," +"dtotneto,"+ "dtotpagos,"+ "dtotdev,"+ "dtotalfinal,"+ "bsiva,"+ "bsflete,"+ "bsretencioniva,"+ "bsretencion, tasadoc, dtotimpuest, dFlete, dretencion, dretencioniva, tipodoc, " +
                "mtodcto, fchvencedcto, tienedcto, cbsret, cdret, cbsretiva, cdretiva, cbsrparme, cdrparme, agencia, bsmtoiva, bsmtofte, retmun_mto, emision, codcliente, cdretflete, cbsretflete")
        val tabla     = "ke_doccti"
        val condicion = "documento IN ("+listadocs.toString().replace("[", "").replace("]", "") +")"
        val cursorDocs:Cursor = ke_android.query(tabla, query, condicion, null,null,null, null)

        while(cursorDocs.moveToNext()){
            documentos = Documentos()

            documentos.documento        = cursorDocs.getString(0)
            documentos.contribesp       = cursorDocs.getDouble(1)
            documentos.ruta_parme       = cursorDocs.getString(2)
            documentos.vence            = cursorDocs.getString(3)
            documentos.tipodocv         = cursorDocs.getString(4)
            documentos.diascred         = cursorDocs.getDouble(5)
            documentos.dtotneto         = cursorDocs.getDouble(6)
            documentos.dtotpagos        = cursorDocs.getDouble(7)
            documentos.dtotdev          = cursorDocs.getDouble(8)
            documentos.dtotalfinal      = cursorDocs.getDouble(9)
            documentos.bsiva            = cursorDocs.getDouble(10)
            documentos.bsflete          = cursorDocs.getDouble(11)
            documentos.bsretencioniva   = cursorDocs.getDouble(12)
            documentos.bsretencion      = cursorDocs.getDouble(13)
            documentos.tasadoc          = cursorDocs.getDouble(14)
            documentos.dtotimpuest      = cursorDocs.getDouble(15)
            documentos.dFlete           = cursorDocs.getDouble(16)
            documentos.dretencion       = cursorDocs.getDouble(17)
            documentos.dretencioniva    = cursorDocs.getDouble(18)
            documentos.tipodoc          = cursorDocs.getString(19)
            documentos.mtodcto          = cursorDocs.getDouble(20)
            documentos.fchvencedcto     = cursorDocs.getString(21)
            documentos.tienedcto        = cursorDocs.getString(22)
            documentos.cbsretencion     = cursorDocs.getDouble(23) // cbsret
            documentos.cdretencion      = cursorDocs.getDouble(24) // cdret
            documentos.cbsretencioniva  = cursorDocs.getDouble(25) // cbsretiva
            documentos.cdretencioniva   = cursorDocs.getDouble(26) // cdretiva
            documentos.cbsretparme      = cursorDocs.getDouble(27) // cbsrparme
            documentos.cdretparme       = cursorDocs.getDouble(28) // cdrparme
            documentos.agencia          = cursorDocs.getString(29)
            documentos.bsmtoiva         = cursorDocs.getDouble(30)
            documentos.bsmtofte         = cursorDocs.getDouble(31)
            documentos.retmun_mto       = cursorDocs.getDouble(32)
            documentos.emision          = cursorDocs.getString(33)
            documentos.codcliente       = cursorDocs.getString(34)
            documentos.cdretflete       = cursorDocs.getDouble(35)
            documentos.cbsretflete      = cursorDocs.getDouble(36)
            listaDocumentos.add(documentos)

        }
        cursorDocs.close()
        /*calculos del(los) doc(s)
        en funcion a la lista creada, analizar que montos se deben calcular en relacion
        al saldo, puesto que los montos finales que se reflejen, deben ser los restantes (es decir,
        considerar lo ya pagado). */
        for (i in listaDocumentos.indices){
            codigoCliente         = listaDocumentos[i].codcliente
            tipoDocsaPagar.add(listaDocumentos[i].tipodocv)

            //calculo de la retencion restante del flete pagada
            //2023-03-22 Se elimino por ser redundante
            //listaDocumentos[i].cdretflete  = (listaDocumentos[i].cdretencion - listaDocumentos[i].cdretencioniva - listaDocumentos[i].cdretparme)
            //listaDocumentos[i].cbsretflete = (listaDocumentos[i].cbsretencion - listaDocumentos[i].cbsretencioniva - listaDocumentos[i].cbsretparme)


            var pagado = listaDocumentos[i].dtotpagos
            //var pagadobss = listaDocumentos[i].dtotpagos * listaDocumentos[i].tasadoc

            //iva restante
            //Quitar iva resta y hacerla pregunta mostrar en dolares y bolivares si no esta pagado, si es pagado todo 0
            //Colocar solo el iva en bolivares y en dolares, pero no su resta
            var ivarestabss = 0.0
            var ivaresta    = 0.0
            if(listaDocumentos[i].bsiva - listaDocumentos[i].bsmtoiva == 0.00){
                ivarestabss = 0.00
                ivaresta    = 0.00
            } else {
                /*if(binding.cbExcReten.isChecked){
                    ivarestabss =  listaDocumentos[i].bsiva
                    ivaresta    = listaDocumentos[i].dtotimpuest
                    binding.tvCxcIva.setTextColor(Color.BLACK)
                }else{
                    ivarestabss =  listaDocumentos[i].bsiva - listaDocumentos[i].cbsretencioniva
                    ivaresta    =  listaDocumentos[i].dtotimpuest - listaDocumentos[i].cdretencioniva
                    binding.tvCxcIva.setTextColor(Color.rgb(16, 124, 65))
                }*/

                ivarestabss = listaDocumentos[i].bsiva
                ivaresta    = listaDocumentos[i].dtotimpuest
            }


            //flete restante
            //misco caso que iva
            var fleterestabss = 0.0
            var fleteresta    = 0.0
            if(listaDocumentos[i].bsflete - listaDocumentos[i].bsmtofte == 0.00){
                fleterestabss = 0.00
                fleteresta    = 0.00
            } else {
                fleterestabss = listaDocumentos[i].bsflete
                fleteresta    = listaDocumentos[i].dFlete
            }


            var totalfdoc    = listaDocumentos[i].dtotalfinal
            var totalpdoc    = listaDocumentos[i].dtotpagos
            var tasadoc      = listaDocumentos[i].tasadoc
            var ivapagodoc   = listaDocumentos[i].bsmtoiva
            var fletepagodoc = listaDocumentos[i].bsmtofte
            //var totalnetodoc = listaDocumentos[i].dtotneto - listaDocumentos[i].dFlete
            var totalnetodoc = listaDocumentos[i].dtotneto

            if(pagado <= 0.00){

                ivaRestantebss   += ivarestabss
                fleteRestantebss += fleterestabss
                ivaRestante      += ivaresta
                fleteRestante    += fleteresta

                //netoresta    += listaDocumentos[i].dtotalfinal - listaDocumentos[i].dtotimpuest - listaDocumentos[i].dFlete
                netoresta       += listaDocumentos[i].dtotneto
                //var netorestadoc = listaDocumentos[i].dtotalfinal - listaDocumentos[i].dtotimpuest - listaDocumentos[i].dFlete
                var netorestadoc = listaDocumentos[i].dtotneto
                //COLOCAR FUERA DEL CICLO
                //netoresta    = valorReal(netoresta)
                //netorestabss += netoresta * tasaCambioSeleccionadaPrincipal
                //Esto deberia solucionar el mal calculo de los bolivares
                netorestabss += listaDocumentos[i].dtotneto * tasaCambioSeleccionadaPrincipal

                //netoRestantebss = netorestabss
                //netoRestante  = netoresta
                listaDocumentos[i].netorestante = netorestadoc

            }else if(pagado > 0.00) {

                ivaRestantebss   += ivarestabss
                fleteRestantebss += fleterestabss
                ivaRestante      += ivaresta
                fleteRestante    += fleteresta

                netoresta    +=  totalnetodoc - (totalpdoc - (ivapagodoc / tasadoc) - (fletepagodoc / tasadoc))
                var netorestadoc  =  totalnetodoc - (totalpdoc - (ivapagodoc / tasadoc) - (fletepagodoc / tasadoc))
                //COLOCAR FUERA DEL CICLO
                //netoresta    = valorReal(netoresta)

                //netorestabss = netoresta * tasaCambioSeleccionadaPrincipal
                //Esto deberia solucionar el mal calculo de los bolivares
                netorestabss = (totalnetodoc - (totalpdoc - (ivapagodoc / tasadoc) - (fletepagodoc / tasadoc))) * tasaCambioSeleccionadaPrincipal


                //netoRestantebss = netorestabss
                //netoRestante  = netoresta
                listaDocumentos[i].netorestante = netorestadoc
            }

            if(listaDocumentos[i].dretencion <= 0){
                //retencionRestante    += listaDocumentos[i].cdretencion  + listaDocumentos[i].cdretflete
                //retencionRestantebss += listaDocumentos[i].cbsretencion + listaDocumentos[i].cbsretflete
                retencionRestante    += listaDocumentos[i].cdretencion //<------------------------------------------------- OJO -----------------
                retencionRestantebss += listaDocumentos[i].cbsretencion//<------------------------------------------------- OJO -----------------
            }else{
                //retencionRestante    += listaDocumentos[i].dretencion   - (listaDocumentos[i].cdretencion + listaDocumentos[i].cdretflete)
                //var retencionbsspag  =  listaDocumentos[i].dretencion   * listaDocumentos[i].tasadoc
                //retencionRestantebss += retencionbsspag                 - (listaDocumentos[i].cbsretencion + listaDocumentos[i].cbsretflete)

                //retencionRestante    +=  listaDocumentos[i].dretencion  - listaDocumentos[i].cdretencion
                //retencionRestantebss +=  listaDocumentos[i].bsretencion - listaDocumentos[i].cbsretencion

                retencionRestante    +=  listaDocumentos[i].cdretencion  - listaDocumentos[i].dretencion
                retencionRestantebss +=  listaDocumentos[i].cbsretencion - listaDocumentos[i].bsretencion

            }
        }

        netoRestantebss = netorestabss
        netoRestante  = if (netoresta < 0.00) 0.00 else netoresta
        //netoRestante  = valorReal(netoresta)

        if(binding.cbCxcDescuentos.isChecked && binding.rbCxcDivisasMain.isChecked){
            calcularDescuentos("USD")
        }else{
            descuentoTotal  = 0.00
            listaDescuentos = ArrayList()
            binding.tvCxcDctos.text = "0.00"
            binding.btVerDetDescuento.visibility = View.INVISIBLE
        }

        if(!pagaret){
            retencionRestante       = 0.00
            retencionRestantebss    = 0.00
            binding.tvCxcReten.setTextColor(Color.BLACK)
        } else {
            binding.tvCxcReten.setTextColor(Color.RED)
        }

        if(!binding.rbCxcEfectivoMain.isChecked){
            saldoAFavor = 0.00
        }

        val netoaMostrarTot = netoRestante - descuentoTotal

        if(netoaMostrarTot < saldoAFavor){
            saldoAFavor = 0.00
        }

        val totalRestante = (netoaMostrarTot - saldoAFavor) + ivaRestante  + fleteRestante - retencionRestante
        val totalRestantebss = netoRestantebss + ivaRestantebss + fleteRestantebss - retencionRestantebss

        //asignacion de valores -- debe ser segun la moneda seleccionada valorReal

        if(moneda == "USD"){
            /* montos en usd (normal, sin problemas) */
            binding.tvCxcNeto.text  = valorReal(netoRestante).toString()
            binding.tvCxcIva.text   = valorReal(ivaRestante).toString()
            binding.tvCxcFlete.text = valorReal(fleteRestante).toString()
            //2023-05-05 Se elimino debido a que cuando se recalculaban se incluian las retenciones cuando el boton de excluir estaba crequeado
            binding.tvCxcReten.text = valorReal(retencionRestante).toString()
            //binding.tvCxcReten.text = if (!pagaRetenciones) valorReal(retencionRestante).toString() else 0.00.toString()
            binding.tvCxcTotal.text = valorReal(totalRestante).toString()

        }else if(moneda == "BSS"){
            /* montos en bss (seran montos del doc o uso las tasas para ser mas rapido?) */
            binding.tvCxcNeto.text  = valorReal(netoRestantebss).toString()
            binding.tvCxcIva.text   = valorReal(ivaRestantebss).toString()
            binding.tvCxcFlete.text = valorReal(fleteRestantebss).toString()
            //2023-05-05 Se elimino debido a que cuando se recalculaban se incluian las retenciones cuando el boton de excluir estaba crequeado
            binding.tvCxcReten.text = valorReal(retencionRestantebss).toString()
            //binding.tvCxcReten.text = if (!pagaRetenciones) valorReal(retencionRestantebss).toString() else 0.00.toString()
            binding.tvCxcTotal.text = valorReal(totalRestantebss).toString()

        }

        /*if((tipoDocsaPagar.contains("FAC") && facturaEspecial())){
            binding.rbCxcBssMain.visibility = View.INVISIBLE
            binding.rbCxcBssMain.isChecked  = false
        }*/

        //2023-06-20 APP_NOTA_ENTREGA_BS variable configurable que dice si una nota puede pagarse o no con Bs.
        if(!APP_NOTA_ENTREGA_BS){
            if(tipoDocsaPagar.contains("N/E")){
                binding.rbCxcBssMain.visibility = View.INVISIBLE
                binding.rbCxcBssMain.isChecked  = false
            }
        }

        //println("Booleano --> ${facturaEspecial()}")

        if(listaDocumentos.size > 1 && tipoDocsaPagar.contains("N/E")){
            //binding.rbCxcBssMain.visibility = View.INVISIBLE
            //binding.rbCxcBssMain.isChecked  = false
            binding.rbCxcAbonoMain.visibility = View.INVISIBLE

        }

        if(listaDocumentos.size > 1){
            binding.rbCxcAbonoMain.visibility = View.INVISIBLE
        }
    }

    private fun facturaEspecial(): Boolean {
        var num = 0
        for (i in listaDocumentos.indices){
            val cursor = ke_android.rawQuery("SELECT diascred, recepcion, vence FROM ke_doccti WHERE documento = '${listaDocumentos[i].documento}';",null)
            //println("SELECT diascred, vence FROM ke_doccti WHERE documento = '${listaDocumentos[i].documento}';")
            if (cursor.moveToNext()){
                val diasCredito = cursor.getDouble(0)
                val fechaRecepcion = cursor.getString(1)
                val fechaVence = cursor.getString(2)
                if(diasCredito.toInt() >= conn.getConfigNum("DIAS_VALIDOS_BOLIVARES_DOCS")){
                    if (!(diasRecepcion(fechaRecepcion))){
                        num++
                    }
                }else{
                    if (compararFecha(fechaVence) < 0){
                        num++
                    }
                }
            }
            /*if (cursor.moveToNext()){
                val diasCredito = cursor.getDouble(0)
                val fechaVence = cursor.getString(1)
                println("Dias de Credito $diasCredito")
                println("Fecha de vencimiento $fechaVence")
                if (diasCredito != 15.0){
                    if (diasCredito == 25.0 && diasVencidos(fechaVence)){
                        num++
                    }else if(diasCredito != 25.0){
                        num++
                    }
                }else{
                    if (compararFecha(fechaVence) < 0){
                        num++
                    }
                }
            }*/
            cursor.close()
        }
        return num > 0

    }


    /* buscar tasa en funcion a la fecha provista en el datepicker
    * por defecto,deberia agregarle una fecha (hoy) para que tome por defecto
    * la tasa de hoy */
    private fun buscarTasas(){
        var flag = true

        //consulto al webservice para guardar las tasas
        descargarTasas("https://"+ enlaceEmpresa + "/webservice/tasas.php?fecha_sinc=" + fecha_auxiliar.trim() + "&&agencia=" + codigoSucursal.trim())

        while (flag){
            //println("https://"+ enlaceEmpresa + "/webservice/tasas.php?fecha_sinc=" + fecha_auxiliar.trim() + "&&agencia=" + codigoSucursal.trim())
            //buscarTasas()


            ke_android = conn.writableDatabase
            var tasas:tasas

            val cursorTasas:Cursor = ke_android.rawQuery("SELECT kecxc_id, kecxc_fecha, kecxc_tasa, kecxc_fchyhora, kecxc_tasaib FROM kecxc_tasas WHERE kecxc_fchyhora LIKE '%$fechaQuery%'", null)

            //vacio el cursor en las variables para mostrar
            if(cursorTasas.moveToFirst()){
                tasas = tasas()
                tasas.id              = cursorTasas.getString(0)
                tasas.fecha           = cursorTasas.getString(1)
                tasas.tasa            = cursorTasas.getDouble(2)
                var fechaSinConvertir = cursorTasas.getString(3)
                tasas.tasaib          = cursorTasas.getDouble(4)



                if (tasas.id.isNotEmpty()){
                    flag = false

                    tasaId     = tasas.id
                    tasaNormal = tasas.tasa
                    tasaInterB = tasas.tasaib
                    tasaFecha  = tasas.fecha

                    tasaCambioSeleccionadaPrincipal = tasaNormal
                    binding.tieTasaselec.hint = ""
                    binding.tieTasaselec.hint = "Tasa: $tasaCambioSeleccionadaPrincipal Bs."
                }else{

                    binding.tieTasaselec.hint = ""

                    flag = true

                    val date1 = SimpleDateFormat("yyyy-MM-dd").parse(fechaQuery)

                    val calendar: Calendar = Calendar.getInstance()
                    calendar.time = date1; // Configuramos la fecha que se recibe

                    calendar.add(Calendar.DATE, -1)


                    val sdf = SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)

                    val parsedDate = sdf.parse(calendar.time.toString())
                    val print = SimpleDateFormat("yyyy-MM-dd")


                    fechaQuery = print.format(parsedDate)
                }

            }else{
                binding.tieTasaselec.hint = ""

                flag = true

                val date1 = SimpleDateFormat("yyyy-MM-dd").parse(fechaQuery)

                val calendar: Calendar = Calendar.getInstance()
                calendar.time = date1; // Configuramos la fecha que se recibe

                calendar.add(Calendar.DATE, -1)


                val sdf = SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)

                val parsedDate = sdf.parse(calendar.time.toString())
                val print = SimpleDateFormat("yyyy-MM-dd")


                fechaQuery = print.format(parsedDate)

            }
            cursorTasas.close()
        }



    }


    private fun actualizarBancosCm() {
        listaInfoBancosCm = ArrayList()
        listaInfoBancosCm.add("Seleccione un banco...")
        binding.spCxcBancoCom.listSelection = 0
        for (i in listaBancosCm.indices) {
            listaInfoBancosCm.add(listaBancosCm[i].nombanco)
        }
    }

    private fun actualizarBancos(){
        listaInfoBancos = ArrayList()
        listaInfoBancos.add("Seleccione un banco...")
        binding.spCxcBancoMain.listSelection = 0
        for (i in listaBancos.indices) {
            listaInfoBancos.add(listaBancos[i].nombanco)
        }

    }

    private fun getFechaHoy():String{
        val fechaHoy:String
        val fechaSinConvertir: Calendar = Calendar.getInstance()
        val sdf: SimpleDateFormat       = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        fechaHoy                        = sdf.format(fechaSinConvertir.time)
        return fechaHoy
    }

    private fun getFechaNow():String{
        val fechaNow:String
        val fechaSinConvertir: Calendar = Calendar.getInstance()
        val sdf       = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        fechaNow                        = sdf.format(fechaSinConvertir.time)
        return fechaNow
    }


    private fun iraRetenciones(){
        var listaDocsconRet:ArrayList<String> = ArrayList()


        if(listaTiposRet.size > 0 && !binding.cbExcReten.isChecked){
            //revisar estos ifs
            for (i in listaDocumentos.indices){
                if((listaDocumentos[i].cdretencion > 0.00 && listaDocumentos[i].dretencion == 0.00) || (listaDocumentos[i].cbsretencion > 0.00 && listaDocumentos[i].bsretencion == 0.00)){
                    listaDocsconRet.add(listaDocumentos[i].documento.toString())
                }else{
                    if(listaDocumentos[i].cdretencion == 0.00 && listaDocumentos[i].dretencion == 0.00 && dmontoRetFlete > 0.00){
                        listaDocsconRet.add(listaDocumentos[i].documento.toString())
                    }
                }
            }
            // -- - - - - - - -
            val intent = Intent(applicationContext, retencionesActivity::class.java)
            var bundle:Bundle = Bundle()

            bundle.putSerializable("listaRetenciones", listaRetGuardada)
            bundle.putSerializable("listaDocs", listaDocsconRet)

            intent.putExtras(bundle)
            intent.putExtra("listatiposret", listaTiposRet)
            intent.putExtra("listaDocs", listaDocsconRet)
            startActivityForResult(intent,request_code)


        }else {
            Toast.makeText(this, "El documento no posee retenciones o se han excluido.", Toast.LENGTH_SHORT).show()
        }
    }

    //fun para cargar datos de la empresa act. en la app segun usuario
    private fun cargarEnlace(){
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

    private fun descargarTasas(URL: String) {
        conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null, 14)
        ke_android = conn.readableDatabase

        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET, // method
            URL, // url
            null, // json request
            { response -> // response listener


                if (response != null) {
                    ll_commit = false
                    ke_android.beginTransaction()
                    var jsonObject: JSONObject? = null
                    try {

                        // loop through the array elements
                        for (i in 0 until response.length()) {
                            jsonObject          = response.getJSONObject(i);
                            idTasa              = jsonObject.getString("id")
                            fechaTasa           = jsonObject.getString("fecha")
                            tasaCambio          = jsonObject.getDouble("tasa")
                            usuarioTasa         = jsonObject.getString("usuario")
                            ipTasa              = jsonObject.getString("ip")
                            fechayHoraTasa      = jsonObject.getString("fechayhora")
                            fechamodifitasa     = jsonObject.getString("fechamodifi")
                            tasaInterbancaria   = jsonObject.getDouble("tasaib")

                            val qTasas = ContentValues()
                            qTasas.put("kecxc_id", idTasa)
                            qTasas.put("kecxc_fecha", fechaTasa)
                            qTasas.put("kecxc_tasa", tasaCambio)
                            qTasas.put("kecxc_usuario", usuarioTasa)
                            qTasas.put("kecxc_ip", ipTasa)
                            qTasas.put("kecxc_fchyhora", fechayHoraTasa)
                            qTasas.put("fechamodifi", fechamodifitasa)
                            qTasas.put("kecxc_tasaib", tasaInterbancaria)

                            val qcodigoLocal: Cursor = ke_android.rawQuery("SELECT count(kecxc_id) FROM kecxc_tasas WHERE kecxc_id ='$idTasa'", null)
                            qcodigoLocal.moveToFirst()
                            //variable para obtener el conteo de documentos que ya esten en el telf
                            val codigoExistente = qcodigoLocal.getInt(0)

                            if (codigoExistente > 0) {
                                ke_android.update("kecxc_tasas", qTasas, "kecxc_id= ?", arrayOf(idTasa)
                                )
                            } else if (codigoExistente == 0) {
                                ke_android.insert("kecxc_tasas", null, qTasas)
                            }
                            ll_commit = true

                            qcodigoLocal.close()

                        }

                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        ll_commit = false
                        if (!ll_commit) return@JsonArrayRequest
                    }
                    if (ll_commit) {
                        ke_android.setTransactionSuccessful()
                        ke_android.endTransaction()
                        //buscarTasas()


                    }else if(!ll_commit){
                        ke_android.endTransaction()
                    }
                }
            },
            {error -> // error listener
                //
            }
        )
        val requestQueue: RequestQueue = Volley.newRequestQueue(applicationContext)
        requestQueue.add(jsonArrayRequest)
    }




    private fun getBancos(monedaBanco:String){
        //descargarBancos("https://"+ enlaceEmpresa + "/webservice/bancos.php?fecha_sinc=" + fecha_auxiliar.trim() + "&&agencia=" + codigoSucursal.trim(), monedaBanco)
    }

    private fun descargarBancos(URL:String, monedaBanco: String){
        conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null, 14)
        ke_android = conn.readableDatabase

        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET, // method
            URL, // url
            null, // json request
            { response -> // response listener
                if (response != null) {
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

                            val qBancos = ContentValues()
                            qBancos.put("codbanco", codbanco)
                            qBancos.put("nombanco", nombanco)
                            qBancos.put("cuentanac", cuentanac)
                            qBancos.put("inactiva", inactiva)
                            qBancos.put("fechamodifi", fechamodifiBan)

                            val qcodigoLocal: Cursor = ke_android.rawQuery("SELECT count(codbanco) FROM listbanc WHERE codbanco ='$codbanco'", null)
                            qcodigoLocal.moveToFirst()
                            //variable para obtener el conteo de documentos que ya esten en el telf
                            val codigoExistente = qcodigoLocal.getInt(0)

                            if (codigoExistente > 0) {
                                ke_android.update("listbanc", qBancos, "codbanco= ?", arrayOf(codbanco)
                                )
                            } else if (codigoExistente == 0) {
                                ke_android.insert("listbanc", null, qBancos)
                            }
                            ll_commit = true

                            qcodigoLocal.close()

                        }

                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        ll_commit = false
                        if (!ll_commit) return@JsonArrayRequest
                    }
                    if (ll_commit) {
                        ke_android.setTransactionSuccessful()
                        ke_android.endTransaction()
                        cargarBancosMain(monedaBanco)



                    }else if(!ll_commit){
                        ke_android.endTransaction()
                    }
                }
            },
            {error -> // error listener
                //
            }
        )
        val requestQueue: RequestQueue = Volley.newRequestQueue(applicationContext)
        requestQueue.add(jsonArrayRequest)
        cargarBancosMain(monedaBanco)

    }
    private fun cargarBancosMain(Moneda:String){

        listaInfoBancos.clear()
        listaBancos.clear()

        ke_android = conn.writableDatabase
        var bancos:Bancos
        var moneda = 0.00

        if(Moneda == "USD"){
            moneda = 2.00

        }else if(Moneda == "BSS"){
            moneda = 1.00
        }

        val cursorBancos:Cursor = ke_android.rawQuery("SELECT DISTINCT codbanco, nombanco,cuentanac, inactiva, fechamodifi FROM listbanc WHERE inactiva = 0 AND cuentanac = $moneda", null)
        while(cursorBancos.moveToNext()){
            bancos = Bancos()
            bancos.codbanco     = cursorBancos.getString(0)
            bancos.nombanco     = cursorBancos.getString(1)
            bancos.cuentanac    = cursorBancos.getDouble(2)
            bancos.inactiva     = cursorBancos.getDouble(3)
            bancos.fechamodifi  = cursorBancos.getString(4)
            listaBancos.add(bancos)

        }
        binding.spCxcBancoMain.setText("Seleccione un banco...")
        actualizarBancos()
        val adapterBancos: ArrayAdapter<CharSequence> =
            ArrayAdapter(this, R.layout.spinner_bancos, listaInfoBancos as List<CharSequence>)
        binding.spCxcBancoMain.setAdapter(adapterBancos)

        adapterBancos.notifyDataSetChanged()

        cursorBancos.close()

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if((requestCode == request_code)  && (resultCode == RESULT_OK)){
            var bundle:Bundle = Bundle()
            if (data != null) {
                bundle = data.extras!!
                if (bundle.containsKey("listaRetenciones")){
                    listaRetGuardada = bundle.getSerializable("listaRetenciones") as ArrayList<Retenciones>
                    /*for (i in listaRetGuardada){
                        println("tipo ${i.tiporet}, nroret ${i.nroret}, refret ${i.refret}  fecha ${i.fecharet}  monto ${i.montoret}")

                    }*/
                    //println("LLEGUE AL ACTIVITY ON RESULT")
                }else{
                    //println("NO ESTA LLEGANDO LA LISTA")
                }
            }
            //var listaRetCadena = data?.getStringArrayListExtra("listaRetenciones")
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun compararFecha(fechaRecepcion: String): Int {

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val current = LocalDateTime.now().format(formatter)

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val firstDate: Date = sdf.parse(fechaRecepcion) as Date
        val secondDate: Date = sdf.parse(current) as Date
        println("Comparacion de la fecha ${firstDate.compareTo(secondDate)}")

        //recepcion > fecha = 1
        //recepcion = fecha = 0
        //recepcion < fecha = -1

        return firstDate.compareTo(secondDate)

    }

    private fun diasRecepcion(fechaRecepcion: String): Boolean {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = sdf.parse(fechaRecepcion)
        val cal = Calendar.getInstance()
        cal.time = date!!
        cal.add(Calendar.DATE, + conn.getConfigNum("DIAS_VALIDOS_BOLIVARES_DOCS").toInt())

        val newDate: Date = cal.time

        var inActiveDate = "0000-00-00"

        try {
            inActiveDate = sdf.format(newDate)
            println(inActiveDate)
        } catch (e1: ParseException) {

            // TODO Auto-generated catch block
            e1.printStackTrace()
        }

        return compararFecha(inActiveDate) >= 0

    }
}