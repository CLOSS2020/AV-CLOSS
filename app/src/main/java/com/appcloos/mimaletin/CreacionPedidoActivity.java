package com.appcloos.mimaletin;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class CreacionPedidoActivity extends AppCompatActivity {

    private int APP_ITEMS_FACTURAS;
    private int APP_ITEMS_NOTAS_ENTREGA;

    private long APP_DIAS_PEDIDO_COMPLEMENTO;

    private RadioButton Factura, NotaEntrega, Credito, Prepago;
    public static String documento, formaPago;
    ArrayAdapter adapter;
    ArrayAdapter<CharSequence> adapterSpinner;
    ListView listaLineas;
    ArrayList<String> listainfo = null, listapedido = null, listainfoClientes = null;
    ArrayList<Carrito> listacarrito;
    ArrayList<Cliente> listacliente;
    public static int seleccion, indexposicion, nroCorrelativo;
    Spinner spinner;
    AdminSQLiteOpenHelper conn;
    TextView tv_subtotal, tv_codigocliente, tv_mtomin;
    Double subtotal;
    Double NetoTotal = 0.00;
    TextView tv_neto;
    TextView tv_bloqueado;
    TextView tv_netocondescuento;
    TextView tv_subcondcto;

    TextView tvNombreCliente;
    BottomNavigationView menunav;
    ImageButton ibt_modificar;
    Double montoMinimoTotal = 0.00;
    public static Double precioTotalporArticulo, kti_precio, preciocliente, montoMinimo, montoNetoConDescuento, descuentoTotal, descuentoDeLaLinea;

    String codigoCliente, cod_usuario;
    public static int enteroPrecio, conteoDocs;
    public static String nroPedido, CorrelativoTexto, nombreEmpresa = "", enlaceEmpresa = "", tipoDoc = "PED", enpreventa,
            n_cliente, tipoDePrecioaMostrar, negociacionActiva, fechaVence, nuevoVencimiento, kti_negesp;
    //CheckBox cb_negoespecial;
    Switch sw_negoespecial;
    Switch sw_preventa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED); //mantener la orientacion vertical
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creacion_pedido);
        // getSupportActionBar().hide(); //metodo para esconder la actionbar

        SharedPreferences preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        cod_usuario = preferences.getString("cod_usuario", null);
        enpreventa = "0";
        menunav = findViewById(R.id.menu_creacion);
        menunav.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        codigoCliente = Objects.requireNonNull(getIntent().getExtras()).getString("cod_cliente");
        n_cliente = getIntent().getExtras().getString("nombre_cliente");

        conn = new AdminSQLiteOpenHelper(CreacionPedidoActivity.this, "ke_android", null, 12);
        SQLiteDatabase ke_android = conn.getWritableDatabase();

        APP_ITEMS_FACTURAS = (int) Math.round(conn.getConfigNum("APP_ITEMS_FACTURAS"));
        APP_ITEMS_NOTAS_ENTREGA = (int) Math.round(conn.getConfigNum("APP_ITEMS_NOTAS_ENTREGA"));
        APP_DIAS_PEDIDO_COMPLEMENTO = conn.getConfigNum("APP_DIAS_PEDIDO_COMPLEMENTO").longValue();
        int NOEMIFAC = conn.getCampoInt("cliempre", "noemifac", "codigo", codigoCliente);
        int NOEMINOTA = conn.getCampoInt("cliempre", "noeminota", "codigo", codigoCliente);

        cargarEnlace();
        Cursor cursor = ke_android.rawQuery("SELECT MAX(kco_numero) FROM ke_correla WHERE kco_vendedor ='" + cod_usuario + "'", null);

        if (cursor.moveToFirst()) {
            nroCorrelativo = cursor.getInt(0);
            nroCorrelativo = nroCorrelativo + 1;
            //CorrelativoTexto = String.valueOf(nroCorrelativo);
            CorrelativoTexto = "0000" + nroCorrelativo;

            generarNumeroPedido();
            Objects.requireNonNull(getSupportActionBar()).setTitle("Pedido: " + nroPedido);
        }
        cursor.close();
        RadioGroup grupoRadio = findViewById(R.id.radioGroup);
        Factura = findViewById(R.id.RbFactura);
        NotaEntrega = findViewById(R.id.RbNotaEntrega);
        Credito = findViewById(R.id.RbCredito);
        Prepago = findViewById(R.id.RbPrepago);
        tv_neto = findViewById(R.id.tv_neto);
        tv_codigocliente = findViewById(R.id.tv_codigocliente);
        listaLineas = findViewById(R.id.ListPedido);
        //spinner             = findViewById(R.id.sp_cliente);
        //ibt_modificar       = findViewById(R.id.ibt_modificar);
        //cb_negoespecial = findViewById(R.id.cb_negespecial);
        tv_mtomin = findViewById(R.id.tv_mtomin);
        tv_bloqueado = findViewById(R.id.tv_avisobloqueo);
        sw_negoespecial = findViewById(R.id.sw_negoespecial);
        tv_netocondescuento = findViewById(R.id.tv_netocondescuento);
        tv_subcondcto = findViewById(R.id.tv_subcondcto);
        sw_preventa = findViewById(R.id.sw_preventa);
        tvNombreCliente = findViewById(R.id.tv_nombre_cliente);

        montoMinimoTotal = obtenerMontoMinimoTotal();
        llamarFuncionesAsignacion();

        tvNombreCliente.setText(n_cliente);
        grupoRadio.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.RbFactura) {
                tv_bloqueado.setVisibility(View.VISIBLE);
                tv_bloqueado.setText("Las facturas solo tendrán un máximo de " + APP_ITEMS_FACTURAS + " líneas de artículos");
            } else if (checkedId == R.id.RbNotaEntrega) {
                tv_bloqueado.setVisibility(View.VISIBLE);
                tv_bloqueado.setText("Las N/E solo tendrán un máximo de " + APP_ITEMS_NOTAS_ENTREGA + " líneas de artículos");
            }
        });

        Prepago.setOnCheckedChangeListener((buttonView, isChecked) -> recalculoPrecio(isChecked));

        NotaEntrega.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Prepago.setChecked(false);
                Prepago.setVisibility(View.INVISIBLE);
                Prepago.setEnabled(false);
                Credito.setChecked(true);
            } else {
                Prepago.setVisibility(View.VISIBLE);
                Prepago.setEnabled(true);
            }
        });



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


        CargarLineas();
        CargarClientes();
        SumaNeto();
        recuprarSeleccion();

        //obtenemos el codigo de usuario de las preferencias guardadas cuando iniciamos sesión
        preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        cod_usuario = preferences.getString("cod_usuario", null);

        /*******************************************************************************************/

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


        /******************************************************************************************/
        //en este metodo onlistener, cuando se mantiene presionado un item, da la opcion de sacarlo del carrito
        //y luego refrescar la lista de lineas del pedido

        listaLineas.setOnItemLongClickListener((adapterView, view, position, l) -> {
            final String codigo = listacarrito.get(position).getCodigo();

            final AlertDialog.Builder ventana = new AlertDialog.Builder(new ContextThemeWrapper(CreacionPedidoActivity.this,R.style.AlertDialogCustom));
            ventana.setTitle("Mensaje del Sistema");
            ventana.setMessage("¿Desea eliminar este artículo?");

            ventana.setPositiveButton("Aceptar", (dialogInterface, i) -> {
                conn = new AdminSQLiteOpenHelper(getApplicationContext(), "ke_android", null, 8);
                SQLiteDatabase ke_android1 = conn.getWritableDatabase();
                ke_android1.execSQL("DELETE FROM ke_carrito WHERE kmv_codart ='" + codigo + "'");
                actualizaLista();

                Toast.makeText(CreacionPedidoActivity.this, "Artículo borrado", Toast.LENGTH_SHORT).show();


            });

            ventana.setNegativeButton("Cancelar", (dialogInterface, i) -> dialogInterface.dismiss());

            AlertDialog dialogo = ventana.create();
            dialogo.show();


            return false;
        });

        /*******************************************************************************************************************/

        listaLineas.setOnItemClickListener((adapterView, view, position, l) -> {
            //-- obtiene el codigo del articulo y el descuento en la línea
            final String codigo = listacarrito.get(position).getCodigo();
            final Double dctolin = listacarrito.get(position).getDctolin();

            final EditText cajatexto = new EditText(new ContextThemeWrapper(CreacionPedidoActivity.this,R.style.EditTextStyleCustom));
            cajatexto.setInputType(InputType.TYPE_CLASS_NUMBER);

            conn = new AdminSQLiteOpenHelper(getApplicationContext(), "ke_android", null, 1);
            final SQLiteDatabase ke_android12 = conn.getWritableDatabase();
            Cursor cursor_mul = ke_android12.rawQuery("SELECT vta_min, vta_minenx FROM articulo WHERE codigo ='" + codigo + "'", null);
            System.out.println("SELECT vta_min, vta_minenx FROM articulo WHERE codigo ='" + codigo + "'");

            cursor_mul.moveToFirst();
            double vta_Min = cursor_mul.getDouble(0);
            int vta_minenx = cursor_mul.getInt(1);

            cursor_mul.close();

            System.out.println("ventaMin: " + vta_Min);

            AlertDialog.Builder ventana = new AlertDialog.Builder(new ContextThemeWrapper(CreacionPedidoActivity.this,R.style.AlertDialogCustom));
            ventana.setTitle("Modificar Linea");
            ventana.setMessage("Porfavor, elige la cantidad");
            if (vta_minenx == 1) {

                LinearLayout layout_h = new LinearLayout(CreacionPedidoActivity.this);
                layout_h.setOrientation(LinearLayout.HORIZONTAL);
                final TextView mensajeCantidadMultiplo = new TextView(CreacionPedidoActivity.this);

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
            }
            ventana.setPositiveButton("Aceptar", (dialogInterface, i) -> {


                if (cajatexto.getText().toString().isEmpty()) {
                    Toast.makeText(CreacionPedidoActivity.this, "la cantidad no puede estar en blanco", Toast.LENGTH_SHORT).show();
                } else {
                    /* 16-11-2021 Corregido error de texto vacio que generaba crash en la app */
                    String cantidad_nueva = cajatexto.getText().toString();

                    int cantidad = Integer.parseInt(cantidad_nueva);

                    if (cantidad != 0) {
                        Cursor cursor1 = ke_android12.rawQuery("SELECT " + tipoDePrecioaMostrar + ", (existencia - comprometido), vta_min, vta_max, vta_minenx FROM articulo WHERE codigo ='" + codigo + "'", null);
                        cursor1.moveToNext();
                        //System.out.println("SELECT " + tipoDePrecioaMostrar + ", (existencia - comprometido), vta_min, vta_max, vta_minenx FROM articulo WHERE codigo ='" + codigo + "'");
                        double precio = cursor1.getDouble(0);
                        double existencia = cursor1.getDouble(1);
                        Double ventaMax = cursor1.getDouble(3);
                        double ventaMin = cursor1.getDouble(2);
                        int vta_minenx1 = cursor1.getInt(4);
                        cursor1.close();
                        int existencia_validar = (int) Math.round(existencia);

                        System.out.println(cantidad);
                        System.out.println(existencia_validar);

                        if (cantidad > existencia_validar) {
                            Toast.makeText(CreacionPedidoActivity.this, "La cantidad no puede ser superior a la existencia", Toast.LENGTH_SHORT).show();
                        } else {
                            if (ventaMin > 0) {
                                System.out.println("Multiplo: " + vta_minenx1);
                                if (vta_minenx1 == 1) {
                                    if (cantidad * ventaMin > existencia) {
                                        Toast.makeText(CreacionPedidoActivity.this, "Debe de elegir una cantidad dentro de la existencia", Toast.LENGTH_LONG).show();
                                    } else if (cantidad * ventaMin <= existencia) {
                                        int cantidad_new = (int) (cantidad * ventaMin);
                                        System.out.println("Nueva cantidad " + cantidad_new);
                                        double precioTotal = precio * cantidad_new;
                                        System.out.println("Precio total: " + precioTotal);
                                        precioTotal = Math.round(precioTotal * 100.00) / 100.00;

                                        ke_android12.beginTransaction();
                                        try {

                                            double precioNuevo = precioTotal;
                                            precioNuevo = Math.round(precioNuevo * 100.0) / 100.00;

                                            double mtoDctoNuevo = precioNuevo - (precioNuevo * (dctolin / 100));
                                            mtoDctoNuevo = Math.round(mtoDctoNuevo * 100.0) / 100.00;


                                            ke_android12.execSQL("UPDATE ke_carrito SET kmv_cant=" + cantidad_new + ", kmv_stot =" + precioTotal + ", kmv_stotdcto =" + mtoDctoNuevo + " WHERE kmv_codart ='" + codigo + "'");


                                            ke_android12.setTransactionSuccessful();
                                            ke_android12.endTransaction();
                                            Toast.makeText(CreacionPedidoActivity.this, "Artículo modificado", Toast.LENGTH_SHORT).show();
                                        } catch (Exception ex) {
                                            System.out.println("--Error--");
                                            ex.printStackTrace();
                                            ke_android12.endTransaction();

                                        }
                                    }

                                } else {
                                    if (cantidad < ventaMin) {
                                        Toast.makeText(CreacionPedidoActivity.this, "Debe cumplir con la cantidad mínima para la venta", Toast.LENGTH_LONG).show();
                                    } else if (cantidad >= ventaMin) {

                                        double precioTotal = precio * (double) cantidad;
                                        precioTotal = Math.round(precioTotal * 100.00) / 100.00;

                                        ke_android12.beginTransaction();
                                        try {

                                            double precioNuevo = precio * (double) cantidad;
                                            precioNuevo = Math.round(precioNuevo * 100.0) / 100.00;

                                            double mtoDctoNuevo = precioNuevo - (precioNuevo * (dctolin / 100));
                                            mtoDctoNuevo = Math.round(mtoDctoNuevo * 100.0) / 100.00;


                                            ke_android12.execSQL("UPDATE ke_carrito SET kmv_cant=" + cantidad + ", kmv_stot =" + precioTotal + ", kmv_stotdcto =" + mtoDctoNuevo + " WHERE kmv_codart ='" + codigo + "'");


                                            ke_android12.setTransactionSuccessful();
                                            ke_android12.endTransaction();
                                            Toast.makeText(CreacionPedidoActivity.this, "Artículo modificado", Toast.LENGTH_SHORT).show();
                                            //finish();
                                        } catch (Exception ex) {
                                            System.out.println("--Error--");
                                            ex.printStackTrace();
                                            ke_android12.endTransaction();

                                        }
                                    }
                                }


                            } else {
                                double precioTotal = precio * (double) cantidad;
                                precioTotal = Math.round(precioTotal * 100.00) / 100.00;

                                ke_android12.beginTransaction();
                                try {

                                    double precioNuevo = precio * (double) cantidad;
                                    precioNuevo = Math.round(precioNuevo * 100.0) / 100.00;

                                    double mtoDctoNuevo = precioNuevo - (precioNuevo * (dctolin / 100));
                                    mtoDctoNuevo = Math.round(mtoDctoNuevo * 100.0) / 100.00;


                                    ke_android12.execSQL("UPDATE ke_carrito SET kmv_cant=" + cantidad + ", kmv_stot =" + precioTotal + ", kmv_stotdcto =" + mtoDctoNuevo + " WHERE kmv_codart ='" + codigo + "'");


                                    ke_android12.setTransactionSuccessful();
                                    ke_android12.endTransaction();
                                    //finish();
                                    Toast.makeText(CreacionPedidoActivity.this, "Artículo modificado", Toast.LENGTH_SHORT).show();
                                } catch (Exception ex) {
                                    System.out.println("--Error--");
                                    ex.printStackTrace();
                                    ke_android12.endTransaction();

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

                            actualizaLista();
                            Toast.makeText(CreacionPedidoActivity.this, "Articulo añadido", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(CreacionPedidoActivity.this, "La existencia no puede ser 0", Toast.LENGTH_SHORT).show();
                    }

                }
            });

            ventana.setNegativeButton("Cancelar", (dialogInterface, i) -> dialogInterface.dismiss());
            AlertDialog dialogo = ventana.create();
            dialogo.show();


        });


        sw_negoespecial.setOnCheckedChangeListener((compoundButton, b) -> {

            if (compoundButton.isChecked()) {

                tv_mtomin.setEnabled(true);
                tv_mtomin.setVisibility(View.VISIBLE);
                tv_mtomin.setText("Monto Mín: $" + montoMinimo);

            } else {
                tv_mtomin.setEnabled(false);
                tv_mtomin.setVisibility(View.INVISIBLE);

            }
/*
            recalculoPrecio(b);*/
        });


        sw_preventa.setOnCheckedChangeListener((compoundButton, b) -> {
            if (compoundButton.isChecked()) {
                enpreventa = "1";
                vaciarCarrito();
                CargarLineas();
                SumaNeto();


            } else if (!compoundButton.isChecked()) {
                enpreventa = "0";
                vaciarCarrito();
                CargarLineas();
                SumaNeto();
            }
        });

        funcionCaducidad();

        if (NOEMIFAC == 1) {
            Factura.setVisibility(View.INVISIBLE);
        } else {
            Factura.setVisibility(View.VISIBLE);
        }

        if (NOEMINOTA == 1) {
            NotaEntrega.setVisibility(View.INVISIBLE);
        } else {
            NotaEntrega.setVisibility(View.VISIBLE);
        }

        if (NOEMIFAC == 1 && NOEMINOTA == 1) {
            Toast.makeText(this, "Cliente suspendido", Toast.LENGTH_SHORT).show();
            finish();
        }

        Factura.setOnClickListener(v -> {
            analizarArticulos("vta_solone", listacarrito);
            montoMinimoTotal = obtenerMontoMinimoTotal();
        });

        NotaEntrega.setOnClickListener(v -> {
            analizarArticulos("vta_solofac", listacarrito);
            montoMinimoTotal = obtenerMontoMinimoTotal();
        });

        /*Factura.setOnCheckedChangeListener((buttonView, isChecked) -> {
            analizarArticulos(isChecked, listacarrito);
        });

        NotaEntrega.setOnCheckedChangeListener((buttonView, isChecked) -> {
            analizarArticulos(!isChecked, listacarrito);
        });*/
    }

    private void analizarArticulos(String campo, ArrayList<Carrito> listacarrito) {

        int num = 0;
        //int numNE = 0;
        for (int i = 0; i < listacarrito.size(); i++) {
            num = conn.getCampoInt("articulo", campo, "codigo", listacarrito.get(i).codigo);
            //numNE += conn.getCampoInt("articulo", "vta_solone", "codigo", listacarrito.get(i).codigo);
            if (num > 0) {
                break;
            }
        }

        if (num > 0 && NotaEntrega.isChecked()) {
            Toast.makeText(this, "Posee artículos que solo están disponibles para Facturas", Toast.LENGTH_SHORT).show();
            Factura.setChecked(true);
        } else if (num > 0 && Factura.isChecked()) {
            Toast.makeText(this, "Posee artículos que solo están disponibles para Notas de Entrega", Toast.LENGTH_SHORT).show();
            NotaEntrega.setChecked(true);
        }

    }

    //554
    private void llamarFuncionesAsignacion() {
        asignarTipodePrecio();
        actualizarPrecios();
        actualizaLista();
        obtenerCondicionesEspeciales();
    }

    private void recalculoPrecio(boolean b) {
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        Cursor cursor = ke_android.rawQuery("SELECT precio FROM cliempre WHERE codigo='" + codigoCliente + "'", null);

        if (b) {
            tipoDePrecioaMostrar = "precio1";
            preciocliente = 1.0;

            //2023-06-05 se comento debido a que la opcion de prepago se borrara y ahora se llamara pago BCV y se asignara precio1 (Posiblemente precio3 se elimine)
            /*tipoDePrecioaMostrar = "precio3";
            preciocliente = 3.0;*/

        } else {

            cursor.moveToFirst();
            preciocliente = cursor.getDouble(0);
            enteroPrecio = (int) Math.round(preciocliente);


            tipoDePrecioaMostrar = "precio" + enteroPrecio;
        }
        cursor.close();

        System.out.println("precio que esta cogiendo (upa): " + tipoDePrecioaMostrar);
        // tipoDePrecioaMostrar = (b)?"precio3":"precio"+enteroPrecio;

        ke_android = conn.getWritableDatabase();
        Cursor cursorMain = ke_android.rawQuery("SELECT * FROM ke_carrito", null);
        while (cursorMain.moveToNext()) {
            String codigo = cursorMain.getString(0);
            Double cantidad = cursorMain.getDouble(2);
            System.out.println("SELECT " + tipoDePrecioaMostrar + " FROM articulo WHERE codigo = '" + codigo + "'");
            cursor = ke_android.rawQuery("SELECT " + tipoDePrecioaMostrar + " FROM articulo WHERE codigo = '" + codigo + "'", null);
            cursor.moveToFirst();
            Double precio = Math.round(cursor.getDouble(0) * 100.00) / 100.00;
            double precioTotal = (precio * cantidad);
            double precioTotalRedondo = Math.round(precioTotal * 100.00) / 100.00;
            cursor.close();


            ke_android.execSQL("UPDATE ke_carrito SET kmv_artprec=" + precio + ", kmv_stot= " + precioTotalRedondo + ", kmv_stotdcto=" + precioTotalRedondo + " WHERE kmv_codart ='" + codigo + "'");
            CargarLineas();
            SumaNeto();

        }
        cursorMain.close();

    }

    /// validacion de pedidos complementarios
    private Boolean validarSiHayPedidosActivos() {

        LocalDateTime hoy = LocalDateTime.now();
        LocalDateTime ayer = hoy.minusHours(APP_DIAS_PEDIDO_COMPLEMENTO);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String fechaAnterior = ayer.format(formatter);


        boolean valorCentinela;
        SQLiteDatabase ke_android = conn.getWritableDatabase();

        Cursor cursorPeds = ke_android.rawQuery("SELECT kti_ndoc FROM ke_opti WHERE kti_codcli = '" + codigoCliente + "'  AND kti_status <> '3' AND kti_fchdoc >='" + fechaAnterior + "'", null);
        if (cursorPeds.moveToFirst()) {
            valorCentinela = true;
            System.out.println("Existen pedidos de este cliente");
        } else {
            valorCentinela = false;
            System.out.println("no se ha regresado ningun registro");
        }
        cursorPeds.close();
        return valorCentinela;
    }


    private Double obtenerMontoMinimoTotal() {
        Double monto;
        if (Factura.isChecked()) {
            monto = conn.getConfigNum("APP_MONTO_MINIMO_FAC");
        } else if (NotaEntrega.isChecked()) {
            monto = conn.getConfigNum("APP_MONTO_MINIMO_NE");
        } else {
            monto = 75.00;
        }

        return monto;
    }

    private void vaciarCarrito() {
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        try {
            ke_android.beginTransaction();
            ke_android.execSQL("DELETE FROM ke_carrito WHERE 1");
            ke_android.setTransactionSuccessful();

        } catch (Exception e) {
            System.out.println("--Error--");
            e.printStackTrace();
        } finally {
            ke_android.endTransaction();
        }

    }

    private void cargarEnlace() {
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        String[] columnas = new String[]{
                "kee_nombre," +
                        "kee_url"};
        Cursor cursor = ke_android.query("ke_enlace", columnas, "1", null, null, null, null);

        while (cursor.moveToNext()) {
            nombreEmpresa = cursor.getString(0);
            enlaceEmpresa = cursor.getString(1);
        }

        cursor.close();

    }

    private final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

            switch (menuItem.getItemId()) {
                case R.id.ic_agregarart:

                    if (codigoCliente == null || (!Factura.isChecked() && !NotaEntrega.isChecked()) || (!Prepago.isChecked() && !Credito.isChecked())) {
                        Toast.makeText(CreacionPedidoActivity.this, "Debes Seleccionar un cliente, factura o nota de entrega y credito o prepago", Toast.LENGTH_SHORT).show();

                    } else {
                        if ((listacarrito.size() >= APP_ITEMS_FACTURAS && Factura.isChecked())) {
                            if (Factura.isChecked()) {
                                Toast.makeText(CreacionPedidoActivity.this, "Para facturas debe tener un maximo de " + APP_ITEMS_FACTURAS + " lineas de articulos", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(CreacionPedidoActivity.this, "Para notas de entrega debe tener un maximo de " + APP_ITEMS_NOTAS_ENTREGA + " lineas de articulos", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            //Asignacion de precio para saber si es negociacion especial
                            //asignarTipodePrecio();
                            iraCatalogo();
                        }


                    }
                    return true;
                case R.id.ic_procesarpedido:
                    boolean centinel = validarSiHayPedidosActivos();
                    if (!negociacionIsActiva() && montoNetoConDescuento < montoMinimoTotal && !centinel) {
                        Toast.makeText(CreacionPedidoActivity.this, "El monto minimo es de $" + montoMinimoTotal, Toast.LENGTH_LONG).show();
                    } else if (montoNetoConDescuento >= montoMinimoTotal || centinel) {
                        if ((Factura.isChecked() && listacarrito.size() > APP_ITEMS_FACTURAS) || (NotaEntrega.isChecked() && listacarrito.size() > APP_ITEMS_NOTAS_ENTREGA)) {
                            if (Factura.isChecked()) {
                                Toast.makeText(CreacionPedidoActivity.this, "Para facturas debe tener un maximo de " + APP_ITEMS_FACTURAS + " lineas de articulos", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(CreacionPedidoActivity.this, "Para notas de entrega debe tener un maximo de " + APP_ITEMS_NOTAS_ENTREGA + " lineas de articulos", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            ProcesarPedido();
                        }
                    }

                    return true;
            }

            return false;
        }
    };

    public boolean onOptionsItemSelected(MenuItem item) {
        int itemid = item.getItemId();

        if (itemid == android.R.id.home) {
            ValidarSalida();
        }
        //return super.onOptionsItemSelected(item);
        return true;
    }

    @Override
    public void onBackPressed() {
        ValidarSalida();
    }

    private void ValidarSalida() {

        AlertDialog dialog = new AlertDialog.Builder(new ContextThemeWrapper(CreacionPedidoActivity.this,R.style.AlertDialogCustom))
                .setTitle("Salir")
                .setMessage("¿Está seguro de desear salir?")
                .setCancelable(true)
                .setPositiveButton("Si", null)
                .setNegativeButton("No", null)
                .show();

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
            conn.DeleteAll("ke_carrito");
            dialog.dismiss();
            finish();
        });
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(v -> dialog.dismiss());

    }


    private void cargarPendientes(String URL) {

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(URL, response -> {
            if (response != null) {
                System.out.println("llegando info");

                JSONObject jsonObject;

                try {
                    jsonObject = response.getJSONObject(0);
                    conteoDocs = jsonObject.getInt("conteo");
                    fechaVence = jsonObject.getString("ultimafecha");
                    kti_negesp = jsonObject.getString("kti_negesp");

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, error -> {
            System.out.println("--Error--");
            error.printStackTrace();
        }) {
            @Override
            protected Map<String, String> getParams() {  //finalmente, estos son los parametros que le enviaremos al webservice, partiendo de las variables
                //donde estan guardados el usuario y password.
                //parametros.put("version_usuario", versionApp);

                return new HashMap<>();
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonArrayRequest); //esto es el request que se envia al url a traves de la conexion volley, (el stringrequest esta armado arriba)
    }

    private void funcionCaducidad() {
        //obtengo la fecha del sistema al ejecutar la función:
        LocalDateTime hoy = LocalDateTime.now(); //el dia en que se hizo el grabado
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String fecha_hoy = hoy.format(formatter);

        SQLiteDatabase ke_android = conn.getWritableDatabase();


        try {
            ke_android.beginTransaction();
            ke_android.execSQL("DELETE FROM ke_limitart WHERE kli_fechavence <='" + fecha_hoy + "'");
            ke_android.setTransactionSuccessful();

        } catch (Exception e) {
            System.out.println("--Error--");
            e.printStackTrace();
        } finally {
            ke_android.endTransaction();
        }


    }


    private void SumaNeto() {
        SQLiteDatabase ke_android = conn.getWritableDatabase();

        Cursor cursor = ke_android.rawQuery("SELECT SUM(kmv_stot), SUM(kmv_dctolin), SUM(kmv_stotdcto) FROM ke_carrito", null);

        if (cursor.moveToNext()) {
            precioTotalporArticulo = cursor.getDouble(0);
            precioTotalporArticulo = Math.round(precioTotalporArticulo * 100.00) / 100.00;
            System.out.println(precioTotalporArticulo);

            descuentoTotal = cursor.getDouble(1);

            montoNetoConDescuento = cursor.getDouble(2);
            tv_neto.setText("$" + precioTotalporArticulo.toString());

            if (descuentoTotal > 0.0) {
                tv_subcondcto.setVisibility(View.VISIBLE);
                tv_netocondescuento.setVisibility(View.VISIBLE);
                montoNetoConDescuento = Math.round(montoNetoConDescuento * 100.00) / 100.00;
                tv_netocondescuento.setText("$" + montoNetoConDescuento);
            } else {

                tv_netocondescuento.setText("$0.00");
                tv_subcondcto.setVisibility(View.INVISIBLE);
                tv_netocondescuento.setVisibility(View.INVISIBLE);
            }

        } else {
            tv_neto.setText("$0.00");
            tv_netocondescuento.setText("$0.00");
            tv_subcondcto.setVisibility(View.INVISIBLE);
            tv_netocondescuento.setVisibility(View.INVISIBLE);

            //---aqui tambien se coloca algo en caso de que sea 0 con descuento
        }
        cursor.close();

        if (negociacionIsActiva()) {


            if (precioTotalporArticulo >= montoMinimo) {
                tv_mtomin.setTextColor(Color.rgb(22, 129, 67));

            } else {
                tv_mtomin.setTextColor(Color.rgb(244, 67, 54));
            }


        }


    }


    public void PedidoCondicion() {
        if (Factura.isChecked()) {
            documento = "1";
            if (Credito.isChecked()) {
                formaPago = "2";
                //asignarTipodePrecio();
            } else if (Prepago.isChecked()) {
                formaPago = "1";
                //tipoDePrecioaMostrar = "precio3";
            }


        } else if (NotaEntrega.isChecked()) {
            documento = "2";
            if (Credito.isChecked()) {
                formaPago = "2";
                //asignarTipodePrecio();
            } else if (Prepago.isChecked()) {
                formaPago = "1";
                //tipoDePrecioaMostrar = "precio3";
            }
        }
        //-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.

    }

    public void CargarClientes() {
        conn = new AdminSQLiteOpenHelper(getApplicationContext(), "ke_android", null, 1);
        SQLiteDatabase ke_android = conn.getReadableDatabase();
        Cliente cliente;
        listacliente = new ArrayList<>();
        Cursor cursor = ke_android.rawQuery("SELECT codigo, nombre FROM cliempre WHERE vendedor ='" + cod_usuario.trim() + "' ORDER BY nombre ASC", null);

        while (cursor.moveToNext()) {
            cliente = new Cliente();
            cliente.setCodigo(cursor.getString(0));
            cliente.setNombre(cursor.getString(1));
            listacliente.add(cliente);
        }
        cursor.close();
        ke_android.close();
        obtenerlistaCliente();
        //ArrayAdapter<CharSequence> adapterSpinner = new ArrayAdapter(getBaseContext(), R.layout.spinner_items , listainfoClientes);
        //spinner.setAdapter(adapterSpinner);
        //adapterSpinner.notifyDataSetChanged();


    }

    private void obtenerlistaCliente() {
        listainfoClientes = new ArrayList<>();
        listainfoClientes.add("Seleccione un Cliente...");

        for (int i = 0; i < listacliente.size(); i++) {
            listainfoClientes.add(listacliente.get(i).getCodigo() + " - " + listacliente.get(i).getNombre());
        }

    }


    public void CargarLineas() {
        CarritoCompras();

        if (!listacarrito.isEmpty()) {

            // adapter.notifyDataSetChanged();
            CarritoAdapter carritoAdapter = new CarritoAdapter(CreacionPedidoActivity.this, listacarrito);
            listaLineas.setAdapter(carritoAdapter);
            carritoAdapter.notifyDataSetChanged();

        } else {
            CarritoAdapter carritoAdapter = new CarritoAdapter(CreacionPedidoActivity.this, new ArrayList<>());
            listaLineas.setAdapter(carritoAdapter);
            //Toast.makeText(CreacionPedidoActivity.this, "Por favor, agrega lineas al pedido", Toast.LENGTH_SHORT).show();
        }
    }


    private void iraCatalogo() {
        menunav.setEnabled(false);
        Toast.makeText(CreacionPedidoActivity.this, "Cargando Datos", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getApplicationContext(), CatalogoActivity.class);
        seleccion = 2;

        intent.putExtra("Seleccion", seleccion);
        intent.putExtra("precioTotalporArticulo", precioTotalporArticulo);
        intent.putExtra("tipoDePrecioaMostrar", tipoDePrecioaMostrar);
        intent.putExtra("codigoCliente", codigoCliente);
        intent.putExtra("nroPedido", nroPedido);
        intent.putExtra("enpreventa", enpreventa);
        intent.putExtra("factura", Factura.isChecked());
        System.out.println("valor de variable preventa " + enpreventa);
        startActivity(intent);

    }


    private void CarritoCompras() {
        listacarrito = new ArrayList<>();

        conn = new AdminSQLiteOpenHelper(getApplicationContext(), "ke_android", null, 1);
        final SQLiteDatabase ke_android = conn.getWritableDatabase();

        Cursor cursor = ke_android.rawQuery("SELECT kmv_codart, kmv_nombre, kmv_cant, kmv_stot, kmv_artprec, kmv_dctolin, kmv_stotdcto FROM ke_carrito WHERE 1", null);

        while (cursor.moveToNext()) {
            Carrito carrito = new Carrito();
            carrito.setCodigo(cursor.getString(0));
            carrito.setNombre(cursor.getString(1));
            carrito.setCantidad(cursor.getInt(2));
            carrito.setPrecio(cursor.getDouble(3));
            carrito.setPreciou(cursor.getDouble(4));
            System.out.println("por aqui" + cursor.getDouble(4));
            carrito.setDctolin(cursor.getDouble(5));
            carrito.setStotNeto(cursor.getDouble(6));
            listacarrito.add(carrito);

        }
        cursor.close();
        ke_android.close();

    }


    public void obtenerCondicionesEspeciales() {
        //CON ESTA FUNCION IDENTIFICO SI EL CLIENTE POSEE O NO LA POSIBLIDAD DE LLEVAR A CABO UNA NEGOCIACIÓN ESPECIAL
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        Cursor cursor = ke_android.rawQuery("SELECT kne_activa, kne_mtomin FROM cliempre WHERE codigo ='" + codigoCliente + "' and direccion IS NOT NULL and telefonos IS NOT NULL", null);

        while (cursor.moveToNext()) {
            negociacionActiva = cursor.getString(0);
            montoMinimo = cursor.getDouble(1); //IMPORTANTE: EL CLIENTE CUENTA CON UN MONTO MINIMO PARA LA NEG. ESPECIAL
        }
        cursor.close();

        if (negociacionActiva.equals("0")) {
            sw_negoespecial.setEnabled(false);
            sw_negoespecial.setVisibility(View.INVISIBLE);

            tv_mtomin.setEnabled(false);
            tv_mtomin.setVisibility(View.INVISIBLE);

        } else if (negociacionActiva.equals("1")) {
            sw_negoespecial.setEnabled(true);
            sw_negoespecial.setVisibility(View.VISIBLE);


        }

    }


    public boolean negociacionIsActiva() {
        return sw_negoespecial.isChecked();
    }


    public void ProcesarPedido() {

        if (codigoCliente == null) {
            Toast.makeText(CreacionPedidoActivity.this, "Debes Seleccionar un Cliente", Toast.LENGTH_SHORT).show();
            menunav.setEnabled(true);
        } else {


            if (!Factura.isChecked() && !NotaEntrega.isChecked() || !Credito.isChecked() && !Prepago.isChecked()) {
                Toast.makeText(CreacionPedidoActivity.this, "Por favor, seleccione las condiciones del pedido", Toast.LENGTH_SHORT).show();
                menunav.setEnabled(true);
            } else {


                if (negociacionIsActiva()) {
                    if (montoNetoConDescuento >= montoMinimo) {
                        guardarDatosDelPedido();
                    } else {
                        Toast.makeText(CreacionPedidoActivity.this, "En negociación especial, el pedido debe cumplir con el monto mínimo asignado.", Toast.LENGTH_LONG).show();
                    }


                } else if (!negociacionIsActiva()) {
                    negociacionActiva = "0";
                    guardarDatosDelPedido();
                }
            }
        }
    }


    public void generarNumeroPedido() {
        Date fechaHoy = new Date(Calendar.getInstance().getTimeInMillis());
        SimpleDateFormat formatofecha = new SimpleDateFormat("yyMM", Locale.getDefault());
        String fecha = formatofecha.format(fechaHoy);

        CorrelativoTexto = right(CorrelativoTexto, 4);

        nroPedido = cod_usuario.trim() + "-" + tipoDoc + "-" + fecha + CorrelativoTexto;

    }

    public void actualizaLista() {

        CargarLineas();
        SumaNeto();

    }


    public void guardarDatosDelPedido() {
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        //Cursor cursor1 = ke_android.rawQuery("SELECT kmv_codart, kmv_nombre, kmv_cant, kmv_stot, kmv_artprec, kmv_stotdcto FROM ke_carrito WHERE 1", null);

        Cursor cursor2 = ke_android.rawQuery("SELECT kmv_codart, kmv_nombre, kmv_cant, kmv_stot, kmv_artprec, kmv_stotdcto FROM ke_carrito WHERE 1", null);

        menunav.setEnabled(false);
        if (cursor2.moveToFirst()) {

            PedidoCondicion();
            SumaNeto();
            String kti_codcli = codigoCliente; //obtenemos el codigo del cliente
            //System.out.println(kti_codcli);
            String kti_docsolicitado = documento;
            //System.out.println(documento);
            String kti_condicion = formaPago;
            //System.out.println(formaPago);
            String kti_codven = cod_usuario.trim();
            String kti_tdoc = tipoDoc;
            Double kti_totneto = precioTotalporArticulo;
            //System.out.println(precioTotalporArticulo);
            kti_precio = preciocliente;
            String kti_ndoc = nroPedido;
            String tmp_nombrecli = n_cliente;
            String kti_negesp = negociacionActiva;

            Date fechaTabla = new Date(Calendar.getInstance().getTimeInMillis());//
            SimpleDateFormat formatoFechaTabla = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

            String kti_fchdoc = formatoFechaTabla.format(fechaTabla);


            try {
                //cargamos un content values para la cabecera
                ContentValues insertarCabecera = new ContentValues();
                ke_android.beginTransaction(); //iniciamos la tranasaccion

                //cargamos la informacion en el contentvalues
                insertarCabecera.put("kti_codcli", kti_codcli);
                insertarCabecera.put("kti_codven", kti_codven);
                insertarCabecera.put("kti_docsol", kti_docsolicitado);
                insertarCabecera.put("kti_condicion", kti_condicion);
                insertarCabecera.put("kti_tdoc", kti_tdoc);
                insertarCabecera.put("kti_ndoc", kti_ndoc);
                insertarCabecera.put("kti_tipprec", kti_precio);
                insertarCabecera.put("kti_nombrecli", tmp_nombrecli);
                insertarCabecera.put("kti_totneto", kti_totneto);
                insertarCabecera.put("kti_fchdoc", kti_fchdoc);
                insertarCabecera.put("kti_status", "0");
                insertarCabecera.put("fechamodifi", kti_fchdoc);
                insertarCabecera.put("kti_negesp", kti_negesp);
                insertarCabecera.put("kti_totnetodcto", montoNetoConDescuento);
                //insertamos la cabecera del pedido
                ke_android.insert("ke_opti", null, insertarCabecera);

                for (int i = 0; i < listacarrito.size(); i++) {

                    ContentValues insertarLineas = new ContentValues();
                    insertarLineas.put("kmv_codart", listacarrito.get(i).getCodigo());
                    insertarLineas.put("kmv_nombre", listacarrito.get(i).getNombre());
                    insertarLineas.put("kti_tipprec", kti_precio);
                    insertarLineas.put("kmv_cant", listacarrito.get(i).getCantidad());
                    insertarLineas.put("kti_tdoc", kti_tdoc);
                    insertarLineas.put("kti_ndoc", kti_ndoc);
                    insertarLineas.put("kmv_stot", listacarrito.get(i).getPrecio());
                    insertarLineas.put("kmv_artprec", listacarrito.get(i).getPreciou());
                    insertarLineas.put("kmv_dctolin", listacarrito.get(i).getDctolin());
                    insertarLineas.put("kmv_stotdcto", listacarrito.get(i).getStotNeto());
                    //insertamos las lineas
                    ke_android.insert("ke_opmv", null, insertarLineas);


                    ke_android.execSQL("UPDATE ke_limitart " +
                            "SET status = '1'" +
                            "WHERE kli_codart ='" + listacarrito.get(i).getCodigo() + "'" +
                            "AND kli_codven ='" + cod_usuario + "'" +
                            "AND kli_codcli ='" + codigoCliente + "'" +
                            "AND kli_track ='" + nroPedido + "'");

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
                //limpiamos ke_carrito
                ke_android.delete("ke_carrito", "1", null);

                //aumentamos el correlativo del pedido
                ContentValues aumentarCorrelatiov = new ContentValues();
                aumentarCorrelatiov.put("kco_numero", nroCorrelativo);
                aumentarCorrelatiov.put("kco_vendedor", cod_usuario);

                //insertamos el correlativo
                ke_android.insert("ke_correla", null, aumentarCorrelatiov);

                ke_android.setTransactionSuccessful();
            } catch (Exception ex) {
                Toast.makeText(CreacionPedidoActivity.this, "Error en: " + ex, Toast.LENGTH_SHORT).show();
            } finally {
                ke_android.endTransaction();
            }

            Toast.makeText(CreacionPedidoActivity.this, "Pedido creado exitosamente", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), PedidosActivity.class);
            startActivity(intent);


        } else {
            Toast.makeText(CreacionPedidoActivity.this, "Por favor, agrega artículos al pedido", Toast.LENGTH_SHORT).show();
        }
        cursor2.close();


    }


    public static String right(String valor, int longitud) {
        //una función "right" utilizando la clase substring
        return valor.substring(valor.length() - longitud);
    }


    public void recuprarSeleccion() {
        SharedPreferences sharpref = getSharedPreferences("sharpref", MODE_PRIVATE);
        indexposicion = sharpref.getInt("Dato", 0);
        System.out.println(indexposicion);
        //Mueve a esa posición el Spinner
        //spinner.setSelection(indexposicion);
    }


    public void asignarTipodePrecio() {
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        Cursor cursor = ke_android.rawQuery("SELECT precio FROM cliempre WHERE codigo='" + codigoCliente + "'", null);

        while (cursor.moveToNext()) {
            preciocliente = cursor.getDouble(0);
            enteroPrecio = (int) Math.round(preciocliente);
        }
        cursor.close();
/*

        System.out.println("bbbbbbbbbbbbbbbbbbbb" + negociacionIsActiva());
        //Seleccion de tipo de precio para las negociaciones especiales
        if (negociacionIsActiva()) {
            enteroPrecio = 2;
        }else{
            enteroPrecio = 1;
        }*/

        switch (enteroPrecio) {

            case 1:
                tipoDePrecioaMostrar = "precio1";
                break;

            case 2:
                tipoDePrecioaMostrar = "precio2";
                break;

            case 3:
                tipoDePrecioaMostrar = "precio3";
                break;

            case 4:
                tipoDePrecioaMostrar = "precio4";
                break;

            case 5:
                tipoDePrecioaMostrar = "precio5";
                break;

            case 6:
                tipoDePrecioaMostrar = "precio6";
                break;

            case 7:
                tipoDePrecioaMostrar = "precio7";
                break;

            default:
                tipoDePrecioaMostrar = "precio1";
        }
        ke_android.close();
    }


    public void actualizarPrecios() {
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        Cursor cursorcarrito = ke_android.rawQuery("SELECT kmv_codart, kmv_cant FROM ke_carrito WHERE 1", null);
        while (cursorcarrito.moveToNext()) {
            String codigoArticuloCarrito = cursorcarrito.getString(0);
            double CantidadCarrito = cursorcarrito.getDouble(1);
            cursorcarrito.close();

            Cursor cursorprecio = ke_android.rawQuery("SELECT " + tipoDePrecioaMostrar + " FROM articulo WHERE codigo ='" + codigoArticuloCarrito + "'", null);
            while (cursorprecio.moveToNext()) {
                double PrecioNuevo = cursorprecio.getDouble(0);
                PrecioNuevo = Math.round(PrecioNuevo * 100.00) / 100.0;
                double SubTotalNuevo = PrecioNuevo * CantidadCarrito;
                SubTotalNuevo = Math.round(SubTotalNuevo * 100.00) / 100.00;
                ke_android.execSQL("UPDATE ke_carrito SET kmv_artprec =" + PrecioNuevo + ", kmv_stot =" + SubTotalNuevo + " WHERE kmv_codart='" + codigoArticuloCarrito + "'");
            }
            cursorprecio.close();

        }


        ke_android.close();
    }


    @Override
    protected void onResume() {
        super.onResume();
        cargarEnlace();
        CargarLineas();
        // CargarClientes();
        SumaNeto();
        recuprarSeleccion();
        menunav.setEnabled(true);
        // adapter.notifyDataSetChanged();


    }

    public int consultarDisponibilidad(String cod_usuario, String cod_cliente, String cod_articulo) {
        int resultado = 0;
        System.out.println(cod_usuario);
        System.out.println(cod_cliente);
        System.out.println(cod_articulo);
        SQLiteDatabase ke_android = conn.getReadableDatabase();
        Cursor cu_comp = ke_android.rawQuery("SELECT SUM(kli_cant) FROM ke_limitart WHERE kli_codven ='" + cod_usuario + "' AND kli_codcli='" + cod_cliente + "' AND kli_codart='" + cod_articulo + "' AND status ='1'", null);

        while (cu_comp.moveToNext()) {
            resultado = cu_comp.getInt(0);
        }
        cu_comp.close();

        return resultado;
    }

    private void guardarLimite(String tracking, String cod_usuario, String cod_cliente, String cod_articulo, int cantidad, String fecha_hoy, String fecha_vence, String status) {

        SQLiteDatabase ke_android = conn.getWritableDatabase();
        ContentValues guardarArticulo = new ContentValues();
        guardarArticulo.put("kli_track", tracking);
        guardarArticulo.put("kli_codven", cod_usuario);
        guardarArticulo.put("kli_codcli", cod_cliente);
        guardarArticulo.put("kli_codart", cod_articulo);
        guardarArticulo.put("kli_cant", cantidad);
        guardarArticulo.put("kli_fechahizo", fecha_hoy);
        guardarArticulo.put("kli_fechavence", fecha_vence);
        guardarArticulo.put("status", status);
        ke_android.insert("ke_limitart", null, guardarArticulo);
    }


}


