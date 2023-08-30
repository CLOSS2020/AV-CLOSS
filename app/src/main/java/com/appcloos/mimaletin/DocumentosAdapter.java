package com.appcloos.mimaletin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class DocumentosAdapter extends BaseAdapter {
    public static String estatusMostrar;
    private final ArrayList<Documentos> listdocumentos;
    private final Context context;
    private LayoutInflater inflater;

    public DocumentosAdapter(Context context, ArrayList<Documentos> listdocumentos) {
        this.context = context;
        this.listdocumentos = listdocumentos;
    }

    @Override
    public int getCount() {
        return listdocumentos.size();
    }

    @Override
    public Object getItem(int i) {
        return listdocumentos.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final Documentos documentos = (Documentos) getItem(position);
        convertView = LayoutInflater.from(context).inflate(R.layout.item_documentos, null);

        TextView lb_nrodoc = convertView.findViewById(R.id.lb_nrodoc);
        TextView lb_tipodocv = convertView.findViewById(R.id.lb_tipodocv);
        TextView lb_estatus = convertView.findViewById(R.id.lb_estatus);
        TextView lb_montototal = convertView.findViewById(R.id.lb_montototal);
        TextView lb_emision = convertView.findViewById(R.id.lb_emision);
        TextView lb_recepcion = convertView.findViewById(R.id.lb_recepcion);
        TextView lb_aceptadev = convertView.findViewById(R.id.lb_aceptadev);

        String estatus = documentos.getEstatusdoc();

        switch (estatus) {

            case "0":
                estatusMostrar = "Pendiente p. pagar";
                break;

            case "1":
                estatusMostrar = "Abonado";
                break;


            case "2":
                estatusMostrar = "Totalmente Pago";
                break;

            case "3":
                estatusMostrar = "Anulado";
                break;
        }

        Double MtoFinal = documentos.getDtotalfinal();
        MtoFinal = Math.round(MtoFinal * 100.0) / 100.0;
        lb_nrodoc.setText(documentos.getDocumento());
        lb_tipodocv.setText(documentos.getTipodocv());
        lb_estatus.setText(estatusMostrar);
        lb_montototal.setText(MtoFinal + "$");
        lb_emision.setText(documentos.getEmision());
        lb_recepcion.setText(documentos.getRecepcion());
        lb_aceptadev.setText(documentos.getAceptadev());
        return convertView;
    }
}
