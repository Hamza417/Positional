package app.simple.positional.activities

import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.os.Handler
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import app.simple.positional.R
import app.simple.positional.constants.vectorBackground
import app.simple.positional.constants.vectorColors
import kotlinx.android.synthetic.main.launcher_activity.*


class LauncherActivity : AppCompatActivity() {

    private var randomValue: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.launcher_activity)

        randomValue = (vectorBackground.indices).random()

        launcher_background.setImageResource(vectorBackground[randomValue])

        launcher_icon.setImageBitmap(R.drawable.ic_place.getBitmapFromVectorDrawable()?.let { addGradient(it) })

        launcher_icon.startAnimation(AnimationUtils.loadAnimation(this, R.anim.launcher_icon))
        launcher_text.startAnimation(AnimationUtils.loadAnimation(this, R.anim.image_in))

        Handler().postDelayed({
            val intent = Intent(this@LauncherActivity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            this.finish()
        }, 1500)
    }

    private fun addGradient(originalBitmap: Bitmap): Bitmap? {
        val width = originalBitmap.width
        val height = originalBitmap.height
        val updatedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(updatedBitmap)
        canvas.drawBitmap(originalBitmap, 0f, 0f, null)
        val paint = Paint()
        val shader = LinearGradient(0f, 0f, 0f, height.toFloat(), vectorColors[randomValue][0], vectorColors[randomValue][1], Shader.TileMode.CLAMP)
        paint.shader = shader
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        return updatedBitmap
    }

    private fun Int.getBitmapFromVectorDrawable(): Bitmap? {
        val drawable = ContextCompat.getDrawable(this@LauncherActivity, this)
        val bitmap = Bitmap.createBitmap(400,
                400, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable?.setBounds(0, 0, canvas.width, canvas.height)
        drawable?.draw(canvas)
        return bitmap
    }
}