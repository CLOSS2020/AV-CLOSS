package com.appcloos.mimaletin

import android.app.Activity
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.icu.text.DateFormat
import android.icu.text.SimpleDateFormat
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.appcloos.mimaletin.Constantes.CLO
import com.appcloos.mimaletin.Constantes.WOKIN
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream
import java.text.DecimalFormat
import java.util.Date
import java.util.Locale
import java.util.Random
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt


fun Activity.color(@ColorRes color: Int): Int = ContextCompat.getColor(this, color)

fun View.color(@ColorRes color: Int): Int = ContextCompat.getColor(context, color)

fun String.formatoFechaShow(): String {
    val fechaDate: Date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(this)
    val dateFormat: DateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return dateFormat.format(fechaDate)
}

fun String.formatoFechaTiempoShow(): String {
    val fechaDate: Date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(this)
    val dateFormat: DateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
    return dateFormat.format(fechaDate)
}

fun Activity.toast(text: String, length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, text, length).show()
}

fun Fragment.toast(text: String, length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(context, text, length).show()
}

fun Fragment.snackBar(text: String, length: Int = Toast.LENGTH_SHORT) {
    Snackbar.make(this.requireView(), text, length).show()
}


fun Any?.isNull(): Boolean = this == null

fun ImageView.setImageURL(url: String) {
    if (url.isEmpty()) {
        Picasso.get().load(url).into(this);
    }
}

//Formater de numeros decimales
fun Double.formatoNum(): String = DecimalFormat("####.00").format(this)

fun Int.formatoNum(): String = DecimalFormat("####").format(this)

fun Double.valorReal(): Double = (this * 100.00).roundToInt() / 100.00

fun Canvas.insertarNumPDF(num: Double, x: Float, y: Float, paint: Paint) {
    val numero = num.valorReal()

    //Redondeo del numero dado hacia abajo
    val numeroInt = floor(abs(numero)).toInt().formatoNum()

    //Formateo del numero dado para que los ceros a la derecha del punto decimal se tomen
    //Y captura de los dos decimales
    val decimales = numero.formatoNum().getLastN(2)

    //Imprecion del numero dado sin decimales
    paint.textAlign = Paint.Align.RIGHT
    this.drawText(numeroInt, x + 25, y, paint)
    //Impresion del punto decimal
    this.drawText(".", x + 28, y, paint)
    //Imprecion de los decimales del numero dado
    paint.textAlign = Paint.Align.LEFT
    this.drawText(decimales, x + 28, y, paint)
}

fun String.getLastN(num: Int) = this.substring(kotlin.math.max(0, this.length - num))

fun String.toDate(): Date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(this)

fun Double.toTwoDecimals(): String = DecimalFormat("#,##0.00").format(this.valorReal())

fun Double.toCeroDecimals(): String = DecimalFormat("#,##0").format(this.valorReal())


fun TextView.alertError() {
    this.apply {
        setBackgroundResource(R.drawable.border_radius_error)
        setTextColor(color(R.color.white))
    }
}

fun TextView.alertWarning() {
    this.apply {
        setBackgroundResource(R.drawable.border_radius_warning)
        setTextColor(color(R.color.white))
    }
}

fun TextView.alertNormal() {
    this.apply {
        setBackgroundResource(R.drawable.border_radius)
        setTextColor(color(R.color.textColor))
    }
}

fun String.log() = Log.i("Informacion de Prueba", this)

fun Activity.log(text: String) = Log.i("Informacion de Prueba", text)

fun <T> T.random(): Int {
    val random = Random()
    return random.nextInt(1000 - 0) + 0
}

fun Bitmap.redimensionarImagen(anchoNuevo: Float, altoNuevo: Float): Bitmap? {
    val bitmap = this
    val ancho = bitmap.width
    val alto = bitmap.height
    try {
        if (ancho > alto) {
            return if (ancho > anchoNuevo || alto > altoNuevo) {
                val escalaAncho = anchoNuevo / ancho
                val escalaAlto = altoNuevo / alto
                val matrix = Matrix()
                matrix.postScale(escalaAncho, escalaAlto)
                Bitmap.createBitmap(bitmap, 0, 0, ancho, alto, matrix, false)
            } else {
                bitmap
            }
        } else if (alto > ancho) {
            return if (ancho > anchoNuevo || alto > altoNuevo) {
                val escalaAncho = anchoNuevo / ancho
                val escalaAlto = altoNuevo / alto
                val matrix = Matrix()
                matrix.postScale(escalaAncho, escalaAlto)
                Bitmap.createBitmap(bitmap, 0, 0, ancho, alto, matrix, false)
            } else {
                bitmap
            }
            //si los anchos y altos son iguales
        } else if (alto == ancho) {
            return if (ancho > anchoNuevo || alto > altoNuevo) {
                val escalaAncho = anchoNuevo / ancho
                val escalaAlto = altoNuevo / alto
                val matrix = Matrix()
                matrix.postScale(escalaAncho, escalaAlto)
                Bitmap.createBitmap(bitmap, 0, 0, ancho, alto, matrix, false)
            } else {
                bitmap
            }
        }//UPDATE usuarios SET ult_sinc = '2023-05-30 08:37:25', version = '1.1.1' WHERE vendedor = 'G98';
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }
    return bitmap
}

