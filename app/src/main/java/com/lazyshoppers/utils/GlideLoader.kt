package com.lazyshoppers.utils

import android.content.Context
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.lazyshoppers.R
import java.io.IOException

class GlideLoader(val context: Context) {

    fun loadUserPicture(image: Any, imageView: ImageView) {
        try {
            Glide
                .with(context)
                .load(image)
                .centerCrop()
                .placeholder(R.drawable.ic_user_placeholder)
                .into(imageView)
        } catch (e: IOException) {
            Log.e("Glide", e.printStackTrace().toString())
            e.printStackTrace()
        }
    }
    fun loadProductPicture(image: Any, imageView: ImageView) {
        try {
            Glide
                .with(context)
                .load(image)
                .centerCrop()
                .into(imageView)
        } catch (e: IOException) {
            Log.e("Glide", e.printStackTrace().toString())
            e.printStackTrace()
        }
    }
}