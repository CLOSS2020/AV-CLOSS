package com.appcloos.mimaletin

import android.app.Application
import android.content.SharedPreferences
import android.widget.Toast

class AVCLOSS : Application() {
    private lateinit var preferences: SharedPreferences
    var codEmpresa: String? = null
    override fun onCreate() {
        super.onCreate()
        preferences = getSharedPreferences("Preferences", MODE_PRIVATE)
        codEmpresa = preferences.getString("codigoEmpresa", null)
        Constantes.AGENCIA = codEmpresa ?: "081196"
    }
}
