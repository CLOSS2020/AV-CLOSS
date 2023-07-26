
/* ******************************************************************************************************
 * 4 DE SEPTIEMBRE, 2020                                                                              *
 * AUTOR: PCV                                                                                         *
 * APP: MI MALETIN V 1.0.0                                                                            *
 * ESTA ES LA ACTIVITY DEL LOGIN DE LA APP PROTOTIPO DE PEDIDOS                                       *
 * EL NOMBRE Y EL ICONO SON TENTATIVOS.                                                               *
 * -------------------------------------------------------------------------------------------------- *
 * 25 DE SEPTIEMBRE, 2020                                                                             *
 * AUTOR: PCV                                                                                         *
 * ACTUALIZACION: DOCUMENTANCION DE LAS CLASES                                                        *
 * ---------------------------------------------------------------------------------------------------*
 * 05 DE OCTUBRE, 2020                                                                                *
 * AUTOR: PCV                                                                                         *
 * ACTUALIZACION: SE CAMBIO EL METODO DE VALIDACION DE USUARIO DE UN STRING REQUEST A UN JSON ARRAY   *
 * REQUEST, CON EL OBJETIVO DE PODER ENVIAR INFORMACION AL SIGUIENTE ACTIVITY (AL HOME)               *
 *                                                                                                    *
 ******************************************************************************************************

 * */


package com.appcloos.mimaletin;

import static android.widget.Toast.LENGTH_LONG;

