/*Clase....: CatalogoActivity
 * Autor.......: PCV MAR 2021
 * Objetivo....: Mostrar los articulos como catalogo o para eleccion al momento de crear
 *               el pedido
 *
 * Notas.......:
 *
 * Parámetros..: ActDirec : segun el valor, mostrara el catalogo o en modo de eleccion
 *
 * Modif.......:
 *
 * NOTAS.......: OJO con los procesos y las transacciones a la hora de guardar el articulo
 *
 * Retorna.....: Ninguno
 *-------------**/
package com.appcloos.mimaletin

import android.content.ContentValues
import android.content.DialogInterface
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.database.Cursor
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.view.ContextThemeWrapper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuItemCompat
import com.appcloos.mimaletin.databinding.ActivityCatalogoBinding
import com.squareup.picasso.Picasso
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.floor

class CatalogoActivity : AppCompatActivity() {
    var seleccionArticulo: ArrayList<*> = ArrayList<Any?>()
    var listainfo: ArrayList<String>? = null
    private var listacatalogo: ArrayList<Catalogo>? = null
    private lateinit var conn: AdminSQLiteOpenHelper
    var seleccionado = 0
    private var existenciaGuardar = ""
    var cantidad = 0
    var mostrarMinimo = 0
    var mostrarMaximo = 0
    private var cursorca: Cursor? = null
    var buscadorarticulo: SearchView? = null
    private var factura: Boolean? = null
    private var APP_ITEMS_FACTURAS = 0
    private var APP_ITEMS_NOTAS_ENTREGA = 0
    private var catalogoAdapter: CatalogoAdapter? = null
    private var APP_DESCUENTOS_PEDIDOS = false

    private lateinit var preferences: SharedPreferences
    private lateinit var codEmpresa:String

    private lateinit var binding: ActivityCatalogoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED //mantener la activity en vertical
        binding = ActivityCatalogoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Creacion del BackButton
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        preferences = getSharedPreferences("Preferences", MODE_PRIVATE)
        codEmpresa = preferences.getString("codigoEmpresa", null).toString()

