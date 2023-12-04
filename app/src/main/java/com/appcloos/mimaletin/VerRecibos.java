package com.appcloos.mimaletin;

import static android.content.Context.MODE_PRIVATE;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;


public class VerRecibos extends Fragment {
    AdminSQLiteOpenHelper conn;
    SharedPreferences preferences;
    public static String cod_usuario, pedido_estatus, codigoRecibo, codigoCliente, nombreCliente, montoRecibo, vendedorRecibo, fechaRecibo, nombreEmpresa = "", nombreVendedor, reciboNum;
    ArrayList<Recibos> listarecibos;
    RecibosAdapter recibosAdapter;
    ListView lv_verrecibos;
    //SQLiteDatabase ke_android;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    public VerRecibos() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *

     */
    // TODO: Rename and change types and number of parameters


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: Rename and change types of parameters
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View v = inflater.inflate(R.layout.fragment_ver_recibos, container, false);

        lv_verrecibos = v.findViewById(R.id.lv_verrecibos);

        preferences = requireActivity().getSharedPreferences("Preferences", MODE_PRIVATE);
        cod_usuario = preferences.getString("cod_usuario", null);

        lineasRecibo(); // ver todos los recibos guardados


        lv_verrecibos.setOnItemClickListener((adapterView, view, position, l) -> {

            codigoRecibo = listarecibos.get(position).getNroRecibo();
            codigoCliente = listarecibos.get(position).getCodigoCliente();
            nombreCliente = listarecibos.get(position).getNombreCliente();
            montoRecibo = listarecibos.get(position).getMontoRecibo();
            fechaRecibo = listarecibos.get(position).getFechaRecibo();
            vendedorRecibo = listarecibos.get(position).getCodigoVendedor();

            AlertDialog.Builder ventana = new AlertDialog.Builder(new ContextThemeWrapper(requireActivity(),R.style.AlertDialogCustom));
            ventana.setTitle("Mensaje del sistema");
            ventana.setMessage("Por favor, elige una opción");

            ventana.setPositiveButton("Generar y compartir PDF", (dialogInterface, i) -> {

                conn = new AdminSQLiteOpenHelper(requireActivity().getApplicationContext(), "ke_android", null);
                SQLiteDatabase ke_android = conn.getWritableDatabase();
                cargarEnlace();
                Cursor cursor = ke_android.rawQuery("SELECT nombre FROM listvend WHERE codigo ='" + vendedorRecibo + "'", null);
                while (cursor.moveToNext()) {
                    nombreVendedor = cursor.getString(0);
                }
                cursor.close();

                //metodo para guardar el recibo en PDF en la carpeta Documentos
                guardarRecibo(codigoRecibo, codigoCliente, nombreCliente, montoRecibo, fechaRecibo, nombreVendedor);
                abrirRecibo(reciboNum);


            });


            AlertDialog dialogo = ventana.create();
            dialogo.show();
        });

