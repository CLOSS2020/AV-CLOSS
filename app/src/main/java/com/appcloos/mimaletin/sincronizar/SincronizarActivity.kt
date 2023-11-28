package com.appcloos.mimaletin.sincronizar

import android.content.Context
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.appcloos.mimaletin.AdminSQLiteOpenHelper
import com.appcloos.mimaletin.ObjetoUtils
import com.appcloos.mimaletin.R
import com.appcloos.mimaletin.databinding.ActivitySincronizarBinding
import com.appcloos.mimaletin.sincronizar.dataClass.articulos.ArticulosResponse
import com.appcloos.mimaletin.sincronizar.dataClass.clientes.ClientesResponse
import com.appcloos.mimaletin.sincronizar.dataClass.documentos.DocumentosResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

typealias MyFun = (ProgressBar, Int, TextView, TextView) -> Unit

class SincronizarActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySincronizarBinding

    private val conn: AdminSQLiteOpenHelper =
        AdminSQLiteOpenHelper(this, "ke_android", null, 12)

    val context: Context = this
    private val fechaArticulos = ObjetoUtils.getDateNow()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySincronizarBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initIU()
    }

    private fun initIU() {
        binding.btnSync.setOnClickListener {
            initBinding()
            descargarDatos()
        }
    }

    private fun initBinding() {
        initProgressBar()
        initTextView()
    }

    private fun initProgressBar() {
        binding.apply {
            visibilityProgressBar(pbArticulos, tvArticulosResponse)
            visibilityProgressBar(pbDocumentos, tvDocumentosResponse)
            visibilityProgressBar(pbCliente, tvClienteResponse)
        }
    }

    private fun visibilityProgressBar(progressBar: ProgressBar, textView: TextView) {
        progressBar.isVisible = true
        textView.isVisible = false
    }


    private fun descargarDatos() {
        downloadArticulos(fechaArticulos)
        downloadDocumentos("99")
        downloadClientes("99")
    }

    private fun downloadArticulos(fechaSinc: String) {
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                Retrofit.getApi(Retrofit.getRetrofit1()).getArticulos(fechaSinc)
            }.onFailure {
                runOnUiThread {
                    ObjetoUtils.showError(context, "Error Articulos")
                    it.printStackTrace()
                }
            }.onSuccess {
                runOnUiThread {
                    if (it.body() != null && it.isSuccessful) {
                        //saveArticulos(it.body()!!)
                        it.body()?.let {
                            saveArticulos(it)
                        }
                    } else {
                        ObjetoUtils.showError(context, "Error Articulos 2")
                    }
                }
            }
        }
    }

    private fun downloadDocumentos(vendedor: String) {
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                Retrofit.getApi(Retrofit.getRetrofit1()).getDocumentos(vendedor)
            }.onFailure {
                runOnUiThread {
                    ObjetoUtils.showError(context, "Error Articulos")
                    it.printStackTrace()
                }
            }.onSuccess {
                runOnUiThread {
                    if (it.body() != null && it.isSuccessful) {
                        saveDocumentos(it.body()!!)
                    } else {
                        ObjetoUtils.showError(context, "Error Articulos 2")
                    }
                }
            }
        }
    }

    private fun downloadClientes(vendedor: String) {
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                Retrofit.getApi(Retrofit.getRetrofit1()).getClientes(vendedor)
            }.onFailure {
                runOnUiThread {
                    ObjetoUtils.showError(context, "Error Articulos")
                    it.printStackTrace()
                }
            }.onSuccess {
                runOnUiThread {
                    if (it.body() != null && it.isSuccessful) {
                        saveClientes(it.body()!!)
                    } else {
                        ObjetoUtils.showError(context, "Error Articulos 2")
                    }
                }
            }
        }
    }

    private fun saveArticulos(body: ArticulosResponse) {
        println("--Articulos--")
        println(body)
        println("--Articulos--")
        conn.guardarArticulos(body)
        terminateProgressBar(
            binding.pbArticulos,
            body.articulo?.size ?: 0,
            binding.tvArticulosResponse,
            binding.tvArticulos
        )
    }

    private fun saveDocumentos(body: DocumentosResponse) {
        println("--Articulos--")
        println(body)
        println("--Articulos--")
        terminateProgressBar(
            binding.pbDocumentos,
            body.documento?.size ?: 0,
            binding.tvDocumentosResponse,
            binding.tvDocumentos
        )
    }

    private fun saveClientes(body: ClientesResponse) {
        println("--Articulos--")
        println(body)
        println("--Articulos--")
        terminateProgressBar(
            binding.pbCliente,
            body.clientes?.size ?: 0,
            binding.tvClienteResponse,
            binding.tvCliente
        )
    }

    //Inicializa los textView
    private fun initTextView() {
        binding.apply {
            setNeuralColor(tvArticulos)
            setNeuralColor(tvArticulosResponse)
            setNeuralColor(tvDocumentos)
            setNeuralColor(tvDocumentosResponse)
            setNeuralColor(tvCliente)
            setNeuralColor(tvClienteResponse)
        }
    }

    //Coloca los textView en negro (color neutral)
    private fun setNeuralColor(textView: TextView) {
        val neutralColor = ContextCompat.getColor(context, R.color.blackColor)
        textView.setTextColor(neutralColor)
    }

    //Coloca los textView wn Verde (Terminado con exito) e indica cuantos items descargo
    private fun terminateProgressBar(
        progressBar: ProgressBar, size: Int, textViewResponse: TextView, textView: TextView
    ) {
        val successColor = ContextCompat.getColor(context, R.color.greenColor)

        progressBar.isVisible = false
        textViewResponse.isVisible = true
        textViewResponse.text = size.toString()
        textView.setTextColor(successColor)
        textViewResponse.setTextColor(successColor)
    }


}