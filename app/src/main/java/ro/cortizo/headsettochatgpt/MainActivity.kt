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
import android.widget.ScrollView
import android.content.ClipData
import android.content.ClipboardManager

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
            text = "HeadsetToChatGPT v0.3"
            textSize = 24f
        }

        val explanation = TextView(this).apply {
            text = "\nBridge experimental: comanda de asistent primită de Android este redirecționată către aplicația oficială ChatGPT.\n"
            textSize = 16f
        }

        statusView = TextView(this).apply { textSize = 16f }
        diagnosticsView = TextView(this).apply {
            textSize = 14f
            setTextIsSelectable(true)
        }

        val assistantButton = Button(this).apply {
            text = "Setează bridge-ul ca asistent implicit"
            setOnClickListener { requestAssistantRole() }
        }

        val assistantSettingsButton = Button(this).apply {
            text = "Deschide lista de asistenți Android"
            setOnClickListener { openAssistantSettings() }
        }

        val testButton = Button(this).apply {
            text = "Test: pornește vocea ChatGPT"
            setOnClickListener {
                Diagnostics.record(this@MainActivity, "TEST MANUAL VOCE")
                val ok = ChatGptLauncher.launch(this@MainActivity, assistantLayer = false)
                refreshStatus()
                if (!ok) {
                    Toast.makeText(this@MainActivity, "Lansarea a eșuat. Verifică diagnosticul.", Toast.LENGTH_LONG).show()
                }
            }
        }

        val normalButton = Button(this).apply {
            text = "Test: deschide doar aplicația"
            setOnClickListener {
                Diagnostics.record(this@MainActivity, "TEST MANUAL APLICAȚIE")
                ChatGptLauncher.launchNormal(this@MainActivity)
                refreshStatus()
            }
        }
        val copyButton = Button(this).apply {
            text = "Copiază diagnosticul"
            setOnClickListener {
                val report = "HeadsetToChatGPT v0.3; ChatGPT " +
                    ChatGptLauncher.installedVersion(this@MainActivity) +
                    "\n" + statusView.text + "\n" +
                    Diagnostics.lastEvent(this@MainActivity)
                getSystemService(ClipboardManager::class.java)
                    .setPrimaryClip(ClipData.newPlainText("Diagnostic", report))
                Toast.makeText(this@MainActivity, "Diagnostic copiat.", Toast.LENGTH_SHORT).show()
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
        layout.addView(normalButton)
        layout.addView(copyButton)
        layout.addView(refreshButton)
        layout.addView(diagnosticsView)
        setContentView(ScrollView(this).apply { addView(layout) })
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
            append("\nVersiune ChatGPT: ").append(ChatGptLauncher.installedVersion(this@MainActivity))
            append("\nBridge activ ca VoiceInteractionService: ").append(if (active) "DA" else "NU")
            append("\n")
        }
        diagnosticsView.text = "\nUltimele evenimente (cel mai nou sus):\n${Diagnostics.lastEvent(this)}"
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
