package com.appcloos.mimaletin

import android.content.ContentValues
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.collections.ArrayList


class CreacionPrecobranzaActivity : AppCompatActivity() {
    private lateinit var conn: AdminSQLiteOpenHelper
    lateinit var ke_android: SQLiteDatabase
    lateinit var nroPrecobranza:String
    var fechaQuery:String? = ""
    var cod_usuario:String? = ""
    var codEmpresa:String?  = ""

    //Spinners
    lateinit var spBancosCompleto   : Spinner
    lateinit var spBancosImpuestos  : Spinner
    lateinit var spBancosComplemento: Spinner

    lateinit var preferences:SharedPreferences

    lateinit var listaDocsSeleccionados:ArrayList<String>

    lateinit var listaTasas            :ArrayList<tasas>
    lateinit var listaInfoTasas        :ArrayList<String>

    lateinit var listaBancos           :ArrayList<Bancos>
    lateinit var listaBancosImp        :ArrayList<Bancos>
    lateinit var listaBancosCm         :ArrayList<Bancos>


    lateinit var listaDocumentos       : ArrayList<Documentos>
    lateinit var listaInfoBancosImp    : ArrayList<String>
    lateinit var listaInfoBancosCm     : ArrayList<String>
    lateinit var listaInfoBancos       : ArrayList<String>
    lateinit var tipoDocsaPagar        : ArrayList<String>
    lateinit var listaDescuentos       : ArrayList<Descuentos>
    lateinit var listaRetGuardada      : ArrayList<Retenciones>
    lateinit var listaTiposRet         : ArrayList<String>

    var ll_commit:Boolean = false
    var request_code = 1
    var bsNeto = 0.00


    //textviews/arrayadapters
    lateinit var tv_fechareci:TextView
    lateinit var adapterTasasSp: ArrayAdapter<CharSequence>
    lateinit var txt_neto:TextView
    lateinit var txt_bsiva:TextView
    lateinit var txt_flete:TextView
    lateinit var txt_total: TextView
    lateinit var txt_reten: TextView

    //labels
    lateinit var lb_montoacobrar:TextView
    lateinit var tv_elegirmetodo:TextView
    lateinit var lb_mtopago:TextView
    lateinit var lb_impuestosbss:TextView
    lateinit var tv_elegirmetodocom:TextView
    lateinit var lb_complemento:TextView
    lateinit var lb_selecmoneda:TextView
    lateinit var lb_montos:TextView
    lateinit var lb_avisoconcepto:TextView
    //lateinit var txt_descuentos:TextView

    lateinit var txt_montorecibocom: EditText
    lateinit var txt_referenciacom: EditText
    lateinit var txt_montoreciboimp: EditText
    lateinit var txt_montorecibo: EditText
    lateinit var txt_referencia:EditText
    lateinit var et_fechatransf: EditText
    lateinit var txt_referenciaimbs: EditText
    //lateinit var txt_reten: TextView

    //radiobuttons
    lateinit var rb_divisascompleto        :RadioButton
    lateinit var rb_bsscompleto            :RadioButton
    lateinit var rb_efectivocompleto       :RadioButton
    lateinit var rb_transferenciacompleto  :RadioButton
    lateinit var rb_efectivocom            :RadioButton
    lateinit var rb_transfcom              :RadioButton
    lateinit var rb_bsscomplemento         :RadioButton
    lateinit var rb_divisascomplemento     :RadioButton
    lateinit var rb_completo               :RadioButton
    lateinit var rb_abono                  :RadioButton


    //checkboxs
    lateinit var cb_iva:            CheckBox
    lateinit var cb_descuentos:     CheckBox
    lateinit var cb_pagintercomp:   CheckBox
    lateinit var cb_pagintercm:     CheckBox
    lateinit var cb_complemento:    CheckBox
    lateinit var cb_notretiva:      CheckBox
    lateinit var cb_noretflete:     CheckBox


    //buttons
    lateinit var bt_retenciones: Button
    lateinit var bt_procesar: Button

    var dnetoTotal           = 0.00
    var bsIvaTotal           = 0.00
    var bsFleteTotal         = 0.00
    var bsretencionIvaTotal  = 0.00
    var bsRetencionTotal     = 0.00
    var montoaPagar          = 0.00
    var dtotimpuest          = 0.00
    var dFlete               = 0.00
    var dretencion           = 0.00
    var dretencioniva        = 0.00
    var dmontoTotal          = 0.00
    var bsmontoTotal         = 0.00
    var tasaNormal           = 0.00
    var tasaInterB           = 0.00
    var descuentoTotal       = 0.00
    var nroCorrelativo       = 0
    var dmontoRetFlete       = 0.00
    var bsmontoRetFlete      = 0.00
    var montominimo          = 0.00
    var cdretencion          = 0.00
    var cdretencioniva       = 0.00
    var cbsretparme          = 0.00
    var cdretparme           = 0.00
    var retenpagado          = 0.00
    var cbsretencioniva      = 0.00
    var montoMinimoRec       = 0.00
    var montoMinimoImp       = 0.00
    var montoMinimoComp      = 0.00
    var montoI               = 0.00
    var montoC               = 0.00
    var montoRec             = 0.00
    var saldo                = 0.00
    var montoNuevoIva        = 0.00
    var montoNuevoFlete      = 0.00



    //VARIABLES DE TEXTO
    var nombreEmpresa:String = "";  var codigoSucursal :String = "";  var enlaceEmpresa :String = "";
    var fecha_auxiliar:String = "" ; var fechaActual = ""; var idTasa:String = ""; var fechaTasa:String = "";
    var fechayHoraTasa:String = ""; var fechamodifitasa:String = ""
    var usuarioTasa:String = ""; var ipTasa:String = ""; var idTasaSeleccionada:String=""
    var fechayHoraSelecc:String = ""; var codbanco:String = ""; var nombanco:String =""; var fechamodifiBan:String = "";
    var CorrelativoTexto:String = ""; var tipoDoc = "PRC"; var codigoCliente = ""
    var codigoBancoCompleto = ""; var codigoBancoImpuesto = ""; var referenciaPrincipal = ""; var referenciaImp = ""
    var referenciaCm = "";   var tasaId = ""
    var banderaRetenciones:String = ""; var tipodeTransaccionPrincipal = ""; var codigoBancoComplemento = ""; var monedaSeleccionadaPr = ""

    // Variables de Double
    var tasaCambio:Double = 0.00; var tasaCambioSeleccionadaPrincipal:Double =0.00; var tasaCambioComplemento:Double = 0.00;
    var cuentanac:Double = 0.00; var inactiva:Double = 0.00; var tasaInterbancaria = 0.00;

