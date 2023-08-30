package com.appcloos.mimaletin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class lineasReclamosAdapter extends BaseAdapter {
    private final ArrayList<Reclamo> listlineas;
    private final Context context;

    public lineasReclamosAdapter(Context context, ArrayList<Reclamo> listlineas) {
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
        final Reclamo reclamo = (Reclamo) getItem(position);
        convertView = LayoutInflater.from(context).inflate(R.layout.item_lineas_reclamos, null);

        TextView tvcodigorcl = convertView.findViewById(R.id.tv_codigorcl);
        TextView tvnombreartrcl = convertView.findViewById(R.id.tv_nombreartrcl);
        TextView tvstotrcl = convertView.findViewById(R.id.tv_stotrcl);
        TextView tvstotdef = convertView.findViewById(R.id.tv_stotdef);
        TextView tvcantidadrcl = convertView.findViewById(R.id.tv_cantidadrcl);
        TextView tvcantidaddef = convertView.findViewById(R.id.tv_cantidaddef);

        Double stotReportado = reclamo.getStot();
        stotReportado = Math.round(stotReportado * 100.0) / 100.0;

        Double stotDef = reclamo.getStotdef();
        stotDef = Math.round(stotDef * 100.0) / 100.0;

        Double cantidad = reclamo.getCant();
        int cantidadEntera = cantidad.intValue();

        Double cantidaddef = reclamo.getCantdef();
        int cantidaddefEntera = cantidaddef.intValue();


        tvcodigorcl.setText(reclamo.getCodart());
        tvnombreartrcl.setText(reclamo.getNombre());
        tvcantidadrcl.setText(Integer.toString(cantidadEntera));
        tvcantidaddef.setText(Integer.toString(cantidaddefEntera));
        tvstotrcl.setText(stotReportado + "$");
        tvstotdef.setText(stotDef + "$");

        return convertView;
    }
}
