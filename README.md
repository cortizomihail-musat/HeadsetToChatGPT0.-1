# HeadsetToChatGPT v0.2

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
