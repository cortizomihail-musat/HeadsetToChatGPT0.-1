package ro.cortizo.headsettochatgpt

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.service.voice.VoiceInteractionSession
import android.util.Log

object ChatGptLauncher {
    private const val TAG = "HeadsetToChatGPT"
    private const val CHATGPT_PACKAGE = "com.openai.chatgpt"
    private const val VOICE_URL = "https://chatgpt.com/voice"

    fun launchVoiceWeb(
        context: Context,
        assistantLayer: Boolean = false,
        session: VoiceInteractionSession? = null
    ): Boolean {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(VOICE_URL)).apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return tryStart(context, intent, "WEB_VOICE", assistantLayer, session)
    }

    fun launch(
        context: Context,
        assistantLayer: Boolean,
        session: VoiceInteractionSession? = null
    ): Boolean = launchVoiceWeb(context, assistantLayer, session)

    fun launchNormal(
        context: Context,
        assistantLayer: Boolean = false,
        session: VoiceInteractionSession? = null
    ): Boolean {
        val intent = context.packageManager.getLaunchIntentForPackage(CHATGPT_PACKAGE)
        if (intent == null) {
            Diagnostics.record(context, "LAUNCHER indisponibil")
            return false
        }
        return tryStart(context, intent, "LAUNCHER (doar aplicația)", assistantLayer, session)
    }

    private fun tryStart(
        context: Context,
        intent: Intent,
        route: String,
        assistantLayer: Boolean,
        session: VoiceInteractionSession?
    ): Boolean {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            if (assistantLayer && session != null) {
                session.startAssistantActivity(intent)
                Diagnostics.record(context, "$route: startAssistantActivity trimis către ${intent.data ?: intent.component?.flattenToShortString()}")
            } else {
                context.startActivity(intent)
                Diagnostics.record(context, "$route: startActivity trimis către ${intent.data ?: intent.component?.flattenToShortString()}")
            }
            true
        } catch (error: Exception) {
            Log.e(TAG, "Launch failed: $route", error)
            Diagnostics.record(context, "$route: ${error.javaClass.simpleName}")
            false
        }
    }

    fun installedVersion(context: Context): String = try {
        context.packageManager.getPackageInfo(CHATGPT_PACKAGE, 0).versionName ?: "necunoscută"
    } catch (_: Exception) {
        "neinstalat"
    }
}
