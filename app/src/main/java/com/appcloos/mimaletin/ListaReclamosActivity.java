package com.appcloos.mimaletin;

import static android.widget.Toast.LENGTH_LONG;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ListaReclamosActivity extends AppCompatActivity implements ReclamosAdapter.ViewHolderDatos.onReclamoListener {

    ArrayList<Reclamo> listareclamo, listalineasrcl;
    AdminSQLiteOpenHelper conn;
    String codigoCliente, nombreCliente, documento;
    RecyclerView listaReclamos;
    private ReclamosAdapter reclamosAdapter;
    private SharedPreferences preferences;
    public static String cod_usuario;
    SQLiteDatabase ke_android;
    JSONArray arrayTi, arrayMV;
    lineasReclamosAdapter lineasReclamosAdapter;
    static String krti_ndoc, krti_status, krti_codcli, krti_docfac, krti_nombrecli, krti_docdev, krti_docnc, krti_fchdoc, fechamodifi,
            krmv_codart, krmv_nombre, krmv_pid, krti_agefac, krti_tipfac, krti_codvend, krti_codcoor, krti_notas, fecha_sinc, codigorcl,
            status, tipnc, docnc, docdev, tipdev, agenc, agedev, codclasif, pid, fechamodifiOP, fechamodifilin, nombreEmpresa = "", enlaceEmpresa = "";

    static Double krti_totneto, krmv_tipprec, krmv_cant, krmv_stot, krmv_artprec, krti_tipprec, totnetodef, cantdef, stotdef;
    private Boolean ll_commit = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_reclamos);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);//mantener la activity en vertical

        //establecemos los detalles de la conexion a la base de datos
        conn = new AdminSQLiteOpenHelper(getApplicationContext(), "ke_android", null, 10);
        SQLiteDatabase ke_android = conn.getWritableDatabase();

        cargarEnlace();
        //enlazo la parte logica a la grafica del recyclerview
        listaReclamos = findViewById(R.id.lv_reclamoslist);
        listaReclamos.setLayoutManager(new LinearLayoutManager(this));

        //PREFERENCIAS PARA TRAER POR EJEMPLO,EL CODIGO DEL USUARIO ACTUAL
        preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        cod_usuario = preferences.getString("cod_usuario", null);
        System.out.println(cod_usuario);
        //llamo al metodo para consultar si hay reclamos creados (seran mostrados en recycleview)
        consultarReclamos();

        //preparamos el adapter pasandole la lista que usará luego de la consulta y el contexto
        reclamosAdapter = new ReclamosAdapter(listareclamo, this);
        listaReclamos.setAdapter(reclamosAdapter);
        reclamosAdapter.notifyDataSetChanged(); //para refrescar el RecyclerView
        getfechaSinc();

    }

    private void getfechaSinc() {
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        Cursor fecha_ultmod = ke_android.rawQuery("SELECT fchhn_ultmod FROM tabla_aux WHERE tabla = 'ke_rclcti'", null);
        fecha_ultmod.moveToFirst();
        fecha_sinc = fecha_ultmod.getString(0);
        fecha_ultmod.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_listareclamos, menu);
        return super.onCreateOptionsMenu(menu);

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
        ke_android.close();
    }


    //metodo para consultar los reclamos creados (2021-12-10)-- revisar si es conveniente un switch
    private void consultarReclamos() {
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        Reclamo reclamo;
        listareclamo = new ArrayList<>();

        //preparamos los campos y la condicion para el query de los reclamos
        String[] campos = new String[]{
                "krti_ndoc, " +
                        "krti_status, " +
                        "krti_codcli, " +
                        "krti_docfac, " +
                        "krti_nombrecli, " +
                        "krti_docdev, " +
                        "krti_totneto, " +
                        "krti_totnetodef, " +
                        "krti_docnc, " +
                        "krti_fchdoc," +
                        "fechamodifi"};

        String condicion = "krti_status != '9'  AND krti_codvend='" + cod_usuario.trim() + "'";
        Cursor cursor = ke_android.query("ke_rclcti", campos, condicion, null, null, null, null);

        while (cursor.moveToNext()) {

            /*estas variables son para validar si vienen vacias
            o que se debe reflejar en el estatus...
             */
            String estatusEval = cursor.getString(1).trim();
            String nroDevEval = cursor.getString(5);
            String notaCEval = cursor.getString(8);

            String reclamo_status = "";
            String reclamo_dev;
            String reclamo_notac;

            //validaciones para ver si estan presentes los valores de los docs
            switch (estatusEval) {
                case "0":
                    reclamo_status = "Por Subir";
                    break;
                case "1":
                    reclamo_status = "Subido";
                    break;
                case "2":
                    reclamo_status = "En revisión";
                    break;
                case "3":
                    reclamo_status = "Rechazado";
                    break;
                case "4":
                    reclamo_status = "Esp. Mercancía";
                    break;
                case "5":
                    reclamo_status = "Procesado";
                    break;
            }

            if (nroDevEval == null) {
                reclamo_dev = "Pendiente";
            } else {
                reclamo_dev = nroDevEval;
            }

            if (notaCEval == null) {
                reclamo_notac = "Pendiente";
            } else {
                reclamo_notac = notaCEval;
            }

            //creamos un objeto reclamo y agregamos valores.
            reclamo = new Reclamo();
            reclamo.setNdoc(cursor.getString(0));
            reclamo.setStatus(reclamo_status);
            reclamo.setCodcli(cursor.getString(2));
            reclamo.setDocfac(cursor.getString(3));
            reclamo.setNombrecli(cursor.getString(4));
            reclamo.setDocdev(reclamo_dev);
            reclamo.setTotneto(cursor.getDouble(6));
            reclamo.setTotnetodef(cursor.getDouble(7));
            reclamo.setDocnc(reclamo_notac);
            reclamo.setFechadoc(cursor.getString(9));
            reclamo.setFechamodifi(cursor.getString(10));
            listareclamo.add(reclamo); //añado los campos a una lista
        }
        cursor.close();
        ke_android.close();

        //Cursor cursor = ke_android.rawQuery("SELECT * FROM ke_rclcti WHERE krti_codven ='" + cod_usuario.trim() + "'", null);
    }


    public boolean onOptionsItemSelected(MenuItem item) {

        int itemid = item.getItemId();

        switch (itemid) {
            case R.id.subir_reclamos:
                SubirReclamos();
                break;

            case R.id.actu_reclamos:
                ActualizarReclamos("https://" + enlaceEmpresa + "/webservice/obtenerdatosreclamos.php?cod_usuario=" + cod_usuario.trim() + "&&fecha_sinc=" + fecha_sinc.trim());
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void ActualizarReclamos(String URL) {

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(URL, response -> { //a traves de un json array request, traemos la informacion que viene del webservice

            if (response != null) { // si la respuesta no viene vacia

                conn = new AdminSQLiteOpenHelper(getApplicationContext(), "ke_android", null, 10);
                SQLiteDatabase ke_android = conn.getWritableDatabase();

                JSONObject jsonObjecthead; //creamos un objeto json vacio para la cabecera
                JSONObject jsonObjectlineas; //creamos un objeto json vacio para las lineas
                ll_commit = false;
                ke_android.beginTransaction();

                for (int i = 0; i < response.length(); i++) { /*pongo todo en el objeto segun lo que venga */
                    try {

                        jsonObjecthead = response.getJSONObject(i);
                        codigorcl = jsonObjecthead.getString("krti_ndoc");
                        codclasif = jsonObjecthead.getString("kdv_codclasif");
                        agenc = jsonObjecthead.getString("krti_agenc");
                        tipnc = jsonObjecthead.getString("krti_tipnc");
                        docnc = jsonObjecthead.getString("krti_docnc");
                        agedev = jsonObjecthead.getString("krti_agedev");
                        tipdev = jsonObjecthead.getString("krti_tipdev");
                        docdev = jsonObjecthead.getString("krti_docdev");
                        status = jsonObjecthead.getString("krti_status");
                        totnetodef = jsonObjecthead.getDouble("krti_totnetodef");
                        fechamodifiOP = jsonObjecthead.getString("fechacabecera");

                        for (int j = 0; j < response.length(); j++) {

                            jsonObjectlineas = response.getJSONObject(j);
                            pid = jsonObjectlineas.getString("krmv_pid");
                            cantdef = jsonObjectlineas.getDouble("krmv_cantdef");
                            stotdef = jsonObjectlineas.getDouble("krmv_stotdef");
                            fechamodifilin = jsonObjectlineas.getString("fechalineas");

                            ContentValues actualizarLineas = new ContentValues();
                            actualizarLineas.put("krmv_cantdef", cantdef);
                            actualizarLineas.put("krmv_stotdef", stotdef);
                            actualizarLineas.put("fechamodifi", fechamodifilin);

                            ke_android.update("ke_rcllmv", actualizarLineas, "krti_ndoc ='" + codigorcl + "' AND krmv_pid ='" + pid + "'", null);

                        }

                        ContentValues actualizarCabeceras = new ContentValues();
                        actualizarCabeceras.put("krti_agenc", agenc);
                        actualizarCabeceras.put("krti_tipnc", tipnc);
                        actualizarCabeceras.put("krti_docnc", docnc);
                        actualizarCabeceras.put("krti_agedev", agedev);
                        actualizarCabeceras.put("krti_tipdev", tipdev);
                        actualizarCabeceras.put("krti_docdev", docdev);
                        actualizarCabeceras.put("krti_status", status);
                        actualizarCabeceras.put("krti_totnetodef", totnetodef);
                        actualizarCabeceras.put("kdv_codclasif", codclasif);
                        actualizarCabeceras.put("fechamodifi", fechamodifiOP);

                        ke_android.update("ke_rclcti", actualizarCabeceras, "krti_ndoc ='" + codigorcl + "'", null);
                        ll_commit = true;

                        //actualizamos la fecha de la tabla de
                        Calendar fecha_reclamos = Calendar.getInstance();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                        String fechareclamos = sdf.format(fecha_reclamos.getTime());

                        ContentValues actualizarFecha = new ContentValues();
                        actualizarFecha.put("fchhn_ultmod", fechareclamos);

                        ke_android.update("tabla_aux", actualizarFecha, "tabla ='ke_rclcti'", null);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        ke_android.endTransaction();
                        ll_commit = false;

                        if (!ll_commit) {
                            return;
                        }
                    }
                }
                if (ll_commit) {
                    ke_android.setTransactionSuccessful();
                    ke_android.endTransaction();
                    Toast.makeText(ListaReclamosActivity.this, "Reclamos Actualizados", LENGTH_LONG).show();
                    listaReclamos.setAdapter(null);
                    listaReclamos.setAdapter(reclamosAdapter);
                    reclamosAdapter.notifyDataSetChanged();
                    finish();
                    overridePendingTransition(0, 0);
                    startActivity(getIntent());
                    overridePendingTransition(0, 0);//para refrescar el RecyclerView

                } else if (!ll_commit) {
                    ke_android.endTransaction();
                    Toast.makeText(ListaReclamosActivity.this, "Error en la actualización", LENGTH_LONG).show();
                }


            }
        }, error -> Toast.makeText(ListaReclamosActivity.this, "Sin Actualización", LENGTH_LONG).show()) {
            @Override
            protected Map<String, String> getParams() {  //finalmente, estos son los parametros que le enviaremos al webservice, partiendo de las variables
                //donde estan guardados las fechas
                // parametros.put("fecha_sinc", fecha_sinc);

                return new HashMap<>();
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonArrayRequest); //esto es el request que se envia al url a traves de la conexion volley, (el stringrequest esta armado arriba)


    }


    public void SubirReclamos() {
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        String[] campos = new String[]{
                "krti_ndoc, " +
                        "krti_status, " +
                        "krti_codcli, " +
                        "krti_docfac, " +
                        "krti_nombrecli, " +
                        "krti_totneto, " +
                        "krti_fchdoc," +
                        "fechamodifi," +
                        "krti_agefac," +
                        "krti_tipfac," +
                        "krti_codvend," +
                        "krti_codcoor," +
                        "krti_tipprec," +
                        "krti_notas"};

        String condicion = "krti_status = '0' AND krti_codvend = '" + cod_usuario.trim() + "'";
        Cursor cursor = ke_android.query("ke_rclcti", campos, condicion, null, null, null, null);

        if (cursor.getCount() > 0) {
            cargarReclamos();


        } else {
            Toast.makeText(ListaReclamosActivity.this, "No hay Reclamos por cargar.", Toast.LENGTH_SHORT).show();
        }
        cursor.close();

    }

    private void cargarReclamos() {
        int contadorReclamos = 0;
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        String[] campos = new String[]{
                "krti_ndoc, " +
                        "krti_status, " +
                        "krti_codcli, " +
                        "krti_docfac, " +
                        "krti_nombrecli, " +
                        "krti_totneto, " +
                        "krti_fchdoc," +
                        "fechamodifi," +
                        "krti_agefac," +
                        "krti_tipfac," +
                        "krti_codvend," +
                        "krti_codcoor," +
                        "krti_tipprec," +
                        "krti_notas"};

        String condicion = "krti_status = '0' AND krti_codvend = '" + cod_usuario.trim() + "'";
        Cursor cursorti = ke_android.query("ke_rclcti", campos, condicion, null, null, null, null);

        arrayTi = new JSONArray();
        arrayMV = new JSONArray();

        while (cursorti.moveToNext()) {
            JSONObject objetoCabecera = new JSONObject();

            try {

                krti_ndoc = cursorti.getString(0);
                krti_status = cursorti.getString(1);
                krti_codcli = cursorti.getString(2);
                krti_docfac = cursorti.getString(3);
                krti_nombrecli = cursorti.getString(4);
                krti_totneto = cursorti.getDouble(5);
                krti_fchdoc = cursorti.getString(6);
                fechamodifi = cursorti.getString(7);
                krti_agefac = cursorti.getString(8);
                krti_tipfac = cursorti.getString(9);
                krti_codvend = cursorti.getString(10);
                krti_codcoor = cursorti.getString(11);
                krti_tipprec = cursorti.getDouble(12);
                krti_notas = cursorti.getString(13);


                objetoCabecera.put("krti_ndoc", krti_ndoc);
                objetoCabecera.put("krti_status", krti_status);
                objetoCabecera.put("krti_codcli", krti_codcli);
                objetoCabecera.put("krti_docfac", krti_docfac);
                objetoCabecera.put("krti_nombrecli", krti_nombrecli);
                objetoCabecera.put("krti_totneto", krti_totneto);
                objetoCabecera.put("krti_fchdoc", krti_fchdoc);
                objetoCabecera.put("fechamodifi", fechamodifi);
                objetoCabecera.put("fechamodifi", fechamodifi);
                objetoCabecera.put("krti_agefac", krti_agefac);
                objetoCabecera.put("krti_tipfac", krti_tipfac);
                objetoCabecera.put("krti_codvend", krti_codvend);
                objetoCabecera.put("krti_codcoor", krti_codcoor);
                objetoCabecera.put("krti_tipprec", krti_tipprec);
                objetoCabecera.put("krti_notas", krti_notas);


                String[] camposLineas = new String[]{
                        "krti_ndoc," +
                                "krmv_tipprec," +
                                "krmv_codart," +
                                "krmv_nombre," +
                                "krmv_cant," +
                                "krmv_artprec," +
                                "krmv_stot," +
                                "krmv_pid," +
                                "fechamodifi"};

                String condicionLineas = "krti_ndoc = '" + krti_ndoc + "'";

                Cursor cursormv = ke_android.query("ke_rcllmv", camposLineas, condicionLineas, null, null, null, null);

                while (cursormv.moveToNext()) {
                    JSONObject objetoLineas = new JSONObject();

                    krmv_tipprec = cursormv.getDouble(1);
                    krmv_codart = cursormv.getString(2);
                    krmv_nombre = cursormv.getString(3);
                    krmv_cant = cursormv.getDouble(4);
                    krmv_artprec = cursormv.getDouble(5);
                    krmv_stot = cursormv.getDouble(6);
                    krmv_pid = cursormv.getString(7);
                    fechamodifi = cursormv.getString(8);


                    objetoLineas.put("krti_ndoc", krti_ndoc);
                    objetoLineas.put("krmv_codart", krmv_codart);
                    objetoLineas.put("krmv_nombre", krmv_nombre);
                    objetoLineas.put("krmv_cant", krmv_cant);
                    objetoLineas.put("krmv_artprec", krmv_artprec);
                    objetoLineas.put("krmv_stot", krmv_stot);
                    objetoLineas.put("krmv_pid", krmv_pid);
                    objetoLineas.put("fechamodifi", fechamodifi);
                    objetoLineas.put("krmv_tipprec", krmv_tipprec);

                    arrayMV.put(objetoLineas);
                }
                cursormv.close();

            } catch (Exception ex) {
                ex.printStackTrace();
                Toast.makeText(ListaReclamosActivity.this, "Error al cargar los Reclamos" + ex, Toast.LENGTH_SHORT).show();
                return;
            }
            arrayTi.put(objetoCabecera);
            contadorReclamos++;

        }
        cursorti.close();
        JSONObject jsonRCL = new JSONObject(); //vamos a hacer un solo objeto de tipo json
        try {

            jsonRCL.put("Cabecera", arrayTi);
            jsonRCL.put("Lineas", arrayMV);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        String jsonStrRCL = jsonRCL.toString();
        try {
            insertarReclamo(jsonStrRCL);


        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private void insertarReclamo(final String jsonStrRCL) {
        //genero un request queue y luego un strig request
        RequestQueue requestQueue = Volley.newRequestQueue(ListaReclamosActivity.this);
        //el string request llamara al webservice
        StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://" + enlaceEmpresa + "/webservice/Reclamos.php", response -> {
            if (response.trim().equals("OK")) { //si la respuesta obtenida es igual a ok, entonces cambio el estado del reclamo
                cambiarEstadoReclamo();
                Toast.makeText(ListaReclamosActivity.this, "Reclamo(s) Subido(s)", Toast.LENGTH_SHORT).show();
                listaReclamos.setAdapter(reclamosAdapter);
                reclamosAdapter.notifyDataSetChanged(); //para refrescar el RecyclerView

                finish();
                overridePendingTransition(0, 0);
                startActivity(getIntent());
                overridePendingTransition(0, 0);//para refrescar el RecyclerView*/
            }
        }, error -> {
            error.printStackTrace();
            Toast.makeText(ListaReclamosActivity.this, "Error en la subida", Toast.LENGTH_SHORT).show();
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("jsonrcl", jsonStrRCL);

                return params;
            }

        };
        requestQueue.add(stringRequest); //importante añadir el string request al request queue
    }


    //si los reclamos subieron bien, hago el cambio de estado de los que subieron
    private void cambiarEstadoReclamo() {
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        System.out.println(arrayTi);

        for (int i = 0; i < arrayTi.length(); i++) {
            try {
                JSONObject objetodeCabeza = arrayTi.getJSONObject(i);

                String codigoDelReclamoenArray = objetodeCabeza.getString("krti_ndoc");
                ke_android.execSQL("UPDATE ke_rclcti SET krti_status = '1' WHERE krti_ndoc ='" + codigoDelReclamoenArray + "'");
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }
    }

    @Override
    protected void onResume() {

        consultarReclamos();
        super.onResume();
    }


    @Override
    public void onItemClick(int position) {
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        ll_commit = false;
        final String docFactura = listareclamo.get(position).getDocfac();
        final String docStatus = listareclamo.get(position).getStatus();
        final String codReclamo = listareclamo.get(position).getNdoc();
        final String documentoNC = listareclamo.get(position).getDocnc();
        final String documentoDEV = listareclamo.get(position).getDocdev();
        final Double montodef = listareclamo.get(position).getTotnetodef();
        System.out.println(docStatus);
        AlertDialog.Builder ventana = new AlertDialog.Builder(new ContextThemeWrapper(ListaReclamosActivity.this,R.style.AlertDialogCustom));
        ventana.setTitle("Opciones");
        ventana.setMessage("Por favor, selecciona una opción");

        ventana.setPositiveButton("Ver más información", (dialogInterface, i) -> {

            AlertDialog.Builder dialogolineas = new AlertDialog.Builder(new ContextThemeWrapper(ListaReclamosActivity.this,R.style.AlertDialogCustom));
            dialogolineas.setTitle("Detalles del Reclamo");
            /*dialogolineas.setMessage("Nº NC: " + documentoNC +"\n"+
                                     "Nº DEV: " + documentoDEV + "\n" +
                                     "Monto def: " + montodef + "\n");*/

            ListView lineasrcl = new ListView(ListaReclamosActivity.this);

            cargarLineasRCL(codReclamo);
            lineasReclamosAdapter = new lineasReclamosAdapter(ListaReclamosActivity.this, listalineasrcl);
            lineasrcl.setAdapter(lineasReclamosAdapter);
            lineasReclamosAdapter.notifyDataSetChanged();
            dialogolineas.setView(lineasrcl);


            AlertDialog dialogodoc = dialogolineas.create();
            dialogodoc.show();
        });

        ventana.setNeutralButton("Borrar Reclamo", (dialogInterface, i) -> {

            if (docStatus.equals("Por Subir")) {
                //SI EL RECLAMO NO HA SIDO SUBIDO, LO BORRO Y DESBLOQUEO EL DOCUMENTO
                AlertDialog.Builder subventana = new AlertDialog.Builder(new ContextThemeWrapper(ListaReclamosActivity.this,R.style.AlertDialogCustom));
                subventana.setTitle("Mensaje de confirmación");
                subventana.setMessage("¿Estás seguro de borrar el reclamo?");

                subventana.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ke_android.beginTransaction();
                        try {
                            //ACA EL METODO DE BORRADO
                            BorrarReclamo(codReclamo);
                            //actualizamos de nuevo el registro del documento donde acepta o no devoluciones (solo si no se han subido reclamos pertenecientes a ese doc).
                            ContentValues actualizarAceptarDev = new ContentValues();
                            actualizarAceptarDev.put("aceptadev", "0");
                            ke_android.update("ke_doccti", actualizarAceptarDev, "documento='" + docFactura + "'", null);
                            ll_commit = true;
                            Toast.makeText(ListaReclamosActivity.this, "Reclamo borrado", LENGTH_LONG).show();
                            reclamosAdapter.notifyDataSetChanged(); //para refrescar el RecyclerView
                            finish();
                            overridePendingTransition(0, 0);
                            startActivity(getIntent());
                            overridePendingTransition(0, 0);//para refrescar el RecyclerView


                        } catch (Exception e) {
                            ke_android.endTransaction();
                            e.printStackTrace();
                            ll_commit = false;

                            if (!ll_commit) {
                                return;
                            }

                        }
                        if (ll_commit = true) {
                            ke_android.setTransactionSuccessful();
                            ke_android.endTransaction();
                        }


                    }
                });

                subventana.setNegativeButton("No", (dialogInterface1, i1) -> Toast.makeText(ListaReclamosActivity.this, "Eliminación cancelada", Toast.LENGTH_SHORT).show());

                AlertDialog dialogo2 = subventana.create();
                dialogo2.show();


            } else {
                Toast.makeText(ListaReclamosActivity.this, "El reclamo ya no puede ser borrado", LENGTH_LONG).show();
                /*AlertDialog.Builder subventana = new AlertDialog.Builder(ListaReclamosActivity.this);
                subventana.setTitle("Mensaje de confirmación");
                subventana.setMessage("¿Estás seguro de borrar el reclamo?, solo se borrara del dispositivo");

                subventana.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ke_android.beginTransaction();
                        try{
                            //ACA EL METODO DE BORRADO
                            BorrarReclamo(codReclamo);
                            ll_commit = true;
                            Toast.makeText(ListaReclamosActivity.this, "Reclamo borrado", LENGTH_LONG).show();
                            reclamosAdapter.notifyDataSetChanged(); //para refrescar el RecyclerView
                            finish();
                            overridePendingTransition(0, 0);
                            startActivity(getIntent());
                            overridePendingTransition(0, 0);//para refrescar el RecyclerView



                        }catch (Exception e){
                            ke_android.endTransaction();
                            e.printStackTrace();
                            ll_commit = false;

                            if(!ll_commit){
                                return;
                            }

                        }
                        if(ll_commit = true){
                            ke_android.setTransactionSuccessful();
                            ke_android.endTransaction();
                        }

                    }
                });

                subventana.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(ListaReclamosActivity.this, "Eliminación cancelada", Toast.LENGTH_SHORT).show();
                    }
                });

                AlertDialog dialogo2 = subventana.create();
                dialogo2.show();*/
            }


        });
        AlertDialog dialogo = ventana.create();
        dialogo.show();


    }

    private void cargarLineasRCL(String codReclamo) {
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        Reclamo reclamo;

        listalineasrcl = new ArrayList<>();
        String tabla = "ke_rcllmv";
        String[] columnas = new String[]{
                "krmv_codart, krmv_nombre, krmv_cant, krmv_stot, krmv_stotdef, krmv_cantdef"
        };
        String condicion = "krti_ndoc ='" + codReclamo + "'";
        Cursor cursor = ke_android.query(tabla, columnas, condicion, null, null, null, null);

        while (cursor.moveToNext()) {
            reclamo = new Reclamo();
            reclamo.setCodart(cursor.getString(0));
            reclamo.setNombre(cursor.getString(1));
            reclamo.setCant(cursor.getDouble(2));
            reclamo.setStot(cursor.getDouble(3));
            reclamo.setStotdef(cursor.getDouble(4));
            reclamo.setCantdef(cursor.getDouble(5));
            listalineasrcl.add(reclamo);


        }
        cursor.close();

    }

    private void BorrarReclamo(String codReclamo) {
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        ll_commit = false;
        ke_android.beginTransaction();

        try {
            //borro las lineas pero cambio la cabecera a 9 (anulado/borrado)
            ke_android.execSQL("UPDATE ke_rclcti SET krti_status = '9' WHERE krti_ndoc = '" + codReclamo + "'");
            ke_android.execSQL("DELETE FROM ke_rcllmv WHERE krti_ndoc = '" + codReclamo + "'");
            //si se efectuo correctamente, digo que el commit fue verdadero
            ll_commit = true;

        } catch (Exception e) {
            ke_android.endTransaction();
            e.printStackTrace();
            ll_commit = false; //de producirse un error, digo que el commit es falso

            if (!ll_commit) {
                ke_android.endTransaction();
                return; //si el commit es falso, cancelo la transacción y regreso (me salgo del proceso)
            }
        }

        if (ll_commit = true) { //si es verdadero, digo que la transaccion fue satisfactoria.
            ke_android.setTransactionSuccessful();
            ke_android.endTransaction();
            System.out.println("Reclamo borrado");
        }

    }
}