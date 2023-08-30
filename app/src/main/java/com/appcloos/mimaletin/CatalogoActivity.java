/*Clase....: CatalogoActivity
 * Autor.......: PCV MAR 2021
 * Objetivo....: Mostrar los articulos como catalogo o para eleccion al momento de crear
 *               el pedido
 *
 * Notas.......:
 *
 * Parámetros..: ActDirec : segun el valor, mostrara el catalogo o en modo de eleccion
 *
 * Modif.......:
 *
 * NOTAS.......: OJO con los procesos y las transacciones a la hora de guardar el articulo
 *
 * Retorna.....: Ninguno
 *-------------**/

package com.appcloos.mimaletin;

import android.app.ActionBar;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static com.appcloos.mimaletin.PrincipalActivity.cod_usuario;


public class CatalogoActivity extends AppCompatActivity  {

    private int APP_ITEMS_FACTURAS, APP_ITEMS_NOTAS_ENTREGA;
    ListView listaArticulos;
    ArrayList seleccionArticulo = new ArrayList();
    ArrayList<String> listainfo;
    ArrayList<Catalogo> listacatalogo;
    AdminSQLiteOpenHelper conn;
    private CatalogoAdapter catalogoAdapter;
    int seleccionado;
    Intent intent;
    String existencia_guardar = "";
    public static String  tipoDePrecioaMostrar, preciomostrar = "precio1", cod_cliente, nroPedido, nombreEmpresa, enlaceEmpresa, enpreventa = "0";
    public static Double precioTotalporArticulo, vtaMin, vtaMax,dctonumerico, stotdcto;
    int cantidad = 0, mostrarMinimo, mostrarMaximo;
    Cursor cursorca;
    SearchView buscadorarticulo;
    Boolean factura;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//mantener la activity en vertical
        setContentView(R.layout.activity_catalogo);

        //Creacion del BackButton
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /* este intent es para obtener la seleccion, tipo de precio, nro del pedido y codigo del cliente*/
        intent                 = getIntent();
        seleccionado           = intent.getIntExtra("Seleccion", 0);
        tipoDePrecioaMostrar   = intent.getStringExtra("tipoDePrecioaMostrar");
        precioTotalporArticulo = intent.getDoubleExtra("precioTotalporArticulo", 00.00);
        cod_cliente            = intent.getStringExtra("codigoCliente");
        nroPedido              = intent.getStringExtra("nroPedido");
        factura                = intent.getBooleanExtra("factura", false);
        /*importante inicializar el ayudante para la conexion, para aquellos procesos que corren al iniciar
          el activyty */
        conn = new AdminSQLiteOpenHelper(getApplicationContext(), "ke_android", null, 8);

        APP_ITEMS_FACTURAS = (int) Math.round(conn.getConfigNum("APP_ITEMS_FACTURAS"));
        APP_ITEMS_NOTAS_ENTREGA = (int) Math.round(conn.getConfigNum("APP_ITEMS_NOTAS_ENTREGA"));

        cargarEnlace();
        //declaro el listview
        listaArticulos = (ListView) findViewById(R.id.lv_articulos);
        consultarArticulosNormal(preciomostrar);//consulto los articulos

        //coloco el adaptador personalizado a la lista del elementos que van al listview
        catalogoAdapter = new CatalogoAdapter(CatalogoActivity.this, listacatalogo);
        //ArrayAdapter adaptador = new ArrayAdapter(CatalogoActivity.this, R.layout.list_catalogo_personalizado, listainfo);
        listaArticulos.setAdapter(catalogoAdapter);//refresco el listview
        listaArticulos.setTextFilterEnabled(true); // inicializo el filtro de texto



        //corro actDirect
        ActDirec();

