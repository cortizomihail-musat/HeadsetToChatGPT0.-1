package ro.cortizo.headsettochatgpt

import android.app.*
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.*
import android.content.pm.ServiceInfo
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.*
import android.provider.Settings
import android.view.KeyEvent

/** Passive observation: no media session, audio focus, SCO request or microphone recording. */
class HeadsetDiagnosticService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var audio: AudioManager
    private var registered = false
    private var label = ""
    private var lastSnapshot = ""
    private var headset: BluetoothHeadset? = null
    private var finished = false
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val key = if (Build.VERSION.SDK_INT >= 33)
                intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT, KeyEvent::class.java)
            else @Suppress("DEPRECATION") (intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT) as? KeyEvent)
            val state = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, -1)
            record("BROADCAST ${intent.action}; state=$state" +
                (key?.let { "; key=${KeyEvent.keyCodeToString(it.keyCode)} action=${it.action}" } ?: ""))
        }
    }
    private val profileListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
            if (finished) {
                getSystemService(BluetoothManager::class.java)?.adapter?.closeProfileProxy(profile, proxy)
                return
            }
            headset = proxy as? BluetoothHeadset
            snapshot()
        }
        override fun onServiceDisconnected(profile: Int) { headset = null; record("HFP proxy deconectat") }
    }
    private val poll = object : Runnable {
        override fun run() { snapshot(); handler.postDelayed(this, 500) }
    }

    override fun onBind(intent: Intent?) = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP") { stopSelf(); return START_NOT_STICKY }
        if (registered) { record("Fereastră deja activă; așteaptă STOP"); return START_NOT_STICKY }
        label = intent?.getStringExtra("label") ?: "?"
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel("diagnostic", "Diagnostic Jabra", NotificationManager.IMPORTANCE_LOW))
        val open = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        val stop = PendingIntent.getService(this, 1, Intent(this, javaClass).setAction("STOP"), PendingIntent.FLAG_IMMUTABLE)
        val notification = Notification.Builder(this, "diagnostic")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentTitle("Diagnostic Jabra $label — 30 secunde")
            .setContentText("Apasă Jabra; apoi notează dacă asistentul ascultă.")
            .setContentIntent(open).setOngoing(true)
            .addAction(Notification.Action.Builder(null, "Oprește", stop).build()).build()
        try {
            startForeground(7, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE)
            audio = getSystemService(AudioManager::class.java)
            val filter = IntentFilter().apply {
                addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)
                addAction(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED)
                addAction(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED)
                addAction(Intent.ACTION_MEDIA_BUTTON)
            }
            // Bluetooth broadcasts can originate from the privileged Bluetooth app.
            if (Build.VERSION.SDK_INT >= 33) registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
            else @Suppress("DEPRECATION") registerReceiver(receiver, filter)
            registered = true
            record("START; assistant=${setting("assistant")}; voice_service=${setting("voice_interaction_service")}")
            record("LIMITĂ: fără acces la evenimentele interne Gemini; MEDIA_BUTTON nu este global. Broadcasturile nu certifică proveniența Jabra.")
            getSystemService(BluetoothManager::class.java)?.adapter?.getProfileProxy(this, profileListener, BluetoothProfile.HEADSET)
            handler.post(poll)
            handler.postDelayed({ stopSelf() }, 30_000)
        } catch (error: RuntimeException) {
            record("EROARE ${error.javaClass.simpleName}; diagnostic oprit")
            stopSelf()
        }
        return START_NOT_STICKY
    }
    private fun setting(key: String): String = try {
        Settings.Secure.getString(contentResolver, key) ?: "indisponibil"
    } catch (_: SecurityException) { "acces refuzat" }
    private fun record(message: String) = Diagnostics.record(this, "DIAG[$label] $message")
    private fun snapshot() {
        try {
            val devices = audio.getDevices(AudioManager.GET_DEVICES_ALL).map { it.type }.distinct().sorted()
            val hfp = headset?.let { proxy ->
                val connected = proxy.connectedDevices
                "${connected.size}; audio=${connected.any { proxy.isAudioConnected(it) }}"
            } ?: "indisponibil"
            val value = "mode=${audio.mode}; micMuted=${audio.isMicrophoneMute}; devices=$devices; HFP=$hfp"
            if (value != lastSnapshot) { lastSnapshot = value; record(value) }
        } catch (error: RuntimeException) {
            val value = "SNAPSHOT ${error.javaClass.simpleName}"
            if (value != lastSnapshot) { lastSnapshot = value; record(value) }
        }
    }
    override fun onDestroy() {
        finished = true
        handler.removeCallbacksAndMessages(null)
        if (registered) unregisterReceiver(receiver)
        headset?.let { getSystemService(BluetoothManager::class.java)?.adapter?.closeProfileProxy(BluetoothProfile.HEADSET, it) }
        record("STOP; absența evenimentelor NU dovedește blocarea MagicOS")
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }
}
