package com.appcloos.mimaletin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.Objects;

public class DetalleVendedorActivity extends AppCompatActivity {
    AdminSQLiteOpenHelper conn;

    TextView lbCantclientes, lbVisitados, lbPrcvisitas, lbPrvisitas, lbPnvisitas, lbCantpedidos, lbMtopedidos, lbCantfacturas, lbMtofacturas, lbMeta, lbPrcmeta,
            lbPrmtoventa, lbPnmtoventa, lbPrprcventa, lbPnprcventa, tvMtofacturasNeto, tvPpgdolTotneto, tvDevdolTotneto, tvDefdolTotneto, tvTotdolcob;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_vendedor);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED); //mantener la orientacion vertical


        Intent intent   = getIntent();
        String codigoVendedor = intent.getStringExtra("codigoVend");
        String nombreVendedor = intent.getStringExtra("nombreVend");
        Objects.requireNonNull(getSupportActionBar()).setTitle(nombreVendedor);

        //declaracion de campos de la tabla que van a ser llenados por el resultado de la consulta.
        lbCantclientes = findViewById(R.id.lb_cantclientes);
        lbVisitados = findViewById(R.id.lb_visitados);
        lbPrcvisitas = findViewById(R.id.lb_prcvisitas);
        lbPrvisitas = findViewById(R.id.lb_prvisitas);
        lbPnvisitas = findViewById(R.id.lb_pnvisitas);
        lbCantpedidos = findViewById(R.id.lb_cantpedidos);
        lbMtopedidos = findViewById(R.id.lb_mtopedidos);
        lbCantfacturas = findViewById(R.id.lb_cantfacturas);
        lbMtofacturas = findViewById(R.id.lb_mtofacturas);
        lbMeta = findViewById(R.id.lb_meta);
        lbPrcmeta = findViewById(R.id.lb_prcmeta);
        lbPrmtoventa = findViewById(R.id.lb_prmtoventa);
        lbPnmtoventa = findViewById(R.id.lb_pnmtoventa);
        lbPrprcventa = findViewById(R.id.lb_prprcventa);
        lbPnprcventa = findViewById(R.id.lb_pnprcventa);
        tvMtofacturasNeto = findViewById(R.id.tv_mtofacturas_neto);
        tvPpgdolTotneto = findViewById(R.id.tv_ppgdol_totneto);
        tvDevdolTotneto = findViewById(R.id.tv_devdol_totneto);
        tvDefdolTotneto = findViewById(R.id.tv_defdol_totneto);
        tvTotdolcob = findViewById(R.id.tv_totdolcob);

        llenarDatosDeFicha(codigoVendedor);

    }

    private void llenarDatosDeFicha(String codigoVendedor) {
        //Locale es = new Locale("es");
        DecimalFormat formato = new DecimalFormat( "#,###.##" );

        //NumberFormat formatoMoneda     = NumberFormat.getCurrencyInstance();


        conn = new AdminSQLiteOpenHelper(getApplicationContext(), "ke_android", null, 4);
        SQLiteDatabase ke_android = conn.getWritableDatabase();

        Cursor cursor = ke_android.rawQuery("SELECT cntpedidos, mtopedidos, cntfacturas, mtofacturas, metavend, prcmeta, cntclientes, clivisit, prcvisitas, lom_montovtas, lom_prcvtas, lom_prcvisit, rlom_montovtas, rlom_prcvtas, rlom_prcvisit, ppgdol_totneto, devdol_totneto, defdol_totneto, totdolcob FROM ke_estadc01 WHERE vendedor ='"+ codigoVendedor + "'", null);

        while(cursor.moveToNext()){



            int cantpedidos = cursor.getInt(0);
            lbCantpedidos.setText(Integer.toString(cantpedidos));

            double mtopedidos = cursor.getDouble(1);
            mtopedidos = Math.round(mtopedidos*100.0)/100.0;
            lbMtopedidos.setText("$ "+formato.format(mtopedidos));

            int cantfacturas = cursor.getInt(2);
            lbCantfacturas.setText(Integer.toString(cantfacturas));

            double mtofacturas = cursor.getDouble(3);
            mtofacturas = Math.round(mtofacturas*100.0)/100.0;
            lbMtofacturas.setText("$ "+formato.format(mtofacturas));

            double metavend = cursor.getDouble(4);
            metavend = Math.round(metavend*100.0)/100.0;
            lbMeta.setText("$ "+formato.format(metavend));

            double prcmeta = cursor.getDouble(5);
            prcmeta = Math.round(prcmeta*100.0)/100.0;
            lbPrcmeta.setText(formato.format(prcmeta) + "%");

            int cntclientes = cursor.getInt(6);
            lbCantclientes.setText(Integer.toString(cntclientes));

            int clivisit = cursor.getInt(7);
            lbVisitados.setText(Integer.toString(clivisit));

            double prcvisitas = cursor.getDouble(8);
            prcvisitas = Math.round(prcvisitas*100.0)/100.0;
            lbPrcvisitas.setText(formato.format(prcvisitas) + "%");

            int lom_montovtas = cursor.getInt(9);
            lbPnmtoventa.setText(Integer.toString(lom_montovtas));

            int lom_prcvtas = cursor.getInt(10);
            lbPnprcventa.setText(Integer.toString(lom_prcvtas));

            int lom_prcvisit = cursor.getInt(11);
            lbPnvisitas.setText(Integer.toString(lom_prcvisit));

            int rlom_montovtas = cursor.getInt(12);
            lbPrmtoventa.setText(Integer.toString(rlom_montovtas));

            int rlom_prcvtas = cursor.getInt(13);
            lbPrprcventa.setText(Integer.toString(rlom_prcvtas));

            int rlom_prcvisit = cursor.getInt(14);
            lbPrvisitas.setText(Integer.toString(rlom_prcvisit));

            double ppgdol_totneto = cursor.getDouble(15);
            ppgdol_totneto = Math.round(ppgdol_totneto*100.0)/100.0;
            tvPpgdolTotneto.setText("$ "+formato.format(ppgdol_totneto));
            System.out.println("ppgdol_totneto" + ppgdol_totneto);

            double devdol_totneto = cursor.getDouble(16);
            devdol_totneto = Math.round(devdol_totneto*100.0)/100.0;
            tvDevdolTotneto.setText("$ "+formato.format(devdol_totneto));
            System.out.println("devdol_totneto" + devdol_totneto);

            double defdol_totneto = cursor.getDouble(17);
            defdol_totneto = Math.round(defdol_totneto*100.0)/100.0;
            tvDefdolTotneto.setText("$ "+formato.format(defdol_totneto));
            System.out.println("defdol_totneto" + defdol_totneto);

            double mtofacturas_neto = mtofacturas - ppgdol_totneto - devdol_totneto - defdol_totneto;
            mtofacturas_neto = Math.round(mtofacturas_neto*100.0)/100.0;
            tvMtofacturasNeto.setText("$ "+formato.format(mtofacturas_neto));
            System.out.println("mtofacturas_neto" + mtofacturas_neto);

            double totdolcob = cursor.getDouble(18);
            totdolcob = Math.round(totdolcob*100.0)/100.0;
            tvTotdolcob.setText("$ "+formato.format(totdolcob));
            System.out.println("totdolcob" + totdolcob);


        }
        cursor.close();
    }
}