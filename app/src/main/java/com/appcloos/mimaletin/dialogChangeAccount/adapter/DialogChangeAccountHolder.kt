package com.appcloos.mimaletin.dialogChangeAccount.adapter

import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Typeface
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.appcloos.mimaletin.R
import com.appcloos.mimaletin.databinding.ItemEmpresasBinding
import com.appcloos.mimaletin.dialogChangeAccount.model.keDataconex

class DialogChangeAccountHolder(view: View) : RecyclerView.ViewHolder(view) {

    val binding: ItemEmpresasBinding = ItemEmpresasBinding.bind(view)

    fun render(keDataconex: keDataconex, onClick: (Int) -> Unit) {
        val nightModeFlags = binding.llEmpresa.context.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK

        binding.checkEmpresa.apply {
            imageTintList =
                if (nightModeFlags == Configuration.UI_MODE_NIGHT_NO && keDataconex.selected) {
                    ColorStateList.valueOf(ContextCompat.getColor(context, R.color.blackColor1))
                } else if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES && keDataconex.selected) {
                    ColorStateList.valueOf(ContextCompat.getColor(context, R.color.whiteColor1))
                } else {
                    ColorStateList.valueOf(ContextCompat.getColor(context, R.color.grayPerfect))
                }
        }

        binding.tvNombreEmpresa.typeface = if (keDataconex.selected) {
            Typeface.DEFAULT_BOLD
        } else {
            Typeface.DEFAULT
        }

        binding.tvNombreEmpresa.text = keDataconex.kedNombre

        binding.llEmpresa.setOnClickListener { onClick(absoluteAdapterPosition) }
    }
}