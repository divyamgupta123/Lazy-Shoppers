package com.lazyshoppers.utils

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatRadioButton


class LSRadioButton(context: Context, attrs: AttributeSet) :
    AppCompatRadioButton(context, attrs) {

    init {
        applyFont()
    }

    private fun applyFont() {

        val typeface: Typeface =
            Typeface.createFromAsset(context.assets, "Montserrat-Bold.ttf")
        setTypeface(typeface)
    }
}