package com.appcloos.mimaletin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SearchView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ClientesActivity extends AppCompatActivity implements ClienteAdapter.ViewHolderDatos.onClienteListener {
    RecyclerView listaClientes;
    ArrayList<String> listainfo;
    ArrayList<Cliente> listacliente;
    AdminSQLiteOpenHelper conn;
    EditText buscarcliente;
    ImageButton bt_buscar;
    ArrayAdapter adaptador;
    public static String cod_usuario, codigoEmpresa;
    private SharedPreferences preferences;
    private ClienteAdapter clienteAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clientes);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//mantener la activity en vertical

        conn = new AdminSQLiteOpenHelper(getApplicationContext(), "ke_android", null, 1);

        //instancio el recyclerview y le coloco layout
        listaClientes = findViewById(R.id.lv_clientes);
        listaClientes.setLayoutManager(new LinearLayoutManager(this));

        preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        cod_usuario = preferences.getString("cod_usuario", null);
        codigoEmpresa = preferences.getString("codigoEmpresa", null);
        System.out.println(cod_usuario);
        consultarClientes();

        clienteAdapter = new ClienteAdapter(listacliente, this);
        listaClientes.setAdapter(clienteAdapter);
        clienteAdapter.notifyDataSetChanged();

        ObjetoAux objetoAux = new ObjetoAux(this);
        objetoAux.descargaDesactivo(cod_usuario);


    }

    private void iraDocumentos(String codigoCliente, String nombreCliente) {

        Intent intent = new Intent(getApplicationContext(), DocumentosActivity.class);
        intent.putExtra("codigoCliente", codigoCliente);
        intent.putExtra("nombreCliente", nombreCliente);
        intent.putExtra("cod_usuario", cod_usuario);
        intent.putExtra("codigoEmpresa", codigoEmpresa);
        startActivity(intent);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_search, menu);

        MenuItem menuItem = menu.findItem(R.id.search_view);

        SearchView buscador = (SearchView) MenuItemCompat.getActionView(menuItem);
        buscador.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String busqueda) {
                BuscarClientes(busqueda);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String busqueda) {
                BuscarClientes(busqueda);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    /*************************************************************************************************/

  /*  public void Onclick(View view){
       BuscarClientes();
    }*/
    public void BuscarClientes(String busqueda) {
        listaClientes.setAdapter(null);

        SQLiteDatabase ke_android = conn.getWritableDatabase();
        Cliente cliente = null;
        // String busqueda = buscador.getText().toString().trim();

        if (busqueda.equals("")) {
            consultarClientes();
        } else {

            listacliente = new ArrayList<Cliente>();
            System.out.println("IMPRIMIENDO EL NOMBRE " + busqueda);
            //select codigo, nombre from articulo
            Cursor cursor = ke_android.rawQuery("SELECT codigo, nombre, direccion FROM cliempre WHERE vendedor ='" + cod_usuario.trim() + "' AND (nombre LIKE '%" + busqueda + "%' OR codigo LIKE'%" + busqueda + "%')", null);


            while (cursor.moveToNext()) {
                cliente = new Cliente();
                cliente.setCodigo(cursor.getString(0));
                cliente.setNombre(cursor.getString(1));
                cliente.setDireccion(cursor.getString(2));


                listacliente.add(cliente);

            }
            cursor.close();
            ke_android.close();

            clienteAdapter = new ClienteAdapter(listacliente, this);
            listaClientes.setAdapter(clienteAdapter);
            clienteAdapter.notifyDataSetChanged();
        }
    }


    private void consultarClientes() {
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        Cliente cliente = null;

        listacliente = new ArrayList<Cliente>();

        //select codigo, nombre from articulo
        Cursor cursor = ke_android.rawQuery("SELECT codigo, nombre, direccion FROM cliempre WHERE vendedor ='" + cod_usuario.trim() + "'  ORDER BY nombre ASC", null);


        while (cursor.moveToNext()) {
            cliente = new Cliente();
            cliente.setCodigo(cursor.getString(0));
            cliente.setNombre(cursor.getString(1));
            cliente.setDireccion(cursor.getString(2));


            listacliente.add(cliente);

        }
        cursor.close();
        //ke_android.close();


    }


    @Override
    protected void onResume() {

        consultarClientes();
        super.onResume();
    }

    @Override
    public void onItemClick(int position) {
        final String codigoCliente = listacliente.get(position).getCodigo();
        final String nombreCliente = listacliente.get(position).getNombre();

        AlertDialog.Builder ventana = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom));
        ventana.setTitle(nombreCliente);
        ventana.setMessage("Por favor, seleccione una opción");
        ventana.setPositiveButton("Ver documentos", (dialogInterface, i) -> iraDocumentos(codigoCliente, nombreCliente));

        AlertDialog dialogo = ventana.create();
        dialogo.show();
    }
}