package com.appcloos.mimaletin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SearchView;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class PlanificadorActivity extends AppCompatActivity {
    AdminSQLiteOpenHelper conn;
    ListView listaplanificador;
    private PlanificadorAdapter planificadorAdapter;
    ArrayList<Documentos> listadocs;
    SwipeRefreshLayout swipeRefreshLayout;
    String cod_usuario, campo;
    private Boolean ll_commit;

    private static String codigoCliente, nombreCliente, agencia, tipodoc, documento, tipodocv, ruta_parme, emision, recepcion, vence, estatusdoc, grupo, subgrupo,
            codhijo, pid, codigo, nombre, fechadoc, vendedor, codcoord, fechamodifi, aceptadev, fechaDocs, kti_negesp, nombreEmpresa = "", enlaceEmpresa = "", codigoSucursal="";

    public static Double contribesp, tipoprecio, diascred, dtotneto, dtotimpuest, dtotalfinal, dtotpagos, dtotdescuen, dFlete, dtotdev, dvndmtototal,
            dretencion, dretencioniva, origen, cantidad, cntdevuelt, vndcntdevuelt, dpreciofin, dpreciounit, dmontoneto, dmontototal, timpueprc,
            unidevuelt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planificador);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//mantener la activity en vertical

        conn = new AdminSQLiteOpenHelper(getApplicationContext(), "ke_android", null, 11);
        //identifico en el layout el listview
        cargarEnlace();
        listaplanificador = findViewById(R.id.listaPlanificador);
        //cargo el codigo del vendedor que viene desde el activity anterior
        SharedPreferences preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        cod_usuario = preferences.getString("cod_usuario", null);
        //identifico el swipe refresh layout
        swipeRefreshLayout = findViewById(R.id.refreshplanificador);

        //tengo que validar si el usuario que inicio sesión es coordinador o vendedor
        validarTipodeUsuario(cod_usuario);
        consultarDocumentos(campo, cod_usuario);

        planificadorAdapter = new PlanificadorAdapter(PlanificadorActivity.this, listadocs);
        listaplanificador.setAdapter(planificadorAdapter);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                actualizarLista();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_planificador, menu);
       MenuItem menuItem = menu.findItem(R.id.search_view_docs);

        SearchView buscadorDocs = (SearchView) MenuItemCompat.getActionView(menuItem);
        buscadorDocs.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String busqueda) {
               BuscarDocumentos(busqueda);
                return false;
            }
            @Override
            public boolean onQueryTextChange(String busqueda) {
                BuscarDocumentos(busqueda);

                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);

    }

    private void BuscarDocumentos(String busqueda) {
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        Documentos documentos = null;
        listadocs = new ArrayList<Documentos>();

        Cursor cursor = ke_android.rawQuery("SELECT codcliente, nombrecli, estatusdoc, documento, vence, diascred, kti_negesp FROM ke_doccti WHERE " + campo +"='" + cod_usuario + "' " +
                "AND (nombrecli LIKE'%" + busqueda + "%' OR documento LIKE '%" + busqueda + "%') AND (estatusdoc = '0' OR estatusdoc= '1') ORDER BY vence asc", null);


        while(cursor.moveToNext()){
            documentos = new Documentos();
            documentos.setCodcliente(cursor.getString(0));
            documentos.setNombrecli(cursor.getString(1));
            documentos.setEstatusdoc(cursor.getString(2));
            documentos.setDocumento(cursor.getString(3));
            documentos.setVence(cursor.getString(4));
            documentos.setDiascred(cursor.getDouble(5));
            documentos.setKti_negesp(cursor.getString(6));
            listadocs.add(documentos);
        }
        ke_android.close();

        planificadorAdapter = new PlanificadorAdapter(PlanificadorActivity.this, listadocs);
        listaplanificador.setAdapter(planificadorAdapter);
        planificadorAdapter.notifyDataSetChanged();
    }


    public boolean onOptionsItemSelected(MenuItem item) {
        int itemid = item.getItemId();

        switch (itemid){

            case R.id.sync_docs:
                bajarDocumentos("https://"+enlaceEmpresa+"/Rest/planificador.php?campo=" + campo +  "&&vendedor=" + cod_usuario  +"&&agencia=" + codigoSucursal.trim());
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void cargarEnlace() {
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        String[] columnas = new String[]{
                "kee_nombre," +
                "kee_url," +
                        "kee_sucursal"};
        Cursor cursor = ke_android.query("ke_enlace", columnas, "1", null, null, null, null);

        while(cursor.moveToNext()){
            nombreEmpresa = cursor.getString(0);
            enlaceEmpresa = cursor.getString(1);
            codigoSucursal = cursor.getString(2);
        }
        ke_android.close();
    }

    private void bajarDocumentos(String URL) {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(URL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

                if( response != null){
                    conn = new AdminSQLiteOpenHelper(getApplicationContext(), "ke_android", null, 8);
                    SQLiteDatabase ke_android = conn.getWritableDatabase();

                    JSONObject jsonObject = null; //creamos un objeto json vacio
                    ll_commit = false;

                    ke_android.beginTransaction();

                    for(int i = 0; i < response.length(); i++){
                        try{

                            //obtengo de la respuesta los datos en un json object
                            jsonObject   = response.getJSONObject(i);
                            //preparo los campos para las operaciones
                            agencia       = jsonObject.getString("agencia").trim();
                            tipodoc       = jsonObject.getString("tipodoc").trim();
                            documento     = jsonObject.getString("documento").trim();
                            tipodocv      = jsonObject.getString("tipodocv").trim();
                            codigoCliente = jsonObject.getString("codcliente").trim();
                            nombreCliente = jsonObject.getString("nombrecli").trim();
                            contribesp    = jsonObject.getDouble("contribesp");
                            ruta_parme    = jsonObject.getString("ruta_parme").trim();
                            tipoprecio    = jsonObject.getDouble("tipoprecio");
                            emision       = jsonObject.getString("emision").trim();
                            recepcion     = jsonObject.getString("recepcion").trim();
                            vence         = jsonObject.getString("vence").trim();
                            diascred      = jsonObject.getDouble("diascred");
                            estatusdoc    = jsonObject.getString("estatusdoc").trim();
                            dtotneto      = jsonObject.getDouble("dtotneto");
                            dtotimpuest   = jsonObject.getDouble("dtotimpuest");
                            dtotalfinal   = jsonObject.getDouble("dtotalfinal");
                            dtotpagos     = jsonObject.getDouble("dtotpagos");
                            dtotdescuen   = jsonObject.getDouble("dtotdescuen");
                            dFlete        = jsonObject.getDouble("dFlete");
                            dtotdev       = jsonObject.getDouble("dtotdev");
                            dvndmtototal  = jsonObject.getDouble("dvndmtototal");
                            dretencion    = jsonObject.getDouble("dretencion");
                            dretencioniva = jsonObject.getDouble("dretencioniva");
                            vendedor      = jsonObject.getString("vendedor").trim();
                            codcoord      = jsonObject.getString("codcoord").trim();
                            fechamodifi   = jsonObject.getString("fechamodifi").trim();
                            aceptadev     = jsonObject.getString("aceptadev").trim();
                            kti_negesp    = jsonObject.getString("kti_negesp").trim();

                            ContentValues qDocumentosCab = new ContentValues();
                            qDocumentosCab.put("agencia",    agencia);
                            qDocumentosCab.put("tipodoc",    tipodoc);
                            qDocumentosCab.put("documento",  documento);
                            qDocumentosCab.put("tipodocv",   tipodocv);
                            qDocumentosCab.put("codcliente", codigoCliente);
                            qDocumentosCab.put("nombrecli",  nombreCliente);
                            qDocumentosCab.put("contribesp", contribesp);
                            qDocumentosCab.put("ruta_parme", ruta_parme);
                            qDocumentosCab.put("tipoprecio", tipoprecio);
                            qDocumentosCab.put("emision",    emision);
                            qDocumentosCab.put("recepcion",  recepcion);
                            qDocumentosCab.put("vence",      vence);
                            qDocumentosCab.put("diascred",   diascred);
                            qDocumentosCab.put("estatusdoc", estatusdoc);
                            qDocumentosCab.put("dtotneto",    dtotneto);
                            qDocumentosCab.put("dtotimpuest",  dtotimpuest);
                            qDocumentosCab.put("dtotalfinal",  dtotalfinal);
                            qDocumentosCab.put("dtotpagos",    dtotpagos);
                            qDocumentosCab.put("dtotdescuen",   dtotdescuen);
                            qDocumentosCab.put("dFlete",        dFlete);
                            qDocumentosCab.put("dtotdev",       dtotdev);
                            qDocumentosCab.put("dvndmtototal",  dvndmtototal);
                            qDocumentosCab.put("vendedor",      vendedor);
                            qDocumentosCab.put("codcoord",      codcoord);
                            qDocumentosCab.put("fechamodifi",   fechamodifi);
                            qDocumentosCab.put("aceptadev",     aceptadev);
                            qDocumentosCab.put("kti_negesp",    kti_negesp);

                            Cursor qcodigoLocal = ke_android.rawQuery("SELECT count(documento) FROM ke_doccti WHERE documento ='" + documento + "'", null);
                            qcodigoLocal.moveToFirst();

                            int codigoExiste = qcodigoLocal.getInt(0);

                            if(codigoExiste > 0){
                                ke_android.update("ke_doccti", qDocumentosCab, "documento = ?", new String[]{documento});
                            } else if (codigoExiste == 0){
                                ke_android.insert("ke_doccti", null, qDocumentosCab);
                            }

                            ll_commit    = true;

                        }catch (Exception e){
                            System.out.println("Error de inserción: " + e);
                            ll_commit = false;

                            if(!ll_commit){
                                return;
                            }
                        }

                    }
                    if(ll_commit){
                        ke_android.setTransactionSuccessful();
                        ke_android.endTransaction();
                        actualizarLista();

                    } else if(!ll_commit){
                        ke_android.endTransaction();
                    }

                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parametros = new HashMap<String, String>();
                parametros.put("fecha_sinc", fechaDocs);
                parametros.put("codigo_cli", codigoCliente);
                return parametros;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonArrayRequest); //esto es el request que se envia al url a traves de la conexion volley, (el stringrequest esta armado arriba)

    }



    private void actualizarLista() {
        consultarDocumentos(campo, cod_usuario);
        planificadorAdapter = new PlanificadorAdapter(PlanificadorActivity.this, listadocs);
        listaplanificador.setAdapter(planificadorAdapter);
        planificadorAdapter.notifyDataSetChanged();
    }

    private void consultarDocumentos(String campo, String cod_usuario) {
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        Documentos documentos = null;
        listadocs = new ArrayList<Documentos>();

        Cursor cursor = ke_android.rawQuery("SELECT codcliente, nombrecli, estatusdoc, documento, vence, diascred, kti_negesp FROM ke_doccti WHERE " + campo + "= '" + cod_usuario + "' AND (estatusdoc = '0' OR estatusdoc= '1') ORDER BY vence asc", null);

        while(cursor.moveToNext()){
            documentos = new Documentos();
            documentos.setCodcliente(cursor.getString(0));
            documentos.setNombrecli(cursor.getString(1));
            documentos.setEstatusdoc(cursor.getString(2));
            documentos.setDocumento(cursor.getString(3));
            documentos.setVence(cursor.getString(4));
            documentos.setDiascred(cursor.getDouble(5));
            documentos.setKti_negesp(cursor.getString(6));
           listadocs.add(documentos);
        }
        ke_android.close();



    }

    private void validarTipodeUsuario(String cod_usuario) {
        String tipodeUsuario = "";
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        Cursor cursorusu = ke_android.rawQuery("SELECT superves FROM usuarios WHERE vendedor ='" + cod_usuario +"'", null);

        while(cursorusu.moveToNext()){
            tipodeUsuario = cursorusu.getString(0);
        }

        switch (tipodeUsuario){

            case "0":
                campo = "vendedor";
                break;

            case "1":
                campo = "codcoord";
                break;
        }
    }


}