package com.appcloos.mimaletin

import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.appcloos.mimaletin.databinding.ActivityEdoCuentaClienteBinding
import com.appcloos.mimaletin.databinding.ActivitySelectorClienteRetenBinding
import com.appcloos.mimaletin.databinding.FragmentEdoCuentaClienteBinding
import com.appcloos.mimaletin.moduloCXC.fragments.EdoCuentaClienteAdapter

class EdoCuentaClienteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEdoCuentaClienteBinding

    private lateinit var cliente: String
    private lateinit var nomCliente: String

    lateinit var ke_android: SQLiteDatabase
    private lateinit var conn: AdminSQLiteOpenHelper

    lateinit var enlaceEmpresa: String
    lateinit var nombreEmpresa: String
    lateinit var codigoSucursal: String
    private var fechaAuxiliar: String = "0001-01-01T00:00:00"

    var listaDocsSeleccionados: ArrayList<String> = ArrayList()

    lateinit var preferences: SharedPreferences
    private var cod_usuario: String? = ""
    private var codEmpresa: String? = ""

    lateinit var listadocs: ArrayList<Documentos>
    lateinit var docsViejos: ArrayList<String>

    private lateinit var adapter: EdoCuentaClienteAdapter

    var ll_commit = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEdoCuentaClienteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cliente = intent.getStringExtra("cod_cliente").toString()
        nomCliente = intent.getStringExtra("nomCliente").toString()
    }
}