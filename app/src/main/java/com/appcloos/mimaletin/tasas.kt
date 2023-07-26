package com.appcloos.mimaletin

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*


class tasas() {
    var id: String    = ""
    var fecha: String = ""
    var tasa: Double  = 0.00
    var fechayhora    = "0000-00-00 00:00:00"
    var fechamodifi   = "0000-00-00 00:00:00"
    var tasaib        = 0.00

}