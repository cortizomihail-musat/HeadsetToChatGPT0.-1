package ro.cortizo.headsettochatgpt

import android.os.Bundle
import android.service.voice.VoiceInteractionService
import android.util.Log

class BridgeVoiceInteractionService : VoiceInteractionService() {
    override fun onReady() {
        super.onReady()
        Log.i(TAG, "VoiceInteractionService ready")
        Diagnostics.record(this, "VoiceInteractionService ready")
    }

    override fun onPrepareToShowSession(args: Bundle, flags: Int) {
        super.onPrepareToShowSession(args, flags)
        Log.i(TAG, "Assistant invocation flags=$flags args=$args")
        Diagnostics.record(this, "Assistant invocation prepared; flags=$flags")
    }

    companion object {
        private const val TAG = "HeadsetToChatGPT"
    }
}
