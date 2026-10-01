package ro.cortizo.headsettochatgpt

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.service.voice.VoiceInteractionSession
import android.util.Log

object ChatGptLauncher {
    private const val TAG = "HeadsetToChatGPT"
    private const val CHATGPT_PACKAGE = "com.openai.chatgpt"

    // Observed in ChatGPT builds, NOT a stable public API.
    // Never enable a disabled component or try to access an unexported one.
    private const val VOICE_ACTIVITY = "com.openai.voice.assistant.AssistantActivity"

    fun launch(
        context: Context,
        assistantLayer: Boolean,
        session: VoiceInteractionSession? = null
    ): Boolean {
        val pm = context.packageManager
        val voiceComponent = ComponentName(CHATGPT_PACKAGE, VOICE_ACTIVITY)
        val voiceInfo = try {
            pm.getActivityInfo(voiceComponent, 0)
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }
        val permitted = voiceInfo?.permission.let {
            it == null || context.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
        }
        if (voiceInfo != null && voiceInfo.exported && voiceInfo.enabled &&
            voiceInfo.applicationInfo.enabled && permitted
        ) {
            // Inside a real VoiceInteractionSession, use Android's dedicated
            // voice-activity path. Android adds CATEGORY_VOICE and associates
            // the launched task with this voice interaction session.
            if (tryStart(
                    context,
                    Intent().setComponent(voiceComponent),
                    "VOICE_ACTIVITY",
                    assistantLayer,
                    session,
                    preferVoiceActivity = true
                )
            ) return true
        } else {
            val reason = when {
                voiceInfo == null -> "componenta lipsește sau este dezactivată"
                !voiceInfo.exported -> "componenta nu este exportată"
                !voiceInfo.enabled || !voiceInfo.applicationInfo.enabled -> "componenta este dezactivată"
                !permitted -> "permisiune necesară: ${voiceInfo.permission}"
                else -> "indisponibilă"
            }
            Diagnostics.record(context, "VOICE_ACTIVITY: $reason")
        }

        Diagnostics.record(context, "Fallback: deschidere normală; vocea NU este confirmată")
        return launchNormal(context, assistantLayer, session)
    }

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
        // The normal launcher is deliberately not promoted to a voice activity.
        // It remains the known-safe fallback that only opens ChatGPT.
        return tryStart(
            context,
            intent,
            "LAUNCHER (doar aplicația)",
            assistantLayer,
            session,
            preferVoiceActivity = false
        )
    }

    private fun tryStart(
        context: Context,
        intent: Intent,
        route: String,
        assistantLayer: Boolean,
        session: VoiceInteractionSession?,
        preferVoiceActivity: Boolean
    ): Boolean {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            when {
                assistantLayer && session != null && preferVoiceActivity -> {
                    session.startVoiceActivity(intent)
                    Diagnostics.record(
                        context,
                        "$route: startVoiceActivity trimis către ${intent.component?.flattenToShortString()}; așteptăm confirmarea pe telefon"
                    )
                }
                assistantLayer && session != null -> {
                    session.startAssistantActivity(intent)
                    Diagnostics.record(
                        context,
                        "$route: startAssistantActivity trimis către ${intent.component?.flattenToShortString()}; voce neconfirmată"
                    )
                }
                else -> {
                    context.startActivity(intent)
                    Diagnostics.record(
                        context,
                        "$route: startActivity trimis către ${intent.component?.flattenToShortString()}; voce neconfirmată"
                    )
                }
            }
            true
        } catch (error: Exception) {
            Log.e(TAG, "Launch failed: $route", error)
            Diagnostics.record(
                context,
                "$route: ${if (preferVoiceActivity) "startVoiceActivity " else ""}${error.javaClass.simpleName}"
            )
            false
        }
    }

    fun installedVersion(context: Context): String = try {
        context.packageManager.getPackageInfo(CHATGPT_PACKAGE, 0).versionName ?: "necunoscută"
    } catch (_: PackageManager.NameNotFoundException) {
        "neinstalat"
    }
}
