package app.simple.positional.util

import android.content.Context
import android.graphics.*
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

object BitmapHelper {
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

    fun addLinearGradient(originalBitmap: Bitmap, array: IntArray, verticalOffset: Float): Bitmap? {
        val width = originalBitmap.width
        val height = originalBitmap.height
        val updatedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(updatedBitmap)
        canvas.drawBitmap(originalBitmap, 0f, 0f, null)
        val paint = Paint()
        val shader = LinearGradient(0f, verticalOffset, 0f, height.toFloat(), array[0], array[1], Shader.TileMode.CLAMP)
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

    fun rotateBitmap(bitmap: Bitmap, rotationAngleDegree: Float): Bitmap? {
        val w = bitmap.width
        val h = bitmap.height
        var newW = w
        var newH = h
        if (rotationAngleDegree == 90F || rotationAngleDegree == 270F) {
            newW = h
            newH = w
        }
        val rotatedBitmap = Bitmap.createBitmap(newW, newH, bitmap.config)
        val canvas = Canvas(rotatedBitmap)
        val rect = Rect(0, 0, newW, newH)
        val matrix = Matrix()
        val px: Float = rect.exactCenterX()
        val py: Float = rect.exactCenterY()
        matrix.postTranslate((-bitmap.width / 2).toFloat(), (-bitmap.height / 2).toFloat())
        matrix.postRotate(rotationAngleDegree)
        matrix.postTranslate(px, py)
        canvas.drawBitmap(bitmap, matrix, Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG or Paint.FILTER_BITMAP_FLAG))
        matrix.reset()
        return rotatedBitmap
    }

    /**
     * Convert vector resource into Bitmap
     *
     * @param context [Context]
     * @param size Resolution/Dimension of the output bitmap
     * @return [Bitmap]
     */
    fun Int.toBitmap(context: Context, size: Int): Bitmap {
        val drawable = ContextCompat.getDrawable(context, this)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable?.setBounds(0, 0, canvas.width, canvas.height)
        drawable?.draw(canvas)
        return bitmap
    }

    /**
     * Convert vector resource into Bitmap
     *
     * @param context [Context]
     * @param size Resolution/Dimension of the output bitmap
     * @param alpha 0 - 255 opacity of output bitmap
     * @return [Bitmap]
     */
    fun Int.toBitmap(context: Context, size: Int, alpha: Int): Bitmap {
        val drawable = ContextCompat.getDrawable(context, this)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable?.setBounds(0, 0, canvas.width, canvas.height)
        drawable?.alpha = alpha
        drawable?.draw(canvas)
        return bitmap
    }

    fun Int.bitmapDescriptorFromVector(context: Context): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(context, this)
        vectorDrawable!!.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
        val bitmap = Bitmap.createBitmap(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        vectorDrawable.draw(Canvas(bitmap))
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}
