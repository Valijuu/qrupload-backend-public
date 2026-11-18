# Design

## Context

Das Backend ist Spring Boot 4 (Java 17). Aktuell: `AuthController` prüft ein QR-Token (`ACCESS_TOKEN`), `JwtService` erzeugt ein JWT, `JwtAuthFilter` liest es aus dem Authorization-Header, `SecurityConfig` ist stateless mit CORS für `localhost:4200` und deaktiviertem CSRF. `PhotoController` ist ein Platzhalter. Motivation: siehe proposal.md.

## Goals / Non-Goals

**Goals:**
- Passwort-Login mit Cookie-Sitzung und Rate-Limit.
- Bilder nie durch das Backend streamen: nur Liste und Weiterleitung auf signierte R2-Links.
- Betrieb auf Cloud Run mit Secrets.

**Non-Goals:**
- Kein Upload, keine Bildverarbeitung im Backend, keine Nutzerverwaltung.

## Decisions

- **Cookie `__session`:** Firebase Hosting reicht nur dieses Cookie an Cloud Run durch. `JwtAuthFilter` liest das Cookie statt des Headers. Alternative: Authorization-Header, verworfen (`<img>` und Downloads senden ihn nicht).
- **CSRF bleibt aus, begründet:** Es gibt keine zustandsändernde Aktion außer dem Login (nimmt nur das Passwort an). Das Cookie ist `SameSite=Lax`, alle anderen Endpunkte sind GET und nur lesend. Das wird in `SecurityConfig` kommentiert.
- **CORS entfernen:** Durch den Rewrite gleiche Domain. Für lokale Entwicklung bleibt der Angular-Proxy (`ng serve --proxy-config`) statt CORS.
- **Rate-Limit im Speicher:** Zähler pro IP (Fenster 1 Minute). Auf Cloud Run `max-instances` auf 2 begrenzen, damit das Limit nicht beliebig umgehbar ist. Die echte Client-IP kommt aus dem ersten `X-Forwarded-For`-Eintrag, den Google setzt. Alternative: Redis, verworfen (zu viel Aufwand und Kosten für den Anlass).
- **Passwortvergleich:** `MessageDigest.isEqual` auf den Bytes (konstante Zeit).
- **Bucket-Zugriff:** AWS SDK v2 `S3Client` und `S3Presigner` gegen den R2-Endpunkt (S3-kompatibel). Die Objektliste wird beim Start geladen und kurz zwischengespeichert (z. B. 10 Minuten), damit nicht jede Anfrage den Bucket listet. Alternative: Index-Datei `index.json`, die das Skript erzeugt, noch einfacher und schneller; **Entscheidung: `index.json`** im Bucket, vom Skript geschrieben, vom Backend gelesen und zwischengespeichert.
- **Signierte Links:** 5 Minuten Gültigkeit, nur GET, `Content-Disposition: attachment` für Originale und ZIPs.
- **Skript:** Node-Skript `tools/prepare-gallery` mit `sharp` für Bilder und `archiver` für ZIPs, Upload per `rclone`. Alternative: Bash mit ImageMagick, verworfen (EXIF-Rotation, Wiederholbarkeit und Tests sind in Node einfacher).

## Risks / Trade-offs

- [In-Memory-Limit mit mehreren Instanzen] → `max-instances` klein halten, langes Passwort.
- [Passwort wird weitergegeben] → unvermeidlich beim gemeinsamen Passwort, nach dem Anlass rotieren oder Bucket löschen.
- [Viele Weiterleitungen pro Seitenaufruf] → `Cache-Control: private, max-age=60` auf den 302-Antworten, Cloud Run skaliert für den Anlass ausreichend.
- [R2-Zugangsdaten im Betrieb] → nur als Secret in Cloud Run, im Bucket nur Leserechte für das Backend.

## Migration Plan

Neue Endpunkte und Secrets auf Cloud Run einrichten, Frontend-Rewrite setzen, dann das alte Token-Login entfernen. Rückfall: vorherige Cloud-Run-Revision aktivieren.
