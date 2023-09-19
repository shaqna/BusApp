package com.maou.busapp.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

fun iconToBitmap(context: Context, drawableId: Int): BitmapDescriptor {
    val vectorDrawable = ContextCompat.getDrawable(context, drawableId)!!

    vectorDrawable.let {
        it.setBounds(0,0, it.intrinsicWidth, it.intrinsicHeight)
    }

    val bitmap = Bitmap.createBitmap(
        vectorDrawable.intrinsicWidth,
        vectorDrawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )

    val canvas = Canvas(bitmap)

    vectorDrawable.draw(canvas)

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}