package com.appcloos.mimaletin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;

public class PedidosArchivadosActivity extends AppCompatActivity {

    ListView lv_archivados;
    AdminSQLiteOpenHelper conn;
    SQLiteDatabase ke_android;
    private SharedPreferences preferences;
    private static String cod_usuario;
    ArrayList<Pedidos> listapedidos;
    PedidosArchivadosAdapter pedidosArchivadosAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedidos_archivados);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //mantener la orientacion vertical

        conn = new AdminSQLiteOpenHelper(getApplicationContext(), "ke_android", null, 12);
        ke_android = conn.getWritableDatabase();
        lv_archivados = findViewById(R.id.lv_archivados);

        preferences    = getSharedPreferences("Preferences", MODE_PRIVATE);
        cod_usuario    = preferences.getString("cod_usuario", null);

        cargarPedidosArchivados(cod_usuario);


    }

    private void cargarPedidosArchivados(String cod_usuario) {
        listapedidos = new ArrayList<Pedidos>();
        String tabla = "ke_opti";
        String [] consulta = new String[]{
                        "kti_ndoc," +
                        "kti_nombrecli," +
                        "kti_totneto," +
                        "kti_nroped," +
                        "datetime('now','start of month','-1 month') as principiomes," +
                        "datetime('now','start of month','-1 day') as finalmes"

        };
        String condicion = "kti_codven = '" + cod_usuario + "' AND (kti_status = '4' OR kti_status = '5') AND kti_fchdoc BETWEEN principiomes AND finalmes";

        Cursor cursor = ke_android.query(tabla, consulta, condicion, null, null, null, null);

        while (cursor.moveToNext()){
            Pedidos pedidos = new Pedidos();
            pedidos.setNumeroDocumento(cursor.getString(0));
            pedidos.setNombreCliente(cursor.getString(1));
            pedidos.setTotalNeto(cursor.getDouble(2));
            pedidos.setNumeroPedido(cursor.getString(3));
            listapedidos.add(pedidos);

        }

        if(listapedidos != null){
            pedidosArchivadosAdapter = new PedidosArchivadosAdapter(PedidosArchivadosActivity.this, listapedidos);
            lv_archivados.setAdapter(pedidosArchivadosAdapter);
            pedidosArchivadosAdapter.notifyDataSetChanged();
        }else{
            //en caso de que no llegue
            System.out.println("pedidos vacios");
        }
    }



}