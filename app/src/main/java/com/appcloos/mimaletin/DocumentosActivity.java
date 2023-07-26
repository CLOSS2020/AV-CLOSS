/* Activity: DocumentosActivity
*  Objetivo: visualizar los documentos por cliente
*  Autor   : PCV SEP 2021*/


package com.appcloos.mimaletin;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class DocumentosActivity extends AppCompatActivity {
    AdminSQLiteOpenHelper conn;

    private static String codigoCliente, nombreCliente, agencia, tipodoc, documento, tipodocv, ruta_parme, emision, recepcion, vence, estatusdoc, grupo, subgrupo,
            codhijo, pid, codigo, nombre, fechadoc, vendedor, codcoord, fechamodifi, aceptadev, fechaDocs, cod_usuario, codigoEmpresa = "", nombreEmpresa ="", enlaceEmpresa="", codigoSucursal="";

    public static Double contribesp, tipoprecio, diascred, dtotneto, dtotimpuest, dtotalfinal, dtotpagos, dtotdescuen, dFlete, dtotdev, dvndmtototal,
            dretencion, dretencioniva, origen, cantidad, cntdevuelt, vndcntdevuelt, dpreciofin, dpreciounit, dmontoneto, dmontototal, timpueprc,
            unidevuelt, bsiva, bsflete, bsretencion, bsretencioniva;

    private Boolean ll_commit;

    private DocumentosAdapter documentosAdapter;

    ListView listaDocumentos;
    ArrayList<String> listainfo;
    ArrayList<String> permisos;
    ArrayList<Documentos> listadocs;
    LineasAdapter lineasAdapter;
    ArrayList<Lineas>listalineasdoc;
    SQLiteDatabase ke_android;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_documentos);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//mantener la activity en vertical

        conn = new AdminSQLiteOpenHelper(getApplicationContext(), "ke_android", null, 8);
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        listaDocumentos = (ListView) findViewById(R.id.lv_documentos);
        cargarEnlace();
        Intent intent   = getIntent();
        codigoCliente = intent.getStringExtra("codigoCliente");
        nombreCliente = intent.getStringExtra("nombreCliente");
        cod_usuario   = intent.getStringExtra("cod_usuario");
        codigoEmpresa = intent.getStringExtra("codigoEmpresa");
        permisos = new ArrayList<String>();
        getSupportActionBar().setTitle(nombreCliente);

        GetfechaDocs();
        EvaluacionDeCargas();
        cargarModulos();
        System.out.println("codigo de la empresa: " + codigoEmpresa);
        System.out.println("Permisos que estan llegando en la ventana de documentos: " + permisos);

        documentosAdapter = new DocumentosAdapter(DocumentosActivity.this, listadocs);
        listaDocumentos.setAdapter(documentosAdapter);
        documentosAdapter.notifyDataSetChanged();


        //al presionar en un documento, abro un alertdialog
        listaDocumentos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {


                System.out.println(permisos);
                final String documentoP         = listadocs.get(position).getDocumento();
                final Double totnetoP           = Math.round(listadocs.get(position).getDtotneto() * 100.0) / 100.0;
                final Double totimpuestP        = Math.round(listadocs.get(position).getDtotimpuest() * 100.0) / 100.0;
                final Double totalfinalP        = Math.round(listadocs.get(position).getDtotalfinal() * 100.0) / 100.0;
                final Double totdescup          = Math.round(listadocs.get(position).getDtotdescuen() * 100.0) / 100.0;
                final String aceptaDevoluciones = listadocs.get(position).getAceptadev();
                final String estadoDoc          = listadocs.get(position).getEstatusdoc();
                System.out.println(estadoDoc);

                if(!permisos.contains("REC001")){
                    AlertDialog.Builder ventana = new AlertDialog.Builder(DocumentosActivity.this);
                    ventana.setTitle("Doc Nº: " + documentoP);
                    ventana.setMessage("Monto Neto  :       " + totnetoP + "$\n" +
                            "Monto IVA   :       " + totimpuestP + "$\n" +
                            "Descuentos  :       " + totdescup + "$\n" +
                            "Monto Total :       " + totalfinalP  + "$\n" +
                            "");


                    AlertDialog dialogo = ventana.create(); //creamos el dialogo en base a la ventana diseñada
                    dialogo.show(); //mostrar el dialogo


                    TextView messageText = (TextView)dialogo.findViewById(android.R.id.message);
                    messageText.setGravity(Gravity.RIGHT);

                }else{
                    AlertDialog.Builder ventana = new AlertDialog.Builder(DocumentosActivity.this);
                    ventana.setTitle("Doc Nº: " + documentoP);
                    ventana.setMessage("Monto Neto  :       " + totnetoP + "$\n" +
                            "Monto IVA   :       " + totimpuestP + "$\n" +
                            "Descuentos  :       " + totdescup + "$\n" +
                            "Monto Total :       " + totalfinalP  + "$\n" +
                            "");


                    ventana.setNeutralButton("Generar Reclamo", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if(estadoDoc.equals("2")){
                                Toast.makeText(DocumentosActivity.this, "Este documento ya no acepta devoluciones", Toast.LENGTH_LONG).show();
                            }else{
                                if(aceptaDevoluciones.equals("0")) {
                                    Toast.makeText(DocumentosActivity.this, "Este documento ya no acepta devoluciones", Toast.LENGTH_LONG).show();
                                } else if (aceptaDevoluciones.equals("1")){
                                    iraReclamos(documentoP, codigoCliente, nombreCliente);
                                }
                            }
                        }
                    });
                    AlertDialog dialogo = ventana.create(); //creamos el dialogo en base a la ventana diseñada
                    dialogo.show(); //mostrar el dialogo


                    TextView messageText = (TextView)dialogo.findViewById(android.R.id.message);
                    messageText.setGravity(Gravity.RIGHT);
                }

            }
        });
        vaciarTmp();


    }

    private void cargarModulos() {
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        Cursor cursor = ke_android.rawQuery("SELECT kmo_codigo FROM ke_modulos WHERE kmo_status = '1' AND ked_codigo='" + codigoEmpresa + "'", null);

        while (cursor.moveToNext()){
            permisos.add(cursor.getString(0));
            System.out.println("PERMISOS" + permisos);
        }
    }

    private void cargarEnlace() {
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        String[] columnas = new String[]{
                        "kee_nombre," +
                        "kee_url," +
                        "kee_sucursal",};
        Cursor cursor = ke_android.query("ke_enlace", columnas, "1", null, null, null, null);

        while(cursor.moveToNext()){
            nombreEmpresa = cursor.getString(0);
            enlaceEmpresa = cursor.getString(1);
            codigoSucursal = cursor.getString(2);
        }
        ke_android.close();
    }
    // metodo para determinar que debo traerme de la nube
    private void EvaluacionDeCargas() {
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        //si el cliente no tiene documentos, por primera vez debo simplemente traerme todo, de lo contrario, debere validad por fecha
        String fecha_auxiliar = "0001-01-01 00:00:00";
        String[] columnacli = new String[]{"count(documento)"};
        String condicioncli = "codcliente = '" + codigoCliente +  "'";
        Cursor cursorcli = ke_android.query("ke_doccti", columnacli, condicioncli, null, null, null, null);

        if(cursorcli.moveToFirst()){
            if(cursorcli.getInt(0) > 0){
                System.out.println("llego al if");
                System.out.println(cursorcli.getString(0));
                cargarCabeceraDocuemntosCliente("https://"+enlaceEmpresa+"/webservice/documentos.php?fecha_sinc=" + fechaDocs.trim() +"&&codigo_cli=" + codigoCliente.trim()+ "&&agencia=" + codigoSucursal.trim());
                consultarDocs();

            } else if (cursorcli.getInt(0) == 0){
                System.out.println("llego al else del principio");
                cargarCabeceraDocuemntosCliente("https://"+enlaceEmpresa+"/webservice/documentos.php?fecha_sinc=" + fecha_auxiliar.trim() +"&&codigo_cli=" + codigoCliente.trim()+ "&&agencia=" + codigoSucursal.trim());
                consultarDocs();
            }
        }
    }


    private void vaciarTmp() {
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        ke_android.beginTransaction();
        try {
            ke_android.execSQL("DELETE FROM ke_devlmtmp");
            ke_android.setTransactionSuccessful();
            ke_android.endTransaction();
        }catch (Exception e){
            ke_android.endTransaction();
        }
    }

    private void iraReclamos(String documentoP, String codigoCliente , String nombreCliente) {
        vaciarTmp();
        Intent intent = new Intent(getApplicationContext(), ReclamosActivity.class);
        //coloco los datos que necesito llevarme al siguiente Activity
        intent.putExtra("documentoP", documentoP);
        intent.putExtra("codigoCliente", codigoCliente);
        intent.putExtra("nombreCliente", nombreCliente);
        intent.putExtra("cod_usuario", cod_usuario);

        startActivity(intent); // inicio la actividad

    }

    private void iraDetalles(String documentoP) {
        AlertDialog.Builder builder = new AlertDialog.Builder(DocumentosActivity.this);
        builder.setTitle("Detalle del Doc. " + documentoP);
        ListView lv_detalledoc = new ListView(DocumentosActivity.this);
        verLineasDocumento(documentoP);
        lineasAdapter = new LineasAdapter(DocumentosActivity.this, listalineasdoc);
        lv_detalledoc.setAdapter(lineasAdapter);
        lineasAdapter.notifyDataSetChanged();
        builder.setView(lv_detalledoc);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void verLineasDocumento(String documentoP) {
        cargarLineasDocumento("https://"+enlaceEmpresa+"/webservice/lineasdocs.php?documento=" + documentoP.trim()+ "&&agencia=" + codigoSucursal.trim() );
        consultarLineasDoc();
    }

    private void consultarLineasDoc() {
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        Lineas lineas = null;

        listalineasdoc = new ArrayList<Lineas>();
        Cursor cursor = ke_android.rawQuery("SELECT pid, codigo, nombre, cantidad, dmontoneto, dpreciofin  FROM ke_doclmv WHERE documento ='" + documento + "' AND pid NOT IN " +
                "(SELECT kdel_pid FROM ke_devlmtmp)", null);

        while(cursor.moveToNext()){
            lineas = new Lineas();
            lineas.setPid(cursor.getString(0));
            lineas.setCodigo(cursor.getString(1));
            lineas.setNombre(cursor.getString(2));
            lineas.setCantidad(cursor.getDouble(3));
            lineas.setDmontototal(cursor.getDouble(4));
            lineas.setDpreciofin(cursor.getDouble(5));
            listalineasdoc.add(lineas);

        }
        lineasAdapter = new LineasAdapter(DocumentosActivity.this, listalineasdoc);
        lineasAdapter.notifyDataSetChanged();
        cursor.close();
    }

    private void cargarLineasDocumento(String URL) {
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
                            grupo         = jsonObject.getString("grupo").trim();
                            subgrupo      = jsonObject.getString("subgrupo").trim();
                            origen        = jsonObject.getDouble("origen");
                            codigo        = jsonObject.getString("codigo").trim();
                            codhijo       = jsonObject.getString("codhijo").trim();
                            pid           = jsonObject.getString("pid").trim();
                            nombre        = jsonObject.getString("nombre").trim();
                            cantidad      = jsonObject.getDouble("cantidad");
                            cntdevuelt    = jsonObject.getDouble("cntdevuelt");
                            vndcntdevuelt = jsonObject.getDouble("vndcntdevuelt");
                            dvndmtototal  = jsonObject.getDouble("dvndmtototal");
                            dpreciofin    = jsonObject.getDouble("dpreciofin");
                            dpreciounit   = jsonObject.getDouble("dpreciounit");
                            dmontoneto    = jsonObject.getDouble("dmontoneto");
                            dmontototal   = jsonObject.getDouble("dmontototal");
                            timpueprc     = jsonObject.getDouble("timpueprc");
                            unidevuelt    = jsonObject.getDouble("unidevuelt");
                            fechadoc      = jsonObject.getString("fechadoc").trim();
                            vendedor      = jsonObject.getString("vendedor").trim();
                            codcoord      = jsonObject.getString("codcoord").trim();
                            fechamodifi   = jsonObject.getString("fechamodifi").trim();


                            ContentValues qDocumentosLin = new ContentValues();
                            qDocumentosLin.put("agencia",    agencia);
                            qDocumentosLin.put("tipodoc",    tipodoc);
                            qDocumentosLin.put("documento",  documento);
                            qDocumentosLin.put("tipodocv",   tipodocv);
                            qDocumentosLin.put("grupo",      grupo);
                            qDocumentosLin.put("subgrupo",   subgrupo);
                            qDocumentosLin.put("origen",     origen);
                            qDocumentosLin.put("codigo",     codigo);
                            qDocumentosLin.put("codhijo",    codhijo);
                            qDocumentosLin.put("pid",          pid);
                            qDocumentosLin.put("nombre",       nombre);
                            qDocumentosLin.put("cantidad",     cantidad);
                            qDocumentosLin.put("cntdevuelt",   cntdevuelt);
                            qDocumentosLin.put("vndcntdevuelt",vndcntdevuelt);
                            qDocumentosLin.put("dvndmtototal", dvndmtototal);
                            qDocumentosLin.put("dpreciofin",   dpreciofin);
                            qDocumentosLin.put("dpreciounit",  dpreciounit);
                            qDocumentosLin.put("dmontoneto",   dmontoneto);
                            qDocumentosLin.put("dmontototal",  dmontototal);
                            qDocumentosLin.put("timpueprc",    timpueprc);
                            qDocumentosLin.put("unidevuelt",   unidevuelt);
                            qDocumentosLin.put("fechadoc",     fechadoc);
                            qDocumentosLin.put("vendedor",      vendedor);
                            qDocumentosLin.put("codcoord",      codcoord);
                            qDocumentosLin.put("fechamodifi",   fechamodifi);


                            Cursor qcodigoLocal = ke_android.rawQuery("SELECT count(pid) FROM ke_doclmv WHERE pid ='" + pid + "'", null);
                            qcodigoLocal.moveToFirst();

                            int codigoExiste = qcodigoLocal.getInt(0);

                            if(codigoExiste > 0){
                                ke_android.update("ke_doclmv", qDocumentosLin, "pid = ?", new String[]{pid});
                            } else if (codigoExiste == 0){
                                ke_android.insert("ke_doclmv", null, qDocumentosLin);
                            }

                            ll_commit    = true;
                            qcodigoLocal.close();
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
                parametros.put("documento", documento);

                return parametros;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonArrayRequest); //esto es el request que se envia al url a traves de la conexion volley, (el stringrequest esta armado arriba)


    }

    private void consultarDocs() {

        SQLiteDatabase ke_android = conn.getWritableDatabase();
        Documentos documentos = null;

        listadocs = new ArrayList<Documentos>();
        Cursor cursor = ke_android.rawQuery("SELECT documento, tipodocv, estatusdoc, dtotalfinal, emision, recepcion, dtotneto, dtotimpuest, dtotdescuen, aceptadev FROM ke_doccti WHERE codcliente ='" + codigoCliente + "'", null);

        while(cursor.moveToNext()){
            documentos = new Documentos();
            documentos.setDocumento(cursor.getString(0));
            documentos.setTipodocv(cursor.getString(1));
            documentos.setEstatusdoc(cursor.getString(2));
            documentos.setDtotalfinal(cursor.getDouble(3));
            documentos.setEmision(cursor.getString(4));
            documentos.setRecepcion(cursor.getString(5));
            documentos.setDtotneto(cursor.getDouble(6));
            documentos.setDtotimpuest(cursor.getDouble(7));
            documentos.setDtotdescuen(cursor.getDouble(8));
            documentos.setAceptadev(cursor.getString(9));
            listadocs.add(documentos);

        }

        documentosAdapter = new DocumentosAdapter(DocumentosActivity.this, listadocs);
        listaDocumentos.setAdapter(documentosAdapter);
        documentosAdapter.notifyDataSetChanged();
        //ke_android.close();
        cursor.close();

    }

    private void GetfechaDocs() {
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        Cursor fecha_ultmod = ke_android.rawQuery("SELECT fchhn_ultmod FROM tabla_aux WHERE tabla = 'ke_doccti'", null);
        fecha_ultmod.moveToFirst();
        fechaDocs = fecha_ultmod.getString(0);
        fecha_ultmod.close();
    }

    private void cargarCabeceraDocuemntosCliente(String URL) {

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
                            bsiva         = jsonObject.getDouble("bsiva");
                            bsflete       = jsonObject.getDouble("bsflete");
                            bsretencioniva = jsonObject.getDouble("bsretencioniva");
                            bsretencion    = jsonObject.getDouble("bsretencion");

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
                            qDocumentosCab.put("bsiva", bsiva);
                            qDocumentosCab.put("bsflete", bsflete);
                            qDocumentosCab.put("bsretencion", bsretencion);
                            qDocumentosCab.put("bsretencioniva", bsretencioniva);

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

                        //si todo se dió bien, preparo la fecha para actualizar la tabla de docs
                        Calendar fecha_modif = Calendar.getInstance();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String fechaActualizada = sdf.format(fecha_modif.getTime());

                        ContentValues qfechaDocs = new ContentValues();
                        qfechaDocs.put("fchhn_ultmod", fechaActualizada);

                        //y actualizo
                        ke_android.update("tabla_aux", qfechaDocs, "tabla = ?", new String[]{"ke_doccti"});
                        consultarDocs();
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

    @Override
    protected void onResume(){
        EvaluacionDeCargas();
        consultarDocs();
        super.onResume();
    }
}