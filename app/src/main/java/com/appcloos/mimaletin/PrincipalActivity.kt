/*
 ****************************************************************************************************
 * 05-10-2020 P.C.V
 * ESTE ES EL "HOME" DE LA APP
 * YA LA INFORMACION DEL USUARIO SE ENCUENTRA GUARDADA EN UN JSON
 *
 */
package com.appcloos.mimaletin

import android.Manifest
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Resources.Theme
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewbinding.ViewBinding
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.appcloos.mimaletin.ModuloReten.ModuloRetenActivity
import com.appcloos.mimaletin.databinding.ActivityPrincipalBinding
import com.appcloos.mimaletin.dialogChangeAccount.DialogChangeAccount
import com.appcloos.mimaletin.moduloCXC.ModuloCXCActivity
import com.google.android.material.navigation.NavigationView
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.ActivityResult
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.squareup.picasso.Picasso
import org.imaginativeworld.whynotimagecarousel.ImageCarousel
import org.imaginativeworld.whynotimagecarousel.listener.CarouselListener
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem
import org.json.JSONArray
import org.json.JSONObject
import java.io.Serializable
import java.text.DecimalFormat
import java.text.ParseException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.system.exitProcess

class PrincipalActivity : AppCompatActivity(), Serializable,
    NavigationView.OnNavigationItemSelectedListener {

    private var toggle: ActionBarDrawerToggle? = null
    private lateinit var tvNombreu: TextView
    private lateinit var llHeaderNavMenu: LinearLayout
    var tvEstadosync: TextView? = null
    var tvPcreados: TextView? = null


    var lvPedidosLista: ListView? = null

    //BottomNavigationView menunav ;
    var btSincact: ImageButton? = null
    var ibtClientes: ImageButton? = null
    var ibtCatalogo: ImageButton? = null
    var ibtPedidos: ImageButton? = null
    var imgPrincipal: ImageView? = null
    var imgComunicados: ImageView? = null
    var btNuevosArticulos: Button? = null
    private var permisos: ArrayList<String>? = null
    var filas = 0
    private var REQUEST_CODE = 200
    var objetoAux: ObjetoAux? = null
    lateinit var conn: AdminSQLiteOpenHelper
    var sesionObsoleta = false
    private var toolbar: Toolbar? = null
    private var fechaAuxiliar = "0001-01-01"
    var llCommit = false
    private lateinit var navView: NavigationView
    private var appUpdateManager: AppUpdateManager? = null
    private val progresoArticulos: ProgressDialog? = null
    private lateinit var preferences: SharedPreferences
    private var SINCRONIZO = false
    private var DESACTIVADO = false
    private var APP_DESCUENTOS_PEDIDOS = false

    private lateinit var binding: ActivityPrincipalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED //mantener la activity en vertical
        super.onCreate(savedInstanceState)



        binding = ActivityPrincipalBinding.inflate(layoutInflater)
        setContentView(binding.root)
        instance = this
        conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null)
        objetoAux = ObjetoAux(this)
        permisos = ArrayList()
        navView = findViewById(R.id.nav_view)
        navView.setNavigationItemSelectedListener(this)
        preferences = getSharedPreferences("Preferences", MODE_PRIVATE)
        cod_usuario = preferences.getString("cod_usuario", null)
        codigoEmpresa = preferences.getString("codigoEmpresa", null)
        codigoSucursal = preferences.getString("codigoSucursal", null)
        val nombreUsuario = preferences.getString("nombre_usuario", null)
        val nombreEmpresa = conn.getCampoString(
            "ke_enlace",
            "kee_nombre",
            "kee_codigo",
            codigoEmpresa ?: Constantes.CLO
        )

        fechaAuxiliar = conn.getFecha("kecxc_tasas", codigoEmpresa!!)

        objetoAux!!.descargaDesactivo(cod_usuario!!)
        SINCRONIZO = conn.sincronizoPriVez(cod_usuario!!, codigoEmpresa!!)
        DESACTIVADO = conn.getCampoInt("usuarios", "desactivo", "vendedor", cod_usuario!!) == 0
        APP_DESCUENTOS_PEDIDOS = conn.getConfigBool("APP_DESCUENTOS_PEDIDOS")
        checkForAppUpdate()
        cargarModulosActivos()
        cargarEnlace()

        windowsColor(Constantes.AGENCIA)

        carousel()

        toolbar = findViewById(R.id.toolbar_main)
        toolbar!!.setBackgroundColor(toolbar!!.colorToolBarAux(Constantes.AGENCIA))

        setSupportActionBar(toolbar)
        binding.apply {
            toggle = ActionBarDrawerToggle(this@PrincipalActivity, drawerLayout, toolbar, 0, 0)
            drawerLayout.addDrawerListener(toggle!!)
        }

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_menu_main)
        supportActionBar!!.setHomeButtonEnabled(true)

        binding.apply {
            version.text = "Ver. " + Constantes.VERSION_NAME
            tvFechaVersion.text = "Actualización " + Constantes.FECHA_VERSION
        }

        //los botones para ir a las diferentes activities
        navView.setNavigationItemSelectedListener(this)
        val headerView = navView.getHeaderView(0)
        tvNombreu = headerView.findViewById(R.id.tv_nombreu)
        val tvNomEmpresa: TextView= headerView.findViewById(R.id.tvNomEmpresa)
        llHeaderNavMenu = headerView.findViewById(R.id.llHeaderNavMenu)
        tvNombreu.text = "Bienvenid@, $nombreUsuario"
        tvNomEmpresa.text = "Empresa: $nombreEmpresa"
        descargarTasas("https://$enlaceEmpresa/webservice/tasas_V2.php?fecha_sinc=$fechaAuxiliar")
        binding.imgSyncTasa.setOnClickListener {
            descargarTasas("https://$enlaceEmpresa/webservice/tasas_V2.php?fecha_sinc=$fechaAuxiliar")
            Toast.makeText(this@PrincipalActivity, "Actualizando tasa...", Toast.LENGTH_SHORT)
                .show()
        }


        //conteoPedidosCreados();
        obtenerPermisos()
        try {
            validarSesionActiva()
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        //2023-08-04 Antigua forma de pedir permisos
        //solicitarPermisosInternos();
        //2023-08-04 Nueva forma de pedir permisos
        checkPermissions()
        //obtenerVersion("https://cloccidental.com/webservice/versionapp.php/?version_usuario=" + versionApp + "");
        this.onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
                exitProcess(0)
            }
        })

        setColors()
    }

    private fun setColors() {
        binding.apply {
            constraintLayout.setDrawableAgencia(Constantes.AGENCIA)
            imgSyncTasa.setDrawableVariantAgencia(Constantes.AGENCIA)
            imgSyncTasa.imageTintList = imgSyncTasa.colorIconReclamo(Constantes.AGENCIA)

            llHeaderNavMenu.setBackgroundResource(llHeaderNavMenu.backgroundNavMenu(Constantes.AGENCIA))
        }

    }

    private fun carousel() {
        val carousel = findViewById<ImageCarousel>(R.id.carousel)
        carousel.registerLifecycle(this.lifecycle)
        val list = conn.imgCarousel(codigoEmpresa!!)
        carousel.carouselListener = object : CarouselListener {
            override fun onCreateViewHolder(
                layoutInflater: LayoutInflater,
                parent: ViewGroup
            ): ViewBinding? {
                return null
            }

            override fun onBindViewHolder(
                binding: ViewBinding,
                item: CarouselItem,
                position: Int
            ) {
            }

            override fun onClick(position: Int, carouselItem: CarouselItem) {
                val imagen = ImageView(this@PrincipalActivity)
                val enlace = list[position].imageUrl
                //int ancho = conn.getSizeImage("ancho", position);
                //int alto = conn.getSizeImage("alto", position);
                Picasso.get().load(enlace).resize(1000, 1000).centerCrop().into(imagen)
                val ventana = AlertDialog.Builder(
                    ContextThemeWrapper(
                        this@PrincipalActivity,
                        R.style.AlertDialogCustom
                    )
                )
                ventana.setTitle("Detalles de la imagen")
                ventana.setView(imagen)
                ventana.setPositiveButton("Aceptar", null)
                val dialogo = ventana.create()
                dialogo.show()
            }

            override fun onLongClick(position: Int, carouselItem: CarouselItem) {}
        }
        carousel.setData(list)
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            //Permiso no aceptado
            requestPermisos()
        }
    }

    private fun requestPermisos() {
        if (!ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        ) {
            //Pedir permisos
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                777
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 777) { // nuestros permisos
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso Aceptado", Toast.LENGTH_SHORT).show()
            } else {
                //Toast.makeText(this, "Seleccionó el boton de \"Rechazar Permisos\"", LENGTH_LONG).show();
            }
        }
    }

    private fun cargarUtilmaTasa() {
        //descargarTasas("https://" + enlaceEmpresa + "/webservice/tasas.php?fecha_sinc=" + fecha_auxiliar.trim() + "&&agencia=" + codigoSucursal.trim());
        val keAndroid = conn.writableDatabase
        val cursor = keAndroid.rawQuery(
            "SELECT kecxc_tasa, kecxc_fchyhora FROM kecxc_tasas WHERE empresa = '$codigoEmpresa' ORDER BY kecxc_fchyhora DESC LIMIT 1",
            null
        )
        var fechaTasa: String? = ""
        val fechaTasaf: LocalDate
        if (cursor.moveToFirst()) {
            val tasa = cursor.getDouble(0)
            val decimalFormat = DecimalFormat("#,##0.00")
            val tasaFormateada = decimalFormat.format(tasa)
            fechaTasa = cursor.getString(1)
            binding.tvTasaMenup.text = tasaFormateada + "Bs."
            val objetoUtils = ObjetoUtils()
            val fechaTasaShow = objetoUtils.formatoFechaHoraShow(fechaTasa)
            binding.tvFechaTasa.text = fechaTasaShow
        } else {
            binding.tvFechaTasa.text = "0000-00-00"
            binding.tvTasaMenup.text = "0.0Bs."
        }
        cursor.close()
        val fechaActual = LocalDate.now()
        var diferencia: Int
        try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            fechaTasaf = LocalDate.parse(fechaTasa, formatter)
            diferencia = ChronoUnit.DAYS.between(fechaTasaf, fechaActual).toInt()
        } catch (e: Exception) {
            diferencia = 1
            e.printStackTrace()
        }
        if (diferencia >= 2) {
            //ib_alert.setVisibility(View.VISIBLE);
        } else {
            //ib_alert.setVisibility(View.INVISIBLE);
        }
    }

    private fun descargarTasas(url: String) {
        val keAndroid = conn.writableDatabase
        val jsonArrayRequest: JsonObjectRequest =
            object : JsonObjectRequest(url, Response.Listener { response: JSONObject ->
                if (response.getString("tasas") != "null") {
                    keAndroid.beginTransaction()
                    try {
                        val tasas = response.getJSONArray("tasas")
                        for (i in 0 until tasas.length()) {
                            val jsonObject = tasas.getJSONObject(i)

                            val id = jsonObject.getString("id")
                            val fecha = jsonObject.getString("fecha")
                            val tasa = jsonObject.getDouble("tasa")
                            val ip = jsonObject.getString("ip")
                            val fchyhora = jsonObject.getString("fechayhora")
                            val fechamod = jsonObject.getString("fechamodifi")

                            val cv = ContentValues()
                            cv.put("kecxc_id", id)
                            cv.put("kecxc_fecha", fecha)
                            cv.put("kecxc_tasa", tasa)
                            cv.put("kecxc_ip", ip)
                            cv.put("kecxc_fchyhora", fchyhora)
                            cv.put("fechamodifi", fechamod)
                            cv.put("empresa", codigoEmpresa)

                            if (conn.validarExistenciaCamposVarios(
                                    "kecxc_tasas", ArrayList(
                                        mutableListOf("kecxc_id", "empresa")
                                    ), arrayListOf(id, codigoEmpresa!!)
                                )
                            ) {
                                conn.updateJSONCamposVarios(
                                    "kecxc_tasas",
                                    cv,
                                    "kecxc_id = ? AND empresa = ?",
                                    arrayOf(id, codigoEmpresa!!)
                                )
                            } else {
                                conn.insertJSON("kecxc_tasas", cv)
                            }

                        }
                        conn.updateTablaAux("kecxc_tasas", codigoEmpresa!!)
                        llCommit = true
                        cargarUtilmaTasa()
                    } catch (exception: Exception) {
                        cargarUtilmaTasa()
                        exception.printStackTrace()
                        llCommit = false
                        if (!llCommit) return@Listener
                    }
                    if (llCommit) {
                        keAndroid.setTransactionSuccessful()
                        keAndroid.endTransaction()
                    } else if (!llCommit) {
                        keAndroid.endTransaction()
                    }
                }else{
                    cargarUtilmaTasa()
                }
            }, Response.ErrorListener { error: VolleyError ->
                cargarUtilmaTasa()
                println("--Error--")
                error.printStackTrace()
                println("--Error--")
            }) {
                override fun getParams(): Map<String, String>? {
                    val parametros: MutableMap<String, String> = HashMap()
                    parametros["cod_usuario"] = cod_usuario!!
                    return parametros
                }
            }
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(jsonArrayRequest)
    }

    /* private void colocarImagen() {

        String enlace = "https://"+ enlaceEmpresa +"/img/app_main_menu.jpg";
        Picasso.get().load(enlace).resize(500,350).centerCrop().into(img_principal);

        String enlaceCom = "https://" + enlaceEmpresa + "/img/app_img_com.jpg";
        Picasso.get().load(enlaceCom).resize(500,400).centerCrop().into(img_comunicados);
    }*/
    private fun cargarModulosActivos() {
        val keAndroid = conn.writableDatabase
        //Cursor cursor = ke_android.rawQuery("SELECT kmo_codigo FROM ke_modulos WHERE kmo_status = '1' AND ked_codigo='" + codigoEmpresa + "'", null);
        val cursor = keAndroid.rawQuery(
            "SELECT cnfg_idconfig FROM ke_wcnf_conf WHERE cnfg_clase = 'M' AND cnfg_valsino = '1' AND empresa = '$codigoEmpresa';",
            null
        )
        while (cursor.moveToNext()) {
            permisos!!.add(cursor.getString(0))
        }
        for (i in permisos!!.indices) {
            when (permisos!![i]) {
                "APP_MODULO_CLIENTES" -> if (!conn.getConfigBoolUsuario(
                        "APP_MODULO_CLIENTES_USER",
                        cod_usuario!!,
                        codigoEmpresa!!
                    ) && SINCRONIZO && DESACTIVADO
                ) {
                    navView.menu.getItem(0).isVisible = true
                }

                "APP_MODULO_CATALOGO" -> if (!conn.getConfigBoolUsuario(
                        "APP_MODULO_CATALOGO_USER",
                        cod_usuario!!,
                        codigoEmpresa!!
                    ) && SINCRONIZO && DESACTIVADO
                ) {
                    navView.menu.getItem(1).isVisible = true
                    navView.menu.getItem(2).isVisible = true
                    navView.menu.getItem(3).isVisible = true
                }

                "APP_MODULO_PEDIDO" -> if (!conn.getConfigBoolUsuario(
                        "APP_MODULO_PEDIDO_USER",
                        cod_usuario!!,
                        codigoEmpresa!!
                    ) && SINCRONIZO && DESACTIVADO
                ) {
                    navView.menu.getItem(4).isVisible = true
                }

                "APP_MODULO_CXC_OLD" ->                     //cobranzas viejas
                    if (!conn.getConfigBoolUsuario(
                            "APP_MODULO_CXC_OLD_USER",
                            cod_usuario!!,
                            codigoEmpresa!!
                        ) && SINCRONIZO && DESACTIVADO
                    ) {
                        navView.menu.getItem(7).isVisible = true
                    }

                "APP_MODULO_CXC" ->                     //navView.getMenu().getItem(4).setVisible(true);

                    //cobranzas nuevas
                    if (!conn.getConfigBoolUsuario(
                            "APP_MODULO_CXC_USER",
                            cod_usuario!!,
                            codigoEmpresa!!
                        ) && SINCRONIZO && DESACTIVADO
                    ) {
                        navView.menu.getItem(8).isVisible = true
                        navView.menu.getItem(9).isVisible = true
                    }

                "APP_MODULO_RETEN" -> {
                    if (!conn.getConfigBoolUsuario(
                            "APP_MODULO_RETEN_USER",
                            cod_usuario!!,
                            codigoEmpresa!!
                        ) && SINCRONIZO && DESACTIVADO
                    ) {
                        navView.menu.getItem(10).isVisible = true
                    }
                    if (!conn.getConfigBoolUsuario(
                            "APP_MODULO_ESTADISTICA_USER",
                            cod_usuario!!,
                            codigoEmpresa!!
                        ) && SINCRONIZO && DESACTIVADO
                    ) {
                        navView.menu.getItem(5).isVisible = true
                    }
                }

                "APP_MODULO_ESTADISTICA" -> if (!conn.getConfigBoolUsuario(
                        "APP_MODULO_ESTADISTICA_USER",
                        cod_usuario!!,
                        codigoEmpresa!!
                    ) && SINCRONIZO && DESACTIVADO
                ) {
                    navView.menu.getItem(5).isVisible = true
                }

                "APP_MODULO_RECLAMO" -> if (!conn.getConfigBoolUsuario(
                        "APP_MODULO_RECLAMO_USER",
                        cod_usuario!!,
                        codigoEmpresa!!
                    ) && SINCRONIZO && DESACTIVADO
                ) {
                    navView.menu.getItem(6).isVisible = true
                }
            }
        }
        cursor.close()
    }

    private fun cargarEnlace() {
        val keAndroid = conn.writableDatabase
        val columnas = arrayOf("kee_nombre," + "kee_url," + "kee_sucursal")
        val cursor = keAndroid.query(
            "ke_enlace",
            columnas,
            "kee_codigo ='$codigoEmpresa'",
            null,
            null,
            null,
            null
        )
        while (cursor.moveToNext()) {
            //cargo los datos de la empresa
            nombreEmpresa = cursor.getString(0)
            enlaceEmpresa = cursor.getString(1)
            //sucursalEmpresa = cursor.getString(2);
        }
        cursor.close()
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
    private fun obtenerVersion(url: String?) {
        val jsonArrayRequest: JsonArrayRequest =
            object : JsonArrayRequest(url, { response: JSONArray? ->
                if (response != null) {
                    val jsonObject: JSONObject //creamos un objeto json vacio
                    try {
                        jsonObject = response.getJSONObject(0)
                        versionNube = jsonObject.getString("kve_version").trim { it <= ' ' }
                        caducidad = jsonObject.getString("kve_activa")
                        if (versionNube != versionApp) {
                            Toast.makeText(
                                this@PrincipalActivity,
                                "Esta versión se encuentra obsoleta, por favor, actualice",
                                Toast.LENGTH_LONG
                            ).show()
                            cerrarsesion()
                        } else if (versionNube == versionApp) {
                            if (caducidad == "0") {
                                Toast.makeText(
                                    this@PrincipalActivity,
                                    "Esta versión se encuentra obsoleta, por favor, actualice",
                                    Toast.LENGTH_LONG
                                ).show()
                                cerrarsesion()
                            } else if (caducidad == "1") {
                                //TODO EN ORDEN, NO TO CAMOS NADA.
                            }
                        }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        Toast.makeText(
                            this@PrincipalActivity,
                            "Esta versión se encuentra obsoleta, por favor, actualice",
                            Toast.LENGTH_LONG
                        ).show()
                        cerrarsesion()
                    }
                } else {
                    Toast.makeText(
                        this@PrincipalActivity,
                        "Esta versión se encuentra obsoleta, por favor, actualice",
                        Toast.LENGTH_LONG
                    ).show()
                    cerrarsesion()
                }
            }, Response.ErrorListener { error: VolleyError ->
                println("--Error--")
                error.printStackTrace()
                println("--Error--")
            }) {
                override fun getParams(): Map<String, String>? {  //finalmente, estos son los parametros que le enviaremos al webservice, partiendo de las variables
                    //donde estan guardados el usuario y password.
                    //parametros.put("version_usuario", versionApp);
                    return HashMap()
                }
            }
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(jsonArrayRequest) //esto es el request que se envia al url a traves de la conexion volley, (el stringrequest esta armado arriba)
    }

    private fun solicitarPermisosInternos() {
        val permisoAlmacenamiento =
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (permisoAlmacenamiento == PackageManager.PERMISSION_GRANTED) {
            //DO NOTHING
        } else {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CODE)
        }

        /*if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){

        }else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
        }*/
    }

    private fun iraCobranzas() {
        val intent = Intent(applicationContext, CobranzasActivity::class.java)
        intent.putExtra("cod_usuario", cod_usuario)
        startActivity(intent)
    }

    private fun iraClientes() {
        val intent = Intent(applicationContext, ClientesActivity::class.java)
        intent.putExtra("cod_usuario", cod_usuario)
        intent.putExtra("codigoEmpresa", codigoEmpresa)
        startActivity(intent)
    }

    private fun iraCXC() {
        val intent = Intent(applicationContext, CXCActivity::class.java)
        intent.putExtra("cod_usuario", cod_usuario)
        intent.putExtra("codigoEmpresa", codigoEmpresa)
        startActivity(intent)
    }

    private fun iraModuloCXC() {

        /*SharedPreferences.Editor editor = preferences.edit();
        editor.putString("cod_usuario", cod_usuario);
        editor.putString("origin", "CXC");
        editor.apply();*/
        val intent = Intent(applicationContext, ModuloCXCActivity::class.java)
        intent.putExtra("cod_usuario", cod_usuario)
        startActivity(intent)
    }

    private fun iraKardex() {
        val intent = Intent(applicationContext, KardexActivity::class.java)
        intent.putExtra("cod_usuario", cod_usuario)
        startActivity(intent)
    }

    private fun iraPedidos() {
        obtenerPermisos()
        if (desactivo == 0.0) {
            val intent = Intent(applicationContext, PedidosActivity::class.java)
            intent.putExtra("cod_usuario", cod_usuario)
            startActivity(intent)
        } else if (desactivo == 1.0) {
            //Toast.makeText(getApplicationContext(), "Usuario bloqueado", Toast.LENGTH_LONG).show();
        }
    }

    private fun iraSync() {
        val intent = Intent(applicationContext, SincronizacionActivity::class.java)
        //Intent intent = new Intent(getApplicationContext(), SincronizarActivity.class);
        startActivity(intent)
    }

    private fun iraEstadisticas() {
        val intent = Intent(applicationContext, EstadisticasActivity::class.java)
        startActivity(intent)
    }

    //este es el metodo para cerrar sesion
    private fun cerrarsesion() {
        conn.deleteAll("usuarios")
        objetoAux!!.login(cod_usuario!!, 0)
        val preferences = getSharedPreferences("Preferences", MODE_PRIVATE)
        preferences.edit().clear().apply()
        val intent = Intent(applicationContext, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    //este es el metodo para ir al catalogo
    private fun iraCatalogo() {
        val intent = Intent(applicationContext, CatalogoActivity::class.java)
        val seleccion = 1
        intent.putExtra("Seleccion", seleccion)
        startActivity(intent)
    }

    private fun obtenerPermisos() {
        val keAndroid = conn.writableDatabase
        val cursor = keAndroid.rawQuery(
            "SELECT desactivo FROM usuarios  WHERE vendedor = '$cod_usuario' AND empresa = '$codigoEmpresa'",
            null
        )
        while (cursor.moveToNext()) {
            desactivo = cursor.getDouble(0)
        }
        cursor.close()
    }

    @Throws(ParseException::class)
    private fun validarSesionActiva() {
        val keAndroid = conn.writableDatabase
        val cursor = keAndroid.rawQuery(
            "SELECT desactivo FROM usuarios  WHERE vendedor ='$cod_usuario' AND empresa = '$codigoEmpresa'",
            null
        )
        while (cursor.moveToNext()) {
            statusDelUsuario = cursor.getDouble(0)
        }
        cursor.close()
        if (statusDelUsuario == 2.0) {
            cerrarsesion()
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

    override fun onResume() {
        //conteoPedidosCreados();
        permisos = ArrayList()
        cargarEnlace()
        cargarModulosActivos()
        preferences = getSharedPreferences("Preferences", MODE_PRIVATE)
        cod_usuario = preferences.getString("cod_usuario", null)
        objetoAux!!.descargaDesactivo(cod_usuario!!)
        obtenerPermisos()
        try {
            validarSesionActiva()
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        super.onResume()
        obtenerVersion("https://cloccidental.com/webservice/versionapp.php?version_usuario=$versionApp")
        validarUpInApp()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.icclientes -> {
                iraClientes()
                return true
            }

            R.id.iccatalogo -> {
                if (pedidoBloq()) {
                    iraCatalogo()
                } else {
                    pedidosDialogAlert()
                }
                return true
            }

            R.id.icpedidos -> {
                println("Los permisos -> $permisos")

                //IF que se ayuda de la Funcion PedidoBloq() que valida la fecha de la ultima vez que sincroonizo el vendedor
                //En caso de ser true el vendedor puede hacer pedido
                //En caso de ser false se le notificara al vendedor por una alerta
                if (pedidoBloq()) {
                    iraPedidos()
                } else {
                    pedidosDialogAlert()
                }
                return true
            }

            R.id.icestadistica -> {
                iraEstadisticas()
                return true
            }

            R.id.iccobranzas -> {
                iraCobranzas()
                return true
            }

            R.id.icreclamos -> {
                iraReclamos()
                return true
            }

            R.id.icCXC -> {
                iraCXC()
                return true
            }

            R.id.ickardex -> {
                iraKardex()
                return true
            }

            R.id.icsync -> {
                iraSync()
                return true
            }

            R.id.iccerrarsesion -> {
                cerrarsesion()
                return true
            }

            R.id.moduloCXC -> {
                iraModuloCXC()
                return true
            }

            R.id.moduloReten -> {
                iraReten()
                return true
            }

            R.id.moduloPromo -> {
                irAPromo()
                return true
            }

            R.id.cambiarSesion -> {
                cambioSesion()
                return true
            }
        }
        return false
    }

    private fun cambioSesion() {
        val dialog = DialogChangeAccount(this) { recargarEmpresa() }
        dialog.show()
    }

    private fun recargarEmpresa() {
        finish()
        startActivity(intent)
    }

    private fun irAPromo() {
        val intent = Intent(applicationContext, PromocionesActivity::class.java)
        val seleccion = 1
        intent.putExtra("Seleccion", seleccion)
        startActivity(intent)
    }

    private fun iraReten() {
        //Intent intent = new Intent(getApplicationContext(), SelectorClienteReten.class);
        val intent = Intent(applicationContext, ModuloRetenActivity::class.java)
        intent.putExtra("cod_usuario", cod_usuario)
        startActivity(intent)
    }

    private fun iraPlanificador() {
        val intent = Intent(applicationContext, PlanificadorActivity::class.java)
        intent.putExtra("cod_usuario", cod_usuario)
        startActivity(intent)
    }

    private fun iraReclamos() {
        val intent = Intent(applicationContext, ListaReclamosActivity::class.java)
        intent.putExtra("cod_usuario", cod_usuario)
        startActivity(intent)
    }

    override fun onPointerCaptureChanged(hasCapture: Boolean) {}

    //Funcion que crea y muestra el DialogAlert
    private fun pedidosDialogAlert() {
        AlertDialog.Builder(ContextThemeWrapper(this@PrincipalActivity, R.style.AlertDialogCustom))
            .setTitle("Alerta")
            .setMessage("Artículos desactualizados. Por favor diríjase a \"Sincronizar Datos\"")
            .setNegativeButton(android.R.string.ok, null).setIcon(android.R.drawable.presence_busy)
            .show()
    }

    //Funcion que valida el tiempo que lleva el vendedor sin sincroonizar
    private fun pedidoBloq(): Boolean {
        //Ejecucion de la seleccion de la fechas mas actual dentro de articulo
        val keAndroid = conn.writableDatabase
        val cursor = keAndroid.rawQuery(
            "SELECT ult_sinc FROM usuarios WHERE vendedor = '$cod_usuario' AND empresa = '$codigoEmpresa'",
            null
        )

        //Declaracion de variables
        var fechamodifi: String? = null
        val fechaActual = LocalDate.now()
        val fechamodifi2: LocalDate
        var diferencia: Int
        if (cursor.moveToFirst()) {
            fechamodifi = cursor.getString(0)
        }
        cursor.close()


        //TRY para el guardado, formateo y comparacion de las fechas (Se crea para los casos en los que el vendedor nunca ha sincroonizado)
        try {
            fechamodifi2 = LocalDate.parse(fechamodifi, DateTimeFormatter.ISO_LOCAL_DATE)
            diferencia = ChronoUnit.DAYS.between(fechamodifi2, fechaActual).toInt()
            //CATCH para el alor de diferencia en caso de error
        } catch (e: Exception) {
            diferencia = 3
        }
        //System.out.println(fechamodifi2);
        //System.out.println(fecha_actual);
        //System.out.println("hola" + diferencia);

        //IF que valida con ayuda de diferencia (Variable que guarda la resta entre la ultima fecha en la base de datos y la actual del tlf) si la fecha en mayor a 2
        //En caso de ser mayor a 2 envia false impidiendo el paso a pedido
        return diferencia <= 2
    }

    //  -------------------------------------------------------------------------------------------------------------------

    private fun checkForAppUpdate() {
        appUpdateManager = AppUpdateManagerFactory.create(this)
        val appUpdateInfoTask = appUpdateManager!!.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo: AppUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {

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
                    println("ACTUALIZACION")
                    appUpdateManager!!.startUpdateFlowForResult( // Pass the intent that is returned by 'getAppUpdateInfo()'.
                        appUpdateInfo,  // an activity result launcher registered via registerForActivityResult
                        AppUpdateType.IMMEDIATE,  // Or pass 'AppUpdateType.FLEXIBLE' to newBuilder() for
                        // flexible updates.
                        this,
                        MY_REQUEST_CODE
                    )
                } catch (e: SendIntentException) {
                    throw RuntimeException(e)
                }
            } else {
                println("NO ACTUALIZACION")
            }
        }
    }

    private fun validarUpInApp() {
        appUpdateManager
            ?.appUpdateInfo
            ?.addOnSuccessListener { appUpdateInfo: AppUpdateInfo ->
                if (appUpdateInfo.updateAvailability()
                    == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
                ) {
                    try {
                        appUpdateManager!!.startUpdateFlowForResult(
                            appUpdateInfo,
                            AppUpdateType.IMMEDIATE,
                            this,
                            MY_REQUEST_CODE
                        )
                    } catch (e: SendIntentException) {
                        throw RuntimeException(e)
                    }
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MY_REQUEST_CODE) {
            when (resultCode) {
                RESULT_OK -> {
                    Toast.makeText(this, "Actualización Exitosa!", Toast.LENGTH_SHORT).show()
                }

                RESULT_CANCELED -> {
                    Toast.makeText(this, "Actualización Cancelada", Toast.LENGTH_SHORT).show()
                    finish()
                    exitProcess(0)
                }

                ActivityResult.RESULT_IN_APP_UPDATE_FAILED -> {
                    Toast.makeText(this, "Algo salio mal", Toast.LENGTH_SHORT).show()
                }

                else -> {
                    Toast.makeText(this, "Algo salio mal, sin datos", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun getTheme(): Theme {
        val theme = super.getTheme()
        theme.applyStyle(setThemeNoBarAgencia(Constantes.AGENCIA), true)
        // you could also use a switch if you have many themes that could apply
        return theme
    }

    companion object {
        private const val MY_REQUEST_CODE = 100

        @JvmField
        var cod_usuario: String? = null
        var desactivo = 0.0
        var statusDelUsuario = 0.0
        var versionApp = "2.3.3"
        var version: String? = null
        var caducidad: String? = null
        var versionNube: String? = null
        var codigoEmpresa: String? = ""
        var nombreEmpresa = ""
        var enlaceEmpresa = ""
        var codigoSucursal: String? = ""
        var instance: PrincipalActivity? = null

    }
}