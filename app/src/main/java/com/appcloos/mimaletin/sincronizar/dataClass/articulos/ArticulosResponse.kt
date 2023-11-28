package com.appcloos.mimaletin.sincronizar.dataClass.articulos

import com.google.gson.annotations.SerializedName

data class ArticulosResponse(

	@field:SerializedName("articulo")
	val articulo: List<ArticuloItem?>? = null,

	@field:SerializedName("status")
	val status: String? = null
)

data class ArticuloItem(

	@field:SerializedName("enpreventa")
	val enpreventa: String? = null,

	@field:SerializedName("vta_solofac")
	val vtaSolofac: String? = null,

	@field:SerializedName("grupo")
	val grupo: String? = null,

	@field:SerializedName("nombre")
	val nombre: String? = null,

	@field:SerializedName("marca")
	val marca: String? = null,

	@field:SerializedName("existencia")
	val existencia: String? = null,

	@field:SerializedName("subgrupo")
	val subgrupo: String? = null,

	@field:SerializedName("comprometido")
	val comprometido: String? = null,

	@field:SerializedName("vta_solone")
	val vtaSolone: String? = null,

	@field:SerializedName("vta_max")
	val vtaMax: String? = null,

	@field:SerializedName("discont")
	val discont: String? = null,

	@field:SerializedName("vta_minenx")
	val vtaMinenx: String? = null,

	@field:SerializedName("codigo")
	val codigo: String? = null,

	@field:SerializedName("vta_min")
	val vtaMin: String? = null,

	@field:SerializedName("dctotope")
	val dctotope: String? = null,

	@field:SerializedName("precio1")
	val precio1: String? = null,

	@field:SerializedName("precio2")
	val precio2: String? = null,

	@field:SerializedName("precio3")
	val precio3: String? = null,

	@field:SerializedName("precio4")
	val precio4: String? = null,

	@field:SerializedName("precio5")
	val precio5: String? = null,

	@field:SerializedName("precio6")
	val precio6: String? = null,

	@field:SerializedName("unidad")
	val unidad: String? = null,

	@field:SerializedName("precio7")
	val precio7: String? = null,

	@field:SerializedName("fechamodifi")
	val fechamodifi: String? = null,

	@field:SerializedName("referencia")
	val referencia: String? = null
)
