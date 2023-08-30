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

public class ClienteAdapter extends RecyclerView.Adapter<ClienteAdapter.ViewHolderDatos> {

    ArrayList<Cliente> listacliente;
    ViewHolderDatos.onClienteListener onClienteListener;

    public ClienteAdapter(ArrayList<Cliente> listacliente,ViewHolderDatos.onClienteListener onClienteListener) {
        this.listacliente = listacliente;
        this.onClienteListener = onClienteListener;

    }

    @NonNull
    @Override
    public ViewHolderDatos onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cliente, null, false);
        return new ViewHolderDatos(view, onClienteListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderDatos holder, int position) {
        holder.asignarDatos(listacliente.get(position));
        
    }

    @Override
    public int getItemCount() {
        return listacliente.size();
    }

    public static class ViewHolderDatos extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView textcodigocliente;
        TextView textnombrecliente;
        TextView textdireccion;
        onClienteListener onClienteListener;

        public ViewHolderDatos(@NonNull View itemView, onClienteListener onClienteListener) {
            super(itemView);
             textcodigocliente = itemView.findViewById(R.id.text_codcliente);
             textnombrecliente = itemView.findViewById(R.id.text_nombrecliente);
             textdireccion     = itemView.findViewById(R.id.text_direccion);
             this.onClienteListener = onClienteListener;

             itemView.setOnClickListener(this);


        }

        public void asignarDatos(Cliente cliente) {
            textcodigocliente.setText("Código: "+ cliente.getCodigo());
            textnombrecliente.setText(cliente.getNombre());
            textdireccion.setText("Dirección: " + cliente.getDireccion());
        }

        @Override
        public void onClick(View view) {
            onClienteListener.onItemClick(getAdapterPosition());

        }

        public interface onClienteListener{
            void onItemClick(int position);
        }

    }


}
