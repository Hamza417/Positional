package app.simple.positional.dialogs.app

import android.content.ComponentName
import android.content.Intent
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import app.simple.positional.R
import app.simple.positional.decorations.ripple.DynamicRippleButton
import app.simple.positional.decorations.views.CustomBottomSheetDialogFragment
import app.simple.positional.decorations.views.CustomWebView
import app.simple.positional.preferences.MainPreferences


class Rate : CustomBottomSheetDialogFragment() {

    private lateinit var webView: CustomWebView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_rating, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        webView = view.findViewById(R.id.rating_webview)

        val sure: DynamicRippleButton = view.findViewById(R.id.rate)
        val close: DynamicRippleButton = view.findViewById(R.id.close)

        webView.loadUrl("file:///android_asset/html/rate.html")

        sure.setOnClickListener {
            openAppRating()
            MainPreferences.setShowRatingDialog(false)
            this.dismiss()
        }

        close.setOnClickListener {
            MainPreferences.setLaunchCount(1)
            this.dismiss()
        }

        view.findViewById<CheckBox>(R.id.show_rate_dialog).setOnCheckedChangeListener { _, isChecked ->
            MainPreferences.setShowRatingDialog(isChecked)
        }
    }

    private fun openAppRating() {
        /**
         * you can also use BuildConfig.APPLICATION_ID
         */
        val appId: String = requireContext().packageName
        val rateIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appId"))
        var marketFound = false

        /**
         * find all applications able to handle our rateIntent
         */
        val otherApps: List<ResolveInfo> = requireContext().packageManager.queryIntentActivities(rateIntent, 0)

        for (otherApp in otherApps) {
            /**
             * look for Google Play application
             */
            if (otherApp.activityInfo.applicationInfo.packageName == "com.android.vending") {
                val otherAppActivity = otherApp.activityInfo
                val componentName = ComponentName(otherAppActivity.applicationInfo.packageName, otherAppActivity.name)

                /**
                 * make sure it does NOT open in the stack of your activity
                 */
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                /**
                 * task re -parenting if needed
                 */
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)

                /**
                 * if the Google Play was already open in a search result
                 * this make sure it still go to the app page you requested
                 */
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

                /**
                 * this make sure only the Google Play app is allowed to
                 * intercept the intent
                 */
                rateIntent.component = componentName
                requireContext().startActivity(rateIntent)
                marketFound = true
                break
            }
        }

        /**
         * if GP not present on device, open web browser
         */
        if (!marketFound) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appId"))
            requireContext().startActivity(webIntent)
        }
    }

    companion object {
        fun newInstance(): Rate {
            val args = Bundle()
            val fragment = Rate()
            fragment.arguments = args
            return fragment
        }
    }
}
