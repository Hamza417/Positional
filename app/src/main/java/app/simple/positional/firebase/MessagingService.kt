package app.simple.positional.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import app.simple.positional.BuildConfig
import app.simple.positional.R
import app.simple.positional.activities.main.MainActivity
import app.simple.positional.preference.MainPreferences
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class MessagingService : FirebaseMessagingService() {

    private val requestCode = 1
    private val notificationID = 6578

    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("TAG", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            if (BuildConfig.DEBUG) {
                Log.d("TAG", task.result)
            }
        })
    }

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)

        if (MainPreferences.isNotificationOn()) {
            try {
                val title = p0.notification?.title
                val body = p0.notification?.body

                sendNotification(title!!, body!!)
            } catch (e: NullPointerException) {
            }
        }
    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        if (BuildConfig.DEBUG) {
            baseContext.getSharedPreferences("Preferences", Context.MODE_PRIVATE).edit().putString("token", p0).apply()
        }
    }

    private fun sendNotification(title: String, messageBody: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, requestCode, intent, PendingIntent.FLAG_ONE_SHOT)

        val channelId = "notification_channel_id_for_positional"

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_place_notification)
                .setColor(Color.parseColor("#1B9CFF"))
                .setSubText("Upcoming Changes")
                .setContentTitle(title)
                .setStyle(NotificationCompat.BigTextStyle().bigText(messageBody))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                //.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId,
                    "App Update",
                    NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(notificationID, notificationBuilder.build())
    }
}