package com.appcloos.mimaletin

import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.appcloos.mimaletin.databinding.ActivityDetalleCxcactivityBinding
import com.appcloos.mimaletin.model.CXC.ke_precobradocs
import com.appcloos.mimaletin.model.CXC.ke_precobranza
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class DetalleCXCActivity : AppCompatActivity() {

    //var codigoRecibo = intent.getStringExtra("codigoRecibo") as String

    private lateinit var binding: ActivityDetalleCxcactivityBinding
    private lateinit var keAndroid: SQLiteDatabase
    private lateinit var conn: AdminSQLiteOpenHelper

    //val bundle = intent.extras
    //val dato = bundle?.getString("codigoRecibo")

    lateinit var preferences: SharedPreferences
    lateinit var cxcndoc: String

    private var kePrecobranza = ke_precobranza()

    //var kePrecobradoc = ke_precobradocs()
    private lateinit var kePrecobradocsMain: MutableList<ke_precobradocs>

    private var documentos = Documentos()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleCxcactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferences = getSharedPreferences("Preferences", MODE_PRIVATE)
        cxcndoc = preferences.getString("recibo", null).toString()

        conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null)
        keAndroid = conn.writableDatabase

        //val cursor =
        //    keAndroid.rawQuery("SELECT * FROM ke_precobranza WHERE cxcndoc = '$cxcndoc';", null)

        kePrecobranza = getPrecobranza(cxcndoc)
        documentos = getCliente(cxcndoc)
        getDocs(cxcndoc)

        if (kePrecobranza.cxcndoc.isNotEmpty()) {

            val fechaEmision: String = if (kePrecobranza.tiporecibo == "D") {
                val ldt: LocalDateTime = LocalDateTime.parse(
                    kePrecobranza.fchrecibo, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                )
                val writingFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss a")
                ldt.format(writingFormatter)
            } else {
                val readingFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val date = LocalDate.parse(kePrecobranza.fchrecibo, readingFormatter)
                val writingFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                date.format(writingFormatter)
            }

            binding.tvCod.text = cxcndoc

            binding.tvTipoRecibo.text = when (kePrecobranza.tiporecibo) {
                "W" -> "Recibo de Cobro"
                "D" -> "Axeno de Deposito"
                else -> "No Identificado"
            }

            binding.tvEmision.text = "Emisión: $fechaEmision"

            binding.tvTipoMoneda.text =
                "Moneda del Recibo: ${if (kePrecobranza.moneda == "2") "Dolar" else "Bolívar"}"

            binding.tvBanco.text = kePrecobranza.bcocod

            val nombreBanco =
                conn.getCampoString("listbanc", "nombanco", "codbanco", kePrecobranza.bcocod)

            if (nombreBanco.isNotEmpty()) {
                binding.tvBanco.text = "${kePrecobranza.bcocod} ${nombreBanco}"
                binding.tvReferencia.text = "REF: ${kePrecobranza.bcoref}"
                binding.tvMonto.text =
                    "Monto: ${kePrecobranza.bcomonto} ${if (kePrecobranza.moneda == "2") "$" else "Bs."}"
            } else {
                binding.tvBanco.text = "Efectivo"
                binding.tvMonto.text = "Monto: ${kePrecobranza.efectivo} $"
            }

            binding.codCliente.text = documentos.codcliente.ifEmpty { "No identificado" }
            binding.nomCliente.text = documentos.nombrecli.ifEmpty { "No identificado" }

            binding.tvFavor.text = "A favor: " + kePrecobradocsMain.sumOf { it.afavor }.toString()

            binding.rvDocumentos.setHasFixedSize(true)
            binding.rvDocumentos.layoutManager = LinearLayoutManager(this)
            binding.rvDocumentos.adapter = DetalleCXCAdapter(kePrecobradocsMain, this)

            if (kePrecobranza.tiporecibo == "W") {
                binding.tvClave.text = getClave()
            }

        } else {
            Toast.makeText(this, "Recibo no Existe", Toast.LENGTH_SHORT).show()
        }

        //println("Intent -> $dato")
        println("Intent -> $cxcndoc")


    }

    private fun getPrecobranza(cxcndoc: String): ke_precobranza {
        val kePrecobranza = ke_precobranza()
        try {
            keAndroid.rawQuery("SELECT * FROM ke_precobranza WHERE cxcndoc = '$cxcndoc';", null)
                .use { cursor ->
                    if (cursor.moveToFirst()) {
                        kePrecobranza.cxcndoc = cursor.getString(0)
                        kePrecobranza.tiporecibo = cursor.getString(1)
                        kePrecobranza.fchrecibo = cursor.getString(6)
                        kePrecobranza.moneda = cursor.getString(18)
                        kePrecobranza.efectivo = cursor.getDouble(27)
                        kePrecobranza.bcocod = cursor.getString(29)
                        kePrecobranza.bcomonto = cursor.getDouble(32)
                        kePrecobranza.bcoref = cursor.getString(33)//59
                        kePrecobranza.tipoPago = cursor.getString(59)
                        kePrecobranza.complemento = cursor.getString(60)
                    }
                }
        } catch (error: SQLiteException) {
            error.printStackTrace()
        }
        return kePrecobranza
    }

    private fun getDocs(cxcndoc: String) {
        kePrecobradocsMain = arrayListOf()
        try {
            keAndroid.rawQuery("SELECT * FROM ke_precobradocs WHERE cxcndoc = '$cxcndoc';", null)
                .use { cursor ->
                    while (cursor.moveToNext()) {
                        val kePrecobradocs = ke_precobradocs()
                        kePrecobradocs.cxcndoc = cursor.getString(0)
                        kePrecobradocs.agencia = cursor.getString(1)
                        kePrecobradocs.tipodoc = cursor.getString(2)
                        kePrecobradocs.documento = cursor.getString(3)
                        kePrecobradocs.bscobro = cursor.getDouble(4)
                        kePrecobradocs.prccobro = cursor.getDouble(5)
                        kePrecobradocs.prcdsctopp = cursor.getDouble(6)
                        kePrecobradocs.nroret = cursor.getString(7)
                        kePrecobradocs.fchemiret = cursor.getString(8)
                        kePrecobradocs.bsretiva = cursor.getDouble(9)
                        kePrecobradocs.refret = cursor.getString(10)
                        kePrecobradocs.nroretfte = cursor.getString(11)
                        kePrecobradocs.fchemirfte = cursor.getString(12)
                        kePrecobradocs.bsmtofte = cursor.getDouble(13)
                        kePrecobradocs.bsretfte = cursor.getDouble(14)
                        kePrecobradocs.refretfte = cursor.getString(15)
                        kePrecobradocs.pidvalid = cursor.getString(16)
                        kePrecobradocs.bsmtoiva = cursor.getDouble(17)
                        kePrecobradocs.retmun_bi = cursor.getDouble(18)
                        kePrecobradocs.retmun_cod = cursor.getString(19)
                        kePrecobradocs.retmun_nro = cursor.getString(20)
                        kePrecobradocs.retmun_mto = cursor.getDouble(21)
                        kePrecobradocs.retmun_fch = cursor.getString(22)
                        kePrecobradocs.retmun_ref = cursor.getString(23)
                        kePrecobradocs.diascalc = cursor.getDouble(24)
                        kePrecobradocs.prccomiv = cursor.getDouble(25)
                        kePrecobradocs.prccomic = cursor.getDouble(26)
                        kePrecobradocs.cxcndoc_aux = cursor.getString(27)
                        kePrecobradocs.tnetodbs = cursor.getDouble(28)
                        kePrecobradocs.tnetoddol = cursor.getDouble(29)
                        kePrecobradocs.fchrecibod = cursor.getString(30)
                        kePrecobradocs.kecxc_idd = cursor.getString(31)
                        kePrecobradocs.tasadiad = cursor.getDouble(32)
                        kePrecobradocs.afavor = cursor.getDouble(33)
                        kePrecobradocs.reten = cursor.getInt(34)

                        kePrecobradocsMain.add(kePrecobradocs)
                    }
                }
        } catch (error: SQLiteException) {
            error.printStackTrace()
        }
        //return kePrecobradocs
    }

    private fun getCliente(cxcndoc: String): Documentos {
        val documentos = Documentos()
        try {
            keAndroid.rawQuery(
                "SELECT nombrecli, codcliente FROM ke_precobradocs WHERE cxcndoc = '$cxcndoc';",null
            ).use { cursor ->
                if (cursor.moveToFirst()) {
                    documentos.nombrecli = cursor.getString(0)
                    documentos.codcliente = cursor.getString(1)
                }
            }
        } catch (error: SQLiteException) {
            error.printStackTrace()
        }
        return documentos
    }

    private fun getClave(): String {
        var retorno = ""
        retorno += if (kePrecobranza.moneda == "1") "B" else "D"
        retorno += if (kePrecobranza.tipoPago == "0") "C" else "A"
        retorno += if (kePrecobranza.complemento.isNotEmpty()) "C" else "S"
        retorno += if (kePrecobradocsMain.sumOf { it.prcdsctopp } != 0.0) "C" else "S"
        retorno += if (kePrecobranza.efectivo == 0.0) "T" else "E"
        retorno += if (kePrecobradocsMain[0].reten == 0) "C" else "S"
        return retorno
    }

}