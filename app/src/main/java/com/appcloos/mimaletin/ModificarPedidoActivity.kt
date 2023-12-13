package com.appcloos.mimaletin

import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
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
import com.appcloos.mimaletin.ObjetoUtils.Companion.valorReal
import com.appcloos.mimaletin.databinding.ActivityModificarPedidoBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class ModificarPedidoActivity : AppCompatActivity() {
    private lateinit var adapter: ArrayAdapter<*>
    var adapterSpinner: ArrayAdapter<CharSequence>? = null
    var listainfo: ArrayList<String>? = null
    var listapedido: ArrayList<String>? = null
    private var listacarritoMod: ArrayList<Carrito>? = null
    private lateinit var spinner: Spinner
    private lateinit var conn: AdminSQLiteOpenHelper
    private lateinit var tv_subtotal: TextView
    var subtotal: Double? = null
    var NetoTotal = 0.00
    private var montoMinimoTotal = 0.00
    private lateinit var ibt_modificar: ImageButton
    private lateinit var carritoAdapter: CarritoAdapter
    var enpreventa = "0"
    private var cod_usuario: String? = null
    var codEmpresa: String? = null
    var formato = DecimalFormat("#,###.00")
    private var APP_ITEMS_FACTURAS = 0
    private var APP_ITEMS_NOTAS_ENTREGA = 0

    private var precioTotalporArticulo = 0.0
    private var montoNetoConDescuento = 0.0

    private lateinit var enlaceEmpresa: String


    private val mOnNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.ic_agregarart -> {
                    if (codigoCliente == null || !binding.RbFacturaMod.isChecked && !binding.RbNotaEntregaMod.isChecked || !binding.RbPrepagoMod.isChecked && !binding.RbCreditoMod.isChecked) {
                        Toast.makeText(
                            this@ModificarPedidoActivity,
                            "Debes Seleccionar un cliente, factura o nota de entrega y credito o prepago",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        if (binding.RbFacturaMod.isChecked && listacarritoMod!!.size > APP_ITEMS_FACTURAS || binding.RbNotaEntregaMod.isChecked && listacarritoMod!!.size > APP_ITEMS_NOTAS_ENTREGA) {
                            if (binding.RbFacturaMod.isChecked) {
                                Toast.makeText(
                                    this@ModificarPedidoActivity,
                                    "Para facturas debe tener un maximo de $APP_ITEMS_FACTURAS lineas de articulos",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                Toast.makeText(
                                    this@ModificarPedidoActivity,
                                    "Para notas de entrega debe tener un maximo de $APP_ITEMS_NOTAS_ENTREGA lineas de articulos",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } else {
                            //Asignacion de precio para saber si es negociacion especial
                            //asignarTipodePrecio();
                            iraCatalogo()
                        }
                    }
                    return@OnNavigationItemSelectedListener true
                }

                R.id.ic_procesarpedido -> {
                    if (!negociacionIsActiva() && montoNetoConDescuento < montoMinimoTotal) {
                        Toast.makeText(
                            this@ModificarPedidoActivity,
                            "Para procesar el pedido, debe cumplir con el monto mínimo de $$montoMinimoTotal",
                            Toast.LENGTH_LONG
                        ).show()
                    } else if (montoNetoConDescuento >= montoMinimoTotal) {
                        if (binding.RbFacturaMod.isChecked && listacarritoMod!!.size > APP_ITEMS_FACTURAS || binding.RbNotaEntregaMod.isChecked && listacarritoMod!!.size > APP_ITEMS_NOTAS_ENTREGA) {
                            if (binding.RbFacturaMod.isChecked) {
                                Toast.makeText(
                                    this@ModificarPedidoActivity,
                                    "Para facturas debe tener un maximo de $APP_ITEMS_FACTURAS lineas de articulos",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                Toast.makeText(
                                    this@ModificarPedidoActivity,
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

    private lateinit var binding: ActivityModificarPedidoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityModificarPedidoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null)

        setColors()

        val intent = intent
        codigoPedido = intent.getStringExtra("codigopedido")
        n_cliente = intent.getStringExtra("n_cliente")
        codigoCliente = intent.getStringExtra("codigocliente")

        supportActionBar!!.title = "Pedido: $codigoPedido"

        val preferences = getSharedPreferences("Preferences", MODE_PRIVATE)
        cod_usuario = preferences.getString("cod_usuario", null)
        codEmpresa = preferences.getString("codigoEmpresa", null)

        APP_ITEMS_FACTURAS = conn.getConfigNum("APP_ITEMS_FACTURAS", codEmpresa!!).roundToInt()
        APP_ITEMS_NOTAS_ENTREGA =
            conn.getConfigNum("APP_ITEMS_NOTAS_ENTREGA", codEmpresa!!).roundToInt()
        enlaceEmpresa = conn.getCampoStringCamposVarios(
            "ke_enlace",
            "kee_url",
            listOf("kee_codigo"),
            listOf(codEmpresa!!)
        )

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

        binding.tvClientepedido.text = n_cliente

        binding.menuModifi.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        lineasDelPedido()
        obtenerCondicionesEspeciales()
        cargarCondiciones()
        asignarTipodePrecio()
        validarNegActivo()
        cargarLineas()
        sumaNeto()
        validarSiHayPreventa()
        montoMinimoTotal = obtenerMontoMinimoTotal()
        if (binding.RbFacturaMod.isChecked) {
            binding.tvAvisobloqueo.visibility = View.VISIBLE
            binding.tvAvisobloqueo.text =
                "Las facturas solo tendran un  maximo de 12 lineas de articulos"
        }
        binding.radioGroupDocMod.setOnCheckedChangeListener { _: RadioGroup?, checkedId: Int ->
            if (checkedId == binding.RbFacturaMod.id) {
                binding.tvAvisobloqueo.visibility = View.VISIBLE
                binding.tvAvisobloqueo.text =
                    "Las facturas solo tendrán un máximo de $APP_ITEMS_FACTURAS líneas de artículos"
            } else if (checkedId == binding.RbNotaEntregaMod.id) {
                binding.tvAvisobloqueo.visibility = View.VISIBLE
                binding.tvAvisobloqueo.text =
                    "Las N/E solo tendrán un máximo de $APP_ITEMS_NOTAS_ENTREGA líneas de artículos"
            }
        }
        binding.RbPrepagoMod.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            recalculoPrecio(
                isChecked
            )
        }
        /*if (binding.RbNotaEntregaMod.isChecked) {
            binding.RbPrepagoMod.isChecked = false
            binding.RbPrepagoMod.visibility = View.INVISIBLE
            binding.RbPrepagoMod.isEnabled = false
            binding.RbCreditoMod.isChecked = true
        } else {
            binding.RbPrepagoMod.visibility = View.VISIBLE
            binding.RbPrepagoMod.isEnabled = true
        }*/
        binding.RbNotaEntregaMod.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            /*if (isChecked) {
                binding.RbPrepagoMod.isChecked = false
                binding.RbPrepagoMod.visibility = View.INVISIBLE
                binding.RbPrepagoMod.isEnabled = false
                binding.RbCreditoMod.isChecked = true
            } else {
                binding.RbPrepagoMod.visibility = View.VISIBLE
                binding.RbPrepagoMod.isEnabled = true
            }*/
        }


        //aqui va el metodo para borrar un articulo del pedido (onlongclick)
        binding.ListPedidoMod.setOnItemLongClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
            val codigo = listacarritoMod!![position].getCodigo()
            val ventana = AlertDialog.Builder(
                ContextThemeWrapper(
                    this@ModificarPedidoActivity,
                    setAlertDialogTheme(Constantes.AGENCIA)
                )
            )
            ventana.setTitle("Mensaje del Sistema")
            ventana.setMessage("¿Desea eliminar este artículo?")
            ventana.setPositiveButton("Aceptar") { _: DialogInterface?, _: Int ->
                val keAndroid = conn.writableDatabase
                keAndroid.execSQL("DELETE FROM ke_carrito WHERE kmv_codart ='$codigo'")
                actualizaLista()
                Toast.makeText(this@ModificarPedidoActivity, "Artículo borrado", Toast.LENGTH_SHORT)
                    .show()
            }
            ventana.setNegativeButton("Cancelar") { dialogInterface: DialogInterface, _: Int -> dialogInterface.dismiss() }
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
        //este metodo permite cambiar las cantidades del articulo que se encuentra en ke_carrito al momento de ser seleccionado
        binding.ListPedidoMod.setOnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
            val codigo = listacarritoMod!![position].getCodigo()
            val dctolin = listacarritoMod!![position].getDctolin()
            val cajatexto = EditText(
                ContextThemeWrapper(
                    this@ModificarPedidoActivity,
                    setEditTextTheme(codEmpresa)
                )
            )
            cajatexto.inputType = InputType.TYPE_CLASS_NUMBER
            conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null)
            val keAndroid = conn.writableDatabase
            val cursorMul = keAndroid.rawQuery(
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
                    this@ModificarPedidoActivity,
                    setAlertDialogTheme(Constantes.AGENCIA)
                )
            )
            ventana.setTitle("Modificar Linea")
            ventana.setMessage("Porfavor, elige la cantidad")
            /*if (vta_minenx == 1) {
                LinearLayout layout_h = new LinearLayout(ModificarPedidoActivity.this);
                layout_h.setOrientation(LinearLayout.HORIZONTAL);
                final TextView mensajeCantidadMultiplo = new TextView(ModificarPedidoActivity.this);
                mensajeCantidadMultiplo.setTextSize(20);
                mensajeCantidadMultiplo.setTypeface(null, Typeface.BOLD);
                mensajeCantidadMultiplo.setTextColor(Color.parseColor("#313131"));
                mensajeCantidadMultiplo.setText(((int) Math.round(vta_Min)) + " x ");
                //mensajeCantidadMultiplo.setLayoutParams(params);
                cajatexto.setWidth(1000);
                cajatexto.setHint("Cantidad de paquetes a pedir");
                layout_h.addView(mensajeCantidadMultiplo);
                //cajatexto.setLayoutParams(params2);
                layout_h.addView(cajatexto);
                ventana.setView(layout_h);
            } else {
                ventana.setView(cajatexto);
            }*/
            val layoutH = LinearLayout(this@ModificarPedidoActivity)
            layoutH.orientation = LinearLayout.VERTICAL
            val darDescuento = CheckBox(this@ModificarPedidoActivity)
            val descuentoBool = conn.getCampoDoubleCamposVarios(
                "articulo",
                "dctotope",
                listOf("codigo", "empresa"),
                listOf(codigo, codEmpresa!!)
            )
            //2023-09-14 Verificando que el articulo permita descuento
            if (descuentoBool > 0.0) {
                //final CheckBox darDescuento = new CheckBox(CreacionPedidoActivity.this);
                darDescuento.text = "Dar Descuento del Articulo"
                //2023-09-14 Checkeo el boton si se chekeo en catalogo
                if (dctolin > 0.0) {
                    darDescuento.isChecked = true
                }
                layoutH.addView(darDescuento)
            }
            if (vtaMinenx == 1) {
                val layoutVtaMinenx = LinearLayout(this@ModificarPedidoActivity)
                layoutVtaMinenx.orientation = LinearLayout.HORIZONTAL
                val mensajeCantidadMultiplo = TextView(this@ModificarPedidoActivity)
                mensajeCantidadMultiplo.textSize = 20f
                mensajeCantidadMultiplo.setTypeface(null, Typeface.BOLD)
                mensajeCantidadMultiplo.setTextColor(Color.parseColor("#313131"))
                mensajeCantidadMultiplo.text = vtaMin.roundToInt().toString() + " x "
                //mensajeCantidadMultiplo.setLayoutParams(params);
                cajatexto.width = 1000
                cajatexto.hint = "Cantidad de paquetes a pedir"
                layoutVtaMinenx.addView(mensajeCantidadMultiplo)
                //cajatexto.setLayoutParams(params2);
                layoutVtaMinenx.addView(cajatexto)
                layoutH.addView(layoutVtaMinenx)
            } else {
                cajatexto.hint = "Cantidad a pedir"
                layoutH.addView(cajatexto)
            }
            ventana.setView(layoutH)
            ventana.setPositiveButton("Aceptar") { _: DialogInterface?, _: Int ->
                val cantidadNueva = cajatexto.text.toString()
                val aprobarDescuento = darDescuento.isChecked
                val cantidad = cantidadNueva.toInt()
                if (cantidad != 0) {
                    val cursor = keAndroid.rawQuery(
                        "SELECT $tipoDePrecioaMostrar, (existencia - comprometido), vta_min, vta_max, vta_minenx FROM articulo " +
                                "WHERE codigo ='$codigo' AND empresa = '$codEmpresa'",
                        null
                    )
                    cursor.moveToNext()
                    val precio = valorReal(cursor.getDouble(0))
                    val existencia = cursor.getDouble(1)
                    val ventaMax = cursor.getDouble(3)
                    val ventaMin = cursor.getDouble(2)
                    val vtaMinenx1 = cursor.getInt(4)
                    cursor.close()
                    val existenciaValidar = existencia.roundToInt()
                    println(cantidad)
                    println(existenciaValidar)
                    if (cantidad > existenciaValidar) {
                        Toast.makeText(
                            this@ModificarPedidoActivity,
                            "La cantidad no puede ser superior a la existencia",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        if (ventaMin > 0) {
                            println("Multiplo: $vtaMinenx1")
                            if (vtaMinenx1 == 1) {
                                if (cantidad * ventaMin > existencia) {
                                    Toast.makeText(
                                        this@ModificarPedidoActivity,
                                        "Debe de elegir una cantidad dentro de la existencia",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else if (cantidad * ventaMin <= existencia) {
                                    val cantidadNew = (cantidad * ventaMin).toInt()
                                    println("Nueva cantidad $cantidadNew")
                                    var precioTotal = precio * cantidadNew
                                    println("Precio total: $precioTotal")
                                    precioTotal = precioTotal.valorReal()
                                    keAndroid.beginTransaction()
                                    try {
                                        var precioNuevo = precioTotal
                                        precioNuevo = precioNuevo.valorReal()

                                        //2023-09-14 If para saber si se guarda el descuento o no
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
                                        keAndroid.execSQL("UPDATE ke_carrito SET kmv_cant=$cantidadNew, kmv_stot =$precioTotal, kmv_stotdcto =$mtoDctoNuevo, kmv_dctolin =$descuento WHERE kmv_codart ='$codigo' AND empresa = '$codEmpresa'")
                                        keAndroid.setTransactionSuccessful()
                                        keAndroid.endTransaction()
                                        Toast.makeText(
                                            this@ModificarPedidoActivity,
                                            "Artículo modificado",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } catch (ex: Exception) {
                                        println("--Error--")
                                        ex.printStackTrace()
                                        println("--Error--")
                                        keAndroid.endTransaction()
                                    }
                                }
                            } else {
                                if (cantidad < ventaMin) {
                                    Toast.makeText(
                                        this@ModificarPedidoActivity,
                                        "Debe cumplir con la cantidad mínima para la venta",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else if (cantidad >= ventaMin) {
                                    var precioTotal = precio * cantidad.toDouble()
                                    precioTotal = precioTotal.valorReal()
                                    keAndroid.beginTransaction()
                                    try {
                                        var precioNuevo = precio * cantidad.toDouble()
                                        precioNuevo = precioNuevo.valorReal()

                                        //2023-09-14 If para saber si se guarda el descuento o no
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
                                        keAndroid.execSQL("UPDATE ke_carrito SET kmv_cant=$cantidad, kmv_stot =$precioTotal, kmv_stotdcto =$mtoDctoNuevo, kmv_dctolin =$descuento WHERE kmv_codart ='$codigo' AND empresa = '$codEmpresa'")
                                        keAndroid.setTransactionSuccessful()
                                        keAndroid.endTransaction()
                                        Toast.makeText(
                                            this@ModificarPedidoActivity,
                                            "Artículo modificado",
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
                            var precioTotal = precio * cantidad.toDouble()
                            precioTotal = precioTotal.valorReal()
                            keAndroid.beginTransaction()
                            try {
                                var precioNuevo = precio * cantidad.toDouble()
                                precioNuevo = precioNuevo.valorReal()

                                //2023-09-14 If para saber si se guarda el descuento o no
                                var mtoDctoNuevo: Double
                                val descuento: Double
                                if (aprobarDescuento) {
                                    mtoDctoNuevo = precioNuevo - precioNuevo * (descuentoBool / 100)
                                    mtoDctoNuevo = mtoDctoNuevo.valorReal()
                                    descuento = descuentoBool
                                } else {
                                    mtoDctoNuevo = precioTotal
                                    descuento = 0.0
                                }
                                keAndroid.execSQL("UPDATE ke_carrito SET kmv_cant=$cantidad, kmv_stot =$precioTotal, kmv_stotdcto =$mtoDctoNuevo, kmv_dctolin =$descuento WHERE kmv_codart ='$codigo' AND empresa = '$codEmpresa'")
                                keAndroid.setTransactionSuccessful()
                                keAndroid.endTransaction()
                                //finish();
                                Toast.makeText(
                                    this@ModificarPedidoActivity,
                                    "Artículo modificado",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } catch (ex: Exception) {
                                println("--Error--")
                                ex.printStackTrace()
                                println("--Error--")
                                keAndroid.endTransaction()
                            }
                        }
                        /*
                            Double precioNuevo = precio * Double.valueOf(cantidad);
                            precioNuevo = Math.round(precioNuevo * 100.0) / 100.00;

                            Double mtoDctoNuevo = precioNuevo - (precioNuevo * (dctolin/100));
                            mtoDctoNuevo = Math.round(mtoDctoNuevo * 100.0) / 100.00;

                            ke_android.execSQL("UPDATE ke_carrito SET kmv_cant=" + cantidad + ", kmv_stot =" + precioNuevo + ", kmv_stotdcto =" + mtoDctoNuevo + " WHERE kmv_codart ='" + codigo + "'");
                            actualizaLista();

                            Toast.makeText(CreacionPedidoActivity.this, "Artículo modificado", Toast.LENGTH_SHORT).show();*/
                        Toast.makeText(
                            this@ModificarPedidoActivity,
                            "Articulo añadido",
                            Toast.LENGTH_LONG
                        ).show()
                        actualizaLista()

                        //Toast.makeText(ModificarPedidoActivity.this, "Artículo modificado", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(
                        this@ModificarPedidoActivity,
                        "La existencia no puede ser 0",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            ventana.setNegativeButton("Cancelar") { dialogInterface: DialogInterface, _: Int -> dialogInterface.dismiss() }
            val dialogo = ventana.create()
            dialogo.show()

            val nbutton: Button = dialogo.getButton(DialogInterface.BUTTON_NEGATIVE)
            nbutton.apply {
                setTextColor(colorTextAgencia(Constantes.AGENCIA))
            }

            val pbutton: Button = dialogo.getButton(DialogInterface.BUTTON_POSITIVE)
            pbutton.apply {
                setTextColor(colorTextAgencia(Constantes.AGENCIA))
            }
        }
        binding.swNegoespecialMod.setOnCheckedChangeListener { compoundButton: CompoundButton, _: Boolean ->
            if (compoundButton.isChecked) {
                binding.tvMontominMod.isEnabled = true
                binding.tvMontominMod.visibility = View.VISIBLE
                binding.tvMontominMod.text = "Monto Mín: $$montoMinimoMod"
            } else {
                binding.tvMontominMod.isEnabled = false
                binding.tvMontominMod.visibility = View.INVISIBLE
            }
        }
        if (NOEMIFAC == 1) {
            binding.RbFacturaMod.visibility = View.INVISIBLE
        } else {
            binding.RbFacturaMod.visibility = View.VISIBLE
        }
        if (NOEMINOTA == 1) {
            binding.RbNotaEntregaMod.visibility = View.INVISIBLE
        } else {
            binding.RbNotaEntregaMod.visibility = View.VISIBLE
        }
        if (NOEMIFAC == 1 && NOEMINOTA == 1) {
            Toast.makeText(this, "Cliente suspendido", Toast.LENGTH_SHORT).show()
            finish()
        }
        binding.RbFacturaMod.setOnClickListener {
            analizarArticulos(
                "vta_solone",
                listacarritoMod
            )
        }
        binding.RbNotaEntregaMod.setOnClickListener {
            analizarArticulos(
                "vta_solofac",
                listacarritoMod
            )
        }
    }

    private fun analizarArticulos(campo: String, listacarrito: ArrayList<Carrito>?) {
        var num = 0
        //int numNE = 0;
        for (i in listacarrito!!.indices) {
            num = conn.getCampoIntCamposVarios(
                "articulo",
                campo,
                listOf("codigo", "empresa"),
                listOf(listacarrito[i].codigo, codEmpresa!!)
            )
            //numNE += conn.getCampoInt("articulo", "vta_solone", "codigo", listacarrito.get(i).codigo);
            if (num > 0) {
                break
            }
        }
        if (num > 0 && binding.RbNotaEntregaMod.isChecked) {
            Toast.makeText(
                this,
                "Posee artículos que solo están disponibles para Facturas",
                Toast.LENGTH_SHORT
            ).show()
            binding.RbFacturaMod.isChecked = true
        } else if (num > 0 && binding.RbFacturaMod.isChecked) {
            Toast.makeText(
                this,
                "Posee artículos que solo están disponibles para Notas de Entrega",
                Toast.LENGTH_SHORT
            ).show()
            binding.RbNotaEntregaMod.isChecked = true
        }
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

            //2023-06-05 se comento debido a que la opcion de prepago se borrara y ahora se llamara pago BCV y se asignara precio1 (Posiblemente precio3 se elimine)
            /*tipoDePrecioaMostrar = "precio3";
            preciocliente = 3.0;*/
        } else {
            cursor.moveToFirst()
            preciocliente = cursor.getDouble(0)
            enteroPrecio = (preciocliente!!).roundToInt()
            tipoDePrecioaMostrar = "precio$enteroPrecio"
        }
        cursor.close()
        println("precio que esta cogiendo (upa): $tipoDePrecioaMostrar")
        // tipoDePrecioaMostrar = (b)?"precio3":"precio"+enteroPrecio;
        keAndroid = conn.writableDatabase
        val cursorMain =
            keAndroid.rawQuery("SELECT * FROM ke_carrito AND empresa = '$codEmpresa'", null)
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
            keAndroid.execSQL("UPDATE ke_carrito SET kmv_artprec=$precio, kmv_stot= $precioTotalRedondo, kmv_stotdcto=$precioTotalRedondo WHERE kmv_codart ='$codigo' AND empresa = '$codEmpresa'")
            cargarLineas()
            sumaNeto()
        }
        cursorMain.close()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemid = item.itemId
        if (itemid == android.R.id.home) {
            validarSalida()
        }
        //return super.onOptionsItemSelected(item);
        return true
    }

    override fun onBackPressed() {
        validarSalida()
    }

    private fun validarSalida() {
        val dialog = AlertDialog.Builder(
            ContextThemeWrapper(
                this@ModificarPedidoActivity,
                setAlertDialogTheme(Constantes.AGENCIA)
            )
        ).setTitle("Salir").setMessage("¿Está seguro de desear salir?").setCancelable(true)
            .setPositiveButton("Si", null).setNegativeButton("No", null).show()
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
            dialog.dismiss()
            finish()
        }
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
            .setOnClickListener { dialog.dismiss() }

        val nbutton: Button = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
        nbutton.apply {
            setTextColor(colorTextAgencia(Constantes.AGENCIA))
        }

        val pbutton: Button = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
        pbutton.apply {
            setTextColor(colorTextAgencia(Constantes.AGENCIA))
        }
    }

    private fun validarSiHayPreventa() {
        conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null)
        val keAndroid = conn.writableDatabase
        val cursor = keAndroid.rawQuery(
            "SELECT count(ke_opmv.kmv_codart) FROM ke_opmv LEFT JOIN articulo ON articulo.codigo = ke_opmv.kmv_codart " +
                    "WHERE articulo.enpreventa = '1' AND kti_ndoc ='$codigoPedido' AND ke_opmv.empresa = '$codEmpresa'",
            null
        )
        var conteo = 0
        if (cursor.moveToFirst()) {
            conteo = cursor.getInt(0)
        }
        cursor.close()
        if (conteo > 0) {
            enpreventa = "1"
        }
    }

    private fun obtenerCondicionesEspeciales() {
        //CON ESTA FUNCION IDENTIFICO SI EL CLIENTE POSEE O NO LA POSIBLIDAD DE LLEVAR A CABO UNA NEGOCIACIÓN ESPECIAL
        val keAndroid = conn.writableDatabase
        val cursor = keAndroid.rawQuery(
            "SELECT kne_activa, kne_mtomin FROM cliempre WHERE codigo ='$codigoCliente' AND empresa = '$codEmpresa'",
            null
        )
        while (cursor.moveToNext()) {
            negociacionActivaMod = cursor.getString(0)
            montoMinimoMod =
                cursor.getDouble(1) //IMPORTANTE: EL CLIENTE CUENTA CON UN MONTO MINIMO PARA LA NEG. ESPECIAL
        }
        cursor.close()
        if (negociacionActivaMod == "0") {
            binding.swNegoespecialMod.isEnabled = false
            binding.swNegoespecialMod.visibility = View.INVISIBLE
            binding.tvMontominMod.isEnabled = false
            binding.tvMontominMod.visibility = View.INVISIBLE
        } else if (negociacionActivaMod == "1") {
            binding.swNegoespecialMod.isEnabled = true
            binding.swNegoespecialMod.visibility = View.VISIBLE
        }
    }

    private fun negociacionIsActiva(): Boolean {
        return binding.swNegoespecialMod.isChecked
    }

    private fun validarNegActivo() {
        if (negociacionIsActiva()) {
            binding.tvMontominMod.text = "Monto Mín: $$montoMinimoMod"
            binding.tvMontominMod.isEnabled = true
            binding.tvMontominMod.visibility = View.VISIBLE
            binding.tvMontominMod.setTextColor(Color.rgb(22, 129, 67))
        }
    }

    //este es el metodo para procesar el pedido
    private fun procesarPedido() {
        if (negociacionIsActiva()) {
            if (precioTotalporArticulo >= montoMinimoMod!!) {
                guardarDatosPedidoMod()
            } else {
                Toast.makeText(
                    this@ModificarPedidoActivity,
                    "En negociación especial, el pedido debe cumplir con el monto mínimo asignado.",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else if (!negociacionIsActiva()) {
            negociacionActivaMod = "0"
            guardarDatosPedidoMod()
        }
    }

    //con este metodo actualizamos las lineas del carrito y del neto cada vez que se produce un cambio (modificacion/eliminacion)
    private fun actualizaLista() {
        cargarLineas()
        sumaNeto()
    }

    /** */ //este metodo realiza la sumatoria del neto en funcion a los articulos que se van agregando en ke_carrito
    private fun sumaNeto() {
        val keAndroid = conn.writableDatabase
        val cursor = keAndroid.rawQuery(
            "SELECT SUM(kmv_stot), SUM(kmv_dctolin), SUM(kmv_stotdcto) FROM ke_carrito WHERE empresa = '$codEmpresa'",
            null
        )
        if (cursor.moveToNext()) {
            precioTotalporArticulo = cursor.getDouble(0)
            precioTotalporArticulo = precioTotalporArticulo.valorReal()
            println(precioTotalporArticulo)
            descuentoTotal = cursor.getDouble(1)
            montoNetoConDescuento = cursor.getDouble(2)
            binding.tvNetoMod.text = "$" + formato.format(precioTotalporArticulo)
            if (descuentoTotal!! > 0.0) {
                binding.tvSubdcto.visibility = View.VISIBLE
                binding.tvNetoDcto.visibility = View.VISIBLE
                montoNetoConDescuento = montoNetoConDescuento.valorReal()
                binding.tvNetoDcto.text = "$" + formato.format(montoNetoConDescuento)
            } else {
                binding.tvNetoDcto.text = "$0.00"
                binding.tvSubdcto.visibility = View.INVISIBLE
                binding.tvNetoDcto.visibility = View.INVISIBLE
            }
        } else {
            binding.tvNetoMod.text = "$0.00"
            binding.tvNetoDcto.text = "$0.00"
            binding.tvSubdcto.visibility = View.INVISIBLE
            binding.tvNetoDcto.visibility = View.INVISIBLE

            //---aqui tambien se coloca algo en caso de que sea 0 con descuento
        }
        cursor.close()
        if (negociacionIsActiva()) {
            if (precioTotalporArticulo >= montoMinimoMod!!) {
                binding.tvMontominMod.setTextColor(Color.rgb(22, 129, 67))
            } else {
                binding.tvMontominMod.setTextColor(Color.rgb(244, 67, 54))
            }
        }
    }

    private fun guardarDatosPedidoMod() {
        val keAndroid = conn.writableDatabase
        val cursor = keAndroid.rawQuery(
            "SELECT kmv_codart, kmv_nombre, kmv_cant, kmv_stot, kmv_artprec, kmv_dctolin FROM ke_carrito WHERE empresa = '$codEmpresa'",
            null
        )
        binding.menuModifi.isEnabled = false
        if (cursor.moveToFirst()) {
            pedidoCondicion()
            val ktiDocsolicitado = documento
            val ktiCondicion = formaPago
            val ktiTotneto = precioTotalporArticulo
            val ktiNdoc = codigoPedido
            val ktiTdoc = tipoDoc
            val ktiPrecio = 1.00
            val ktiNegesp = negociacionActivaMod
            val fechaTabla = Date(Calendar.getInstance().timeInMillis)
            val formatoFechaTabla = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val ktiFchdoc = formatoFechaTabla.format(fechaTabla)
            try {
                val actualizarCabecera = ContentValues()
                keAndroid.beginTransaction() //iniciamos la tranasaccion
                actualizarCabecera.put("kti_docsol", ktiDocsolicitado)
                actualizarCabecera.put("kti_condicion", ktiCondicion)
                actualizarCabecera.put("kti_totneto", ktiTotneto)
                actualizarCabecera.put("kti_fchdoc", ktiFchdoc)
                actualizarCabecera.put("kti_negesp", ktiNegesp)
                actualizarCabecera.put(
                    "kti_totnetodcto", valorReal(
                        montoNetoConDescuento
                    )
                )
                keAndroid.update(
                    "ke_opti",
                    actualizarCabecera,
                    "kti_ndoc='$codigoPedido' AND empresa = '$codEmpresa'",
                    null
                )

                //borramos las lineas actuales para incluir las nuevas
                keAndroid.execSQL("DELETE FROM ke_opmv WHERE kti_ndoc ='$codigoPedido' AND empresa = '$codEmpresa'")

                //insertamos las lineas
                for (i in listacarritoMod!!.indices) {
                    val cvLineas = ContentValues()
                    cvLineas.put("kmv_codart", listacarritoMod!![i].getCodigo())
                    cvLineas.put("kmv_nombre", listacarritoMod!![i].getNombre())
                    cvLineas.put("kti_tipprec", ktiPrecio)
                    cvLineas.put("kmv_cant", listacarritoMod!![i].getCantidad())
                    cvLineas.put("kti_tdoc", ktiTdoc)
                    cvLineas.put("kti_ndoc", ktiNdoc)
                    cvLineas.put("kmv_stot", listacarritoMod!![i].getPrecio())
                    cvLineas.put("kmv_artprec", listacarritoMod!![i].getPreciou())
                    cvLineas.put("kmv_dctolin", listacarritoMod!![i].getDctolin())
                    cvLineas.put("kmv_stotdcto", listacarritoMod!![i].getStotNeto())
                    cvLineas.put("empresa", codEmpresa)

                    //insertamos las lineas
                    keAndroid.insert("ke_opmv", null, cvLineas)
                }
                //limpiamos ke_carrito
                keAndroid.delete("ke_carrito", "empresa = '$codEmpresa'", null)
                keAndroid.setTransactionSuccessful()
            } catch (ex: Exception) {
                Toast.makeText(this@ModificarPedidoActivity, "Error en: $ex", Toast.LENGTH_SHORT)
                    .show()
            } finally {
                keAndroid.endTransaction()
            }
            Toast.makeText(
                this@ModificarPedidoActivity,
                "Pedido modificado exitosamente",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        } else {
            Toast.makeText(
                this@ModificarPedidoActivity,
                "Por favor, agrega artículos al pedido",
                Toast.LENGTH_SHORT
            ).show()
        }
        cursor.close()
    }

    private fun obtenerMontoMinimoTotal(): Double {
        val monto: Double = if (binding.RbFacturaMod.isChecked) {
            conn.getConfigNum("APP_MONTO_MINIMO_FAC", codEmpresa!!)
        } else if (binding.RbFacturaMod.isChecked) {
            conn.getConfigNum("APP_MONTO_MINIMO_NE", codEmpresa!!)
        } else {
            75.00
        }
        return monto
    }

    private fun cargarLineas() {
        carritoCompras()
        if (listacarritoMod != null) {
            // adapter.notifyDataSetChanged();
            carritoAdapter =
                CarritoAdapter(this@ModificarPedidoActivity, listacarritoMod, enlaceEmpresa)
            binding.ListPedidoMod.adapter = carritoAdapter
            carritoAdapter.notifyDataSetChanged()
        } else {
            Toast.makeText(
                this@ModificarPedidoActivity,
                "Por favor, agrega lineas al pedido",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /** */
    private fun carritoCompras() {
        listacarritoMod = ArrayList()
        conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null)
        val keAndroid = conn.writableDatabase
        val cursor = keAndroid.rawQuery(
            "SELECT kmv_codart, kmv_nombre, kmv_cant, kmv_stot, kmv_artprec, kmv_dctolin, kmv_stotdcto FROM ke_carrito WHERE empresa = '$codEmpresa'",
            null
        )
        while (cursor.moveToNext()) {
            val carrito = Carrito()
            carrito.setCodigo(cursor.getString(0))
            carrito.setNombre(cursor.getString(1))
            carrito.setCantidad(cursor.getInt(2))
            carrito.setPrecio(cursor.getDouble(3))
            carrito.setPreciou(cursor.getDouble(4))
            carrito.setDctolin(cursor.getDouble(5))
            carrito.setStotNeto(cursor.getDouble(6))
            listacarritoMod!!.add(carrito)
        }
        cursor.close()
        keAndroid.close()
    }

    private fun iraCatalogo() {
        binding.menuModifi.isEnabled = false
        Toast.makeText(this@ModificarPedidoActivity, "Cargando Datos", Toast.LENGTH_SHORT).show()
        val intent = Intent(this@ModificarPedidoActivity, CatalogoActivity::class.java)
        seleccion = 2
        intent.putExtra("Seleccion", seleccion)
        intent.putExtra("tipoDePrecioaMostrar", tipoDePrecioaMostrar)
        intent.putExtra("enpreventa", enpreventa)
        intent.putExtra("factura", binding.RbFacturaMod.isChecked)
        startActivity(intent)
    }

    private fun lineasDelPedido() {
        conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null)
        val keAndroid = conn.writableDatabase
        val cursor = keAndroid.rawQuery(
            "SELECT kmv_codart, kmv_nombre, kmv_cant, kmv_stot, kmv_artprec, kmv_dctolin, kmv_stotdcto FROM ke_opmv " +
                    "WHERE kti_ndoc='$codigoPedido' AND empresa = '$codEmpresa'",
            null
        )
        while (cursor.moveToNext()) {
            val kmvCodart = cursor.getString(0)
            val kmvNombre = cursor.getString(1)
            val kmvCant = cursor.getDouble(2)
            val kmvStot = cursor.getDouble(3)
            val kmvArtprec = cursor.getDouble(4)
            val kmvDctolin = cursor.getDouble(5)
            val kmvStotdcto = cursor.getDouble(6)
            val cvCarrito = ContentValues()
            cvCarrito.put("kmv_codart", kmvCodart)
            cvCarrito.put("kmv_nombre", kmvNombre)
            cvCarrito.put("kmv_cant", kmvCant)
            cvCarrito.put("kmv_stot", kmvStot)
            cvCarrito.put("kmv_artprec", kmvArtprec)
            cvCarrito.put("kmv_dctolin", kmvDctolin)
            cvCarrito.put("kmv_stotdcto", kmvStotdcto)
            cvCarrito.put("empresa", codEmpresa)
            keAndroid.insert("ke_carrito", null, cvCarrito)
        }
        cursor.close()
    }

    //aqui determinamos el valor de las condiciones para el pedido(documento/condicion)
    private fun pedidoCondicion() {
        if (binding.RbFacturaMod.isChecked) {
            documento = "1"
            if (binding.RbCreditoMod.isChecked) {
                formaPago = "2"
            } else if (binding.RbPrepagoMod.isChecked) {
                formaPago = "1"
            }
        } else if (binding.RbNotaEntregaMod.isChecked) {
            documento = "2"
            if (binding.RbCreditoMod.isChecked) {
                formaPago = "2"
            } else if (binding.RbPrepagoMod.isChecked) {
                formaPago = "1"
            }
        }
        //-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.
    }

    //este metodo evalua la cabecera del pedido y marca los radiobuttons segun la condicion que encuentre
    private fun cargarCondiciones() {
        conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null)
        val keAndroid = conn.writableDatabase
        val cursor = keAndroid.rawQuery(
            "SELECT kti_docsol, kti_condicion, kti_negesp FROM ke_opti " +
                    "WHERE kti_ndoc='$codigoPedido' AND empresa = '$codEmpresa'",
            null
        )
        while (cursor.moveToNext()) {
            documento = cursor.getString(0)
            formaPago = cursor.getString(1)
            negociacionEstado = cursor.getString(2)
        }
        cursor.close()
        if (negociacionEstado == "1") {
            binding.swNegoespecialMod.isChecked = true
        }
        if (documento == "1") {
            binding.RbFacturaMod.toggle()
        } else if (documento == "2") {
            binding.RbNotaEntregaMod.toggle()
        }
        if (formaPago == "1") {
            binding.RbPrepagoMod.toggle()
        } else if (formaPago == "2") {
            binding.RbCreditoMod.toggle()
        }
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
    }

    override fun onResume() {
        super.onResume()
        cargarLineas()
        sumaNeto()
        binding.menuModifi.isEnabled = true
        // adapter.notifyDataSetChanged();
        validarSiHayPreventa()
    }

    fun consultarDisponibilidad(
        codUsuario: String,
        codCliente: String,
        codArticulo: String
    ): Int {
        var resultado = 0
        val keAndroid = conn.readableDatabase
        val cuComp = keAndroid.rawQuery(
            "SELECT SUM(kli_cant) FROM ke_limitart " +
                    "WHERE kli_codven ='$codUsuario' AND kli_codcli='$codCliente' AND kli_codart='$codArticulo' AND status ='1' AND empresa = '$codEmpresa'",
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

    companion object {
        var codigoPedido: String? = null
        var n_cliente: String? = null
        var documento: String? = null
        var formaPago: String? = null
        var tipoDoc = "PED"
        var tipoDePrecioaMostrar: String? = null
        var codigoCliente: String? = null
        var negociacionActivaMod: String? = null
        var negociacionEstado: String? = null
        var seleccion = 0
        var indexposicion = 0
        var nroCorrelativo = 0
        var enteroPrecio = 0
        var preciocliente: Double? = null
        var montoMinimoMod: Double? = null
        var descuentoTotal: Double? = null
    }

    fun setColors() {
        binding.apply {
            RbFacturaMod.buttonTintList = RbFacturaMod.setColorRadioButon(Constantes.AGENCIA)
            RbNotaEntregaMod.buttonTintList =
                RbNotaEntregaMod.setColorRadioButon(Constantes.AGENCIA)
            RbCreditoMod.buttonTintList = RbCreditoMod.setColorRadioButon(Constantes.AGENCIA)
            RbPrepagoMod.buttonTintList = RbPrepagoMod.setColorRadioButon(Constantes.AGENCIA)

            tvClientepedido.setDrawableHeadAgencia(Constantes.AGENCIA)

            menuModifi.setBackgroundColor(menuModifi.colorAgencia(Constantes.AGENCIA))

            linearLayout4.setBackgroundResource(linearLayout4.changeColorMarco(Constantes.AGENCIA))

            menuModifi.itemTextColor = menuModifi.colorIconReclamo(Constantes.AGENCIA)
            menuModifi.itemIconTintList = menuModifi.colorIconReclamo(Constantes.AGENCIA)

        }
    }

    override fun getTheme(): Resources.Theme {
        val theme = super.getTheme()
        theme.applyStyle(setThemeAgencia(Constantes.AGENCIA), true)
        // you could also use a switch if you have many themes that could apply
        return theme
    }
}