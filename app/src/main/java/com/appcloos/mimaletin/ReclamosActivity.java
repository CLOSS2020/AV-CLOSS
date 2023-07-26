package com.appcloos.mimaletin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class ReclamosActivity extends AppCompatActivity {

    //DECLARACION DE TODOS LOS ELEMENTOS USADOS EN EL ACTIVITY Y DE LAS VARIABLES.
    AdminSQLiteOpenHelper conn;
    String codigoCliente, nombreCliente, documento, codSeleccionado,  motivoSeleccionado, codigoTipo, nomWeb, nomRecl, helpRec, fechaMod;
    TextView tv_cliente, tv_documento, tv_montodev;
    private Boolean ll_commit;
    ListView listaLineas;
    BottomNavigationView menunav ;
    ArrayList<Lineas> listalineas, listalineasdoc;
    LineasTmpAdapter lineasTmpAdapter;
    LineasAdapter lineasAdapter;
    private static String agencia, tipodoc, tipodocv, grupo, subgrupo,  codhijo, pid, codigo, nombre, fechadoc, vendedor, codcoord, fechamodifi,
            CorrelativoTexto,  nroDev, tipoDoc;
    public static String cod_usuario, NotaReclamo = "";
    private static Double origen, cantidad, cntdevuelt, vndcntdevuelt,dvndmtototal,dpreciofin, dpreciounit, dmontoneto, dmontototal, timpueprc,
            unidevuelt, cantidad_a_devolver, montoDev = 0.00, tipoprecio ;
    private static int nroCorrelativo;
    int PICK_IMAGE_MULTIPLE = 1, posicionClasif;
    Uri imageUri = null;
    List<Uri> listaImagenes = new ArrayList<>();
    List<Uri> listaImagenesTabla = new ArrayList<>();
    GridView gridfotos;
    GridViewAdapter baseAdapter;
    List<String> listaBase64Imagenes = new ArrayList<>();
    String URL_UPLOAD_IMAGENES = "https://www.cloccidental.com/webservice/ImagenesReclamos.php";
    ArrayList<String> listaCodigos = new ArrayList<String>();
    ArrayList<String> listaMotivos = new ArrayList<String>();
    String[] codigosClasif = new String[]{};
    String[] motivosClasif = new String[]{};


    private JSONArray arrayTi, arrayMV;
    static String krti_ndoc, krti_status, krti_codcli, krti_docfac, krti_nombrecli, krti_docdev, krti_docnc, krti_fchdoc,
            krmv_codart, krmv_nombre, krmv_pid, krti_agefac, krti_tipfac, krti_codvend, krti_codcoor, krti_notas, fecha_sinc, codigorcl,
            status,tipnc,docnc, docdev, tipdev, agenc, agedev, codclasif, fechamodifiOP, fechamodifilin, enlaceEmpresa = "", nombreEmpresa="", codigoSucursal="";

    static Double krti_totneto, krmv_tipprec, krmv_cant, krmv_stot, krmv_artprec, krti_tipprec, totnetodef, cantdef,stotdef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reclamos);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//mantener la activity en vertical

        //de la actividad de documentos, traigo los datos necesarios
        Intent intent   = getIntent();
        codigoCliente   = intent.getStringExtra("codigoCliente");
        nombreCliente   = intent.getStringExtra("nombreCliente");
        documento       = intent.getStringExtra("documentoP");
        cod_usuario     = intent.getStringExtra("cod_usuario");
        tipoDoc         = "RCL";
        NotaReclamo     = "";
        montoDev        = 0.00;
        posicionClasif = 0;
        System.out.println(cod_usuario);

        //instancia del objeto de conexion a base de datos
        conn = new AdminSQLiteOpenHelper(getApplicationContext(), "ke_android", null, 10);
        SQLiteDatabase ke_android =  conn.getWritableDatabase();
        //valido el numero de la dev para obtener el nuevo correlativo
        Cursor cursor = ke_android.rawQuery("SELECT MAX(kdev_numero) FROM ke_correladev WHERE kdev_vendedor ='" + cod_usuario + "'", null);

        if(cursor.moveToFirst()){
            nroCorrelativo   = cursor.getInt(0);
            nroCorrelativo   = nroCorrelativo+1;
            CorrelativoTexto = String.valueOf(nroCorrelativo);
            CorrelativoTexto = "0000"+ nroCorrelativo;

            generarNumeroDevolucion(cod_usuario);
            getSupportActionBar().setTitle("Devo.:" + nroDev);
        }

        cargarEnlace();
        URL_UPLOAD_IMAGENES = "https://"+ enlaceEmpresa +"/webservice/ImagenesReclamos.php";

        listaLineas  = findViewById(R.id.lv_lineasR);
        tv_cliente   = findViewById(R.id.tv_clientedoc);
        tv_documento = findViewById(R.id.tv_documentodoc);
        tv_montodev  = findViewById(R.id.tv_montodev);

        tv_cliente.setText("Cliente: " + codigoCliente + " " + nombreCliente);
        tv_documento.setText("Nº DOC: " + documento);
        tv_montodev.setText("Monto Devolución: $" + montoDev);

        cargarLineasDocumento("https://"+enlaceEmpresa+"/webservice/lineasdocs.php?documento=" + documento.trim() + "&&agencia=" + codigoSucursal.trim());
        consultarLineas();

        lineasTmpAdapter = new LineasTmpAdapter(ReclamosActivity.this, listalineas);
        listaLineas.setAdapter(lineasTmpAdapter);
        lineasTmpAdapter.notifyDataSetChanged();

        menunav = findViewById(R.id.menunav_rec);
        menunav.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        gridfotos = findViewById(R.id.gridfotos);
        cargarCodigos();

        listaLineas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                //obtengo los valores que me interesan de la lista en la posicion elegida
                String pid         = listalineas.get(position).getPid();
                String codigo      = listalineas.get(position).getCodigo();
                Double cantidad    = listalineas.get(position).getCantidad();
                Double precioFin   = listalineas.get(position).getDpreciofin();

                final EditText cajacantidad = new EditText(ReclamosActivity.this);
                cajacantidad.setInputType(InputType.TYPE_CLASS_NUMBER);

                AlertDialog.Builder builder = new AlertDialog.Builder(ReclamosActivity.this);
                builder.setTitle("Mensaje del Sistema");
                builder.setMessage("Por favor, selecciona una opción: ");

                builder.setNegativeButton("Borrar articulo", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ke_android.beginTransaction();

                        try{
                            ke_android.execSQL("DELETE FROM ke_devlmtmp WHERE kdel_pid ='" + pid + "' AND kdel_codart = '" + codigo + "'");
                            ke_android.setTransactionSuccessful();
                            ke_android.endTransaction();

                            SumaNeto();
                            consultarLineas();

                        }catch(Exception ex){
                            ex.printStackTrace();
                            ke_android.endTransaction();
                        }
                    }
                });



                builder.setPositiveButton("Modificar Cantidad", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        AlertDialog.Builder dialogocantidad = new AlertDialog.Builder(ReclamosActivity.this); //aqui lo llamo igual
                        dialogocantidad.setTitle("Introduce una cantidad");
                        dialogocantidad.setView(cajacantidad);
                        dialogocantidad.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                int cantidadNueva = Integer.parseInt(cajacantidad.getText().toString());
                                System.out.println(cantidadNueva);

                                if(cantidadNueva <= 0){
                                    Toast.makeText(ReclamosActivity.this, "Debes introducir una cantidad mayor a 0", Toast.LENGTH_SHORT).show();

                                } else if (cantidadNueva > 0) {

                                    if(cantidadNueva > cantidad){
                                        Toast.makeText(ReclamosActivity.this, "Cantidad inválida.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Double cantidadN   = Double.valueOf(cantidadNueva);
                                        Double ln_mtoLinea = cantidadN * precioFin;
                                        try {
                                            ke_android.beginTransaction();
                                            ke_android.execSQL("UPDATE ke_devlmtmp SET kdel_cantdev = "+  cantidadN + ", kdel_mtolinea =" + ln_mtoLinea  + " WHERE kdel_pid ='"+ pid +"' and kdel_codart ='"+ codigo+"'");
                                            ke_android.setTransactionSuccessful();
                                            ke_android.endTransaction();
                                            SumaNeto();
                                            consultarLineas();
                                        }catch(Exception ex){
                                            ex.printStackTrace();
                                            ke_android.endTransaction();
                                        }
                                    }
                                }
                            }
                        });

                        AlertDialog dialogocant = dialogocantidad.create();
                        dialogocant.show();

                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
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

        while(cursor.moveToNext()){
            nombreEmpresa  = cursor.getString(0);
            enlaceEmpresa  = cursor.getString(1);
            codigoSucursal = cursor.getString(2);
        }
        ke_android.close();
    }

    private void generarNumeroDevolucion(String cod_usuario) {
        Date fechaHoy = new Date(Calendar.getInstance().getTimeInMillis());
        SimpleDateFormat formatofecha = new SimpleDateFormat("yyMM");
        String fecha = formatofecha.format(fechaHoy);

        CorrelativoTexto = right(CorrelativoTexto, 4);

        nroDev = cod_usuario.trim() + "-"+ tipoDoc +"-"+ fecha+CorrelativoTexto;
    }

    private String right(String valor, int longitud) {
        return valor.substring(valor.length() - longitud);
    }

    private final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

            switch(menuItem.getItemId()){

                case R.id.iclinearec:
                    AlertDialog.Builder dialogolineas = new AlertDialog.Builder(ReclamosActivity.this);
                    dialogolineas.setTitle("Elige el o los articulos a devolver");
                    ListView lineasdoc = new ListView(ReclamosActivity.this);

                    CargarLineasDoc();
                    lineasAdapter = new LineasAdapter(ReclamosActivity.this, listalineasdoc);
                    lineasdoc.setAdapter(lineasAdapter);
                    lineasAdapter.notifyDataSetChanged();
                    dialogolineas.setView(lineasdoc);

                    lineasdoc.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                            String pid         = listalineasdoc.get(position).getPid();
                            String nombre      = listalineasdoc.get(position).getNombre();
                            String codigo      = listalineasdoc.get(position).getCodigo();
                            Double cantidad    = listalineasdoc.get(position).getCantidad();
                            Double precioFin   = listalineasdoc.get(position).getDpreciofin();

                            final EditText cajatexto = new EditText(ReclamosActivity.this);
                            cajatexto.setInputType(InputType.TYPE_CLASS_NUMBER);

                            AlertDialog.Builder dialogocantidad = new AlertDialog.Builder(ReclamosActivity.this);

                            dialogocantidad.setTitle("Selecciona la cantidad a devolver");
                            dialogocantidad.setView(cajatexto);
                            dialogocantidad.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if(cajatexto.getText().toString().isEmpty()){
                                        Toast.makeText(ReclamosActivity.this, "Debes agregar una cantidad", Toast.LENGTH_SHORT).show();
                                    } else {
                                        cantidad_a_devolver = Double.valueOf(cajatexto.getText().toString());

                                        if(cantidad_a_devolver <= cantidad && cantidad_a_devolver > 0){

                                            Double ln_montodev = cantidad_a_devolver * precioFin;

                                            guardarLineaEnTemp(nroDev, documento, pid, codigo, cantidad, cantidad_a_devolver, nombre, ln_montodev, precioFin);
                                            CargarLineasDoc();
                                            lineasAdapter = new LineasAdapter(ReclamosActivity.this, listalineasdoc);
                                            SumaNeto();
                                            lineasdoc.setAdapter(lineasAdapter);
                                            lineasAdapter.notifyDataSetChanged();
                                            dialogolineas.setView(lineasdoc);


                                        } else if(cantidad_a_devolver > cantidad || cantidad_a_devolver <= 0){
                                            Toast.makeText(ReclamosActivity.this, "Cantidad invalida", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            });

                            AlertDialog ventanalineas = dialogocantidad.create();
                            ventanalineas.show();



                        }
                    });

                    /*dialogolineas.setPositiveButton("Devolución Completa", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    agregarDocCompleto();
                                    CargarLineasDoc();
                                    lineasAdapter = new LineasAdapter(ReclamosActivity.this, listalineasdoc);
                                    SumaNeto();
                                    lineasdoc.setAdapter(lineasAdapter);
                                    lineasAdapter.notifyDataSetChanged();
                                }
                            });*/


                    AlertDialog dialogodoc = dialogolineas.create();
                    dialogodoc.show();


                    return true;


                case R.id.icfotos:
                    abrirGaleria();
                    return true;

                case R.id.icnota:

                    final EditText textomotivo = new EditText(ReclamosActivity.this);
                    textomotivo.setText(NotaReclamo);

                    textomotivo.setInputType(InputType.TYPE_CLASS_TEXT);
                    textomotivo.setFilters(new InputFilter[] {new InputFilter.LengthFilter(250)});
                    textomotivo.setSingleLine(false);
                    textomotivo.setLines(6);
                    textomotivo.setMaxLines(8);
                    textomotivo.setHorizontalScrollBarEnabled(false);
                    textomotivo.setGravity(Gravity.LEFT | Gravity.TOP);

                    AlertDialog.Builder builderNota = new AlertDialog.Builder(ReclamosActivity.this);
                    builderNota.setTitle("Introduce una Nota complementaria");
                    builderNota.setMessage("Cuentas con un máximo de 250 caracteres para detallar el motivo del reclamo.");
                    builderNota.setView(textomotivo);
                    builderNota.setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            NotaReclamo = textomotivo.getText().toString();
                            Toast.makeText(ReclamosActivity.this, "Motivo guardado", Toast.LENGTH_SHORT).show();

                        }
                    });
                    AlertDialog dialogo = builderNota.create();
                    dialogo.show();


                    return true;


                case R.id.icprocesar:
                    procesarReclamo(montoDev);
                    return true;

                //cuando se presione la clasificacion, se cargan los datos y posteriormente se muestra un alertdialog
                case R.id.icclasif:

                    cargarCodigos();
                    listaCodigos  = new ArrayList<String>();
                    listaMotivos  = new ArrayList<String>();
                    codigosClasif = new String[]{};
                    motivosClasif = new String[]{};
                    SQLiteDatabase ke_android =  conn.getWritableDatabase();
                    Cursor cursorClasif = ke_android.rawQuery("SELECT kdv_codclasif, kdv_nomclaweb FROM ke_tiporecl WHERE 1", null);

                    listaCodigos.add("0");
                    listaMotivos.add("--Elija una Opción--");

                    while(cursorClasif.moveToNext()){
                        listaCodigos.add(cursorClasif.getString(0));
                        listaMotivos.add(cursorClasif.getString(1));


                    }

                    cursorClasif.close();

                    codigosClasif = listaCodigos.toArray(new String[listaCodigos.size()]);
                    motivosClasif = listaMotivos.toArray(new String[listaMotivos.size()]);

                    AlertDialog.Builder builder = new AlertDialog.Builder(ReclamosActivity.this);
                    builder.setTitle("Elige el motivo del reclamo");
                    builder.setSingleChoiceItems(motivosClasif, posicionClasif, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            codSeleccionado    = codigosClasif[which];
                            motivoSeleccionado = motivosClasif[which];
                            posicionClasif = which;
                            //Toast.makeText(ReclamosActivity.this, "Elegiste " + motivoSeleccionado + "con codigo " + codSeleccionado,  Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    });
                    builder.show();

                    return true;
            }

            return false;
        }
    };

    private void cargarCodigos() {
        cargarTiposReclamos("https://"+enlaceEmpresa+"/webservice/obtenertiposclasif_V2.php" + "?agencia=" + codigoSucursal.trim());
    }

    private void cargarTiposReclamos(String URL) {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(URL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

                if( response != null){
                    conn = new AdminSQLiteOpenHelper(getApplicationContext(), "ke_android", null, 10);
                    SQLiteDatabase ke_android = conn.getWritableDatabase();

                    JSONObject jsonObject = null; //creamos un objeto json vacio
                    ll_commit = false;

                    ke_android.beginTransaction();
                    ke_android.delete("ke_tiporecl","1", null);
                    for(int i = 0; i < response.length(); i++){
                        try{

                            //obtengo de la respuesta los datos en un json object
                            jsonObject   = response.getJSONObject(i);
                            //preparo los campos para las operaciones
                            codigoTipo   = jsonObject.getString("kdv_codclasif").trim();
                            nomWeb       = jsonObject.getString("kdv_nomclaweb").trim();
                            nomRecl      = jsonObject.getString("kdv_nomclasif").trim();
                            helpRec      = jsonObject.getString("kdv_hlpclasif").trim();
                            fechaMod     = jsonObject.getString("fechamodifi").trim();

                            ContentValues qtiposRec = new ContentValues();
                            qtiposRec.put("kdv_codclasif",    codigoTipo);
                            qtiposRec.put("kdv_nomclaweb",   nomWeb);
                            qtiposRec.put("kdv_nomclasif",   nomRecl);
                            qtiposRec.put("kdv_hlpclasif",   helpRec);
                            qtiposRec.put("fechamodifi",   fechaMod);

                            ke_android.insert("ke_tiporecl", null, qtiposRec);
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
                //parametros.put("documento", documento);

                return parametros;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonArrayRequest); //esto es el request que se envia al url a traves de la conexion volley, (el stringrequest esta armado arriba)
    }

    //metodo para abrir la galeria y traer las fotos
    private void abrirGaleria() {

        Intent intent = new Intent();
        intent.setType("image/jpeg");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "SELECCIONA LAS IMAGENES"), PICK_IMAGE_MULTIPLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            ClipData clipData = data.getClipData();

            if(resultCode == RESULT_OK && requestCode == PICK_IMAGE_MULTIPLE) {

                if(clipData == null) {
                    imageUri = data.getData();
                    listaImagenes.add(imageUri);
                    System.out.println(listaImagenes);
                } else {
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        listaImagenes.add(clipData.getItemAt(i).getUri());
                        System.out.println(listaImagenes);
                    }
                }
            }

        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(this, "No se seleccionó una imágen", Toast.LENGTH_LONG) .show();
        }
        //super.onActivityResult(requestCode, resultCode, data);
        baseAdapter = new GridViewAdapter(ReclamosActivity.this, listaImagenes);
        gridfotos.setAdapter(baseAdapter);

    }



    private void agregarDocCompleto() {
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        ke_android.beginTransaction();
        Cursor cursor = ke_android.rawQuery("SELECT documento, pid, codigo, cantidad, nombre, dmontoneto, dpreciofin FROM ke_doclmv WHERE documento='" + documento + "'", null);
        try {
            ke_android.execSQL("DELETE FROM ke_devlmtmp");
            while(cursor.moveToNext()){
                String lc_pid       = cursor.getString(1);
                String lc_codigo    = cursor.getString(2);
                Double ln_cantidad  = cursor.getDouble(3);
                String nombre       = cursor.getString(4);
                Double mtonetolinea = cursor.getDouble(5);
                Double preciofinal  = cursor.getDouble(6);
                ContentValues guardarDoc = new ContentValues();
                guardarDoc.put("kdel_referencia", nroDev);
                guardarDoc.put("kdel_documento", documento);
                guardarDoc.put("kdel_pid", lc_pid);
                guardarDoc.put("kdel_codart", lc_codigo);
                guardarDoc.put("kdel_cantdev", ln_cantidad);
                guardarDoc.put("kdel_cantped", ln_cantidad);
                guardarDoc.put("kdel_nombre", nombre);
                guardarDoc.put("kdel_mtolinea", mtonetolinea);
                guardarDoc.put("kdel_preciofin", preciofinal);

                ke_android.insert("ke_devlmtmp", null, guardarDoc);
                Toast.makeText(ReclamosActivity.this, "¡Articulo(s) agregado(s)!", Toast.LENGTH_SHORT).show();
            }
            ke_android.setTransactionSuccessful();
            ke_android.endTransaction();
            SumaNeto();
        }catch (Exception ex){
            Toast.makeText(ReclamosActivity.this, "Error al guardar la tabla", Toast.LENGTH_SHORT).show();
            ke_android.endTransaction();
        }


    }

    private void SumaNeto() {
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        Cursor cursor = ke_android.rawQuery("SELECT SUM(kdel_mtolinea) FROM ke_devlmtmp", null);

        if(cursor.moveToNext()) {
            montoDev = cursor.getDouble(0);
            montoDev = Math.round(montoDev*100.00)/100.00;

            tv_montodev.setText("$" + montoDev.toString());
        } else{
            tv_montodev.setText("$0.00");
        }

    }

    private void procesarReclamo(Double totneto) {
        Boolean ll_commit = false;
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        Cursor cursor = ke_android.rawQuery("SELECT kdel_preciofin, kdel_mtolinea, kdel_pid, kdel_codart, kdel_cantdev, kdel_cantped, kdel_nombre FROM ke_devlmtmp WHERE 1", null);

        if(cursor.getCount() > 0){
            System.out.println("Codigo seleccionado: " + codSeleccionado);
            if((codSeleccionado == null) || (codSeleccionado.trim().equals("0")) || (codSeleccionado.trim().equals(""))){
                Toast.makeText(ReclamosActivity.this, "Debes elegir el motivo del reclamo", Toast.LENGTH_SHORT).show();
            }else{
                if(listaImagenes.size() == 0){
                    Toast.makeText(ReclamosActivity.this, "Debes añadir imágenes al reclamo", Toast.LENGTH_SHORT).show();
                }else {
                    ke_android.beginTransaction();
                    try {

                        Cursor cursorti = ke_android.rawQuery("SELECT agencia, tipodoc, codcliente, tipoprecio, vendedor, codcoord, nombrecli FROM ke_doccti WHERE documento = '" + documento + "'", null);
                        while (cursorti.moveToNext()) {
                            agencia = cursorti.getString(0);
                            tipodoc = cursorti.getString(1);
                            codigoCliente = cursorti.getString(2);
                            tipoprecio = cursorti.getDouble(3);
                            vendedor = cursorti.getString(4);
                            codcoord = cursorti.getString(5);
                            nombreCliente = cursorti.getString(6);
                        }

                        //generamos la fecha para la creacion y la primera actualización de fechamodifi
                        Date fechaTabla = new Date(Calendar.getInstance().getTimeInMillis());
                        SimpleDateFormat formatoFechaTabla = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                        String fechaGuardar = formatoFechaTabla.format(fechaTabla);
                        String fechaCreacion = fechaGuardar;

                        ContentValues cabeceraReclamos = new ContentValues();
                        cabeceraReclamos.put("krti_ndoc", nroDev);
                        cabeceraReclamos.put("krti_docfac", documento);
                        cabeceraReclamos.put("krti_codcli", codigoCliente);
                        cabeceraReclamos.put("krti_nombrecli", nombreCliente);
                        cabeceraReclamos.put("krti_status", "0");
                        cabeceraReclamos.put("krti_tipfac", tipodocv);
                        cabeceraReclamos.put("krti_totneto", totneto);
                        cabeceraReclamos.put("krti_agefac", agencia.trim());
                        cabeceraReclamos.put("krti_tipfac", tipodoc);
                        cabeceraReclamos.put("krti_tipprec", tipoprecio);
                        cabeceraReclamos.put("krti_notas", NotaReclamo.trim());
                        cabeceraReclamos.put("krti_codvend", vendedor.trim());
                        cabeceraReclamos.put("krti_codcoor", codcoord.trim());
                        cabeceraReclamos.put("krti_fchdoc", fechaCreacion);
                        cabeceraReclamos.put("fechamodifi", fechaCreacion);
                        cabeceraReclamos.put("kdv_codclasif", codSeleccionado);

                        while (cursor.moveToNext()) {
                            ContentValues lineasReclamos = new ContentValues();

                            lineasReclamos.put("krti_ndoc", nroDev);
                            lineasReclamos.put("krmv_tipprec", tipoprecio);
                            lineasReclamos.put("krmv_pid", cursor.getString(2).trim());
                            lineasReclamos.put("krmv_codart", cursor.getString(3).trim());
                            lineasReclamos.put("krmv_cant", cursor.getDouble(4));
                            lineasReclamos.put("krmv_nombre", cursor.getString(6).trim());
                            lineasReclamos.put("krmv_artprec", cursor.getDouble(0));
                            lineasReclamos.put("krmv_stot", cursor.getDouble(1));
                            lineasReclamos.put("fechamodifi", fechaCreacion);

                            //insertamos las lineas del reclamo
                            ke_android.insert("ke_rcllmv", null, lineasReclamos);
                        }
                        //insertamos la cabercera del reclamo
                        ke_android.insert("ke_rclcti", null, cabeceraReclamos);

                        //aumentamos el correlativo en la tabla de correlativos de reclamos
                        ContentValues aumentarCorrelatiodev = new ContentValues();
                        aumentarCorrelatiodev.put("kdev_numero", nroCorrelativo);
                        aumentarCorrelatiodev.put("kdev_vendedor", cod_usuario);

                        //insertamos el correlativo
                        ke_android.insert("ke_correladev", null, aumentarCorrelatiodev);

                        //limpiamos la tabla de temporal
                        ke_android.delete("ke_devlmtmp", "1", null);

                        //insertamos las imagenes en la tabla de imagenes
                        ContentValues imagenesreclamo = new ContentValues();
                        for (int i = 0; i < listaImagenes.size(); i++) {
                            imagenesreclamo.put("krti_ndoc", nroDev);
                            imagenesreclamo.put("kircl_rutafoto", listaImagenes.get(i).toString());
                            ke_android.insert("ke_imgrcl", null, imagenesreclamo);
                        }


                        // y actualizamos la fecha y el campo de aceptadev en la tabla de los documentos:
                        ContentValues bloquearReclamo = new ContentValues();

                        Calendar fecha_modif = Calendar.getInstance();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String fecha_modificado = sdf.format(fecha_modif.getTime());


                        bloquearReclamo.put("aceptadev", "0");
                        bloquearReclamo.put("fechamodifi", fecha_modificado);
                        ke_android.update("ke_doccti", bloquearReclamo, "documento='" + documento + "'", null);


                        ll_commit = true; /*si se dió bien, deberia andar correctamente*/

                    } catch (Exception ex) {
                        System.out.println(ex);
                        ll_commit = false; //al haber exception, tira a falso


                        if (ll_commit == false) {
                            ke_android.endTransaction();
                            Toast.makeText(ReclamosActivity.this, ex.toString(), Toast.LENGTH_LONG).show();
                        }
                    }

                    if (ll_commit == true) { //si las acciones se ejecutaron bien, hago commit
                        ke_android.setTransactionSuccessful();
                        ke_android.endTransaction();
                        Toast.makeText(ReclamosActivity.this, "Reclamo creado exitosamente", Toast.LENGTH_LONG).show();
                        SubirReclamo();
                        SubirImagenes(nroDev);
                        ReclamosActivity.this.finish();


                    } else if (ll_commit == false) { //pero si no, hago rollback
                        return;

                    }
                }
            }



        } else {
            Toast.makeText(ReclamosActivity.this, "Por favor, agrega artículos al reclamo", Toast.LENGTH_SHORT).show();
        }



    }

    public void SubirReclamo(){
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
                        "krti_notas," +
                        "kdv_codclasif"};

        String condicion = "krti_status = '0' AND krti_ndoc = '" + nroDev +"'";
        Cursor cursor = ke_android.query("ke_rclcti", campos, condicion, null,null,null, null);

        if(cursor.getCount() > 0){
            cargarReclamos();


        } else {
            Toast.makeText(ReclamosActivity.this, "No hay Reclamos por cargar.", Toast.LENGTH_SHORT).show();
        }

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
                        "krti_notas," +
                        "kdv_codclasif"};

        String condicion = "krti_status = '0' AND krti_ndoc = '" + nroDev +"'";
        Cursor cursorti = ke_android.query("ke_rclcti", campos, condicion, null,null,null, null);

        arrayTi = new JSONArray();
        arrayMV = new JSONArray();

        while(cursorti.moveToNext()){
            JSONObject objetoCabecera = new JSONObject();

            try {

                krti_ndoc      = cursorti.getString(0);
                krti_status    = cursorti.getString(1);
                krti_codcli    = cursorti.getString(2);
                krti_docfac    = cursorti.getString(3);
                krti_nombrecli = cursorti.getString(4);
                krti_totneto   = cursorti.getDouble(5);
                krti_fchdoc    = cursorti.getString(6);
                fechamodifi    = cursorti.getString(7);
                krti_agefac    = cursorti.getString(8);
                krti_tipfac    = cursorti.getString(9);
                krti_codvend   = cursorti.getString(10);
                krti_codcoor   = cursorti.getString(11);
                krti_tipprec   = cursorti.getDouble(12);
                krti_notas     = cursorti.getString(13);


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
                objetoCabecera.put("kdv_codclasif", codSeleccionado);


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

                String condicionLineas = "krti_ndoc = '" + krti_ndoc +"'";

                Cursor cursormv = ke_android.query("ke_rcllmv", camposLineas, condicionLineas, null, null, null, null);

                while(cursormv.moveToNext()){
                    JSONObject objetoLineas = new JSONObject();

                    krmv_tipprec = cursormv.getDouble(1);
                    krmv_codart  = cursormv.getString(2);
                    krmv_nombre  = cursormv.getString(3);
                    krmv_cant    = cursormv.getDouble(4);
                    krmv_artprec = cursormv.getDouble(5);
                    krmv_stot    = cursormv.getDouble(6);
                    krmv_pid     = cursormv.getString(7);
                    fechamodifi  = cursormv.getString(8);


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

            }catch (Exception ex){
                ex.printStackTrace();
                Toast.makeText(ReclamosActivity.this, "Error al cargar los Reclamos" + ex, Toast.LENGTH_SHORT).show();
                return;
            }
            arrayTi.put(objetoCabecera);
            contadorReclamos++;

        }

        JSONObject jsonRCL = new JSONObject(); //vamos a hacer un solo objeto de tipo json
        try {

            jsonRCL.put("Cabecera", arrayTi);
            jsonRCL.put("Lineas", arrayMV);

        }catch (Exception ex){
            ex.printStackTrace();
        }
        String jsonStrRCL = jsonRCL.toString();
        try {
            insertarReclamo(jsonStrRCL);


        }catch (Exception ex){
            ex.printStackTrace();
            return;
        }

    }

    private void insertarReclamo(String jsonStrRCL) {
        //genero un request queue y luego un strig request
        RequestQueue requestQueue = Volley.newRequestQueue(ReclamosActivity.this);
        //el string request llamara al webservice
        StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://"+enlaceEmpresa+"/webservice/Reclamos.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response.trim().equals("OK")) { //si la respuesta obtenida es igual a ok, entonces cambio el estado del reclamo
                    cambiarEstadoReclamo();
                    Toast.makeText(ReclamosActivity.this, "Reclamo(s) Subido(s)", Toast.LENGTH_SHORT).show();

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(ReclamosActivity.this, "Error en la subida", Toast.LENGTH_SHORT).show();
            }
        }) {
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("jsonrcl", jsonStrRCL);
                params.put("agencia", codigoSucursal);
                return params;
            }

        };
        requestQueue.add(stringRequest); //importante añadir el string request al request queue
    }

    private void cambiarEstadoReclamo() {
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        System.out.println(arrayTi);

        for(int i = 0; i < arrayTi.length(); i++){
            try {
                JSONObject objetodeCabeza = arrayTi.getJSONObject(i);

                String codigoDelReclamoenArray = objetodeCabeza.getString("krti_ndoc");
                ke_android.execSQL("UPDATE ke_rclcti SET krti_status = '1' WHERE krti_ndoc ='" + codigoDelReclamoenArray +  "'");
            }catch (Exception ex){
                ex.printStackTrace();
            }

        }
    }

    private void guardarLineaEnTemp(String nroDev, String documento, String pid, String codigo, Double cantidad, Double cantidad_a_devolver, String nombre, Double montoDevlinea, Double precioFin) {

        SQLiteDatabase ke_android = conn.getWritableDatabase();
        ke_android.beginTransaction();
        try {

            ContentValues guardarlinea = new ContentValues();
            guardarlinea.put("kdel_referencia", nroDev);
            guardarlinea.put("kdel_documento", documento);
            guardarlinea.put("kdel_pid", pid);
            guardarlinea.put("kdel_codart", codigo);
            guardarlinea.put("kdel_mtolinea", montoDevlinea);
            guardarlinea.put("kdel_cantdev", cantidad_a_devolver);
            guardarlinea.put("kdel_cantped", cantidad);
            guardarlinea.put("kdel_nombre", nombre);
            guardarlinea.put("kdel_preciofin", precioFin);
            ke_android.insert("ke_devlmtmp", null, guardarlinea);
            ke_android.setTransactionSuccessful();
            ke_android.endTransaction();
            Toast.makeText(ReclamosActivity.this, "¡artículo agregado!", Toast.LENGTH_SHORT).show();

        }catch (Exception e){
            e.printStackTrace();
            ke_android.endTransaction();
        }




    }

    private void CargarLineasDoc() {
        cargarLineasDocumento("https://"+enlaceEmpresa+"/webservice/lineasdocs.php?documento=" + documento.trim() + "&&agencia=" + codigoSucursal.trim());
        consultarLineasDoc();


    }

    private void consultarLineasDoc() {

        SQLiteDatabase ke_android = conn.getWritableDatabase();
        Lineas lineas = null;

        listalineasdoc = new ArrayList<Lineas>();
        Cursor cursor = ke_android.rawQuery("SELECT pid, codigo, nombre, cantidad, dmontoneto, dpreciofin  FROM ke_doclmv WHERE documento ='" + documento + "' AND pid NOT IN " +
                "(SELECT kdel_pid FROM ke_devlmtmp)", null);

        // Cursor cursor = ke_android.rawQuery("SELECT pid, codigo, nombre, cantidad, dmontoneto, dpreciofin  FROM ke_doclmv WHERE documento ='" + documento + "'", null);

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

    }

    //este metodo se acciona al pulsar agregar y llama a un webservice
    private void cargarLineasDocumento(String URL) {

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(URL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

                if( response != null){
                    conn = new AdminSQLiteOpenHelper(getApplicationContext(), "ke_android", null, 10);
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
                            qcodigoLocal.moveToPosition(0);
                            System.out.println("codigos: " + codigo.toString());

                            int codigoExiste = qcodigoLocal.getInt(0);
                            System.out.println("cantidad del codigo " + codigo + ": " + codigoExiste);

                            if(codigoExiste > 0){
                                ke_android.update("ke_doclmv", qDocumentosLin, "pid = ?", new String[]{pid});
                                System.out.println("ACTUALIZO EL: " + codigo.toString());
                            } else if (codigoExiste == 0){
                                ke_android.insert("ke_doclmv",null,qDocumentosLin);
                                System.out.println("INSERTO EL: " + codigo.toString());
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
                        consultarLineas();
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

    private void consultarLineas() {

        SQLiteDatabase ke_android = conn.getWritableDatabase();
        Lineas lineas = null;

        listalineas = new ArrayList<Lineas>();
        Cursor cursor = ke_android.rawQuery("SELECT kdel_pid, kdel_codart, kdel_nombre, kdel_cantped, kdel_mtolinea, kdel_cantdev, kdel_preciofin FROM ke_devlmtmp WHERE kdel_documento='" + documento + "'", null);

        while(cursor.moveToNext()){
            lineas = new Lineas();
            lineas.setPid(cursor.getString(0));
            lineas.setCodigo(cursor.getString(1));
            lineas.setNombre(cursor.getString(2));
            lineas.setCantidad(cursor.getDouble(3));
            lineas.setDmontototal(cursor.getDouble(4));
            lineas.setCntdevuelt(cursor.getDouble(5));
            lineas.setDpreciofin(cursor.getDouble(6));
            listalineas.add(lineas);

        }

        lineasTmpAdapter = new LineasTmpAdapter(ReclamosActivity.this, listalineas);
        listaLineas.setAdapter(lineasTmpAdapter);
        lineasTmpAdapter.notifyDataSetChanged();

    }

    private void SubirImagenes(String nrodev) {
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        String codigo_rcl_img = "", rutafoto;

        listaBase64Imagenes.clear();
        //preparo los parametros del query
        String tabla       = "ke_imgrcl";
        String[] columnas  = new String[]{
                "krti_ndoc, " +
                        "kircl_rutafoto"};
        String condicion = "krti_ndoc='" + nrodev + "'";

        //genero un cursor en base al query
        Cursor cursor = ke_android.query(tabla, columnas, condicion, null, null, null, null);
        while(cursor.moveToNext()){
            codigo_rcl_img = cursor.getString(0);
            rutafoto       = cursor.getString(1);
            //listaImagenesTabla.add(Uri.parse(rutafoto));
        }

        cursor.close();

        for(int i = 0 ; i < listaImagenes.size() ; i++) {
            try {
                InputStream is = ReclamosActivity.this.getContentResolver().openInputStream(listaImagenes.get(i));
                Bitmap bitmap  = BitmapFactory.decodeStream(is);
                bitmap         = redimensionarImagen(bitmap, 500, 500);
                String cadena  = convertirUriToBase64(bitmap);

                enviarImagenes(codigo_rcl_img + "_"+ i , cadena, nroDev);
                bitmap.recycle();

            } catch (IOException e) {
                Toast.makeText(ReclamosActivity.this, "Imagen muy pequeña", Toast.LENGTH_SHORT).show();
            }

        }

    }
    //mtodo para redimensionar/reescalar la imagen
    /*
     * ESTE METODO SE ESTA REVISANDO PUESTO QUE ES PROBABLE QUE ESTE GENERANDO PROBLEMAS
     * PARA LA CARGA DE LOS RECLAMOS.*/
    private Bitmap redimensionarImagen(Bitmap bitmap, float anchoNuevo, float altoNuevo) {
        int ancho = bitmap.getWidth();
        int alto  = bitmap.getHeight();

        try {

            if(ancho > alto){
                if(ancho > anchoNuevo || alto > altoNuevo){
                    float escalaAncho = anchoNuevo/ancho;
                    float escalaAlto  = altoNuevo/alto;

                    Matrix matrix = new Matrix();
                    matrix.postScale(escalaAncho, escalaAlto);

                    return Bitmap.createBitmap(bitmap, 0,0, ancho, alto, matrix, false);
                } else{
                    return bitmap;
                }


            } else if(alto > ancho){
                if(ancho > anchoNuevo || alto > altoNuevo){
                    float escalaAncho = anchoNuevo/ancho;
                    float escalaAlto  = altoNuevo/alto;

                    Matrix matrix = new Matrix();
                    matrix.postScale(escalaAncho, escalaAlto);

                    return Bitmap.createBitmap(bitmap, 0,0, ancho, alto, matrix, false);
                } else{
                    return bitmap;
                }
                //si los anchos y altos son iguales
            }else if (alto == ancho) {
                if (ancho > anchoNuevo || alto > altoNuevo) {
                    float escalaAncho = anchoNuevo / ancho;
                    float escalaAlto = altoNuevo / alto;

                    Matrix matrix = new Matrix();
                    matrix.postScale(escalaAncho, escalaAlto);

                    return Bitmap.createBitmap(bitmap, 0, 0, ancho, alto, matrix, false);
                } else {
                    return bitmap;
                }
            }


        }catch (Exception e){
            e.printStackTrace();
        }
        return bitmap;
    }

    private void enviarImagenes(final String nombre, final String cadena, final String docReclamo) {

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_UPLOAD_IMAGENES,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if(response.equals("Subido")) {
                            System.out.println(response);

                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            protected Map<String, String> getParams() {

                Map<String, String> params = new Hashtable<String, String>();
                params.put("nombre", nombre);
                params.put("imagen", cadena);
                params.put("reclamo", docReclamo);

                return params;
            }
        };

        requestQueue.add(stringRequest);
    }

    private String convertirUriToBase64(Bitmap bitmap) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] bytes = baos.toByteArray();
        String encode = Base64.encodeToString(bytes, Base64.DEFAULT);

        return encode;
    }






    @Override
    protected void onResume(){
        consultarLineas();
        //NotaReclamo = "";
        super.onResume();
    }


}