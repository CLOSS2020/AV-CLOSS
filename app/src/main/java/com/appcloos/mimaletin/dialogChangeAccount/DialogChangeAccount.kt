package com.appcloos.mimaletin.dialogChangeAccount

import android.app.AlertDialog
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.Window
import androidx.recyclerview.widget.LinearLayoutManager
import com.appcloos.mimaletin.AdminSQLiteOpenHelper
import com.appcloos.mimaletin.Constantes
import com.appcloos.mimaletin.databinding.DialogChangeAccountBinding
import com.appcloos.mimaletin.dialogChangeAccount.adapter.DialogChangeAccountAdapter
import com.appcloos.mimaletin.dialogChangeAccount.model.keDataconex

class DialogChangeAccount(context: Context, private val onClick: () -> Unit) :
    AlertDialog(context) {

    init {
        setCancelable(true)
    }

    private lateinit var binding: DialogChangeAccountBinding

    private lateinit var listaEmpresas: MutableList<keDataconex>

    private lateinit var conn: AdminSQLiteOpenHelper


    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        binding = DialogChangeAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        conn = AdminSQLiteOpenHelper(context, "ke_android", null, 46)

        listaEmpresas = conn.getEmpresas(Constantes.AGENCIA)


        val nightModeFlags = context.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK


        binding.rvEmpresa.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = DialogChangeAccountAdapter(listaEmpresas){ position -> cambiarEmpresa(position) }
        }


        /*binding.checkEmpresa1.imageTintList =
            if (nightModeFlags == Configuration.UI_MODE_NIGHT_NO && Constantes.AGENCIA == 1) {
                ColorStateList.valueOf(ContextCompat.getColor(context, R.color.blackColor1))
            } else if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES && Constantes.AGENCIA == 1) {
                ColorStateList.valueOf(ContextCompat.getColor(context, R.color.whiteColor1))
            } else {
                ColorStateList.valueOf(ContextCompat.getColor(context, R.color.grayPerfect))
            }

        binding.checkEmpresa2.imageTintList =
            if (nightModeFlags == Configuration.UI_MODE_NIGHT_NO && Constantes.AGENCIA == 2) {
                ColorStateList.valueOf(ContextCompat.getColor(context, R.color.blackColor1))
            } else if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES && Constantes.AGENCIA == 2) {
                ColorStateList.valueOf(ContextCompat.getColor(context, R.color.whiteColor1))
            } else {
                ColorStateList.valueOf(ContextCompat.getColor(context, R.color.grayPerfect))
            }


        binding.llEmpresa1.setOnClickListener {
            Constantes.AGENCIA = 1
            this.dismiss()
            onClick()
        }

        binding.llEmpresa2.setOnClickListener {
            Constantes.AGENCIA = 2
            this.dismiss()
            onClick()
        }*/

        binding.llAgregar.setOnClickListener {
            val dialog = DialogAddAccount(context, onClick)
            dialog.show()
        }
    }

    private fun cambiarEmpresa(position: Int) {
        val empresa = listaEmpresas[position].kedCodigo
        Constantes.AGENCIA = empresa
        this.dismiss()
        onClick()
    }

}