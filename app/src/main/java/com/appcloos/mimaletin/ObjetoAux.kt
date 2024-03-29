package com.appcloos.mimaletin

import android.content.ContentValues
import android.content.Context
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject

class ObjetoAux(val context: Context) {
    private var conn: AdminSQLiteOpenHelper = AdminSQLiteOpenHelper(context, "ke_android", null)

    fun login(codUsuario: String, sesion: Int, enlaceEmpresa: String) {
        val url = "http://$enlaceEmpresa:5001/login"

        val login = JSONObject()
        val subLogin = JSONObject()
        try {
            subLogin.put("vendedor", codUsuario)
            subLogin.put("sesion", sesion)
            login.put("Login", subLogin)
        } catch (e: Exception) {
            println("--Error--")
            e.printStackTrace()
            println("--Error--")
        }
        val jsonObjectRequest =
            JsonObjectRequest(Request.Method.POST, url, login, { response: JSONObject? ->
                if (response != null) {
                    try {
                        val jsonObject = response.getJSONObject("estado")
                        if (jsonObject.getString("status") == "200" && jsonObject.getString("usuario") == codUsuario) {
                            if (sesion == 0) {
                                Toast.makeText(context, "Sesión Terminada", Toast.LENGTH_LONG)
                                    .show()
                            } else {
                                Toast.makeText(context, "Sesión Iniciada", Toast.LENGTH_LONG).show()
                            }
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }) { error: VolleyError? ->
                error?.printStackTrace()
                if (sesion == 0) {
                    Toast.makeText(
                        context,
                        "No se pudo terminar sesión adecuadamente",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        context,
                        "No se pudo iniciar sesión adecuadamente",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        val requestQueue = Volley.newRequestQueue(context)
        requestQueue.add(jsonObjectRequest)
    }

    fun descargaDesactivo(codUsuario: String, codEmpresa: String, enlaceEmpresa: String) {
        val user = conn.getCampoStringCamposVarios(
            "usuarios",
            "username",
            arrayListOf("vendedor", "empresa"),
            arrayListOf(codUsuario, codEmpresa)
        ).trim()

        val pass = conn.getCampoStringCamposVarios(
            "usuarios",
            "password",
            arrayListOf("vendedor", "empresa"),
            arrayListOf(codUsuario, codEmpresa)
        ).trim()

        // val url = "https://$enlaceEmpresa/webservice/desactivo.php?cod_usuario=$codUsuario"

        // 2024-02-23 Uso el Usuario y la Contraseña debido a que varios usuarios pueden compartir
        // el mismo codigo pero no es mismo usuario
        val url = "https://$enlaceEmpresa/webservice/desactivo_V2.php?nick_usuario=$user&pass_usuario=$pass"

        val jsonObjectRequest =
            JsonObjectRequest(Request.Method.GET, url, null, { response: JSONObject? ->
                if (response != null && response.getString("desactivos") != "null" && response.getString(
                        "status"
                    ) == "0"
                ) {
                    try {
                        val desactivos = response.getJSONArray("desactivos")
                        val jsonObject: JSONObject = desactivos.getJSONObject(0)

                        val vendedor = jsonObject.getString("vendedor")
                        val empresa = jsonObject.getString("empresa")

                        val cv = ContentValues()
                        cv.put("desactivo", jsonObject.getString("desactivo"))
                        cv.put("cierre_sesion", jsonObject.getBoolean("cierre_sesion"))
                        cv.put("empresa", empresa) // <- uso el del api por si acaso

                        conn.updateJSONCamposVarios(
                            "usuarios",
                            cv,
                            "vendedor = ? AND empresa = ?",
                            arrayOf(vendedor, empresa)
                        )

                        println("Si se logro descargar desactivo")
                        println(jsonObject.getString("desactivo"))
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        println("No se logro descargar desactivo")
                    }
                } else {
                    println("No se logro descargar desactivo")
                    println("Response --> $response")
                }
            }) { error: VolleyError? ->
                error?.printStackTrace()
            }
        val requestQueue = Volley.newRequestQueue(context)
        requestQueue.add(jsonObjectRequest)
    }
}
