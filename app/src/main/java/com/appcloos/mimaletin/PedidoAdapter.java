package com.appcloos.mimaletin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class PedidoAdapter extends BaseAdapter {
    private final ArrayList<Pedidos> listpedidos;
    private final Context context;
    private LayoutInflater inflater;

    public PedidoAdapter(Context context, ArrayList<Pedidos> listpedidos) {
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

        final Pedidos pedidos = (Pedidos) getItem(position);
        convertView = LayoutInflater.from(context).inflate(R.layout.item_pedido, null);

        TextView textcodigoped = convertView.findViewById(R.id.txt_codinternopedido);
        TextView textnroped = convertView.findViewById(R.id.txt_numeroped);
        TextView textcodigocliente = convertView.findViewById(R.id.txt_codclienteped);
        TextView textnombrecliente = convertView.findViewById(R.id.txt_nombrecliente);
        TextView textmontoneto = convertView.findViewById(R.id.txt_montonetoped);
        TextView textestatusped = convertView.findViewById(R.id.txt_estatusped);
        TextView textfechapedido = convertView.findViewById(R.id.txt_fechapedcreado);
        TextView textmontonetodcto = convertView.findViewById(R.id.txt_montonetodcto);

        Double montoNeto = pedidos.getTotalNeto();
        montoNeto = (montoNeto * 100.00) / 100.00;

        Double montoNetoconDcto = pedidos.getTotalNetoDcto();
        montoNetoconDcto = (montoNetoconDcto * 100.00) / 100.00;

        double diferencia = montoNeto - montoNetoconDcto;

        if(diferencia > 0.0){
            textmontonetodcto.setVisibility(View.VISIBLE);
            textmontonetodcto.setText("Monto con Dscto: $" + montoNetoconDcto);
        }

        textcodigoped.setText("COD: " + pedidos.getNumeroDocumento());
        textnroped.setText("Nº " + pedidos.getNumeroPedido());
        textcodigocliente.setText("Cliente: " + pedidos.getCodigoCliente());
        textnombrecliente.setText(pedidos.getNombreCliente());
        textmontoneto.setText("Monto Neto: $" + pedidos.getTotalNeto());
        textestatusped.setText("Estatus: " + pedidos.getEstatus());
        textfechapedido.setText("Fecha de Creación: " + pedidos.getFechaDocumento());

        return convertView;
    }
}
