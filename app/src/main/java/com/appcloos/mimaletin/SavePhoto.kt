package com.appcloos.mimaletin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class SavePhoto(val activity: AppCompatActivity) {

    lateinit var listaImagenes: MutableList<Uri>

    fun findPhoto() {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_GET_CONTENT
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            type = "image/jpeg"
        }

        val shareIntent = Intent.createChooser(sendIntent, "SELECCIONA LAS IMAGENES")
        return resultLauncher.launch(shareIntent)
    }

    private var resultLauncher =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data: Intent? = result.data
                lateinit var imageUri: Uri

                try {
                    val clipData = data!!.clipData

                    if (clipData == null) {
                        imageUri = data.data!!
                        listaImagenes.add(imageUri)
                    } else {
                        for (i in 0 until clipData.itemCount) {
                            listaImagenes.add(clipData.getItemAt(i).uri)
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    listaImagenes = mutableListOf()
                }

            }
        }

}