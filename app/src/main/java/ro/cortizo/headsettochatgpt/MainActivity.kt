package ro.cortizo.headsettochatgpt

import android.app.Activity
import android.app.role.RoleManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.service.voice.VoiceInteractionService
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var statusView: TextView
    private lateinit var diagnosticsView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 64, 48, 48)
        }

        val title = TextView(this).apply {
            text = "HeadsetToChatGPT v0.2"
            textSize = 24f
        }

        val explanation = TextView(this).apply {
            text = "\nBridge experimental: comanda de asistent primită de Android este redirecționată către aplicația oficială ChatGPT.\n"
            textSize = 16f
        }

        statusView = TextView(this).apply { textSize = 16f }
        diagnosticsView = TextView(this).apply { textSize = 14f }

        val assistantButton = Button(this).apply {
            text = "Setează bridge-ul ca asistent implicit"
            setOnClickListener { requestAssistantRole() }
        }

        val assistantSettingsButton = Button(this).apply {
            text = "Deschide lista de asistenți Android"
            setOnClickListener { openAssistantSettings() }
        }

        val testButton = Button(this).apply {
            text = "Test: deschide ChatGPT"
            setOnClickListener {
                val ok = ChatGptLauncher.launch(this@MainActivity, assistantLayer = false)
                Diagnostics.record(this@MainActivity, "Manual ChatGPT launch result=$ok")
                if (!ok) {
                    Toast.makeText(this@MainActivity, "ChatGPT nu a fost găsit.", Toast.LENGTH_LONG).show()
                }
            }
        }

        val refreshButton = Button(this).apply {
            text = "Reîmprospătează diagnosticul"
            setOnClickListener { refreshStatus() }
        }

        layout.addView(title)
        layout.addView(explanation)
        layout.addView(statusView)
        layout.addView(assistantButton)
        layout.addView(assistantSettingsButton)
        layout.addView(testButton)
        layout.addView(refreshButton)
        layout.addView(diagnosticsView)
        setContentView(layout)
    }

    override fun onResume() {
        super.onResume()
        refreshStatus()
    }

    private fun refreshStatus() {
        val service = ComponentName(this, BridgeVoiceInteractionService::class.java)
        val active = VoiceInteractionService.isActiveService(this, service)
        val installed = packageManager.getLaunchIntentForPackage("com.openai.chatgpt") != null

        statusView.text = buildString {
            append("\nChatGPT instalat: ").append(if (installed) "DA" else "NU")
            append("\nBridge activ ca VoiceInteractionService: ").append(if (active) "DA" else "NU")
            append("\n")
        }
        diagnosticsView.text = "\nUltimul eveniment:\n${Diagnostics.lastEvent(this)}"
    }

    private fun requestAssistantRole() {
        val roleManager = getSystemService(RoleManager::class.java)
        if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_ASSISTANT)) {
            if (roleManager.isRoleHeld(RoleManager.ROLE_ASSISTANT)) {
                Toast.makeText(this, "Bridge-ul deține deja rolul Assistant.", Toast.LENGTH_SHORT).show()
            } else {
                startActivityForResult(
                    roleManager.createRequestRoleIntent(RoleManager.ROLE_ASSISTANT),
                    REQ_ASSISTANT
                )
            }
        } else {
            openAssistantSettings()
        }
    }

    private fun openAssistantSettings() {
        val intents = listOf(
            Intent(Settings.ACTION_VOICE_INPUT_SETTINGS),
            Intent("android.settings.MANAGE_DEFAULT_APPS_SETTINGS")
        )
        val target = intents.firstOrNull { it.resolveActivity(packageManager) != null }
        if (target != null) startActivity(target)
        else Toast.makeText(this, "Nu am găsit pagina de setări pentru asistent.", Toast.LENGTH_LONG).show()
    }

    companion object {
        private const val REQ_ASSISTANT = 1001
    }
}
