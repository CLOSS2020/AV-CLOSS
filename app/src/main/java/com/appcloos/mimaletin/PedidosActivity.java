package com.appcloos.mimaletin;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PedidosActivity extends AppCompatActivity {
    FloatingActionButton Add;
    AdminSQLiteOpenHelper conn;
    private SharedPreferences sharpref;
    ArrayList<Pedidos> listapedidos;
    ArrayList<String> listainfo, listapedido;
    ArrayList<Carrito> listalineas;
    PedidoAdapter pedidoAdapter;

    ListView lv_pedidos;
    public static String cod_usuario;
    private SharedPreferences preferences;
    public static String codigoPedido, pedido_estatus, statusPedido, n_cliente, codigoCliente, nropedido, fechadoc, docsol, condicion, sesionNube,
            sesionActiva, fechapedido, pedidonumero, totneto, nombreEmpresa = "", codigoSucursal = "", enlaceEmpresa = "";
    public static Double montoNeto;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedidos);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //mantener la orientacion vertical
        sharpref = getSharedPreferences("sharpref", MODE_PRIVATE);
        Add = findViewById(R.id.bt_nuevop);
        lv_pedidos = findViewById(R.id.lv_pedidos);


        preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        cod_usuario = preferences.getString("cod_usuario", null);

        lineasPedidos(); //cargar las lineas del item donde se visualizan los pedidos.
        // sesion();
        // ValidezDeSesion("https://www.cloccidental.com/webservice/sesionactiva.php?cod_usuario=" +cod_usuario.trim());


        cargarEnlace();
        Add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LimpiarCarrito();
                IraCreacionPedido();


            }
        });
        /********************************************************************************/

        lv_pedidos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                codigoPedido = listapedidos.get(position).getNumeroDocumento();
                statusPedido = statusPedido;
                n_cliente = listapedidos.get(position).getNombreCliente();
                codigoCliente = listapedidos.get(position).getCodigoCliente();
                fechapedido = listapedidos.get(position).getFechaDocumento();
                pedidonumero = listapedidos.get(position).getNumeroPedido();
                totneto = String.valueOf(listapedidos.get(position).getTotalNeto());

                //System.out.println("ESTE ES EL ESTATUS QUE ESTA LLEGANDO DEL PEDIDO " + statusPedido);

                AlertDialog.Builder ventana = new AlertDialog.Builder(PedidosActivity.this);
                ventana.setTitle("Mensaje del sistema");
                ventana.setMessage("Por favor, elige una opción");

                ventana.setPositiveButton("Modificar Pedido", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (statusPedido.equals("0")) {
                            LimpiarCarrito();
                            IraModificacionPedido();
                        } else {
                            Toast.makeText(getBaseContext(), "Este pedido ya no puede ser modificado", Toast.LENGTH_LONG).show();
                        }

                    }
                });


                ventana.setNegativeButton("Borrar Pedido", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (statusPedido.equals("0")) {

                            AlertDialog.Builder subventana = new AlertDialog.Builder(PedidosActivity.this);
                            subventana.setTitle("Mensaje de confirmación");
                            subventana.setMessage("¿Estás seguro de borrar el pedido?");

                            subventana.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    BorrarPedido();
                                }
                            });

                            subventana.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Toast.makeText(PedidosActivity.this, "Eliminación cancelada", Toast.LENGTH_SHORT).show();
                                }
                            });

                            AlertDialog dialogo2 = subventana.create();
                            dialogo2.show();
                        } else if (statusPedido.equals("5")) {

                            BorrarPedidoAlt();
                            Toast.makeText(getBaseContext(), "Este pedido fue borrado solamente del dispositivo.", Toast.LENGTH_LONG).show();
                            //Toast.makeText(getBaseContext(), "Este pedido fue borrado solamente del dispositivo.", Toast.LENGTH_LONG).show();
                        }

                    }
                });


                ventana.setNeutralButton("Ver Pedido", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        AlertDialog.Builder dialogofull = new AlertDialog.Builder(PedidosActivity.this, R.style.FullSreenDialog);

                        dialogofull.setTitle(codigoPedido);
                        ListView listadeLineas = new ListView(PedidosActivity.this);
                        CargarLineasdelPedido();
                        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(PedidosActivity.this, R.layout.list_items, listapedido);
                        listadeLineas.setAdapter(arrayAdapter);
                        dialogofull.setView(listadeLineas);
                        AlertDialog dialogoverpedido = dialogofull.create();
                        dialogoverpedido.show();
                    }
                });


                AlertDialog dialogo = ventana.create();
                dialogo.show();
            }


        });

    }

    private void cargarEnlace() {
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        String[] columnas = new String[]{
                "kee_nombre," +
                        "kee_url," +
                        "kee_sucursal"};
        Cursor cursor = ke_android.query("ke_enlace", columnas, "1", null, null, null, null);

        while (cursor.moveToNext()) {
            nombreEmpresa = cursor.getString(0);
            enlaceEmpresa = cursor.getString(1);
            codigoSucursal = cursor.getString(2);
        }

    }

    private void LimpiarCarrito() {
        conn = new AdminSQLiteOpenHelper(getApplicationContext(), "ke_android", null, 1);
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        ke_android.delete("ke_carrito", "1", null);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_pedidos_main, menu);
        return super.onCreateOptionsMenu(menu);

    }


    public boolean onOptionsItemSelected(MenuItem item) {

        int itemid = item.getItemId();

        switch (itemid) {
            case R.id.validarSubidas:
                ValidarPendientes("https://cloccidental.com/Rest/obtenerpedidosdelmes.php?cod_usuario=" + cod_usuario + "&&agencia=" + codigoSucursal.trim());
                break;

            case R.id.archivados:
                iraArchivados();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void ValidarPendientes(String URL) {

        //cada vez que se ejecute, vacio la lista para asegurarme que los datos no se repitan
        listainfo = new ArrayList<String>();

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(URL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                if (response != null) {
                    //preparo los datos para la conexion a la base de datos
                    conn = new AdminSQLiteOpenHelper(getApplicationContext(), "ke_android", null, 12);
                    SQLiteDatabase ke_android = conn.getWritableDatabase();

                    JSONObject jsonObject = null;
                    //mientras la respuesta sea mayor a 0
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            //guardo lo que viene del jsonobject
                            jsonObject = response.getJSONObject(i);
                            //y lo agrego como un elemento string a la lista
                            listainfo.add(jsonObject.getString("kti_ndoc"));

                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                    //Hago la consulta para determinar que pedidos del mes estan "procesados"
                    //y que voy a validar que esten en la nube.
                    String tabla = "ke_opti";
                    String[] columnas = new String[]{
                            "kti_ndoc," +
                                    "datetime('now','start of month') as principiomes," +
                                    "  datetime('now') as hoy"
                    };
                    String condicion = "kti_status = '1' AND kti_fchdoc BETWEEN principiomes AND hoy";
                    Cursor cursor = ke_android.query(tabla, columnas, condicion, null, null, null, null);

                    while (cursor.moveToNext()) {
                        String pedidoEnTelf = cursor.getString(0);
                        System.out.println("PEDIDO EN SISTEMA: " + pedidoEnTelf);
                        if (!pedidoEnTelf.equals("")) {
                            if (!listainfo.contains(pedidoEnTelf)) {
                                ke_android.beginTransaction();
                                try {
                                    System.out.println("Este pedido no se encuentra en la nube, debe subirse");
                                    ke_android.execSQL("UPDATE ke_opti SET kti_status = '0' WHERE kti_ndoc ='" + pedidoEnTelf + "'");
                                    ke_android.setTransactionSuccessful();
                                    ke_android.endTransaction();

                                    PedidosActivity.this.finish();
                                    PedidosActivity.this.overridePendingTransition(0, 0);
                                    startActivity(PedidosActivity.this.getIntent());
                                    PedidosActivity.this.overridePendingTransition(0, 0);//para refrescar el RecyclerView
                                } catch (Exception ex) {
                                    ke_android.endTransaction();
                                }
                            } else {
                                System.out.println("Este pedido ya está en la nube");
                            }
                        } else {
                            System.out.println("No hay pedidos por subir");
                        }
                    }


                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {  //finalmente, estos son los parametros que le enviaremos al webservice, partiendo de las variables
                //donde estan guardados las fechas
                Map<String, String> parametros = new HashMap<String, String>();
                parametros.put("cod_usuario", cod_usuario);

                return parametros;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonArrayRequest); //esto es el request que se envia al url a traves de la conexion volley, (el stringrequest esta armado arriba)


    }

    private void iraArchivados() {
        sharpref.edit().clear().commit();
        Intent intent = new Intent(PedidosActivity.this, PedidosArchivadosActivity.class);
        intent.putExtra("cod_usuario", cod_usuario);
        startActivity(intent);

    }


    public void IraCreacionPedido() {
        sharpref.edit().clear().apply();
        Intent intent = new Intent(PedidosActivity.this, SeleccionarClientePedidoActivity.class);

        startActivity(intent);


    }

    public void IraModificacionPedido() {

        sharpref.edit().clear().commit();
        Intent intent = new Intent(PedidosActivity.this, ModificarPedidoActivity.class);
        intent.putExtra("codigopedido", codigoPedido);
        intent.putExtra("codigocliente", codigoCliente);
        intent.putExtra("n_cliente", n_cliente);
        startActivity(intent);
    }


    /****************************************************************************************************/
    public void lineasPedidos() {
        cargarPedidos();

        if (listapedidos != null) {
            pedidoAdapter = new PedidoAdapter(PedidosActivity.this, listapedidos);
            lv_pedidos.setAdapter(pedidoAdapter);
            pedidoAdapter.notifyDataSetChanged();

        } else {
            //System.out.println("pedidos vacios");
        }
    }

    public void cargarPedidos() {
        listapedidos = new ArrayList<Pedidos>();
        conn = new AdminSQLiteOpenHelper(getApplicationContext(), "ke_android", null, 8);
        final SQLiteDatabase ke_android = conn.getWritableDatabase();

        Cursor cursor = ke_android.rawQuery("SELECT kti_codcli, kti_ndoc, kti_nombrecli, kti_fchdoc, kti_totneto, kti_status, kti_nroped, kti_totnetodcto, datetime('now','start of month') as principiomes,\n" +
                "  datetime('now') as hoy, ke_pedstatus FROM ke_opti WHERE kti_status !='3' AND kti_codven = '" + cod_usuario.toString().trim() + "' and kti_fchdoc BETWEEN principiomes AND hoy", null);

        while (cursor.moveToNext()) {
            String estatusEval = cursor.getString(5);
            String nropedidoEval = cursor.getString(6);
            String estatusPed = cursor.getString(10);

            switch (estatusEval) {
                case "0":
                    pedido_estatus = "Por Subir";
                    statusPedido = "0";
                    break;
                case "1":
                case "2":
                    pedido_estatus = "Subido";
                    statusPedido = "1";
                    break;

            }

            switch (estatusPed) {
                case "01":
                    if (nropedido == null || nropedido.isEmpty()) {
                        pedido_estatus = "Esperando por Aprobación";
                    } else {
                        pedido_estatus = "Procesando Pedido";
                    }
                    statusPedido = "5";
                    break;
                case "12":
                    pedido_estatus = "Ya impreso";
                    statusPedido = "5";
                    break;
                case "17":
                    pedido_estatus = "En Proceso de embalaje";
                    statusPedido = "5";
                    break;
                case "20":
                    pedido_estatus = "En Proceso de etiquetado";
                    statusPedido = "5";
                    break;
                case "25":
                    pedido_estatus = "Listo Para facturar";
                    statusPedido = "5";
                    break;
                case "80":
                    pedido_estatus = "Facturado";
                    statusPedido = "5";
                    break;
                case "82":
                    pedido_estatus = "Esperando orden de salida";
                    statusPedido = "5";
                    break;
                case "85":
                    pedido_estatus = "Entregado al cliente";
                    statusPedido = "5";
                    break;
            }
            if (nropedidoEval == null) {
                nropedido = "Por Asignar";
            } else if (nropedidoEval != null) {
                nropedido = nropedidoEval;
            }


            Pedidos pedido = new Pedidos();
            pedido.setCodigoCliente(cursor.getString(0));
            pedido.setNumeroDocumento(cursor.getString(1));
            pedido.setNombreCliente(cursor.getString(2));
            pedido.setFechaDocumento(cursor.getString(3));
            pedido.setTotalNeto(cursor.getDouble(4));
            pedido.setEstatus(pedido_estatus);
            pedido.setNumeroPedido(nropedido);
            pedido.setTotalNetoDcto(cursor.getDouble(7));
            listapedidos.add(pedido);

        }
        cursor.close();
        ke_android.close();


    }


    private void CargarLineasdelPedido() {
        listalineas = new ArrayList<Carrito>();
        // System.out.println(cod_usuario);
        conn = new AdminSQLiteOpenHelper(getApplicationContext(), "ke_android", null, 12);
        final SQLiteDatabase ke_android = conn.getWritableDatabase();

        Cursor cursor = ke_android.rawQuery("SELECT kmv_codart, kmv_nombre, kmv_cant, kmv_stot, kmv_artprec FROM ke_opmv WHERE kti_ndoc='" + codigoPedido + "'", null);

        while (cursor.moveToNext()) {
            Carrito carrito = new Carrito();
            carrito.setCodigo(cursor.getString(0));
            carrito.setNombre(cursor.getString(1));
            carrito.setCantidad(cursor.getInt(2));
            carrito.setPrecio(cursor.getDouble(3));
            carrito.setPreciou(cursor.getDouble(4));
            listalineas.add(carrito);

        }
        ke_android.close();
        obtenerlineas();
    }

    private void obtenerlineas() {
        listapedido = new ArrayList<String>();

        for (int i = 0; i < listalineas.size(); i++) {
            listapedido.add("Codigo: " + listalineas.get(i).getCodigo()
                    + "\n " + listalineas.get(i).getNombre() + "\nCantidad: " + listalineas.get(i).getCantidad() + " Precio: $" + listalineas.get(i).getPrecio());


        }
    }

    public void BorrarPedido() {
        SQLiteDatabase ke_android = conn.getWritableDatabase();
/*
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
        //en realidad le cambiamos la cabecera a "3" Y borramos solo las lineas
        ke_android.execSQL("UPDATE ke_opti SET kti_status = '3' WHERE kti_ndoc ='" + codigoPedido + "'");
        ke_android.execSQL("DELETE FROM ke_opmv WHERE kti_ndoc ='" + codigoPedido + "'");
        ke_android.execSQL("DELETE FROM ke_limitart WHERE kli_track ='" + codigoPedido + "'");

        Toast.makeText(PedidosActivity.this, "Pedido borrado", Toast.LENGTH_SHORT).show();
        lineasPedidos();
        ke_android.close();
    }

    public void BorrarPedidoAlt() {
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        //en realidad le cambiamos la cabecera a "3" Y borramos solo las lineas
        ke_android.execSQL("UPDATE ke_opti SET kti_status = '3' WHERE kti_ndoc ='" + codigoPedido + "'");
        ke_android.execSQL("DELETE FROM ke_opmv WHERE kti_ndoc ='" + codigoPedido + "'");


        Toast.makeText(PedidosActivity.this, "Pedido borrado", Toast.LENGTH_SHORT).show();
        lineasPedidos();
        ke_android.close();
    }


    public void sesion() {
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        Cursor cursor = ke_android.rawQuery("SELECT sesionactiva FROM usuarios WHERE vendedor ='" + cod_usuario.trim() + "'", null);

        while (cursor.moveToNext()) {
            sesionActiva = cursor.getString(0).trim();
        }
    }


    private void cerrarsesion() {
        SharedPreferences preferences = getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        preferences.edit().clear().commit();

        PrincipalActivity.getInstance().finish();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();

    }


    private void ValidezDeSesion(String URL) {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(URL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                if (response != null) {

                    JSONObject jsonObject = null; //creamos un objeto json vacio
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            jsonObject = response.getJSONObject(i);
                            sesionNube = jsonObject.getString("sesionactiva").trim();
                            System.out.println(sesionNube);


                            if (!sesionNube.equals(sesionActiva)) {
                                AlertDialog.Builder ventana = new AlertDialog.Builder(PedidosActivity.this);
                                ventana.setTitle("Alerta del sistema:");
                                ventana.setMessage("Su sesión ha expirado porque existe otra activa, será redireccionado a la pantalla de inicio");
                                ventana.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        cerrarsesion();
                                        finish();
                                    }
                                });
                                AlertDialog dialogo = ventana.create();
                                dialogo.show();


                            } else if (sesionNube.equals(sesionActiva)) {
                                System.out.println("La sesion es la misma");
                                /* Toast.makeText(getApplicationContext(), "La sesion es la misma", Toast.LENGTH_LONG).show();*/
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                } else {
                    // Toast.makeText(getApplicationContext(), "empty", Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parametros = new HashMap<String, String>();
                return parametros;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonArrayRequest); //esto es el request que se envia al url a traves de la conexion volley, (el stringrequest esta armado arriba


    }


    @Override
    protected void onResume() {
        super.onResume();
        lineasPedidos();
        preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        cod_usuario = preferences.getString("cod_usuario", null);


    }

    @Override
    public void onBackPressed() {

        Intent intent = new Intent(getApplicationContext(), PrincipalActivity.class);

        //Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
        super.onBackPressed();
    }
}