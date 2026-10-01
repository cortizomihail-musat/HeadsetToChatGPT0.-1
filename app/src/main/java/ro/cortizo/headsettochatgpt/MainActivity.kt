package ro.cortizo.headsettochatgpt

import android.app.Activity
import android.app.role.RoleManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.os.Build
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

    private var pendingDiagnostic = ""
    private lateinit var statusView: TextView
    private lateinit var diagnosticsView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 64, 48, 48)
        }

        val title = TextView(this).apply {
            text = "HeadsetToChatGPT v${bridgeVersion()}"
            textSize = 24f
        }

        val explanation = TextView(this).apply {
            text = "\nRuta v0.7: Jabra / asistent Android → bridge → chatgpt.com/voice. Browser Voice este ruta principală; aplicația ChatGPT rămâne fallback.\n"
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
            text = "Test: ChatGPT Voice în browser"
            setOnClickListener {
                Diagnostics.record(this@MainActivity, "TEST MANUAL VOCE")
                val ok = ChatGptLauncher.launch(this@MainActivity, assistantLayer = false)
                refreshStatus()
                if (!ok) {
                    Toast.makeText(this@MainActivity, "Lansarea a eșuat. Verifică diagnosticul.", Toast.LENGTH_LONG).show()
                }
            }
        }

        val sessionButton = Button(this).apply {
            text = "Test: comandă prin bridge → Web Voice"
            setOnClickListener {
                val requested = BridgeVoiceInteractionService.testSession(this@MainActivity)
                refreshStatus()
                if (!requested) {
                    Toast.makeText(this@MainActivity, "Testul nu a pornit. Vezi diagnosticul.", Toast.LENGTH_LONG).show()
                }
            }
        }

        val normalButton = Button(this).apply {
            text = "Test fallback: deschide aplicația ChatGPT"
            setOnClickListener {
                Diagnostics.record(this@MainActivity, "TEST MANUAL APLICAȚIE")
                ChatGptLauncher.launchNormal(this@MainActivity)
                refreshStatus()
            }
        }
        val copyButton = Button(this).apply {
            text = "Copiază diagnosticul"
            setOnClickListener {
                refreshStatus()
                val report = "HeadsetToChatGPT v${bridgeVersion()}; ChatGPT " +
                    ChatGptLauncher.installedVersion(this@MainActivity) +
                    "\nTelefon: ${Build.MANUFACTURER} ${Build.MODEL}; Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})" +
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
        layout.addView(sessionButton)
        layout.addView(testButton)
        layout.addView(normalButton)
        for (label in listOf("A Google/Gemini", "B Bridge")) {
            layout.addView(Button(this).apply {
                text = "START DIAGNOSTIC 30s — $label"
                setOnClickListener { startDiagnostic(label) }
            })
        }
        layout.addView(Button(this).apply {
            text = "Oprește diagnosticul"
            setOnClickListener { stopService(Intent(this@MainActivity, HeadsetDiagnosticService::class.java)); refreshStatus() }
        })
        layout.addView(TextView(this).apply {
            text = "Alege întâi asistentul A sau B. START → ecran principal → aceeași apăsare Jabra. După 30s copiază logul și spune dacă asistentul a ascultat. Nu capturăm microfonul sau evenimentele interne Gemini."
        })
        layout.addView(copyButton)
        layout.addView(refreshButton)
        layout.addView(diagnosticsView)
        setContentView(ScrollView(this).apply { addView(layout) })
    }

    override fun onResume() {
        super.onResume()
        refreshStatus()
    }

    private fun startDiagnostic(label: String) {
        if (Build.VERSION.SDK_INT >= 31 && checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            pendingDiagnostic = label
            requestPermissions(arrayOf(android.Manifest.permission.BLUETOOTH_CONNECT), 1002)
            return
        }
        try {
            startForegroundService(Intent(this, HeadsetDiagnosticService::class.java).putExtra("label", label))
            Toast.makeText(this, "Diagnostic $label pornit pentru 30s.", Toast.LENGTH_LONG).show()
        } catch (error: RuntimeException) {
            Diagnostics.record(this, "Diagnostic start: ${error.javaClass.simpleName}")
        }
        refreshStatus()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1002) {
            val label = pendingDiagnostic
            pendingDiagnostic = ""
            if (grantResults.firstOrNull() == android.content.pm.PackageManager.PERMISSION_GRANTED) startDiagnostic(label)
            else Toast.makeText(this, "Diagnostic Bluetooth: permisiune refuzată.", Toast.LENGTH_LONG).show()
        }
    }

    override fun dispatchKeyEvent(event: android.view.KeyEvent): Boolean {
        if (event.keyCode in listOf(android.view.KeyEvent.KEYCODE_HEADSETHOOK, android.view.KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, android.view.KeyEvent.KEYCODE_VOICE_ASSIST)) {
            Diagnostics.record(this, "ACTIVITY KEY ${android.view.KeyEvent.keyCodeToString(event.keyCode)}; action=${event.action}; doar când activitatea primește evenimentul")
        }
        return super.dispatchKeyEvent(event)
    }

    private fun bridgeVersion(): String =
        packageManager.getPackageInfo(packageName, 0).versionName ?: "?"

    private fun refreshStatus() {
        val service = ComponentName(this, BridgeVoiceInteractionService::class.java)
        val active = VoiceInteractionService.isActiveService(this, service)
        val installed = packageManager.getLaunchIntentForPackage("com.openai.chatgpt") != null

        statusView.text = buildString {
            append("\nChatGPT instalat: ").append(if (installed) "DA" else "NU")
            append("\nVersiune ChatGPT: ").append(ChatGptLauncher.installedVersion(this@MainActivity))
            append("\nBridge activ ca VoiceInteractionService: ").append(if (active) "DA" else "NU")
            if (!active) append("\nPentru testul Jabra cu bridge, alege HeadsetToChatGPT la Aplicația Asistent.")
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
