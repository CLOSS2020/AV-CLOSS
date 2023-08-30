package com.appcloos.mimaletin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class PedidosArchivadosAdapter extends BaseAdapter {
    private final ArrayList<Pedidos> listpedidos;
    private final Context context;

    public PedidosArchivadosAdapter(Context context, ArrayList<Pedidos> listpedidos) {
        this.context = context;
        this.listpedidos = listpedidos;
    }


    @Override
    public int getCount() {
        return listpedidos.size();
    }

    @Override
    public Object getItem(int i) {
        return listpedidos.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //indicamos el item que va a inflar el adapter mediante el convertview
        convertView = LayoutInflater.from(context).inflate(R.layout.item_pedidos_archivados, null);

        final Pedidos pedidos = (Pedidos) getItem(position);
        TextView textCorrelativo = convertView.findViewById(R.id.tv_ndoc);
        TextView textNroPedido = convertView.findViewById(R.id.tv_nroped);
        TextView textNombreCliente = convertView.findViewById(R.id.tv_nombrecli);
        TextView textMontoPedido = convertView.findViewById(R.id.tv_monto);

        textCorrelativo.setText(pedidos.getNumeroDocumento());
        textNroPedido.setText(pedidos.getNumeroPedido());
        textNombreCliente.setText("Cliente: " + pedidos.getNombreCliente());
        textMontoPedido.setText("Monto Neto: $" + pedidos.getTotalNeto());
        return convertView;
    }
}
