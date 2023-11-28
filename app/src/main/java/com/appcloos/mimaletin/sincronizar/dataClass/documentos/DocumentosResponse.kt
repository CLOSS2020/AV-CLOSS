package com.appcloos.mimaletin.sincronizar.dataClass.documentos

import com.google.gson.annotations.SerializedName

data class DocumentosResponse(

	@field:SerializedName("documento")
	val documento: List<DocumentoItem?>? = null,

	@field:SerializedName("status")
	val status: String? = null
)

data class DocumentoItem(

	@field:SerializedName("dretencion")
	val dretencion: String? = null,

	@field:SerializedName("cdret")
	val cdret: String? = null,

	@field:SerializedName("dtotpagos")
	val dtotpagos: String? = null,

	@field:SerializedName("dtotdescuen")
	val dtotdescuen: String? = null,

	@field:SerializedName("kti_negesp")
	val ktiNegesp: String? = null,

	@field:SerializedName("tasadoc")
	val tasadoc: String? = null,

	@field:SerializedName("documento")
	val documento: String? = null,

	@field:SerializedName("dtotimpuest")
	val dtotimpuest: String? = null,

	@field:SerializedName("recepcion")
	val recepcion: String? = null,

	@field:SerializedName("bsmtoiva")
	val bsmtoiva: String? = null,

	@field:SerializedName("retmun_mto")
	val retmunMto: String? = null,

	@field:SerializedName("cbsrparme")
	val cbsrparme: String? = null,

	@field:SerializedName("cdrparme")
	val cdrparme: String? = null,

	@field:SerializedName("agencia")
	val agencia: String? = null,

	@field:SerializedName("codcoord")
	val codcoord: String? = null,

	@field:SerializedName("cdretflete")
	val cdretflete: String? = null,

	@field:SerializedName("codcliente")
	val codcliente: String? = null,

	@field:SerializedName("bsretencioniva")
	val bsretencioniva: String? = null,

	@field:SerializedName("tipodocv")
	val tipodocv: String? = null,

	@field:SerializedName("estatusdoc")
	val estatusdoc: String? = null,

	@field:SerializedName("tipodoc")
	val tipodoc: String? = null,

	@field:SerializedName("dretencioniva")
	val dretencioniva: String? = null,

	@field:SerializedName("fchvencedcto")
	val fchvencedcto: String? = null,

	@field:SerializedName("aceptadev")
	val aceptadev: String? = null,

	@field:SerializedName("bsretencion")
	val bsretencion: String? = null,

	@field:SerializedName("ruta_parme")
	val rutaParme: String? = null,

	@field:SerializedName("vendedor")
	val vendedor: String? = null,

	@field:SerializedName("cbsretiva")
	val cbsretiva: String? = null,

	@field:SerializedName("emision")
	val emision: String? = null,

	@field:SerializedName("cdretiva")
	val cdretiva: String? = null,

	@field:SerializedName("mtodcto")
	val mtodcto: String? = null,

	@field:SerializedName("tienedcto")
	val tienedcto: String? = null,

	@field:SerializedName("cbsret")
	val cbsret: String? = null,

	@field:SerializedName("diascred")
	val diascred: String? = null,

	@field:SerializedName("dvndmtototal")
	val dvndmtototal: String? = null,

	@field:SerializedName("cbsretflete")
	val cbsretflete: String? = null,

	@field:SerializedName("contribesp")
	val contribesp: String? = null,

	@field:SerializedName("vence")
	val vence: String? = null,

	@field:SerializedName("dtotneto")
	val dtotneto: String? = null,

	@field:SerializedName("bsflete")
	val bsflete: String? = null,

	@field:SerializedName("dtotdev")
	val dtotdev: String? = null,

	@field:SerializedName("nombrecli")
	val nombrecli: String? = null,

	@field:SerializedName("fechamodifi")
	val fechamodifi: String? = null,

	@field:SerializedName("bsmtofte")
	val bsmtofte: String? = null,

	@field:SerializedName("bsiva")
	val bsiva: String? = null,

	@field:SerializedName("dFlete")
	val dFlete: String? = null,

	@field:SerializedName("tipoprecio")
	val tipoprecio: String? = null,

	@field:SerializedName("dtotalfinal")
	val dtotalfinal: String? = null
)
