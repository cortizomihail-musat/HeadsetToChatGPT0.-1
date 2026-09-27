package ro.cortizo.headsettochatgpt

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionService
import android.speech.SpeechRecognizer

/**
 * Serviciu minim necesar pentru profilul de VoiceInteractionService.
 * Bridge-ul nu face recunoaștere vocală; ChatGPT se ocupă de voce după lansare.
 */
class BridgeRecognitionService : RecognitionService() {
    override fun onStartListening(recognizerIntent: Intent?, listener: Callback?) {
        listener?.error(SpeechRecognizer.ERROR_CLIENT)
    }

    override fun onStopListening(listener: Callback?) = Unit

    override fun onCancel(listener: Callback?) = Unit
}
