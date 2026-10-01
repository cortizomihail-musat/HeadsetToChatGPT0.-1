# HeadsetToChatGPT v0.6

Bridge Android experimental: **Jabra → asistent Android → HeadsetToChatGPT → ChatGPT Voice în browser**.

## Schimbarea principală

Testul pe HONOR NLA-LX1 / Android 15 cu Jabra a confirmat că `https://chatgpt.com/voice`
deschide o sesiune ChatGPT Voice funcțională în browser. v0.6 folosește această rută
drept țintă principală și nu mai încearcă activitatea internă ChatGPT Voice, care în
build-ul testat nu este exportată.

```
Jabra
  ↓
Android / MagicOS
  ↓
HeadsetToChatGPT
  ↓
ACTION_VIEW https://chatgpt.com/voice
  ↓
browser
  ↓
ChatGPT Voice
```

## Testare

1. Instalează v0.6 și verifică titlul aplicației.
2. Păstrează HeadsetToChatGPT ca aplicație Asistent Android.
3. Apasă **Test: ChatGPT Voice în browser**.
4. Revino și apasă **Test: comandă prin bridge → Web Voice**.
5. Revino pe Home și execută gestul Jabra pentru asistent.
6. Dacă Jabra nu deschide Voice, revino în bridge și apasă **Copiază diagnosticul**.

Dacă după gestul Jabra nu apare niciun eveniment nou, blocajul rămâne înainte de bridge,
în rutarea Jabra / Android / MagicOS. URL-ul Web Voice rezolvă ținta ChatGPT, nu poate
intercepta singur un eveniment pe care sistemul nu îl livrează aplicației.

## Diagnostic

- `WEB_VOICE`: bridge-ul a cerut deschiderea `https://chatgpt.com/voice`.
- `Voice session`: Android a creat sesiunea bridge.
- `PROXY`: OEM-ul a trimis ASSIST / VOICE_ASSIST / VOICE_COMMAND.
- `LAUNCHER`: fallback-ul deschide numai aplicația ChatGPT.

## Limitări

- Nu automatizează apăsări pe ecran și nu accesează componente private ChatGPT.
- Browserul trebuie să aibă permisiunea pentru microfon și utilizatorul să fie autentificat.
- Rutarea gestului Jabra către aplicația Asistent depinde de Android/MagicOS.

## Compilare

JDK 17, Android SDK 35, AGP 8.7.3, Kotlin 2.0.21, Gradle 8.9.

`gradle :app:assembleDebug --stacktrace`

Workflow-ul `.github/workflows/android.yml` compilează la push pe `main`.
Artifactul `HeadsetToChatGPT-debug-apk` conține `app-debug.apk`.
