package com.appcloos.mimaletin

import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.appcloos.mimaletin.databinding.ActivityEdoCuentaClienteBinding

class EdoCuentaClienteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEdoCuentaClienteBinding

    private lateinit var cliente: String
    private lateinit var nomCliente: String

    lateinit var keAndroid: SQLiteDatabase

    lateinit var enlaceEmpresa: String
    lateinit var nombreEmpresa: String
    lateinit var codigoSucursal: String

    lateinit var preferences: SharedPreferences

    lateinit var listadocs: ArrayList<Documentos>

    var ll_commit = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEdoCuentaClienteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cliente = intent.getStringExtra("cod_cliente").toString()
        nomCliente = intent.getStringExtra("nomCliente").toString()
    }
}
