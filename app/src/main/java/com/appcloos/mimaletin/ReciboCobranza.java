package com.appcloos.mimaletin;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ReciboCobranza#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReciboCobranza extends Fragment {
    AdminSQLiteOpenHelper conn;
    SQLiteDatabase ke_android;
    SharedPreferences preferences;
    public static String cod_usuario, clientePagar, codigoCliente, nroRecibo, CorrelativoTexto, nombreEmpresa = "", enlaceEmpresa = "", codigoSucursal="";
    ArrayList<Cliente> listacliente;
    ArrayList<String> listainfoClientes;
    Spinner spinnerClientes;
    Button bt_procesarpago;
    EditText et_montopago;
    ArrayList<Recibos> listarecibos;
    RecibosAdapter recibosAdapter;
    ListView lv_verrecibos;


    public static int montoPago, nroCorrelativo;
    public static JSONArray arrayRec;
    public static String pedido_estatus;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ReciboCobranza() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ReciboCobranza.
     */
    // TODO: Rename and change types and number of parameters
    public static ReciboCobranza newInstance(String param1, String param2) {
        ReciboCobranza fragment = new ReciboCobranza();
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

        final View v = inflater.inflate(R.layout.fragment_recibo_cobranza, container, false);

        //identificacion de los elementos en el fragment
        spinnerClientes = v.findViewById(R.id.spinnercli);
        bt_procesarpago = v.findViewById(R.id.bt_procesarpago);
        et_montopago    = v.findViewById(R.id.et_montopago);


        conn = new AdminSQLiteOpenHelper(getActivity(), "ke_android", null, 10);
        ke_android = conn.getWritableDatabase();
        cargarEnlace();
        preferences    = getActivity().getSharedPreferences("Preferences", MODE_PRIVATE);
        cod_usuario    = preferences.getString("cod_usuario", null);
        CargarClientes();
        obtenerCorrelativo();



        // Inflate the layout for this fragment

        bt_procesarpago.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(codigoCliente == null){
                    Toast.makeText(getActivity().getApplicationContext(), "Debes Seleccionar un cliente", Toast.LENGTH_LONG).show();
                }else {
                    validaryProcesar();
                }



            }
        });

        //guardar la seleccion del spinner (osea el codigo y nombre del cliente).
        spinnerClientes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if(position!= 0){
                    clientePagar  = listacliente.get(position-1).getNombre();
                    codigoCliente = listacliente.get(position-1).getCodigo();
                } else{
                    codigoCliente = null;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });




        return v;
    }

    //asi obtengo el correlativo correspondiente; tambien me sirve para actualizar el correlativo mientras el usuario se mantiene en el fragment.
    private void obtenerCorrelativo() {
        conn = new AdminSQLiteOpenHelper(getActivity(), "ke_android", null, 6);
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        Cursor cursor = ke_android.rawQuery("SELECT MAX(kcc_numero) FROM ke_correlacxc WHERE kcc_vendedor ='"+ cod_usuario + "'", null);

        if(cursor.moveToFirst()) {
            nroCorrelativo = cursor.getInt(0);
            nroCorrelativo = nroCorrelativo+1;
            CorrelativoTexto = String.valueOf(nroCorrelativo);
            CorrelativoTexto = "0000"+ nroCorrelativo;
        }
    }

    private void cargarEnlace() {
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        String[] columnas = new String[]{
                "kee_nombre," +
                        "kee_url," +
                        "kee_sucursal"};
        Cursor cursor = ke_android.query("ke_enlace", columnas, "1", null, null, null, null);

        while(cursor.moveToNext()){
            nombreEmpresa = cursor.getString(0);
            enlaceEmpresa = cursor.getString(1);
            codigoSucursal = cursor.getString(2);
        }

    }


    //limpio los campos luego de procesar exitosamente el recibo
    private void LimpiarCampos() {
        et_montopago.setText("");
        spinnerClientes.setSelection(0);
    }

    //validacion y procesamiento del pago para su posterior subida.
    public void validaryProcesar() {

        String valorPago = et_montopago.getText().toString().trim();



        if(valorPago.equals("") ){
            Toast.makeText(getActivity().getApplicationContext(), "Debes introducir un Monto", Toast.LENGTH_LONG).show();
        } else {
            montoPago = Integer.parseInt(valorPago);
            if (montoPago > 0 ) {


                AlertDialog.Builder ventana = new AlertDialog.Builder(getActivity());
                ventana.setTitle("Confirmación de Datos");
                ventana.setMessage("¿Está seguro que desea crear un recibo de reporte por efectivo con estos datos?,\n Luego de procesado no podrá ser modificado");

                ventana.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        System.out.println("CLIENTE: " + clientePagar + " MONTO A PAGAR: " + montoPago);
                        Toast.makeText(getActivity(), "Guardando recibo", Toast.LENGTH_SHORT).show();
                        generarNumerodeRecibo();

                        Date fechaTabla = new Date(Calendar.getInstance().getTimeInMillis());
                        SimpleDateFormat formatoFechaTabla = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                        String fechaGuardar = formatoFechaTabla.format(fechaTabla);


                        String kcx_nrorecibo = nroRecibo;
                        String kcx_codcli = codigoCliente;
                        String kcx_codven = cod_usuario;
                        String kcx_ncliente = clientePagar;
                        String kcx_fechamodifi = fechaGuardar;
                        int kcx_monto = montoPago;
                        String kcx_status = "0";


                        conn = new AdminSQLiteOpenHelper(getActivity(), "ke_android", null, 7);
                        SQLiteDatabase ke_android = conn.getWritableDatabase();

                        try {

                            ContentValues insertar = new ContentValues();
                            ke_android.beginTransaction(); //iniciamos la tranasaccion

                            insertar.put("kcx_nrorecibo", kcx_nrorecibo);
                            insertar.put("kcx_codcli", kcx_codcli);
                            insertar.put("kcx_codven", kcx_codven);
                            insertar.put("kcx_ncliente", kcx_ncliente);
                            insertar.put("kcx_fechamodifi", kcx_fechamodifi);
                            insertar.put("kcx_monto", kcx_monto);
                            insertar.put("kcx_status", kcx_status);

                            ke_android.insert("ke_cxc", null, insertar);


                            ContentValues aumentarCorrelatiov = new ContentValues();
                            aumentarCorrelatiov.put("kcc_numero", nroCorrelativo);
                            aumentarCorrelatiov.put("kcc_vendedor", cod_usuario);
                            //insertamos el correlativo
                            ke_android.insert("ke_correlacxc", null, aumentarCorrelatiov);
                            ke_android.setTransactionSuccessful();

                        }catch (Exception ex){
                            Toast.makeText(getActivity(), "Error en: " + ex, Toast.LENGTH_SHORT).show();
                        }finally {
                            ke_android.endTransaction();

                        }

                        cargarRecibo();


                        Toast.makeText(getActivity().getApplicationContext(), "Recibo registrado", Toast.LENGTH_LONG).show();
                        LimpiarCampos();

                    }
                });


                ventana.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //hacer nada
                    }
                });

                AlertDialog dialogo = ventana.create();
                dialogo.show();

                obtenerCorrelativo();
                }
            }



    }



    private void cargarRecibo() {

        bt_procesarpago.setEnabled(false);
        bt_procesarpago.setBackgroundColor(Color.rgb(220, 220, 220));

        conn = new AdminSQLiteOpenHelper(getActivity(), "ke_android", null, 7);
        SQLiteDatabase ke_android = conn.getWritableDatabase();
        Cursor cursor = ke_android.rawQuery("SELECT kcx_nrorecibo, kcx_codcli, kcx_codven, kcx_fechamodifi, kcx_monto FROM ke_cxc WHERE kcx_status = '0' and kcx_nrorecibo='"+ nroRecibo + "'", null);

        arrayRec = new JSONArray();

        while(cursor.moveToNext()){

            JSONObject objetoRecibo = new JSONObject();
            try {
                String kcx_nrorecibo = cursor.getString(0);
                String kcx_codcli = cursor.getString(1);
                String kcx_codven = cursor.getString(2);
                String kcx_fechamodifi = cursor.getString(3);
                int kcx_monto = cursor.getInt(4);
                //char kcx_status = "1";

                objetoRecibo.put("kcx_nrorecibo", kcx_nrorecibo);
                objetoRecibo.put("kcx_codcli", kcx_codcli);
                objetoRecibo.put("kcx_codven", kcx_codven);
                objetoRecibo.put("kcx_fechamodifi", kcx_fechamodifi);
                objetoRecibo.put("kcx_monto", kcx_monto);
                //objetoRecibo.put("kcx_status", '1');

                arrayRec.put(objetoRecibo);




            }catch (JSONException e){
                e.printStackTrace();
                Toast.makeText(getActivity(), "Error al cargar el recibo" + e, Toast.LENGTH_SHORT).show();
            }
        }

        JSONObject jsonObject = new JSONObject();
        try {

            jsonObject.put("Recibo", arrayRec);


        } catch (JSONException e) {
            e.printStackTrace();
            // Toast.makeText(SincronizacionActivity.this, "Error al cargar el recibo" + e, Toast.LENGTH_SHORT).show();
        }
        String jsonStrREC = jsonObject.toString();
        try {
            insertarRecibo(jsonStrREC);



        } catch (Exception exc) {
            exc.printStackTrace();
            // Toast.makeText(SincronizacionActivity.this, "Error al cargar el recibo" + exc, Toast.LENGTH_SHORT).show();

        }

    }

    public void insertarRecibo(final String jsonrec) {
        RequestQueue requestQueue   = Volley.newRequestQueue(getActivity());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://"+enlaceEmpresa+"/Rest/Recibos_2.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response.trim().equals("OK")) {
                    Toast.makeText(getActivity(), "Recibo Subido", Toast.LENGTH_LONG).show();
                    bt_procesarpago.setEnabled(true);
                    bt_procesarpago.setBackgroundColor(Color.rgb(0, 150, 136));
                    cambiarEstadoRecibo();
                    getActivity().finish();
                    getActivity().overridePendingTransition(0, 0);
                    startActivity(getActivity().getIntent());
                    getActivity().overridePendingTransition(0, 0);//para refrescar el RecyclerView

                    //getActivity().finish();

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(getActivity(), "Error en la subida", Toast.LENGTH_SHORT).show();
                bt_procesarpago.setEnabled(true);
                bt_procesarpago.setBackgroundColor(Color.rgb(0, 150, 136));
            }
        }){
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("jsonrec", jsonrec);
                params.put("agencia", codigoSucursal);
                return params;
            }
        };

        requestQueue.add(stringRequest);

    }

    private void cambiarEstadoRecibo() {

        SQLiteDatabase ke_android = conn.getWritableDatabase();
        //System.out.println(arrayRec);
        for (int i = 0; i <  arrayRec.length(); i++) {
            try {
                JSONObject objetodeRecibo = arrayRec.getJSONObject(i);

                String codigoDelReciboEnArray = objetodeRecibo.getString("kcx_nrorecibo");
                ke_android.execSQL("UPDATE ke_cxc SET kcx_status = '1' WHERE kcx_nrorecibo = '" + codigoDelReciboEnArray + "'");

            }catch (JSONException e){
                e.printStackTrace();
            }

        }
    }


    private void generarNumerodeRecibo() {
        Date fechaHoy = new Date(Calendar.getInstance().getTimeInMillis());
        SimpleDateFormat formatofecha = new SimpleDateFormat("yyMM");
        String fecha = formatofecha.format(fechaHoy);
        CorrelativoTexto = right(CorrelativoTexto, 4);
        nroRecibo = "WE-" + cod_usuario.trim() + "-" + fecha + CorrelativoTexto;
    }


    private void CargarClientes() {
        conn = new AdminSQLiteOpenHelper(getActivity().getApplicationContext(), "ke_android", null, 4);
        SQLiteDatabase ke_android = conn.getReadableDatabase();
        Cliente cliente = null;
        listacliente  = new ArrayList<Cliente>();
        Cursor cursor = ke_android.rawQuery("SELECT codigo, nombre FROM cliempre WHERE vendedor ='"+ cod_usuario.toString().trim() +"' ORDER BY nombre ASC", null);

        while (cursor.moveToNext()){
            cliente = new Cliente();
            cliente.setCodigo(cursor.getString(0));
            cliente.setNombre(cursor.getString(1));
            listacliente.add(cliente);
        }
        ke_android.close();
        obtenerlistaCliente();
        ArrayAdapter<CharSequence> adapterSpinner = new ArrayAdapter(this.getActivity(), R.layout.spinner_pagos_clientes , listainfoClientes);
        spinnerClientes.setAdapter(adapterSpinner);
        adapterSpinner.notifyDataSetChanged();
    }

    private void obtenerlistaCliente() {
        listainfoClientes = new ArrayList<String>();
        listainfoClientes.add("Seleccione un Cliente...");

        for (int i = 0; i < listacliente.size(); i++) {
            listainfoClientes.add(listacliente.get(i).getCodigo() + ": " + listacliente.get(i).getNombre().trim());

        }
    }

    public static String right(String valor, int longitud) {
        //una función "right" utilizando la clase substring
        return  valor.substring(valor.length() - longitud );
    }



}