package ro.cortizo.headsettochatgpt

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Diagnostics {
    private const val PREFS = "diagnostics"
    private const val KEY_LAST_EVENT = "last_event"

    fun record(context: Context, event: String) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LAST_EVENT, "$timestamp — $event")
            .apply()
    }

    fun lastEvent(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_LAST_EVENT, "Niciun eveniment primit încă.")
            ?: "Niciun eveniment primit încă."
}
