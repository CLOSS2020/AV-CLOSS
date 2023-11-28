package com.appcloos.mimaletin.sincronizar

import com.appcloos.mimaletin.sincronizar.dataClass.articulos.ArticulosResponse
import com.appcloos.mimaletin.sincronizar.dataClass.clientes.ClientesResponse
import com.appcloos.mimaletin.sincronizar.dataClass.documentos.DocumentosResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("articulos_V26.php?")
    suspend fun getArticulos(@Query("fecha_sinc") fechaSinc: String): Response<ArticulosResponse>

    @GET("planificador_V3.php?")
    suspend fun getDocumentos(@Query("vendedor") vendedor: String): Response<DocumentosResponse>

    @GET("clientes_V4.php?")
    suspend fun getClientes(@Query("vendedor") vendedor: String): Response<ClientesResponse>
}