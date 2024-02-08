package com.appcloos.mimaletin

import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import com.appcloos.mimaletin.databinding.ItemCatalogoBinding
import com.squareup.picasso.Picasso

class CatalogoAdapter(
    private val context: Context,
    private val listcatalogo: ArrayList<Catalogo>,
    private val enlaceEmpresa: String,
    private val codEmpresa: String
) : BaseAdapter() {
    private val inflater: LayoutInflater? = null
    private val it_backcatalogo: ConstraintLayout? = null
    override fun getCount(): Int {
        return listcatalogo.size
    }

    override fun getItem(i: Int): Any {
        return listcatalogo[i]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        // var convertView = convertView
        val catalogo = getItem(position) as Catalogo

        val convertView = LayoutInflater.from(parent!!.context).inflate(R.layout.item_catalogo, null)

        val binding = ItemCatalogoBinding.bind(convertView!!)

        // TextView textDescuentoNormal     = (TextView) convertView.findViewById(R.id.tv_dsctonormal);

        val nuevoKardex = catalogo.getCodigoKardex()
        val dctonumerico = catalogo.getDctotope()
        if (nuevoKardex != null) {
            binding.itBackcatalogo.setBackgroundColor(Color.rgb(212, 243, 222))
        }

        // valores enteros para mostrar las cantidades minimas y maximas de cada articulo
        val minimo = Math.round(catalogo.getVta_min()).toInt()
        val maximo = Math.round(catalogo.getVta_max()).toInt()
        val multi = catalogo.getMultiplo()
        binding.textCodigo.text = "Código: " + catalogo.getCodigo()
        binding.textNombre.text = catalogo.getNombre()
        binding.textExistencia.text = "Existencia: " + catalogo.getExistencia() + ""
        if (catalogo.getReferencia() != null && catalogo.getReferencia() != "") {
            binding.tvRefernacia.visibility = View.VISIBLE
            binding.tvRefernacia.text = "Referencia: " + catalogo.getReferencia()
        } else {
            binding.tvRefernacia.visibility = View.INVISIBLE
        }
        val precio = catalogo.getPrecio1()
        val montoDsctoNormal = precio * 0.20
        var precioConDsctoNormal = precio - montoDsctoNormal
        precioConDsctoNormal = Math.round(precioConDsctoNormal * 100.00) / 100.00
        binding.textPrecio.text = "Precio: $$precio"
        // textDescuentoNormal.setText("Precio Dscto: $" + precioConDsctoNormal.toString());
        if (maximo > 0) {
            binding.tvMax.visibility = View.VISIBLE
            binding.tvMax.text = "Cant. Máxima: $maximo"
        }
        if (minimo > 0 && multi == 0) {
            binding.tvMin.visibility = View.VISIBLE
            binding.tvMin.text = "Cant. Mínima: $minimo"
        } else if (minimo > 0 && multi == 1) {
            binding.tvMultiplo.visibility = View.VISIBLE
            binding.tvMultiplo.text = "Emp de $minimo Unds"
        }

        // validacion del label preventa
        var preventa = catalogo.getEnpreventa()
        if (preventa == null || preventa == "") {
            preventa = "0"
        }
        if (preventa == "1") {
            binding.tvPreventalb.visibility = View.VISIBLE
        } else if (preventa == "0") {
            binding.tvPreventalb.visibility = View.INVISIBLE
        }
        if (dctonumerico > 0.0) {
            val dctoaplicar = dctonumerico / 100
            val mtodescuento = precio * dctoaplicar
            var preciocondescuento = precio - mtodescuento
            preciocondescuento = Math.round(preciocondescuento * 100.00) / 100.00
            binding.tvDescuento.visibility = View.VISIBLE
            binding.tvDescuento.text = "Promo -$dctonumerico%"
            binding.tvPreciodescuento.visibility = View.VISIBLE
            binding.tvPreciodescuento.text = "Precio desc: $$preciocondescuento"
        }
        if (catalogo.vta_solofac == 1) {
            binding.tvPedFacNE.visibility = View.VISIBLE
            binding.tvPedFacNE.text = "Disponible para FAC"
            binding.tvPedFacNE.setBackgroundResource(R.drawable.custom_label_solo_fac)
        } else if (catalogo.vta_solone == 1) {
            binding.tvPedFacNE.visibility = View.VISIBLE
            binding.tvPedFacNE.text = "Disponible para N/E"
            binding.tvPedFacNE.setBackgroundResource(R.drawable.custom_label_solo_ne)
        } else {
            binding.tvPedFacNE.visibility = View.INVISIBLE
        }

        // generacion de la miniatura de la imagen --
        val codigo = catalogo.codigo
        val enlace =
            "https://$enlaceEmpresa/img/$codigo.jpg" // este enlace debe parametrizarse despues
        Picasso.get().load(enlace).resize(100, 100).centerCrop()
            .into(binding.imgThumb) // cargo la imagen en cada objeto img

        binding.imgThumb.setOnClickListener {
            val imagen = ImageView(binding.imgThumb.context)
            Picasso.get().load(enlace).resize(1000, 1000).centerCrop().into(imagen)

            // este builder mostrara la ficha del articulo
            val ventana = AlertDialog.Builder(
                ContextThemeWrapper(
                    binding.imgThumb.context,
                    binding.imgThumb.setAlertDialogTheme(codEmpresa)
                )
            )
            ventana.setTitle("Imagen del articulo")
            ventana.setView(imagen)
            ventana.setPositiveButton("Aceptar", null)
            val dialogo = ventana.create()
            dialogo.show() //

            val pbutton: Button = dialogo.getButton(DialogInterface.BUTTON_POSITIVE)
            pbutton.apply {
                setTextColor(colorTextAgencia(codEmpresa))
            }
        }

        return binding.root
    }
}
