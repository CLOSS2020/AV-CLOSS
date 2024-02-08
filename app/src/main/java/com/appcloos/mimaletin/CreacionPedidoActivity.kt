package com.appcloos.mimaletin

import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.view.ContextThemeWrapper
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.appcloos.mimaletin.ObjetoUtils.Companion.valorReal
import com.appcloos.mimaletin.databinding.ActivityCreacionPedidoBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.json.JSONArray
import org.json.JSONObject
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class CreacionPedidoActivity : AppCompatActivity() {
    var adapter: ArrayAdapter<*>? = null
    var adapterSpinner: ArrayAdapter<CharSequence>? = null
    var listainfo: ArrayList<String>? = null
    var listapedido: ArrayList<String>? = null
    private var listainfoClientes: ArrayList<String>? = null
    private var listacarrito: ArrayList<Carrito>? = null
    private var listacliente: ArrayList<Cliente>? = null
    var spinner: Spinner? = null
    lateinit var conn: AdminSQLiteOpenHelper
    var tvSubtotal: TextView? = null
    var subtotal: Double? = null
    var netoTotal = 0.00
    var ibtModificar: ImageButton? = null
    private var montoMinimoTotal = 0.00
    var codigoCliente: String? = null
    private var codUsuario1: String? = null
    private var codEmpresa: String? = null

    // CheckBox cb_negoespecial;
    var formato = DecimalFormat("#,###.00")
    private var APP_ITEMS_FACTURAS = 0
    private var APP_ITEMS_NOTAS_ENTREGA = 0
    private var APP_DIAS_PEDIDO_COMPLEMENTO: Long = 0
    private var APP_DOLAR_FLETE: Boolean = false
    private val mOnNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.ic_agregarart -> {
                    if (codigoCliente == null || !binding.RbFactura.isChecked && !binding.RbNotaEntrega.isChecked || !binding.RbPrepago.isChecked && !binding.RbCredito.isChecked) {
                        Toast.makeText(
                            this@CreacionPedidoActivity,
                            "Debes Seleccionar un cliente, factura o nota de entrega y credito o prepago",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        if (listacarrito!!.size >= APP_ITEMS_FACTURAS && binding.RbFactura.isChecked) {
                            if (binding.RbFactura.isChecked) {
                                Toast.makeText(
                                    this@CreacionPedidoActivity,
                                    "Para facturas debe tener un maximo de $APP_ITEMS_FACTURAS lineas de articulos",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                Toast.makeText(
                                    this@CreacionPedidoActivity,
                                    "Para notas de entrega debe tener un maximo de $APP_ITEMS_NOTAS_ENTREGA lineas de articulos",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } else {
                            // Asignacion de precio para saber si es negociacion especial
                            // asignarTipodePrecio();
                            iraCatalogo()
                        }
                    }
                    return@OnNavigationItemSelectedListener true
                }

                R.id.ic_procesarpedido -> {
                    val centinel = validarSiHayPedidosActivos()
                    if (!negociacionIsActiva() && montoNetoConDescuento!! < montoMinimoTotal && !centinel) {
                        Toast.makeText(
                            this@CreacionPedidoActivity,
                            "El monto minimo es de $$montoMinimoTotal",
                            Toast.LENGTH_LONG
                        ).show()
                    } else if (montoNetoConDescuento!! >= montoMinimoTotal || centinel) {
                        if (binding.RbFactura.isChecked && listacarrito!!.size > APP_ITEMS_FACTURAS || binding.RbNotaEntrega.isChecked && listacarrito!!.size > APP_ITEMS_NOTAS_ENTREGA) {
                            if (binding.RbFactura.isChecked) {
                                Toast.makeText(
                                    this@CreacionPedidoActivity,
                                    "Para facturas debe tener un maximo de $APP_ITEMS_FACTURAS lineas de articulos",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                Toast.makeText(
                                    this@CreacionPedidoActivity,
                                    "Para notas de entrega debe tener un maximo de $APP_ITEMS_NOTAS_ENTREGA lineas de articulos",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } else {
                            procesarPedido()
                        }
                    }
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        }

    private lateinit var binding: ActivityCreacionPedidoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED // mantener la orientacion vertical
        super.onCreate(savedInstanceState)
        binding = ActivityCreacionPedidoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setColors()
        // getSupportActionBar().hide(); //metodo para esconder la actionbar
        var preferences = getSharedPreferences("Preferences", MODE_PRIVATE)
        codUsuario1 = preferences.getString("cod_usuario", null)
        codEmpresa = preferences.getString("codigoEmpresa", null)
        enpreventa = "0"
        binding.menuCreacion.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        codigoCliente = intent.extras!!.getString("cod_cliente")
        n_cliente = intent.extras!!.getString("nombre_cliente")
        conn = AdminSQLiteOpenHelper(this@CreacionPedidoActivity, "ke_android", null)
        val keAndroid = conn.writableDatabase
        APP_ITEMS_FACTURAS = conn.getConfigNum("APP_ITEMS_FACTURAS", codEmpresa!!).roundToInt()
        APP_ITEMS_NOTAS_ENTREGA =
            conn.getConfigNum("APP_ITEMS_NOTAS_ENTREGA", codEmpresa!!).roundToInt()
        APP_DIAS_PEDIDO_COMPLEMENTO =
            conn.getConfigNum("APP_DIAS_PEDIDO_COMPLEMENTO", codEmpresa!!).toLong()
        APP_DOLAR_FLETE = conn.getConfigBool("APP_DOLAR_FLETE", codEmpresa!!)
        val NOEMIFAC = conn.getCampoIntCamposVarios(
            "cliempre",
            "noemifac",
            listOf("codigo", "empresa"),
            listOf(codigoCliente!!, codEmpresa!!)
        )
        val NOEMINOTA = conn.getCampoIntCamposVarios(
            "cliempre",
            "noeminota",
            listOf("codigo", "empresa"),
            listOf(codigoCliente!!, codEmpresa!!)
        )
        cargarEnlace()
        val cursor = keAndroid.rawQuery(
            "SELECT MAX(kco_numero) FROM ke_correla WHERE kco_vendedor ='$codUsuario1' AND empresa = '$codEmpresa'",
            null
        )
        if (cursor.moveToFirst()) {
            nroCorrelativo = cursor.getInt(0)
            nroCorrelativo += 1
            // CorrelativoTexto = String.valueOf(nroCorrelativo);
            CorrelativoTexto = "0000$nroCorrelativo"
            generarNumeroPedido()
            supportActionBar!!.title = "Pedido: $nroPedido"
        }
        cursor.close()
        // spinner             = findViewById(R.id.sp_cliente);
        // ibt_modificar       = findViewById(R.id.ibt_modificar);
        // cb_negoespecial = findViewById(R.id.cb_negespecial);
        montoMinimoTotal = obtenerMontoMinimoTotal()
        llamarFuncionesAsignacion()
        verificarDocSelected()
        binding.tvNombreCliente.text = n_cliente
        binding.radioGroup.setOnCheckedChangeListener { _: RadioGroup?, _: Int -> verificarDocSelected() }

        // Prepago.setOnCheckedChangeListener((buttonView, isChecked) -> recalculoPrecio(isChecked));
        binding.RbNotaEntrega.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            if (isChecked) {
                /*Prepago.setChecked(false);
                Prepago.setVisibility(View.INVISIBLE);
                Prepago.setEnabled(false);*/
                binding.RbCredito.isChecked = true
            } else {
                /*Prepago.setVisibility(View.VISIBLE);
                Prepago.setEnabled(true);*/
            }
        }

        /* Credito.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                recalculoPrecio(false);
            }
        });*/

        /*ibt_modificar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                spinner.setEnabled(true);
            }
        });*/

        /* carritoAdapter = new CarritoAdapter(CreacionPedidoActivity.this, listacarrito);
        listaLineas.setAdapter(carritoAdapter);*/
        cargarLineas()
        cargarClientes()
        sumaNeto()
        recuprarSeleccion()

        // obtenemos el codigo de usuario de las preferencias guardadas cuando iniciamos sesión
        preferences = getSharedPreferences("Preferences", MODE_PRIVATE)
        codUsuario1 = preferences.getString("cod_usuario", null)
        /** */

        /*spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {

                if(position!= 0) {
                    codigoCliente = listacliente.get(position-1).getCodigo();
                    n_cliente = listacliente.get(position-1).getNombre();
                    //tv_codigocliente.setText(codigoCliente);
                    System.out.println(codigoCliente);

                    int indexposicion = position;

                    SharedPreferences sharpref = getSharedPreferences("sharpref", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharpref.edit();
                    editor.putInt("Dato", indexposicion);
                    // System.out.println("Indice:"+index);
                    editor.apply();
                    spinner.setEnabled(false);
                    cargarPendientes("https://"+enlaceEmpresa+"/webservice/validarpend.php?cliente=" + codigoCliente);

                    asignarTipodePrecio();
                    actualizarPrecios();
                    actualizaLista();
                    obtenerCondicionesEspeciales();

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                spinner.setEnabled(true);
            }
        });*/
        /** */
        // en este metodo onlistener, cuando se mantiene presionado un item, da la opcion de sacarlo del carrito
        // y luego refrescar la lista de lineas del pedido
        binding.ListPedido.setOnItemLongClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
            val codigo = listacarrito!![position].getCodigo()
            val ventana = AlertDialog.Builder(
                ContextThemeWrapper(
                    this@CreacionPedidoActivity,
                    setAlertDialogTheme(Constantes.AGENCIA)
                )
            )
            ventana.setTitle("Mensaje del Sistema")
            ventana.setMessage("¿Desea eliminar este artículo?")
            ventana.setPositiveButton("Aceptar") { _: DialogInterface?, _: Int ->
                conn = AdminSQLiteOpenHelper(
                    applicationContext, "ke_android", null
                )
                val keAndroid1 = conn.writableDatabase
                keAndroid1.execSQL("DELETE FROM ke_carrito WHERE kmv_codart ='$codigo'")
                actualizaLista()
                Toast.makeText(this@CreacionPedidoActivity, "Artículo borrado", Toast.LENGTH_SHORT)
                    .show()
            }
            ventana.setNegativeButton(
                "Cancelar"
            ) { dialogInterface: DialogInterface, _: Int -> dialogInterface.dismiss() }
            val dialogo = ventana.create()
            dialogo.show()

            val pbutton: Button = dialogo.getButton(DialogInterface.BUTTON_POSITIVE)
            pbutton.apply {
                setTextColor(colorTextAgencia(Constantes.AGENCIA))
            }

            val nbutton: Button = dialogo.getButton(DialogInterface.BUTTON_NEGATIVE)
            nbutton.apply {
                setTextColor(colorTextAgencia(Constantes.AGENCIA))
            }

            false
        }
        /** */
        binding.ListPedido.setOnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
            // -- obtiene el codigo del articulo y el descuento en la línea
            val codigo = listacarrito!![position].getCodigo()
            val dctolin = listacarrito!![position].getDctolin()
            val cajatexto = EditText(
                ContextThemeWrapper(
                    this@CreacionPedidoActivity,
                    setEditTextTheme(Constantes.AGENCIA)
                )
            )
            cajatexto.inputType = InputType.TYPE_CLASS_NUMBER
            conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null)
            val keAndroid12 = conn.writableDatabase
            val cursorMul = keAndroid12.rawQuery(
                "SELECT vta_min, vta_minenx FROM articulo WHERE codigo ='$codigo' AND empresa = '$codEmpresa'",
                null
            )
            cursorMul.moveToFirst()
            val vtaMin = cursorMul.getDouble(0)
            val vtaMinenx = cursorMul.getInt(1)
            cursorMul.close()
            println("ventaMin: $vtaMin")
            val ventana = AlertDialog.Builder(
                ContextThemeWrapper(
                    this@CreacionPedidoActivity,
                    setAlertDialogTheme(Constantes.AGENCIA)
                )
            )
            ventana.setTitle("Modificar Linea")
            ventana.setMessage("Porfavor, elige la cantidad")
            val layoutH = LinearLayout(this@CreacionPedidoActivity)
            layoutH.orientation = LinearLayout.VERTICAL
            val darDescuento = CheckBox(
                ContextThemeWrapper(
                    this@CreacionPedidoActivity,
                    setCheckBoxTheme(Constantes.AGENCIA)
                )
            )
            val descuentoBool = conn.getCampoDoubleCamposVarios(
                "articulo",
                "dctotope",
                listOf("codigo", "empresa"),
                listOf(codigo, codEmpresa!!)
            )
            // 2023-09-14 Verificando que el articulo permita descuento
            if (descuentoBool > 0.0) {
                // final CheckBox darDescuento = new CheckBox(CreacionPedidoActivity.this);
                darDescuento.text = "Dar Descuento del Articulo"
                // 2023-09-14 Checkeo el boton si se chekeo en catalogo
                if (dctolin > 0.0) {
                    darDescuento.isChecked = true
                }
                layoutH.addView(darDescuento)
            }
            if (vtaMinenx == 1) {
                val layoutVtaMinenx = LinearLayout(this@CreacionPedidoActivity)
                layoutVtaMinenx.orientation = LinearLayout.HORIZONTAL
                val mensajeCantidadMultiplo = TextView(this@CreacionPedidoActivity)
                mensajeCantidadMultiplo.textSize = 20f
                mensajeCantidadMultiplo.setTypeface(null, Typeface.BOLD)
                // mensajeCantidadMultiplo.setTextColor(Color.parseColor("#313131"));
                mensajeCantidadMultiplo.text = vtaMin.roundToInt().toString() + " x "
                // mensajeCantidadMultiplo.setLayoutParams(params);
                cajatexto.width = 1000
                cajatexto.hint = "Cantidad de paquetes a pedir"
                layoutVtaMinenx.addView(mensajeCantidadMultiplo)
                // cajatexto.setLayoutParams(params2);
                layoutVtaMinenx.addView(cajatexto)
                layoutH.addView(layoutVtaMinenx)
            } else {
                cajatexto.hint = "Cantidad a pedir"
                layoutH.addView(cajatexto)
            }
            ventana.setView(layoutH)
            ventana.setPositiveButton("Aceptar") { _: DialogInterface?, _: Int ->
                if (cajatexto.text.toString().isEmpty()) {
                    Toast.makeText(
                        this@CreacionPedidoActivity,
                        "la cantidad no puede estar en blanco",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    /* 16-11-2021 Corregido error de texto vacio que generaba crash en la app */
                    val cantidadNueva = cajatexto.text.toString()
                    val aprobarDescuento = darDescuento.isChecked
                    val cantidad = cantidadNueva.toInt()
                    if (cantidad != 0) {
                        val cursor1 = keAndroid12.rawQuery(
                            "SELECT $tipoDePrecioaMostrar, (existencia - comprometido), vta_min, vta_max, vta_minenx FROM articulo " + "WHERE codigo ='$codigo' AND empresa = '$codEmpresa'",
                            null
                        )
                        cursor1.moveToNext()
                        // System.out.println("SELECT " + tipoDePrecioaMostrar + ", (existencia - comprometido), vta_min, vta_max, vta_minenx FROM articulo WHERE codigo ='" + codigo + "'");
                        val precio = valorReal(cursor1.getDouble(0))
                        val existencia = cursor1.getDouble(1)
                        val ventaMax = cursor1.getDouble(3)
                        val ventaMin = cursor1.getDouble(2)
                        val vtaMinenx1 = cursor1.getInt(4)
                        cursor1.close()
                        val existenciaValidar = existencia.roundToInt()
                        println(cantidad)
                        println(existenciaValidar)
                        if (cantidad > existenciaValidar) {
                            Toast.makeText(
                                this@CreacionPedidoActivity,
                                "La cantidad no puede ser superior a la existencia",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            if (ventaMin > 0) {
                                println("Multiplo: $vtaMinenx1")
                                if (vtaMinenx1 == 1) {
                                    if (cantidad * ventaMin > existencia) {
                                        Toast.makeText(
                                            this@CreacionPedidoActivity,
                                            "Debe de elegir una cantidad dentro de la existencia",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } else if (cantidad * ventaMin <= existencia) {
                                        val cantidadNew = (cantidad * ventaMin).toInt()
                                        println("Nueva cantidad $cantidadNew")
                                        var precioTotal = precio * cantidadNew
                                        println("Precio total: $precioTotal")
                                        precioTotal = precioTotal.valorReal()
                                        keAndroid12.beginTransaction()
                                        try {
                                            var precioNuevo = precioTotal
                                            precioNuevo = precioNuevo.valorReal()
                                            var mtoDctoNuevo: Double
                                            val descuento: Double
                                            if (aprobarDescuento) {
                                                mtoDctoNuevo =
                                                    precioNuevo - precioNuevo * (descuentoBool / 100)
                                                mtoDctoNuevo = mtoDctoNuevo.valorReal()
                                                descuento = descuentoBool
                                            } else {
                                                mtoDctoNuevo = precioTotal
                                                descuento = 0.0
                                            }
                                            keAndroid12.execSQL(
                                                "UPDATE ke_carrito SET kmv_cant=$cantidadNew, kmv_stot =$precioTotal, kmv_stotdcto =$mtoDctoNuevo, kmv_dctolin =$descuento " + "WHERE kmv_codart ='$codigo' AND empresa = '$codEmpresa'"
                                            )
                                            keAndroid12.setTransactionSuccessful()
                                            keAndroid12.endTransaction()
                                            Toast.makeText(
                                                this@CreacionPedidoActivity,
                                                "Artículo modificado",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } catch (ex: Exception) {
                                            println("--Error--")
                                            ex.printStackTrace()
                                            keAndroid12.endTransaction()
                                        }
                                    }
                                } else {
                                    if (cantidad < ventaMin) {
                                        Toast.makeText(
                                            this@CreacionPedidoActivity,
                                            "Debe cumplir con la cantidad mínima para la venta",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } else if (cantidad >= ventaMin) {
                                        var precioTotal = precio * cantidad.toDouble()
                                        precioTotal = precioTotal.valorReal()
                                        keAndroid12.beginTransaction()
                                        try {
                                            var precioNuevo = precio * cantidad.toDouble()
                                            precioNuevo = precioNuevo.valorReal()
                                            var mtoDctoNuevo: Double
                                            val descuento: Double
                                            if (aprobarDescuento) {
                                                mtoDctoNuevo =
                                                    precioNuevo - precioNuevo * (descuentoBool / 100)
                                                mtoDctoNuevo = mtoDctoNuevo.valorReal()
                                                descuento = descuentoBool
                                            } else {
                                                mtoDctoNuevo = precioTotal
                                                descuento = 0.0
                                            }
                                            keAndroid12.execSQL(
                                                "UPDATE ke_carrito SET kmv_cant=$cantidad, kmv_stot =$precioTotal, kmv_stotdcto =$mtoDctoNuevo, kmv_dctolin =$descuento " + "WHERE kmv_codart ='$codigo' AND empresa = '$codEmpresa'"
                                            )
                                            keAndroid12.setTransactionSuccessful()
                                            keAndroid12.endTransaction()
                                            Toast.makeText(
                                                this@CreacionPedidoActivity,
                                                "Artículo modificado",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            // finish();
                                        } catch (ex: Exception) {
                                            println("--Error--")
                                            ex.printStackTrace()
                                            keAndroid12.endTransaction()
                                        }
                                    }
                                }
                            } else {
                                var precioTotal = precio * cantidad.toDouble()
                                precioTotal = precioTotal.valorReal()
                                keAndroid12.beginTransaction()
                                try {
                                    var precioNuevo = precio * cantidad.toDouble()
                                    precioNuevo = precioNuevo.valorReal()
                                    var mtoDctoNuevo: Double
                                    val descuento: Double
                                    if (aprobarDescuento) {
                                        mtoDctoNuevo =
                                            precioNuevo - precioNuevo * (descuentoBool / 100)
                                        mtoDctoNuevo = mtoDctoNuevo.valorReal()
                                        descuento = descuentoBool
                                    } else {
                                        mtoDctoNuevo = precioTotal
                                        descuento = 0.0
                                    }
                                    keAndroid12.execSQL(
                                        "UPDATE ke_carrito SET kmv_cant=$cantidad, kmv_stot =$precioTotal, kmv_stotdcto =$mtoDctoNuevo, kmv_dctolin =$descuento " + "WHERE kmv_codart ='$codigo' AND empresa = '$codEmpresa'"
                                    )
                                    keAndroid12.setTransactionSuccessful()
                                    keAndroid12.endTransaction()
                                    // finish();
                                    Toast.makeText(
                                        this@CreacionPedidoActivity,
                                        "Artículo modificado",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } catch (ex: Exception) {
                                    println("--Error--")
                                    ex.printStackTrace()
                                    keAndroid12.endTransaction()
                                }
                            } /*
                            Double precioNuevo = precio * Double.valueOf(cantidad);
                            precioNuevo = Math.round(precioNuevo * 100.0) / 100.00;

                            Double mtoDctoNuevo = precioNuevo - (precioNuevo * (dctolin/100));
                            mtoDctoNuevo = Math.round(mtoDctoNuevo * 100.0) / 100.00;

                            ke_android.execSQL("UPDATE ke_carrito SET kmv_cant=" + cantidad + ", kmv_stot =" + precioNuevo + ", kmv_stotdcto =" + mtoDctoNuevo + " WHERE kmv_codart ='" + codigo + "'");
                            actualizaLista();

                            Toast.makeText(CreacionPedidoActivity.this, "Artículo modificado", Toast.LENGTH_SHORT).show();*/
                            actualizaLista()
                            Toast.makeText(
                                this@CreacionPedidoActivity,
                                "Articulo añadido",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this@CreacionPedidoActivity,
                            "La existencia no puede ser 0",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            ventana.setNegativeButton(
                "Cancelar"
            ) { dialogInterface: DialogInterface, _: Int -> dialogInterface.dismiss() }
            val dialogo = ventana.create()
            dialogo.show()

            val pbutton: Button = dialogo.getButton(DialogInterface.BUTTON_POSITIVE)
            pbutton.apply {
                setTextColor(colorTextAgencia(Constantes.AGENCIA))
            }

            val nbutton: Button = dialogo.getButton(DialogInterface.BUTTON_NEGATIVE)
            nbutton.apply {
                setTextColor(colorTextAgencia(Constantes.AGENCIA))
            }
        }
        binding.swNegoespecial.setOnCheckedChangeListener { compoundButton: CompoundButton, _: Boolean ->
            if (compoundButton.isChecked) {
                binding.tvMtomin.isEnabled = true
                binding.tvMtomin.visibility = View.VISIBLE
                binding.tvMtomin.text = "Monto Mín: $$montoMinimo"
            } else {
                binding.tvMtomin.isEnabled = false
                binding.tvMtomin.visibility = View.INVISIBLE
            }
        }
        binding.swPreventa.setOnCheckedChangeListener { compoundButton: CompoundButton, _: Boolean ->
            if (compoundButton.isChecked) {
                enpreventa = "1"
                vaciarCarrito()
                cargarLineas()
                sumaNeto()
            } else if (!compoundButton.isChecked) {
                enpreventa = "0"
                vaciarCarrito()
                cargarLineas()
                sumaNeto()
            }
        }
        funcionCaducidad()
        if (NOEMIFAC == 1) {
            binding.RbFactura.visibility = View.INVISIBLE
        } else {
            binding.RbFactura.visibility = View.VISIBLE
        }
        if (NOEMINOTA == 1) {
            binding.RbNotaEntrega.visibility = View.INVISIBLE
        } else {
            binding.RbNotaEntrega.visibility = View.VISIBLE
        }
        if (NOEMIFAC == 1 && NOEMINOTA == 1) {
            Toast.makeText(this, "Cliente suspendido", Toast.LENGTH_SHORT).show()
            finish()
        }
        binding.RbFactura.setOnClickListener {
            analizarArticulos("vta_solone", listacarrito)
            montoMinimoTotal = obtenerMontoMinimoTotal()
        }
        binding.RbNotaEntrega.setOnClickListener {
            analizarArticulos("vta_solofac", listacarrito)
            montoMinimoTotal = obtenerMontoMinimoTotal()
        }

        /*Factura.setOnCheckedChangeListener((buttonView, isChecked) -> {
            analizarArticulos(isChecked, listacarrito);
        });

        NotaEntrega.setOnCheckedChangeListener((buttonView, isChecked) -> {
            analizarArticulos(!isChecked, listacarrito);
        });*/

        /*binding.cbFleteDol.isChecked = conn.getCampoBooleanCamposVarios(
            "cliempre",
            "dolarflete",
            listOf("codigo", "empresa"),
            listOf(codigoCliente!!, codEmpresa!!)
        )

        binding.cbFleteDol.isVisible = APP_DOLAR_FLETE*/

        if (APP_DOLAR_FLETE) {
            binding.cbFleteDol.isChecked = conn.getCampoBooleanCamposVarios(
                "cliempre",
                "dolarflete",
                listOf("codigo", "empresa"),
                listOf(codigoCliente!!, codEmpresa!!)
            )
            binding.cbFleteDol.isVisible = true
        } else {
            binding.cbFleteDol.isChecked = false
            binding.cbFleteDol.isVisible = false
        }
    }

    private fun verificarDocSelected() {
        if (binding.RbFactura.isChecked) {
            binding.tvAvisobloqueo.visibility = View.VISIBLE
            binding.tvAvisobloqueo.text =
                "Las facturas solo tendrán un máximo de $APP_ITEMS_FACTURAS líneas de artículos"
        } else if (binding.RbNotaEntrega.isChecked) {
            binding.tvAvisobloqueo.visibility = View.VISIBLE
            binding.tvAvisobloqueo.text =
                "Las N/E solo tendrán un máximo de $APP_ITEMS_NOTAS_ENTREGA líneas de artículos"
        }
    }

    private fun analizarArticulos(campo: String, listacarrito: ArrayList<Carrito>?) {
        var num = 0
        // int numNE = 0;
        for (i in listacarrito!!.indices) {
            num = conn.getCampoIntCamposVarios(
                "articulo",
                campo,
                listOf("codigo", "empresa"),
                listOf(listacarrito[i].codigo, codEmpresa!!)
            )
            // numNE += conn.getCampoInt("articulo", "vta_solone", "codigo", listacarrito.get(i).codigo);
            if (num > 0) {
                break
            }
        }
        if (num > 0 && binding.RbNotaEntrega.isChecked) {
            Toast.makeText(
                this,
                "Posee artículos que solo están disponibles para Facturas",
                Toast.LENGTH_SHORT
            ).show()
            binding.RbFactura.isChecked = true
        } else if (num > 0 && binding.RbFactura.isChecked) {
            Toast.makeText(
                this,
                "Posee artículos que solo están disponibles para Notas de Entrega",
                Toast.LENGTH_SHORT
            ).show()
            binding.RbNotaEntrega.isChecked = true
        }
    }

    // 554
    private fun llamarFuncionesAsignacion() {
        asignarTipodePrecio()
        actualizarPrecios()
        actualizaLista()
        obtenerCondicionesEspeciales()
    }

    private fun recalculoPrecio(b: Boolean) {
        var keAndroid = conn.writableDatabase
        var cursor = keAndroid.rawQuery(
            "SELECT precio FROM cliempre WHERE codigo='$codigoCliente' AND empresa = '$codEmpresa'",
            null
        )
        if (b) {
            tipoDePrecioaMostrar = "precio1"
            preciocliente = 1.0

            // 2023-06-05 se comento debido a que la opcion de prepago se borrara y ahora se llamara pago BCV y se asignara precio1 (Posiblemente precio3 se elimine)
            /*tipoDePrecioaMostrar = "precio3";
            preciocliente = 3.0;*/
        } else {
            cursor.moveToFirst()
            preciocliente = cursor.getDouble(0)
            enteroPrecio = (preciocliente!!).roundToInt()
            tipoDePrecioaMostrar = "precio$enteroPrecio"
        }
        cursor.close()
        // tipoDePrecioaMostrar = (b)?"precio3":"precio"+enteroPrecio;
        keAndroid = conn.writableDatabase
        val cursorMain =
            keAndroid.rawQuery("SELECT * FROM ke_carrito WHERE empresa = '$codEmpresa'", null)
        while (cursorMain.moveToNext()) {
            val codigo = cursorMain.getString(0)
            val cantidad = cursorMain.getDouble(2)
            cursor = keAndroid.rawQuery(
                "SELECT $tipoDePrecioaMostrar FROM articulo WHERE codigo = '$codigo' AND empresa = '$codEmpresa'",
                null
            )
            cursor.moveToFirst()
            val precio = cursor.getDouble(0).valorReal()
            val precioTotal = precio * cantidad
            val precioTotalRedondo = precioTotal.valorReal()
            cursor.close()
            keAndroid.execSQL(
                "UPDATE ke_carrito SET kmv_artprec=$precio, kmv_stot= $precioTotalRedondo, kmv_stotdcto=$precioTotalRedondo " + "WHERE kmv_codart ='$codigo' AND empresa = '$codEmpresa'"
            )
            cargarLineas()
            sumaNeto()
        }
        cursorMain.close()
    }

    // / validacion de pedidos complementarios
    private fun validarSiHayPedidosActivos(): Boolean {
        val hoy = LocalDateTime.now()
        val ayer = hoy.minusHours(APP_DIAS_PEDIDO_COMPLEMENTO)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val fechaAnterior = ayer.format(formatter)
        val valorCentinela: Boolean
        val keAndroid = conn.writableDatabase
        val cursorPeds = keAndroid.rawQuery(
            "SELECT kti_ndoc FROM ke_opti " + "WHERE kti_codcli = '$codigoCliente' AND kti_status <> '3' AND kti_fchdoc >='$fechaAnterior' AND empresa = '$codEmpresa'",
            null
        )
        if (cursorPeds.moveToFirst()) {
            valorCentinela = true
            println("Existen pedidos de este cliente")
        } else {
            valorCentinela = false
            println("no se ha regresado ningun registro")
        }
        cursorPeds.close()
        return valorCentinela
    }

    private fun obtenerMontoMinimoTotal(): Double {
        val monto: Double = if (binding.RbFactura.isChecked) {
            conn.getConfigNum("APP_MONTO_MINIMO_FAC", codEmpresa!!)
        } else if (binding.RbNotaEntrega.isChecked) {
            conn.getConfigNum("APP_MONTO_MINIMO_NE", codEmpresa!!)
        } else {
            75.00
        }
        return monto
    }

    private fun vaciarCarrito() {
        val keAndroid = conn.writableDatabase
        try {
            keAndroid.beginTransaction()
            keAndroid.execSQL("DELETE FROM ke_carrito WHERE empresa = '$codEmpresa'")
            keAndroid.setTransactionSuccessful()
        } catch (e: Exception) {
            println("--Error--")
            e.printStackTrace()
        } finally {
            keAndroid.endTransaction()
        }
    }

    private fun cargarEnlace() {
        val keAndroid = conn.writableDatabase
        val columnas = arrayOf("kee_nombre," + "kee_url")
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
        }
        cursor.close()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemid = item.itemId
        if (itemid == android.R.id.home) {
            validarSalida()
        }
        // return super.onOptionsItemSelected(item);
        return true
    }

    override fun onBackPressed() {
        validarSalida()
    }

    private fun validarSalida() {
        val dialog = AlertDialog.Builder(
            ContextThemeWrapper(
                this@CreacionPedidoActivity,
                setAlertDialogTheme(Constantes.AGENCIA)
            )
        ).setTitle("Salir").setMessage("¿Está seguro de desear salir?").setCancelable(true)
            .setPositiveButton("Si", null).setNegativeButton("No", null).show()
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
            conn.deleteAll("ke_carrito")
            dialog.dismiss()
            finish()
        }
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener { dialog.dismiss() }

        val pbutton: Button = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
        pbutton.apply {
            setTextColor(colorTextAgencia(Constantes.AGENCIA))
        }

        val nbutton: Button = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
        nbutton.apply {
            setTextColor(colorTextAgencia(Constantes.AGENCIA))
        }
    }

    private fun cargarPendientes(url: String) {
        val jsonArrayRequest: JsonArrayRequest =
            object : JsonArrayRequest(
                url,
                Response.Listener { response: JSONArray? ->
                    if (response != null) {
                        println("llegando info")
                        val jsonObject: JSONObject
                        try {
                            jsonObject = response.getJSONObject(0)
                            conteoDocs = jsonObject.getInt("conteo")
                            fechaVence = jsonObject.getString("ultimafecha")
                            kti_negesp = jsonObject.getString("kti_negesp")
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                },
                Response.ErrorListener { error: VolleyError ->
                    println("--Error--")
                    error.printStackTrace()
                }
            ) {
                override fun getParams(): Map<String, String> { // finalmente, estos son los parametros que le enviaremos al webservice, partiendo de las variables
                    // donde estan guardados el usuario y password.
                    // parametros.put("version_usuario", versionApp);
                    return HashMap()
                }
            }
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(
            jsonArrayRequest
        ) // esto es el request que se envia al url a traves de la conexion volley, (el stringrequest esta armado arriba)
    }

    private fun funcionCaducidad() {
        // obtengo la fecha del sistema al ejecutar la función:
        val hoy = LocalDateTime.now() // el dia en que se hizo el grabado
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val fechaHoy = hoy.format(formatter)
        val keAndroid = conn.writableDatabase
        try {
            keAndroid.beginTransaction()
            keAndroid.execSQL("DELETE FROM ke_limitart WHERE kli_fechavence <='$fechaHoy' AND empresa = '$codEmpresa'")
            keAndroid.setTransactionSuccessful()
        } catch (e: Exception) {
            println("--Error--")
            e.printStackTrace()
        } finally {
            keAndroid.endTransaction()
        }
    }

    private fun sumaNeto() {
        val keAndroid = conn.writableDatabase
        val cursor = keAndroid.rawQuery(
            "SELECT SUM(kmv_stot), SUM(kmv_dctolin), SUM(kmv_stotdcto) FROM ke_carrito WHERE empresa = '$codEmpresa'",
            null
        )
        if (cursor.moveToNext()) {
            precioTotalporArticulo = cursor.getDouble(0)
            precioTotalporArticulo = precioTotalporArticulo!!.valorReal()
            descuentoTotal = cursor.getDouble(1)

            // 2023-09-16 Aqui guardo el monto con descuento,
            // como casualmente en el mismo campo guardo el monto sin descuento cuando no se selecciona
            // aqui tambien se guarda el total con y sin descuento dependiendo la seleccion
            montoNetoConDescuento = cursor.getDouble(2)
            binding.tvNeto.text = "$" + precioTotalporArticulo!!.toTwoDecimals()
            if (descuentoTotal!! > 0.0) {
                binding.tvSubcondcto.visibility = View.VISIBLE
                binding.tvNetocondescuento.visibility = View.VISIBLE
                montoNetoConDescuento = (montoNetoConDescuento!!.valorReal())
                binding.tvNetocondescuento.text = "$" + montoNetoConDescuento!!.toTwoDecimals()
            } else {
                binding.tvNetocondescuento.text = "$0.00"
                binding.tvSubcondcto.visibility = View.INVISIBLE
                binding.tvNetocondescuento.visibility = View.INVISIBLE
            }
        } else {
            binding.tvNeto.text = "$0.00"
            binding.tvNetocondescuento.text = "$0.00"
            binding.tvSubcondcto.visibility = View.INVISIBLE
            binding.tvNetocondescuento.visibility = View.INVISIBLE

            // ---aqui tambien se coloca algo en caso de que sea 0 con descuento
        }
        cursor.close()
        if (negociacionIsActiva()) {
            if (precioTotalporArticulo!! >= montoMinimo!!) {
                binding.tvMtomin.setTextColor(Color.rgb(22, 129, 67))
            } else {
                binding.tvMtomin.setTextColor(Color.rgb(244, 67, 54))
            }
        }
    }

    private fun pedidoCondicion() {
        if (binding.RbFactura.isChecked) {
            documento = "1"
            if (binding.RbCredito.isChecked) {
                formaPago = "2"
                // asignarTipodePrecio();
            } else if (binding.RbPrepago.isChecked) {
                formaPago = "1"
                // tipoDePrecioaMostrar = "precio3";
            }
        } else if (binding.RbNotaEntrega.isChecked) {
            documento = "2"
            if (binding.RbCredito.isChecked) {
                formaPago = "2"
                // asignarTipodePrecio();
            } else if (binding.RbPrepago.isChecked) {
                formaPago = "1"
                // tipoDePrecioaMostrar = "precio3";
            }
        }
        // -.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.
    }

    private fun cargarClientes() {
        conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null)
        val keAndroid = conn.readableDatabase
        var cliente: Cliente
        listacliente = ArrayList()
        val cursor = keAndroid.rawQuery(
            "SELECT codigo, nombre FROM cliempre " + "WHERE vendedor ='$codUsuario1' AND empresa = '$codEmpresa' " + "ORDER BY nombre ASC",
            null
        )
        while (cursor.moveToNext()) {
            cliente = Cliente()
            cliente.setCodigo(cursor.getString(0))
            cliente.setNombre(cursor.getString(1))
            listacliente!!.add(cliente)
        }
        cursor.close()
        keAndroid.close()
        obtenerlistaCliente()
        // ArrayAdapter<CharSequence> adapterSpinner = new ArrayAdapter(getBaseContext(), R.layout.spinner_items , listainfoClientes);
        // spinner.setAdapter(adapterSpinner);
        // adapterSpinner.notifyDataSetChanged();
    }

    private fun obtenerlistaCliente() {
        listainfoClientes = ArrayList()
        listainfoClientes!!.add("Seleccione un Cliente...")
        for (i in listacliente!!.indices) {
            listainfoClientes!!.add(listacliente!![i].getCodigo() + " - " + listacliente!![i].getNombre())
        }
    }

    private fun cargarLineas() {
        carritoCompras()
        if (listacarrito!!.isNotEmpty()) {
            // adapter.notifyDataSetChanged();
            val carritoAdapter =
                CarritoAdapter(this@CreacionPedidoActivity, listacarrito, enlaceEmpresa)
            binding.ListPedido.adapter = carritoAdapter
            carritoAdapter.notifyDataSetChanged()
        } else {
            val carritoAdapter =
                CarritoAdapter(this@CreacionPedidoActivity, ArrayList(), enlaceEmpresa)
            binding.ListPedido.adapter = carritoAdapter
            // Toast.makeText(CreacionPedidoActivity.this, "Por favor, agrega lineas al pedido", Toast.LENGTH_SHORT).show();
        }
    }

    private fun iraCatalogo() {
        binding.menuCreacion.isEnabled = false
        Toast.makeText(this@CreacionPedidoActivity, "Cargando Datos", Toast.LENGTH_SHORT).show()
        val intent = Intent(applicationContext, CatalogoActivity::class.java)
        seleccion = 2
        intent.putExtra("Seleccion", seleccion)
        intent.putExtra("precioTotalporArticulo", precioTotalporArticulo)
        intent.putExtra("tipoDePrecioaMostrar", tipoDePrecioaMostrar)
        intent.putExtra("codigoCliente", codigoCliente)
        intent.putExtra("nroPedido", nroPedido)
        intent.putExtra("enpreventa", enpreventa)
        intent.putExtra("factura", binding.RbFactura.isChecked)
        startActivity(intent)
    }

    private fun carritoCompras() {
        listacarrito = ArrayList()
        conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null)
        val keAndroid = conn.writableDatabase
        val cursor = keAndroid.rawQuery(
            "SELECT kmv_codart, kmv_nombre, kmv_cant, kmv_stot, kmv_artprec, kmv_dctolin, kmv_stotdcto FROM ke_carrito " + "WHERE empresa = '$codEmpresa'",
            null
        )
        while (cursor.moveToNext()) {
            val carrito = Carrito()
            carrito.setCodigo(cursor.getString(0))
            carrito.setNombre(cursor.getString(1))
            carrito.setCantidad(cursor.getInt(2))
            carrito.setPrecio(cursor.getDouble(3))
            carrito.setPreciou(cursor.getDouble(4))
            println("por aqui" + cursor.getDouble(4))
            carrito.setDctolin(cursor.getDouble(5))
            carrito.setStotNeto(cursor.getDouble(6))
            listacarrito!!.add(carrito)
        }
        cursor.close()
        keAndroid.close()
    }

    private fun obtenerCondicionesEspeciales() {
        // CON ESTA FUNCION IDENTIFICO SI EL CLIENTE POSEE O NO LA POSIBLIDAD DE LLEVAR A CABO UNA NEGOCIACIÓN ESPECIAL
        val keAndroid = conn.writableDatabase
        val cursor = keAndroid.rawQuery(
            "SELECT kne_activa, kne_mtomin FROM cliempre WHERE codigo ='$codigoCliente' AND empresa = '$codEmpresa' and direccion IS NOT NULL and telefonos IS NOT NULL",
            null
        )
        while (cursor.moveToNext()) {
            negociacionActiva = cursor.getString(0)
            montoMinimo =
                cursor.getDouble(1) // IMPORTANTE: EL CLIENTE CUENTA CON UN MONTO MINIMO PARA LA NEG. ESPECIAL
        }
        cursor.close()
        if (negociacionActiva == "0") {
            binding.swNegoespecial.isEnabled = false
            binding.swNegoespecial.visibility = View.INVISIBLE
            binding.tvMtomin.isEnabled = false
            binding.tvMtomin.visibility = View.INVISIBLE
        } else if (negociacionActiva == "1") {
            binding.swNegoespecial.isEnabled = true
            binding.swNegoespecial.visibility = View.VISIBLE
        }
    }

    private fun negociacionIsActiva(): Boolean {
        return binding.swNegoespecial.isChecked
    }

    private fun procesarPedido() {
        if (codigoCliente == null) {
            Toast.makeText(
                this@CreacionPedidoActivity,
                "Debes Seleccionar un Cliente",
                Toast.LENGTH_SHORT
            ).show()
            binding.menuCreacion.isEnabled = true
        } else {
            if (!binding.RbFactura.isChecked && !binding.RbNotaEntrega.isChecked || !binding.RbCredito.isChecked && !binding.RbPrepago.isChecked) {
                Toast.makeText(
                    this@CreacionPedidoActivity,
                    "Por favor, seleccione las condiciones del pedido",
                    Toast.LENGTH_SHORT
                ).show()
                binding.menuCreacion.isEnabled = true
            } else {
                if (negociacionIsActiva()) {
                    if (montoNetoConDescuento!! >= montoMinimo!!) {
                        guardarDatosDelPedido()
                    } else {
                        Toast.makeText(
                            this@CreacionPedidoActivity,
                            "En negociación especial, el pedido debe cumplir con el monto mínimo asignado.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else if (!negociacionIsActiva()) {
                    negociacionActiva = "0"
                    guardarDatosDelPedido()
                }
            }
        }
    }

    private fun generarNumeroPedido() {
        val fechaHoy = Date(Calendar.getInstance().timeInMillis)
        val formatofecha = SimpleDateFormat("yyMM", Locale.getDefault())
        val fecha = formatofecha.format(fechaHoy)
        CorrelativoTexto = right(CorrelativoTexto, 4)
        nroPedido =
            codUsuario1!!.trim { it <= ' ' } + "-" + tipoDoc + "-" + fecha + CorrelativoTexto
    }

    private fun actualizaLista() {
        cargarLineas()
        sumaNeto()
    }

    private fun guardarDatosDelPedido() {
        val keAndroid = conn.writableDatabase
        // Cursor cursor1 = ke_android.rawQuery("SELECT kmv_codart, kmv_nombre, kmv_cant, kmv_stot, kmv_artprec, kmv_stotdcto FROM ke_carrito WHERE 1", null);
        val cursor2 = keAndroid.rawQuery(
            "SELECT kmv_codart, kmv_nombre, kmv_cant, kmv_stot, kmv_artprec, kmv_stotdcto FROM ke_carrito WHERE empresa = '$codEmpresa'",
            null
        )
        binding.menuCreacion.isEnabled = false
        if (cursor2.moveToFirst()) {
            pedidoCondicion()
            sumaNeto()
            val ktiCodcli = codigoCliente // obtenemos el codigo del cliente
            // System.out.println(kti_codcli);
            val ktiDocsolicitado = documento
            // System.out.println(documento);
            val ktiCondicion = formaPago
            // System.out.println(formaPago);
            val ktiCodven = codUsuario1!!.trim { it <= ' ' }
            val ktiTdoc = tipoDoc
            val ktiTotneto = precioTotalporArticulo
            // System.out.println(precioTotalporArticulo);
            kti_precio = preciocliente
            val ktiNdoc = nroPedido
            val tmpNombrecli = n_cliente
            val ktiNegesp = negociacionActiva
            val fechaTabla = Date(Calendar.getInstance().timeInMillis) //
            val formatoFechaTabla = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val ktiFchdoc = formatoFechaTabla.format(fechaTabla)
            try {
                // cargamos un content values para la cabecera
                val cvCabecera = ContentValues()
                keAndroid.beginTransaction() // iniciamos la tranasaccion

                // cargamos la informacion en el contentvalues
                cvCabecera.put("kti_codcli", ktiCodcli)
                cvCabecera.put("kti_codven", ktiCodven)
                cvCabecera.put("kti_docsol", ktiDocsolicitado)
                cvCabecera.put("kti_condicion", ktiCondicion)
                cvCabecera.put("kti_tdoc", ktiTdoc)
                cvCabecera.put("kti_ndoc", ktiNdoc)
                cvCabecera.put("kti_tipprec", kti_precio)
                cvCabecera.put("kti_nombrecli", tmpNombrecli)
                cvCabecera.put("kti_totneto", ktiTotneto)
                cvCabecera.put("kti_fchdoc", ktiFchdoc)
                cvCabecera.put("kti_status", "0")
                cvCabecera.put("fechamodifi", ktiFchdoc)
                cvCabecera.put("kti_negesp", ktiNegesp)
                cvCabecera.put(
                    "kti_totnetodcto",
                    valorReal(
                        montoNetoConDescuento!!
                    )
                )
                cvCabecera.put("empresa", codEmpresa)
                cvCabecera.put(
                    "dolarflete",
                    binding.cbFleteDol.isChecked
                ) // <-Guarda el valor de la checkbox del Flete Dolarizado
                // insertamos la cabecera del pedido
                keAndroid.insert("ke_opti", null, cvCabecera)
                for (i in listacarrito!!.indices) {
                    val cvLineas = ContentValues()
                    cvLineas.put("kmv_codart", listacarrito!![i].getCodigo())
                    cvLineas.put("kmv_nombre", listacarrito!![i].getNombre())
                    cvLineas.put("kti_tipprec", kti_precio)
                    cvLineas.put("kmv_cant", listacarrito!![i].getCantidad())
                    cvLineas.put("kti_tdoc", ktiTdoc)
                    cvLineas.put("kti_ndoc", ktiNdoc)
                    cvLineas.put("kmv_stot", listacarrito!![i].getPrecio())
                    cvLineas.put("kmv_artprec", listacarrito!![i].getPreciou())
                    cvLineas.put("kmv_dctolin", listacarrito!![i].getDctolin())
                    cvLineas.put("kmv_stotdcto", listacarrito!![i].getStotNeto())
                    cvLineas.put("empresa", codEmpresa)
                    // insertamos las lineas
                    keAndroid.insert("ke_opmv", null, cvLineas)
                    keAndroid.execSQL(
                        "UPDATE ke_limitart SET status = '1' WHERE kli_codart = '${listacarrito!![i].getCodigo()}' AND kli_codven ='$codUsuario1' AND kli_codcli = '$codigoCliente' AND kli_track ='$nroPedido' AND empresa = '$codEmpresa'"
                    )

                    /*
                   COMPROMETIDO
                    Cursor cursor_s = ke_android.rawQuery("SELECT comprometido FROM articulo WHERE codigo='"+ listacarrito.get(i).getCodigo() +"'", null);

                    double comprometido = 0;

                    while (cursor_s.moveToNext()){
                        comprometido = cursor_s.getDouble(0);
                    }

                    System.out.println("SELECT existencia FROM articulo WHERE codigo='"+ listacarrito.get(i).getCodigo() +"'");
                    System.out.println("AA " + (listacarrito.get(i).getCantidad()));
                    System.out.println("BB " + (comprometido));
                    System.out.println("hola " + (comprometido + listacarrito.get(i).getCantidad()));

                    ke_android.execSQL("UPDATE articulo SET comprometido = '" + (comprometido + listacarrito.get(i).getCantidad()) + "' WHERE codigo = '"+ listacarrito.get(i).getCodigo() +"' ;");
                     */
                }
                // limpiamos ke_carrito
                keAndroid.delete("ke_carrito", "empresa = '$codEmpresa'", null)

                // aumentamos el correlativo del pedido
                val aumentarCorrelatiov = ContentValues()
                aumentarCorrelatiov.put("kco_numero", nroCorrelativo)
                aumentarCorrelatiov.put("kco_vendedor", codUsuario1)
                aumentarCorrelatiov.put("empresa", codEmpresa)

                // insertamos el correlativo
                keAndroid.insert("ke_correla", null, aumentarCorrelatiov)
                keAndroid.setTransactionSuccessful()
            } catch (ex: Exception) {
                Toast.makeText(this@CreacionPedidoActivity, "Error en: $ex", Toast.LENGTH_SHORT)
                    .show()
            } finally {
                keAndroid.endTransaction()
            }
            Toast.makeText(
                this@CreacionPedidoActivity,
                "Pedido creado exitosamente",
                Toast.LENGTH_SHORT
            ).show()
            val intent = Intent(applicationContext, PedidosActivity::class.java)
            startActivity(intent)
        } else {
            Toast.makeText(
                this@CreacionPedidoActivity,
                "Por favor, agrega artículos al pedido",
                Toast.LENGTH_SHORT
            ).show()
        }
        cursor2.close()
    }

    private fun recuprarSeleccion() {
        val sharpref = getSharedPreferences("sharpref", MODE_PRIVATE)
        indexposicion = sharpref.getInt("Dato", 0)
        println(indexposicion)
        // Mueve a esa posición el Spinner
        // spinner.setSelection(indexposicion);
    }

    private fun asignarTipodePrecio() {
        val keAndroid = conn.writableDatabase
        val cursor = keAndroid.rawQuery(
            "SELECT precio FROM cliempre WHERE codigo='$codigoCliente' AND empresa = '$codEmpresa'",
            null
        )
        while (cursor.moveToNext()) {
            preciocliente = cursor.getDouble(0)
            enteroPrecio = (preciocliente!!).roundToInt()
        }
        cursor.close()
        tipoDePrecioaMostrar = when (enteroPrecio) {
            1 -> "precio1"
            2 -> "precio2"
            3 -> "precio3"
            4 -> "precio4"
            5 -> "precio5"
            6 -> "precio6"
            7 -> "precio7"
            else -> "precio1"
        }
        keAndroid.close()
    }

    private fun actualizarPrecios() {
        val keAndroid = conn.writableDatabase
        val cursorcarrito = keAndroid.rawQuery(
            "SELECT kmv_codart, kmv_cant FROM ke_carrito WHERE empresa = '$codEmpresa'",
            null
        )
        while (cursorcarrito.moveToNext()) {
            val codigoArticuloCarrito = cursorcarrito.getString(0)
            val cantidadCarrito = cursorcarrito.getDouble(1)
            val cursorprecio = keAndroid.rawQuery(
                "SELECT $tipoDePrecioaMostrar FROM articulo " + "WHERE codigo ='$codigoArticuloCarrito' AND empresa = '$codEmpresa'",
                null
            )
            while (cursorprecio.moveToNext()) {
                var precioNuevo = cursorprecio.getDouble(0)
                precioNuevo = precioNuevo.valorReal()
                var subTotalNuevo = precioNuevo * cantidadCarrito
                subTotalNuevo = subTotalNuevo.valorReal()
                keAndroid.execSQL(
                    "UPDATE ke_carrito SET kmv_artprec =$precioNuevo, kmv_stot =$subTotalNuevo WHERE kmv_codart='$codigoArticuloCarrito' AND empresa = '$codEmpresa'"
                )
            }
            cursorprecio.close()
        }
        cursorcarrito.close()
        keAndroid.close()
    }

    override fun onResume() {
        super.onResume()
        cargarEnlace()
        cargarLineas()
        // CargarClientes();
        sumaNeto()
        recuprarSeleccion()
        binding.menuCreacion.isEnabled = true
        // adapter.notifyDataSetChanged();
    }

    fun consultarDisponibilidad(
        codUsuario: String,
        codCliente: String,
        codArticulo: String
    ): Int {
        var resultado = 0
        println(codUsuario)
        println(codCliente)
        println(codArticulo)
        val keAndroid = conn.readableDatabase
        val cuComp = keAndroid.rawQuery(
            "SELECT SUM(kli_cant) FROM ke_limitart " + "WHERE kli_codven ='$codUsuario' AND kli_codcli='$codCliente' AND kli_codart='$codArticulo' AND status ='1' AND empresa = '$codEmpresa'",
            null
        )
        while (cuComp.moveToNext()) {
            resultado = cuComp.getInt(0)
        }
        cuComp.close()
        return resultado
    }

    private fun guardarLimite(
        tracking: String,
        codUsuario: String,
        codCliente: String,
        codArticulo: String,
        cantidad: Int,
        fechaHoy: String,
        fechaVence1: String,
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
        guardarArticulo.put("kli_fechavence", fechaVence1)
        guardarArticulo.put("status", status)
        guardarArticulo.put("empresa", codEmpresa)
        keAndroid.insert("ke_limitart", null, guardarArticulo)
    }

    companion object {
        var documento: String? = null
        var formaPago: String? = null
        var seleccion = 0
        var indexposicion = 0
        var nroCorrelativo = 0
        var precioTotalporArticulo: Double? = null
        var kti_precio: Double? = null
        var preciocliente: Double? = null
        var montoMinimo: Double? = null
        var montoNetoConDescuento: Double? = null
        var descuentoTotal: Double? = null
        var descuentoDeLaLinea: Double? = null
        var enteroPrecio = 0
        var conteoDocs = 0
        var nroPedido: String? = null
        var CorrelativoTexto: String? = null
        var nombreEmpresa = ""
        var enlaceEmpresa = ""
        var tipoDoc = "PED"
        var enpreventa: String? = null
        var n_cliente: String? = null
        var tipoDePrecioaMostrar: String? = null
        var negociacionActiva: String? = null
        var fechaVence: String? = null
        var nuevoVencimiento: String? = null
        var kti_negesp: String? = null
        fun right(valor: String?, longitud: Int): String {
            // una función "right" utilizando la clase substring
            return valor!!.substring(valor.length - longitud)
        }
    }

    fun setColors() {
        binding.apply {
            RbFactura.buttonTintList = RbFactura.setColorRadioButon(Constantes.AGENCIA)
            RbNotaEntrega.buttonTintList = RbNotaEntrega.setColorRadioButon(Constantes.AGENCIA)
            RbCredito.buttonTintList = RbCredito.setColorRadioButon(Constantes.AGENCIA)
            RbPrepago.buttonTintList = RbPrepago.setColorRadioButon(Constantes.AGENCIA)

            cbFleteDol.buttonTintList = cbFleteDol.setColorRadioButon(Constantes.AGENCIA)
            //cbFleteDol.setTextColor()
            //cbFleteDol.

            tvNombreCliente.setDrawableHeadAgencia(Constantes.AGENCIA)

            menuCreacion.setBackgroundColor(menuCreacion.colorAgencia(Constantes.AGENCIA))

            linearLayout4.setBackgroundResource(linearLayout4.changeColorMarco(Constantes.AGENCIA))

            menuCreacion.itemTextColor = menuCreacion.colorIconReclamo(Constantes.AGENCIA)
            menuCreacion.itemIconTintList = menuCreacion.colorIconReclamo(Constantes.AGENCIA)
        }
    }

    override fun getTheme(): Resources.Theme {
        val theme = super.getTheme()
        theme.applyStyle(setThemeAgencia(Constantes.AGENCIA), true)
        // you could also use a switch if you have many themes that could apply
        return theme
    }
}
