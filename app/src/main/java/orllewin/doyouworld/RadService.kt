package orllewin.doyouworld

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.audio.AudioAttributes


const val START_RAD_SERVICE = 111
const val MUTE_RAD_SERVICE = 112
const val UNMUTE_RAD_SERVICE = 113
const val STOP_RAD_SERVICE = 114

const val NOTIFICATION_ID = 1001

class RadService: Service() {

    companion object{
        fun isServiceRunningInForeground(context: Context, serviceClass: Class<*>): Boolean {
            val manager = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
            for (service in manager.getRunningServices(Int.MAX_VALUE)) {
                if (serviceClass.name == service.service.className) {
                    if (service.foreground) {
                        return true
                    }
                }
            }
            return false
        }
    }
    private lateinit var notification: Notification
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var muteAction: NotificationCompat.Action
    private lateinit var unmuteAction: NotificationCompat.Action
    private lateinit var stopAction: NotificationCompat.Action

    private lateinit var  muteIntent: PendingIntent
    private lateinit var  unmuteIntent: PendingIntent
    private lateinit var  stopIntent: PendingIntent

    private var volume = 1.0f

    private val mediaItem = MediaItem.fromUri("https://doyouworld.out.airtime.pro/doyouworld_a")

    private val uAmpAudioAttributes = AudioAttributes.Builder()
        .setContentType(C.CONTENT_TYPE_MUSIC)
        .setUsage(C.USAGE_MEDIA)
        .build()

    private val player: ExoPlayer by lazy {
        ExoPlayer.Builder(this).build().apply {
            setAudioAttributes(uAmpAudioAttributes, true)
            setHandleAudioBecomingNoisy(true)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.l("RadService onStartCommand()")
        intent?.run{
            when(getIntExtra("action", -1)){
                START_RAD_SERVICE -> initialise()
                MUTE_RAD_SERVICE -> mute()
                UNMUTE_RAD_SERVICE -> unmute()
                STOP_RAD_SERVICE -> stop()
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun initialise() {
        println("Initialise RAD")

        muteIntent = PendingIntent.getService(
            this,
            MUTE_RAD_SERVICE,
            Intent(this, RadService::class.java).also { intent ->
                intent.putExtra("action", MUTE_RAD_SERVICE)
            },
            PendingIntent.FLAG_IMMUTABLE
        )

        unmuteIntent = PendingIntent.getService(
            this,
            UNMUTE_RAD_SERVICE,
            Intent(this, RadService::class.java).also { intent ->
                intent.putExtra("action", UNMUTE_RAD_SERVICE)
            },
            PendingIntent.FLAG_IMMUTABLE
        )

        stopIntent = PendingIntent.getService(
            this,
            STOP_RAD_SERVICE,
            Intent(this, RadService::class.java).also { intent ->
                intent.putExtra("action", STOP_RAD_SERVICE)
            },
            PendingIntent.FLAG_IMMUTABLE
        )

        val pendingIntent: PendingIntent = Intent(this, MainActivity::class.java).run {
            PendingIntent.getActivity(applicationContext, 0, this, PendingIntent.FLAG_IMMUTABLE)
        }

        val channelId = createNotificationChannel("rad_service", "RAD CHANNEL")

        notificationBuilder = NotificationCompat.Builder(this, channelId)

        muteAction = NotificationCompat.Action.Builder(null, "Mute", muteIntent).build()
        unmuteAction = NotificationCompat.Action.Builder(null, "Unmute", unmuteIntent).build()
        stopAction = NotificationCompat.Action.Builder(null, "Stop", stopIntent).build()


        notification = notificationBuilder
            .setContentTitle("Do!! You!!! World!")
            .setContentText("DO!! YOU!!! RADIO")
            .setSmallIcon(R.drawable.notification_banana)
            .setContentIntent(pendingIntent)
            .setTicker("DO!! YOU!!! RADIO")
            .addAction(stopAction)
            .addAction(muteAction)
            .build()

        startForeground(NOTIFICATION_ID, notification)

        Log.l("Asking exoPlayer to play...")
        player.addMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = true

    }

    private fun mute(){
        volume = player.volume
        player.volume = 0f
        notification = notificationBuilder
            .clearActions()
            .addAction(stopAction)
            .addAction(unmuteAction)
            .build()
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun unmute(){
        player.volume = volume
        notification = notificationBuilder
            .clearActions()
            .addAction(stopAction)
            .addAction(muteAction)
            .build()
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun stop(){
        player.stop()
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.cancel(NOTIFICATION_ID)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String{
        val chan = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
        chan.lightColor = Color.parseColor("#7251A8")
        chan.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }
}