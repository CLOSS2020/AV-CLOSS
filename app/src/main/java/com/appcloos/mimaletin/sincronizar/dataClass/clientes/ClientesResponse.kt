package com.appcloos.mimaletin.sincronizar.dataClass.clientes

import com.google.gson.annotations.SerializedName

data class ClientesResponse(

	@field:SerializedName("clientes")
	val clientes: List<ClientesItem?>? = null,

	@field:SerializedName("status")
	val status: String? = null
)

data class ClientesItem(

	@field:SerializedName("codigo")
	val codigo: String? = null,

	@field:SerializedName("vendedor")
	val vendedor: String? = null,

	@field:SerializedName("direccion")
	val direccion: String? = null,

	@field:SerializedName("perscont")
	val perscont: String? = null,

	@field:SerializedName("nombre")
	val nombre: String? = null,

	@field:SerializedName("kne_activa")
	val kneActiva: String? = null,

	@field:SerializedName("kne_mtomin")
	val kneMtomin: String? = null,

	@field:SerializedName("precio")
	val precio: String? = null,

	@field:SerializedName("contribespecial")
	val contribespecial: String? = null,

	@field:SerializedName("noeminota")
	val noeminota: String? = null,

	@field:SerializedName("fechamodifi")
	val fechamodifi: String? = null,

	@field:SerializedName("noemifac")
	val noemifac: String? = null,

	@field:SerializedName("telefonos")
	val telefonos: String? = null,

	@field:SerializedName("subcodigo")
	val subcodigo: String? = null,

	@field:SerializedName("sector")
	val sector: String? = null,

	@field:SerializedName("email")
	val email: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)