fun Bitmap.convertirUriToBase64(): String? {
    val baos = ByteArrayOutputStream()
    this.compress(Bitmap.CompressFormat.JPEG, 100, baos)
    val bytes = baos.toByteArray()
    return Base64.encodeToString(bytes, Base64.DEFAULT)
}

fun Activity.windowsColor(agencia: String) {
    val window = this.window
    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    val nightModeFlags = this.resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK
    if (nightModeFlags == Configuration.UI_MODE_NIGHT_NO && agencia == "081196") {
        window.statusBarColor = ContextCompat.getColor(this, R.color.primaryColor)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.primaryColor)
    } else if (nightModeFlags == Configuration.UI_MODE_NIGHT_NO && agencia == "170122") {
        window.statusBarColor = ContextCompat.getColor(this, R.color.wokinPrimaryColor)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.wokinColor3)
    } else {
        window.statusBarColor = ContextCompat.getColor(this, R.color.blackColor1)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.blackColor2)
    }
}

fun View.setDrawableAgencia(agencia: String) {
    if (agencia == CLO) {
        this.setBackgroundResource(R.drawable.bordes_redondos)
    } else if (agencia == WOKIN) {
        this.setBackgroundResource(R.drawable.wokin_bordes_redondos)
    }
}

fun View.setDrawableHeadAgencia(agencia: String) {
    if (agencia == CLO) {
        this.setBackgroundResource(R.drawable.border_radius_botr_botl)
    } else if (agencia == WOKIN) {
        this.setBackgroundResource(R.drawable.wokin_border_radius_botr_botl)
    }
}

fun View.setDrawableHeadVariantAgencia(agencia: String) {
    if (agencia == CLO) {
        this.setBackgroundResource(R.drawable.border_item_edo_general_cxc)
    } else if (agencia == WOKIN) {
        this.setBackgroundResource(R.drawable.wokin_border_item_edo_general_cxc)
    }
}

fun View.setDrawableVariantAgencia(agencia: String) {
    if (agencia == CLO) {
        this.setBackgroundResource(R.drawable.border_radius_secondary_color)
    } else if (agencia == WOKIN) {
        this.setBackgroundResource(R.drawable.wokin_border_radius_secondary_color)
    }
}

fun View.setDrawableCobranzaAgencia(agencia: String) {
    if (agencia == CLO) {
        this.setBackgroundResource(R.drawable.fondoamarillo)
    } else if (agencia == WOKIN) {
        this.setBackgroundResource(R.drawable.wokin_fondoamarillo)
    }
}

fun View.setDrawableCobranzaVariantAgencia(agencia: String) {
    if (agencia == CLO) {
        this.setBackgroundResource(R.drawable.fondototal)
    } else if (agencia == WOKIN) {
        this.setBackgroundResource(R.drawable.wokin_fondototal)
    }
}

fun View.colorAgencia(agencia: String): Int {
    val nightModeFlags = this.resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK
    val retorno: Int = if (nightModeFlags == Configuration.UI_MODE_NIGHT_NO && agencia == CLO) {
        ContextCompat.getColor(context, R.color.primaryColor)
    } else if (nightModeFlags == Configuration.UI_MODE_NIGHT_NO && agencia == WOKIN) {
        ContextCompat.getColor(context, R.color.wokinPrimaryColor)
    } else {
        ContextCompat.getColor(context, R.color.blackColor2)
    }

    return retorno
}

fun View.colorVariantAgencia(agencia: String): Int {
    val nightModeFlags = this.resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK
    val retorno: Int = if (nightModeFlags == Configuration.UI_MODE_NIGHT_NO && agencia == CLO) {
        ContextCompat.getColor(context, R.color.colorVariantLite)
    } else if (nightModeFlags == Configuration.UI_MODE_NIGHT_NO && agencia == WOKIN) {
        ContextCompat.getColor(context, R.color.wokinColorVariantLite)
    } else {
        ContextCompat.getColor(context, R.color.blackColor2)
    }

    return retorno
}

