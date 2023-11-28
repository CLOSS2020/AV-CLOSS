package com.appcloos.mimaletin.classes

import android.text.InputFilter
import android.text.Spanned
import java.util.regex.Matcher
import java.util.regex.Pattern


internal class NumberInputFilter(digitsBeforeZero: Int) :
    InputFilter {
    private val mPattern: Pattern

    init {
        mPattern =
            Pattern.compile("^\\d{0," + (digitsBeforeZero - 1) + "}")
    }

    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned,
        dstart: Int,
        dend: Int,
    ): String? {
        val matcher: Matcher = mPattern.matcher(dest)
        return if (!matcher.matches()) "" else null
    }
}