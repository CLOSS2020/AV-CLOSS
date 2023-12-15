package com.appcloos.mimaletin

import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.database.Cursor
import android.os.Bundle
import android.view.Menu
import android.widget.ListView
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuItemCompat

class PromocionesActivity : AppCompatActivity() {
    private lateinit var listaArticulos: ListView
    private var listacatalogo: ArrayList<Catalogo>? = null
    private lateinit var conn: AdminSQLiteOpenHelper
    var seleccionado = 0
    var cantidad = 0
    private var cursorca: Cursor? = null
    private var factura: Boolean? = null
    private var catalogoAdapter: CatalogoAdapter? = null
    private var APP_DESCUENTOS_PEDIDOS = false

    private lateinit var preferences: SharedPreferences
    private var codEmpresa: String? = null
    private var codUsuario: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_promociones)
        requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED //mantener la activity en vertical

        //Creacion del BackButton
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = "Promociones"

        preferences = getSharedPreferences("Preferences", MODE_PRIVATE)
        codEmpresa = preferences.getString("codigoEmpresa", null)
        codUsuario = preferences.getString("cod_usuario", null)

        /* este intent es para obtener la seleccion, tipo de precio, nro del pedido y codigo del cliente*/
        seleccionado = intent.getIntExtra("Seleccion", 0)
        tipoDePrecioaMostrar = intent.getStringExtra("tipoDePrecioaMostrar")
        precioTotalporArticulo = intent.getDoubleExtra("precioTotalporArticulo", 00.00)
        cod_cliente = intent.getStringExtra("codigoCliente")
        nroPedido = intent.getStringExtra("nroPedido")
        factura = intent.getBooleanExtra("factura", false)
        /*importante inicializar el ayudante para la conexion, para aquellos procesos que corren al iniciar
          el activyty */
        conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null)
        APP_DESCUENTOS_PEDIDOS = conn.getConfigBool("APP_DESCUENTOS_PEDIDOS", codEmpresa!!)
        enlaceEmpresa = conn.getCampoStringCamposVarios("ke_enlace", "kee_url", listOf("kee_codigo"), listOf(codEmpresa!!))

        //APP_ITEMS_FACTURAS = (int) Math.round(conn.getConfigNum("APP_ITEMS_FACTURAS"));
        //APP_ITEMS_NOTAS_ENTREGA = (int) Math.round(conn.getConfigNum("APP_ITEMS_NOTAS_ENTREGA"));
        //APP_DESCUENTOS_PEDIDOS = conn.getConfigBool("APP_DESCUENTOS_PEDIDOS");


        //declaro el listview
        listaArticulos = findViewById(R.id.lv_articulosPromo)
        obtenerArticulosPromo()

        //coloco el adaptador personalizado a la lista del elementos que van al listview
        catalogoAdapter = CatalogoAdapter(this, listacatalogo, enlaceEmpresa)
        listaArticulos.adapter = catalogoAdapter //refresco el listview
        listaArticulos.isTextFilterEnabled = true // inicializo el filtro de texto
        val objetoAux = ObjetoAux(this)
        objetoAux.descargaDesactivo(codUsuario!!, codEmpresa!!)
    }

    private fun obtenerArticulosPromo() {
        listacatalogo = if (conn.getConfigBool("APP_DESCUENTOS_PEDIDOS", codEmpresa!!)) {
            conn.articulosPromo(codEmpresa!!)
        } else {
            ArrayList()
        }
    }

    //busqueda de articulo
    fun buscarArticulo(busqueda: String) {
        listaArticulos.adapter = null
        val keAndroid = conn.writableDatabase
        var catalogo: Catalogo
        if (busqueda == "") {
            //Toast.makeText(CatalogoActivity.this, "Debes introducir una palabra o código", Toast.LENGTH_SHORT).show();
        } else {
            listacatalogo = ArrayList()
            // System.out.println("IMPRIMIENDO EL NOMBRE " + busqueda);
            if (seleccionado == 2) {
                enpreventa = intent!!.getStringExtra("enpreventa")
                if (enpreventa == "0") {
                    cursorca = keAndroid.rawQuery(
                        "select articulo.codigo, articulo.nombre, articulo.$tipoDePrecioaMostrar, articulo.existencia - articulo.comprometido, articulo.fechamodifi, ke_kardex.kde_codart, articulo.vta_min, articulo.vta_max, articulo.dctotope, articulo.enpreventa, articulo.vta_minenx, articulo.vta_solofac, articulo.vta_solone FROM articulo LEFT JOIN  ke_kardex ON articulo.codigo = ke_kardex.kde_codart WHERE dctotope > 0 AND (existencia - comprometido) > 0 and (nombre LIKE '%$busqueda%' OR codigo LIKE'%$busqueda%') and $tipoDePrecioaMostrar > 0.00 AND discont = 0.0 AND enpreventa != '1' ORDER BY articulo.codigo ASC",
                        null
                    )
                } else if (enpreventa == "1") {
                    cursorca = keAndroid.rawQuery(
                        "select articulo.codigo, articulo.nombre, articulo.$tipoDePrecioaMostrar, articulo.existencia - articulo.comprometido, articulo.fechamodifi, ke_kardex.kde_codart, articulo.vta_min, articulo.vta_max, articulo.dctotope, articulo.enpreventa, articulo.vta_minenx, articulo.vta_solofac, articulo.vta_solone FROM articulo LEFT JOIN  ke_kardex ON articulo.codigo = ke_kardex.kde_codart WHERE dctotope > 0 AND (existencia - comprometido) > 0 and (nombre LIKE '%$busqueda%' OR codigo LIKE'%$busqueda%') and $tipoDePrecioaMostrar > 0.00 AND discont = 0.0 AND enpreventa = '$enpreventa' ORDER BY articulo.codigo ASC",
                        null
                    )
                }
            } else if (seleccionado == 1) {
                cursorca = keAndroid.rawQuery(
                    "select articulo.codigo, articulo.nombre, articulo.$preciomostrar, articulo.existencia - articulo.comprometido, articulo.fechamodifi, ke_kardex.kde_codart, articulo.vta_min, articulo.vta_max, articulo.dctotope, articulo.enpreventa, articulo.vta_minenx , articulo.vta_solofac, articulo.vta_solone FROM articulo LEFT JOIN ke_kardex ON articulo.codigo = ke_kardex.kde_codart WHERE dctotope > 0 AND (existencia - comprometido) > 0 and (nombre LIKE '%$busqueda%' OR codigo LIKE'%$busqueda%') and $preciomostrar> 0.00  AND discont = 0.0 ORDER BY articulo.codigo ASC",
                    null
                )
            }
            while (cursorca!!.moveToNext()) {
                catalogo = Catalogo()
                catalogo.setCodigo(cursorca!!.getString(0))
                catalogo.setNombre(cursorca!!.getString(1))
                val precio = cursorca!!.getDouble(2)
                val precioRd = precio.valorReal()
                catalogo.setPrecio1(precioRd)
                val existenc = cursorca!!.getDouble(3)
                val existenciaRd = existenc.toInt()
                catalogo.setExistencia(existenciaRd)
                catalogo.setCodigoKardex(cursorca!!.getString(5))
                catalogo.setVta_min(cursorca!!.getDouble(6))
                catalogo.setVta_max(cursorca!!.getDouble(7))
                val descuento = cursorca!!.getDouble(8)
                catalogo.setDctotope(validarDescuento(descuento))
                catalogo.setEnpreventa(cursorca!!.getString(9))
                catalogo.setMultiplo(cursorca!!.getInt(10))
                catalogo.setVta_solofac(cursorca!!.getInt(11))
                catalogo.setVta_solone(cursorca!!.getInt(12))
                vtaMin = cursorca!!.getDouble(6) //VARIABLE EN DOUBLE DE VTA MIN
                vtaMax = cursorca!!.getDouble(7) //VARIABLE EN DOUBLE DE VTA MAX
                listacatalogo!!.add(catalogo)
            }
            //ke_android.close();
            catalogoAdapter = CatalogoAdapter(this, listacatalogo, enlaceEmpresa)
            listaArticulos.adapter = catalogoAdapter
            catalogoAdapter!!.notifyDataSetChanged()
        }
    }

    private fun validarDescuento(descuento: Double): Double {
        return if (APP_DESCUENTOS_PEDIDOS) {
            descuento
        } else {
            0.0
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.menu_catalogo, menu)
        val menuItem = menu.findItem(R.id.search_view_catalogo)
        val buscador = MenuItemCompat.getActionView(menuItem) as SearchView
        buscador.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(busqueda: String): Boolean {
                buscarArticulo(busqueda)
                return false
            }

            override fun onQueryTextChange(busqueda: String): Boolean {
                buscarArticulo(busqueda)
                return false
            }
        })
        if (seleccionado == 2) {
            for (i in 1 until menu.size()) {
                menu.getItem(i).isVisible = false
            }
        }
        return super.onCreateOptionsMenu(menu)
    }

    companion object {
        var tipoDePrecioaMostrar: String? = null
        var preciomostrar = "precio1"
        var cod_cliente: String? = null
        var nroPedido: String? = null
        var nombreEmpresa: String? = null
        var enlaceEmpresa: String? = null
        var enpreventa: String? = "0"
        var precioTotalporArticulo: Double? = null
        var vtaMin: Double? = null
        var vtaMax: Double? = null
    }

    override fun getTheme(): Resources.Theme {
        val theme = super.getTheme()
        theme.applyStyle(setThemeAgencia(Constantes.AGENCIA), true)
        // you could also use a switch if you have many themes that could apply
        return theme
    }

}