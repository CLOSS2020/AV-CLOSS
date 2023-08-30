package com.appcloos.mimaletin;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EstadisticasActivity extends AppCompatActivity {
    AdminSQLiteOpenHelper conn;
    ListView listavendedores;
    private VendedoresAdapter vendedoresAdapter;
    ArrayList<Estadistica> listadeestadisticas;
    public static String cod_usuario, campo, fechaEstadis, nombrevend, codcoord, nomcoord, vendedor, fecha_estad, nombreEmpresa = "", enlaceEmpresa = "", codigoSucursal = "", ppgdol_totneto, devdol_totneto, defdol_totneto, totdolcob;
    public static Double cntpedidos, mtopedidos, cntfacturas, mtofacturas, metavend, prcmeta, cntclientes, clivisit, prcvisitas, lom_montovtas, lom_prcvtas, lom_prcvisit, rlom_montovtas,
            rlom_prcvtas, rlom_prcvisit, codigoVend;
    SwipeRefreshLayout swipeRefreshLayout;
    TextView tv_porcentaje;
    //TableRow rw_porcentaje;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estadisticas);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);//mantener la activity en vertical
        conn = new AdminSQLiteOpenHelper(getApplicationContext(), "ke_android", null, 4);
        listavendedores = findViewById(R.id.listaVendedores);
        SharedPreferences preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        cod_usuario = preferences.getString("cod_usuario", null);
        swipeRefreshLayout = findViewById(R.id.refreshlay);
        tv_porcentaje = findViewById(R.id.tv_porcentaje);
        cargarEnlace();


        validarTipodeUsuario(cod_usuario);
        consultarVendedores(campo, cod_usuario);

        vendedoresAdapter = new VendedoresAdapter(EstadisticasActivity.this, listadeestadisticas);
        listavendedores.setAdapter(vendedoresAdapter);


        swipeRefreshLayout.setOnRefreshListener(() -> {
            actualizarLista();
            swipeRefreshLayout.setRefreshing(false);
        });


        listavendedores.setOnItemClickListener((adapterView, view, position, l) -> {

            final String codigoVend = listadeestadisticas.get(position).getVendedor();
            final String nombreVendedor = listadeestadisticas.get(position).getNombrevend();
            iraDetalleVendedor(codigoVend, nombreVendedor);


        });

        ObjetoAux objetoAux = new ObjetoAux(this);
        objetoAux.descargaDesactivo(cod_usuario);
    }

    private void iraDetalleVendedor(String codigoVend, String nombreVendedor) {

        Intent intent = new Intent(getApplicationContext(), DetalleVendedorActivity.class);
        intent.putExtra("codigoVend", codigoVend);
        intent.putExtra("nombreVend", nombreVendedor);
        startActivity(intent);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_estadisticas_main, menu);
        MenuItem menuItem = menu.findItem(R.id.search_view_estadisticas);

        SearchView buscadorVendedores = (SearchView) MenuItemCompat.getActionView(menuItem);
        buscadorVendedores.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String busqueda) {
                BuscarVendedor(busqueda);

                return false;
            }

            @Override
            public boolean onQueryTextChange(String busqueda) {
                BuscarVendedor(busqueda);

                return false;
            }
        });


        return super.onCreateOptionsMenu(menu);

    }

    private void BuscarVendedor(String busqueda) {
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        Estadistica estadistica;
        listadeestadisticas = new ArrayList<>();

        Cursor cursor = ke_android.rawQuery("SELECT vendedor, nombrevend, prcmeta, fecha_estad FROM ke_estadc01 WHERE " + campo + "='" + cod_usuario + "' AND (vendedor LIKE '%" + busqueda + "%' OR nombrevend LIKE '%" + busqueda + "%')  ORDER BY prcmeta desc", null);

        while (cursor.moveToNext()) {
            estadistica = new Estadistica();
            estadistica.setVendedor(cursor.getString(0));
            estadistica.setNombrevend(cursor.getString(1));
            double prcmeta = cursor.getDouble(2);
            prcmeta = Math.round(prcmeta * 100.0) / 100.0;
            estadistica.setPrcmeta(prcmeta);
            estadistica.setFecha_estad(cursor.getString(3));

            listadeestadisticas.add(estadistica);
        }
        cursor.close();
        ke_android.close();
        vendedoresAdapter = new VendedoresAdapter(EstadisticasActivity.this, listadeestadisticas);
        listavendedores.setAdapter(vendedoresAdapter);
        vendedoresAdapter.notifyDataSetChanged();

    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int itemid = item.getItemId();

        if (itemid == R.id.sync_estad) {
            getFecha();
            bajarEstadisticas("https://" + enlaceEmpresa + "/webservice/estadisticas.php?campo=" + campo + "&&cod_usuario=" + cod_usuario.trim() + "&&fecha_sinc=" + fechaEstadis + "&&agencia=" + codigoSucursal.trim());
        }

        return super.onOptionsItemSelected(item);
    }

    private void getFecha() {
        //Calendar hoy = Calendar.getInstance();
        //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        //fechaEstadis = sdf.format(hoy.getTime());
        fechaEstadis = "0001-01-01";


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
        cursor.close();
        ke_android.close();
    }

    private void bajarEstadisticas(String URL) {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(URL, response -> {
            System.out.println(response);
            if (response != null) {

                Toast.makeText(getApplicationContext(), "Descargando Estadísticas", Toast.LENGTH_SHORT).show();
                SQLiteDatabase ke_android = conn.getWritableDatabase();
                long filas = DatabaseUtils.queryNumEntries(ke_android, "ke_estadc01");

                if (filas > 0) {

                    JSONObject jsonObject = null; //creamos un objeto json vacio

                    for (int i = 0; i < response.length(); i++) {
                        try {

                            ke_android.beginTransaction();
                            jsonObject = response.getJSONObject(i);

                            codcoord = jsonObject.getString("codcoord").trim();
                            nomcoord = jsonObject.getString("nomcoord").trim();
                            vendedor = jsonObject.getString("vendedor").trim();
                            nombrevend = jsonObject.getString("nombrevend").trim();
                            cntpedidos = jsonObject.getDouble("cntpedidos");
                            mtopedidos = jsonObject.getDouble("mtopedidos");
                            cntfacturas = jsonObject.getDouble("cntfacturas");
                            mtofacturas = jsonObject.getDouble("mtofacturas");
                            metavend = jsonObject.getDouble("metavend");
                            prcmeta = jsonObject.getDouble("prcmeta");
                            cntclientes = jsonObject.getDouble("cntclientes");
                            clivisit = jsonObject.getDouble("clivisit");
                            prcvisitas = jsonObject.getDouble("prcvisitas");
                            lom_montovtas = jsonObject.getDouble("lom_montovtas");
                            lom_prcvtas = jsonObject.getDouble("lom_prcvtas");
                            lom_prcvisit = jsonObject.getDouble("lom_prcvisit");
                            rlom_montovtas = jsonObject.getDouble("rlom_montovtas");
                            rlom_prcvtas = jsonObject.getDouble("rlom_prcvtas");
                            rlom_prcvisit = jsonObject.getDouble("rlom_prcvisit");
                            fecha_estad = jsonObject.getString("fecha_estad");
                            ppgdol_totneto = jsonObject.getString("ppgdol_totneto");
                            devdol_totneto = jsonObject.getString("devdol_totneto");
                            defdol_totneto = jsonObject.getString("defdol_totneto");
                            totdolcob = jsonObject.getString("totdolcob");

                            ContentValues actualizar = new ContentValues();
                            actualizar.put("codcoord", codcoord);
                            actualizar.put("nomcoord", nomcoord);
                            actualizar.put("vendedor", vendedor);
                            actualizar.put("nombrevend", nombrevend);
                            actualizar.put("cntpedidos", cntpedidos);
                            actualizar.put("mtopedidos", mtopedidos);
                            actualizar.put("cntfacturas", cntfacturas);
                            actualizar.put("mtofacturas", mtofacturas);
                            actualizar.put("metavend", metavend);
                            actualizar.put("prcmeta", prcmeta);
                            actualizar.put("cntclientes", cntclientes);
                            actualizar.put("clivisit", clivisit);
                            actualizar.put("prcvisitas", prcvisitas);
                            actualizar.put("lom_montovtas", lom_montovtas);
                            actualizar.put("lom_prcvtas", lom_prcvtas);
                            actualizar.put("lom_prcvisit", lom_prcvisit);
                            actualizar.put("rlom_montovtas", rlom_montovtas);
                            actualizar.put("rlom_prcvtas", rlom_prcvtas);
                            actualizar.put("rlom_prcvisit", rlom_prcvisit);
                            actualizar.put("fecha_estad", fecha_estad);
                            actualizar.put("ppgdol_totneto", ppgdol_totneto);
                            actualizar.put("devdol_totneto", devdol_totneto);
                            actualizar.put("defdol_totneto", defdol_totneto);
                            actualizar.put("totdolcob", totdolcob);

                            ke_android.update("ke_estadc01", actualizar, "vendedor = ?", new String[]{vendedor});
                            ke_android.setTransactionSuccessful();
                            Toast.makeText(EstadisticasActivity.this, "Datos actualizados.", Toast.LENGTH_SHORT).show();

                        } catch (SQLException | JSONException e) {
                            Toast.makeText(EstadisticasActivity.this, "Error en la insercion en " + e, Toast.LENGTH_LONG).show();
                        } finally {
                            ke_android.endTransaction();
                        }

                        Cursor vendedorExistente = ke_android.rawQuery("SELECT count(vendedor) FROM ke_estadc01 WHERE vendedor ='" + vendedor + "'", null);
                        vendedorExistente.moveToFirst();
                        int vendedor_existente = vendedorExistente.getInt(0);
                        vendedorExistente.close();
                        if (vendedor_existente > 0) {
                            try {

                                ke_android.beginTransaction();
                                jsonObject = response.getJSONObject(i);

                                codcoord = jsonObject.getString("codcoord").trim();
                                nomcoord = jsonObject.getString("nomcoord").trim();
                                vendedor = jsonObject.getString("vendedor").trim();
                                nombrevend = jsonObject.getString("nombrevend").trim();
                                cntpedidos = jsonObject.getDouble("cntpedidos");
                                mtopedidos = jsonObject.getDouble("mtopedidos");
                                cntfacturas = jsonObject.getDouble("cntfacturas");
                                mtofacturas = jsonObject.getDouble("mtofacturas");
                                metavend = jsonObject.getDouble("metavend");
                                prcmeta = jsonObject.getDouble("prcmeta");
                                cntclientes = jsonObject.getDouble("cntclientes");
                                clivisit = jsonObject.getDouble("clivisit");
                                prcvisitas = jsonObject.getDouble("prcvisitas");
                                lom_montovtas = jsonObject.getDouble("lom_montovtas");
                                lom_prcvtas = jsonObject.getDouble("lom_prcvtas");
                                lom_prcvisit = jsonObject.getDouble("lom_prcvisit");
                                rlom_montovtas = jsonObject.getDouble("rlom_montovtas");
                                rlom_prcvtas = jsonObject.getDouble("rlom_prcvtas");
                                rlom_prcvisit = jsonObject.getDouble("rlom_prcvisit");
                                fecha_estad = jsonObject.getString("fecha_estad");
                                ppgdol_totneto = jsonObject.getString("ppgdol_totneto");
                                devdol_totneto = jsonObject.getString("devdol_totneto");
                                defdol_totneto = jsonObject.getString("defdol_totneto");
                                totdolcob = jsonObject.getString("totdolcob");

                                ContentValues actualizar = new ContentValues();
                                actualizar.put("codcoord", codcoord);
                                actualizar.put("nomcoord", nomcoord);
                                actualizar.put("vendedor", vendedor);
                                actualizar.put("nombrevend", nombrevend);
                                actualizar.put("cntpedidos", cntpedidos);
                                actualizar.put("mtopedidos", mtopedidos);
                                actualizar.put("cntfacturas", cntfacturas);
                                actualizar.put("mtofacturas", mtofacturas);
                                actualizar.put("metavend", metavend);
                                actualizar.put("prcmeta", prcmeta);
                                actualizar.put("cntclientes", cntclientes);
                                actualizar.put("clivisit", clivisit);
                                actualizar.put("prcvisitas", prcvisitas);
                                actualizar.put("lom_montovtas", lom_montovtas);
                                actualizar.put("lom_prcvtas", lom_prcvtas);
                                actualizar.put("lom_prcvisit", lom_prcvisit);
                                actualizar.put("rlom_montovtas", rlom_montovtas);
                                actualizar.put("rlom_prcvtas", rlom_prcvtas);
                                actualizar.put("rlom_prcvisit", rlom_prcvisit);
                                actualizar.put("fecha_estad", fecha_estad);
                                actualizar.put("ppgdol_totneto", ppgdol_totneto);
                                actualizar.put("devdol_totneto", devdol_totneto);
                                actualizar.put("defdol_totneto", defdol_totneto);
                                actualizar.put("totdolcob", totdolcob);


                                ke_android.update("ke_estadc01", actualizar, "vendedor = ?", new String[]{vendedor});
                                ke_android.setTransactionSuccessful();
                                Toast.makeText(EstadisticasActivity.this, "Datos actualizados.", Toast.LENGTH_SHORT).show();

                            } catch (SQLException | JSONException e) {
                                Toast.makeText(EstadisticasActivity.this, "Error en la insercion en " + e, Toast.LENGTH_LONG).show();
                            } finally {
                                ke_android.endTransaction();
                            }


                        } else {

                            try {

                                ke_android.beginTransaction();
                                jsonObject = response.getJSONObject(i);

                                codcoord = jsonObject.getString("codcoord").trim();
                                nomcoord = jsonObject.getString("nomcoord").trim();
                                vendedor = jsonObject.getString("vendedor").trim();
                                nombrevend = jsonObject.getString("nombrevend").trim();
                                cntpedidos = jsonObject.getDouble("cntpedidos");
                                mtopedidos = jsonObject.getDouble("mtopedidos");
                                cntfacturas = jsonObject.getDouble("cntfacturas");
                                mtofacturas = jsonObject.getDouble("mtofacturas");
                                metavend = jsonObject.getDouble("metavend");
                                prcmeta = jsonObject.getDouble("prcmeta");
                                cntclientes = jsonObject.getDouble("cntclientes");
                                clivisit = jsonObject.getDouble("clivisit");
                                prcvisitas = jsonObject.getDouble("prcvisitas");
                                lom_montovtas = jsonObject.getDouble("lom_montovtas");
                                lom_prcvtas = jsonObject.getDouble("lom_prcvtas");
                                lom_prcvisit = jsonObject.getDouble("lom_prcvisit");
                                rlom_montovtas = jsonObject.getDouble("rlom_montovtas");
                                rlom_prcvtas = jsonObject.getDouble("rlom_prcvtas");
                                rlom_prcvisit = jsonObject.getDouble("rlom_prcvisit");
                                fecha_estad = jsonObject.getString("fecha_estad");
                                ppgdol_totneto = jsonObject.getString("ppgdol_totneto");
                                devdol_totneto = jsonObject.getString("devdol_totneto");
                                defdol_totneto = jsonObject.getString("defdol_totneto");
                                totdolcob = jsonObject.getString("totdolcob");

                                ContentValues insertar = new ContentValues();
                                insertar.put("codcoord", codcoord);
                                insertar.put("nomcoord", nomcoord);
                                insertar.put("vendedor", vendedor);
                                insertar.put("nombrevend", nombrevend);
                                insertar.put("cntpedidos", cntpedidos);
                                insertar.put("mtopedidos", mtopedidos);
                                insertar.put("cntfacturas", cntfacturas);
                                insertar.put("mtofacturas", mtofacturas);
                                insertar.put("metavend", metavend);
                                insertar.put("prcmeta", prcmeta);
                                insertar.put("cntclientes", cntclientes);
                                insertar.put("clivisit", clivisit);
                                insertar.put("prcvisitas", prcvisitas);
                                insertar.put("lom_montovtas", lom_montovtas);
                                insertar.put("lom_prcvtas", lom_prcvtas);
                                insertar.put("lom_prcvisit", lom_prcvisit);
                                insertar.put("rlom_montovtas", rlom_montovtas);
                                insertar.put("rlom_prcvtas", rlom_prcvtas);
                                insertar.put("rlom_prcvisit", rlom_prcvisit);
                                insertar.put("fecha_estad", fecha_estad);
                                insertar.put("ppgdol_totneto", ppgdol_totneto);
                                insertar.put("devdol_totneto", devdol_totneto);
                                insertar.put("defdol_totneto", defdol_totneto);
                                insertar.put("totdolcob", totdolcob);

                                ke_android.insert("ke_estadc01", null, insertar);
                                ke_android.setTransactionSuccessful();
                                Toast.makeText(EstadisticasActivity.this, "Datos actualizados.", Toast.LENGTH_SHORT).show();

                            } catch (SQLException | JSONException e) {
                                Toast.makeText(EstadisticasActivity.this, "Error en la insercion en " + e, Toast.LENGTH_LONG).show();
                            } finally {
                                ke_android.endTransaction();
                            }

                        }
                    }//aca cierra el for
                } else { // aqui cierran las filas

                    JSONObject jsonObject; //creamos un objeto json vacio

                    for (int i = 0; i < response.length(); i++) {

                        try {

                            ke_android.beginTransaction();
                            jsonObject = response.getJSONObject(i);

                            codcoord = jsonObject.getString("codcoord").trim();
                            nomcoord = jsonObject.getString("nomcoord").trim();
                            vendedor = jsonObject.getString("vendedor").trim();
                            nombrevend = jsonObject.getString("nombrevend").trim();
                            cntpedidos = jsonObject.getDouble("cntpedidos");
                            mtopedidos = jsonObject.getDouble("mtopedidos");
                            cntfacturas = jsonObject.getDouble("cntfacturas");
                            mtofacturas = jsonObject.getDouble("mtofacturas");
                            metavend = jsonObject.getDouble("metavend");
                            prcmeta = jsonObject.getDouble("prcmeta");
                            cntclientes = jsonObject.getDouble("cntclientes");
                            clivisit = jsonObject.getDouble("clivisit");
                            prcvisitas = jsonObject.getDouble("prcvisitas");
                            lom_montovtas = jsonObject.getDouble("lom_montovtas");
                            lom_prcvtas = jsonObject.getDouble("lom_prcvtas");
                            lom_prcvisit = jsonObject.getDouble("lom_prcvisit");
                            rlom_montovtas = jsonObject.getDouble("rlom_montovtas");
                            rlom_prcvtas = jsonObject.getDouble("rlom_prcvtas");
                            rlom_prcvisit = jsonObject.getDouble("rlom_prcvisit");
                            fecha_estad = jsonObject.getString("fecha_estad");
                            ppgdol_totneto = jsonObject.getString("ppgdol_totneto");
                            devdol_totneto = jsonObject.getString("devdol_totneto");
                            defdol_totneto = jsonObject.getString("defdol_totneto");
                            totdolcob = jsonObject.getString("totdolcob");

                            ContentValues insertar = new ContentValues();
                            insertar.put("codcoord", codcoord);
                            insertar.put("nomcoord", nomcoord);
                            insertar.put("vendedor", vendedor);
                            insertar.put("nombrevend", nombrevend);
                            insertar.put("cntpedidos", cntpedidos);
                            insertar.put("mtopedidos", mtopedidos);
                            insertar.put("cntfacturas", cntfacturas);
                            insertar.put("mtofacturas", mtofacturas);
                            insertar.put("metavend", metavend);
                            insertar.put("prcmeta", prcmeta);
                            insertar.put("cntclientes", cntclientes);
                            insertar.put("clivisit", clivisit);
                            insertar.put("prcvisitas", prcvisitas);
                            insertar.put("lom_montovtas", lom_montovtas);
                            insertar.put("lom_prcvtas", lom_prcvtas);
                            insertar.put("lom_prcvisit", lom_prcvisit);
                            insertar.put("rlom_montovtas", rlom_montovtas);
                            insertar.put("rlom_prcvtas", rlom_prcvtas);
                            insertar.put("rlom_prcvisit", rlom_prcvisit);
                            insertar.put("fecha_estad", fecha_estad);
                            insertar.put("ppgdol_totneto", ppgdol_totneto);
                            insertar.put("devdol_totneto", devdol_totneto);
                            insertar.put("defdol_totneto", defdol_totneto);
                            insertar.put("totdolcob", totdolcob);

                            ke_android.insert("ke_estadc01", null, insertar);
                            ke_android.setTransactionSuccessful();
                            Toast.makeText(EstadisticasActivity.this, "Datos actualizados.", Toast.LENGTH_SHORT).show();

                        } catch (SQLException | JSONException e) {
                            Toast.makeText(EstadisticasActivity.this, "Error en la insercion en " + e, Toast.LENGTH_LONG).show();
                        } finally {
                            ke_android.endTransaction();
                        }

                    }
                    ke_android.close();
                }
                actualizarLista();


            } else {
                Toast.makeText(EstadisticasActivity.this, "Sin actualización", Toast.LENGTH_LONG).show();
            }

        }, error -> Toast.makeText(EstadisticasActivity.this, "Sin actualización", Toast.LENGTH_LONG).show()) {
            protected Map<String, String> getParams() {
                return new HashMap<>();
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonArrayRequest); //esto es el request que se envia al url a traves de la conexion volley, (el stringrequest esta armado arriba

    }

    private void actualizarLista() {
        consultarVendedores(campo, cod_usuario);
        vendedoresAdapter = new VendedoresAdapter(EstadisticasActivity.this, listadeestadisticas);
        listavendedores.setAdapter(vendedoresAdapter);
        vendedoresAdapter.notifyDataSetChanged();
    }


    private void validarTipodeUsuario(String cod_usuario) {

        String tipodeUsuario = "";
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        Cursor cursorusu = ke_android.rawQuery("SELECT superves FROM usuarios WHERE vendedor ='" + cod_usuario + "'", null);

        while (cursorusu.moveToNext()) {
            tipodeUsuario = cursorusu.getString(0);
        }
        cursorusu.close();

        switch (tipodeUsuario) {

            case "0":
                campo = "vendedor";
                break;

            case "1":
                campo = "codcoord";
                break;
        }
    }


    //el metodo para consultar los vendedores segun el coordinador
    private void consultarVendedores(String campo, String cod_usuario) {

        SQLiteDatabase ke_android = conn.getWritableDatabase();
        Estadistica estadistica;
        listadeestadisticas = new ArrayList<>();

        Cursor cursor = ke_android.rawQuery("SELECT vendedor, nombrevend, prcmeta, fecha_estad FROM ke_estadc01 WHERE " + campo + "= '" + cod_usuario + "' ORDER BY prcmeta desc", null);

        while (cursor.moveToNext()) {
            estadistica = new Estadistica();
            estadistica.setVendedor(cursor.getString(0));
            estadistica.setNombrevend(cursor.getString(1));
            double prcmeta = cursor.getDouble(2);
            prcmeta = Math.round(prcmeta * 100.0) / 100.0;
            estadistica.setPrcmeta(prcmeta);
            estadistica.setFecha_estad(cursor.getString(3));

            listadeestadisticas.add(estadistica);
        }
        cursor.close();


        ke_android.close();

    }


}