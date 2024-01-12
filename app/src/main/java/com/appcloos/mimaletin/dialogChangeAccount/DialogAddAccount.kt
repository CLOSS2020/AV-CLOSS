package com.appcloos.mimaletin.dialogChangeAccount

import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.appcloos.mimaletin.AdminSQLiteOpenHelper
import com.appcloos.mimaletin.Constantes
import com.appcloos.mimaletin.MainActivity
import com.appcloos.mimaletin.ObjetoAux
import com.appcloos.mimaletin.colorButtonAgencia
import com.appcloos.mimaletin.databinding.DialogAddAccountBinding
import com.appcloos.mimaletin.dialogChangeAccount.model.keDataconex
import com.appcloos.mimaletin.setColorModel
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DialogAddAccount(context: Context, private val onAddClick: () -> Unit) :
    AlertDialog(context) {

    init {
        setCancelable(true)
    }

    private lateinit var binding: DialogAddAccountBinding

    lateinit var conn: AdminSQLiteOpenHelper

    private lateinit var newEmpresa: keDataconex

    lateinit var objetoAux: ObjetoAux

    private lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        binding = DialogAddAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.window?.clearFlags(
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
        )

        this.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        conn = AdminSQLiteOpenHelper(context, "ke_android", null)
        preferences = context.getSharedPreferences("Preferences", AppCompatActivity.MODE_PRIVATE)
        objetoAux = ObjetoAux(context)

        setListener()

        setColor()
    }

    private fun setListener() {
        binding.btnValidar.setOnClickListener { validarEmpresa(binding.etCodigo.text.toString()) }
        binding.btnIngresar.setOnClickListener { ingresarEmpresa() }
    }

    private fun validarEmpresa(codigoEmpresa: String) {
        if (binding.etCodigo.text.isNullOrEmpty()) {
            Toast.makeText(context, "Falta codigo de la empresa", Toast.LENGTH_SHORT).show()
            return
        }

        val flag = conn.validarExistencia("ke_enlace", "kee_codigo", codigoEmpresa)

        if (flag) {
            Toast.makeText(
                context,
                "Ya posee una sesion iniciada con esa empresa",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val url = "https://www.cloccidental.com/webservice/validarempresa.php?codigo=$codigoEmpresa"
        val keAndroid = conn.writableDatabase
        val jsonArrayRequest = JsonArrayRequest(url, { response ->
            if (response != null) {
                val jsonObject: JSONObject // creamos un objeto json vacio
                keAndroid.beginTransaction()

                try {
                    // bajamos los datos de la empresa/sucursal
                    jsonObject = response.getJSONObject(0)

                    val codigoEmp = jsonObject.getString("codigoEmpresa")
                    val enlace = jsonObject.getString("enlaceEmpresa")
                    val nombreEmp = jsonObject.getString("nombreEmpresa")
                    val statusEmp = jsonObject.getString("statusEmpresa")
                    val codigoSuc = jsonObject.getString("agenciaEmpresa")

                    newEmpresa = keDataconex(
                        codigoEmp, nombreEmp, statusEmp, enlace, codigoSuc
                    )

                    /*val guardarEnlaces = ContentValues()
                    guardarEnlaces.put("kee_codigo", codigoEmp)
                    guardarEnlaces.put("kee_nombre", nombreEmp)
                    guardarEnlaces.put("kee_url", enlace)
                    guardarEnlaces.put("kee_status", statusEmp)
                    guardarEnlaces.put("kee_sucursal", codigoSuc)

                    keAndroid.insert("ke_enlace", null, guardarEnlaces)*/

                    // en este proceso vamos a cargar los permisos
                    var permisosJson: JSONObject
                    for (i in 0 until response.length()) {
                        permisosJson = response.getJSONObject(i)
                        val codigoModulo = permisosJson.getString("codigoModulo")
                        // System.out.println("CODIGO DEL MODULO " + codigoModulo);
                        val activoModulo = permisosJson.getString("estadoModulo")

                        val guardarPermisos = ContentValues()
                        guardarPermisos.put("ked_codigo", codigoEmp)
                        guardarPermisos.put("kmo_codigo", codigoModulo)
                        guardarPermisos.put("kmo_status", activoModulo)
                        guardarPermisos.put("kee_sucursal", codigoSuc)
                        keAndroid.insert("ke_modulos", null, guardarPermisos)
                    }
                    keAndroid.setTransactionSuccessful()
                    keAndroid.endTransaction()
                    if (enlace != "") {
                        binding.tilUser.isEnabled = true
                        binding.tilPass.isEnabled = true
                        binding.btnIngresar.isEnabled = true
                    } else {
                        binding.tilUser.isEnabled = false
                        binding.tilPass.isEnabled = false
                        binding.btnIngresar.isEnabled = false
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(
                        context,
                        "No se pudo validar el codigo",
                        Toast.LENGTH_LONG
                    ).show()
                    keAndroid.endTransaction()
                }
            }
        }, { error ->
            println("--Error--")
            error.printStackTrace()
            println("--Error--")
            Toast.makeText(
                context,
                "No se pudo validar el codigo, intente más tarde",
                Toast.LENGTH_LONG
            ).show()
        })

        val requestQueue = Volley.newRequestQueue(context)
        requestQueue.add(jsonArrayRequest)
    }

    private fun ingresarEmpresa() {
        val user = binding.etUser.text.toString()
        val pass = binding.etPass.text.toString()

        if (newEmpresa.kedCodigo.isEmpty()) {
            Toast.makeText(context, "Falta codigo de la empresa", Toast.LENGTH_SHORT).show()
            return
        }

        if (user.isEmpty()) {
            Toast.makeText(context, "Falta el usuario de vendedor", Toast.LENGTH_SHORT).show()
            return
        }

        if (pass.isEmpty()) {
            Toast.makeText(context, "Falta la contraseña", Toast.LENGTH_SHORT).show()
            return
        }

        validarUsuario(
            "https://${newEmpresa.kedEnlace}/webservice/validar_usuario_actualizadoV_5.php?nick_usuario=$user&pass_usuario=$pass",
            user,
            pass
        )
    }

    private fun validarUsuario(url: String, user: String, pass: String) {
        val jsonArrayRequest = JsonArrayRequest(url, { response ->
            if (response != null) { // si la respuesta no viene vacia
                var sesion =
                    0 // <-----Variable de logueo previo, 1 = Esta logueado, 0 = No esta nadie logueado
                var codUsuario = ""
                var nUsuario = ""
                var nombreUsuario = ""
                var almacen = ""
                var desactivo = 0.0
                var fechamodifi = ""
                var ualterprec = 0.0
                var ultimoped = ""
                var sesionactiva = ""
                var superves = ""
                var vendedor = ""
                var ultimorec = ""
                var ultimorcl = ""
                var ultimorcxc = ""

                var jsonObject: JSONObject // creamos un objeto json vacio
                for (i in 0 until response.length()) {
                    /*pongo todo en el objeto segun lo que venga */
                    try {
                        jsonObject = response.getJSONObject(i)
                        nUsuario = jsonObject.getString("nombre") // el nombre del vendedor
                        codUsuario = jsonObject.getString("vendedor") // el codigo
                        nombreUsuario =
                            jsonObject.getString("username") // almacenamos el nombre de usuario
                        almacen = jsonObject.getString("almacen").trim { it <= ' ' }
                        desactivo =
                            jsonObject.getDouble("desactivo") // este campo nos indicara si el usuario se encuentra bloqueado o no.
                        fechamodifi = jsonObject.getString("fechamodifi").trim { it <= ' ' }
                        ualterprec = jsonObject.getDouble("ualterprec")
                        ultimoped =
                            jsonObject.getString("correlativo") // obtenemos el ultimo correlativo
                        sesionactiva =
                            jsonObject.getString("sesionactiva") // traemos la fecha de la sesion que estamos iniciando.
                        superves = jsonObject.getString("superves")
                        vendedor = jsonObject.getString("vendedor").trim { it <= ' ' }
                        ultimorec = jsonObject.getString("recibocobro").trim { it <= ' ' }
                        ultimorcl = jsonObject.getString("correlativoreclamo").trim { it <= ' ' }
                        ultimorcxc =
                            jsonObject.getString("correlativoprecobranza").trim { it <= ' ' }
                        sesion = jsonObject.getInt("sesion")
                    } catch (e: JSONException) {
                        println("--Error--")
                        e.printStackTrace()
                        println("--Error--")
                        Toast.makeText(context, "No se logro ingresar", Toast.LENGTH_LONG).show()
                    }
                }
                if (codUsuario.isEmpty()) {
                    Toast.makeText(
                        context,
                        "Usuario o contraseña incorrecto",
                        Toast.LENGTH_LONG
                    ).show()

                    dismiss()
                } else {
                    if (sesion == 1) {
                        Toast.makeText(
                            context,
                            "El usuario ya tiene una sessión activa.",
                            Toast.LENGTH_LONG
                        ).show()

                        dismiss()

                        return@JsonArrayRequest
                    }
                    if (desactivo == 0.0 || desactivo == 1.0) {
                        if (ultimoped.isEmpty()) {
                            ultimoped = "0000"
                        }
                        if (ultimorec.isEmpty()) {
                            ultimorec = "0000"
                        }
                        if (ultimorcl.isEmpty()) {
                            ultimorcl = "0000"
                        }
                        if (ultimorcxc.isEmpty()) {
                            ultimorcxc = "0000"
                        }
                        val keAndroid = conn.writableDatabase
                        keAndroid.beginTransaction()
                        try {
                            // preparacion e inserción del correlativo de pedidos
                            val correlativoTexto = MainActivity.right(ultimoped, 4)
                            var nroCorrelativo = correlativoTexto.toInt()
                            nroCorrelativo += 1
                            val insertar = ContentValues()
                            insertar.put("kco_numero", nroCorrelativo)
                            insertar.put("kco_vendedor", codUsuario.trim { it <= ' ' })
                            insertar.put("empresa", newEmpresa.kedCodigo)
                            keAndroid.insert("ke_correla", null, insertar)
                            // --------------------------------------------------------------------

                            // preparacion e inserción del correlativo de recibos
                            val reciboTexto = MainActivity.right(ultimorec, 4)
                            var nroRecibo = reciboTexto.toInt()
                            nroRecibo += 1
                            val insertarRec = ContentValues()
                            insertarRec.put("kcc_numero", nroRecibo)
                            insertarRec.put("kcc_vendedor", codUsuario.trim { it <= ' ' })
                            insertarRec.put("empresa", newEmpresa.kedCodigo)
                            keAndroid.insert("ke_correlacxc", null, insertarRec)
                            // -------------------------------------------------------

                            // preparacion e inserción del correlativo de reclamos
                            val reclamoTexto = MainActivity.right(ultimorcl, 4)
                            var nroReclamo = reclamoTexto.toInt()
                            nroReclamo += 1
                            val insertarRcl = ContentValues()
                            insertarRcl.put("kdev_numero", nroReclamo)
                            insertarRcl.put("kdev_vendedor", codUsuario.trim { it <= ' ' })
                            insertarRcl.put("empresa", newEmpresa.kedCodigo)
                            keAndroid.insert("ke_correladev", null, insertarRcl)
                            // ---------------------------------------------------------------------------

                            // preparacion e inserción del correlativo de precobranza
                            val correlaCXC = MainActivity.right(ultimorcxc, 4)
                            var nroCXC = correlaCXC.toInt()
                            nroCXC += 1
                            val insertarCXC = ContentValues()
                            insertarCXC.put("kcor_numero", nroCXC)
                            insertarCXC.put("kcor_vendedor", codUsuario.trim { it <= ' ' })
                            insertarCXC.put("empresa", newEmpresa.kedCodigo)
                            keAndroid.insert("ke_corprec", null, insertarCXC)
                            // ---------------------------------------------------------------------------
                            /*keAndroid.delete(
                                "usuarios",
                                "username = ?",
                                arrayOf(user)
                            )*/

                            val guardarEnlaces = ContentValues()
                            guardarEnlaces.put("kee_codigo", newEmpresa.kedCodigo)
                            guardarEnlaces.put("kee_nombre", newEmpresa.kedNombre)
                            guardarEnlaces.put("kee_url", newEmpresa.kedEnlace)
                            guardarEnlaces.put("kee_status", newEmpresa.kedStatus)
                            guardarEnlaces.put("kee_sucursal", newEmpresa.kedAgen)

                            keAndroid.insert("ke_enlace", null, guardarEnlaces)

                            // agrego la fecha en la cual inició sesión
                            val hoy = Calendar.getInstance().time
                            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                            val fechaSync = sdf.format(hoy)
                            val usuarioDatos = ContentValues()
                            usuarioDatos.put("nombre", nUsuario)
                            usuarioDatos.put("username", nombreUsuario)
                            usuarioDatos.put("password", pass)
                            usuarioDatos.put("vendedor", vendedor)
                            usuarioDatos.put("almacen", almacen)
                            usuarioDatos.put("desactivo", desactivo)
                            usuarioDatos.put("fechamodifi", fechaSync)
                            usuarioDatos.put("ualterprec", ualterprec)
                            usuarioDatos.put("sesionactiva", sesionactiva)
                            usuarioDatos.put("superves", superves)
                            usuarioDatos.put("empresa", newEmpresa.kedCodigo)
                            keAndroid.insert("usuarios", null, usuarioDatos)
                            keAndroid.setTransactionSuccessful()
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                "Error insertando correlativo $e",
                                Toast.LENGTH_LONG
                            ).show()
                            keAndroid.endTransaction()
                        } finally {
                            keAndroid.endTransaction()
                        }

                        objetoAux.login(codUsuario, 1, newEmpresa.kedEnlace)

                        val editor = preferences.edit()
                        editor.putString("nick_usuario", user)
                        editor.putString("cod_usuario", codUsuario)
                        editor.putString("nombre_usuario", nUsuario)
                        editor.putString("superves", superves)
                        editor.putString("codigoEmpresa", newEmpresa.kedCodigo)
                        editor.putString("codigoSucursal", newEmpresa.kedAgen)
                        editor.apply()

                        Constantes.AGENCIA = newEmpresa.kedCodigo

                        onAddClick()
                    } else if (desactivo == 2.0) {
                        Toast.makeText(
                            context,
                            "Este usuario se encuentra desactivado",
                            Toast.LENGTH_LONG
                        ).show()
                        dismiss()
                    }
                }
            } else {
                Toast.makeText(
                    context,
                    "Usuario o password incorrecto",
                    Toast.LENGTH_LONG
                ).show()
                dismiss()
            }
        }, { error ->
            println("--Error--")
            error.printStackTrace()
            println("--Error--")
            Toast.makeText(context, "No se logró el inicio de sesión", Toast.LENGTH_LONG).show()
            dismiss()
        })

        val requestQueue = Volley.newRequestQueue(context)
        requestQueue.add(jsonArrayRequest)
    }

    fun setColor() {
        binding.apply {
            tilCodigo.setColorModel(Constantes.AGENCIA)
            tilUser.setColorModel(Constantes.AGENCIA)
            tilPass.setColorModel(Constantes.AGENCIA)

            btnValidar.setBackgroundColor(btnValidar.colorButtonAgencia(Constantes.AGENCIA))
            btnIngresar.setBackgroundColor(btnIngresar.colorButtonAgencia(Constantes.AGENCIA))
        }
    }
}
