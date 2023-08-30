/*
 ****************************************************************************************************
 * 05-10-2020 P.C.V
 * ESTE ES EL "HOME" DE LA APP
 * YA LA INFORMACION DEL USUARIO SE ENCUENTRA GUARDADA EN UN JSON
 *
 */


package com.appcloos.mimaletin;

import static android.widget.Toast.LENGTH_LONG;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewbinding.ViewBinding;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.appcloos.mimaletin.ModuloReten.ModuloRetenActivity;
import com.appcloos.mimaletin.moduloCXC.ModuloCXCActivity;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.ActivityResult;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.squareup.picasso.Picasso;

import org.imaginativeworld.whynotimagecarousel.ImageCarousel;
import org.imaginativeworld.whynotimagecarousel.listener.CarouselListener;
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PrincipalActivity extends AppCompatActivity implements Serializable, NavigationView.OnNavigationItemSelectedListener {

    public static String cod_usuario;
    public static Double desactivo = 0.0, statusDelUsuario = 0.0;
    public static String versionApp = "2.3.3", version, caducidad, versionNube, codigoEmpresa = "", nombreEmpresa = "", enlaceEmpresa = "", codigoSucursal = "";
    static PrincipalActivity activaprincipal;
    DrawerLayout drawer;
    ActionBarDrawerToggle toggle;
    TextView tv_nombreu, tv_estadosync, tv_pcreados, tv_version, tv_tasa_menup, tv_fecha_tasa, tvFechaVersion;
    ListView lv_pedidos_lista;
    //BottomNavigationView menunav ;
    ImageButton bt_sincact, ibt_clientes, ibt_catalogo, ibt_pedidos, ib_sync, ib_alert;
    ImageView img_principal, img_comunicados;
    Button bt_nuevosarticulos;
    ArrayList<String> permisos;
    int filas, REQUEST_CODE = 200;
    ObjetoAux objetoAux;
    AdminSQLiteOpenHelper conn;
    Boolean sesionObsoleta = false;
    Toolbar toolbar;
    String fecha_auxiliar = "0001-01-01";
    Boolean ll_commit = false;
    NavigationView navView;
    private ProgressDialog progresoArticulos;
    private SharedPreferences preferences;

    private boolean SINCRONIZO, DESACTIVADO;

    private static final int MY_REQUEST_CODE = 100;

    AppUpdateManager appUpdateManager;

    public static PrincipalActivity getInstance() {
        return activaprincipal;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);//mantener la activity en vertical
        super.onCreate(savedInstanceState);

        //System.out.println("Modelo ->"+ Build.MODEL);
        //System.out.println("Manofactura ->"+ Build.MANUFACTURER);

        //String nick_usuario = getIntent().getStringExtra("nick_usuario");
        setContentView(R.layout.activity_principal);
        activaprincipal = this;
        conn = new AdminSQLiteOpenHelper(getApplicationContext(), "ke_android", null, 12);
        objetoAux = new ObjetoAux(this);

        SQLiteDatabase ke_android = conn.getWritableDatabase();
        permisos = new ArrayList<>();

        navView = findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(this);
        preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        cod_usuario = preferences.getString("cod_usuario", null);
        codigoEmpresa = preferences.getString("codigoEmpresa", null);
        codigoSucursal = preferences.getString("codigoSucursal", null);
        String nombre_usuario = preferences.getString("nombre_usuario", null);

        objetoAux.descargaDesactivo(cod_usuario);
        SINCRONIZO = conn.SincronizoPriVez(cod_usuario);
        DESACTIVADO = conn.getCampoInt("usuarios", "desactivo", "vendedor", cod_usuario) == 0;

        checkForAppUpdate();

        //System.out.println("MAIN ACTIVITY");

        /*Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .setRequiresBatteryNotLow(false)
                .setRequiresDeviceIdle(true)
                .build();

        PeriodicWorkRequest uploadWorkRequest =
                new PeriodicWorkRequest.Builder(CxcUpWorker.class,
                        15, TimeUnit.MINUTES)
                        .setConstraints(constraints)
                        .setInputData(
                                new Data.Builder()
                                        .putString("codUsuario", cod_usuario)
                                        .build()
                        )
                        .addTag("SubirCobranza")
                        .build();

        *//*WorkManager
                .getInstance(this)
                .enqueue(uploadWorkRequest);*//*

        WorkManager.getInstance(this)
                .enqueueUniquePeriodicWork("SubirCXC", ExistingPeriodicWorkPolicy.UPDATE, uploadWorkRequest);*/

        cargarModulosActivos();
        cargarEnlace();
        //System.out.println("ENLACE " + enlaceEmpresa);


        //Edicion de los colores del Bar de arriba de notificacion de las app y el bar de abajo de los 3 botones
        Window window = this.getWindow();

        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        int nightModeFlags =
                this.getResources().getConfiguration().uiMode &
                        Configuration.UI_MODE_NIGHT_MASK;

        if (nightModeFlags == Configuration.UI_MODE_NIGHT_NO){
            window.setStatusBarColor(ContextCompat.getColor(this,R.color.primaryColor));
            window.setNavigationBarColor(ContextCompat.getColor(this,R.color.primaryColor));
        }else {
            window.setStatusBarColor(ContextCompat.getColor(this,R.color.blackColor1));
            window.setNavigationBarColor(ContextCompat.getColor(this,R.color.blackColor2));
        }


        carousel();

        toolbar = findViewById(R.id.toolbar_main);
        drawer = findViewById(R.id.drawer_layout);
        this.setSupportActionBar(toolbar);

        toggle = new ActionBarDrawerToggle(PrincipalActivity.this, drawer, toolbar, 0, 0);
        drawer.addDrawerListener(toggle);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_main);
        getSupportActionBar().setHomeButtonEnabled(true);

        tv_version = findViewById(R.id.version);
        tv_tasa_menup = findViewById(R.id.tv_tasa_menup);
        tv_fecha_tasa = findViewById(R.id.tv_fecha_tasa);
        tv_version.setText("Ver. " + Constantes.VERSION_NAME);
        ib_sync = findViewById(R.id.img_sync_tasa);
        ib_alert = findViewById(R.id.img_aviso_tasa);
        tvFechaVersion = findViewById(R.id.tvFechaVersion);

        tvFechaVersion.setText("Actualización " + Constantes.FECHA_VERSION);

        //los botones para ir a las diferentes activities
        navView.setNavigationItemSelectedListener(this);
        View headerView = navView.getHeaderView(0);


        tv_nombreu = headerView.findViewById(R.id.tv_nombreu);
        tv_nombreu.setText("Bienvenid@, " + nombre_usuario);
        descargarTasas("https://" + enlaceEmpresa + "/webservice/tasas.php?fecha_sinc=" + fecha_auxiliar.trim() + "&&agencia=" + codigoSucursal.trim());

        ib_sync.setOnClickListener(v -> {
            descargarTasas("https://" + enlaceEmpresa + "/webservice/tasas.php?fecha_sinc=" + fecha_auxiliar.trim() + "&&agencia=" + codigoSucursal.trim());
            Toast.makeText(PrincipalActivity.this, "Actualizando tasa...", Toast.LENGTH_SHORT).show();
        });


        //conteoPedidosCreados();

        obtenerPermisos();
        try {
            validarSesionActiva();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //2023-08-04 Antigua forma de pedir permisos
        //solicitarPermisosInternos();
        //2023-08-04 Nueva forma de pedir permisos
        checkPermissions();
        //obtenerVersion("https://cloccidental.com/webservice/versionapp.php/?version_usuario=" + versionApp + "");

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
                System.exit(0);
            }
        });

    }

    private void carousel() {

        ImageCarousel carousel = findViewById(R.id.carousel);

        carousel.registerLifecycle(getLifecycle());

        List<CarouselItem> list = conn.getImgCarousel();


        carousel.setCarouselListener(new CarouselListener() {
            @Nullable
            @Override
            public ViewBinding onCreateViewHolder(@NonNull LayoutInflater layoutInflater, @NonNull ViewGroup viewGroup) {
                return null;
            }

            @Override
            public void onBindViewHolder(@NonNull ViewBinding viewBinding, @NonNull CarouselItem carouselItem, int i) {

            }

            @Override
            public void onClick(int position, @NonNull CarouselItem carouselItem) {
                final ImageView imagen = new ImageView(PrincipalActivity.this);
                String enlace = list.get(position).getImageUrl();
                Picasso.get().load(enlace).resize(1000, 1000).centerCrop().into(imagen);

                AlertDialog.Builder ventana = new AlertDialog.Builder(new ContextThemeWrapper(PrincipalActivity.this,R.style.AlertDialogCustom));
                ventana.setTitle("Imagen del articulo");
                ventana.setView(imagen);
                ventana.setPositiveButton("Aceptar", null);

                AlertDialog dialogo = ventana.create();
                dialogo.show();
            }

            @Override
            public void onLongClick(int i, @NonNull CarouselItem carouselItem) {

            }
        });

        carousel.setData(list);
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //Permiso no aceptado
            requestPermisos();
        }
    }

    private void requestPermisos() {
        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            //Pedir permisos
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 777);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 777) { // nuestros permisos
            if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso Aceptado", Toast.LENGTH_SHORT).show();
            } else {
                //Toast.makeText(this, "Seleccionó el boton de \"Rechazar Permisos\"", LENGTH_LONG).show();
            }
        }
    }

    private void cargarUtilmaTasa() {
        //descargarTasas("https://" + enlaceEmpresa + "/webservice/tasas.php?fecha_sinc=" + fecha_auxiliar.trim() + "&&agencia=" + codigoSucursal.trim());

        SQLiteDatabase ke_android = conn.getWritableDatabase();
        Cursor cursor = ke_android.rawQuery("SELECT kecxc_tasa, kecxc_fchyhora FROM kecxc_tasas ORDER BY kecxc_fchyhora DESC LIMIT 1", null);
        String fechaTasa = "";
        LocalDate fechaTasaf;


        if (cursor.moveToFirst()) {
            fechaTasa = cursor.getString(1);
            tv_tasa_menup.setText(cursor.getString(0) + "Bs.");

            ObjetoUtils objetoUtils = new ObjetoUtils();

            String fechaTasaShow = objetoUtils.formatoFechaHoraShow(fechaTasa);

            tv_fecha_tasa.setText(fechaTasaShow);

        }else {
            tv_fecha_tasa.setText("0000-00-00");
            tv_tasa_menup.setText("0.0Bs.");
        }
        cursor.close();

        LocalDate fecha_actual = LocalDate.now();
        int diferencia;

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");
            fechaTasaf = LocalDate.parse(fechaTasa, formatter);

            diferencia = (int) ChronoUnit.DAYS.between(fecha_actual, fechaTasaf);

        } catch (Exception e) {
            diferencia = 1;
        }

        if (diferencia >= 1) {
            ib_alert.setVisibility(View.VISIBLE);
        } else {
            ib_alert.setVisibility(View.INVISIBLE);
        }
    }

    private void descargarTasas(String URL) {
        SQLiteDatabase ke_android = conn.getWritableDatabase();


        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(URL, response -> {
            if (response != null) {

                JSONObject jsonObject;
                ke_android.beginTransaction();

                try {
                    for (int i = 0; i < response.length(); i++) {

                        jsonObject = response.getJSONObject(i);
                        String id = jsonObject.getString("id").trim();
                        String fecha = jsonObject.getString("fecha").trim();
                        Double tasa = jsonObject.getDouble("tasa");
                        String ip = jsonObject.getString("ip").trim();
                        String fchyhora = jsonObject.getString("fechayhora").trim();
                        String fechamod = jsonObject.getString("fechamodifi").trim();

                        ContentValues qtasas = new ContentValues();
                        qtasas.put("kecxc_id", id);
                        qtasas.put("kecxc_fecha", fecha);
                        qtasas.put("kecxc_tasa", tasa);
                        qtasas.put("kecxc_ip", ip);
                        qtasas.put("kecxc_fchyhora", fchyhora);
                        qtasas.put("fechamodifi", fechamod);

                        Cursor qcodigolocal = ke_android.rawQuery("SELECT count(kecxc_id) FROM kecxc_tasas WHERE kecxc_id ='" + id + "'", null);
                        qcodigolocal.moveToFirst();
                        int codigoExistente = qcodigolocal.getInt(0);
                        qcodigolocal.close();
                        if (codigoExistente > 0) {
                            String[] args = new String[]{"" + id + ""};
                            ke_android.update("kecxc_tasas", qtasas, "kecxc_id= ?", args);
                        } else if (codigoExistente == 0) {
                            ke_android.insert("kecxc_tasas", null, qtasas);
                        }
                        ll_commit = true;

                    }

                    cargarUtilmaTasa();

                } catch (Exception exception) {
                    cargarUtilmaTasa();
                    exception.printStackTrace();
                    ll_commit = false;
                    if (!ll_commit) return;
                }
                if (ll_commit) {
                    ke_android.setTransactionSuccessful();
                    ke_android.endTransaction();

                } else if (!ll_commit) {
                    ke_android.endTransaction();
                }
            }
        }, error -> {
            cargarUtilmaTasa();
            System.out.println("--Error--");
            error.printStackTrace();
            System.out.println("--Error--");
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> parametros = new HashMap<>();
                parametros.put("cod_usuario", cod_usuario);
                return parametros;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonArrayRequest);


    }

    public void cargarModulosActivos() {

        SQLiteDatabase ke_android = conn.getWritableDatabase();
        //Cursor cursor = ke_android.rawQuery("SELECT kmo_codigo FROM ke_modulos WHERE kmo_status = '1' AND ked_codigo='" + codigoEmpresa + "'", null);
        Cursor cursor = ke_android.rawQuery("SELECT cnfg_idconfig FROM ke_wcnf_conf WHERE cnfg_clase = 'M' AND cnfg_valsino = '1';", null);

        while (cursor.moveToNext()) {
            permisos.add(cursor.getString(0));
        }



        for (int i = 0; i < permisos.size(); i++) {
            String permisoEval = permisos.get(i);

            //ANCHOR -- AL PARECER EL PROBLEMA DE LOS PERMISOS SE RESOLVIÓ (2022-03-18 -- PCV)
            switch (permisoEval) {

                //Verificando que para ver:
                //El permiso de exclusion del modulo.
                //El usuario haya sincronizado por primera vez.
                //El usuario no este desactivo

                case "APP_MODULO_CLIENTES":
                    if (!conn.getConfigBoolUsuario("APP_MODULO_CLIENTES_USER", cod_usuario) && SINCRONIZO && DESACTIVADO) {
                        navView.getMenu().getItem(0).setVisible(true);
                    }
                    break;

                case "APP_MODULO_CATALOGO":
                    if(!conn.getConfigBoolUsuario("APP_MODULO_CATALOGO_USER", cod_usuario) && SINCRONIZO && DESACTIVADO){
                        navView.getMenu().getItem(1).setVisible(true);
                        navView.getMenu().getItem(2).setVisible(true);
                    }
                    break;

                case "APP_MODULO_PEDIDO":
                    if (!conn.getConfigBoolUsuario("APP_MODULO_PEDIDO_USER", cod_usuario) && SINCRONIZO && DESACTIVADO) {
                        navView.getMenu().getItem(3).setVisible(true);
                    }
                    break;

                case "APP_MODULO_CXC_OLD":
                    //cobranzas viejas
                    if (!conn.getConfigBoolUsuario("APP_MODULO_CXC_OLD_USER", cod_usuario) && SINCRONIZO && DESACTIVADO) {
                        navView.getMenu().getItem(6).setVisible(true);
                    }
                    break;

                //pilas con esta verga
                case "APP_MODULO_CXC":
                    //navView.getMenu().getItem(4).setVisible(true);

                    //cobranzas nuevas
                    if (!conn.getConfigBoolUsuario("APP_MODULO_CXC_USER", cod_usuario) && SINCRONIZO && DESACTIVADO) {
                        navView.getMenu().getItem(7).setVisible(true);
                        navView.getMenu().getItem(8).setVisible(true);
                    }

                    //Modulo Retencion
                    //navView.getMenu().getItem(9).setVisible(true);


                    //navView.getMenu().getItem(5).setVisible(true);
                    break;

                case "APP_MODULO_RETEN":
                    if (!conn.getConfigBoolUsuario("APP_MODULO_RETEN_USER", cod_usuario) && SINCRONIZO && DESACTIVADO) {
                        navView.getMenu().getItem(9).setVisible(true);
                    }

                case "APP_MODULO_ESTADISTICA":
                    if (!conn.getConfigBoolUsuario("APP_MODULO_ESTADISTICA_USER", cod_usuario) && SINCRONIZO && DESACTIVADO) {
                        navView.getMenu().getItem(4).setVisible(true);
                    }
                    break;

                case "APP_MODULO_RECLAMO":
                    if (!conn.getConfigBoolUsuario("APP_MODULO_RECLAMO_USER", cod_usuario) && SINCRONIZO && DESACTIVADO) {
                        navView.getMenu().getItem(5).setVisible(true);
                    }
                    break;

            }


        }

        cursor.close();


    }

   /* private void colocarImagen() {

        String enlace = "https://"+ enlaceEmpresa +"/img/app_main_menu.jpg";
        Picasso.get().load(enlace).resize(500,350).centerCrop().into(img_principal);

        String enlaceCom = "https://" + enlaceEmpresa + "/img/app_img_com.jpg";
        Picasso.get().load(enlaceCom).resize(500,400).centerCrop().into(img_comunicados);
    }*/

    private void cargarEnlace() {
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        String[] columnas = new String[]{"kee_nombre," + "kee_url," + "kee_sucursal"};
        Cursor cursor = ke_android.query("ke_enlace", columnas, "kee_codigo ='" + codigoEmpresa + "'", null, null, null, null);

        while (cursor.moveToNext()) {
            //cargo los datos de la empresa
            nombreEmpresa = cursor.getString(0);
            enlaceEmpresa = cursor.getString(1);
            //sucursalEmpresa = cursor.getString(2);
        }
        cursor.close();

    }

    public void obtenerVersion(String URL) {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(URL, response -> {

            if (response != null) {
                JSONObject jsonObject; //creamos un objeto json vacio

                try {
                    jsonObject = response.getJSONObject(0);

                    versionNube = jsonObject.getString("kve_version").trim();
                    caducidad = jsonObject.getString("kve_activa");

                    if (!versionNube.equals(versionApp)) {

                        Toast.makeText(PrincipalActivity.this, "Esta versión se encuentra obsoleta, por favor, actualice", Toast.LENGTH_LONG).show();
                        cerrarsesion();

                    } else if (versionNube.equals(versionApp)) {

                        if (caducidad.equals("0")) {

                            Toast.makeText(PrincipalActivity.this, "Esta versión se encuentra obsoleta, por favor, actualice", Toast.LENGTH_LONG).show();
                            cerrarsesion();

                        } else if (caducidad.equals("1")) {
                            //TODO EN ORDEN, NO TO CAMOS NADA.
                        }
                    }


                } catch (Exception ex) {
                    ex.printStackTrace();
                    Toast.makeText(PrincipalActivity.this, "Esta versión se encuentra obsoleta, por favor, actualice", Toast.LENGTH_LONG).show();
                    cerrarsesion();
                }


            } else if (response.equals("")) {
                Toast.makeText(PrincipalActivity.this, "Esta versión se encuentra obsoleta, por favor, actualice", Toast.LENGTH_LONG).show();
                cerrarsesion();
            }

        }, error -> {
            System.out.println("--Error--");
            error.printStackTrace();
            System.out.println("--Error--");
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

    /*@Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(activaprincipal, "", Toast.LENGTH_SHORT).show();
            }
        }
    }*/

    private void solicitarPermisosInternos() {

        int permisoAlmacenamiento = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permisoAlmacenamiento == PackageManager.PERMISSION_GRANTED) {
            //DO NOTHING
        } else {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
        }

        /*if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){

        }else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
        }*/


    }

    private void iraCobranzas() {
        Intent intent = new Intent(getApplicationContext(), CobranzasActivity.class);
        intent.putExtra("cod_usuario", cod_usuario);
        startActivity(intent);
    }

    private void iraClientes() {
        Intent intent = new Intent(getApplicationContext(), ClientesActivity.class);
        intent.putExtra("cod_usuario", cod_usuario);
        intent.putExtra("codigoEmpresa", codigoEmpresa);
        startActivity(intent);
    }

    private void iraCXC() {
        Intent intent = new Intent(getApplicationContext(), CXCActivity.class);
        intent.putExtra("cod_usuario", cod_usuario);
        intent.putExtra("codigoEmpresa", codigoEmpresa);
        startActivity(intent);
    }

    private void iraModuloCXC() {

        /*SharedPreferences.Editor editor = preferences.edit();
        editor.putString("cod_usuario", cod_usuario);
        editor.putString("origin", "CXC");
        editor.apply();*/

        Intent intent = new Intent(getApplicationContext(), ModuloCXCActivity.class);
        intent.putExtra("cod_usuario", cod_usuario);
        startActivity(intent);
    }

    private void iraKardex() {
        Intent intent = new Intent(getApplicationContext(), KardexActivity.class);
        intent.putExtra("cod_usuario", cod_usuario);
        startActivity(intent);
    }

    private void iraPedidos() {
        obtenerPermisos();

        if (desactivo.equals(0.0)) {

            Intent intent = new Intent(getApplicationContext(), PedidosActivity.class);
            intent.putExtra("cod_usuario", cod_usuario);
            startActivity(intent);

        } else if (desactivo.equals(1.0)) {
            Toast.makeText(getApplicationContext(), "Usuario bloqueado", Toast.LENGTH_LONG).show();
        }


    }


    private void iraSync() {
        Intent intent = new Intent(getApplicationContext(), SincronizacionActivity.class);
        startActivity(intent);
    }


    private void iraEstadisticas() {
        Intent intent = new Intent(getApplicationContext(), EstadisticasActivity.class);
        startActivity(intent);
    }


    //este es el metodo para cerrar sesion
    private void cerrarsesion() {
        conn.DeleteAll("usuarios");
        objetoAux.login(cod_usuario, 0);

        SharedPreferences preferences = getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        preferences.edit().clear().apply();

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();

    }

    //este es el metodo para ir al catalogo
    private void iraCatalogo() {

        Intent intent = new Intent(getApplicationContext(), CatalogoActivity.class);
        int seleccion = 1;
        intent.putExtra("Seleccion", seleccion);
        startActivity(intent);


    }


    private void obtenerPermisos() {
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        Cursor cursor = ke_android.rawQuery("SELECT desactivo FROM usuarios  WHERE vendedor = '" + cod_usuario + "'", null);


        while (cursor.moveToNext()) {
            desactivo = cursor.getDouble(0);
        }
        cursor.close();

    }


    private void validarSesionActiva() throws ParseException {

        SQLiteDatabase ke_android = conn.getWritableDatabase();
        Cursor cursor = ke_android.rawQuery("SELECT desactivo FROM usuarios  WHERE vendedor ='" + cod_usuario + "'", null);

        while (cursor.moveToNext()) {
            statusDelUsuario = cursor.getDouble(0);
        }
        cursor.close();
        if (statusDelUsuario.equals(2.0)) {
            cerrarsesion();
        }

        //REVISAR ESTO, ESTÁ GENERANDO QUE EL USUARIO SE SALGA LUEGO DE SINCRONIZAR:
        //obtengo la fecha de hoy puesto que voy a comparar con la ultima sincronizacion mas reciente
        /*Date hoy         = Calendar.getInstance().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String fechaUltm = "";
        //ahora voy a buscar la fecha mas reciente de las sincronizaciones
        Cursor cursorFec = ke_android.rawQuery("SELECT fechamodifi FROM usuarios WHERE vendedor='" + cod_usuario + "' AND fechamodifi = (SELECT MAX(fechamodifi) FROM usuarios)", null);

        while(cursorFec.moveToNext()){
            fechaUltm = cursorFec.getString(0);
            System.out.println(fechaUltm);
        }

        if(fechaUltm.contains("/")){
            fechaUltm.replace("/", "-");
        }
        Date fechaDeLogin = sdf.parse(fechaUltm);
        assert fechaDeLogin != null;
        long diff         = hoy.getTime() - fechaDeLogin.getTime();
        TimeUnit time     = TimeUnit.DAYS;
        long diferencia   = time.convert(diff, TimeUnit.MILLISECONDS);
        System.out.println("diferencia: " + diferencia);

        if(diferencia >= 3){
            cerrarsesion();
            Toast.makeText(getApplicationContext(), "La sesión ha expirado", Toast.LENGTH_SHORT).show();
        }*/
    }


    @Override
    protected void onResume() {
        //conteoPedidosCreados();



        permisos = new ArrayList<>();
        cargarEnlace();
        cargarModulosActivos();

        preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        cod_usuario = preferences.getString("cod_usuario", null);
        objetoAux.descargaDesactivo(cod_usuario);
        obtenerPermisos();
        try {
            validarSesionActiva();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        super.onResume();
        obtenerVersion("https://cloccidental.com/webservice/versionapp.php?version_usuario=" + versionApp);

        validarUpInApp();

    }



    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.icclientes:
                iraClientes();
                return true;

            case R.id.iccatalogo:
                if (PedidoBloq()) {
                    iraCatalogo();
                } else {
                    PedidosDialogAlert();
                }
                return true;

            case R.id.icpedidos:
                System.out.println("Los permisos -> " + permisos);

                    //IF que se ayuda de la Funcion PedidoBloq() que valida la fecha de la ultima vez que sincroonizo el vendedor
                    //En caso de ser true el vendedor puede hacer pedido
                    //En caso de ser false se le notificara al vendedor por una alerta
                    if (PedidoBloq()) {
                        iraPedidos();
                    } else {
                        PedidosDialogAlert();
                    }


                return true;

            case R.id.icestadistica:

                    iraEstadisticas();

                return true;

            case R.id.iccobranzas:

                    iraCobranzas();

                return true;

           /* case R.id.icplanificador:
                if(permisos.contains("CXC001")){
                    iraPlanificador();
                }else{
                    System.out.println("Este modulo no está disponible");
                }
                return true;*/

            case R.id.icreclamos:

                    iraReclamos();

                return true;

            case R.id.icCXC:
                iraCXC();
                return true;

            case R.id.ickardex:
                iraKardex();
                return true;

            case R.id.icsync:
                iraSync();
                return true;

            case R.id.iccerrarsesion:
                cerrarsesion();
                return true;

            case R.id.moduloCXC:
                iraModuloCXC();
                return true;

            case R.id.moduloReten:
                iraReten();
                return true;

        }

        return false;

    }

    private void iraReten() {
        //Intent intent = new Intent(getApplicationContext(), SelectorClienteReten.class);
        Intent intent = new Intent(getApplicationContext(), ModuloRetenActivity.class);
        intent.putExtra("cod_usuario", cod_usuario);
        startActivity(intent);
    }

    private void iraPlanificador() {
        Intent intent = new Intent(getApplicationContext(), PlanificadorActivity.class);
        intent.putExtra("cod_usuario", cod_usuario);
        startActivity(intent);
    }

    private void iraReclamos() {
        Intent intent = new Intent(getApplicationContext(), ListaReclamosActivity.class);
        intent.putExtra("cod_usuario", cod_usuario);
        startActivity(intent);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    //Funcion que crea y muestra el DialogAlert
    private void PedidosDialogAlert() {
        new AlertDialog.Builder(this).setTitle("Alerta").setMessage("Artículos desactualizados. Por favor diríjase a \"Sincronizar Datos\"").setNegativeButton(android.R.string.ok, null).setIcon(android.R.drawable.presence_busy).show();
    }

    //Funcion que valida el tiempo que lleva el vendedor sin sincroonizar
    private boolean PedidoBloq() {
        //Ejecucion de la seleccion de la fechas mas actual dentro de articulo
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        Cursor cursor = ke_android.rawQuery("SELECT ult_sinc FROM usuarios WHERE vendedor = '" + cod_usuario + "'", null);

        //Declaracion de variables
        String fechamodifi = null;
        LocalDate fecha_actual = LocalDate.now();
        LocalDate fechamodifi2;
        int diferencia;


        if (cursor.moveToFirst()) {
            fechamodifi = cursor.getString(0);
        }
        cursor.close();


        //TRY para el guardado, formateo y comparacion de las fechas (Se crea para los casos en los que el vendedor nunca ha sincroonizado)
        try {
            fechamodifi2 = LocalDate.parse(fechamodifi, DateTimeFormatter.ISO_LOCAL_DATE);
            diferencia = (int) ChronoUnit.DAYS.between(fechamodifi2, fecha_actual);
            //CATCH para el alor de diferencia en caso de error
        } catch (Exception e) {
            diferencia = 3;
        }
        //System.out.println(fechamodifi2);
        //System.out.println(fecha_actual);
        //System.out.println("hola" + diferencia);

        //IF que valida con ayuda de diferencia (Variable que guarda la resta entre la ultima fecha en la base de datos y la actual del tlf) si la fecha en mayor a 2
        //En caso de ser mayor a 2 envia false impidiendo el paso a pedido
        return diferencia <= 2;
    }

    private void login() {
        String URL = "https://8135-45-186-202-166.ngrok.io/login";

        JSONObject Login = new JSONObject();
        JSONObject subLogin = new JSONObject();

        try {
            subLogin.put("vendedor", cod_usuario);
            subLogin.put("sesion", 0);

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
                        Toast.makeText(this, "No se pudo terminar sesión adecuadamente", LENGTH_LONG).show();
                    } else if ((jsonObject.getString("status").equals("200")) && (jsonObject.getString("usuario").equals(cod_usuario))) {
                        Toast.makeText(this, "Sesión Terminada", LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }, error -> Toast.makeText(this, "No se pudo terminar sesión adecuadamente", LENGTH_LONG).show());

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }

    //  -------------------------------------------------------------------------------------------------------------------

    private void checkForAppUpdate(){
        appUpdateManager = AppUpdateManagerFactory.create(this);

        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
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
                    System.out.println("ACTUALIZACION");
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

            }else {
                System.out.println("NO ACTUALIZACION");
            }
        });
    }

    private void validarUpInApp() {
        appUpdateManager
                .getAppUpdateInfo()
                .addOnSuccessListener(
                        appUpdateInfo -> {
                            if (appUpdateInfo.updateAvailability()
                                    == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS){
                                try {
                                    appUpdateManager.startUpdateFlowForResult(
                                            appUpdateInfo,
                                            AppUpdateType.IMMEDIATE,
                                            this,
                                            MY_REQUEST_CODE
                                    );
                                } catch (IntentSender.SendIntentException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Actualización Exitosa!", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Actualización Cancelada", Toast.LENGTH_SHORT).show();
                this.finish();
                System.exit(0);
            } else if (resultCode == ActivityResult.RESULT_IN_APP_UPDATE_FAILED) {
                Toast.makeText(this, "Algo salio mal", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Algo salio mal, sin datos", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static Bitmap getRoundedCornerBitmap(Drawable drawable, boolean square) {
        int width;
        int height;

        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap() ;

        if(square){
            if(bitmap.getWidth() < bitmap.getHeight()){
                width = bitmap.getWidth();
                height = bitmap.getWidth();
            } else {
                width = bitmap.getHeight();
                height = bitmap.getHeight();
            }
        } else {
            height = bitmap.getHeight();
            width = bitmap.getWidth();
        }

        Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, width, height);
        final RectF rectF = new RectF(rect);
        final float roundPx = 90;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

}







