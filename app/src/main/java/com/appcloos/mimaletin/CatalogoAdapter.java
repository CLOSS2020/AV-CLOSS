package com.appcloos.mimaletin;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.ArrayList;

public class CatalogoAdapter extends BaseAdapter {
    private ArrayList<Catalogo> listcatalogo;
    private Context context;
    private LayoutInflater inflater;
    private ConstraintLayout it_backcatalogo;

    public CatalogoAdapter(Context context, ArrayList<Catalogo> listcatalogo) {
        this.context = context;
        this.listcatalogo = listcatalogo;
    }

    @Override
    public int getCount() {
        return listcatalogo.size();

    }

    @Override
    public Object getItem(int i) {
        return listcatalogo.get(i);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final Catalogo catalogo = (Catalogo) getItem(position);
        convertView = LayoutInflater.from(context).inflate(R.layout.item_catalogo, null);

        TextView tvMin                   = (TextView) convertView.findViewById(R.id.tv_min);
        TextView tvMax                   = (TextView) convertView.findViewById(R.id.tv_max);
        TextView textcodigo              = (TextView) convertView.findViewById(R.id.text_codigo);
        TextView textnombre              = (TextView) convertView.findViewById(R.id.text_nombre);
        TextView textexistencia          = (TextView) convertView.findViewById(R.id.text_existencia);
        TextView textprecio              = (TextView) convertView.findViewById(R.id.text_precio);
        TextView textdescuento           = (TextView) convertView.findViewById(R.id.tv_descuento);
        TextView textpreciodescuento     = (TextView) convertView.findViewById(R.id.tv_preciodescuento);
        TextView textpreventa            = (TextView) convertView.findViewById(R.id.tv_preventalb);
        //TextView textDescuentoNormal     = (TextView) convertView.findViewById(R.id.tv_dsctonormal);
        TextView tv_multiplo             = (TextView) convertView.findViewById(R.id.tv_multiplo);
        TextView tvPedFacNE              = (TextView) convertView.findViewById(R.id.tvPedFacNE);
        ConstraintLayout it_backcatalogo = convertView.findViewById(R.id.it_backcatalogo);
        ImageView img_thumb              = convertView.findViewById(R.id.img_thumb);


        String nuevoKardex               = catalogo.getCodigoKardex();
        Double dctonumerico              = catalogo.getDctotope();


        if(nuevoKardex !=null){
            it_backcatalogo.setBackgroundColor(Color.rgb(212,243,222));
        }

        //valores enteros para mostrar las cantidades minimas y maximas de cada articulo
        int minimo = (int) Math.round(catalogo.getVta_min());
        int maximo = (int) Math.round(catalogo.getVta_max());
        int multi = catalogo.getMultiplo();

        textcodigo.setText("Código: "+ catalogo.getCodigo());
        textnombre.setText(catalogo.getNombre());
        textexistencia.setText("Existencia: " +catalogo.getExistencia()+"");

        Double precio = catalogo.getPrecio1();
        Double montoDsctoNormal      = precio * 0.20;
        Double precioConDsctoNormal  = precio - montoDsctoNormal;
        precioConDsctoNormal = Math.round(precioConDsctoNormal*100.00)/100.00;

        textprecio.setText("Precio: $"+ precio.toString());
        //textDescuentoNormal.setText("Precio Dscto: $" + precioConDsctoNormal.toString());


        if(maximo > 0){
            tvMax.setVisibility(View.VISIBLE);
            tvMax.setText("Cant. Máxima: " + maximo);
        }

        if(minimo > 0 && multi == 0){
            tvMin.setVisibility(View.VISIBLE);
            tvMin.setText("Cant. Mínima: " + minimo);
        } else if (minimo > 0 && multi == 1){
            tv_multiplo.setVisibility(View.VISIBLE);
            tv_multiplo.setText("Emp de " + minimo + " Unds");
        }

        //validacion del label preventa
        String preventa = catalogo.getEnpreventa();


        if(preventa == null || preventa.equals("")) {
            preventa = "0";
        }


        if(preventa.equals("1")){
            textpreventa.setVisibility(View.VISIBLE);
        } else if(preventa.equals("0")){
            textpreventa.setVisibility(View.INVISIBLE);
        }

        if(dctonumerico  > 0.0) {
            Double dctoaplicar  = dctonumerico  / 100;
            Double mtodescuento = precio * dctoaplicar;
            Double preciocondescuento = precio - mtodescuento;

            preciocondescuento = Math.round(preciocondescuento * 100.00) / 100.00;
            textdescuento.setVisibility(View.VISIBLE);
            textdescuento.setText("-" + dctonumerico  + "%");
            textpreciodescuento.setVisibility(View.VISIBLE);
            textpreciodescuento.setText("Precio con desc: $"+ preciocondescuento);
        }

        if (catalogo.vta_solofac == 1){
            tvPedFacNE.setVisibility(View.VISIBLE);
            tvPedFacNE.setText("Disponible para FAC");
            tvPedFacNE.setBackgroundResource(R.drawable.custom_label_solo_fac);
        }else if (catalogo.vta_solone == 1){
            tvPedFacNE.setVisibility(View.VISIBLE);
            tvPedFacNE.setText("Disponible para N/E");
            tvPedFacNE.setBackgroundResource(R.drawable.custom_label_solo_ne);
        }else{
            tvPedFacNE.setVisibility(View.INVISIBLE);
        }

        //generacion de la miniatura de la imagen --
        String codigo    = catalogo.codigo;
        String enlace    = "https://www.cloccidental.com/img/"+codigo+".jpg"; //este enlace debe parametrizarse despues
        Picasso.get().load(enlace).resize(100, 100).centerCrop().into(img_thumb); //cargo la imagen en cada objeto img


        return convertView;
    }
}
