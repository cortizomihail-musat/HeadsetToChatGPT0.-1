package ro.cortizo.headsettochatgpt

import android.app.Activity
import android.os.Bundle
import android.util.Log

class AssistProxyActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val action = intent?.action ?: "null"
        Log.i(TAG, "Assistant intent received: $action")
        Diagnostics.record(this, "PROXY: action=$action flags=${intent?.flags ?: 0}")
        ChatGptLauncher.launchVoiceWeb(this, assistantLayer = false)
        finish()
    }

    companion object {
        private const val TAG = "HeadsetToChatGPT"
    }
}
