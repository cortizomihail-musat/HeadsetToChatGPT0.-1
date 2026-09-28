# HeadsetToChatGPT v0.4

Bridge Android experimental: Jabra → asistentul Android → HeadsetToChatGPT → aplicația ChatGPT.
Deschiderea aplicației nu confirmă pornirea conversației vocale.

## Corecția v0.4

v0.3 accepta răspunsul la `ACTION_ASSIST` ca final al încercării de lansare.
Pe telefonul testat, vocea nu pornește, iar comanda Jabra deschide setările
asistentului chiar și cu ChatGPT selectat direct. Acest rezultat nu dovedește
că evenimentul Jabra ajunge la bridge.

- Elimină trimiterile către proxy-urile generice `ASSIST` / `VOICE_ASSIST` din
  lansatorul ChatGPT. Dacă intrarea vocală experimentală nu poate fi lansată,
  încearcă imediat launcher-ul aplicației oficiale `com.openai.chatgpt`.
- Păstrează recepția comenzilor ASSIST / VOICE_ASSIST / VOICE_COMMAND în bridge.
- Distinge lipsa/dezactivarea componentei vocale de lipsa exportării/permisiunii.
- Adaugă **Test: comandă prin bridge**, care cere `showSession` numai serviciului
  activ și inițializat de Android. Acest test nu simulează Bluetooth, dar verifică
  sesiunea și lansarea ChatGPT fără să depindă de comanda căștilor.
- Înregistrează crearea/eșecul sesiunii, sursa manuală a testului și apelurile
  serviciului de recunoaștere. Acesta din urmă nu implementează dictarea.
- Afișează versiunea reală din APK și păstrează 40 de evenimente locale.

## Instalare și test pe HONOR

1. Instalează APK-ul și verifică titlul **v0.4**. APK-urile debug generate în rulări
   diferite pot avea semnături diferite: dacă actualizarea este refuzată, dezinstalează
   numai HeadsetToChatGPT, reinstalează și selectează din nou asistentul.
2. În Jabra Sound+ alege **Android default**; în setările telefonului alege
   **HeadsetToChatGPT** ca aplicație Asistent pentru testul bridge-ului.
3. Cu telefonul deblocat, apasă **Test: comandă prin bridge**. Notează dacă apare
   ChatGPT, vocea, setările sau nimic.
4. Revino pe Home și execută gestul de asistent configurat pe căști.
5. Revino în bridge și apasă **Copiază diagnosticul**. Trimite textul împreună cu
   rezultatul celor două teste.

`MANUAL_SESSION_TEST` marchează testul intern, nu o comandă Jabra.
`VoiceInteractionService ready` confirmă inițializarea, nu apăsarea căștii.
`SESSION CREATED`, `Voice session` și `PROXY` arată ce intrare a fost folosită.
`SESSION FAILED` arată un eșec raportat de Android.
`LAUNCHER` înseamnă deschiderea obișnuită a aplicației; vocea rămâne neconfirmată.
Absența evenimentelor după apăsare necesită investigarea traseului Android/Jabra;
nu este rezolvată prin schimbarea adresei site-ului sau a launcher-ului.

## Limitări

- Intrarea `com.openai.voice.assistant.AssistantActivity` este experimentală,
  observată în anumite versiuni ChatGPT, nu un API public stabil OpenAI.
- Nu activează componente dezactivate, nu accesează componente neexportate,
  nu ocolește permisiuni și nu automatizează apăsări pe ecran.
- Bridge-ul nu înregistrează audio. Testul sesiunii nu cere textul ecranului sau capturi.
- Testează cu telefonul deblocat; pornirea de pe ecranul blocat nu este implementată.
- Nici acceptarea unui Intent, nici un build reușit nu dovedesc că vocea ascultă.
- v0.4 corectează fallback-ul și permite localizarea problemei; nu pretinde că
  rezolvă rutarea butonului Jabra pe toate versiunile MagicOS.

Referințe:
- https://developer.android.com/reference/android/service/voice/VoiceInteractionService
- https://github.com/keymapperorg/KeyMapper/issues/1733#issuecomment-3039506479
- https://help.openai.com/en/articles/20001274-chatgpt-voice

## Compilare

JDK 17, Android SDK 35, AGP 8.7.3, Kotlin 2.0.21, Gradle 8.9.

`gradle :app:assembleDebug --stacktrace`

Workflow-ul `.github/workflows/android.yml` compilează la push pe `main` sau manual
prin **Actions → Build Android APK → Run workflow**. Artifactul ZIP
`HeadsetToChatGPT-debug-apk` conține `app-debug.apk`.
