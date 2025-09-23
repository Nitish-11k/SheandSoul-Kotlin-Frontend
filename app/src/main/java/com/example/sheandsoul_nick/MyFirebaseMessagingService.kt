package com.example.sheandsoul_nick

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.sheandsoul_nick.data.SessionManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    // Called when a new FCM token is generated.
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token received: $token")
        // TODO: Send this token to your backend server and associate it with the logged-in user.
        // You'll need to create an API endpoint for this.
        val sessionManager = SessionManager(applicationContext)
        CoroutineScope(Dispatchers.IO).launch {
            sessionManager.saveFcmToken(token)
            // We don't call the API here directly. The ViewModel will handle it.
        }
    }

    // Called when a message is received while the app is in the foreground.
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        remoteMessage.notification?.let {
            Log.d("FCM", "Message Notification Body: ${it.body}")
            sendLocalNotification(it.title ?: "She&Soul", it.body ?: "You have a new message.")
        }
    }

    private fun sendLocalNotification(title: String, body: String) {
        val channelId = "sheandsoul_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "App Notifications", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_official_logo) // Make sure you have this drawable
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)

        notificationManager.notify(0, notificationBuilder.build())
    }
}