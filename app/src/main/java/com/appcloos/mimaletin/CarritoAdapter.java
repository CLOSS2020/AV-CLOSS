/*Clase....: CarritoAdapter
 * Autor.......: PCV MAR 2021
 * Objetivo....: provee un adaptador personalizado en base al XML diseñado que sirve
 * para el listview que disponga del metodo
 * Notas.......:
 *
 * Parámetros..: -- Ninguno --
 *
 * Modif.......:
 *
 * NOTAS.......: adaptador enfocado a ListView ( no se ha probado en RecyclerView)
 *
 * Retorna.....: Ninguno
 *-------------**/



package com.appcloos.mimaletin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class CarritoAdapter extends BaseAdapter {
    private ArrayList<Carrito> listcarrito;
    private Context context;
    private LayoutInflater inflater;

    //tendra como parametro el contexto donde se use y el arraylist de tipo carrito que se use en
    //ese momento
    public CarritoAdapter(Context context, ArrayList<Carrito> listcarrito){
        this.context = context;
        this.listcarrito = listcarrito;
    }

    //metodos nativos del BaseAdapter
    @Override
    public int getCount() {
        return listcarrito.size();

    }

    @Override
    public Object getItem(int i) {
        return listcarrito.get(i);

    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //en cada item del adaptador, defino una serie de elementos de acuerdo a como se encuentra
        //diseñado el XML y los valores que le seran asignados.
        final Carrito carrito = (Carrito) getItem(position);
        convertView = LayoutInflater.from(context).inflate(R.layout.item_catalogo, null);

        TextView textcodigo              = (TextView) convertView.findViewById(R.id.text_codigo);
        TextView textnombre              = (TextView) convertView.findViewById(R.id.text_nombre);
        TextView textexistencia          = (TextView) convertView.findViewById(R.id.text_existencia);
        TextView textprecio              = (TextView) convertView.findViewById(R.id.text_precio);
        TextView textdescuento           = (TextView) convertView.findViewById(R.id.tv_descuento);
        TextView textpreciodescuento     = (TextView) convertView.findViewById(R.id.tv_preciodescuento);
        //TextView textDescuentoNormal     = (TextView) convertView.findViewById(R.id.tv_dsctonormal);
        Double dctonumerico              = carrito.getDctolin();
        ImageView img_thumb              = convertView.findViewById(R.id.img_thumb);

        textcodigo.setText("Código: "+ carrito.getCodigo());
        textnombre.setText(carrito.getNombre());
        textexistencia.setText("Cantidad: " +carrito.getCantidad());

        Double precio = carrito.getPrecio();
        textprecio.setText("Precio: $"+ precio.toString());

        Double montoDsctoNormal      = precio * 0.20;
        Double precioConDsctoNormal  = precio - montoDsctoNormal;
        precioConDsctoNormal = Math.round(precioConDsctoNormal*100.00)/100.00;
        //textDescuentoNormal.setText("Precio Dscto: $" + precioConDsctoNormal.toString());


        if(dctonumerico  > 0.0) {
            Double dctoaplicar  = dctonumerico  / 100;
            Double mtodescuento = precio * dctoaplicar;
            Double preciocondescuento = precio - mtodescuento;

            preciocondescuento = Math.round(preciocondescuento * 100.00) / 100.00;
            textdescuento.setVisibility(View.VISIBLE);
            textdescuento.setText("-" + dctonumerico  + "%");
            textpreciodescuento.setVisibility(View.VISIBLE);
            textpreciodescuento.setText("Precio con desc: $"+ preciocondescuento.toString());

        }
        //generacion de la miniatura de la imagen --
        String codigo    = carrito.codigo;
        String enlace    = "https://www.cloccidental.com/img/"+codigo+".jpg"; //este enlace debe parametrizarse despues
        Picasso.get().load(enlace).resize(100, 100).centerCrop().into(img_thumb); //cargo la imagen en cada objeto img

        return convertView;
    }
}
