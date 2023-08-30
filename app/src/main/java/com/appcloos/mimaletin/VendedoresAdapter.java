package com.appcloos.mimaletin;


import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class VendedoresAdapter extends BaseAdapter {

    private final ArrayList<Estadistica> listestadistica;
    private final Context context;
    private LayoutInflater inflater;


    public VendedoresAdapter(Context context, ArrayList<Estadistica> listestadistica) {
        this.context = context;
        this.listestadistica = listestadistica;
    }

    @Override
    public int getCount() {
        return listestadistica.size();
    }

    @Override
    public Object getItem(int i) {
        return listestadistica.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        convertView = LayoutInflater.from(context).inflate(R.layout.item_estadistica, null);

        final Estadistica estadistica = (Estadistica) getItem(position);
        TextView tv_codvend = convertView.findViewById(R.id.tv_codvend);
        TextView tv_nombrevend = convertView.findViewById(R.id.tv_nombrevend);
        TextView tv_porcentaje = convertView.findViewById(R.id.tv_porcentaje);
        TextView tv_fechaact = convertView.findViewById(R.id.tv_fechaact);
        ImageView im_estrella = convertView.findViewById(R.id.im_estrella);


        Double prcmeta = estadistica.getPrcmeta();

        tv_codvend.setText(estadistica.getVendedor());
        tv_nombrevend.setText(estadistica.getNombrevend());
        tv_porcentaje.setText(prcmeta + "%");
        tv_fechaact.setText(estadistica.getFecha_estad());


        if (prcmeta >= 100.0) {
            tv_porcentaje.setBackgroundColor(Color.rgb(0, 137, 87));
            im_estrella.setVisibility(View.VISIBLE);

        } else if (prcmeta > 80.0 && prcmeta < 100.0) {
            tv_porcentaje.setBackgroundColor(Color.rgb(0, 186, 0));
        } else if (prcmeta > 70.0 && prcmeta <= 80.0) {
            tv_porcentaje.setBackgroundColor(Color.rgb(0, 186, 64));
        } else if (prcmeta > 60.0 && prcmeta <= 70.0) {
            tv_porcentaje.setBackgroundColor(Color.rgb(216, 255, 0));
        } else if (prcmeta > 50.0 && prcmeta <= 60.0) {
            tv_porcentaje.setBackgroundColor(Color.rgb(255, 192, 0));
        } else if (prcmeta > 30.0 && prcmeta <= 50.0) {
            tv_porcentaje.setBackgroundColor(Color.rgb(255, 135, 0));
        } else if (prcmeta > 0 && prcmeta <= 30.0) {
            tv_porcentaje.setBackgroundColor(Color.rgb(207, 0, 0));
        } else if (prcmeta <= 0) {
            tv_porcentaje.setBackgroundColor(Color.rgb(0, 0, 0));
        }

        return convertView;
    }
}
