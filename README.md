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

Das Skript `tools/prepare-gallery` (Node 20+) erzeugt Vorschaubilder (400 px), Handy-Versionen (2560 px, JPEG 85), ZIP-Pakete (ca. 1 GB, plus `handy.zip`) und die `index.json`. Es lässt die Originale unverändert und ist wiederholbar: Ein zweiter Lauf erzeugt und lädt nichts doppelt.

```bash
cd tools/prepare-gallery
npm ci
node src/cli.js /pfad/zu/den/fotos /pfad/zum/ausgabeordner --remote r2:hochzeit
```

`--remote` ist ein rclone-Remote (`rclone config`, die Zugangsdaten stehen in der rclone-Konfiguration, nicht im Repo). Ohne `--remote` wird nur lokal erzeugt. Im Bucket entsteht `originals/`, `thumbs/`, `mobile/`, `zips/` und `index.json`.

## Betrieb auf Cloud Run

- Container bauen: `docker build -t qrupload-backend .`
- `--max-instances` klein halten (z. B. 2), weil das Login-Limit im Speicher liegt.
- Das Frontend (Firebase Hosting) leitet `/api/**` per Rewrite an diesen Dienst weiter. So liegt alles auf einer Domain, und das Cookie funktioniert auch in Safari. CORS ist deshalb nicht nötig.
- Der Rewrite reicht nur das Cookie `__session` durch. Der Name ist daher fest.

## Bekannte Einschränkung

Das Login-Limit nimmt die IP aus dem ersten `X-Forwarded-For`-Eintrag und ist darüber umgehbar, siehe Issue #1.
