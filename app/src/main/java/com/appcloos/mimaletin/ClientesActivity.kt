package com.appcloos.mimaletin


import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.os.Bundle
import android.view.Menu
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuItemCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.appcloos.mimaletin.databinding.ActivityClientesBinding
import com.appcloos.mimaletin.model.cliente.ClientesKt

class ClientesActivity : AppCompatActivity() {
    var listainfo: ArrayList<String>? = null
    private var listacliente: ArrayList<ClientesKt> = ArrayList()
    lateinit var conn: AdminSQLiteOpenHelper
    var buscarcliente: EditText? = null
    var bt_buscar: ImageButton? = null
    var adaptador: ArrayAdapter<*>? = null
    private lateinit var preferences: SharedPreferences
    private lateinit var clienteAdapter: ClienteAdapter

    private lateinit var binding: ActivityClientesBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED //mantener la activity en vertical
        conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null)

        preferences = getSharedPreferences("Preferences", MODE_PRIVATE)
        cod_usuario = preferences.getString("cod_usuario", null)
        codigoEmpresa = preferences.getString("codigoEmpresa", null)

        //instancio el recyclerview y le coloco layout

        clienteAdapter = ClienteAdapter(listacliente) { position -> onItemClick(position) }

        buscarClientes()

        binding.rvClientes.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = clienteAdapter
        }


        val objetoAux = ObjetoAux(this)
        objetoAux.descargaDesactivo(cod_usuario!!)
    }

    /*    private fun iraDocumentos(codigoCliente: String, nombreCliente: String) {
            val intent = Intent(applicationContext, DocumentosActivity::class.java)
            intent.putExtra("codigoCliente", codigoCliente)
            intent.putExtra("nombreCliente", nombreCliente)
            intent.putExtra("cod_usuario", cod_usuario)
            intent.putExtra("codigoEmpresa", codigoEmpresa)
            startActivity(intent)
        }*/

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.menu_search, menu)
        val menuItem = menu.findItem(R.id.search_view)
        val buscador = MenuItemCompat.getActionView(menuItem) as SearchView
        buscador.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(busqueda: String): Boolean {
                buscarClientes(busqueda)
                return false
            }

            override fun onQueryTextChange(busqueda: String): Boolean {
                buscarClientes(busqueda)
                return false
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

    /** */ /*  public void Onclick(View view){
       BuscarClientes();
    }*/
    fun buscarClientes(busqueda: String? = null) {

        listacliente.clear()
        listacliente = conn.getClientes(busqueda, codigoEmpresa!!)
        clienteAdapter.updateAdapter(listacliente)
    }

    /*    private fun consultarClientes() {
            val ke_android = conn!!.writableDatabase
            var cliente: Cliente?
            listacliente = ArrayList()

            //select codigo, nombre from articulo
            val cursor =
                ke_android.rawQuery("SELECT codigo, nombre, direccion FROM cliempre WHERE vendedor ='" + cod_usuario!!.trim { it <= ' ' } + "'  ORDER BY nombre ASC",
                    null)
            while (cursor.moveToNext()) {
                cliente = Cliente()
                cliente.setCodigo(cursor.getString(0))
                cliente.setNombre(cursor.getString(1))
                cliente.setDireccion(cursor.getString(2))
                listacliente!!.add(cliente)
            }
            cursor.close()
            //ke_android.close();
        }*/

    /*    override fun onResume() {
            consultarClientes()
            super.onResume()
        }*/

    private fun onItemClick(position: Int) {
        val codigoCliente = listacliente[position].codigo
        val nombreCliente = listacliente[position].nombre

        /*AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View customView = LayoutInflater.from(this).inflate(R.layout.dialog_datos_clientes, null);
        builder.setView(customView);

        builder.create().show();*/
        val dialog = DialogClientesDatos(this, codigoCliente, nombreCliente, codigoEmpresa!!)
        dialog.show()

        /*AlertDialog.Builder ventana = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom));
        ventana.setTitle(nombreCliente);
        ventana.setMessage("Por favor, seleccione una opción");
        ventana.setPositiveButton("Ver documentos", (dialogInterface, i) -> iraDocumentos(codigoCliente, nombreCliente));*/

        /*AlertDialog dialogo = ventana.create();
        dialogo.show();*/
    }

    companion object {
        var cod_usuario: String? = null
        var codigoEmpresa: String? = null
    }

    override fun getTheme(): Resources.Theme {
        val theme = super.getTheme()
        theme.applyStyle(setThemeAgencia(Constantes.AGENCIA), true)
        // you could also use a switch if you have many themes that could apply
        return theme
    }

}