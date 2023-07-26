package com.appcloos.mimaletin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class DetalleVendedorActivity extends AppCompatActivity {
    AdminSQLiteOpenHelper conn;

    private static String codigo_vendedor, nombre_vendedor;
    TextView lb_cantclientes, lb_visitados, lb_prcvisitas, lb_prvisitas, lb_pnvisitas, lb_cantpedidos, lb_mtopedidos, lb_cantfacturas, lb_mtofacturas, lb_meta, lb_prcmeta,
            lb_prmtoventa, lb_pnmtoventa, lb_prprcventa, lb_pnprcventa, tv_mtofacturas_neto, tv_ppgdol_totneto, tv_devdol_totneto, tv_defdol_totneto, tv_totdolcob;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_vendedor);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //mantener la orientacion vertical


        Intent intent   = getIntent();
        codigo_vendedor = intent.getStringExtra("codigoVend");
        nombre_vendedor = intent.getStringExtra("nombreVend");
        getSupportActionBar().setTitle(nombre_vendedor);

        //declaracion de campos de la tabla que van a ser llenados por el resultado de la consulta.
        lb_cantclientes     = findViewById(R.id.lb_cantclientes);
        lb_visitados        = findViewById(R.id.lb_visitados);
        lb_prcvisitas       = findViewById(R.id.lb_prcvisitas);
        lb_prvisitas        = findViewById(R.id.lb_prvisitas);
        lb_pnvisitas        = findViewById(R.id.lb_pnvisitas);
        lb_cantpedidos      = findViewById(R.id.lb_cantpedidos);
        lb_mtopedidos       = findViewById(R.id.lb_mtopedidos);
        lb_cantfacturas     = findViewById(R.id.lb_cantfacturas);
        lb_mtofacturas      = findViewById(R.id.lb_mtofacturas);
        lb_meta             = findViewById(R.id.lb_meta);
        lb_prcmeta          = findViewById(R.id.lb_prcmeta);
        lb_prmtoventa       = findViewById(R.id.lb_prmtoventa);
        lb_pnmtoventa       = findViewById(R.id.lb_pnmtoventa);
        lb_prprcventa       = findViewById(R.id.lb_prprcventa);
        lb_pnprcventa       = findViewById(R.id.lb_pnprcventa);
        tv_mtofacturas_neto = findViewById(R.id.tv_mtofacturas_neto);
        tv_ppgdol_totneto   = findViewById(R.id.tv_ppgdol_totneto);
        tv_devdol_totneto   = findViewById(R.id.tv_devdol_totneto);
        tv_defdol_totneto   = findViewById(R.id.tv_defdol_totneto);
        tv_totdolcob        = findViewById(R.id.tv_totdolcob);

        llenarDatosDeFicha(codigo_vendedor);

    }

    private void llenarDatosDeFicha(String codigo_vendedor) {
        //Locale es = new Locale("es");
        DecimalFormat formato = new DecimalFormat( "#,###.##" );

        //NumberFormat formatoMoneda     = NumberFormat.getCurrencyInstance();


        conn = new AdminSQLiteOpenHelper(getApplicationContext(), "ke_android", null, 4);
        SQLiteDatabase ke_android = conn.getWritableDatabase();

        Cursor cursor = ke_android.rawQuery("SELECT cntpedidos, mtopedidos, cntfacturas, mtofacturas, metavend, prcmeta, cntclientes, clivisit, prcvisitas, lom_montovtas, lom_prcvtas, lom_prcvisit, rlom_montovtas, rlom_prcvtas, rlom_prcvisit, ppgdol_totneto, devdol_totneto, defdol_totneto, totdolcob FROM ke_estadc01 WHERE vendedor ='"+ codigo_vendedor + "'", null);

        while(cursor.moveToNext()){



            int cantpedidos = cursor.getInt(0);
            lb_cantpedidos.setText(Integer.toString(cantpedidos));

            double mtopedidos = cursor.getDouble(1);
            mtopedidos = Math.round(mtopedidos*100.0)/100.0;
            lb_mtopedidos.setText("$ "+formato.format(mtopedidos));

            int cantfacturas = cursor.getInt(2);
            lb_cantfacturas.setText(Integer.toString(cantfacturas));

            double mtofacturas = cursor.getDouble(3);
            mtofacturas = Math.round(mtofacturas*100.0)/100.0;
            lb_mtofacturas.setText("$ "+formato.format(mtofacturas));

            double metavend = cursor.getDouble(4);
            metavend = Math.round(metavend*100.0)/100.0;
            lb_meta.setText("$ "+formato.format(metavend));

            double prcmeta = cursor.getDouble(5);
            prcmeta = Math.round(prcmeta*100.0)/100.0;
            lb_prcmeta.setText(formato.format(prcmeta) + "%");

            int cntclientes = cursor.getInt(6);
            lb_cantclientes.setText(Integer.toString(cntclientes));

            int clivisit = cursor.getInt(7);
            lb_visitados.setText(Integer.toString(clivisit));

            double prcvisitas = cursor.getDouble(8);
            prcvisitas = Math.round(prcvisitas*100.0)/100.0;
            lb_prcvisitas.setText(formato.format(prcvisitas) + "%");

            int lom_montovtas = cursor.getInt(9);
            lb_pnmtoventa.setText(Integer.toString(lom_montovtas));

            int lom_prcvtas = cursor.getInt(10);
            lb_pnprcventa.setText(Integer.toString(lom_prcvtas));

            int lom_prcvisit = cursor.getInt(11);
            lb_pnvisitas.setText(Integer.toString(lom_prcvisit));

            int rlom_montovtas = cursor.getInt(12);
            lb_prmtoventa.setText(Integer.toString(rlom_montovtas));

            int rlom_prcvtas = cursor.getInt(13);
            lb_prprcventa.setText(Integer.toString(rlom_prcvtas));

            int rlom_prcvisit = cursor.getInt(14);
            lb_prvisitas.setText(Integer.toString(rlom_prcvisit));

            double ppgdol_totneto = cursor.getDouble(15);
            ppgdol_totneto = Math.round(ppgdol_totneto*100.0)/100.0;
            tv_ppgdol_totneto.setText("$ "+formato.format(ppgdol_totneto));
            System.out.println("ppgdol_totneto" + ppgdol_totneto);

            double devdol_totneto = cursor.getDouble(16);
            devdol_totneto = Math.round(devdol_totneto*100.0)/100.0;
            tv_devdol_totneto.setText("$ "+formato.format(devdol_totneto));
            System.out.println("devdol_totneto" + devdol_totneto);

            double defdol_totneto = cursor.getDouble(17);
            defdol_totneto = Math.round(defdol_totneto*100.0)/100.0;
            tv_defdol_totneto.setText("$ "+formato.format(defdol_totneto));
            System.out.println("defdol_totneto" + defdol_totneto);

            double mtofacturas_neto = mtofacturas - ppgdol_totneto - devdol_totneto - defdol_totneto;
            mtofacturas_neto = Math.round(mtofacturas_neto*100.0)/100.0;
            tv_mtofacturas_neto.setText("$ "+formato.format(mtofacturas_neto));
            System.out.println("mtofacturas_neto" + mtofacturas_neto);

            double totdolcob = cursor.getDouble(18);
            totdolcob = Math.round(totdolcob*100.0)/100.0;
            tv_totdolcob.setText("$ "+formato.format(totdolcob));
            System.out.println("totdolcob" + totdolcob);


        }

    }
}