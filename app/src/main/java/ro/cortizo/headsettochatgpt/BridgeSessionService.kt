package ro.cortizo.headsettochatgpt

import android.os.Bundle
import android.service.voice.VoiceInteractionSession
import android.service.voice.VoiceInteractionSessionService

class BridgeSessionService : VoiceInteractionSessionService() {
    override fun onNewSession(args: Bundle?): VoiceInteractionSession {
        Diagnostics.record(this, "SESSION CREATED")
        return BridgeSession(this)
    }
}
