package ro.cortizo.headsettochatgpt

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Diagnostics {
    private const val PREFS = "diagnostics"
    private const val KEY_HISTORY = "history"
    private const val MAX_EVENTS = 200

    @Synchronized
    fun record(context: Context, event: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val previous = prefs.getString(KEY_HISTORY, "").orEmpty()
        val lines = ("$timestamp — $event\n" + previous).lineSequence()
            .filter { it.isNotBlank() }.take(MAX_EVENTS).joinToString("\n")
        prefs.edit().putString(KEY_HISTORY, lines).apply()
    }

    fun lastEvent(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_HISTORY, null)
            ?: "Niciun eveniment primit încă."
}
