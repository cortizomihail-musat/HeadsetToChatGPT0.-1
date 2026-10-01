package ro.cortizo.headsettochatgpt

import android.os.Bundle
import android.content.ComponentName
import android.content.Context
import android.service.voice.VoiceInteractionService
import android.util.Log
import java.lang.ref.WeakReference

class BridgeVoiceInteractionService : VoiceInteractionService() {
    private var destroyed = false

    override fun onReady() {
        super.onReady()
        if (destroyed) return
        readyService = WeakReference(this)
        Log.i(TAG, "VoiceInteractionService ready")
        Diagnostics.record(this, "VoiceInteractionService ready")
    }

    override fun onPrepareToShowSession(args: Bundle, flags: Int) {
        super.onPrepareToShowSession(args, flags)
        Log.i(TAG, "Assistant invocation flags=$flags")
        Diagnostics.record(this, "Assistant invocation prepared; flags=$flags")
    }

    override fun onShowSessionFailed(args: Bundle) {
        super.onShowSessionFailed(args)
        Diagnostics.record(this, "SESSION FAILED: Android nu a putut afișa sesiunea bridge")
    }

    override fun onLaunchVoiceAssistFromKeyguard() {
        super.onLaunchVoiceAssistFromKeyguard()
        Diagnostics.record(this, "KEYGUARD voice assist callback; fără lansare automată din ecran blocat")
    }

    override fun onShutdown() {
        if (readyService.get() === this) readyService.clear()
        Diagnostics.record(this, "VoiceInteractionService shutdown")
        super.onShutdown()
    }

    override fun onDestroy() {
        destroyed = true
        if (readyService.get() === this) readyService.clear()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "HeadsetToChatGPT"
        const val TEST_SESSION = "ro.cortizo.headsettochatgpt.TEST_SESSION"
        // Same-process UI helper; never exposes a new service or broadcast entry point.
        private var readyService = WeakReference<BridgeVoiceInteractionService>(null)

        fun testSession(context: Context): Boolean {
            Diagnostics.record(context, "TEST SESIUNE BRIDGE")
            if (!isActiveService(context, ComponentName(context, BridgeVoiceInteractionService::class.java))) {
                Diagnostics.record(context, "TEST BLOCAT: selectează HeadsetToChatGPT ca asistent implicit")
                return false
            }
            val service = readyService.get()
            if (service == null) {
                Diagnostics.record(context, "TEST BLOCAT: serviciul nu este încă ready; reîncearcă după câteva secunde")
                return false
            }
            return try {
                // Do not request screen text or screenshots for this launch test.
                service.showSession(Bundle().apply { putBoolean(TEST_SESSION, true) }, 0)
                Diagnostics.record(context, "TEST: showSession solicitat; așteptăm Voice session")
                true
            } catch (error: RuntimeException) {
                Diagnostics.record(context, "TEST: showSession ${error.javaClass.simpleName}")
                false
            }
        }
    }
}
