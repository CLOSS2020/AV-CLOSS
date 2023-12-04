package com.appcloos.mimaletin.domain

import android.content.Context
import com.appcloos.mimaletin.AdminSQLiteOpenHelper
import com.appcloos.mimaletin.R


class ClienteAlertauseCase {

    fun comparar(codCliente: String, context: Context): Boolean {
        val conn = AdminSQLiteOpenHelper(context, "ke_android", null)
        val cliente = conn.getCliente(codCliente)

        return (cliente.diasultvta > cliente.promdiasvta) ||
                (cliente.prcdpagdia < 50.0) ||
                (cliente.riesgocrd > 10.0) ||
                (cliente.diasultvta > 40.0) ||
                (cliente.email.isEmpty()) ||
                (cliente.perscont.isEmpty()) ||
                (cliente.telefonos.isEmpty())
    }

    fun compararIcon(codCliente: String, context: Context): Int {
        val conn = AdminSQLiteOpenHelper(context, "ke_android", null)
        val cliente = conn.getCliente(codCliente)

        return if ((cliente.diasultvta > cliente.promdiasvta) ||
            (cliente.prcdpagdia < 50.0) ||
            (cliente.riesgocrd > 10.0) ||
            (cliente.diasultvta > 40.0)
        ) {
            R.color.errorColor
        } else {
            R.color.warningColor
        }

    }

}