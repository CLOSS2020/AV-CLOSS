package com.appcloos.mimaletin

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import android.os.Environment
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.graphics.createBitmap
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class DialogRecibo {

    lateinit var aceptar: Button
    lateinit var monto: TextView
    lateinit var fecha: TextView
    lateinit var id: TextView
    lateinit var moneda: TextView
    lateinit var banco: TextView
    lateinit var referencia: TextView
    lateinit var cliente: TextView
    private lateinit var rvDocs: RecyclerView
    val adapter: SimpleDocsAdapter = SimpleDocsAdapter()

    //var myBitmap: Bitmap? = null
    var myBitmap: Bitmap = createBitmap(1000, 1000)

    var nombanco = ""
    var nomcliente = ""

    private lateinit var conn: AdminSQLiteOpenHelper
    private lateinit var keAndroid: SQLiteDatabase


    fun DialogRecibo(contexto: Context, datos: ArrayList<CXC>) {
        conn = AdminSQLiteOpenHelper(contexto, "ke_android", null, 19)
        keAndroid = conn.writableDatabase


        //conf basica del dialogo
        val dialogo = Dialog(contexto, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialogo.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogo.setCancelable(false)
        dialogo.window?.setBackgroundDrawable(ColorDrawable(Color.GRAY))
        dialogo.setContentView(R.layout.dialog_recibo_ok)

        // instanciamiento de los elementos
        aceptar = dialogo.findViewById(R.id.bt_aceptardiag)
        monto = dialogo.findViewById(R.id.tv_montodiag)
        fecha = dialogo.findViewById(R.id.tv_fechadiag)
        id = dialogo.findViewById(R.id.tv_prcid)
        moneda = dialogo.findViewById(R.id.tv_monedadiag)
        banco = dialogo.findViewById(R.id.tv_bancodiag)
        referencia = dialogo.findViewById(R.id.tv_refdiag)
        cliente = dialogo.findViewById(R.id.tv_clientediag)

        var rmoneda = ""
        var rfecha = ""
        var rmonto = 0.00
        var rid = ""
        var rbanco = ""
        var rref = ""
        var rcli = ""
        var mtoefec = 0.00
        val listadocs: ArrayList<CXC> = ArrayList()

        for (i in datos.indices) {
            rid = datos[i].id_recibo
            rmonto = datos[i].bcomonto
            rfecha = datos[i].fchrecibo
            rmoneda = datos[i].moneda
            rbanco = datos[i].bcocod
            rref = datos[i].bcoref
            rcli = datos[i].cliente
            mtoefec = datos[i].efectivo

        }

        // segun la moneda que llegue, coloco el tipo
        if (rmoneda == "2") {
            moneda.text = "$"
        } else if (rmoneda == "1") {
            moneda.text = "Bs."
        }



        keAndroid = conn.writableDatabase
        val buscaBanco: Cursor =
            keAndroid.rawQuery("SELECT nombanco FROM listbanc WHERE codbanco = '${rbanco}'", null)

        while (buscaBanco.moveToNext()) {
            nombanco = buscaBanco.getString(0)
        }
        buscaBanco.close()

        val buscaCliente: Cursor =
            keAndroid.rawQuery("SELECT nombre FROM cliempre WHERE codigo = '${rcli}'", null)

        while (buscaCliente.moveToNext()) {
            nomcliente = buscaCliente.getString(0)
        }
        buscaCliente.close()

        val buscarDocs: Cursor = keAndroid.rawQuery(
            "SELECT ke_precobradocs.documento, nombrecli FROM ke_precobradocs LEFT JOIN ke_doccti ON ke_doccti.documento = ke_precobradocs.documento WHERE cxcndoc = '${rid}'",
            null
        )

        while (buscarDocs.moveToNext()) {
            val cxcdocs = CXC()
            cxcdocs.documento = buscarDocs.getString(0)
            cxcdocs.cliente = buscarDocs.getString(1)
            listadocs.add(cxcdocs)
        }
        buscarDocs.close()

        // colocación de los datos
        cliente.text = nomcliente
        id.text = rid
        fecha.text = rfecha


        if (mtoefec > 0.00) {
            monto.text = mtoefec.toString()

        } else {
            rmonto = Math.round(rmonto * 100.00) / 100.00
            monto.text = rmonto.toString()
            banco.text = nombanco
            referencia.text = rref
        }

        //mostrar los docs en el pago
        rvDocs = dialogo.findViewById(R.id.rv_docs_recibo)
        rvDocs.layoutManager = LinearLayoutManager(contexto)
        adapter.SimpleDocsAdapter(contexto, listadocs)
        rvDocs.adapter = adapter
        adapter.notifyDataSetChanged()


        //cerrar el dialogo
        aceptar.setOnClickListener {
            /*   var viewDialog: View = dialogo.window!!.decorView.rootView
               myBitmap = captureScreen(viewDialog)*/

            /*  try {
                  if(myBitmap != null){
                      saveImage(myBitmap, rid)
                  }
              }catch (ex:IOException){
                  println(ex.message)
              }*/

            dialogo.dismiss()

            val menucxc = Intent(contexto, CXCActivity::class.java)
            contexto.startActivity(menucxc)
            (contexto as Activity).finish()
        }


        dialogo.show()
    }


    fun savePdfFile(
        contexto: Context,
        nombreEmpresa: String,
        codigoCliente: String,
        nombreCliente: String,
        codigoRecibo: String,
        montoRecibo: Double,
        fechaRecibo: String,
        vendedorRecibo: String
    ) {

        val reciboPDF = PdfDocument()
        val paint = Paint()
        //conf inicial de la pag
        val myInfo = PageInfo.Builder(300, 500, 1).create()
        val pagina: PdfDocument.Page = reciboPDF.startPage(myInfo)
        val canvas = pagina.canvas
        //del obj paint
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 12f
        paint.color = Color.BLACK
        paint.typeface = Typeface.createFromAsset(contexto.assets, "font/arialbd.ttf")


        //CABECERA
        //imagen del la cabecera
        val bmp = BitmapFactory.decodeResource(contexto.resources, R.drawable.plantillasello)
        val scaledBitmap = Bitmap.createScaledBitmap(bmp, 300, 500, false)
        canvas.drawBitmap(scaledBitmap, 0f, 0f, paint)

        //titulos de la cabecera

        //titulos de la cabecera
        canvas.drawText(nombreEmpresa, 150f, 60f, paint)
        canvas.drawText("Recibo de Pago", 150f, 80f, paint)
        canvas.drawText("Estimado(s), ${nombreCliente}, se ha generado", 150f, 100f, paint)
        canvas.drawText("un recibo de pago con los siguientes datos:", 150f, 110f, paint)
        canvas.drawRect(0f, 130f, 300f, 133f, paint)

        //lineas
        paint.textSize = 12f

        paint.color = Color.BLACK
        paint.textAlign = Paint.Align.LEFT

        //Codigo del cliente


        //Codigo del cliente
        paint.typeface = Typeface.createFromAsset(contexto.assets, "font/arialbd.ttf")
        canvas.drawText("Código del Cliente: ", 30f, 160f, paint)
        paint.typeface = Typeface.createFromAsset(contexto.assets, "font/arial.ttf")
        canvas.drawText(codigoCliente, 30f, 180f, paint)

        //NOMBRE DEL CLIENTE

        //NOMBRE DEL CLIENTE
        paint.typeface = Typeface.createFromAsset(contexto.assets, "font/arialbd.ttf")
        canvas.drawText("Cliente: ", 30f, 220f, paint)
        paint.typeface = Typeface.createFromAsset(contexto.assets, "font/arial.ttf")
        canvas.drawText(nombreCliente, 30f, 240f, paint)

        //Monto del Recibo

        //Monto del Recibo
        paint.typeface = Typeface.createFromAsset(contexto.assets, "font/arialbd.ttf")
        canvas.drawText("Monto Pagado: ", 30f, 280f, paint)
        paint.typeface = Typeface.createFromAsset(contexto.assets, "font/arial.ttf")
        canvas.drawText("$$montoRecibo", 30f, 300f, paint)

        //Fecha del Recibo

        //Fecha del Recibo
        paint.typeface = Typeface.createFromAsset(contexto.assets, "font/arialbd.ttf")
        canvas.drawText("Fecha del Recibo: ", 30f, 340f, paint)
        paint.typeface = Typeface.createFromAsset(contexto.assets, "font/arial.ttf")
        canvas.drawText(fechaRecibo, 30f, 360f, paint)

        //vendedor

        //vendedor
        paint.typeface = Typeface.createFromAsset(contexto.assets, "font/arialbd.ttf")
        canvas.drawText("Vendedor: ", 30f, 400f, paint)
        paint.typeface = Typeface.createFromAsset(contexto.assets, "font/arial.ttf")
        canvas.drawText(vendedorRecibo, 30f, 420f, paint)




        reciboPDF.finishPage(pagina)
        VerRecibos.reciboNum =
            "recibo + $codigoRecibo.pdf"
        //este sera el nombre del documento al momento de crearlo y guardarlo en el almacenamiento


        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            VerRecibos.reciboNum
        )


        try {
            reciboPDF.writeTo(FileOutputStream(file))
            Toast.makeText(contexto.applicationContext, "PDF Generado", Toast.LENGTH_LONG)
                .show()
        } catch (e: IOException) {
            Toast.makeText(contexto.applicationContext, "error en $e", Toast.LENGTH_LONG)
                .show()
        }

        reciboPDF.close()


    }

    private fun abrirRecibo(nombreArchivo: String, context: Context) {
        val ruta = "/storage/emulated/0/Documents/$nombreArchivo"
        val file = File(ruta)

        if (!file.exists()) {
            Toast.makeText(
                context.applicationContext,
                "Este archivo no existe o fue cambiado de lugar.",
                Toast.LENGTH_LONG
            ).show()
        }

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "application/pdf"

        intent.putExtra(
            Intent.EXTRA_STREAM,
            FileProvider.getUriForFile(
                context,
                context.applicationContext.packageName + ".provider",
                file
            )
        )
        val chooser = Intent.createChooser(intent, "Compartir recibo...")


    }


}