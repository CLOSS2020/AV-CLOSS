package com.appcloos.mimaletin

import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.appcloos.mimaletin.databinding.ActivityDetalleVendedorBinding

class DetalleVendedorActivity : AppCompatActivity() {
    var conn: AdminSQLiteOpenHelper? = null


    private lateinit var binding: ActivityDetalleVendedorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleVendedorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED //mantener la orientacion vertical
        val intent = intent
        val codigoVendedor = intent.getStringExtra("codigoVend")
        val nombreVendedor = intent.getStringExtra("nombreVend")
        supportActionBar!!.title = nombreVendedor

        //declaracion de campos de la tabla que van a ser llenados por el resultado de la consulta.

        llenarDatosDeFicha(codigoVendedor)

        setColors()
    }

    private fun llenarDatosDeFicha(codigoVendedor: String?) {
        //Locale es = new Locale("es");

        //NumberFormat formatoMoneda     = NumberFormat.getCurrencyInstance();
        conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null, 4)
        val keAndroid = conn!!.writableDatabase
        val cursor = keAndroid.rawQuery(
            "SELECT cntpedidos, mtopedidos, cntfacturas, mtofacturas, metavend, prcmeta, cntclientes, clivisit, prcvisitas, lom_montovtas, lom_prcvtas, lom_prcvisit, rlom_montovtas, rlom_prcvtas, rlom_prcvisit, ppgdol_totneto, devdol_totneto, defdol_totneto, totdolcob FROM ke_estadc01 WHERE vendedor ='$codigoVendedor'",
            null
        )
        while (cursor.moveToNext()) {
            val cantpedidos = cursor.getInt(0)
            binding.lbCantpedidos.text = cantpedidos.toString()

            val mtopedidos = cursor.getDouble(1)
            binding.lbMtopedidos.text = "$ " + mtopedidos.toTwoDecimals()

            val cantfacturas = cursor.getInt(2)
            binding.lbCantfacturas.text = cantfacturas.toString()

            val mtofacturas = cursor.getDouble(3)
            binding.lbMtofacturas.text = "$ " + mtofacturas.toTwoDecimals()

            val metavend = cursor.getDouble(4)
            binding.lbMeta.text = "$ " + metavend.toTwoDecimals()

            val prcmeta = cursor.getDouble(5)
            binding.lbPrcmeta.text = prcmeta.toTwoDecimals() + "%"

            val cntclientes = cursor.getInt(6)
            binding.lbCantclientes.text = cntclientes.toString()

            val clivisit = cursor.getInt(7)
            binding.lbVisitados.text = clivisit.toString()

            val prcvisitas = cursor.getDouble(8)
            binding.lbPrcvisitas.text = prcvisitas.toTwoDecimals() + "%"

            val lomMontovtas = cursor.getInt(9)
            binding.lbPnmtoventa.text = lomMontovtas.toString()

            val lomPrcvtas = cursor.getInt(10)
            binding.lbPnprcventa.text = lomPrcvtas.toString()

            val lomPrcvisit = cursor.getInt(11)
            binding.lbPnvisitas.text = lomPrcvisit.toString()

            val rlomMontovtas = cursor.getInt(12)
            binding.lbPrmtoventa.text = rlomMontovtas.toString()

            val rlomPrcvtas = cursor.getInt(13)
            binding.lbPrprcventa.text = rlomPrcvtas.toString()

            val rlomPrcvisit = cursor.getInt(14)
            binding.lbPrvisitas.text = rlomPrcvisit.toString()

            val ppgdolTotneto = cursor.getDouble(15)
            binding.tvPpgdolTotneto.text = "$ " + ppgdolTotneto.toTwoDecimals()

            val devdolTotneto = cursor.getDouble(16)
            binding.tvDevdolTotneto.text = "$ " + devdolTotneto.toTwoDecimals()

            val defdolTotneto = cursor.getDouble(17)
            binding.tvDefdolTotneto.text = "$ " + defdolTotneto.toTwoDecimals()

            val mtofacturasNeto = mtofacturas - ppgdolTotneto - devdolTotneto - defdolTotneto
            binding.tvMtofacturasNeto.text = "$ " + mtofacturasNeto.toTwoDecimals()

            val totdolcob = cursor.getDouble(18)
            binding.tvTotdolcob.text = "$ " + totdolcob.toTwoDecimals()

        }
        cursor.close()
    }

    override fun getTheme(): Resources.Theme {
        val theme = super.getTheme()
        theme.applyStyle(setThemeAgencia(Constantes.AGENCIA), true)
        // you could also use a switch if you have many themes that could apply
        return theme
    }

    private fun setColors() {
        binding.apply {
            trTableRow1.setBackgroundColor(trTableRow1.colorAgencia(Constantes.AGENCIA))
            trTableRow2.setBackgroundColor(trTableRow2.colorAgencia(Constantes.AGENCIA))

        }

    }
}