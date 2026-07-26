# qrupload-backend

Spring Boot Backend für die QRUpload-Hochzeitsseite. Prüft den Zugriffstoken aus dem QR-Code und stellt bei Erfolg ein JWT aus, das anschließend für geschützte Endpunkte (z. B. `/api/photos`) nötig ist.

## Konfiguration (Pflicht)

Die Anwendung benötigt zwei Umgebungsvariablen. Ohne sie startet sie bewusst **nicht**:

| Variable | Zweck | Anforderung |
|---|---|---|
| `JWT_SECRET` | Signierschlüssel für die JWTs | mindestens 32 Zeichen, zufällig |
| `ACCESS_TOKEN` | Token, der auch im QR-Code / in der Frontend-Konfiguration steht | muss mit dem Wert im Frontend (`environment.prod.ts` → `accessToken`) übereinstimmen |

Beispiel (lokal, bash):

```bash
export JWT_SECRET="<zufälliger String, mind. 32 Zeichen>"
export ACCESS_TOKEN="<derselbe Token wie im Frontend>"
./gradlew bootRun
```

In einer echten Deployment-Umgebung (Server, Container, CI/CD) als Secret/Umgebungsvariable setzen, niemals in `application.properties` oder Code hartcodieren.

## CORS

`SecurityConfig` erlaubt aktuell nur `http://localhost:4200` als Origin. Vor einem Produktiv-Deployment muss dort die echte Frontend-URL ergänzt werden.

## Gradle Wrapper

`gradle/wrapper/gradle-wrapper.jar` ist nicht Teil dieses initialen Commits (Binärdatei). Vor dem ersten Build entweder aus dem privaten Arbeits-Repo `qrupload-backend` kopieren oder lokal `gradle wrapper` ausführen, um sie neu zu erzeugen.
