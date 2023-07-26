package com.appcloos.mimaletin;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.PictureDrawable;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import java.io.OutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link VerRecibos#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VerRecibos extends Fragment {
    AdminSQLiteOpenHelper conn;
    SharedPreferences preferences;
    public static String cod_usuario, pedido_estatus, codigoRecibo, codigoCliente, nombreCliente, montoRecibo, vendedorRecibo, fechaRecibo, nombreEmpresa="", nombreVendedor, reciboNum;
    ArrayList<Recibos> listarecibos;
    RecibosAdapter recibosAdapter;
    ListView lv_verrecibos;
    SQLiteDatabase ke_android;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public VerRecibos() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment VerRecibos.
     */
    // TODO: Rename and change types and number of parameters
    public static VerRecibos newInstance(String param1, String param2) {
        VerRecibos fragment = new VerRecibos();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View v = inflater.inflate(R.layout.fragment_ver_recibos, container, false);

        lv_verrecibos = v.findViewById(R.id.lv_verrecibos);

        preferences    = getActivity().getSharedPreferences("Preferences", MODE_PRIVATE);
        cod_usuario    = preferences.getString("cod_usuario", null);

        lineasRecibo(); // ver todos los recibos guardados




        lv_verrecibos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                codigoRecibo   = listarecibos.get(position).getNroRecibo();
                codigoCliente  = listarecibos.get(position).getCodigoCliente();
                nombreCliente  = listarecibos.get(position).getNombreCliente();
                montoRecibo    = listarecibos.get(position).getMontoRecibo();
                fechaRecibo    = listarecibos.get(position).getFechaRecibo();
                vendedorRecibo = listarecibos.get(position).getCodigoVendedor();

                AlertDialog.Builder ventana = new AlertDialog.Builder(getActivity());
                ventana.setTitle("Mensaje del sistema");
                ventana.setMessage("Por favor, elige una opción");

                ventana.setPositiveButton("Generar y compartir PDF", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        conn = new AdminSQLiteOpenHelper(getActivity().getApplicationContext(), "ke_android", null, 8);
                        SQLiteDatabase ke_android = conn.getWritableDatabase();
                        cargarEnlace();
                        Cursor cursor = ke_android.rawQuery("SELECT nombre FROM listvend WHERE codigo ='" + vendedorRecibo + "'", null);
                        while(cursor.moveToNext()){
                            nombreVendedor = cursor.getString(0);
                        }

                        //metodo para guardar el recibo en PDF en la carpeta Documentos
                        guardarRecibo(codigoRecibo, codigoCliente, nombreCliente, montoRecibo, fechaRecibo, nombreVendedor);
                        abrirRecibo(reciboNum);






                    }
                });


                AlertDialog dialogo = ventana.create();
                dialogo.show();
            }
        });

        return v;
    }

    private void abrirRecibo(String nombreArchivo) {

         String ruta = "/storage/emulated/0/Documents/" + nombreArchivo;
         File file = new File(ruta);

         if(!file.exists()){
             Toast.makeText(getActivity().getApplicationContext(),"Este archivo no existe o fue cambiado de lugar.", Toast.LENGTH_LONG).show();
         }
        Intent intentShare = new Intent(Intent.ACTION_SEND);
        intentShare.setType("application/pdf");
        intentShare.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(getActivity(), getActivity().getApplicationContext().getPackageName() + ".provider", file));
        startActivity(Intent.createChooser(intentShare, "Compartir Archivo..."));



    }

    public void lineasRecibo() {
        cargarRecibos();

        if(listarecibos != null){
            recibosAdapter = new RecibosAdapter(getActivity(), listarecibos);
            lv_verrecibos.setAdapter(null);
            lv_verrecibos.setAdapter(recibosAdapter);
            recibosAdapter.notifyDataSetChanged();

        }
    }

    private void cargarRecibos() {

        listarecibos = new ArrayList<Recibos>();
        AdminSQLiteOpenHelper conn = new AdminSQLiteOpenHelper(getActivity().getApplicationContext(), "ke_android", null, 10);
        SQLiteDatabase ke_android = conn.getWritableDatabase();


        Cursor cursor = ke_android.rawQuery("SELECT kcx_nrorecibo, kcx_codcli, kcx_ncliente, kcx_monto, kcx_fechamodifi, kcx_status, kcx_codven FROM ke_cxc WHERE " +
                "kcx_codven ='"+ cod_usuario+"'", null);

        while(cursor.moveToNext()){
            String estatusEval = cursor.getString(5);

            if(estatusEval.equals("0")) {
                pedido_estatus = "Por Subir";


            } else if (estatusEval.equals("1")){
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
        ke_android.close();
    }


    private void cargarEnlace() {
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        String[] columnas = new String[]{
                "kee_nombre," +
                        "kee_url"};
        Cursor cursor = ke_android.query("ke_enlace", columnas, "1", null, null, null, null);

        while(cursor.moveToNext()){
            nombreEmpresa = cursor.getString(0);
            //enlaceEmpresa = cursor.getString(1);
        }

    }

    public void guardarRecibo(String codigoRecibo, String codigoCliente, String nombreCliente, String montoRecibo, String fechaRecibo, String vendedorRecibo ){


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
        paint.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "font/arialbd.ttf"));

        //CABECERA
        //imagen del la cabecera


        Bitmap bmp = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.plantillasello);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bmp, 300,500, false);
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
        paint.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "font/arialbd.ttf"));
        canvas.drawText("Código del Cliente: ", 30, 160, paint);
        paint.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "font/arial.ttf"));
        canvas.drawText(codigoCliente, 30, 180, paint);

        //NOMBRE DEL CLIENTE
        paint.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "font/arialbd.ttf"));
        canvas.drawText("Cliente: ", 30, 220, paint);
        paint.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "font/arial.ttf"));
        canvas.drawText(nombreCliente, 30, 240, paint);

        //Monto del Recibo
        paint.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "font/arialbd.ttf"));
        canvas.drawText("Monto Pagado: " , 30,280, paint);
        paint.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "font/arial.ttf"));
        canvas.drawText(montoRecibo + " $" , 30,300, paint);

        //Fecha del Recibo
        paint.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "font/arialbd.ttf"));
        canvas.drawText("Fecha del Recibo: " , 30,340, paint);
        paint.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "font/arial.ttf"));
        canvas.drawText(fechaRecibo, 30,360, paint);

        //vendedor
        paint.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "font/arialbd.ttf"));
        canvas.drawText("Vendedor: " , 30,400, paint);
        paint.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "font/arial.ttf"));
        canvas.drawText(vendedorRecibo, 30,420, paint);




        pedidoPDF.finishPage(pagina);
        reciboNum = "recibo" + codigoRecibo + ".pdf"; //este sera el nombre del documento al momento de crearlo y guardarlo en el almacenamiento

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), reciboNum);


        try {
            pedidoPDF.writeTo(new FileOutputStream(file));
            Toast.makeText(getActivity().getApplicationContext(), "PDF Generado", Toast.LENGTH_LONG).show();
        }catch (IOException e){
            Toast.makeText(getActivity().getApplicationContext(), "error en " + e, Toast.LENGTH_LONG).show();
        }

        pedidoPDF.close();

    }







}