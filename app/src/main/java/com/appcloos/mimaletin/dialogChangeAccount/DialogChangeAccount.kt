package com.appcloos.mimaletin.dialogChangeAccount

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
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

        conn = AdminSQLiteOpenHelper(context, "ke_android", null)

        listaEmpresas = conn.getEmpresas(Constantes.AGENCIA)



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
        val preferences: SharedPreferences =
            context.getSharedPreferences("Preferences", AppCompatActivity.MODE_PRIVATE)

        val user = conn.getCampoStringCamposVarios(
            "usuarios",
            "username",
            listOf("empresa"),
            listOf(listaEmpresas[position].kedCodigo)
        )
        val codUsuario = conn.getCampoStringCamposVarios(
            "usuarios",
            "vendedor",
            listOf("empresa"),
            listOf(listaEmpresas[position].kedCodigo)
        )
        val nUsuario = conn.getCampoStringCamposVarios(
            "usuarios",
            "nombre",
            listOf("empresa"),
            listOf(listaEmpresas[position].kedCodigo)
        )
        val superves = conn.getCampoStringCamposVarios(
            "usuarios",
            "superves",
            listOf("empresa"),
            listOf(listaEmpresas[position].kedCodigo)
        )

        val editor = preferences.edit()
        editor.putString("nick_usuario", user)
        editor.putString("cod_usuario", codUsuario)
        editor.putString("nombre_usuario", nUsuario)
        editor.putString("superves", superves)
        editor.putString("codigoEmpresa", listaEmpresas[position].kedCodigo)
        editor.putString("codigoSucursal", listaEmpresas[position].kedAgen)
        editor.apply()

        Constantes.AGENCIA = listaEmpresas[position].kedCodigo

        this.dismiss()
        onClick()
    }

}