    // on create function
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_creacion_precobranza)
        conn = AdminSQLiteOpenHelper(getApplicationContext(), "ke_android", null, 18)
        preferences  = getSharedPreferences("Preferences", MODE_PRIVATE)
        cod_usuario  = preferences.getString("cod_usuario", null)
        codEmpresa   = preferences.getString("codigoEmpresa", null)
        cargarEnlace()


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

        //labels
        txt_neto        = findViewById(R.id.txt_neto)
        tv_fechareci    = findViewById(R.id.tv_fechareci)
        txt_bsiva       = findViewById(R.id.txt_iva)
        txt_flete       = findViewById(R.id.txt_flete)
        txt_total       = findViewById(R.id.txt_total)


                //Edittexts
        txt_referencia      = findViewById(R.id.txt_referencia)
        txt_montorecibo     = findViewById(R.id.txt_montorecibo)
        txt_montorecibocom  = findViewById(R.id.txt_montorecibocom)
        txt_referenciacom   = findViewById(R.id.txt_referenciacom)
        txt_montoreciboimp  = findViewById(R.id.txt_montoreciboimp)
        txt_referenciaimbs  = findViewById(R.id.txt_referenciaimbs)
        txt_reten           = findViewById(R.id.txt_reten)

        //mas labels
        tv_elegirmetodo     = findViewById(R.id.tv_elegirmetodo)
        lb_selecmoneda      = findViewById(R.id.lb_selecmoneda)
        lb_montos           = findViewById(R.id.lb_montos)
        lb_avisoconcepto    = findViewById(R.id.lb_avisoconcepto)
        lb_complemento      = findViewById(R.id.lb_complemento)
        tv_elegirmetodocom  = findViewById(R.id.tv_elegirmetodocom)
        lb_impuestosbss     = findViewById(R.id.lb_impuestosbss)


        //para escoger fecha
        et_fechatransf          = findViewById(R.id.et_fechatransf)

        //checkboxs
        cb_iva                  = findViewById(R.id.cb_impbss)
        cb_pagintercomp         = findViewById(R.id.cb_pagintercomp)
        cb_pagintercm           = findViewById(R.id.cb_pagintercm)
        cb_complemento          = findViewById(R.id.cb_complemento)
        cb_descuentos           = findViewById(R.id.cb_descuentos)
        cb_notretiva            = findViewById(R.id.cb_notretiva)
        cb_noretflete           = findViewById(R.id.cb_noretflete)

        //Spinners
        spBancosCompleto        = findViewById(R.id.sp_selecbanco)
        spBancosImpuestos       = findViewById(R.id.sp_selecbancoim)
        spBancosComplemento     = findViewById(R.id.sp_selecbancocom)


        //radio buttons
        rb_bsscompleto           = findViewById(R.id.rb_bsscompleto)
        rb_divisascompleto       = findViewById(R.id.rb_divisascompleto)
        rb_efectivocompleto      = findViewById(R.id.rb_efectivorec)
        rb_transferenciacompleto = findViewById(R.id.rb_transfrec)
        rb_efectivocom           = findViewById(R.id.rb_efectivocom)
        rb_transfcom             = findViewById(R.id.rb_transfcom)
        rb_divisascomplemento    = findViewById(R.id.rb_divisascomplemento)
        rb_bsscomplemento        = findViewById(R.id.rb_bsscomplemento)
        rb_completo              = findViewById(R.id.rb_completo)
        rb_abono                 = findViewById(R.id.rb_abono)


        bt_retenciones  = findViewById(R.id.bt_retenciones)
        bt_procesar     = findViewById(R.id.bt_procesarrecibo)

        //me traigo el arraylist que cree en la activity anterior con los documentos a ser pagados
        listaDocsSeleccionados  = intent.getStringArrayListExtra("listaDocs") as ArrayList<String>
        fechaActual             = getFechaHoy()


        //validacion del correlativo para la cobranza
        var cursorCorrelativo = ke_android.rawQuery("SELECT MAX(kcor_numero) FROM ke_corprec WHERE kcor_vendedor ='" +cod_usuario+ "'", null)

        if(cursorCorrelativo.moveToFirst()){
            println("YA HAY CORRELATIVOS")
            nroCorrelativo    = cursorCorrelativo.getInt(0)
            nroCorrelativo+=1
            CorrelativoTexto  = nroCorrelativo.toString()
            CorrelativoTexto  = "0000$CorrelativoTexto"

        } else{
            nroCorrelativo    = cursorCorrelativo.getInt(0)
            nroCorrelativo+=1
            CorrelativoTexto  = nroCorrelativo.toString()
            CorrelativoTexto  = "0000$CorrelativoTexto"
        }


        //----
        //generacion del correlativo completo
        nroPrecobranza          = generarNroPrecobranza()
        supportActionBar?.title = "REC: " + nroPrecobranza
        listaDocsSeleccionados.joinToString(separator = ",")
        cargarDatosdeDocs(listaDocsSeleccionados)
        getTasas()

        rb_divisascompleto.isChecked = true
        rb_completo.isChecked = true
        rb_transferenciacompleto.isChecked = true
        monedaSeleccionadaPr = "2"

        condicionMonedaRecibo()
        getBancos("USD")
        buscarBancosParaImp("BSS")
        buscarBancosCm("USD")
        calcularDescuentos("USD")

        // obtengo la fecha
        tv_fechareci.text = getFechaHoy()
        //valido si hay hay retenciones seleccionadas
        validarSiHayRetenciones()
        validarCamposPagados()

        //bancos destino del Recibo principal (tambien llamado completo)
        spBancosCompleto.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
                //
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
               if(position != 0){
                   codigoBancoCompleto   = listaBancos.get(position-1).codbanco
                   //
               } else if( position == 0){
                   codigoBancoCompleto = ""
               }
            }
        }

        //bancos destino del Recibo de Impuestos y servicios en BSS
        spBancosImpuestos.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
                //
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                if(position != 0){
                    codigoBancoImpuesto    = listaBancosImp.get(position-1).codbanco

                } else if( position == 0){
                    codigoBancoImpuesto = ""
                }
            }
        }

        spBancosComplemento.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
                //
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                if(position != 0){
                    codigoBancoComplemento    = listaBancosCm.get(position-1).codbanco

                } else if( position == 0){
                    codigoBancoComplemento = ""
                }
            }
        }



        et_fechatransf.setOnClickListener { showDatePickerDialog() }

        rb_divisascompleto.setOnClickListener(View.OnClickListener {
            if(rb_divisascompleto.isChecked){
                buscarBancos("USD")
                rb_efectivocompleto.visibility = View.VISIBLE
                colocarMontosEnMonedaSelec("USD")
                monedaSeleccionadaPr = "2"
            }

        })

        rb_bsscompleto.setOnClickListener(View.OnClickListener {

            if(fechaQuery.equals("")){
                rb_efectivocompleto.visibility = View.INVISIBLE
                rb_efectivocompleto.isChecked  = false
                bt_procesar.visibility         = View.INVISIBLE
                cb_descuentos.visibility       = View.INVISIBLE

                Toast.makeText(this, "Debe elegir la fecha de pago", Toast.LENGTH_SHORT).show()
                monedaSeleccionadaPr = "1"
            }else{

                if(rb_bsscompleto.isChecked){

                    buscarBancos("BSS")
                    rb_efectivocompleto.visibility = View.INVISIBLE
                    rb_efectivocompleto.isChecked  = false
                    bt_procesar.visibility         = View.VISIBLE
                    cb_iva.visibility              = View.VISIBLE
                    cb_descuentos.visibility       = View.INVISIBLE
                    colocarMontosEnMonedaSelec("BSS")
                    monedaSeleccionadaPr = "1"

                }else{
                    cb_iva.visibility              = View.INVISIBLE
                }
            }


        })

        cb_descuentos.setOnClickListener(View.OnClickListener {
            if(cb_descuentos.isChecked){

                if(rb_divisascompleto.isChecked){
                    calcularDescuentos("USD")
                    colocarMontosEnMonedaSelec("USD")
                }/*else{
                    calcularDescuentos("BSS")
                    colocarMontosEnMonedaSelec("BSS")
                }*/

            }else{
                descuentoTotal = 0.00

                if(rb_divisascompleto.isChecked){

                    colocarMontosEnMonedaSelec("USD")
                }else{

                    colocarMontosEnMonedaSelec("BSS")
                }
            }
        }
        )

        rb_bsscomplemento.setOnClickListener(View.OnClickListener {
            if(rb_bsscomplemento.isChecked){
                buscarBancosCm("BSS")
                rb_efectivocom.visibility = View.INVISIBLE
                rb_efectivocom.isChecked = false
            }
        })



        rb_divisascomplemento.setOnClickListener(View.OnClickListener {
            if(rb_divisascomplemento.isChecked){
                buscarBancosCm("USD")
                rb_efectivocom.visibility = View.VISIBLE

            }
        })


        cb_iva.setOnClickListener(View.OnClickListener {
            if(cb_iva.isChecked){

                lb_impuestosbss.visibility    = View.VISIBLE
                spBancosImpuestos.visibility  = View.VISIBLE
                txt_montoreciboimp.visibility = View.VISIBLE
                txt_referenciaimbs.visibility = View.VISIBLE

            }else{

                lb_impuestosbss.visibility   = View.INVISIBLE
                spBancosImpuestos.visibility = View.INVISIBLE
                txt_montoreciboimp.visibility = View.INVISIBLE
                txt_referenciaimbs.visibility = View.INVISIBLE

            }
        })

        cb_complemento.setOnClickListener(View.OnClickListener {
            if(cb_complemento.isChecked){
                rb_divisascomplemento.isChecked = true
                rb_transfcom.isChecked          = true
                lb_complemento.visibility       = View.VISIBLE
                spBancosComplemento.visibility  = View.VISIBLE
                tv_elegirmetodocom.visibility   = View.VISIBLE
                rb_efectivocom.visibility       = View.VISIBLE
                rb_transfcom.visibility         = View.VISIBLE
                txt_montorecibocom.visibility   = View.VISIBLE
                txt_referenciacom.visibility    = View.VISIBLE
                rb_bsscomplemento.visibility    = View.VISIBLE
                rb_divisascomplemento.visibility= View.VISIBLE
                cb_pagintercm.visibility        = View.VISIBLE

            }else{
                lb_complemento.visibility       = View.INVISIBLE
                spBancosComplemento.visibility  = View.INVISIBLE
                tv_elegirmetodocom.visibility   = View.INVISIBLE
                rb_efectivocom.visibility       = View.INVISIBLE
                rb_transfcom.visibility         = View.INVISIBLE
                txt_referenciacom.visibility    = View.INVISIBLE
                txt_montorecibocom.visibility   = View.INVISIBLE
                rb_bsscomplemento.visibility    = View.INVISIBLE
                rb_divisascomplemento.visibility= View.INVISIBLE
                cb_pagintercm.visibility        = View.INVISIBLE
            }
        })


        cb_pagintercomp.setOnClickListener(View.OnClickListener {
            tasaCambioSeleccionadaPrincipal = ValidarTasaCambioCompleto()

            if(rb_divisascompleto.isChecked){
                colocarMontosEnMonedaSelec("USD")
            }else{
                colocarMontosEnMonedaSelec("BSS")
            }

        })

        cb_pagintercm.setOnClickListener(View.OnClickListener {
            tasaCambioComplemento = ValidarTasaCambioComplemento()
        })



        bt_retenciones.setOnClickListener(View.OnClickListener {
            println("BOTON DE RETENCIONES PRESIONADO")
            irAretenciones()

        }
        )

        bt_procesar.setOnClickListener(
            View.OnClickListener {
                if(fechaQuery.equals("") || fechaQuery.equals(null)){
                    Toast.makeText(this, "Debe indicar la fecha de Pago", Toast.LENGTH_LONG).show()
                }else{
                    procesoDeValidacion()
                }


            }
        )

        cb_noretflete.setOnClickListener(View.OnClickListener {
            validarSiHayRetenciones()
            condicionMonedaRecibo()
        })

        cb_notretiva.setOnClickListener(View.OnClickListener {
            condicionMonedaRecibo()
        })



    }

    private fun validarCamposPagados() {
        if(dretencion >= cdretencion && dtotimpuest > 0.00){
            cb_iva.visibility = View.INVISIBLE
        }

    }

    private fun procesoDeValidacion() {
        /* PROCESO DE VALIDACION Y PROCESAMIENTO DE DATOS DE CXC
         1 - PRIMERO SE VALIDA SI ES UNO O VARIOS DOCS A PAGAR
         2 - EN FUNCION A LO ANTERIOR, SE VALIDA SI EL PAGO ES COMPLETO O UN ABONO
         3 - LUEGO, SE VALIDA SI EL PAGO ES EN BSS O EN DIVISAS
         4 - SE EVALUA SI CUMPLE CON TODOS LOS MONTOS Y MONTOS MINIMOS
         5 - POSTERIOR, SE EVALUA LOS MONTOS DE RETENCIONES Y SI LAS RETENCIONES ESTÁN COMPLETAS
         6 - POR ULTIMO, SE GUARDAN LOS DATOS
        */

        //inicializamos variables en 0
        montominimo = 0.00

        //valida cantidad de docs
        if(listaDocumentos.size > 1){

            // valido si es completo o abono (en este caso abono NO existe)
            // ABONO NO EXISTE, NO AGREGAR, ABONO NO EXISTE COÑOOOOO
            if(rb_completo.isChecked){

                if(rb_bsscompleto.isChecked){
                    montominimo = txt_total.text.toString().toDouble()
                    if(validarMontos(montominimo, 0, 0)){
                        if(validarListadeRetenciones()){
                            if(validarBancos()){
                                procesamientoDeDatos()
                            }
                        }
                    }
                }else if(rb_divisascompleto.isChecked){
                    montominimo = txt_total.text.toString().toDouble()
                    if(validarMontos(montominimo, 1, 0)){
                        if(validarListadeRetenciones()){
                            if(validarBancos()){
                                procesamientoDeDatos()
                            }
                        }
                    }
                }
            }

        //si solo tiene un documento,
        }else if(listaDocumentos.size == 1){
            //valido si es completo o abono (en este caso abono SI existe)
            if(rb_completo.isChecked){
                if(rb_bsscompleto.isChecked){
                    montominimo = txt_total.text.toString().toDouble()
                    if(validarMontos(montominimo, 0, 0)){
                       if(validarListadeRetenciones()){
                           if(validarBancos()){
                               procesamientoDeDatos()
                           }
                       }
                    }
                }else if(rb_divisascompleto.isChecked){
                    montominimo = txt_total.text.toString().toDouble()
                    if(validarMontos(montominimo, 1, 0)){
                        if(validarListadeRetenciones()){
                            if(validarBancos()){
                                procesamientoDeDatos()
                            }
                        }
                    }
                }

            }else if(rb_abono.isChecked){
                montominimo = txt_flete.text.toString().toDouble() + txt_bsiva.text.toString().toDouble()
                if(rb_bsscompleto.isChecked){
                    montominimo = txt_flete.text.toString().toDouble() + txt_bsiva.text.toString().toDouble()
                    if(validarMontos(montominimo, 0, 1)){
                        if(validarListadeRetenciones()){
                            if(validarBancos()){
                                procesamientoDeDatos()
                            }
                        }
                    }
                }else if(rb_divisascompleto.isChecked){
                    montominimo = txt_flete.text.toString().toDouble() + txt_bsiva.text.toString().toDouble()
                    if(validarMontos(montominimo, 1, 1)){
                        if(validarListadeRetenciones()){
                            if(validarBancos()){
                                procesamientoDeDatos()
                            }
                        }
                    }
                }
            }

        }

    }

    private fun validarMontos(montoMinimo:Double, tipodiv:Int, tipomodo: Int): Boolean {

        /* el tipodiv indica el tipo de divisa de pago, y el tipomodo, si es abono o
        *  o completo, que debe tomarse en cuenta */

        //declaro una variable booleana a ser devuelta

        var response = false

        if(tipomodo == 0){
            if(txt_montorecibo.text.toString().equals("") || txt_montorecibo.text.toString().equals(null)){
                Toast.makeText(this, "No se permiten montos en blanco", Toast.LENGTH_SHORT).show()
                return false
            }else{
                var montoreportadopr = txt_montorecibo.text.toString().toDouble()
                var montoreportadoim = 0.00
                var montoreportadocm = 0.00
                var montominimoMod   = montoMinimo

                //defino los montos minimos
                montoMinimoComp = 1.0
                when(tipodiv){
                    0->{

                        //caso bss
                        if(!cb_iva.isChecked && !cb_complemento.isChecked){
                            //si el monto reportado es mayor o igual al minimo ( o no)
                            if(montoreportadopr >= montoMinimo){
                                response = true
                            }else{
                                Toast.makeText(this, "El monto del recibo es insuficiente", Toast.LENGTH_SHORT).show()
                                return false
                            }
                            println("llegue a la validación donde no hay nada seleccionado de campos")
                        }else{


                            //validar si iva y flete en bss
                            if(cb_iva.isChecked){
                                if(txt_montoreciboimp.text.toString().toDouble().equals("") || txt_montoreciboimp.text.toString().toDouble().equals(null)){
                                    Toast.makeText(this, "No se permiten montos en blanco", Toast.LENGTH_SHORT).show()
                                    return false
                                }else{
                                    montoreportadoim = txt_montoreciboimp.text.toString().toDouble()
                                    montoMinimoImp  = bsFleteTotal + bsIvaTotal
                                    //si el monto reportado es mayor o igual al minimo ( o no)
                                    if(montoreportadoim >= montoMinimoImp) {
                                        response = true
                                        montominimoMod -= montoMinimoImp
                                    }else{
                                        Toast.makeText(this, "Monto insuficiente en los pagos de iva y flete en bss", Toast.LENGTH_SHORT).show()
                                        return false
                                    }
                                }
                            }


                            //validar si se va a pagar complemento
                            if(cb_complemento.isChecked){

                                if(rb_divisascomplemento.isChecked){
                                    if(txt_montorecibocom.text.toString().equals("") || txt_montorecibocom.text.toString().equals(null)){
                                        Toast.makeText(this, "No se permiten montos en blanco", Toast.LENGTH_SHORT).show()
                                        return false
                                    }else{
                                        montoreportadocm = txt_montorecibocom.text.toString().toDouble()
                                        if(montoreportadocm >= montoMinimoComp) {
                                            response = true
                                            montoreportadocm = montoreportadocm * tasaCambioComplemento
                                            montominimoMod -= montoreportadocm
                                        }else{
                                            Toast.makeText(this, "Monto insuficiente en el complemento", Toast.LENGTH_SHORT).show()
                                            return false
                                        }
                                    }

                                }else if(rb_bsscomplemento.isChecked){
                                    if(txt_montorecibocom.text.toString().equals("") || txt_montorecibocom.text.toString().equals(null)){
                                        Toast.makeText(this, "No se permiten montos en blanco", Toast.LENGTH_SHORT).show()
                                        return false
                                    }else{
                                        montoreportadocm = txt_montorecibocom.text.toString().toDouble()
                                        if(montoreportadocm >= 1){
                                            response = true
                                        }else{
                                            Toast.makeText(this, "Monto insuficiente en el complemento", Toast.LENGTH_SHORT).show()
                                            return false
                                        }
                                    }
                                }
                            }
                            var montoTotalRep = montoreportadopr
                            if(montoTotalRep >= montominimoMod){
                                response = true
                            }else{
                                Toast.makeText(this, "Monto(s) insuficientes(s)", Toast.LENGTH_SHORT).show()
                            }

                        }
                    }

                    1->{

                        //caso divisas
                        if(!cb_iva.isChecked && !cb_complemento.isChecked){
                            //si el monto reportado es mayor o igual al minimo ( o no)
                            if(montoreportadopr >= montoMinimo){
                                response = true
                            }else{
                                Toast.makeText(this, "El monto del recibo es insuficiente", Toast.LENGTH_SHORT).show()
                                return false
                            }
                        }else{

                            //validar si iva y flete en bss
                            if(cb_iva.isChecked){
                                if(txt_montoreciboimp.text.toString().toDouble().equals("") || txt_montoreciboimp.text.toString().toDouble().equals(null)){
                                    Toast.makeText(this, "No se permiten montos en blanco", Toast.LENGTH_SHORT).show()
                                    return false
                                }else{
                                    montoreportadoim      = txt_montoreciboimp.text.toString().toDouble()
                                    montoMinimoImp        = bsmontoRetFlete + bsIvaTotal

                                    var montoMinimoImpDiv = txt_bsiva.toString().toDouble() + txt_flete.toString().toDouble()
                                    //si el monto reportado es mayor o igual al minimo ( o no)
                                    if(montoreportadoim >= montoMinimoImp) {
                                        response = true
                                        montominimoMod -= montoMinimoImpDiv
                                    }else{
                                        Toast.makeText(this, "Monto insuficiente en los pagos de iva y flete en bss", Toast.LENGTH_SHORT).show()
                                        return false
                                    }
                                }

                            }


                            //validar si se va a pagar complemento
                            if(cb_complemento.isChecked){

                                if(rb_divisascomplemento.isChecked){
                                    if(txt_montorecibocom.text.toString().equals("") || txt_montorecibocom.text.toString().equals(null)){
                                        Toast.makeText(this, "No se permiten montos en blanco", Toast.LENGTH_SHORT).show()
                                        return false
                                    }else{
                                        montoreportadocm = txt_montorecibocom.text.toString().toDouble()
                                        if(montoreportadocm >= 1) {
                                            response = true
                                            montominimoMod -= montoreportadocm
                                        }else{
                                            Toast.makeText(this, "Monto insuficiente en el complemento", Toast.LENGTH_SHORT).show()
                                            return false
                                        }
                                    }


                                }else if(rb_bsscomplemento.isChecked){
                                    if(txt_montorecibocom.text.toString().equals("") || txt_montorecibocom.text.toString().equals(null)){
                                        Toast.makeText(this, "No se permiten montos en blanco", Toast.LENGTH_SHORT).show()
                                        return false
                                    }else{
                                        montoreportadocm = txt_montorecibocom.text.toString().toDouble()
                                        montoreportadocm = montoreportadocm / tasaCambioComplemento
                                        if(montoreportadocm >= 1){
                                            montominimoMod -= montoreportadocm
                                            response = true
                                        }else{
                                            Toast.makeText(this, "Monto insuficiente en el complemento", Toast.LENGTH_SHORT).show()
                                            return false
                                        }
                                    }

                                }
                            }

                            var montoTotalRep = montoreportadopr
                            if(montoTotalRep >= montominimoMod){
                                response = true
                            }else{
                                Toast.makeText(this, "Monto(s) insuficientes(s)", Toast.LENGTH_SHORT).show()
                                return false
                            }
                        }

                    }
                }

            }
        }else if (tipomodo == 1){
            if(txt_montorecibo.text.toString().equals("") || txt_montorecibo.text.toString().equals(null)){
                Toast.makeText(this, "No se permiten montos en blanco", Toast.LENGTH_SHORT).show()
                return false
            }else{
                var montoreportadopr = txt_montorecibo.text.toString().toDouble()
                var montoreportadoim = 0.00
                var montoreportadocm = 0.00
                var montominimoMod   = montoMinimo

                //defino los montos minimos
                montoMinimoComp = 1.0
                when(tipodiv){
                    0->{

                        //caso bss
                        if(!cb_iva.isChecked && !cb_complemento.isChecked){
                            //si el monto reportado es mayor o igual al minimo ( o no)
                            if(montoreportadopr >= montoMinimo){
                                response = true
                            }else{
                                Toast.makeText(this, "El monto del recibo es insuficiente", Toast.LENGTH_SHORT).show()
                                return false
                            }
                            println("llegue a la validación donde no hay nada seleccionado de campos")
                        }else{


                            //validar si iva y flete en bss
                            if(cb_iva.isChecked){
                                if(txt_montoreciboimp.text.toString().toDouble().equals("") || txt_montoreciboimp.text.toString().toDouble().equals(null)){
                                    Toast.makeText(this, "No se permiten montos en blanco", Toast.LENGTH_SHORT).show()
                                    return false
                                }else{
                                    montoreportadoim = txt_montoreciboimp.text.toString().toDouble()
                                    montoMinimoImp  = bsFleteTotal + bsIvaTotal
                                    //si el monto reportado es mayor o igual al minimo ( o no)
                                    if(montoreportadoim >= montoMinimoImp) {
                                        response = true

                                    }else{
                                        Toast.makeText(this, "Monto insuficiente en los pagos de iva y flete en bss", Toast.LENGTH_SHORT).show()
                                        return false
                                    }
                                }
                            }


                            //validar si se va a pagar complemento
                            if(cb_complemento.isChecked){

                                if(rb_divisascomplemento.isChecked){
                                    if(txt_montorecibocom.text.toString().equals("") || txt_montorecibocom.text.toString().equals(null)){
                                        Toast.makeText(this, "No se permiten montos en blanco", Toast.LENGTH_SHORT).show()
                                        return false
                                    }else{
                                        montoreportadocm = txt_montorecibocom.text.toString().toDouble()
                                        if(montoreportadocm >= montoMinimoComp) {
                                            response = true
                                            montoreportadocm = montoreportadocm * tasaCambioComplemento

                                        }else{
                                            Toast.makeText(this, "Monto insuficiente en el complemento", Toast.LENGTH_SHORT).show()
                                            return false
                                        }
                                    }

                                }else if(rb_bsscomplemento.isChecked){
                                    if(txt_montorecibocom.text.toString().equals("") || txt_montorecibocom.text.toString().equals(null)){
                                        Toast.makeText(this, "No se permiten montos en blanco", Toast.LENGTH_SHORT).show()
                                        return false
                                    }else{
                                        montoreportadocm = txt_montorecibocom.text.toString().toDouble()
                                        if(montoreportadocm >= 1){
                                            response = true
                                        }else{
                                            Toast.makeText(this, "Monto insuficiente en el complemento", Toast.LENGTH_SHORT).show()
                                            return false
                                        }
                                    }
                                }
                            }
                            var montoTotalRep = montoreportadopr
                            if(montoTotalRep >= montominimoMod){
                                response = true
                            }else{
                                Toast.makeText(this, "Monto(s) insuficientes(s)", Toast.LENGTH_SHORT).show()
                                return false
                            }

                        }
                    }

                    1->{

                        //caso divisas
                        if(!cb_iva.isChecked && !cb_complemento.isChecked){
                            //si el monto reportado es mayor o igual al minimo ( o no)
                            if(montoreportadopr >= montoMinimo){
                                response = true
                            }else{
                                Toast.makeText(this, "El monto del recibo es insuficiente", Toast.LENGTH_SHORT).show()
                                return false
                            }
                        }else{

                            //validar si iva y flete en bss
                            if(cb_iva.isChecked){
                                if(txt_montoreciboimp.text.toString().toDouble().equals("") || txt_montoreciboimp.text.toString().toDouble().equals(null)){
                                    Toast.makeText(this, "No se permiten montos en blanco", Toast.LENGTH_SHORT).show()
                                    return false
                                }else{
                                    montoreportadoim      = txt_montoreciboimp.text.toString().toDouble()
                                    montoMinimoImp        = bsmontoRetFlete + bsIvaTotal

                                    var montoMinimoImpDiv = txt_bsiva.toString().toDouble() + txt_flete.toString().toDouble()
                                    //si el monto reportado es mayor o igual al minimo ( o no)
                                    if(montoreportadoim >= montoMinimoImp) {
                                        response = true

                                    }else{
                                        Toast.makeText(this, "Monto insuficiente en los pagos de iva y flete en bss", Toast.LENGTH_SHORT).show()
                                        return false
                                    }
                                }

                            }


                            //validar si se va a pagar complemento
                            if(cb_complemento.isChecked){

                                if(rb_divisascomplemento.isChecked){
                                    if(txt_montorecibocom.text.toString().equals("") || txt_montorecibocom.text.toString().equals(null)){
                                        Toast.makeText(this, "No se permiten montos en blanco", Toast.LENGTH_SHORT).show()
                                        return false
                                    }else{
                                        montoreportadocm = txt_montorecibocom.text.toString().toDouble()
                                        if(montoreportadocm >= 1) {
                                            response = true
                                            montominimoMod -= montoreportadocm
                                        }else{
                                            Toast.makeText(this, "Monto insuficiente en el complemento", Toast.LENGTH_SHORT).show()
                                            return false
                                        }
                                    }


                                }else if(rb_bsscomplemento.isChecked){
                                    if(txt_montorecibocom.text.toString().equals("") || txt_montorecibocom.text.toString().equals(null)){
                                        Toast.makeText(this, "No se permiten montos en blanco", Toast.LENGTH_SHORT).show()
                                        return false
                                    }else{
                                        montoreportadocm = txt_montorecibocom.text.toString().toDouble()
                                        montoreportadocm = montoreportadocm / tasaCambioComplemento
                                        if(montoreportadocm >= 1){
                                            response = true
                                        }else{
                                            Toast.makeText(this, "Monto insuficiente en el complemento", Toast.LENGTH_SHORT).show()
                                            return false
                                        }
                                    }

                                }
                            }

                            var montoTotalRep = montoreportadopr
                            if(montoTotalRep >= montominimoMod){
                                response = true
                            }else{
                                Toast.makeText(this, "Monto(s) insuficientes(s)", Toast.LENGTH_SHORT).show()
                                return false
                            }
                        }

                    }
                }

            }
        }


        return response

    }



    private fun validarListadeRetenciones():Boolean {
        var todoOK = false

        if(cdretencion > 0.00 && dretencion <= 0.00 && !cb_notretiva.isChecked && !cb_noretflete.isChecked){
            if(listaRetGuardada.isEmpty()){
                todoOK = false
                Toast.makeText(this, "Debe añadir retenciones al recibo", Toast.LENGTH_LONG).show()
            }else{
                if((listaDocumentos.sumOf { it.cdretencion } - listaRetGuardada.sumOf { it.montoret }) < 1 ){
                    todoOK = true
                }
            }
        }else{
            todoOK = true
        }
        return todoOK
    }






    //metodo final que será usado para guardar los datos
    private fun procesamientoDeDatos() {
        //variable de control
        var ll_commit = false
        ke_android = conn.writableDatabase
        //listas con el tipo de datos para los recibos
        var listaReciboPrCabecera:ArrayList<CXC>  = ArrayList()
        var listaReciboPrLineas: ArrayList<CXC>   = ArrayList()
        var listaReciboIsH:ArrayList<CXC>         = ArrayList()
        var listaReciboIsL:ArrayList<CXC>         = ArrayList()
        var listaReciboCmH:ArrayList<CXC>         = ArrayList()
        var listaReciboCmL:ArrayList<CXC>         = ArrayList()


        listaDocumentos.sortBy { it.fechaDocs }

        Toast.makeText(this, "llegue al procesamiento de datos", Toast.LENGTH_LONG).show()


        if(!cb_iva.isChecked && !cb_complemento.isChecked){
            montoRec = txt_montorecibo.text.toString().toDouble()
            Toast.makeText(this, "entre al caso 1 del procesamiento", Toast.LENGTH_LONG).show()
            //caso donde todo se va a pagar en una sola moneda sin complemento ni servicios en bss

            //llenado de datos de cabecera
            var cxc = CXC()
            cxc.id_recibo  = nroPrecobranza
            cxc.tipoRecibo = "W"
            cxc.codigoVend = cod_usuario.toString()
            cxc.kecxc_id   = tasaId
            cxc.tasadia    = tasaCambioSeleccionadaPrincipal
            cxc.fchrecibo  = fechaActual
            cxc.clicontesp = "" //esto lo jalo de  la lista de docs?
            cxc.moneda     = monedaSeleccionadaPr
            cxc.bcocod     = codigoBancoCompleto
            cxc.bcomonto   = txt_montorecibo.text.toString().toDouble()
            cxc.bcoref     = txt_referencia.text.toString()
            cxc.edorec     = "0"
            cxc.fchhr      = fechaActual

            println("cargue los datos en cabecera")
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


                if(listaReciboPrLineas[i].documento == listaDocumentos[i].documento && listaDocumentos[i].tipodocv == "FAC"){
                    //descuento ivas y fletes
                    listaReciboPrLineas[i].bsmtoiva = listaDocumentos[i].bsiva
                    listaReciboPrLineas[i].doliva   = listaDocumentos[i].dtotimpuest - listaDocumentos[i].cdretencioniva


                    if(rb_divisascompleto.isChecked){
                        var restaIva = valorReal((listaDocumentos[i].dtotimpuest - listaDocumentos[i].cdretencioniva))
                        montoRec -= restaIva
                        listaReciboPrLineas[i].bscobro  += restaIva

                    }else{
                        var restaIva = valorReal((listaDocumentos[i].bsiva - listaDocumentos[i].cbsretencioniva))
                        montoRec -= restaIva
                        listaReciboPrLineas[i].bscobro  += restaIva

                    }

                    listaReciboPrLineas[i].bsmtofte = listaDocumentos[i].bsflete
                    listaReciboPrLineas[i].dolflete = listaDocumentos[i].dFlete
                    println("monto de ret de flete en el doc: " + listaDocumentos[i].cbsretflete)
                    listaReciboPrLineas[i].bscobro  += valorReal((listaDocumentos[i].bsflete - listaDocumentos[i].cbsretflete))

                    if(rb_divisascompleto.isChecked){
                        montoRec -= valorReal((listaDocumentos[i].dFlete - listaDocumentos[i].cdretflete))
                    }else{
                        montoRec -= valorReal((listaDocumentos[i].bsflete - listaDocumentos[i].cbsretflete))
                    }



                }else if(listaReciboPrLineas[i].documento == listaDocumentos[i].documento && listaDocumentos[i].tipodocv == "N/E"){
                    //descuento ivas y fletes
                    listaReciboPrLineas[i].bsmtoiva = 0.00
                    listaReciboPrLineas[i].bsmtofte = listaDocumentos[i].bsflete
                    listaReciboPrLineas[i].dolflete = listaDocumentos[i].dFlete
                    listaReciboPrLineas[i].bscobro  += listaDocumentos[i].bsflete

                    montoRec -= listaDocumentos[i].dFlete

                }

            }

            //llenado de los netos
            for (i in listaReciboPrLineas.indices){
                println("voy al segundo ciclo")
                println(" monto del Recibo que llega al neto: ${montoRec}")
                if(montoRec < 1){
                    // borré el return
                }else{
                    if(rb_divisascompleto.isChecked){
                        var netoRealenDoc = listaDocumentos[i].dtotneto - (listaDocumentos[i].dFlete - listaDocumentos[i].cbsretflete)
                        netoRealenDoc = valorReal(netoRealenDoc)

                        if( montoRec >= netoRealenDoc){
                            var bscobrado = (listaDocumentos[i].dtotneto - (listaDocumentos[i].dFlete - listaDocumentos[i].cbsretflete)) * listaDocumentos[i].tasadoc
                            bscobrado     = valorReal(bscobrado)

                            println(" bs del neto a cobrar: ${bscobrado}")

                            listaReciboPrLineas[i].bscobro += valorReal(bscobrado)

                            println(" monto agregado: ${listaReciboPrLineas[i].bscobro}")

                            var netocobrado = listaDocumentos[i].dtotneto - listaDocumentos[i].dFlete
                            listaReciboPrLineas[i].dolneto = valorReal(netocobrado)
                            montoRec -= netocobrado
                        }

                       else if(netoRealenDoc > montoRec && montoRec > 0) {
                            println(" monto que queda del recibo: ${montoRec}")

                            var cobroAbono = montoRec * listaDocumentos[i].tasadoc

                            listaReciboPrLineas[i].bscobro += valorReal(cobroAbono)
                            var netocobrado =  (listaDocumentos[i].dtotneto - listaDocumentos[i].dFlete) - ( (listaDocumentos[i].dtotneto - listaDocumentos[i].dFlete) - montoRec)
                            listaReciboPrLineas[i].dolneto = valorReal(netocobrado)
                            montoRec -= netocobrado
                            montoRec= valorReal(montoRec)
                       }
                    }

                    if(rb_bsscompleto.isChecked){
                        var netoRealenDoc = listaDocumentos[i].dtotneto - listaDocumentos[i].dFlete
                        netoRealenDoc = valorReal(netoRealenDoc)

                        //si el monto del recibo cubre el monto del documento
                        if( montoRec > (netoRealenDoc * tasaCambioSeleccionadaPrincipal)){
                            var bscobrado = valorReal(netoRealenDoc * listaDocumentos[i].tasadoc)
                            listaReciboPrLineas[i].bscobro += bscobrado
                            var netocobrado = listaDocumentos[i].dtotneto - listaDocumentos[i].dFlete
                            listaReciboPrLineas[i].dolneto = netocobrado

                            montoRec -= valorReal(netoRealenDoc * listaDocumentos[i].tasadoc)

                        }
                        //si el monto del recibo no cubre el monto completo del documento (se comporta como un abono)
                        else if((netoRealenDoc * tasaCambioSeleccionadaPrincipal) > montoRec && montoRec > 0) {
                            var cobroAbono = montoRec
                            listaReciboPrLineas[i].bscobro += valorReal(cobroAbono)
                            var dolneto = (montoRec / tasaCambioSeleccionadaPrincipal)
                            listaReciboPrLineas[i].dolneto = valorReal(dolneto)
                            montoRec -= montoRec
                        }
                    }
                    //los bss del neto cobrado

                    /*recorrido de lista de descuentos para asignar cantidad según nro doc. (de tener)*/
                    for(j in listaDescuentos.indices){
                        if(listaDescuentos[j].nrodoc == listaDocumentos[i].documento){
                            listaReciboPrLineas[i].prcdsctopp = listaDescuentos[j].cantdscto
                        }
                    }

                }
            }

            /*esto va a ser resultado de la suma de los campos de la lista (ke_precobranza)
             de detalles */

            var difReteIva   = valorReal(listaReciboPrLineas.sumOf { it.bsmtoiva }) + valorReal(listaReciboPrLineas.sumOf { it.bsretiva })
            var difRetyFlete = valorReal(listaReciboPrLineas.sumOf { it.bsmtofte }) + valorReal(listaReciboPrLineas.sumOf { it.bsretfte })
            var netoReal   = valorReal(listaReciboPrLineas.sumOf { it.bscobro }) - difReteIva - difRetyFlete
            cxc.bsneto     = valorReal(netoReal)
            cxc.bsretiva   = valorReal(listaReciboPrLineas.sumOf { it.bsretiva })
            cxc.bsiva      = valorReal(listaReciboPrLineas.sumOf { it.bsmtoiva })
            cxc.bsflete    = valorReal(listaReciboPrLineas.sumOf { it.bsmtofte })

            /* sumo los bss en total y los redondeo al momento de guardarlo */
            var bssumaTotal  = valorReal(listaReciboPrLineas.sumOf { it.bscobro })

            cxc.bstotal    = valorReal(bssumaTotal)
            cxc.dolneto    = valorReal(listaReciboPrLineas.sumOf { it.dolneto } )
            cxc.doliva     = valorReal(listaReciboPrLineas.sumOf { it.doliva })
            cxc.dolretiva  = valorReal(listaDocumentos.sumOf { it.cdretencioniva })
            cxc.dolflete   = valorReal(listaReciboPrLineas.sumOf{it.dolflete})
            var doltotal = listaReciboPrLineas.sumOf { it.dolneto } + listaDocumentos.sumOf { it.dtotimpuest } + listaReciboPrLineas.sumOf { it.dolflete }
            cxc.doltotal   = valorReal(doltotal)
            cxc.netocob    = 0.00
            cxc.bsretflete = valorReal(listaReciboPrLineas.sumOf { it.bsretflete })
            cxc.retmun_sbi = 0.00//definir
            cxc.retmun_sbs = 0.00//definir
            var fechaVigen = fechaSuma(fechaActual, 3)
            cxc.fchvigen   = fechaVigen
            cxc.moneda     = monedaSeleccionadaPr
            cxc.tasadia    = tasaCambioSeleccionadaPrincipal
            listaReciboPrCabecera.add(cxc)

            println("voy a insertar los datos")
            try{

                // inicio la transacción
                ke_android.beginTransaction()
                var qcabecera:ContentValues = ContentValues()
                var qlineas:  ContentValues = ContentValues()

                for (i in listaReciboPrCabecera.indices){
                    println("recorrido de cabecera")
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
                    qcabecera.put("dolretiva", listaReciboPrCabecera[i].dolretiva)
                    qcabecera.put("dolflete", listaReciboPrCabecera[i].dolflete)
                    qcabecera.put("doltotal", listaReciboPrCabecera[i].doltotal)
                    qcabecera.put("moneda", listaReciboPrCabecera[i].moneda)
                    qcabecera.put("bcocod", listaReciboPrCabecera[i].bcocod)
                    qcabecera.put("bcomonto", listaReciboPrCabecera[i].bcomonto)
                    qcabecera.put("bcoref", listaReciboPrCabecera[i].bcoref)
                    qcabecera.put("edorec", listaReciboPrCabecera[i].edorec)
                    qcabecera.put("fchvigen", listaReciboPrCabecera[i].fchvigen)
                    qcabecera.put("bsretflete", listaReciboPrCabecera[i].bsretflete)

                    for(j in listaReciboPrLineas.indices){
                        println("recorrido de lineas")
                        qlineas.put("cxcndoc", listaReciboPrLineas[j].id_recibo)
                        qlineas.put("agencia",   listaReciboPrLineas[j].agencia)
                        qlineas.put("tipodoc",   listaReciboPrLineas[j].tipodoc)
                        qlineas.put("documento", listaReciboPrLineas[j].documento)
                        qlineas.put("bscobro", listaReciboPrLineas[j].bscobro)
                        println("bs cobrados en la linea ${j} : ${listaReciboPrLineas[j].bscobro}")
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

                        ke_android.insert("ke_precobradocs", null, qlineas)
                    }
                }

                ke_android.insert("ke_precobranza", null, qcabecera)
                ll_commit = true

                var qcorrelativo: ContentValues = ContentValues()
                qcorrelativo.put("kcor_numero", nroCorrelativo)
                qcorrelativo.put("kcor_vendedor", cod_usuario)

                ke_android.insert("ke_corprec", null, qcorrelativo)

            }catch (exception: SQLException){

                println(exception.message)
                ll_commit = false

                ke_android.endTransaction()
                if(!ll_commit){
                    return
                }
            }

            if(ll_commit){
                ke_android.setTransactionSuccessful()
                ke_android.endTransaction()
                Toast.makeText(this, "RECIBO CREADO", Toast.LENGTH_SHORT).show()
                finish()
            }else{
                ke_android.endTransaction()
            }





        }else if(cb_iva.isChecked && !cb_complemento.isChecked){
            Toast.makeText(this, "entre al caso 2 del procesamiento", Toast.LENGTH_LONG).show()
            //caso donde son dos recibos ( neto el primero e iva y flete en el segundo).

        }else if(!cb_iva.isChecked && cb_complemento.isChecked){
            Toast.makeText(this, "entre al caso 3 del procesamiento", Toast.LENGTH_LONG).show()
            //caso donde son dos recibos (el neto e iba y un complemento adicional).

            //descuento del monto neto





        }else if(cb_iva.isChecked && cb_complemento.isChecked){
            Toast.makeText(this, "entre al caso 4 del procesamiento", Toast.LENGTH_LONG).show()
        //caso donde son tres recibos (el neto, iva  flete en bss, y un complemento.

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

    private fun validarSiHayRetenciones() {
        var cursor:Cursor = ke_android.rawQuery("SELECT contribespecial FROM cliempre WHERE codigo= '$codigoCliente'", null)
        var esConEspecial = "0"
        if(cursor.moveToFirst()){
            esConEspecial = cursor.getString(0)
        }

        if(esConEspecial.equals("1")){
            //primero analizo el monto total de las retenciones
            var montoRetencionTpp    = cdretencion
            var montoRetencionTpf    = cdretencion
            var montoRetencionPagado = dretencion

            var isRetpagado:Double  = cdretencion.minus(dretencion)
            var isRetivaPagado      = cdretencioniva.minus(dretencioniva)


            println("monto de retencion total : $cdretencion")
            println("monto de diferencia total : $isRetpagado")
            println("monto diferencia iva: $isRetivaPagado")

            // valido que ya estas ret hayan sido pagadas
            var hayretParme:Double  =   montoRetencionPagado  - cdretencioniva - dmontoRetFlete
            var hayretFlete:Double  =   montoRetencionPagado - cdretencioniva - cdretparme
            println("monto diferencia  hayretFlete: $hayretFlete")

            if (isRetpagado <= 0.00){



            }else if (isRetpagado > 0.00){
                //valido si ya la retencion del iva esta pagada

                if (isRetivaPagado <= 0.00){
                    //

                }else if (isRetivaPagado > 0.00){
                    listaTiposRet.add("iva")
                    cb_notretiva.visibility = View.VISIBLE
                    println("se va a pagar ret iva")

                }
                //valido si hay retencion del flete
                if(hayretFlete <= 0.00){
                    listaTiposRet.add("flete")
                    cb_noretflete.visibility = View.VISIBLE
                    println("se va a pagar ret flete")

                }else if (hayretFlete > 0.00){
                    //
                }

                if(hayretParme <= 0.00){
                    listaTiposRet.add("parme")
                    println("se va a pagar ret parme")

                }else if(hayretParme > 0.00){
                    //
                }
            }

            if(listaTiposRet.size > 0){
                println("al menos una retencion va a ser pagada")
                bt_retenciones.visibility = View.VISIBLE
                bt_retenciones.isEnabled = true

            }else{
                // bt_retenciones.visibility = View.INVISIBLE
                //bt_retenciones.isEnabled = false
            }
        }else{
            // TBD
        }


    }

    private fun irAretenciones() {
        var listaDocsconRet:ArrayList<String> = ArrayList()


        if(listaTiposRet.size > 0){
            for (i in listaDocumentos.indices){
                if(listaDocumentos[i].cdretencion > 0.00 && listaDocumentos[i].dretencion == 0.00){
                    listaDocsconRet.add(listaDocumentos[i].documento.toString())
                }else{
                    if(listaDocumentos[i].cdretencion == 0.00 && listaDocumentos[i].dretencion == 0.00 && dmontoRetFlete > 0.00){
                        listaDocsconRet.add(listaDocumentos[i].documento.toString())
                    }
                }
            }

            val intent = Intent(applicationContext, retencionesActivity::class.java)
            var bundle:Bundle = Bundle()

            bundle.putSerializable("listaRetenciones", listaRetGuardada)
            bundle.putSerializable("listaDocs", listaDocsconRet)

            intent.putExtras(bundle)
            intent.putExtra("listatiposret", listaTiposRet)
            intent.putExtra("listaDocs", listaDocsconRet)
            startActivityForResult(intent,request_code)

            /*intent.putExtra("cod_usuario", cod_usuario)
            intent.putExtra("codigoEmpresa", codEmpresa)


            startActivity(intent)*/
        }else {

        }

    }


    private fun showDatePickerDialog(){
        val datePicker = DatePickerFragment ("CreacionPrecobranzaActivity") { day, month, year -> onDateSelected(day, month, year) }
        datePicker.show(supportFragmentManager, "datePicker")

    }

    fun onDateSelected(day: Int, month:Int, year:Int){
        var fechaMostrar = "$year-$month-$day"

        et_fechatransf.setText("Fecha: $fechaMostrar")
        //en formato para query de tasas
        fechaQuery = ""
        var formatter:SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        var date:Date =  formatter.parse(fechaMostrar)
        var formatNuevo:SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        fechaQuery = formatNuevo.format(date)

        println("fechadelquery$fechaQuery")

        buscarTasas()
        tasaCambioSeleccionadaPrincipal = ValidarTasaCambioCompleto()
        tasaCambioComplemento           = ValidarTasaCambioComplemento()
        condicionMonedaRecibo()

        println("$tasaInterB")

    }

    private fun condicionMonedaRecibo(){
        if(rb_divisascompleto.isChecked){
            colocarMontosEnMonedaSelec("USD")
            buscarBancos("USD")
        }else{
            colocarMontosEnMonedaSelec("BSS")
            buscarBancos("BSS")
        }

    }

    private fun ValidarTasaCambioCompleto(): Double{
        //
        var tasaAusar = 0.00

        if(!cb_pagintercomp.isChecked){
            tasaAusar = tasaNormal
            println(tasaAusar)

        }else if(cb_pagintercomp.isChecked){
            tasaAusar = tasaInterB
            println(tasaAusar)
        }
        return tasaAusar
    }


    private fun ValidarTasaCambioComplemento():Double{
        //
        var tasaAusar = 0.00

        if(!cb_pagintercm.isChecked){
            tasaAusar = tasaNormal
            println(tasaAusar)

        }else if(cb_pagintercm.isChecked){
            tasaAusar = tasaInterB
        }
        return tasaAusar
    }




    private fun colocarMontosEnMonedaSelec(moneda: String) {

        montoNuevoIva       = 0.00
        montoNuevoFlete     = 0.00
        var retencionesTotales  = 0.00
        bsNeto                  = 0.00
        dnetoTotal              = 0.00
         if(moneda.equals("USD")){


             //recorro los documentos para ir sacando los montos resultantes
             for(i in listaDocumentos.indices){

                 var ivaaCobrarporDoc   = 0.00
                 var fleteaCobrarporDoc = 0.00

                 //calculo  flete segun el documento
                 if(listaDocumentos[i].agencia.equals("001")){
                     listaDocumentos[i].cdretflete  = listaDocumentos[i].dFlete * 0.03
                     listaDocumentos[i].cbsretflete = listaDocumentos[i].bsflete * 0.03
                 }else{
                     listaDocumentos[i].cdretflete = 0.00
                 }

                 //calculo el iva total a pagar
                if(listaDocumentos[i].bsiva - listaDocumentos[i].bsmtoiva < 1) {
                    montoNuevoIva    += 0.00
                    ivaaCobrarporDoc = 0.00


                }else{
                    montoNuevoIva += listaDocumentos[i].dtotimpuest
                    ivaaCobrarporDoc = listaDocumentos[i].dtotimpuest
                }

                 //calculo el flete a pagar
                 if(listaDocumentos[i].bsflete - listaDocumentos[i].bsmtofte < 1){
                     montoNuevoFlete += 0.00
                     fleteaCobrarporDoc = 0.00
                 }else{
                     montoNuevoFlete += listaDocumentos[i].dFlete
                     fleteaCobrarporDoc = listaDocumentos[i].dFlete
                 }

                 //calculo del neto a pagar
                 dnetoTotal += listaDocumentos[i].saldo - ivaaCobrarporDoc - fleteaCobrarporDoc

                //Calculo las retenciones
                retencionesTotales += listaDocumentos[i].cdretflete + listaDocumentos[i].cdretparme +listaDocumentos[i]. cdretencioniva

             }

            //si es mas de un doc, no permito abonos
            if(listaDocumentos.size > 1){
                rb_abono.visibility = View.INVISIBLE
            }

             dnetoTotal         = Math.round(dnetoTotal*100.00)/100.00
             montoNuevoIva      = Math.round(montoNuevoIva*100.00)/100.00
             montoNuevoFlete    = Math.round(montoNuevoFlete*100.00)/100.00
             retencionesTotales = Math.round(retencionesTotales*100.00)/100.00

            txt_neto.text     = dnetoTotal.toString()
            txt_bsiva.text    = montoNuevoIva.toString()
            txt_flete.text    = montoNuevoFlete.toString()
            txt_reten.text    = retencionesTotales.toString()

            if(cb_descuentos.isChecked){

                calcularDescuentos("USD")

            }else{
                descuentoTotal = 0.00

            }

            if(cb_notretiva.isChecked){

                montoNuevoIva = dtotimpuest + cdretencioniva
                montoNuevoIva = Math.round(montoNuevoIva*100.00)/100.00
                retencionesTotales = retencionesTotales - cdretencioniva
                retencionesTotales = Math.round(retencionesTotales*100.00)/100.00
                txt_bsiva.text = montoNuevoIva.toString()
                txt_reten.text = retencionesTotales.toString()
            }


             if(cb_noretflete.isChecked){
                 dmontoRetFlete  = listaDocumentos.sumOf { it.cdretflete }
                 montoNuevoFlete = dFlete + dmontoRetFlete
                 montoNuevoFlete = Math.round(montoNuevoFlete * 100.00)/100.00
                 retencionesTotales = retencionesTotales - dmontoRetFlete
                 retencionesTotales = Math.round(retencionesTotales * 100.00)/100.00
                 txt_flete.text = montoNuevoFlete.toString()
                 txt_reten.text = retencionesTotales.toString()
             }

             if(cb_complemento.isChecked){
                 rb_divisascomplemento.isChecked
                 rb_transfcom.isChecked

             }

             if(cb_iva.isChecked){

             }


            var totalMostrado = Math.round((dnetoTotal + montoNuevoIva + montoNuevoFlete  - retencionesTotales - descuentoTotal)*100.00)/100.00
            txt_total.text    = totalMostrado.toString()

            //sumarizarMontos(dnetoTotal, dtotimpuest, dFlete,dretencioniva, dmontoRetFlete, retmundif, descuentoTotal)



        }else if (moneda.equals("BSS")){

             for(i in listaDocumentos.indices){

                 var ivaaCobrarporDoc   = 0.00
                 var fleteaCobrarporDoc = 0.00

                 if(listaDocumentos[i].agencia.equals("001")){
                     listaDocumentos[i].cbsretflete = listaDocumentos[i].bsflete * 0.03
                 }else{
                     listaDocumentos[i].cbsretflete = 0.00
                 }

                 //calculo el iva total a pagar
                 if(listaDocumentos[i].bsiva - listaDocumentos[i].bsmtoiva < 1) {
                     montoNuevoIva    += 0.00
                     ivaaCobrarporDoc = 0.00

                 }else{
                     montoNuevoIva += listaDocumentos[i].bsiva
                     ivaaCobrarporDoc = listaDocumentos[i].dtotimpuest
                 }

                 //calculo el flete a pagar
                 if(listaDocumentos[i].bsflete - listaDocumentos[i].bsmtofte < 1){
                     montoNuevoFlete += 0.00
                     fleteaCobrarporDoc = 0.00
                 }else{
                     montoNuevoFlete += listaDocumentos[i].bsflete
                     fleteaCobrarporDoc = listaDocumentos[i].dFlete
                 }

                 //calculo del neto a pagar
                 dnetoTotal += listaDocumentos[i].saldo - ivaaCobrarporDoc - fleteaCobrarporDoc


                 //Calculo las retenciones
                 retencionesTotales += listaDocumentos[i].cbsretflete + listaDocumentos[i].cbsretparme +listaDocumentos[i].cbsretencioniva

             }

             dnetoTotal         = Math.round(dnetoTotal*100.00)/100.00
             montoNuevoIva      = Math.round(montoNuevoIva*100.00)/100.00
             montoNuevoFlete    = Math.round(montoNuevoFlete*100.00)/100.00
             retencionesTotales = Math.round(retencionesTotales*100.00)/100.00

            if(fechaQuery == ""){
                Toast.makeText(this,"Debe Seleccionar la fecha de Pago", Toast.LENGTH_SHORT).show()

            }else{
                tasaCambioSeleccionadaPrincipal = ValidarTasaCambioCompleto()
                bsNeto            = dnetoTotal * tasaCambioSeleccionadaPrincipal
                bsNeto            = Math.round(bsNeto * 100.00)/100.00
                txt_neto.text     = bsNeto.toString()
                txt_bsiva.text    = montoNuevoIva.toString()
                txt_flete.text    = montoNuevoFlete.toString()
                txt_reten.text    = retencionesTotales.toString()
                if(cb_descuentos.isChecked){
                    calcularDescuentos("BSS")

                }else{
                    descuentoTotal = 0.00
                }

                if(cb_notretiva.isChecked){

                    montoNuevoIva = bsIvaTotal + cbsretencioniva
                    montoNuevoIva = Math.round(montoNuevoIva*100.00)/100.00
                    retencionesTotales = retencionesTotales - cbsretencioniva
                    retencionesTotales = Math.round(retencionesTotales*100.00)/100.00
                    txt_bsiva.text = montoNuevoIva.toString()
                    txt_reten.text = retencionesTotales.toString()
                }

                if(cb_noretflete.isChecked){
                    bsmontoRetFlete = listaDocumentos.sumOf { it.cbsretflete }
                    montoNuevoFlete = bsFleteTotal + bsmontoRetFlete
                    montoNuevoFlete = Math.round(montoNuevoFlete * 100.00)/100.00

                    retencionesTotales = retencionesTotales - bsmontoRetFlete
                    retencionesTotales = Math.round(retencionesTotales*100.00)/100.00
                    txt_flete.text = montoNuevoFlete.toString()
                    txt_reten.text = retencionesTotales.toString()
                }

                if(cb_complemento.isChecked){

                }

                if(cb_iva.isChecked){


                }

                bsmontoTotal      = Math.round((bsNeto + montoNuevoIva + montoNuevoFlete -  retencionesTotales  - descuentoTotal)*100.00)/100.00
                bsmontoTotal      = Math.round(bsmontoTotal * 100.0)/100.0
                txt_total.text    = bsmontoTotal.toString()


                //sumarizarMontos(bsNeto,bsIvaTotal, bsFleteTotal,bsretencionIvaTotal,  bsmontoRetFlete, retmundif, descuentoTotal)

            }
        }
    }


    private fun cargarDatosdeDocs(listadocs:ArrayList<String>){
        var documentos:Documentos

        println("lista en el metodo: " + listadocs)

        var query     = arrayOf("documento," + "contribesp,"+ "ruta_parme," +"vence,"+ "tipodocv,"+
                        "diascred," +"dtotneto,"+ "dtotpagos,"+ "dtotdev,"+ "dtotalfinal,"+ "bsiva,"+ "bsflete,"+ "bsretencioniva,"+ "bsretencion, tasadoc, dtotimpuest, dFlete, dretencion, dretencioniva, tipodoc, " +
                "mtodcto, fchvencedcto, tienedcto, cbsret, cdret, cbsretiva, cdretiva, cbsrparme, cdrparme, agencia, bsmtoiva, bsmtofte, retmun_mto, emision, codcliente")
        var tabla     = "ke_doccti"
        var condicion = "documento IN ("+listadocs.toString().replace("[", "").replace("]", "") +")"

        var cursorDocs:Cursor = ke_android.query(tabla, query, condicion, null,null,null, null)

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
            documentos.tipodoc         = cursorDocs.getString(19)
            documentos.mtodcto          = cursorDocs.getDouble(20)
            documentos.fchvencedcto     = cursorDocs.getString(21)
            documentos.tienedcto        = cursorDocs.getString(22)
            documentos.cbsretencion     = cursorDocs.getDouble(23)
            documentos.cdretencion      = cursorDocs.getDouble(24)
            documentos.cbsretencioniva  = cursorDocs.getDouble(25)
            documentos.cdretencioniva   = cursorDocs.getDouble(26)
            documentos.cbsretparme      = cursorDocs.getDouble(27)
            documentos.cdretparme       = cursorDocs.getDouble(28)
            documentos.agencia          = cursorDocs.getString(29)
            documentos.bsmtoiva         = cursorDocs.getDouble(30)
            documentos.bsmtofte         = cursorDocs.getDouble(31)
            documentos.retmun_mto       = cursorDocs.getDouble(32)
            documentos.emision          = cursorDocs.getString(33)
            documentos.codcliente       = cursorDocs.getString(34)
            listaDocumentos.add(documentos)

        }

        for (i in listaDocumentos.indices){
                dnetoTotal           += listaDocumentos[i].dtotneto
                bsIvaTotal           += listaDocumentos[i].bsiva
                bsFleteTotal         += listaDocumentos[i].bsflete
                bsRetencionTotal     += listaDocumentos[i].cbsretencion
                bsretencionIvaTotal  += listaDocumentos[i].cbsretencioniva
                dtotimpuest          += listaDocumentos[i].dtotimpuest
                dFlete               += listaDocumentos[i].dFlete
                dretencion           += listaDocumentos[i].dretencion
                dretencioniva        += listaDocumentos[i].dretencioniva
                retenpagado          += listaDocumentos[i].dretencion
                cdretencion          += listaDocumentos[i].cdretencion
                cdretencioniva       += listaDocumentos[i].cdretencioniva
                cdretparme           += listaDocumentos[i].cdretparme
                cbsretparme          += listaDocumentos[i].cbsretparme
                cbsretencioniva      += listaDocumentos[i].cbsretencioniva
                //tasadoc              += listaDocumentos[i].tasadoc
                codigoCliente         = listaDocumentos[i].codcliente


                //dmontoTotal          += listaDocumentos[i].dtotalfinal
                tipoDocsaPagar.add(listaDocumentos[i].tipodocv)

                //calculo del saldo pendiente
                saldo = listaDocumentos[i].dtotalfinal - (listaDocumentos[i].dtotpagos + listaDocumentos[i].dretencion)
                listaDocumentos[i].saldo = saldo

        }



            dnetoTotal      = Math.round(dnetoTotal * 100.00)/100.00
            bsIvaTotal      = Math.round(bsIvaTotal * 100.00)/100.00
            dtotimpuest     = Math.round(dtotimpuest * 100.00)/100.00
            dFlete          = Math.round(dFlete * 100.00)/100.00
            dretencion      = Math.round(dretencion * 100.00)/100.00
            dretencioniva   = Math.round(dretencioniva * 100.00)/100.00



            txt_neto.text   = dnetoTotal.toString()


            if (tipoDocsaPagar.contains("N/E") ){
                rb_bsscompleto.isChecked = false
                rb_bsscompleto.visibility = View.INVISIBLE

            }

            // si los montos estan en cero, no se toman en cuenta para el proceso de cxc
            if(bsIvaTotal == 0.00 && dtotimpuest == 0.00){

                txt_bsiva.visibility = View.INVISIBLE

            }else{
                txt_bsiva.text       = bsIvaTotal.toString()
            }

            if( bsretencionIvaTotal > 0.00 && dretencioniva == 0.00){
                cb_notretiva.visibility    = View.VISIBLE
            }else{
                cb_notretiva.visibility    = View.INVISIBLE

            }



            if(dretencion >= cdretencion){
                txt_reten.visibility = View.INVISIBLE
            }


            if (tipoDocsaPagar.contains("FAC") ){
                cb_noretflete.visibility = View.VISIBLE

                var difretenciones = dretencion - retenpagado
                if(difretenciones > 0 ){
                    cb_notretiva.visibility = View.VISIBLE
                } else if (difretenciones <= 0){
                    cb_notretiva.visibility = View.INVISIBLE

                }

                if( bsFleteTotal == 0.00 && dFlete == 0.00){
                    txt_flete.visibility = View.INVISIBLE
                    cb_noretflete.visibility = View.INVISIBLE
                }else{
                    txt_flete.text  =  bsFleteTotal.toString()
                    cb_noretflete.visibility = View.VISIBLE
                }
            }


            if(tipoDocsaPagar.contains("N/E") && !tipoDocsaPagar.contains("FAC")){
                cb_iva.visibility = View.INVISIBLE
            }

        val dateTimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        println(" retencion de iva a pagar: $cdretencioniva")
        listaDocumentos.sortedByDescending { LocalDate.parse(it.emision, dateTimeFormat) }

        for (j in listaDocumentos.indices){
            println(" DOC: ${listaDocumentos[j].documento} , emision: ${listaDocumentos[j].emision}")
        }


    }

    private fun getFechaHoy(): String{
        var fechaHoy:String
        var fechaSinConvertir: Calendar = Calendar.getInstance()
        var sdf: SimpleDateFormat       = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        fechaHoy                        = sdf.format(fechaSinConvertir.time)
        return fechaHoy
    }


    // funcion para generar el nro de la precobranza (nro del recibo)
    private fun generarNroPrecobranza(): String {
        var fechaHoy: Date
        fechaHoy = Date(Calendar.getInstance().timeInMillis)

        var formatoFecha:SimpleDateFormat
        formatoFecha = SimpleDateFormat("yyMM")

        var fecha = formatoFecha.format(fechaHoy)

        var correlativo = right(CorrelativoTexto, 4)
        correlativo = cod_usuario + "-" + tipoDoc + "-" + fecha + correlativo
        return correlativo
    }

    //funcion para retornar el valor del correlativo acortado a 4 caracteres.
    private fun right(valor:String, longitud:Int):String {
        return valor.substring(valor.length - longitud)
    }




    private fun getTasas(){
        //consulto al webservice para guardar las tasas
        descargarTasas("https://"+ enlaceEmpresa + "/webservice/tasas.php?fecha_sinc=" + fecha_auxiliar.trim() + "&&agencia=" + codigoSucursal.trim())
        //buscarTasas()
    }



    private fun buscarTasas(){


        ke_android = conn.writableDatabase
        var tasas:tasas

        var cursorTasas:Cursor = ke_android.rawQuery("SELECT kecxc_id, kecxc_fecha, kecxc_tasa, kecxc_fchyhora, kecxc_tasaib FROM kecxc_tasas WHERE kecxc_fchyhora LIKE '%" + fechaQuery+"%'", null)
        println("SE EJECUTO EL QUERY DE TASAS ")
        //vacio el cursor en las variables para mostrar
        while(cursorTasas.moveToNext()){
            tasas = tasas()
            tasas.id              = cursorTasas.getString(0)
            tasas.fecha           = cursorTasas.getString(1)
            tasas.tasa            = cursorTasas.getDouble(2)
            var fechaSinConvertir = cursorTasas.getString(3)
            tasas.tasaib          = cursorTasas.getDouble(4)

            var fechaC = Calendar.getInstance()

            tasaId     = tasas.id
            tasaNormal = tasas.tasa
            tasaInterB = tasas.tasaib
            println(" tasa normal :$tasaNormal, tasa interb :$tasaInterB")

        }

    }

    private fun descargarTasas(URL: String) {
            conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null, 14)
            ke_android = conn.readableDatabase

            val jsonArrayRequest: JsonArrayRequest
            println("hasta aca todo bien")
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
                                jsonObject          = response.getJSONObject(i);
                                idTasa              = jsonObject.getString("id")
                                fechaTasa           = jsonObject.getString("fecha")
                                tasaCambio          = jsonObject.getDouble("tasa")
                                usuarioTasa         = jsonObject.getString("usuario")
                                ipTasa              = jsonObject.getString("ip")
                                fechayHoraTasa      = jsonObject.getString("fechayhora")
                                fechamodifitasa     = jsonObject.getString("fechamodifi")
                                tasaInterbancaria   = jsonObject.getDouble("tasaib")

                                var qTasas: ContentValues = ContentValues()
                                qTasas.put("kecxc_id", idTasa)
                                qTasas.put("kecxc_fecha", fechaTasa)
                                qTasas.put("kecxc_tasa", tasaCambio)
                                qTasas.put("kecxc_usuario", usuarioTasa)
                                qTasas.put("kecxc_ip", ipTasa)
                                qTasas.put("kecxc_fchyhora", fechayHoraTasa)
                                qTasas.put("fechamodifi", fechamodifitasa)
                                qTasas.put("kecxc_tasaib", tasaInterbancaria)

                                var qcodigoLocal: Cursor
                                qcodigoLocal = ke_android.rawQuery("SELECT count(kecxc_id) FROM kecxc_tasas WHERE kecxc_id ='" + idTasa + "'", null)
                                qcodigoLocal.moveToFirst()
                                //variable para obtener el conteo de documentos que ya esten en el telf
                                var codigoExistente = qcodigoLocal.getInt(0)

                                if (codigoExistente > 0) {
                                    ke_android.update("kecxc_tasas", qTasas, "kecxc_id= ?", arrayOf(idTasa)
                                    )
                                } else if (codigoExistente == 0) {
                                    ke_android.insert("kecxc_tasas", null, qTasas)
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
                            buscarTasas()


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
    }

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

    private fun getBancos(monedaBanco:String){
        descargarBancos("https://"+ enlaceEmpresa + "/webservice/bancos.php?fecha_sinc=" + fecha_auxiliar.trim() + "&&agencia=" + codigoSucursal.trim(), monedaBanco)

    }

    private fun actualizarBancos(){
        listaInfoBancos = ArrayList()
        listaInfoBancos.add("Seleccione un banco...")
        for (i in listaBancos.indices) {
            listaInfoBancos.add(listaBancos[i].nombanco)
        }


    }

    private fun descargarBancos(URL:String, monedaBanco: String){
        conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null, 14)
        ke_android = conn.readableDatabase

        val jsonArrayRequest: JsonArrayRequest
        println("hasta aca todo bien")
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
                        buscarBancos(monedaBanco)



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

        buscarBancos(monedaBanco)

    }

    private fun buscarBancos(monedaBanco: String){
        listaInfoBancos.clear()
        listaBancos.clear()

        ke_android = conn.writableDatabase
        var bancos:Bancos
        var moneda:Double = 0.00

        if(monedaBanco == "USD"){
            moneda = 2.00
        }else if(monedaBanco == "BSS"){
            moneda = 1.00
        }

        var cursorBancos:Cursor = ke_android.rawQuery("SELECT codbanco, nombanco,cuentanac, inactiva, fechamodifi FROM listbanc WHERE inactiva = 0 AND cuentanac = $moneda", null)
        while(cursorBancos.moveToNext()){
            bancos = Bancos()
            bancos.codbanco     = cursorBancos.getString(0)
            bancos.nombanco     = cursorBancos.getString(1)
            bancos.cuentanac    = cursorBancos.getDouble(2)
            bancos.inactiva     = cursorBancos.getDouble(3)
            bancos.fechamodifi  = cursorBancos.getString(4)
            listaBancos.add(bancos)

        }

        actualizarBancos()
        var adapterBancos: ArrayAdapter<CharSequence>
        adapterBancos = ArrayAdapter(this, R.layout.spinner_bancos, listaInfoBancos as List<CharSequence>)
        spBancosCompleto.adapter = adapterBancos
        adapterBancos.notifyDataSetChanged()

    }

    private fun buscarBancosParaImp(monedaBanco: String){
        listaInfoBancosImp.clear()
        listaBancosImp.clear()

        ke_android = conn.writableDatabase
        var bancos:Bancos
        var moneda:Double = 0.00

        if(monedaBanco == "USD"){
            moneda = 2.00
        }else if(monedaBanco == "BSS"){
            moneda = 1.00
        }

        var cursorBancos:Cursor = ke_android.rawQuery("SELECT codbanco, nombanco,cuentanac, inactiva, fechamodifi FROM listbanc WHERE inactiva = 0 AND cuentanac = $moneda", null)

        while(cursorBancos.moveToNext()){
            bancos = Bancos()
            bancos.codbanco     = cursorBancos.getString(0)
            bancos.nombanco     = cursorBancos.getString(1)
            bancos.cuentanac    = cursorBancos.getDouble(2)
            bancos.inactiva     = cursorBancos.getDouble(3)
            bancos.fechamodifi  = cursorBancos.getString(4)
            listaBancosImp.add(bancos)
            println(listaBancosImp.toString())

        }

        actualizarBancosImp()
        var adapterBancosIm: ArrayAdapter<CharSequence>
        adapterBancosIm = ArrayAdapter(this, R.layout.spinner_bancos, listaInfoBancosImp as List<CharSequence>)
        spBancosImpuestos.adapter = adapterBancosIm
        adapterBancosIm.notifyDataSetChanged()

    }


    private fun actualizarBancosImp(){
        listaInfoBancosImp = ArrayList()
        listaInfoBancosImp.add("Seleccione un banco...")
        for (i in listaBancosImp.indices){
            listaInfoBancosImp.add(listaBancosImp[i].nombanco)
            println(listaBancosImp[i].nombanco)
        }

    }


    private fun buscarBancosCm(monedaBanco: String){
        listaInfoBancosCm.clear()
        listaBancosCm.clear()

        ke_android = conn.writableDatabase
        var bancos:Bancos
        var moneda:Double = 0.00

        if(monedaBanco == "USD"){
            moneda = 2.00
        }else if(monedaBanco == "BSS"){
            moneda = 1.00
        }

        var cursorBancos:Cursor = ke_android.rawQuery("SELECT codbanco, nombanco,cuentanac, inactiva, fechamodifi FROM listbanc WHERE inactiva = 0 AND cuentanac = $moneda", null)
        while(cursorBancos.moveToNext()){
            bancos = Bancos()
            bancos.codbanco     = cursorBancos.getString(0)
            bancos.nombanco     = cursorBancos.getString(1)
            bancos.cuentanac    = cursorBancos.getDouble(2)
            bancos.inactiva     = cursorBancos.getDouble(3)
            bancos.fechamodifi  = cursorBancos.getString(4)
            listaBancosCm.add(bancos)

        }

        actualizarBancosCm()
        var adapterBancosCm: ArrayAdapter<CharSequence>
        adapterBancosCm = ArrayAdapter(this, R.layout.spinner_bancos, listaInfoBancosCm as List<CharSequence>)
        spBancosComplemento.adapter = adapterBancosCm
        adapterBancosCm.notifyDataSetChanged()

    }

    private fun actualizarBancosCm(){
        listaInfoBancosCm = ArrayList()
        listaInfoBancosCm.add("Seleccione un banco...")
        for (i in listaBancosCm.indices){
            listaInfoBancosCm.add(listaBancosCm[i].nombanco)
            println(listaBancosCm[i].nombanco)
        }

    }


    private fun calcularDescuentos(moneda:String){
        var porcentajeAsignado = 0.00
        var cantidadDeDescuento = 0.00
        listaDescuentos = ArrayList()
        var descuentos:Descuentos

        /*variable que devolvera el descuento total que
        se pueden aplicar a todos los docs*/
        descuentoTotal     = 0.00
        var fechaVence     = ""
        var nrodocumento   = ""
        var montonetoDol   = 0.00
        var montonetoBs    = 0.00
        /*Por cada documento, voy a buscar la fecha de vencimiento
        y determinar cada descuento*/

        for (i in listaDocumentos.indices){
            descuentos = Descuentos()
            nrodocumento    = listaDocumentos[i].documento
            fechaVence      = listaDocumentos[i].vence
            montonetoDol    = listaDocumentos[i].dtotneto
            montonetoBs     = montonetoDol * tasaCambioSeleccionadaPrincipal

            val fechaConvertidaVence = LocalDate.parse(fechaVence)
            val fechaHoy             = LocalDate.now()
            val diasDiferencia       = ChronoUnit.DAYS.between(fechaHoy, fechaConvertidaVence)
            println("DIAS DE DIFERENCIA: $diasDiferencia")

            //si la diferencia de dias es positiva, puede aplicar al descuento

            if(diasDiferencia > 0){


                if(moneda.equals("USD")){
                        porcentajeAsignado = 0.10
                        cantidadDeDescuento = 10.0

                    if(codbanco.equals("99")){
                            porcentajeAsignado  = 0.127
                            cantidadDeDescuento = 12.7
                        }


                    //println("fecha esta aun dentro de los 15 dias")
                        var cantDescuento    = montonetoDol * porcentajeAsignado
                        //var descuentoUni     = montonetoDol - cantDescuento
                        descuentoTotal      += cantDescuento
                        descuentoTotal       = Math.round(descuentoTotal*100.00)/100.00

                        descuentos.nrodoc    = nrodocumento
                        descuentos.cantdscto = cantDescuento
                        listaDescuentos.add(descuentos)
                        println(listaDescuentos)

                } else if (moneda.equals("BSS")){
                       //println("fecha esta aun dentro de los 15 dias")
                       //var cantDescuento    = montonetoBs * 0.10
                        //var descuentoUni   = montonetoDol - cantDescuento
                       /* descuentoTotal      += cantDescuento
                        descuentoTotal       = Math.round(descuentoTotal*100.00)/100.00

                        descuentos.nrodoc    = nrodocumento
                        descuentos.cantdscto = cantDescuento
                        listaDescuentos.add(descuentos)
                        println(listaDescuentos)*/
                }

            }else if(diasDiferencia < 0){
                //println("este documento ya no opta por descuento")
                var cantDescuento    = 0.00
                descuentoTotal      += 0.00

                if(codbanco.equals("99")){
                    porcentajeAsignado   = 0.03
                    cantidadDeDescuento  = 3.0
                    var cantDescuento    = montonetoDol * porcentajeAsignado
                    //var descuentoUni     = montonetoDol - cantDescuento
                    descuentoTotal      += cantDescuento
                    descuentoTotal       = Math.round(descuentoTotal*100.00)/100.00

                    descuentos.nrodoc    = nrodocumento
                    descuentos.cantdscto = cantidadDeDescuento

                    listaDescuentos.add(descuentos)
                    println(listaDescuentos)

                }
            }
        }

        //txt_descuentos.text = "- $descuentoTotal"

        if(descuentoTotal > 0.00){
            cb_descuentos.visibility = View.VISIBLE
            cb_descuentos.isEnabled = true

        }else if(descuentoTotal <= 0.00){
            cb_descuentos.visibility = View.INVISIBLE
            cb_descuentos.isEnabled = false
        }

    }

    private fun validarBancos(): Boolean{
        var response = false

        if(codigoBancoCompleto.isEmpty() || codigoBancoCompleto.equals("")){
            Toast.makeText(this, "Debe seleccionar un banco ", Toast.LENGTH_LONG).show()
            return false

        }else{
            if(txt_referencia.text.toString().equals("") || txt_referencia.text.toString().equals(null)){
                Toast.makeText(this, "Debe indicar una referencia valida", Toast.LENGTH_LONG).show()
                return false
            }else{
                referenciaPrincipal = txt_referencia.text.toString()
                response = true
            }


        }

        if(cb_iva.isChecked){
            if(codigoBancoImpuesto.isEmpty() || codigoBancoImpuesto.equals("")){
                Toast.makeText(this, "Debe seleccionar un banco ", Toast.LENGTH_LONG).show()
                return false

            }else{
                if(txt_referenciaimbs.text.toString().equals("") || txt_referenciaimbs.text.toString().equals(null)){
                    Toast.makeText(this, "Debe indicar una referencia valida", Toast.LENGTH_LONG).show()
                    return false
                }else{
                    referenciaImp = txt_referenciaimbs.text.toString()
                    response = true
                }

            }
        }

        if(cb_complemento.isChecked){
            if(codigoBancoComplemento.isEmpty() || codigoBancoComplemento.equals("")){
                Toast.makeText(this, "Debe seleccionar un banco ", Toast.LENGTH_LONG).show()
                return false

            }else{
                if(txt_referenciacom.text.toString().equals("") || txt_referenciacom.text.toString().equals(null)){
                    Toast.makeText(this, "Debe indicar una referencia valida", Toast.LENGTH_LONG).show()
                    return false
                }else{
                    referenciaCm = txt_referenciacom.text.toString()
                    response = true
                }

            }
        }

        return response
    }





    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if((requestCode == request_code)  && (resultCode == RESULT_OK)){
        var bundle:Bundle = Bundle()
            if (data != null) {
                bundle = data.extras!!
                if (bundle.containsKey("listaRetenciones")){
                    listaRetGuardada = bundle.getSerializable("listaRetenciones") as ArrayList<Retenciones>
                    for (i in listaRetGuardada){
                        println("tipo ${i.tiporet}, nroret ${i.nroret}, refret ${i.refret}  fecha ${i.fecharet}  monto ${i.montoret}")

                    }
                    println("LLEGUE AL ACTIVITY ON RESULT")
                }else{
                    println("NO ESTA LLEGANDO LA LISTA")
                }
            }


        //var listaRetCadena = data?.getStringArrayListExtra("listaRetenciones")
        }

        super.onActivityResult(requestCode, resultCode, data)
    }




}