        return v;
    }

    private void abrirRecibo(String nombreArchivo) {

        String ruta = "/storage/emulated/0/Documents/" + nombreArchivo;
        File file = new File(ruta);

        if (!file.exists()) {
            Toast.makeText(requireActivity().getApplicationContext(), "Este archivo no existe o fue cambiado de lugar.", Toast.LENGTH_LONG).show();
        }
        Intent intentShare = new Intent(Intent.ACTION_SEND);
        intentShare.setType("application/pdf");
        intentShare.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(requireActivity(), requireActivity().getApplicationContext().getPackageName() + ".provider", file));
        startActivity(Intent.createChooser(intentShare, "Compartir Archivo..."));


    }

    public void lineasRecibo() {
        cargarRecibos();

        if (listarecibos != null) {
            recibosAdapter = new RecibosAdapter(getActivity(), listarecibos);
            lv_verrecibos.setAdapter(null);
            lv_verrecibos.setAdapter(recibosAdapter);
            recibosAdapter.notifyDataSetChanged();

        }
    }

    private void cargarRecibos() {

        listarecibos = new ArrayList<>();
        AdminSQLiteOpenHelper conn = new AdminSQLiteOpenHelper(requireActivity().getApplicationContext(), "ke_android", null);
        SQLiteDatabase ke_android = conn.getWritableDatabase();


        Cursor cursor = ke_android.rawQuery("SELECT kcx_nrorecibo, kcx_codcli, kcx_ncliente, kcx_monto, kcx_fechamodifi, kcx_status, kcx_codven FROM ke_cxc WHERE " +
                "kcx_codven ='" + cod_usuario + "'", null);

        while (cursor.moveToNext()) {
            String estatusEval = cursor.getString(5);

            if (estatusEval.equals("0")) {
                pedido_estatus = "Por Subir";


            } else if (estatusEval.equals("1")) {
                pedido_estatus = "Subido";

            }

            Recibos recibos = new Recibos();
            recibos.setNroRecibo(cursor.getString(0));
            recibos.setCodigoCliente(cursor.getString(1));
            recibos.setNombreCliente(cursor.getString(2));
            recibos.setMontoRecibo(String.valueOf(cursor.getInt(3)));
            recibos.setStatusRecibo(pedido_estatus);
            recibos.setFechaRecibo(cursor.getString(4));
            recibos.setCodigoVendedor(cursor.getString(6));
            listarecibos.add(recibos);
        }
        cursor.close();
        ke_android.close();
    }


    private void cargarEnlace() {
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        String[] columnas = new String[]{
                "kee_nombre," +
                        "kee_url"};
        Cursor cursor = ke_android.query("ke_enlace", columnas, "1", null, null, null, null);

        while (cursor.moveToNext()) {
            nombreEmpresa = cursor.getString(0);
            //enlaceEmpresa = cursor.getString(1);
        }
        cursor.close();
    }

    public void guardarRecibo(String codigoRecibo, String codigoCliente, String nombreCliente, String montoRecibo, String fechaRecibo, String vendedorRecibo) {


        // --- CONF DEL PDF -----
        PdfDocument pedidoPDF = new PdfDocument();
        Paint paint = new Paint();

        PdfDocument.PageInfo myInfo = new PdfDocument.PageInfo.Builder(300, 500, 1).create();
        PdfDocument.Page pagina = pedidoPDF.startPage(myInfo);
        Canvas canvas = pagina.getCanvas();

        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(12);
        //paint.setColor(Color.rgb(15, 52, 151));
        //canvas.drawRect(0, 0, 300, 120, paint);
        paint.setColor(Color.BLACK);
        paint.setTypeface(Typeface.createFromAsset(requireActivity().getAssets(), "font/arialbd.ttf"));

        //CABECERA
        //imagen del la cabecera


        Bitmap bmp = BitmapFactory.decodeResource(requireActivity().getResources(), R.drawable.plantillasello);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bmp, 300, 500, false);
        canvas.drawBitmap(scaledBitmap, 0, 0, paint);

        //titulos de la cabecera
        canvas.drawText(nombreEmpresa, 150, 60, paint);
        canvas.drawText("Recibo de Pago", 150, 80, paint);
        canvas.drawText("Estimado(s), se le ha generado", 150, 100, paint);
        canvas.drawText("un recibo de pago con los siguientes datos:", 150, 110, paint);
        canvas.drawRect(0, 130, 300, 133, paint);

        //lineas
        paint.setTextSize(12);

        paint.setColor(Color.BLACK);
        paint.setTextAlign(Paint.Align.LEFT);


        //Codigo del cliente
        paint.setTypeface(Typeface.createFromAsset(requireActivity().getAssets(), "font/arialbd.ttf"));
        canvas.drawText("Código del Cliente: ", 30, 160, paint);
        paint.setTypeface(Typeface.createFromAsset(requireActivity().getAssets(), "font/arial.ttf"));
        canvas.drawText(codigoCliente, 30, 180, paint);

        //NOMBRE DEL CLIENTE
        paint.setTypeface(Typeface.createFromAsset(requireActivity().getAssets(), "font/arialbd.ttf"));
        canvas.drawText("Cliente: ", 30, 220, paint);
        paint.setTypeface(Typeface.createFromAsset(requireActivity().getAssets(), "font/arial.ttf"));
        canvas.drawText(nombreCliente, 30, 240, paint);

        //Monto del Recibo
        paint.setTypeface(Typeface.createFromAsset(requireActivity().getAssets(), "font/arialbd.ttf"));
        canvas.drawText("Monto Pagado: ", 30, 280, paint);
        paint.setTypeface(Typeface.createFromAsset(requireActivity().getAssets(), "font/arial.ttf"));
        canvas.drawText(montoRecibo + " $", 30, 300, paint);

        //Fecha del Recibo
        paint.setTypeface(Typeface.createFromAsset(requireActivity().getAssets(), "font/arialbd.ttf"));
        canvas.drawText("Fecha del Recibo: ", 30, 340, paint);
        paint.setTypeface(Typeface.createFromAsset(requireActivity().getAssets(), "font/arial.ttf"));
        canvas.drawText(fechaRecibo, 30, 360, paint);

        //vendedor
        paint.setTypeface(Typeface.createFromAsset(requireActivity().getAssets(), "font/arialbd.ttf"));
        canvas.drawText("Vendedor: ", 30, 400, paint);
        paint.setTypeface(Typeface.createFromAsset(requireActivity().getAssets(), "font/arial.ttf"));
        canvas.drawText(vendedorRecibo, 30, 420, paint);


        pedidoPDF.finishPage(pagina);
        reciboNum = "recibo" + codigoRecibo + ".pdf"; //este sera el nombre del documento al momento de crearlo y guardarlo en el almacenamiento

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), reciboNum);


        try {
            pedidoPDF.writeTo(Files.newOutputStream(file.toPath()));
            Toast.makeText(requireActivity().getApplicationContext(), "PDF Generado", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(requireActivity().getApplicationContext(), "error en " + e, Toast.LENGTH_LONG).show();
        }

        pedidoPDF.close();

    }


}