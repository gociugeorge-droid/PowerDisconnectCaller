# Power Disconnect Caller — versiunea 1.1

Proiect Android Kotlin pentru Samsung Galaxy A54 / Android 16.

## Funcții
- Ascultă evenimentele de conectare/deconectare a alimentării.
- Monitorizează doar după activarea explicită din aplicație.
- La activare verifică dacă telefonul este conectat la încărcător.
- La deconectare încearcă un apel direct prin TelecomManager.
- Întârziere configurabilă 0–5 secunde.
- Un singur apel pentru fiecare întrerupere; reconectarea rearmează detectorul.
- După repornire, se rearmează dacă aplicația era activă și telefonul este alimentat.

## Instalare/dezvoltare
1. Deschide folderul în Android Studio (versiune recentă).
2. Așteaptă sincronizarea Gradle.
3. Conectează Samsung A54 prin USB cu USB debugging activat.
4. Rulează aplicația pe telefon.
5. Introdu numărul în format internațional (+40...).
6. Salvează setările și acordă permisiunea CALL_PHONE.
7. Activează detectorul în timp ce telefonul este conectat la încărcător.
8. Adaugă aplicația la Never sleeping apps și setează bateria pe Unrestricted, dacă opțiunea este disponibilă.

## Limitări și testare
- Acesta este cod-sursă; arhiva nu conține un APK compilat.
- Apelarea automată trebuie testată pe telefonul real. Android/One UI/operatorul pot limita inițierea apelurilor din fundal.
- Testează cu un număr propriu și cu telefonul supravegheat.
- Dacă alimentarea cade în timp ce telefonul este oprit sau complet descărcat, nu poate detecta evenimentul.
- Dacă rețeaua mobilă cade odată cu curentul, apelul poate eșua.
- Întârzierea de 0–5 secunde este o fereastră scurtă de verificare; nu reprezintă o garanție de execuție exactă la secundă.
