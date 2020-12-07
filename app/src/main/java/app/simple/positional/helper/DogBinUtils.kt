package app.simple.positional.helper

import android.os.Handler
import android.os.HandlerThread
import android.util.JsonReader
import java.io.InputStreamReader
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/**
 * Helper functions for uploading to del.dog
 */
object DogBinUtils {
    private const val TAG = "DogbinUtils"
    private const val BASE_URL = "https://del.dog"
    private val API_URL = String.format("%s/documents", BASE_URL)
    private var handler: Handler? = null
        get() {
            if (field == null) {
                val handlerThread = HandlerThread("dogbinThread")
                if (!handlerThread.isAlive) handlerThread.start()
                field = Handler(handlerThread.looper)
            }
            return field
        }

    /**
     * Uploads `content` to dogbin
     *
     * @param content the content to upload to dogbin
     * @param callback the callback to call on success / failure
     */
    fun upload(content: String, callback: UploadResultCallback) {
        handler?.post {
            try {
                val urlConnection: HttpsURLConnection = URL(API_URL).openConnection() as HttpsURLConnection
                try {
                    urlConnection.setRequestProperty("Accept-Charset", "UTF-8")
                    urlConnection.doOutput = true
                    urlConnection.outputStream.use { output -> output.write(content.toByteArray(charset("UTF-8"))) }
                    var key = ""
                    JsonReader(InputStreamReader(urlConnection.inputStream, "UTF-8")).use { reader ->
                        reader.beginObject()
                        while (reader.hasNext()) {
                            val name: String = reader.nextName()
                            if (name == "key") {
                                key = reader.nextString()
                                break
                            } else {
                                reader.skipValue()
                            }
                        }
                        reader.endObject()
                    }
                    if (key.isNotEmpty()) {
                        callback.onSuccess(getUrl(key))
                    } else {
                        val msg = "Failed to upload to dogbin: No key retrieved"
                        callback.onFail(msg, DogbinException(msg))
                    }
                } finally {
                    urlConnection.disconnect()
                }
            } catch (e: Exception) {
                callback.onFail("Failed to upload to dogbin", e)
            }
        }
    }

    /**
     * Get the view URL from a key
     */
    private fun getUrl(key: String): String {
        return String.format("%s/%s", BASE_URL, key)
    }

    interface UploadResultCallback {
        fun onSuccess(url: String?)
        fun onFail(message: String?, e: Exception?)
    }
}