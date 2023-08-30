package com.appcloos.mimaletin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class LineasAdapter extends BaseAdapter {
    private final ArrayList<Lineas> listlineas;
    private final Context context;
    private LayoutInflater inflater;

    public LineasAdapter(Context context, ArrayList<Lineas> listlineas) {
        this.context = context;
        this.listlineas = listlineas;
    }

    @Override
    public int getCount() {
        return listlineas.size();
    }

    @Override
    public Object getItem(int i) {
        return listlineas.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final Lineas lineas = (Lineas) getItem(position);
        convertView = LayoutInflater.from(context).inflate(R.layout.item_lineas_docs, null);


        TextView tv_codlinea = convertView.findViewById(R.id.tv_codlinea);
        TextView tv_desclinea = convertView.findViewById(R.id.tv_desclinea);
        TextView tv_mtolinea = convertView.findViewById(R.id.tv_mtolinea);
        TextView tv_cantlinea = convertView.findViewById(R.id.tv_cantlinea);
        TextView tv_pid = convertView.findViewById(R.id.tv_pid);
        TextView tv_precfin = convertView.findViewById(R.id.tv_preciofin);

        Double preciofin = lineas.getDpreciofin();
        Double mtoTotal = lineas.getDmontototal();
        mtoTotal = Math.round(mtoTotal * 100.0) / 100.0;
        Double cantidad = lineas.getCantidad();
        int cantidadEntera = cantidad.intValue();


        tv_codlinea.setText("Código:  " + lineas.getCodigo());
        tv_desclinea.setText(lineas.getNombre());
        tv_mtolinea.setText("Monto: " + mtoTotal + "$");
        tv_cantlinea.setText("Cantidad en Doc: " + cantidadEntera);
        tv_pid.setText(lineas.getPid());
        tv_precfin.setText(preciofin.toString());

        return convertView;
    }
}
