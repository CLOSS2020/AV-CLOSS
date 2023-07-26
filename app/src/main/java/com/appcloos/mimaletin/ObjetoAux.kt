package com.appcloos.mimaletin

import android.content.Context
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject

class ObjetoAux(val context: Context) {

    fun login(codUsuario: String, sesion: Int) {
        val URL: String = "http://cloccidental.com:5001/login"

        val Login = JSONObject()
        val subLogin = JSONObject()
        try {
            subLogin.put("vendedor", codUsuario)
            subLogin.put("sesion", sesion)
            Login.put("Login", subLogin)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, URL, Login,
            { response: JSONObject? ->
                if (response != null) {
                    try {
                        val jsonObject = response.getJSONObject("estado")
                        if (jsonObject.getString("status") == "200" && jsonObject.getString("usuario") == codUsuario) {
                            if (sesion == 0) {
                                Toast.makeText(context, "Sesión Terminada", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Sesión Iniciada", Toast.LENGTH_LONG).show()
                            }

                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }
        ) { error: VolleyError? ->
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

}