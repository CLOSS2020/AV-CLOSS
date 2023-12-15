/* ******************************************************************************************************
 * 4 DE SEPTIEMBRE, 2020                                                                              *
 * AUTOR: PCV                                                                                         *
 * APP: MI MALETIN V 1.0.0                                                                            *
 * ESTA ES LA ACTIVITY DEL LOGIN DE LA APP PROTOTIPO DE PEDIDOS                                       *
 * EL NOMBRE Y EL ICONO SON TENTATIVOS.                                                               *
 * -------------------------------------------------------------------------------------------------- *
 * 25 DE SEPTIEMBRE, 2020                                                                             *
 * AUTOR: PCV                                                                                         *
 * ACTUALIZACION: DOCUMENTANCION DE LAS CLASES                                                        *
 * ---------------------------------------------------------------------------------------------------*
 * 05 DE OCTUBRE, 2020                                                                                *
 * AUTOR: PCV                                                                                         *
 * ACTUALIZACION: SE CAMBIO EL METODO DE VALIDACION DE USUARIO DE UN STRING REQUEST A UN JSON ARRAY   *
 * REQUEST, CON EL OBJETIVO DE PODER ENVIAR INFORMACION AL SIGUIENTE ACTIVITY (AL HOME)               *
 *                                                                                                    *
 ******************************************************************************************************

 * */
package com.appcloos.mimaletin

