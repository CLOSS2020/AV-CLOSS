package com.appcloos.mimaletin;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
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

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ModificarPedidoActivity extends AppCompatActivity {

    private int APP_ITEMS_FACTURAS, APP_ITEMS_NOTAS_ENTREGA, NOEMIFAC, NOEMINOTA;
    public static String codigoPedido, n_cliente;
    private RadioButton Factura, NotaEntrega, Credito, Prepago;
    private RadioGroup GrupoRadio;
    public static String documento, formaPago, tipoDoc = "PED", tipoDePrecioaMostrar, codigoCliente, negociacionActivaMod, negociacionEstado;
    ArrayAdapter adapter;
    ArrayAdapter<CharSequence> adapterSpinner;
    ListView listaLineasMod;
    ArrayList<String> listainfo = null, listapedido = null;
    ArrayList<Carrito> listacarritoMod;
    BottomNavigationView menunav;
    public static int seleccion, indexposicion, nroCorrelativo, enteroPrecio;
    Spinner spinner;
    AdminSQLiteOpenHelper conn;
    TextView tv_subtotal, tv_nombrecliente, tv_montominMod;
    TextView tv_subcondcto;
    TextView tv_netocondescuento;
    Double subtotal;
    Double NetoTotal = 0.00;
    Double montoMinimoTotal = 0.00;
    TextView tv_neto;
    ImageButton ibt_modificar;
    CarritoAdapter carritoAdapter;
    public static Double precioTotalporArticulo, preciocliente, montoMinimoMod, montoNetoConDescuento, descuentoTotal;
    String enpreventa = "0";
    Switch sw_negoespemod;
    TextView tv_bloqueado;

    String cod_usuario;

    private SharedPreferences preferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modificar_pedido);

        conn = new AdminSQLiteOpenHelper(getApplicationContext(), "ke_android", null, 1);

        Intent intent = getIntent();
        codigoPedido = intent.getStringExtra("codigopedido");
        n_cliente = intent.getStringExtra("n_cliente");
        codigoCliente = intent.getStringExtra("codigocliente");
        getSupportActionBar().setTitle("Pedido: " + codigoPedido);

        APP_ITEMS_FACTURAS = (int) Math.round(conn.getConfigNum("APP_ITEMS_FACTURAS"));
        APP_ITEMS_NOTAS_ENTREGA = (int) Math.round(conn.getConfigNum("APP_ITEMS_NOTAS_ENTREGA"));
        NOEMIFAC = conn.getCampoInt("cliempre", "noemifac", "codigo", codigoCliente);
        NOEMINOTA = conn.getCampoInt("cliempre", "noeminota", "codigo", codigoCliente);

        preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        cod_usuario = preferences.getString("cod_usuario", null);

        GrupoRadio = (RadioGroup) findViewById(R.id.radioGroupDocMod);
        Factura = (RadioButton) findViewById(R.id.RbFacturaMod);
        NotaEntrega = (RadioButton) findViewById(R.id.RbNotaEntregaMod);
        Credito = (RadioButton) findViewById(R.id.RbCreditoMod);
        Prepago = (RadioButton) findViewById(R.id.RbPrepagoMod);
        tv_neto = findViewById(R.id.tv_neto_mod);
        sw_negoespemod = findViewById(R.id.sw_negoespecialMod);
        tv_nombrecliente = findViewById(R.id.tv_clientepedido);
        tv_netocondescuento = findViewById(R.id.tv_neto_dcto);
        tv_nombrecliente.setText(n_cliente);
        tv_subcondcto = findViewById(R.id.tv_subdcto);
        listaLineasMod = findViewById(R.id.ListPedidoMod);
        tv_montominMod = findViewById(R.id.tv_montominMod);
        tv_bloqueado = findViewById(R.id.tv_avisobloqueo);

        menunav = findViewById(R.id.menu_modifi);
        menunav.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        lineasDelPedido();
        obtenerCondicionesEspeciales();
        CargarCondiciones();
        asignarTipodePrecio();
        validarNegActivo();
        CargarLineas();
        SumaNeto();
        validarSiHayPreventa();
        montoMinimoTotal = obtenerMontoMinimoTotal();

        if (Factura.isChecked()) {
            tv_bloqueado.setVisibility(View.VISIBLE);
            tv_bloqueado.setText("Las facturas solo tendran un  maximo de 12 lineas de articulos");
        }

        GrupoRadio.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.RbFacturaMod) {
                    tv_bloqueado.setVisibility(View.VISIBLE);
                    tv_bloqueado.setText("Las facturas solo tendrán un máximo de " + APP_ITEMS_FACTURAS + " líneas de artículos");
                } else if (checkedId == R.id.RbNotaEntregaMod) {
                    tv_bloqueado.setVisibility(View.VISIBLE);
                    tv_bloqueado.setText("Las N/E solo tendrán un máximo de " + APP_ITEMS_NOTAS_ENTREGA + " líneas de artículos");
                }
            }
        });

        Prepago.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                recalculoPrecio(isChecked);
            }
        });

        if (NotaEntrega.isChecked()) {
            Prepago.setChecked(false);
            Prepago.setVisibility(View.INVISIBLE);
            Prepago.setEnabled(false);
            Credito.setChecked(true);
        } else {
            Prepago.setVisibility(View.VISIBLE);
            Prepago.setEnabled(true);
        }

        NotaEntrega.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Prepago.setChecked(false);
                    Prepago.setVisibility(View.INVISIBLE);
                    Prepago.setEnabled(false);
                    Credito.setChecked(true);
                } else {
                    Prepago.setVisibility(View.VISIBLE);
                    Prepago.setEnabled(true);
                }
            }
        });


        //aqui va el metodo para borrar un articulo del pedido (onlongclick)
        listaLineasMod.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
                final String codigo = listacarritoMod.get(position).getCodigo();

                final AlertDialog.Builder ventana = new AlertDialog.Builder(ModificarPedidoActivity.this);
                ventana.setTitle("Mensaje del Sistema");
                ventana.setMessage("¿Desea eliminar este artículo?");

                ventana.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        SQLiteDatabase ke_android = conn.getWritableDatabase();
                        ke_android.execSQL("DELETE FROM ke_carrito WHERE kmv_codart ='" + codigo + "'");
                        actualizaLista();

                        Toast.makeText(ModificarPedidoActivity.this, "Artículo borrado", Toast.LENGTH_SHORT).show();


                    }
                });

                ventana.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                AlertDialog dialogo = ventana.create();
                dialogo.show();


                return false;
            }
        });


        /************************************************************************************************************/
        //este metodo permite cambiar las cantidades del articulo que se encuentra en ke_carrito al momento de ser seleccionado
        listaLineasMod.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                final String codigo = listacarritoMod.get(position).getCodigo();
                final Double dctolin = listacarritoMod.get(position).getDctolin();

                final EditText cajatexto = new EditText(ModificarPedidoActivity.this);
                cajatexto.setInputType(InputType.TYPE_CLASS_NUMBER);

                conn = new AdminSQLiteOpenHelper(getApplicationContext(), "ke_android", null, 1);
                final SQLiteDatabase ke_android = conn.getWritableDatabase();
                Cursor cursor_mul = ke_android.rawQuery("SELECT vta_min, vta_minenx FROM articulo WHERE codigo ='" + codigo + "'", null);
                System.out.println("SELECT vta_min, vta_minenx FROM articulo WHERE codigo ='" + codigo + "'");
                cursor_mul.moveToFirst();
                double vta_Min = cursor_mul.getDouble(0);
                int vta_minenx = cursor_mul.getInt(1);
                System.out.println("ventaMin: " + vta_Min);
                AlertDialog.Builder ventana = new AlertDialog.Builder(ModificarPedidoActivity.this);
                ventana.setTitle("Modificar Linea");
                ventana.setMessage("Porfavor, elige la cantidad");
                if (vta_minenx == 1) {
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
                }
                ventana.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {


                        String cantidad_nueva = cajatexto.getText().toString();


                        int cantidad = Integer.parseInt(cantidad_nueva);

                        if (cantidad != 0) {

                            Cursor cursor = ke_android.rawQuery("SELECT " + tipoDePrecioaMostrar + ", (existencia - comprometido), vta_min, vta_max, vta_minenx FROM articulo WHERE codigo ='" + codigo + "'", null);
                            cursor.moveToNext();
                            Double precio = cursor.getDouble(0);
                            Double existencia = cursor.getDouble(1);
                            Double ventaMax = cursor.getDouble(3);
                            Double ventaMin = cursor.getDouble(2);
                            int vta_minenx = cursor.getInt(4);
                            int existencia_validar = (int) Math.round(existencia);

                            System.out.println(cantidad);
                            System.out.println(existencia_validar);

                            if (cantidad > existencia_validar) {
                                Toast.makeText(ModificarPedidoActivity.this, "La cantidad no puede ser superior a la existencia", Toast.LENGTH_SHORT).show();
                            } else {
                                if (ventaMin > 0) {
                                    System.out.println("Multiplo: " + vta_minenx);
                                    if (vta_minenx == 1) {
                                        if (cantidad * ventaMin > existencia) {
                                            Toast.makeText(ModificarPedidoActivity.this, "Debe de elegir una cantidad dentro de la existencia", Toast.LENGTH_LONG).show();
                                        } else if (cantidad * ventaMin <= existencia) {
                                            int cantidad_new = (int) (cantidad * ventaMin);
                                            System.out.println("Nueva cantidad " + cantidad_new);
                                            Double precioTotal = precio * cantidad_new;
                                            System.out.println("Precio total: " + precioTotal);
                                            precioTotal = Math.round(precioTotal * 100.00) / 100.00;

                                            ke_android.beginTransaction();
                                            try {

                                                Double precioNuevo = precioTotal;
                                                precioNuevo = Math.round(precioNuevo * 100.0) / 100.00;

                                                Double mtoDctoNuevo = precioNuevo - (precioNuevo * (dctolin / 100));
                                                mtoDctoNuevo = Math.round(mtoDctoNuevo * 100.0) / 100.00;


                                                ke_android.execSQL("UPDATE ke_carrito SET kmv_cant=" + cantidad_new + ", kmv_stot =" + precioTotal + ", kmv_stotdcto =" + mtoDctoNuevo + " WHERE kmv_codart ='" + codigo + "'");


                                                ke_android.setTransactionSuccessful();
                                                ke_android.endTransaction();
                                                Toast.makeText(ModificarPedidoActivity.this, "Artículo modificado", Toast.LENGTH_SHORT).show();
                                            } catch (Exception ex) {
                                                System.out.println(ex);
                                                ke_android.endTransaction();

                                            }
                                        }

                                    } else {
                                        if (cantidad < ventaMin) {
                                            Toast.makeText(ModificarPedidoActivity.this, "Debe cumplir con la cantidad mínima para la venta", Toast.LENGTH_LONG).show();
                                        } else if (cantidad >= ventaMin) {

                                            Double precioTotal = precio * Double.valueOf(cantidad);
                                            precioTotal = Math.round(precioTotal * 100.00) / 100.00;

                                            ke_android.beginTransaction();
                                            try {

                                                Double precioNuevo = precio * Double.valueOf(cantidad);
                                                precioNuevo = Math.round(precioNuevo * 100.0) / 100.00;

                                                Double mtoDctoNuevo = precioNuevo - (precioNuevo * (dctolin / 100));
                                                mtoDctoNuevo = Math.round(mtoDctoNuevo * 100.0) / 100.00;


                                                ke_android.execSQL("UPDATE ke_carrito SET kmv_cant=" + cantidad + ", kmv_stot =" + precioTotal + ", kmv_stotdcto =" + mtoDctoNuevo + " WHERE kmv_codart ='" + codigo + "'");


                                                ke_android.setTransactionSuccessful();
                                                ke_android.endTransaction();
                                                Toast.makeText(ModificarPedidoActivity.this, "Artículo modificado", Toast.LENGTH_SHORT).show();
                                                //finish();
                                            } catch (Exception ex) {
                                                System.out.println(ex);
                                                ke_android.endTransaction();

                                            }
                                        }
                                    }


                                } else {
                                    Double precioTotal = precio * Double.valueOf(cantidad);
                                    precioTotal = Math.round(precioTotal * 100.00) / 100.00;

                                    ke_android.beginTransaction();
                                    try {

                                        Double precioNuevo = precio * Double.valueOf(cantidad);
                                        precioNuevo = Math.round(precioNuevo * 100.0) / 100.00;

                                        Double mtoDctoNuevo = precioNuevo - (precioNuevo * (dctolin / 100));
                                        mtoDctoNuevo = Math.round(mtoDctoNuevo * 100.0) / 100.00;


                                        ke_android.execSQL("UPDATE ke_carrito SET kmv_cant=" + cantidad + ", kmv_stot =" + precioTotal + ", kmv_stotdcto =" + mtoDctoNuevo + " WHERE kmv_codart ='" + codigo + "'");


                                        ke_android.setTransactionSuccessful();
                                        ke_android.endTransaction();
                                        //finish();
                                        Toast.makeText(ModificarPedidoActivity.this, "Artículo modificado", Toast.LENGTH_SHORT).show();
                                    } catch (Exception ex) {
                                        System.out.println(ex);
                                        ke_android.endTransaction();

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

                                Toast.makeText(ModificarPedidoActivity.this, "Articulo añadido", Toast.LENGTH_LONG).show();
                                actualizaLista();

                                //Toast.makeText(ModificarPedidoActivity.this, "Artículo modificado", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(ModificarPedidoActivity.this, "La existencia no puede ser 0", Toast.LENGTH_SHORT).show();
                        }

                    }
                });

                ventana.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                AlertDialog dialogo = ventana.create();
                dialogo.show();


            }
        });


        sw_negoespemod.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (compoundButton.isChecked()) {

                    tv_montominMod.setEnabled(true);
                    tv_montominMod.setVisibility(View.VISIBLE);
                    tv_montominMod.setText("Monto Mín: $" + montoMinimoMod);

                } else {
                    tv_montominMod.setEnabled(false);
                    tv_montominMod.setVisibility(View.INVISIBLE);

                }
            }
        });

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

        if (NOEMIFAC == 1 && NOEMINOTA == 1){
            Toast.makeText(this, "Cliente suspendido", Toast.LENGTH_SHORT).show();
            finish();
        }

        Factura.setOnClickListener(v -> {
            analizarArticulos("vta_solone", listacarritoMod);
        });

        NotaEntrega.setOnClickListener(v -> {
            analizarArticulos("vta_solofac", listacarritoMod);
        });

    }

    private void analizarArticulos(String campo, ArrayList<Carrito> listacarrito) {

        int num = 0;
        //int numNE = 0;
        for (int i = 0; i < listacarrito.size(); i++){
            num = conn.getCampoInt("articulo", campo, "codigo", listacarrito.get(i).codigo);
            //numNE += conn.getCampoInt("articulo", "vta_solone", "codigo", listacarrito.get(i).codigo);
            if (num > 0){
                break;
            }
        }

        if (num > 0 && NotaEntrega.isChecked()){
            Toast.makeText(this, "Posee artículos que solo están disponibles para Facturas", Toast.LENGTH_SHORT).show();
            Factura.setChecked(true);
        } else if (num > 0 && Factura.isChecked()){
            Toast.makeText(this, "Posee artículos que solo están disponibles para Notas de Entrega", Toast.LENGTH_SHORT).show();
            NotaEntrega.setChecked(true);
        }

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
            Double precioTotal = (precio * cantidad);
            Double precioTotalRedondo = Math.round(precioTotal * 100.00) / 100.00;
            cursor.close();


            ke_android.execSQL("UPDATE ke_carrito SET kmv_artprec=" + precio + ", kmv_stot= " + precioTotalRedondo + ", kmv_stotdcto=" + precioTotalRedondo + " WHERE kmv_codart ='" + codigo + "'");
            CargarLineas();
            SumaNeto();

        }
        cursorMain.close();

    }


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

    private AlertDialog ValidarSalida() {

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Salir")
                .setMessage("¿Está seguro de desear salir?")
                .setCancelable(true)
                .setPositiveButton("Si", null)
                .setNegativeButton("No", null)
                .show();

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(v -> dialog.dismiss());

        return dialog;
    }

    private void validarSiHayPreventa() {
        conn = new AdminSQLiteOpenHelper(getApplicationContext(), "ke_android", null, 1);
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        Cursor cursor = ke_android.rawQuery("SELECT count(ke_opmv.kmv_codart) FROM ke_opmv " +
                "LEFT JOIN articulo ON articulo.codigo = ke_opmv.kmv_codart WHERE articulo.enpreventa = '1' AND kti_ndoc ='" + codigoPedido + "'", null);
        Integer conteo = 0;
        if (cursor.moveToFirst()) {
            conteo = cursor.getInt(0);
        }

        if (conteo > 0) {
            enpreventa = "1";
        }
    }

    private final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

            switch (menuItem.getItemId()) {
                case R.id.ic_agregarart:

                    if (codigoCliente == null || (!Factura.isChecked() && !NotaEntrega.isChecked()) || (!Prepago.isChecked() && !Credito.isChecked())) {
                        Toast.makeText(ModificarPedidoActivity.this, "Debes Seleccionar un cliente, factura o nota de entrega y credito o prepago", Toast.LENGTH_SHORT).show();

                    } else {
                        if ((Factura.isChecked() && listacarritoMod.size() > APP_ITEMS_FACTURAS) || (NotaEntrega.isChecked() && listacarritoMod.size() > APP_ITEMS_NOTAS_ENTREGA)) {
                            if (Factura.isChecked()) {
                                Toast.makeText(ModificarPedidoActivity.this, "Para facturas debe tener un maximo de " + APP_ITEMS_FACTURAS + " lineas de articulos", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(ModificarPedidoActivity.this, "Para notas de entrega debe tener un maximo de " + APP_ITEMS_NOTAS_ENTREGA + " lineas de articulos", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            //Asignacion de precio para saber si es negociacion especial
                            //asignarTipodePrecio();
                            iraCatalogo();
                        }


                    }
                    return true;
                case R.id.ic_procesarpedido:
                    if (negociacionIsActiva() == false && montoNetoConDescuento < montoMinimoTotal) {
                        Toast.makeText(ModificarPedidoActivity.this, "Para procesar el pedido, debe cumplir con el monto mínimo de $" + montoMinimoTotal, Toast.LENGTH_LONG).show();
                    } else if (montoNetoConDescuento >= montoMinimoTotal) {
                        if ((Factura.isChecked() && listacarritoMod.size() > APP_ITEMS_FACTURAS) || (NotaEntrega.isChecked() && listacarritoMod.size() > APP_ITEMS_NOTAS_ENTREGA)) {
                            if (Factura.isChecked()) {
                                Toast.makeText(ModificarPedidoActivity.this, "Para facturas debe tener un maximo de " + APP_ITEMS_FACTURAS + " lineas de articulos", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(ModificarPedidoActivity.this, "Para notas de entrega debe tener un maximo de " + APP_ITEMS_NOTAS_ENTREGA + " lineas de articulos", Toast.LENGTH_LONG).show();
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


    public void obtenerCondicionesEspeciales() {
        //CON ESTA FUNCION IDENTIFICO SI EL CLIENTE POSEE O NO LA POSIBLIDAD DE LLEVAR A CABO UNA NEGOCIACIÓN ESPECIAL
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        Cursor cursor = ke_android.rawQuery("SELECT kne_activa, kne_mtomin FROM cliempre WHERE codigo ='" + codigoCliente + "'", null);

        while (cursor.moveToNext()) {
            negociacionActivaMod = cursor.getString(0);
            montoMinimoMod = cursor.getDouble(1); //IMPORTANTE: EL CLIENTE CUENTA CON UN MONTO MINIMO PARA LA NEG. ESPECIAL
        }

        if (negociacionActivaMod.equals("0")) {
            sw_negoespemod.setEnabled(false);
            sw_negoespemod.setVisibility(View.INVISIBLE);

            tv_montominMod.setEnabled(false);
            tv_montominMod.setVisibility(View.INVISIBLE);

        } else if (negociacionActivaMod.equals("1")) {
            sw_negoespemod.setEnabled(true);
            sw_negoespemod.setVisibility(View.VISIBLE);

        }

    }

    public boolean negociacionIsActiva() {
        return sw_negoespemod.isChecked();
    }

    public void validarNegActivo() {
        if (negociacionIsActiva() == true) {
            tv_montominMod.setText("Monto Mín: $" + montoMinimoMod);
            tv_montominMod.setEnabled(true);
            tv_montominMod.setVisibility(View.VISIBLE);
            tv_montominMod.setTextColor(Color.rgb(22, 129, 67));
        }
    }


    //este es el metodo para procesar el pedido
    private void ProcesarPedido() {

        if (negociacionIsActiva() == true) {
            if (precioTotalporArticulo >= montoMinimoMod) {
                guardarDatosPedidoMod();
            } else if (precioTotalporArticulo < montoMinimoMod) {
                Toast.makeText(ModificarPedidoActivity.this, "En negociación especial, el pedido debe cumplir con el monto mínimo asignado.", Toast.LENGTH_LONG).show();
            }
        } else if (negociacionIsActiva() == false) {
            negociacionActivaMod = "0";
            guardarDatosPedidoMod();
        }


    }

    //con este metodo actualizamos las lineas del carrito y del neto cada vez que se produce un cambio (modificacion/eliminacion)
    private void actualizaLista() {
        CargarLineas();
        SumaNeto();
    }


    /************************************************************************************/
    //este metodo realiza la sumatoria del neto en funcion a los articulos que se van agregando en ke_carrito
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
                tv_netocondescuento.setText("$" + montoNetoConDescuento.toString());
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

        if (negociacionIsActiva() == true) {


            if (precioTotalporArticulo >= montoMinimoMod) {
                tv_montominMod.setTextColor(Color.rgb(22, 129, 67));

            } else if (precioTotalporArticulo < montoMinimoMod) {
                tv_montominMod.setTextColor(Color.rgb(244, 67, 54));
            }


        }


    }


    private void guardarDatosPedidoMod() {
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        Cursor cursor = ke_android.rawQuery("SELECT kmv_codart, kmv_nombre, kmv_cant, kmv_stot, kmv_artprec, kmv_dctolin FROM ke_carrito WHERE 1", null);

        menunav.setEnabled(false);

        if (cursor.moveToFirst()) {
            PedidoCondicion();
            String kti_docsolicitado = documento;
            String kti_condicion = formaPago;
            Double kti_totneto = precioTotalporArticulo;
            String kti_ndoc = codigoPedido;
            String kti_tdoc = tipoDoc;
            Double kti_precio = 1.00;
            String kti_negesp = negociacionActivaMod;

            Date fechaTabla = new Date(Calendar.getInstance().getTimeInMillis());
            SimpleDateFormat formatoFechaTabla = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String fechaGuardar = formatoFechaTabla.format(fechaTabla);

            String kti_fchdoc = fechaGuardar;

            try {

                ContentValues actualizarCabecera = new ContentValues();
                ke_android.beginTransaction(); //iniciamos la tranasaccion

                actualizarCabecera.put("kti_docsol", kti_docsolicitado);
                actualizarCabecera.put("kti_condicion", kti_condicion);
                actualizarCabecera.put("kti_totneto", kti_totneto);
                actualizarCabecera.put("kti_fchdoc", kti_fchdoc);
                actualizarCabecera.put("kti_negesp", kti_negesp);
                actualizarCabecera.put("kti_totnetodcto", montoNetoConDescuento);
                ke_android.update("ke_opti", actualizarCabecera, "kti_ndoc='" + codigoPedido + "'", null);

                //borramos las lineas actuales para incluir las nuevas
                ke_android.execSQL("DELETE FROM ke_opmv WHERE kti_ndoc ='" + codigoPedido + "'");

                //insertamos las lineas
                for (int i = 0; i < listacarritoMod.size(); i++) {

                    ContentValues insertarLineas = new ContentValues();
                    insertarLineas.put("kmv_codart", listacarritoMod.get(i).getCodigo());
                    insertarLineas.put("kmv_nombre", listacarritoMod.get(i).getNombre());
                    insertarLineas.put("kti_tipprec", kti_precio);
                    insertarLineas.put("kmv_cant", listacarritoMod.get(i).getCantidad());
                    insertarLineas.put("kti_tdoc", kti_tdoc);
                    insertarLineas.put("kti_ndoc", kti_ndoc);
                    insertarLineas.put("kmv_stot", listacarritoMod.get(i).getPrecio());
                    insertarLineas.put("kmv_artprec", listacarritoMod.get(i).getPreciou());
                    insertarLineas.put("kmv_dctolin", listacarritoMod.get(i).getDctolin());
                    insertarLineas.put("kmv_stotdcto", listacarritoMod.get(i).getStotNeto());

                    //insertamos las lineas
                    ke_android.insert("ke_opmv", null, insertarLineas);
                }
                //limpiamos ke_carrito
                ke_android.delete("ke_carrito", "1", null);


                ke_android.setTransactionSuccessful();

            } catch (Exception ex) {
                Toast.makeText(ModificarPedidoActivity.this, "Error en: " + ex, Toast.LENGTH_SHORT).show();
            } finally {
                ke_android.endTransaction();
            }
            Toast.makeText(ModificarPedidoActivity.this, "Pedido modificado exitosamente", Toast.LENGTH_SHORT).show();
            finish();

        } else {
            Toast.makeText(ModificarPedidoActivity.this, "Por favor, agrega artículos al pedido", Toast.LENGTH_SHORT).show();
        }
    }


    private Double obtenerMontoMinimoTotal() {
        Double monto = 0.00;
        monto = 75.00;

        return monto;
    }


    private void CargarLineas() {

        CarritoCompras();

        if (listacarritoMod != null) {
            // adapter.notifyDataSetChanged();
            carritoAdapter = new CarritoAdapter(ModificarPedidoActivity.this, listacarritoMod);
            listaLineasMod.setAdapter(carritoAdapter);
            carritoAdapter.notifyDataSetChanged();

        } else {
            Toast.makeText(ModificarPedidoActivity.this, "Por favor, agrega lineas al pedido", Toast.LENGTH_SHORT).show();
        }
    }

    /*************************************************************/

    private void CarritoCompras() {
        listacarritoMod = new ArrayList<Carrito>();

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
            carrito.setDctolin(cursor.getDouble(5));
            carrito.setStotNeto(cursor.getDouble(6));

            listacarritoMod.add(carrito);

        }
        ke_android.close();

    }


    private void iraCatalogo() {
        menunav.setEnabled(false);
        Toast.makeText(ModificarPedidoActivity.this, "Cargando Datos", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(ModificarPedidoActivity.this, CatalogoActivity.class);
        seleccion = 2;
        intent.putExtra("Seleccion", seleccion);
        intent.putExtra("tipoDePrecioaMostrar", tipoDePrecioaMostrar);
        intent.putExtra("enpreventa", enpreventa);
        intent.putExtra("factura", Factura.isChecked());
        startActivity(intent);

    }

    public void lineasDelPedido() {
        conn = new AdminSQLiteOpenHelper(getApplicationContext(), "ke_android", null, 12);
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        Cursor cursor = ke_android.rawQuery("SELECT kmv_codart, kmv_nombre, kmv_cant, kmv_stot, kmv_artprec, kmv_dctolin, kmv_stotdcto FROM ke_opmv WHERE kti_ndoc='" + codigoPedido + "'", null);

        while (cursor.moveToNext()) {

            String kmv_codart = cursor.getString(0);
            String kmv_nombre = cursor.getString(1);
            Double kmv_cant = cursor.getDouble(2);
            Double kmv_stot = cursor.getDouble(3);
            Double kmv_artprec = cursor.getDouble(4);
            Double kmv_dctolin = cursor.getDouble(5);
            Double kmv_stotdcto = cursor.getDouble(6);

            ContentValues insertarenCarrito = new ContentValues();
            insertarenCarrito.put("kmv_codart", kmv_codart);
            insertarenCarrito.put("kmv_nombre", kmv_nombre);
            insertarenCarrito.put("kmv_cant", kmv_cant);
            insertarenCarrito.put("kmv_stot", kmv_stot);
            insertarenCarrito.put("kmv_artprec", kmv_artprec);
            insertarenCarrito.put("kmv_dctolin", kmv_dctolin);
            insertarenCarrito.put("kmv_stotdcto", kmv_stotdcto);

            ke_android.insert("ke_carrito", null, insertarenCarrito);

        }
    }

    //aqui determinamos el valor de las condiciones para el pedido(documento/condicion)
    public void PedidoCondicion() {
        if (Factura.isChecked() == true) {
            documento = "1";
            if (Credito.isChecked() == true) {
                formaPago = "2";
            } else if (Prepago.isChecked() == true) {
                formaPago = "1";
            }


        } else if (NotaEntrega.isChecked() == true) {
            documento = "2";
            if (Credito.isChecked() == true) {
                formaPago = "2";
            } else if (Prepago.isChecked() == true) {
                formaPago = "1";
            }
        }
        //-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.

    }

    //este metodo evalua la cabecera del pedido y marca los radiobuttons segun la condicion que encuentre
    public void CargarCondiciones() {
        conn = new AdminSQLiteOpenHelper(getApplicationContext(), "ke_android", null, 1);
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        Cursor cursor = ke_android.rawQuery("SELECT kti_docsol, kti_condicion, kti_negesp FROM ke_opti WHERE kti_ndoc='" + codigoPedido + "'", null);

        while (cursor.moveToNext()) {
            documento = cursor.getString(0);
            formaPago = cursor.getString(1);
            negociacionEstado = cursor.getString(2);
        }

        if (negociacionEstado.equals("1")) {
            sw_negoespemod.setChecked(true);
        }

        if (documento.equals("1")) {
            Factura.toggle();
        } else if (documento.equals("2")) {
            NotaEntrega.toggle();
        }

        if (formaPago.equals("1")) {
            Prepago.toggle();
        } else if (formaPago.equals("2")) {
            Credito.toggle();
        }

    }


    public void asignarTipodePrecio() {
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        Cursor cursor = ke_android.rawQuery("SELECT precio FROM cliempre WHERE codigo='" + codigoCliente + "'", null);

        while (cursor.moveToNext()) {
            preciocliente = cursor.getDouble(0);
            enteroPrecio = (int) Math.round(preciocliente);
        }

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

    }


    @Override
    protected void onResume() {
        super.onResume();
        CargarLineas();
        SumaNeto();
        menunav.setEnabled(true);
        // adapter.notifyDataSetChanged();
        validarSiHayPreventa();

    }


    public int consultarDisponibilidad(String cod_usuario, String cod_cliente, String cod_articulo) {
        int resultado = 0;
        SQLiteDatabase ke_android = conn.getReadableDatabase();
        Cursor cu_comp = ke_android.rawQuery("SELECT SUM(kli_cant) FROM ke_limitart WHERE kli_codven ='" + cod_usuario + "' AND kli_codcli='" + cod_cliente + "' AND kli_codart='" + cod_articulo + "' AND status ='1'", null);

        while (cu_comp.moveToNext()) {
            resultado = cu_comp.getInt(0);
        }

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