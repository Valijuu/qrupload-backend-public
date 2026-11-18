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
- Die Anmeldung läuft nicht mehr über das QR-Token, sondern über ein gemeinsames Passwort (`GALLERY_PASSWORD`).

### Removed
- QR-Token-Login (`/api/auth/validate`, `ACCESS_TOKEN`) und die CORS-Freigabe.
