package ro.cortizo.headsettochatgpt

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings

object AssistantRouting {
    fun service(context: Context): String = try {
        Settings.Secure.getString(context.contentResolver, "voice_interaction_service").orEmpty()
    } catch (_: SecurityException) { "indisponibil" }

    fun inspect(context: Context) {
        Diagnostics.record(context, "ROUTE voice_service=${service(context)}")
        for (action in listOf(Intent.ACTION_ASSIST, Intent.ACTION_VOICE_ASSIST, Intent.ACTION_VOICE_COMMAND)) {
            val intent = Intent(action)
            val chosen = context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)?.activityInfo
            val handlers = context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
                .joinToString(",") { "${it.activityInfo.packageName}/${it.activityInfo.name}" }
            val own = context.packageManager.resolveActivity(Intent(action).setPackage(context.packageName), PackageManager.MATCH_DEFAULT_ONLY)?.activityInfo
            Diagnostics.record(context, "ROUTE $action; selected=${chosen?.packageName}/${chosen?.name}; own=${own?.name}; candidates=$handlers")
        }
    }

    fun testVoiceCommand(context: Context, ownPackage: Boolean) {
        inspect(context)
        val intent = Intent(Intent.ACTION_VOICE_COMMAND).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (ownPackage) intent.setPackage(context.packageName)
        Diagnostics.record(context, "ROUTE TEST VOICE_COMMAND; ownPackage=$ownPackage; test software, nu dovadă rută Jabra")
        try { context.startActivity(intent) }
        catch (error: RuntimeException) { Diagnostics.record(context, "ROUTE TEST FAILED ${error.javaClass.simpleName}") }
    }
}