fun View.colorAccentAgencia(agencia: String): Int {
    val nightModeFlags = this.resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK
    val retorno: Int = if (nightModeFlags == Configuration.UI_MODE_NIGHT_NO && agencia == CLO) {
        ContextCompat.getColor(context, R.color.rojologo)
    } else if (nightModeFlags == Configuration.UI_MODE_NIGHT_NO && agencia == WOKIN) {
        ContextCompat.getColor(context, R.color.wokinAccentColor)
    } else {
        ContextCompat.getColor(context, R.color.whiteColor4)
    }

    return retorno
}

fun View.colorTextAgencia(agencia: String): Int {
    val nightModeFlags = this.resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK
    val retorno: Int = if (nightModeFlags == Configuration.UI_MODE_NIGHT_NO && agencia == CLO) {
        ContextCompat.getColor(context, R.color.primaryColor)
    } else if (nightModeFlags == Configuration.UI_MODE_NIGHT_NO && agencia == WOKIN) {
        ContextCompat.getColor(context, R.color.wokinPrimaryColor)
    } else if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES && agencia == CLO) {
        ContextCompat.getColor(context, R.color.md_theme_dark_primary)
    } else if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES && agencia == WOKIN) {
        ContextCompat.getColor(context, R.color.md_theme_dark_primary_wokin)
    } else {
        ContextCompat.getColor(context, R.color.whiteColor4)
    }

    return retorno
}

fun View.setColorCheckBox(agencia: String): ColorStateList {
    val nightModeFlags = this.resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK
    val retorno: ColorStateList =
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_NO && agencia == CLO) {
            ColorStateList.valueOf(ContextCompat.getColor(context, R.color.primaryColor))
        } else if (nightModeFlags == Configuration.UI_MODE_NIGHT_NO && agencia == WOKIN) {
            ColorStateList.valueOf(ContextCompat.getColor(context, R.color.wokinAccentColor))
        } else {
            ColorStateList.valueOf(ContextCompat.getColor(context, R.color.whiteColor4))
        }

    return retorno
}

fun View.setColorRadioButon(agencia: String): ColorStateList? {
    //Estos ya tienen su modo oscuro por xml


    val retorno: ColorStateList? =
        when (agencia) {
            CLO -> {
                ContextCompat.getColorStateList(context, R.color.radio_button)
            }

            WOKIN -> {
                ContextCompat.getColorStateList(context, R.color.wokin_radio_button)
            }

            else -> {
                ContextCompat.getColorStateList(context, R.color.radio_button)
            }
        }
    return retorno
}

fun Resources.Theme.getTheme(agencia: String) {
    return if (agencia == CLO) {
        this.applyStyle(R.style.BasicStyleButtomMaterialReten, true)
    } else {
        this.applyStyle(R.style.BasicStyleButtomMaterialRetenWokin, true)
    }
}

fun Activity.setProgressDialogTheme(agencia: String): Int {
    val nightModeFlags = this.resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK

    val retorno: Int = if (nightModeFlags == Configuration.UI_MODE_NIGHT_NO && agencia == CLO) {
        R.style.ProgressDialogCustom
    } else if (nightModeFlags == Configuration.UI_MODE_NIGHT_NO && agencia == WOKIN) {
        R.style.ProgressDialogCustomWokin
    } else {
        R.style.ProgressDialogCustom
    }
    return retorno
}

fun Activity.setAlertDialogTheme(agencia: String): Int {
    val nightModeFlags = this.resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK

    val retorno: Int = if (nightModeFlags == Configuration.UI_MODE_NIGHT_NO && agencia == CLO) {
        R.style.AlertDialogCustom
    } else if (nightModeFlags == Configuration.UI_MODE_NIGHT_NO && agencia == WOKIN) {
        R.style.AlertDialogCustomWokin
    } else {
        R.style.AlertDialogCustom
    }
    return retorno
}

fun Activity.setEditTextTheme(agencia: String): Int {
    val nightModeFlags = this.resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK

    val retorno: Int = if (nightModeFlags == Configuration.UI_MODE_NIGHT_NO && agencia == CLO) {
        R.style.EditTextStyleCustom
    } else if (nightModeFlags == Configuration.UI_MODE_NIGHT_NO && agencia == WOKIN) {
        R.style.EditTextStyleCustomWokin
    } else {
        R.style.EditTextStyleCustom
    }
    return retorno
}

