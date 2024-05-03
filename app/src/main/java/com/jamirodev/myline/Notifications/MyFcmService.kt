package com.jamirodev.myline.Notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.jamirodev.myline.R
import com.jamirodev.myline.chat.ChatActivity
import kotlin.random.Random

class MyFcmService : FirebaseMessagingService() {

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "GURUAPP_CHANNEL_ID"
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val title = "${message.notification?.title}"
        val body = "${message.notification?.body}"

        val senderUid = "${message.data["senderUid"]}"
        val notificationType = "${message.data["notificationType"]}"

        showNotification(title, body, senderUid)
    }

    private fun showNotification(title: String, body: String, senderUid: String) {
        val notificationId = Random.nextInt(3000)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        configureChanelNotification(notificationManager)
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("uidSeller", senderUid) //uid could be wrong
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
        notificationManager.notify(notificationId, notificationBuilder.build())

    }

    private fun configureChanelNotification(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Chat_Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.description = "Show Chat Notifications"
            notificationChannel.enableVibration(true)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
}