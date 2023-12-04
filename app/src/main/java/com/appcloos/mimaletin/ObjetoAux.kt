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

    fun login(codUsuario: String, sesion: Int) {
        val url = "http://cloccidental.com:5001/login"

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
                        context, "No se pudo terminar sesión adecuadamente", Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        context, "No se pudo iniciar sesión adecuadamente", Toast.LENGTH_LONG
                    ).show()
                }

            }
        val requestQueue = Volley.newRequestQueue(context)
        requestQueue.add(jsonObjectRequest)
    }

    fun descargaDesactivo(codUsuario: String) {
        val url = "https://cloccidental.com/webservice/desactivo.php?cod_usuario=$codUsuario"

        val jsonObjectRequest =
            JsonObjectRequest(Request.Method.GET, url, null, { response: JSONObject? ->
                if (response != null &&
                    response.getString("desactivos") != "null" &&
                    response.getString("status") == "0"
                ) {
                    try {
                        val desactivos = response.getJSONArray("desactivos")
                        val jsonObject: JSONObject = desactivos.getJSONObject(0)

                        val vendedor = jsonObject.getString("vendedor")

                        val cv = ContentValues()
                        cv.put("desactivo", jsonObject.getString("desactivo"))

                        conn.updateJSON("usuarios", cv, "vendedor", vendedor)

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