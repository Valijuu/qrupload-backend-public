# Proposal

## Why

Das Backend prüft bisher ein QR-Token und hat nur einen Platzhalter-Endpunkt für Fotos. Für die Galerie nach der Hochzeit braucht es einen Passwort-Login und den Zugriff auf rund 1000 hochauflösende Fotos in einem privaten Cloud-Bucket, ohne dass das Backend Bilder selbst ausliefert oder verarbeitet.

## What Changes

- **BREAKING**: Das QR-Token-Login (`/api/auth/validate`, `ACCESS_TOKEN`) wird durch einen Passwort-Login (`POST /api/login`, `GALLERY_PASSWORD`) ersetzt.
- Die Sitzung läuft über ein HttpOnly-Cookie `__session` (JWT) statt über einen Authorization-Header.
- Rate-Limit für den Login (5 Versuche pro Minute und IP) mit HTTP 429.
- Neue Endpunkte: `GET /api/gallery` (Fotoliste und ZIP-Pakete) und Weiterleitungen auf kurzlebige signierte R2-Links für Vorschau, Handy-Qualität, Original und ZIP.
- Neues Hilfsskript, das lokal Thumbnails, Handy-Qualität und ZIP-Pakete erzeugt und per `rclone` in den Bucket lädt.
- CORS-Freigabe für das Frontend entfällt (gleiche Domain über den Firebase-Rewrite). Betrieb auf Cloud Run mit Secrets.

## Capabilities

### New Capabilities
- `gallery-auth`: Passwort-Login, Cookie-Sitzung, Rate-Limit.
- `gallery-content`: Fotoliste, ZIP-Pakete und signierte Links aus dem privaten Bucket.
- `gallery-asset-preparation`: Lokales Skript für Thumbnails, Handy-Qualität, ZIPs und Upload.

### Modified Capabilities

(keine, es gibt noch keine Specs im Projekt)

## Impact

- Backend (`qrupload-backend-public`): `AuthController`, `JwtAuthFilter`, `JwtService`, `SecurityConfig`, `PhotoController`, neue Klassen für Login-Begrenzung und Bucket-Zugriff, `build.gradle.kts` (S3-SDK), `application.properties`, README.
- Neues Verzeichnis `tools/prepare-gallery`.
- Betrieb: Cloud Run (Secrets `JWT_SECRET`, `GALLERY_PASSWORD`, R2-Zugangsdaten), Cloudflare R2.
- Frontend: siehe Change `danke-galerie-frontend`.