        /* este intent es para obtener la seleccion, tipo de precio, nro del pedido y codigo del cliente*/
        seleccionado = intent.getIntExtra("Seleccion", 0)
        tipoDePrecioaMostrar = intent.getStringExtra("tipoDePrecioaMostrar")
        precioTotalporArticulo = intent.getDoubleExtra("precioTotalporArticulo", 00.00)
        cod_cliente = intent.getStringExtra("codigoCliente")
        nroPedido = intent.getStringExtra("nroPedido")
        factura = intent.getBooleanExtra("factura", false)
        /*importante inicializar el ayudante para la conexion, para aquellos procesos que corren al iniciar
          el activyty */conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null)
        APP_ITEMS_FACTURAS = conn.getConfigNum("APP_ITEMS_FACTURAS", codEmpresa).toInt()
        APP_ITEMS_NOTAS_ENTREGA = conn.getConfigNum("APP_ITEMS_NOTAS_ENTREGA", codEmpresa).toInt()
        APP_DESCUENTOS_PEDIDOS = conn.getConfigBool("APP_DESCUENTOS_PEDIDOS", codEmpresa)

        enlaceEmpresa = conn.getCampoString("ke_enlace", "kee_url", "kee_codigo", codEmpresa)

        //cargarEnlace()
        //declaro el listview
        consultarArticulosNormal(preciomostrar) //consulto los articulos

        //coloco el adaptador personalizado a la lista del elementos que van al listview
        catalogoAdapter = CatalogoAdapter(this@CatalogoActivity, listacatalogo, enlaceEmpresa)
        //ArrayAdapter adaptador = new ArrayAdapter(CatalogoActivity.this, R.layout.list_catalogo_personalizado, listainfo);
        binding.lvArticulos.adapter = catalogoAdapter //refresco el listview
        binding.lvArticulos.isTextFilterEnabled = true // inicializo el filtro de texto


        //corro actDirect
        actDirec()
        val objetoAux = ObjetoAux(this)
        objetoAux.descargaDesactivo(PrincipalActivity.cod_usuario!!)
    }

    //este metodo inicializa un menu con el searchview y el selector de precios   SLECT
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

    //y este es el selector de precios que segun la seleccion, consulta los articulos por precios
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemid = item.itemId

        /*if(itemid == R.id.bt_precio1){
            preciomostrar = "precio1";
            consultarArticulosNormal(preciomostrar);

            catalogoAdapter = new CatalogoAdapter(CatalogoActivity.this, listacatalogo);
            listaArticulos.setAdapter(catalogoAdapter);
            catalogoAdapter.notifyDataSetChanged(); //cada vez que se ejecute la consulta, debe refrescarse el adapter


         } else if(itemid == R.id.bt_precio2){
            preciomostrar = "precio2";
            consultarArticulosNormal(preciomostrar);

            catalogoAdapter = new CatalogoAdapter(CatalogoActivity.this, listacatalogo);
            listaArticulos.setAdapter(catalogoAdapter);
            catalogoAdapter.notifyDataSetChanged();


        } else if(itemid == R.id.bt_precio3){
            preciomostrar = "precio3";
            consultarArticulosNormal(preciomostrar);

            catalogoAdapter = new CatalogoAdapter(CatalogoActivity.this, listacatalogo);
            listaArticulos.setAdapter(catalogoAdapter);
            catalogoAdapter.notifyDataSetChanged();


        }else */if (itemid == android.R.id.home) {
            //Valida que se le da al backbutton y se regresa
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    /*private fun cargarEnlace() {
        val keAndroid = conn!!.writableDatabase
        val columnas = arrayOf("kee_nombre," + "kee_url")
        val cursor = keAndroid.query("ke_enlace", columnas, "empresa = '$codEmpresa'", null, null, null, null)
        while (cursor.moveToNext()) {
            nombreEmpresa = cursor.getString(0)
            enlaceEmpresa = cursor.getString(1)
        }
        cursor.close()
        keAndroid.close()
    }*/

    //metodo para ver la seleccion del activity
    private fun actDirec() {
        if (seleccionado == 2) { /*viene de pedidos, para indicar si es el catalogo de seleccion
                                o solo para mostrar los articulos*/
            consultarArticulosenPedido()

            //el listener en este caso sirve para agregar el articulo en el pedido
            binding.lvArticulos.onItemClickListener =
                OnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
                    val codArticulo = listacatalogo!![position].getCodigo()
                    val nArticulo = listacatalogo!![position].getNombre()
                    val precio = listacatalogo!![position].getPrecio1()
                    val existencia = listacatalogo!![position].getExistencia()
                    val ventaMax = listacatalogo!![position].getVta_max()
                    val ventaMin = listacatalogo!![position].getVta_min()
                    val dctotope = listacatalogo!![position].getDctotope()
                    conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null)
                    val keAndroid = conn.writableDatabase
                    val cursorMul = keAndroid.rawQuery(
                        "SELECT vta_minenx, vta_solofac, vta_solone FROM articulo " +
                                "WHERE codigo ='$codArticulo' AND empresa = '$codEmpresa';",
                        null
                    )
                    cursorMul.moveToFirst()
                    val vtaMinenx = cursorMul.getInt(0)
                    val vtaSolofac = cursorMul.getInt(1)
                    val vtaSolone = cursorMul.getInt(2)
                    cursorMul.close()
                    if (vtaSolofac == 1 && !factura!!) {
                        Toast.makeText(
                            this@CatalogoActivity,
                            "Este articulo solo se puede agregar a Facturas",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@OnItemClickListener
                    } else if (vtaSolone == 1 && factura!!) {
                        Toast.makeText(
                            this@CatalogoActivity,
                            "Este articulo solo se puede agregar a Notas de Entrega",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@OnItemClickListener
                    }
                    val layout = LinearLayout(this@CatalogoActivity)
                    layout.orientation = LinearLayout.VERTICAL
                    val cursor = keAndroid.rawQuery(
                        "SELECT kmv_codart FROM ke_carrito WHERE kmv_codart ='$codArticulo' AND empresa = '$codEmpresa';",
                        null
                    )
                    if (cursor.moveToFirst()) {
                        Toast.makeText(
                            this@CatalogoActivity,
                            "El artículo ya se encuentra en el pedido",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        val cajaDescuento = EditText(
                            ContextThemeWrapper(
                                this@CatalogoActivity,
                                setEditTextTheme(codEmpresa)
                            )
                        )
                        val cajatexto = EditText(
                            ContextThemeWrapper(
                                this@CatalogoActivity,
                                setEditTextTheme(codEmpresa)
                            )
                        )
                        cajaDescuento.inputType = InputType.TYPE_CLASS_NUMBER
                        cajatexto.inputType = InputType.TYPE_CLASS_NUMBER
                        //cajatexto.setFilters(new InputFilter[] {new InputFilter.LengthFilter(250)}); -- como referencia para  campos de notas

                        //un alert dialogo builder que va a servir para introducir cantidad de articulos
                        val ventana = AlertDialog.Builder(
                            ContextThemeWrapper(
                                this@CatalogoActivity,
                                setAlertDialogTheme(codEmpresa)
                            )
                        )
                        //declaramos textviews porque vamos a usar layout
                        val titulo = TextView(this@CatalogoActivity)
                        val mensaje = TextView(this@CatalogoActivity)
                        val montoEnPedido = TextView(this@CatalogoActivity)
                        val mensajecantidad = TextView(this@CatalogoActivity)
                        val darDescuento = CheckBox(
                            ContextThemeWrapper(
                                this@CatalogoActivity,
                                setCheckBoxTheme(codEmpresa)
                            )
                        )
                        //final TextView mensajeCantidadMultiplo = new TextView(CatalogoActivity.this);

                        //declaramos las propiedades de cada textview
                        mensaje.textSize = 15f
                        //mensaje.setTextColor(Color.parseColor("#313131"));
                        mensajecantidad.textSize = 15f
                        //mensajecantidad.setTextColor(Color.parseColor("#313131"));
                        montoEnPedido.textSize = 15f
                        //montoEnPedido.setTextColor(Color.parseColor("#313131"));
                        titulo.text = "Selección del artículo"
                        //titulo.setTextColor(Color.parseColor("#313131"));
                        titulo.textSize = 22f
                        titulo.setTypeface(null, Typeface.BOLD)
                        layout.addView(titulo)

                        //si el articulo no posee descuento, entonces escondo la opción y asumo que es 0 el descuento
                        if (dctotope > 0 && APP_DESCUENTOS_PEDIDOS) {
                            //de resto, permito elegir el descuento
                            mensaje.text = "Porfavor, elige el descuento"
                            //layout.addView(mensaje);
                            darDescuento.text = "Dar Descuento del Articulo"
                            layout.addView(darDescuento)
                            //layout.addView(cajaDescuento);
                        } else {
                            mensaje.visibility = View.INVISIBLE
                            cajaDescuento.visibility = View.INVISIBLE
                            dctonumerico = 0.00
                        }
                        mensajecantidad.text = "Porfavor, elige la cantidad"
                        val cursorPretotal =
                            keAndroid.rawQuery("SELECT SUM(kmv_stotdcto) FROM ke_carrito WHERE empresa = '$codEmpresa';", null)
                        cursorPretotal.moveToFirst()
                        precioTotalporArticulo = cursorPretotal.getDouble(0)
                        cursorPretotal.close()
                        precioTotalporArticulo = precioTotalporArticulo.valorReal()
                        montoEnPedido.text = """
                Monto del Pedido: ${"$"}$precioTotalporArticulo
                Cantidad Disponible: $existencia
                """.trimIndent()
                        layout.addView(mensajecantidad)
                        layout.addView(montoEnPedido)
                        if (vtaMinenx == 1) {
                            val layoutH = LinearLayout(this@CatalogoActivity)
                            layoutH.orientation = LinearLayout.HORIZONTAL
                            val mensajeCantidadMultiplo = TextView(this@CatalogoActivity)
                            val mensajeMultiplo = TextView(this@CatalogoActivity)
                            mensajeMultiplo.textSize = 15f
                            //mensajeMultiplo.setTextColor(Color.parseColor("#313131"));
                            mensajeMultiplo.text =
                                "Cantidad de paquetes: " + floor(existencia / ventaMin).toInt()


                            //LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
                            //params.weight = 1.0f;

                            //LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
                            //params2.weight = 50.0f;
                            mensajeCantidadMultiplo.textSize = 20f
                            mensajeCantidadMultiplo.setTypeface(null, Typeface.BOLD)
                            //mensajeCantidadMultiplo.setTextColor(Color.parseColor("#313131"));
                            mensajeCantidadMultiplo.text =
                                Math.round(ventaMin).toInt().toString() + " x "
                            //mensajeCantidadMultiplo.setLayoutParams(params);
                            cajatexto.width = 1000
                            cajatexto.hint = "Cantidad de paquetes a pedir"
                            layoutH.addView(mensajeCantidadMultiplo)
                            //cajatexto.setLayoutParams(params2);
                            layoutH.addView(cajatexto)
                            layout.addView(mensajeMultiplo)
                            layout.addView(layoutH)
                        } else {
                            layout.addView(cajatexto)
                        }
                        ventana.setView(layout)

                        //el boton procesar que servira para procesar el agregado del articulo
                        ventana.setPositiveButton("Aceptar") { _: DialogInterface?, _: Int ->

                            //validadciones para procesar
                            if (cajatexto.text.toString().isEmpty()) {
                                Toast.makeText(
                                    this@CatalogoActivity,
                                    "Debes agregar una cantidad",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                existenciaGuardar = cajatexto.text.toString()
                                cantidad = existenciaGuardar.toInt()
                                if (cantidad in 1..existencia) {
                                    //si la cantidad de venta maxima es mayor a 0, debo hacer validadciones adicionales
                                    if (ventaMax > 0) {
                                        if (cantidad > ventaMax) {
                                            Toast.makeText(
                                                this@CatalogoActivity,
                                                "La cantidad solicitada no puede ser mayor a la cantidad de máxima de Venta",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        } else if (cantidad <= ventaMax) {
                                            if (cantidad > ventaMin) {
                                                val comprobacion = consultarDisponibilidad(
                                                    PrincipalActivity.cod_usuario,
                                                    cod_cliente,
                                                    codArticulo
                                                )
                                                val exisHist = comprobacion + cantidad
                                                println("ESTA ES LA EXISTENCIA HISTORICA$exisHist")
                                                if (exisHist > ventaMax) {
                                                    Toast.makeText(
                                                        this@CatalogoActivity,
                                                        "Este artículo ha superado la cantidad máxima para este cliente",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                } else {
                                                    val hoy =
                                                        LocalDateTime.now() //el dia en que se hizo el grabado
                                                    val formatter =
                                                        DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")
                                                    val vencimiento =
                                                        hoy.plusDays(7) //cuando se vence

                                                    //las fechas formateadas
                                                    val fechaHoy = hoy.format(formatter)
                                                    val fechaVence = vencimiento.format(formatter)
                                                    println(fechaHoy)
                                                    println(fechaVence)
                                                    val descuentoTexto =
                                                        cajaDescuento.text.toString()
                                                    val aprobarDescuento = darDescuento.isChecked

                                                    //if (descuentoTexto.equals("")) {
                                                    dctonumerico = if (!aprobarDescuento) {
                                                        0.00
                                                    } else {
                                                        dctotope.toString().toDouble()
                                                    }
                                                    if (dctonumerico!! > dctotope) {
                                                        Toast.makeText(
                                                            this@CatalogoActivity,
                                                            "El descuento introducido es inválido",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    } else {
                                                        val tracking = nroPedido
                                                        var precioTotal =
                                                            precio * cantidad.toDouble()
                                                        precioTotal = precioTotal.valorReal()
                                                        stotdcto = if (dctonumerico!! > 0) {
                                                            precioTotal - precioTotal * (dctonumerico!! / 100)
                                                        } else {
                                                            precioTotal
                                                        }
                                                        keAndroid.beginTransaction()
                                                        try {
                                                            val cv = ContentValues()
                                                            cv.put("kmv_codart", codArticulo)
                                                            cv.put("kmv_nombre", nArticulo)
                                                            cv.put("kmv_stot", precioTotal)
                                                            cv.put("kmv_cant", cantidad)
                                                            cv.put("kmv_artprec", precio)
                                                            cv.put(
                                                                "kmv_dctolin",
                                                                dctonumerico
                                                            )
                                                            cv.put("kmv_stotdcto", stotdcto)
                                                            cv.put("empresa", codEmpresa)

                                                            keAndroid.insert(
                                                                "ke_carrito",
                                                                null,
                                                                cv
                                                            )

                                                            //llamo al metodo guardar limites si el articulo posee limites
                                                            guardarLimite(
                                                                tracking,
                                                                PrincipalActivity.cod_usuario,
                                                                cod_cliente,
                                                                codArticulo,
                                                                cantidad,
                                                                fechaHoy,
                                                                fechaVence,
                                                                "0"
                                                            )
                                                            keAndroid.setTransactionSuccessful()
                                                            keAndroid.endTransaction()
                                                            Toast.makeText(
                                                                this@CatalogoActivity,
                                                                "Artículo añadido",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                            //finish();
                                                        } catch (ex: Exception) {
                                                            println("--Error--")
                                                            ex.printStackTrace()
                                                            println("--Error--")
                                                            keAndroid.endTransaction()
                                                        }
                                                    }
                                                }
                                            } else {
                                                Toast.makeText(
                                                    this@CatalogoActivity,
                                                    "Debe cumplir con la cantidad mínima",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }

                                        //de no tener un limite de venta maxima, sigo y guardo el articulo.
                                    } else if (ventaMin > 0) {
                                        if (vtaMinenx == 1) {
                                            println("hola " + cantidad * ventaMin + " " + existencia)
                                            if (cantidad * ventaMin > existencia) {
                                                Toast.makeText(
                                                    this@CatalogoActivity,
                                                    "Debe de elegir una cantidad dentro de la existencia",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            } else if (cantidad * ventaMin <= existencia) {
                                                val cantidadNew = (cantidad * ventaMin).toInt()
                                                println("Nueva cantidad $cantidadNew")
                                                var precioTotal = precio * cantidadNew
                                                println("Precio total: $precioTotal")
                                                precioTotal = precioTotal.valorReal()
                                                val descuentoTexto = cajaDescuento.text.toString()
                                                val aprobarDescuento = darDescuento.isChecked

                                                //if (descuentoTexto.equals("")) {
                                                dctonumerico = if (!aprobarDescuento) {
                                                    0.00
                                                } else {
                                                    dctotope.toString().toDouble()
                                                }
                                                if (dctonumerico!! > dctotope) {
                                                    Toast.makeText(
                                                        this@CatalogoActivity,
                                                        "El descuento introducido es inválido",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                } else {
                                                    stotdcto = if (dctonumerico!! > 0) {
                                                        precioTotal - precioTotal * (dctonumerico!! / 100)
                                                    } else {
                                                        precioTotal
                                                    }
                                                    keAndroid.beginTransaction()
                                                    try {
                                                        val cv = ContentValues()
                                                        cv.put("kmv_codart", codArticulo)
                                                        cv.put("kmv_nombre", nArticulo)
                                                        cv.put("kmv_stot", precioTotal)
                                                        cv.put("kmv_cant", cantidadNew)
                                                        cv.put("kmv_artprec", precio)
                                                        cv.put("kmv_dctolin", dctonumerico)
                                                        cv.put("kmv_stotdcto", stotdcto)
                                                        cv.put("empresa", codEmpresa)

                                                        keAndroid.insert(
                                                            "ke_carrito",
                                                            null,
                                                            cv
                                                        )
                                                        keAndroid.setTransactionSuccessful()
                                                        keAndroid.endTransaction()
                                                        println("Precio: " + cantidad * ventaMin * precio)
                                                        //finish();
                                                        Toast.makeText(
                                                            this@CatalogoActivity,
                                                            "Artículo añadido",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    } catch (ex: Exception) {
                                                        println("--Error--")
                                                        ex.printStackTrace()
                                                        println("--Error--")
                                                        keAndroid.endTransaction()
                                                    }
                                                }
                                            }
                                        } else {
                                            if (cantidad < ventaMin) {
                                                Toast.makeText(
                                                    this@CatalogoActivity,
                                                    "Debe cumplir con la cantidad mínima para la venta",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            } else if (cantidad >= ventaMin) {
                                                var precioTotal = precio * cantidad.toDouble()
                                                precioTotal = precioTotal.valorReal()
                                                val descuentoTexto = cajaDescuento.text.toString()
                                                val aprobarDescuento = darDescuento.isChecked

                                                //if (descuentoTexto.equals("")) {
                                                dctonumerico = if (!aprobarDescuento) {
                                                    0.00
                                                } else {
                                                    dctotope.toString().toDouble()
                                                }
                                                if (dctonumerico!! > dctotope) {
                                                    Toast.makeText(
                                                        this@CatalogoActivity,
                                                        "El descuento introducido es inválido",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                } else {
                                                    stotdcto = if (dctonumerico!! > 0) {
                                                        precioTotal - precioTotal * (dctonumerico!! / 100)
                                                    } else {
                                                        precioTotal
                                                    }
                                                    keAndroid.beginTransaction()
                                                    try {
                                                        val cv = ContentValues()
                                                        cv.put("kmv_codart", codArticulo)
                                                        cv.put("kmv_nombre", nArticulo)
                                                        cv.put("kmv_stot", precioTotal)
                                                        cv.put("kmv_cant", cantidad)
                                                        cv.put("kmv_artprec", precio)
                                                        cv.put("kmv_dctolin", dctonumerico)
                                                        cv.put("kmv_stotdcto", stotdcto)
                                                        cv.put("empresa", codEmpresa)

                                                        keAndroid.insert(
                                                            "ke_carrito",
                                                            null,
                                                            cv
                                                        )
                                                        keAndroid.setTransactionSuccessful()
                                                        keAndroid.endTransaction()
                                                        println(cantidad * ventaMin)
                                                        //finish();
                                                        Toast.makeText(
                                                            this@CatalogoActivity,
                                                            "Artículo añadido",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    } catch (ex: Exception) {
                                                        println("--Error--")
                                                        ex.printStackTrace()
                                                        println("--Error--")
                                                        keAndroid.endTransaction()
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        var precioTotal = precio * cantidad.toDouble()
                                        precioTotal = precioTotal.valorReal()
                                        val descuentoTexto = cajaDescuento.text.toString()
                                        val aprobarDescuento = darDescuento.isChecked

                                        //if (descuentoTexto.equals("")) {
                                        dctonumerico = if (!aprobarDescuento) {
                                            0.00
                                        } else {
                                            dctotope.toString().toDouble()
                                        }
                                        if (dctonumerico!! > dctotope) {
                                            Toast.makeText(
                                                this@CatalogoActivity,
                                                "El descuento introducido es inválido",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            stotdcto = if (dctonumerico!! > 0) {
                                                precioTotal - precioTotal * (dctonumerico!! / 100)
                                            } else {
                                                precioTotal
                                            }
                                            keAndroid.beginTransaction()
                                            try {
                                                val cv = ContentValues()
                                                cv.put("kmv_codart", codArticulo)
                                                cv.put("kmv_nombre", nArticulo)
                                                cv.put("kmv_stot", precioTotal)
                                                cv.put("kmv_cant", cantidad)
                                                cv.put("kmv_artprec", precio)
                                                cv.put("kmv_dctolin", dctonumerico)
                                                cv.put("kmv_stotdcto", stotdcto)
                                                cv.put("empresa", codEmpresa)

                                                keAndroid.insert("ke_carrito", null, cv)
                                                keAndroid.setTransactionSuccessful()
                                                keAndroid.endTransaction()
                                                Toast.makeText(
                                                    this@CatalogoActivity,
                                                    "Artículo añadido",
                                                    Toast.LENGTH_SHORT
                                                ).show()

                                                //finish();
                                            } catch (ex: Exception) {
                                                println("--Error--")
                                                ex.printStackTrace()
                                                println("--Error--")
                                                keAndroid.endTransaction()
                                            }
                                        }
                                    }
                                    val cursorF = keAndroid.rawQuery(
                                        "SELECT COUNT(kmv_codart) FROM ke_carrito WHERE empresa = '$codEmpresa';",
                                        null
                                    )
                                    cursorF.moveToFirst()
                                    val cantidadCarritoFac = cursorF.getInt(0)
                                    cursorF.close()
                                    println("El numero $cantidadCarritoFac")
                                    if (cantidadCarritoFac > APP_ITEMS_FACTURAS && factura!!) {
                                        finish()
                                    }
                                    if (cantidadCarritoFac > APP_ITEMS_NOTAS_ENTREGA && !factura!!) {
                                        finish()
                                    }
                                } else if (cantidad > existencia || cantidad == 0) {
                                    Toast.makeText(
                                        this@CatalogoActivity,
                                        "La cantidad no puede ser mayor a la existencia o igual a 0",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                        val dialogo = ventana.create() //creo el alertdialog en funcion al builder
                        dialogo.show() // y lo muestro

                        val pbutton: Button = dialogo.getButton(DialogInterface.BUTTON_POSITIVE)
                        pbutton.apply {
                            setTextColor(colorTextAgencia(codEmpresa))
                        }


                    }
                    cursor.close()
                }

            //si viene desde el menu principal, entonces solo voy a mostrar el catalogo
        } else if (seleccionado == 1) {
            //un listener que, por los momentos, me permitira mostrar una imagen (mas adelante se debe desarrollar la ficha)
            binding.lvArticulos.onItemClickListener =
                OnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
                    val codArticulo = listacatalogo!![position].getCodigo().trim { it <= ' ' }
                    val imagen = ImageView(this@CatalogoActivity)
                    val enlace = "https://$enlaceEmpresa/img/$codArticulo.jpg"
                    Picasso.get().load(enlace).resize(1000, 1000).centerCrop().into(imagen)

                    //este builder mostrara la ficha del articulo
                    val ventana = AlertDialog.Builder(
                        ContextThemeWrapper(
                            this@CatalogoActivity,
                            setAlertDialogTheme(codEmpresa)
                        )
                    )
                    ventana.setTitle("Imagen del articulo")
                    ventana.setView(imagen)
                    ventana.setPositiveButton("Aceptar", null)
                    val dialogo = ventana.create()
                    dialogo.show() //

                    val pbutton: Button = dialogo.getButton(DialogInterface.BUTTON_POSITIVE)
                    pbutton.apply {
                        setTextColor(colorTextAgencia(codEmpresa))
                    }

                }
            consultarArticulosNormal(preciomostrar)
        }
    }

    //este metodo permite guardar los limites de articulos que se encuentran en pedidos
    private fun guardarLimite(
        tracking: String?,
        codUsuario: String?,
        codCliente: String?,
        codArticulo: String,
        cantidad: Int,
        fechaHoy: String,
        fechaVence: String,
        status: String
    ) {
        val keAndroid = conn.writableDatabase
        val guardarArticulo = ContentValues()
        guardarArticulo.put("kli_track", tracking)
        guardarArticulo.put("kli_codven", codUsuario)
        guardarArticulo.put("kli_codcli", codCliente)
        guardarArticulo.put("kli_codart", codArticulo)
        guardarArticulo.put("kli_cant", cantidad)
        guardarArticulo.put("kli_fechahizo", fechaHoy)
        guardarArticulo.put("kli_fechavence", fechaVence)
        guardarArticulo.put("status", status)
        guardarArticulo.put("empresa", codEmpresa)

        keAndroid.insert("ke_limitart", null, guardarArticulo)
    }

    //funcion para consultar la disponiblidad de un articulo según este limitado o no.
    private fun consultarDisponibilidad(
        codUsuario: String?,
        codCliente: String?,
        codArticulo: String
    ): Int {
        var resultado = 0
        val keAndroid = conn.readableDatabase
        val cuComp = keAndroid.rawQuery(
            "SELECT SUM(kli_cant) FROM ke_limitart " +
                    "WHERE kli_codven ='$codUsuario' AND kli_codcli='$codCliente' AND " +
                    "kli_codart='$codArticulo' AND status ='1' AND empresa = '$codEmpresa';",
            null
        )
        while (cuComp.moveToNext()) {
            resultado = cuComp.getInt(0)
        }
        cuComp.close()
        return resultado
    }

    //busqueda de articulo
    fun buscarArticulo(busqueda: String) {
        binding.lvArticulos.adapter = null
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
                        "select articulo.codigo, articulo.nombre, articulo.$tipoDePrecioaMostrar, articulo.existencia - articulo.comprometido, articulo.fechamodifi, ke_kardex.kde_codart, articulo.vta_min, articulo.vta_max, articulo.dctotope, articulo.enpreventa, articulo.vta_minenx, articulo.vta_solofac, articulo.vta_solone FROM articulo LEFT JOIN  ke_kardex ON articulo.codigo = ke_kardex.kde_codart WHERE (existencia - comprometido) > 0 and (nombre LIKE '%$busqueda%' OR codigo LIKE'%$busqueda%') and $tipoDePrecioaMostrar > 0.00 AND discont = 0.0 AND enpreventa != '1' AND articulo.empresa = '$codEmpresa' ORDER BY articulo.codigo ASC",
                        null
                    )
                } else if (enpreventa == "1") {
                    cursorca = keAndroid.rawQuery(
                        "select articulo.codigo, articulo.nombre, articulo.$tipoDePrecioaMostrar, articulo.existencia - articulo.comprometido, articulo.fechamodifi, ke_kardex.kde_codart, articulo.vta_min, articulo.vta_max, articulo.dctotope, articulo.enpreventa, articulo.vta_minenx, articulo.vta_solofac, articulo.vta_solone FROM articulo LEFT JOIN  ke_kardex ON articulo.codigo = ke_kardex.kde_codart WHERE (existencia - comprometido) > 0 and (nombre LIKE '%$busqueda%' OR codigo LIKE'%$busqueda%') and $tipoDePrecioaMostrar > 0.00 AND discont = 0.0 AND enpreventa = '$enpreventa' AND articulo.empresa = '$codEmpresa' ORDER BY articulo.codigo ASC",
                        null
                    )
                }
            } else if (seleccionado == 1) {
                cursorca = keAndroid.rawQuery(
                    "select articulo.codigo, articulo.nombre, articulo.$preciomostrar, articulo.existencia - articulo.comprometido, articulo.fechamodifi, ke_kardex.kde_codart, articulo.vta_min, articulo.vta_max, articulo.dctotope, articulo.enpreventa, articulo.vta_minenx , articulo.vta_solofac, articulo.vta_solone FROM articulo LEFT JOIN ke_kardex ON articulo.codigo = ke_kardex.kde_codart WHERE (existencia - comprometido) > 0 and (nombre LIKE '%$busqueda%' OR codigo LIKE'%$busqueda%') and $preciomostrar> 0.00 AND discont = 0.0 AND articulo.empresa = '$codEmpresa' ORDER BY articulo.codigo ASC",
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
            catalogoAdapter = CatalogoAdapter(this@CatalogoActivity, listacatalogo, enlaceEmpresa)
            binding.lvArticulos.adapter = catalogoAdapter
            catalogoAdapter!!.notifyDataSetChanged()
        }
    }

    private fun consultarArticulosNormal(precioparametro: String) {
        val keAndroid = conn.writableDatabase
        var catalogo: Catalogo
        var cursor: Cursor? = null
        listacatalogo = ArrayList()
        enpreventa = intent!!.getStringExtra("enpreventa")
        if (enpreventa == null) {
            enpreventa = "0"
        }
        if (enpreventa == "0") {
            cursor = keAndroid.rawQuery(
                "SELECT articulo.codigo, articulo.nombre, articulo.$precioparametro, articulo.existencia - articulo.comprometido, articulo.fechamodifi, ke_kardex.kde_codart, articulo.vta_min, articulo.vta_max, articulo.dctotope, articulo.enpreventa, articulo.vta_minenx, articulo.vta_solofac, articulo.vta_solone  FROM articulo LEFT JOIN  ke_kardex ON articulo.codigo = ke_kardex.kde_codart WHERE (existencia - comprometido) > 0 AND discont = 0.0 AND articulo.empresa = '$codEmpresa' ORDER BY articulo.codigo ASC",
                null
            )
        } else if (enpreventa == "1") {
            cursor = keAndroid.rawQuery(
                "SELECT articulo.codigo, articulo.nombre, articulo.$precioparametro, articulo.existencia - articulo.comprometido, articulo.fechamodifi, ke_kardex.kde_codart, articulo.vta_min, articulo.vta_max, articulo.dctotope, articulo.enpreventa, articulo.vta_minenx, articulo.vta_solofac, articulo.vta_solone  FROM articulo LEFT JOIN  ke_kardex ON articulo.codigo = ke_kardex.kde_codart WHERE (existencia - comprometido) > 0 AND discont = 0.0 AND enpreventa ='1' AND articulo.empresa = '$codEmpresa' ORDER BY articulo.codigo ASC",
                null
            )
        }

        //select codigo, nombre from articulo
        // Cursor cursor = ke_android.rawQuery("SELECT articulo.codigo, articulo.nombre, articulo." + precioparametro + ", articulo.existencia, articulo.fechamodifi, ke_kardex.kde_codart, articulo.vta_min, articulo.vta_max, articulo.dctotope   FROM articulo LEFT JOIN  ke_kardex ON articulo.codigo = ke_kardex.kde_codart WHERE existencia > 0 AND discont = 0.0 AND enpreventa ='" + enpreventa + "'", null);
        cursor!!.moveToFirst()
        while (!cursor.isAfterLast) {
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
            val descuento = cursor.getDouble(8)
            catalogo.setDctotope(validarDescuento(descuento))
            catalogo.setEnpreventa(cursor.getString(9))
            catalogo.setMultiplo(cursor.getInt(10)) // <------------------------ TE QUEDASTE AQUI
            catalogo.setVta_solofac(cursor.getInt(11))
            catalogo.setVta_solone(cursor.getInt(12))
            vtaMin = cursor.getDouble(6) //VARIABLE EN DOUBLE DE VTA MIN
            vtaMax = cursor.getDouble(7) //VARIABLE EN DOUBLE DE VTA MAX
            listacatalogo!!.add(catalogo)
            cursor.moveToNext()
        }
        cursor.close()
        keAndroid.close()
    }

    private fun validarDescuento(descuento: Double): Double {
        return if (APP_DESCUENTOS_PEDIDOS) {
            descuento
        } else {
            0.0
        }
    }

    private fun consultarArticulosenPedido() {
        val keAndroid = conn.writableDatabase
        var catalogo: Catalogo
        var cursor: Cursor? = null
        listacatalogo = ArrayList()
        enpreventa = intent!!.getStringExtra("enpreventa")
        if (enpreventa == null || enpreventa == "") {
            enpreventa = "0"
        }
        if (enpreventa == "0") {
            cursor = keAndroid.rawQuery(
                "SELECT articulo.codigo, articulo.nombre, articulo.$tipoDePrecioaMostrar, articulo.existencia - articulo.comprometido, articulo.fechamodifi, ke_kardex.kde_codart, articulo.vta_min, articulo.vta_max, articulo.dctotope, articulo.enpreventa, articulo.vta_minenx, articulo.vta_solofac, articulo.vta_solone FROM articulo LEFT JOIN ke_kardex ON articulo.codigo = ke_kardex.kde_codart WHERE (existencia - comprometido) > 0 AND discont = 0.0 AND enpreventa = '' AND articulo.empresa = '$codEmpresa' ORDER BY articulo.codigo ASC",
                null
            )
        } else if (enpreventa == "1") {
            cursor = keAndroid.rawQuery(
                "SELECT articulo.codigo, articulo.nombre, articulo.$tipoDePrecioaMostrar, articulo.existencia - articulo.comprometido, articulo.fechamodifi, ke_kardex.kde_codart, articulo.vta_min, articulo.vta_max, articulo.dctotope, articulo.enpreventa, articulo.vta_minenx, articulo.vta_solofac, articulo.vta_solone FROM articulo LEFT JOIN ke_kardex ON articulo.codigo = ke_kardex.kde_codart WHERE (existencia - comprometido) > 0 AND discont = 0.0 AND enpreventa ='$enpreventa' AND articulo.empresa = '$codEmpresa' ORDER BY articulo.codigo ASC",
                null
            )
        }

        //select codigo, nombre from articulo
        while (cursor!!.moveToNext()) {
            catalogo = Catalogo()
            catalogo.setCodigo(cursor.getString(0))
            catalogo.setNombre(cursor.getString(1))
            val precio1 = cursor.getDouble(2)
            val precio1Rd = precio1.valorReal()
            catalogo.setPrecio1(precio1Rd)
            val existenc = cursor.getDouble(3)
            val existenciaRd = existenc.toInt()
            catalogo.setCodigoKardex(cursor.getString(5))
            catalogo.setExistencia(existenciaRd)
            catalogo.setVta_min(cursor.getDouble(6))
            catalogo.setVta_max(cursor.getDouble(7))
            val descuento = cursor.getDouble(8)
            catalogo.setDctotope(validarDescuento(descuento))
            catalogo.setEnpreventa(cursor.getString(9))
            catalogo.setMultiplo(cursor.getInt(10))
            catalogo.setVta_solofac(cursor.getInt(11))
            catalogo.setVta_solone(cursor.getInt(12))
            vtaMin = cursor.getDouble(6) //VARIABLE EN DOUBLE DE VTA MIN
            vtaMax = cursor.getDouble(7) //VARIABLE EN DOUBLE DE VTA MAX
            listacatalogo!!.add(catalogo)
        }
        cursor.close()
        keAndroid.close()
        catalogoAdapter = CatalogoAdapter(this@CatalogoActivity, listacatalogo, enlaceEmpresa)
        binding.lvArticulos.adapter = catalogoAdapter
        catalogoAdapter!!.notifyDataSetChanged()
    }

    companion object {
        var tipoDePrecioaMostrar: String? = null
        var preciomostrar = "precio1"
        var cod_cliente: String? = null
        var nroPedido: String? = null
        var nombreEmpresa: String? = null
        var enlaceEmpresa: String? = null
        var enpreventa: String? = "0"
        var precioTotalporArticulo: Double = 0.0
        var vtaMin: Double? = null
        var vtaMax: Double? = null
        var dctonumerico: Double? = null
        var stotdcto: Double? = null
    }

    override fun getTheme(): Resources.Theme {
        val theme = super.getTheme()
        theme.applyStyle(setThemeAgencia(Constantes.AGENCIA), true)
        // you could also use a switch if you have many themes that could apply
        return theme
    }

}