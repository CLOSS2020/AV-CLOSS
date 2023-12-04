package com.appcloos.mimaletin

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Window
import android.widget.TextView
import com.appcloos.mimaletin.databinding.DialogDatosClientesBinding


class DialogClientesDatos(
    context: Context, private val codigoCliente: String, private val nombreCliente: String
) : AlertDialog(context) {


    private lateinit var binding: DialogDatosClientesBinding

    init {
        setCancelable(true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        binding = DialogDatosClientesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setColors()
        val conn = AdminSQLiteOpenHelper(context, "ke_android", null)

        val clientes = conn.getCliente(codigoCliente)

        binding.apply {
            tvCodigoCliente.text = clientes.codigo
            tvNombreCliente.text = clientes.nombre
            tvDireccionCliente.text = clientes.direccion
            tvTelefonoCliente.text = clientes.telefonos.ifEmpty { "Sin Número" }
            tvCorreoCliente.text = clientes.email.ifEmpty { "Sin Correo" }
            tvPerscontCliente.text = clientes.perscont.ifEmpty { "No posee" }
            tvContribespecialCliente.text =
                if (clientes.contribespecial == 1.0) "Es contribuyente" else "No es contribuyente"

            "Hace ${
                clientes.diasultvta.toCeroDecimals()
            } días, el ${
                clientes.fchultvta.formatoFechaShow()
            }, por ${
                clientes.mtoultvta.toTwoDecimals()
            } $".also { tvDiasultvtaCliente.text = it }

            tvFchcreaCliente.text = clientes.fchcrea.formatoFechaShow()

            tvTotmtodocsCliente.text = clientes.totmtodocs.toTwoDecimals()
            tvPrommtodocCliente.text = clientes.prommtodoc.toTwoDecimals()
            tvPromdiasvtaCliente.text = clientes.promdiasvta.toCeroDecimals()
            tvLimcredCliente.text = clientes.limcred.toTwoDecimals()

            tvCantdocsCLiente.text = clientes.cantdocs.toCeroDecimals()
            tvPrcdpagdiaCliente.text = clientes.prcdpagdia.toTwoDecimals()
            tvPromdiaspCLiente.text = clientes.promdiasp.toTwoDecimals()
            tvRiesgocrdCliente.text = clientes.riesgocrd.toCeroDecimals()

        }

        if ((clientes.diasultvta > clientes.promdiasvta) && (clientes.promdiasvta > 0)) {
            alertRed(binding.tvDiasultvtaCliente)
        }

        if (clientes.diasultvta > 40.0) {
            alertRed(binding.tvDiasultvtaCliente)
        }

        if (clientes.prcdpagdia < 50.0) {
            alertRed(binding.tvPrcdpagdiaCliente)
        }

        if (clientes.riesgocrd > 10.0) {
            alertRed(binding.tvRiesgocrdCliente)
        }

        if (clientes.email.isEmpty()) {
            alertYellow(binding.tvCorreoCliente)
        }

        if (clientes.perscont.isEmpty()) {
            alertYellow(binding.tvPerscontCliente)
        }

        if (clientes.telefonos.isEmpty()) {
            alertYellow(binding.tvTelefonoCliente)
        }

        binding.btnVerDocs.setOnClickListener {
            iraDocumentos(codigoCliente, nombreCliente)
        }
    }

    private fun alertRed(textview: TextView) {
        textview.apply {
            setBackgroundResource(R.drawable.border_radius_error)
            setTextColor(color(R.color.white))
        }
    }

    private fun alertYellow(textview: TextView) {
        textview.apply {
            setBackgroundResource(R.drawable.border_radius_warning)
            setTextColor(color(R.color.blackColor4))
        }
    }

    private fun iraDocumentos(codigoCliente: String, nombreCliente: String) {
        val intent = Intent(context, DocumentosActivity::class.java)
        intent.putExtra("codigoCliente", codigoCliente)
        intent.putExtra("nombreCliente", nombreCliente)
        intent.putExtra("cod_usuario", ClientesActivity.cod_usuario)
        intent.putExtra("codigoEmpresa", ClientesActivity.codigoEmpresa)
        context.startActivity(intent)
    }

    private fun setColors() {
        binding.apply {
            tvTelefono.setTextColor(tvTelefono.colorTextAgencia(Constantes.AGENCIA))
            tvCorreo.setTextColor(tvCorreo.colorTextAgencia(Constantes.AGENCIA))
            tvPerscont.setTextColor(tvPerscont.colorTextAgencia(Constantes.AGENCIA))
            tvContribespecial.setTextColor(tvContribespecial.colorTextAgencia(Constantes.AGENCIA))

            tvDiasultvta.setTextColor(tvDiasultvta.colorTextAgencia(Constantes.AGENCIA))

            tvTotmtodocs.setTextColor(tvTotmtodocs.colorTextAgencia(Constantes.AGENCIA))
            tvPrommtodoc.setTextColor(tvPrommtodoc.colorTextAgencia(Constantes.AGENCIA))
            tvPromdiasvta.setTextColor(tvPromdiasvta.colorTextAgencia(Constantes.AGENCIA))
            tvLimcred.setTextColor(tvLimcred.colorTextAgencia(Constantes.AGENCIA))

            tvCantdocs.setTextColor(tvCantdocs.colorTextAgencia(Constantes.AGENCIA))
            tvPrcdpagdia.setTextColor(tvPrcdpagdia.colorTextAgencia(Constantes.AGENCIA))
            tvPromdiasp.setTextColor(tvPromdiasp.colorTextAgencia(Constantes.AGENCIA))
            tvRiesgocrd.setTextColor(tvRiesgocrd.colorTextAgencia(Constantes.AGENCIA))

            btnVerDocs.setTextColor(btnVerDocs.colorTextAgencia(Constantes.AGENCIA))
        }
    }

}