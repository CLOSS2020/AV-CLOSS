package com.appcloos.mimaletin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ReclamosAdapter extends RecyclerView.Adapter<ReclamosAdapter.ViewHolderDatos> {

    ArrayList<Reclamo> listareclamo;
    ViewHolderDatos.onReclamoListener onReclamoListener;

    public ReclamosAdapter(ArrayList<Reclamo> listareclamo,ViewHolderDatos.onReclamoListener onReclamoListener) {
        this.listareclamo = listareclamo;
        this.onReclamoListener = onReclamoListener;

    }

    @NonNull
    @Override
    public ViewHolderDatos onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reclamo, null, false);
        return new ViewHolderDatos(view, onReclamoListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderDatos holder, int position) {
        holder.asignarDatos(listareclamo.get(position));

    }

    @Override
    public int getItemCount() {
        return listareclamo.size();
    }

    public static class ViewHolderDatos extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView textcodigocliente, textnombrecliente, textcodigoreclamo, textdocumento, textstatus,
                textmontoreclamo,  textfechacreado, textfechamodifi;

        onReclamoListener onReclamoListener;

        public ViewHolderDatos(@NonNull View itemView, onReclamoListener onReclamoListener) {
            super(itemView);

            textcodigocliente = (TextView) itemView.findViewById(R.id.tv_codclircl);
            textnombrecliente = (TextView) itemView.findViewById(R.id.tv_nombreclircl);
            textcodigoreclamo = (TextView) itemView.findViewById(R.id.tv_codrcl);
            textdocumento     = (TextView) itemView.findViewById(R.id.tv_docrcl);
            textstatus        = (TextView) itemView.findViewById(R.id.tv_statusrcl);
            textmontoreclamo  = (TextView) itemView.findViewById(R.id.tv_montorcl);
            textfechacreado   = (TextView) itemView.findViewById(R.id.tv_fcreado);
            textfechamodifi   = (TextView) itemView.findViewById(R.id.tv_fechamodifircl);

            this.onReclamoListener = onReclamoListener;

            itemView.setOnClickListener(this);


        }

        public void asignarDatos(Reclamo reclamo) {
            //aqui le asigno los valores a cada elemento del item layout
            textcodigocliente.setText(reclamo.getCodcli());
            textnombrecliente.setText(reclamo.getNombrecli());
            textcodigoreclamo.setText(reclamo.getNdoc());
            textdocumento.setText(reclamo.getDocfac());
            textstatus.setText(reclamo.getStatus());
            textmontoreclamo.setText(reclamo.getTotneto().toString() + "$");
            textfechacreado.setText(reclamo.getFechadoc());
            textfechamodifi.setText(reclamo.getFechamodifi());


        }

        @Override
        public void onClick(View view) {
            onReclamoListener.onItemClick(getAdapterPosition());

        }

        public interface onReclamoListener{
            void onItemClick(int position);
        }

    }


}