package com.appcloos.mimaletin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class LineasTmpAdapter extends BaseAdapter {
    private final ArrayList<Lineas> listlineastmp;
    private final Context context;
    private LayoutInflater inflater;

    public LineasTmpAdapter(Context context, ArrayList<Lineas> listlineastmp) {
        this.context = context;
        this.listlineastmp = listlineastmp;
    }


    @Override
    public int getCount() {
        return listlineastmp.size();
    }

    @Override
    public Object getItem(int i) {
        return listlineastmp.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final Lineas lineas = (Lineas) getItem(position);
        convertView = LayoutInflater.from(context).inflate(R.layout.item_docslintmp, null);

        TextView tv_codlineatmp = convertView.findViewById(R.id.tv_codlineatmp);
        TextView tv_desclineatmp = convertView.findViewById(R.id.tv_desclineatmp);
        TextView tv_mtolineatmp = convertView.findViewById(R.id.tv_mtolineatmp);
        TextView tv_cantlineatmp = convertView.findViewById(R.id.tv_cantlineatmp);
        TextView tv_pidtmp = convertView.findViewById(R.id.tv_pidtmp);
        TextView tv_cantdevol = convertView.findViewById(R.id.tv_cantdevol);


        Double mtoTotal = lineas.getDmontototal();
        mtoTotal = Math.round(mtoTotal * 100.0) / 100.0;
        Double cantidad = lineas.getCantidad();
        int cantidadEntera = cantidad.intValue();
        Double cantiDev = lineas.getCntdevuelt();
        int cantidadDevEntera = cantiDev.intValue();

        tv_codlineatmp.setText("Código:  " + lineas.getCodigo());
        tv_desclineatmp.setText(lineas.getNombre());
        tv_mtolineatmp.setText("Monto: " + mtoTotal + "$");
        tv_cantlineatmp.setText("Cantidad en Doc: " + cantidadEntera);
        tv_pidtmp.setText(lineas.getPid());
        tv_cantdevol.setText("A devolver: " + cantidadDevEntera);


        return convertView;
    }
}
