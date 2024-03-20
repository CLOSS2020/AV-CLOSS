package com.appcloos.mimaletin

import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.res.ColorStateList
import android.content.res.Resources
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.MenuItemCompat
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.appcloos.mimaletin.databinding.ActivityPedidosBinding
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.DecimalFormat
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

class PedidosActivity : AppCompatActivity() {
    private lateinit var conn: AdminSQLiteOpenHelper
    private var listapedidos: ArrayList<Pedidos>? = null
    private var lineasAdapter: LineasAdapter? = null
    var listainfo: ArrayList<String>? = null
    private var listapedido: ArrayList<String>? = null
    private var listalineas: ArrayList<Carrito>? = null
    private var listalineasdoc: ArrayList<Lineas>? = null
    private var pedidoAdapter: PedidoAdapter? = null
    private var sharpref: SharedPreferences? = null
    private lateinit var preferences: SharedPreferences

    private lateinit var binding: ActivityPedidosBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPedidosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setColors()

        requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED // mantener la orientacion vertical
        sharpref = getSharedPreferences("sharpref", MODE_PRIVATE)
        preferences = getSharedPreferences("Preferences", MODE_PRIVATE)
        cod_usuario = preferences.getString("cod_usuario", null)
        codEmpresa = preferences.getString("codigoEmpresa", null)
        conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null)
        lineasPedidos() // cargar las lineas del item donde se visualizan los pedidos.
        // sesion();
        // ValidezDeSesion("https://www.cloccidental.com/webservice/sesionactiva.php?cod_usuario=" +cod_usuario.trim());
        cargarEnlace()
        binding.btNuevop.setOnClickListener { _: View? ->
            limpiarCarrito()
            iraCreacionPedido()
        }
        /** */
        binding.lvPedidos.setOnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
            codigoPedido = listapedidos!![position].numeroDocumento
            n_cliente = listapedidos!![position].nombreCliente
            codigoCliente = listapedidos!![position].codigoCliente
            fechapedido = listapedidos!![position].fechaDocumento
            pedidonumero = listapedidos!![position].numeroPedido
            totneto = listapedidos!![position].totalNeto.toString()

            // System.out.println("ESTE ES EL ESTATUS QUE ESTA LLEGANDO DEL PEDIDO " + statusPedido);

            /*AlertDialog.Builder ventana = new AlertDialog.Builder(new ContextThemeWrapper(PedidosActivity.this, R.style.AlertDialogCustom));
            ventana.setTitle("Mensaje del sistema");
            ventana.setMessage("Por favor, elige una opción");

            ventana.setPositiveButton("Modificar Pedido", (dialogInterface, i) -> {
                if (statusPedido.equals("0")) {
                    LimpiarCarrito();
                    IraModificacionPedido();
                } else {
                    Toast.makeText(getBaseContext(), "Este pedido ya no puede ser modificado", Toast.LENGTH_LONG).show();
                }

            });

            ventana.setNegativeButton("Borrar Pedido", (dialogInterface, i) -> {
                if (statusPedido.equals("0")) {

                    AlertDialog.Builder subventana = new AlertDialog.Builder(new ContextThemeWrapper(PedidosActivity.this, R.style.AlertDialogCustom));
                    subventana.setTitle("Mensaje de confirmación");
                    subventana.setMessage("¿Estás seguro de borrar el pedido?");

                    subventana.setPositiveButton("Si", (dialogInterface1, i1) -> BorrarPedido());

                    subventana.setNegativeButton("No", (dialogInterface12, i12) -> Toast.makeText(PedidosActivity.this, "Eliminación cancelada", Toast.LENGTH_SHORT).show());

                    AlertDialog dialogo2 = subventana.create();
                    dialogo2.show();
                } else if (statusPedido.equals("5")) {

                    BorrarPedidoAlt();
                    Toast.makeText(getBaseContext(), "Este pedido fue borrado solamente del dispositivo.", Toast.LENGTH_LONG).show();
                    //Toast.makeText(getBaseContext(), "Este pedido fue borrado solamente del dispositivo.", Toast.LENGTH_LONG).show();
                }

            });

            ventana.setNeutralButton("Ver Pedido", (dialogInterface, i) -> {

                AlertDialog.Builder dialogofull = new AlertDialog.Builder(new ContextThemeWrapper(PedidosActivity.this, R.style.AlertDialogCustom));

                //dialogofull.setTitle(codigoPedido);

                TextView title = new TextView(PedidosActivity.this);
                title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                title.setTypeface(Typeface.DEFAULT_BOLD);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(40, 20, 0, 30);
                title.setPadding(40, 30, 0, 40);
                title.setLayoutParams(lp);
                title.setText("Pedido: " + codigoPedido);
                dialogofull.setCustomTitle(title);

                ListView listadeLineas = new ListView(PedidosActivity.this);
                listadeLineas.setHeaderDividersEnabled(true);

                CargarLineasdelPedido();
                lineasAdapter = new LineasAdapter(PedidosActivity.this, listalineasdoc);
                listadeLineas.setAdapter(lineasAdapter);
                lineasAdapter.notifyDataSetChanged();
                dialogofull.setView(listadeLineas);

                //ListView listadeLineas = new ListView(PedidosActivity.this);
                //CargarLineasdelPedido();
                //ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(PedidosActivity.this, R.layout.list_items, listapedido);
                //listadeLineas.setAdapter(arrayAdapter);
                //dialogofull.setView(listadeLineas);
                AlertDialog dialogoverpedido = dialogofull.create();
                dialogoverpedido.show();
            });

            ventana.setPositiveButton("Generar PDF", (dialogInterface, i) -> {});


            AlertDialog dialogo = ventana.create();
            dialogo.show();*/
            val builder = AlertDialog.Builder(this)
            val customView = LayoutInflater.from(this).inflate(R.layout.custom_dialog_pedidos, null)
            builder.setView(customView)
            val btnVer = customView.findViewById<Button>(R.id.btnVer)
            val btnModificar = customView.findViewById<Button>(R.id.btnModificar)
            val btnEliminar = customView.findViewById<Button>(R.id.btnEliminar)
            val btnPdf = customView.findViewById<Button>(R.id.btnPdf)
            val btnEspera = customView.findViewById<Button>(R.id.btnEspera)
            val creacion = builder.create()
            creacion.show()

            colorButton(btnVer)
            colorButton(btnModificar)
            colorButton(btnEliminar)
            colorButton(btnPdf)

            btnVer.setOnClickListener { _: View? -> verPedido() }
            btnModificar.setOnClickListener { _: View? -> modificarPedido(codigoPedido!!) }
            btnEliminar.setOnClickListener { _: View? ->
                eliminarPedido(creacion, codigoPedido!!)
            }
            btnPdf.setOnClickListener { _: View? -> crearPDF(codigoPedido) }
            btnEspera.setOnClickListener { pedidoEspera(codigoPedido!!) { creacion.dismiss() } }
        }
        val objetoAux = ObjetoAux(this)
        objetoAux.descargaDesactivo(cod_usuario!!, codEmpresa!!, enlaceEmpresa)
    }

    private fun pedidoEspera(codigoPedido: String, dismissDialog: () -> Unit) {
        val currentStatus = conn.getCampoIntCamposVarios(
            "ke_opti",
            "kti_status",
            listOf("kti_ndoc", "empresa"),
            listOf(codigoPedido, codEmpresa!!)
        )

        when (currentStatus) {
            0 -> {
                val cv = ContentValues()
                cv.put("kti_status", 99)
                conn.updateJSONCamposVarios(
                    "ke_opti",
                    cv,
                    "kti_ndoc = ? AND empresa = ?",
                    arrayOf(codigoPedido, codEmpresa)
                )
                toast("Pedido $codigoPedido Por subir")
            }

            99 -> {
                val cv = ContentValues()
                cv.put("kti_status", 0)
                conn.updateJSONCamposVarios(
                    "ke_opti",
                    cv,
                    "kti_ndoc = ? AND empresa = ?",
                    arrayOf(codigoPedido, codEmpresa)
                )
                toast("Pedido $codigoPedido En espera")
            }

            else -> {
                toast("No se puede cambiar el estado el pedido $codigoPedido")
            }
        }
        lineasPedidos()
        dismissDialog()
    }

    private fun cargarEnlace() {
        val keAndroid = conn.writableDatabase
        val columnas = arrayOf(
            "kee_nombre," + "kee_url," + "kee_sucursal"
        )
        val cursor = keAndroid.query(
            "ke_enlace",
            columnas,
            "kee_codigo = '$codEmpresa'",
            null,
            null,
            null,
            null
        )
        while (cursor.moveToNext()) {
            nombreEmpresa = cursor.getString(0)
            enlaceEmpresa = cursor.getString(1)
            codigoSucursal = cursor.getString(2)
        }
        cursor.close()
    }

    private fun limpiarCarrito() {
        conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null)
        val keAndroid = conn.writableDatabase
        try {
            keAndroid.delete(
                "ke_carrito",
                "empresa = '$codEmpresa'",
                null
            ) // <- no deberia importar si se coloca empresa
        } catch (e: Exception) {
            println("--Error--")
            e.printStackTrace()
            println("--Error--")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.menu_pedidos_main, menu)
        val menuItem = menu.findItem(R.id.search_view_catalogo)
        val buscador = MenuItemCompat.getActionView(menuItem) as SearchView
        buscador.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(busqueda: String): Boolean {
                cargarPedidos(busqueda)
                return true
            }

            override fun onQueryTextChange(busqueda: String): Boolean {
                cargarPedidos(busqueda)
                return true
            }
        })

        return super.onCreateOptionsMenu(menu)
    }

    private fun buscarPedido(busqueda: String) {
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.validarSubidas -> validarPendientes(
                "https://cloccidental.com/Rest/obtenerpedidosdelmes.php?cod_usuario=$cod_usuario&&agencia=$codigoSucursal"
            )

            R.id.archivados -> iraArchivados()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun validarPendientes(url: String) {
        // cada vez que se ejecute, vacio la lista para asegurarme que los datos no se repitan
        listainfo = ArrayList()
        val jsonArrayRequest: JsonArrayRequest =
            object : JsonArrayRequest(
                url,
                Response.Listener { response: JSONArray? ->
                    if (response != null) {
                        // preparo los datos para la conexion a la base de datos
                        conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null)
                        val ke_android = conn.writableDatabase
                        var jsonObject: JSONObject
                        // mientras la respuesta sea mayor a 0
                        for (i in 0 until response.length()) {
                            try {
                                // guardo lo que viene del jsonobject
                                jsonObject = response.getJSONObject(i)
                                // y lo agrego como un elemento string a la lista
                                listainfo!!.add(jsonObject.getString("kti_ndoc"))
                            } catch (ex: Exception) {
                                ex.printStackTrace()
                            }
                        }
                        // Hago la consulta para determinar que pedidos del mes estan "procesados"
                        // y que voy a validar que esten en la nube.
                        val tabla = "ke_opti"
                        val columnas = arrayOf(
                            "kti_ndoc," + "datetime('now','start of month') as principiomes," + "  datetime('now') as hoy"
                        )
                        val condicion = "kti_status = '1' AND kti_fchdoc BETWEEN principiomes AND hoy"
                        val cursor =
                            ke_android.query(tabla, columnas, condicion, null, null, null, null)
                        while (cursor.moveToNext()) {
                            val pedidoEnTelf = cursor.getString(0)
                            println("PEDIDO EN SISTEMA: $pedidoEnTelf")
                            if (pedidoEnTelf != "") {
                                if (!listainfo!!.contains(pedidoEnTelf)) {
                                    ke_android.beginTransaction()
                                    try {
                                        println("Este pedido no se encuentra en la nube, debe subirse")
                                        ke_android.execSQL(
                                            "UPDATE ke_opti SET kti_status = '0' WHERE kti_ndoc ='$pedidoEnTelf' AND empresa = '$codEmpresa'"
                                        )
                                        ke_android.setTransactionSuccessful()
                                        ke_android.endTransaction()
                                        finish()
                                        this@PedidosActivity.overridePendingTransition(0, 0)
                                        startActivity(this@PedidosActivity.intent)
                                        this@PedidosActivity.overridePendingTransition(
                                            0,
                                            0
                                        ) // para refrescar el RecyclerView
                                    } catch (ex: Exception) {
                                        ke_android.endTransaction()
                                    }
                                } else {
                                    println("Este pedido ya está en la nube")
                                }
                            } else {
                                println("No hay pedidos por subir")
                            }
                        }
                        cursor.close()
                    }
                },
                Response.ErrorListener { error: VolleyError ->
                    println("--Error--")
                    error.printStackTrace()
                    println("--Error--")
                }
            ) {
                override fun getParams(): Map<String, String> {
                    // finalmente, estos son los parametros que le enviaremos al webservice, partiendo de las variables
                    // donde estan guardados las fechas
                    val parametros: MutableMap<String, String> = HashMap()
                    parametros["cod_usuario"] = cod_usuario!!
                    return parametros
                }
            }
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(jsonArrayRequest)
        // esto es el request que se envia al url a traves de la conexion volley, (el stringrequest esta armado arriba)
    }

    private fun iraArchivados() {
        sharpref!!.edit().clear().apply()
        val intent = Intent(this@PedidosActivity, PedidosArchivadosActivity::class.java)
        intent.putExtra("cod_usuario", cod_usuario)
        startActivity(intent)
    }

    private fun iraCreacionPedido() {
        sharpref!!.edit().clear().apply()
        val intent = Intent(this@PedidosActivity, SeleccionarClientePedidoActivity::class.java)
        startActivity(intent)
    }

    private fun iraModificacionPedido() {
        limpiarCarrito()
        sharpref!!.edit().clear().apply()
        val intent = Intent(this@PedidosActivity, ModificarPedidoActivity::class.java)
        intent.putExtra("codigopedido", codigoPedido)
        intent.putExtra("codigocliente", codigoCliente)
        intent.putExtra("codigoEmpresa", codEmpresa)
        intent.putExtra("n_cliente", n_cliente)
        startActivity(intent)
    }

    /** */
    private fun lineasPedidos() {
        cargarPedidos()
        if (listapedidos != null) {
            pedidoAdapter = PedidoAdapter(this@PedidosActivity, listapedidos!!)
            binding.lvPedidos.adapter = pedidoAdapter
            pedidoAdapter!!.notifyDataSetChanged()
        } else {
            // System.out.println("pedidos vacios");
        }
    }

    private fun cargarPedidos(text: String? = null) {
        listapedidos = ArrayList()
        conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null)
        val keAndroid = conn.writableDatabase
        val cursor: Cursor = if (text == null) {
            keAndroid.rawQuery(
                """SELECT kti_codcli, kti_ndoc, kti_nombrecli, kti_fchdoc, kti_totneto, kti_status, kti_nroped, kti_totnetodcto, datetime('now','start of month') as principiomes,
      datetime('now') as hoy, ke_pedstatus, kti_docsol, dolarflete FROM ke_opti WHERE kti_status !='3' AND empresa = '$codEmpresa' AND kti_codven = '$cod_usuario' and kti_fchdoc BETWEEN principiomes AND hoy
      ORDER BY kti_ndoc DESC""",
                null
            )
        } else {
            keAndroid.rawQuery(
                """SELECT kti_codcli, kti_ndoc, kti_nombrecli, kti_fchdoc, kti_totneto, kti_status, kti_nroped, kti_totnetodcto, datetime('now','start of month') as principiomes,
      datetime('now') as hoy, ke_pedstatus, kti_docsol, dolarflete FROM ke_opti WHERE kti_nombrecli LIKE '%$text%' AND kti_status !='3' AND empresa = '$codEmpresa' AND kti_codven = '$cod_usuario' and kti_fchdoc BETWEEN principiomes AND hoy
      ORDER BY kti_ndoc DESC""",
                null
            )
        }
        while (cursor.moveToNext()) {
            val estatusEval = cursor.getString(5)
            val estatusPed = cursor.getString(10)
            when (estatusEval) {
                "0" -> {
                    pedido_estatus = "Por Subir"
                    statusPedido = "0"
                }

                "99" -> {
                    pedido_estatus = "En espera"
                    statusPedido = "0"
                }

                "1", "2" -> {
                    pedido_estatus = "Subido"
                    statusPedido = "1"
                }
            }
            when (estatusPed) {
                "01" -> {
                    pedido_estatus = if (nropedido == null || nropedido!!.isEmpty()) {
                        "Esperando por Aprobación"
                    } else {
                        "Procesando Pedido"
                    }
                    statusPedido = "5"
                }

                "12" -> {
                    pedido_estatus = "Ya impreso"
                    statusPedido = "5"
                }

                "17" -> {
                    pedido_estatus = "En Proceso de embalaje"
                    statusPedido = "5"
                }

                "20" -> {
                    pedido_estatus = "En Proceso de etiquetado"
                    statusPedido = "5"
                }

                "25" -> {
                    pedido_estatus = "Listo Para facturar"
                    statusPedido = "5"
                }

                "80" -> {
                    pedido_estatus = "Facturado"
                    statusPedido = "5"
                }

                "82" -> {
                    pedido_estatus = "Esperando orden de salida"
                    statusPedido = "5"
                }

                "85" -> {
                    pedido_estatus = "Entregado al cliente"
                    statusPedido = "5"
                }
            }

            nropedido = cursor.getString(6) ?: "Por Asignar"

            val pedido = Pedidos()
            pedido.codigoCliente = cursor.getString(0)
            pedido.numeroDocumento = cursor.getString(1)
            pedido.nombreCliente = cursor.getString(2)
            pedido.fechaDocumento = cursor.getString(3)
            pedido.totalNeto = cursor.getDouble(4)
            pedido.estatus = pedido_estatus.toString()
            pedido.numeroPedido = nropedido as String
            pedido.totalNetoDcto = cursor.getDouble(7)
            pedido.docSolicitado = cursor.getString(11)
            pedido.dolarflete = cursor.getInt(12) == 1
            listapedidos!!.add(pedido)
        }
        cursor.close()
        keAndroid.close()

        if (listapedidos != null) {
            pedidoAdapter = PedidoAdapter(this@PedidosActivity, listapedidos!!)
            binding.lvPedidos.adapter = pedidoAdapter
            pedidoAdapter!!.notifyDataSetChanged()
        }
    }

    private fun cargarLineasdelPedido() {
        listalineas = ArrayList()
        listalineasdoc = ArrayList()
        // System.out.println(cod_usuario);
        conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null)
        val keAndroid = conn.writableDatabase
        val cursor = keAndroid.rawQuery(
            "SELECT kmv_codart, kmv_nombre, kmv_cant, kmv_stot, kmv_artprec FROM ke_opmv WHERE kti_ndoc='$codigoPedido' AND empresa = '$codEmpresa';",
            null
        )
        while (cursor.moveToNext()) {
            val carrito = Carrito()
            carrito.setCodigo(cursor.getString(0))
            carrito.setNombre(cursor.getString(1))
            carrito.setCantidad(cursor.getInt(2))
            carrito.setPrecio(cursor.getDouble(3))
            carrito.setPreciou(cursor.getDouble(4))
            listalineas!!.add(carrito)
            val lineas = Lineas()
            lineas.setCodigo(cursor.getString(0))
            lineas.setNombre(cursor.getString(1))
            lineas.setCantidad(cursor.getDouble(2))
            lineas.setDpreciofin(cursor.getDouble(3))
            lineas.setDmontototal(cursor.getDouble(3))
            listalineasdoc!!.add(lineas)
        }
        cursor.close()
        keAndroid.close()
        obtenerlineas()
    }

    private fun obtenerlineas() {
        listapedido = ArrayList()
        for (i in listalineas!!.indices) {
            listapedido!!.add(
                """Codigo: ${listalineas!![i].getCodigo()}
 ${listalineas!![i].getNombre()}
Cantidad: ${listalineas!![i].getCantidad()} Precio: ${"$"}${listalineas!![i].getPrecio()}"""
            )
        }
    }

    private fun BorrarPedido(codigoPedido: String) {
        val ke_android = conn.writableDatabase/*
        PARTE DEL COMPROMETIDO
        Cursor cursor_s = ke_android.rawQuery("SELECT kmv_codart, kmv_cant FROM ke_opmv WHERE kti_ndoc ='" + codigoPedido + "'", null);

        while (cursor_s.moveToNext()){

            String codigo = cursor_s.getString(0);
            Double cantidad = cursor_s.getDouble(1);

            Cursor cursor_s_2 = ke_android.rawQuery("SELECT comprometido FROM articulo WHERE codigo ='" + codigo + "'", null);
            cursor_s_2.moveToFirst();
            Double comprometido = cursor_s_2.getDouble(0);

            ke_android.execSQL("UPDATE articulo SET comprometido = "+ (comprometido - cantidad) +" WHERE codigo ='"+ codigo +"'");
        }
*/
        // en realidad le cambiamos la cabecera a "3" Y borramos solo las lineas
        ke_android.execSQL(
            "UPDATE ke_opti SET kti_status = '3' WHERE kti_ndoc ='$codigoPedido' AND empresa = '$codEmpresa'"
        )
        ke_android.execSQL("DELETE FROM ke_opmv WHERE kti_ndoc ='$codigoPedido' AND empresa = '$codEmpresa'")
        ke_android.execSQL("DELETE FROM ke_limitart WHERE kli_track ='$codigoPedido' AND empresa = '$codEmpresa'")
        Toast.makeText(this@PedidosActivity, "Pedido borrado", Toast.LENGTH_SHORT).show()
        lineasPedidos()
        ke_android.close()
    }

    private fun borrarPedidoAlt() {
        val keAndroid = conn.writableDatabase
        // en realidad le cambiamos la cabecera a "3" Y borramos solo las lineas
        keAndroid.execSQL(
            "UPDATE ke_opti SET kti_status = '3' WHERE kti_ndoc ='$codigoPedido' AND empresa = '$codEmpresa';"
        )
        keAndroid.execSQL("DELETE FROM ke_opmv WHERE kti_ndoc = '$codigoPedido' AND empresa = '$codEmpresa';")
        Toast.makeText(this@PedidosActivity, "Pedido borrado", Toast.LENGTH_SHORT).show()
        lineasPedidos()
        keAndroid.close()
    }

    fun sesion() {
        val keAndroid = conn.writableDatabase
        val cursor = keAndroid.rawQuery(
            "SELECT sesionactiva FROM usuarios WHERE vendedor ='$cod_usuario' AND empresa = '$codEmpresa';",
            null
        )
        while (cursor.moveToNext()) {
            sesionActiva = cursor.getString(0).trim { it <= ' ' }
        }
        cursor.close()
    }

    private fun cerrarsesion() {
        val preferences = getSharedPreferences("Preferences", MODE_PRIVATE)
        preferences.edit().clear().apply()
        // PrincipalActivity.getInstance().finish()
        val intent = Intent(applicationContext, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun ValidezDeSesion(URL: String) {
        val jsonArrayRequest: JsonArrayRequest =
            object : JsonArrayRequest(
                URL,
                Response.Listener { response: JSONArray? ->
                    if (response != null) {
                        var jsonObject: JSONObject // creamos un objeto json vacio
                        for (i in 0 until response.length()) {
                            try {
                                jsonObject = response.getJSONObject(i)
                                sesionNube = jsonObject.getString("sesionactiva").trim { it <= ' ' }
                                println(sesionNube)
                                if (sesionNube != sesionActiva) {
                                    val ventana = AlertDialog.Builder(
                                        ContextThemeWrapper(
                                            this@PedidosActivity,
                                            R.style.AlertDialogCustom
                                        )
                                    )
                                    ventana.setTitle("Alerta del sistema:")
                                    ventana.setMessage(
                                        "Su sesión ha expirado porque existe otra activa, será redireccionado a la pantalla de inicio"
                                    )
                                    ventana.setPositiveButton("Aceptar") { _: DialogInterface?, _: Int ->
                                        cerrarsesion()
                                        finish()
                                    }
                                    val dialogo = ventana.create()
                                    dialogo.show()
                                } else {
                                    println(
                                        "La sesion es la misma"
                                    ) /* Toast.makeText(getApplicationContext(), "La sesion es la misma", Toast.LENGTH_LONG).show();*/
                                }
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }
                    } else {
                        // Toast.makeText(getApplicationContext(), "empty", Toast.LENGTH_LONG).show();
                    }
                },
                Response.ErrorListener { error: VolleyError ->
                    println("--Error--")
                    error.printStackTrace()
                    println("--Error--")
                }
            ) {
                override fun getParams(): Map<String, String> {
                    return HashMap()
                }
            }
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(
            jsonArrayRequest
        ) // esto es el request que se envia al url a traves de la conexion volley, (el stringrequest esta armado arriba
    }

    override fun onResume() {
        super.onResume()
        lineasPedidos()
        preferences = getSharedPreferences("Preferences", MODE_PRIVATE)
        cod_usuario = preferences.getString("cod_usuario", null)
    }

    override fun onBackPressed() {
        val intent = Intent(applicationContext, PrincipalActivity::class.java)

        // Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent)
        finish()
        super.onBackPressed()
    }

    private fun verPedido() {
        val dialogofull = AlertDialog.Builder(
            ContextThemeWrapper(
                this@PedidosActivity,
                R.style.AlertDialogCustom
            )
        )

        // dialogofull.setTitle(codigoPedido);
        val title = TextView(this@PedidosActivity)
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
        title.typeface = Typeface.DEFAULT_BOLD
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        lp.setMargins(40, 20, 0, 30)
        title.setPadding(40, 30, 0, 40)
        title.layoutParams = lp
        title.text = "Pedido: $codigoPedido"
        dialogofull.setCustomTitle(title)
        val listadeLineas = ListView(this@PedidosActivity)
        listadeLineas.setHeaderDividersEnabled(true)
        cargarLineasdelPedido()
        lineasAdapter = LineasAdapter(this@PedidosActivity, listalineasdoc!!)
        listadeLineas.adapter = lineasAdapter
        lineasAdapter!!.notifyDataSetChanged()
        dialogofull.setView(listadeLineas)

        // ListView listadeLineas = new ListView(PedidosActivity.this);
        // CargarLineasdelPedido();
        // ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(PedidosActivity.this, R.layout.list_items, listapedido);
        // listadeLineas.setAdapter(arrayAdapter);
        // dialogofull.setView(listadeLineas);
        val dialogoverpedido = dialogofull.create()
        dialogoverpedido.show()
    }

    private fun modificarPedido(codigoPedido: String) {
        // 2024-01-24 ahora se busca el estatus del pedido directamente de la base de datos
        // esto es debido a que la variable que se usaba solo guarda el ultimo pedido de la lista
        // se debe de evaluar por individual dicho estatus
        val estatusPedido = conn.getCampoStringCamposVarios(
            "ke_opti",
            "kti_status",
            listOf("kti_ndoc", "empresa"),
            listOf(codigoPedido, codEmpresa!!)
        )
        if (estatusPedido == "0") {
            limpiarCarrito()
            iraModificacionPedido()
        } else {
            Toast.makeText(baseContext, "Este pedido ya no puede ser modificado", Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun eliminarPedido(creacion: AlertDialog, codigoPedido: String) {
        val estatusPedido = conn.getCampoStringCamposVarios(
            "ke_opti",
            "kti_status",
            listOf("kti_ndoc", "empresa"),
            listOf(codigoPedido, codEmpresa!!)
        )
        if (estatusPedido == "0") {
            val subventana = AlertDialog.Builder(
                ContextThemeWrapper(
                    this@PedidosActivity,
                    R.style.AlertDialogCustom
                )
            )
            subventana.setTitle("Mensaje de confirmación")
            subventana.setMessage("¿Estás seguro de borrar el pedido?")
            subventana.setPositiveButton("Si") { _: DialogInterface?, _: Int ->
                BorrarPedido(codigoPedido)
            }
            subventana.setNegativeButton("No") { _: DialogInterface?, _: Int ->
                Toast.makeText(
                    this@PedidosActivity,
                    "Eliminación cancelada",
                    Toast.LENGTH_SHORT
                ).show()
            }
            val dialogo2 = subventana.create()
            dialogo2.show()

            val pbutton: Button = dialogo2.getButton(DialogInterface.BUTTON_POSITIVE)
            pbutton.apply {
                setTextColor(colorTextAgencia(Constantes.AGENCIA))
            }

            val nbutton: Button = dialogo2.getButton(DialogInterface.BUTTON_NEGATIVE)
            nbutton.apply {
                setTextColor(colorTextAgencia(Constantes.AGENCIA))
            }
        } else if (estatusPedido == "5") {
            borrarPedidoAlt()
            Toast.makeText(
                baseContext,
                "Este pedido fue borrado solamente del dispositivo.",
                Toast.LENGTH_LONG
            ).show()
            // Toast.makeText(getBaseContext(), "Este pedido fue borrado solamente del dispositivo.", Toast.LENGTH_LONG).show();
        }
        creacion.dismiss()
    }

    private fun crearPDF(codigoPedido: String?) {
        val nroArtPag = 30.0

        val cabecera = conn.getCabeceraPedido(codigoPedido!!, codEmpresa!!)
        val lineas = conn.getLineasPedido(codigoPedido, codEmpresa!!)

        val nroPag = ceil(lineas.size / nroArtPag).toInt()

        val nombreEmpresa = nombreEmpresa(codEmpresa)
        val rifEmpresa = rifEmpresa(codEmpresa)
        val dirEmpresa1 = "CALLE 18 CON AV GOAJIRA VIA EL MOJAN, LOCALGALPON 3, ZONA"
        val dirEmpresa2 = "INDUSTRIAL NORTE, COMPLEJO PARQUE INDUSTRIAL NORTE,"
        val dirEmpresa3 = "MARACAIBO ZULIA POSTAL 4001"
        val direccion = direccionEmpresa(codEmpresa)
        val tipoDoc = "Pedido"
        val subTipoDoc = "Prototipo Pedido"

        val dolarFlete = if (cabecera.dolarFlete == 1) "FD" else ""

        val nroPedido = if (cabecera.ktiStatus == "0") {
            "*PED:*$codigoPedido*"
        } else {
            "PED:$codigoPedido"
        }

        val tipoDocSol = if (cabecera.ktiDocsol == "1") {
            this.getString(R.string.ped_fac)
        } else {
            this.getString(R.string.ped_nota)
        }

        val totalNeto = if (cabecera.ktiStatus == "0") {
            "*${cabecera.ktiTotnetodcto.toTwoDecimals()}*"
        } else {
            cabecera.ktiTotnetodcto.toTwoDecimals()
        }

        var pagActual = 1

        var primerIndice = 0
        var ultimoIndice = nroArtPag.toInt()

        val document = PdfDocument()
        val paint = Paint()
        // conf inicial de la pag
        val pageInfo = PdfDocument.PageInfo.Builder(612, 792, nroPag).create()
        // Creacion del objeto pagina
        var page: PdfDocument.Page

        // For que crea la cantidad de paginas que vea necesario crear para mostrar todos los
        // articos seleccionados
        for (i in 1..nroPag) {
            // Inicializando la pagina
            page = document.startPage(pageInfo)
            // Inicializando el lienzo
            val canvas = page.canvas

            // del obj paint
            paint.textAlign = Paint.Align.LEFT
            paint.textSize = 16f
            paint.color = Color.BLACK
            paint.typeface = Typeface.createFromAsset(this.assets, "font/arialbd.ttf")

            // CABECERA
            // imagen del la cabecera
            val bmp = BitmapFactory.decodeResource(this.resources, plantillaPDF(codEmpresa))
            val scaledBitmap = Bitmap.createScaledBitmap(bmp, 612, 792, false)
            canvas.drawBitmap(scaledBitmap, 0f, 0f, paint)

            // ICONO DE LA EMPRESA
            val iconEmpresa = BitmapFactory.decodeResource(this.resources, logoPDF(codEmpresa))
            val scaledBtmpEmpresa = Bitmap.createScaledBitmap(iconEmpresa, 100, 100, false)
            canvas.drawBitmap(scaledBtmpEmpresa, 5f, 15f, paint)

            // titulos de la cabecera
            canvas.drawText(nombreEmpresa, 105f, 35f, paint)

            // RIF Empresa
            paint.textSize = 10f
            canvas.drawText(rifEmpresa, 105f, 45f, paint)

            // Direccion Empresa
            paint.typeface = Typeface.createFromAsset(this.assets, "font/arial.ttf")
            var y = 55f
            direccion.forEach { direc ->
                canvas.drawText(direc, 105f, y, paint)
                y += 10
            }
            // canvas.drawText(direccion, 105f, 55f, paint)
            // canvas.drawText(dirEmpresa2, 105f, 65f, paint)
            // canvas.drawText(dirEmpresa3, 105f, 75f, paint)

            // Tipo de documento
            paint.textAlign = Paint.Align.RIGHT
            paint.textSize = 15f
            paint.color = Color.rgb(7, 4, 97)
            paint.typeface = Typeface.createFromAsset(this.assets, "font/arialbd.ttf")
            canvas.drawText(tipoDoc, 597f, 60f, paint)

            // Subtipo de documento
            paint.textSize = 13f
            canvas.drawText(subTipoDoc, 597f, 75f, paint)

            // Numero de Rrecibo
            paint.color = Color.RED
            paint.textSize = 16f
            canvas.drawText(nroPedido, 597f, 100f, paint)

            // Fecha de creacion
            paint.textSize = 12f
            paint.color = Color.BLACK
            paint.typeface = Typeface.createFromAsset(this.assets, "font/arialbd.ttf")
            canvas.drawText("Emisión", 480f, 115f, paint)

            paint.typeface = Typeface.createFromAsset(this.assets, "font/arial.ttf")
            canvas.drawText(cabecera.ktiFchdoc.formatoFechaTiempoShow(), 597f, 115f, paint)

            // Tipo de Documento, FAC o N/E
            paint.typeface = Typeface.createFromAsset(this.assets, "font/arialbd.ttf")
            canvas.drawText("Tipo de documento: $tipoDocSol $dolarFlete", 597f, 130f, paint)

            // Condicion de pago, Credito o BCV
            paint.textSize = 14f
            canvas.drawText("*El documento va sin IVA y sin flete*", 597f, 145f, paint)

            paint.textSize = 11f
            paint.textAlign = Paint.Align.LEFT
            paint.typeface = Typeface.createFromAsset(this.assets, "font/arialbd.ttf")
            canvas.drawText("[ Notas del documento ]", 60f, 131f, paint)

            val iconMarco = BitmapFactory.decodeResource(this.resources, R.drawable.marco)
            val scaledBtmpMarco = Bitmap.createScaledBitmap(iconMarco, 320, 60, false)
            canvas.drawBitmap(scaledBtmpMarco, 15f, 126f, paint)

            paint.textSize = 11f
            paint.textAlign = Paint.Align.LEFT
            paint.typeface = Typeface.createFromAsset(this.assets, "font/arialbd.ttf")
            canvas.drawText("Sujeto a cambios, disponibilidad de productos", 30f, 150f, paint)

            canvas.drawText("y aprobación", 30f, 165f, paint)

            // Pedido Generado
            paint.textAlign = Paint.Align.LEFT
            paint.textSize = 11f
            paint.typeface = Typeface.createFromAsset(this.assets, "font/arialbd.ttf")
            canvas.drawText(
                "Pedido generado ${if (cabecera.ktiStatus == "0") "" else "y subido"}",
                30f,
                220f,
                paint
            )

            // Monto Neto Total
            paint.textSize = 14f
            paint.typeface = Typeface.createFromAsset(this.assets, "font/arial.ttf")
            canvas.drawText("Monto neto total", 400f, 220f, paint)

            // Total de la suma de los articulos
            paint.typeface = Typeface.createFromAsset(this.assets, "font/arialbd.ttf")
            canvas.drawText(totalNeto, 510f, 220f, paint)

            // Linea divisora superior
            val iconLinea = BitmapFactory.decodeResource(this.resources, R.drawable.linea)
            val scaledBtmpLinea = Bitmap.createScaledBitmap(iconLinea, 595, 5, false)
            canvas.drawBitmap(scaledBtmpLinea, 5f, 230f, paint)

            // Linea divisora entre la cabecera del cuadro y las lineas
            val iconLineaD = BitmapFactory.decodeResource(this.resources, R.drawable.linea_d)
            val scaledBtmpLineaD = Bitmap.createScaledBitmap(iconLineaD, 570, 5, false)
            canvas.drawBitmap(scaledBtmpLineaD, 18f, 247.5f, paint)

            // Cabecera del cuadro
            paint.textSize = 12f
            paint.typeface = Typeface.createFromAsset(this.assets, "font/arial.ttf")
            canvas.drawText("Código", 15f, 245f, paint)
            canvas.drawText("Artículo", 65f, 245f, paint)
            canvas.drawText("Cantidad", 300f, 245f, paint)
            canvas.drawText("Precio Uni.", 360f, 245f, paint)
            canvas.drawText("Precio Tot.", 430f, 245f, paint)
            canvas.drawText("Desc.%", 500f, 245f, paint)
            canvas.drawText("Desc.", 550f, 245f, paint)

            // Variable que sumara y bajara las nuevas lineas del pedido
            var counter = 265f

            // For que crea las lineas del pedido
            for (k in primerIndice..ultimoIndice) {
                // salgo del for al llegar al limite de articulos por pagina
                if (k == (nroArtPag.toInt() * pagActual)) {
                    // Marca el numero de paginas
                    canvas.drawText("$pagActual/$nroPag", 570f, 770f, paint)
                    // Sumo 1 a la pagina actual debido a que cambio de pagina
                    pagActual++
                    // Cuando k llegue a su limite este malor ahora estara en el primerIndice
                    primerIndice = k
                    // A ultimo indice se le suma el mismo y se le agrega el numero limite de articulos
                    // por pagina
                    ultimoIndice += nroArtPag.toInt()
                    // Sale del for para iniciar una nueva pagina
                    break
                }

                // Cuando k llega al total de lineas se sale del for y el for principal muere
                if (k == lineas.size) {
                    // Marca el numero de paginas
                    canvas.drawText("$pagActual/$nroPag", 570f, 770f, paint)
                    break
                }

                // Resumiendo el nombre del articulo
                var nomArt = ""
                if (lineas[k].kmvNombre.length > 30) {
                    for (j in 0..29) {
                        nomArt += lineas[k].kmvNombre[j]
                    }
                    nomArt += "..."
                } else {
                    nomArt = lineas[k].kmvNombre
                }

                // Mostrando codigo y nombre del articulo
                canvas.drawText(lineas[k].kmvCodart, 15f, counter, paint)
                canvas.drawText(nomArt, 65f, counter, paint)

                // Mostrando datos numericos del pedido en funciones apartes para mejor visualizacion
                // Cantidad del producto
                canvas.insertarNumPDF(lineas[k].kmvCant, 300f, counter, paint)
                // insertarNumPDF(canvas, lineas[k].kmv_cant, 300f, counter, paint)
                // Precio unitario del articulo
                canvas.insertarNumPDF(lineas[k].kmvArtprec, 360f, counter, paint)
                // insertarNumPDF(canvas, lineas[k].kmv_artprec, 360f, counter, paint)
                // Precio = cantidad * precio unitario
                canvas.insertarNumPDF(lineas[k].kmvStot, 430f, counter, paint)
                // insertarNumPDF(canvas, lineas[k].kmv_stot, 430f, counter, paint)
                // Porcentaje de descuento
                canvas.insertarNumPDF(lineas[k].kmvDctolin, 490f, counter, paint)
                // insertarNumPDF(canvas, lineas[k].kmv_dctolin, 490f, counter, paint)
                // Precio = cantidad * precio unitario * descuento%
                canvas.insertarNumPDF(lineas[k].kmvStotdcto, 540f, counter, paint)
                // insertarNumPDF(canvas, lineas[k].kmv_stotdcto.valorReal(), 540f, counter, paint)

                // Suma a la variable counter para bajar lineas
                counter = counter.plus(15f)
            }
            // Divisor final del cuadro
            canvas.drawBitmap(scaledBtmpLineaD, 18f, counter - 13, paint)

            // Datos del Cliente
            paint.textSize = 11f
            paint.typeface = Typeface.createFromAsset(this.assets, "font/arialbd.ttf")
            canvas.drawText("Cliente : ", 25f, counter, paint)
            paint.typeface = Typeface.createFromAsset(this.assets, "font/arial.ttf")
            canvas.drawText(cabecera.ktiCodcli, 75f, counter, paint)
            canvas.drawText(cabecera.ktiNombrecli, 25f, counter + 15f, paint)

            // Datos del Vendedor
            paint.typeface = Typeface.createFromAsset(this.assets, "font/arialbd.ttf")
            canvas.drawText("Vendedor : ", 25f, counter + 30f, paint)
            paint.typeface = Typeface.createFromAsset(this.assets, "font/arial.ttf")
            canvas.drawText(cabecera.ktiCodven, 90f, counter + 30f, paint)
            canvas.drawText(
                conn.getCampoStringCamposVarios(
                    "listvend",
                    "nombre",
                    listOf("codigo", "empresa"),
                    listOf(cabecera.ktiCodven, codEmpresa!!)
                ),
                25f,
                counter + 45f,
                paint
            )

            // Marca el numero de paginas
            // Se le agrega el -1 debido a que al salir del for de las lineas ahi se le suma +1
            // canvas.drawText("${pagActual - 1}/$nroPag", 550f, counter + 60f, paint)

            // Final de la pagina
            document.finishPage(page)
        }

        // document.finishPage(page)
        val numeroRecibo = "PEDIDO_NRO_$codigoPedido.pdf"
        // este sera el nombre del documento al momento de crearlo y guardarlo en el almacenamiento

        val path = getExternalFilesDir(null)!!.absoluteFile.toString() + "/" + numeroRecibo
        val file = File(path)

        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString()

        try {
            document.writeTo(FileOutputStream(file))

            Toast.makeText(this.applicationContext, "PDF Generado", Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            Toast.makeText(this.applicationContext, "PDF No Generado", Toast.LENGTH_LONG).show()
            println("error en ${e.printStackTrace()}")
        }

        document.close()
        abrirRecibo(file)
    }

    // Funcion que organiza el numero dado para que decimales y enteros esten alineados
    private fun insertarNumPDF(canvas: Canvas, num: Double, x: Float, y: Float, paint: Paint) {
        // Formater de numeros decimales
        val formato = DecimalFormat("####.00")

        // Redondeo del numero dado hacia abajo
        val numeroInt = floor(num).toInt()

        // Formateo del numero dado para que los ceros a la derecha del punto decimal se tomen
        // Y captura de los dos decimales
        val decimales = getLastN(formato.format(num), 2)

        // Imprecion del numero dado sin decimales
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(numeroInt.toString(), x + 25, y, paint)
        // Impresion del punto decimal
        canvas.drawText(".", x + 28, y, paint)
        // Imprecion de los decimales del numero dado
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText(decimales!!, x + 30, y, paint)
    }

    // Funcion para optener los ultimos caracteres que se le indique
    private fun getLastN(s: String?, num: Int): String? {
        return s?.substring(max(0, s.length - num))
    }

    private fun abrirRecibo(file: File) {
        // val ruta = "$rutaRaiz/$nombreArchivo"
        // println("La ruta -> $ruta")
        // val file = File(ruta.substring(1))

        if (!file.exists()) {
            Toast.makeText(
                this.applicationContext,
                "Este archivo no existe o fue cambiado de lugar.",
                Toast.LENGTH_LONG
            ).show()
        } else {
            try {
                val builder = StrictMode.VmPolicy.Builder()
                StrictMode.setVmPolicy(builder.build())

                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "application/pdf"

                File(file.path)

                val outputPdfUri = FileProvider.getUriForFile(
                    this,
                    this.packageName + ".provider",
                    file
                )

                shareIntent.putExtra(Intent.EXTRA_STREAM, outputPdfUri)

                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                // Write Permission might not be necessary
                // Write Permission might not be necessary
                shareIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

                startActivity(Intent.createChooser(shareIntent, "Share PDF using.."))

                /*println("antes del intent")
                val share = Intent()
                share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                share.action = Intent.ACTION_SEND
                share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file))
                share.type = "application/pdf"
                println("antes del intent2 ")
                startActivity(share)
                println("despues del intent")*/

                /* println("1")
                 val intentShare = Intent(Intent.ACTION_SEND)
                 println("2")*/
                // intentShare.type = "*/*"
                /*println("3")
                intentShare.addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                println("4")
                intentShare.putExtra(Intent.EXTRA_STREAM, file)
                println("5")
                startActivity(Intent.createChooser(intentShare, "Compartir Archivo..."))
                println("6")*/
            } catch (e: Exception) {
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
                println("error")
                println("Error -> $e")
            }
        }
    }

    companion object {
        var cod_usuario: String? = null
        var codEmpresa: String? = null
        var codigoPedido: String? = null
        var pedido_estatus: String? = null
        var statusPedido: String? = null
        var n_cliente: String? = null
        var codigoCliente: String? = null
        var nropedido: String? = null
        var fechadoc: String? = null
        var docsol: String? = null
        var condicion: String? = null
        var sesionNube: String? = null
        var sesionActiva: String? = null
        var fechapedido: String? = null
        var pedidonumero: String? = null
        var totneto: String? = null
        var nombreEmpresa = ""
        var codigoSucursal = ""
        var enlaceEmpresa = ""
        var montoNeto: Double? = null
    }

    private fun colorButton(btn: Button) {
        btn.apply {
            setTextColor(colorTextAgencia(Constantes.AGENCIA))
        }
    }

    private fun setColors() {
        binding.apply {
            btNuevop.backgroundTintList =
                ColorStateList.valueOf(binding.btNuevop.colorAgencia(Constantes.AGENCIA))
            btNuevop.setRippleColor(ColorStateList.valueOf(btNuevop.colorTextAgencia(Constantes.AGENCIA)))
            btNuevop.imageTintList = btNuevop.colorIconReclamo(Constantes.AGENCIA)
        }
    }

    override fun getTheme(): Resources.Theme {
        val theme = super.getTheme()
        theme.applyStyle(setThemeAgencia(Constantes.AGENCIA), true)
        // you could also use a switch if you have many themes that could apply
        return theme
    }
}
