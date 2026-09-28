package ro.cortizo.headsettochatgpt

import android.content.Context
import android.os.Bundle
import android.service.voice.VoiceInteractionSession
import android.util.Log

class BridgeSession(private val appContext: Context) : VoiceInteractionSession(appContext) {

    override fun onPrepareShow(args: Bundle?, showFlags: Int) {
        // The bridge has no visual assistant UI of its own.
        setUiEnabled(false)
        super.onPrepareShow(args, showFlags)
    }

    override fun onShow(args: Bundle?, showFlags: Int) {
        super.onShow(args, showFlags)

        val source = buildList {
            if (args?.getBoolean(BridgeVoiceInteractionService.TEST_SESSION, false) == true) add("MANUAL_SESSION_TEST")
            if (showFlags and SHOW_SOURCE_PUSH_TO_TALK != 0) add("PUSH_TO_TALK")
            if (showFlags and SHOW_SOURCE_ASSIST_GESTURE != 0) add("ASSIST_GESTURE")
            if (showFlags and SHOW_SOURCE_APPLICATION != 0) add("APPLICATION")
            if (showFlags and SHOW_SOURCE_ACTIVITY != 0) add("ACTIVITY")
        }.ifEmpty { listOf("UNKNOWN") }.joinToString("+")

        Log.i(TAG, "Voice session shown. flags=$showFlags source=$source ")
        Diagnostics.record(appContext, "Voice session: $source; flags=$showFlags")

        val launched = ChatGptLauncher.launch(appContext, assistantLayer = true, session = this)
        Log.i(TAG, "ChatGPT launch result=$launched")
        Diagnostics.record(appContext, "ChatGPT launch result=$launched; source=$source")

        // Do not immediately destroy the assistant session after launch.
        if (!launched) finish()
    }

    companion object {
        private const val TAG = "HeadsetToChatGPT"
    }
}
