package com.appcloos.mimaletin;

import static android.widget.Toast.LENGTH_LONG;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class SincronizacionActivity extends AppCompatActivity implements Serializable {
    public static String codigo, grupo, subgrupo, nombre, referencia, marca, unidad, fecha_sinc, fechamodifi, cod_usuario, direccion, perscont, telefonos, vendedor, sector, subcodigo, telefono_movil, supervpor, zona, subsector, id_precio1, id_precio2, id_precio3, id_precio4, id_precio5, id_precio6, id_precio7, username, password, almacen, kti_codcli, kti_codven, kti_docsolicitado, kti_condicion, kti_tdoc, kti_ndoc, tmp_nombrecli, kti_fchdoc, kti_status, kmv_codart, kmv_nombre, kti_fechamodifi, nropedido, numinterno, fechamodifidoc, fecha_sinc_articulo, fecha_sinc_cliempre, fecha_sinc_grupos, fecha_sinc_listvend, fecha_sinc_usuarios, ke_pedstatus, fecha_sinc_sectores, fecha_sinc_subsectores, fecha_sinc_subgrupos, kne_activa, kti_negesp, codigoKardex, fechaKardex, nivelUsuario, fecha_sinc_limites, ltrack, lvendedor, lcliente, larticulo, lfhizo, lfvence, nombreEmpresa = "", enlaceEmpresa = "", codigoSucursal = "", ambienteJob = "webservice_prueba", enpreventa, comprometido, vta_minenx, cxcndoc, tiporecibo, codvend, nro_recibo, kecxc_id, fchrecibo, clicontesp, moneda, bcoecod, bcocod, bconombre, fchr_dep, bcoref, edorec, fchhr, fchvigen, agencia, tipodoc, documento, nroret, fchemiret, refret, nroretfte, fchemirfte, refretfte, retmun_cod, retmun_nro, retmun_fch, retmun_ref, reci_doc;
    public static Double precio1, precio2, precio3, precio4, precio5, precio6, precio7, existencia, discont, status, contribespecial, superves, nivgcial, desactivo, ualterprec, kti_tipprec, kti_totneto, kmv_cant, kmv_stot, kmv_artprec, precio, kne_mtomin, cantidadKardex, vta_max, vta_min, dctotope, kmv_dctolin, tasadia, bsneto, bsiva, bsretiva, bsflete, bstotal, dolneto, doliva, dolretiva, dolflete, doltotal, dctoaplic, netocob, efectivo, bcomonto, bsretflete, diasvigen, retmun_sbi, retmun_sbs, bscobro, prcdsctopp, bsmtofte, bsretfte, bsmtoiva, retmun_bi, retmun_mto, diascalc;
    public static int contadorvend = 0, contadorart = 0, contadorcli = 0, contadorpedidosactualizados = 0, varAux = 0, lcantidad;
    public static double numBarraProgreso = 100 / 14;
    public static boolean varAuxError = false;
    public static Cursor cursorti = null, cursormv = null, cursorLim = null;
    public static JSONArray arrayTi, arrayMV, arrayLimite, arrayRec, arrayCH, arrayCL;
    private final String Version = Constantes.VERSION_NAME + " " + Constantes.FECHA_VERSION;
    Button bt_sync, bt_subir, bt_subirprecob;
    AdminSQLiteOpenHelper conn;
    ArrayList<Pedidos> listapedido;
    ArrayList<Carrito> listalineas;
    TextView tv_vendedor, tv_cliente, tv_grupos, tv_subgrupos, tv_sector, tv_subsector, tv_articulos, tv_pedidossubidos, tv_aviso, tv_pedidosact, tvDocumentos;
    private SharedPreferences preferences;
    private ProgressDialog progressDialog;

    public static Date ParseFecha(String fecha) {
        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd HH:mm:s");
        Date fechaDate = null;
        try {
            fechaDate = formato.parse(fecha);
        } catch (ParseException ex) {
            System.out.println(ex);
        }
        return fechaDate;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sincronizacion);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//mantener la activity en vertical

        preferences = getSharedPreferences("Preferences", MODE_PRIVATE);


        cod_usuario = preferences.getString("cod_usuario", null);
        String nombre_usuario = preferences.getString("nombre_usuario", null);
        //nivelUsuario = preferences.getString("superves", "0");

        bt_sync = findViewById(R.id.bt_sync);
        bt_subir = findViewById(R.id.bt_subir);
        bt_subirprecob = findViewById(R.id.bt_subirprecob);


        tv_vendedor = findViewById(R.id.tv_vendedor);
        tv_cliente = findViewById(R.id.tv_cliente);
        tv_grupos = findViewById(R.id.tv_grupos);
        tv_subgrupos = findViewById(R.id.tv_subgrupos);
        tv_sector = findViewById(R.id.tv_sector);
        tv_subsector = findViewById(R.id.tv_subsector);
        tv_articulos = findViewById(R.id.tv_articulos);
        tv_pedidossubidos = findViewById(R.id.tv_subidospedidos);
        tv_aviso = findViewById(R.id.tv_aviso);
        tv_pedidosact = findViewById(R.id.tv_pedidosact);
        tvDocumentos = findViewById(R.id.tvDocumentos);


        conn = new AdminSQLiteOpenHelper(getApplicationContext(), "ke_android", null, 12);
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        cargarEnlace();

        Cursor cursorsupervisor = ke_android.rawQuery("SELECT superves FROM usuarios WHERE vendedor ='" + cod_usuario + "'", null);

        while (cursorsupervisor.moveToNext()) {
            nivelUsuario = cursorsupervisor.getString(0);
        }


        bt_sync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                varAuxError = false;
                varAux = 0;
                tv_aviso.setTextSize(12);
                tv_aviso.setTypeface(Typeface.DEFAULT_BOLD);
                tv_aviso.setText("Por favor, espere mientras los datos sincronizan. No abandone esta pantalla hasta que finalice el proceso");
                tv_aviso.setTextColor(Color.rgb(4, 98, 193));
                GetFechas();

                //estoy aplicando la misma sincronización para el coordinador que para el vendedor, por el tema del progressdialog.
                if (nivelUsuario.equals("1")) {
                    //sincronizacion del vendedor
                    progressDialog = new ProgressDialog(SincronizacionActivity.this);
                    progressDialog.setMax(100);
                    progressDialog.setMessage("Descargando datos...");
                    progressDialog.setTitle("Sincronización en proceso");
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    progressDialog.show();


                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                while (progressDialog.getProgress() <= progressDialog.getMax()) {
                                    Thread.sleep(200);

                                }

                            } catch (Exception exception) {
                                exception.printStackTrace();
                            }

                        }
                    }).start();
                    sincronizacionVendedor();

                } else if (nivelUsuario.equals("0")) {
                    //sincronizacion del vendedor
                    progressDialog = new ProgressDialog(SincronizacionActivity.this);
                    progressDialog.setMax(100);
                    progressDialog.setMessage("Descargando datos...");
                    progressDialog.setTitle("Sincronización en proceso");
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    progressDialog.show();

                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                while (progressDialog.getProgress() <= progressDialog.getMax()) {
                                    Thread.sleep(200);

                                }

                            } catch (Exception exception) {
                                exception.printStackTrace();
                            }

                        }
                    }).start();
                    sincronizacionVendedor();
                }
            }


        });

        bt_subir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SubirPedidos();
                cargarRecibo();
            }
        });

        bt_subirprecob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SubirPrecob();

            }
        });

    }

    //IMPORTANTE hay 2 funciones de AnalisisError debido a que vollie trabaja de forma asincrona y en ocasiones actualiza primero la fecha con un error antes de realizar todos los procesos
    //Funcion que evalua la variable auxiliar de error
    //En caso de ser False (No tener error), el textView de aviso indicara que todo salio en orden
    //En caso de ser True (Hay error), el textView de aviso indicara que hay un error y ademas de atualizara la fecha de ultima sinroonizacion a una fecha 0 para no aceder
    //a ciertos modulos como catalogo y pedidos
    private void AnalisisError() {
        System.out.println("La variable -> " + varAuxError);
        if (varAuxError) {

            SQLiteDatabase ke_android = conn.getWritableDatabase();
            try {

                ContentValues contenedor = new ContentValues();
                contenedor.put("ult_sinc", "0001-01-01");

                ke_android.beginTransaction();

                ke_android.update("usuarios", contenedor, "vendedor = ?", new String[]{cod_usuario});
                //ke_android.rawQuery("UPDATE usuarios SET ult_sinc = '0001-01-01' WHERE vendedor = '" + cod_usuario + "'", null);
                //System.out.println("LLEGUE " + varAuxError);
                //System.out.println("UPDATE usuarios SET ult_sinc = '0001-01-01' WHERE vendedor = '" + cod_usuario + "'");

                ke_android.setTransactionSuccessful();

            } catch (Exception exception) {
                exception.printStackTrace();
                //System.out.println("NO LLEGUE");
            } finally {
                ke_android.endTransaction();
                //ke_android.close();
            }

            tv_aviso.setTextColor(Color.rgb(232, 17, 35));
            tv_aviso.setTextSize(14);
            tv_aviso.setTypeface(Typeface.DEFAULT_BOLD);
            tv_aviso.setText("Se han detectado inconvenientes en la conexión, Por favor sincronice nuevamente");

            Toast.makeText(this, "Por favor sincronice nuevamente , se detectaron errores en la conexión", LENGTH_LONG).show();

        } else {

            tv_aviso.setTextColor(Color.rgb(62, 197, 58));
            tv_aviso.setTextSize(15);
            tv_aviso.setTypeface(Typeface.DEFAULT_BOLD);
            tv_aviso.setText("Parametros al día");
            progressDialog.incrementProgressBy(100);

            Toast.makeText(this, "Parametros al día", LENGTH_LONG).show();

        }

        varAux++;
    }

    //Segunda funcion que actua en todas las oportunidades de error de los llamados de la API
    //Su utilidad radica en la actualizacio de la ultima sincronizacion en caso de error con fecha 0
    private void AnalisisError2() {

        //System.out.println("AAAAAAAAAAAAAAAAAAAAAA");
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        try {

            ContentValues contenedor = new ContentValues();
            contenedor.put("ult_sinc", "0001-01-01");

            ke_android.beginTransaction();

            ke_android.update("usuarios", contenedor, "vendedor = ?", new String[]{cod_usuario});
            //ke_android.rawQuery("UPDATE usuarios SET ult_sinc = '0001-01-01' WHERE vendedor = '" + cod_usuario + "'", null);
            //System.out.println("LLEGUE " + varAuxError);
            //System.out.println("UPDATE usuarios SET ult_sinc = '0001-01-01' WHERE vendedor = '" + cod_usuario + "'");

            ke_android.setTransactionSuccessful();

        } catch (Exception exception) {
            exception.printStackTrace();
            //System.out.println("NO LLEGUE");
        } finally {
            ke_android.endTransaction();
            //ke_android.close();
        }

        tv_aviso.setTextColor(Color.rgb(232, 17, 35));
        tv_aviso.setTextSize(14);
        tv_aviso.setTypeface(Typeface.DEFAULT_BOLD);
        tv_aviso.setText("Se han detectado inconvenientes en la conexión\nPor favor sincronice nuevamente");
        progressDialog.incrementProgressBy(100);

        //Toast.makeText(this, "Por favor sincronice nuevamente , se detectaron errores en la conexión", LENGTH_LONG).show();


        varAux++;
    }

    private void cargarEnlace() {
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        String[] columnas = new String[]{"kee_nombre," + "kee_url," + "kee_sucursal"};
        Cursor cursor = ke_android.query("ke_enlace", columnas, "1", null, null, null, null);

        while (cursor.moveToNext()) {
            nombreEmpresa = cursor.getString(0);
            enlaceEmpresa = cursor.getString(1);
            codigoSucursal = cursor.getString(2);
        }

    }

    private void cargarRecibo() {


        conn = new AdminSQLiteOpenHelper(SincronizacionActivity.this, "ke_android", null, 10);
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        Cursor cursor = ke_android.rawQuery("SELECT kcx_nrorecibo, kcx_codcli, kcx_codven, kcx_fechamodifi, kcx_monto FROM ke_cxc WHERE kcx_status = '0'", null);

        arrayRec = new JSONArray();

        while (cursor.moveToNext()) {

            JSONObject objetoRecibo = new JSONObject();
            try {
                String kcx_nrorecibo = cursor.getString(0);
                String kcx_codcli = cursor.getString(1);
                String kcx_codven = cursor.getString(2);
                String kcx_fechamodifi = cursor.getString(3);
                int kcx_monto = cursor.getInt(4);
                //char kcx_status = "1";

                objetoRecibo.put("kcx_nrorecibo", kcx_nrorecibo);
                objetoRecibo.put("kcx_codcli", kcx_codcli);
                objetoRecibo.put("kcx_codven", kcx_codven);
                objetoRecibo.put("kcx_fechamodifi", kcx_fechamodifi);
                objetoRecibo.put("kcx_monto", kcx_monto);
                //objetoRecibo.put("kcx_status", '1');

                arrayRec.put(objetoRecibo);

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(SincronizacionActivity.this, "Error al cargar el recibo" + e, Toast.LENGTH_SHORT).show();
            }
        }

        JSONObject jsonObject = new JSONObject();
        try {

            jsonObject.put("Recibo", arrayRec);


        } catch (JSONException e) {
            e.printStackTrace();
            // Toast.makeText(SincronizacionActivity.this, "Error al cargar el recibo" + e, Toast.LENGTH_SHORT).show();
        }
        String jsonStrREC = jsonObject.toString();
        try {
            insertarRecibo(jsonStrREC);


        } catch (Exception exc) {
            exc.printStackTrace();
            // Toast.makeText(SincronizacionActivity.this, "Error al cargar el recibo" + exc, Toast.LENGTH_SHORT).show();

        }

    }

    public void insertarRecibo(final String jsonrec) {
        RequestQueue requestQueue = Volley.newRequestQueue(SincronizacionActivity.this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://" + enlaceEmpresa + "/" + ambienteJob + "/Recibos_2.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response.trim().equals("OK")) {
                    Toast.makeText(SincronizacionActivity.this, "Recibo(s) Subido", Toast.LENGTH_LONG).show();
                    cambiarEstadoRecibo();

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                // Toast.makeText(SincronizacionActivity.this, "Error en la subida de precobranza", Toast.LENGTH_SHORT).show();
            }
        }) {
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("jsonrec", jsonrec);
                params.put("agencia", codigoSucursal);
                return params;
            }
        };

        requestQueue.add(stringRequest);

    }

    private void cambiarEstadoRecibo() {

        SQLiteDatabase ke_android = conn.getWritableDatabase();
        //System.out.println(arrayRec);
        for (int i = 0; i < arrayRec.length(); i++) {
            try {
                JSONObject objetodeRecibo = arrayRec.getJSONObject(i);

                String codigoDelReciboEnArray = objetodeRecibo.getString("kcx_nrorecibo");
                ke_android.execSQL("UPDATE ke_cxc SET kcx_status = '1' WHERE kcx_nrorecibo = '" + codigoDelReciboEnArray + "'");

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    public void sincronizacionVendedor() {

        switch (varAux) {
            case 0:
                BajarUsuario("https://" + enlaceEmpresa + "/" + ambienteJob + "/usuarios_V3.php?cod_usuario=" + cod_usuario.trim() + "&&agencia=" + codigoSucursal.trim());//listo

                break;
            case 1:
                BajarVendedor("https://" + enlaceEmpresa + "/" + ambienteJob + "/listvend_V2.php?cod_usuario=" + cod_usuario.trim() + "&&agencia=" + codigoSucursal.trim());//listo

                break;
            case 2:
                BajarGrupos("https://" + enlaceEmpresa + "/" + ambienteJob + "/grupos_V3.php?fecha_sinc=" + fecha_sinc_grupos.trim() + "&&agencia=" + codigoSucursal.trim());//listo

                break;
            case 3:
                BajarConfig("https://" + enlaceEmpresa + "/" + ambienteJob + "/config_V3.php?agencia=" + codigoSucursal.trim());//listo

                break;
            case 4:
                BajarSubGrupos("https://" + enlaceEmpresa + "/" + ambienteJob + "/subgrupos_V2.php?fecha_sinc=" + fecha_sinc_subgrupos.trim() + "&&agencia=" + codigoSucursal.trim());//listo

                break;
            case 5:
                BajarSectores("https://" + enlaceEmpresa + "/" + ambienteJob + "/sectores_V2.php?fecha_sinc=" + fecha_sinc_sectores.trim() + "&&agencia=" + codigoSucursal.trim());//listo

                break;
            case 6:
                BajarSubSectores("https://" + enlaceEmpresa + "/" + ambienteJob + "/subsectores_V2.php?fecha_sinc=" + fecha_sinc_subsectores + "&&agencia=" + codigoSucursal.trim());//listo

                break;
            case 7:
                BajarClientes("https://" + enlaceEmpresa + "/" + ambienteJob + "/clientes_V4.php?cod_usuario=" + cod_usuario.trim() + "&&agencia=" + codigoSucursal.trim());//listo

                break;
            case 8:
                BajarInfoPedidos("https://" + enlaceEmpresa + "/" + ambienteJob + "/obtenerdatospedidos_V3.php?cod_usuario=" + cod_usuario.trim() + "&&agencia=" + codigoSucursal.trim());//listo

                break;
            case 9:
                BajarArticulos("https://" + enlaceEmpresa + "/" + ambienteJob + "/articulos_V26.php?fecha_sinc=" + fecha_sinc_articulo.trim() + "&&agencia=" + codigoSucursal.trim());//listo

                break;
            case 10:
                BajarKardex("https://" + enlaceEmpresa + "/" + ambienteJob + "/kardex_V2.php?fecha_sinc=" + fecha_sinc_articulo.trim() + "&&agencia=" + codigoSucursal.trim());//listo

                break;
            case 11:
                SubirLimite();
                break;
            case 12:
                actualizarLimites("https://" + enlaceEmpresa + "/" + ambienteJob + "/obtenerlimites_V3.php?cod_usuario=" + cod_usuario.trim() + "&&agencia=" + codigoSucursal.trim());
                break;
            case 13:
                BajarDocumentos("https://" + enlaceEmpresa + "/" + ambienteJob + "/planificador_V3.php?vendedor=" + cod_usuario.trim());
                break;
            case 14:
                BajarDatosExtra("https://" + enlaceEmpresa + "/" + ambienteJob + "/descarga_referencias.php?vendedor=" + cod_usuario.trim());
                break;
            case 15:
                BajarConfigExtra("https://" + enlaceEmpresa + "/" + ambienteJob + "/config_gen.php?vendedor=" + cod_usuario.trim() + "&fecha_sinc=" + fechaSincronizar("ke_wcnf_conf"));
                break;
            case 16:
                BajarBancos("https://" + enlaceEmpresa + "/webservice/bancos_V2.php?fecha_sinc=" + GetFechaBancos() + "&&agencia=" + codigoSucursal.trim());
                break;
            case 17:
                //IF que valida si hasta el momento no hay errores en la sincronizacion, en caso de haber no enviara la ultima sincronizacion
                if (!varAuxError) {
                    SubirSincronizacion();
                }
                //System.out.println(varAux);
                break;
            case 18:
                //Funcion que indica si el proceso de sincronizacion se hizo adecuadamente
                AnalisisError();

                break;
        }

        System.out.println(varAux);

    }

    private void BajarBancos(String URL) {
        SQLiteDatabase ke_android = conn.getWritableDatabase();

        System.out.println(URL);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URL, null, response -> {

            try {
                if (response.getString("status").equals("0")) {
                    JSONArray bancos = response.getJSONArray("banco");

                    for (int i = 0; i < bancos.length(); i++) {
                        JSONObject banco = bancos.getJSONObject(i);

                        String cuentanac = banco.getString("cuentanac");
                        String fechamodifi = banco.getString("fechamodifi");
                        String nombanco = banco.getString("nombanco");
                        String codbanco = banco.getString("codbanco");
                        String inactiva = banco.getString("inactiva");

                        ContentValues cv = new ContentValues();

                        cv.put("cuentanac", cuentanac);
                        cv.put("fechamodifi", fechamodifi);
                        cv.put("nombanco", nombanco);
                        cv.put("codbanco", codbanco);
                        cv.put("inactiva", inactiva);


                        Cursor qcodigoLocal = ke_android.rawQuery("SELECT count(codbanco) FROM listbanc WHERE codbanco = '" + codbanco + "';", null);
                        int codigoExistente = 0;
                        if (qcodigoLocal.moveToFirst()) {
                            codigoExistente = qcodigoLocal.getInt(0);
                        }
                        qcodigoLocal.close();

                        if (codigoExistente > 0) {
                            //System.out.println("UPDATE " + documento);
                            ke_android.update("listbanc", cv, "codbanco= ?", new String[]{codbanco});
                        } else if (codigoExistente == 0) {
                            //System.out.println("INSERT " + documento);
                            ke_android.insert("listbanc", null, cv);
                        }

                    }

                    try {

                        Calendar fecha_limites = Calendar.getInstance();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                        String fechaLimites = sdf.format(fecha_limites.getTime());

                        ContentValues cv = new ContentValues();
                        cv.put("fchhn_ultmod", fechaLimites);

                        ke_android.update("tabla_aux", cv, "tabla= ?", new String[]{"listbanc"});
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    varAux++;
                    sincronizacionVendedor();

                } else {
                    varAux++;
                    sincronizacionVendedor();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }, error -> {
            System.out.println("Error -->" + error);
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);

    }

    //TE QUEDASTE BAJANDO LA CONFIGURACION - DEBERIAS VER SI SE PUEDE ELIMINAR CONFIGS VIEJAS COMO CLIENTES Y DOCUMENTOS
    private void BajarConfigExtra(String URL) {
        SQLiteDatabase ke_android = conn.getWritableDatabase();

        System.out.println(URL);

        //Fecha tomada para ser coloada en tabla auxiliar en caso de dar un error
        //String fecha_error = ObtenerFechaPreError("fchhn_ultmod");

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URL, null, response -> {
            try {
                if (response.getString("status").equals("0")) {
                    JSONArray config = response.getJSONArray("config");

                    for (int i = 0; i < config.length(); i++) {

                        JSONObject configDatos = config.getJSONObject(i);

                        String cnfgValtxt = configDatos.getString("cnfg_valtxt");
                        String cnfgTtip = configDatos.getString("cnfg_ttip");
                        String cnfgValsino = configDatos.getString("cnfg_valsino");
                        String cnfgValnum = configDatos.getString("cnfg_valnum");
                        String cnfgLentxt = configDatos.getString("cnfg_lentxt");
                        String fechamodifi = configDatos.getString("fechamodifi");
                        String cnfgActiva = configDatos.getString("cnfg_activa");
                        String cnfgIdconfig = configDatos.getString("cnfg_idconfig");
                        String cnfgEtiq = configDatos.getString("cnfg_etiq");
                        String cnfgClase = configDatos.getString("cnfg_clase");
                        String cnfgTipo = configDatos.getString("cnfg_tipo");
                        String cnfgValfch = configDatos.getString("cnfg_valfch");
                        String username = configDatos.getString("username");

                        ContentValues cv = new ContentValues();

                        cv.put("cnfg_valtxt", cnfgValtxt);
                        cv.put("cnfg_ttip", cnfgTtip);
                        cv.put("cnfg_valsino", cnfgValsino);
                        cv.put("cnfg_valnum", cnfgValnum);
                        cv.put("cnfg_lentxt", cnfgLentxt);
                        cv.put("fechamodifi", fechamodifi);
                        cv.put("cnfg_activa", cnfgActiva);
                        cv.put("cnfg_idconfig", cnfgIdconfig);
                        cv.put("cnfg_etiq", cnfgEtiq);
                        cv.put("cnfg_clase", cnfgClase);
                        cv.put("cnfg_tipo", cnfgTipo);
                        cv.put("cnfg_valfch", cnfgValfch);
                        cv.put("username", username);

                        Cursor qcodigoLocal = ke_android.rawQuery("SELECT count(cnfg_valtxt) FROM ke_wcnf_conf WHERE cnfg_idconfig = '" + cnfgIdconfig + "';", null);
                        int codigoExistente = 0;
                        if (qcodigoLocal.moveToFirst()) {
                            codigoExistente = qcodigoLocal.getInt(0);
                        }
                        qcodigoLocal.close();

                        if (codigoExistente > 0) {
                            //System.out.println("UPDATE " + documento);
                            ke_android.update("ke_wcnf_conf", cv, "cnfg_idconfig= ?", new String[]{cnfgIdconfig});
                        } else if (codigoExistente == 0) {
                            //System.out.println("INSERT " + documento);
                            ke_android.insert("ke_wcnf_conf", null, cv);
                        }

                    }
                    try {

                        Calendar fecha_limites = Calendar.getInstance();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                        String fechaLimites = sdf.format(fecha_limites.getTime());

                        ContentValues cv = new ContentValues();
                        cv.put("fchhn_ultmod", fechaLimites);

                        ke_android.update("tabla_aux", cv, "tabla= ?", new String[]{"ke_wcnf_conf"});
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Ocurrio algo en Config 1", LENGTH_LONG).show();
                    }

                    varAux++;
                    sincronizacionVendedor();

                } else {
                    varAux++;
                    sincronizacionVendedor();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(this, "Ocurrio algo en Config 2", LENGTH_LONG).show();
            }
        }, error -> {
            System.out.println("Error -->" + error);
            Toast.makeText(this, "Ocurrio algo en Config 3", LENGTH_LONG).show();
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }

    private String fechaSincronizar(String tabla) {
        String resultado = "0001-01-01T01:01:01";
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        Cursor fecha_ultmod = ke_android.rawQuery("SELECT fchhn_ultmod FROM tabla_aux WHERE tabla = '" + tabla + "';", null);
        if (fecha_ultmod.moveToFirst()) {
            resultado = fecha_ultmod.getString(0);
        }
        fecha_ultmod.close();
        return resultado;

    }

    private void BajarDatosExtra(String URL) {
        System.out.println("Referencias -> " + URL);

        ArrayList<String> refNube = new ArrayList<>();

        SQLiteDatabase ke_android = conn.getWritableDatabase();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URL, null, response -> {
            try {
                if (!(response.getString("referencias").equals("null"))) {
                    int countRef = 0;
                    JSONArray referencias = response.getJSONArray("referencias");

                    for (int i = 0; i < referencias.length(); i++) {
                        JSONObject jsonObject = referencias.getJSONObject(i);

                        String bcoref = jsonObject.getString("bcoref");
                        String bcocod = jsonObject.getString("bcocod");

                        refNube.add(bcoref);

                        ContentValues qReferencias = new ContentValues();

                        qReferencias.put("bcoref", bcoref);
                        qReferencias.put("bcocod", bcocod);
                        qReferencias.put("tiporef", "banc");

                        Cursor qcodigoRef = ke_android.rawQuery("SELECT count(bcoref) FROM ke_referencias WHERE bcoref = '" + bcoref + "';", null);
                        int refExistente = 0;
                        if (qcodigoRef.moveToFirst()) {
                            refExistente = qcodigoRef.getInt(0);
                        }
                        qcodigoRef.close();

                        if (refExistente > 0) {
                            //System.out.println("UPDATE " + bcoref);
                            ke_android.update("ke_referencias", qReferencias, "bcoref= ?", new String[]{bcoref});
                        } else if (refExistente == 0) {
                            //System.out.println("INSERT " + bcoref);
                            ke_android.insert("ke_referencias", null, qReferencias);
                        }
                        countRef++;
                    }
                    //progressDialog.setMessage("Referencias:" + countRef);
                    eliminarDocViejos(refNube, ke_android, "ke_referencias", "bcoref");
                    varAux++;
                    sincronizacionVendedor();

                } else if (response.getString("referencias").equals("null")) {

                    //progressDialog.setMessage("Referencias: Sin actualización" );

                    varAux++;
                    sincronizacionVendedor();

                }
            } catch (Exception e) {
                System.out.println("Error Bajar Documento -> " + e);
                e.printStackTrace();
                Toast.makeText(SincronizacionActivity.this, "Ocurrio algo en Referencias", LENGTH_LONG).show();
            }
        }, error -> {
            Toast.makeText(SincronizacionActivity.this, "Ocurrio algo en Referencias 2", LENGTH_LONG).show();
            progressDialog.setMessage("Referencias: No ha logrado sincronizar");
            error.printStackTrace();
            varAux++;
            progressDialog.incrementProgressBy((int) numBarraProgreso);
            varAuxError = true;
            AnalisisError2();
            //-----
            sincronizacionVendedor();
        });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }

    private void BajarDocumentos(String URL) {
        System.out.println("Documentos -> " + URL);
        //final ArrayList<String> documentosBDD = arrayDocumento();

        ArrayList<String> documentosNube = new ArrayList<>();

        progressDialog.setMessage("Sincronizando Documentos");

        tvDocumentos.setTextColor(Color.rgb(41, 184, 214));
        tvDocumentos.setText("Documentos: Sincronizando");
        //Fecha tomada para ser coloada en tabla auxiliar en caso de dar un error
        //String fecha_error = ObtenerFechaPreError("limites");
        SQLiteDatabase ke_android = conn.getWritableDatabase();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (!(response.getString("documento").equals("null"))) {
                        int countDoc = 0;
                        JSONArray documentos = response.getJSONArray("documento");

                        for (int i = 0; i < documentos.length(); i++) {
                            JSONObject jsonObject = documentos.getJSONObject(i);


//!documentosBDD.contains(documento)

                            String agencia = jsonObject.getString("agencia");
                            String tipodoc = jsonObject.getString("tipodoc");
                            String codigoCliente = jsonObject.getString("codcliente");
                            String nombreCliente = jsonObject.getString("nombrecli");
                            String documento = jsonObject.getString("documento");
                            String tipodocv = jsonObject.getString("tipodocv");
                            Double contribesp = jsonObject.getDouble("contribesp");
                            String ruta_parme = jsonObject.getString("ruta_parme");
                            Double tipoprecio = jsonObject.getDouble("tipoprecio");
                            String emision = jsonObject.getString("emision");
                            String recepcion = jsonObject.getString("recepcion");
                            String vence = jsonObject.getString("vence");
                            Double diascred = jsonObject.getDouble("diascred");
                            String estatusdoc = jsonObject.getString("estatusdoc");
                            Double dtotneto = jsonObject.getDouble("dtotneto");
                            Double dtotimpuest = jsonObject.getDouble("dtotimpuest");
                            Double dtotalfinal = jsonObject.getDouble("dtotalfinal");
                            Double dtotpagos = jsonObject.getDouble("dtotpagos");
                            Double dtotdescuen = jsonObject.getDouble("dtotdescuen");
                            Double dFlete = jsonObject.getDouble("dFlete");
                            Double dtotdev = jsonObject.getDouble("dtotdev");
                            Double dvndmtototal = jsonObject.getDouble("dvndmtototal");
                            Double dretencion = jsonObject.getDouble("dretencion");
                            Double dretencioniva = jsonObject.getDouble("dretencioniva");
                            String vendedor = jsonObject.getString("vendedor");
                            String codcoord = jsonObject.getString("codcoord");
                            String fechamodifi = jsonObject.getString("fechamodifi");
                            String aceptadev = jsonObject.getString("aceptadev");
                            Double bsiva = jsonObject.getDouble("bsiva");
                            Double bsflete = jsonObject.getDouble("bsflete");
                            Double bsretencioniva = jsonObject.getDouble("bsretencioniva");
                            Double bsretencion = jsonObject.getDouble("bsretencion");
                            Double tasadoc = jsonObject.getDouble("tasadoc");
                            Double montodcto = jsonObject.getDouble("mtodcto");
                            String fechavencedcto = jsonObject.getString("fchvencedcto");
                            String tienedcto = jsonObject.getString("tienedcto");
                            Double cbsret = jsonObject.getDouble("cbsret");
                            Double cdret = jsonObject.getDouble("cdret");
                            Double cbsretiva = jsonObject.getDouble("cbsretiva");
                            Double cdretiva = jsonObject.getDouble("cdretiva");
                            Double cbsrparme = jsonObject.getDouble("cbsrparme");
                            Double cdrparme = jsonObject.getDouble("cdrparme");
                            Double bsmtoiva = jsonObject.getDouble("bsmtoiva");
                            Double bsmtofte = jsonObject.getDouble("bsmtofte");
                            Double cbsretflete = jsonObject.getDouble("cbsretflete");
                            Double cdretflete = jsonObject.getDouble("cdretflete");
                            Double retmun_mto = jsonObject.getDouble("retmun_mto");
                            int kti_negesp = jsonObject.getInt("kti_negesp");

                            documentosNube.add(documento);

                            ContentValues qDocumentosCab = new ContentValues();
                            qDocumentosCab.put("agencia", agencia);
                            qDocumentosCab.put("tipodoc", tipodoc);
                            qDocumentosCab.put("documento", documento);
                            qDocumentosCab.put("tipodocv", tipodocv);
                            qDocumentosCab.put("codcliente", codigoCliente);
                            qDocumentosCab.put("nombrecli", nombreCliente);
                            qDocumentosCab.put("contribesp", contribesp);
                            qDocumentosCab.put("ruta_parme", ruta_parme);
                            qDocumentosCab.put("tipoprecio", tipoprecio);
                            qDocumentosCab.put("emision", emision);
                            qDocumentosCab.put("recepcion", recepcion);
                            qDocumentosCab.put("vence", vence);
                            qDocumentosCab.put("diascred", diascred);
                            qDocumentosCab.put("estatusdoc", estatusdoc);
                            qDocumentosCab.put("dtotneto", dtotneto);
                            qDocumentosCab.put("dretencion", dretencion);
                            qDocumentosCab.put("dretencioniva", dretencioniva);
                            qDocumentosCab.put("dtotimpuest", dtotimpuest);
                            qDocumentosCab.put("dtotalfinal", dtotalfinal);
                            qDocumentosCab.put("dtotpagos", dtotpagos);
                            qDocumentosCab.put("dtotdescuen", dtotdescuen);
                            qDocumentosCab.put("dFlete", dFlete);
                            qDocumentosCab.put("dtotdev", dtotdev);
                            qDocumentosCab.put("dvndmtototal", dvndmtototal);
                            qDocumentosCab.put("vendedor", vendedor);
                            qDocumentosCab.put("codcoord", codcoord);
                            qDocumentosCab.put("fechamodifi", fechamodifi);
                            qDocumentosCab.put("aceptadev", aceptadev);
                            qDocumentosCab.put("bsiva", bsiva);
                            qDocumentosCab.put("bsflete", bsflete);
                            qDocumentosCab.put("bsretencion", bsretencion);
                            qDocumentosCab.put("bsretencioniva", bsretencioniva);
                            qDocumentosCab.put("tasadoc", tasadoc);
                            qDocumentosCab.put("mtodcto", montodcto);
                            qDocumentosCab.put("fchvencedcto", fechavencedcto);
                            qDocumentosCab.put("tienedcto", tienedcto);
                            qDocumentosCab.put("cbsret", cbsret);
                            qDocumentosCab.put("cdret", cdret);
                            qDocumentosCab.put("cbsretiva", cbsretiva);
                            qDocumentosCab.put("cdretiva", cdretiva);
                            qDocumentosCab.put("cbsrparme", cbsrparme);
                            qDocumentosCab.put("bsmtoiva", bsmtoiva);
                            qDocumentosCab.put("bsmtofte", bsmtofte);
                            qDocumentosCab.put("cbsretflete", cbsretflete);
                            qDocumentosCab.put("cdretflete", cdretflete);
                            qDocumentosCab.put("retmun_mto", retmun_mto);
                            qDocumentosCab.put("kti_negesp", kti_negesp);
                            qDocumentosCab.put("cdrparme", cdrparme);

                            Cursor qcodigoLocal = ke_android.rawQuery("SELECT count(documento) FROM ke_doccti WHERE documento = '" + documento + "';", null);
                            int codigoExistente = 0;
                            if (qcodigoLocal.moveToFirst()) {
                                codigoExistente = qcodigoLocal.getInt(0);
                            }
                            qcodigoLocal.close();

                            if (codigoExistente > 0) {
                                //System.out.println("UPDATE " + documento);
                                ke_android.update("ke_doccti", qDocumentosCab, "documento= ?", new String[]{documento});
                            } else if (codigoExistente == 0) {
                                //System.out.println("INSERT " + documento);
                                ke_android.insert("ke_doccti", null, qDocumentosCab);
                            }
                            countDoc++;


                        }
                        ke_android.delete("ke_doccti", "estatusdoc= ?", new String[]{("2")});
                        eliminarDocViejos(documentosNube, ke_android, "ke_doccti", "documento");
                        tvDocumentos.setTextColor(Color.rgb(62, 197, 58));
                        tvDocumentos.setText("Documentos: " + countDoc);
                        progressDialog.setMessage("Documentos:" + countDoc);
                        varAux++;
                        progressDialog.incrementProgressBy((int) numBarraProgreso);
                        sincronizacionVendedor();

                    } else if (response.getString("documento").equals("null")) {

                        tvDocumentos.setTextColor(Color.rgb(98, 117, 141));
                        tvDocumentos.setText("Documentos: Sin actualización");
                        progressDialog.setMessage("Documentos: Sin actualización");
                        varAux++;
                        progressDialog.incrementProgressBy((int) numBarraProgreso);
                        sincronizacionVendedor();

                    }
                } catch (Exception e) {
                    //System.out.println("Error Bajar Documento -> " + e);
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //System.out.println("Este es el error -> "+error);
                //Ingreso de la fecha antes de ser actualizada
                //ActualizarFechaError(fecha_error);

                //--Manejo visual que indica al usuario del error--
                tvDocumentos.setTextColor(Color.rgb(232, 17, 35));
                tvDocumentos.setText("Documentos: No ha logrado sincronizar");
                progressDialog.setMessage("Documentos: No ha logrado sincronizar");
                varAux++;
                progressDialog.incrementProgressBy((int) numBarraProgreso);
                varAuxError = true;
                AnalisisError2();
                //-----
                sincronizacionVendedor();
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }

    private void eliminarDocViejos(ArrayList<String> documentosNube, SQLiteDatabase ke_android, String tabla, String campo) {
        ArrayList<String> documentosBDD = arrayDocumento(tabla, campo);

        for (int i = 0; i < documentosBDD.size(); i++) {
            if (!documentosNube.contains(documentosBDD.get(i))) {
                //System.out.println("DELETE " + documentosBDD.get(i));
                ke_android.delete(tabla, campo + " = ?", new String[]{documentosBDD.get(i)});
            }
        }

    }

    private ArrayList<String> arrayDocumento(String tabla, String campo) {
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        ArrayList<String> documentos = new ArrayList<>();
        Cursor cursor = ke_android.rawQuery("SELECT " + campo + " FROM " + tabla + ";", null);

        while (cursor.moveToNext()) {
            documentos.add(cursor.getString(0));
        }

        cursor.close();

        return documentos;
    }

    private void actualizarLimites(String URL) {
        //Fecha tomada para ser coloada en tabla auxiliar en caso de dar un error
        String fecha_error = ObtenerFechaPreError("limites");


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(JSONObject response) {

                try {
                    if (!(response.getString("limites").equals("null"))) {

                        conn = new AdminSQLiteOpenHelper(getApplicationContext(), "ke_android", null, 8);
                        SQLiteDatabase ke_android = conn.getWritableDatabase();
                        long filas = DatabaseUtils.queryNumEntries(ke_android, "ke_limitart"); //obtenemos las filas de la tabla articulos para comprobar si hay o no registros

                        JSONArray limites = response.getJSONArray("limites");

                        if (filas > 0) {
                            JSONObject jsonObject = null; //creamos un objeto json vacio
                            for (int i = 0; i < limites.length(); i++) {

                                try {

                                    ke_android.beginTransaction();

                                    jsonObject = limites.getJSONObject(i);
                                    ltrack = jsonObject.getString("kli_track").trim();
                                    lvendedor = jsonObject.getString("kli_codven").trim();
                                    lcliente = jsonObject.getString("kli_codcli").trim();
                                    larticulo = jsonObject.getString("kli_codart").trim();
                                    lcantidad = jsonObject.getInt("kli_cant");
                                    lfhizo = jsonObject.getString("kli_fechahizo").trim();
                                    lfvence = jsonObject.getString("kli_fechavence").trim();

                                    ContentValues actualizar = new ContentValues();
                                    actualizar.put("kli_track", ltrack);
                                    actualizar.put("kli_codven", lvendedor);
                                    actualizar.put("kli_codcli", lcliente);
                                    actualizar.put("kli_codart", larticulo);
                                    actualizar.put("kli_cant", lcantidad);
                                    actualizar.put("kli_fechahizo", lfhizo);
                                    actualizar.put("kli_fechavence", lfvence);
                                    actualizar.put("status", "1");

                                    LocalDateTime hoy = LocalDateTime.now();
                                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");
                                    LocalDateTime validar = hoy.minusDays(7);
                                    String fechaVal = validar.format(formatter);


                                    // ke_android.update("ke_limitart", actualizar, "kli_fechahizo > ?", new String[]{fechaVal});

                                    //actualizamos la fecha de la tabla de
                                    Calendar fecha_limites = Calendar.getInstance();
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                    String fechaLimites = sdf.format(fecha_limites.getTime());

                                    ContentValues actualizarFecha = new ContentValues();
                                    actualizarFecha.put("fchhn_ultmod", fechaLimites);
                                    ke_android.update("tabla_aux", actualizarFecha, "tabla = ?", new String[]{"limites"});

                                    ke_android.setTransactionSuccessful();


                                } catch (Exception exception) {
                                    exception.printStackTrace();
                                } finally {
                                    ke_android.endTransaction();
                                }

                                Cursor codigo_en_local = ke_android.rawQuery("SELECT count(kli_track), count(kli_codart) FROM ke_limitart WHERE kli_codart = '" + larticulo + "' AND kli_track ='" + ltrack + "'", null);
                                codigo_en_local.moveToFirst();
                                int track_existente = codigo_en_local.getInt(0);
                                int codigo_existente = codigo_en_local.getInt(1);

                                if (track_existente > 0 && codigo_existente > 0) {
                                    try {

                                        ke_android.beginTransaction();

                                        jsonObject = limites.getJSONObject(i);
                                        ltrack = jsonObject.getString("kli_track").trim();
                                        lvendedor = jsonObject.getString("kli_codven").trim();
                                        lcliente = jsonObject.getString("kli_codcli").trim();
                                        larticulo = jsonObject.getString("kli_codart").trim();
                                        lcantidad = jsonObject.getInt("kli_cant");
                                        lfhizo = jsonObject.getString("kli_fechahizo").trim();
                                        lfvence = jsonObject.getString("kli_fechavence").trim();

                                        ContentValues actualizar = new ContentValues();
                                        actualizar.put("kli_track", ltrack);
                                        actualizar.put("kli_codven", lvendedor);
                                        actualizar.put("kli_codcli", lcliente);
                                        actualizar.put("kli_codart", larticulo);
                                        actualizar.put("kli_cant", lcantidad);
                                        actualizar.put("kli_fechahizo", lfhizo);
                                        actualizar.put("kli_fechavence", lfvence);
                                        actualizar.put("status", "1");

                                        LocalDateTime hoy = LocalDateTime.now();
                                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");
                                        LocalDateTime validar = hoy.minusDays(7);
                                        String fechaVal = validar.format(formatter);


                                        //ke_android.update("ke_limitart", actualizar, "kli_fechahizo > ?", new String[]{fechaVal});

                                        //actualizamos la fecha de la tabla de
                                        Calendar fecha_limites = Calendar.getInstance();
                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                        String fechaLimites = sdf.format(fecha_limites.getTime());

                                        ContentValues actualizarFecha = new ContentValues();
                                        actualizarFecha.put("fchhn_ultmod", fechaLimites);
                                        //ke_android.update("tabla_aux", actualizarFecha, "tabla = ?", new String[]{"limites"});

                                        ke_android.setTransactionSuccessful();


                                    } catch (Exception exception) {
                                        exception.printStackTrace();
                                    } finally {
                                        ke_android.endTransaction();
                                    }

                                } else {
                                    try {

                                        ke_android.beginTransaction();

                                        jsonObject = limites.getJSONObject(i);
                                        ltrack = jsonObject.getString("kli_track").trim();
                                        lvendedor = jsonObject.getString("kli_codven").trim();
                                        lcliente = jsonObject.getString("kli_codcli").trim();
                                        larticulo = jsonObject.getString("kli_codart").trim();
                                        lcantidad = jsonObject.getInt("kli_cant");
                                        lfhizo = jsonObject.getString("kli_fechahizo").trim();
                                        lfvence = jsonObject.getString("kli_fechavence").trim();

                                        ContentValues insertar = new ContentValues();
                                        insertar.put("kli_track", ltrack);
                                        insertar.put("kli_codven", lvendedor);
                                        insertar.put("kli_codcli", lcliente);
                                        insertar.put("kli_codart", larticulo);
                                        insertar.put("kli_cant", lcantidad);
                                        insertar.put("kli_fechahizo", lfhizo);
                                        insertar.put("kli_fechavence", lfvence);
                                        insertar.put("status", "1");

                                        LocalDateTime hoy = LocalDateTime.now();
                                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");
                                        LocalDateTime validar = hoy.minusDays(7);
                                        String fechaVal = validar.format(formatter);

                                        //INSERCION DE LOS REGISTROS
                                        ke_android.insert("ke_limitart", null, insertar);

                                        //actualizamos la fecha de la tabla de
                                        Calendar fecha_limites = Calendar.getInstance();
                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                        String fechaLimites = sdf.format(fecha_limites.getTime());

                                        ContentValues actualizarFecha = new ContentValues();
                                        actualizarFecha.put("fchhn_ultmod", fechaLimites);
                                        ke_android.update("tabla_aux", actualizarFecha, "tabla = ?", new String[]{"limites"});

                                        ke_android.setTransactionSuccessful();


                                    } catch (Exception exception) {
                                        exception.printStackTrace();
                                    } finally {
                                        ke_android.endTransaction();
                                    }
                                }

                            }
                            varAux++;
                            progressDialog.incrementProgressBy((int) numBarraProgreso);
                            sincronizacionVendedor();


                        } else {
                            ke_android = conn.getWritableDatabase();
                            JSONObject jsonObject = null; //creamos un objeto json vacio
                            for (int i = 0; i < limites.length(); i++) {
                                try {

                                    ke_android.beginTransaction();
                                    jsonObject = limites.getJSONObject(i);

                                    ltrack = jsonObject.getString("kli_track").trim();
                                    lvendedor = jsonObject.getString("kli_codven").trim();
                                    lcliente = jsonObject.getString("kli_codcli").trim();
                                    larticulo = jsonObject.getString("kli_codart").trim();
                                    lcantidad = jsonObject.getInt("kli_cant");
                                    lfhizo = jsonObject.getString("kli_fechahizo").trim();
                                    lfvence = jsonObject.getString("kli_fechavence").trim();

                                    ContentValues insertar = new ContentValues();
                                    insertar.put("kli_track", ltrack);
                                    insertar.put("kli_codven", lvendedor);
                                    insertar.put("kli_codcli", lcliente);
                                    insertar.put("kli_codart", larticulo);
                                    insertar.put("kli_cant", lcantidad);
                                    insertar.put("kli_fechahizo", lfhizo);
                                    insertar.put("kli_fechavence", lfvence);
                                    insertar.put("status", "1");

                                    LocalDateTime hoy = LocalDateTime.now();
                                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");
                                    LocalDateTime validar = hoy.minusDays(7);
                                    String fechaVal = validar.format(formatter);


                                    ke_android.insert("ke_limitart", null, insertar);

                                    //actualizamos la fecha de la tabla de
                                    Calendar fecha_limites = Calendar.getInstance();
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                    String fechaLimites = sdf.format(fecha_limites.getTime());

                                    ContentValues actualizarFecha = new ContentValues();
                                    actualizarFecha.put("fchhn_ultmod", fechaLimites);
                                    ke_android.update("tabla_aux", actualizarFecha, "tabla = ?", new String[]{"limites"});

                                    ke_android.setTransactionSuccessful();
                                } catch (JSONException e) {
                                    Toast.makeText(SincronizacionActivity.this, "Error 1", Toast.LENGTH_SHORT).show();

                                } finally {
                                    ke_android.endTransaction();
                                }
                            }
                            varAux++;
                            progressDialog.incrementProgressBy((int) numBarraProgreso);
                            sincronizacionVendedor();

                        }

                    } else if (response.getString("limites").equals("null")) {
                        varAux++;
                        progressDialog.incrementProgressBy((int) numBarraProgreso);
                        sincronizacionVendedor();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Ingreso de la fecha antes de ser actualizada
                ActualizarFechaError(fecha_error);

                //--Manejo visual que indica al usuario del error--
                varAux++;
                progressDialog.incrementProgressBy((int) numBarraProgreso);
                //varAuxError = true;
                //AnalisisError2();
                //------
                sincronizacionVendedor();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parametros = new HashMap<String, String>();
                parametros.put("cod_usuario", cod_usuario);
                return parametros;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);


    }

    private void SubirLimite() {
        SQLiteDatabase ke_android = conn.getWritableDatabase();

        cursorLim = ke_android.rawQuery("SELECT " + "kli_track, " + "kli_codven, " + "kli_codcli, " + "kli_codart, " + "kli_cant, " + "kli_fechahizo, " + "kli_fechavence " + "FROM ke_limitart" + " WHERE status = '1'  " + "AND kli_fechahizo >'" + fecha_sinc_limites + "'" + "AND kli_codven = '" + cod_usuario.trim() + "'", null);

        if (cursorLim.moveToFirst()) {
            cargarLimites();
        } else {
            //Toast.makeText(SincronizacionActivity.this, "Parametros al día", Toast.LENGTH_SHORT).show();
            varAux++;
            progressDialog.incrementProgressBy((int) numBarraProgreso);
            sincronizacionVendedor();
        }
    }

    public void sincronizacionCoordinador() {
        tv_aviso.setText("Por favor, espere mientras los datos sincronizan. No abandone esta pantalla hasta que finalice el proceso");
        GetFechas();
        BajarVendedor("https://" + enlaceEmpresa + "/" + ambienteJob + "/listvend.php?cod_usuario=" + cod_usuario.trim() + "&&agencia=" + codigoSucursal.trim());
        BajarArticulos("https://" + enlaceEmpresa + "/" + ambienteJob + "/articulos.php?fecha_sinc=" + fecha_sinc_articulo.trim() + "&&agencia=" + codigoSucursal.trim());
        BajarKardex("https://" + enlaceEmpresa + "/" + ambienteJob + "/kardex.php?fecha_sinc=" + fecha_sinc_articulo.trim() + "&&agencia=" + codigoSucursal.trim());
    }

    public void BajarInfoPedidos(String URL) {
        System.out.println(URL);
        tv_pedidosact.setTextColor(Color.rgb(41, 184, 214));
        tv_pedidosact.setText("Pedidos Act.: Sincronizando.");
        contadorpedidosactualizados = 0;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) { //a traves de un json array request, traemos la informacion que viene del webservice
                //System.out.println("Rspuestaaaaaaa ->" + response);
                try {
                    if (!(response.getString("pedidos").equals("null"))) { // si la respuesta no viene vacia
                        //System.out.println("NO VINO NULA");
                        conn = new AdminSQLiteOpenHelper(getApplicationContext(), "ke_android", null, 1);
                        SQLiteDatabase ke_android = conn.getWritableDatabase();
                        long filas = DatabaseUtils.queryNumEntries(ke_android, "ke_opti"); //obtenemos las filas de la tabla articulos para comprobar si hay o no registros

                        JSONArray pedidos = response.getJSONArray("pedidos");

                        //aqui valido las filas de la tabla de articulos en el telefono
                        if (filas > 0) {
                            JSONObject jsonObject = null; //creamos un objeto json vacio
                            for (int i = 0; i < pedidos.length(); i++) { /*pongo todo en el objeto segun lo que venga */
                                try {

                                    ke_android.beginTransaction();

                                    jsonObject = pedidos.getJSONObject(i);
                                    nropedido = jsonObject.getString("kti_nroped").trim();
                                    fechamodifi = jsonObject.getString("fechamodifi").trim();
                                    numinterno = jsonObject.getString("kti_ndoc").trim();
                                    kti_status = jsonObject.getString("kti_status").trim();
                                    ke_pedstatus = jsonObject.getString("ke_pedstatus").trim();
                                    //System.out.println(nropedido);

                                    ContentValues actualizar = new ContentValues();
                                    actualizar.put("kti_nroped", nropedido);
                                    actualizar.put("fechamodifi", fechamodifi);
                                    actualizar.put("kti_status", kti_status);
                                    actualizar.put("ke_pedstatus", ke_pedstatus);

                                    ke_android.update("ke_opti", actualizar, "kti_ndoc = ?", new String[]{numinterno});
                                    ke_android.setTransactionSuccessful();
                                    contadorpedidosactualizados++;

                                } catch (JSONException e) {
                                    Toast.makeText(SincronizacionActivity.this, "Error 2", LENGTH_LONG).show();
                                } finally {
                                    ke_android.endTransaction();
                                }
                            }
                            ke_android.close();
                            tv_pedidosact.setTextColor(Color.rgb(62, 197, 58));
                            tv_pedidosact.setText("Pedidos Act: " + contadorpedidosactualizados);
                            varAux++;
                            progressDialog.incrementProgressBy((int) numBarraProgreso);
                            progressDialog.setMessage("Pedidos act." + contadorpedidosactualizados);
                            sincronizacionVendedor();

                        } else {
                            tv_pedidosact.setTextColor(Color.rgb(98, 117, 141));
                            tv_pedidosact.setText("Pedidos Act: Sin actualización");
                            progressDialog.setMessage("Pedidos Act: Sin actualización");
                            varAux++;
                            progressDialog.incrementProgressBy((int) numBarraProgreso);
                            sincronizacionVendedor();
                            // Toast.makeText(SincronizacionActivity.this,"Nada en el if del long", LENGTH_LONG).show();
                        }
                    } else if (response.getString("pedidos").equals("null")) {
                        tv_pedidosact.setTextColor(Color.rgb(98, 117, 141));
                        tv_pedidosact.setText("Pedidos Act: Sin actualización");
                        progressDialog.setMessage("Pedidos Act: Sin actualización");
                        varAux++;
                        progressDialog.incrementProgressBy((int) numBarraProgreso);
                        sincronizacionVendedor();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //--Manejo visual que indica al usuario del error--
                tv_pedidosact.setTextColor(Color.rgb(232, 17, 35));
                tv_pedidosact.setText("Pedidos Act: No ha logrado sincronizar");
                progressDialog.setMessage("Pedidos No ha logrado sincronizar");
                varAux++;
                progressDialog.incrementProgressBy((int) numBarraProgreso);
                //varAuxError = true;
                //AnalisisError2();
                //------
                sincronizacionVendedor();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {  //finalmente, estos son los parametros que le enviaremos al webservice, partiendo de las variables
                //donde estan guardados las fechas
                Map<String, String> parametros = new HashMap<String, String>();
                // parametros.put("fecha_sinc", fecha_sinc);

                return parametros;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest); //esto es el request que se envia al url a traves de la conexion volley, (el stringrequest esta armado arriba)


    }

    private void BajarUsuario(String URL) {
        System.out.println("Usuario ->" + URL);
        //Fecha tomada para ser coloada en tabla auxiliar en caso de dar un error
        String fecha_error = ObtenerFechaPreError("usuarios");

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(JSONObject response) { //a traves de un json array request, traemos la informacion que viene del webservice

                try {
                    if (!(response.getString("usuario").equals("null"))) { // si la respuesta no viene vacia

                        conn = new AdminSQLiteOpenHelper(getApplicationContext(), "ke_android", null, 1);
                        SQLiteDatabase ke_android = conn.getWritableDatabase();
                        long filas = DatabaseUtils.queryNumEntries(ke_android, "usuarios"); //obtenemos las filas de la tabla articulos para comprobar si hay o no registros

                        JSONArray usuario = response.getJSONArray("usuario");

                        //aqui valido las filas de la tabla de articulos en el telefono
                        if (filas > 0) {
                            JSONObject jsonObject = null; //creamos un objeto json vacio
                            for (int i = 0; i < usuario.length(); i++) { /*pongo todo en el objeto segun lo que venga */
                                try {

                                    ke_android.beginTransaction();

                                    jsonObject = usuario.getJSONObject(i);
                                    nombre = jsonObject.getString("nombre").trim();
                                    username = jsonObject.getString("username").trim();
                                    password = jsonObject.getString("password").trim();
                                    vendedor = jsonObject.getString("vendedor").trim();
                                    almacen = jsonObject.getString("almacen").trim();
                                    desactivo = jsonObject.getDouble("desactivo");
                                    fechamodifi = jsonObject.getString("fechamodifi").trim();
                                    ualterprec = jsonObject.getDouble("ualterprec");


                                    ContentValues actualizar = new ContentValues();
                                    actualizar.put("nombre", nombre);
                                    actualizar.put("username", username);
                                    actualizar.put("password", password);
                                    actualizar.put("vendedor", vendedor);
                                    actualizar.put("almacen", almacen);
                                    actualizar.put("desactivo", desactivo);
                                    actualizar.put("fechamodifi", fechamodifi);
                                    actualizar.put("ualterprec", ualterprec);

                                    ke_android.update("usuarios", actualizar, null, null);


                                    //actualizamos la fecha de la tabla de
                                    Calendar fecha_usuarios = Calendar.getInstance();
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                    String fechausuarios = sdf.format(fecha_usuarios.getTime());

                                    ContentValues actualizarFecha = new ContentValues();
                                    actualizarFecha.put("fchhn_ultmod", fechausuarios);

                                    ke_android.setTransactionSuccessful();

                                } catch (JSONException e) {
                                    Toast.makeText(getApplicationContext(), "Error 3", LENGTH_LONG).show();
                                } finally {
                                    ke_android.endTransaction();
                                }

                                Cursor codigo_en_local = ke_android.rawQuery("SELECT count(nombre) FROM usuarios WHERE vendedor = '" + vendedor + "'", null);
                                codigo_en_local.moveToFirst();
                                int codigo_existente = codigo_en_local.getInt(0);


                                if (codigo_existente > 0) {

                                    try {

                                        ke_android.beginTransaction();

                                        jsonObject = usuario.getJSONObject(i);
                                        nombre = jsonObject.getString("nombre").trim();
                                        username = jsonObject.getString("username").trim();
                                        password = jsonObject.getString("password").trim();
                                        vendedor = jsonObject.getString("vendedor").trim();
                                        almacen = jsonObject.getString("almacen").trim();
                                        desactivo = jsonObject.getDouble("desactivo");
                                        fechamodifi = jsonObject.getString("fechamodifi").trim();
                                        ualterprec = jsonObject.getDouble("ualterprec");


                                        ContentValues actualizar = new ContentValues();
                                        actualizar.put("nombre", nombre);
                                        actualizar.put("username", username);
                                        actualizar.put("password", password);
                                        actualizar.put("vendedor", vendedor);
                                        actualizar.put("almacen", almacen);
                                        actualizar.put("desactivo", desactivo);
                                        actualizar.put("fechamodifi", fechamodifi);
                                        actualizar.put("ualterprec", ualterprec);

                                        ke_android.update("usuarios", actualizar, null, null);

                                        //actualizamos la fecha de la tabla de
                                        Calendar fecha_usuarios = Calendar.getInstance();
                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                        String fechausuarios = sdf.format(fecha_usuarios.getTime());

                                        ContentValues actualizarFecha = new ContentValues();
                                        actualizarFecha.put("fchhn_ultmod", fechausuarios);


                                        ke_android.setTransactionSuccessful();

                                    } catch (JSONException e) {
                                        Toast.makeText(getApplicationContext(), "Error 4", LENGTH_LONG).show();
                                    } finally {
                                        ke_android.endTransaction();
                                    }


                                } else {
                                    try {
                                        ke_android.beginTransaction();

                                        jsonObject = usuario.getJSONObject(i);
                                        nombre = jsonObject.getString("nombre").trim();
                                        username = jsonObject.getString("username").trim();
                                        password = jsonObject.getString("password").trim();
                                        vendedor = jsonObject.getString("vendedor").trim();
                                        almacen = jsonObject.getString("almacen").trim();
                                        desactivo = jsonObject.getDouble("desactivo");
                                        fechamodifi = jsonObject.getString("fechamodifi").trim();
                                        ualterprec = jsonObject.getDouble("ualterprec");

                                        ContentValues insertar = new ContentValues();
                                        insertar.put("nombre", nombre);
                                        insertar.put("username", username);
                                        insertar.put("password", password);
                                        insertar.put("vendedor", vendedor);
                                        insertar.put("almacen", almacen);
                                        insertar.put("desactivo", desactivo);
                                        insertar.put("fechamodifi", fechamodifi);
                                        insertar.put("ualterprec", ualterprec);


                                        ke_android.insert("usuarios", null, insertar);

                                        //actualizamos la fecha de la tabla de
                                        Calendar fecha_usuarios = Calendar.getInstance();
                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                        String fechausuarios = sdf.format(fecha_usuarios.getTime());

                                        ContentValues actualizarFecha = new ContentValues();
                                        actualizarFecha.put("fchhn_ultmod", fechausuarios);
                                        ke_android.update("tabla_aux", actualizarFecha, "tabla = 'usuarios'", null);

                                        ke_android.setTransactionSuccessful();

                                    } catch (JSONException e) {
                                        Toast.makeText(getApplicationContext(), "Error 5", LENGTH_LONG).show();
                                    } finally {
                                        ke_android.endTransaction();
                                    }
                                }

                            }
                            ke_android.close();

                            // tv_estadosync.setTextColor(Color.rgb(62,197,58));
                            //   tv_estadosync.setText("Subsectores Sincronizado");
                            progressDialog.setMessage("Usuario actualizado");
                            varAux++;
                            progressDialog.incrementProgressBy((int) numBarraProgreso);
                            sincronizacionVendedor();

                        } else {

                            //si no hay nada, hago un insert
                            AdminSQLiteOpenHelper conn = new AdminSQLiteOpenHelper(getApplicationContext(), "ke_android", null, 1);
                            ke_android = conn.getWritableDatabase();


                            JSONObject jsonObject = null; //creamos un objeto json vacio
                            for (int i = 0; i < usuario.length(); i++) { /*pongo todo en el objeto segun lo que venga */
                                try {

                                    ke_android.beginTransaction();

                                    jsonObject = usuario.getJSONObject(i);
                                    nombre = jsonObject.getString("nombre").trim();
                                    username = jsonObject.getString("username").trim();
                                    password = jsonObject.getString("password").trim();
                                    vendedor = jsonObject.getString("vendedor").trim();
                                    almacen = jsonObject.getString("almacen").trim();
                                    desactivo = jsonObject.getDouble("desactivo");
                                    fechamodifi = jsonObject.getString("fechamodifi").trim();
                                    ualterprec = jsonObject.getDouble("ualterprec");


                                    ContentValues insertar = new ContentValues();
                                    insertar.put("nombre", nombre);
                                    insertar.put("username", username);
                                    insertar.put("password", password);
                                    insertar.put("vendedor", vendedor);
                                    insertar.put("almacen", almacen);
                                    insertar.put("desactivo", desactivo);
                                    insertar.put("fechamodifi", fechamodifi);
                                    insertar.put("ualterprec", ualterprec);

                                    ke_android.insert("usuarios", null, insertar);

                                    //actualizamos la fecha de la tabla de
                                    Calendar fecha_usuarios = Calendar.getInstance();
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                    String fechausuarios = sdf.format(fecha_usuarios.getTime());

                                    ContentValues actualizarFecha = new ContentValues();
                                    actualizarFecha.put("fchhn_ultmod", fechausuarios);
                                    ke_android.update("tabla_aux", actualizarFecha, "tabla = 'usuarios'", null);

                                    ke_android.setTransactionSuccessful();

                                } catch (JSONException e) {
                                    Toast.makeText(getApplicationContext(), "Error 6", LENGTH_LONG).show();
                                } finally {
                                    ke_android.endTransaction();
                                }


                            }
                            //    Toast.makeText(PrincipalActivity.this, "Subsectores descargados", Toast.LENGTH_SHORT).show();

                            ke_android.close();


                            progressDialog.setMessage("Usuario: actualizado.");
                            varAux++;
                            progressDialog.incrementProgressBy((int) numBarraProgreso);
                            sincronizacionVendedor();
                        }
                    } else if (response.getString("usuario").equals("null")) {
                        progressDialog.setMessage("Usuario: sin actualizar.");
                        varAux++;
                        progressDialog.incrementProgressBy((int) numBarraProgreso);
                        sincronizacionVendedor();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Ingreso de la fecha antes de ser actualizada
                ActualizarFechaError(fecha_error);
                //--Manejo visual que indica al usuario del error--
                progressDialog.setMessage("Usuario: No ha logrado sincronizar.");
                varAux++;
                progressDialog.incrementProgressBy((int) numBarraProgreso);
                //varAuxError = true;
                //AnalisisError2();
                //------
                sincronizacionVendedor();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {  //finalmente, estos son los parametros que le enviaremos al webservice, partiendo de las variables
                //donde estan guardados las fechas
                Map<String, String> parametros = new HashMap<String, String>();
                // parametros.put("fecha_sinc", fecha_sinc);

                return parametros;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest); //esto es el request que se envia al url a traves de la conexion volley, (el stringrequest esta armado arriba)


    }

    private void BajarConfig(String URL) {
        //Fecha tomada para ser coloada en tabla auxiliar en caso de dar un error
        String fecha_error = ObtenerFechaPreError("config2");


        // tv_estadosync.setTextColor(Color.rgb(41,184,214));
        // tv_estadosync.setText("Sincronizando Co");


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(JSONObject response) { //a traves de un json array request, traemos la informacion que viene del webservice

                try {
                    if (!(response.getString("config").equals("null"))) { // si la respuesta no viene vacia

                        conn = new AdminSQLiteOpenHelper(getApplicationContext(), "ke_android", null, 1);
                        SQLiteDatabase ke_android = conn.getWritableDatabase();
                        long filas = DatabaseUtils.queryNumEntries(ke_android, "config2"); //obtenemos las filas de la tabla articulos para comprobar si hay o no registros

                        JSONArray config = response.getJSONArray("config");

                        //aqui valido las filas de la tabla de articulos en el telefono
                        if (filas > 0) {
                            JSONObject jsonObject = null; //creamos un objeto json vacio
                            for (int i = 0; i < config.length(); i++) { /*pongo todo en el objeto segun lo que venga */
                                try {

                                    ke_android.beginTransaction();

                                    jsonObject = config.getJSONObject(i);
                                    id_precio1 = jsonObject.getString("id_precio1").trim();
                                    id_precio2 = jsonObject.getString("id_precio2").trim();
                                    id_precio3 = jsonObject.getString("id_precio3").trim();
                                    id_precio4 = jsonObject.getString("id_precio4").trim();
                                    id_precio5 = jsonObject.getString("id_precio5").trim();
                                    id_precio6 = jsonObject.getString("id_precio6").trim();
                                    id_precio7 = jsonObject.getString("id_precio7").trim();


                                    ContentValues actualizar = new ContentValues();
                                    actualizar.put("id_precio1", id_precio1);
                                    actualizar.put("id_precio2", id_precio2);
                                    actualizar.put("id_precio3", id_precio3);
                                    actualizar.put("id_precio4", id_precio4);
                                    actualizar.put("id_precio5", id_precio5);
                                    actualizar.put("id_precio6", id_precio6);
                                    actualizar.put("id_precio7", id_precio7);

                                    ke_android.update("config2", actualizar, null, null);

                                    //actualizamos la fecha de la tabla de
                                    Calendar fecha_config = Calendar.getInstance();
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                    String fechaconfig = sdf.format(fecha_config.getTime());

                                    ContentValues actualizarFecha = new ContentValues();
                                    actualizarFecha.put("fchhn_ultmod", fechaconfig);
                                    ke_android.update("tabla_aux", actualizarFecha, "tabla = 'config2'", null);

                                    ke_android.setTransactionSuccessful();


                                } catch (JSONException e) {
                                    Toast.makeText(getApplicationContext(), "Error 7", LENGTH_LONG).show();
                                } finally {
                                    ke_android.endTransaction();
                                }


                                Cursor codigo_en_local = ke_android.rawQuery("SELECT count(id_precio1) FROM config2 WHERE id_precio1 = '" + id_precio1 + "'", null);
                                codigo_en_local.moveToFirst();
                                int codigo_existente = codigo_en_local.getInt(0);

                                if (codigo_existente > 0) {

                                    try {

                                        ke_android.beginTransaction();

                                        jsonObject = config.getJSONObject(i);
                                        id_precio1 = jsonObject.getString("id_precio1").trim();
                                        id_precio2 = jsonObject.getString("id_precio2").trim();
                                        id_precio3 = jsonObject.getString("id_precio3").trim();
                                        id_precio4 = jsonObject.getString("id_precio4").trim();
                                        id_precio5 = jsonObject.getString("id_precio5").trim();
                                        id_precio6 = jsonObject.getString("id_precio6").trim();
                                        id_precio7 = jsonObject.getString("id_precio7").trim();


                                        ContentValues actualizar = new ContentValues();
                                        actualizar.put("id_precio1", id_precio1);
                                        actualizar.put("id_precio2", id_precio2);
                                        actualizar.put("id_precio3", id_precio3);
                                        actualizar.put("id_precio4", id_precio4);
                                        actualizar.put("id_precio5", id_precio5);
                                        actualizar.put("id_precio6", id_precio6);
                                        actualizar.put("id_precio7", id_precio7);

                                        ke_android.update("config2", actualizar, null, null);

                                        //actualizamos la fecha de la tabla de
                                        Calendar fecha_config = Calendar.getInstance();
                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                        String fechaconfig = sdf.format(fecha_config.getTime());

                                        ContentValues actualizarFecha = new ContentValues();
                                        actualizarFecha.put("fchhn_ultmod", fechaconfig);
                                        ke_android.update("tabla_aux", actualizarFecha, "tabla = 'config2'", null);

                                        ke_android.setTransactionSuccessful();


                                    } catch (JSONException e) {
                                        Toast.makeText(getApplicationContext(), "Error 8", LENGTH_LONG).show();
                                    } finally {
                                        ke_android.endTransaction();
                                    }


                                } else {


                                    try {
                                        ke_android.beginTransaction();

                                        jsonObject = config.getJSONObject(i);
                                        id_precio1 = jsonObject.getString("id_precio1").trim();
                                        id_precio2 = jsonObject.getString("id_precio2").trim();
                                        id_precio3 = jsonObject.getString("id_precio3").trim();
                                        id_precio4 = jsonObject.getString("id_precio4").trim();
                                        id_precio5 = jsonObject.getString("id_precio5").trim();
                                        id_precio6 = jsonObject.getString("id_precio6").trim();
                                        id_precio7 = jsonObject.getString("id_precio7").trim();

                                        ContentValues insertar = new ContentValues();
                                        insertar.put("id_precio1", id_precio1);
                                        insertar.put("id_precio2", id_precio2);
                                        insertar.put("id_precio3", id_precio3);
                                        insertar.put("id_precio4", id_precio4);
                                        insertar.put("id_precio5", id_precio5);
                                        insertar.put("id_precio6", id_precio6);
                                        insertar.put("id_precio7", id_precio7);


                                        ke_android.insert("config2", null, insertar);

                                        //actualizamos la fecha de la tabla de
                                        Calendar fecha_config = Calendar.getInstance();
                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                        String fechaconfig = sdf.format(fecha_config.getTime());

                                        ContentValues actualizarFecha = new ContentValues();
                                        actualizarFecha.put("fchhn_ultmod", fechaconfig);
                                        ke_android.update("tabla_aux", actualizarFecha, "tabla = 'config2'", null);

                                        ke_android.setTransactionSuccessful();

                                    } catch (JSONException e) {
                                        Toast.makeText(getApplicationContext(), "Error 9", LENGTH_LONG).show();
                                    } finally {
                                        ke_android.endTransaction();
                                    }

                                }

                            }
                            ke_android.close();
                            progressDialog.setMessage("config. actualizada");
                            varAux++;
                            progressDialog.incrementProgressBy((int) numBarraProgreso);
                            sincronizacionVendedor();


                        } else {

                            //si no hay nada, hago un insert
                            AdminSQLiteOpenHelper conn = new AdminSQLiteOpenHelper(getApplicationContext(), "ke_android", null, 1);
                            ke_android = conn.getWritableDatabase();


                            JSONObject jsonObject = null; //creamos un objeto json vacio
                            for (int i = 0; i < config.length(); i++) { /*pongo todo en el objeto segun lo que venga */
                                try {

                                    ke_android.beginTransaction();

                                    jsonObject = config.getJSONObject(i);
                                    id_precio1 = jsonObject.getString("id_precio1").trim();
                                    id_precio2 = jsonObject.getString("id_precio2").trim();
                                    id_precio3 = jsonObject.getString("id_precio3").trim();
                                    id_precio4 = jsonObject.getString("id_precio4").trim();
                                    id_precio5 = jsonObject.getString("id_precio5").trim();
                                    id_precio6 = jsonObject.getString("id_precio6").trim();
                                    id_precio7 = jsonObject.getString("id_precio7").trim();

                                    ContentValues insertar = new ContentValues();
                                    insertar.put("id_precio1", id_precio1);
                                    insertar.put("id_precio2", id_precio2);
                                    insertar.put("id_precio3", id_precio3);
                                    insertar.put("id_precio4", id_precio4);
                                    insertar.put("id_precio5", id_precio5);
                                    insertar.put("id_precio6", id_precio6);
                                    insertar.put("id_precio7", id_precio7);

                                    ke_android.insert("config2", null, insertar);

                                    //actualizamos la fecha de la tabla de
                                    Calendar fecha_config = Calendar.getInstance();
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                    String fechaconfig = sdf.format(fecha_config.getTime());

                                    ContentValues actualizarFecha = new ContentValues();
                                    actualizarFecha.put("fchhn_ultmod", fechaconfig);
                                    ke_android.update("tabla_aux", actualizarFecha, "tabla = 'config2'", null);

                                    ke_android.setTransactionSuccessful();
                                } catch (JSONException e) {
                                    Toast.makeText(getApplicationContext(), "Error 10", LENGTH_LONG).show();
                                    System.out.println("Error 10 -> " + e);
                                } finally {
                                    ke_android.endTransaction();
                                }


                            }
                            //    Toast.makeText(PrincipalActivity.this, "Subsectores descargados", Toast.LENGTH_SHORT).show();

                            ke_android.close();

                            // tv_estadosync.setTextColor(Color.rgb(62,197,58));
                            // tv_estadosync.setText("Subsectores Sincronizado");
                            progressDialog.setMessage("Configuración: Actualizando.");
                            varAux++;
                            progressDialog.incrementProgressBy((int) numBarraProgreso);
                            sincronizacionVendedor();
                        }
                    } else if (response.getString("config").equals("null")) {

                        progressDialog.setMessage("Configuración: Sin Actualizar.");
                        varAux++;
                        progressDialog.incrementProgressBy((int) numBarraProgreso);
                        sincronizacionVendedor();


                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Ingreso de la fecha antes de ser actualizada
                ActualizarFechaError(fecha_error);

                //--Manejo visual que indica al usuario del error--
                progressDialog.setMessage("Configuración: No ha logrado sincronizar.");
                varAux++;
                progressDialog.incrementProgressBy((int) numBarraProgreso);
                //varAuxError = true;
                //AnalisisError2();
                //------
                sincronizacionVendedor();

                //  tv_estadosync.setTextColor(Color.rgb(98,117,141));
                //  tv_estadosync.setText("Sin actualizacion");
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {  //finalmente, estos son los parametros que le enviaremos al webservice, partiendo de las variables
                //donde estan guardados las fechas
                Map<String, String> parametros = new HashMap<String, String>();
                // parametros.put("fecha_sinc", fecha_sinc);

                return parametros;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest); //esto es el request que se envia al url a traves de la conexion volley, (el stringrequest esta armado arriba)


    }

    private void BajarVendedor(String URL) {
        System.out.println("URL vndeodr -> " + URL);
        //Fecha tomada para ser coloada en tabla auxiliar en caso de dar un error
        String fecha_error = ObtenerFechaPreError("listvend");


        contadorvend = 0;

        tv_vendedor.setTextColor(Color.rgb(41, 184, 214));
        tv_vendedor.setText("Vendedor: Sincronizando");

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(JSONObject response) { //a traves de un json array request, traemos la informacion que viene del webservice

                try {
                    if (!(response.getString("vendedor").equals("null"))) { // si la respuesta no viene vacia

                        conn = new AdminSQLiteOpenHelper(getApplicationContext(), "ke_android", null, 1);
                        SQLiteDatabase ke_android = conn.getWritableDatabase();
                        long filas = DatabaseUtils.queryNumEntries(ke_android, "listvend"); //obtenemos las filas de la tabla articulos para comprobar si hay o no registros

                        JSONArray vendedorArray = response.getJSONArray("vendedor");

                        //aqui valido las filas de la tabla de articulos en el telefono
                        if (filas > 0) {
                            JSONObject jsonObject = null; //creamos un objeto json vacio
                            for (int i = 0; i < vendedorArray.length(); i++) { /*pongo todo en el objeto segun lo que venga */
                                try {

                                    ke_android.beginTransaction();
                                    jsonObject = vendedorArray.getJSONObject(i);
                                    codigo = jsonObject.getString("codigo").trim();
                                    nombre = jsonObject.getString("nombre").trim();
                                    telefonos = jsonObject.getString("telefonos").trim();
                                    telefono_movil = jsonObject.getString("telefono_movil").trim();
                                    status = jsonObject.getDouble("status");
                                    superves = jsonObject.getDouble("superves");
                                    supervpor = jsonObject.getString("supervpor").trim();
                                    sector = jsonObject.getString("sector").trim();
                                    subcodigo = jsonObject.getString("subcodigo").trim();
                                    nivgcial = jsonObject.getDouble("nivgcial");
                                    fechamodifi = jsonObject.getString("fechamodifi");


                                    ContentValues actualizar = new ContentValues();
                                    actualizar.put("codigo", codigo);
                                    actualizar.put("nombre", nombre);
                                    actualizar.put("telefonos", telefonos);
                                    actualizar.put("telefono_movil", telefono_movil);
                                    actualizar.put("status", status);
                                    actualizar.put("superves", superves);
                                    actualizar.put("supervpor", supervpor);
                                    actualizar.put("sector", sector);
                                    actualizar.put("subcodigo", subcodigo);
                                    actualizar.put("nivgcial", nivgcial);
                                    actualizar.put("fechamodifi", fechamodifi);


                                    ke_android.update("listvend", actualizar, "codigo = '" + codigo + "'", null);


                                    //actualizamos la fecha de la tabla de
                                    Calendar fecha_listvend = Calendar.getInstance();
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                    String fechaListvend = sdf.format(fecha_listvend.getTime());

                                    ContentValues actualizarFecha = new ContentValues();
                                    actualizarFecha.put("fchhn_ultmod", fechaListvend);
                                    ke_android.update("tabla_aux", actualizarFecha, "tabla = 'listvend'", null);

                                    ke_android.setTransactionSuccessful();


                                } catch (JSONException e) {
                                    Toast.makeText(getApplicationContext(), "Error 11", LENGTH_LONG).show();
                                } finally {
                                    ke_android.endTransaction();
                                }

                                Cursor codigo_en_local = ke_android.rawQuery("SELECT count(codigo) FROM listvend WHERE codigo = '" + codigo + "'", null);
                                codigo_en_local.moveToFirst();
                                int codigo_existente = codigo_en_local.getInt(0);

                                if (codigo_existente > 0) {

                                    try {

                                        ke_android.beginTransaction();
                                        jsonObject = vendedorArray.getJSONObject(i);
                                        codigo = jsonObject.getString("codigo").trim();
                                        nombre = jsonObject.getString("nombre").trim();
                                        telefonos = jsonObject.getString("telefonos").trim();
                                        telefono_movil = jsonObject.getString("telefono_movil").trim();
                                        status = jsonObject.getDouble("status");
                                        superves = jsonObject.getDouble("superves");
                                        supervpor = jsonObject.getString("supervpor").trim();
                                        sector = jsonObject.getString("sector").trim();
                                        subcodigo = jsonObject.getString("subcodigo").trim();
                                        nivgcial = jsonObject.getDouble("nivgcial");
                                        fechamodifi = jsonObject.getString("fechamodifi");


                                        ContentValues actualizar = new ContentValues();
                                        actualizar.put("codigo", codigo);
                                        actualizar.put("nombre", nombre);
                                        actualizar.put("telefonos", telefonos);
                                        actualizar.put("telefono_movil", telefono_movil);
                                        actualizar.put("status", status);
                                        actualizar.put("superves", superves);
                                        actualizar.put("supervpor", supervpor);
                                        actualizar.put("sector", sector);
                                        actualizar.put("subcodigo", subcodigo);
                                        actualizar.put("nivgcial", nivgcial);
                                        actualizar.put("fechamodifi", fechamodifi);


                                        ke_android.update("listvend", actualizar, "codigo = '" + codigo + "'", null);


                                        //actualizamos la fecha de la tabla de
                                        Calendar fecha_listvend = Calendar.getInstance();
                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                        String fechaListvend = sdf.format(fecha_listvend.getTime());

                                        ContentValues actualizarFecha = new ContentValues();
                                        actualizarFecha.put("fchhn_ultmod", fechaListvend);
                                        ke_android.update("tabla_aux", actualizarFecha, "tabla = 'listvend'", null);
                                        ke_android.setTransactionSuccessful();
                                        contadorvend++;

                                    } catch (JSONException e) {
                                        Toast.makeText(getApplicationContext(), "Error 12", LENGTH_LONG).show();
                                    } finally {
                                        ke_android.endTransaction();
                                    }


                                } else {
                                    try {
                                        ke_android.beginTransaction();
                                        jsonObject = vendedorArray.getJSONObject(i);
                                        codigo = jsonObject.getString("codigo").trim();
                                        nombre = jsonObject.getString("nombre").trim();
                                        telefonos = jsonObject.getString("telefonos").trim();
                                        telefono_movil = jsonObject.getString("telefono_movil").trim();
                                        status = jsonObject.getDouble("status");
                                        superves = jsonObject.getDouble("superves");
                                        supervpor = jsonObject.getString("supervpor").trim();
                                        sector = jsonObject.getString("sector").trim();
                                        subcodigo = jsonObject.getString("subcodigo").trim();
                                        nivgcial = jsonObject.getDouble("nivgcial");
                                        fechamodifi = jsonObject.getString("fechamodifi");

                                        ContentValues insertar = new ContentValues();
                                        insertar.put("codigo", codigo);
                                        insertar.put("nombre", nombre);
                                        insertar.put("telefonos", telefonos);
                                        insertar.put("telefono_movil", telefono_movil);
                                        insertar.put("status", status);
                                        insertar.put("superves", superves);
                                        insertar.put("supervpor", supervpor);
                                        insertar.put("sector", sector);
                                        insertar.put("subcodigo", subcodigo);
                                        insertar.put("nivgcial", nivgcial);
                                        insertar.put("fechamodifi", fechamodifi);


                                        ke_android.insert("listvend", null, insertar);

                                        //actualizamos la fecha de la tabla de
                                        Calendar fecha_listvend = Calendar.getInstance();
                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                        String fechaListvend = sdf.format(fecha_listvend.getTime());

                                        ContentValues actualizarFecha = new ContentValues();
                                        actualizarFecha.put("fchhn_ultmod", fechaListvend);
                                        ke_android.update("tabla_aux", actualizarFecha, "tabla = 'listvend'", null);
                                        ke_android.setTransactionSuccessful();
                                        contadorvend++;
                                    } catch (JSONException e) {
                                        Toast.makeText(getApplicationContext(), "Error 13", LENGTH_LONG).show();
                                    } finally {
                                        ke_android.endTransaction();
                                    }
                                }

                            }
                            //Toast.makeText(PrincipalActivity.this, "vendedor Descargado", Toast.LENGTH_SHORT).show();
                            ke_android.close();
                            // Clientes.setEnabled(true);
                            tv_vendedor.setTextColor(Color.rgb(62, 197, 58));
                            tv_vendedor.setText("Vendedor: " + contadorvend);
                            progressDialog.setMessage("Vendedor: " + contadorvend);
                            varAux++;
                            progressDialog.incrementProgressBy((int) numBarraProgreso);
                            sincronizacionVendedor();

                        } else {

                            //si no hay nada, hago un insert
                            AdminSQLiteOpenHelper conn = new AdminSQLiteOpenHelper(getApplicationContext(), "ke_android", null, 1);
                            ke_android = conn.getWritableDatabase();

                            JSONObject jsonObject = null; //creamos un objeto json vacio
                            for (int i = 0; i < vendedorArray.length(); i++) { /*pongo todo en el objeto segun lo que venga */
                                try {

                                    ke_android.beginTransaction();

                                    jsonObject = vendedorArray.getJSONObject(i);
                                    codigo = jsonObject.getString("codigo").trim();
                                    nombre = jsonObject.getString("nombre").trim();
                                    telefonos = jsonObject.getString("telefonos").trim();
                                    telefono_movil = jsonObject.getString("telefono_movil").trim();
                                    status = jsonObject.getDouble("status");
                                    superves = jsonObject.getDouble("superves");
                                    supervpor = jsonObject.getString("supervpor").trim();
                                    sector = jsonObject.getString("sector").trim();
                                    subcodigo = jsonObject.getString("subcodigo").trim();
                                    nivgcial = jsonObject.getDouble("nivgcial");
                                    fechamodifi = jsonObject.getString("fechamodifi");

                                    ContentValues insertar = new ContentValues();
                                    insertar.put("codigo", codigo);
                                    insertar.put("nombre", nombre);
                                    insertar.put("telefonos", telefonos);
                                    insertar.put("telefono_movil", telefono_movil);
                                    insertar.put("status", status);
                                    insertar.put("superves", superves);
                                    insertar.put("supervpor", supervpor);
                                    insertar.put("sector", sector);
                                    insertar.put("subcodigo", subcodigo);
                                    insertar.put("nivgcial", nivgcial);
                                    insertar.put("fechamodifi", fechamodifi);


                                    ke_android.insert("listvend", null, insertar);

                                    //actualizamos la fecha de la tabla de la tabla
                                    Calendar fecha_listvend = Calendar.getInstance();
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                    String fechaListvend = sdf.format(fecha_listvend.getTime());

                                    ContentValues actualizarFecha = new ContentValues();
                                    actualizarFecha.put("fchhn_ultmod", fechaListvend);
                                    ke_android.update("tabla_aux", actualizarFecha, "tabla = 'listvend'", null);

                                    ke_android.setTransactionSuccessful();
                                    contadorvend++;

                                } catch (JSONException e) {
                                    Toast.makeText(getApplicationContext(), "Error 14", LENGTH_LONG).show();
                                } finally {
                                    ke_android.endTransaction();
                                }


                            }
                            // Toast.makeText(PrincipalActivity.this, "vendedor Descargado", Toast.LENGTH_SHORT).show();


                            ke_android.close();
                            //  Clientes.setEnabled(true);
                            tv_vendedor.setTextColor(Color.rgb(62, 197, 58));
                            tv_vendedor.setText("Vendedor: " + contadorvend);
                            progressDialog.setMessage("Vendedor: " + contadorvend);
                            varAux++;
                            progressDialog.incrementProgressBy((int) numBarraProgreso);
                            sincronizacionVendedor();
                        }
                    } else if (response.getString("vendedor").equals("null")) {

                        // Toast.makeText(getApplicationContext(), "No se recibieron mas datos", LENGTH_LONG).show(); /* si en la consulta no ncuentra nada
                        //es que el usuario o password estan incorrectos */

                        //    Clientes.setEnabled(true);
                        tv_vendedor.setTextColor(Color.rgb(98, 117, 141));
                        tv_vendedor.setText("Vendedor: Sin actualización");
                        progressDialog.setMessage("Vendedor: Sin actualización");
                        varAux++;
                        progressDialog.incrementProgressBy((int) numBarraProgreso);
                        sincronizacionVendedor();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Ingreso de la fecha antes de ser actualizada
                ActualizarFechaError(fecha_error);

                //    Clientes.setEnabled(true);
                //--Manejo visual que indica al usuario del error--
                tv_vendedor.setTextColor(Color.rgb(232, 17, 35));
                tv_vendedor.setText("Vendedor: No ha logrado sincronizar");
                progressDialog.setMessage("Vendedor: No ha logrado sincronizar");
                varAux++;
                progressDialog.incrementProgressBy((int) numBarraProgreso);
                //varAuxError = true;
                //AnalisisError2();
                //------
                sincronizacionVendedor();
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
        requestQueue.add(jsonObjectRequest); //esto es el request que se envia al url a traves de la conexion volley, (el stringrequest esta armado arriba)


    }

    //------------------------------ METODO PARA OBTENER FECHA -------------------------------------
    private void GetFechas() {
        GetFechaArticulo();
        GetFechaCliempre();
        GetFechaListvend();
        GetFechaGrupos();
        GetFechaSubGrupos();
        GetFechaSectores();
        GetFechaSubgrupos();
        GetFechaSubsectores();
        GetFechaLimites();
    }

    private void GetFechaArticulo() {
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        Cursor fecha_ultmod = ke_android.rawQuery("SELECT fchhn_ultmod FROM tabla_aux WHERE tabla = 'articulo'", null);
        fecha_ultmod.moveToFirst();
        fecha_sinc_articulo = fecha_ultmod.getString(0);

    }

    private void GetFechaCliempre() {
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        Cursor fecha_ultmod = ke_android.rawQuery("SELECT fchhn_ultmod FROM tabla_aux WHERE tabla = 'cliempre'", null);
        fecha_ultmod.moveToFirst();
        fecha_sinc_cliempre = fecha_ultmod.getString(0);

    }

    private void GetFechaListvend() {
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        Cursor fecha_ultmod = ke_android.rawQuery("SELECT fchhn_ultmod FROM tabla_aux WHERE tabla = 'listvend'", null);
        fecha_ultmod.moveToFirst();
        fecha_sinc_listvend = fecha_ultmod.getString(0);

    }

    private void GetFechaGrupos() {
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        Cursor fecha_ultmod = ke_android.rawQuery("SELECT fchhn_ultmod FROM tabla_aux WHERE tabla = 'grupos'", null);
        fecha_ultmod.moveToFirst();
        fecha_sinc_grupos = fecha_ultmod.getString(0);

    }

    private void GetFechaSubGrupos() {
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        Cursor fecha_ultmod = ke_android.rawQuery("SELECT fchhn_ultmod FROM tabla_aux WHERE tabla = 'subgrupos'", null);
        fecha_ultmod.moveToFirst();
        fecha_sinc_subgrupos = fecha_ultmod.getString(0);

    }

    private void GetFechaSectores() {
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        Cursor fecha_ultmod = ke_android.rawQuery("SELECT fchhn_ultmod FROM tabla_aux WHERE tabla = 'sectores'", null);
        fecha_ultmod.moveToFirst();
        fecha_sinc_sectores = fecha_ultmod.getString(0);

    }

    private void GetFechaSubgrupos() {
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        Cursor fecha_ultmod = ke_android.rawQuery("SELECT fchhn_ultmod FROM tabla_aux WHERE tabla = 'subgrupos'", null);
        fecha_ultmod.moveToFirst();
        fecha_sinc_subgrupos = fecha_ultmod.getString(0);

    }

    private void GetFechaSubsectores() {
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        Cursor fecha_ultmod = ke_android.rawQuery("SELECT fchhn_ultmod FROM tabla_aux WHERE tabla = 'subsectores'", null);
        fecha_ultmod.moveToFirst();
        fecha_sinc_subsectores = fecha_ultmod.getString(0);

    }

    private void GetFechaLimites() {
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        Cursor fecha_ultmod = ke_android.rawQuery("SELECT fchhn_ultmod FROM tabla_aux WHERE tabla = 'limites'", null);
        fecha_ultmod.moveToFirst();
        fecha_sinc_limites = fecha_ultmod.getString(0);
    }
    //-------------------------------

    private String GetFechaBancos() {
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        Cursor fecha_ultmod = ke_android.rawQuery("SELECT fchhn_ultmod FROM tabla_aux WHERE tabla = 'listbanc'", null);
        if (fecha_ultmod.moveToFirst()) {
            return fecha_ultmod.getString(0);
        } else {
            return "0001-01-01T01:01:01";
        }
    }

    private void BajarArticulos(String URL) {
        //System.out.println("Este es el URL -> " + URL);
        progressDialog.setMessage("Sincronizando articulos");
        //OJO AQUI QUE HAY 2 UPDATE Y SI SE ELIMINA 1 SE JODE TODA LA VAINA
        tv_articulos.setTextColor(Color.rgb(41, 184, 214));
        tv_articulos.setText("Articulos: Sincronizando");

        //Fecha tomada para ser coloada en tabla auxiliar en caso de dar un error
        String fecha_error = ObtenerFechaPreError("articulo");

        contadorart = 0;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(JSONObject response) { //a traves de un json array request, traemos la informacion que viene del webservice
                try {
                    if (!(response.getString("articulo").equals("null"))) { // si la respuesta no viene vacia

                        conn = new AdminSQLiteOpenHelper(getApplicationContext(), "ke_android", null, 8);
                        SQLiteDatabase ke_android = conn.getWritableDatabase();
                        long filas = DatabaseUtils.queryNumEntries(ke_android, "articulo"); //obtenemos las filas de la tabla articulos para comprobar si hay o no registros

                        JSONArray articulo = response.getJSONArray("articulo");

                        //aqui valido las filas de la tabla de articulos en el telefono
                        if (filas > 0) {
                            JSONObject jsonObject = null; //creamos un objeto json vacio
                            for (int i = 0; i < articulo.length(); i++) { /*pongo todo en el objeto segun lo que venga */
                                try {

                                    ke_android.beginTransaction();

                                    jsonObject = articulo.getJSONObject(i);
                                    codigo = jsonObject.getString("codigo").trim();
                                    grupo = jsonObject.getString("grupo").trim();
                                    subgrupo = jsonObject.getString("subgrupo").trim();
                                    nombre = jsonObject.getString("nombre").trim();
                                    marca = jsonObject.getString("marca").trim();
                                    referencia = jsonObject.getString("referencia").trim();
                                    unidad = jsonObject.getString("unidad").trim();
                                    precio1 = jsonObject.getDouble("precio1");
                                    precio2 = jsonObject.getDouble("precio2");
                                    precio3 = jsonObject.getDouble("precio3");
                                    precio4 = jsonObject.getDouble("precio4");
                                    precio5 = jsonObject.getDouble("precio5");
                                    precio6 = jsonObject.getDouble("precio6");
                                    precio7 = jsonObject.getDouble("precio7");
                                    existencia = jsonObject.getDouble("existencia");
                                    fechamodifi = jsonObject.getString("fechamodifi");
                                    discont = jsonObject.getDouble("discont");
                                    vta_max = jsonObject.getDouble("vta_max");
                                    vta_min = jsonObject.getDouble("vta_min");
                                    dctotope = jsonObject.getDouble("dctotope");
                                    enpreventa = jsonObject.getString("enpreventa").trim();
                                    comprometido = jsonObject.getString("comprometido");
                                    vta_minenx = jsonObject.getString("vta_minenx");
                                    int vta_solofac = jsonObject.getInt("vta_solofac");
                                    int vta_solone = jsonObject.getInt("vta_solone");

                                    ContentValues actualizar = new ContentValues();
                                    actualizar.put("codigo", codigo);
                                    actualizar.put("grupo", grupo);
                                    actualizar.put("subgrupo", subgrupo);
                                    actualizar.put("nombre", nombre);
                                    actualizar.put("referencia", referencia);
                                    actualizar.put("marca", marca);
                                    actualizar.put("unidad", unidad);
                                    actualizar.put("precio1", precio1);
                                    actualizar.put("precio2", precio2);
                                    actualizar.put("precio3", precio3);
                                    actualizar.put("precio4", precio4);
                                    actualizar.put("precio5", precio5);
                                    actualizar.put("precio6", precio6);
                                    actualizar.put("precio7", precio7);
                                    actualizar.put("discont", discont);
                                    actualizar.put("fechamodifi", fechamodifi);
                                    actualizar.put("existencia", existencia);
                                    actualizar.put("discont", discont);
                                    actualizar.put("vta_max", vta_max);
                                    actualizar.put("vta_min", vta_min);
                                    actualizar.put("dctotope", dctotope);
                                    actualizar.put("enpreventa", enpreventa);
                                    actualizar.put("comprometido", comprometido);
                                    actualizar.put("vta_minenx", vta_minenx);
                                    actualizar.put("vta_solofac", vta_solofac);
                                    actualizar.put("vta_solone", vta_solone);

                                    ke_android.update("articulo", actualizar, "codigo = ?", new String[]{codigo});


                                    //actualizamos la fecha de la tabla de
                                    Calendar fecha_articulo = Calendar.getInstance();
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                    String fechaArticulo = sdf.format(fecha_articulo.getTime());

                                    ContentValues actualizarFecha = new ContentValues();
                                    actualizarFecha.put("fchhn_ultmod", fechaArticulo);
                                    ke_android.update("tabla_aux", actualizarFecha, "tabla = ?", new String[]{"articulo"});

                                    ke_android.setTransactionSuccessful();
                                } catch (Exception exception) {
                                    exception.printStackTrace();
                                } finally {
                                    ke_android.endTransaction();
                                }

                                Cursor codigo_en_local = ke_android.rawQuery("SELECT count(codigo) FROM articulo WHERE codigo = '" + codigo + "'", null);
                                codigo_en_local.moveToFirst();
                                int codigo_existente = codigo_en_local.getInt(0);
                                //System.out.println("ESTE ES EL CODIGO: " + codigo);
                                //System.out.println("SELECT count(codigo) FROM articulo WHERE codigo = '" + codigo + "'");

                                if (codigo_existente > 0) {


                                    try {

                                        ke_android.beginTransaction();

                                        jsonObject = articulo.getJSONObject(i);
                                        codigo = jsonObject.getString("codigo").trim();
                                        grupo = jsonObject.getString("grupo").trim();
                                        subgrupo = jsonObject.getString("subgrupo").trim();
                                        nombre = jsonObject.getString("nombre").trim();
                                        marca = jsonObject.getString("marca").trim();
                                        referencia = jsonObject.getString("referencia").trim();
                                        unidad = jsonObject.getString("unidad").trim();
                                        precio1 = jsonObject.getDouble("precio1");
                                        precio2 = jsonObject.getDouble("precio2");
                                        precio3 = jsonObject.getDouble("precio3");
                                        precio4 = jsonObject.getDouble("precio4");
                                        precio5 = jsonObject.getDouble("precio5");
                                        precio6 = jsonObject.getDouble("precio6");
                                        precio7 = jsonObject.getDouble("precio7");
                                        existencia = jsonObject.getDouble("existencia");
                                        fechamodifi = jsonObject.getString("fechamodifi");
                                        discont = jsonObject.getDouble("discont");
                                        vta_max = jsonObject.getDouble("vta_max");
                                        vta_min = jsonObject.getDouble("vta_min");
                                        dctotope = jsonObject.getDouble("dctotope");
                                        enpreventa = jsonObject.getString("enpreventa").trim();
                                        comprometido = jsonObject.getString("comprometido");
                                        vta_minenx = jsonObject.getString("vta_minenx");
                                        int vta_solofac = jsonObject.getInt("vta_solofac");
                                        int vta_solone = jsonObject.getInt("vta_solone");

                                        ContentValues actualizar = new ContentValues();
                                        actualizar.put("codigo", codigo);
                                        actualizar.put("grupo", grupo);
                                        actualizar.put("subgrupo", subgrupo);
                                        actualizar.put("nombre", nombre);
                                        actualizar.put("referencia", referencia);
                                        actualizar.put("marca", marca);
                                        actualizar.put("unidad", unidad);
                                        actualizar.put("precio1", precio1);
                                        actualizar.put("precio2", precio2);
                                        actualizar.put("precio3", precio3);
                                        actualizar.put("precio4", precio4);
                                        actualizar.put("precio5", precio5);
                                        actualizar.put("precio6", precio6);
                                        actualizar.put("precio7", precio7);
                                        actualizar.put("discont", discont);
                                        actualizar.put("fechamodifi", fechamodifi);
                                        actualizar.put("existencia", existencia);
                                        actualizar.put("discont", discont);
                                        actualizar.put("vta_max", vta_max);
                                        actualizar.put("vta_min", vta_min);
                                        actualizar.put("dctotope", dctotope);
                                        actualizar.put("enpreventa", enpreventa);
                                        actualizar.put("comprometido", comprometido);
                                        actualizar.put("vta_minenx", vta_minenx);
                                        actualizar.put("vta_solofac", vta_solofac);
                                        actualizar.put("vta_solone", vta_solone);
                                        ke_android.update("articulo", actualizar, "codigo = ?", new String[]{codigo});

                                        //actualizamos la fecha de la tabla de
                                        Calendar fecha_articulo = Calendar.getInstance();
                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                        String fechaArticulo = sdf.format(fecha_articulo.getTime());

                                        ContentValues actualizarFecha = new ContentValues();
                                        actualizarFecha.put("fchhn_ultmod", fechaArticulo);
                                        ke_android.update("tabla_aux", actualizarFecha, "tabla = ?", new String[]{"articulo"});

                                        ke_android.setTransactionSuccessful();
                                        contadorart++;

                                    } catch (Exception exception) {
                                        exception.printStackTrace();
                                    } finally {
                                        codigo_en_local.close();
                                        ke_android.endTransaction();
                                    }

                                    tv_articulos.setTextColor(Color.rgb(62, 197, 58));
                                    tv_articulos.setText("Articulos:" + contadorart);
                                    progressDialog.setMessage("Articulos:" + contadorart);

                                } else {
                                    try {

                                        ke_android.beginTransaction();

                                        jsonObject = articulo.getJSONObject(i);
                                        codigo = jsonObject.getString("codigo").trim();
                                        grupo = jsonObject.getString("grupo").trim();
                                        subgrupo = jsonObject.getString("subgrupo").trim();
                                        nombre = jsonObject.getString("nombre").trim();
                                        marca = jsonObject.getString("marca").trim();
                                        referencia = jsonObject.getString("referencia").trim();
                                        unidad = jsonObject.getString("unidad").trim();
                                        precio1 = jsonObject.getDouble("precio1");
                                        precio2 = jsonObject.getDouble("precio2");
                                        precio3 = jsonObject.getDouble("precio3");
                                        precio4 = jsonObject.getDouble("precio4");
                                        precio5 = jsonObject.getDouble("precio5");
                                        precio6 = jsonObject.getDouble("precio6");
                                        precio7 = jsonObject.getDouble("precio7");
                                        existencia = jsonObject.getDouble("existencia");
                                        fechamodifi = jsonObject.getString("fechamodifi");
                                        discont = jsonObject.getDouble("discont");
                                        vta_max = jsonObject.getDouble("vta_max");
                                        vta_min = jsonObject.getDouble("vta_min");
                                        dctotope = jsonObject.getDouble("dctotope");
                                        enpreventa = jsonObject.getString("enpreventa").trim();
                                        comprometido = jsonObject.getString("comprometido");
                                        vta_minenx = jsonObject.getString("vta_minenx");
                                        int vta_solofac = jsonObject.getInt("vta_solofac");
                                        int vta_solone = jsonObject.getInt("vta_solone");

                                        ContentValues insertar = new ContentValues();
                                        insertar.put("codigo", codigo);
                                        insertar.put("grupo", grupo);
                                        insertar.put("subgrupo", subgrupo);
                                        insertar.put("nombre", nombre);
                                        insertar.put("referencia", referencia);
                                        insertar.put("marca", marca);
                                        insertar.put("unidad", unidad);
                                        insertar.put("precio1", precio1);
                                        insertar.put("precio2", precio2);
                                        insertar.put("precio3", precio3);
                                        insertar.put("precio4", precio4);
                                        insertar.put("precio5", precio5);
                                        insertar.put("precio6", precio6);
                                        insertar.put("precio7", precio7);
                                        insertar.put("discont", discont);
                                        insertar.put("fechamodifi", fechamodifi);
                                        insertar.put("existencia", existencia);
                                        insertar.put("discont", discont);
                                        insertar.put("vta_max", vta_max);
                                        insertar.put("vta_min", vta_min);
                                        insertar.put("dctotope", dctotope);
                                        insertar.put("enpreventa", enpreventa);
                                        insertar.put("comprometido", comprometido);
                                        insertar.put("vta_minenx", vta_minenx);
                                        insertar.put("vta_solofac", vta_solofac);
                                        insertar.put("vta_solone", vta_solone);

                                        ke_android.insert("articulo", null, insertar);

                                        //actualizamos la fecha de la tabla de
                                        Calendar fecha_articulo = Calendar.getInstance();
                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                        String fechaArticulo = sdf.format(fecha_articulo.getTime());

                                        ContentValues actualizarFecha = new ContentValues();
                                        actualizarFecha.put("fchhn_ultmod", fechaArticulo);
                                        ke_android.update("tabla_aux", actualizarFecha, "tabla = ?", new String[]{"articulo"});
                                        contadorart++;

                                        ke_android.setTransactionSuccessful();

                                        tv_articulos.setTextColor(Color.rgb(62, 197, 58));
                                        tv_articulos.setText("Articulos:" + contadorart);
                                        progressDialog.setMessage("Articulos:" + contadorart);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    } finally {
                                        ke_android.endTransaction();
                                    }
                                }

                            }

                            varAux++;
                            progressDialog.setMessage("Articulos:" + contadorart);
                            progressDialog.incrementProgressBy((int) numBarraProgreso);
                            sincronizacionVendedor();
                        } else {

                            //si no hay nada, hago un insert
                            AdminSQLiteOpenHelper conn = new AdminSQLiteOpenHelper(getApplicationContext(), "ke_android", null, 3);
                            ke_android = conn.getWritableDatabase();


                            JSONObject jsonObject = null; //creamos un objeto json vacio
                            for (int i = 0; i < articulo.length(); i++) { /*pongo todo en el objeto segun lo que venga */
                                try {

                                    ke_android.beginTransaction();
                                    jsonObject = articulo.getJSONObject(i);
                                    codigo = jsonObject.getString("codigo").trim();
                                    grupo = jsonObject.getString("grupo").trim();
                                    subgrupo = jsonObject.getString("subgrupo").trim();
                                    nombre = jsonObject.getString("nombre").trim();
                                    marca = jsonObject.getString("marca").trim();
                                    referencia = jsonObject.getString("referencia").trim();
                                    unidad = jsonObject.getString("unidad").trim();
                                    precio1 = jsonObject.getDouble("precio1");
                                    precio2 = jsonObject.getDouble("precio2");
                                    precio3 = jsonObject.getDouble("precio3");
                                    precio4 = jsonObject.getDouble("precio4");
                                    precio5 = jsonObject.getDouble("precio5");
                                    precio6 = jsonObject.getDouble("precio6");
                                    precio7 = jsonObject.getDouble("precio7");
                                    existencia = jsonObject.getDouble("existencia");
                                    fechamodifi = jsonObject.getString("fechamodifi");
                                    discont = jsonObject.getDouble("discont");
                                    vta_max = jsonObject.getDouble("vta_max");
                                    vta_min = jsonObject.getDouble("vta_min");
                                    dctotope = jsonObject.getDouble("dctotope");
                                    enpreventa = jsonObject.getString("enpreventa").trim();
                                    comprometido = jsonObject.getString("comprometido");
                                    vta_minenx = jsonObject.getString("vta_minenx");
                                    int vta_solofac = jsonObject.getInt("vta_solofac");
                                    int vta_solone = jsonObject.getInt("vta_solone");

                                    ContentValues insertar = new ContentValues();
                                    insertar.put("codigo", codigo);
                                    insertar.put("grupo", grupo);
                                    insertar.put("subgrupo", subgrupo);
                                    insertar.put("nombre", nombre);
                                    insertar.put("referencia", referencia);
                                    insertar.put("marca", marca);
                                    insertar.put("unidad", unidad);
                                    insertar.put("precio1", precio1);
                                    insertar.put("precio2", precio2);
                                    insertar.put("precio3", precio3);
                                    insertar.put("precio4", precio4);
                                    insertar.put("precio5", precio5);
                                    insertar.put("precio6", precio6);
                                    insertar.put("precio7", precio7);
                                    insertar.put("discont", discont);
                                    insertar.put("fechamodifi", fechamodifi);
                                    insertar.put("existencia", existencia);
                                    insertar.put("discont", discont);
                                    insertar.put("vta_max", vta_max);
                                    insertar.put("vta_min", vta_min);
                                    insertar.put("dctotope", dctotope);
                                    insertar.put("enpreventa", enpreventa);
                                    insertar.put("comprometido", comprometido);
                                    insertar.put("vta_minenx", vta_minenx);
                                    insertar.put("vta_solofac", vta_solofac);
                                    insertar.put("vta_solone", vta_solone);

                                    ke_android.insert("articulo", null, insertar);

                                    //actualizamos la fecha de la tabla de
                                    Calendar fecha_articulo = Calendar.getInstance();
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                    String fechaArticulo = sdf.format(fecha_articulo.getTime());

                                    ContentValues actualizarFecha = new ContentValues();
                                    actualizarFecha.put("fchhn_ultmod", fechaArticulo);
                                    ke_android.update("tabla_aux", actualizarFecha, "tabla = ?", new String[]{"articulo"});
                                    contadorart++;

                                    ke_android.setTransactionSuccessful();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Toast.makeText(getApplicationContext(), "Error 15", LENGTH_LONG).show();
                                } finally {
                                    ke_android.endTransaction();
                                }


                            }
                            // Toast.makeText(PrincipalActivity.this, "Articulos descargados", Toast.LENGTH_SHORT).show();

                            ke_android.close();

                            tv_articulos.setTextColor(Color.rgb(62, 197, 58));
                            tv_articulos.setText("Articulos: " + contadorart);
                            progressDialog.setMessage("Articulos:" + contadorart);
                            varAux++;
                            progressDialog.incrementProgressBy((int) numBarraProgreso);
                            sincronizacionVendedor();
                        }


                    } else if (response.getString("articulo").equals("null")) {
                        //System.out.println("AQUI ANDA");

                        // Toast.makeText(getApplicationContext(), "No se recibieron mas datos", LENGTH_LONG).show(); /* si en la consulta no ncuentra nada
                        //es que el usuario o password estan incorrectos */


                        tv_articulos.setTextColor(Color.rgb(98, 117, 141));
                        tv_articulos.setText("Articulos: Sin actualización");
                        progressDialog.setMessage("Articulos: Sin actualización");
                        varAux++;
                        progressDialog.incrementProgressBy((int) numBarraProgreso);
                        sincronizacionVendedor();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //System.out.println("Este es el error -> "+error);
                //Ingreso de la fecha antes de ser actualizada
                ActualizarFechaError(fecha_error);

                //--Manejo visual que indica al usuario del error--
                tv_articulos.setTextColor(Color.rgb(232, 17, 35));
                tv_articulos.setText("Articulos: No ha logrado sincronizar");
                progressDialog.setMessage("Articulos: No ha logrado sincronizar");
                varAux++;
                progressDialog.incrementProgressBy((int) numBarraProgreso);
                varAuxError = true;
                AnalisisError2();
                //-----
                sincronizacionVendedor();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {  //finalmente, estos son los parametros que le enviaremos al webservice, partiendo de las variables
                //donde estan guardados las fechas
                Map<String, String> parametros = new HashMap<String, String>();
                parametros.put("fecha_sinc", fecha_sinc_articulo);

                return parametros;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest); //esto es el request que se envia al url a traves de la conexion volley, (el stringrequest esta armado arriba)

    }

    private void ActualizarFechaError(String fecha_error) {

        ContentValues actualizarFecha = new ContentValues();
        actualizarFecha.put("fchhn_ultmod", fecha_error);
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        ke_android.update("tabla_aux", actualizarFecha, "tabla = 'articulo'", null);
        ke_android.close();

    }

    private String ObtenerFechaPreError(String tabla) {
        conn = new AdminSQLiteOpenHelper(getApplicationContext(), "ke_android", null, 8);
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        Cursor cursor = ke_android.rawQuery("SELECT fchhn_ultmod FROM tabla_aux WHERE tabla = '" + tabla + "'", null);
        cursor.moveToFirst();
        String fecha_error = cursor.getString(0);
        cursor.close();
        ke_android.close();

        return fecha_error;
    }

    //Funcion que crea un JSON para indicarle a la ase de datos quien y cuando sincronizo
    private void SubirSincronizacion() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String fecha = dateFormat.format(Date.from(Instant.now()));

        //System.out.println("Fecha actual = " + fecha);

        JSONObject jsonObject = new JSONObject();
        JSONObject SjsonObject = new JSONObject();

        try {

            jsonObject.put("usuario", cod_usuario);
            jsonObject.put("version", Version);
            jsonObject.put("fecha", fecha);

            SjsonObject.put("sincroonizacion", jsonObject);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        insertarSincronizacion(SjsonObject, fecha);

    }

    //Funcion que sube el JSON de sinronizacion a la base de datos
    private void insertarSincronizacion(JSONObject json, String fecha) {
        //System.out.println(json);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, "http://cloccidental.com:5001/sincroonizacion", json, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (response != null) {
                    try {
                        JSONObject jsonObject = response.getJSONObject("estado");
                        if (jsonObject.getString("status").equals("404")) {
                            Toast.makeText(SincronizacionActivity.this, "Error 404", LENGTH_LONG).show();
                            varAuxError = true;
                        } else if ((jsonObject.getString("status").equals("200")) && (!jsonObject.getString("usuario").equals(cod_usuario))) {
                            Toast.makeText(SincronizacionActivity.this, "Error inesperado al sincronizar", LENGTH_LONG).show();
                            varAuxError = true;
                        } else if ((jsonObject.getString("status").equals("200")) && (jsonObject.getString("usuario").equals(cod_usuario))) {
                            //System.out.println("Sincronizacion Exitosa");

                            //Guardado de la ultima sincronnizaion en la base de datos
                            SQLiteDatabase ke_android = conn.getWritableDatabase();
                            try {

                                ke_android.beginTransaction();

                                Date fecha_bdd = new SimpleDateFormat("yyyy-MM-dd").parse(fecha);

                                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

                                String str_fecha_bdd = formatter.format(fecha_bdd);


                                ContentValues contenedor = new ContentValues();
                                contenedor.put("ult_sinc", str_fecha_bdd);

                                ke_android.update("usuarios", contenedor, "vendedor = ?", new String[]{cod_usuario});

                                ke_android.setTransactionSuccessful();

                            } catch (Exception exception) {
                                exception.printStackTrace();
                            } finally {
                                ke_android.endTransaction();
                                ke_android.close();
                            }

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(SincronizacionActivity.this, "No ha logrado sincronizar", LENGTH_LONG).show();
                varAuxError = true;
                AnalisisError2();
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);

        varAux++;
        sincronizacionVendedor();
    }

    private void BajarArticulos2(String URL) {
        //Texto de la ventana de actualizacion de datos
        progressDialog.setMessage("Sincronizando articulos");
        tv_articulos.setTextColor(Color.rgb(41, 184, 214));
        tv_articulos.setText("Articulos: Sincronizando");
        //Contador de articulos actualizados
        contadorart = 0;
        //Objeto que baja los articulos
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(URL, new Response.Listener<JSONArray>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(JSONArray response) {//Funcion si  la respuesta de la API es existosa
                //Verificacion de que la repuesta de la API no sea nula
                if (response != null) {
                    //Variable que guarda la coexion
                    conn = new AdminSQLiteOpenHelper(getApplicationContext(), "ke_android", null, 8);
                    //Objeto que creara las sentencias SQL
                    SQLiteDatabase ke_android = conn.getWritableDatabase();
                    //Creacion del objeto JSON que contendra la descomposicion del array JSON enviada por el servidor
                    JSONObject jsonObject = null;
                    //Descomposicion del array JSON enviada por el servidor
                    for (int i = 0; i < response.length(); i++) {
                        //TRY para tratar los movimientos de los datos traidos por el JSON y guardarlos en variables
                        try {
                            //Guardado de una parte del JSON array en un JSON object
                            jsonObject = response.getJSONObject(i);
                            //Guardado en variables de los datos del JSON object
                            codigo = jsonObject.getString("codigo").trim();
                            grupo = jsonObject.getString("grupo").trim();
                            subgrupo = jsonObject.getString("subgrupo").trim();
                            nombre = jsonObject.getString("nombre").trim();
                            marca = jsonObject.getString("marca").trim();
                            referencia = jsonObject.getString("referencia").trim();
                            unidad = jsonObject.getString("unidad").trim();
                            precio1 = jsonObject.getDouble("precio1");
                            precio2 = jsonObject.getDouble("precio2");
                            precio3 = jsonObject.getDouble("precio3");
                            precio4 = jsonObject.getDouble("precio4");
                            precio5 = jsonObject.getDouble("precio5");
                            precio6 = jsonObject.getDouble("precio6");
                            precio7 = jsonObject.getDouble("precio7");
                            existencia = jsonObject.getDouble("existencia");
                            fechamodifi = jsonObject.getString("fechamodifi");
                            discont = jsonObject.getDouble("discont");
                            vta_max = jsonObject.getDouble("vta_max");
                            vta_min = jsonObject.getDouble("vta_min");
                            dctotope = jsonObject.getDouble("dctotope");
                            enpreventa = jsonObject.getString("enpreventa").trim();
                            comprometido = jsonObject.getString("comprometido");
                            vta_minenx = jsonObject.getString("vta_minenx");

                            //Creacion del objeto contedor (en si un array del tipo MAP que guarda el nombre del campo de la base de datos y el valor de la ariable que guardara en la misma)
                            ContentValues contenedor = new ContentValues();
                            //LLenado del objeto contenedor
                            contenedor.put("codigo", codigo);
                            contenedor.put("grupo", grupo);
                            contenedor.put("subgrupo", subgrupo);
                            contenedor.put("nombre", nombre);
                            contenedor.put("referencia", referencia);
                            contenedor.put("marca", marca);
                            contenedor.put("unidad", unidad);
                            contenedor.put("precio1", precio1);
                            contenedor.put("precio2", precio2);
                            contenedor.put("precio3", precio3);
                            contenedor.put("precio4", precio4);
                            contenedor.put("precio5", precio5);
                            contenedor.put("precio6", precio6);
                            contenedor.put("precio7", precio7);
                            contenedor.put("discont", discont);
                            contenedor.put("fechamodifi", fechamodifi);
                            contenedor.put("existencia", existencia);
                            contenedor.put("discont", discont);
                            contenedor.put("vta_max", vta_max);
                            contenedor.put("vta_min", vta_min);
                            contenedor.put("dctotope", dctotope);
                            contenedor.put("enpreventa", enpreventa);
                            contenedor.put("comprometido", comprometido);
                            contenedor.put("vta_minenx", vta_minenx);

                            //Creacion del cursor que ejecutara la sentencia SQL que busca ver si el articulo existe o no en la base de datos, buscandolo por medio de su articulo
                            Cursor codigo_en_local = ke_android.rawQuery("SELECT count(codigo), fechamodifi FROM articulo WHERE codigo = '" + codigo + "'", null);
                            //Posicionamiento del cursor sobre su primer resultado
                            codigo_en_local.moveToFirst();
                            //Guardado en variable del numero de coincidencias encontradas por la busqueda del codigo (1 para indicar que existe)
                            int codigo_existente = codigo_en_local.getInt(0);
                            String S_Fecha_bdd = codigo_en_local.getString(1);

                            codigo_en_local.close();

                            if (codigo_existente == 0) {

                                try {

                                    //System.out.println("El codigo: " + codigo + " hizo INSERT");

                                    ke_android.beginTransaction();

                                    ke_android.insert("articulo", null, contenedor);

                                    ke_android.setTransactionSuccessful();
                                    contadorart++;

                                } catch (Exception exception) {
                                    exception.printStackTrace();
                                } finally {
                                    ke_android.endTransaction();
                                }
                            } else {

                                Date Fecha_bdd = ParseFecha(S_Fecha_bdd);
                                Date Fecha_api = ParseFecha(fechamodifi);

                                if (Fecha_bdd.before(Fecha_api)) {

                                    try {

                                        //System.out.println("El codigo: " + codigo + " hizo UPDATE");

                                        ke_android.beginTransaction();

                                        ke_android.update("articulo", contenedor, "codigo = ?", new String[]{codigo});

                                        ke_android.setTransactionSuccessful();
                                        contadorart++;

                                    } catch (Exception exception) {
                                        exception.printStackTrace();
                                    } finally {
                                        ke_android.endTransaction();
                                    }

                                } else {
                                    //System.out.println("El codigo: " + codigo + " hizo OUT");
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    ke_android.close();

                    tv_articulos.setTextColor(Color.rgb(62, 197, 58));
                    tv_articulos.setText("Articulos: " + contadorart);
                    progressDialog.setMessage("Articulos:" + contadorart);
                    varAux++;
                    progressDialog.incrementProgressBy((int) numBarraProgreso);
                    sincronizacionVendedor();


                } else if (response == null) {

                    tv_articulos.setTextColor(Color.rgb(98, 117, 141));
                    tv_articulos.setText("Articulos: Sin actualización");
                    progressDialog.setMessage("Articulos: Sin actualización");
                    varAux++;
                    progressDialog.incrementProgressBy((int) numBarraProgreso);
                    sincronizacionVendedor();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {


                tv_articulos.setTextColor(Color.rgb(98, 117, 141));
                tv_articulos.setText("Articulos: Sin actualización");
                progressDialog.setMessage("Articulos: Sin actualización");
                varAux++;
                progressDialog.incrementProgressBy((int) numBarraProgreso);
                sincronizacionVendedor();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {  //finalmente, estos son los parametros que le enviaremos al webservice, partiendo de las variables
                //donde estan guardados las fechas
                Map<String, String> parametros = new HashMap<String, String>();
                parametros.put("fecha_sinc", fecha_sinc_articulo);

                return parametros;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonArrayRequest); //esto es el request que se envia al url a traves de la conexion volley, (el stringrequest esta armado arriba)

    }

    private void calendario(SQLiteDatabase ke_android) {
        Calendar fecha_articulo = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        String fechaArticulo = sdf.format(fecha_articulo.getTime());

        ContentValues actualizarFecha = new ContentValues();
        actualizarFecha.put("fchhn_ultmod", fechaArticulo);
        ke_android.update("tabla_aux", actualizarFecha, "tabla = ?", new String[]{"articulo"});
        contadorart++;
    }

    private void BajarClientes(String URL) {

        System.out.println("Documentos -> " + URL);
        //final ArrayList<String> documentosBDD = arrayDocumento();

        ArrayList<String> ClientesNube = new ArrayList<>();

        progressDialog.setMessage("Sincronizando Documentos");

        tvDocumentos.setTextColor(Color.rgb(41, 184, 214));
        tvDocumentos.setText("Documentos: Sincronizando");
        //Fecha tomada para ser coloada en tabla auxiliar en caso de dar un error
        //String fecha_error = ObtenerFechaPreError("limites");
        SQLiteDatabase ke_android = conn.getWritableDatabase();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (!(response.getString("clientes").equals("null"))) {
                        int countDoc = 0;
                        JSONArray clientes = response.getJSONArray("clientes");

                        for (int i = 0; i < clientes.length(); i++) {
                            JSONObject jsonObject = clientes.getJSONObject(i);

                            try {

                                ke_android.beginTransaction();

                                codigo = jsonObject.getString("codigo");
                                nombre = jsonObject.getString("nombre");
                                direccion = jsonObject.getString("direccion");
                                telefonos = jsonObject.getString("telefonos");
                                perscont = jsonObject.getString("perscont");
                                vendedor = jsonObject.getString("vendedor");
                                contribespecial = jsonObject.getDouble("contribespecial");
                                status = jsonObject.getDouble("status");
                                sector = jsonObject.getString("sector");
                                subcodigo = jsonObject.getString("subcodigo");
                                fechamodifi = jsonObject.getString("fechamodifi");
                                precio = jsonObject.getDouble("precio");
                                kne_activa = jsonObject.getString("kne_activa");
                                kne_mtomin = jsonObject.getDouble("kne_mtomin");
                                int noemifac = jsonObject.getInt("noemifac");
                                int noeminota = jsonObject.getInt("noeminota");

                                ClientesNube.add(codigo);

                                ContentValues contenedor = new ContentValues();
                                contenedor.put("codigo", codigo);
                                contenedor.put("nombre", nombre);
                                contenedor.put("direccion", direccion);
                                contenedor.put("telefonos", telefonos);
                                contenedor.put("perscont", perscont);
                                contenedor.put("vendedor", vendedor);
                                contenedor.put("contribespecial", contribespecial);
                                contenedor.put("status", status);
                                contenedor.put("sector", sector);
                                contenedor.put("subcodigo", subcodigo);
                                contenedor.put("fechamodifi", fechamodifi);
                                contenedor.put("precio", precio);
                                contenedor.put("kne_activa", kne_activa);
                                contenedor.put("kne_mtomin", kne_mtomin);
                                contenedor.put("noemifac", noemifac);
                                contenedor.put("noeminota", noeminota);

                                Cursor qcodigoLocal = ke_android.rawQuery("SELECT count(codigo) FROM cliempre WHERE codigo = '" + codigo + "';", null);
                                int codigoExistente = 0;
                                if (qcodigoLocal.moveToFirst()) {
                                    codigoExistente = qcodigoLocal.getInt(0);
                                }
                                qcodigoLocal.close();

                                if (codigoExistente > 0) {
                                    //System.out.println("UPDATE " + documento);
                                    ke_android.update("cliempre", contenedor, "codigo= ?", new String[]{codigo});
                                } else if (codigoExistente == 0) {
                                    //System.out.println("INSERT " + documento);
                                    ke_android.insert("cliempre", null, contenedor);
                                }
                                countDoc++;

                                ke_android.setTransactionSuccessful();


                            } catch (JSONException e) {
                                Toast.makeText(getApplicationContext(), "Error 16", LENGTH_LONG).show();
                            } finally {
                                ke_android.endTransaction();
                            }

                        }
                        ke_android.delete("cliempre", "status= ?", new String[]{("2")});
                        eliminarDocViejos(ClientesNube, ke_android, "cliempre", "codigo");
                        tv_cliente.setTextColor(Color.rgb(62, 197, 58));
                        tv_cliente.setText("Clientes: " + countDoc);
                        progressDialog.setMessage("Clientes: " + countDoc);
                        varAux++;
                        progressDialog.incrementProgressBy((int) numBarraProgreso);
                        sincronizacionVendedor();

                    } else if (response.getString("clientes").equals("null")) {

                        tv_cliente.setTextColor(Color.rgb(98, 117, 141));
                        tv_cliente.setText("Clientes: Sin actualización");
                        progressDialog.setMessage("Clientes: Sin actualización");
                        varAux++;
                        progressDialog.incrementProgressBy((int) numBarraProgreso);
                        sincronizacionVendedor();

                    }
                } catch (Exception e) {
                    //System.out.println("Error Bajar Documento -> " + e);
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //System.out.println("Este es el error -> "+error);
                //Ingreso de la fecha antes de ser actualizada
                //ActualizarFechaError(fecha_error);

                //--Manejo visual que indica al usuario del error--
                tv_cliente.setTextColor(Color.rgb(232, 17, 35));
                tv_cliente.setText("Clientes: No ha logrado sincronizar");
                progressDialog.setMessage("Clientes: No ha logrado sincronizar");
                varAux++;
                progressDialog.incrementProgressBy((int) numBarraProgreso);
                varAuxError = true;
                AnalisisError2();
                //-----
                sincronizacionVendedor();
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }
    //2023-06-07 Se comenta debido a que se va a mejorar para eliminar los clientes que cambian de vendedor, ya que estos permanecian con su antiguo vendedor
    /*private void BajarClientes(String URL) {
        //Fecha tomada para ser coloada en tabla auxiliar en caso de dar un error
        String fecha_error = ObtenerFechaPreError("cliempre");

        System.out.println("CLIENTES ->" + URL);

        contadorcli = 0;

        tv_cliente.setTextColor(Color.rgb(41, 184, 214));
        tv_cliente.setText("Clientes: Sincronizando.");


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(JSONObject response) { //a traves de un json array request, traemos la informacion que viene del webservice

                try {
                    if (!(response.getString("clientes").equals("null"))) { // si la respuesta no viene vacia

                        conn = new AdminSQLiteOpenHelper(getApplicationContext(), "ke_android", null, 1);
                        SQLiteDatabase ke_android = conn.getWritableDatabase();
                        long filas = DatabaseUtils.queryNumEntries(ke_android, "cliempre"); //obtenemos las filas de la tabla articulos para comprobar si hay o no registros

                        JSONArray clientes = response.getJSONArray("clientes");

                        //aqui valido las filas de la tabla de articulos en el telefono
                        if (filas > 0) {
                            JSONObject jsonObject = null; //creamos un objeto json vacio
                            for (int i = 0; i < clientes.length(); i++) { *//*pongo todo en el objeto segun lo que venga *//*
                                try {

                                    ke_android.beginTransaction();

                                    jsonObject = clientes.getJSONObject(i);
                                    codigo = jsonObject.getString("codigo").trim();
                                    nombre = jsonObject.getString("nombre").trim();
                                    direccion = jsonObject.getString("direccion").trim();
                                    telefonos = jsonObject.getString("telefonos").trim();
                                    perscont = jsonObject.getString("perscont").trim();
                                    vendedor = jsonObject.getString("vendedor").trim();
                                    contribespecial = jsonObject.getDouble("contribespecial");
                                    status = jsonObject.getDouble("status");
                                    sector = jsonObject.getString("sector").trim();
                                    subcodigo = jsonObject.getString("subcodigo").trim();
                                    fechamodifi = jsonObject.getString("fechamodifi");
                                    precio = jsonObject.getDouble("precio");
                                    kne_activa = jsonObject.getString("kne_activa");
                                    kne_mtomin = jsonObject.getDouble("kne_mtomin");


                                    ContentValues actualizar = new ContentValues();
                                    actualizar.put("codigo", codigo);
                                    actualizar.put("nombre", nombre);
                                    actualizar.put("direccion", direccion);
                                    actualizar.put("telefonos", telefonos);
                                    actualizar.put("perscont", perscont);
                                    actualizar.put("vendedor", vendedor);
                                    actualizar.put("contribespecial", contribespecial);
                                    actualizar.put("status", status);
                                    actualizar.put("sector", sector);
                                    actualizar.put("subcodigo", subcodigo);
                                    actualizar.put("fechamodifi", fechamodifi);
                                    actualizar.put("precio", precio);
                                    actualizar.put("kne_activa", kne_activa);
                                    actualizar.put("kne_mtomin", kne_mtomin);


                                    ke_android.update("cliempre", actualizar, "codigo = '" + codigo + "'", null);

                                    //actualizamos la fecha de la tabla de
                                    Calendar fecha_cliempre = Calendar.getInstance();
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                    String fechaCliempre = sdf.format(fecha_cliempre.getTime());

                                    ContentValues actualizarFecha = new ContentValues();
                                    actualizarFecha.put("fchhn_ultmod", fechaCliempre);
                                    ke_android.update("tabla_aux", actualizarFecha, "tabla = 'cliempre'", null);

                                    ke_android.setTransactionSuccessful();


                                } catch (JSONException e) {
                                    Toast.makeText(getApplicationContext(), "Error 16", LENGTH_LONG).show();
                                } finally {
                                    ke_android.endTransaction();
                                }


                                Cursor codigo_en_local = ke_android.rawQuery("SELECT count(codigo) FROM cliempre WHERE codigo = '" + codigo + "'", null);
                                codigo_en_local.moveToFirst();
                                int codigo_existente = codigo_en_local.getInt(0);

                                if (codigo_existente > 0) {
                                    try {

                                        ke_android.beginTransaction();

                                        jsonObject = clientes.getJSONObject(i);
                                        codigo = jsonObject.getString("codigo").trim();
                                        nombre = jsonObject.getString("nombre").trim();
                                        direccion = jsonObject.getString("direccion").trim();
                                        telefonos = jsonObject.getString("telefonos").trim();
                                        perscont = jsonObject.getString("perscont").trim();
                                        vendedor = jsonObject.getString("vendedor").trim();
                                        contribespecial = jsonObject.getDouble("contribespecial");
                                        status = jsonObject.getDouble("status");
                                        sector = jsonObject.getString("sector").trim();
                                        subcodigo = jsonObject.getString("subcodigo").trim();
                                        fechamodifi = jsonObject.getString("fechamodifi");
                                        precio = jsonObject.getDouble("precio");
                                        kne_activa = jsonObject.getString("kne_activa");
                                        kne_mtomin = jsonObject.getDouble("kne_mtomin");


                                        ContentValues actualizar = new ContentValues();
                                        actualizar.put("codigo", codigo);
                                        actualizar.put("nombre", nombre);
                                        actualizar.put("direccion", direccion);
                                        actualizar.put("telefonos", telefonos);
                                        actualizar.put("perscont", perscont);
                                        actualizar.put("vendedor", vendedor);
                                        actualizar.put("contribespecial", contribespecial);
                                        actualizar.put("status", status);
                                        actualizar.put("sector", sector);
                                        actualizar.put("subcodigo", subcodigo);
                                        actualizar.put("fechamodifi", fechamodifi);
                                        actualizar.put("precio", precio);
                                        actualizar.put("kne_activa", kne_activa);
                                        actualizar.put("kne_mtomin", kne_mtomin);


                                        ke_android.update("cliempre", actualizar, "codigo = '" + codigo + "'", null);

                                        //actualizamos la fecha de la tabla de
                                        Calendar fecha_cliempre = Calendar.getInstance();
                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                        String fechaCliempre = sdf.format(fecha_cliempre.getTime());

                                        ContentValues actualizarFecha = new ContentValues();
                                        actualizarFecha.put("fchhn_ultmod", fechaCliempre);
                                        ke_android.update("tabla_aux", actualizarFecha, "tabla = 'cliempre'", null);
                                        ke_android.setTransactionSuccessful();
                                        contadorcli++;


                                    } catch (JSONException e) {
                                        Toast.makeText(getApplicationContext(), e.getMessage(), LENGTH_LONG).show();
                                    } finally {
                                        ke_android.endTransaction();
                                    }


                                } else {
                                    try {

                                        ke_android.beginTransaction();

                                        jsonObject = clientes.getJSONObject(i);
                                        codigo = jsonObject.getString("codigo").trim();
                                        nombre = jsonObject.getString("nombre").trim();
                                        direccion = jsonObject.getString("direccion").trim();
                                        telefonos = jsonObject.getString("telefonos").trim();
                                        perscont = jsonObject.getString("perscont").trim();
                                        vendedor = jsonObject.getString("vendedor").trim();
                                        contribespecial = jsonObject.getDouble("contribespecial");
                                        status = jsonObject.getDouble("status");
                                        sector = jsonObject.getString("sector").trim();
                                        subcodigo = jsonObject.getString("subcodigo").trim();
                                        fechamodifi = jsonObject.getString("fechamodifi");
                                        precio = jsonObject.getDouble("precio");
                                        kne_activa = jsonObject.getString("kne_activa");
                                        kne_mtomin = jsonObject.getDouble("kne_mtomin");

                                        ContentValues insertar = new ContentValues();
                                        insertar.put("codigo", codigo);
                                        insertar.put("nombre", nombre);
                                        insertar.put("direccion", direccion);
                                        insertar.put("telefonos", telefonos);
                                        insertar.put("perscont", perscont);
                                        insertar.put("vendedor", vendedor);
                                        insertar.put("contribespecial", contribespecial);
                                        insertar.put("status", status);
                                        insertar.put("sector", sector);
                                        insertar.put("subcodigo", subcodigo);
                                        insertar.put("fechamodifi", fechamodifi);
                                        insertar.put("precio", precio);
                                        insertar.put("kne_activa", kne_activa);
                                        insertar.put("kne_mtomin", kne_mtomin);

                                        ke_android.insert("cliempre", null, insertar);

                                        //actualizamos la fecha de la tabla de
                                        Calendar fecha_cliempre = Calendar.getInstance();
                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                        String fechaCliempre = sdf.format(fecha_cliempre.getTime());

                                        ContentValues actualizarFecha = new ContentValues();
                                        actualizarFecha.put("fchhn_ultmod", fechaCliempre);
                                        ke_android.update("tabla_aux", actualizarFecha, "tabla = 'cliempre'", null);

                                        ke_android.setTransactionSuccessful();
                                        contadorcli++;

                                    } catch (JSONException e) {
                                        Toast.makeText(getApplicationContext(), "Error 17", LENGTH_LONG).show();
                                    } finally {
                                        ke_android.endTransaction();
                                    }


                                }

                            }
                            //  Toast.makeText(PrincipalActivity.this, "Clientes Descargados", Toast.LENGTH_SHORT).show();
                            ke_android.close();

                            tv_cliente.setTextColor(Color.rgb(62, 197, 58));
                            tv_cliente.setText("Clientes: " + contadorcli);
                            progressDialog.setMessage("Clientes: " + contadorcli);
                            varAux++;
                            progressDialog.incrementProgressBy((int) numBarraProgreso);
                            sincronizacionVendedor();

                        } else {

                            //si no hay nada, hago un insert
                            AdminSQLiteOpenHelper conn = new AdminSQLiteOpenHelper(getApplicationContext(), "ke_android", null, 1);
                            ke_android = conn.getWritableDatabase();

                            JSONObject jsonObject = null; //creamos un objeto json vacio
                            for (int i = 0; i < clientes.length(); i++) { *//*pongo todo en el objeto segun lo que venga *//*
                                try {

                                    ke_android.beginTransaction();

                                    jsonObject = clientes.getJSONObject(i);
                                    codigo = jsonObject.getString("codigo").trim();
                                    nombre = jsonObject.getString("nombre").trim();
                                    direccion = jsonObject.getString("direccion").trim();
                                    telefonos = jsonObject.getString("telefonos").trim();
                                    perscont = jsonObject.getString("perscont").trim();
                                    vendedor = jsonObject.getString("vendedor").trim();
                                    contribespecial = jsonObject.getDouble("contribespecial");
                                    status = jsonObject.getDouble("status");
                                    sector = jsonObject.getString("sector").trim();
                                    subcodigo = jsonObject.getString("subcodigo").trim();
                                    fechamodifi = jsonObject.getString("fechamodifi");
                                    precio = jsonObject.getDouble("precio");
                                    kne_activa = jsonObject.getString("kne_activa");
                                    kne_mtomin = jsonObject.getDouble("kne_mtomin");

                                    ContentValues insertar = new ContentValues();
                                    insertar.put("codigo", codigo);
                                    insertar.put("nombre", nombre);
                                    insertar.put("direccion", direccion);
                                    insertar.put("telefonos", telefonos);
                                    insertar.put("perscont", perscont);
                                    insertar.put("vendedor", vendedor);
                                    insertar.put("contribespecial", contribespecial);
                                    insertar.put("status", status);
                                    insertar.put("sector", sector);
                                    insertar.put("subcodigo", subcodigo);
                                    insertar.put("fechamodifi", fechamodifi);
                                    insertar.put("precio", precio);
                                    insertar.put("kne_activa", kne_activa);
                                    insertar.put("kne_mtomin", kne_mtomin);

                                    ke_android.insert("cliempre", null, insertar);

                                    //actualizamos la fecha de la tabla de
                                    Calendar fecha_cliempre = Calendar.getInstance();
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                    String fechaCliempre = sdf.format(fecha_cliempre.getTime());

                                    ContentValues actualizarFecha = new ContentValues();
                                    actualizarFecha.put("fchhn_ultmod", fechaCliempre);
                                    ke_android.update("tabla_aux", actualizarFecha, "tabla = 'cliempre'", null);
                                    ke_android.setTransactionSuccessful();
                                    contadorcli++;

                                } catch (JSONException e) {
                                    Toast.makeText(getApplicationContext(), "Error 18", LENGTH_LONG).show();
                                } finally {
                                    ke_android.endTransaction();
                                }


                            }
                            //  Toast.makeText(PrincipalActivity.this, "Clientes Descargados", Toast.LENGTH_SHORT).show();


                            ke_android.close();

                            tv_cliente.setTextColor(Color.rgb(62, 197, 58));
                            tv_cliente.setText("Clientes: " + contadorcli);
                            progressDialog.setMessage("Clientes: " + contadorcli);
                            varAux++;
                            progressDialog.incrementProgressBy((int) numBarraProgreso);
                            sincronizacionVendedor();
                        }
                    } else if (response.getString("clientes").equals("null")) {

                        //Toast.makeText(getApplicationContext(), "No se recibieron mas datos", LENGTH_LONG).show();
                        *//* si en la consulta no ncuentra nada
                        es que el usuario o password estan incorrectos *//*


                        tv_cliente.setTextColor(Color.rgb(98, 117, 141));
                        tv_cliente.setText("Clientes: Sin actualización.");
                        progressDialog.setMessage("Clientes: Sin actualización.");
                        varAux++;
                        progressDialog.incrementProgressBy((int) numBarraProgreso);
                        sincronizacionVendedor();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Ingreso de la fecha antes de ser actualizada
                ActualizarFechaError(fecha_error);

                //--Manejo visual que indica al usuario del error--
                tv_cliente.setTextColor(Color.rgb(232, 17, 35));
                tv_cliente.setText("Clientes: No ha logrado sincronizar.");
                progressDialog.setMessage("Clientes: No ha logrado sincronizar.");
                varAux++;
                progressDialog.incrementProgressBy((int) numBarraProgreso);
                varAuxError = true;
                AnalisisError2();
                //------
                sincronizacionVendedor();
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
        requestQueue.add(jsonObjectRequest); //esto es el request que se envia al url a traves de la conexion volley, (el stringrequest esta armado arriba)


    }*/

    private void BajarGrupos(String URL) {
        //Fecha tomada para ser coloada en tabla auxiliar en caso de dar un error
        String fecha_error = ObtenerFechaPreError("grupos");


        tv_grupos.setTextColor(Color.rgb(41, 184, 214));
        tv_grupos.setText("Grupos: Sincronizando");


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(JSONObject response) { //a traves de un json array request, traemos la informacion que viene del webservice

                try {
                    if (!(response.getString("grupos").equals("null"))) { // si la respuesta no viene vacia

                        conn = new AdminSQLiteOpenHelper(getApplicationContext(), "ke_android", null, 1);
                        SQLiteDatabase ke_android = conn.getWritableDatabase();
                        long filas = DatabaseUtils.queryNumEntries(ke_android, "grupos"); //obtenemos las filas de la tabla articulos para comprobar si hay o no registros

                        JSONArray grupos = response.getJSONArray("grupos");

                        //aqui valido las filas de la tabla de sectores en el telefono
                        if (filas > 0) {

                            JSONObject jsonObject = null; //creamos un objeto json vacio
                            for (int i = 0; i < grupos.length(); i++) { /*pongo todo en el objeto segun lo que venga */
                                try {

                                    ke_android.beginTransaction();

                                    jsonObject = grupos.getJSONObject(i);
                                    codigo = jsonObject.getString("codigo").trim();
                                    nombre = jsonObject.getString("nombre").trim();
                                    fechamodifi = jsonObject.getString("fechamodifi");


                                    ContentValues actualizar = new ContentValues();
                                    actualizar.put("codigo", codigo);
                                    actualizar.put("nombre", nombre);
                                    actualizar.put("fechamodifi", fechamodifi);

                                    ke_android.update("grupos", actualizar, "codigo = '" + codigo + "'", null);
                                    //actualizamos la fecha de la tabla de
                                    Calendar fecha_grupos = Calendar.getInstance();
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                    String fechagrupos = sdf.format(fecha_grupos.getTime());

                                    ContentValues actualizarFecha = new ContentValues();
                                    actualizarFecha.put("fchhn_ultmod", fechagrupos);
                                    ke_android.update("tabla_aux", actualizarFecha, "tabla = 'grupos'", null);
                                    ke_android.setTransactionSuccessful();


                                } catch (JSONException e) {
                                    Toast.makeText(getApplicationContext(), "Error 19", LENGTH_LONG).show();
                                } finally {
                                    ke_android.endTransaction();
                                }


                                Cursor codigo_en_local = ke_android.rawQuery("SELECT count(codigo) FROM grupos WHERE codigo = '" + codigo + "'", null);
                                codigo_en_local.moveToFirst();
                                int codigo_existente = codigo_en_local.getInt(0);

                                if (codigo_existente > 0) {

                                    try {

                                        ke_android.beginTransaction();

                                        jsonObject = grupos.getJSONObject(i);
                                        codigo = jsonObject.getString("codigo").trim();
                                        nombre = jsonObject.getString("nombre").trim();
                                        fechamodifi = jsonObject.getString("fechamodifi");

                                        ContentValues actualizar = new ContentValues();
                                        actualizar.put("codigo", codigo);
                                        actualizar.put("nombre", nombre);
                                        actualizar.put("fechamodifi", fechamodifi);

                                        ke_android.update("grupos", actualizar, "codigo = '" + codigo + "'", null);
                                        //actualizamos la fecha de la tabla de
                                        Calendar fecha_grupos = Calendar.getInstance();
                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                        String fechagrupos = sdf.format(fecha_grupos.getTime());

                                        ContentValues actualizarFecha = new ContentValues();
                                        actualizarFecha.put("fchhn_ultmod", fechagrupos);
                                        ke_android.update("tabla_aux", actualizarFecha, "tabla = 'grupos'", null);
                                        ke_android.setTransactionSuccessful();


                                    } catch (JSONException e) {
                                        Toast.makeText(getApplicationContext(), e.getMessage(), LENGTH_LONG).show();
                                    } finally {
                                        ke_android.endTransaction();
                                    }


                                } else {

                                    try {
                                        ke_android.beginTransaction();

                                        jsonObject = grupos.getJSONObject(i);
                                        codigo = jsonObject.getString("codigo").trim();
                                        nombre = jsonObject.getString("nombre").trim();
                                        fechamodifi = jsonObject.getString("fechamodifi");


                                        ContentValues insertar = new ContentValues();
                                        insertar.put("codigo", codigo);
                                        insertar.put("nombre", nombre);
                                        insertar.put("fechamodifi", fechamodifi);


                                        ke_android.insert("grupos", null, insertar);

                                        //actualizamos la fecha de la tabla de
                                        Calendar fecha_grupos = Calendar.getInstance();
                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                        String fechagrupos = sdf.format(fecha_grupos.getTime());

                                        ContentValues actualizarFecha = new ContentValues();
                                        actualizarFecha.put("fchhn_ultmod", fechagrupos);
                                        ke_android.update("tabla_aux", actualizarFecha, "tabla = 'grupos'", null);

                                        ke_android.setTransactionSuccessful();


                                    } catch (JSONException e) {
                                        Toast.makeText(getApplicationContext(), "Error 20", LENGTH_LONG).show();
                                    } finally {
                                        ke_android.endTransaction();
                                    }

                                }

                            }
                            /// Toast.makeText(PrincipalActivity.this, "grupos Descargados", Toast.LENGTH_SHORT).show();
                            ke_android.close();
                            //  Clientes.setEnabled(true);
                            tv_grupos.setTextColor(Color.rgb(62, 197, 58));
                            tv_grupos.setText("Grupos: Sincronizado");
                            progressDialog.setMessage("Grupos act.");
                            varAux++;
                            progressDialog.incrementProgressBy((int) numBarraProgreso);
                            sincronizacionVendedor();

                        } else {

                            //si no hay nada, hago un insert
                            AdminSQLiteOpenHelper conn = new AdminSQLiteOpenHelper(getApplicationContext(), "ke_android", null, 1);
                            ke_android = conn.getWritableDatabase();

                            JSONObject jsonObject = null; //creamos un objeto json vacio
                            for (int i = 0; i < grupos.length(); i++) { /*pongo todo en el objeto segun lo que venga */
                                try {


                                    ke_android.beginTransaction();
                                    jsonObject = grupos.getJSONObject(i);
                                    codigo = jsonObject.getString("codigo").trim();
                                    nombre = jsonObject.getString("nombre").trim();
                                    fechamodifi = jsonObject.getString("fechamodifi");

                                    ContentValues insertar = new ContentValues();
                                    insertar.put("codigo", codigo);
                                    insertar.put("nombre", nombre);
                                    insertar.put("fechamodifi", fechamodifi);


                                    ke_android.insert("grupos", null, insertar);

                                    //actualizamos la fecha de la tabla de
                                    Calendar fecha_grupos = Calendar.getInstance();
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                    String fechagrupos = sdf.format(fecha_grupos.getTime());

                                    ContentValues actualizarFecha = new ContentValues();
                                    actualizarFecha.put("fchhn_ultmod", fechagrupos);
                                    ke_android.update("tabla_aux", actualizarFecha, "tabla = 'grupos'", null);
                                    ke_android.setTransactionSuccessful();

                                } catch (JSONException e) {
                                    Toast.makeText(getApplicationContext(), "Error 21", LENGTH_LONG).show();
                                } finally {
                                    ke_android.endTransaction();
                                }


                            }
                            //   Toast.makeText(PrincipalActivity.this, "grupos Descargados", Toast.LENGTH_SHORT).show();


                            ke_android.close();
                            // Clientes.setEnabled(true);
                            tv_grupos.setTextColor(Color.rgb(62, 197, 58));
                            tv_grupos.setText("Grupos: Sincronizado");
                            progressDialog.setMessage("Grupos act.");
                            varAux++;
                            progressDialog.incrementProgressBy((int) numBarraProgreso);
                            sincronizacionVendedor();
                        }
                    } else if ((response.getString("grupos").equals("null"))) {

                        //Toast.makeText(getApplicationContext(), "No se recibieron mas datos", LENGTH_LONG).show();
                        /* si en la consulta no ncuentra nada
                        es que el usuario o password estan incorrectos */

                        //  Clientes.setEnabled(true);
                        tv_grupos.setTextColor(Color.rgb(98, 117, 141));
                        tv_grupos.setText("Grupos: Sin actualización");
                        progressDialog.setMessage("Grupos: Sin actualización");
                        varAux++;
                        progressDialog.incrementProgressBy((int) numBarraProgreso);
                        sincronizacionVendedor();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Ingreso de la fecha antes de ser actualizada
                ActualizarFechaError(fecha_error);

                // Clientes.setEnabled(true);
                //--Manejo visual que indica al usuario del error--
                tv_grupos.setTextColor(Color.rgb(232, 17, 35));
                tv_grupos.setText("Grupos: No ha logrado sincronizar");
                progressDialog.setMessage("Grupos: No ha logrado sincronizar");
                varAux++;
                progressDialog.incrementProgressBy((int) numBarraProgreso);
                varAuxError = true;
                AnalisisError2();
                //------
                sincronizacionVendedor();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {  //finalmente, estos son los parametros que le enviaremos al webservice, partiendo de las variables
                //donde estan guardados las fechas
                Map<String, String> parametros = new HashMap<String, String>();


                return parametros;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest); //esto es el request que se envia al url a traves de la conexion volley, (el stringrequest esta armado arriba)
    }
//-----------------------------------------------------------------------------------------------------------------------


    private void BajarSubGrupos(String URL) {
        //Fecha tomada para ser coloada en tabla auxiliar en caso de dar un error
        String fecha_error = ObtenerFechaPreError("subgrupos");


        tv_subgrupos.setTextColor(Color.rgb(41, 184, 214));
        tv_subgrupos.setText("Info. Adcional: Sincronizando.");


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(JSONObject response) { //a traves de un json array request, traemos la informacion que viene del webservice
                try {
                    if (!(response.getString("subgrupo").equals("null"))) { // si la respuesta no viene vacia

                        conn = new AdminSQLiteOpenHelper(getApplicationContext(), "ke_android", null, 1);
                        SQLiteDatabase ke_android = conn.getWritableDatabase();
                        long filas = DatabaseUtils.queryNumEntries(ke_android, "subgrupos"); //obtenemos las filas de la tabla articulos para comprobar si hay o no registros

                        JSONArray subgrupoArray = response.getJSONArray("subgrupo");

                        //aqui valido las filas de la tabla de articulos en el telefono
                        if (filas > 0) {

                            JSONObject jsonObject = null; //creamos un objeto json vacio
                            for (int i = 0; i < subgrupoArray.length(); i++) { /*pongo todo en el objeto segun lo que venga */
                                try {

                                    ke_android.beginTransaction();
                                    jsonObject = subgrupoArray.getJSONObject(i);
                                    codigo = jsonObject.getString("codigo").trim();
                                    subcodigo = jsonObject.getString("subcodigo").trim();
                                    nombre = jsonObject.getString("nombre").trim();
                                    fechamodifi = jsonObject.getString("fechamodifi");


                                    ContentValues actualizar = new ContentValues();
                                    actualizar.put("codigo", codigo);
                                    actualizar.put("nombre", nombre);
                                    actualizar.put("subcodigo", subcodigo);
                                    actualizar.put("fechamodifi", fechamodifi);


                                    ke_android.update("subgrupos", actualizar, "codigo = '" + codigo + "'", null);


                                    //actualizamos la fecha de la tabla de
                                    Calendar fecha_subgrupos = Calendar.getInstance();
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                    String fechasubgrupos = sdf.format(fecha_subgrupos.getTime());

                                    ContentValues actualizarFecha = new ContentValues();
                                    actualizarFecha.put("fchhn_ultmod", fechasubgrupos);
                                    ke_android.update("tabla_aux", actualizarFecha, "tabla = 'subgrupos'", null);
                                    ke_android.setTransactionSuccessful();

                                } catch (JSONException e) {
                                    Toast.makeText(getApplicationContext(), "Error 22", LENGTH_LONG).show();
                                } finally {
                                    ke_android.endTransaction();
                                }


                                Cursor codigo_en_local = ke_android.rawQuery("SELECT count(codigo) FROM subgrupos WHERE codigo = '" + codigo + "'", null);
                                codigo_en_local.moveToFirst();
                                int codigo_existente = codigo_en_local.getInt(0);

                                if (codigo_existente > 0) {


                                    try {

                                        ke_android.beginTransaction();
                                        jsonObject = subgrupoArray.getJSONObject(i);
                                        codigo = jsonObject.getString("codigo").trim();
                                        subcodigo = jsonObject.getString("subcodigo").trim();
                                        nombre = jsonObject.getString("nombre").trim();
                                        fechamodifi = jsonObject.getString("fechamodifi");


                                        ContentValues actualizar = new ContentValues();
                                        actualizar.put("codigo", codigo);
                                        actualizar.put("nombre", nombre);
                                        actualizar.put("subcodigo", subcodigo);
                                        actualizar.put("fechamodifi", fechamodifi);


                                        ke_android.update("subgrupos", actualizar, "codigo = '" + codigo + "'", null);


                                        //actualizamos la fecha de la tabla de
                                        Calendar fecha_subgrupos = Calendar.getInstance();
                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                        String fechasubgrupos = sdf.format(fecha_subgrupos.getTime());

                                        ContentValues actualizarFecha = new ContentValues();
                                        actualizarFecha.put("fchhn_ultmod", fechasubgrupos);
                                        ke_android.update("tabla_aux", actualizarFecha, "tabla = 'subgrupos'", null);
                                        ke_android.setTransactionSuccessful();

                                    } catch (JSONException e) {
                                        Toast.makeText(getApplicationContext(), "Error 23", LENGTH_LONG).show();
                                    } finally {
                                        ke_android.endTransaction();
                                    }


                                } else {

                                    try {

                                        ke_android.beginTransaction();
                                        jsonObject = subgrupoArray.getJSONObject(i);
                                        codigo = jsonObject.getString("codigo").trim();
                                        subcodigo = jsonObject.getString("subcodigo").trim();
                                        nombre = jsonObject.getString("nombre").trim();
                                        fechamodifi = jsonObject.getString("fechamodifi");

                                        ContentValues insertar = new ContentValues();
                                        insertar.put("codigo", codigo);
                                        insertar.put("subcodigo", subcodigo);
                                        insertar.put("nombre", nombre);
                                        insertar.put("fechamodifi", fechamodifi);


                                        ke_android.insert("subgrupos", null, insertar);

                                        //actualizamos la fecha de la tabla de
                                        Calendar fecha_subgrupos = Calendar.getInstance();
                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                        String fechasubgrupos = sdf.format(fecha_subgrupos.getTime());

                                        ContentValues actualizarFecha = new ContentValues();
                                        actualizarFecha.put("fchhn_ultmod", fechasubgrupos);
                                        ke_android.update("tabla_aux", actualizarFecha, "tabla = 'subgrupos'", null);
                                        ke_android.setTransactionSuccessful();

                                    } catch (JSONException e) {
                                        Toast.makeText(getApplicationContext(), "Error 24", LENGTH_LONG).show();
                                    } finally {
                                        ke_android.endTransaction();
                                    }
                                }

                            }
                            ke_android.close();
                            //  Catalogo.setEnabled(true);
                            tv_subgrupos.setTextColor(Color.rgb(62, 197, 58));
                            tv_subgrupos.setText("Info. Adicional: Sincronizado.");
                            progressDialog.setMessage("Info. Adicional: Sincronizado.");
                            varAux++;
                            progressDialog.incrementProgressBy((int) numBarraProgreso);
                            sincronizacionVendedor();

                        } else {

                            //si no hay nada, hago un insert
                            AdminSQLiteOpenHelper conn = new AdminSQLiteOpenHelper(getApplicationContext(), "ke_android", null, 1);
                            ke_android = conn.getWritableDatabase();


                            JSONObject jsonObject = null; //creamos un objeto json vacio
                            for (int i = 0; i < subgrupoArray.length(); i++) { /*pongo todo en el objeto segun lo que venga */
                                try {


                                    ke_android.beginTransaction();
                                    jsonObject = subgrupoArray.getJSONObject(i);
                                    codigo = jsonObject.getString("codigo").trim();
                                    subcodigo = jsonObject.getString("subcodigo").trim();
                                    nombre = jsonObject.getString("nombre").trim();
                                    fechamodifi = jsonObject.getString("fechamodifi");


                                    ContentValues insertar = new ContentValues();
                                    insertar.put("codigo", codigo);
                                    insertar.put("subcodigo", subcodigo);
                                    insertar.put("nombre", nombre);
                                    insertar.put("fechamodifi", fechamodifi);


                                    ke_android.insert("subgrupos", null, insertar);

                                    //actualizamos la fecha de la tabla de
                                    Calendar fecha_subgrupos = Calendar.getInstance();
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                    String fechasubgrupos = sdf.format(fecha_subgrupos.getTime());

                                    ContentValues actualizarFecha = new ContentValues();
                                    actualizarFecha.put("fchhn_ultmod", fechasubgrupos);
                                    ke_android.update("tabla_aux", actualizarFecha, "tabla = 'subgrupos'", null);
                                    ke_android.setTransactionSuccessful();

                                } catch (JSONException e) {
                                    Toast.makeText(getApplicationContext(), "Error 25", LENGTH_LONG).show();
                                } finally {
                                    ke_android.endTransaction();
                                }


                            }
                            //   Toast.makeText(PrincipalActivity.this, "SubGrupos descargados", Toast.LENGTH_SHORT).show();

                            ke_android.close();
                            // Catalogo.setEnabled(true);
                            tv_subgrupos.setTextColor(Color.rgb(62, 197, 58));
                            tv_subgrupos.setText("Info. Adicional: Sincronizado.");
                            progressDialog.setMessage("Info. Adicional: Sincronizado.");
                            varAux++;
                            progressDialog.incrementProgressBy((int) numBarraProgreso);
                            sincronizacionVendedor();
                        }
                    } else if (response.getString("subgrupo").equals("null")) {

                        //Toast.makeText(getApplicationContext(), "No se recibieron mas datos", LENGTH_LONG).show();
                        /* si en la consulta no ncuentra nada
                        es que el usuario o password estan incorrectos */

                        //    Catalogo.setEnabled(true);
                        tv_subgrupos.setTextColor(Color.rgb(98, 117, 141));
                        tv_subgrupos.setText("Info. Adicional: Sin actualización");
                        progressDialog.setMessage("Info. Adicional: Sin actualización.");
                        varAux++;
                        progressDialog.incrementProgressBy((int) numBarraProgreso);
                        sincronizacionVendedor();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Ingreso de la fecha antes de ser actualizada
                ActualizarFechaError(fecha_error);

                //Catalogo.setEnabled(true);
                //--Manejo visual que indica al usuario del error--
                tv_subgrupos.setTextColor(Color.rgb(232, 17, 35));
                tv_subgrupos.setText("Info. Adicional: No ha logrado sincronizar");
                progressDialog.setMessage("Info. Adicional: No ha logrado sincronizar");
                varAux++;
                progressDialog.incrementProgressBy((int) numBarraProgreso);
                varAuxError = true;
                AnalisisError2();
                //------
                sincronizacionVendedor();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {  //finalmente, estos son los parametros que le enviaremos al webservice, partiendo de las variables
                //donde estan guardados las fechas
                Map<String, String> parametros = new HashMap<String, String>();
                //  parametros.put("fecha_sinc", fecha_sinc);

                return parametros;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest); //esto es el request que se envia al url a traves de la conexion volley, (el stringrequest esta armado arriba)

    }

    //---------------------------------------------------------------------------------------------------
    private void BajarSectores(String URL) {
        //Fecha tomada para ser coloada en tabla auxiliar en caso de dar un error
        String fecha_error = ObtenerFechaPreError("sectores");


        tv_sector.setTextColor(Color.rgb(41, 184, 214));
        tv_sector.setText("Zona: Sincronizando.");


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(JSONObject response) { //a traves de un json array request, traemos la informacion que viene del webservice
                try {
                    if (!(response.getString("sector").equals("null"))) { // si la respuesta no viene vacia

                        conn = new AdminSQLiteOpenHelper(getApplicationContext(), "ke_android", null, 1);
                        SQLiteDatabase ke_android = conn.getWritableDatabase();
                        long filas = DatabaseUtils.queryNumEntries(ke_android, "sectores"); //obtenemos las filas de la tabla articulos para comprobar si hay o no registros

                        JSONArray sector = response.getJSONArray("sector");

                        //aqui valido las filas de la tabla de sectores en el telefono
                        if (filas > 0) {
                            JSONObject jsonObject = null; //creamos un objeto json vacio
                            for (int i = 0; i < sector.length(); i++) { /*pongo todo en el objeto segun lo que venga */
                                try {
                                    ke_android.beginTransaction();
                                    jsonObject = sector.getJSONObject(i);
                                    codigo = jsonObject.getString("codigo").trim();
                                    zona = jsonObject.getString("zona").trim();
                                    fechamodifi = jsonObject.getString("fechamodifi");


                                    ContentValues actualizar = new ContentValues();
                                    actualizar.put("codigo", codigo);
                                    actualizar.put("zona", zona);
                                    actualizar.put("fechamodifi", fechamodifi);


                                    ke_android.update("sectores", actualizar, "codigo = '" + codigo + "'", null);
                                    //actualizamos la fecha de la tabla de
                                    Calendar fecha_Sectores = Calendar.getInstance();
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                    String fechaSectores = sdf.format(fecha_Sectores.getTime());

                                    ContentValues actualizarFecha = new ContentValues();
                                    actualizarFecha.put("fchhn_ultmod", fechaSectores);
                                    ke_android.update("tabla_aux", actualizarFecha, "tabla = 'sectores'", null);
                                    ke_android.setTransactionSuccessful();


                                } catch (JSONException e) {
                                    Toast.makeText(getApplicationContext(), "Error 26", LENGTH_LONG).show();
                                } finally {
                                    ke_android.endTransaction();
                                }


                                Cursor codigo_en_local = ke_android.rawQuery("SELECT count(codigo) FROM sectores WHERE codigo = '" + codigo + "'", null);
                                codigo_en_local.moveToFirst();
                                int codigo_existente = codigo_en_local.getInt(0);

                                if (codigo_existente > 0) {

                                    try {
                                        ke_android.beginTransaction();
                                        jsonObject = sector.getJSONObject(i);
                                        codigo = jsonObject.getString("codigo").trim();
                                        zona = jsonObject.getString("zona").trim();
                                        fechamodifi = jsonObject.getString("fechamodifi");


                                        ContentValues actualizar = new ContentValues();
                                        actualizar.put("codigo", codigo);
                                        actualizar.put("zona", zona);
                                        actualizar.put("fechamodifi", fechamodifi);


                                        ke_android.update("sectores", actualizar, "codigo = '" + codigo + "'", null);
                                        //actualizamos la fecha de la tabla de
                                        Calendar fecha_Sectores = Calendar.getInstance();
                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                        String fechaSectores = sdf.format(fecha_Sectores.getTime());

                                        ContentValues actualizarFecha = new ContentValues();
                                        actualizarFecha.put("fchhn_ultmod", fechaSectores);
                                        ke_android.update("tabla_aux", actualizarFecha, "tabla = 'sectores'", null);
                                        ke_android.setTransactionSuccessful();


                                    } catch (JSONException e) {
                                        Toast.makeText(getApplicationContext(), "Error 27", LENGTH_LONG).show();
                                    } finally {
                                        ke_android.endTransaction();
                                    }


                                } else {
                                    try {
                                        ke_android.beginTransaction();
                                        jsonObject = sector.getJSONObject(i);
                                        codigo = jsonObject.getString("codigo").trim();
                                        zona = jsonObject.getString("zona").trim();
                                        fechamodifi = jsonObject.getString("fechamodifi");


                                        ContentValues insertar = new ContentValues();
                                        insertar.put("codigo", codigo);
                                        insertar.put("zona", zona);
                                        insertar.put("fechamodifi", fechamodifi);


                                        ke_android.insert("sectores", null, insertar);

                                        //actualizamos la fecha de la tabla de
                                        Calendar fecha_Sectores = Calendar.getInstance();
                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                        String fechaSectores = sdf.format(fecha_Sectores.getTime());

                                        ContentValues actualizarFecha = new ContentValues();
                                        actualizarFecha.put("fchhn_ultmod", fechaSectores);
                                        ke_android.update("tabla_aux", actualizarFecha, "tabla = 'sectores'", null);
                                        ke_android.setTransactionSuccessful();

                                    } catch (JSONException e) {
                                        Toast.makeText(getApplicationContext(), "Error 28", LENGTH_LONG).show();
                                    } finally {
                                        ke_android.endTransaction();
                                    }
                                }
                            }
                            // Toast.makeText(PrincipalActivity.this, "Sectores Descargados", Toast.LENGTH_SHORT).show();
                            ke_android.close();

                            tv_sector.setTextColor(Color.rgb(62, 197, 58));
                            tv_sector.setText("Zona: Sincronizado.");
                            progressDialog.setMessage("Zona: Sincronizado.");
                            varAux++;
                            progressDialog.incrementProgressBy((int) numBarraProgreso);
                            sincronizacionVendedor();

                        } else {

                            //si no hay nada, hago un insert
                            AdminSQLiteOpenHelper conn = new AdminSQLiteOpenHelper(getApplicationContext(), "ke_android", null, 3);
                            ke_android = conn.getWritableDatabase();


                            JSONObject jsonObject = null; //creamos un objeto json vacio
                            for (int i = 0; i < sector.length(); i++) { /*pongo todo en el objeto segun lo que venga */
                                try {
                                    ke_android.beginTransaction();
                                    jsonObject = sector.getJSONObject(i);
                                    codigo = jsonObject.getString("codigo").trim();
                                    zona = jsonObject.getString("zona").trim();
                                    fechamodifi = jsonObject.getString("fechamodifi");


                                    ContentValues insertar = new ContentValues();
                                    insertar.put("codigo", codigo);
                                    insertar.put("zona", zona);
                                    insertar.put("fechamodifi", fechamodifi);


                                    ke_android.insert("sectores", null, insertar);

                                    //actualizamos la fecha de la tabla de
                                    Calendar fecha_Sectores = Calendar.getInstance();
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                    String fechaSectores = sdf.format(fecha_Sectores.getTime());

                                    ContentValues actualizarFecha = new ContentValues();
                                    actualizarFecha.put("fchhn_ultmod", fechaSectores);
                                    ke_android.update("tabla_aux", actualizarFecha, "tabla = 'sectores'", null);
                                    ke_android.setTransactionSuccessful();
                                } catch (JSONException e) {
                                    Toast.makeText(getApplicationContext(), "Error 29", LENGTH_LONG).show();
                                } finally {
                                    ke_android.endTransaction();
                                }


                            }
                            // Toast.makeText(PrincipalActivity.this, "Sectores Descargados", Toast.LENGTH_SHORT).show();


                            ke_android.close();

                            tv_sector.setTextColor(Color.rgb(62, 197, 58));
                            tv_sector.setText("Zona: Sincronizado.");
                            progressDialog.setMessage("Zona: Sincronizado.");
                            varAux++;
                            progressDialog.incrementProgressBy((int) numBarraProgreso);
                            sincronizacionVendedor();
                        }
                    } else if (response.getString("sector").equals("null")) {

                        //Toast.makeText(getApplicationContext(), "No se recibieron mas datos", LENGTH_LONG).show(); /* si en la consulta no ncuentra nada
                        // es que el usuario o password estan incorrectos */


                        tv_sector.setTextColor(Color.rgb(98, 117, 141));
                        tv_sector.setText("Zona: Sin actualización.");
                        progressDialog.setMessage("Zona: Sin actualización.");
                        varAux++;
                        progressDialog.incrementProgressBy((int) numBarraProgreso);
                        sincronizacionVendedor();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Ingreso de la fecha antes de ser actualizada
                ActualizarFechaError(fecha_error);

                //--Manejo visual que indica al usuario del error--
                tv_sector.setTextColor(Color.rgb(232, 17, 35));
                tv_sector.setText("Zona: No ha logrado sincronizar");
                progressDialog.setMessage("Zona: No ha logrado sincronizar");
                varAux++;
                progressDialog.incrementProgressBy((int) numBarraProgreso);
                //varAuxError = true;
                //AnalisisError2();
                //------
                sincronizacionVendedor();
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
        requestQueue.add(jsonObjectRequest); //esto es el request que se envia al url a traves de la conexion volley, (el stringrequest esta armado arriba)
    }


    private void BajarSubSectores(String URL) {
        //Fecha tomada para ser coloada en tabla auxiliar en caso de dar un error
        String fecha_error = ObtenerFechaPreError("subsectores");


        tv_subsector.setTextColor(Color.rgb(41, 184, 214));
        tv_subsector.setText("Ruta: Sincronizando.");

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(JSONObject response) { //a traves de un json array request, traemos la informacion que viene del webservice

                try {
                    if (!(response.getString("subsector").equals("null"))) { // si la respuesta no viene vacia

                        conn = new AdminSQLiteOpenHelper(getApplicationContext(), "ke_android", null, 1);
                        SQLiteDatabase ke_android = conn.getWritableDatabase();
                        long filas = DatabaseUtils.queryNumEntries(ke_android, "subsectores"); //obtenemos las filas de la tabla articulos para comprobar si hay o no registros

                        JSONArray subsectorArray = response.getJSONArray("subsector");

                        //aqui valido las filas de la tabla de articulos en el telefono
                        if (filas > 0) {
                            JSONObject jsonObject = null; //creamos un objeto json vacio
                            for (int i = 0; i < subsectorArray.length(); i++) { /*pongo todo en el objeto segun lo que venga */
                                try {
                                    ke_android.beginTransaction();
                                    jsonObject = subsectorArray.getJSONObject(i);
                                    codigo = jsonObject.getString("codigo").trim();
                                    subcodigo = jsonObject.getString("subcodigo").trim();
                                    subsector = jsonObject.getString("subsector").trim();
                                    fechamodifi = jsonObject.getString("fechamodifi");


                                    ContentValues actualizar = new ContentValues();
                                    actualizar.put("codigo", codigo);
                                    actualizar.put("subsector", subsector);
                                    actualizar.put("subcodigo", subcodigo);
                                    actualizar.put("fechamodifi", fechamodifi);

                                    ke_android.update("subsectores", actualizar, "codigo = '" + codigo + "'", null);
                                    //actualizamos la fecha de la tabla de
                                    Calendar fecha_subsectores = Calendar.getInstance();
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                    String fechasubsectores = sdf.format(fecha_subsectores.getTime());

                                    ContentValues actualizarFecha = new ContentValues();
                                    actualizarFecha.put("fchhn_ultmod", fechasubsectores);
                                    ke_android.update("tabla_aux", actualizarFecha, "tabla = 'subsectores'", null);

                                    ke_android.setTransactionSuccessful();

                                } catch (JSONException e) {
                                    Toast.makeText(getApplicationContext(), "Error 30", LENGTH_LONG).show();
                                } finally {
                                    ke_android.endTransaction();
                                }


                                Cursor codigo_en_local = ke_android.rawQuery("SELECT count(codigo) FROM subsectores WHERE codigo = '" + codigo + "'", null);
                                codigo_en_local.moveToFirst();
                                int codigo_existente = codigo_en_local.getInt(0);

                                if (codigo_existente > 0) {

                                    try {
                                        ke_android.beginTransaction();
                                        jsonObject = subsectorArray.getJSONObject(i);
                                        codigo = jsonObject.getString("codigo").trim();
                                        subcodigo = jsonObject.getString("subcodigo").trim();
                                        subsector = jsonObject.getString("subsector").trim();
                                        fechamodifi = jsonObject.getString("fechamodifi");


                                        ContentValues actualizar = new ContentValues();
                                        actualizar.put("codigo", codigo);
                                        actualizar.put("subsector", subsector);
                                        actualizar.put("subcodigo", subcodigo);
                                        actualizar.put("fechamodifi", fechamodifi);

                                        ke_android.update("subsectores", actualizar, "codigo = '" + codigo + "'", null);
                                        //actualizamos la fecha de la tabla de
                                        Calendar fecha_subsectores = Calendar.getInstance();
                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                        String fechasubsectores = sdf.format(fecha_subsectores.getTime());

                                        ContentValues actualizarFecha = new ContentValues();
                                        actualizarFecha.put("fchhn_ultmod", fechasubsectores);
                                        ke_android.update("tabla_aux", actualizarFecha, "tabla = 'subsectores'", null);

                                        ke_android.setTransactionSuccessful();

                                    } catch (JSONException e) {
                                        Toast.makeText(getApplicationContext(), "Error 31", LENGTH_LONG).show();
                                    } finally {
                                        ke_android.endTransaction();
                                    }


                                } else {

                                    try {
                                        ke_android.beginTransaction();
                                        jsonObject = subsectorArray.getJSONObject(i);
                                        codigo = jsonObject.getString("codigo").trim();
                                        subcodigo = jsonObject.getString("subcodigo").trim();
                                        subsector = jsonObject.getString("subsector").trim();
                                        fechamodifi = jsonObject.getString("fechamodifi");

                                        ContentValues insertar = new ContentValues();
                                        insertar.put("codigo", codigo);
                                        insertar.put("subsector", subsector);
                                        insertar.put("subcodigo", subcodigo);
                                        insertar.put("fechamodifi", fechamodifi);


                                        ke_android.insert("subsectores", null, insertar);

                                        //actualizamos la fecha de la tabla de
                                        Calendar fecha_subsectores = Calendar.getInstance();
                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                        String fechasubsectores = sdf.format(fecha_subsectores.getTime());

                                        ContentValues actualizarFecha = new ContentValues();
                                        actualizarFecha.put("fchhn_ultmod", fechasubsectores);
                                        ke_android.update("tabla_aux", actualizarFecha, "tabla = 'subsectores'", null);
                                        ke_android.setTransactionSuccessful();

                                    } catch (JSONException e) {
                                        Toast.makeText(getApplicationContext(), e.getMessage(), LENGTH_LONG).show();
                                    } finally {
                                        ke_android.endTransaction();
                                    }
                                }
                            }
                            ke_android.close();

                            tv_subsector.setTextColor(Color.rgb(62, 197, 58));
                            tv_subsector.setText("Ruta: Sincronizado.");
                            progressDialog.setMessage("Ruta: Sincronizado.");
                            varAux++;
                            progressDialog.incrementProgressBy((int) numBarraProgreso);
                            sincronizacionVendedor();
                        } else {

                            //si no hay nada, hago un insert
                            AdminSQLiteOpenHelper conn = new AdminSQLiteOpenHelper(getApplicationContext(), "ke_android", null, 1);
                            ke_android = conn.getWritableDatabase();


                            JSONObject jsonObject = null; //creamos un objeto json vacio
                            for (int i = 0; i < subsectorArray.length(); i++) { /*pongo todo en el objeto segun lo que venga */
                                try {

                                    ke_android.beginTransaction();
                                    jsonObject = subsectorArray.getJSONObject(i);
                                    codigo = jsonObject.getString("codigo").trim();
                                    subcodigo = jsonObject.getString("subcodigo").trim();
                                    subsector = jsonObject.getString("subsector").trim();
                                    fechamodifi = jsonObject.getString("fechamodifi");

                                    ContentValues insertar = new ContentValues();
                                    insertar.put("codigo", codigo);
                                    insertar.put("subsector", subsector);
                                    insertar.put("subcodigo", subcodigo);
                                    insertar.put("fechamodifi", fechamodifi);
                                    ke_android.insert("subsectores", null, insertar);

                                    //actualizamos la fecha de la tabla de
                                    Calendar fecha_subsectores = Calendar.getInstance();
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                    String fechasubsectores = sdf.format(fecha_subsectores.getTime());

                                    ContentValues actualizarFecha = new ContentValues();
                                    actualizarFecha.put("fchhn_ultmod", fechasubsectores);
                                    ke_android.update("tabla_aux", actualizarFecha, "tabla = 'subsectores'", null);
                                    ke_android.setTransactionSuccessful();

                                } catch (JSONException e) {
                                    Toast.makeText(getApplicationContext(), "Error 32", LENGTH_LONG).show();
                                } finally {
                                    ke_android.endTransaction();
                                }


                            }
                            //Toast.makeText(PrincipalActivity.this, "Subsectores descargados", Toast.LENGTH_SHORT).show();

                            ke_android.close();

                            tv_subsector.setTextColor(Color.rgb(62, 197, 58));
                            tv_subsector.setText("Ruta: Sincronizado.");
                            progressDialog.setMessage("Ruta: Sincronizado.");
                            varAux++;
                            progressDialog.incrementProgressBy((int) numBarraProgreso);
                            sincronizacionVendedor();
                        }
                    } else if ((response.getString("subsector").equals("null"))) {

                        //Toast.makeText(getApplicationContext(), "No se recibieron mas datos", LENGTH_LONG).show();
                        /* si en la consulta no ncuentra nada
                        es que el usuario o password estan incorrectos */


                        tv_subsector.setTextColor(Color.rgb(98, 117, 141));
                        tv_subsector.setText("Ruta: Sin actualización.");
                        progressDialog.setMessage("Ruta: Sin actualización.");
                        varAux++;
                        progressDialog.incrementProgressBy((int) numBarraProgreso);
                        sincronizacionVendedor();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Ingreso de la fecha antes de ser actualizada
                ActualizarFechaError(fecha_error);

                //--Manejo visual que indica al usuario del error--
                tv_subsector.setTextColor(Color.rgb(232, 17, 35));
                tv_subsector.setText("Ruta: No ha logrado sincronizar");
                progressDialog.setMessage("Ruta: No ha logrado sincronizar");
                varAux++;
                progressDialog.incrementProgressBy((int) numBarraProgreso);
                //varAuxError = true;
                //AnalisisError2();
                //------
                sincronizacionVendedor();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {  //finalmente, estos son los parametros que le enviaremos al webservice, partiendo de las variables
                //donde estan guardados las fechas
                Map<String, String> parametros = new HashMap<String, String>();
                parametros.put("fecha_sinc", fecha_sinc);

                return parametros;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest); //esto es el request que se envia al url a traves de la conexion volley, (el stringrequest esta armado arriba)

    }

    private void InsertarCorrelativo() {

        SQLiteDatabase ke_android = conn.getWritableDatabase();

        Cursor cursor = ke_android.rawQuery("SELECT * FROM ke_correla WHERE kco_vendedor ='" + cod_usuario + "'", null);

        if (cursor.moveToFirst()) {
            //System.out.println("EL VENDEDOR YA POSEE CORRELATIVOS");
        } else {
            ContentValues insertar = new ContentValues();
            insertar.put("kco_numero", 0);
            insertar.put("kco_vendedor", cod_usuario);

            ke_android.insert("ke_correla", null, insertar);
        }
    }


    public void SubirPedidos() {
        //Inicializacion de la conexion
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        //Ejecucion del query que busca los pedidos que no se hayan subido
        cursorti = ke_android.rawQuery("SELECT kti_codcli, kti_codven, kti_docsol, kti_condicion, kti_tdoc, kti_ndoc, kti_tipprec, kti_nombrecli, kti_totneto, kti_fchdoc, kti_status, fechamodifi FROM ke_opti WHERE kti_status = '0'  AND kti_codven ='" + cod_usuario.trim() + "'", null);
        //If que analiza si existen pedidos aun no subidos
        if (cursorti.moveToFirst()) {
            //En el caso que si hayan pedidos por subir
            cargarPedidos();
        } else {
            //En el caso en el que no hayan pedidos por subir
            Toast.makeText(SincronizacionActivity.this, "No hay pedidos por cargar", Toast.LENGTH_SHORT).show();
            bt_subir.setEnabled(true);
        }

    }

    public void SubirPrecob() {
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        //Busqueda de datos en la base de datos del tlf para validar la existencia de nuevas cobranzas por subir
        Cursor cursorpc = ke_android.rawQuery("SELECT cxcndoc FROM ke_precobranza WHERE (edorec = '0' OR edorec = '9' OR edorec = '3') AND codvend ='" + cod_usuario.trim() + "'", null);
        //IF para que valida la existencia de esas nuevas obranzas con  ayuda del cursor
        if (cursorpc.moveToFirst()) {
            //Funcion para crear el JSON
            cargarPrecob();
        } else {
            //Mensaje en caso de no haber nuevas cobranzas por subir
            Toast.makeText(SincronizacionActivity.this, "No hay precobranza por cargar", Toast.LENGTH_SHORT).show();
            bt_subirprecob.setEnabled(true);
        }
        cursorpc.close();
    }

    public void cargarPrecob() {
        int contadorPrecob = 0;
        //Boton para subir la precobranza
        bt_subirprecob.setEnabled(false);
        bt_subirprecob.setBackgroundColor(Color.rgb(242, 238, 238));
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        //Creacion del ursor y la sentencia SQL que ejecutara la busqueda de cada una de las abeeras de los documentos por cobrar
        Cursor cursorRc = null;
        cursorRc = ke_android.rawQuery("SELECT * FROM ke_precobranza WHERE (edorec = '0' OR edorec = '9' OR edorec = '3') AND codvend ='" + cod_usuario.trim() + "'", null);
        //System.out.println("SELECT * FROM ke_precobranza WHERE (edorec = '0' OR edorec = '9') AND codvend ='" + cod_usuario.trim() + "'");
        //Creacion del JSON Array que contendra las precobranzas
        arrayCH = new JSONArray();
        //while que reorera todos los casos positivos de la sentencia SQL ejeutada
        while (cursorRc.moveToNext()) {
            //Creacion de los Objetos JSON Cabecera y SuperCabecera
            JSONObject objetoCabecera = new JSONObject();
            JSONObject objetoSCabecera = new JSONObject();
            //TRY para la creacion del JSON completo
            try {
                //Guardado en variables de los valores enontrados en la base de datos
                /*cxcndoc = cursorRc.getString(0);
                System.out.println("numero rec: " + cxcndoc);
                tiporecibo = cursorRc.getString(1);
                codvend = cursorRc.getString(2);
                nro_recibo = cursorRc.getString(3);
                kecxc_id = cursorRc.getString(4);
                tasadia = cursorRc.getDouble(5);
                fchrecibo = cursorRc.getString(6);
                clicontesp = cursorRc.getString(7);
                bsneto = cursorRc.getDouble(8);
                bsiva = cursorRc.getDouble(9);
                bsretiva = cursorRc.getDouble(10);
                bsflete = cursorRc.getDouble(11);
                bstotal = cursorRc.getDouble(12);
                dolneto = cursorRc.getDouble(13);
                doliva = cursorRc.getDouble(14);
                dolretiva = cursorRc.getDouble(15);
                dolflete = cursorRc.getDouble(16);
                doltotal = cursorRc.getDouble(17);
                moneda = cursorRc.getString(18);
                dctoaplic = cursorRc.getDouble(19);
                netocob = cursorRc.getDouble(20);
                efectivo = cursorRc.getDouble(21);
                bcoecod = cursorRc.getString(22);
                bcocod = cursorRc.getString(23);
                bconombre = cursorRc.getString(24);
                fchr_dep = cursorRc.getString(25);
                bcomonto = cursorRc.getDouble(26);
                bcoref = cursorRc.getString(27);
                edorec = cursorRc.getString(28);
                fchhr = cursorRc.getString(29);
                fchvigen = cursorRc.getString(30);
                bsretflete = cursorRc.getDouble(31);
                diasvigen = cursorRc.getDouble(32);
                retmun_sbi = cursorRc.getDouble(33);
                retmun_sbs = cursorRc.getDouble(34);
                reci_doc = cursorRc.getString(35);
                fechamodifi = cursorRc.getString(36);*/
                cxcndoc = cursorRc.getString(0);
                //Guardado de los valores en el objeto JSON de la Cabecera
                objetoCabecera.put("cxcndoc", cursorRc.getString(0));
                objetoCabecera.put("tiporecibo", cursorRc.getString(1));
                objetoCabecera.put("codvend", cursorRc.getString(2));
                objetoCabecera.put("nro_recibo", cursorRc.getString(3));
                objetoCabecera.put("kecxc_id", cursorRc.getString(4));
                objetoCabecera.put("tasadia", cursorRc.getString(5));
                objetoCabecera.put("fchrecibo", cursorRc.getString(6));
                objetoCabecera.put("clicontesp", cursorRc.getString(7));
                objetoCabecera.put("bsneto", cursorRc.getString(8));
                objetoCabecera.put("bsiva", cursorRc.getString(9));
                objetoCabecera.put("bsretiva", cursorRc.getString(10));
                objetoCabecera.put("bsflete", cursorRc.getString(11));
                objetoCabecera.put("bstotal", cursorRc.getString(12));
                objetoCabecera.put("dolneto", cursorRc.getString(13));
                objetoCabecera.put("doliva", cursorRc.getString(14));
                objetoCabecera.put("dolretiva", cursorRc.getString(15));
                objetoCabecera.put("dolflete", cursorRc.getString(16));
                objetoCabecera.put("doltotal", cursorRc.getString(17));
                objetoCabecera.put("moneda", cursorRc.getString(18));
                objetoCabecera.put("docdifcamb", cursorRc.getString(19));
                objetoCabecera.put("ddc_age", cursorRc.getString(20));
                objetoCabecera.put("ddc_tipo", cursorRc.getString(21));
                objetoCabecera.put("ddc_montobs", cursorRc.getString(22));
                objetoCabecera.put("ddc_doc", cursorRc.getString(23));
                objetoCabecera.put("dctoaplic", cursorRc.getString(24));
                objetoCabecera.put("netocob", cursorRc.getString(25));
                objetoCabecera.put("concepto", cursorRc.getString(26));
                objetoCabecera.put("efectivo", cursorRc.getString(27));
                objetoCabecera.put("bcoecod", cursorRc.getString(28));
                objetoCabecera.put("bcocod", cursorRc.getString(29));
                objetoCabecera.put("bconombre", cursorRc.getString(30));
                objetoCabecera.put("fchr_dep", cursorRc.getString(31));
                objetoCabecera.put("bcomonto", cursorRc.getString(32));
                objetoCabecera.put("bcoref", cursorRc.getString(33));
                objetoCabecera.put("pidvalid", cursorRc.getString(34));
                objetoCabecera.put("edorec", cursorRc.getString(35));
                objetoCabecera.put("edocomiv", cursorRc.getString(36));
                objetoCabecera.put("prccomiv", cursorRc.getString(37));
                objetoCabecera.put("mtocomiv", cursorRc.getString(38));
                objetoCabecera.put("fchr_pcomv", cursorRc.getString(39));
                objetoCabecera.put("codcoord", cursorRc.getString(40));
                objetoCabecera.put("edocomic", cursorRc.getString(41));
                objetoCabecera.put("prccomic", cursorRc.getString(42));
                objetoCabecera.put("mtocomic", cursorRc.getString(43));
                objetoCabecera.put("fchr_pcomc", cursorRc.getString(44));
                objetoCabecera.put("fchhr", cursorRc.getString(45));
                objetoCabecera.put("fchvigen", cursorRc.getString(46));
                objetoCabecera.put("bsretflete", cursorRc.getString(47));
                objetoCabecera.put("diasvigen", cursorRc.getString(48));
                objetoCabecera.put("retmun_sbi", cursorRc.getString(49));
                objetoCabecera.put("retmun_sbs", cursorRc.getString(50));
                objetoCabecera.put("comiaut", cursorRc.getString(51));
                objetoCabecera.put("comiautpor", cursorRc.getString(52));
                objetoCabecera.put("comiautfch", cursorRc.getString(53));
                objetoCabecera.put("reci_age", cursorRc.getString(54));
                objetoCabecera.put("reci_doc", cursorRc.getString(55));
                objetoCabecera.put("status", cursorRc.getString(56));
                objetoCabecera.put("fechamodifi", cursorRc.getString(57));
                objetoCabecera.put("cxcndoc_aux", cursorRc.getString(58));

                //Cursor que ejecuta un SQL para ver las lineas de una cobranza en speifico para armar las lineas del documento
                Cursor cursorRl = ke_android.rawQuery("SELECT * FROM ke_precobradocs WHERE cxcndoc ='" + cxcndoc + "'", null);
                //System.out.println("SELECT * FROM ke_precobradocs WHERE cxcndoc ='" + cxcndoc + "'");

                //Creacion del JSON Array que contendra todas las lineas de un documento
                arrayCL = new JSONArray();
                //WHILE que reccorera todos los casos positivos de la sentencia anterior
                while (cursorRl.moveToNext()) {
                    //Creacion del Objeto JSON para las lineas del documento
                    JSONObject objetoLineas = new JSONObject();
                    //Guardado de los valores de la base de datos en las variables
                    /*cxcndoc = cursorRl.getString(0);
                    agencia = cursorRl.getString(1);
                    tipodoc = cursorRl.getString(2);
                    documento = cursorRl.getString(3);
                    bscobro = cursorRl.getDouble(4);
                    prcdsctopp = cursorRl.getDouble(5);
                    nroret = cursorRl.getString(6);
                    fchemiret = cursorRl.getString(7);
                    bsretiva = cursorRl.getDouble(8);
                    refret = cursorRl.getString(9);
                    nroretfte = cursorRl.getString(10);
                    fchemirfte = cursorRl.getString(11);
                    bsmtofte = cursorRl.getDouble(12);
                    bsretfte = cursorRl.getDouble(13);
                    refretfte = cursorRl.getString(14);
                    bsmtoiva = cursorRl.getDouble(15);
                    retmun_bi = cursorRl.getDouble(16);
                    retmun_cod = cursorRl.getString(17);
                    retmun_nro = cursorRl.getString(18);
                    retmun_mto = cursorRl.getDouble(19);
                    retmun_fch = cursorRl.getString(20);
                    retmun_ref = cursorRl.getString(21);
                    diascalc = cursorRl.getDouble(22);*/

                    //Guardado de los valoes en la estructura del JSON de las lineas del documento
                    objetoLineas.put("cxcndoc", cursorRl.getString(0));
                    objetoLineas.put("agencia", cursorRl.getString(1));
                    objetoLineas.put("tipodoc", cursorRl.getString(2));
                    objetoLineas.put("documento", cursorRl.getString(3));
                    objetoLineas.put("bscobro", cursorRl.getString(4));
                    objetoLineas.put("prccobro", cursorRl.getString(5));
                    objetoLineas.put("prcdsctopp", cursorRl.getString(6));
                    objetoLineas.put("nroret", cursorRl.getString(7));
                    objetoLineas.put("fchemiret", cursorRl.getString(8));
                    objetoLineas.put("bsretiva", cursorRl.getString(9));
                    objetoLineas.put("refret", cursorRl.getString(10));
                    objetoLineas.put("nroretfte", cursorRl.getString(11));
                    objetoLineas.put("fchemirfte", cursorRl.getString(12));
                    objetoLineas.put("bsmtofte", cursorRl.getString(13));
                    objetoLineas.put("bsretfte", cursorRl.getString(14));
                    objetoLineas.put("refretfte", cursorRl.getString(15));
                    objetoLineas.put("pidvalid", cursorRl.getString(16));
                    objetoLineas.put("bsmtoiva", cursorRl.getString(17));
                    objetoLineas.put("retmun_bi", cursorRl.getString(18));
                    objetoLineas.put("retmun_cod", cursorRl.getString(19));
                    objetoLineas.put("retmun_nro", cursorRl.getString(20));
                    objetoLineas.put("retmun_mto", cursorRl.getString(21));
                    objetoLineas.put("retmun_fch", cursorRl.getString(22));
                    objetoLineas.put("retmun_ref", cursorRl.getString(23));
                    objetoLineas.put("diascalc", cursorRl.getString(24));
                    objetoLineas.put("prccomiv", cursorRl.getString(25));
                    objetoLineas.put("prccomic", cursorRl.getString(26));
                    objetoLineas.put("cxcndoc_aux", cursorRl.getString(27));
                    objetoLineas.put("tnetodbs", cursorRl.getString(28));
                    objetoLineas.put("tnetoddol", cursorRl.getString(29));
                    objetoLineas.put("fchrecibod", cursorRl.getString(30));
                    objetoLineas.put("kecxc_idd", cursorRl.getString(31));
                    objetoLineas.put("tasadiad", cursorRl.getString(32));
                    objetoLineas.put("afavor", cursorRl.getString(33));
                    //System.out.println("LA FECHA --> "+ cursorRl.getString(23));
                    //Guardado del objeto JSON Lineas en el JSON Array
                    arrayCL.put(objetoLineas);
                }
                cursorRl.close();

                //Insercion del array de las Lineas en el Objeto JSON de la cabecera
                objetoCabecera.put("Lineas", arrayCL);
                //Insercion del objeto cabecera en el Objeto JSON super cabecera
                objetoSCabecera.put("Cabecera", objetoCabecera);
                //Capturador de errores
            } catch (Exception ex) {
                ex.printStackTrace();
                System.out.println("Error al cargar datos del recibo" + ex);
                Toast.makeText(SincronizacionActivity.this, "Error al cargar datos del recibo" + ex, Toast.LENGTH_SHORT).show();
            }
            //Insercion de la super cabeera en el array de la precobranza
            arrayCH.put(objetoSCabecera);
            contadorPrecob++;
            //tv_pedidossubidos.setText("Cargando pedido: " + contadorPedidos +" de " + cursorti.getCount());
        }
        cursorRc.close();
        //Creacion de Objeto JSON que contendra todo
        JSONObject jsonREC = new JSONObject(); //vamos a hacer un solo objeto de tipo json
        try {
            //Guardado del array Cobranza con su asignacion de nombre dentro del JSON
            jsonREC.put("Cobranza", arrayCH);

        } catch (JSONException e) {
            e.printStackTrace();
            // Toast.makeText(SincronizacionActivity.this, "Error al cargar el pedido" + e, Toast.LENGTH_SHORT).show();
        }
        String jsonStrREC = jsonREC.toString();
        try {
            //Funcion para el envio del JSON
            insertarCobranza(jsonREC);

        } catch (Exception exc) {
            exc.printStackTrace();
            // Toast.makeText(SincronizacionActivity.this, "Error al cargar el pedido" + exc, Toast.LENGTH_SHORT).show();

        }
    }


    /* Pedidos version 2 */
    //Funcion que arma el JSON
    public void cargarPedidos() {
        int contadorPedidos = 0;
        //Bloqueo del boton
        bt_subir.setEnabled(false);
        bt_subir.setBackgroundColor(Color.rgb(242, 238, 238));
        //Inicializacion de la conexion y ejecucion del query
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        cursorti = ke_android.rawQuery("SELECT DISTINCT kti_codcli, kti_codven, kti_docsol, kti_condicion, kti_tdoc, kti_ndoc, kti_tipprec, kti_nombrecli, kti_totneto, kti_fchdoc, kti_status, fechamodifi, kti_negesp FROM ke_opti WHERE kti_status = '0'  AND kti_codven ='" + cod_usuario.trim() + "'", null);
        //Creacion del array JSON
        arrayTi = new JSONArray();

        //While que recorreo todas las columnas del query anterior
        while (cursorti.moveToNext()) {
            //Creacion del objeto JSON para la abecera y la super cabecera
            JSONObject objetoSCabecera = new JSONObject();
            JSONObject objetoCabecera = new JSONObject();
            //Try para la ejecucion de la creacion del todo el JSON
            try {

                //Llenando las variables con cada campo del query enviado
                kti_codcli = cursorti.getString(0);
                kti_codven = cursorti.getString(1);
                kti_docsolicitado = cursorti.getString(2);
                kti_condicion = cursorti.getString(3);
                kti_tdoc = cursorti.getString(4);
                kti_ndoc = cursorti.getString(5);
                kti_tipprec = cursorti.getDouble(6);
                tmp_nombrecli = cursorti.getString(7);
                kti_totneto = cursorti.getDouble(8);
                kti_fchdoc = cursorti.getString(9);
                kti_status = "1";
                kti_fechamodifi = cursorti.getString(11);
                kti_negesp = cursorti.getString(12);

                //Creacion de diccionario JSON
                objetoCabecera.put("kti_codcli", kti_codcli);
                objetoCabecera.put("kti_codven", kti_codven);
                objetoCabecera.put("kti_docsol", kti_docsolicitado);
                objetoCabecera.put("kti_condicion", kti_condicion);
                objetoCabecera.put("kti_tdoc", kti_tdoc);
                objetoCabecera.put("kti_ndoc", kti_ndoc);
                objetoCabecera.put("kti_tipprec", kti_tipprec);
                objetoCabecera.put("kti_nombrecli", tmp_nombrecli);
                objetoCabecera.put("kti_totneto", kti_totneto);
                objetoCabecera.put("kti_fchdoc", kti_fchdoc);
                objetoCabecera.put("kti_status", kti_status);
                objetoCabecera.put("fechamodifi", kti_fechamodifi);
                objetoCabecera.put("kti_negesp", kti_negesp);
                //Query para la solicitud de las lineas del pedido que se esta procesando
                Cursor cursormv = ke_android.rawQuery("SELECT kmv_codart, kmv_nombre, kti_tipprec, kmv_cant, kti_tdoc, kti_ndoc, kmv_stot, kmv_artprec, kmv_dctolin FROM ke_opmv WHERE kti_ndoc ='" + kti_ndoc + "'", null);
                //Creacion del array JSON que contendra los objetos JSON para las lineas
                arrayMV = new JSONArray();
                //While que recorreo todas las columnas del query anterior
                while (cursormv.moveToNext()) {
                    //Creacion del objeto JSON para las lineas
                    JSONObject objetoLineas = new JSONObject();
                    kmv_codart = cursormv.getString(0);
                    kmv_nombre = cursormv.getString(1);
                    kti_tipprec = cursormv.getDouble(2);
                    kmv_cant = cursormv.getDouble(3);
                    kti_tdoc = cursormv.getString(4);
                    kti_ndoc = cursormv.getString(5);
                    kmv_stot = cursormv.getDouble(6);
                    kmv_artprec = cursormv.getDouble(7);
                    kmv_dctolin = cursormv.getDouble(8);
                    //Armado del Objeto JSON de las lineas
                    objetoLineas.put("kmv_codart", kmv_codart);
                    objetoLineas.put("kmv_nombre", kmv_nombre);
                    objetoLineas.put("kti_tipprec", kti_tipprec);
                    objetoLineas.put("kmv_cant", kmv_cant);
                    objetoLineas.put("kti_tdoc", kti_tdoc);
                    objetoLineas.put("kti_ndoc", kti_ndoc);
                    objetoLineas.put("kmv_stot", kmv_stot);
                    objetoLineas.put("kmv_artprec", kmv_artprec);
                    objetoLineas.put("kmv_dctolin", kmv_dctolin);
                    //Llenando el array JSON de los Objetos JSON de las lineas
                    arrayMV.put(objetoLineas);
                }
                //Insercion del array de las Lineas en el Objeto JSON de la abecera
                objetoCabecera.put("Lineas", arrayMV);
                //Insercion del objeto cabecera en el Objeto JSON super cabecera
                objetoSCabecera.put("Cabecera", objetoCabecera);
                //Catch para la camtura de errores
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(SincronizacionActivity.this, "Error al cargar el pedido" + e, Toast.LENGTH_SHORT).show();
            }
            //Insercion del objeto JSON super cabeera en el array JSON TI
            arrayTi.put(objetoSCabecera);
            //Suma de pedidos procesados y muestra en pantalla
            contadorPedidos++;
            tv_pedidossubidos.setText("Cargando pedido: " + contadorPedidos + " de " + cursorti.getCount());


        }
        //System.out.println(arrayTi);
        //  System.out.println(arrayMV);
        //Objeto JSON que contiene todo el JSON
        JSONObject jsonPE = new JSONObject(); //vamos a hacer un solo objeto de tipo json
        try {
            //Creacion de Objeto JSON pedido que contiene todo el JSON
            jsonPE.put("Pedido", arrayTi);


        } catch (JSONException e) {
            e.printStackTrace();
            // Toast.makeText(SincronizacionActivity.this, "Error al cargar el pedido" + e, Toast.LENGTH_SHORT).show();
        }
        String jsonStrPE = jsonPE.toString();
        try {
            //Envio de pedidos
            insertarPedido(jsonPE);


        } catch (Exception exc) {
            exc.printStackTrace();
            // Toast.makeText(SincronizacionActivity.this, "Error al cargar el pedido" + exc, Toast.LENGTH_SHORT).show();

        }

    }


    private void cargarLimites() {

        arrayLimite = new JSONArray();
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        cursorLim = ke_android.rawQuery("SELECT " + "kli_track, " + "kli_codven, " + "kli_codcli, " + "kli_codart, " + "kli_cant, " + "kli_fechahizo, " + "kli_fechavence " + "FROM ke_limitart" + " WHERE status = '1'  " + "AND kli_fechahizo >'" + fecha_sinc_limites + "'" + "AND kli_codven = '" + cod_usuario.trim() + "'", null);

        while (cursorLim.moveToNext()) {
            JSONObject objetoLimite = new JSONObject();
            try {

                String kli_track = cursorLim.getString(0);
                String kli_codven = cursorLim.getString(1);
                String kli_codcli = cursorLim.getString(2);
                String kli_codart = cursorLim.getString(3);
                int kli_cant = cursorLim.getInt(4);
                String kli_fechahizo = cursorLim.getString(5);
                String kli_fechavence = cursorLim.getString(6);

                objetoLimite.put("kli_track", kli_track.trim());
                objetoLimite.put("kli_codven", kli_codven.trim());
                objetoLimite.put("kli_codcli", kli_codcli.trim());
                objetoLimite.put("kli_codart", kli_codart.trim());
                objetoLimite.put("kli_cant", kli_cant);
                objetoLimite.put("kli_fechahizo", kli_fechahizo.trim());
                objetoLimite.put("kli_fechavence", kli_fechavence.trim());

                arrayLimite.put(objetoLimite);

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Error respaldando parámetros" + e, Toast.LENGTH_SHORT).show();
            }
        }


        //System.out.println(arrayLimite);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("Limites", arrayLimite);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String jsonStrLim = jsonObject.toString();
        try {
            insertarLimites(jsonStrLim);
        } catch (Exception exc) {
            exc.printStackTrace();
        }


    }

    private void insertarLimites(final String jsonlim) {

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://" + enlaceEmpresa + "/" + ambienteJob + "/Limites.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response.trim().equals("OK")) {
                    Toast.makeText(getApplicationContext(), "Parámetros cargados.", Toast.LENGTH_LONG).show();
                    varAux++;
                    progressDialog.incrementProgressBy((int) numBarraProgreso);
                    sincronizacionVendedor();
                } else if (response == null) {
                    varAux++;
                    progressDialog.incrementProgressBy((int) numBarraProgreso);
                    sincronizacionVendedor();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                varAux++;
                progressDialog.incrementProgressBy((int) numBarraProgreso);
                //varAuxError = true;
                sincronizacionVendedor();
            }

        }) {
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("jsonlim", jsonlim);
                params.put("agencia", codigoSucursal);

                return params;
            }
        };
        requestQueue.add(stringRequest);
    }


    public void cambiarEstadoPedido(String Correlativo) {
        //Actualizacion del status del pedido
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        ke_android.execSQL("UPDATE ke_opti SET kti_status = '1' WHERE kti_ndoc = '" + Correlativo + "'");
    }

    public void cambiarEstadoPrecob(String Correlativo) {
        //Actualizacion del status del pedido
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        Cursor cursor = ke_android.rawQuery("SELECT edorec, tiporecibo FROM ke_precobranza WHERE cxcndoc = '" + Correlativo + "';", null);
        if (cursor.moveToNext()) {
            if (Objects.equals(cursor.getString(0), "0")) {
                ke_android.execSQL("UPDATE ke_precobranza SET edorec = '1' WHERE cxcndoc = '" + Correlativo + "';");
                if (Objects.equals(cursor.getString(1), "R")) {
                    subirImgRet(Correlativo);
                }
            } else if (Objects.equals(cursor.getString(0), "9")) {
                ke_android.execSQL("UPDATE ke_precobranza SET edorec = '10' WHERE cxcndoc = '" + Correlativo + "';");
            } else if (Objects.equals(cursor.getString(0), "3")) {
                ke_android.execSQL("UPDATE ke_precobranza SET edorec = '4' WHERE cxcndoc = '" + Correlativo + "';");
            }
        } else {
            Toast.makeText(this, "Error al actualizar estatus", Toast.LENGTH_SHORT).show();
        }
        cursor.close();
    }

    private void subirImgRet(String correlativo) {
        int i = 0;
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        Cursor cursor = ke_android.rawQuery("SELECT * FROM ke_retimg WHERE ke_retimg.cxcndoc = '" + correlativo + "';", null);
        //System.out.println("SELECT * FROM ke_imgret WHERE ke_imgret.cxcndoc = '" + correlativo + "';");
        while (cursor.moveToNext()) {
            /*ke_imgret img = new ke_imgret(
                    cursor.getString(0),
                    cursor.getString(1),
                    nombre
            );*/
            JSONObject imagen = new JSONObject();
            try {
                imagen.put("ret_nomimg", cursor.getString(2));
                imagen.put("cxcndoc", cursor.getString(0));
                imagen.put("ruta", cursor.getString(1));
                enviarImgRet(imagen, correlativo);
            } catch (Exception e) {
                e.printStackTrace();
            }

            //System.out.println(img);

            i++;
        }

        cursor.close();

    }

    private void enviarImgRet(JSONObject imgs, String correlativo) {
        //System.out.println(imgs);
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        //System.out.println("Imagen -->" + imgs);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, "https://www.cloccidental.com/webservice/ImagenesRetenciones.php", imgs, response -> {
            try {
                if (response.getString("status").equals("0")) {
                    ke_android.execSQL("UPDATE ke_precobranza SET edorec = '1' WHERE cxcndoc = '" + correlativo + "';");
                    ke_android.execSQL("DELETE FROM ke_retimg WHERE cxcndoc = '" + correlativo + "';");
                } else {
                    ke_android.execSQL("UPDATE ke_precobranza SET edorec = '0' WHERE cxcndoc = '" + correlativo + "';");
                }
            } catch (JSONException e) {
                e.printStackTrace();
                ke_android.execSQL("UPDATE ke_precobranza SET edorec = '0' WHERE cxcndoc = '" + correlativo + "';");
            }
        }, error -> {
            //System.out.println("Error -->" + error);
            error.printStackTrace();
            ke_android.execSQL("UPDATE ke_precobranza SET edorec = '0' WHERE cxcndoc = '" + correlativo + "';");
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);

            /*RequestQueue requestQueue = Volley.newRequestQueue(this);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://www.cloccidental.com/webservice/ImagenesRetenciones.php",
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            System.out.println(response);
                            if(response.equals("Subido")) {
                                System.out.println(response);

                            } else {
                                Toast.makeText(SincronizacionActivity.this, "Teléfono sin internet adecuada", LENGTH_LONG).show();
                                ke_android.execSQL("UPDATE ke_precobranza SET edorec = '0' WHERE cxcndoc = '" + correlativo + "';");
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(SincronizacionActivity.this, "Teléfono sin internet adecuada", LENGTH_LONG).show();
                    ke_android.execSQL("UPDATE ke_precobranza SET edorec = '0' WHERE cxcndoc = '" + correlativo + "';");
                }
            }){
                @Override
                public Map<String, String> getParams() {

                    HashMap<String, String> params = new HashMap<String, String>();
                    params.put("Content-Type", "application/json; charset=utf-8");
                    params.put("Host", "https://www.cloccidental.com/webservice/ImagenesRetenciones.php");
                    params.put("nombre", imgs.getNombre());
                    params.put("imagen", imgs.getRutafoto());
                    params.put("cxcndoc", imgs.getCxcndoc());

                    return params;
                }

                @Override
                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                    String responseString = "";
                    if (response != null) {
                        responseString = String.valueOf (response.statusCode);
                        // can get more details such as response.headers
                    }
                    return Response.success (responseString, HttpHeaderParser.parseCacheHeaders (response));
                }

            };

        stringRequest.setShouldCache(false);
        stringRequest.setRetryPolicy(new DefaultRetryPolicy (30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));


            requestQueue.add(stringRequest);

            try {
                System.out.println("EL REQUEST --> "+ stringRequest.getBody());
            }catch (Exception e){
                e.printStackTrace();
            }*/


    }


    /***********************************************************/

    public void insertarCobranza(JSONObject jsoncxc) {
        System.out.println(jsoncxc);

        RequestQueue requestQueue = Volley.newRequestQueue(SincronizacionActivity.this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, "https://cf10-45-186-201-166.ngrok.io/precobranzas2", jsoncxc, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (response != null) {
                    try {
                        //Descomposiion del objeto JSON llamado "estado"
                        JSONArray jsonArray = response.getJSONArray("estado");
                        //Analicis y descomposicion del array JSON
                        for (int i = 0; i < jsonArray.length(); i++) {
                            //Obtencion del objeto JSON del array
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            //Obtencion de las variables "correlatvo" y "status" del Objeto JSON
                            String Correlativo = jsonObject.getString("correlativo");
                            String status = jsonObject.getString("status");
                            //Analicis de la respuesta con la variable status
                            if (status.equals("200")) {
                                //Funcion que cambia el status del pedido de 0 a 1
                                cambiarEstadoPrecob(Correlativo);
                                //tv_pedidossubidos.setText("Pedido " + Correlativo + " Cargado Correctamente");
                                Toast.makeText(SincronizacionActivity.this, "Cobanza " + Correlativo + " Cargado Correctamente.", LENGTH_LONG).show();
                            }

                            if (status.equals("111")) {
                                //Funcion que cambia el status del pedido de 0 a 1
                                cambiarEstadoPrecob(Correlativo);
                                //tv_pedidossubidos.setText("Pedido" + Correlativo + " previamente cargado");
                                Toast.makeText(SincronizacionActivity.this, "Cobranza" + Correlativo + " previamente cargado. \nRecomendaión: Sincronizar nuevamente.", LENGTH_LONG).show();
                            }

                            if (status.equals("112")) {
                                //tv_pedidossubidos.setText("Linea(s) del pedido" + Correlativo + " repetida(s)");
                                Toast.makeText(SincronizacionActivity.this, "Linea(s) de la Cobranza" + Correlativo + " repetida(s) \nRecomendación: Verificar el contenido del pedido.", LENGTH_LONG).show();
                            }

                            if (status.equals("403")) {
                                //tv_pedidossubidos.setText("Correlativos del pedido" + Correlativo + " no concuerdan");
                                Toast.makeText(SincronizacionActivity.this, "Correlativos de la Cobanza" + Correlativo + " no concuerdan \nRecomendaión: Rehacer el pedido.", LENGTH_LONG).show();
                            }

                            if (status.equals("404")) {
                                //tv_pedidossubidos.setText("Correlativos del pedido" + Correlativo + " no concuerdan");
                                Toast.makeText(SincronizacionActivity.this, "Error súbito.", LENGTH_LONG).show();
                            }
                        }
                        //Mensaje final del proceso
                        tv_pedidossubidos.setText("Cobranzas Procesadas");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("--Error--");
                error.printStackTrace();
                if (error == null || error.networkResponse == null) {
                    return;
                }
                String body;
                //get status code here
                String statusCode = java.lang.String.valueOf(error.networkResponse.statusCode);
                //get response body and parse with appropriate encoding
                Charset UTF_8 = StandardCharsets.UTF_8;
                body = new String(error.networkResponse.data, UTF_8);
                System.out.println(body);
                System.out.println("--Error--");
                Toast.makeText(SincronizacionActivity.this, "Error de red en carga de recibos", Toast.LENGTH_SHORT).show();
                //bt_subir.setEnabled(true);
            }
        });
        requestQueue.add(jsonObjectRequest);


    }

    public void insertarPedido(JSONObject jsonpe) {
        //Muestra pantalla del json generado
        System.out.println("pedido llegando: " + jsonpe);

        //http://cloccidental.com:5000/pedidos
        //Envio del JSON en la direccion dada
        RequestQueue requestQueue = Volley.newRequestQueue(SincronizacionActivity.this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, "http://cloccidental.com:5001/pedido", jsonpe, new Response.Listener<JSONObject>() {
            @Override
            //Respuesta positiva del url
            public void onResponse(JSONObject response) {
                if (response != null) {
                    try {
                        //Descomposiion del objeto JSON llamado "estado"
                        JSONArray jsonArray = response.getJSONArray("estado");
                        //Analicis y descomposicion del array JSON
                        for (int i = 0; i < jsonArray.length(); i++) {
                            //Obtencion del objeto JSON del array
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            //Obtencion de las variables "correlatvo" y "status" del Objeto JSON
                            String Correlativo = jsonObject.getString("correlativo");
                            String status = jsonObject.getString("status");
                            //Analicis de la respuesta con la variable status
                            if (status.equals("200")) {
                                //Funcion que cambia el status del pedido de 0 a 1
                                cambiarEstadoPedido(Correlativo);
                                //tv_pedidossubidos.setText("Pedido " + Correlativo + " Cargado Correctamente");
                                Toast.makeText(SincronizacionActivity.this, "Pedido " + Correlativo + " Cargado Correctamente.", LENGTH_LONG).show();
                            }

                            if (status.equals("111")) {
                                //Funcion que cambia el status del pedido de 0 a 1
                                cambiarEstadoPedido(Correlativo);
                                //tv_pedidossubidos.setText("Pedido" + Correlativo + " previamente cargado");
                                Toast.makeText(SincronizacionActivity.this, "Pedido" + Correlativo + " previamente cargado. \nRecomendaión: Sincronizar nuevamente.", LENGTH_LONG).show();
                            }

                            if (status.equals("112")) {
                                //tv_pedidossubidos.setText("Linea(s) del pedido" + Correlativo + " repetida(s)");
                                Toast.makeText(SincronizacionActivity.this, "Línea(s) del pedido" + Correlativo + " repetida(s) \nRecomendación: Verificar el contenido del pedido.", LENGTH_LONG).show();
                            }

                            if (status.equals("403")) {
                                //tv_pedidossubidos.setText("Correlativos del pedido" + Correlativo + " no concuerdan");
                                Toast.makeText(SincronizacionActivity.this, "Correlativos del pedido" + Correlativo + " no concuerdan \nRecomendaión: Rehacer el pedido.", LENGTH_LONG).show();
                            }

                            if (status.equals("404")) {
                                //tv_pedidossubidos.setText("Correlativos del pedido" + Correlativo + " no concuerdan");
                                Toast.makeText(SincronizacionActivity.this, "Error súbito.", LENGTH_LONG).show();
                            }
                        }
                        //Mensaje final del proceso
                        tv_pedidossubidos.setText("Pedidos Procesados");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        System.out.println("Error status-correlativo");
                    }


                }
            }
        }, new Response.ErrorListener() {
            @Override
            //Respuesta negativa del url
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(SincronizacionActivity.this, "Error en la subida", Toast.LENGTH_SHORT).show();
                bt_subir.setEnabled(true);
            }
        });

        //Envio puesto en cola
        requestQueue.add(jsonObjectRequest);
    }


    /*******************tabla kardex******************************************************/


    private void BajarKardex(String URL) {

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (!(response.getString("articulo").equals("null"))) {
                        JSONObject jsonObject = null; //creamos un objeto json vacio

                        JSONArray articulo = response.getJSONArray("articulo");

                        conn = new AdminSQLiteOpenHelper(getApplicationContext(), "ke_android", null, 1);
                        SQLiteDatabase ke_android = conn.getWritableDatabase();
                        ke_android.delete("ke_kardex", "1", null);
                        for (int i = 0; i < articulo.length(); i++) {

                            try {

                                ke_android.beginTransaction();

                                jsonObject = articulo.getJSONObject(i);
                                codigoKardex = jsonObject.getString("codigo").trim();
                                cantidadKardex = jsonObject.getDouble("cantidad");
                                fechaKardex = jsonObject.getString("fecha");

                                ContentValues insertar = new ContentValues();
                                insertar.put("kde_codart", codigoKardex);
                                insertar.put("kde_cantidad", cantidadKardex);
                                insertar.put("ke_fecha", fechaKardex);

                                ke_android.insert("ke_kardex", null, insertar);

                                ke_android.setTransactionSuccessful();

                            } catch (Exception e) {
                                Toast.makeText(getApplicationContext(), "Error 33", LENGTH_LONG).show();
                            } finally {
                                ke_android.endTransaction();
                            }

                        }
                        Toast.makeText(getApplicationContext(), "Hay artículos nuevos/actualizados", LENGTH_LONG).show();
                        progressDialog.setMessage("Kard: Actualizando.");
                        varAux++;
                        progressDialog.incrementProgressBy((int) numBarraProgreso);
                        sincronizacionVendedor();
                    } else if ((response.getString("articulo").equals("null"))) {
                        progressDialog.setMessage("Kard: Sin Actualización.");
                        varAux++;
                        progressDialog.incrementProgressBy((int) numBarraProgreso);
                        sincronizacionVendedor();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //--Manejo visual que indica al usuario del error--
                progressDialog.setMessage("Kard: No ha logrado sincronizar.");
                varAux++;
                progressDialog.incrementProgressBy((int) numBarraProgreso);
                //varAuxError = true;
                //AnalisisError2();
                //------
                sincronizacionVendedor();

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {  //finalmente, estos son los parametros que le enviaremos al webservice, partiendo de las variables
                //donde estan guardados las fechas
                Map<String, String> parametros = new HashMap<String, String>();
                // parametros.put("fecha_sinc", fecha_sinc);

                return parametros;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest); //esto es el request que se envia al url a traves de la conexion volley, (el stringrequest esta armado arriba)

    }
}