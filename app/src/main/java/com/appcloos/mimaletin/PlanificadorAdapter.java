package com.appcloos.mimaletin;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class PlanificadorAdapter extends BaseAdapter {

    private ArrayList<Documentos> listadocs;
    private Context context;
    private LayoutInflater inflater;

    public PlanificadorAdapter(Context context, ArrayList<Documentos> listadocs){
        this.context = context;
        this.listadocs = listadocs;
    }

    @Override
    public int getCount() {
        return listadocs.size();
    }

    @Override
    public Object getItem(int i) {
        return listadocs.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        convertView = LayoutInflater.from(context).inflate(R.layout.item_planificador, null);

        final Documentos documentos = (Documentos) getItem(position);
        ConstraintLayout cons_item = convertView.findViewById(R.id.const_item);

        TextView tvp_codcliente = (TextView) convertView.findViewById(R.id.tvp_codcliente);
        TextView tvp_nombrecli  = (TextView) convertView.findViewById(R.id.tvp_nombrecli);
        TextView tvp_estatus    = (TextView) convertView.findViewById(R.id.tvp_estatus);
        TextView tvp_documento  = (TextView) convertView.findViewById(R.id.tvp_ndoc);
        TextView tvp_vence      = (TextView) convertView.findViewById(R.id.tvp_vence);
        TextView tvp_dias       = (TextView) convertView.findViewById(R.id.tvp_dias);

        String estatus = documentos.getEstatusdoc();

        String negociacionEspecial = documentos.getKti_negesp();

        switch(estatus){
            case "1":
                tvp_estatus.setText("Abonado");
                break;
            case "0":
                tvp_estatus.setText("Pendiente");
                break;
        }


        try {
            String vence = "";
            vence = documentos.getVence();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            Date hoy = Calendar.getInstance().getTime();
            Date vencimiento = sdf.parse(vence);

            Calendar calendar  = Calendar.getInstance();
            calendar.setTime(vencimiento);

            if(negociacionEspecial.equals("1")){
                calendar.add(Calendar.DATE, 10);
                System.out.println("Entro al if de los documentos con negociacion especial");
                tvp_vence.setText(documentos.getVence());
                tvp_vence.setTextColor(Color.parseColor("#172a8a"));

            }else if(negociacionEspecial.equals("0")){
                calendar.add(Calendar.DATE, 0);
                System.out.println("Entro al if de los documentos sin negociacion especial");
                tvp_vence.setText(documentos.getVence());
            }

            Date nuevoVencimiento = calendar.getTime();

            assert nuevoVencimiento != null;
            long diff = hoy.getTime() - nuevoVencimiento.getTime();
            TimeUnit time = TimeUnit.DAYS;
            long diferencia = time.convert(diff, TimeUnit.MILLISECONDS);
            int difFechas = Integer.parseInt(String.valueOf(diferencia));

            if(difFechas > 0){
                cons_item.setBackgroundColor(Color.parseColor("#f2766d"));
                tvp_dias.setText(String.valueOf(String.valueOf(difFechas)));
            } else if(difFechas >= -7){
                cons_item.setBackgroundColor(Color.parseColor("#e0df99"));
                tvp_dias.setText(String.valueOf(String.valueOf(difFechas)));
            } else if(difFechas < -7){
                tvp_dias.setText(String.valueOf(String.valueOf(difFechas)));
            }

        }catch (Exception exception){
            exception.printStackTrace();
        }

        tvp_codcliente.setText(documentos.getCodcliente());
        tvp_nombrecli.setText(documentos.getNombrecli());
        tvp_documento.setText(documentos.getDocumento());


        int diascred = (int) Math.round(documentos.getDiascred());


        return convertView;
    }
}
