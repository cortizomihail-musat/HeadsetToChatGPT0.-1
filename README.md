# HeadsetToChatGPT v0.3

Bridge Android experimental pentru a testa traseul:

`Jabra Voice Command -> Android Assistant -> HeadsetToChatGPT -> ChatGPT`

## Ce s-a schimbat în v0.2

- implementează `VoiceInteractionService`, deci poate candida pentru rolul Android Assistant;
- adaugă și un `ACTION_ASSIST` proxy, ca fallback pentru ROM-uri/OEM-uri care invocă asistentul prin Intent;
- redirecționează explicit către pachetul oficial `com.openai.chatgpt`;
- salvează ultimul eveniment primit pentru diagnostic;
- interfața arată dacă bridge-ul este serviciul VoiceInteraction activ;
- include GitHub Actions pentru compilarea automată a APK-ului.

## Test pe HONOR

1. Compilează și instalează APK-ul debug.
2. Deschide **HeadsetToChatGPT**.
3. Apasă **Setează bridge-ul ca asistent implicit**.
4. Dacă HONOR deschide lista manuală, alege **HeadsetToChatGPT** la aplicația de asistent digital.
5. Ieși pe Home.
6. Cu Jabra Elite 8 Active conectate și comanda de asistent configurată pe gestul ales, execută gestul.
7. Dacă bridge-ul primește evenimentul, ar trebui să deschidă ChatGPT.
8. Revino în HeadsetToChatGPT și apasă **Reîmprospătează diagnosticul** pentru a vedea ultima sursă detectată.

## Dacă nu apare în lista de asistenți

Aplicația declară două mecanisme acceptate de Android pentru rolul Assistant: un `VoiceInteractionService` și un handler `ACTION_ASSIST`. Dacă HONOR nu o listează, verifică instalarea și diagnosticul înainte de a atribui problema sistemului MagicOS.

## Build în Android Studio

- JDK 17
- Android SDK 35
- Android Gradle Plugin 8.7.3
- Kotlin 2.0.21

Deschide folderul proiectului în Android Studio, lasă Gradle Sync să termine, apoi:

`Build > Build APK(s)`

APK-ul debug va fi în:

`app/build/outputs/apk/debug/app-debug.apk`

## Build prin GitHub Actions

Workflow-ul `.github/workflows/android.yml` compilează automat APK-ul la push pe `main` sau manual prin **Actions > Build Android APK > Run workflow**. APK-ul apare ca artifact `HeadsetToChatGPT-debug-apk`.

## Limitări

- Nu modifică aplicația oficială ChatGPT.
- Nu garantează că ChatGPT va intra direct în Voice; v0.2 validează mai întâi lansarea din căști.
- Comportamentul poate diferi pe MagicOS față de Android standard.

## Corecții incluse

- Lansarea normală este încercată și dacă ACTION_ASSIST aruncă o excepție.
- Sesiunea și interfața rulează în același proces pentru diagnosticul SharedPreferences.
- Testează întâi cu telefonul deblocat; lansarea de pe ecranul blocat nu este activată.

## v0.3: lansarea vocii și diagnostic

- Încearcă explicit com.openai.voice.assistant.AssistantActivity, fără ACTION_ASSIST,
  numai dacă activitatea există, este activă, exportată și accesibilă.
- Apoi încearcă VOICE_ASSIST, ASSIST și lansarea normală a aplicației.
- Componenta vocală este o integrare experimentală, nu un API public garantat OpenAI.
  Referință de implementare și diferența observată între cele două activități:
  https://github.com/keymapperorg/KeyMapper/issues/1733#issuecomment-3039506479
- Android acceptând o cerere de lansare NU confirmă că vocea ascultă.
- Păstrează 20 de evenimente locale, cu ruta folosită și sursa invocării.
- Nu distruge imediat sesiunea după lansarea activității asistent.
- Nu activează componente dezactivate, nu ocolește permisiuni și nu automatizează ecranul.

### Test pe telefon

1. Instalează v0.3 și verifică numărul versiunii afișate.
2. Cu telefonul deblocat apasă «Test: pornește vocea ChatGPT».
3. Confirmă vizual dacă începe vocea; cererea trimisă nu este dovada pornirii ei.
4. Revino, apoi testează separat gestul asistentului telefonului și butonul Jabra.
5. Apasă «Copiază diagnosticul». TEST MANUAL și Voice session/PROXY sunt surse distincte.
6. Dacă apare LAUNCHER, s-a folosit deschiderea normală și vocea nu este confirmată.

GitHub Actions generează un APK debug. Cheia debug poate diferi între rulări.
Dacă Android refuză actualizarea din cauza semnăturii, dezinstalează numai
HeadsetToChatGPT și instalează noul APK, apoi reselectează asistentul.
Nu dezinstala aplicația ChatGPT.
