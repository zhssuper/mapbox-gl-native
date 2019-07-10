package com.mapbox.mapboxsdk.wearapp.extensions

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes

fun Resources.getBitmapFromDrawable(@DrawableRes drawableResId: Int): Bitmap {
    return getDrawable(drawableResId, null).getBitmapFromDrawable()
}

fun Drawable.getBitmapFromDrawable(): Bitmap {
    return if (this is BitmapDrawable) {
        bitmap
    } else {
        val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
        Canvas(bitmap).let {
            setBounds(0, 0, it.width, it.height)
            draw(it)
            bitmap
        }
    }
}