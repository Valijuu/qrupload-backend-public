# qrupload-backend

Spring Boot Backend für die Foto-Galerie nach der Hochzeit. Gäste melden sich mit einem gemeinsamen Passwort an und bekommen ein Sitzungs-Cookie (`__session`). Das Backend liefert die Foto-Liste und leitet auf kurzlebige, signierte Links in einem privaten Cloudflare-R2-Bucket weiter. Bilder laufen nie durch das Backend.

## Konfiguration (Pflicht)

Ohne diese Umgebungsvariablen startet die Anwendung bewusst **nicht**. In Cloud Run kommen sie als Secrets, niemals in `application.properties` oder in den Code.

| Variable | Zweck | Anforderung |
|---|---|---|
| `JWT_SECRET` | Signierschlüssel für die Sitzung | mindestens 32 Zeichen, zufällig |
| `GALLERY_PASSWORD` | gemeinsames Galerie-Passwort | lang (z. B. vier Wörter), nach dem Anlass ändern |
| `GALLERY_STORAGE_ENDPOINT` | S3-Endpunkt des R2-Buckets | `https://<konto>.r2.cloudflarestorage.com` |
| `GALLERY_STORAGE_BUCKET` | Name des Buckets | |
| `GALLERY_STORAGE_ACCESS_KEY` | R2-Zugangsschlüssel | nur Leserechte auf den Bucket |
| `GALLERY_STORAGE_SECRET_KEY` | R2-Geheimschlüssel | |

Lokal (bash):

```bash
export JWT_SECRET="<zufälliger String, mind. 32 Zeichen>"
export GALLERY_PASSWORD="<Passwort>"
export GALLERY_STORAGE_ENDPOINT="https://<konto>.r2.cloudflarestorage.com"
export GALLERY_STORAGE_BUCKET="<bucket>"
export GALLERY_STORAGE_ACCESS_KEY="<key>"
export GALLERY_STORAGE_SECRET_KEY="<secret>"
./gradlew bootRun
```

Die Tests brauchen keine Umgebungsvariablen: `./gradlew test`.

## Schnittstellen

| Aufruf | Zweck |
|---|---|
| `POST /api/login` mit `{"password": "..."}` | 204 und Cookie `__session`, sonst 401. Mehr als 5 Versuche pro Minute und IP: 429 mit `Retry-After`. |
| `GET /api/gallery` | Fotos (`id`, `file`) und ZIP-Pakete (`name`, `bytes`, `kind`: `original` oder `mobile`) |
| `GET /api/photos/{id}/{thumb\|mobile\|original}` | 302 auf einen 5 Minuten gültigen, signierten Link (Original als Download) |
| `GET /api/zips/{name}` | 302 auf den signierten Download-Link des ZIP-Pakets |

Alle außer dem Login brauchen das Cookie. Bei Bucket-Problemen antwortet das Backend mit 503 ohne interne Details.

## Fotos vorbereiten und hochladen

Das Skript `tools/prepare-gallery` (Node 20+) erzeugt Vorschaubilder (400 px), Handy-Versionen (2560 px, JPEG 85), ZIP-Pakete (ca. 1 GB, plus `handy.zip`) und die `index.json`. Es lässt die Originale unverändert und ist wiederholbar: Ein zweiter Lauf erzeugt nichts neu, rclone überspringt Unverändertes. Hochgeladen werden nur die Bilddateien des Fotoordners (ohne Unterordner), `index.json` kommt als letzter Schritt. HEIC- und RAW-Dateien werden übersprungen, bitte vorher als JPEG exportieren.

```bash
cd tools/prepare-gallery
npm ci
node src/cli.js /pfad/zu/den/fotos /pfad/zum/ausgabeordner --remote r2:wedding
```

`--remote` ist ein rclone-Remote (`rclone config`, die Zugangsdaten stehen in der rclone-Konfiguration, nicht im Repo). Einrichtung: Storage `s3`, Provider `Cloudflare`, `env_auth` `false`, Access Key ID und Secret eines R2-Tokens mit **Object Read & Write** auf den Bucket, Endpoint `https://<konto-id>.r2.cloudflarestorage.com`, Region leer. Das Skript übergibt `--s3-no-check-bucket`, weil ein Object-Token keinen Bucket anlegen darf und rclone es sonst vor jedem Upload versucht (Fehler 403).

Einrichtung prüfen (liest, schreibt eine Testdatei nach `_rights-check/` und löscht sie wieder):

```bash
tools/r2-check/check-r2.sh            # Standard: Remote r2, Bucket wedding
tools/r2-check/check-r2.sh r2 <bucket>
``` Ohne `--remote` wird nur lokal erzeugt. Im Bucket entsteht `originals/`, `thumbs/`, `mobile/`, `zips/` und `index.json`.

## Betrieb auf Cloud Run

- Container bauen und bereitstellen, z. B.:

  ```bash
  gcloud run deploy qrupload-backend --source . --region europe-west3 \
    --max-instances 2 --allow-unauthenticated \
    --set-secrets JWT_SECRET=jwt-secret:latest,GALLERY_PASSWORD=gallery-password:latest,GALLERY_STORAGE_ACCESS_KEY=r2-access-key:latest,GALLERY_STORAGE_SECRET_KEY=r2-secret-key:latest \
    --set-env-vars GALLERY_STORAGE_ENDPOINT=https://<konto>.r2.cloudflarestorage.com,GALLERY_STORAGE_BUCKET=<bucket>
  ```

- **Rückfall:** Frontend und Backend gehören zusammen. Wird nur die alte Cloud-Run-Revision aktiviert, funktioniert der Login nicht, weil das Frontend `POST /api/login` aufruft und die alte Revision `/api/auth/validate` mit Header erwartet. Für einen Rückfall also auch die passende Frontend-Version in Firebase Hosting zurückholen (Konsole → Hosting → Versionen → Rollback) und die alte Revision samt ihrer Konfiguration (`ACCESS_TOKEN`) bis zum Ende des Rückfallfensters behalten.
- `--max-instances` klein halten (z. B. 2), weil das Login-Limit im Speicher liegt.
- `--allow-unauthenticated` ist nötig, damit Firebase Hosting den Dienst erreicht. Jeder Endpunkt außer dem Login prüft selbst die Sitzung.
- Das Frontend (Firebase Hosting) leitet `/api/**` per Rewrite an diesen Dienst weiter. So liegt alles auf einer Domain, und das Cookie funktioniert auch in Safari. CORS ist deshalb nicht nötig.
- Der Rewrite reicht nur das Cookie `__session` durch. Der Name ist daher fest.

## Nach dem Anlass

- `GALLERY_PASSWORD` **und** `JWT_SECRET` rotieren: Ein neues Passwort allein beendet laufende Sitzungen nicht. Ablauf: neue Secret-Versionen anlegen, eine neue Cloud-Run-Revision mit genau diesen Versionen ausrollen (`--set-secrets ...:<version>`, nicht `latest`), den gesamten Traffic auf sie stellen und getaggte Routen zur alten Revision entfernen. Cloud Run liest Secrets beim Start einer Instanz, eine laufende alte Revision akzeptiert sonst weiter das alte Passwort. Danach prüfen: neues Passwort funktioniert, altes Passwort und ein vorher ausgestelltes Cookie werden abgewiesen.
- Oder den Bucket und den Dienst ganz löschen.

## Bekannte Einschränkung

Das Login-Limit nimmt die IP aus dem ersten `X-Forwarded-For`-Eintrag und ist darüber umgehbar, siehe Issue #1.
