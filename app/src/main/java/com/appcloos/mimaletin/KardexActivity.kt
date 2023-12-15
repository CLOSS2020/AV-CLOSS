package com.appcloos.mimaletin

import android.content.DialogInterface
import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso

class KardexActivity : AppCompatActivity() {
    private lateinit var listaKardex: ListView
    private var listacatalogo: ArrayList<Catalogo>? = null
    private lateinit var conn: AdminSQLiteOpenHelper
    lateinit var codEmpresa:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED //mantener la activity en vertical
        setContentView(R.layout.activity_kardex)
        val preferences = getSharedPreferences("Preferences", MODE_PRIVATE)
        val codUsuario = preferences.getString("cod_usuario", null)
        codEmpresa = preferences.getString("codigoEmpresa", null).toString()
        conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null)
        listaKardex = findViewById(R.id.lv_kardex)
        cargarEnlace()

        /*  Intent intent =  getIntent();
        tipoDePrecioaMostrar = intent.getStringExtra("tipoDePrecioaMostrar");*/
        listaKardex.setOnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
            val codArticulo = listacatalogo!![position].getCodigo().trim { it <= ' ' }
            val imagen = ImageView(this@KardexActivity)
            val enlace = "https://$enlaceEmpresa/img/$codArticulo.jpg"
            Picasso.get().load(enlace).resize(1000, 1000).centerCrop().into(imagen)
            val ventana = AlertDialog.Builder(
                ContextThemeWrapper(
                    this@KardexActivity,
                    setAlertDialogTheme(Constantes.AGENCIA)
                )
            )
            ventana.setTitle("Imagen del articulo")
            ventana.setView(imagen)
            ventana.setPositiveButton("Aceptar", null)
            val dialogo = ventana.create()
            dialogo.show()

            val pbutton: Button = dialogo.getButton(DialogInterface.BUTTON_POSITIVE)
            pbutton.apply {
                setTextColor(colorTextAgencia(Constantes.AGENCIA))
            }
        }
        consultarArticulosNormal()
        val catalogoAdapter = CatalogoAdapter(this@KardexActivity, listacatalogo, enlaceEmpresa
        )
        listaKardex.adapter = catalogoAdapter
        val objetoAux = ObjetoAux(this)
        objetoAux.descargaDesactivo(codUsuario!!, codEmpresa)
    }

    private fun consultarArticulosNormal() {
        val keAndroid = conn.writableDatabase
        var catalogo: Catalogo
        listacatalogo = ArrayList()
        val cursor = keAndroid.rawQuery(
            "SELECT articulo.codigo, articulo.nombre, articulo.$tipoDePrecioaMostrar, articulo.existencia, articulo.fechamodifi, ke_kardex.kde_codart, articulo.vta_min, articulo.vta_max, articulo.dctotope, articulo.enpreventa FROM ke_kardex LEFT JOIN articulo ON ke_kardex.kde_codart = articulo.codigo WHERE (existencia - comprometido) > 0 AND discont = 0.0 AND articulo.empresa = '$codEmpresa';",
            null
        )


        //select codigo, nombre from articulo
        while (cursor.moveToNext()) {
            catalogo = Catalogo()
            catalogo.setCodigo(cursor.getString(0))
            catalogo.setNombre(cursor.getString(1))
            val precio1 = cursor.getDouble(2)
            val precio1Rd = precio1.valorReal()
            catalogo.setPrecio1(precio1Rd)
            val existenc = cursor.getDouble(3)
            val existenciaRd = existenc.toInt()
            catalogo.setExistencia(existenciaRd)
            catalogo.setCodigoKardex(cursor.getString(5))
            catalogo.setVta_min(cursor.getDouble(6))
            catalogo.setVta_max(cursor.getDouble(7))
            catalogo.setDctotope(cursor.getDouble(8))
            catalogo.setEnpreventa(cursor.getString(9))
            listacatalogo!!.add(catalogo)
        }
        cursor.close()
        keAndroid.close()
    }

    private fun cargarEnlace() {
        val keAndroid = conn.writableDatabase
        val columnas = arrayOf(
            "kee_nombre," +
                    "kee_url," +
                    "kee_sucursal"
        )
        val cursor = keAndroid.query("ke_enlace", columnas, "kee_codigo = '$codEmpresa'", null, null, null, null)
        while (cursor.moveToNext()) {
            nombreEmpresa = cursor.getString(0)
            enlaceEmpresa = cursor.getString(1)
            codigoSucursal = cursor.getString(2)
        }
        cursor.close()
        keAndroid.close()
    }

    companion object {
        var tipoDePrecioaMostrar = "precio1"
        var enlaceEmpresa = ""
        var nombreEmpresa = ""
        var codigoSucursal = ""
    }

    override fun getTheme(): Resources.Theme {
        val theme = super.getTheme()
        theme.applyStyle(setThemeAgencia(Constantes.AGENCIA), true)
        // you could also use a switch if you have many themes that could apply
        return theme
    }
}