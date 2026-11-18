# Tasks

## 1. Konfiguration und Sitzung

- [x] 1.1 `GALLERY_PASSWORD` statt `ACCESS_TOKEN` in `application.properties` und Startprüfung (fehlend oder leer: kein Start), mit Test; Verifikation: Kontext-Test startet nicht ohne Passwort
- [x] 1.2 `JwtAuthFilter` liest das Cookie `__session`, Header-Auswertung entfällt; Tests für gültig, abgelaufen, manipuliert und fehlend

## 2. Login

- [x] 2.1 `POST /api/login` mit konstantem Passwortvergleich, Cookie mit `HttpOnly`, `Secure`, `SameSite=Lax`, Laufzeit wenige Stunden; Tests für richtig und falsch
- [x] 2.2 Login-Rate-Limit (5 pro Minute und IP, `Retry-After`, IP aus `X-Forwarded-For`) mit Tests für Sperre und Ablauf
- [x] 2.3 Alten Endpunkt `/api/auth/validate`, `TokenRequest`, `TokenResponse` und CORS-Konfiguration entfernen, nachdem alle Verwendungen geprüft sind; Verifikation: Build und Tests grün

## 3. Galerie-Inhalt

- [x] 3.1 Bucket-Zugriff (`S3Client`, `S3Presigner` für R2, Konfiguration über Umgebung) und Lesen und Zwischenspeichern von `index.json`, mit Test gegen einen Fake
- [x] 3.2 `GET /api/gallery` mit Fotos und Paketen, mit Tests für angemeldet und nicht angemeldet
- [x] 3.3 `GET /api/photos/{id}/{variant}` und `GET /api/zips/{name}` mit 302 auf signierten Link (5 Minuten, `attachment` für Original und ZIP); Tests inkl. unbekannte Kennung und `../`-Angriff
- [x] 3.4 Fehlerbehandlung ohne interne Details (502 oder 503) mit Test

## 4. Skript

- [ ] 4.1 `tools/prepare-gallery`: Vorschau (400 px) und Handy-Version (2560 px, Qualität 85) mit EXIF-Ausrichtung, mit Tests an Beispielbildern
- [ ] 4.2 ZIP-Pakete (ca. 1 GB, Reihenfolge) und Handy-ZIP sowie `index.json`, mit Tests der Aufteilung
- [ ] 4.3 Upload per `rclone` und Wiederholbarkeit ohne doppelte Arbeit, mit Test auf einem Probeordner; Verifikation: zweiter Lauf erzeugt nichts neu

## 5. Betrieb

- [ ] 5.1 Dockerfile und Cloud-Run-Konfiguration (Secrets `JWT_SECRET`, `GALLERY_PASSWORD`, R2-Zugangsdaten, `max-instances` 2); Verifikation: Container startet lokal mit Beispielwerten
- [ ] 5.2 README und `CHANGELOG.md` aktualisieren; Verifikation: README beschreibt Variablen, Skript und Deployment
- [ ] 5.3 Ende-zu-Ende-Prüfung mit dem Frontend (Login, Liste, Vorschau, Download) gegen einen Test-Bucket

## Workflow follow-up

- Review (Opus, danach CodeRabbit gegen `main`), `openspec validate --all --strict`, `/opsx:archive`, Rebase auf `main`, Branch löschen.
