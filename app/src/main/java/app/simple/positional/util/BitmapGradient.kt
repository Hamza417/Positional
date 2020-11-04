package app.simple.positional.util

import android.graphics.*

fun addLinearGradient(originalBitmap: Bitmap, array: IntArray): Bitmap? {
    val width = originalBitmap.width
    val height = originalBitmap.height
    val updatedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(updatedBitmap)
    canvas.drawBitmap(originalBitmap, 0f, 0f, null)
    val paint = Paint()
    val shader = LinearGradient(0f, 0f, 0f, height.toFloat(), array[0], array[1], Shader.TileMode.CLAMP)
    paint.shader = shader
    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    return updatedBitmap
}

fun addRadialGradient(originalBitmap: Bitmap, int: Int): Bitmap? {
    val width = originalBitmap.width
    val height = originalBitmap.height
    val updatedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(updatedBitmap)
    canvas.drawBitmap(originalBitmap, 0f, 0f, null)
    val paint = Paint()
    val shader = RadialGradient(200f, 200f, 200f, 0x000000, int, Shader.TileMode.CLAMP)
    paint.shader = shader
    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    return updatedBitmap
}