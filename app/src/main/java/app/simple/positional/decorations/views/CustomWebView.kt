@file:Suppress("LeakingThis")

package app.simple.positional.decorations.views

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.util.AttributeSet
import android.webkit.WebView
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature

open class CustomWebView(context: Context, attributeSet: AttributeSet) : WebView(context, attributeSet) {
    init {
        setBackgroundColor(0)
        settings.allowContentAccess = true
        settings.allowFileAccess = true
        settings.setSupportZoom(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
                WebSettingsCompat.setAlgorithmicDarkeningAllowed(this.settings, context.resources.configuration.uiMode and
                        Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES)
            } else if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                if (context.resources.configuration.uiMode and
                        Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
                    @Suppress("DEPRECATION")
                    WebSettingsCompat.setForceDark(this.settings, WebSettingsCompat.FORCE_DARK_ON)
                }
            }
        } else {
            if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                if (context.resources.configuration.uiMode and
                        Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
                    @Suppress("DEPRECATION")
                    WebSettingsCompat.setForceDark(this.settings, WebSettingsCompat.FORCE_DARK_ON)
                }
            }
        }
    }
}
