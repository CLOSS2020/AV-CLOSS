package com.appcloos.mimaletin;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class KardexActivity extends AppCompatActivity {
    ListView listaKardex;
    ArrayList<Catalogo> listacatalogo;
    AdminSQLiteOpenHelper conn;
    public static String tipoDePrecioaMostrar = "precio1", enlaceEmpresa = "", nombreEmpresa = "", codigoSucursal = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);//mantener la activity en vertical
        setContentView(R.layout.activity_kardex);

        SharedPreferences preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        String cod_usuario = preferences.getString("cod_usuario", null);


        conn = new AdminSQLiteOpenHelper(getApplicationContext(), "ke_android", null, 8);
        listaKardex = findViewById(R.id.lv_kardex);
        cargarEnlace();

      /*  Intent intent =  getIntent();
        tipoDePrecioaMostrar = intent.getStringExtra("tipoDePrecioaMostrar");*/

        listaKardex.setOnItemClickListener((parent, view, position, id) -> {

            final String cod_articulo = listacatalogo.get(position).getCodigo().trim();
            final ImageView imagen = new ImageView(KardexActivity.this);
            String enlace = "https://" + enlaceEmpresa + "/img/" + cod_articulo + ".jpg";
            Picasso.get().load(enlace).resize(1000, 1000).centerCrop().into(imagen);

            AlertDialog.Builder ventana = new AlertDialog.Builder(new ContextThemeWrapper(KardexActivity.this,R.style.AlertDialogCustom));
            ventana.setTitle("Imagen del articulo");
            ventana.setView(imagen);
            ventana.setPositiveButton("Aceptar", null);

            AlertDialog dialogo = ventana.create();
            dialogo.show();

        });
        consultarArticulosNormal(tipoDePrecioaMostrar);
        CatalogoAdapter catalogoAdapter = new CatalogoAdapter(KardexActivity.this, listacatalogo);
        listaKardex.setAdapter(catalogoAdapter);

        ObjetoAux objetoAux = new ObjetoAux(this);
        objetoAux.descargaDesactivo(cod_usuario);
    }


    private void consultarArticulosNormal(String precioparametro) {
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        Catalogo catalogo;

        listacatalogo = new ArrayList<>();

        Cursor cursor = ke_android.rawQuery("SELECT articulo.codigo, articulo.nombre, articulo." + tipoDePrecioaMostrar + ", articulo.existencia, articulo.fechamodifi, ke_kardex.kde_codart, articulo.vta_min, articulo.vta_max, articulo.dctotope, articulo.enpreventa FROM ke_kardex LEFT JOIN articulo ON ke_kardex.kde_codart = articulo.codigo WHERE (existencia - comprometido) > 0 AND discont = 0.0", null);


        //select codigo, nombre from articulo
        while (cursor.moveToNext()) {
            catalogo = new Catalogo();
            catalogo.setCodigo(cursor.getString(0));
            catalogo.setNombre(cursor.getString(1));
            double precio1 = cursor.getDouble(2);
            Double precio1_rd = Math.round(precio1 * 100.0) / 100.0;
            catalogo.setPrecio1(precio1_rd);
            double existenc = cursor.getDouble(3);
            int existencia_rd = (int) existenc;
            catalogo.setExistencia(existencia_rd);
            catalogo.setVta_min(cursor.getDouble(6));
            catalogo.setVta_max(cursor.getDouble(7));
            catalogo.setDctotope(cursor.getDouble(8));
            catalogo.setEnpreventa(cursor.getString(9));
            listacatalogo.add(catalogo);

        }
        cursor.close();
        ke_android.close();


    }

    private void cargarEnlace() {
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        String[] columnas = new String[]{
                "kee_nombre," +
                        "kee_url," +
                        "kee_sucursal"};
        Cursor cursor = ke_android.query("ke_enlace", columnas, "1", null, null, null, null);

        while (cursor.moveToNext()) {
            nombreEmpresa = cursor.getString(0);
            enlaceEmpresa = cursor.getString(1);
            codigoSucursal = cursor.getString(2);
        }
        cursor.close();
        ke_android.close();
    }


}