fun Activity.setCheckBoxTheme(agencia: String): Int {
    val nightModeFlags = this.resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK

    val retorno: Int = if (nightModeFlags == Configuration.UI_MODE_NIGHT_NO && agencia == CLO) {
        R.style.BasicStyleCheckBox
    } else if (nightModeFlags == Configuration.UI_MODE_NIGHT_NO && agencia == WOKIN) {
        R.style.BasicStyleCheckBoxWokin
    } else {
        R.style.BasicStyleCheckBox
    }
    return retorno
}

fun Activity.setThemeAgencia(agencia: String): Int {
    val nightModeFlags = this.resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK

    val retorno: Int = if (nightModeFlags == Configuration.UI_MODE_NIGHT_NO && agencia == CLO) {
        R.style.AppTheme
    } else if (nightModeFlags == Configuration.UI_MODE_NIGHT_NO && agencia == WOKIN) {
        R.style.AppThemeWokin
    } else {
        R.style.AppTheme
    }
    return retorno
}

fun Activity.setThemeNoBarAgencia(agencia: String): Int {
    val nightModeFlags = this.resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK

    val retorno: Int = if (nightModeFlags == Configuration.UI_MODE_NIGHT_NO && agencia == CLO) {
        R.style.BasicThemeBasic
    } else if (nightModeFlags == Configuration.UI_MODE_NIGHT_NO && agencia == WOKIN) {
        R.style.BasicThemeBasicWokin
    } else {
        R.style.BasicThemeBasic
    }
    return retorno
}

fun Activity.setThemeNoBarCXCAgencia(agencia: String): Int {
    val nightModeFlags = this.resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK

    val retorno: Int = if (nightModeFlags == Configuration.UI_MODE_NIGHT_NO && agencia == CLO) {
        R.style.AppThemeCXC
    } else if (nightModeFlags == Configuration.UI_MODE_NIGHT_NO && agencia == WOKIN) {
        R.style.AppThemeCXCWokin
    } else {
        R.style.AppThemeCXC
    }
    return retorno
}

fun View.setBoxStrokeColorInputText(agencia: String): ColorStateList? {

    val retorno: ColorStateList? =
        when (agencia) {
            CLO -> {
                ContextCompat.getColorStateList(context, R.color.input_box_stroke)
            }

            WOKIN -> {
                ContextCompat.getColorStateList(context, R.color.wokin_input_box_stroke)
            }

            else -> {
                ContextCompat.getColorStateList(context, R.color.input_box_stroke)
            }
        }

    return retorno
}

fun MaterialButton.setColorModelVariant(agencia: String) {
    val nightModeFlags = this.resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK

    this.apply {
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_NO) {
            setBackgroundColor(Color.WHITE)
        } else {
            setBackgroundColor(this.color(R.color.blackColor4))
        }
        setTextColor(colorTextAgencia(agencia))
        strokeColor = ColorStateList.valueOf(colorTextAgencia(agencia))
        strokeWidth = 3
    }
}

fun TextInputLayout.setColorModel(agencia: String) {
    this.apply {
        setBoxStrokeColorStateList(setBoxStrokeColorInputText(agencia)!!)
        hintTextColor = ColorStateList.valueOf(colorTextInputLayoutAgencia(agencia))
        setEndIconTintList(setBoxStrokeColorInputText(agencia))
    }
}

fun Fragment.setThemeDateFragment(agencia: String): Int {
    this.resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK
    val retorno: Int = when (agencia) {
        CLO -> {
            R.style.MyDatePickerStyle
        }

        WOKIN -> {
            R.style.MyDatePickerStyleWokin
        }

        else -> {
            R.style.MyDatePickerStyle
        }
    }

    return retorno
}

fun View.changeColorMarco(agencia: String): Int {
    val nightModeFlags = this.resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK

    val retorno: Int = when (agencia) {
        CLO -> {
            R.drawable.marcos
        }

        WOKIN -> {
            R.drawable.wokin_marcos
        }

        else -> {
            R.drawable.marcos
        }
    }
    return retorno
}

