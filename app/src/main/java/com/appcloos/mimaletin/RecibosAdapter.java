package com.appcloos.mimaletin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class RecibosAdapter  extends BaseAdapter {
    private ArrayList<Recibos> listrecibos;
    private Context context;
    private LayoutInflater inflater;

    public RecibosAdapter(Context context, ArrayList<Recibos> listrecibos){
        this.context = context;
        this.listrecibos = listrecibos;
    }

    @Override
    public int getCount() {
        return listrecibos.size();
    }

    @Override
    public Object getItem(int i) {
        return listrecibos.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final Recibos recibos = (Recibos) getItem(position);
        convertView = LayoutInflater.from(context).inflate(R.layout.item_recibos, null);


        TextView nrorec            = (TextView) convertView.findViewById(R.id.nrorec);
        TextView codclirec         = (TextView) convertView.findViewById(R.id.codigocli);
        TextView nombreclirec      = (TextView) convertView.findViewById(R.id.nombreclirec);
        TextView montorec          = (TextView) convertView.findViewById(R.id.montorec);
        TextView statusrec         = (TextView) convertView.findViewById(R.id.estatusrec);
        TextView fecharec          = (TextView) convertView.findViewById(R.id.fecharec);
        TextView vendedorrec       = (TextView) convertView.findViewById(R.id.vendedorrec);

        nrorec.setText(recibos.getNroRecibo());
        codclirec.setText(recibos.getCodigoCliente());
        nombreclirec.setText(recibos.getNombreCliente());
        montorec.setText(recibos.getMontoRecibo());
        statusrec.setText(recibos.getStatusRecibo());
        fecharec.setText(recibos.getFechaRecibo());
        vendedorrec.setText(recibos.getCodigoVendedor());




        return convertView;
    }
}
