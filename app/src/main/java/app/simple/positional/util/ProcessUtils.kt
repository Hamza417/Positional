package app.simple.positional.util

import android.os.Handler
import android.os.Looper

object ProcessUtils {
    /**
     * Run a code block inside the main thread
     */
    fun runOnUiThread(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            block()
        } else {
            Handler(Looper.getMainLooper()).post(block)
        }
    }
}