fun View.colorToolBarAux(agencia: String): Int {
    val nightModeFlags = this.resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK

    val retorno: Int = if (nightModeFlags == Configuration.UI_MODE_NIGHT_NO && agencia == CLO) {
        ContextCompat.getColor(context, R.color.md_theme_light_primary)
    } else if (nightModeFlags == Configuration.UI_MODE_NIGHT_NO && agencia == WOKIN) {
        ContextCompat.getColor(context, R.color.md_theme_light_primary_wokin)
    } else if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES && agencia == CLO) {
        ContextCompat.getColor(context, R.color.blackColor2)
    } else if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES && agencia == WOKIN) {
        ContextCompat.getColor(context, R.color.blackColor2)
    } else {
        ContextCompat.getColor(context, R.color.md_theme_light_primary)
    }
    return retorno
}

fun View.colorButtonAgencia(agencia: String): Int {
    val nightModeFlags = this.resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK
    val retorno: Int = if (nightModeFlags == Configuration.UI_MODE_NIGHT_NO && agencia == CLO) {
        ContextCompat.getColor(context, R.color.primaryColor)
    } else if (nightModeFlags == Configuration.UI_MODE_NIGHT_NO && agencia == WOKIN) {
        ContextCompat.getColor(context, R.color.wokinPrimaryColor)
    } else if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES && agencia == CLO) {
        ContextCompat.getColor(context, R.color.blackColor4)
    } else if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES && agencia == WOKIN) {
        ContextCompat.getColor(context, R.color.blackColor4)
    } else {
        ContextCompat.getColor(context, R.color.md_theme_light_primary)
    }

    return retorno
}

fun View.colorLabelAgencia(agencia: String): Int {
    val nightModeFlags = this.resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK
    val retorno: Int = if (nightModeFlags == Configuration.UI_MODE_NIGHT_NO && agencia == CLO) {
        ContextCompat.getColor(context, R.color.primaryColor)
    } else if (nightModeFlags == Configuration.UI_MODE_NIGHT_NO && agencia == WOKIN) {
        ContextCompat.getColor(context, R.color.wokinPrimaryColor)
    } else if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES && agencia == CLO) {
        ContextCompat.getColor(context, R.color.blackColor2)
    } else if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES && agencia == WOKIN) {
        ContextCompat.getColor(context, R.color.blackColor2)
    } else {
        ContextCompat.getColor(context, R.color.md_theme_light_primary)
    }

    return retorno
}

fun View.colorTextInputLayoutAgencia(agencia: String): Int {
    val nightModeFlags = this.resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK
    val retorno: Int = if (nightModeFlags == Configuration.UI_MODE_NIGHT_NO && agencia == CLO) {
        ContextCompat.getColor(context, R.color.primaryColor)
    } else if (nightModeFlags == Configuration.UI_MODE_NIGHT_NO && agencia == WOKIN) {
        ContextCompat.getColor(context, R.color.wokinPrimaryColor)
    } else if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES && agencia == CLO) {
        ContextCompat.getColor(context, R.color.md_theme_dark_surfaceTint)
    } else if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES && agencia == WOKIN) {
        ContextCompat.getColor(context, R.color.md_theme_dark_surfaceTint_wokin)
    } else {
        ContextCompat.getColor(context, R.color.md_theme_light_primary)
    }

    return retorno
}

fun View.colorListaReclamo(agencia: String): Int {
    val retorno = when (agencia) {
        CLO -> {
            R.drawable.fondo_listas
        }

        WOKIN -> {
            R.drawable.wokin_fondo_listas
        }

        else -> {
            R.drawable.fondo_listas
        }
    }
    return retorno
}

fun View.colorIconReclamo(agencia: String): ColorStateList? {

    val nightModeFlags = this.resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK
    val retorno: ColorStateList? =
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_NO && agencia == CLO) {
            ContextCompat.getColorStateList(context, R.color.whiteColor1)
        } else if (nightModeFlags == Configuration.UI_MODE_NIGHT_NO && agencia == WOKIN) {
            ContextCompat.getColorStateList(context, R.color.whiteColor1)
        } else if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES && agencia == CLO) {
            ContextCompat.getColorStateList(context, R.color.md_theme_dark_surfaceTint)
        } else if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES && agencia == WOKIN) {
            ContextCompat.getColorStateList(context, R.color.md_theme_dark_surfaceTint_wokin)
        } else {
            ContextCompat.getColorStateList(context, R.color.whiteColor1)
        }

    return retorno

}

fun View.backgroundNavMenu(agencia: String): Int {

    val retorno: Int =
        when (agencia) {
            CLO -> {
                R.drawable.gradient_main_nav_header
            }

            WOKIN -> {
                R.drawable.wokin_gradient_main_nav_header
            }

            else -> {
                R.drawable.gradient_main_nav_header
            }
        }

    return retorno

}
