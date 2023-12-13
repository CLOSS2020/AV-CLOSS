package com.appcloos.mimaletin

import android.content.ContentValues
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.icu.text.DecimalFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.InputFilter
import android.text.InputType
import android.text.SpannableStringBuilder
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.appcloos.mimaletin.classes.DecimalDigitsInputFilter
import com.appcloos.mimaletin.databinding.ActivityCxcReportBinding
import org.json.JSONObject
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt


class CxcReportActivity : AppCompatActivity() {
    private var APP_NOTA_ENTREGA_BS = false
    private var APP_PORCENTAJE_COMPLEMENTO = 0.9

    private var contadorRetenIVA = 0
    private var contadorRetenFlete = 0
    private var contadorRetenParme = 0
    private var contadorDoc = 0

    //declaracion de variables--
    //viewbinding
    private lateinit var binding: ActivityCxcReportBinding

    //DB
    private lateinit var conn: AdminSQLiteOpenHelper
    lateinit var keAndroid: SQLiteDatabase
    lateinit var preferences: SharedPreferences

    //Strings
    private lateinit var nroPrecobranza: String
    private var fechaQuery: String = ""
    private var codUsuario: String? = ""
    var codEmpresa: String? = ""
    var nombreEmpresa: String = ""
    var codigoSucursal: String = ""
    var enlaceEmpresa: String = ""
    private var fechaAuxiliar: String = "0001-01-01"
    private var fechaActual = ""
    private var idTasa: String = ""
    private var fechaTasa: String = ""
    private var fechayHoraTasa: String = ""
    private var fechamodifitasa: String = ""
    private var usuarioTasa: String = ""
    private var ipTasa: String = ""
    var codbanco: String = ""
    var nombanco: String = ""
    private var fechamodifiBan: String = ""
    private var correlativoTexto: String = ""
    var tipoDoc = "PRC"
    var codigoCliente = ""
    private var codigoBancoCompleto = ""
    private var nombreBancoCompleto = ""
    private var referenciaPrincipal = ""
    private var referenciaCm = ""
    private var tasaId = ""
    private var codigoBancoComplemento = ""
    private var monedaSeleccionadaPr = ""
    private var monedaSeleccionadaCm = "2"
    private var nroComplemento = ""
    private var correlativoTextoCom = ""
    private var fechatasaH = ""

    //Integers
    var requestCodeRetencion = random()
    var requestCodeImg = random()
    var nroCorrelativo = 0
    private var nroCorrelativoCom = 0
    private var tasaFecha = ""

    private lateinit var listBankDesc: List<String>

    //Doubles
    var bsNeto = 0.00
    var dnetoTotal = 0.00
    var bsIvaTotal = 0.00
    var bsFleteTotal = 0.00
    var bsretencionIvaTotal = 0.00
    var bsRetencionTotal = 0.00
    var montoaPagar = 0.00
    var dtotimpuest = 0.00
    var dFlete = 0.00
    var dretencion = 0.00
    var dretencioniva = 0.00
    var dmontoTotal = 0.00
    var bsmontoTotal = 0.00
    private var tasaNormal = 0.00
    private var tasaInterB = 0.00
    private var descuentoTotal = 0.00
    private var dmontoRetFlete = 0.00
    var bsmontoRetFlete = 0.00
    var montominimo = 0.00
    private var cdretencion = 0.00
    private var cdretencioniva = 0.00
    var cbsretparme = 0.00
    private var cdretparme = 0.00
    var retenpagado = 0.00
    var cbsretencioniva = 0.00
    private var montoMinimoRec = 0.00
    var montoMinimoImp = 0.00
    var montoMinimoComp = 0.00
    var montoI = 0.00
    var montoC = 0.00
    private var montoRec = 0.00
    var saldo = 0.00
    var montoNuevoIva = 0.00
    var montoNuevoFlete = 0.00
    private var tasaCambio: Double = 0.00
    private var tasaCambioSeleccionadaPrincipal: Double = 0.00
    var tasaCambioComplemento: Double = 0.00
    var cuentanac: Double = 0.00
    var inactiva: Double = 0.00
    private var tasaInterbancaria = 0.00
    private var netoRestante = 0.00
    private var ivaRestante = 0.00
    private var fleteRestante = 0.00
    private var cantidadDeDescuento = 0.00

    //Booleans
    private var llCommit: Boolean = false
    private var pagaRetenciones: Boolean = false

    //Variable que me dira si debo de mostrar Excluir Retenciones
    private var debeReten: Boolean = false

    val formatoNum = DecimalFormat("0.00")

    //listas--
    //listas string
    private lateinit var listaDocsSeleccionados: ArrayList<String>
    private lateinit var listaInfoTasas: ArrayList<String>
    private lateinit var listaInfoBancosImp: ArrayList<String>
    private lateinit var listaInfoBancosCm: ArrayList<String>
    private lateinit var listaInfoBancos: ArrayList<String>
    private lateinit var tipoDocsaPagar: ArrayList<String>
    private lateinit var listaTiposRet: ArrayList<String>

    //tipo tasas
    private lateinit var listaTasas: ArrayList<tasas>

    // tipo bancos
    private lateinit var listaBancos: ArrayList<Bancos>
    private lateinit var listaBancosImp: ArrayList<Bancos>
    private lateinit var listaBancosCm: ArrayList<Bancos>

    //tipo docs
    private lateinit var listaDocumentos: ArrayList<Documentos>

    //tipo descuentos
    private lateinit var listaDescuentos: ArrayList<Descuentos>

    //tipo retenciones
    private lateinit var listaRetGuardada: ArrayList<Retenciones>

    private lateinit var listaBancoRepetible: List<String>

    private var listaImagenes: MutableList<Uri> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCxcReportBinding.inflate(layoutInflater) //initializing the binding class
        setContentView(binding.root)
        //clase para guardar fotos
        val savePhoto = SavePhoto(this)

        //System.out.println("ENLACE " + enlaceEmpresa);

