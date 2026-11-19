# Changelog

Alle sichtbaren Änderungen dieses Projekts. Format nach [Keep a Changelog](https://keepachangelog.com/de/).

## [Unreleased]

### Added
- Passwort-Login für die Foto-Galerie (`POST /api/login`) mit Sitzungs-Cookie `__session`.
- Begrenzung der Login-Versuche (5 pro Minute und IP).
- Foto-Liste (`GET /api/gallery`) und kurzlebige, signierte Links für Vorschau, Handy-Version, Original und ZIP-Pakete aus einem privaten Cloudflare-R2-Bucket.
- Skript `tools/prepare-gallery`, das Vorschaubilder, Handy-Versionen und ZIP-Pakete erzeugt und in den Bucket hochlädt.
- Dockerfile für den Betrieb auf Cloud Run.

### Changed
- Das Vorbereitungs-Skript ordnet die Fotos nach der Zahl am Ende des Dateinamens.
- Die Anmeldung läuft nicht mehr über das QR-Token, sondern über ein gemeinsames Passwort (`GALLERY_PASSWORD`).

### Fixed
- Das Login-Limit ließ sich über einen gefälschten `X-Forwarded-For`-Header umgehen. Die Client-IP kommt jetzt von den vertrauten Proxys (`TRUSTED_PROXY_HOPS`).

### Removed
- QR-Token-Login (`/api/auth/validate`, `ACCESS_TOKEN`) und die CORS-Freigabe.
