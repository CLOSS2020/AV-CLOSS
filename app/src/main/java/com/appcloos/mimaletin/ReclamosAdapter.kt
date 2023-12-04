package com.appcloos.mimaletin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.appcloos.mimaletin.databinding.ItemReclamoBinding

class ReclamosAdapter(
    private var listareclamo: ArrayList<Reclamo>,
    private var onClickListener: (Int) -> Unit
) : RecyclerView.Adapter<ReclamosAdapter.ViewHolderDatos?>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderDatos {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_reclamo, null, false)
        return ViewHolderDatos(view, onClickListener)
    }

    override fun onBindViewHolder(holder: ViewHolderDatos, position: Int) {
        holder.asignarDatos(listareclamo[position])
    }

    override fun getItemCount(): Int {
        return listareclamo.size
    }

    inner class ViewHolderDatos(itemView: View, var onClickListener: (Int) -> Unit) :
        RecyclerView.ViewHolder(itemView) {

        private val binding = ItemReclamoBinding.bind(itemView)

        fun asignarDatos(reclamo: Reclamo) {
            //aqui le asigno los valores a cada elemento del item layout
            binding.apply {
                tvCodclircl.text = reclamo.getCodcli()
                tvNombreclircl.text = reclamo.getNombrecli()
                tvCodrcl.text = reclamo.getNdoc()
                tvDocrcl.text = reclamo.getDocfac()
                tvStatusrcl.text = reclamo.getStatus()
                tvMontorcl.text = reclamo.getTotneto().toString() + "$"
                tvFcreado.text = reclamo.getFechadoc()
                tvFechamodifircl.text = reclamo.getFechamodifi()
            }

            setColors()

            binding.clContainer.setOnClickListener { onClickListener(absoluteAdapterPosition) }
        }

        private fun setColors() {
            binding.apply {
                textView8.setBackgroundColor(textView8.colorAgencia(Constantes.AGENCIA))
                tvDocrcl.setBackgroundColor(tvDocrcl.colorAgencia(Constantes.AGENCIA))
                textView10.setBackgroundColor(textView10.colorAgencia(Constantes.AGENCIA))
                tvStatusrcl.setBackgroundColor(tvStatusrcl.colorAgencia(Constantes.AGENCIA))
            }

        }

    }
}