        ObjetoAux objetoAux = new ObjetoAux(this);
        objetoAux.descargaDesactivo(cod_usuario);

    }
    //este metodo inicializa un menu con el searchview y el selector de precios   SLECT
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_catalogo,menu);
        MenuItem menuItem = menu.findItem(R.id.search_view_catalogo);

        SearchView buscador = (SearchView) MenuItemCompat.getActionView(menuItem);

        buscador.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String busqueda) {
                BuscarArticulo(busqueda);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String busqueda) {
                BuscarArticulo(busqueda);
                return false;
            }
        });


        if(seleccionado == 2){
            for(int i = 1; i < menu.size(); i++){
                menu.getItem(i).setVisible(false);
            }
        }
        return super.onCreateOptionsMenu(menu);




    }
    //y este es el selector de precios que segun la seleccion, consulta los articulos por precios
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemid = item.getItemId();

        /*if(itemid == R.id.bt_precio1){
            preciomostrar = "precio1";
            consultarArticulosNormal(preciomostrar);

            catalogoAdapter = new CatalogoAdapter(CatalogoActivity.this, listacatalogo);
            listaArticulos.setAdapter(catalogoAdapter);
            catalogoAdapter.notifyDataSetChanged(); //cada vez que se ejecute la consulta, debe refrescarse el adapter


         } else if(itemid == R.id.bt_precio2){
            preciomostrar = "precio2";
            consultarArticulosNormal(preciomostrar);

            catalogoAdapter = new CatalogoAdapter(CatalogoActivity.this, listacatalogo);
            listaArticulos.setAdapter(catalogoAdapter);
            catalogoAdapter.notifyDataSetChanged();


        } else if(itemid == R.id.bt_precio3){
            preciomostrar = "precio3";
            consultarArticulosNormal(preciomostrar);

            catalogoAdapter = new CatalogoAdapter(CatalogoActivity.this, listacatalogo);
            listaArticulos.setAdapter(catalogoAdapter);
            catalogoAdapter.notifyDataSetChanged();


        }else */ if (itemid == android.R.id.home){
            //Valida que se le da al backbutton y se regresa
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void cargarEnlace() {
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        String[] columnas = new String[]{
                "kee_nombre," +
                        "kee_url"};
        Cursor cursor = ke_android.query("ke_enlace", columnas, "1", null, null, null, null);

        while(cursor.moveToNext()){
            nombreEmpresa = cursor.getString(0);
            enlaceEmpresa = cursor.getString(1);
        }
        cursor.close();
        ke_android.close();
    }

    //metodo para ver la seleccion del activity
    public void ActDirec (){

        if (seleccionado == 2){ /*viene de pedidos, para indicar si es el catalogo de seleccion
                                o solo para mostrar los articulos*/


            consultarArticulosenPedido();

            //el listener en este caso sirve para agregar el articulo en el pedido
            listaArticulos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    final String cod_articulo = listacatalogo.get(position).getCodigo();
                    final String n_articulo   = listacatalogo.get(position).getNombre();
                    final Double precio       = listacatalogo.get(position).getPrecio1();
                    final int existencia      = listacatalogo.get(position).getExistencia();
                    final Double ventaMax     = listacatalogo.get(position).getVta_max();
                    final Double ventaMin     = listacatalogo.get(position).getVta_min();
                    final Double dctotope     = listacatalogo.get(position).getDctotope();

                    conn = new AdminSQLiteOpenHelper(getApplicationContext(), "ke_android", null, 12);
                    final SQLiteDatabase ke_android = conn.getWritableDatabase();
                    Cursor cursor_mul = ke_android.rawQuery("SELECT vta_minenx, vta_solofac, vta_solone FROM articulo WHERE codigo ='"+cod_articulo+"'", null);
                    cursor_mul.moveToFirst();
                    int vta_minenx = cursor_mul.getInt(0);
                    int vta_solofac = cursor_mul.getInt(1);
                    int vta_solone = cursor_mul.getInt(2);
                    cursor_mul.close();

                    if (vta_solofac == 1 && !factura){
                        Toast.makeText(CatalogoActivity.this, "Este articulo solo se puede agregar a Facturas", Toast.LENGTH_SHORT).show();
                        return;
                    } else if (vta_solone == 1 && factura){
                        Toast.makeText(CatalogoActivity.this, "Este articulo solo se puede agregar a Notas de Entrega", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    LinearLayout layout = new LinearLayout(CatalogoActivity.this);
                    layout.setOrientation(LinearLayout.VERTICAL);


                    Cursor cursor = ke_android.rawQuery("SELECT kmv_codart FROM ke_carrito WHERE kmv_codart ='"+cod_articulo+"'", null);

                    if(cursor.moveToFirst()){
                        Toast.makeText(CatalogoActivity.this, "El artículo ya se encuentra en el pedido",Toast.LENGTH_SHORT).show();
                    } else {

                        final EditText cajaDescuento = new EditText(new ContextThemeWrapper(CatalogoActivity.this,R.style.EditTextStyleCustom));
                        final EditText cajatexto     = new EditText(new ContextThemeWrapper(CatalogoActivity.this,R.style.EditTextStyleCustom));

                        cajaDescuento.setInputType(InputType.TYPE_CLASS_NUMBER);
                        cajatexto.setInputType(InputType.TYPE_CLASS_NUMBER);
                        //cajatexto.setFilters(new InputFilter[] {new InputFilter.LengthFilter(250)}); -- como referencia para  campos de notas

                        //un alert dialogo builder que va a servir para introducir cantidad de articulos
                        AlertDialog.Builder ventana = new AlertDialog.Builder(new ContextThemeWrapper(CatalogoActivity.this,R.style.AlertDialogCustom));
                        //declaramos textviews porque vamos a usar layout
                        final TextView titulo  = new TextView(CatalogoActivity.this);
                        final TextView mensaje = new TextView(CatalogoActivity.this);
                        final TextView montoEnPedido = new TextView(CatalogoActivity.this);
                        final TextView mensajecantidad = new TextView(CatalogoActivity.this);
                        //final TextView mensajeCantidadMultiplo = new TextView(CatalogoActivity.this);

                        //declaramos las propiedades de cada textview
                        mensaje.setTextSize(15);
                        //mensaje.setTextColor(Color.parseColor("#313131"));
                        mensajecantidad.setTextSize(15);
                        //mensajecantidad.setTextColor(Color.parseColor("#313131"));
                        montoEnPedido.setTextSize(15);
                        //montoEnPedido.setTextColor(Color.parseColor("#313131"));
                        titulo.setText("Selección del artículo");
                        //titulo.setTextColor(Color.parseColor("#313131"));
                        titulo.setTextSize(22);
                        titulo.setTypeface(null, Typeface.BOLD);


                        layout.addView(titulo);

                        //si el articulo no posee descuento, entonces escondo la opción y asumo que es 0 el descuento
                        if(dctotope == 0){
                            mensaje.setVisibility(View.INVISIBLE);
                            cajaDescuento.setVisibility(View.INVISIBLE);
                            dctonumerico = 0.00;

                        } else{
                            //de resto, permito elegir el descuento
                            mensaje.setText("Porfavor, elige el descuento");
                            layout.addView(mensaje);
                            layout.addView(cajaDescuento);

                        }


                        mensajecantidad.setText("Porfavor, elige la cantidad");

                        Cursor cursor_preTotal = ke_android.rawQuery("SELECT SUM(kmv_stotdcto) FROM ke_carrito", null);
                        cursor_preTotal.moveToFirst();
                        precioTotalporArticulo = cursor_preTotal.getDouble(0);
                        cursor_preTotal.close();
                        precioTotalporArticulo = Math.round(precioTotalporArticulo * 100.00) / 100.00;

                        montoEnPedido.setText("Monto del Pedido: $" + precioTotalporArticulo + "\n"+
                                "Cantidad Disponible: " + existencia);

                        layout.addView(mensajecantidad);
                        layout.addView(montoEnPedido);

                        if (vta_minenx == 1) {
                            LinearLayout layout_h = new LinearLayout(CatalogoActivity.this);
                            layout_h.setOrientation(LinearLayout.HORIZONTAL);

                            final TextView mensajeCantidadMultiplo = new TextView(CatalogoActivity.this);
                            final TextView mensajeMultiplo = new TextView(CatalogoActivity.this);
                            mensajeMultiplo.setTextSize(15);
                            //mensajeMultiplo.setTextColor(Color.parseColor("#313131"));
                            mensajeMultiplo.setText("Cantidad de paquetes: " + ((int) Math.floor(existencia/ventaMin)));


                            //LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
                            //params.weight = 1.0f;

                            //LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
                            //params2.weight = 50.0f;

                            mensajeCantidadMultiplo.setTextSize(20);
                            mensajeCantidadMultiplo.setTypeface(null, Typeface.BOLD);
                            //mensajeCantidadMultiplo.setTextColor(Color.parseColor("#313131"));
                            mensajeCantidadMultiplo.setText(((int) Math.round(ventaMin)) + " x ");
                            //mensajeCantidadMultiplo.setLayoutParams(params);
                            cajatexto.setWidth(1000);
                            cajatexto.setHint("Cantidad de paquetes a pedir");
                            layout_h.addView(mensajeCantidadMultiplo);
                            //cajatexto.setLayoutParams(params2);
                            layout_h.addView(cajatexto);
                            layout.addView(mensajeMultiplo);
                            layout.addView(layout_h);
                        }else{
                            layout.addView(cajatexto);
                        }
                        ventana.setView(layout);

                        //el boton procesar que servira para procesar el agregado del articulo
                        ventana.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            @RequiresApi(api = Build.VERSION_CODES.O)
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                //validadciones para procesar
                                if (cajatexto.getText().toString().isEmpty()) {
                                    Toast.makeText(CatalogoActivity.this, "Debes agregar una cantidad", Toast.LENGTH_SHORT).show();
                                } else {
                                    existencia_guardar = cajatexto.getText().toString();
                                    cantidad = Integer.parseInt(existencia_guardar);

                                    if (cantidad <= existencia && cantidad > 0) {
                                        //si la cantidad de venta maxima es mayor a 0, debo hacer validadciones adicionales
                                        if(ventaMax > 0){
                                            if(cantidad > ventaMax){
                                                Toast.makeText(CatalogoActivity.this, "La cantidad solicitada no puede ser mayor a la cantidad de máxima de Venta", Toast.LENGTH_LONG).show();
                                            } else if (cantidad <= ventaMax) {
                                                if(cantidad > ventaMin){
                                                    int comprobacion = consultarDisponibilidad(cod_usuario, cod_cliente, cod_articulo);
                                                    int exisHist = comprobacion + cantidad;

                                                    System.out.println("ESTA ES LA EXISTENCIA HISTORICA" + exisHist);
                                                    if((exisHist) > ventaMax){
                                                        Toast.makeText(CatalogoActivity.this, "Este artículo ha superado la cantidad máxima para este cliente", Toast.LENGTH_LONG).show();
                                                    }else {

                                                        LocalDateTime hoy = LocalDateTime.now(); //el dia en que se hizo el grabado
                                                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");
                                                        LocalDateTime vencimiento = hoy.plusDays(7); //cuando se vence

                                                        //las fechas formateadas
                                                        String fecha_hoy = hoy.format(formatter);
                                                        String fecha_vence = vencimiento.format(formatter);

                                                        System.out.println(fecha_hoy);
                                                        System.out.println(fecha_vence);

                                                        String descuentoTexto =  cajaDescuento.getText().toString();

                                                        if(descuentoTexto.equals("")){
                                                            dctonumerico = 0.00;
                                                        }else{
                                                            dctonumerico = Double.parseDouble(descuentoTexto);
                                                        }

                                                        if(dctonumerico > dctotope){
                                                            Toast.makeText(CatalogoActivity.this, "El descuento introducido es inválido", Toast.LENGTH_SHORT).show();
                                                        } else {

                                                            String tracking = nroPedido;

                                                            Double precioTotal = precio * Double.valueOf(cantidad);
                                                            precioTotal = Math.round(precioTotal * 100.00) / 100.00;

                                                            if(dctonumerico > 0){
                                                                stotdcto = precioTotal - (precioTotal * (dctonumerico/100));
                                                            } else{
                                                                stotdcto = precioTotal;
                                                            }

                                                            ke_android.beginTransaction();
                                                            try {
                                                                ContentValues insertar = new ContentValues();

                                                                insertar.put("kmv_codart", cod_articulo);
                                                                insertar.put("kmv_nombre", n_articulo);
                                                                insertar.put("kmv_stot", precioTotal);
                                                                insertar.put("kmv_cant", cantidad);
                                                                insertar.put("kmv_artprec", precio);
                                                                insertar.put("kmv_dctolin", dctonumerico);
                                                                insertar.put("kmv_stotdcto", stotdcto);

                                                                ke_android.insert("ke_carrito", null, insertar);

                                                                //llamo al metodo guardar limites si el articulo posee limites
                                                                guardarLimite(tracking, cod_usuario, cod_cliente, cod_articulo, cantidad, fecha_hoy, fecha_vence, "0");
                                                                ke_android.setTransactionSuccessful();
                                                                ke_android.endTransaction();
                                                                Toast.makeText(CatalogoActivity.this, "Artículo añadido", Toast.LENGTH_SHORT).show();
                                                                //finish();
                                                            } catch (Exception ex) {
                                                                System.out.println(ex);
                                                                ke_android.endTransaction();

                                                            }
                                                        }

                                                    }

                                                } else{
                                                    Toast.makeText(CatalogoActivity.this, "Debe cumplir con la cantidad mínima", Toast.LENGTH_LONG).show();
                                                }
                                            }

                                            //de no tener un limite de venta maxima, sigo y guardo el articulo.
                                        } else if (ventaMin > 0){
                                            if (vta_minenx == 1) {
                                                System.out.println("hola " + cantidad * ventaMin + " " + existencia);
                                                if (cantidad * ventaMin > existencia) {
                                                    Toast.makeText(CatalogoActivity.this, "Debe de elegir una cantidad dentro de la existencia", Toast.LENGTH_LONG).show();
                                                }else if (cantidad * ventaMin <= existencia){
                                                    int cantidad_new = (int) (cantidad * ventaMin);
                                                    System.out.println("Nueva cantidad " + cantidad_new);
                                                    Double precioTotal = precio * cantidad_new;
                                                    System.out.println("Precio total: " + precioTotal);
                                                    precioTotal = Math.round(precioTotal * 100.00) / 100.00;


                                                    String descuentoTexto = cajaDescuento.getText().toString();

                                                    if (descuentoTexto.equals("")) {
                                                        dctonumerico = 0.00;
                                                    } else {
                                                        dctonumerico = Double.parseDouble(descuentoTexto);
                                                    }

                                                    if (dctonumerico > dctotope) {
                                                        Toast.makeText(CatalogoActivity.this, "El descuento introducido es inválido", Toast.LENGTH_SHORT).show();
                                                    } else{

                                                        if(dctonumerico > 0){
                                                            stotdcto = precioTotal - (precioTotal * (dctonumerico/100));
                                                        } else{
                                                            stotdcto = precioTotal;
                                                        }

                                                        ke_android.beginTransaction();
                                                        try {
                                                            ContentValues insertar = new ContentValues();

                                                            insertar.put("kmv_codart", cod_articulo);
                                                            insertar.put("kmv_nombre", n_articulo);
                                                            insertar.put("kmv_stot", precioTotal);
                                                            insertar.put("kmv_cant", cantidad_new);
                                                            insertar.put("kmv_artprec", precio);
                                                            insertar.put("kmv_dctolin", dctonumerico);
                                                            insertar.put("kmv_stotdcto", stotdcto);

                                                            ke_android.insert("ke_carrito", null, insertar);
                                                            ke_android.setTransactionSuccessful();
                                                            ke_android.endTransaction();
                                                            System.out.println("Precio: "+(cantidad * ventaMin * precio));
                                                            //finish();
                                                            Toast.makeText(CatalogoActivity.this, "Artículo añadido", Toast.LENGTH_SHORT).show();
                                                        } catch (Exception ex) {
                                                            System.out.println(ex);
                                                            ke_android.endTransaction();
                                                        }
                                                    }
                                                }
                                            }else{
                                                if (cantidad < ventaMin) {
                                                    Toast.makeText(CatalogoActivity.this, "Debe cumplir con la cantidad mínima para la venta", Toast.LENGTH_LONG).show();
                                                }else if (cantidad >= ventaMin){
                                                    Double precioTotal = precio * Double.valueOf(cantidad);
                                                    precioTotal = Math.round(precioTotal * 100.00) / 100.00;


                                                    String descuentoTexto = cajaDescuento.getText().toString();

                                                    if (descuentoTexto.equals("")) {
                                                        dctonumerico = 0.00;
                                                    } else {
                                                        dctonumerico = Double.parseDouble(descuentoTexto);
                                                    }

                                                    if (dctonumerico > dctotope) {
                                                        Toast.makeText(CatalogoActivity.this, "El descuento introducido es inválido", Toast.LENGTH_SHORT).show();
                                                    } else{

                                                        if(dctonumerico > 0){
                                                            stotdcto = precioTotal - (precioTotal * (dctonumerico/100));
                                                        } else{
                                                            stotdcto = precioTotal;
                                                        }

                                                        ke_android.beginTransaction();
                                                        try {
                                                            ContentValues insertar = new ContentValues();

                                                            insertar.put("kmv_codart", cod_articulo);
                                                            insertar.put("kmv_nombre", n_articulo);
                                                            insertar.put("kmv_stot", precioTotal);
                                                            insertar.put("kmv_cant", cantidad );
                                                            insertar.put("kmv_artprec", precio);
                                                            insertar.put("kmv_dctolin", dctonumerico);
                                                            insertar.put("kmv_stotdcto", stotdcto);

                                                            ke_android.insert("ke_carrito", null, insertar);
                                                            ke_android.setTransactionSuccessful();
                                                            ke_android.endTransaction();
                                                            System.out.println(cantidad * ventaMin);
                                                            //finish();
                                                            Toast.makeText(CatalogoActivity.this, "Artículo añadido", Toast.LENGTH_SHORT).show();
                                                        } catch (Exception ex) {
                                                            System.out.println(ex);
                                                            ke_android.endTransaction();
                                                        }
                                                    }
                                                }
                                            }
                                        } else {
                                            Double precioTotal = precio * Double.valueOf(cantidad);
                                            precioTotal = Math.round(precioTotal * 100.00) / 100.00;


                                            String descuentoTexto = cajaDescuento.getText().toString();

                                            if (descuentoTexto.equals("")) {
                                                dctonumerico = 0.00;
                                            } else {
                                                dctonumerico = Double.parseDouble(descuentoTexto);
                                            }

                                            if (dctonumerico > dctotope) {
                                                Toast.makeText(CatalogoActivity.this, "El descuento introducido es inválido", Toast.LENGTH_SHORT).show();
                                            } else{

                                                if(dctonumerico > 0){
                                                    stotdcto = precioTotal - (precioTotal * (dctonumerico/100));
                                                } else{
                                                    stotdcto = precioTotal;
                                                }

                                                ke_android.beginTransaction();
                                                try {
                                                    ContentValues insertar = new ContentValues();

                                                    insertar.put("kmv_codart", cod_articulo);
                                                    insertar.put("kmv_nombre", n_articulo);
                                                    insertar.put("kmv_stot", precioTotal);
                                                    insertar.put("kmv_cant", cantidad);
                                                    insertar.put("kmv_artprec", precio);
                                                    insertar.put("kmv_dctolin", dctonumerico);
                                                    insertar.put("kmv_stotdcto", stotdcto);

                                                    ke_android.insert("ke_carrito", null, insertar);
                                                    ke_android.setTransactionSuccessful();
                                                    ke_android.endTransaction();
                                                    Toast.makeText(CatalogoActivity.this, "Artículo añadido", Toast.LENGTH_SHORT).show();

                                                    //finish();
                                                } catch (Exception ex) {
                                                    System.out.println(ex);
                                                    ke_android.endTransaction();
                                                }
                                            }
                                        }

                                        Cursor cursorF = ke_android.rawQuery("SELECT COUNT(kmv_codart) FROM ke_carrito", null);
                                        cursorF.moveToFirst();
                                        int cantidadCarritoFac = cursorF.getInt(0);
                                        cursorF.close();
                                        System.out.println("El numero "+ cantidadCarritoFac);
                                        if ((cantidadCarritoFac > APP_ITEMS_FACTURAS) && factura) {
                                            finish();
                                        }
                                        if ((cantidadCarritoFac > APP_ITEMS_NOTAS_ENTREGA) && !factura) {
                                            finish();
                                        }
                                    } else if (cantidad > existencia || cantidad == 0) {
                                        Toast.makeText(CatalogoActivity.this, "La cantidad no puede ser mayor a la existencia o igual a 0", Toast.LENGTH_LONG).show();

                                    }


                                }

                            }

                        });


                        AlertDialog dialogo = ventana.create(); //creo el alertdialog en funcion al builder
                        dialogo.show(); // y lo muestro

                    }
                    cursor.close();


                }
            });

            //si viene desde el menu principal, entonces solo voy a mostrar el catalogo
        }else if (seleccionado == 1){
            //un listener que, por los momentos, me permitira mostrar una imagen (mas adelante se debe desarrollar la ficha)
            listaArticulos.setOnItemClickListener((parent, view, position, id) -> {

                final String cod_articulo = listacatalogo.get(position).getCodigo().trim();
                final ImageView imagen = new ImageView(CatalogoActivity.this);
                String enlace = "https://"+enlaceEmpresa+"/img/"+cod_articulo+".jpg";
                Picasso.get().load(enlace).resize(1000, 1000).centerCrop().into(imagen);

                //este builder mostrara la ficha del articulo
                AlertDialog.Builder ventana = new AlertDialog.Builder(new ContextThemeWrapper(CatalogoActivity.this,R.style.AlertDialogCustom));
                ventana.setTitle("Imagen del articulo");
                ventana.setView(imagen);
                ventana.setPositiveButton("Aceptar", null);

                AlertDialog dialogo = ventana.create();
                dialogo.show(); //

            });
            consultarArticulosNormal(preciomostrar);

        }
    }

    //este metodo permite guardar los limites de articulos que se encuentran en pedidos
    private void guardarLimite(String tracking, String cod_usuario, String cod_cliente, String cod_articulo, int cantidad, String fecha_hoy, String fecha_vence, String status) {

        SQLiteDatabase ke_android = conn.getWritableDatabase();
        ContentValues guardarArticulo = new ContentValues();
        guardarArticulo.put("kli_track", tracking);
        guardarArticulo.put("kli_codven", cod_usuario);
        guardarArticulo.put("kli_codcli", cod_cliente);
        guardarArticulo.put("kli_codart", cod_articulo);
        guardarArticulo.put("kli_cant", cantidad);
        guardarArticulo.put("kli_fechahizo", fecha_hoy);
        guardarArticulo.put("kli_fechavence", fecha_vence);
        guardarArticulo.put("status", status);
        ke_android.insert("ke_limitart", null, guardarArticulo);
    }

    //funcion para consultar la disponiblidad de un articulo según este limitado o no.
    public int consultarDisponibilidad(String cod_usuario, String cod_cliente, String cod_articulo) {
        int resultado = 0;
        SQLiteDatabase  ke_android = conn.getReadableDatabase();
        Cursor cu_comp = ke_android.rawQuery("SELECT SUM(kli_cant) FROM ke_limitart WHERE kli_codven ='"+ cod_usuario+ "' AND kli_codcli='"+ cod_cliente+ "' AND kli_codart='"+ cod_articulo+"' AND status ='1'", null);

        while(cu_comp.moveToNext()){
            resultado = cu_comp.getInt(0);
        }

        cu_comp.close();

        return resultado;
    }




    //busqueda de articulo
    public void BuscarArticulo(String busqueda){

        listaArticulos.setAdapter(null);
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        Catalogo catalogo = null;
        if (busqueda.equals("")) {
            //Toast.makeText(CatalogoActivity.this, "Debes introducir una palabra o código", Toast.LENGTH_SHORT).show();
        } else{


            listacatalogo = new ArrayList<Catalogo>();
            // System.out.println("IMPRIMIENDO EL NOMBRE " + busqueda);



            if(seleccionado == 2){
                enpreventa             = intent.getStringExtra("enpreventa");
                if(enpreventa.equals("0")){
                    cursorca = ke_android.rawQuery("select articulo.codigo, articulo.nombre, articulo."+ tipoDePrecioaMostrar +", articulo.existencia - articulo.comprometido, articulo.fechamodifi, ke_kardex.kde_codart, articulo.vta_min, articulo.vta_max, articulo.dctotope, articulo.enpreventa, articulo.vta_minenx, articulo.vta_solofac, articulo.vta_solone FROM articulo LEFT JOIN  ke_kardex ON articulo.codigo = ke_kardex.kde_codart WHERE (existencia - comprometido) > 0 and (nombre LIKE '%" + busqueda + "%' OR codigo LIKE'%" + busqueda + "%') and "+ tipoDePrecioaMostrar +" > 0.00 AND discont = 0.0 AND enpreventa != '1' ORDER BY articulo.codigo ASC", null);
                } else if (enpreventa.equals("1")){
                    cursorca = ke_android.rawQuery("select articulo.codigo, articulo.nombre, articulo."+ tipoDePrecioaMostrar +", articulo.existencia - articulo.comprometido, articulo.fechamodifi, ke_kardex.kde_codart, articulo.vta_min, articulo.vta_max, articulo.dctotope, articulo.enpreventa, articulo.vta_minenx, articulo.vta_solofac, articulo.vta_solone FROM articulo LEFT JOIN  ke_kardex ON articulo.codigo = ke_kardex.kde_codart WHERE (existencia - comprometido) > 0 and (nombre LIKE '%" + busqueda + "%' OR codigo LIKE'%" + busqueda + "%') and "+ tipoDePrecioaMostrar +" > 0.00 AND discont = 0.0 AND enpreventa = '" + enpreventa +"' ORDER BY articulo.codigo ASC", null);
                }

            } else if(seleccionado == 1){
                cursorca = ke_android.rawQuery("select articulo.codigo, articulo.nombre, articulo."+ preciomostrar +", articulo.existencia - articulo.comprometido, articulo.fechamodifi, ke_kardex.kde_codart, articulo.vta_min, articulo.vta_max, articulo.dctotope, articulo.enpreventa, articulo.vta_minenx , articulo.vta_solofac, articulo.vta_solone FROM articulo LEFT JOIN ke_kardex ON articulo.codigo = ke_kardex.kde_codart WHERE (existencia - comprometido) > 0 and (nombre LIKE '%" + busqueda + "%' OR codigo LIKE'%" + busqueda + "%') and " + preciomostrar + "> 0.00   AND discont = 0.0 ORDER BY articulo.codigo ASC", null);
            }


            while (cursorca.moveToNext()) {
                catalogo = new Catalogo();
                catalogo.setCodigo(cursorca.getString(0));
                catalogo.setNombre(cursorca.getString(1));
                Double precio = cursorca.getDouble(2);
                Double precio_rd = Math.round(precio * 100.0) / 100.0;
                catalogo.setPrecio1(precio_rd);
                Double existenc = cursorca.getDouble(3);
                int existencia_rd = existenc.intValue();
                catalogo.setExistencia(existencia_rd);
                catalogo.setCodigoKardex(cursorca.getString(5));
                catalogo.setVta_min(cursorca.getDouble(6));
                catalogo.setVta_max(cursorca.getDouble(7));
                catalogo.setDctotope(cursorca.getDouble(8));
                catalogo.setEnpreventa(cursorca.getString(9));

                catalogo.setMultiplo(cursorca.getInt(10));

                catalogo.setVta_solofac(cursorca.getInt(11));
                catalogo.setVta_solone(cursorca.getInt(12));

                vtaMin = cursorca.getDouble(6); //VARIABLE EN DOUBLE DE VTA MIN
                vtaMax = cursorca.getDouble(7); //VARIABLE EN DOUBLE DE VTA MAX


                listacatalogo.add(catalogo);

            }
            //ke_android.close();

            catalogoAdapter = new CatalogoAdapter(CatalogoActivity.this, listacatalogo);
            listaArticulos.setAdapter(catalogoAdapter);
            catalogoAdapter.notifyDataSetChanged();
        }

    }


    private void consultarArticulosNormal(String precioparametro) {
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        Catalogo catalogo = null;
        Cursor cursor = null;
        listacatalogo = new ArrayList<Catalogo>();
        enpreventa    = intent.getStringExtra("enpreventa");

        if(enpreventa == null){
            enpreventa = "0";
        }
        if(enpreventa.equals("0")){
            cursor = ke_android.rawQuery("SELECT articulo.codigo, articulo.nombre, articulo." + precioparametro + ", articulo.existencia - articulo.comprometido, articulo.fechamodifi, ke_kardex.kde_codart, articulo.vta_min, articulo.vta_max, articulo.dctotope, articulo.enpreventa, articulo.vta_minenx, articulo.vta_solofac, articulo.vta_solone  FROM articulo LEFT JOIN  ke_kardex ON articulo.codigo = ke_kardex.kde_codart WHERE (existencia - comprometido) > 0 AND discont = 0.0 ORDER BY articulo.codigo ASC", null);
        } else if (enpreventa.equals("1")){
            cursor = ke_android.rawQuery("SELECT articulo.codigo, articulo.nombre, articulo." + precioparametro + ", articulo.existencia - articulo.comprometido, articulo.fechamodifi, ke_kardex.kde_codart, articulo.vta_min, articulo.vta_max, articulo.dctotope, articulo.enpreventa, articulo.vta_minenx, articulo.vta_solofac, articulo.vta_solone  FROM articulo LEFT JOIN  ke_kardex ON articulo.codigo = ke_kardex.kde_codart WHERE (existencia - comprometido) > 0 AND discont = 0.0 AND enpreventa ='1' ORDER BY articulo.codigo ASC", null);
        }

        //select codigo, nombre from articulo
        // Cursor cursor = ke_android.rawQuery("SELECT articulo.codigo, articulo.nombre, articulo." + precioparametro + ", articulo.existencia, articulo.fechamodifi, ke_kardex.kde_codart, articulo.vta_min, articulo.vta_max, articulo.dctotope   FROM articulo LEFT JOIN  ke_kardex ON articulo.codigo = ke_kardex.kde_codart WHERE existencia > 0 AND discont = 0.0 AND enpreventa ='" + enpreventa + "'", null);
        cursor.moveToFirst();

        while(!cursor.isAfterLast()){
            catalogo = new Catalogo();
            catalogo.setCodigo(cursor.getString(0));
            catalogo.setNombre(cursor.getString(1));
            Double precio1 = cursor.getDouble(2);
            Double precio1_rd = Math.round(precio1*100.0)/100.0;
            catalogo.setPrecio1(precio1_rd);
            Double existenc = cursor.getDouble(3);
            int existencia_rd = existenc.intValue();
            catalogo.setExistencia(existencia_rd);
            catalogo.setCodigoKardex(cursor.getString(5));
            catalogo.setVta_min(cursor.getDouble(6));
            catalogo.setVta_max(cursor.getDouble(7));
            catalogo.setDctotope(cursor.getDouble(8));
            catalogo.setEnpreventa(cursor.getString(9));

            catalogo.setMultiplo(cursor.getInt(10));// <------------------------ TE QUEDASTE AQUI

            catalogo.setVta_solofac(cursor.getInt(11));
            catalogo.setVta_solone(cursor.getInt(12));

            vtaMin = cursor.getDouble(6); //VARIABLE EN DOUBLE DE VTA MIN
            vtaMax = cursor.getDouble(7); //VARIABLE EN DOUBLE DE VTA MAX
            listacatalogo.add(catalogo);
            cursor.moveToNext();
        }
        cursor.close();
        ke_android.close();


    }

    private void consultarArticulosenPedido() {
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        Catalogo catalogo = null;
        Cursor cursor = null;
        listacatalogo = new ArrayList<Catalogo>();
        enpreventa    = intent.getStringExtra("enpreventa");

        if(enpreventa == null || enpreventa.equals("")){
            enpreventa = "0";
        }
        if(enpreventa.equals("0")){
            cursor = ke_android.rawQuery("SELECT articulo.codigo, articulo.nombre, articulo." + tipoDePrecioaMostrar + ", articulo.existencia - articulo.comprometido, articulo.fechamodifi, ke_kardex.kde_codart, articulo.vta_min, articulo.vta_max, articulo.dctotope, articulo.enpreventa, articulo.vta_minenx, articulo.vta_solofac, articulo.vta_solone FROM articulo LEFT JOIN ke_kardex ON articulo.codigo = ke_kardex.kde_codart WHERE (existencia - comprometido) > 0 AND discont = 0.0 AND enpreventa = '' ORDER BY articulo.codigo ASC", null);
        }else if(enpreventa.equals("1")){
            cursor = ke_android.rawQuery("SELECT articulo.codigo, articulo.nombre, articulo." + tipoDePrecioaMostrar + ", articulo.existencia - articulo.comprometido, articulo.fechamodifi, ke_kardex.kde_codart, articulo.vta_min, articulo.vta_max, articulo.dctotope, articulo.enpreventa, articulo.vta_minenx, articulo.vta_solofac, articulo.vta_solone FROM articulo LEFT JOIN ke_kardex ON articulo.codigo = ke_kardex.kde_codart WHERE (existencia - comprometido) > 0 AND discont = 0.0 AND enpreventa ='" +enpreventa+ "' ORDER BY articulo.codigo ASC", null);
        }

        //select codigo, nombre from articulo



        while(cursor.moveToNext()){
            catalogo = new Catalogo();
            catalogo.setCodigo(cursor.getString(0));
            catalogo.setNombre(cursor.getString(1));
            Double precio1 = cursor.getDouble(2);
            Double precio1_rd = Math.round(precio1*100.0)/100.0;
            catalogo.setPrecio1(precio1_rd);
            Double existenc = cursor.getDouble(3);
            int existencia_rd = existenc.intValue();
            catalogo.setCodigoKardex(cursor.getString(5));
            catalogo.setExistencia(existencia_rd);
            catalogo.setVta_min(cursor.getDouble(6));
            catalogo.setVta_max(cursor.getDouble(7));
            catalogo.setDctotope(cursor.getDouble(8));
            catalogo.setEnpreventa(cursor.getString(9));

            catalogo.setMultiplo(cursor.getInt(10));

            catalogo.setVta_solofac(cursor.getInt(11));
            catalogo.setVta_solone(cursor.getInt(12));

            vtaMin = cursor.getDouble(6); //VARIABLE EN DOUBLE DE VTA MIN
            vtaMax = cursor.getDouble(7); //VARIABLE EN DOUBLE DE VTA MAX

            listacatalogo.add(catalogo);

        }
        cursor.close();
        ke_android.close();
        catalogoAdapter = new CatalogoAdapter(CatalogoActivity.this, listacatalogo);
        listaArticulos.setAdapter(catalogoAdapter);
        catalogoAdapter.notifyDataSetChanged();


    }





}