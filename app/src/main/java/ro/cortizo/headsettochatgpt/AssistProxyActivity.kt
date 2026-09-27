package ro.cortizo.headsettochatgpt

import android.app.Activity
import android.os.Bundle
import android.util.Log

/**
 * OEM fallback: some Android ROMs invoke the selected assistant through
 * ACTION_ASSIST rather than a full VoiceInteractionService session.
 *
 * This activity is intentionally invisible and immediately redirects to
 * the official ChatGPT application.
 */
class AssistProxyActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.i(TAG, "ACTION_ASSIST received by proxy activity")
        Diagnostics.record(this, "ACTION_ASSIST proxy invoked")
        ChatGptLauncher.launch(this, assistantLayer = false)
        finish()
    }

    companion object {
        private const val TAG = "HeadsetToChatGPT"
    }
}
