package ro.cortizo.headsettochatgpt

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.service.voice.VoiceInteractionSession
import android.util.Log

object ChatGptLauncher {
    private const val TAG = "HeadsetToChatGPT"
    private const val CHATGPT_PACKAGE = "com.openai.chatgpt"

    fun launch(context: Context, assistantLayer: Boolean, session: VoiceInteractionSession? = null): Boolean {
        // 1) Preferăm ACTION_ASSIST către ChatGPT. Dacă aplicația expune un handler de asistent,
        //    acesta are cele mai mari șanse să reproducă lansarea făcută de butonul telefonului.
        val assistIntent = Intent(Intent.ACTION_ASSIST).apply {
            setPackage(CHATGPT_PACKAGE)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val pm = context.packageManager
        val assistResolved = pm.resolveActivity(assistIntent, PackageManager.MATCH_DEFAULT_ONLY) != null
        if (assistResolved) {
            Log.i(TAG, "Launching ChatGPT through ACTION_ASSIST")
            val started = runCatching {
                if (assistantLayer && session != null) {
                    session.startAssistantActivity(assistIntent)
                } else {
                    context.startActivity(assistIntent)
                }
            }
            if (started.isSuccess) return true
            Log.w(TAG, "ACTION_ASSIST failed; trying launcher", started.exceptionOrNull())
        }

        // 2) Fallback: launcher intent normal pentru aplicația oficială ChatGPT.
        val launchIntent = pm.getLaunchIntentForPackage(CHATGPT_PACKAGE)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        } ?: return false

        Log.i(TAG, "ACTION_ASSIST unavailable; launching ChatGPT normally")
        return runCatching {
            if (assistantLayer && session != null) {
                session.startAssistantActivity(launchIntent)
            } else {
                context.startActivity(launchIntent)
            }
        }.isSuccess
    }
}