import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.Task;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.appupdate.AppUpdateOptions;
import com.google.android.play.core.install.model.ActivityResult;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements Serializable {

    EditText et_usuario, et_password, et_empresa; //objetos de texto
    Button bt_iniciar, bt_validar; //objeto boton
    public static String nick_usuario, nick_usuario_env, codigo_empresa,
            pass_usuario, ultimorec, n_usuario, cod_usuario, nombre_usuario, ultimoped, sesionactiva, superves, almacen, fechamodifi, ultimorcl, ultimorcxc,
            vendedor, version, caducidad, versionNube, enlace = "", nombreEmp, codigoEmp, statusEmp, codigoModulo, activoModulo, codigoSuc;
    public static Double desactivo = 0.0, ualterprec;
    private SharedPreferences preferences;

    ObjetoAux objetoAux;
    AdminSQLiteOpenHelper conn;
    SQLiteDatabase ke_android;
    public static String versionApp = "2.3.3";
    TextView tv_version;

    private static final int MY_REQUEST_CODE = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //mantener la orientacion vertical
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide(); //metodo para esconder la actionbar

        et_empresa = (EditText) findViewById(R.id.txt_empresa); //codigo validador de la empresa
        et_usuario = (EditText) findViewById(R.id.txt_usuario); // este objeto es referente al id del txt usuario
        et_password = (EditText) findViewById(R.id.txt_password); // y este del txt password
        bt_iniciar = (Button) findViewById(R.id.bt_iniciar); //y una variable para el boton.
        bt_validar = (Button) findViewById(R.id.bt_validarempresa); //validar la empresa segun el codigo
        tv_version = (TextView) findViewById(R.id.tvversion);
        et_empresa = (EditText) findViewById(R.id.txt_empresa); //codigo validador de la empresa
        tv_version.setText("Ver. " + versionApp);
        enlace = "";
        preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        conn = new AdminSQLiteOpenHelper(MainActivity.this, "ke_android", null, 12);
        objetoAux = new ObjetoAux(this);
        ValidarSesion();
        enlace = "";
        bt_validar.setVisibility(View.INVISIBLE);
        codigo_empresa = "081196";
        validarEmpresaLicencia("https://www.cloccidental.com/webservice/validarempresa.php?codigo=" + codigo_empresa);
        et_usuario.setEnabled(true);
        et_password.setEnabled(true);

        checkForAppUpdate();
        //obtenerVersion("https://cloccidental.com/webservice/versionapp.php?version_usuario=" + versionApp);

        /*bt_validar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(codigo_empresa.isEmpty()){
                    Toast.makeText(MainActivity.this, "Se requiere codigo de validacion.", Toast.LENGTH_LONG).show(); // si esta vacio,
                } else {

                }

                if(enlace.equals("")){
                    //si no consigue la empresa, desactiva el inicio de sesión
                    bt_iniciar.setEnabled(false);
                    et_password.setEnabled(false);
                    et_usuario.setEnabled(false);

                    et_usuario.setVisibility(View.INVISIBLE);
                    et_password.setVisibility(View.INVISIBLE);
                    bt_iniciar.setVisibility(View.INVISIBLE);
                }


            }
        });*/

        bt_iniciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {  //le indicamos al boton que usara un metodo on click listener

                et_usuario.setEnabled(true);
                et_password.setEnabled(true);
                bt_iniciar.setText("Iniciar Sesion");

                nick_usuario = et_usuario.getText().toString(); // guardamos en la variable lo que viDebes indicar la empresa ene del objeto
                pass_usuario = et_password.getText().toString();// guardamos en la variable lo que viene del objeto

                if (!nick_usuario.isEmpty() && !pass_usuario.isEmpty()) { //si el nombre de usuarioy  el password no estan en blanco que ejecute
                    //el metodo a continuacion

                    bt_iniciar.setEnabled(false);
                    bt_iniciar.setText("Iniciando Sesión");

                    ValidarUsuario("https://" + enlace + "/webservice/validar_usuario_actualizadoV_5.php?nick_usuario=" + nick_usuario + "&pass_usuario=" + pass_usuario + "&agencia=" + codigoSuc); //llamamos al metodo para validar y hacer login de usuario.
                    //System.out.println("https://"+ enlace + "/webservice/validar_usuario_actualizadoV_5.php?nick_usuario="+nick_usuario + "&pass_usuario=" + pass_usuario +"&agencia=" + codigoSuc);

                } else {
                    Toast.makeText(MainActivity.this, "No se permiten campos en blanco.", Toast.LENGTH_LONG).show(); // si esta vacio,
                    //le decimos al usuario que no se permiten campos en blanco.
                }


            }
        });



    }

    private void validarEmpresaLicencia(String URL) {
        SQLiteDatabase ke_android = conn.getWritableDatabase();

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(URL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

                if (response != null) {
                    JSONObject jsonObject = null; //creamos un objeto json vacio

                    ke_android.beginTransaction();
                    ke_android.execSQL("DELETE FROM ke_enlace");
                    ke_android.execSQL("DELETE FROM ke_modulos");
                    try {
                        //bajamos los datos de la empresa/sucursal
                        jsonObject = response.getJSONObject(0);
                        codigoEmp = jsonObject.getString("codigoEmpresa");
                        enlace = jsonObject.getString("enlaceEmpresa");
                        nombreEmp = jsonObject.getString("nombreEmpresa");
                        statusEmp = jsonObject.getString("statusEmpresa");
                        codigoSuc = jsonObject.getString("agenciaEmpresa");

                        ContentValues guardarEnlaces = new ContentValues();
                        guardarEnlaces.put("kee_codigo", codigoEmp);
                        guardarEnlaces.put("kee_nombre", nombreEmp);
                        guardarEnlaces.put("kee_url", enlace);
                        guardarEnlaces.put("kee_status", statusEmp);
                        guardarEnlaces.put("kee_sucursal", codigoSuc);

                        /*//analizamos si hay modulos
                        Cursor cursorurl = ke_android.rawQuery("SELECT count(kmo_codigo) FROM ke_modulos WHERE ked_codigo ='" + codigoEmp+"' AND kmo_codigo='" + codigoModulo+"'", null);
                        cursorurl.moveToFirst();*/
                        ke_android.insert("ke_enlace", null, guardarEnlaces);
                        //si ya hay modulos, es que hay empresa
                        /*int conteoEnlace = cursorurl.getInt(0);
                        if(conteoEnlace > 0){
                            ke_android.execSQL("UPDATE ke_enlace SET kee_codigo ='" + codigoEmp + "', kee_nombre = '" +nombreEmp+ "', kee_url = '"+enlace+ "', kee_status='"+statusEmp+ "', kee_sucursal='"+codigoSuc+"'" +
                                    " WHERE kee_codigo = '"+codigoEmp +"' AND kee_sucursal ='" + codigoSuc +  "'");
                         //caso contrario, guardo la nueva empresa
                        }else{

                        }*/


                        //en este proceso vamos a cargar los permisos
                        JSONObject permisosJson = null;
                        for (int i = 0; i < response.length(); i++) {


                            permisosJson = response.getJSONObject(i);
                            codigoModulo = permisosJson.getString("codigoModulo");
                            //System.out.println("CODIGO DEL MODULO " + codigoModulo);
                            activoModulo = permisosJson.getString("estadoModulo");
                            //System.out.println("ESTADO DEL MODULO " + activoModulo);

                           /* Cursor cursor = ke_android.rawQuery("SELECT count(kmo_codigo) FROM ke_modulos WHERE ked_codigo ='" + codigoEmp+"' AND kmo_codigo='" + codigoModulo+"' AND kee_sucursal ='" + codigoSuc +  "'", null);
                            cursor.moveToFirst();

                            int conteoPermiso = cursor.getInt(0);
                            System.out.println("Conteo de Permisos: " + conteoPermiso);


                            if(conteoPermiso > 0){*/
                               /* ContentValues guardarPermisos = new ContentValues();
                                guardarPermisos.put("ked_codigo", codigoEmp);
                                guardarPermisos.put("kmo_codigo", codigoModulo);
                                guardarPermisos.put("kmo_status", activoModulo);

                                ke_android.update("ke_modulos", guardarPermisos, "ked_codigo = "+codigoEmp +" AND kmo_codigo = ?", new String[]{codigoModulo});*/

                            /*NO SE POR QUE, PERO SOLO AGARRA EL UPDATE HACIENDOLO DE LA FORMA LARGA*/
                            //ke_android.execSQL("UPDATE ke_modulos SET kmo_status ='" +activoModulo+ "' WHERE ked_codigo = '"+codigoEmp +"' AND kmo_codigo = '" + codigoModulo +"' AND kee_sucursal ='" + codigoSuc +  "'");

                            /*  }else{*/
                            ContentValues guardarPermisos = new ContentValues();
                            guardarPermisos.put("ked_codigo", codigoEmp);
                            guardarPermisos.put("kmo_codigo", codigoModulo);
                            guardarPermisos.put("kmo_status", activoModulo);
                            guardarPermisos.put("kee_sucursal", codigoSuc);

                            ke_android.insert("ke_modulos", null, guardarPermisos);
                            //  }

                        }
                        ke_android.setTransactionSuccessful();
                        ke_android.endTransaction();


                        if (!enlace.equals("")) {
                            //si existe la empresa, muestro los campos
                            et_usuario.setVisibility(View.VISIBLE);
                            et_password.setVisibility(View.VISIBLE);
                            bt_iniciar.setVisibility(View.VISIBLE);
                            //los activo
                            et_usuario.setEnabled(true);
                            et_password.setEnabled(true);
                            bt_iniciar.setEnabled(true);

                        } else {
                            //de lo contrario, los escondo
                            et_usuario.setVisibility(View.INVISIBLE);
                            et_password.setVisibility(View.INVISIBLE);
                            bt_iniciar.setVisibility(View.INVISIBLE);
                            //los activo
                            et_usuario.setEnabled(false);
                            et_password.setEnabled(false);
                            bt_iniciar.setEnabled(false);
                        }

                    } catch (Exception e) {
                        System.out.println(e);
                        Toast.makeText(MainActivity.this, "Error en la validacion", Toast.LENGTH_LONG).show();
                        ke_android.endTransaction();
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
                //donde estan guardados el usuario y password.
                Map<String, String> parametros = new HashMap<String, String>();
                //parametros.put("version_usuario", versionApp);

                return parametros;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonArrayRequest); //esto es el request que se envia al url a traves de la conexion volley, (el stringrequest esta armado arriba)

    }

    public void obtenerVersion(String URL) {

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(URL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

                if (response != null) {
                    System.out.println("llego aqui");
                    JSONObject jsonObject = null; //creamos un objeto json vacio

                    try {
                        jsonObject = response.getJSONObject(0);
                        System.out.println("contenido del json object" + jsonObject);

                        versionNube = jsonObject.getString("kve_version").trim();
                        System.out.println("version en nube " + versionNube);
                        caducidad = jsonObject.getString("kve_activa");

                        if (!versionNube.equals(versionApp)) {

                            Toast.makeText(MainActivity.this, "Esta versión se encuentra obsoleta, por favor, actualice", Toast.LENGTH_LONG).show();
                            bt_iniciar.setEnabled(false);

                        } else if (versionNube.equals(versionApp)) {

                            if (caducidad.equals("0")) {

                                Toast.makeText(MainActivity.this, "Esta versión se encuentra obsoleta, por favor, actualice", Toast.LENGTH_LONG).show();
                                bt_iniciar.setEnabled(false);

                            } else if (caducidad.equals("1")) {
                                //TODO EN ORDEN, NO TO CAMOS NADA.
                            }
                        }


                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Toast.makeText(MainActivity.this, "Esta versión se encuentra obsoleta, por favor, actualice", Toast.LENGTH_LONG).show();
                        bt_iniciar.setEnabled(false);
                    }


                } else if (response.equals("")) {
                    Toast.makeText(MainActivity.this, "Esta versión se encuentra obsoleta, por favor, actualice", Toast.LENGTH_LONG).show();
                    bt_iniciar.setEnabled(false);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {  //finalmente, estos son los parametros que le enviaremos al webservice, partiendo de las variables
                //donde estan guardados el usuario y password.
                Map<String, String> parametros = new HashMap<String, String>();
                //parametros.put("version_usuario", versionApp);

                return parametros;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonArrayRequest); //esto es el request que se envia al url a traves de la conexion volley, (el stringrequest esta armado arriba)
    }


    //metodo de validacion
    private void ValidarUsuario(String URL) {

        System.out.println(URL);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(URL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) { //a traves de un json array request, traemos la informacion que viene del usuario
                if (response != null) { // si la respuesta no viene vacia
                    int sesion = 0; //<-----Variable de logueo previo, 1 = Esta logueado, 0 = No esta nadie logueado

                    System.out.println(response);

                    JSONObject jsonObject = null; //creamos un objeto json vacio
                    for (int i = 0; i < response.length(); i++) { /*pongo todo en el objeto segun lo que venga */
                        try {
                            jsonObject = response.getJSONObject(i);

                            n_usuario = jsonObject.getString("nombre"); //el nombre del vendedor
                            cod_usuario = jsonObject.getString("vendedor"); //el codigo
                            nombre_usuario = jsonObject.getString("username");  //almacenamos el nombre de usuario
                            almacen = jsonObject.getString("almacen").trim();
                            desactivo = jsonObject.getDouble("desactivo"); //este campo nos indicara si el usuario se encuentra bloqueado o no.
                            fechamodifi = jsonObject.getString("fechamodifi").trim();
                            ualterprec = jsonObject.getDouble("ualterprec");
                            ultimoped = jsonObject.getString("correlativo"); //obtenemos el ultimo correlativo
                            sesionactiva = jsonObject.getString("sesionactiva"); //traemos la fecha de la sesion que estamos iniciando.
                            superves = jsonObject.getString("superves");
                            vendedor = jsonObject.getString("vendedor").trim();
                            ultimorec = jsonObject.getString("recibocobro").trim();
                            ultimorcl = jsonObject.getString("correlativoreclamo").trim();
                            ultimorcxc = jsonObject.getString("correlativoprecobranza").trim();
                            sesion = jsonObject.getInt("sesion");

                        } catch (JSONException e) {
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }

                    if (cod_usuario.length() == 0) {

                        System.out.println("LLEGO AQUI" + cod_usuario);

                        Toast.makeText(MainActivity.this, "Usuario o password incorrecto", Toast.LENGTH_LONG).show();
                        bt_iniciar.setEnabled(true);
                        bt_iniciar.setText("Iniciar Sesión");

                    } else {

                        if (sesion == 1) {
                            Toast.makeText(MainActivity.this, "Previamente Logueado", Toast.LENGTH_LONG).show();
                            bt_iniciar.setEnabled(true);
                            bt_iniciar.setText("Iniciar Sesión");
                            return;
                        }

                        if (desactivo == 0.0 || desactivo == 1.0) {

                            if (ultimoped.length() == 0) {
                                ultimoped = "0000";
                            }

                            if (ultimorec.length() == 0) {
                                ultimorec = "0000";
                            }

                            if (ultimorcl.length() == 0) {
                                ultimorcl = "0000";
                            }

                            if (ultimorcxc.length() == 0) {
                                ultimorcxc = "0000";
                            }

                            SQLiteDatabase ke_android = conn.getWritableDatabase();
                            ke_android.beginTransaction();

                            try {
                                //preparacion e inserción del correlativo de pedidos
                                String correlativoTexto = right(ultimoped, 4);
                                int nroCorrelativo = Integer.parseInt(correlativoTexto);
                                nroCorrelativo = nroCorrelativo + 1;

                                ContentValues insertar = new ContentValues();
                                insertar.put("kco_numero", nroCorrelativo);
                                insertar.put("kco_vendedor", cod_usuario.trim());

                                ke_android.insert("ke_correla", null, insertar);
                                //--------------------------------------------------------------------

                                //preparacion e inserción del correlativo de recibos
                                String reciboTexto = right(ultimorec, 4);
                                int nroRecibo = Integer.parseInt(reciboTexto);
                                nroRecibo = nroRecibo + 1;

                                ContentValues insertarRec = new ContentValues();
                                insertarRec.put("kcc_numero", nroRecibo);
                                insertarRec.put("kcc_vendedor", cod_usuario.trim());

                                ke_android.insert("ke_correlacxc", null, insertarRec);
                                //-------------------------------------------------------

                                //preparacion e inserción del correlativo de reclamos
                                String reclamoTexto = right(ultimorcl, 4);
                                int nroReclamo = Integer.parseInt(reclamoTexto);
                                nroReclamo = nroReclamo + 1;

                                ContentValues insertarRcl = new ContentValues();
                                insertarRcl.put("kdev_numero", nroReclamo);
                                insertarRcl.put("kdev_vendedor", cod_usuario.trim());

                                ke_android.insert("ke_correladev", null, insertarRcl);
                                //---------------------------------------------------------------------------

                                //preparacion e inserción del correlativo de precobranza
                                String correlaCXC = right(ultimorcxc, 4);
                                int nroCXC = Integer.parseInt(correlaCXC);
                                nroCXC = nroCXC + 1;

                                ContentValues insertarCXC = new ContentValues();
                                insertarCXC.put("kcor_numero", nroCXC);
                                insertarCXC.put("kcor_vendedor", cod_usuario.trim());

                                ke_android.insert("ke_corprec", null, insertarCXC);
                                //---------------------------------------------------------------------------


                                ke_android.delete("usuarios", "username = ?", new String[]{nick_usuario});

                                //agrego la fecha en la cual inició sesión
                                Date hoy = Calendar.getInstance().getTime();
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                String fechaSync = sdf.format(hoy);


                                ContentValues usuarioDatos = new ContentValues();
                                usuarioDatos.put("nombre", n_usuario);
                                usuarioDatos.put("username", nombre_usuario);
                                usuarioDatos.put("password", pass_usuario);
                                usuarioDatos.put("vendedor", vendedor);
                                usuarioDatos.put("almacen", almacen);
                                usuarioDatos.put("desactivo", desactivo);
                                usuarioDatos.put("fechamodifi", fechaSync);
                                usuarioDatos.put("ualterprec", ualterprec);
                                usuarioDatos.put("sesionactiva", sesionactiva);
                                usuarioDatos.put("superves", superves);

                                ke_android.insert("usuarios", null, usuarioDatos);
                                ke_android.setTransactionSuccessful();


                            } catch (Exception e) {
                                Toast.makeText(MainActivity.this, "Error insertando correlativo " + e, Toast.LENGTH_LONG).show();
                                ke_android.endTransaction();

                            } finally {
                                ke_android.endTransaction();
                            }

                            objetoAux.login(cod_usuario,1);

                            iraPrincipal();

                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString("nick_usuario", nick_usuario);
                            editor.putString("cod_usuario", cod_usuario);
                            editor.putString("nombre_usuario", n_usuario);
                            editor.putString("superves", superves);
                            editor.putString("codigoEmpresa", codigoEmp);
                            editor.putString("codigoSucursal", codigoSuc);
                            editor.commit();

                        } else if (desactivo == 2.0) {
                            Toast.makeText(MainActivity.this, "Este usuario se encuentra desactivado", Toast.LENGTH_LONG).show();
                            bt_iniciar.setEnabled(true);
                            bt_iniciar.setText("Iniciar Sesión");
                        }

                    }
                } else {
                    Toast.makeText(MainActivity.this, "Usuario o password incorrecto", Toast.LENGTH_LONG).show();
                    bt_iniciar.setEnabled(true);
                    bt_iniciar.setText("Iniciar Sesión");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                bt_iniciar.setEnabled(true);
                bt_iniciar.setText("Iniciar Sesión");
                Toast.makeText(getApplicationContext(), "Error en inicio de sesión ", Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {  //finalmente, estos son los parametros que le enviaremos al webservice, partiendo de las variables
                //donde estan guardados el usuario y password.
                Map<String, String> parametros = new HashMap<String, String>();
                parametros.put("nick_usuario", nick_usuario);
                parametros.put("pass_usuario", pass_usuario);
                return parametros;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonArrayRequest); //esto es el request que se envia al url a traves de la conexion volley, (el stringrequest esta armado arriba)

    }

    /*private void login() {
        String URL = "https://8135-45-186-202-166.ngrok.io/login";

        JSONObject Login = new JSONObject();
        JSONObject subLogin = new JSONObject();

        try {
            subLogin.put("vendedor", cod_usuario);
            subLogin.put("sesion", 1);

            Login.put("Login", subLogin);
        } catch (Exception e) {
            e.printStackTrace();
        }


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URL, Login, response -> {

            if (response != null) {
                try {
                    JSONObject jsonObject = response.getJSONObject("estado");
                    if (jsonObject.getString("status").equals("404")) {
                        Toast.makeText(this, "Error 404", LENGTH_LONG).show();
                    } else if ((jsonObject.getString("status").equals("200")) && (!jsonObject.getString("usuario").equals(cod_usuario))) {
                        Toast.makeText(this, "No se pudo iniciar sesión adecuadamente", LENGTH_LONG).show();
                    } else if ((jsonObject.getString("status").equals("200")) && (jsonObject.getString("usuario").equals(cod_usuario))) {
                        Toast.makeText(this, "Sesión Iniciada", LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }, error -> {
            Toast.makeText(this, "No se pudo iniciar sesión adecuadamente", LENGTH_LONG).show();
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }*/


    private void ValidarSesion() {
        nick_usuario = preferences.getString("nick_usuario", null);
        cod_usuario = preferences.getString("cod_usuario", null);

        SQLiteDatabase ke_android = conn.getWritableDatabase();
        Cursor cursor = ke_android.rawQuery("SELECT desactivo FROM usuarios  WHERE 1", null);

        while (cursor.moveToNext()) {

            desactivo = cursor.getDouble(0);
            System.out.println(desactivo);
        }
        if (nick_usuario != null && !desactivo.equals(2.0)) {
            iraPrincipal();
        } else {

        }
    }

    private void iraPrincipal() {

        Intent intent = new Intent(getApplicationContext(), PrincipalActivity.class); // creamos intent hacia la clase principal
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent); //y lo iniciamos
    }


    public static String right(String valor, int longitud) {
        //una función "right" utilizando la clase substring
        return valor.substring(valor.length() - longitud);
    }

    @Override
    protected void onResume() {
        codigo_empresa = "081196";
        super.onResume();
        validarEmpresaLicencia("https://www.cloccidental.com/webservice/validarempresa.php?codigo=" + codigo_empresa);
    }

    private void checkForAppUpdate(){
        AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(this);

        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.clientVersionStalenessDays() != null
                    && appUpdateInfo.clientVersionStalenessDays() >= 2
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {

                /*try {
                    appUpdateManager.startUpdateFlowForResult(
                            // Pass the intent that is returned by 'getAppUpdateInfo()'.
                            appUpdateInfo,

                            AppUpdateType.IMMEDIATE,

                            this,

                            MY_REQUEST_CODE);
                } catch (IntentSender.SendIntentException e) {
                    throw new RuntimeException(e);
                }*/

                try {
                    appUpdateManager.startUpdateFlowForResult(
                            // Pass the intent that is returned by 'getAppUpdateInfo()'.
                            appUpdateInfo,
                            // an activity result launcher registered via registerForActivityResult
                            AppUpdateType.IMMEDIATE,
                            // Or pass 'AppUpdateType.FLEXIBLE' to newBuilder() for
                            // flexible updates.
                            this,
                            MY_REQUEST_CODE);
                } catch (IntentSender.SendIntentException e) {
                    throw new RuntimeException(e);
                }

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Actualización Exitosa!", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Actualización Cancelada", Toast.LENGTH_SHORT).show();
            } else if (resultCode == ActivityResult.RESULT_IN_APP_UPDATE_FAILED) {
                Toast.makeText(this, "Algo salio mal", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Algo salio mal, sin datos", Toast.LENGTH_SHORT).show();
            }
        }
    }

}