        //inst. conexion
        conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null)

        APP_NOTA_ENTREGA_BS = conn.getConfigBool("APP_NOTA_ENTREGA_BS", codEmpresa!!)
        APP_PORCENTAJE_COMPLEMENTO = conn.getConfigNum("APP_PORCENTAJE_COMPLEMENTO", codEmpresa!!)

        //listBankDesc = conn.getConfigString("APP_BANCOS_DESCUENTOS").trim().split(",")

        listaBancoRepetible =
            conn.getConfigString("APP_BANCOS_REFERENCIA_REPETIBL", codEmpresa!!).replace(" ", "").split(",")

        /*if (listBankDesc[0] == "") {
            binding.cbCxcDescuentos.isEnabled = false
            binding.cbCxcDescuentos.isChecked = false
        }*/

        //cargar preferences
        preferences = getSharedPreferences("Preferences", MODE_PRIVATE)
        codUsuario = preferences.getString("cod_usuario", null)
        codEmpresa = preferences.getString("codigoEmpresa", null)

        //Edicion de los colores del Bar de arriba de notificacion de las app y el bar de abajo de los 3 botones
        windowsColor(Constantes.AGENCIA)
        setColors()
        //cargar datos
        cargarEnlace()
        fechaActual = getFechaHoy()
        bajarDocsConDesc("https://" + enlaceEmpresa + "/webservice/bajardescuentos.php?fecha_sinc=" + fechaAuxiliar.trim() + "&&codigo_cli=" + codigoCliente.trim() + "&&agencia=" + codigoSucursal.trim())
        //Listas de Array
        listaTasas = ArrayList()
        listaInfoTasas = ArrayList()
        listaBancos = ArrayList()
        listaBancosImp = ArrayList()
        listaInfoBancos = ArrayList()
        listaDocumentos = ArrayList()
        tipoDocsaPagar = ArrayList()
        listaInfoBancosImp = ArrayList()
        listaDescuentos = ArrayList()
        listaRetGuardada = ArrayList()
        listaInfoBancosCm = ArrayList()
        listaBancosCm = ArrayList()
        listaTiposRet = ArrayList()

        //carga de los docs previamente adjuntados
        listaDocsSeleccionados = intent.getStringArrayListExtra("listaDocs") as ArrayList<String>
        //validacion del correlativo para la cobranza
        val cursorCorrelativo = keAndroid.rawQuery(
            "SELECT MAX(kcor_numero) FROM ke_corprec WHERE kcor_vendedor ='$codUsuario'", null
        )
        //----
        if (cursorCorrelativo.moveToFirst()) {
            nroCorrelativo = cursorCorrelativo.getInt(0)
            nroCorrelativo += 1
            correlativoTexto = nroCorrelativo.toString()
            correlativoTexto = "0000$correlativoTexto"

            nroCorrelativoCom = nroCorrelativo + 1
            correlativoTextoCom = nroCorrelativoCom.toString()
            correlativoTextoCom = "0000$correlativoTextoCom"

        } else {
            nroCorrelativo = cursorCorrelativo.getInt(0)
            nroCorrelativo += 1
            correlativoTexto = nroCorrelativo.toString()
            correlativoTexto = "0000$correlativoTexto"

            nroCorrelativoCom = nroCorrelativo + 1
            correlativoTextoCom = nroCorrelativoCom.toString()
            correlativoTextoCom = "0000$correlativoTextoCom"
        }
        cursorCorrelativo.close()

        //generacion del correlativo completo
        nroPrecobranza = generarNroPrecobranza()

        //generacion del nro del complemento
        nroComplemento = generarNroComplemento()

        supportActionBar?.title = "REC: $nroPrecobranza"
        listaDocsSeleccionados.joinToString(separator = ",")

        //query de los bancos
        //getBancos("USD")
        //cargarBancosMain("BSS")

        //coloco por defecto opciones seleccionadas
        binding.rbCxcDivisasMain.isChecked = true
        binding.rbCxcCompMain.isChecked = true
        binding.rbCxcTransfMain.isChecked = true

        //calculo automatico, mejorar despues .-
        if (binding.rbCxcDivisasMain.isChecked) {
            cargarBancosMain("USD")
            binding.rbCxcEfectivoMain.visibility = View.VISIBLE
            cargarSaldos("USD", listaDocsSeleccionados)
            monedaSeleccionadaPr = "2"

        }
        if (binding.rbCxcCompMain.isChecked) {
            //TBD
        } else {
            binding.cbCxcComplemento.visibility = View.INVISIBLE
        }

        //---------------------
        validarRetenciones()

        //Radio button bss principal
        binding.rbCxcBssMain.setOnClickListener {
            if (binding.rbCxcBssMain.isChecked) {
                if (tasaCambioSeleccionadaPrincipal == 0.00) {
                    Toast.makeText(
                        this, "Debes seleccionar una fecha de pago", Toast.LENGTH_SHORT
                    ).show()
                    binding.rbCxcDivisasMain.isChecked = true
                    binding.rbCxcTransfMain.isChecked = true
                    //Cuando se cambia la moneda se deja en blanco la variable que guarda el banco
                    codigoBancoCompleto = ""
                    binding.spCxcBancoMain.listSelection = 0

                } else {
                    cargarBancosMain("BSS")
                    binding.rbCxcEfectivoMain.visibility = View.INVISIBLE
                    binding.cbCxcDescuentos.visibility = View.INVISIBLE
                    cargarSaldos("BSS", listaDocsSeleccionados, !binding.cbExcReten.isChecked)
                    monedaSeleccionadaPr = "1"
                    binding.rbCxcTransfMain.isChecked = true
                }

            } else {
                cargarBancosMain("USD")
                binding.rbCxcEfectivoMain.visibility = View.VISIBLE
                monedaSeleccionadaPr = "2"
                binding.rbCxcTransfMain.isChecked = true
            }
            mostrandoComplemento()
        }
        //Radio button divisas principal
        binding.rbCxcDivisasMain.setOnClickListener {
            if (binding.rbCxcDivisasMain.isChecked) {
                cargarBancosMain("USD")
                binding.rbCxcEfectivoMain.visibility = View.VISIBLE
                binding.cbCxcDescuentos.visibility = View.VISIBLE
                cargarSaldos("USD", listaDocsSeleccionados, !binding.cbExcReten.isChecked)
                monedaSeleccionadaPr = "2"
                //Cuando se cambia la moneda se deja en blanco la variable que guarda el banco
                codigoBancoCompleto = ""
                binding.spCxcBancoMain.listSelection = 0
            }
            mostrandoComplemento()
        }

        //datepicker de la fecha de pago
        binding.dpFecharec.setOnClickListener {
            showDatePickerDialog()
            mostrandoComplemento()
            //retenciones()
        }

        //boton procesar
        binding.btCxcProcesar.setOnClickListener {
            if (binding.rbCxcCompMain.isChecked &&
                superSaldoFavor() > 0 &&
                binding.rbCxcBssMain.isChecked
            ) {
                val ventana =
                    AlertDialog.Builder(ContextThemeWrapper(this, R.style.AlertDialogCustom))
                ventana.setTitle("Advertencia")
                ventana.setMessage("Recuerde que no se guardan saldos a favor en bolivares. \n¿Esta seguro de desear continuar con la operacion?")
                ventana.setPositiveButton("Si") { dialog, which ->
                    procesamientodeDatos()
                }
                ventana.setNegativeButton("No") { dialog, which ->

                }
                val dialogo = ventana.create()
                dialogo.show()
            } else {
                procesamientodeDatos()
            }

            //procesar2()
        }

        //boton retencion
        binding.btCxcRetenciones.setOnClickListener {
            iraRetenciones()
        }

        binding.cbCxcDescuentos.setOnClickListener {

            pagaRetenciones = if (binding.cbCxcDescuentos.isChecked) {
                cargarSaldos("USD", listaDocsSeleccionados, !binding.cbExcReten.isChecked)
                recalcularComplemento()
                !binding.cbExcReten.isChecked
            } else {
                cargarSaldos("USD", listaDocsSeleccionados, !binding.cbExcReten.isChecked)
                recalcularComplemento()
                !binding.cbExcReten.isChecked
            }
            mostrandoComplemento()

        }


        //seleccion de bancos Main
        binding.spCxcBancoMain.setOnItemClickListener { parent, view, position, id ->
            if (position != 0) {
                codigoBancoCompleto = listaBancos[position - 1].codbanco
                nombreBancoCompleto = listaBancos[position - 1].nombanco

                if (binding.rbCxcDivisasMain.isChecked) {
                    calcularDescuentos2("USD")
                    cargarSaldos("USD", listaDocsSeleccionados, !binding.cbExcReten.isChecked)
                }

            } else if (position == 0) {
                codigoBancoCompleto = ""
                cargarSaldos("USD", listaDocsSeleccionados, !binding.cbExcReten.isChecked)

                if (binding.rbCxcDivisasMain.isChecked) {
                    calcularDescuentos2("USD")
                    cargarSaldos("USD", listaDocsSeleccionados, !binding.cbExcReten.isChecked)
                }
            }
            mostrandoComplemento()
        }

        binding.radioGroup6.setOnCheckedChangeListener { radioGroup, i -> //Es ek RadioGroup de los tipos de movimientos bancarios
            if (binding.rbCxcTransfMain.isChecked) {
                calcularDescuentos2("USD")
            } else {
                calcularDescuentos2("BSS")
            }
        }


        //seleccion de bancos Complemento
        binding.spCxcBancoCom.setOnItemClickListener { parent, view, position, id ->
            if (position != 0) {
                codigoBancoComplemento = listaBancosCm[position - 1].codbanco


            } else if (position == 0) {
                codigoBancoComplemento = ""

            }
        }

        binding.rbCxcAbonoMain.setOnClickListener {
            if (binding.rbCxcAbonoMain.isChecked) {

                binding.cbCxcComplemento.visibility = View.INVISIBLE
                binding.cbCxcComplemento.isChecked = false

                val editable: Editable = SpannableStringBuilder("")

                binding.etCxcMontoCom.text = editable
                binding.etCxcRefCom.text = editable

                binding.tvCxcComplemento.visibility = View.INVISIBLE
                binding.rbCxcBssCom.visibility = View.INVISIBLE
                binding.rbCxcDivisasCom.visibility = View.INVISIBLE
                binding.rbCxcTransfCom.visibility = View.INVISIBLE
                binding.rbCxcEfectivoCom.visibility = View.INVISIBLE
                binding.tilBancoCom.visibility = View.INVISIBLE
                binding.spCxcBancoCom.visibility = View.INVISIBLE
                binding.tilRefCom.visibility = View.INVISIBLE
                binding.etCxcRefCom.visibility = View.INVISIBLE
                binding.tilMontoCom.visibility = View.INVISIBLE
                binding.etCxcMontoCom.visibility = View.INVISIBLE
                binding.tvPrecioMostrarComplemento.visibility = View.INVISIBLE

                binding.cbCxcDescuentos.visibility = View.INVISIBLE
                binding.cbCxcDescuentos.isChecked = false


            } else {
                binding.cbCxcComplemento.visibility = View.VISIBLE
                binding.cbCxcDescuentos.visibility = View.VISIBLE
            }
            cargarSaldos(
                if (binding.rbCxcDivisasMain.isChecked) "USD" else "BSS",
                listaDocsSeleccionados,
                !binding.cbExcReten.isChecked
            )
            mostrandoComplemento()

            //2023-11-10 Verificando retencion
            //Estabas configurando el boton completo y abono para bloquear excluir retenciones
            validarReten()
            binding.cbExcReten.isEnabled = (contadorRetenFlete <= 0)
        }

        binding.rbCxcCompMain.setOnClickListener {
            if (binding.rbCxcCompMain.isChecked) {
                binding.cbCxcComplemento.visibility = View.VISIBLE
                binding.cbCxcComplemento.isChecked = false
                binding.cbCxcDescuentos.visibility = View.VISIBLE
            } else {
                binding.cbCxcComplemento.visibility = View.INVISIBLE
                binding.cbCxcDescuentos.visibility = View.INVISIBLE
            }
            cargarSaldos(
                if (binding.rbCxcDivisasMain.isChecked) "USD" else "BSS",
                listaDocsSeleccionados,
                !binding.cbExcReten.isChecked
            )
            mostrandoComplemento()

            //2023-11-10 Verificando retencion
            //Estabas configurando el boton completo y abono para bloquear excluir retenciones
            calcularRetencion()

        }

        binding.rbCxcEfectivoMain.setOnClickListener {

            //Limpia el editText y re-afirma el tipo de dato a ingresar
            //En este caso el tipo es numerico SIN decimales
            binding.etCxcMontoMain.text?.clear()
            binding.etCxcMontoMain.inputType = InputType.TYPE_CLASS_NUMBER

            if (binding.rbCxcEfectivoMain.isChecked) {
                binding.tilCxcSpbanco.visibility = View.INVISIBLE
                binding.tilCxcRefMain.visibility = View.INVISIBLE
                codigoBancoCompleto = ""
                referenciaPrincipal = ""
                cargarSaldos("USD", listaDocsSeleccionados, !binding.cbExcReten.isChecked)


            } else {

                binding.tilCxcRefMain.visibility = View.VISIBLE
                binding.tilCxcSpbanco.visibility = View.VISIBLE

                if (binding.rbCxcDivisasMain.isChecked) {
                    cargarSaldos("USD", listaDocsSeleccionados, !binding.cbExcReten.isChecked)
                }

            }

            mostrandoComplemento()

        }

        binding.rbCxcTransfMain.setOnClickListener {

            //Limpia el editText y re-afirma el tipo de dato a ingresar
            //En este caso el tipo es numerico COM decimales
            binding.etCxcMontoMain.text?.clear()
            binding.etCxcMontoMain.inputType =
                InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED

            binding.tilCxcMontoMain.visibility = View.VISIBLE
            binding.tilCxcRefMain.visibility = View.VISIBLE
            binding.tilCxcSpbanco.visibility = View.VISIBLE
            if (binding.rbCxcDivisasMain.isChecked) {
                cargarSaldos("USD", listaDocsSeleccionados, !binding.cbExcReten.isChecked)
            }
            mostrandoComplemento()

        }

        binding.rbCxcTransfCom.setOnClickListener {

            //Limpia el editText y re-afirma el tipo de dato a ingresar
            //En este caso el tipo es numerico CON decimales
            binding.etCxcMontoCom.text?.clear()
            binding.etCxcMontoCom.inputType =
                InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED

            binding.tilBancoCom.visibility = View.VISIBLE
            binding.tilRefCom.visibility = View.VISIBLE
            binding.rbCxcBssCom.visibility = View.VISIBLE

        }

        binding.rbCxcEfectivoCom.setOnClickListener {

            //Limpia el editText y re-afirma el tipo de dato a ingresar
            //En este caso el tipo es numerico SIN decimales
            binding.etCxcMontoCom.text?.clear()
            binding.etCxcMontoCom.inputType = InputType.TYPE_CLASS_NUMBER

            binding.tilBancoCom.visibility = View.INVISIBLE
            binding.tilRefCom.visibility = View.INVISIBLE
            codigoBancoComplemento = ""
            referenciaCm = ""
            binding.rbCxcBssCom.visibility = View.INVISIBLE
            binding.rbCxcBssCom.isChecked = false
        }

        binding.btVerDetDescuento.setOnClickListener {
            cargarDetalleDescuentos()
        }


        //activar complemento
        binding.cbCxcComplemento.setOnClickListener {


            if (binding.cbCxcComplemento.isChecked) {
                //hago visible las cosas
                binding.tvCxcComplemento.visibility = View.VISIBLE
                binding.rbCxcBssCom.visibility = View.VISIBLE
                binding.rbCxcDivisasCom.visibility = View.VISIBLE
                binding.rbCxcTransfCom.visibility = View.VISIBLE
                binding.rbCxcEfectivoCom.visibility = View.VISIBLE
                binding.tilBancoCom.visibility = View.VISIBLE
                binding.spCxcBancoCom.visibility = View.VISIBLE
                binding.tilRefCom.visibility = View.VISIBLE
                binding.etCxcRefCom.visibility = View.VISIBLE
                binding.tilMontoCom.visibility = View.VISIBLE
                binding.etCxcMontoCom.visibility = View.VISIBLE
                //ejecuto la carga de bancos (por defecto en USD)
                cargarBancosCom("USD")
                binding.rbCxcDivisasCom.isChecked = true
                binding.rbCxcTransfCom.isChecked = true
                if (binding.rbCxcDivisasMain.isChecked) {
                    cargarSaldos("USD", listaDocsSeleccionados, !binding.cbExcReten.isChecked)
                }
                binding.tvPrecioMostrarComplemento.visibility = View.VISIBLE


            } else {
                //Para poner texto en blanco en un edittext
                val editable: Editable = SpannableStringBuilder("")

                binding.etCxcMontoCom.text = editable
                binding.etCxcRefCom.text = editable

                binding.tvCxcComplemento.visibility = View.INVISIBLE
                binding.rbCxcBssCom.visibility = View.INVISIBLE
                binding.rbCxcDivisasCom.visibility = View.INVISIBLE
                binding.rbCxcTransfCom.visibility = View.INVISIBLE
                binding.rbCxcEfectivoCom.visibility = View.INVISIBLE
                binding.tilBancoCom.visibility = View.INVISIBLE
                binding.spCxcBancoCom.visibility = View.INVISIBLE
                binding.tilRefCom.visibility = View.INVISIBLE
                binding.etCxcRefCom.visibility = View.INVISIBLE
                binding.tilMontoCom.visibility = View.INVISIBLE
                binding.etCxcMontoCom.visibility = View.INVISIBLE
                binding.tvPrecioMostrarComplemento.visibility = View.INVISIBLE
                //binding.rbCxcTransfCom.isChecked   = false
                if (binding.rbCxcDivisasMain.isChecked) {
                    cargarSaldos("USD", listaDocsSeleccionados, !binding.cbExcReten.isChecked)
                }
            }
            recalcularComplemento()
            mostrandoComplemento()

        }

        binding.rbCxcDivisasCom.setOnClickListener {
            if (binding.rbCxcDivisasCom.isChecked) {
                cargarBancosCom("USD")
                monedaSeleccionadaCm = "2"
                binding.rbCxcEfectivoCom.visibility = View.VISIBLE
                //cargarSaldos("USD", listaDocsSeleccionados, pagaRetenciones)

                recalcularComplemento()
                mostrandoComplemento()

            } else {
                //cargarBancosCom("BSS")
                monedaSeleccionadaCm = "1"
                //cargarSaldos("BSS", listaDocsSeleccionados, pagaRetenciones)
                recalcularComplemento()
            }
        }

        binding.rbCxcBssCom.setOnClickListener {
            if (binding.rbCxcBssCom.isChecked) {
                if (tasaCambioSeleccionadaPrincipal == 0.00) {
                    Toast.makeText(this, "Debes seleccionar una fecha de pago", Toast.LENGTH_SHORT)
                        .show()
                    binding.rbCxcDivisasCom.isChecked = true
                    binding.rbCxcDivisasCom.isChecked = true
                } else {
                    cargarBancosCom("BSS")
                    monedaSeleccionadaCm = "1"
                    binding.rbCxcEfectivoCom.visibility = View.INVISIBLE
                    binding.rbCxcEfectivoCom.isChecked = false
                    binding.rbCxcTransfCom.isChecked = true
                    recalcularComplemento()
                    mostrandoComplemento()
                }

            } else {
                //cargarBancosCom("USD")
                monedaSeleccionadaCm = "2"
                recalcularComplemento()
            }
            //Funcion para recalcular lo que se debe de poner en complemento
            mostrandoComplemento()
        }

        binding.cbExcReten.setOnClickListener {
            //Comprobando que exista una retencion de flete para bloquear el boton
            //si existe una retencion de flete no se puede excluir la retencion
            if (contadorRetenFlete > 0 && contadorRetenParme > 0) {
                binding.cbExcReten.isChecked = false
                Toast.makeText(this, "Ya agregó retención de flete y Parme", Toast.LENGTH_SHORT)
                    .show()
            } else if (contadorRetenFlete > 0) {
                binding.cbExcReten.isChecked = false
                Toast.makeText(this, "Ya agregó retención de flete", Toast.LENGTH_SHORT).show()
            } else if (contadorRetenParme > 0) {
                binding.cbExcReten.isChecked = false
                Toast.makeText(this, "Ya agregó retención Parme", Toast.LENGTH_SHORT).show()
            } else {
                retenciones()
                mostrandoComplemento()
            }

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

        binding.etCxcMontoMain.addTextChangedListener {
            mostrandoComplemento()
        }
        binding.etCxcMontoMain.filters = arrayOf<InputFilter>(DecimalDigitsInputFilter(10, 2))
        binding.etCxcMontoCom.filters = arrayOf<InputFilter>(DecimalDigitsInputFilter(10, 2))

        validandoRetencion()

        cambioEstadoView()
    }

    private fun cambioEstadoView() {
        binding.apply {
            cbExcReten.isVisible = debeReten
        }
    }

    private fun validandoRetencion() {
        var contador = 0
        listaDocumentos.forEach { documento ->
            if (documento.agencia == "001" && (documento.bsretencion - documento.bsretencioniva >= 0.0) && (documento.cbsretflete > 0 || documento.cbsretparme > 0)) {
                contador++
            }
        }
        if (contador > 0) {
            debeReten = true
        }
    }

    /*fun procesar2() {
        //FALTA DESCUENTO EN CADA DOCUMENTO
        //FALTA RETENCION EN CADA DOCUMENTO

        val cabecera = ke_precobranza()
        val lineas = ke_precobradocs()

        val fechaRegistro = fechaQuery
        //Variables de Botones (Boolean)
        // Variables Moneda
        val monedaDivisa = binding.rbCxcDivisasMain.isChecked
        val monedaBs = binding.rbCxcBssMain.isChecked

        //Variables Tipo de Pago
        val pagoCompleto = binding.rbCxcCompMain.isChecked
        val pagoAbono = binding.rbCxcAbonoMain.isChecked

        //Variable Complemento
        val complemento = binding.cbCxcComplemento.isChecked

        //Tipo de metodo de pago
        val transferencia = binding.rbCxcTransfMain.isChecked
        val efectivo = binding.rbCxcEfectivoMain.isChecked

        //Excluir retenciones
        val excReten = binding.cbExcReten.isChecked
        val darDescu = binding.cbCxcDescuentos.isChecked

        //Variables Numericas
        val neto = binding.tvCxcNeto.text.toString().toDouble()
        val iva = binding.tvCxcIva.text.toString().toDouble()
        val flete = binding.tvCxcFlete.text.toString().toDouble()
        val retencion = binding.tvCxcReten.text.toString().toDouble()
        val descuento = binding.tvCxcDctos.text.toString().toDouble()
        val total = binding.tvCxcTotal.text.toString().toDouble()

        var pago = binding.etCxcMontoMain.text.toString().toDouble()

        val montoMinimo = if (pagoCompleto) total else (iva + flete)
        val bancoRef = if (transferencia) binding.etCxcRefMain.text.toString() else ""
        val banco = if (transferencia) codigoBancoCompleto else ""

        if (validacionesPrincipales(
                fechaRegistro, pago, bancoRef, banco, transferencia, efectivo
            )
        ) {
            return
        }


        if (complemento) {

            val cabeceraCom = ke_precobranza()
            val lineasCom = ke_precobradocs()

            val monedaDivisaCom = binding.rbCxcDivisasCom.isChecked
            val monedaBsCom = binding.rbCxcBssCom.isChecked

            val transferenciaCom = binding.rbCxcTransfCom.isChecked
            val efectivoCom = binding.rbCxcEfectivoCom.isChecked

            val pagoCom = binding.etCxcMontoCom.text.toString().toDouble()

            val bancoRefCom = if (transferencia) binding.etCxcRefCom.text.toString() else ""
            val bancoCom = if (transferencia) codigoBancoComplemento else ""

            if (validacionesComplemento(
                    pagoCom,
                    bancoRefCom,
                    bancoCom,
                    transferencia,
                    efectivoCom,
                    montoMinimo,
                    pago,
                    monedaDivisaCom
                )
            ) {
                return
            }


        } else {
            if (montoMinimo > pago) {
                Toast.makeText(
                    this, "El monto que registra es insuficiente para el pago", Toast.LENGTH_SHORT
                ).show()
                return
            }

            cabecera.cxcndoc = nroPrecobranza
            cabecera.tiporecibo = "W"
            cabecera.codvend = codUsuario.toString()
            cabecera.kecxc_id = tasaId
            cabecera.tasadia = tasaCambioSeleccionadaPrincipal
            cabecera.fchrecibo = fechatasaH
            cabecera.clicontesp = listaDocumentos[0].contribesp.toString()
            cabecera.moneda = monedaSeleccionadaPr
            if (efectivo) {
                cabecera.efectivo = pago
                cabecera.fchvigen = fechaSuma(fechaActual, 60)
                cabecera.diasvigen = 60.0
            } else {
                cabecera.bcocod = codigoBancoCompleto
                cabecera.bconombre = nombreBancoCompleto.trimEnd()
                cabecera.bcomonto = pago
                cabecera.bcoref = bancoRef
                cabecera.fchvigen = fechaSuma(fechaActual, 5)
                cabecera.diasvigen = 5.0
            }
            cabecera.edorec = "0"
            cabecera.fchhr = fechaActual
            cabecera.fechamodifi = getFechaHoy()


            //EMpezando a llenar lineas

            lineas.cxcndoc = nroPrecobranza
            lineas.agencia = listaDocumentos[0].agencia
            lineas.tipodoc = listaDocumentos[0].tipodoc
            lineas.documento = listaDocumentos[0].documento
            lineas.fchrecibod = getFechaNow()
            lineas.kecxc_idd = tasaId
            lineas.tasadiad = tasaCambioSeleccionadaPrincipal

            if (listaDocumentos[0].tipodocv == "FAC") {
                //IF que valida que lo pagado por el cliente por el IVA sea 0, indicando que ya fue pagado
                if (listaDocumentos[0].bsiva - listaDocumentos[0].bsmtoiva > 0.00) {
                    //Calculando el pago del IVA en bolivares y dolares, tomando en cuenta si paga o no retencion
                    val restaIvaDol =
                        if (excReten) listaDocumentos[0].dtotimpuest else listaDocumentos[0].dtotimpuest - listaDocumentos[0].cdretencioniva
                    val restaIvaBss =
                        if (excReten) listaDocumentos[0].bsiva else listaDocumentos[0].bsiva - listaDocumentos[0].cbsretencioniva

                    //IF que valida en que moneda se paga el documento
                    if (monedaDivisa) {// Caso de que la moneda sea en dolares
                        //Restando IVA en dolares del pago
                        pago -= restaIvaDol

                        lineas.bscobro += restaIvaBss  //Adicionando lo cobrado del IVA al cobro total en bolivares del documento
                        lineas.tnetoddol += restaIvaDol  //Adicionando lo cobrado del IVA al cobro total en dolares del documento
                        lineas.bsmtoiva =
                            restaIvaBss   //Monto de lo cobrado del IVA del documento en bolivares

                        cabecera.doliva += restaIvaDol  //Adicionando lo cobrado del IVA en dolates en la cabecera
                        cabecera.bsiva += restaIvaBss
                    } else {// Caso de que la moneda sea en bolivares
                        //Restando FLete en bolivares del pago
                        pago -= restaIvaBss

                        lineas.bscobro += restaIvaBss  //Adicionando lo cobrado del IVA al cobro total en bolivares del documento
                        lineas.tnetodbs += restaIvaBss  //Adicionando lo cobrado del IVA al cobro total en bolivares del documento
                        lineas.bsmtoiva =
                            restaIvaBss   //Monto de lo cobrado del IVA del documento en bolivares

                        cabecera.doliva += restaIvaDol  //Adicionando lo cobrado del IVA en dolates en la cabecera
                        cabecera.bsiva += restaIvaBss
                    }
                }

                //IF que valida que lo pagado por el cliente por el flete sea 0, indicando que ya fue pagado
                if (listaDocumentos[0].bsflete - listaDocumentos[0].bsmtofte > 0) {
                    //Calculando el pago del flete en bolivares y dolares, tomando en cuenta si paga o no retencion
                    val restaFleteDol =
                        if (excReten) listaDocumentos[0].dFlete else listaDocumentos[0].dFlete - listaDocumentos[0].cdretflete
                    val restaFleteBss =
                        if (excReten) listaDocumentos[0].bsflete else listaDocumentos[0].bsflete - listaDocumentos[0].cbsretflete

                    //IF que valida en que moneda se paga el documento
                    if (monedaDivisa) { // Caso de que la moneda sea en dolares
                        //Restando FLete en dolares del pago
                        pago -= restaFleteDol

                        lineas.bscobro += restaFleteBss    //Adicionando lo cobrado del flete al cobro total en bolivares del documento
                        lineas.tnetoddol += restaFleteDol    //Adicionando lo cobrado del flete al cobro total en dolares del documento
                        lineas.bsmtofte =
                            restaFleteBss     //Monto de lo cobrado del flete del documento en bolivares

                        cabecera.dolflete += restaFleteDol    //Adicionando lo cobrado del flete en dolates en la cabecera
                        cabecera.bsflete += restaFleteBss

                    } else { // Caso de que la moneda sea en bolivares
                        //Restando FLete en bolivares del pago
                        pago -= restaFleteBss

                        lineas.bscobro += restaFleteBss    //Adicionando lo cobrado del flete al cobro total en bolivares del documento
                        lineas.tnetodbs += restaFleteBss    //Adicionando lo cobrado del flete al cobro total en bolivares del documento
                        lineas.bsmtofte =
                            restaFleteBss     //Monto de lo cobrado del flete del documento en bolivares

                        cabecera.dolflete += restaFleteDol    //Adicionando lo cobrado del flete en dolates en la cabecera
                        cabecera.bsflete += restaFleteBss
                    }
                }

            } else { // En el caso de ser N/E solo paga flete
                //IF que valida que lo pagado por el cliente por el flete sea 0, indicando que ya fue pagado
                if (listaDocumentos[0].bsflete - listaDocumentos[0].bsmtofte > 0) {
                    //Calculando el pago del flete en bolivares y dolares, tomando en cuenta si paga o no retencion
                    val restaFleteDol =
                        if (excReten) listaDocumentos[0].dFlete else listaDocumentos[0].dFlete - listaDocumentos[0].cdretflete
                    val restaFleteBss =
                        if (excReten) listaDocumentos[0].bsflete else listaDocumentos[0].bsflete - listaDocumentos[0].cbsretflete

                    //IF que valida en que moneda se paga el documento
                    if (monedaDivisa) { // Caso de que la moneda sea en dolares
                        //Restando FLete en dolares del pago
                        pago -= restaFleteDol

                        lineas.bscobro += restaFleteBss    //Adicionando lo cobrado del flete al cobro total en bolivares del documento
                        lineas.tnetoddol += restaFleteDol    //Adicionando lo cobrado del flete al cobro total en dolares del documento
                        lineas.bsmtofte =
                            restaFleteBss     //Monto de lo cobrado del flete del documento en bolivares

                        cabecera.dolflete += restaFleteDol    //Adicionando lo cobrado del flete en dolates en la cabecera
                        cabecera.bsflete += restaFleteBss

                    } else { // Caso de que la moneda sea en bolivares
                        //Restando FLete en bolivares del pago
                        pago -= restaFleteBss

                        lineas.bscobro += restaFleteBss    //Adicionando lo cobrado del flete al cobro total en bolivares del documento
                        lineas.tnetodbs += restaFleteBss    //Adicionando lo cobrado del flete al cobro total en bolivares del documento
                        lineas.bsmtofte =
                            restaFleteBss     //Monto de lo cobrado del flete del documento en bolivares

                        cabecera.dolflete += restaFleteDol    //Adicionando lo cobrado del flete en dolates en la cabecera
                        cabecera.bsflete += restaFleteBss
                    }
                }
            }

            //Pago del neto
            if (pago > 0.00 && neto > 0.00) {
                if (monedaDivisa) {
                    if (pago >= neto) {
                        lineas.bscobro += neto * listaDocumentos[0].tasadoc
                        lineas.tnetoddol += neto

                        cabecera.dolneto = neto
                        cabecera.bsneto = neto * tasaCambioSeleccionadaPrincipal

                        pago -= neto
                    } else {
                        lineas.bscobro += pago * listaDocumentos[0].tasadoc
                        lineas.tnetoddol += pago

                        cabecera.dolneto = pago
                        cabecera.bsneto = pago * tasaCambioSeleccionadaPrincipal

                        pago -= pago
                    }
                } else {
                    if (pago >= neto) {
                        lineas.bscobro += (neto / tasaCambioSeleccionadaPrincipal) * listaDocumentos[0].tasadoc //Se maneja asi para cambiar los bolivares calculados a la tasa de cuando se paga a la tasa original del documento
                        lineas.tnetodbs += neto

                        //OOOOOOOOOOOOOOOOOOOOOOOJJJJJJJJJJJJJJJJJJJJOOOOOOOOOOOOOOOOOOOOOOOOOO
                        //DEBERIA SER BSNETO
                        cabecera.dolneto = (neto / tasaCambioSeleccionadaPrincipal)
                        cabecera.bsneto = neto

                        pago -= neto
                    } else {
                        lineas.bscobro += (pago / tasaCambioSeleccionadaPrincipal) * listaDocumentos[0].tasadoc //Se maneja asi para cambiar los bolivares calculados a la tasa de cuando se paga a la tasa original del documento
                        lineas.tnetodbs += pago

                        cabecera.dolneto = (pago / tasaCambioSeleccionadaPrincipal)
                        cabecera.bsneto = pago

                        pago -= pago
                    }
                }
            }

            cabecera.bstotal = (cabecera.bsiva + cabecera.bsflete + cabecera.bsneto)
            cabecera.doltotal = (cabecera.doliva + cabecera.dolflete + cabecera.dolneto)

            cabecera.netocob = if (monedaDivisa) {
                cabecera.doltotal - cabecera.dolflete - cabecera.doliva
            } else {
                cabecera.bstotal - cabecera.bsflete - cabecera.bsiva
            }

        }
    }*/

    /*private fun validacionesComplemento(
        pagoCom: Double,
        bancoRefCom: String,
        bancoCom: String,
        transferencia: Boolean,
        efectivoCom: Boolean,
        montoMinimo: Double,
        pago: Double,
        monedaDivisaCom: Boolean
    ): Boolean {
        if (pagoCom.equals(null) || pagoCom == 0.00) {
            Toast.makeText(
                this,
                "Ingrese un valor valido en el monto a registrar en complemento",
                Toast.LENGTH_SHORT
            ).show()
            return true
        }

        if ((bancoRefCom == "") && transferencia) {
            Toast.makeText(
                this, "Debe ingresar una referencia bancaria en complemento", Toast.LENGTH_SHORT
            ).show()
            return true
        }

        if (bancoCom == "" && transferencia) {
            Toast.makeText(this, "Debe serleccionar un banco en complemento", Toast.LENGTH_SHORT)
                .show()
            return true
        }

        //If que valida que si la cobranza es en tranferencia no se repita el banco y la referencia (para complemento)
        if (!efectivoCom) {
            val cursor = keAndroid.rawQuery(
                "SELECT COUNT(*) FROM ke_precobranza WHERE " + "bcoref = '${bancoRefCom.uppercase()}' AND " + "bcoref != '' AND bcocod = '$codigoBancoComplemento';",
                null
            )
            if (cursor.moveToFirst()) {
                val resultEncontrado = cursor.getInt(0)
                if (resultEncontrado > 0) {
                    Toast.makeText(
                        this,
                        "Ya se realizó un pago con este banco y esta referencia en complemento",
                        Toast.LENGTH_SHORT
                    ).show()
                    return true
                }
            }
            cursor.close()
        }

        if (montoMinimo > (pago + pagoCom)) {
            Toast.makeText(
                this, "El monto que registra es insuficiente para el pago", Toast.LENGTH_SHORT
            ).show()
            return true
        }

        val diferencia =
            if (monedaDivisaCom) true else ((montoMinimo * APP_PORCENTAJE_COMPLEMENTO) <= pago)

        //si la diferencia entre el monto reportado y el monto minimo es mayor a un 10%, no se permite el complemento
        if (!diferencia) {
            Toast.makeText(this, "Monto Excedido para complemento", Toast.LENGTH_SHORT).show()
            return true
        }

        return false
    }*/

    /*private fun validacionesPrincipales(
        fechaRegistro: String,
        pago: Double,
        bancoRef: String,
        banco: String,
        transferencia: Boolean,
        efectivo: Boolean
    ): Boolean {
        if (fechaRegistro == "") {
            Toast.makeText(this, "Seleccione una fecha", Toast.LENGTH_SHORT).show()
            return true
        }

        if (pago == 0.00) {
            Toast.makeText(
                this, "Ingrese un valor valido en el monto a registrar", Toast.LENGTH_SHORT
            ).show()
            return true
        }

        if ((bancoRef == "") && transferencia) {
            Toast.makeText(this, "Debe ingresar una referencia bancaria", Toast.LENGTH_SHORT).show()
            return true
        }

        if (banco == "" && transferencia) {
            Toast.makeText(this, "Debe seleccionar un banco", Toast.LENGTH_SHORT).show()
            return true
        }

        //If que valida que si la cobranza es en tranferencia no se repita el banco y la referencia
        if (!efectivo) {
            val cursor = keAndroid.rawQuery(
                "SELECT COUNT(*) FROM ke_precobranza WHERE bcoref = '${
                    binding.etCxcRefMain.text.toString().uppercase()
                }' AND bcoref != '' AND bcocod = '$codigoBancoCompleto';", null
            )
            if (cursor.moveToFirst()) {
                val resultEncontrado = cursor.getInt(0)
                if (resultEncontrado > 0) {
                    Toast.makeText(
                        this,
                        "Ya se realizó un pago con este banco y esta referencia",
                        Toast.LENGTH_SHORT
                    ).show()
                    return true
                }
            }
            cursor.close()
        }
        return false
    }*/

    private fun verificarSiHayDescuentos(documento: String): Boolean {
        val cursorDesc: Cursor = keAndroid.rawQuery(
            "SELECT edodcto FROM ke_precobdcto WHERE documento = '${documento}'", null
        )
        return if (cursorDesc.count > 0) {
            cursorDesc.close()
            true
        } else {
            cursorDesc.close()
            false
        }
    }

    private fun cargarBancosCom(monedaSelec: String) {

        listaInfoBancosCm.clear()
        listaBancosCm.clear()

        keAndroid = conn.writableDatabase
        var bancos: Bancos
        var moneda = 0.00

        if (monedaSelec == "USD") {
            moneda = 2.00

        } else if (monedaSelec == "BSS") {
            moneda = 1.00
        }

        val cursorBancos: Cursor = keAndroid.rawQuery(
            "SELECT DISTINCT codbanco, nombanco,cuentanac, inactiva, fechamodifi FROM listbanc " + "WHERE inactiva = 0 AND cuentanac = $moneda",
            null
        )
        while (cursorBancos.moveToNext()) {
            bancos = Bancos()
            bancos.codbanco = cursorBancos.getString(0)
            bancos.nombanco = cursorBancos.getString(1)
            bancos.cuentanac = cursorBancos.getDouble(2)
            bancos.inactiva = cursorBancos.getDouble(3)
            bancos.fechamodifi = cursorBancos.getString(4)
            listaBancosCm.add(bancos)

        }
        binding.spCxcBancoCom.setText("Seleccione un banco...")
        actualizarBancosCm()
        val adapterBancos: ArrayAdapter<CharSequence> =
            ArrayAdapter(this, R.layout.spinner_bancos, listaInfoBancosCm as List<CharSequence>)
        binding.spCxcBancoCom.setAdapter(adapterBancos)
        adapterBancos.notifyDataSetChanged()

        cursorBancos.close()

    }

    //Funcion que actua en cada evento que se realice para saber si se excluyen las retenciones o no
    private fun retenciones() {
        if (binding.cbExcReten.isChecked) {

            if (binding.rbCxcBssMain.isChecked) {
                cargarSaldos("BSS", listaDocsSeleccionados, false)
                recalcularComplemento()
                pagaRetenciones = false

            } else if (binding.rbCxcDivisasMain.isChecked) {
                cargarSaldos("USD", listaDocsSeleccionados, false)
                recalcularComplemento()
                pagaRetenciones = false
            }

            val nightModeFlags: Int =
                this.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

            if (nightModeFlags == Configuration.UI_MODE_NIGHT_NO) {
                binding.tvCxcReten.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.blackColor1
                    )
                )
            } else {
                binding.tvCxcReten.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.whiteColor4
                    )
                )
            }

        } else {

            if (binding.rbCxcBssMain.isChecked) {
                cargarSaldos("BSS", listaDocsSeleccionados, true)
                recalcularComplemento()
                pagaRetenciones = true

            } else if (binding.rbCxcDivisasMain.isChecked) {
                cargarSaldos("USD", listaDocsSeleccionados, true)
                recalcularComplemento()
                pagaRetenciones = true
            }
            binding.tvCxcReten.setTextColor(Color.RED)

        }
    }

    private fun mostrandoComplemento() {
        var montoComple = "0.0"

        if (binding.cbCxcComplemento.isChecked) {
            val montoEscrito: Double =
                if (binding.etCxcMontoMain.text.toString() == "") 0.0 else binding.etCxcMontoMain.text.toString()
                    .toDouble()
            val montoDado: Double =
                if (binding.tvCxcTotal.text.toString() == "") 0.0 else binding.tvCxcTotal.text.toString()
                    .toDouble()


            //var diferencia = false

            val diferencia = if (binding.rbCxcDivisasCom.isChecked) {
                true
            } else {
                ((valorReal(montoDado * APP_PORCENTAJE_COMPLEMENTO) <= montoEscrito))
            }

            if (!diferencia) {
                //TE QUEDASTE VALIDANDO COMPLEMENTO
                binding.tvPrecioMostrarComplemento.text =
                    "Monto Complementario supera el ${Math.round((1 - APP_PORCENTAJE_COMPLEMENTO) * 100)}%"
                binding.tvPrecioMostrarComplemento.setBackgroundColor(Color.RED)
            } else {
                if (binding.rbCxcDivisasMain.isChecked) { //En el caso de que este seleccionado divisas

                    if (binding.rbCxcDivisasCom.isChecked) {
                        //En el caso de que este seleccionado divisas en el prinipal, y divisas en complemento

                        montoComple =
                            "${((montoDado - montoEscrito) * 100.0).roundToInt() / 100.0} $"

                    } else if (binding.rbCxcBssCom.isChecked) {
                        //En el caso de que este seleccionado divisas en el prinipal, y bolivares en complemento

                        montoComple = "${
                            (((((montoDado - montoEscrito) * tasaCambioSeleccionadaPrincipal) + 0.01) * 100.0).roundToInt() / 100.0)
                        } Bs."

                    }

                } else if (binding.rbCxcBssMain.isChecked) { //En el caso de que este seleccionado bolivares

                    if (binding.rbCxcDivisasCom.isChecked) {
                        //En el caso de que este seleccionado bolivares en el prinipal, y divisas en complemento

                        montoComple = "${
                            (((montoDado - montoEscrito) / tasaCambioSeleccionadaPrincipal) * 100.0).roundToInt() / 100.0
                        } $"

                    } else if (binding.rbCxcBssCom.isChecked) {
                        //En el caso de que este seleccionado bolivvares en el prinipal, y bolivares en complemento

                        montoComple =
                            "${((montoDado - montoEscrito) * 100.0).roundToInt() / 100.0} Bs."

                    }

                }

                binding.tvPrecioMostrarComplemento.text = "Monto a pagar: $montoComple"

                binding.tvPrecioMostrarComplemento.setBackgroundColor(
                    binding.tvPrecioMostrarComplemento.colorLabelAgencia(
                        Constantes.AGENCIA
                    )
                )


            }


        }


    }

    private fun cargarDetalleDescuentos() {
        val listaDesc: ArrayList<Descuentos> = listaDescuentos

        /*for (i in listaDesc.indices){
            println("${listaDesc[i].nrodoc}")
        }*/

        val dialog = DialogDescuento()
        dialog.DialogDescuento(this, listaDesc)
    }

    //  -- esto va a una funcion
    private fun recalcularComplemento() {
        val montototal = binding.tvCxcTotal.text.toString().toDouble()
        var montoReciboCom: Double
        //  -- esto va a una funcion
        if (binding.rbCxcDivisasMain.isChecked) {
            //si el monto es en dolares, saco el 10% del mismo
            montoReciboCom = montototal * valorReal(1 - APP_PORCENTAJE_COMPLEMENTO)
            montoReciboCom = valorReal(montoReciboCom)

            if (binding.rbCxcDivisasCom.isChecked) {
                binding.etCxcMontoCom.hint = ""
                binding.etCxcMontoCom.hint = "Monto máximo: $montoReciboCom"
                //binding.etCxcMontoCom.hint = "Monto Complementario"
            } else {
                montoReciboCom = valorReal(montoReciboCom * tasaCambioSeleccionadaPrincipal)
                binding.etCxcMontoCom.hint = ""
                binding.etCxcMontoCom.hint = "Monto máximo: $montoReciboCom"
                //binding.etCxcMontoCom.hint = "Monto Complementario"
            }

        } else {
            //si el monto es en bss, saco el 10% del mismo
            montoReciboCom = montototal * valorReal(1 - APP_PORCENTAJE_COMPLEMENTO)
            montoReciboCom = valorReal(montoReciboCom)

            if (binding.rbCxcDivisasCom.isChecked) {
                binding.etCxcMontoCom.hint = ""
                montoReciboCom /= tasaCambioSeleccionadaPrincipal
                montoReciboCom = valorReal(montoReciboCom)
                binding.etCxcMontoCom.hint = "Monto máximo: $montoReciboCom"
                //binding.etCxcMontoCom.hint = "Monto Complementario"
            } else {

                binding.etCxcMontoCom.hint = ""
                binding.etCxcMontoCom.hint = "Monto máximo: $montoReciboCom"
                //binding.etCxcMontoCom.hint = "Monto Complementario"
            }
        }
    }

    private fun procesamientodeDatos() {
        var netocobrado: Double
        //Guardando en una variable si exluyo la retencion
        val retennn = if (binding.cbExcReten.isChecked) 1 else 0
        //val diferencialCambiario = if (binding.cbDocDifCambio.isChecked) 1 else 0

        /*if (existReten(listaDocumentos)){
            return
        }*/

        //Ventana emergente que le indica al vendedor a la hora de precionar el boton que le mande
        // una foto de las retenciones del documento a su analista
        /*if (binding.tvCxcReten.text.toString().toDouble() > 0.00 && !binding.cbExcReten.isChecked){
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Retenciones")
            builder.setMessage("Recuerde subir las imágenes de las retenciones de forma nítida a
            su respectivo analista de cobranza")

            builder.setPositiveButton("OK") { dialog, which ->

            }
            builder.show()

        }*/

        val contadorImg = contadorImagenesRequeridas()

        if (listaImagenes.size < contadorImg) {
            toast("Debe incluir un minimo de $contadorImg imágen(es)")
            return
        }

        //2023-11-01 Validacion para verificar que en el primer pago se anexe la retencion
        //estabas buscando que en el primer pago te den la retencion del iva

        validarReten()
        if (contadorRetenIVA != contadorDoc) {
            toast("Falta retención del IVA")
            return
        }
        if ((contadorRetenFlete < contadorDoc) && (contadorRetenFlete > 0)) {
            toast("Falta retención del Flete")
            return
        }
        if ((contadorRetenParme < contadorDoc) && (contadorRetenParme > 0)) {
            toast("Falta retención de Parme")
            return
        }

        //Ventana emergente que le indica al vendedor que se comunique con su analista debido a que
        // la suma de las retenciones da en negativo
        if (binding.tvCxcReten.text.toString().toDouble() < 0.00) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(Html.fromHtml("<font color='#FF0000'>Advertencia</font>"))
            builder.setMessage(
                Html.fromHtml(
                    "<font color='#FF0000'>Comuníquese con su Analista de " + "Cobranza para tratar este caso<br>Causa: Retención en negativo</font>"
                )
            )

            builder.setPositiveButton("OK") { dialog, which ->

            }
            builder.show()

        }

        //If para validar que se selecciono la fecha
        if (fechaQuery == "") {
            Toast.makeText(this, "Debe seleccionar una fecha de pago", Toast.LENGTH_SHORT)
                .show()
            return
        }

        //2023-06-21 se cambio retenciones para otro modulo
        //2023-11-02 se comento debido a que la retencionde iva es obligatoria
        if (!binding.cbExcReten.isChecked && listaRetGuardada.size < 1 && binding.tvCxcReten.text.toString()
                .toDouble() > 0
        ) {
            Toast.makeText(
                this,
                "Debe agregar los comprobantes de retención",
                Toast.LENGTH_SHORT
            )
                .show()
            return
        }
        //Guarda si el boton de efectivo principal esta marcado
        val ignore: Boolean = binding.rbCxcEfectivoMain.isChecked
        //Guarda si el boton de efectivo complementario esta marcado
        val ignorecm: Boolean = binding.rbCxcEfectivoCom.isChecked

        //If que verifica si el ID del banco esta vacio
        if (codigoBancoCompleto == "" && !ignore) {
            //Si ignore es true significa que se pago en efectivo y continua el proceso con normalidad
            Toast.makeText(this, "Debes Seleccionar un banco", Toast.LENGTH_SHORT).show()
            return
        }

        //Valida que la cantidad introducida para la transaccion sea mayor a 0
        if (binding.etCxcMontoMain.text.toString() == "" || binding.etCxcMontoMain.toString()
                .equals(null) || binding.etCxcMontoMain.text.toString().toDouble() == 0.00
        ) {
            Toast.makeText(this, "Debes introducir una cantidad mayor a 0", Toast.LENGTH_SHORT)
                .show()
            return
        }
        //Valida que lo puesto en la referencia bancaria no este en blanco
        if ((binding.etCxcRefMain.text.toString() == "" || binding.etCxcRefMain.text.toString()
                .equals(null)) && !ignore
        ) {
            //Si ignore es false significa que no se ha puesto en banco para terminar la transaccion
            Toast.makeText(
                this, "Debes introducir la referencia bancaria principal", Toast.LENGTH_SHORT
            ).show()
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
        if (!ignore) {
            numVerificador += verificacionReferencia(
                binding.etCxcRefMain.text.toString().uppercase(),
                "ke_precobranza", codigoBancoCompleto
            )
            numVerificador += verificacionReferencia(
                binding.etCxcRefMain.text.toString().uppercase(),
                "ke_referencias",
                codigoBancoCompleto
            )/*val cursor = ke_android.rawQuery("SELECT COUNT(*) FROM ke_precobranza WHERE bcoref =
            '${binding.etCxcRefMain.text.toString().uppercase()}' AND bcoref != '' AND bcocod =
            '$codigoBancoCompleto';", null)

            if(cursor.moveToFirst()){
                val resultEncontrado = cursor.getInt(0)
                if(resultEncontrado > 0){
                    Toast.makeText(this, "Ya se realizó un pago con este banco y esta referencia",
                    Toast.LENGTH_SHORT).show()
                    return
                }
            }
            cursor.close()*/
        }
        //If que valida que si la cobranza es en tranferencia no se repita el banco y la referencia (para complemento)
        if (!ignorecm && binding.cbCxcComplemento.isChecked) {
            numVerificadorComple += verificacionReferencia(
                binding.etCxcRefCom.text.toString()
                    .uppercase(), "ke_precobranza", codigoBancoComplemento
            )
            numVerificadorComple += verificacionReferencia(
                binding.etCxcRefCom.text.toString().uppercase(),
                "ke_referencias",
                codigoBancoComplemento
            )/*val cursor = ke_android.rawQuery("SELECT COUNT(*) FROM ke_precobranza WHERE bcoref =
            '${binding.etCxcRefCom.text.toString().uppercase()}' AND bcoref != '' AND bcocod =
            '$codigoBancoComplemento';", null)
            if(cursor.moveToFirst()){
                val resultEncontrado = cursor.getInt(0)
                if(resultEncontrado > 0){
                    Toast.makeText(this, "Ya se realizó un pago con este banco y esta referencia",
                    Toast.LENGTH_SHORT).show()
                    return
                }
            }
            cursor.close()*/
        }

        if (numVerificador != 0) {
            Toast.makeText(
                this,
                "Pago Principal: Referencia y banco utilizados previamente.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        if (numVerificadorComple != 0) {
            Toast.makeText(
                this,
                "Pago Complemento: Referencia y banco utilizados previamente.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        referenciaPrincipal = binding.etCxcRefMain.text.toString().trim().uppercase()
        montoRec = binding.etCxcMontoMain.text.toString().toDouble()

        if (binding.rbCxcAbonoMain.isChecked && montoRec > binding.tvCxcTotal.text.toString()
                .toDouble()
        ) {
            Toast.makeText(
                this, "El Abono no debe ser mayor al total.", Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (binding.rbCxcCompMain.isChecked &&
            superSaldoFavor() > conn.getConfigNum("APP_LIMITE_SALDO_FAVOR", codEmpresa!!) &&
            binding.rbCxcDivisasMain.isChecked
        ) {
            Toast.makeText(
                this,
                "El monto a favor no puede exceder de ${conn.getConfigNum("APP_LIMITE_SALDO_FAVOR", codEmpresa!!)}.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }


        //declaro variables auxiliares
        val ivaTot = binding.tvCxcIva.text.toString().toDouble()
        val fleteTot = binding.tvCxcFlete.text.toString().toDouble()
        //2023-06-07 Nueva variable para llevar el total de las retenciones para ser restada con iva
        // y flete para que el abono sea pagado sin retenciones
        val retTot = binding.tvCxcReten.text.toString().toDouble()
        /*val retTot = if (binding.cbExcReten.isChecked) {
            var guardar = 0.0
            listaDocumentos.forEach { documento ->
                val dretencionFlete = documento.dretencion - documento.dretencioniva
                guardar += documento.cdretencion - (documento.cdretencioniva - documento.dretencioniva) - (documento.cdretflete - dretencionFlete)
                if(guardar < 0){
                    guardar = 0.0
                }
            }
            guardar
        } else {
            binding.tvCxcReten.text.toString().toDouble()
        }*/
        // <----------------------- matar retencion TODO

        //dependiendo del tipo de pago, elijo ya sea monto minimo total o si es abono, la
        //cantidad minima que seria flete + iva (de estar pagos, asumo un valor de 1)

        if (listaDocumentos.size > 1) {

            binding.rbCxcAbonoMain.visibility = View.INVISIBLE
            binding.rbCxcAbonoMain.isChecked = false

            if (binding.rbCxcCompMain.isChecked) {
                montoMinimoRec = binding.tvCxcTotal.text.toString().toDouble() - 0.5

            }
            // Lo que el cliente da es menor al total y complemento NO esta marcado
            if (montoRec < montoMinimoRec && !binding.cbCxcComplemento.isChecked) {//PUEDE ENTRAR AQUI EN ALGUN CASO?
                //Toast.makeText(this, "El monto del recibo no debe ser menor a la suma del IVA y el
                // Flete", Toast.LENGTH_SHORT).show()
                if (binding.rbCxcCompMain.isChecked) {
                    Toast.makeText(
                        this,
                        "El monto del recibo no debe ser menor al Total",
                        Toast.LENGTH_SHORT
                    ).show()

                } else if (binding.rbCxcAbonoMain.isChecked) {
                    Toast.makeText(
                        this,
                        "El monto del recibo no debe ser menor a la suma del IVA y el Flete",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            // Lo que el cliente da es menor al total y complemento SI esta marcado
            // 2023-07-06 se coloco un or que suma la cantidad previamente restada a la variable
            // montoMinimoRec debido a un caso donde no entre a ningun if
            // esto es debido a que si se paga un precio cercano al precio total del documento pero
            // ligeramente menor no entra a ningun condicional ejemplo
            // total = 92.25
            // ingresado = 90
            // total restado 8.75 <-- (montoMinimoRec)
            // REDUNDANTE
            else if ((montoRec < montoMinimoRec && binding.cbCxcComplemento.isChecked) || (montoRec < (montoMinimoRec + 0.5) && binding.cbCxcComplemento.isChecked)) {
                //si el monto del recibo es menor al monto minimo pero el complemento esta marcado
                //var diferencia = ((montoMinimoRec / montoRec) * 100) - 100
                // 2023-03-29 se comenta por la necesidad de que al seleccionar divisa estas no se
                // les sera aplicada el 10%
                //val diferencia = (valorReal(montoMinimoRec * 0.9) <= montoRec)
                //println(diferencia)

                val diferencia = if (binding.rbCxcDivisasCom.isChecked) {
                    true
                } else {
                    ((valorReal(montoMinimoRec * APP_PORCENTAJE_COMPLEMENTO) <= montoRec))
                }

                //si la diferencia entre el monto reportado y el monto minimo es mayor a un 10%, no
                // se permite el complemento
                if (!diferencia) {
                    Toast.makeText(this, "Monto Excedido para complemento", Toast.LENGTH_SHORT)
                        .show()
                    return

                    //de manera contraria, si
                } else if (diferencia) {

                    //valido montos del complemento
                    if (binding.etCxcMontoCom.text.toString() == "" || binding.etCxcMontoCom.text.toString()
                            .equals(null)
                    ) {

                        Toast.makeText(
                            this,
                            "Monto de complemento no puede estar vacío",
                            Toast.LENGTH_SHORT
                        ).show()
                        return

                    }

                    //valido montos en 0 y banco vacio del complemento, asi como tambien
                    //la referencia.
                    if (binding.etCxcRefCom.text.toString() == "" || binding.etCxcRefCom.text.toString()
                            .equals(null)
                    ) {
                        if (ignorecm) {

                        } else {
                            Toast.makeText(
                                this,
                                "Referencia del complemento no puede estar vacía",
                                Toast.LENGTH_SHORT
                            ).show()
                            return
                        }

                    }
                    if (codigoBancoComplemento == "") {
                        if (ignorecm) {

                        } else {
                            Toast.makeText(
                                this,
                                "Debes seleccionar un banco para el complemento",
                                Toast.LENGTH_SHORT
                            ).show()
                            return
                        }


                    }
                    var montoRecComp = binding.etCxcMontoCom.text.toString().toDouble()

                    val complementoMontoStandard =
                        if (binding.rbCxcBssCom.isChecked) { // if que valida que moneda se seleccionó en complemento
                            // Si se selecciona bolivares hara la conversion a dolares
                            (montoRecComp / tasaCambioSeleccionadaPrincipal)
                        } else {
                            // Si se selecciona dolares la moneda permanecera y se guardara en otra variable
                            montoRecComp
                        }

                    val montoPrinciStandard =
                        if (binding.rbCxcBssMain.isChecked) {// if que valida que moneda se seleccionó en principal
                            // Si se selecciona bolivares hara la conversion a dolares
                            montoRec / tasaCambioSeleccionadaPrincipal
                        } else {
                            // Si se selecciona dolares la moneda permanecera y se guardara en otra variable
                            montoRec
                        }

                    val montoComprar =
                        if (binding.rbCxcBssMain.isChecked) {// if que valida que moneda se seleccionó en principal
                            // Si se selecciona bolivares hara la conversion a dolares
                            montoMinimoRec / tasaCambioSeleccionadaPrincipal
                        } else {
                            // Si se selecciona dolares la moneda permanecera y se guardara en otra variable
                            montoMinimoRec
                        }
                    //Posiuble ERROR encontrado, no se transforma la moneda
                    if ((complementoMontoStandard + montoPrinciStandard) < montoComprar) {
                        Toast.makeText(
                            this,
                            "Montos insuficientes para completar ambos recibos 1",
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    } else {

                        referenciaCm = binding.etCxcRefCom.text.toString().trim().uppercase()
                        if (referenciaCm == "" && !ignorecm) {
                            Toast.makeText(
                                this,
                                "Falta la referencia bancaria de complemento",
                                Toast.LENGTH_SHORT
                            ).show()
                            return
                        }

                        var llCommit: Boolean
                        keAndroid = conn.writableDatabase

                        //listas con el tipo de datos para los recibos
                        val listaReciboPrCabecera: ArrayList<CXC> = ArrayList()
                        val listaReciboPrLineas: ArrayList<CXC> = ArrayList()
                        val listaReciboCmCabecera: ArrayList<CXC> = ArrayList()
                        val listaReciboCmLineas: ArrayList<CXC> = ArrayList()

                        listaDocumentos.sortBy { it.fechaDocs }//llenado de datos de cabecera
                        val cxc = CXC()
                        cxc.id_recibo = nroPrecobranza
                        cxc.tipoRecibo = "W"
                        cxc.codigoVend = codUsuario.toString()
                        cxc.kecxc_id = tasaId
                        cxc.tasadia = tasaCambioSeleccionadaPrincipal
                        cxc.fchrecibo = fechatasaH
                        cxc.clicontesp = "" //esto lo jalo de  la lista de docs?
                        cxc.moneda = monedaSeleccionadaPr
                        if (ignore) {
                            cxc.bcocod = ""
                            cxc.bcoref = ""
                            cxc.efectivo = montoRec
                            cxc.fchvigen = fechaSuma(fechaActual, 60)
                        } else {
                            cxc.bcocod = codigoBancoCompleto
                            cxc.bcomonto = montoRec
                            cxc.bcoref = referenciaPrincipal
                            cxc.fchvigen = fechaSuma(fechaActual, 5)
                        }
                        cxc.edorec = "0"
                        cxc.fchhr = fechaActual

                        //genero cabecera del complemento
                        val comp = CXC()
                        comp.id_recibo = nroComplemento
                        comp.tipoRecibo = "W"
                        comp.codigoVend = codUsuario.toString()
                        comp.kecxc_id = tasaId
                        comp.tasadia = tasaCambioSeleccionadaPrincipal
                        comp.fchrecibo = fechatasaH
                        comp.clicontesp = "" //esto lo jalo de  la lista de docs?
                        comp.moneda = monedaSeleccionadaCm

                        if (ignorecm) {
                            comp.bcocod = ""
                            comp.bcoref = ""
                            comp.efectivo = montoRecComp
                            comp.fchvigen = fechaSuma(fechaActual, 60)
                        } else {
                            comp.bcocod = codigoBancoComplemento
                            comp.bcoref = referenciaCm
                            comp.bcomonto = montoRecComp
                            comp.fchvigen = fechaSuma(fechaActual, 5)
                        }

                        comp.edorec = "0"
                        comp.fchhr = fechaActual


                        //en este ciclo lleno la lista de precobradocs
                        for (i in listaDocumentos.indices) {
                            val cxclineas = CXC()

                            cxclineas.id_recibo = nroPrecobranza
                            cxclineas.agencia = listaDocumentos[i].agencia
                            cxclineas.tipodoc = listaDocumentos[i].tipodoc
                            cxclineas.documento = listaDocumentos[i].documento
                            listaReciboPrLineas.add(cxclineas)

                        }

                        //en este ciclo, agrego las retenciones
                        for (i in listaReciboPrLineas.indices) {
                            if (listaReciboPrLineas[i].agencia == "002" && (listaReciboPrLineas[i].documento == listaDocumentos[i].documento)) {
                                listaDocumentos[i].cbsretflete = 0.00

                            }

                            //llenado de campos de retenciones
                            //retenciones fase 1
                            for (j in listaRetGuardada.indices) {
                                if (listaRetGuardada[j].nrodoc == listaReciboPrLineas[i].documento) {
                                    if (listaRetGuardada[j].tiporet == "iva") {
                                        //si es de iva
                                        listaReciboPrLineas[i].nroret =
                                            listaRetGuardada[j].nroret
                                        listaReciboPrLineas[i].fchemiret =
                                            listaRetGuardada[j].fecharet
                                        listaReciboPrLineas[i].bsretiva =
                                            -listaRetGuardada[j].montoret
                                        listaReciboPrLineas[i].refret =
                                            listaRetGuardada[j].refret

                                    }

                                    if (listaRetGuardada[j].tiporet == "flete") {
                                        //si es de flete
                                        listaReciboPrLineas[i].nroretfte =
                                            listaRetGuardada[j].nroret
                                        listaReciboPrLineas[i].fchemirfte =
                                            listaRetGuardada[j].fecharet
                                        listaReciboPrLineas[i].bsretfte =
                                            -listaRetGuardada[j].montoret
                                        listaReciboPrLineas[i].refretfte =
                                            listaRetGuardada[j].refret
                                    }

                                    if (listaRetGuardada[j].tiporet == "parme") {
                                        //si es de parme
                                        listaReciboPrLineas[i].retmun_nro =
                                            listaRetGuardada[j].nroret
                                        listaReciboPrLineas[i].retmun_fch =
                                            listaRetGuardada[j].fecharet
                                        listaReciboPrLineas[i].retmun_mto =
                                            -listaRetGuardada[j].montoret
                                        listaReciboPrLineas[i].retmun_cod =
                                            listaRetGuardada[j].refret
                                    }
                                }
                            }


                            //descuento ivas y fletes ( de haberlos)
                            if (listaReciboPrLineas[i].documento == listaDocumentos[i].documento && listaDocumentos[i].tipodocv == "FAC") {

                                if (listaDocumentos[i].bsiva - listaDocumentos[i].bsmtoiva <= 0.00) {
                                    listaReciboPrLineas[i].bsmtoiva = 0.00
                                    listaReciboPrLineas[i].doliva = 0.00

                                } else {


                                    //descuento ivas del monto del recibo original .-
                                    val restaIvadol =
                                        listaDocumentos[i].dtotimpuest - listaDocumentos[i].cdretencioniva
                                    //2023-04-03 comentario por no tener en cuenta cuando se escluyen retenciones
                                    //var restaIvadol = listaDocumentos[i].dtotimpuest - listaDocumentos[i].cdretencioniva
                                    val restaIvabss =
                                        listaDocumentos[i].bsiva - listaDocumentos[i].cbsretencioniva
                                    //var restaIvabss = listaDocumentos[i].bsiva - listaDocumentos[i].cbsretencioniva

                                    if (binding.rbCxcDivisasMain.isChecked) {
                                        //hago el descuento del iva del nmonto de pago
                                        montoRec = (montoRec - restaIvadol).valorReal()

                                        if (montoRec < 0.00) {
                                            Toast.makeText(
                                                this, "Monto insuficiente", Toast.LENGTH_SHORT
                                            ).show()
                                            return
                                        }

                                        listaReciboPrLineas[i].bscobro += (listaDocumentos[i].bsiva - listaDocumentos[i].cbsretencioniva)
                                        listaReciboPrLineas[i].bsmtoiva =
                                            listaDocumentos[i].bsiva
                                        listaReciboPrLineas[i].doliva =
                                            listaDocumentos[i].dtotimpuest - listaDocumentos[i].cdretencioniva
                                        //2023-04-03 comentado por mal calculo?
                                        //listaReciboPrLineas[i].tnetoddol  += listaReciboPrLineas[i].doliva
                                        listaReciboPrLineas[i].tnetoddol += (listaDocumentos[i].dtotimpuest - listaDocumentos[i].cdretencioniva)

                                    } else {
                                        montoRec =
                                            (montoRec - restaIvabss).valorReal()//320$ //121.53Bs.
                                        if (montoRec < 0.00) {
                                            Toast.makeText(
                                                this, "Monto insuficiente", Toast.LENGTH_SHORT
                                            ).show()
                                            return
                                        }
                                        listaReciboPrLineas[i].bscobro += (listaDocumentos[i].bsiva - listaDocumentos[i].cbsretencioniva)
                                        listaReciboPrLineas[i].bsmtoiva =
                                            listaDocumentos[i].bsiva
                                        listaReciboPrLineas[i].doliva =
                                            listaDocumentos[i].dtotimpuest - listaDocumentos[i].cdretencioniva
                                        listaReciboPrLineas[i].tnetodbs += (listaDocumentos[i].bsiva - listaDocumentos[i].cbsretencioniva)
                                        //listaReciboPrLineas[i].tnetodbs += listaDocumentos[i].bsiva

                                    }

                                }

                                //descuento del flete de los documentos
                                if (listaDocumentos[i].bsflete - listaDocumentos[i].bsmtofte <= 1) {
                                    listaReciboPrLineas[i].bsmtofte = 0.00
                                    listaReciboPrLineas[i].dolflete = 0.00

                                } else {

                                    if (binding.rbCxcDivisasMain.isChecked) {

                                        //si aqui ya llega el monto en 0 o menos
                                        if (montoRec < 0.00) {
                                            Toast.makeText(
                                                this, "Monto insuficiente", Toast.LENGTH_SHORT
                                            ).show()
                                            return
                                        }
                                        //descuento del monto del recibo, el flete
                                        montoRec -= if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dFlete - listaDocumentos[i].cdretflete

                                        //de no llegar a menos de cero, lo agrego al monto cobrado en bss
                                        if (binding.cbExcReten.isChecked) {
                                            listaReciboPrLineas[i].bscobro += listaDocumentos[i].bsflete
                                        } else {
                                            listaReciboPrLineas[i].bscobro += listaDocumentos[i].bsflete - listaDocumentos[i].cbsretflete
                                        }
                                        //2023-04-04 Repeticion inecesaria
                                        //listaReciboPrLineas[i].bscobro  += valorReal((listaDocumentos[i].bsflete - listaDocumentos[i].cbsretflete))
                                        listaReciboPrLineas[i].bsmtofte =
                                            listaDocumentos[i].bsflete
                                        listaReciboPrLineas[i].dolflete =
                                            (if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dFlete - listaDocumentos[i].cdretflete)
//2023-04-04 Actualizado para incluir excluir retenciones
                                        /*if(binding.rbCxcDivisasMain.isChecked == true){
                                            listaReciboPrLineas[i].tnetoddol  += listaDocumentos[i].dFlete
                                        }else{
                                            listaReciboPrLineas[i].tnetodbs += listaDocumentos[i].bsflete

                                        }*/

                                        if (binding.rbCxcDivisasMain.isChecked) {
                                            listaReciboPrLineas[i].tnetoddol += (if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dFlete - listaDocumentos[i].cdretflete)
                                        } else {
                                            listaReciboPrLineas[i].tnetodbs += (if (binding.cbExcReten.isChecked) listaDocumentos[i].bsflete else listaDocumentos[i].bsflete - listaDocumentos[i].cbsretflete)
                                            //listaReciboPrLineas[i].tnetodbs += listaDocumentos[i].bsiva
                                        }
                                        println("")


                                    } else {

                                        //si aqui ya llega el monto en 0 o menos
                                        if (montoRec < 0.00) {
                                            Toast.makeText(
                                                this, "Monto insuficiente", Toast.LENGTH_SHORT
                                            ).show()
                                            return
                                        }
                                        //descuento del monto del recibo, el flete
                                        montoRec -= if (binding.cbExcReten.isChecked) listaDocumentos[i].bsflete else listaDocumentos[i].bsflete - listaDocumentos[i].cbsretflete

                                        //de no llegar a menos de cero, lo agrego al monto cobrado en bss
                                        //2023-0404 Repeticion inecesaria
                                        //listaReciboPrLineas[i].bscobro  += valorReal((listaDocumentos[i].bsflete - listaDocumentos[i].cbsretflete))
                                        if (binding.cbExcReten.isChecked) {
                                            listaReciboPrLineas[i].bscobro += listaDocumentos[i].bsflete
                                        } else {
                                            listaReciboPrLineas[i].bscobro += listaDocumentos[i].bsflete - listaDocumentos[i].cbsretflete
                                        }
                                        listaReciboPrLineas[i].dolflete =
                                            if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dFlete - listaDocumentos[i].cdretflete

                                        listaReciboPrLineas[i].bsmtofte =
                                            listaDocumentos[i].bsflete

                                        if (binding.rbCxcDivisasMain.isChecked) {
                                            listaReciboPrLineas[i].tnetoddol += (if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dtotimpuest - listaDocumentos[i].cdretflete)
                                        } else {
                                            listaReciboPrLineas[i].tnetodbs += (if (binding.cbExcReten.isChecked) listaDocumentos[i].bsflete else listaDocumentos[i].bsflete - listaDocumentos[i].cbsretflete)
                                            //listaReciboPrLineas[i].tnetodbs += listaDocumentos[i].bsiva
                                            println("")
                                        }

                                    }

                                }

                            } else {
                                if (listaDocumentos[i].bsflete - listaDocumentos[i].bsmtofte <= 1) {
                                    listaReciboPrLineas[i].bsmtofte = 0.00
                                    listaReciboPrLineas[i].dolflete = 0.00
                                } else {

                                    val fleteaCobrar =
                                        if (binding.cbExcReten.isChecked) listaDocumentos[i].bsflete else listaDocumentos[i].bsflete - listaDocumentos[i].cbsretflete
                                    listaReciboPrLineas[i].bscobro += fleteaCobrar

                                    if (binding.rbCxcDivisasMain.isChecked) {
                                        montoRec -= if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dFlete - listaDocumentos[i].cdretflete
                                        listaReciboPrLineas[i].bsmtofte =
                                            listaDocumentos[i].bsflete
                                        listaReciboPrLineas[i].dolflete =
                                            if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dFlete - listaDocumentos[i].cdretflete
                                        listaReciboPrLineas[i].tnetoddol += if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dFlete - listaDocumentos[i].cdretflete
                                    } else {
                                        montoRec -= if (binding.cbExcReten.isChecked) listaDocumentos[i].bsflete else listaDocumentos[i].bsflete - listaDocumentos[i].cbsretflete
                                        listaReciboPrLineas[i].bsmtofte =
                                            listaDocumentos[i].bsflete
                                        listaReciboPrLineas[i].dolflete =
                                            if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dFlete - listaDocumentos[i].cdretflete
                                        listaReciboPrLineas[i].tnetodbs += if (binding.cbExcReten.isChecked) listaDocumentos[i].bsflete else listaDocumentos[i].bsflete - listaDocumentos[i].cbsretflete


                                    }

                                }
                            }

                        }

                        //llenado de los netos OJOOOOO
                        for (i in listaReciboPrLineas.indices) {

                            if (montoRec < 1) {
                                // borré el return
                            } else {
                                if (binding.rbCxcDivisasMain.isChecked) {
                                    val netoTV = binding.tvCxcNeto.text.toString().toDouble()

                                    if (netoTV > 0.00) {
                                        val netoRealenDoc = listaDocumentos[i].netorestante

                                        if (montoRec >= netoRealenDoc) {
                                            val bscobrado =
                                                netoRealenDoc * listaDocumentos[i].tasadoc

                                            listaReciboPrLineas[i].bscobro += bscobrado


                                            netocobrado = listaDocumentos[i].netorestante
                                            listaReciboPrLineas[i].dolneto = netocobrado

                                            listaReciboPrLineas[i].tnetoddol += netocobrado
                                            montoRec -= netocobrado
                                            listaReciboPrLineas[i].ispagadoTotal = "1"
                                            //DESCUESTO DEBERIA IR POR AQUI CREO
                                        } else if (netoRealenDoc > montoRec && montoRec > 0) {

                                            val cobroAbono =
                                                montoRec * listaDocumentos[i].tasadoc

                                            listaReciboPrLineas[i].bscobro += cobroAbono
                                            netocobrado = montoRec
                                            listaReciboPrLineas[i].dolneto =
                                                valorReal(netocobrado)
                                            montoRec -= netocobrado
                                            listaReciboPrLineas[i].tnetoddol += netocobrado
                                            montoRec = valorReal(montoRec)

                                            listaReciboPrLineas[i].ispagadoTotal = "0"
                                            //DESCUESTO DEBERIA IR POR AQUI CREO
                                        }

                                    }
                                }

                                if (binding.rbCxcBssMain.isChecked) {
                                    var netoRealenDoc = listaDocumentos[i].netorestante
                                    netoRealenDoc = valorReal(netoRealenDoc)

                                    //si el monto del recibo cubre el monto del documento
                                    if (montoRec > (netoRealenDoc * tasaCambioSeleccionadaPrincipal)) {
                                        val bscobrado =
                                            valorReal(netoRealenDoc * listaDocumentos[i].tasadoc)
                                        listaReciboPrLineas[i].bscobro += bscobrado
                                        netocobrado = listaDocumentos[i].netorestante
                                        listaReciboPrLineas[i].dolneto = netocobrado
                                        listaReciboPrLineas[i].bsneto =
                                            valorReal(netoRealenDoc * tasaCambioSeleccionadaPrincipal)
                                        listaReciboPrLineas[i].tnetodbs += valorReal(
                                            netoRealenDoc * tasaCambioSeleccionadaPrincipal
                                        )
                                        listaReciboPrLineas[i].ispagadoTotal = "1"
                                        montoRec -= valorReal(netoRealenDoc * tasaCambioSeleccionadaPrincipal)
                                        print("")

                                    }
                                    //si el monto del recibo no cubre el monto completo del documento (se comporta como un abono)
                                    else if ((netoRealenDoc * tasaCambioSeleccionadaPrincipal) > montoRec && montoRec > 0) {
                                        netocobrado = montoRec / tasaCambioSeleccionadaPrincipal
                                        val cobroAbono =
                                            netocobrado * listaDocumentos[i].tasadoc
                                        listaReciboPrLineas[i].bscobro += valorReal(cobroAbono)
                                        val dolneto = netocobrado
                                        listaReciboPrLineas[i].dolneto = valorReal(dolneto)
                                        listaReciboPrLineas[i].bsneto = montoRec
                                        listaReciboPrLineas[i].tnetodbs += montoRec
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
                                for (j in listaDescuentos.indices) {
                                    if (listaDescuentos[j].nrodoc == listaDocumentos[i].documento) {
                                        listaReciboPrLineas[i].prcdsctopp =
                                            listaDescuentos[j].pordscto
                                    }
                                }

                            }
                        }

                        //recorrido del complemento
                        //tambien voy a pagar el neto pero con complemento de los documentos ya pagos
                        //las lineas del recibo del complemento
                        //EL PROBLEM ESTA POR AQUI ERROR
                        for (i in listaReciboPrLineas.indices) {
                            //valido si fue pagado completo
                            if (listaReciboPrLineas[i].ispagadoTotal == "1") {


                            } else {
                                //de no estar pago completo, le aplico el complemento
                                val complineas = CXC()
                                complineas.id_recibo = nroComplemento
                                complineas.agencia = listaReciboPrLineas[i].agencia
                                complineas.tipodoc = listaReciboPrLineas[i].tipodoc
                                complineas.documento = listaReciboPrLineas[i].documento
                                listaReciboCmLineas.add(complineas)
                            }

                        }

                        //llenado neto del complemento
                        for (i in listaReciboCmLineas.indices) {

                            if (montoRecComp <= 0) {

                            } else {
                                if (binding.rbCxcDivisasCom.isChecked) {
                                    for (j in listaDocumentos.indices) {
                                        if (listaDocumentos[j].documento == listaReciboCmLineas[i].documento) {
                                            val netoRealenDoc =
                                                listaDocumentos[j].netorestante - listaReciboPrLineas[j].dolneto

                                            if (montoRecComp >= netoRealenDoc) {

                                                val bscobrado =
                                                    netoRealenDoc * listaDocumentos[j].tasadoc

                                                listaReciboCmLineas[i].bscobro += bscobrado


                                                netocobrado =
                                                    listaDocumentos[j].netorestante - listaReciboPrLineas[j].dolneto
                                                listaReciboCmLineas[i].dolneto = netocobrado
                                                montoRecComp -= netocobrado
                                                listaReciboCmLineas[i].tnetoddol += netocobrado

                                            } else if (netoRealenDoc > montoRecComp && montoRecComp > 0) {


                                                val cobroAbono =
                                                    montoRecComp * listaDocumentos[j].tasadoc

                                                listaReciboCmLineas[i].bscobro += cobroAbono
                                                netocobrado = montoRecComp
                                                listaReciboCmLineas[i].dolneto =
                                                    valorReal(netocobrado)
                                                montoRecComp -= netocobrado
                                                montoRecComp = valorReal(montoRecComp)
                                                listaReciboCmLineas[i].tnetoddol += netocobrado

                                            }

                                        } else {
                                            //do nothing papa
                                        }
                                    }

                                }

                                if (binding.rbCxcBssCom.isChecked) {
                                    for (j in listaDocumentos.indices) {
                                        if (listaReciboCmLineas[i].documento == listaDocumentos[j].documento) {
                                            var netoRealenDoc =
                                                listaDocumentos[j].netorestante - listaReciboPrLineas[j].dolneto
                                            netoRealenDoc = valorReal(netoRealenDoc)

                                            //si el monto del recibo cubre el monto del documento
                                            if (montoRecComp >= (netoRealenDoc * tasaCambioSeleccionadaPrincipal)) {
                                                val bscobrado =
                                                    valorReal(netoRealenDoc * listaDocumentos[j].tasadoc)
                                                listaReciboCmLineas[i].bscobro += bscobrado

                                                netocobrado =
                                                    listaDocumentos[j].netorestante - listaReciboPrLineas[j].dolneto
                                                listaReciboCmLineas[i].dolneto = netocobrado
                                                listaReciboCmLineas[i].bsneto =
                                                    valorReal(netoRealenDoc * tasaCambioSeleccionadaPrincipal)
                                                listaReciboCmLineas[i].tnetodbs += valorReal(
                                                    netoRealenDoc * tasaCambioSeleccionadaPrincipal
                                                )
                                                montoRecComp -= valorReal(netoRealenDoc * listaDocumentos[j].tasadoc)
                                            }

                                            //si el monto del recibo no cubre el monto completo del documento (se comporta como un abono)
                                            else if ((netoRealenDoc * tasaCambioSeleccionadaPrincipal) > montoRecComp && montoRecComp > 0) {
                                                val dolneto =
                                                    (montoRecComp / tasaCambioSeleccionadaPrincipal)
                                                val cobroAbono =
                                                    valorReal(dolneto) * listaDocumentos[j].tasadoc
                                                listaReciboCmLineas[i].bscobro += valorReal(
                                                    cobroAbono
                                                )
                                                listaReciboCmLineas[i].dolneto =
                                                    valorReal(dolneto)
                                                listaReciboCmLineas[i].bsneto = montoRecComp
                                                listaReciboCmLineas[i].tnetodbs += montoRecComp
                                                montoRecComp -= montoRecComp
                                            }
                                        }
                                    }
                                }
                            }
                        }


                        /*esto va a ser resultado de la suma de los campos de la lista (ke_precobranza)
                         de detalles */
                        val difReteIva =
                            valorReal(listaReciboPrLineas.sumOf { it.bsmtoiva }) + valorReal(
                                listaReciboPrLineas.sumOf { it.bsretiva })
                        val difRetyFlete =
                            valorReal(listaReciboPrLineas.sumOf { it.bsmtofte }) + valorReal(
                                listaReciboPrLineas.sumOf { it.bsretfte })
                        var netoReal =
                            valorReal(listaReciboPrLineas.sumOf { it.bscobro }) - difReteIva - difRetyFlete //--revisar si esto es necesario
                        cxc.bsneto = valorReal(listaReciboPrLineas.sumOf { it.bsneto })
                        cxc.bsretiva = valorReal(listaReciboPrLineas.sumOf { it.bsretiva })
                        cxc.bsiva = valorReal(listaReciboPrLineas.sumOf { it.bsmtoiva })
                        cxc.bsflete = valorReal(listaReciboPrLineas.sumOf { it.bsmtofte })

                        /* sumo los bss en total y los redondeo al momento de guardarlo */
                        var bssumaTotal = valorReal(listaReciboPrLineas.sumOf { it.bscobro })

                        //cxc.bstotal    = valorReal(bssumaTotal)
                        cxc.bstotal =
                            valorReal(listaReciboPrLineas.sumOf { it.bsneto } + (listaReciboPrLineas.sumOf { it.bsmtoiva } + listaReciboPrLineas.sumOf { it.bsretiva }) + (listaReciboPrLineas.sumOf { it.bsmtofte } + listaReciboPrLineas.sumOf { it.bsretfte }))
                        //cxc.dolneto    = valorReal(listaDocumentos.sumOf{it.dtotneto }) //<-------------------- Revisar si es necesario
                        cxc.dolneto = valorReal(listaReciboPrLineas.sumOf { it.dolneto })
                        cxc.doliva = valorReal(listaReciboPrLineas.sumOf { it.doliva })
                        //cxc.dolretiva  = valorReal(listaDocumentos.sumOf { it.cdretencioniva })
                        cxc.dolflete = valorReal(listaReciboPrLineas.sumOf { it.dolflete })
                        cxc.doltotal =
                            valorReal(listaReciboPrLineas.sumOf { it.dolneto } + listaReciboPrLineas.sumOf { it.doliva } + listaReciboPrLineas.sumOf { it.dolflete })
                        cxc.netocob =
                            binding.etCxcMontoMain.text.toString().ifEmpty { "0.0" }.toDouble()
                        //if (monedaSeleccionadaPr == "2") cxc.doltotal + superSaldoFavor() else cxc.bstotal + valorReal(superSaldoFavor() * tasaCambioSeleccionadaPrincipal)
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
                        cxc.moneda = monedaSeleccionadaPr
                        cxc.tasadia = tasaCambioSeleccionadaPrincipal
                        listaReciboPrCabecera.add(cxc)

                        try {

                            // inicio la transacción
                            keAndroid.beginTransaction()
                            val qcabecera = ContentValues()
                            val qlineas = ContentValues()

                            for (i in listaReciboPrCabecera.indices) {
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

                                if (ignore) {
                                    qcabecera.put("efectivo", listaReciboPrCabecera[i].efectivo)
                                } else {
                                    qcabecera.put("bcocod", listaReciboPrCabecera[i].bcocod)
                                    qcabecera.put("bcomonto", listaReciboPrCabecera[i].bcomonto)
                                    qcabecera.put("bcoref", listaReciboPrCabecera[i].bcoref)
                                }

                                qcabecera.put("edorec", listaReciboPrCabecera[i].edorec)
                                qcabecera.put("fchvigen", listaReciboPrCabecera[i].fchvigen)
                                qcabecera.put("bsretflete", listaReciboPrCabecera[i].bsretflete)
                                qcabecera.put("netocob", listaReciboPrCabecera[i].netocob)
                                qcabecera.put("fechamodifi", getFechaHoy())
                                qcabecera.put(
                                    "tipo_pago",
                                    if (binding.rbCxcAbonoMain.isChecked) 1 else 0
                                )
                                qcabecera.put(
                                    "complemento",
                                    if (binding.cbCxcComplemento.isChecked) nroComplemento else ""
                                )
                                //qcabecera.put("docdifcamb",diferencialCambiario)

                                for (j in listaReciboPrLineas.indices) {
                                    qlineas.put("cxcndoc", listaReciboPrLineas[j].id_recibo)
                                    qlineas.put("agencia", listaReciboPrLineas[j].agencia)
                                    qlineas.put("tipodoc", listaReciboPrLineas[j].tipodoc)
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
                                    qlineas.put("codcliente", listaDocumentos[i].codcliente)
                                    qlineas.put(
                                        "nombrecli",
                                        conn.getCampoStringCamposVarios(
                                            "cliempre",
                                            "nombre",
                                            listOf("codigo", "empresa"),
                                            listOf(listaDocumentos[i].codcliente, codEmpresa!!)
                                        )
                                    )
                                    qlineas.put("tasadoc", listaDocumentos[j].tasadoc)
                                    //2023-10-19 esto es mal pero debido a la falta de tiempo va asi
                                    qlineas.put(
                                        "cbsretiva",
                                        conn.getCampoDoubleCamposVarios(
                                            "ke_doccti",
                                            "cbsretiva",
                                            listOf("documento", "empresa"),
                                            listOf(listaDocumentos[i].documento, codEmpresa!!)
                                        )
                                    )
                                    qlineas.put(
                                        "cbsretflete",
                                        conn.getCampoDoubleCamposVarios(
                                            "ke_doccti",
                                            "cbsretflete",
                                            listOf("documento", "empresa"),
                                            listOf(listaDocumentos[i].documento, codEmpresa!!)
                                        )
                                    )
                                    keAndroid.insert("ke_precobradocs", null, qlineas)
                                }
                            }
                            //guardaSaldoFavor2(binding.rbCxcDivisasMain, binding.rbCxcCompMain, binding.etCxcMontoMain.text.toString().toDouble(), 0.00,  binding.tvCxcTotal, listaReciboPrLineas[listaReciboPrLineas.size-1].id_recibo, listaReciboPrLineas[listaReciboPrLineas.size-1].documento)
                            keAndroid.insert("ke_precobranza", null, qcabecera)
                            val qcorrelativo = ContentValues()
                            qcorrelativo.put("kcor_numero", nroCorrelativo)
                            qcorrelativo.put("kcor_vendedor", codUsuario)

                            conn.saveImg(
                                listaImagenes,
                                nroPrecobranza,
                                this
                            ) // <-- Guardando Imagenes

                            keAndroid.insert("ke_corprec", null, qcorrelativo)
                            llCommit = true

                        } catch (exception: SQLException) {

                            exception.printStackTrace()
                            llCommit = false

                            keAndroid.endTransaction()
                            if (!llCommit) {
                                return
                            }
                        }


                        //preparacion para el guardado de datos
                        var netoCmReal = valorReal(listaReciboCmLineas.sumOf { it.bscobro })
                        comp.bsneto = valorReal(listaReciboCmLineas.sumOf { it.bsneto })

                        /* sumo los bss en total y los redondeo al momento de guardarlo */
                        var bssumaTotalCm = valorReal(listaReciboCmLineas.sumOf { it.bscobro })

                        comp.bstotal = comp.bsneto
                        comp.dolneto = valorReal(listaReciboCmLineas.sumOf { it.dolneto })
                        var doltotalCm = binding.etCxcMontoCom.text.toString().toDouble()

                        comp.doltotal = comp.dolneto
                        comp.netocob =
                            binding.etCxcMontoCom.text.toString().ifEmpty { "0.0" }.toDouble()
                        //if (monedaSeleccionadaCm == "2") listaReciboCmLineas.sumOf { it.dolneto } else listaReciboCmLineas.sumOf { it.bsneto }
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
                        comp.moneda = monedaSeleccionadaCm
                        comp.tasadia = tasaCambioSeleccionadaPrincipal
                        listaReciboCmCabecera.add(comp)

                        try {

                            val qcabecera = ContentValues()
                            val qlineas = ContentValues()
                            val qdescuentos = ContentValues()

                            for (i in listaReciboCmCabecera.indices) {

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
                                if (ignorecm) {
                                    qcabecera.put("efectivo", listaReciboCmCabecera[i].efectivo)
                                } else {
                                    qcabecera.put("bcocod", listaReciboCmCabecera[i].bcocod)
                                    qcabecera.put("bcomonto", listaReciboCmCabecera[i].bcomonto)
                                    qcabecera.put("bcoref", listaReciboCmCabecera[i].bcoref)
                                }
                                qcabecera.put("edorec", listaReciboCmCabecera[i].edorec)
                                qcabecera.put("fchvigen", listaReciboCmCabecera[i].fchvigen)
                                qcabecera.put("netocob", listaReciboCmCabecera[i].netocob)
                                qcabecera.put("fechamodifi", getFechaHoy())
                                qcabecera.put(
                                    "tipo_pago",
                                    if (binding.rbCxcAbonoMain.isChecked) 1 else 0
                                )
                                qcabecera.put(
                                    "complemento",
                                    if (binding.cbCxcComplemento.isChecked) nroPrecobranza else ""
                                )
                                //qcabecera.put("docdifcamb",diferencialCambiario)

                                for (j in listaReciboCmLineas.indices) {
                                    qlineas.put("cxcndoc", listaReciboCmLineas[j].id_recibo)
                                    qlineas.put("agencia", listaReciboCmLineas[j].agencia)
                                    qlineas.put("tipodoc", listaReciboCmLineas[j].tipodoc)
                                    qlineas.put("documento", listaReciboCmLineas[j].documento)
                                    qlineas.put("bscobro", listaReciboCmLineas[j].bscobro)
                                    qlineas.put("tnetoddol", listaReciboCmLineas[j].tnetoddol)
                                    qlineas.put("tnetodbs", listaReciboCmLineas[j].tnetodbs)
                                    qlineas.put("fchrecibod", getFechaNow())
                                    qlineas.put("kecxc_idd", listaReciboCmCabecera[i].kecxc_id)
                                    qlineas.put("tasadiad", listaReciboCmCabecera[i].tasadia)
                                    qlineas.put("reten", 1)
                                    qlineas.put("codcliente", listaDocumentos[i].codcliente)
                                    qlineas.put(
                                        "nombrecli",
                                        conn.getCampoStringCamposVarios(
                                            "cliempre",
                                            "nombre",
                                            listOf("codigo", "empresa"),
                                            listOf(listaDocumentos[i].codcliente, codEmpresa!!)
                                        )
                                    )
                                    qlineas.put("tasadoc", listaDocumentos[j].tasadoc)
                                    //2023-10-19 esto es mal pero debido a la falta de tiempo va asi
                                    qlineas.put(
                                        "cbsretiva",
                                        conn.getCampoDoubleCamposVarios(
                                            "ke_doccti",
                                            "cbsretiva",
                                            listOf("documento", "empresa"),
                                            listOf(listaDocumentos[i].documento, codEmpresa!!)
                                        )
                                    )
                                    qlineas.put(
                                        "cbsretflete",
                                        conn.getCampoDoubleCamposVarios(
                                            "ke_doccti",
                                            "cbsretflete",
                                            listOf("documento", "empresa"),
                                            listOf(listaDocumentos[i].documento, codEmpresa!!)
                                        )
                                    )
                                    keAndroid.insert("ke_precobradocs", null, qlineas)
                                }

                            }
                            //ke_android.execSQL("UPDATE ke_precobradocs SET afavor= ${guardaSaldoFavor(montoRec, binding.rbCxcDivisasCom, binding.rbCxcCompMain, (binding.etCxcMontoCom.text.toString().toDouble() + binding.etCxcMontoCom.text.toString().toDouble()) , binding.tvCxcTotal)} WHERE cxcndoc='${listaReciboCmLineas[listaReciboCmLineas.size-1].id_recibo}' AND documento = '${listaReciboCmLineas[listaReciboCmLineas.size-1].documento}';")
                            guardaSaldoFavor2(
                                binding.rbCxcDivisasCom,
                                binding.rbCxcCompMain,
                                binding.etCxcMontoMain.text.toString().toDouble(),
                                binding.etCxcMontoCom.text.toString().toDouble(),
                                binding.tvCxcTotal,
                                listaReciboCmLineas[listaReciboCmLineas.size - 1].id_recibo,
                                listaReciboCmLineas[listaReciboCmLineas.size - 1].documento,
                                binding.rbCxcDivisasCom.isChecked
                            )
                            keAndroid.insert("ke_precobranza", null, qcabecera)

                            //si hay descuentos, los inserto tambien
                            if (listaDescuentos.size > 0) {
                                for (i in listaDocumentos.indices) {
                                    for (j in listaDescuentos.indices) {
                                        if (listaDocumentos[i].documento == listaDescuentos[j].nrodoc) {
                                            qdescuentos.put(
                                                "agencia",
                                                listaDocumentos[i].agencia
                                            )
                                            qdescuentos.put(
                                                "tipodoc",
                                                listaDocumentos[i].tipodoc
                                            )
                                            qdescuentos.put(
                                                "documento",
                                                listaDescuentos[j].nrodoc
                                            )
                                            qdescuentos.put(
                                                "prcdctoaplic", listaDescuentos[j].pordscto
                                            )
                                            qdescuentos.put(
                                                "montodctodol", listaDescuentos[j].cantdscto
                                            )
                                            qdescuentos.put(
                                                "tasadoc",
                                                listaDocumentos[i].tasadoc
                                            )
                                            qdescuentos.put(
                                                "codcliente", listaDocumentos[i].codcliente
                                            )
                                            qdescuentos.put(
                                                "fchvigen",
                                                listaDocumentos[i].vence
                                            )
                                            qdescuentos.put("fechamodifi", fechaActual)
                                            //inserción de descuentos de tenerlos
                                            keAndroid.insert("ke_precobdcto", null, qdescuentos)
                                        }
                                    }
                                }
                            }


                            val qcorrelativo = ContentValues()
                            qcorrelativo.put("kcor_numero", nroCorrelativoCom)
                            qcorrelativo.put("kcor_vendedor", codUsuario)

                            keAndroid.insert("ke_corprec", null, qcorrelativo)
                            llCommit = true

                        } catch (exception: SQLException) {
                            exception.printStackTrace()
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
                            listadatos.add(cxc)
                            listadatos[0].cliente = codigoCliente

                            val dialog = DialogRecibo()
                            dialog.DialogRecibo(this, listadatos)

                            Toast.makeText(this, "RECIBO CREADO", Toast.LENGTH_SHORT).show()
                            // finish()
                        } else {
                            keAndroid.endTransaction()
                        }

                    }

                }


            }
            // Lo que el clienta da es mayor o igual total y no esta marcado complemento
            else if (montoRec >= montoMinimoRec && !binding.cbCxcComplemento.isChecked) {
                var llCommit: Boolean
                keAndroid = conn.writableDatabase

                //listas con el tipo de datos para los recibos
                val listaReciboPrCabecera: ArrayList<CXC> = ArrayList()
                val listaReciboPrLineas: ArrayList<CXC> = ArrayList()

                listaDocumentos.sortBy { it.fechaDocs }//llenado de datos de cabecera
                val cxc = CXC()
                cxc.id_recibo = nroPrecobranza
                cxc.tipoRecibo = "W"
                cxc.codigoVend = codUsuario.toString()
                cxc.kecxc_id = tasaId
                cxc.tasadia = tasaCambioSeleccionadaPrincipal
                cxc.fchrecibo = fechatasaH
                cxc.clicontesp =
                    listaDocumentos[0].contribesp.toString() //esto lo jalo de  la lista de docs? <--- PREGUNTAR, se puede obtener
                cxc.moneda = monedaSeleccionadaPr
                //IF que valida y llena los datos para cuando se selecciona efectivo o transferencia
                if (ignore) {
                    cxc.bcocod = ""
                    cxc.bcoref = ""
                    cxc.efectivo = montoRec
                    cxc.fchvigen = fechaSuma(fechaActual, 60)
                } else {
                    cxc.bcocod = codigoBancoCompleto
                    cxc.bcomonto = montoRec
                    cxc.bcoref = referenciaPrincipal
                    cxc.fchvigen = fechaSuma(fechaActual, 5)
                }

                cxc.edorec = "0"
                cxc.fchhr = fechaActual


                //en este ciclo lleno la lista de precobradocs
                for (i in listaDocumentos.indices) {
                    val cxclineas = CXC()

                    cxclineas.id_recibo = nroPrecobranza
                    cxclineas.agencia = listaDocumentos[i].agencia
                    cxclineas.tipodoc = listaDocumentos[i].tipodoc
                    cxclineas.documento = listaDocumentos[i].documento
                    listaReciboPrLineas.add(cxclineas)

                }

                //en este ciclo, agrego las retenciones
                for (i in listaReciboPrLineas.indices) {
                    if (listaReciboPrLineas[i].agencia == "002" && (listaReciboPrLineas[i].documento == listaDocumentos[i].documento)) {

                        listaDocumentos[i].cbsretflete = 0.00

                    }

                    //llenado de campos de retenciones
                    //retenciones fase 2
                    for (j in listaRetGuardada.indices) {
                        if (listaRetGuardada[j].nrodoc == listaReciboPrLineas[i].documento) {
                            if (listaRetGuardada[j].tiporet == "iva") {
                                //si es de iva
                                listaReciboPrLineas[i].nroret = listaRetGuardada[j].nroret
                                listaReciboPrLineas[i].fchemiret = listaRetGuardada[j].fecharet
                                listaReciboPrLineas[i].bsretiva = -listaRetGuardada[j].montoret
                                listaReciboPrLineas[i].refret = listaRetGuardada[j].refret
                            }

                            if (listaRetGuardada[j].tiporet == "flete") {
                                //si es de flete
                                listaReciboPrLineas[i].nroretfte = listaRetGuardada[j].nroret
                                listaReciboPrLineas[i].fchemirfte = listaRetGuardada[j].fecharet
                                listaReciboPrLineas[i].bsretfte = -listaRetGuardada[j].montoret
                                listaReciboPrLineas[i].refretfte = listaRetGuardada[j].refret
                            }

                            if (listaRetGuardada[j].tiporet == "parme") {
                                //si es de parme
                                listaReciboPrLineas[i].retmun_nro = listaRetGuardada[j].nroret
                                listaReciboPrLineas[i].retmun_fch = listaRetGuardada[j].fecharet
                                listaReciboPrLineas[i].retmun_mto =
                                    -listaRetGuardada[j].montoret
                                listaReciboPrLineas[i].retmun_cod = listaRetGuardada[j].refret
                            }
                        }
                    }

                    //descuento ivas y fletes ( de haberlos)
                    if (listaReciboPrLineas[i].documento == listaDocumentos[i].documento && listaDocumentos[i].tipodocv == "FAC") {

                        if (listaDocumentos[i].bsiva - listaDocumentos[i].bsmtoiva <= 0.00) {
                            listaReciboPrLineas[i].bsmtoiva = 0.00
                            listaReciboPrLineas[i].doliva = 0.00

                        } else {

                            //descuento ivas del monto del recibo original .-
                            val restaIvadol =
                                listaDocumentos[i].dtotimpuest - listaDocumentos[i].cdretencioniva
                            //2023-04-03 comentario por no tener en cuenta cuando se escluyen retenciones
                            //var restaIvadol = listaDocumentos[i].dtotimpuest - listaDocumentos[i].cdretencioniva
                            val restaIvabss =
                                listaDocumentos[i].bsiva - listaDocumentos[i].cbsretencioniva
                            //var restaIvabss = listaDocumentos[i].bsiva - listaDocumentos[i].cbsretencioniva

                            if (binding.rbCxcDivisasMain.isChecked) {
                                //hago el descuento del iva del nmonto de pago
                                montoRec = (montoRec - restaIvadol).valorReal()

                                if (montoRec < 0.00) {
                                    Toast.makeText(
                                        this,
                                        "Monto insuficiente",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                    return
                                }

                                listaReciboPrLineas[i].bscobro += (listaDocumentos[i].bsiva - listaDocumentos[i].cbsretencioniva)
                                listaReciboPrLineas[i].bsmtoiva = listaDocumentos[i].bsiva
                                listaReciboPrLineas[i].doliva =
                                    listaDocumentos[i].dtotimpuest - listaDocumentos[i].cdretencioniva
                                //2023-04-03 comentado por mal calculo?
                                //listaReciboPrLineas[i].tnetoddol  += listaReciboPrLineas[i].doliva
                                listaReciboPrLineas[i].tnetoddol += (listaDocumentos[i].dtotimpuest - listaDocumentos[i].cdretencioniva)

                            } else {
                                montoRec = (montoRec - restaIvabss).valorReal()
                                if (montoRec < 0.00) {
                                    Toast.makeText(
                                        this,
                                        "Monto insuficiente",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                    return
                                }
                                listaReciboPrLineas[i].bscobro += (listaDocumentos[i].bsiva - listaDocumentos[i].cbsretencioniva)
                                listaReciboPrLineas[i].bsmtoiva = listaDocumentos[i].bsiva
                                listaReciboPrLineas[i].doliva =
                                    listaDocumentos[i].dtotimpuest - listaDocumentos[i].cdretencioniva
                                listaReciboPrLineas[i].tnetodbs += (listaDocumentos[i].bsiva - listaDocumentos[i].cbsretencioniva)
                                //listaReciboPrLineas[i].tnetodbs += listaDocumentos[i].bsiva

                            }



                            if (listaDocumentos[i].bsflete - listaDocumentos[i].bsmtofte <= 1) {
                                listaReciboPrLineas[i].bsmtofte = 0.00
                                listaReciboPrLineas[i].dolflete = 0.00
                            } else {
                                if (binding.cbExcReten.isChecked) {
                                    listaReciboPrLineas[i].bscobro += listaDocumentos[i].bsflete
                                } else {
                                    listaReciboPrLineas[i].bscobro += listaDocumentos[i].bsflete - listaDocumentos[i].cbsretflete
                                }

                                if (binding.rbCxcDivisasMain.isChecked) {
                                    montoRec -= if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dFlete - listaDocumentos[i].cdretflete
                                    listaReciboPrLineas[i].bsmtofte = listaDocumentos[i].bsflete
                                    listaReciboPrLineas[i].dolflete =
                                        if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dFlete - listaDocumentos[i].cdretflete
                                    listaReciboPrLineas[i].tnetoddol += if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dFlete - listaDocumentos[i].cdretflete

                                } else {
                                    montoRec -= if (binding.cbExcReten.isChecked) listaDocumentos[i].bsflete else listaDocumentos[i].bsflete - listaDocumentos[i].cbsretflete
                                    listaReciboPrLineas[i].bsmtofte = listaDocumentos[i].bsflete
                                    listaReciboPrLineas[i].dolflete =
                                        if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dFlete - listaDocumentos[i].cdretflete
                                    listaReciboPrLineas[i].tnetodbs += if (binding.cbExcReten.isChecked) listaDocumentos[i].bsflete else listaDocumentos[i].bsflete - listaDocumentos[i].cbsretflete

                                }

                            }

                        }
                    } else {
                        if (listaDocumentos[i].bsflete - listaDocumentos[i].bsmtofte <= 1) {
                            listaReciboPrLineas[i].bsmtofte = 0.00
                            listaReciboPrLineas[i].dolflete = 0.00
                        } else {

                            val fleteaCobrar =
                                if (binding.cbExcReten.isChecked) listaDocumentos[i].bsflete else listaDocumentos[i].bsflete - listaDocumentos[i].cbsretflete
                            listaReciboPrLineas[i].bscobro += fleteaCobrar

                            if (binding.rbCxcDivisasMain.isChecked) {
                                montoRec -= if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dFlete - listaDocumentos[i].cdretflete
                                listaReciboPrLineas[i].bsmtofte = listaDocumentos[i].bsflete
                                listaReciboPrLineas[i].dolflete =
                                    if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dFlete - listaDocumentos[i].cdretflete
                                listaReciboPrLineas[i].tnetoddol += if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dFlete - listaDocumentos[i].cdretflete

                            } else {
                                montoRec -= if (binding.cbExcReten.isChecked) listaDocumentos[i].bsflete else listaDocumentos[i].bsflete - listaDocumentos[i].cbsretflete
                                listaReciboPrLineas[i].bsmtofte = listaDocumentos[i].bsflete
                                listaReciboPrLineas[i].dolflete =
                                    if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dFlete - listaDocumentos[i].cdretflete
                                listaReciboPrLineas[i].tnetodbs += if (binding.cbExcReten.isChecked) listaDocumentos[i].bsflete else listaDocumentos[i].bsflete - listaDocumentos[i].cbsretflete

                            }

                        }
                    }
                }

                //llenado de los netos
                for (i in listaReciboPrLineas.indices) {

                    if (montoRec < 1) {
                        // borré el return
                    } else {
                        if (binding.rbCxcDivisasMain.isChecked) {
                            val netoTV = binding.tvCxcNeto.text.toString().toDouble()

                            if (netoTV > 0.00) {
                                val netoRealenDoc = listaDocumentos[i].netorestante

                                //Buscando el porcentaje de descuento del documento a cobrar
                                val porcentajeDescuento =
                                    cantidadDeDescuento(listaDocumentos[i].documento)
                                //Calculando cuanto del neto se le debe de restar al documento
                                val netoRestaDesc =
                                    (listaDocumentos[i].netorestante * (porcentajeDescuento / 100))

                                //Aqui si es valido poner valorReal porque en ocasiones la resta deja demasiado decimales que alteran al if
                                val netoDesc = valorReal(netoRealenDoc - netoRestaDesc)

                                if (montoRec >= netoDesc) {
                                    val bscobrado = netoRealenDoc * listaDocumentos[i].tasadoc

                                    listaReciboPrLineas[i].bscobro += bscobrado

                                    //Buscando el porcentaje de descuento del documento a cobrar
                                    val porcentajeDescuento =
                                        cantidadDeDescuento(listaDocumentos[i].documento)
                                    //Calculando cuanto del neto se le debe de restar al documento
                                    val netoRestaDesc =
                                        (listaDocumentos[i].netorestante * (porcentajeDescuento / 100))

                                    //quitando descuento al neto
                                    netocobrado = if (binding.tvCxcDctos.text.toString()
                                            .toDouble() > 0.0
                                    ) valorReal(listaDocumentos[i].netorestante - netoRestaDesc) else listaDocumentos[i].netorestante // >---------------------------------------------------------------- AQUI

                                    listaReciboPrLineas[i].dolneto = netocobrado
                                    montoRec -= netocobrado

                                    //2023-09-07 se coloca valorReal para redondear decimas, si se quita es posible que salte al else de abajo ↓↓↓
                                    montoRec = valorReal(montoRec)

                                    listaReciboPrLineas[i].tnetoddol += netocobrado

                                } else if (netoRealenDoc > montoRec && montoRec > 0) {

                                    val cobroAbono = montoRec * listaDocumentos[i].tasadoc

                                    listaReciboPrLineas[i].bscobro += cobroAbono
                                    netocobrado = montoRec
                                    listaReciboPrLineas[i].dolneto = valorReal(netocobrado)
                                    montoRec -= netocobrado
                                    montoRec = valorReal(montoRec)
                                    listaReciboPrLineas[i].tnetoddol += netocobrado
                                }

                            }
                        }

                        if (binding.rbCxcBssMain.isChecked) {
                            var netoRealenDoc = listaDocumentos[i].netorestante
                            netoRealenDoc = valorReal(netoRealenDoc)

                            //si el monto del recibo cubre el monto del documento
                            if (montoRec > (netoRealenDoc * tasaCambioSeleccionadaPrincipal)) {
                                val bscobrado =
                                    valorReal(netoRealenDoc * listaDocumentos[i].tasadoc)
                                listaReciboPrLineas[i].bscobro += bscobrado
                                netocobrado = listaDocumentos[i].netorestante
                                listaReciboPrLineas[i].dolneto = netocobrado
                                montoRec -= valorReal(netoRealenDoc * listaDocumentos[i].tasadoc)
                                listaReciboPrLineas[i].bsneto =
                                    valorReal(netoRealenDoc * tasaCambioSeleccionadaPrincipal)
                                listaReciboPrLineas[i].tnetodbs += valorReal(netoRealenDoc * tasaCambioSeleccionadaPrincipal)

                            }
                            //si el monto del recibo no cubre el monto completo del documento (se comporta como un abono)
                            else if ((netoRealenDoc * tasaCambioSeleccionadaPrincipal) > montoRec && montoRec > 0) {
                                netocobrado = montoRec / tasaCambioSeleccionadaPrincipal
                                val cobroAbono = netocobrado * listaDocumentos[i].tasadoc
                                listaReciboPrLineas[i].bscobro += valorReal(cobroAbono)
                                val dolneto = netocobrado
                                listaReciboPrLineas[i].dolneto = valorReal(dolneto)
                                listaReciboPrLineas[i].bsneto = montoRec
                                listaReciboPrLineas[i].tnetodbs += montoRec
                                montoRec -= montoRec

                            }
                        }

                        //los bss del neto cobrado
                        /*recorrido de lista de descuentos para asignar cantidad según nro doc. (de tener)*/
                        for (j in listaDescuentos.indices) {
                            if (listaDescuentos[j].nrodoc == listaDocumentos[i].documento) {
                                listaReciboPrLineas[i].prcdsctopp = listaDescuentos[j].pordscto
                            }
                        }

                    }
                }

                /*esto va a ser resultado de la suma de los campos de la lista (ke_precobranza)
                 de detalles */

                val difReteIva =
                    valorReal(listaReciboPrLineas.sumOf { it.bsmtoiva }) + valorReal(
                        listaReciboPrLineas.sumOf { it.bsretiva })
                val difRetyFlete =
                    valorReal(listaReciboPrLineas.sumOf { it.bsmtofte }) + valorReal(
                        listaReciboPrLineas.sumOf { it.bsretfte })
                var netoReal =
                    valorReal(listaReciboPrLineas.sumOf { it.bscobro }) - difReteIva - difRetyFlete //--revisar si esto es necesario
                cxc.bsneto = valorReal(listaReciboPrLineas.sumOf { it.bsneto })
                cxc.bsretiva = valorReal(listaReciboPrLineas.sumOf { it.bsretiva })
                cxc.bsiva = valorReal(listaReciboPrLineas.sumOf { it.bsmtoiva })
                cxc.bsflete = valorReal(listaReciboPrLineas.sumOf { it.bsmtofte })

                /* sumo los bss en total y los redondeo al momento de guardarlo */
                var bssumaTotal = valorReal(listaReciboPrLineas.sumOf { it.bscobro })

                //cxc.bstotal    = valorReal(bssumaTotal)
                cxc.bstotal =
                    valorReal(listaReciboPrLineas.sumOf { it.bsneto } + (listaReciboPrLineas.sumOf { it.bsmtoiva } + listaReciboPrLineas.sumOf { it.bsretiva }) + (listaReciboPrLineas.sumOf { it.bsmtofte } + listaReciboPrLineas.sumOf { it.bsretfte }))
                //cxc.dolneto    = valorReal(listaDocumentos.sumOf{it.dtotneto }) //<-------------------- Revisar si es necesario
                cxc.dolneto = valorReal(listaReciboPrLineas.sumOf { it.dolneto })
                cxc.doliva = valorReal(listaReciboPrLineas.sumOf { it.doliva })
                //cxc.dolretiva  = valorReal(listaDocumentos.sumOf { it.cdretencioniva })
                cxc.dolflete = valorReal(listaReciboPrLineas.sumOf { it.dolflete })
                cxc.doltotal =
                    valorReal(listaReciboPrLineas.sumOf { it.dolneto } + listaReciboPrLineas.sumOf { it.doliva } + listaReciboPrLineas.sumOf { it.dolflete })
                cxc.netocob =
                    binding.etCxcMontoMain.text.toString().ifEmpty { "0.0" }.toDouble()
                //if (monedaSeleccionadaPr == "2") cxc.doltotal + superSaldoFavor() else cxc.bstotal + valorReal(superSaldoFavor() * tasaCambioSeleccionadaPrincipal)
                println(valorReal(superSaldoFavor() * tasaCambioSeleccionadaPrincipal))
                println(cxc.bstotal)
                println(cxc.netocob)
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
                cxc.moneda = monedaSeleccionadaPr
                cxc.tasadia = tasaCambioSeleccionadaPrincipal
                listaReciboPrCabecera.add(cxc)

                try {

                    // inicio la transacción
                    keAndroid.beginTransaction()
                    val qcabecera = ContentValues()
                    val qlineas = ContentValues()
                    val qdescuentos = ContentValues()

                    for (i in listaReciboPrCabecera.indices) {
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
                        qcabecera.put("dolretiva", listaReciboPrCabecera[i].dolretiva)
                        qcabecera.put("dolflete", listaReciboPrCabecera[i].dolflete)
                        qcabecera.put("doltotal", listaReciboPrCabecera[i].doltotal)
                        qcabecera.put("moneda", listaReciboPrCabecera[i].moneda)
                        if (ignore) {
                            qcabecera.put("efectivo", listaReciboPrCabecera[i].efectivo)
                        } else {
                            qcabecera.put("bcocod", listaReciboPrCabecera[i].bcocod)
                            qcabecera.put("bcomonto", listaReciboPrCabecera[i].bcomonto)
                            qcabecera.put("bcoref", listaReciboPrCabecera[i].bcoref)
                        }
                        qcabecera.put("edorec", listaReciboPrCabecera[i].edorec)
                        qcabecera.put("fchvigen", listaReciboPrCabecera[i].fchvigen)
                        qcabecera.put("bsretflete", listaReciboPrCabecera[i].bsretflete)
                        qcabecera.put("netocob", listaReciboPrCabecera[i].netocob)
                        qcabecera.put("fechamodifi", getFechaHoy())
                        qcabecera.put(
                            "tipo_pago",
                            if (binding.rbCxcAbonoMain.isChecked) 1 else 0
                        )
                        qcabecera.put(
                            "complemento",
                            if (binding.cbCxcComplemento.isChecked) nroComplemento else ""
                        )
                        //qcabecera.put("docdifcamb",diferencialCambiario)

                        for (j in listaReciboPrLineas.indices) {
                            qlineas.put("cxcndoc", listaReciboPrLineas[j].id_recibo)
                            qlineas.put("agencia", listaReciboPrLineas[j].agencia)
                            qlineas.put("tipodoc", listaReciboPrLineas[j].tipodoc)
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
                            qlineas.put(
                                "tnetoddol",
                                valorReal(listaReciboPrLineas[j].tnetoddol)
                            )
                            qlineas.put("tnetodbs", valorReal(listaReciboPrLineas[j].tnetodbs))
                            qlineas.put("fchrecibod", getFechaNow())
                            qlineas.put("kecxc_idd", listaReciboPrCabecera[i].kecxc_id)
                            qlineas.put("tasadiad", listaReciboPrCabecera[i].tasadia)
                            qlineas.put("reten", retennn)
                            qlineas.put("codcliente", listaDocumentos[i].codcliente)
                            qlineas.put(
                                "nombrecli",
                                conn.getCampoStringCamposVarios(
                                    "cliempre",
                                    "nombre",
                                    listOf("codigo", "empresa"),
                                    listOf(listaDocumentos[i].codcliente, codEmpresa!!)
                                )
                            )
                            qlineas.put("tasadoc", listaDocumentos[j].tasadoc)
                            //2023-10-19 esto es mal pero debido a la falta de tiempo va asi
                            qlineas.put(
                                "cbsretiva",
                                conn.getCampoDoubleCamposVarios(
                                    "ke_doccti",
                                    "cbsretiva",
                                    listOf("documento", "empresa"),
                                    listOf(listaDocumentos[i].documento, codEmpresa!!)
                                )
                            )
                            qlineas.put(
                                "cbsretflete",
                                conn.getCampoDoubleCamposVarios(
                                    "ke_doccti",
                                    "cbsretflete",
                                    listOf("documento","empresa"),
                                    listOf(listaDocumentos[i].documento, codEmpresa!!)
                                )
                            )

                            keAndroid.insert("ke_precobradocs", null, qlineas)
                        }
                    }

                    //ke_android.execSQL("UPDATE ke_precobradocs SET afavor= ${guardaSaldoFavor(montoRec, binding.rbCxcDivisasMain, binding.rbCxcCompMain, binding.etCxcMontoMain.text.toString().toDouble(), binding.tvCxcTotal)} WHERE cxcndoc='${listaReciboPrLineas[listaReciboPrLineas.size-1].id_recibo}' AND documento = '${listaReciboPrLineas[listaReciboPrLineas.size-1].documento}';")
                    guardaSaldoFavor2(
                        binding.rbCxcDivisasMain,
                        binding.rbCxcCompMain,
                        binding.etCxcMontoMain.text.toString().toDouble(),
                        0.00,
                        binding.tvCxcTotal,
                        listaReciboPrLineas[listaReciboPrLineas.size - 1].id_recibo,
                        listaReciboPrLineas[listaReciboPrLineas.size - 1].documento,
                        true
                    )
                    keAndroid.insert("ke_precobranza", null, qcabecera)

                    //si hay descuentos, los inserto tambien
                    if (listaDescuentos.size > 0) {
                        for (i in listaDocumentos.indices) {
                            for (j in listaDescuentos.indices) {
                                if (listaDocumentos[i].documento == listaDescuentos[j].nrodoc) {
                                    qdescuentos.put("agencia", listaDocumentos[i].agencia)
                                    qdescuentos.put("tipodoc", listaDocumentos[i].tipodoc)
                                    qdescuentos.put("documento", listaDescuentos[j].nrodoc)
                                    qdescuentos.put("prcdctoaplic", listaDescuentos[j].pordscto)
                                    qdescuentos.put(
                                        "montodctodol",
                                        listaDescuentos[j].cantdscto
                                    )
                                    qdescuentos.put("tasadoc", listaDocumentos[i].tasadoc)
                                    qdescuentos.put("codcliente", listaDocumentos[i].codcliente)
                                    qdescuentos.put("fchvigen", listaDocumentos[i].vence)
                                    qdescuentos.put("fechamodifi", fechaActual)
                                    //inserción de descuentos de tenerlos
                                    keAndroid.insert("ke_precobdcto", null, qdescuentos)
                                }
                            }
                        }
                    }

                    conn.saveImg(listaImagenes, nroPrecobranza, this) // <-- Guardando Imagenes

                    val qcorrelativo = ContentValues()
                    qcorrelativo.put("kcor_numero", nroCorrelativo)
                    qcorrelativo.put("kcor_vendedor", codUsuario)

                    keAndroid.insert("ke_corprec", null, qcorrelativo)


                    llCommit = true
                } catch (exception: SQLException) {

                    exception.printStackTrace()
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
                    listadatos.add(cxc)
                    listadatos[0].cliente = codigoCliente

                    val dialog = DialogRecibo()
                    dialog.DialogRecibo(this, listadatos)

                    Toast.makeText(this, "RECIBO CREADO", Toast.LENGTH_SHORT).show()
                    // finish()
                } else {
                    keAndroid.endTransaction()
                }

            }

        } else if (listaDocumentos.size == 1) {
            //2023-03-27 se comento la linea de codigo debido a que al inicio de codigo ya verifica si se seleccionoun solo documento o varios para colocar abono
            //binding.rbCxcAbonoMain.visibility = View.VISIBLE

            if (binding.rbCxcCompMain.isChecked) {
                montoMinimoRec = binding.tvCxcTotal.text.toString().toDouble() - 0.5

            } else if (binding.rbCxcAbonoMain.isChecked) {
                //si se presiona abono, el monto minimo sera la suma del flete mas el iva
                montoMinimoRec = (((ivaTot + fleteTot) - retTot) - 0.01).valorReal()
                // de no haber iva y flete, el monto minimo se hace aut. en 1 (bs o $)
                if (montoMinimoRec == 0.00) {
                    montoMinimoRec = 1.0
                }
            }

            if (montoRec < montoMinimoRec && !binding.cbCxcComplemento.isChecked) {

                if (binding.rbCxcCompMain.isChecked) {
                    Toast.makeText(
                        this,
                        "El monto del recibo no debe ser menor al Total",
                        Toast.LENGTH_SHORT
                    ).show()

                } else if (binding.rbCxcAbonoMain.isChecked) {
                    Toast.makeText(
                        this,
                        "El monto del recibo no debe ser menor a la suma del IVA y el Flete",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return
            }
            // 2023-07-06 se coloco un or que suma la cantidad previamente restada a la variable montoMinimoRec debido a un caso donde no entre a ningun if
            // esto es debido a que si se paga un precio cercano al precio total del documento pero ligeramente menor no entra a ningun condicional ejemplo
            // total = 92.25
            // ingresado = 90
            // total restado 8.75 <-- (montoMinimoRec)
            // REDUNDANTE
            else if ((montoRec < montoMinimoRec && binding.cbCxcComplemento.isChecked) || (montoRec < (montoMinimoRec + 0.5) && binding.cbCxcComplemento.isChecked)) {
                //Solo aplica cuando se paga COMPLETO
                // en este caso es solo un documento en el cual se va a pagar una parte con el recibo principal
                // y el resto sera pagado mediante un recibo de complemento que solo afecta al neto.

                //si el monto del recibo es menor al monto minimo pero el complemento esta marcado
                //val diferencia = (valorReal(montoMinimoRec * 0.9) <= montoRec)

                val diferencia = if (binding.rbCxcDivisasCom.isChecked) {
                    true
                } else {
                    ((valorReal(montoMinimoRec * APP_PORCENTAJE_COMPLEMENTO) <= montoRec))
                }

                //si la diferencia entre el monto reportado y el monto minimo es mayor a un 10%, no se permite el complemento
                if (!diferencia) {
                    Toast.makeText(this, "Monto Excedido para complemento", Toast.LENGTH_SHORT)
                        .show()
                    return

                    //de manera contraria, si
                } else if (diferencia) {

                    //valido montos del complemento
                    if (binding.etCxcMontoCom.text.toString() == "" || binding.etCxcMontoCom.text.toString()
                            .equals(null)
                    ) {
                        Toast.makeText(
                            this,
                            "Monto de complemento no puede estar vacío",
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }
                    //valido montos en 0 y banco vacio del complemento, asi como tambien
                    //la referencia.
                    // Si encuentra la referencia bancaria del complemento vacia entrara aqui
                    if (binding.etCxcRefCom.text.toString() == "" || binding.etCxcRefCom.text.toString()
                            .equals(null)
                    ) {
                        // Si la variable ignorecm es true significa que se selecciono efectivo como forma de pago, y continua
                        if (ignorecm) {

                        } else { // Si la variable ignorecm es false significa que se selecciono transferencia, lanzara este mensaje y parará
                            Toast.makeText(
                                this,
                                "La referencia del complemento no puede estar vacía",
                                Toast.LENGTH_SHORT
                            ).show()
                            return
                        }
                    }
                    // Si encuentra el codigo de banco del complemento vacia entrara aqui
                    if (codigoBancoComplemento == "") {
                        // Si la variable ignorecm es true significa que se selecciono efectivo como forma de pago, y continua
                        if (ignorecm) {

                        } else {// Si la variable ignorecm es false significa que se selecciono transferencia, lanzara este mensaje y parará
                            Toast.makeText(
                                this,
                                "Debes seleccionar un banco para el complemento",
                                Toast.LENGTH_SHORT
                            ).show()
                            return
                        }
                    }
                    //Monto ingresado en complemento
                    var montoRecComp = binding.etCxcMontoCom.text.toString().toDouble()

                    val complementoMontoStandard: Double =
                        if (binding.rbCxcBssCom.isChecked) { // if que valida que moneda se seleccionó en complemento
                            // Si se selecciona bolivares hara la conversion a dolares
                            (montoRecComp / tasaCambioSeleccionadaPrincipal)
                        } else {
                            // Si se selecciona dolares la moneda permanecera y se guardara en otra variable
                            montoRecComp
                        }

                    val montoPrinciStandard: Double =
                        if (binding.rbCxcBssMain.isChecked) {// if que valida que moneda se seleccionó en principal
                            // Si se selecciona bolivares hara la conversion a dolares
                            montoRec / tasaCambioSeleccionadaPrincipal
                        } else {
                            // Si se selecciona dolares la moneda permanecera y se guardara en otra variable
                            montoRec
                        }

                    val montoComprar: Double =
                        if (binding.rbCxcBssMain.isChecked) {// if que valida que moneda se seleccionó en principal
                            // Si se selecciona bolivares hara la conversion a dolares
                            montoMinimoRec / tasaCambioSeleccionadaPrincipal
                        } else {
                            // Si se selecciona dolares la moneda permanecera y se guardara en otra variable
                            montoMinimoRec
                        }

                    // IF que valida que la suma del principal y del complemento sean mayores al monto minimo requerido
                    if ((complementoMontoStandard + montoPrinciStandard) < montoComprar) {
                        Toast.makeText(
                            this,
                            "Montos insuficientes para completar ambos recibos 2",
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    } else {

                        referenciaCm = binding.etCxcRefCom.text.toString().uppercase()
                        //hace algo?
                        /*if(referenciaCm.equals("")){

                        }*/
                        //-----Inicio Tipo 5----- Probando 96-PRC-23040105
                        var llCommit: Boolean
                        keAndroid = conn.writableDatabase

                        //listas con el tipo de datos para los recibos
                        val listaReciboPrCabecera: ArrayList<CXC> = ArrayList()
                        val listaReciboPrLineas: ArrayList<CXC> = ArrayList()
                        val listaReciboCmCabecera: ArrayList<CXC> = ArrayList()
                        val listaReciboCmLineas: ArrayList<CXC> = ArrayList()

                        //por que ordena el array si se supone que es un array de 1?
                        listaDocumentos.sortBy { it.fechaDocs }//llenado de datos de cabecera
                        val cxc = CXC()
                        cxc.id_recibo = nroPrecobranza
                        cxc.tipoRecibo = "W"
                        cxc.codigoVend = codUsuario.toString()
                        cxc.kecxc_id = tasaId
                        cxc.tasadia = tasaCambioSeleccionadaPrincipal
                        cxc.fchrecibo = fechatasaH
                        cxc.clicontesp =
                            listaDocumentos[0].contribesp.toString() //ATENCION, hay que ver como funciona cuando es mas de un documento
                        cxc.moneda = monedaSeleccionadaPr
                        if (ignore) {
                            cxc.bcocod = ""
                            cxc.bcoref = ""
                            cxc.efectivo = montoRec
                            cxc.fchvigen = fechaSuma(fechaActual, 60)
                        } else {
                            cxc.bcocod = codigoBancoCompleto
                            cxc.bcomonto = montoRec
                            cxc.bcoref = referenciaPrincipal
                            cxc.fchvigen = fechaSuma(fechaActual, 5)
                        }

                        cxc.edorec = "0"
                        cxc.fchhr = fechaActual
                        //genero cabecera del complemento
                        val comp = CXC()
                        comp.id_recibo = nroComplemento
                        comp.tipoRecibo = "W"
                        comp.codigoVend = codUsuario.toString()
                        comp.kecxc_id = tasaId
                        comp.tasadia = tasaCambioSeleccionadaPrincipal
                        comp.fchrecibo = fechatasaH
                        comp.clicontesp =
                            listaDocumentos[0].contribesp.toString() //ATENCION, hay que ver como funciona cuando es mas de un documento
                        comp.moneda = monedaSeleccionadaCm
                        if (ignorecm) {
                            comp.bcocod = ""
                            comp.bcoref = ""
                            comp.efectivo = montoRecComp
                            comp.fchvigen = fechaSuma(fechaActual, 60)

                        } else {
                            comp.bcocod = codigoBancoComplemento
                            comp.bcoref = referenciaCm
                            comp.bcomonto = montoRecComp
                            comp.fchvigen = fechaSuma(fechaActual, 5)
                        }
                        comp.edorec = "0"
                        comp.fchhr = fechaActual


                        //en este ciclo lleno la lista de precobradocs
                        for (i in listaDocumentos.indices) {
                            val cxclineas = CXC()

                            cxclineas.id_recibo = nroPrecobranza
                            cxclineas.agencia = listaDocumentos[i].agencia
                            cxclineas.tipodoc = listaDocumentos[i].tipodoc
                            cxclineas.documento = listaDocumentos[i].documento
                            listaReciboPrLineas.add(cxclineas)

                        }

                        //en este ciclo, agrego las retenciones
                        // Porque pote retencion de flete en 0?
                        // de ser el caso que el cliente es contribuyente especial, pòrque no validar eso?
                        for (i in listaReciboPrLineas.indices) {
                            if (listaReciboPrLineas[i].agencia == "002" && (listaReciboPrLineas[i].documento == listaDocumentos[i].documento)) {
                                listaDocumentos[i].cbsretflete = 0.00

                            }

                            //llenado de campos de retenciones
                            //retenciones fase 3
                            for (j in listaRetGuardada.indices) {
                                if (listaRetGuardada[j].nrodoc == listaReciboPrLineas[i].documento) {
                                    if (listaRetGuardada[j].tiporet == "iva") {
                                        //si es de iva
                                        listaReciboPrLineas[i].nroret =
                                            listaRetGuardada[j].nroret
                                        listaReciboPrLineas[i].fchemiret =
                                            listaRetGuardada[j].fecharet
                                        listaReciboPrLineas[i].bsretiva =
                                            -listaRetGuardada[j].montoret
                                        listaReciboPrLineas[i].refret =
                                            listaRetGuardada[j].refret
                                    }

                                    if (listaRetGuardada[j].tiporet == "flete") {
                                        //si es de flete
                                        listaReciboPrLineas[i].nroretfte =
                                            listaRetGuardada[j].nroret
                                        listaReciboPrLineas[i].fchemirfte =
                                            listaRetGuardada[j].fecharet
                                        listaReciboPrLineas[i].bsretfte =
                                            -listaRetGuardada[j].montoret
                                        listaReciboPrLineas[i].refretfte =
                                            listaRetGuardada[j].refret
                                    }

                                    if (listaRetGuardada[j].tiporet == "parme") {
                                        //si es de parme
                                        listaReciboPrLineas[i].retmun_nro =
                                            listaRetGuardada[j].nroret
                                        listaReciboPrLineas[i].retmun_fch =
                                            listaRetGuardada[j].fecharet
                                        listaReciboPrLineas[i].retmun_mto =
                                            -listaRetGuardada[j].montoret
                                        listaReciboPrLineas[i].retmun_cod =
                                            listaRetGuardada[j].refret
                                    }
                                }
                            }


                            //descuento ivas y fletes ( de haberlos)
                            if (listaReciboPrLineas[i].documento == listaDocumentos[i].documento && listaDocumentos[i].tipodocv == "FAC") {
                                //Aunque es el array de las lineas bsmtoiva y doliva se van para la cabecera
                                if (listaDocumentos[i].bsiva - listaDocumentos[i].bsmtoiva <= 0.00) {
                                    listaReciboPrLineas[i].bsmtoiva = 0.00
                                    listaReciboPrLineas[i].doliva = 0.00

                                } else {

                                    //descuento ivas del monto del recibo original .-
                                    val restaIvadol =
                                        listaDocumentos[i].dtotimpuest - listaDocumentos[i].cdretencioniva
                                    //2023-04-03 comentario por no tener en cuenta cuando se escluyen retenciones
                                    //var restaIvadol = listaDocumentos[i].dtotimpuest - listaDocumentos[i].cdretencioniva
                                    val restaIvabss =
                                        listaDocumentos[i].bsiva - listaDocumentos[i].cbsretencioniva
                                    //var restaIvabss = listaDocumentos[i].bsiva - listaDocumentos[i].cbsretencioniva

                                    if (binding.rbCxcDivisasMain.isChecked) {
                                        //hago el descuento del iva del nmonto de pago
                                        montoRec = (montoRec - restaIvadol).valorReal()

                                        if (montoRec < 0.00) {
                                            Toast.makeText(
                                                this, "Monto insuficiente", Toast.LENGTH_SHORT
                                            ).show()
                                            return
                                        }

                                        listaReciboPrLineas[i].bscobro += (listaDocumentos[i].bsiva - listaDocumentos[i].cbsretencioniva)
                                        listaReciboPrLineas[i].bsmtoiva =
                                            listaDocumentos[i].bsiva
                                        listaReciboPrLineas[i].doliva =
                                            listaDocumentos[i].dtotimpuest - listaDocumentos[i].cdretencioniva
                                        //2023-04-03 comentado por mal calculo?
                                        //listaReciboPrLineas[i].tnetoddol  += listaReciboPrLineas[i].doliva
                                        listaReciboPrLineas[i].tnetoddol += (listaDocumentos[i].dtotimpuest - listaDocumentos[i].cdretencioniva)

                                    } else {
                                        montoRec = (montoRec - restaIvabss).valorReal()
                                        if (montoRec < 0.00) {
                                            Toast.makeText(
                                                this, "Monto insuficiente", Toast.LENGTH_SHORT
                                            ).show()
                                            return
                                        }
                                        listaReciboPrLineas[i].bscobro += (listaDocumentos[i].bsiva - listaDocumentos[i].cbsretencioniva)
                                        listaReciboPrLineas[i].bsmtoiva =
                                            listaDocumentos[i].bsiva
                                        listaReciboPrLineas[i].doliva =
                                            listaDocumentos[i].dtotimpuest - listaDocumentos[i].cdretencioniva
                                        listaReciboPrLineas[i].tnetodbs += (listaDocumentos[i].bsiva - listaDocumentos[i].cbsretencioniva)
                                        //listaReciboPrLineas[i].tnetodbs += listaDocumentos[i].bsiva
                                    }
                                }

                                //descuento del flete de los documentos
                                // 1 para indicar que si le falta 1bs de flete ignorarlo?
                                if (listaDocumentos[i].bsflete - listaDocumentos[i].bsmtofte <= 1) {
                                    listaReciboPrLineas[i].bsmtofte = 0.00
                                    listaReciboPrLineas[i].dolflete = 0.00

                                } else {


                                    if (binding.rbCxcDivisasMain.isChecked) {

                                        //si aqui ya llega el monto en 0 o menos
                                        // Pienso que es redundante por que antes se dijo que si no cumple con la suma de iva y flete no pasa
                                        if (montoRec < 0.00) {
                                            Toast.makeText(
                                                this, "Monto insuficiente", Toast.LENGTH_SHORT
                                            ).show()
                                            return
                                        }
                                        //descuento del monto del recibo, el flete
                                        montoRec -= if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dFlete - listaDocumentos[i].cdretflete

                                        //de no llegar a menos de cero, lo agrego al monto cobrado en bss
                                        // por que el boton de excluir retenciones si esta declarado aqui y no en IVA
                                        if (binding.cbExcReten.isChecked) {
                                            listaReciboPrLineas[i].bscobro += listaDocumentos[i].bsflete
                                        } else {
                                            listaReciboPrLineas[i].bscobro += listaDocumentos[i].bsflete - listaDocumentos[i].cbsretflete
                                        }
                                        listaReciboPrLineas[i].bsmtofte =
                                            listaDocumentos[i].bsflete
                                        listaReciboPrLineas[i].dolflete =
                                            if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dFlete - listaDocumentos[i].cdretflete
                                        listaReciboPrLineas[i].tnetoddol += if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dFlete - listaDocumentos[i].cdretflete

                                    } else {

                                        //si aqui ya llega el monto en 0 o menos
                                        if (montoRec < 0.00) {
                                            Toast.makeText(
                                                this, "Monto insuficiente", Toast.LENGTH_SHORT
                                            ).show()
                                            return
                                        }
                                        //descuento del monto del recibo, el flete
                                        montoRec -= if (binding.cbExcReten.isChecked) listaDocumentos[i].bsflete else listaDocumentos[i].bsflete - listaDocumentos[i].cbsretflete
                                        if (montoRec < 0.00) {
                                            Toast.makeText(
                                                this, "Monto insuficiente", Toast.LENGTH_SHORT
                                            ).show()
                                            return
                                        }
                                        //de no llegar a menos de cero, lo agrego al monto cobrado en bss

                                        listaReciboPrLineas[i].bscobro += ((if (binding.cbExcReten.isChecked) listaDocumentos[i].bsflete else listaDocumentos[i].bsflete - listaDocumentos[i].cbsretflete))
                                        listaReciboPrLineas[i].bsmtofte =
                                            listaDocumentos[i].bsflete
                                        listaReciboPrLineas[i].dolflete =
                                            if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dFlete - listaDocumentos[i].cdretflete
                                        listaReciboPrLineas[i].tnetodbs += if (binding.cbExcReten.isChecked) listaDocumentos[i].bsflete else listaDocumentos[i].bsflete - listaDocumentos[i].cbsretflete

                                    }

                                }

                            } else {
                                if (listaDocumentos[i].bsflete - listaDocumentos[i].bsmtofte <= 1) {
                                    listaReciboPrLineas[i].bsmtofte = 0.00
                                    listaReciboPrLineas[i].dolflete = 0.00
                                } else {


                                    val fleteaCobrar =
                                        if (binding.cbExcReten.isChecked) listaDocumentos[i].bsflete else listaDocumentos[i].bsflete - listaDocumentos[i].cbsretflete
                                    listaReciboPrLineas[i].bscobro += fleteaCobrar

                                    if (binding.rbCxcDivisasMain.isChecked) {
                                        montoRec -= if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dFlete - listaDocumentos[i].cdretflete
                                        listaReciboPrLineas[i].bsmtofte =
                                            listaDocumentos[i].bsflete
                                        listaReciboPrLineas[i].dolflete =
                                            if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dFlete - listaDocumentos[i].cdretflete
                                        listaReciboPrLineas[i].tnetoddol += if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dFlete - listaDocumentos[i].cdretflete
                                    } else {
                                        montoRec -= if (binding.cbExcReten.isChecked) listaDocumentos[i].bsflete else listaDocumentos[i].bsflete - listaDocumentos[i].cbsretflete
                                        listaReciboPrLineas[i].bsmtofte =
                                            listaDocumentos[i].bsflete
                                        listaReciboPrLineas[i].dolflete =
                                            if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dFlete - listaDocumentos[i].cdretflete
                                        listaReciboPrLineas[i].tnetodbs += if (binding.cbExcReten.isChecked) listaDocumentos[i].bsflete else listaDocumentos[i].bsflete - listaDocumentos[i].cbsretflete
                                    }

                                }

                            }
                        }

                        //llenado de los netos
                        for (i in listaReciboPrLineas.indices) {


                            if (montoRec < 1) {
                                // borré el return
                            } else {
                                if (binding.rbCxcDivisasMain.isChecked) {
                                    val netoTV = binding.tvCxcNeto.text.toString().toDouble()

                                    if (netoTV > 0.00) {
                                        val netoRealenDoc = listaDocumentos[i].netorestante

                                        if (montoRec >= netoRealenDoc) {
                                            val bscobrado =
                                                netoRealenDoc * listaDocumentos[i].tasadoc

                                            listaReciboPrLineas[i].bscobro += bscobrado


                                            netocobrado = listaDocumentos[i].netorestante
                                            listaReciboPrLineas[i].dolneto = netocobrado
                                            montoRec -= netocobrado
                                            listaReciboPrLineas[i].ispagadoTotal = "1"
                                            listaReciboPrLineas[i].tnetoddol += netocobrado
                                        } else if (netoRealenDoc > montoRec && montoRec > 0) {

                                            val cobroAbono =
                                                montoRec * listaDocumentos[i].tasadoc

                                            listaReciboPrLineas[i].bscobro += cobroAbono
                                            netocobrado = montoRec
                                            listaReciboPrLineas[i].dolneto =
                                                valorReal(netocobrado)
                                            montoRec -= netocobrado
                                            montoRec = valorReal(montoRec)
                                            listaReciboPrLineas[i].ispagadoTotal = "0"
                                            listaReciboPrLineas[i].tnetoddol += netocobrado
                                        }

                                    }
                                }

                                if (binding.rbCxcBssMain.isChecked) {
                                    var netoRealenDoc = listaDocumentos[i].netorestante
                                    netoRealenDoc = valorReal(netoRealenDoc)

                                    //si el monto del recibo cubre el monto del documento
                                    if (montoRec >= (netoRealenDoc * tasaCambioSeleccionadaPrincipal)) {
                                        val bscobrado =
                                            valorReal(netoRealenDoc * listaDocumentos[i].tasadoc)
                                        listaReciboPrLineas[i].bscobro += bscobrado
                                        netocobrado = listaDocumentos[i].netorestante
                                        listaReciboPrLineas[i].dolneto = netocobrado
                                        listaReciboPrLineas[i].bsneto =
                                            valorReal(netoRealenDoc * tasaCambioSeleccionadaPrincipal)
                                        listaReciboPrLineas[i].tnetodbs += valorReal(
                                            netoRealenDoc * tasaCambioSeleccionadaPrincipal
                                        )
                                        listaReciboPrLineas[i].ispagadoTotal = "1"
                                        montoRec -= valorReal(netoRealenDoc * tasaCambioSeleccionadaPrincipal)

                                    }
                                    //si el monto del recibo no cubre el monto completo del documento (se comporta como un abono)
                                    else if ((netoRealenDoc * tasaCambioSeleccionadaPrincipal) > montoRec && montoRec > 0) {
                                        netocobrado = montoRec / tasaCambioSeleccionadaPrincipal
                                        val cobroAbono =
                                            netocobrado * listaDocumentos[i].tasadoc
                                        listaReciboPrLineas[i].bscobro += valorReal(cobroAbono)
                                        val dolneto = netocobrado
                                        listaReciboPrLineas[i].dolneto = valorReal(dolneto)
                                        listaReciboPrLineas[i].bsneto = montoRec
                                        listaReciboPrLineas[i].tnetodbs += montoRec
                                        listaReciboPrLineas[i].ispagadoTotal = "0"
                                        montoRec -= montoRec
                                    }
                                }


                                /*recorrido de lista de descuentos para asignar cantidad según nro doc. (de tener)*/
                                for (j in listaDescuentos.indices) {
                                    if (listaDescuentos[j].nrodoc == listaDocumentos[i].documento) {
                                        listaReciboPrLineas[i].prcdsctopp =
                                            listaDescuentos[j].pordscto
                                    }
                                }

                            }
                        }

                        //recorrido del complemento
                        //tambien voy a pagar el neto pero con complemento de los documentos ya pagos
                        //las lineas del recibo del complemento
                        for (i in listaReciboPrLineas.indices) {
                            //valido si fue pagado completo
                            if (listaReciboPrLineas[i].ispagadoTotal == "1") {


                            } else {
                                //de no estar pago completo, le aplico el complemento
                                val complineas = CXC()
                                complineas.id_recibo = nroComplemento
                                complineas.agencia = listaReciboPrLineas[i].agencia
                                complineas.tipodoc = listaReciboPrLineas[i].tipodoc
                                complineas.documento = listaReciboPrLineas[i].documento
                                listaReciboCmLineas.add(complineas)
                            }

                        }

                        //llenado neto del complemento
                        for (i in listaReciboCmLineas.indices) {

                            if (montoRecComp <= 0) {

                            } else {
                                if (binding.rbCxcDivisasCom.isChecked) {
                                    for (j in listaDocumentos.indices) {
                                        if (listaDocumentos[j].documento == listaReciboCmLineas[i].documento) {
                                            val netoRealenDoc =
                                                listaDocumentos[j].netorestante - listaReciboPrLineas[j].dolneto

                                            if (montoRecComp >= netoRealenDoc) {

                                                val bscobrado =
                                                    netoRealenDoc * listaDocumentos[j].tasadoc
                                                // 2023-03-27 muy redundante
                                                //bscobrado     = bscobrado

                                                listaReciboCmLineas[j].bscobro += bscobrado


                                                netocobrado =
                                                    listaDocumentos[j].netorestante - listaReciboPrLineas[j].dolneto
                                                listaReciboCmLineas[i].dolneto = netocobrado
                                                listaReciboCmLineas[i].tnetoddol += netocobrado
                                                montoRecComp -= netocobrado

                                            } else if (netoRealenDoc > montoRecComp && montoRecComp > 0) {

                                                val cobroAbono =
                                                    montoRecComp * listaDocumentos[j].tasadoc

                                                listaReciboCmLineas[i].bscobro += cobroAbono
                                                netocobrado = montoRecComp
                                                listaReciboCmLineas[i].dolneto =
                                                    valorReal(netocobrado)
                                                montoRecComp = valorReal(montoRecComp)
                                                listaReciboCmLineas[i].tnetoddol += netocobrado
                                                montoRecComp -= netocobrado

                                            }

                                        } else {
                                            //do nothing papa
                                        }
                                    }

                                }

                                if (binding.rbCxcBssCom.isChecked) {
                                    for (j in listaDocumentos.indices) {
                                        if (listaReciboCmLineas[i].documento == listaDocumentos[j].documento) {
                                            var netoRealenDoc =
                                                listaDocumentos[j].netorestante - listaReciboPrLineas[j].dolneto
                                            netoRealenDoc = valorReal(netoRealenDoc)

                                            //si el monto del recibo cubre el monto del documento
                                            if (montoRecComp >= (netoRealenDoc * tasaCambioSeleccionadaPrincipal)) {
                                                val bscobrado =
                                                    valorReal(netoRealenDoc * listaDocumentos[j].tasadoc)
                                                listaReciboCmLineas[i].bscobro += bscobrado

                                                netocobrado =
                                                    listaDocumentos[j].netorestante - listaReciboPrLineas[j].dolneto
                                                listaReciboCmLineas[i].dolneto = netocobrado
                                                listaReciboCmLineas[i].bsneto =
                                                    valorReal(netoRealenDoc * tasaCambioSeleccionadaPrincipal)
                                                listaReciboCmLineas[i].tnetodbs += valorReal(
                                                    netoRealenDoc * tasaCambioSeleccionadaPrincipal
                                                )
                                                montoRecComp -= valorReal(netoRealenDoc * listaDocumentos[j].tasadoc)
                                            }

                                            //si el monto del recibo no cubre el monto completo del documento (se comporta como un abono)
                                            else if ((netoRealenDoc * tasaCambioSeleccionadaPrincipal) > montoRecComp && montoRecComp > 0) {


                                                val dolneto =
                                                    (montoRecComp / tasaCambioSeleccionadaPrincipal)
                                                val cobroAbono =
                                                    valorReal(dolneto) * listaDocumentos[j].tasadoc
                                                listaReciboCmLineas[i].bscobro += valorReal(
                                                    cobroAbono
                                                )
                                                listaReciboCmLineas[i].dolneto =
                                                    valorReal(dolneto)
                                                listaReciboCmLineas[i].bsneto = montoRecComp
                                                listaReciboCmLineas[i].tnetodbs += montoRecComp
                                                montoRecComp -= montoRecComp

                                            }
                                        }
                                    }
                                }
                            }
                        }


                        /*esto va a ser resultado de la suma de los campos de la lista (ke_precobranza)
                         de detalles */
                        val difReteIva =
                            valorReal(listaReciboPrLineas.sumOf { it.bsmtoiva }) + valorReal(
                                listaReciboPrLineas.sumOf { it.bsretiva })
                        val difRetyFlete =
                            valorReal(listaReciboPrLineas.sumOf { it.bsmtofte }) + valorReal(
                                listaReciboPrLineas.sumOf { it.bsretfte })
                        var netoReal =
                            valorReal(listaReciboPrLineas.sumOf { it.bscobro }) - difReteIva - difRetyFlete //--revisar si esto es necesario
                        cxc.bsneto = valorReal(listaReciboPrLineas.sumOf { it.bsneto })
                        cxc.bsretiva = valorReal(listaReciboPrLineas.sumOf { it.bsretiva })
                        cxc.bsiva = valorReal(listaReciboPrLineas.sumOf { it.bsmtoiva })
                        cxc.bsflete = valorReal(listaReciboPrLineas.sumOf { it.bsmtofte })

                        /* sumo los bss en total y los redondeo al momento de guardarlo */
                        var bssumaTotal = valorReal(listaReciboPrLineas.sumOf { it.bscobro })

                        //cxc.bstotal    = valorReal(bssumaTotal)
                        cxc.bstotal =
                            valorReal(listaReciboPrLineas.sumOf { it.bsneto } + (listaReciboPrLineas.sumOf { it.bsmtoiva } + listaReciboPrLineas.sumOf { it.bsretiva }) + (listaReciboPrLineas.sumOf { it.bsmtofte } + listaReciboPrLineas.sumOf { it.bsretfte }))
                        //cxc.dolneto    = valorReal(listaDocumentos.sumOf{it.dtotneto }) //<-------------------- Revisar si es necesario
                        cxc.dolneto = valorReal(listaReciboPrLineas.sumOf { it.dolneto })
                        cxc.doliva = valorReal(listaReciboPrLineas.sumOf { it.doliva })
                        //cxc.dolretiva  = valorReal(listaDocumentos.sumOf { it.cdretencioniva })
                        cxc.dolflete = valorReal(listaReciboPrLineas.sumOf { it.dolflete })
                        cxc.doltotal =
                            valorReal(listaReciboPrLineas.sumOf { it.dolneto } + listaReciboPrLineas.sumOf { it.doliva } + listaReciboPrLineas.sumOf { it.dolflete })
                        cxc.netocob =
                            binding.etCxcMontoMain.text.toString().ifEmpty { "0.0" }.toDouble()
                        //if (monedaSeleccionadaPr == "2") cxc.doltotal + superSaldoFavor() else cxc.bstotal + superSaldoFavor()
                        println(valorReal(superSaldoFavor() * tasaCambioSeleccionadaPrincipal))
                        println(cxc.bstotal)
                        println(cxc.netocob)
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
                        cxc.moneda = monedaSeleccionadaPr
                        cxc.tasadia = tasaCambioSeleccionadaPrincipal
                        listaReciboPrCabecera.add(cxc)

                        try {

                            // inicio la transacción
                            keAndroid.beginTransaction()
                            val qcabecera = ContentValues()
                            val qlineas = ContentValues()

                            for (i in listaReciboPrCabecera.indices) {
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
                                if (ignore) {
                                    qcabecera.put("efectivo", listaReciboPrCabecera[i].efectivo)
                                } else {
                                    qcabecera.put("bcocod", listaReciboPrCabecera[i].bcocod)
                                    qcabecera.put("bcomonto", listaReciboPrCabecera[i].bcomonto)
                                    qcabecera.put("bcoref", listaReciboPrCabecera[i].bcoref)
                                }
                                qcabecera.put("edorec", listaReciboPrCabecera[i].edorec)
                                qcabecera.put("fchvigen", listaReciboPrCabecera[i].fchvigen)
                                qcabecera.put("bsretflete", listaReciboPrCabecera[i].bsretflete)
                                qcabecera.put("netocob", listaReciboPrCabecera[i].netocob)
                                qcabecera.put("fechamodifi", getFechaHoy())
                                qcabecera.put(
                                    "tipo_pago",
                                    if (binding.rbCxcAbonoMain.isChecked) 1 else 0
                                )
                                qcabecera.put(
                                    "complemento",
                                    if (binding.cbCxcComplemento.isChecked) nroComplemento else ""
                                )
                                //qcabecera.put("docdifcamb",diferencialCambiario)

                                for (j in listaReciboPrLineas.indices) {
                                    qlineas.put("cxcndoc", listaReciboPrLineas[j].id_recibo)
                                    qlineas.put("agencia", listaReciboPrLineas[j].agencia)
                                    qlineas.put("tipodoc", listaReciboPrLineas[j].tipodoc)
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
                                    qlineas.put("codcliente", listaDocumentos[i].codcliente)
                                    qlineas.put(
                                        "nombrecli",
                                        conn.getCampoStringCamposVarios(
                                            "cliempre",
                                            "nombre",
                                            listOf("codigo", "empresa"),
                                            listOf(listaDocumentos[i].codcliente, codEmpresa!!)
                                        )
                                    )
                                    qlineas.put("tasadoc", listaDocumentos[j].tasadoc)
                                    //2023-10-19 esto es mal pero debido a la falta de tiempo va asi
                                    qlineas.put(
                                        "cbsretiva",
                                        conn.getCampoDoubleCamposVarios(
                                            "ke_doccti",
                                            "cbsretiva",
                                            listOf("documento", "empresa"),
                                            listOf(listaDocumentos[i].documento, codEmpresa!!)
                                        )
                                    )
                                    qlineas.put(
                                        "cbsretflete",
                                        conn.getCampoDoubleCamposVarios(
                                            "ke_doccti",
                                            "cbsretflete",
                                            listOf("documento", "empresa"),
                                            listOf(listaDocumentos[i].documento, codEmpresa!!)
                                        )
                                    )
                                    keAndroid.insert("ke_precobradocs", null, qlineas)
                                }
                            }
                            //ke_android.execSQL("UPDATE ke_precobradocs SET afavor= ${guardaSaldoFavor(montoRec, binding.rbCxcDivisasMain, binding.rbCxcCompMain, binding.etCxcMontoMain.text.toString().toDouble(), binding.tvCxcTotal)} WHERE cxcndoc='${listaReciboPrLineas[listaReciboPrLineas.size-1].id_recibo}' AND documento = '${listaReciboPrLineas[listaReciboPrLineas.size-1].documento}';")
                            keAndroid.insert("ke_precobranza", null, qcabecera)

                            conn.saveImg(
                                listaImagenes,
                                nroPrecobranza,
                                this
                            ) // <-- Guardando Imagenes

                            val qcorrelativo = ContentValues()
                            qcorrelativo.put("kcor_numero", nroCorrelativo)
                            qcorrelativo.put("kcor_vendedor", codUsuario)

                            keAndroid.insert("ke_corprec", null, qcorrelativo)
                            llCommit = true

                        } catch (exception: SQLException) {

                            exception.printStackTrace()
                            llCommit = false

                            keAndroid.endTransaction()
                            if (!llCommit) {
                                return
                            }
                        }


                        //preparacion para el guardado de datos
                        var netoCmReal = valorReal(listaReciboCmLineas.sumOf { it.bscobro })
                        comp.bsneto = valorReal(listaReciboCmLineas.sumOf { it.bsneto })

                        /* sumo los bss en total y los redondeo al momento de guardarlo */
                        var bssumaTotalCm = valorReal(listaReciboCmLineas.sumOf { it.bscobro })

                        comp.bstotal = comp.bsneto
                        comp.dolneto = valorReal(listaReciboCmLineas.sumOf { it.dolneto })
                        var doltotalCm = binding.etCxcMontoCom.text.toString().toDouble()

                        comp.doltotal = comp.dolneto
                        comp.netocob =
                            binding.etCxcMontoCom.text.toString().ifEmpty { "0.0" }.toDouble()
                        //if (monedaSeleccionadaCm == "2") listaReciboCmLineas.sumOf { it.dolneto } else listaReciboCmLineas.sumOf { it.bsneto }
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
                        comp.moneda = monedaSeleccionadaCm
                        comp.tasadia = tasaCambioSeleccionadaPrincipal
                        listaReciboCmCabecera.add(comp)

                        try {

                            val qcabecera = ContentValues()
                            val qlineas = ContentValues()

                            for (i in listaReciboCmCabecera.indices) {

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
                                if (ignorecm) {
                                    qcabecera.put("efectivo", listaReciboCmCabecera[i].efectivo)
                                } else {
                                    qcabecera.put("bcocod", listaReciboCmCabecera[i].bcocod)
                                    qcabecera.put("bcomonto", listaReciboCmCabecera[i].bcomonto)
                                    qcabecera.put("bcoref", listaReciboCmCabecera[i].bcoref)
                                }
                                qcabecera.put("edorec", listaReciboCmCabecera[i].edorec)
                                qcabecera.put("fchvigen", listaReciboCmCabecera[i].fchvigen)
                                qcabecera.put("netocob", listaReciboCmCabecera[i].netocob)
                                qcabecera.put("fechamodifi", getFechaHoy())
                                qcabecera.put(
                                    "tipo_pago",
                                    if (binding.rbCxcAbonoMain.isChecked) 1 else 0
                                )
                                qcabecera.put(
                                    "complemento",
                                    if (binding.cbCxcComplemento.isChecked) nroPrecobranza else ""
                                )
                                //qcabecera.put("docdifcamb",diferencialCambiario)

                                for (j in listaReciboCmLineas.indices) {
                                    qlineas.put("cxcndoc", listaReciboCmLineas[j].id_recibo)
                                    qlineas.put("agencia", listaReciboCmLineas[j].agencia)
                                    qlineas.put("tipodoc", listaReciboCmLineas[j].tipodoc)
                                    qlineas.put("documento", listaReciboCmLineas[j].documento)
                                    qlineas.put("bscobro", listaReciboCmLineas[j].bscobro)
                                    qlineas.put("tnetoddol", listaReciboCmLineas[j].tnetoddol)
                                    qlineas.put("tnetodbs", listaReciboCmLineas[j].tnetodbs)
                                    qlineas.put("fchrecibod", getFechaNow())
                                    qlineas.put("kecxc_idd", listaReciboCmCabecera[i].kecxc_id)
                                    qlineas.put("tasadiad", listaReciboCmCabecera[i].tasadia)
                                    qlineas.put("reten", 1)
                                    qlineas.put("codcliente", listaDocumentos[i].codcliente)
                                    qlineas.put(
                                        "nombrecli",
                                        conn.getCampoStringCamposVarios(
                                            "cliempre",
                                            "nombre",
                                            listOf("codigo", "empresa"),
                                            listOf(listaDocumentos[i].codcliente, codEmpresa!!)
                                        )
                                    )
                                    qlineas.put("tasadoc", listaDocumentos[j].tasadoc)
                                    //2023-10-19 esto es mal pero debido a la falta de tiempo va asi
                                    qlineas.put(
                                        "cbsretiva",
                                        conn.getCampoDoubleCamposVarios(
                                            "ke_doccti",
                                            "cbsretiva",
                                            listOf("documento", "empresa"),
                                            listOf(listaDocumentos[i].documento, codEmpresa!!)
                                        )
                                    )
                                    qlineas.put(
                                        "cbsretflete",
                                        conn.getCampoDoubleCamposVarios(
                                            "ke_doccti",
                                            "cbsretflete",
                                            listOf("documento", "empresa"),
                                            listOf(listaDocumentos[i].documento, codEmpresa!!)
                                        )
                                    )
                                    keAndroid.insert("ke_precobradocs", null, qlineas)
                                }

                            }
                            //ke_android.execSQL("UPDATE ke_precobradocs SET afavor= ${guardaSaldoFavor(montoRecComp, binding.rbCxcDivisasCom, binding.rbCxcCompMain, (binding.etCxcMontoCom.text.toString().toDouble() + binding.etCxcMontoCom.text.toString().toDouble()) , binding.tvCxcTotal)} WHERE cxcndoc='${listaReciboCmLineas[listaReciboCmLineas.size-1].id_recibo}' AND documento = '${listaReciboCmLineas[listaReciboCmLineas.size-1].documento}';")
                            guardaSaldoFavor2(
                                binding.rbCxcDivisasCom,
                                binding.rbCxcCompMain,
                                binding.etCxcMontoMain.text.toString().toDouble(),
                                binding.etCxcMontoCom.text.toString().toDouble(),
                                binding.tvCxcTotal,
                                listaReciboCmLineas[listaReciboCmLineas.size - 1].id_recibo,
                                listaReciboCmLineas[listaReciboCmLineas.size - 1].documento,
                                binding.rbCxcDivisasCom.isChecked
                            )
                            keAndroid.insert("ke_precobranza", null, qcabecera)

                            val qcorrelativo = ContentValues()
                            qcorrelativo.put("kcor_numero", nroCorrelativoCom)
                            qcorrelativo.put("kcor_vendedor", codUsuario)

                            keAndroid.insert("ke_corprec", null, qcorrelativo)
                            llCommit = true


                        } catch (exception: SQLException) {
                            exception.printStackTrace()
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
                            listadatos.add(cxc)
                            listadatos[0].cliente = codigoCliente

                            val dialog = DialogRecibo()
                            dialog.DialogRecibo(this, listadatos)

                            Toast.makeText(this, "RECIBO CREADO", Toast.LENGTH_SHORT).show()
                            // finish()
                        } else {
                            keAndroid.endTransaction()
                        }
                    }
                }
            } else if (montoRec >= montoMinimoRec && !binding.cbCxcComplemento.isChecked) {
                //-----Inicio de la ruta buena-----
                var llCommit: Boolean
                keAndroid = conn.writableDatabase

                //listas con el tipo de datos para los recibos
                val listaReciboPrCabecera: ArrayList<CXC> = ArrayList()
                val listaReciboPrLineas: ArrayList<CXC> = ArrayList()

                listaDocumentos.sortBy { it.fechaDocs }//llenado de datos de cabecera
                val cxc = CXC()
                cxc.id_recibo = nroPrecobranza
                cxc.tipoRecibo = "W"
                cxc.codigoVend = codUsuario.toString()
                cxc.kecxc_id = tasaId
                cxc.tasadia = tasaCambioSeleccionadaPrincipal
                cxc.fchrecibo = fechatasaH
                cxc.clicontesp =
                    listaDocumentos[0].contribesp.toString() //esto lo jalo de  la lista de docs?
                cxc.moneda = monedaSeleccionadaPr
                if (ignore) {// posible problema de la suma de fechas
                    cxc.bcocod = ""
                    cxc.bcoref = ""
                    cxc.efectivo = montoRec
                    cxc.fchvigen = fechaSuma(fechaActual, 60)
                } else {
                    cxc.bcocod = codigoBancoCompleto
                    cxc.bcomonto = montoRec
                    cxc.bcoref = referenciaPrincipal
                    cxc.fchvigen = fechaSuma(fechaActual, 5)
                }
                cxc.edorec = "0"
                cxc.fchhr = fechaActual


                //en este ciclo lleno la lista de precobradocs
                for (i in listaDocumentos.indices) {
                    val cxclineas = CXC()

                    cxclineas.id_recibo = nroPrecobranza
                    cxclineas.agencia = listaDocumentos[i].agencia
                    cxclineas.tipodoc = listaDocumentos[i].tipodoc
                    cxclineas.documento = listaDocumentos[i].documento
                    listaReciboPrLineas.add(cxclineas)

                }

                //en este ciclo, agrego las retenciones
                for (i in listaReciboPrLineas.indices) {
                    if (listaReciboPrLineas[i].agencia == "002" && (listaReciboPrLineas[i].documento == listaDocumentos[i].documento)) {
                        listaDocumentos[i].cbsretflete = 0.00

                    }

                    //llenado de campos de retenciones
                    //retenciones fase 4
                    for (j in listaRetGuardada.indices) {
                        if (listaReciboPrLineas[i].tipodoc == "N/E") {

                        } else {
                            if (listaRetGuardada[j].nrodoc == listaReciboPrLineas[i].documento) {
                                if (listaRetGuardada[j].tiporet == "iva") {
                                    //si es de iva
                                    listaReciboPrLineas[i].nroret = listaRetGuardada[j].nroret
                                    listaReciboPrLineas[i].fchemiret =
                                        listaRetGuardada[j].fecharet
                                    listaReciboPrLineas[i].bsretiva =
                                        -listaRetGuardada[j].montoret
                                    listaReciboPrLineas[i].refret = listaRetGuardada[j].refret
                                }

                                if (listaRetGuardada[j].tiporet == "flete") {
                                    //si es de flete
                                    listaReciboPrLineas[i].nroretfte =
                                        listaRetGuardada[j].nroret
                                    listaReciboPrLineas[i].fchemirfte =
                                        listaRetGuardada[j].fecharet
                                    listaReciboPrLineas[i].bsretfte =
                                        -listaRetGuardada[j].montoret
                                    listaReciboPrLineas[i].refretfte =
                                        listaRetGuardada[j].refret
                                }

                                if (listaRetGuardada[j].tiporet == "parme") {
                                    //si es de parme
                                    listaReciboPrLineas[i].retmun_nro =
                                        listaRetGuardada[j].nroret
                                    listaReciboPrLineas[i].retmun_fch =
                                        listaRetGuardada[j].fecharet
                                    listaReciboPrLineas[i].retmun_mto =
                                        -listaRetGuardada[j].montoret
                                    listaReciboPrLineas[i].retmun_cod =
                                        listaRetGuardada[j].refret
                                }
                            }
                        }

                    }


                    //descuento ivas y fletes ( de haberlos)
                    if (listaReciboPrLineas[i].documento == listaDocumentos[i].documento && listaDocumentos[i].tipodocv == "FAC") {

                        if (listaDocumentos[i].bsiva - listaDocumentos[i].bsmtoiva <= 0.00) {
                            listaReciboPrLineas[i].bsmtoiva = 0.00
                            listaReciboPrLineas[i].doliva = 0.00

                        } else {

                            //descuento ivas del monto del recibo original .-
                            //val restaIvadol =
                            //    if (binding.cbExcReten.isChecked) listaDocumentos[i].dtotimpuest else listaDocumentos[i].dtotimpuest - listaDocumentos[i].cdretencioniva
                            val restaIvadol =
                                listaDocumentos[i].dtotimpuest - listaDocumentos[i].cdretencioniva
                            //2023-04-03 comentario por no tener en cuenta cuando se escluyen retenciones
                            //var restaIvadol = listaDocumentos[i].dtotimpuest - listaDocumentos[i].cdretencioniva
//                            val restaIvabss =
//                                if (binding.cbExcReten.isChecked) listaDocumentos[i].bsiva else listaDocumentos[i].bsiva - listaDocumentos[i].cbsretencioniva
                            val restaIvabss =
                                listaDocumentos[i].bsiva - listaDocumentos[i].cbsretencioniva
                            //var restaIvabss = listaDocumentos[i].bsiva - listaDocumentos[i].cbsretencioniva

                            if (binding.rbCxcDivisasMain.isChecked) {
                                //hago el descuento del iva del nmonto de pago
                                montoRec = (montoRec - restaIvadol).valorReal()

                                if (montoRec < 0.00) {
                                    Toast.makeText(
                                        this,
                                        "Monto insuficiente",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                    return
                                }

                                listaReciboPrLineas[i].bscobro += listaDocumentos[i].bsiva - listaDocumentos[i].cbsretencioniva
                                listaReciboPrLineas[i].bsmtoiva = listaDocumentos[i].bsiva
                                listaReciboPrLineas[i].doliva =
                                    listaDocumentos[i].dtotimpuest - listaDocumentos[i].cdretencioniva
                                //2023-04-03 comentado por mal calculo?
                                //listaReciboPrLineas[i].tnetoddol  += listaReciboPrLineas[i].doliva
                                listaReciboPrLineas[i].tnetoddol += listaDocumentos[i].dtotimpuest - listaDocumentos[i].cdretencioniva

                            } else {
                                montoRec = (montoRec - restaIvabss).valorReal()
                                if (montoRec < 0.00) {
                                    Toast.makeText(
                                        this,
                                        "Monto insuficiente",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                    return
                                }
                                listaReciboPrLineas[i].bscobro += listaDocumentos[i].bsiva - listaDocumentos[i].cbsretencioniva
                                listaReciboPrLineas[i].bsmtoiva = listaDocumentos[i].bsiva
                                listaReciboPrLineas[i].doliva =
                                    listaDocumentos[i].dtotimpuest - listaDocumentos[i].cdretencioniva
                                listaReciboPrLineas[i].tnetodbs += listaDocumentos[i].bsiva - listaDocumentos[i].cbsretencioniva
                                //listaReciboPrLineas[i].tnetodbs += listaDocumentos[i].bsiva

                            }
                        }

                        //descuento del flete de los documentos
                        if (listaDocumentos[i].bsflete - listaDocumentos[i].bsmtofte <= 1) {
                            listaReciboPrLineas[i].bsmtofte = 0.00
                            listaReciboPrLineas[i].dolflete = 0.00

                        } else {


                            if (binding.rbCxcDivisasMain.isChecked) {

                                //si aqui ya llega el monto en 0 o menos
                                if (montoRec < 0.00) {
                                    Toast.makeText(
                                        this,
                                        "Monto insuficiente",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                    return
                                }
                                //descuento del monto del recibo, el flete
                                montoRec -= valorReal((if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dFlete - listaDocumentos[i].cdretflete))

                                //de no llegar a menos de cero, lo agrego al monto cobrado en bss
                                //si el boton esta presionado, entonces agrego el flete completo, de no estarlo, agrego el flete menos el monto de la retención.
                                listaReciboPrLineas[i].bscobro += if (binding.cbExcReten.isChecked) listaDocumentos[i].bsflete else listaDocumentos[i].bsflete - listaDocumentos[i].cbsretflete/*if(binding.cbExcReten.isChecked){
                                    listaReciboPrLineas[i].bscobro  += listaDocumentos[i].bsflete
                                }
                                else{
                                    listaReciboPrLineas[i].bscobro  += listaDocumentos[i].bsflete - listaDocumentos[i].cbsretflete
                                }*/

                                listaReciboPrLineas[i].bsmtofte = listaDocumentos[i].bsflete
                                listaReciboPrLineas[i].dolflete =
                                    if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dFlete - listaDocumentos[i].cdretflete
                                listaReciboPrLineas[i].tnetoddol += if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dFlete - listaDocumentos[i].cdretflete

                            } else {

                                //si aqui ya llega el monto en 0 o menos
                                if (montoRec < 0.00) {
                                    Toast.makeText(
                                        this,
                                        "Monto insuficiente",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                    return
                                }
                                //descuento del monto del recibo, el flete
                                montoRec -= valorReal(if (binding.cbExcReten.isChecked) listaDocumentos[i].bsflete else listaDocumentos[i].bsflete - listaDocumentos[i].cbsretflete)
                                if (montoRec < 0.00) {
                                    Toast.makeText(
                                        this,
                                        "Monto insuficiente",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                    return
                                }
                                //de no llegar a menos de cero, lo agrego al monto cobrado en bss

                                listaReciboPrLineas[i].bscobro += if (binding.cbExcReten.isChecked) listaDocumentos[i].bsiva else listaDocumentos[i].bsiva - listaDocumentos[i].cbsretencioniva
                                listaReciboPrLineas[i].bsmtofte = listaDocumentos[i].bsflete
                                listaReciboPrLineas[i].dolflete =
                                    if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dFlete - listaDocumentos[i].cdretflete
                                listaReciboPrLineas[i].tnetodbs += if (binding.cbExcReten.isChecked) listaDocumentos[i].bsflete else listaDocumentos[i].bsflete - listaDocumentos[i].cbsretflete

                            }

                        }

                    } else {
                        if (listaDocumentos[i].bsflete - listaDocumentos[i].bsmtofte <= 1) {
                            listaReciboPrLineas[i].bsmtofte = 0.00
                            listaReciboPrLineas[i].dolflete = 0.00
                        } else {


                            val fleteaCobrar =
                                valorReal(if (binding.cbExcReten.isChecked) listaDocumentos[i].bsflete else listaDocumentos[i].bsflete - listaDocumentos[i].cbsretflete)
                            listaReciboPrLineas[i].bscobro += fleteaCobrar

                            if (binding.rbCxcDivisasMain.isChecked) {
                                montoRec -= valorReal(if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dFlete - listaDocumentos[i].cdretflete)
                                listaReciboPrLineas[i].bsmtofte = listaDocumentos[i].bsflete
                                listaReciboPrLineas[i].dolflete =
                                    if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dFlete - listaDocumentos[i].cdretflete

                                //2023-03-28 Se cambio ya que se deben de guardar los dolares, ya que se selecciono divisa, y lo estaba guardando en bolivares
                                //listaReciboPrLineas[i].tnetodbs  += listaDocumentos[i].bsflete
                                listaReciboPrLineas[i].tnetoddol += if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dFlete - listaDocumentos[i].cdretflete
                            } else {
                                montoRec -= valorReal(if (binding.cbExcReten.isChecked) listaDocumentos[i].bsflete else listaDocumentos[i].bsflete - listaDocumentos[i].cbsretflete)
                                listaReciboPrLineas[i].bsmtofte = listaDocumentos[i].bsflete
                                listaReciboPrLineas[i].dolflete =
                                    if (binding.cbExcReten.isChecked) listaDocumentos[i].dFlete else listaDocumentos[i].dFlete - listaDocumentos[i].cdretflete
                                listaReciboPrLineas[i].tnetodbs += if (binding.cbExcReten.isChecked) listaDocumentos[i].bsflete else listaDocumentos[i].bsflete - listaDocumentos[i].cbsretflete
                                //print("a")
                            }
                        }
                    }
                }
                montoRec = valorReal(montoRec)
                //llenado de los netos
                for (i in listaReciboPrLineas.indices) {
                    if (montoRec < 1) {
                        // borré el return
                    } else {
                        if (binding.rbCxcDivisasMain.isChecked) {
                            val netoTV = binding.tvCxcNeto.text.toString().toDouble()

                            if (netoTV > 0.00) {
                                val netoRealenDoc = listaDocumentos[i].netorestante

                                //Buscando el porcentaje de descuento del documento a cobrar
                                val porcentajeDescuento =
                                    cantidadDeDescuento(listaDocumentos[i].documento)
                                //Calculando cuanto del neto se le debe de restar al documento
                                val netoRestaDesc =
                                    (listaDocumentos[i].netorestante * (porcentajeDescuento / 100))

                                //Aqui si es valido poner valorReal porque en ocasiones la resta deja demasiado decimales que alteran al if
                                val netoDesc = valorReal(netoRealenDoc - netoRestaDesc)

                                if (montoRec >= netoDesc) {
                                    val bscobrado = netoRealenDoc * listaDocumentos[i].tasadoc

                                    listaReciboPrLineas[i].bscobro += valorReal(bscobrado)

                                    //Buscando el porcentaje de descuento del documento a cobrar
                                    val porcentajeDescuento =
                                        cantidadDeDescuento(listaDocumentos[i].documento)
                                    //Calculando cuanto del neto se le debe de restar al documento
                                    val netoRestaDesc =
                                        (listaDocumentos[i].netorestante * (porcentajeDescuento / 100))

                                    // 2023-06-01 se comento para poder aplicar el descuento a tnetoddol
                                    //netocobrado =  listaDocumentos[i].netorestante
                                    //val netoRestaDesc = (listaDocumentos[i].netorestante * (cantidadDeDescuento / 100))
                                    //Quitando descuento al neto
                                    netocobrado = if (binding.tvCxcDctos.text.toString()
                                            .toDouble() > 0.0
                                    ) valorReal(listaDocumentos[i].netorestante - netoRestaDesc) else listaDocumentos[i].netorestante
                                    listaReciboPrLineas[i].dolneto = valorReal(netocobrado)
                                    listaReciboPrLineas[i].tnetoddol += netocobrado
                                    //2023-05-11 Sele agrego que al netocobrado (es decir lo que el cliente dio luego de ser cobrado iva, flete y el neto) se le reste ademas el descuento
                                    //para que si aun queda un poco de dinero sobrante se guarde futuramente como saldo a favor
                                    //montoRec -= netocobrado
                                    montoRec -= (netocobrado - binding.tvCxcDctos.text.toString()
                                        .toDouble())
                                } else if (netoDesc > montoRec && montoRec > 0) {

                                    val cobroAbono = montoRec * listaDocumentos[i].tasadoc

                                    listaReciboPrLineas[i].bscobro += cobroAbono
                                    val netocobrado = montoRec
                                    listaReciboPrLineas[i].dolneto = valorReal(netocobrado)
                                    listaReciboPrLineas[i].tnetoddol += netocobrado
                                    montoRec -= netocobrado
                                    montoRec = valorReal(montoRec)

                                }
                            }
                        }

                        if (binding.rbCxcBssMain.isChecked) {
                            var netoRealenDoc = listaDocumentos[i].netorestante
                            netoRealenDoc = valorReal(netoRealenDoc)

                            //si el monto del recibo cubre el monto del documento
                            if (montoRec >= (netoRealenDoc * tasaCambioSeleccionadaPrincipal)) {
                                val bscobrado =
                                    valorReal(netoRealenDoc * listaDocumentos[i].tasadoc)
                                listaReciboPrLineas[i].bscobro += bscobrado
                                netocobrado = listaDocumentos[i].netorestante
                                listaReciboPrLineas[i].dolneto = netocobrado
                                montoRec -= valorReal(netoRealenDoc * listaDocumentos[i].tasadoc)
                                listaReciboPrLineas[i].bsneto =
                                    valorReal(netoRealenDoc * tasaCambioSeleccionadaPrincipal)
                                listaReciboPrLineas[i].tnetodbs += valorReal(netoRealenDoc * tasaCambioSeleccionadaPrincipal)

                            }
                            //si el monto del recibo no cubre el monto completo del documento (se comporta como un abono)
                            else if ((netoRealenDoc * tasaCambioSeleccionadaPrincipal) > montoRec && montoRec > 0) {
                                netocobrado = montoRec / tasaCambioSeleccionadaPrincipal
                                val cobroAbono = netocobrado * listaDocumentos[i].tasadoc
                                listaReciboPrLineas[i].bscobro += valorReal(cobroAbono)
                                val dolneto = netocobrado
                                listaReciboPrLineas[i].dolneto = valorReal(dolneto)
                                listaReciboPrLineas[i].bsneto = montoRec
                                listaReciboPrLineas[i].tnetodbs += montoRec
                                montoRec -= montoRec

                            }
                        }

                        //los bss del neto cobrado
                        /*recorrido de lista de descuentos para asignar cantidad según nro doc. (de tener)*/
                        for (j in listaDescuentos.indices) {
                            if (listaDescuentos[j].nrodoc == listaDocumentos[i].documento) {
                                listaReciboPrLineas[i].prcdsctopp = listaDescuentos[j].pordscto
                            }
                        }
                    }
                }

                /*esto va a ser resultado de la suma de los campos de la lista (ke_precobranza)
                 de detalles */

                val difReteIva =
                    valorReal(listaReciboPrLineas.sumOf { it.bsmtoiva }) + valorReal(
                        listaReciboPrLineas.sumOf { it.bsretiva })
                val difRetyFlete =
                    valorReal(listaReciboPrLineas.sumOf { it.bsmtofte }) + valorReal(
                        listaReciboPrLineas.sumOf { it.bsretfte })
                var netoReal =
                    valorReal(listaReciboPrLineas.sumOf { it.bscobro }) - difReteIva - difRetyFlete //--revisar si esto es necesario
                cxc.bsneto = valorReal(listaReciboPrLineas.sumOf { it.bsneto })
                cxc.bsretiva = valorReal(listaReciboPrLineas.sumOf { it.bsretiva })
                cxc.bsiva = valorReal(listaReciboPrLineas.sumOf { it.bsmtoiva })
                cxc.bsflete = valorReal(listaReciboPrLineas.sumOf { it.bsmtofte })

                /* sumo los bss en total y los redondeo al momento de guardarlo */
                var bssumaTotal = valorReal(listaReciboPrLineas.sumOf { it.bscobro })

                //cxc.bstotal    = valorReal(bssumaTotal)
                cxc.bstotal =
                    valorReal(listaReciboPrLineas.sumOf { it.bsneto } + (listaReciboPrLineas.sumOf { it.bsmtoiva } + listaReciboPrLineas.sumOf { it.bsretiva }) + (listaReciboPrLineas.sumOf { it.bsmtofte } + listaReciboPrLineas.sumOf { it.bsretfte }))
                //cxc.dolneto    = valorReal(listaDocumentos.sumOf{it.dtotneto }) //<-------------------- Revisar si es necesario
                cxc.dolneto = valorReal(listaReciboPrLineas.sumOf { it.dolneto })
                cxc.doliva = valorReal(listaReciboPrLineas.sumOf { it.doliva })
                //cxc.dolretiva  = valorReal(listaDocumentos.sumOf { it.cdretencioniva })
                cxc.dolflete = valorReal(listaReciboPrLineas.sumOf { it.dolflete })
                cxc.doltotal =
                    valorReal(listaReciboPrLineas.sumOf { it.dolneto } + listaReciboPrLineas.sumOf { it.doliva } + listaReciboPrLineas.sumOf { it.dolflete })
                cxc.netocob =
                    binding.etCxcMontoMain.text.toString().ifEmpty { "0.0" }.toDouble()
                //if (monedaSeleccionadaPr == "2") cxc.doltotal + superSaldoFavor() else cxc.bstotal + cxc.bstotal + valorReal(superSaldoFavor() * tasaCambioSeleccionadaPrincipal)
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
                cxc.moneda = monedaSeleccionadaPr
                cxc.tasadia = tasaCambioSeleccionadaPrincipal
                listaReciboPrCabecera.add(cxc)

                try {

                    // inicio la transacción
                    keAndroid.beginTransaction()
                    val qcabecera = ContentValues()
                    val qlineas = ContentValues()

                    for (i in listaReciboPrCabecera.indices) {
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
                        if (ignore) {
                            qcabecera.put("efectivo", listaReciboPrCabecera[i].efectivo)
                        } else {
                            qcabecera.put("bcocod", listaReciboPrCabecera[i].bcocod)
                            qcabecera.put("bcomonto", listaReciboPrCabecera[i].bcomonto)
                            qcabecera.put("bcoref", listaReciboPrCabecera[i].bcoref)
                        }
                        qcabecera.put("edorec", listaReciboPrCabecera[i].edorec)
                        qcabecera.put("fchvigen", listaReciboPrCabecera[i].fchvigen)
                        qcabecera.put("bsretflete", listaReciboPrCabecera[i].bsretflete)
                        qcabecera.put("netocob", listaReciboPrCabecera[i].netocob)
                        qcabecera.put("fechamodifi", getFechaHoy())
                        qcabecera.put(
                            "tipo_pago",
                            if (binding.rbCxcAbonoMain.isChecked) 1 else 0
                        )
                        qcabecera.put(
                            "complemento",
                            if (binding.cbCxcComplemento.isChecked) nroComplemento else ""
                        )
                        //qcabecera.put("docdifcamb",diferencialCambiario)


                        for (j in listaReciboPrLineas.indices) {
                            qlineas.put("cxcndoc", listaReciboPrLineas[j].id_recibo)
                            qlineas.put("agencia", listaReciboPrLineas[j].agencia)
                            qlineas.put("tipodoc", listaReciboPrLineas[j].tipodoc)
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
                            qlineas.put(
                                "tnetoddol",
                                valorReal(listaReciboPrLineas[j].tnetoddol)
                            )
                            qlineas.put("tnetodbs", valorReal(listaReciboPrLineas[j].tnetodbs))
                            qlineas.put("fchrecibod", getFechaNow())
                            qlineas.put("kecxc_idd", listaReciboPrCabecera[i].kecxc_id)
                            qlineas.put("tasadiad", listaReciboPrCabecera[i].tasadia)
                            qlineas.put("reten", retennn)
                            qlineas.put("codcliente", listaDocumentos[i].codcliente)
                            qlineas.put(
                                "nombrecli",
                                conn.getCampoStringCamposVarios(
                                    "cliempre",
                                    "nombre",
                                    listOf("codigo", "empresa"),
                                    listOf(listaDocumentos[i].codcliente, codEmpresa!!)
                                )
                            )
                            qlineas.put("tasadoc", listaDocumentos[j].tasadoc)
                            //2023-10-19 esto es mal pero debido a la falta de tiempo va asi
                            qlineas.put(
                                "cbsretiva",
                                conn.getCampoDoubleCamposVarios(
                                    "ke_doccti",
                                    "cbsretiva",
                                    listOf("documento", "empresa"),
                                    listOf(listaDocumentos[i].documento, codEmpresa!!)
                                )
                            )
                            qlineas.put(
                                "cbsretflete",
                                conn.getCampoDoubleCamposVarios(
                                    "ke_doccti",
                                    "cbsretflete",
                                    listOf("documento","empresa"),
                                    listOf(listaDocumentos[i].documento, codEmpresa!!)
                                )
                            )
                            keAndroid.insert("ke_precobradocs", null, qlineas)
                        }
                    }

                    //val favor = guardaSaldoFavor(montoRec, binding.rbCxcDivisasMain, binding.rbCxcCompMain, binding.etCxcMontoMain.text.toString().toDouble(), binding.tvCxcTotal)
                    //ke_android.execSQL("UPDATE ke_precobradocs SET afavor= '$favor' WHERE cxcndoc='${listaReciboPrLineas[listaReciboPrLineas.size-1].id_recibo}' AND documento = '${listaReciboPrLineas[listaReciboPrLineas.size-1].documento}';")
                    guardaSaldoFavor2(
                        binding.rbCxcDivisasMain,
                        binding.rbCxcCompMain,
                        binding.etCxcMontoMain.text.toString().toDouble(),
                        0.00,
                        binding.tvCxcTotal,
                        listaReciboPrLineas[listaReciboPrLineas.size - 1].id_recibo,
                        listaReciboPrLineas[listaReciboPrLineas.size - 1].documento,
                        true
                    )
                    keAndroid.insert("ke_precobranza", null, qcabecera)

                    conn.saveImg(listaImagenes, nroPrecobranza, this) // <-- Guardando Imagenes

                    val qcorrelativo = ContentValues()
                    qcorrelativo.put("kcor_numero", nroCorrelativo)
                    qcorrelativo.put("kcor_vendedor", codUsuario)

                    keAndroid.insert("ke_corprec", null, qcorrelativo)
                    llCommit = true

                } catch (exception: SQLException) {

                    exception.printStackTrace()
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
                    listadatos.add(cxc)
                    listadatos[0].cliente = codigoCliente

                    val dialog = DialogRecibo()
                    dialog.DialogRecibo(this, listadatos)

                    Toast.makeText(this, "RECIBO CREADO", Toast.LENGTH_SHORT).show()
                    // finish()
                } else {
                    keAndroid.endTransaction()
                }
            }
        }
    }

    private fun verificacionReferencia(
        referencia: String, tabla: String, codigoBanco: String
    ): Int {//TE QUEDASTE QUITANDO PROVICIAL Y MERCANTIL DE LA VERIFICACION DE REFERENCIAS
        if (listaBancoRepetible.contains(codigoBanco)) {
            return 0
        }
        val cursor = keAndroid.rawQuery(
            "SELECT COUNT(*) FROM $tabla WHERE bcoref = '$referencia' AND bcoref != '' AND bcocod = '$codigoBanco';",
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

    /*private fun guardaSaldoFavor(
        montoRec: Double,
        btnDolar: RadioButton,
        btnCompleto: RadioButton,
        ingreso: Double,
        total: TextView,
    ): Double {
        return if (btnDolar.isChecked && btnCompleto.isChecked && (ingreso > total.text.toString()
                .toDouble())
        ) {
            valorReal(montoRec)
        } else {
            0.00
        }
    }*/

    /*
        2023-05-16 ASDA
    * Funcion para calcular el saldo a favor siempre y cuando se page en divisas como pago principal
    * en el caso de haber un complemento, tanto principal como complementario deben de estar en divisas
    * */
    private fun guardaSaldoFavor2(
        btnDolar: RadioButton,      //Boton de la moneda divisa en pago principal
        btnCompleto: RadioButton,   // Boton de la moneda divisa en pago principal
        ingreso: Double,            // Monto colocado por el cliente en el Pago Principal
        ingresoCom: Double,         // Monto colocado por el cliente en el Pago Complementario (si es un pago sin complemento su valor es 0.00)
        total: TextView,            // Monto total del o los Documentos seleccionados
        cxcndoc: String,            // Numero del correlativo de la ultima precobranza que se va a realizar
        documento: String,          // Numero del ultimo documento que se va a pagar en la transaccion
        btnDolarCom: Boolean,        // Valor booleano que indica si el complemento se paga en divisas (si no se paga complemento por defecto sera true)
    ) {
        // En esta variable se guarda en Divisa el monto colocado en el EditText del pago principal (de haber seleccionado bolivares estos seran convertidos)
        val ingresoPrincipal =
            if (binding.rbCxcDivisasMain.isChecked) ingreso else ingreso / tasaCambioSeleccionadaPrincipal
        // En esta variable se guarda en Divisa el monto colocado en el EditText del pago complementario (de haber seleccionado bolivares estos seran convertidos)
        val ingresoComplemento =
            if (!binding.cbCxcComplemento.isChecked) 0.00 else (if (binding.rbCxcDivisasCom.isChecked) ingresoCom else ingresoCom / tasaCambioSeleccionadaPrincipal)
        // En esta variable se guarda el Total del o los documentos a pagar en Divisa (de haber seleccionado bolivares estos seran convertidos)
        val totalReal = if (binding.rbCxcDivisasMain.isChecked) total.text.toString()
            .toDouble() else total.text.toString().toDouble() / tasaCambioSeleccionadaPrincipal/*
        * If que valida
        * que como pago principal se selecciono Divisa
        * que sea un pago Completo
        * que en caso de haber un complemento este sea en divisas tambien (Divisa como principal y como complemento)
        * que el pago principal y complmentario, juntos superen el total de la suma del o los documentos a pagar
        * */
        if (btnDolar.isChecked && btnCompleto.isChecked && btnDolarCom && ((ingresoPrincipal + ingresoComplemento) > totalReal)) {/*
            * La sentencia SQL
            * guarda el monto excesido del pago a travez de la supa del pago principal y complementario, menos el total del o los documentos a pagar
            *
            * Lo guarga en el ultimo documento a pagar
            * selecciona el correlativo del pago principal (en caso de no haber complemento), o el correlativo del complemento (en caso de haber un complemento)
            * selecciona siempre el ultimo documento de la lista de documentos seleccionados a pagar
            * */
            keAndroid.execSQL("UPDATE ke_precobradocs SET afavor= '${valorReal((ingresoPrincipal + ingresoComplemento) - totalReal)}' WHERE cxcndoc='$cxcndoc' AND documento = '$documento';")
            //keAndroid.execSQL("UPDATE ke_precobranza SET netocob='${valorReal(valorReal(total.text.toString().toDouble()) + valorReal((ingresoPrincipal + ingresoComplemento) - totalReal))}' WHERE cxcndoc='$cxcndoc';")
        }
    }


    /*private fun existReten(listaDocumentos: ArrayList<Documentos>): Boolean {
        //println("Documentos --> ${listaDocsSeleccionados}")

        //println("Correlativo --> $CorrelativoTexto")
        //println("Retenciones --> ${listaRetGuardada[0].tiporet} ${listaRetGuardada[0].fecharet} ${listaRetGuardada[0].nrodoc} ${listaRetGuardada[0].montoret} ${listaRetGuardada[0].nroret} ${listaRetGuardada[0].refret}")
        //println("Retenciones --> ${listaRetGuardada[1].tiporet}")
        //println("Retenciones --> ${listaRetGuardada[2].tiporet}")

        val documentosRet = listaDocsSeleccionados
        val rentenciones = listaRetGuardada
        rentenciones.sortBy { it.nrodoc }
        documentosRet.sort()
        //println("Ordenados --> $documentosRet")
        //println("Ordenados2 --> ${rentenciones[0].nrodoc}, ${rentenciones[1].nrodoc}, ${rentenciones[2].nrodoc}")

        var retIva = 0
        var retFlete = 0

        for (retencion in rentenciones) {
            if (retencion.tiporet == "iva") {
                retIva++
            }

            if (retencion.tiporet == "flete") {
                retFlete++
            }
        }

        return if (((retIva != 0) && (retIva != listaDocsSeleccionados.size)) || ((retFlete != 0) && (retFlete != listaDocsSeleccionados.size))) {
            println("Retencion --> $retIva")
            println("Flete --> $retFlete")
            println("Validacion errada")
            Toast.makeText(this, "Falto una retencion", Toast.LENGTH_SHORT).show()
            false
        } else {
            println("Retencion --> $retIva")
            println("Flete --> $retFlete")
            println("Validacion exitosa")
            true
        }
    }*/

    private fun fechaSuma(fechaOld: String, cantDias: Long): String {
        val fechaNueva: String
        //2023-04-03 Comentado por usar muchas variables, ahora se usan los parametros obtenidos de la funcion
        //val diasAdicional = cantDias

        // de string a fecha
        //2023-04-03 Comentado por usar muchas variables, ahora se usan los parametros obtenidos de la funcion
        //var fechaActual:String = fechaOld
        val fechaNow =
            LocalDate.parse(fechaOld, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
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

    private fun valorReal(monto: Double): Double {
        return Math.round(monto * 100.00) / 100.00
    }

    private fun bajarDocsConDesc(url: String) {
        var agencia: String
        var tipodoc: String
        var documento: String
        var codcliente: String
        var edodcto: String
        var prcdctoaplic: Double
        var montodctodol: Double
        var tasadoc: Double
        var fechamodifi: String

        var llCommitDc = false
        val jsonArrayRequest = JsonArrayRequest(Request.Method.GET, url, null, { response ->
            if (response != null) {
                llCommitDc = false
                keAndroid.beginTransaction()

                var jsonObject: JSONObject?
                try {
                    for (i in 0 until response.length()) {
                        jsonObject = response.getJSONObject(i)
                        agencia = jsonObject.getString("agencia").trim()
                        tipodoc = jsonObject.getString("tipodoc").trim()
                        documento = jsonObject.getString("documento").trim()
                        codcliente = jsonObject.getString("codcliente").trim()
                        edodcto = jsonObject.getString("edodcto").trim()
                        prcdctoaplic = jsonObject.getDouble("prcdctoaplic")
                        montodctodol = jsonObject.getDouble("montodctodol")
                        tasadoc = jsonObject.getDouble("tasadoc")
                        fechamodifi = jsonObject.getString("fechamodifi").trim()

                        val qDescuentos = ContentValues()
                        qDescuentos.put("agencia", agencia)
                        qDescuentos.put("tipodoc", tipodoc)
                        qDescuentos.put("documento", documento)
                        qDescuentos.put("codcliente", codcliente)
                        qDescuentos.put("edodcto", edodcto)
                        qDescuentos.put("prcdctoaplic", prcdctoaplic)
                        qDescuentos.put("montodctodol", montodctodol)
                        qDescuentos.put("tasadoc", tasadoc)
                        qDescuentos.put("fechamodifi", fechamodifi)

                        val qcodigoLocal: Cursor = keAndroid.rawQuery(
                            "SELECT count(documento) FROM ke_precobdcto WHERE documento ='${documento}'",
                            null
                        )
                        qcodigoLocal.moveToFirst()

                        val codigoExistente = qcodigoLocal.getInt(0)
                        qcodigoLocal.close()

                        if (codigoExistente > 0) {
                            keAndroid.update(
                                "ke_precobdcto",
                                qDescuentos,
                                "documento = ?",
                                arrayOf(documento)
                            )
                        } else if (codigoExistente == 0) {
                            keAndroid.insert("ke_precobdcto", null, qDescuentos)
                        }

                    }
                    llCommitDc = true

                } catch (ex: Exception) {
                    ex.printStackTrace()
                    llCommitDc = false
                    if (!llCommitDc) return@JsonArrayRequest
                }

            }
            if (llCommitDc) {
                keAndroid.setTransactionSuccessful()
                keAndroid.endTransaction()

            } else if (!llCommitDc) {
                keAndroid.endTransaction()
            }

        }, { error ->
            println("--Error--")
            error.printStackTrace()
            println("--Error--")
        })
        val requestQueue: RequestQueue = Volley.newRequestQueue(this)
        requestQueue.add(jsonArrayRequest)
    }

    private fun calcularDescuentos2(moneda: String) {
        if (binding.cbCxcDescuentos.isChecked && //se seleccionó descuento
            (conn.validarExistenciaDescuento(codigoBancoCompleto) || binding.rbCxcEfectivoMain.isChecked) && //validar que exista un descuento activo
            !binding.cbCxcComplemento.isChecked //validar que no se selecciono complemento
        ) {
            var descuentos: Descuentos
            var nrodocumento: String
            var fechaVence: String
            var montonetoDol: Double

            descuentoTotal = 0.00
            listaDescuentos.clear()

            for (i in listaDocumentos.indices) {
                val tieneDescPrev = verificarSiHayDescuentos(listaDocumentos[i].documento)

                fechaVence = listaDocumentos[i].vence
                montonetoDol = listaDocumentos[i].netorestante
                nrodocumento = listaDocumentos[i].documento

                descuentos = Descuentos()

                val fechaConvertidaVence = LocalDate.parse(fechaVence)
                val fechaHoy = LocalDate.now()
                val diasDiferencia = ChronoUnit.DAYS.between(fechaHoy, fechaConvertidaVence)

                if (!tieneDescPrev /*&& diasDiferencia > 0*/) {

                    var cantDescuento: Double

                    //cantidadDeDescuento = conn.getDescuento(codigoBancoCompleto)
                    cantidadDeDescuento =
                        if (binding.rbCxcTransfMain.isChecked) conn.getDescuento(
                            codigoBancoCompleto,
                            listaDocumentos[i].tipodocv
                        ) else conn.getDescuentoEfectivo(
                            moneda, listaDocumentos[i].tipodoc
                        )
                    val porcentajeAsignado = cantidadDeDescuento / 100

                    cantDescuento = montonetoDol * porcentajeAsignado
                    //var descuentoUni     = montonetoDol - cantDescuento
                    descuentoTotal += cantDescuento
                    descuentoTotal = Math.round(descuentoTotal * 100.00) / 100.00

                    descuentos.nrodoc = nrodocumento
                    descuentos.cantdscto = cantDescuento
                    descuentos.pordscto = cantidadDeDescuento
                    listaDescuentos.add(descuentos)

                }/* else if (!tieneDescPrev && diasDiferencia <= 0) {

                    var cantDescuento: Double

                    cantidadDeDescuento = conn.getDescuento(codigoBancoCompleto)
                    val porcentajeAsignado = cantidadDeDescuento / 100

                    cantDescuento = montonetoDol * porcentajeAsignado
                    //var descuentoUni     = montonetoDol - cantDescuento
                    descuentoTotal += cantDescuento
                    descuentoTotal = Math.round(descuentoTotal * 100.00) / 100.00

                    descuentos.nrodoc = nrodocumento
                    descuentos.cantdscto = cantDescuento
                    descuentos.pordscto = cantidadDeDescuento
                    listaDescuentos.add(descuentos)

                }*/

                if (descuentoTotal > 0.00) {
                    binding.cbCxcDescuentos.visibility = View.VISIBLE
                    //binding.cbCxcDescuentos.isEnabled = true
                    binding.tvCxcDctos.text = descuentoTotal.toString()
                    binding.btVerDetDescuento.visibility = View.VISIBLE

                } else if (descuentoTotal <= 0.00) {
                    binding.cbCxcDescuentos.visibility = View.INVISIBLE
                    //binding.cbCxcDescuentos.isEnabled = false
                    binding.tvCxcDctos.text = "0.00"
                    binding.btVerDetDescuento.visibility = View.INVISIBLE
                }

            }

        } else {
            binding.btVerDetDescuento.visibility =
                View.INVISIBLE //<-- no deja ver el boton para ver detalles de descuento
            binding.cbCxcDescuentos.isChecked =
                false //<-- para destildar los descuentos si se cambia a un banco que no posea descuento
            listaDescuentos.clear()
        }
    }

    /*private fun calcularDescuentos(moneda: String) {


        if (binding.cbCxcDescuentos.isChecked) {
            binding.btVerDetDescuento.visibility = View.VISIBLE
            var porcentajeAsignado = 0.00
            cantidadDeDescuento = 0.00
            listaDescuentos = ArrayList()
            var descuentos: Descuentos

            *//*variable que devolvera el descuento total que
            se pueden aplicar a todos los docs*//*
            descuentoTotal = 0.00
            var fechaVence: String
            var nrodocumento: String
            var montonetoDol: Double
            var montonetoBs = 0.00
            var tieneDescPrev: Boolean*//*Por cada documento, voy a buscar la fecha de vencimiento
            y determinar cada descuento*//*
            for (i in listaDocumentos.indices) {

                tieneDescPrev = verificarSiHayDescuentos(listaDocumentos[i].documento)
                if (tieneDescPrev) {
                    //significa que ya tiene un descuento aplicado.
                } else {
                    descuentos = Descuentos()
                    nrodocumento = listaDocumentos[i].documento
                    fechaVence = listaDocumentos[i].vence
                    montonetoDol = listaDocumentos[i].netorestante
                    montonetoBs = montonetoDol * tasaCambioSeleccionadaPrincipal

                    val fechaConvertidaVence = LocalDate.parse(fechaVence)
                    val fechaHoy = LocalDate.now()
                    val diasDiferencia = ChronoUnit.DAYS.between(fechaHoy, fechaConvertidaVence)


                    //si la diferencia de dias es positiva, puede aplicar al descuento
                    if (diasDiferencia > 0) {
                        //si la moneda es $$, el descuento es del 10%
                        if (moneda == "USD") {

                            val fechaEmisionS = listaDocumentos[i].emision
                            val fechaLimiteS = "2022-11-01"
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                            val fechaEmic: Date = sdf.parse(fechaEmisionS)!!
                            val fechaLimite: Date = sdf.parse(fechaLimiteS)!!

                            val comparativa = fechaEmic.compareTo(fechaLimite)

                            if (comparativa >= 0) {
                                //si la emision es mayor o igual al 1er de noviembre (no hay descuentos)
                                if (listBankDesc.contains(codigoBancoCompleto) && !binding.cbCxcComplemento.isChecked) {
                                    // ------- OJO AQUI DESCUENTO ---------
                                    porcentajeAsignado = 0.03
                                    cantidadDeDescuento = 3.0
                                }

                                val cantDescuento = montonetoDol * porcentajeAsignado
                                //var descuentoUni     = montonetoDol - cantDescuento
                                descuentoTotal += cantDescuento
                                descuentoTotal = Math.round(descuentoTotal * 100.00) / 100.00

                                descuentos.nrodoc = nrodocumento
                                descuentos.cantdscto = cantDescuento
                                descuentos.pordscto = cantidadDeDescuento
                                listaDescuentos.add(descuentos)


                            } else if (comparativa < 0) {
                                // si la emisión es menor  al 1ero de noviembre (Si hay descuentos)
                                porcentajeAsignado = 0.0
                                cantidadDeDescuento = 0.0
                                //Si los pagos son por zelle, el descuento pasa a ser de 12,7%
                                if (listBankDesc.contains(codigoBancoCompleto) && !binding.cbCxcComplemento.isChecked) {
                                    porcentajeAsignado = 0.03
                                    cantidadDeDescuento = 3.0
                                }


                                val cantDescuento = montonetoDol * porcentajeAsignado
                                //var descuentoUni     = montonetoDol - cantDescuento
                                descuentoTotal += cantDescuento
                                descuentoTotal = Math.round(descuentoTotal * 100.00) / 100.00

                                descuentos.nrodoc = nrodocumento
                                descuentos.cantdscto = cantDescuento
                                descuentos.pordscto = cantidadDeDescuento
                                listaDescuentos.add(descuentos)

                            }

                        } else if (moneda == "BSS") {

                        }

                    } else if (diasDiferencia <= 0) {

                        var cantDescuento = 0.00
                        descuentoTotal += 0.00

                        if (listBankDesc.contains(codigoBancoCompleto) && !binding.cbCxcComplemento.isChecked) {
                            porcentajeAsignado = 0.03
                            cantidadDeDescuento = 3.0
                            val cantDescuento = montonetoDol * porcentajeAsignado
                            //var descuentoUni     = montonetoDol - cantDescuento
                            descuentoTotal += cantDescuento
                            descuentoTotal = Math.round(descuentoTotal * 100.00) / 100.00

                            descuentos.nrodoc = nrodocumento
                            descuentos.cantdscto = cantDescuento
                            descuentos.pordscto = cantidadDeDescuento
                            listaDescuentos.add(descuentos)

                        } else {
                            //
                        }
                    }
                }

            }

            if (descuentoTotal > 0.00) {
                binding.cbCxcDescuentos.visibility = View.VISIBLE
                //binding.cbCxcDescuentos.isEnabled = true
                binding.tvCxcDctos.text = descuentoTotal.toString()
                binding.btVerDetDescuento.visibility = View.VISIBLE

            } else if (descuentoTotal <= 0.00) {
                binding.cbCxcDescuentos.visibility = View.INVISIBLE
                //binding.cbCxcDescuentos.isEnabled = false
                binding.tvCxcDctos.text = "0.00"
                binding.btVerDetDescuento.visibility = View.INVISIBLE
            }

        } else {
            binding.btVerDetDescuento.visibility = View.INVISIBLE
        }

    }*/

    private fun validarRetenciones() {

        var cantretsiva = 0
        var cantretsfte = 0
        var cantretsparme = 0
        var cdretflete = 0.00
        val cursor: Cursor = keAndroid.rawQuery(
            "SELECT contribespecial FROM cliempre WHERE codigo= '$codigoCliente'", null
        )
        var esConEspecial = "0"

        if (cursor.moveToFirst()) {
            esConEspecial = cursor.getString(0)

        }



        if (esConEspecial == "1") {


            for (i in listaDocumentos.indices) {
                //estos son los campos que muestran el monto que se tienen que pagar
                cdretencion = listaDocumentos[i].cdretencion
                cdretencioniva = listaDocumentos[i].cdretencioniva
                cdretparme = listaDocumentos[i].cdretparme
                cdretflete = listaDocumentos[i].cdretflete
                //estos son los cmapos que contienen pagos (de estar pagados)
                dretencion = listaDocumentos[i].dretencion
                dretencioniva = listaDocumentos[i].dretencioniva

                //resto para determinar que montos se encuentran pagados
                var montoretPagado = listaDocumentos[i].bsretencion
                val isRetpagado: Double =
                    listaDocumentos[i].cbsretencion.minus(listaDocumentos[i].bsretencion)
                val isretivaPagado =
                    listaDocumentos[i].cbsretencioniva.minus(listaDocumentos[i].bsretencioniva)


                // valido que ya estas ret hayan sido pagadas
                //var hayretParme:Double  =   montoretPagado - cbsretencioniva - bsmontoRetFlete
                //var hayretFlete:Double  =   montoretPagado - cbsretencioniva - cbsretparme

                val hayretParme: Double = listaDocumentos[i].cbsretparme
                val hayretFlete: Double = listaDocumentos[i].cbsretflete



                if (isRetpagado <= 0.00) {
                    //si los montos son cero, no paga retenciones
                    pagaRetenciones = false

                } else {

                    if (isretivaPagado > 0.00) {
                        //contabilizo si tiene ret de este tipo
                        cantretsiva += 1
                        pagaRetenciones = true
                    }

                    if (hayretFlete > 0.00) {
                        //contabilizo si tiene ret de este tipo
                        cantretsfte += 1
                        pagaRetenciones = true
                    }

                    if (hayretParme > 0.00) {
                        //contabilizo si tiene ret de este tipo
                        cantretsparme += 1
                        pagaRetenciones = true
                    }

                }

            } //final del ciclo


            if (cantretsiva > 0) {
                listaTiposRet.add("iva")
            }
            if (cantretsfte > 0) {
                listaTiposRet.add("flete")
            }
            if (cantretsparme > 0) {
                listaTiposRet.add("parme")
            }

            if (listaTiposRet.size > 0) {
                binding.btCxcRetenciones.visibility = View.VISIBLE
                binding.btCxcRetenciones.isEnabled = true

            } else {
                // bt_retenciones.visibility = View.INVISIBLE
                //bt_retenciones.isEnabled = false
            }

        } else {
            for (i in listaDocumentos.indices) {
                //estos son los campos que muestran el monto que se tienen que pagar
                cdretencion = listaDocumentos[i].cdretencion
                cdretencioniva = listaDocumentos[i].cdretencioniva
                cdretparme = listaDocumentos[i].cdretparme
                cdretflete = listaDocumentos[i].cdretflete
                //estos son los cmapos que contienen pagos (de estar pagados)
                dretencion = listaDocumentos[i].dretencion
                dretencioniva = listaDocumentos[i].dretencioniva

                //resto para determinar que montos se encuentran pagados
                val isRetpagado: Double =
                    listaDocumentos[i].cbsretencion.minus(listaDocumentos[i].bsretencion)

                val hayretParme: Double = listaDocumentos[i].cbsretparme
                val hayretFlete: Double = listaDocumentos[i].cbsretflete

                if (isRetpagado <= 0.00) {
                    //si los montos son cero, no paga retenciones
                    pagaRetenciones = false

                } else {

                    if (hayretFlete > 0.00) {
                        //contabilizo si tiene ret de este tipo
                        cantretsfte += 1
                        pagaRetenciones = true
                    }

                    if (hayretParme > 0.00) {
                        //contabilizo si tiene ret de este tipo
                        cantretsparme += 1
                        pagaRetenciones = true
                    }

                }

            } //final del ciclo

            if (cantretsfte > 0) {
                listaTiposRet.add("flete")
            }
            if (cantretsparme > 0) {
                listaTiposRet.add("parme")
            }

            if (listaTiposRet.size > 0) {
                binding.btCxcRetenciones.visibility = View.VISIBLE
                binding.btCxcRetenciones.isEnabled = true

            } else {
                // bt_retenciones.visibility = View.INVISIBLE
                //bt_retenciones.isEnabled = false
            }
        }

        cursor.close()

    }

    //picker dialog para la fecha
    private fun showDatePickerDialog() {
        val datePicker = DatePickerFragment("cxcReportActivity") { day, month, year ->
            onDateSelected(
                day, month, year
            )
        }

        datePicker.show(supportFragmentManager, "datePicker")

    }

    //funcion para seleccionar la fecha y guardarla
    private fun onDateSelected(day: Int, month: Int, year: Int) {
        val fechaMostrar = "$year-$month-$day"


        //en formato para query de tasas
        fechaQuery = ""
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date: Date = formatter.parse(fechaMostrar)!!
        val formatNuevo = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formatNuevoVista = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        fechaQuery = formatNuevo.format(date)

        binding.dpFecharec.setText("Fecha: ${formatNuevoVista.format(date)}")

        //var formatoHorafec = SimpleDateFormat("yyyy-MM-dd HH:mm:ss"Locale.getDefault())
        fechatasaH = fechaQuery

        buscarTasas()

        if (tasaCambioSeleccionadaPrincipal == 0.00) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Alerta de validación de datos")
            builder.setMessage("La fecha seleccionada no posee tasa de cambio.")
            builder.apply {
                setPositiveButton(
                    "Ok"
                ) { dialog, id ->

                }
            }
            builder.create()
            fechaQuery = ""
            return
        }

        if (binding.rbCxcDivisasMain.isChecked) {
            cargarSaldos("USD", listaDocsSeleccionados, !binding.cbExcReten.isChecked)
        } else {
            cargarSaldos("BSS", listaDocsSeleccionados, !binding.cbExcReten.isChecked)
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

    private fun generarNroComplemento(): String {
        val fechaHoy = Date(Calendar.getInstance().timeInMillis)

        val formatoFecha = SimpleDateFormat("yyMM", Locale.getDefault())

        val fecha = formatoFecha.format(fechaHoy)

        var correlativo = right(correlativoTextoCom, 4)
        correlativo = "$codUsuario-PRC-$fecha$correlativo"
        return correlativo
    }

    //funcion para retornar el valor del correlativo acortado a 4 caracteres.
    private fun right(valor: String, longitud: Int): String {
        return valor.substring(valor.length - longitud)
    }

    private fun montoaFavor(): Double {
        var montoTotalAFavor = 0.00

        val query = arrayOf("sum(montocli)")
        val tabla = "ke_mtopendcli"
        val condicion =
            "moneda ='1' AND codcliente='${codigoCliente}' AND estado = '0' AND edoweb ='1'"

        val cursorMto: Cursor = keAndroid.query(tabla, query, condicion, null, null, null, null)

        //si no esta vacio, lo recorro
        /*if(cursorMto.count > 0){
            while(cursorMto.moveToNext()){
                montoTotalAFavor = cursorMto.getDouble(0)
            }
        }*/
        if (cursorMto.moveToFirst()) {
            montoTotalAFavor = cursorMto.getDouble(0)
        }
        cursorMto.close()
        //retorno el valor
        return montoTotalAFavor
    }

    private fun cargarSaldos(
        moneda: String, listadocs: ArrayList<String>, pagaret: Boolean = true
    ) {
        //cada vez que se recalcule, se debe volver a colocar en cero los montos
        ivaRestante = 0.00
        netoRestante = 0.00
        fleteRestante = 0.00
        var netoresta = 0.00
        //esta variable es prod. del query para saber si tiene saldo a favor y DEBE ser siempre menor
        //al monto neto.
        var saldoAFavor = montoaFavor()

        //variables para los bss
        var ivaRestantebss = 0.00
        val netoRestantebss: Double
        var fleteRestantebss = 0.00
        var netorestabss = 0.00
        //las retenciones
        var retencionRestante = 0.00
        var retencionRestantebss = 0.00
        var retencionRestantedoc = 0.00

        var reclamo = 0.00
        var reclamoBS = 0.00

        listaDocumentos = ArrayList()

        //query para cargar los montos del documento
        var documentos: Documentos
        val query = arrayOf(
            "documento," + "contribesp," + "ruta_parme," + "vence," + "tipodocv," + "diascred," + "dtotneto," + "dtotpagos," + "dtotdev," + "dtotalfinal," + "bsiva," + "bsflete," + "bsretencioniva," + "bsretencion, tasadoc, dtotimpuest, dFlete, dretencion, dretencioniva, tipodoc, " + "mtodcto, fchvencedcto, tienedcto, cbsret, cdret, cbsretiva, cdretiva, cbsrparme, cdrparme, agencia, bsmtoiva, bsmtofte, retmun_mto, emision, codcliente, cdretflete, cbsretflete"
        )
        val tabla = "ke_doccti"
        val condicion =
            "documento IN (" + listadocs.toString().replace("[", "").replace("]", "") + ")"
        val cursorDocs: Cursor =
            keAndroid.query(tabla, query, condicion, null, null, null, null)

        while (cursorDocs.moveToNext()) {
            documentos = Documentos()

            documentos.documento = cursorDocs.getString(0)
            documentos.contribesp = cursorDocs.getDouble(1)
            documentos.ruta_parme = cursorDocs.getString(2)
            documentos.vence = cursorDocs.getString(3)
            documentos.tipodocv = cursorDocs.getString(4)
            documentos.diascred = cursorDocs.getDouble(5)
            documentos.dtotneto = cursorDocs.getDouble(6)
            documentos.dtotpagos = cursorDocs.getDouble(7)
            documentos.dtotdev = cursorDocs.getDouble(8)
            documentos.dtotalfinal = cursorDocs.getDouble(9)
            documentos.bsiva = cursorDocs.getDouble(10)
            documentos.bsflete = cursorDocs.getDouble(11)
            documentos.bsretencioniva = cursorDocs.getDouble(12)
            documentos.bsretencion = cursorDocs.getDouble(13)
            documentos.tasadoc = cursorDocs.getDouble(14)
            documentos.dtotimpuest = cursorDocs.getDouble(15)
            documentos.dFlete = cursorDocs.getDouble(16)
            documentos.dretencion = cursorDocs.getDouble(17)
            documentos.dretencioniva = cursorDocs.getDouble(18)
            documentos.tipodoc = cursorDocs.getString(19)
            documentos.mtodcto = cursorDocs.getDouble(20)
            documentos.fchvencedcto = cursorDocs.getString(21)
            documentos.tienedcto = cursorDocs.getString(22)
            documentos.cbsretencion = cursorDocs.getDouble(23) // cbsret
            documentos.cdretencion = cursorDocs.getDouble(24) // cdret
            documentos.cbsretencioniva = cursorDocs.getDouble(25) // cbsretiva
            documentos.cdretencioniva = cursorDocs.getDouble(26) // cdretiva
            documentos.cbsretparme = cursorDocs.getDouble(27) // cbsrparme
            documentos.cdretparme = cursorDocs.getDouble(28) // cdrparme
            documentos.agencia = cursorDocs.getString(29)
            documentos.bsmtoiva = cursorDocs.getDouble(30)
            documentos.bsmtofte = cursorDocs.getDouble(31)
            documentos.retmun_mto = cursorDocs.getDouble(32)
            documentos.emision = cursorDocs.getString(33)
            documentos.codcliente = cursorDocs.getString(34)
            documentos.cdretflete = cursorDocs.getDouble(35)
            documentos.cbsretflete = cursorDocs.getDouble(36)
            listaDocumentos.add(documentos)

        }
        cursorDocs.close()/*calculos del(los) doc(s)
        en funcion a la lista creada, analizar que montos se deben calcular en relacion
        al saldo, puesto que los montos finales que se reflejen, deben ser los restantes (es decir,
        considerar lo ya pagado). */
        for (i in listaDocumentos.indices) {
            codigoCliente = listaDocumentos[i].codcliente
            tipoDocsaPagar.add(listaDocumentos[i].tipodocv)

            //calculo de la retencion restante del flete pagada
            //2023-03-22 Se elimino por ser redundante
            //listaDocumentos[i].cdretflete  = (listaDocumentos[i].cdretencion - listaDocumentos[i].cdretencioniva - listaDocumentos[i].cdretparme)
            //listaDocumentos[i].cbsretflete = (listaDocumentos[i].cbsretencion - listaDocumentos[i].cbsretencioniva - listaDocumentos[i].cbsretparme)


            val pagado = listaDocumentos[i].dtotpagos
            //var pagadobss = listaDocumentos[i].dtotpagos * listaDocumentos[i].tasadoc

            //iva restante
            //Quitar iva resta y hacerla pregunta mostrar en dolares y bolivares si no esta pagado, si es pagado todo 0
            //Colocar solo el iva en bolivares y en dolares, pero no su resta
            var ivarestabss: Double
            var ivaresta: Double
            if (listaDocumentos[i].bsiva - listaDocumentos[i].bsmtoiva <= 0.00) {
                ivarestabss = 0.00
                ivaresta = 0.00
            } else {/*if(binding.cbExcReten.isChecked){
                    ivarestabss =  listaDocumentos[i].bsiva
                    ivaresta    = listaDocumentos[i].dtotimpuest
                    binding.tvCxcIva.setTextColor(Color.BLACK)
                }else{
                    ivarestabss =  listaDocumentos[i].bsiva - listaDocumentos[i].cbsretencioniva
                    ivaresta    =  listaDocumentos[i].dtotimpuest - listaDocumentos[i].cdretencioniva
                    binding.tvCxcIva.setTextColor(Color.rgb(16, 124, 65))
                }*/

                ivarestabss = listaDocumentos[i].bsiva
                ivaresta = listaDocumentos[i].dtotimpuest
            }


            //flete restante
            //misco caso que iva
            var fleterestabss: Double
            var fleteresta: Double
            if (listaDocumentos[i].bsflete - listaDocumentos[i].bsmtofte <= 0.00) {
                fleterestabss = 0.00
                fleteresta = 0.00
            } else {
                fleterestabss = listaDocumentos[i].bsflete
                fleteresta = listaDocumentos[i].dFlete
            }


            var totalfdoc = listaDocumentos[i].dtotalfinal
            val totalpdoc = listaDocumentos[i].dtotpagos
            val tasadoc = listaDocumentos[i].tasadoc
            val ivapagodoc = listaDocumentos[i].bsmtoiva
            val fletepagodoc = listaDocumentos[i].bsmtofte
            //var totalnetodoc = listaDocumentos[i].dtotneto - listaDocumentos[i].dFlete
            val totalnetodoc = listaDocumentos[i].dtotneto

            if (pagado <= 0.00) {

                ivaRestantebss += ivarestabss
                fleteRestantebss += fleterestabss
                ivaRestante += ivaresta
                fleteRestante += fleteresta

                //netoresta    += listaDocumentos[i].dtotalfinal - listaDocumentos[i].dtotimpuest - listaDocumentos[i].dFlete
                netoresta += listaDocumentos[i].dtotneto - listaDocumentos[i].dtotdev
                //var netorestadoc = listaDocumentos[i].dtotalfinal - listaDocumentos[i].dtotimpuest - listaDocumentos[i].dFlete
                val netorestadoc = listaDocumentos[i].dtotneto - listaDocumentos[i].dtotdev
                //COLOCAR FUERA DEL CICLO
                //netoresta    = valorReal(netoresta)
                //netorestabss += netoresta * tasaCambioSeleccionadaPrincipal
                //Esto deberia solucionar el mal calculo de los bolivares
                netorestabss += (listaDocumentos[i].dtotneto - listaDocumentos[i].dtotdev) * tasaCambioSeleccionadaPrincipal

                //netoRestantebss = netorestabss
                //netoRestante  = netoresta
                listaDocumentos[i].netorestante = netorestadoc

            } else if (pagado > 0.00) {

                ivaRestantebss += ivarestabss
                fleteRestantebss += fleterestabss
                ivaRestante += ivaresta
                fleteRestante += fleteresta

                netoresta += (totalnetodoc - listaDocumentos[i].dtotdev) - (totalpdoc - (ivapagodoc / tasadoc) - (fletepagodoc / tasadoc))
                val netorestadoc =
                    (totalnetodoc - listaDocumentos[i].dtotdev) - (totalpdoc - (ivapagodoc / tasadoc) - (fletepagodoc / tasadoc))
                //COLOCAR FUERA DEL CICLO
                //netoresta    = valorReal(netoresta)

                //netorestabss = netoresta * tasaCambioSeleccionadaPrincipal
                //Esto deberia solucionar el mal calculo de los bolivares
                netorestabss +=
                    ((totalnetodoc - listaDocumentos[i].dtotdev) - (totalpdoc - (ivapagodoc / tasadoc) - (fletepagodoc / tasadoc))) * tasaCambioSeleccionadaPrincipal


                //netoRestantebss = netorestabss
                //netoRestante  = netoresta
                listaDocumentos[i].netorestante = netorestadoc
            }

            if (listaDocumentos[i].dretencion <= 0) {
                //retencionRestante    += listaDocumentos[i].cdretencion  + listaDocumentos[i].cdretflete
                //retencionRestantebss += listaDocumentos[i].cbsretencion + listaDocumentos[i].cbsretflete
                retencionRestante += listaDocumentos[i].cdretencion //<------------------------------------------------- OJO -----------------
                retencionRestantebss += listaDocumentos[i].cbsretencion//<------------------------------------------------- OJO -----------------
            } else {
                //retencionRestante    += listaDocumentos[i].dretencion   - (listaDocumentos[i].cdretencion + listaDocumentos[i].cdretflete)
                //var retencionbsspag  =  listaDocumentos[i].dretencion   * listaDocumentos[i].tasadoc
                //retencionRestantebss += retencionbsspag                 - (listaDocumentos[i].cbsretencion + listaDocumentos[i].cbsretflete)

                //retencionRestante    +=  listaDocumentos[i].dretencion  - listaDocumentos[i].cdretencion
                //retencionRestantebss +=  listaDocumentos[i].bsretencion - listaDocumentos[i].cbsretencion

                retencionRestante += listaDocumentos[i].cdretencion - listaDocumentos[i].dretencion
                retencionRestantebss += listaDocumentos[i].cbsretencion - listaDocumentos[i].bsretencion

            }
        }

        netoRestantebss = netorestabss
        netoRestante = if (netoresta < 0.00) 0.00 else netoresta
        //netoRestante  = valorReal(netoresta)

        if (binding.cbCxcDescuentos.isChecked && binding.rbCxcDivisasMain.isChecked) {
            calcularDescuentos2("USD")
        } else {
            descuentoTotal = 0.00
            listaDescuentos = ArrayList()
            binding.tvCxcDctos.text = "0.00"
            binding.btVerDetDescuento.visibility = View.INVISIBLE
        }

        if (!pagaret) {
            //Al total de retenciones a deber solo le quito la del Flete
            retencionRestante = 0.0
            retencionRestantebss = 0.0
            listaDocumentos.forEach { documento ->
                //dretencion (Pago total de las retenciones)
                //dretencioniva (Pago de la retencion de IVA)
                //Si dretencion - dretencioniva menor o igual que 0 (y comprobando que dretencion sea
                // mayor que 0) significa que pago la retencion del IVA pero no la del flete
                // NOTA: se comprube aque dretencion sea mayor a 0 para comprobar que ya se pago con
                // anterioridad una retencion, si es 0 significa que no ha pagado ninguna retencion

                if ((documento.bsretencion - documento.bsretencioniva <= 0) && (documento.bsretencion > 0)) {
                    retencionRestante += 0.0
                    retencionRestantebss += 0.0
                } else if ((documento.bsretencion - (documento.bsretencioniva + documento.cbsretflete) >= 0) && (documento.bsretencion > 0)) {
                    retencionRestante += 0.0
                    retencionRestantebss += 0.0
                } else {//Excluyo solo la retencion del FLETE y PARME
                    retencionRestante += documento.cdretencion - (documento.cdretflete + documento.cdretparme)
                    retencionRestantebss += documento.cbsretencion - (documento.cbsretflete + documento.cbsretparme)
                }

            }


            val nightModeFlags: Int =
                this.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

            if (nightModeFlags == Configuration.UI_MODE_NIGHT_NO) {
                binding.tvCxcReten.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.blackColor1
                    )
                )
            } else {
                binding.tvCxcReten.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.whiteColor4
                    )
                )
            }

        } else {
            binding.tvCxcReten.setTextColor(Color.RED)
        }

        if (!binding.rbCxcEfectivoMain.isChecked) {
            saldoAFavor = 0.00
        }

        val netoaMostrarTot = netoRestante - descuentoTotal

        if (netoaMostrarTot < saldoAFavor) {
            saldoAFavor = 0.00
        }

        val totalRestante =
            (netoaMostrarTot - saldoAFavor) + ivaRestante + fleteRestante - retencionRestante
        val totalRestantebss =
            netoRestantebss + ivaRestantebss + fleteRestantebss - retencionRestantebss

        //asignacion de valores -- debe ser segun la moneda seleccionada valorReal

        if (moneda == "USD") {/* montos en usd (normal, sin problemas) */
            binding.tvCxcNeto.text = valorReal(netoRestante).toString()
            binding.tvCxcIva.text = valorReal(ivaRestante).toString()
            binding.tvCxcFlete.text = valorReal(fleteRestante).toString()
            //2023-05-05 Se elimino debido a que cuando se recalculaban se incluian las retenciones cuando el boton de excluir estaba crequeado
            binding.tvCxcReten.text = valorReal(retencionRestante).toString()
            //binding.tvCxcReten.text = if (!pagaRetenciones) valorReal(retencionRestante).toString() else 0.00.toString()
            binding.tvCxcTotal.text = valorReal(totalRestante).toString()

        } else if (moneda == "BSS") {/* montos en bss (seran montos del doc o uso las tasas para ser mas rapido?) */
            binding.tvCxcNeto.text = valorReal(netoRestantebss).toString()
            binding.tvCxcIva.text = valorReal(ivaRestantebss).toString()
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
        if (!APP_NOTA_ENTREGA_BS) {
            if (tipoDocsaPagar.contains("N/E")) {
                binding.rbCxcBssMain.visibility = View.INVISIBLE
                binding.rbCxcBssMain.isChecked = false
            }
        }

        //println("Booleano --> ${facturaEspecial()}")

        if (listaDocumentos.size > 1 && tipoDocsaPagar.contains("N/E")) {
            //binding.rbCxcBssMain.visibility = View.INVISIBLE
            //binding.rbCxcBssMain.isChecked  = false
            binding.rbCxcAbonoMain.visibility = View.INVISIBLE

        }

        if (listaDocumentos.size > 1) {
            binding.rbCxcAbonoMain.visibility = View.INVISIBLE
        }

        //2023-08-14
        //Se comento por que para los abonos tambien cuenta las notas de credito y devoluciones.
        /*for (documento in listaDocumentos) {
            if (conn.getCampoInt(
                    "ke_doccti",
                    "estatusdoc",
                    "documento",
                    documento.documento
                ) != 0
            ) {
                binding.cbCxcDescuentos.visibility = View.INVISIBLE
            }
        }*/
    }

    /*private fun facturaEspecial(): Boolean {
        var num = 0
        for (i in listaDocumentos.indices) {
            val cursor = keAndroid.rawQuery(
                "SELECT diascred, recepcion, vence FROM ke_doccti WHERE documento = '${listaDocumentos[i].documento}';",
                null
            )
            //println("SELECT diascred, vence FROM ke_doccti WHERE documento = '${listaDocumentos[i].documento}';")
            if (cursor.moveToNext()) {
                val diasCredito = cursor.getDouble(0)
                val fechaRecepcion = cursor.getString(1)
                val fechaVence = cursor.getString(2)
                if (diasCredito.toInt() >= conn.getConfigNum("DIAS_VALIDOS_BOLIVARES_DOCS")) {
                    if (!(diasRecepcion(fechaRecepcion))) {
                        num++
                    }
                } else {
                    if (compararFecha(fechaVence) < 0) {
                        num++
                    }
                }
            }*//*if (cursor.moveToNext()){
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
            }*//*
            cursor.close()
        }
        return num > 0

    }*/


    /* buscar tasa en funcion a la fecha provista en el datepicker
    * por defecto,deberia agregarle una fecha (hoy) para que tome por defecto
    * la tasa de hoy */
    private fun buscarTasas() {
        var flag = true

        //consulto al webservice para guardar las tasas
        descargarTasas("https://" + enlaceEmpresa + "/webservice/tasas.php?fecha_sinc=" + fechaAuxiliar.trim() + "&&agencia=" + codigoSucursal.trim())

        while (flag) {
            //println("https://"+ enlaceEmpresa + "/webservice/tasas.php?fecha_sinc=" + fecha_auxiliar.trim() + "&&agencia=" + codigoSucursal.trim())
            //buscarTasas()


            keAndroid = conn.writableDatabase
            var tasas: tasas

            val cursorTasas: Cursor = keAndroid.rawQuery(
                "SELECT kecxc_id, kecxc_fecha, kecxc_tasa, kecxc_fchyhora, kecxc_tasaib FROM kecxc_tasas WHERE kecxc_fchyhora LIKE '%$fechaQuery%' ORDER BY kecxc_fchyhora DESC LIMIT 1",
                null
            )

            //vacio el cursor en las variables para mostrar
            if (cursorTasas.moveToFirst()) {
                tasas = tasas()
                tasas.id = cursorTasas.getString(0)
                tasas.fecha = cursorTasas.getString(1)
                tasas.tasa = cursorTasas.getDouble(2)
                var fechaSinConvertir = cursorTasas.getString(3)
                tasas.tasaib = cursorTasas.getDouble(4)



                if (tasas.id.isNotEmpty()) {
                    flag = false

                    tasaId = tasas.id
                    tasaNormal = tasas.tasa
                    tasaInterB = tasas.tasaib
                    tasaFecha = tasas.fecha

                    tasaCambioSeleccionadaPrincipal = tasaNormal
                    binding.tieTasaselec.hint = ""
                    binding.tieTasaselec.hint =
                        "Tasa: ${tasaCambioSeleccionadaPrincipal.toTwoDecimals()} Bs."
                } else {

                    binding.tieTasaselec.hint = ""

                    flag = true

                    val date1 =
                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(fechaQuery)

                    val calendar: Calendar = Calendar.getInstance()
                    calendar.time = date1!! // Configuramos la fecha que se recibe

                    calendar.add(Calendar.DATE, -1)


                    val sdf = SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)

                    val parsedDate = sdf.parse(calendar.time.toString())
                    val print = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())


                    fechaQuery = print.format(parsedDate!!)
                }

            } else {
                binding.tieTasaselec.hint = ""

                flag = true

                val date1 =
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(fechaQuery)

                val calendar: Calendar = Calendar.getInstance()
                calendar.time = date1!! // Configuramos la fecha que se recibe

                calendar.add(Calendar.DATE, -1)


                val sdf = SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)

                val parsedDate = sdf.parse(calendar.time.toString())
                val print = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())


                fechaQuery = print.format(parsedDate!!)

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

    private fun actualizarBancos() {
        listaInfoBancos = ArrayList()
        listaInfoBancos.add("Seleccione un banco...")
        binding.spCxcBancoMain.listSelection = 0
        for (i in listaBancos.indices) {
            listaInfoBancos.add(listaBancos[i].nombanco)
        }

    }

    private fun getFechaHoy(): String {
        val fechaHoy: String
        val fechaSinConvertir: Calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        fechaHoy = sdf.format(fechaSinConvertir.time)
        return fechaHoy
    }

    private fun getFechaNow(): String {
        val fechaNow: String
        val fechaSinConvertir: Calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        fechaNow = sdf.format(fechaSinConvertir.time)
        return fechaNow
    }


    private fun iraRetenciones() {
        val listaDocsconRet: ArrayList<String> = ArrayList()



        listaDocumentos.forEach { documento ->
            val bsretencionFlete = documento.bsretencion - documento.bsretencioniva

            if (documento.cbsretencion > 0 && (documento.bsretencioniva <= 0 || bsretencionFlete <= 0)) {
                listaDocsconRet.add(documento.documento.toString())
            }
        }

        //if (listaTiposRet.size > 0 && !binding.cbExcReten.isChecked) {
        //revisar estos ifs
        /*for (i in listaDocumentos.indices) {
            val variable1 = listaDocumentos[i].cdretencion
            val variable2 = listaDocumentos[i].dretencion
            val variable3 = listaDocumentos[i].cbsretencion
            val variable4 = listaDocumentos[i].bsretencion
            val variable5 = dmontoRetFlete

            if ((listaDocumentos[i].cdretencion > 0.00 && listaDocumentos[i].dretencion == 0.00) || (listaDocumentos[i].cbsretencion > 0.00 && listaDocumentos[i].bsretencion == 0.00)) {
                listaDocsconRet.add(listaDocumentos[i].documento.toString())
            } else {
                if (listaDocumentos[i].cdretencion == 0.00 && listaDocumentos[i].dretencion == 0.00 && dmontoRetFlete > 0.00) {
                    listaDocsconRet.add(listaDocumentos[i].documento.toString())
                }
            }
        }*/
        // -- - - - - - - -
        if (!(listaDocsconRet.isNullOrEmpty())) {
            val intent = Intent(applicationContext, RetencionesActivity::class.java)
            val bundle = Bundle()

            bundle.putSerializable("listaRetenciones", listaRetGuardada)
            bundle.putSerializable("listaDocs", listaDocsconRet)

            intent.putExtras(bundle)
            intent.putExtra("listatiposret", listaTiposRet)
            intent.putExtra("listaDocs", listaDocsconRet)
            startActivityForResult(intent, requestCodeRetencion)
        } else {
            Toast.makeText(
                this, "El documento no posee retenciones.", Toast.LENGTH_SHORT
            ).show()
        }


        //} else {
        //Toast.makeText(
        //this, "El documento no posee retenciones o se han excluido.", Toast.LENGTH_SHORT
        //).show()
        //}
    }

    //fun para cargar datos de la empresa act. en la app segun usuario
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

    private fun descargarTasas(url: String) {
        conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null)
        keAndroid = conn.readableDatabase

        val jsonArrayRequest = JsonArrayRequest(Request.Method.GET, // method
            url, // url
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
                            idTasa = jsonObject.getString("id")
                            fechaTasa = jsonObject.getString("fecha")
                            tasaCambio = jsonObject.getDouble("tasa")
                            usuarioTasa = jsonObject.getString("usuario")
                            ipTasa = jsonObject.getString("ip")
                            fechayHoraTasa = jsonObject.getString("fechayhora")
                            fechamodifitasa = jsonObject.getString("fechamodifi")
                            tasaInterbancaria = jsonObject.getDouble("tasaib")

                            val qTasas = ContentValues()
                            qTasas.put("kecxc_id", idTasa)
                            qTasas.put("kecxc_fecha", fechaTasa)
                            qTasas.put("kecxc_tasa", tasaCambio)
                            qTasas.put("kecxc_usuario", usuarioTasa)
                            qTasas.put("kecxc_ip", ipTasa)
                            qTasas.put("kecxc_fchyhora", fechayHoraTasa)
                            qTasas.put("fechamodifi", fechamodifitasa)
                            qTasas.put("kecxc_tasaib", tasaInterbancaria)

                            val qcodigoLocal: Cursor = keAndroid.rawQuery(
                                "SELECT count(kecxc_id) FROM kecxc_tasas WHERE kecxc_id ='$idTasa'",
                                null
                            )
                            qcodigoLocal.moveToFirst()
                            //variable para obtener el conteo de documentos que ya esten en el telf
                            val codigoExistente = qcodigoLocal.getInt(0)

                            if (codigoExistente > 0) {
                                keAndroid.update(
                                    "kecxc_tasas", qTasas, "kecxc_id= ?", arrayOf(idTasa)
                                )
                            } else if (codigoExistente == 0) {
                                keAndroid.insert("kecxc_tasas", null, qTasas)
                            }
                            llCommit = true

                            qcodigoLocal.close()

                        }

                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        llCommit = false
                        if (!llCommit) return@JsonArrayRequest
                    }
                    if (llCommit) {
                        keAndroid.setTransactionSuccessful()
                        keAndroid.endTransaction()
                        //buscarTasas()


                    } else if (!llCommit) {
                        keAndroid.endTransaction()
                    }
                }
            }, { error -> // error listener
                println("--Error--")
                error.printStackTrace()
                println("--Error--")
            })
        val requestQueue: RequestQueue = Volley.newRequestQueue(applicationContext)
        requestQueue.add(jsonArrayRequest)
    }


    /*private fun getBancos(monedaBanco: String) {
        //descargarBancos("https://"+ enlaceEmpresa + "/webservice/bancos.php?fecha_sinc=" + fecha_auxiliar.trim() + "&&agencia=" + codigoSucursal.trim(), monedaBanco)
    }*/

    /*private fun descargarBancos(url: String, monedaBanco: String) {
        conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null, 14)
        keAndroid = conn.readableDatabase

        val jsonArrayRequest = JsonArrayRequest(Request.Method.GET, // method
            url, // url
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
                            qBancos.put("inactiva", inactiva)
                            qBancos.put("fechamodifi", fechamodifiBan)

                            val qcodigoLocal: Cursor = keAndroid.rawQuery(
                                "SELECT count(codbanco) FROM listbanc WHERE codbanco ='$codbanco'",
                                null
                            )
                            qcodigoLocal.moveToFirst()
                            //variable para obtener el conteo de documentos que ya esten en el telf
                            val codigoExistente = qcodigoLocal.getInt(0)

                            if (codigoExistente > 0) {
                                keAndroid.update(
                                    "listbanc", qBancos, "codbanco= ?", arrayOf(codbanco)
                                )
                            } else if (codigoExistente == 0) {
                                keAndroid.insert("listbanc", null, qBancos)
                            }
                            llCommit = true

                            qcodigoLocal.close()

                        }

                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        llCommit = false
                        if (!llCommit) return@JsonArrayRequest
                    }
                    if (llCommit) {
                        keAndroid.setTransactionSuccessful()
                        keAndroid.endTransaction()
                        cargarBancosMain(monedaBanco)


                    } else if (!llCommit) {
                        keAndroid.endTransaction()
                    }
                }
            }, { error -> // error listener
                println("--Error--")
                error.printStackTrace()
                println("--Error--")
            })
        val requestQueue: RequestQueue = Volley.newRequestQueue(applicationContext)
        requestQueue.add(jsonArrayRequest)
        cargarBancosMain(monedaBanco)

    }*/

    private fun cargarBancosMain(monedaSelec: String) {

        listaInfoBancos.clear()
        listaBancos.clear()

        keAndroid = conn.writableDatabase
        var bancos: Bancos
        var moneda = 0.00

        if (monedaSelec == "USD") {
            moneda = 2.00

        } else if (monedaSelec == "BSS") {
            moneda = 1.00
        }

        val cursorBancos: Cursor = keAndroid.rawQuery(
            "SELECT DISTINCT codbanco, nombanco,cuentanac, inactiva, fechamodifi FROM listbanc " + "WHERE inactiva = 0 AND cuentanac = $moneda",
            null
        )
        while (cursorBancos.moveToNext()) {
            bancos = Bancos()
            bancos.codbanco = cursorBancos.getString(0)
            bancos.nombanco = cursorBancos.getString(1)
            bancos.cuentanac = cursorBancos.getDouble(2)
            bancos.inactiva = cursorBancos.getDouble(3)
            bancos.fechamodifi = cursorBancos.getString(4)
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
        val variable = 0
        if ((requestCode == this.requestCodeRetencion) && (resultCode == RESULT_OK)) {
            val bundle: Bundle
            if (data != null) {
                bundle = data.extras!!
                if (bundle.containsKey("listaRetenciones")) {
                    listaRetGuardada =
                        bundle.getSerializable("listaRetenciones") as ArrayList<Retenciones>/*for (i in listaRetGuardada){
                        println("tipo ${i.tiporet}, nroret ${i.nroret}, refret ${i.refret}  fecha ${i.fecharet}  monto ${i.montoret}")

                    }*/
                    calcularRetencion()
                    //println("LLEGUE AL ACTIVITY ON RESULT")
                } else {

                    //println("NO ESTA LLEGANDO LA LISTA")
                }
            }
            //var listaRetCadena = data?.getStringArrayListExtra("listaRetenciones")
        } else if ((requestCode == this.requestCodeImg) && (resultCode == RESULT_OK)) {
            try {
                lateinit var imageUri: Uri
                val clipData = data!!.clipData
                if (clipData == null) {
                    imageUri = data!!.data!!
                    listaImagenes.add(imageUri)
                    println(listaImagenes)
                } else {
                    for (i in 0 until clipData.itemCount) {
                        listaImagenes.add(clipData.getItemAt(i).uri)
                        println(listaImagenes)
                    }
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Algo salió mal", Toast.LENGTH_LONG).show()
            }
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
        cal.add(Calendar.DATE, +conn.getConfigNum("DIAS_VALIDOS_BOLIVARES_DOCS", codEmpresa!!).toInt())

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

    private fun superSaldoFavor(): Double {
        val total: Double = binding.tvCxcTotal.text.toString().ifEmpty { "0.0" }.toDouble()
        val pagoPrincipal: Double =
            binding.etCxcMontoMain.text.toString().ifEmpty { "0.0" }.toDouble()
        val pagoComplemento: Double =
            binding.etCxcMontoCom.text.toString().ifEmpty { "0.0" }.toDouble()

        val realPrincipal =
            if (monedaSeleccionadaPr == "2") pagoPrincipal else pagoPrincipal / tasaCambioSeleccionadaPrincipal
        val realComplemento =
            if (monedaSeleccionadaCm == "2") pagoComplemento else pagoComplemento / tasaCambioSeleccionadaPrincipal

        val realTotal =
            if (monedaSeleccionadaPr == "2") total else total / tasaCambioSeleccionadaPrincipal

        val retorno = realTotal - (realPrincipal + realComplemento)

        return if (retorno < 0) {
            valorReal(abs(retorno))
        } else {
            0.00
        }

    }

    private fun cantidadDeDescuento(ndoc: String): Double {
        var porcentajeDescuento = 0.00
        for (j in listaDescuentos.indices) {
            if (listaDescuentos[j].nrodoc == ndoc) {
                porcentajeDescuento = listaDescuentos[j].pordscto
            }
        }
        return porcentajeDescuento
    }

    private fun validarReten() {
        contadorRetenIVA = 0
        contadorRetenFlete = 0
        contadorRetenParme = 0
        contadorDoc = 0

        listaRetGuardada.forEach { retencion ->
            if (retencion.tiporet == "iva") {
                contadorRetenIVA++
            }
        }

        listaRetGuardada.forEach { retencion ->
            if (retencion.tiporet == "flete") {
                contadorRetenFlete++
            }
        }

        listaRetGuardada.forEach { retencion ->
            if (retencion.tiporet == "parme") {
                contadorRetenParme++
            }
        }

        listaDocumentos.forEach { documento ->
            if ((documento.bsretencioniva <= 0.0) && (documento.cbsretencioniva > 0)) {
                contadorDoc++
            }
        }
    }

    private fun calcularRetencion() {
        validarReten()
        if (contadorDoc == contadorRetenIVA && contadorRetenFlete == 0) {
            binding.apply {
                if (rbCxcCompMain.isChecked) {
                    cbExcReten.isChecked = true
                    cbExcReten.isEnabled = false
                }
                val moneda = if (rbCxcDivisasMain.isChecked) "USD" else "BSS"
                cargarSaldos(moneda, listaDocsSeleccionados, !cbExcReten.isChecked)
            }
        } else {
            binding.apply {
                cbExcReten.isChecked = false
                val moneda = if (rbCxcDivisasMain.isChecked) "USD" else "BSS"
                cargarSaldos(moneda, listaDocsSeleccionados, !cbExcReten.isChecked)
            }
            //Toast.makeText(this, "Ya agregó retención de flete", Toast.LENGTH_SHORT).show()
        }
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
        btnAceptar.setOnClickListener { _: View? -> creacion.dismiss() }

    }

    private fun contadorImagenesRequeridas(): Int {
        var contador = 1

        if (binding.cbCxcComplemento.isChecked) {
            contador++
        }

        contador += listaRetGuardada.size

        return contador

    }

    private fun setColors() {
        binding.apply {
            textInputLayout2.setColorModel(Constantes.AGENCIA)
            tilCxcSpbanco.setColorModel(Constantes.AGENCIA)
            tilCxcMontoMain.setColorModel(Constantes.AGENCIA)
            tilCxcRefMain.setColorModel(Constantes.AGENCIA)
            tilBancoCom.setColorModel(Constantes.AGENCIA)
            tilMontoCom.setColorModel(Constantes.AGENCIA)
            tilRefCom.setColorModel(Constantes.AGENCIA)

            btCxcRetenciones.setColorModelVariant(Constantes.AGENCIA)
            btnFoto.setColorModelVariant(Constantes.AGENCIA)

            btCxcProcesar.setBackgroundColor(btCxcProcesar.colorButtonAgencia(Constantes.AGENCIA))

            cbCxcComplemento.buttonTintList = cbCxcComplemento.setColorRadioButon(Constantes.AGENCIA)
            cbCxcDescuentos.buttonTintList = cbCxcDescuentos.setColorRadioButon(Constantes.AGENCIA)
            cbExcReten.buttonTintList = cbExcReten.setColorRadioButon(Constantes.AGENCIA)

            rbCxcDivisasMain.buttonTintList =
                rbCxcDivisasMain.setColorRadioButon(Constantes.AGENCIA)
            rbCxcBssMain.buttonTintList = rbCxcBssMain.setColorRadioButon(Constantes.AGENCIA)
            rbCxcCompMain.buttonTintList = rbCxcCompMain.setColorRadioButon(Constantes.AGENCIA)
            rbCxcAbonoMain.buttonTintList = rbCxcAbonoMain.setColorRadioButon(Constantes.AGENCIA)
            rbCxcTransfMain.buttonTintList = rbCxcTransfMain.setColorRadioButon(Constantes.AGENCIA)
            rbCxcEfectivoMain.buttonTintList =
                rbCxcEfectivoMain.setColorRadioButon(Constantes.AGENCIA)
            rbCxcTransfCom.buttonTintList = rbCxcTransfCom.setColorRadioButon(Constantes.AGENCIA)
            rbCxcEfectivoCom.buttonTintList =
                rbCxcEfectivoCom.setColorRadioButon(Constantes.AGENCIA)
            rbCxcDivisasCom.buttonTintList = rbCxcDivisasCom.setColorRadioButon(Constantes.AGENCIA)
            rbCxcBssCom.buttonTintList = rbCxcBssCom.setColorRadioButon(Constantes.AGENCIA)

            tvCxcNeto.setDrawableCobranzaAgencia(Constantes.AGENCIA)
            tvCxcIva.setDrawableCobranzaAgencia(Constantes.AGENCIA)
            tvCxcFlete.setDrawableCobranzaAgencia(Constantes.AGENCIA)
            tvCxcReten.setDrawableCobranzaAgencia(Constantes.AGENCIA)
            tvCxcDctos.setDrawableCobranzaAgencia(Constantes.AGENCIA)

            tvCxcTotal.setDrawableCobranzaVariantAgencia(Constantes.AGENCIA)

            textView43.setBackgroundColor(textView43.colorLabelAgencia(Constantes.AGENCIA))
            textView51.setBackgroundColor(textView51.colorLabelAgencia(Constantes.AGENCIA))
            textView66.setBackgroundColor(textView66.colorLabelAgencia(Constantes.AGENCIA))
            tvPrecioMostrarComplemento.setBackgroundColor(
                tvPrecioMostrarComplemento.colorLabelAgencia(
                    Constantes.AGENCIA
                )
            )
            tvCxcComplemento.setBackgroundColor(tvCxcComplemento.colorLabelAgencia(Constantes.AGENCIA))

            tilTasaselec.boxBackgroundColor = tilTasaselec.colorVariantAgencia(Constantes.AGENCIA)

        }
    }

    override fun getTheme(): Resources.Theme {
        val theme = super.getTheme()
        theme.applyStyle(setThemeAgencia(Constantes.AGENCIA), true)
        // you could also use a switch if you have many themes that could apply
        return theme
    }

}