import android.content.ContentValues
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.appcloos.mimaletin.databinding.ActivityMainBinding
import com.appcloos.mimaletin.dialogChangeAccount.model.keDataconex
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.ActivityResult
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity(), Serializable {
    private lateinit var preferences: SharedPreferences
    lateinit var objetoAux: ObjetoAux
    lateinit var conn: AdminSQLiteOpenHelper
    lateinit var keAndroid1: SQLiteDatabase
    private lateinit var appUpdateManager: AppUpdateManager

    private lateinit var binding: ActivityMainBinding

    private lateinit var newEmpresa: keDataconex

    private lateinit var codEmpresa: String

    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED //mantener la orientacion vertical
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar!!.hide() //metodo para esconder la actionbar
        //checkForAppUpdate();

        binding.tvversion.text = "Ver. " + Constantes.VERSION_NAME
        enlace = ""
        preferences = getSharedPreferences("Preferences", MODE_PRIVATE)
        conn = AdminSQLiteOpenHelper(this@MainActivity, "ke_android", null)
        objetoAux = ObjetoAux(this)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        delegate.applyDayNight()
        validarSesion()

        //validarEmpresaLicencia("https://www.cloccidental.com/webservice/validarempresa.php?codigo=$codigo_empresa")
        setListener()

        //checkForAppUpdate();
        //obtenerVersion("https://cloccidental.com/webservice/versionapp.php?version_usuario=" + versionApp);

        /*bt_validar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(codigo_empresa.isEmpty()){
                    Toast.makeText(MainActivity.this, "Se requiere codigo de validacion.", Toast.LENGTH_LONG).show(); // si esta vacio,
                } else {

                }

                if(enlace.equals("")){
                    //si no consigue la empresa, desactiva el inicio de sesión
                    bt_iniciar.setEnabled(false);
                    et_password.setEnabled(false);
                    et_usuario.setEnabled(false);

                    et_usuario.setVisibility(View.INVISIBLE);
                    et_password.setVisibility(View.INVISIBLE);
                    bt_iniciar.setVisibility(View.INVISIBLE);
                }


            }
        });*/
        /*binding.btIniciar.setOnClickListener {     //le indicamos al boton que usara un metodo on click listener
            binding.txtUsuario.isEnabled = true
            binding.txtPassword.isEnabled = true
            binding.btIniciar.text = "Iniciar Sesion"
            nick_usuario = binding.txtUsuario.text
                .toString() // guardamos en la variable lo que viDebes indicar la empresa ene del objeto
            pass_usuario =
                binding.txtPassword.text.toString() // guardamos en la variable lo que viene del objeto
            if (nick_usuario!!.isNotEmpty() && pass_usuario!!.isNotEmpty()) { //si el nombre de usuarioy  el password no estan en blanco que ejecute
                //el metodo a continuacion
                binding.btIniciar.isEnabled = false
                binding.btIniciar.text = "Iniciando Sesión"
                validarUsuario("https://$enlace/webservice/validar_usuario_actualizadoV_5.php?nick_usuario=$nick_usuario&pass_usuario=$pass_usuario&agencia=$codigoSuc") //llamamos al metodo para validar y hacer login de usuario.
                //System.out.println("https://"+ enlace + "/webservice/validar_usuario_actualizadoV_5.php?nick_usuario="+nick_usuario + "&pass_usuario=" + pass_usuario +"&agencia=" + codigoSuc);
            } else {
                Toast.makeText(
                    this@MainActivity,
                    "No se permiten campos en blanco.",
                    Toast.LENGTH_LONG
                ).show() // si esta vacio,
                //le decimos al usuario que no se permiten campos en blanco.
            }
        }*/
    }

    /*private fun validarEmpresaLicencia(url: String) {
        val keAndroid = conn.writableDatabase
        println(url)
        val jsonArrayRequest: JsonArrayRequest =
            object : JsonArrayRequest(url, Response.Listener { response: JSONArray? ->
                if (response != null) {
                    val jsonObject: JSONObject //creamos un objeto json vacio
                    keAndroid.beginTransaction()
                    //keAndroid.execSQL("DELETE FROM ke_enlace")
                    //keAndroid.execSQL("DELETE FROM ke_modulos")
                    try {
                        //bajamos los datos de la empresa/sucursal
                        jsonObject = response.getJSONObject(0)
                        codigoEmp = jsonObject.getString("codigoEmpresa")
                        enlace = jsonObject.getString("enlaceEmpresa")
                        nombreEmp = jsonObject.getString("nombreEmpresa")
                        statusEmp = jsonObject.getString("statusEmpresa")
                        codigoSuc = jsonObject.getString("agenciaEmpresa")
                        val guardarEnlaces = ContentValues()
                        guardarEnlaces.put("kee_codigo", codigoEmp)
                        guardarEnlaces.put("kee_nombre", nombreEmp)
                        guardarEnlaces.put("kee_url", enlace)
                        guardarEnlaces.put("kee_status", statusEmp)
                        guardarEnlaces.put("kee_sucursal", codigoSuc)

                        /*//analizamos si hay modulos
                    Cursor cursorurl = ke_android.rawQuery("SELECT count(kmo_codigo) FROM ke_modulos WHERE ked_codigo ='" + codigoEmp+"' AND kmo_codigo='" + codigoModulo+"'", null);
                    cursorurl.moveToFirst();*/
                        keAndroid.insert("ke_enlace", null, guardarEnlaces)
                        //si ya hay modulos, es que hay empresa
                        /*int conteoEnlace = cursorurl.getInt(0);
                    if(conteoEnlace > 0){
                        ke_android.execSQL("UPDATE ke_enlace SET kee_codigo ='" + codigoEmp + "', kee_nombre = '" +nombreEmp+ "', kee_url = '"+enlace+ "', kee_status='"+statusEmp+ "', kee_sucursal='"+codigoSuc+"'" +
                                " WHERE kee_codigo = '"+codigoEmp +"' AND kee_sucursal ='" + codigoSuc +  "'");
                     //caso contrario, guardo la nueva empresa
                    }else{

                    }*/


                        //en este proceso vamos a cargar los permisos
                        var permisosJson: JSONObject
                        for (i in 0 until response.length()) {
                            permisosJson = response.getJSONObject(i)
                            codigoModulo = permisosJson.getString("codigoModulo")
                            //System.out.println("CODIGO DEL MODULO " + codigoModulo);
                            activoModulo = permisosJson.getString("estadoModulo")
                            //System.out.println("ESTADO DEL MODULO " + activoModulo);

                            /* Cursor cursor = ke_android.rawQuery("SELECT count(kmo_codigo) FROM ke_modulos WHERE ked_codigo ='" + codigoEmp+"' AND kmo_codigo='" + codigoModulo+"' AND kee_sucursal ='" + codigoSuc +  "'", null);
                        cursor.moveToFirst();

                        int conteoPermiso = cursor.getInt(0);
                        System.out.println("Conteo de Permisos: " + conteoPermiso);


                        if(conteoPermiso > 0){*/
                            /* ContentValues guardarPermisos = new ContentValues();
                            guardarPermisos.put("ked_codigo", codigoEmp);
                            guardarPermisos.put("kmo_codigo", codigoModulo);
                            guardarPermisos.put("kmo_status", activoModulo);

                            ke_android.update("ke_modulos", guardarPermisos, "ked_codigo = "+codigoEmp +" AND kmo_codigo = ?", new String[]{codigoModulo});*/

                            /*NO SE POR QUE, PERO SOLO AGARRA EL UPDATE HACIENDOLO DE LA FORMA LARGA*/
                            //ke_android.execSQL("UPDATE ke_modulos SET kmo_status ='" +activoModulo+ "' WHERE ked_codigo = '"+codigoEmp +"' AND kmo_codigo = '" + codigoModulo +"' AND kee_sucursal ='" + codigoSuc +  "'");

                            /*  }else{*/
                            val guardarPermisos = ContentValues()
                            guardarPermisos.put("ked_codigo", codigoEmp)
                            guardarPermisos.put("kmo_codigo", codigoModulo)
                            guardarPermisos.put("kmo_status", activoModulo)
                            guardarPermisos.put("kee_sucursal", codigoSuc)
                            keAndroid.insert("ke_modulos", null, guardarPermisos)
                            //  }
                        }
                        keAndroid.setTransactionSuccessful()
                        keAndroid.endTransaction()
                        if (enlace != "") {
                            //si existe la empresa, muestro los campos
                            binding.txtUsuario.visibility = View.VISIBLE
                            binding.txtPassword.visibility = View.VISIBLE
                            binding.btIniciar.visibility = View.VISIBLE
                            //los activo
                            binding.txtUsuario.isEnabled = true
                            binding.txtPassword.isEnabled = true
                            binding.btIniciar.isEnabled = true
                        } else {
                            //de lo contrario, los escondo
                            binding.txtUsuario.visibility = View.INVISIBLE
                            binding.txtPassword.visibility = View.INVISIBLE
                            binding.btIniciar.visibility = View.INVISIBLE
                            //los activo
                            binding.txtUsuario.isEnabled = false
                            binding.txtPassword.isEnabled = false
                            binding.btIniciar.isEnabled = false
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(
                            this@MainActivity,
                            "Error en la validacion",
                            Toast.LENGTH_LONG
                        ).show()
                        keAndroid.endTransaction()
                    }
                }
            }, Response.ErrorListener { error: VolleyError ->
                println("--Error--")
                error.printStackTrace()
            }) {
                override fun getParams(): Map<String, String> {  //finalmente, estos son los parametros que le enviaremos al webservice, partiendo de las variables
                    //donde estan guardados el usuario y password.
                    //parametros.put("version_usuario", versionApp);
                    return HashMap()
                }
            }
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(jsonArrayRequest) //esto es el request que se envia al url a traves de la conexion volley, (el stringrequest esta armado arriba)
    }*/

    private fun setListener() {
        binding.btValidarempresa.setOnClickListener { validarEmpresa(binding.txtEmpresa.text.toString()) }
        binding.btIniciar.setOnClickListener { ingresarEmpresa() }
    }

    private fun ingresarEmpresa() {
        val user = binding.txtUsuario.text.toString()
        val pass = binding.txtPassword.text.toString()

        if (newEmpresa.kedCodigo.isEmpty()) {
            toast("Falta codigo de la empresa")
            return
        }

        if (user.isEmpty()) {
            toast("Falta el usuario de vendedor")
            return
        }

        if (pass.isEmpty()) {
            toast("Falta la contraseña")
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
                    0 //<-----Variable de logueo previo, 1 = Esta logueado, 0 = No esta nadie logueado
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

                var jsonObject: JSONObject //creamos un objeto json vacio
                for (i in 0 until response.length()) { /*pongo todo en el objeto segun lo que venga */
                    try {
                        jsonObject = response.getJSONObject(i)
                        nUsuario = jsonObject.getString("nombre") //el nombre del vendedor
                        codUsuario = jsonObject.getString("vendedor") //el codigo
                        nombreUsuario =
                            jsonObject.getString("username") //almacenamos el nombre de usuario
                        almacen = jsonObject.getString("almacen").trim { it <= ' ' }
                        desactivo =
                            jsonObject.getDouble("desactivo") //este campo nos indicara si el usuario se encuentra bloqueado o no.
                        fechamodifi = jsonObject.getString("fechamodifi").trim { it <= ' ' }
                        ualterprec = jsonObject.getDouble("ualterprec")
                        ultimoped =
                            jsonObject.getString("correlativo") //obtenemos el ultimo correlativo
                        sesionactiva =
                            jsonObject.getString("sesionactiva") //traemos la fecha de la sesion que estamos iniciando.
                        superves = jsonObject.getString("superves")
                        vendedor = jsonObject.getString("vendedor").trim { it <= ' ' }
                        ultimorec = jsonObject.getString("recibocobro").trim { it <= ' ' }
                        ultimorcl =
                            jsonObject.getString("correlativoreclamo").trim { it <= ' ' }
                        ultimorcxc =
                            jsonObject.getString("correlativoprecobranza").trim { it <= ' ' }
                        sesion = jsonObject.getInt("sesion")
                    } catch (e: JSONException) {
                        println("--Error--")
                        e.printStackTrace()
                        println("--Error--")
                        Toast.makeText(this, "No se logro ingresar", Toast.LENGTH_LONG).show()
                    }
                }
                if (codUsuario.isEmpty()) {
                    println("LLEGO AQUI $codUsuario")
                    Toast.makeText(
                        this,
                        "Usuario o password incorrecto",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    if (sesion == 1) {
                        Toast.makeText(
                            this,
                            "Previamente Logueado",
                            Toast.LENGTH_LONG
                        ).show()
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
                            //preparacion e inserción del correlativo de pedidos
                            val correlativoTexto = MainActivity.right(ultimoped, 4)
                            var nroCorrelativo = correlativoTexto.toInt()
                            nroCorrelativo += 1
                            val insertar = ContentValues()
                            insertar.put("kco_numero", nroCorrelativo)
                            insertar.put(
                                "kco_vendedor",
                                codUsuario.trim { it <= ' ' })
                            insertar.put("empresa", newEmpresa.kedCodigo)
                            keAndroid.insert("ke_correla", null, insertar)
                            //--------------------------------------------------------------------

                            //preparacion e inserción del correlativo de recibos
                            val reciboTexto = MainActivity.right(ultimorec, 4)
                            var nroRecibo = reciboTexto.toInt()
                            nroRecibo += 1
                            val insertarRec = ContentValues()
                            insertarRec.put("kcc_numero", nroRecibo)
                            insertarRec.put(
                                "kcc_vendedor",
                                codUsuario.trim { it <= ' ' })
                            insertarRec.put("empresa", newEmpresa.kedCodigo)
                            keAndroid.insert("ke_correlacxc", null, insertarRec)
                            //-------------------------------------------------------

                            //preparacion e inserción del correlativo de reclamos
                            val reclamoTexto = MainActivity.right(ultimorcl, 4)
                            var nroReclamo = reclamoTexto.toInt()
                            nroReclamo += 1
                            val insertarRcl = ContentValues()
                            insertarRcl.put("kdev_numero", nroReclamo)
                            insertarRcl.put(
                                "kdev_vendedor",
                                codUsuario.trim { it <= ' ' })
                            insertarRcl.put("empresa", newEmpresa.kedCodigo)
                            keAndroid.insert("ke_correladev", null, insertarRcl)
                            //---------------------------------------------------------------------------

                            //preparacion e inserción del correlativo de precobranza
                            val correlaCXC = MainActivity.right(ultimorcxc, 4)
                            var nroCXC = correlaCXC.toInt()
                            nroCXC += 1
                            val insertarCXC = ContentValues()
                            insertarCXC.put("kcor_numero", nroCXC)
                            insertarCXC.put(
                                "kcor_vendedor",
                                codUsuario.trim { it <= ' ' })
                            insertarCXC.put("empresa", newEmpresa.kedCodigo)
                            keAndroid.insert("ke_corprec", null, insertarCXC)
                            //---------------------------------------------------------------------------
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

                            //agrego la fecha en la cual inició sesión
                            val hoy = Calendar.getInstance().time
                            val sdf =
                                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
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
                                this,
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

                        iraPrincipal()

                    } else if (desactivo == 2.0) {
                        Toast.makeText(
                            this,
                            "Este usuario se encuentra desactivado",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } else {
                Toast.makeText(
                    this,
                    "Usuario o password incorrecto",
                    Toast.LENGTH_LONG
                ).show()
            }
        }, { error ->
            println("--Error--")
            error.printStackTrace()
            println("--Error--")
            Toast.makeText(this, "No se logró el inicio de sesión", Toast.LENGTH_LONG)
                .show()
        })

        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(jsonArrayRequest)
    }

    private fun validarEmpresa(codigoEmpresa: String) {

        if (binding.txtEmpresa.text.isEmpty()) {
            toast("Falta codigo de la empresa")
            return
        }

        val url = "https://www.cloccidental.com/webservice/validarempresa.php?codigo=$codigoEmpresa"
        val keAndroid = conn.writableDatabase
        val jsonArrayRequest = JsonArrayRequest(url, { response ->
            if (response != null) {
                val jsonObject: JSONObject //creamos un objeto json vacio
                keAndroid.beginTransaction()

                try {
                    //bajamos los datos de la empresa/sucursal
                    jsonObject = response.getJSONObject(0)

                    val codigoEmp = jsonObject.getString("codigoEmpresa")
                    val enlace = jsonObject.getString("enlaceEmpresa")
                    val nombreEmp = jsonObject.getString("nombreEmpresa")
                    val statusEmp = jsonObject.getString("statusEmpresa")
                    val codigoSuc = jsonObject.getString("agenciaEmpresa")

                    newEmpresa = keDataconex(
                        codigoEmp,
                        nombreEmp,
                        statusEmp,
                        enlace,
                        codigoSuc
                    )

                    //en este proceso vamos a cargar los permisos
                    var permisosJson: JSONObject
                    for (i in 0 until response.length()) {
                        permisosJson = response.getJSONObject(i)
                        val codigoModulo = permisosJson.getString("codigoModulo")
                        //System.out.println("CODIGO DEL MODULO " + codigoModulo);
                        val activoModulo = permisosJson.getString("estadoModulo")

                        val guardarPermisos = ContentValues()
                        guardarPermisos.put("ked_codigo", codigoEmp)
                        guardarPermisos.put("kmo_codigo", codigoModulo)
                        guardarPermisos.put("kmo_status", activoModulo)
                        guardarPermisos.put("kee_sucursal", codigoSuc)
                        guardarPermisos.put("empresa", newEmpresa.kedCodigo)
                        keAndroid.insert("ke_modulos", null, guardarPermisos)
                    }
                    keAndroid.setTransactionSuccessful()
                    keAndroid.endTransaction()
                    if (enlace != "") {
                        binding.apply {
                            txtUsuario.isEnabled = true
                            txtPassword.isEnabled = true
                            btIniciar.isEnabled = true

                            txtUsuario.visibility = View.VISIBLE
                            txtPassword.visibility = View.VISIBLE
                            btIniciar.visibility = View.VISIBLE
                        }
                    } else {
                        binding.apply {
                            txtUsuario.isEnabled = true
                            txtPassword.isEnabled = true
                            btIniciar.isEnabled = true

                            txtUsuario.visibility = View.INVISIBLE
                            txtPassword.visibility = View.INVISIBLE
                            btIniciar.visibility = View.INVISIBLE
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(
                        this,
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
                this,
                "No se pudo validar el codigo, intente más tarde",
                Toast.LENGTH_LONG
            ).show()
        })

        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(jsonArrayRequest)
    }

    fun obtenerVersion(url: String?) {
        val jsonArrayRequest: JsonArrayRequest =
            object : JsonArrayRequest(url, Response.Listener { response: JSONArray? ->
                if (response != null) {
                    println("llego aqui")
                    val jsonObject: JSONObject //creamos un objeto json vacio
                    try {
                        jsonObject = response.getJSONObject(0)
                        println("contenido del json object$jsonObject")
                        versionNube = jsonObject.getString("kve_version").trim { it <= ' ' }
                        println("version en nube $versionNube")
                        caducidad = jsonObject.getString("kve_activa")
                        if (versionNube != versionApp) {
                            Toast.makeText(
                                this@MainActivity,
                                "Esta versión se encuentra obsoleta, por favor, actualice",
                                Toast.LENGTH_LONG
                            ).show()
                            binding.btIniciar.isEnabled = false
                        } else {
                            if (caducidad == "0") {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Esta versión se encuentra obsoleta, por favor, actualice",
                                    Toast.LENGTH_LONG
                                ).show()
                                binding.btIniciar.isEnabled = false
                            } else if (caducidad == "1") {
                                //TODO EN ORDEN, NO TO CAMOS NADA.
                            }
                        }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        Toast.makeText(
                            this@MainActivity,
                            "Esta versión se encuentra obsoleta, por favor, actualice",
                            Toast.LENGTH_LONG
                        ).show()
                        binding.btIniciar.isEnabled = false
                    }
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Esta versión se encuentra obsoleta, por favor, actualice",
                        Toast.LENGTH_LONG
                    ).show()
                    binding.btIniciar.isEnabled = false
                }
            }, Response.ErrorListener { error: VolleyError ->
                println("--Error--")
                error.printStackTrace()
                println("--Error--")
            }) {
                override fun getParams(): Map<String, String> {  //finalmente, estos son los parametros que le enviaremos al webservice, partiendo de las variables
                    //donde estan guardados el usuario y password.
                    //parametros.put("version_usuario", versionApp);
                    return HashMap()
                }
            }
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(jsonArrayRequest) //esto es el request que se envia al url a traves de la conexion volley, (el stringrequest esta armado arriba)
    }

    //metodo de validacion
    /*private fun validarUsuario(url: String) {
        println(url)
        val jsonArrayRequest: JsonArrayRequest = object : JsonArrayRequest(
            url,
            Response.Listener { response: JSONArray? ->  //a traves de un json array request, traemos la informacion que viene del usuario
                if (response != null) { // si la respuesta no viene vacia
                    var sesion =
                        0 //<-----Variable de logueo previo, 1 = Esta logueado, 0 = No esta nadie logueado
                    println(response)
                    var jsonObject: JSONObject //creamos un objeto json vacio
                    for (i in 0 until response.length()) { *//*pongo todo en el objeto segun lo que venga *//*
                        try {
                            conn.deleteAll("usuarios")
                            jsonObject = response.getJSONObject(i)
                            n_usuario = jsonObject.getString("nombre") //el nombre del vendedor
                            cod_usuario = jsonObject.getString("vendedor") //el codigo
                            nombre_usuario =
                                jsonObject.getString("username") //almacenamos el nombre de usuario
                            almacen = jsonObject.getString("almacen").trim { it <= ' ' }
                            desactivo =
                                jsonObject.getDouble("desactivo") //este campo nos indicara si el usuario se encuentra bloqueado o no.
                            fechamodifi = jsonObject.getString("fechamodifi").trim { it <= ' ' }
                            ualterprec = jsonObject.getDouble("ualterprec")
                            ultimoped =
                                jsonObject.getString("correlativo") //obtenemos el ultimo correlativo
                            sesionactiva =
                                jsonObject.getString("sesionactiva") //traemos la fecha de la sesion que estamos iniciando.
                            superves = jsonObject.getString("superves")
                            vendedor = jsonObject.getString("vendedor").trim { it <= ' ' }
                            ultimorec = jsonObject.getString("recibocobro").trim { it <= ' ' }
                            ultimorcl =
                                jsonObject.getString("correlativoreclamo").trim { it <= ' ' }
                            ultimorcxc =
                                jsonObject.getString("correlativoprecobranza").trim { it <= ' ' }
                            sesion = jsonObject.getInt("sesion")
                        } catch (e: JSONException) {
                            Toast.makeText(applicationContext, e.message, Toast.LENGTH_LONG).show()
                        }
                    }
                    if (cod_usuario!!.isEmpty()) {
                        println("LLEGO AQUI$cod_usuario")
                        Toast.makeText(
                            this@MainActivity,
                            "Usuario o password incorrecto",
                            Toast.LENGTH_LONG
                        ).show()
                        binding.btIniciar.isEnabled = true
                        binding.btIniciar.text = "Iniciar Sesión"
                    } else {
                        if (sesion == 1) {
                            Toast.makeText(
                                this@MainActivity,
                                "Previamente Logueado",
                                Toast.LENGTH_LONG
                            ).show()
                            binding.btIniciar.isEnabled = true
                            binding.btIniciar.text = "Iniciar Sesión"
                            return@Listener
                        }
                        if (desactivo == 0.0 || desactivo == 1.0) {
                            if (ultimoped!!.isEmpty()) {
                                ultimoped = "0000"
                            }
                            if (ultimorec!!.isEmpty()) {
                                ultimorec = "0000"
                            }
                            if (ultimorcl!!.isEmpty()) {
                                ultimorcl = "0000"
                            }
                            if (ultimorcxc!!.isEmpty()) {
                                ultimorcxc = "0000"
                            }
                            val keAndroid = conn.writableDatabase
                            keAndroid.beginTransaction()
                            try {
                                //preparacion e inserción del correlativo de pedidos
                                val correlativoTexto = right(ultimoped, 4)
                                var nroCorrelativo = correlativoTexto.toInt()
                                nroCorrelativo += 1
                                val insertar = ContentValues()
                                insertar.put("kco_numero", nroCorrelativo)
                                insertar.put("kco_vendedor", cod_usuario!!.trim { it <= ' ' })
                                keAndroid.insert("ke_correla", null, insertar)
                                //--------------------------------------------------------------------

                                //preparacion e inserción del correlativo de recibos
                                val reciboTexto = right(ultimorec, 4)
                                var nroRecibo = reciboTexto.toInt()
                                nroRecibo += 1
                                val insertarRec = ContentValues()
                                insertarRec.put("kcc_numero", nroRecibo)
                                insertarRec.put("kcc_vendedor", cod_usuario!!.trim { it <= ' ' })
                                keAndroid.insert("ke_correlacxc", null, insertarRec)
                                //-------------------------------------------------------

                                //preparacion e inserción del correlativo de reclamos
                                val reclamoTexto = right(ultimorcl, 4)
                                var nroReclamo = reclamoTexto.toInt()
                                nroReclamo += 1
                                val insertarRcl = ContentValues()
                                insertarRcl.put("kdev_numero", nroReclamo)
                                insertarRcl.put("kdev_vendedor", cod_usuario!!.trim { it <= ' ' })
                                keAndroid.insert("ke_correladev", null, insertarRcl)
                                //---------------------------------------------------------------------------

                                //preparacion e inserción del correlativo de precobranza
                                val correlaCXC = right(ultimorcxc, 4)
                                var nroCXC = correlaCXC.toInt()
                                nroCXC += 1
                                val insertarCXC = ContentValues()
                                insertarCXC.put("kcor_numero", nroCXC)
                                insertarCXC.put("kcor_vendedor", cod_usuario!!.trim { it <= ' ' })
                                keAndroid.insert("ke_corprec", null, insertarCXC)
                                //---------------------------------------------------------------------------
                                keAndroid.delete("usuarios", "username = ?", arrayOf(nick_usuario))

                                //agrego la fecha en la cual inició sesión
                                val hoy = Calendar.getInstance().time
                                val sdf =
                                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                                val fechaSync = sdf.format(hoy)
                                val usuarioDatos = ContentValues()
                                usuarioDatos.put("nombre", n_usuario)
                                usuarioDatos.put("username", nombre_usuario)
                                usuarioDatos.put("password", pass_usuario)
                                usuarioDatos.put("vendedor", vendedor)
                                usuarioDatos.put("almacen", almacen)
                                usuarioDatos.put("desactivo", desactivo)
                                usuarioDatos.put("fechamodifi", fechaSync)
                                usuarioDatos.put("ualterprec", ualterprec)
                                usuarioDatos.put("sesionactiva", sesionactiva)
                                usuarioDatos.put("superves", superves)
                                keAndroid.insert("usuarios", null, usuarioDatos)
                                keAndroid.setTransactionSuccessful()
                            } catch (e: Exception) {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Error insertando correlativo $e",
                                    Toast.LENGTH_LONG
                                ).show()
                                keAndroid.endTransaction()
                            } finally {
                                keAndroid.endTransaction()
                            }
                            objetoAux.login(cod_usuario!!, 1)
                            iraPrincipal()
                            val editor = preferences.edit()
                            editor.putString("nick_usuario", nick_usuario)
                            editor.putString("cod_usuario", cod_usuario)
                            editor.putString("nombre_usuario", n_usuario)
                            editor.putString("superves", superves)
                            editor.putString("codigoEmpresa", codigoEmp)
                            editor.putString("codigoSucursal", codigoSuc)
                            editor.apply()
                        } else if (desactivo == 2.0) {
                            Toast.makeText(
                                this@MainActivity,
                                "Este usuario se encuentra desactivado",
                                Toast.LENGTH_LONG
                            ).show()
                            binding.btIniciar.isEnabled = true
                            binding.btIniciar.text = "Iniciar Sesión"
                        }
                    }
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Usuario o password incorrecto",
                        Toast.LENGTH_LONG
                    ).show()
                    binding.btIniciar.isEnabled = true
                    binding.btIniciar.text = "Iniciar Sesión"
                }
            },
            Response.ErrorListener { error: VolleyError ->
                error.printStackTrace()
                binding.btIniciar.isEnabled = true
                binding.btIniciar.text = "Iniciar Sesión"
                Toast.makeText(applicationContext, "Error en inicio de sesión ", Toast.LENGTH_LONG)
                    .show()
            }) {
            override fun getParams(): Map<String, String> {  //finalmente, estos son los parametros que le enviaremos al webservice, partiendo de las variables
                //donde estan guardados el usuario y password.
                val parametros: MutableMap<String, String> = HashMap()
                parametros["nick_usuario"] = nick_usuario!!
                parametros["pass_usuario"] = pass_usuario!!
                return parametros
            }
        }
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(jsonArrayRequest) //esto es el request que se envia al url a traves de la conexion volley, (el stringrequest esta armado arriba)
    }*/

    /*private void login() {
        String URL = "https://8135-45-186-202-166.ngrok.io/login";

        JSONObject Login = new JSONObject();
        JSONObject subLogin = new JSONObject();

        try {
            subLogin.put("vendedor", cod_usuario);
            subLogin.put("sesion", 1);

            Login.put("Login", subLogin);
        } catch (Exception e) {
            e.printStackTrace();
        }


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URL, Login, response -> {

            if (response != null) {
                try {
                    JSONObject jsonObject = response.getJSONObject("estado");
                    if (jsonObject.getString("status").equals("404")) {
                        Toast.makeText(this, "Error 404", LENGTH_LONG).show();
                    } else if ((jsonObject.getString("status").equals("200")) && (!jsonObject.getString("usuario").equals(cod_usuario))) {
                        Toast.makeText(this, "No se pudo iniciar sesión adecuadamente", LENGTH_LONG).show();
                    } else if ((jsonObject.getString("status").equals("200")) && (jsonObject.getString("usuario").equals(cod_usuario))) {
                        Toast.makeText(this, "Sesión Iniciada", LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }, error -> {
            Toast.makeText(this, "No se pudo iniciar sesión adecuadamente", LENGTH_LONG).show();
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }*/
    private fun validarSesion() {
        nick_usuario = preferences.getString("nick_usuario", null)
        cod_usuario = preferences.getString("cod_usuario", null)
        codEmpresa = preferences.getString("codigoEmpresa", null).toString()
        desactivo = conn.getCampoDoubleCamposVarios(
            "usuarios",
            "desactivo",
            listOf("vendedor", "empresa"),
            listOf(cod_usuario ?: "0", codEmpresa)
        )
        /*val keAndroid = conn.writableDatabase
        val cursor = keAndroid.rawQuery("SELECT desactivo FROM usuarios  WHERE 1", null)
        while (cursor.moveToNext()) {
            desactivo = cursor.getDouble(0)
            println(desactivo)
        }
        cursor.close()*/
        if (nick_usuario != null && desactivo != 2.0) {
            iraPrincipal()
        }
    }

    private fun iraPrincipal() {
        val intent = Intent(
            applicationContext,
            PrincipalActivity::class.java
        ) // creamos intent hacia la clase principal
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent) //y lo iniciamos
    }

    override fun onResume() {
        codigo_empresa = "081196"
        super.onResume()
        //validarEmpresaLicencia("https://www.cloccidental.com/webservice/validarempresa.php?codigo=$codigo_empresa")

        /*appUpdateManager
                .getAppUpdateInfo()
                .addOnSuccessListener(
                        appUpdateInfo -> {
                            if (appUpdateInfo.updateAvailability()
                            == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS){
                                try {
                                    appUpdateManager.startUpdateFlowForResult(
                                            appUpdateInfo,
                                            AppUpdateType.IMMEDIATE,
                                            this,
                                            MY_REQUEST_CODE
                                    );
                                } catch (IntentSender.SendIntentException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                );*/
    }

    private fun checkForAppUpdate() {
        appUpdateManager = AppUpdateManagerFactory.create(this)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo: AppUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {

                /*try {
                    appUpdateManager.startUpdateFlowForResult(
                            // Pass the intent that is returned by 'getAppUpdateInfo()'.
                            appUpdateInfo,

                            AppUpdateType.IMMEDIATE,

                            this,

                            MY_REQUEST_CODE);
                } catch (IntentSender.SendIntentException e) {
                    throw new RuntimeException(e);
                }*/
                try {
                    appUpdateManager.startUpdateFlowForResult( // Pass the intent that is returned by 'getAppUpdateInfo()'.
                        appUpdateInfo,  // an activity result launcher registered via registerForActivityResult
                        AppUpdateType.IMMEDIATE,  // Or pass 'AppUpdateType.FLEXIBLE' to newBuilder() for
                        // flexible updates.
                        this,
                        MY_REQUEST_CODE
                    )
                } catch (e: SendIntentException) {
                    throw RuntimeException(e)
                }
            } else {
                validarSesion()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MY_REQUEST_CODE) {
            when (resultCode) {
                RESULT_OK -> {
                    Toast.makeText(this, "Actualización Exitosa!", Toast.LENGTH_SHORT).show()
                }
                RESULT_CANCELED -> {
                    Toast.makeText(this, "Actualización Cancelada", Toast.LENGTH_SHORT).show()
                    finish()
                    exitProcess(0)
                }
                ActivityResult.RESULT_IN_APP_UPDATE_FAILED -> {
                    Toast.makeText(this, "Algo salio mal", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Toast.makeText(this, "Algo salio mal, sin datos", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        var nick_usuario: String? = null
        var nick_usuario_env: String? = null
        var codigo_empresa: String? = null
        var pass_usuario: String? = null
        var ultimorec: String? = null
        var n_usuario: String? = null
        var cod_usuario: String? = null
        var nombre_usuario: String? = null
        var ultimoped: String? = null
        var sesionactiva: String? = null
        var superves: String? = null
        var almacen: String? = null
        var fechamodifi: String? = null
        var ultimorcl: String? = null
        var ultimorcxc: String? = null
        var vendedor: String? = null
        var version: String? = null
        var caducidad: String? = null
        var versionNube: String? = null
        var enlace = ""
        var nombreEmp: String? = null
        var codigoEmp: String? = null
        var statusEmp: String? = null
        var codigoModulo: String? = null
        var activoModulo: String? = null
        var codigoSuc: String? = null
        var desactivo = 0.0
        var ualterprec: Double? = null
        var versionApp = "2.3.3"
        private const val MY_REQUEST_CODE = 100
        fun right(valor: String?, longitud: Int): String {
            //una función "right" utilizando la clase substring
            return valor!!.substring(valor.length - longitud)
        }
    }
}