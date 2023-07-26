package com.appcloos.mimaletin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class lineasReclamosAdapter extends BaseAdapter {
    private ArrayList<Reclamo> listlineas;
    private Context context;

    public lineasReclamosAdapter(Context context, ArrayList<Reclamo> listlineas){
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

        TextView tvcodigorcl    = (TextView) convertView.findViewById(R.id.tv_codigorcl);
        TextView tvnombreartrcl = (TextView) convertView.findViewById(R.id.tv_nombreartrcl);
        TextView tvstotrcl      = (TextView) convertView.findViewById(R.id.tv_stotrcl);
        TextView tvstotdef      = (TextView) convertView.findViewById(R.id.tv_stotdef);
        TextView tvcantidadrcl  = (TextView) convertView.findViewById(R.id.tv_cantidadrcl);
        TextView tvcantidaddef  = (TextView) convertView.findViewById(R.id.tv_cantidaddef);

        Double stotReportado = reclamo.getStot();
        stotReportado        = Math.round(stotReportado * 100.0)/100.0;

        Double stotDef = reclamo.getStotdef();
        stotDef        = Math.round(stotDef * 100.0)/100.0;

        Double cantidad        = reclamo.getCant();
        Integer cantidadEntera = cantidad.intValue();

        Double cantidaddef        = reclamo.getCantdef();
        Integer cantidaddefEntera = cantidaddef.intValue();


        tvcodigorcl.setText(reclamo.getCodart());
        tvnombreartrcl.setText(reclamo.getNombre());
        tvcantidadrcl.setText(cantidadEntera.toString());
        tvcantidaddef.setText(cantidaddefEntera.toString());
        tvstotrcl.setText(stotReportado + "$");
        tvstotdef.setText(stotDef + "$");

        return convertView;
    }
}
