package com.appcloos.mimaletin

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.appcloos.mimaletin.databinding.ItemDialogImgBinding

class DialogImgAdapter(
    private var imgList: List<Uri> = emptyList(),
    val onClickListener: (Int) -> Unit
) :
    RecyclerView.Adapter<DialogImgAdapter.DialogImgHolder>() {

    fun updateAdapter(newList: List<Uri>) {
        imgList = newList
        notifyDataSetChanged()
    }

    inner class DialogImgHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val binding = ItemDialogImgBinding.bind(view)

        fun render(img: Uri, onClickListener: (Int) -> Unit) {
            binding.apply {
                ivImagen.setImageURI(img)

                ibtnEliminar.setOnClickListener {
                    onClickListener(absoluteAdapterPosition)
                    notifyDataSetChanged()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DialogImgHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        return DialogImgHolder(
            layoutInflater.inflate(
                R.layout.item_dialog_img,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = imgList.size

    override fun onBindViewHolder(holder: DialogImgHolder, position: Int) {
        holder.render(imgList[position], onClickListener)
    }
}
