# Spec Delta

## Purpose

Schützt die Galerie mit einem gemeinsamen Passwort und einer Cookie-Sitzung und begrenzt Rate-Versuche gegen das Durchprobieren.

## ADDED Requirements

### Requirement: Login mit gemeinsamem Passwort
Das System SHALL über `POST /api/login` ein Passwort entgegennehmen und es in konstanter Zeit mit dem konfigurierten Passwort vergleichen. Das Passwort SHALL aus einer Umgebungsvariable kommen, und das System SHALL ohne sie nicht starten.

#### Scenario: Richtiges Passwort
- **WHEN** ein Client das richtige Passwort sendet
- **THEN** antwortet das System mit HTTP 204
- **AND** setzt das Cookie `__session`

#### Scenario: Falsches Passwort
- **WHEN** ein Client ein falsches Passwort sendet
- **THEN** antwortet das System mit HTTP 401 ohne Cookie und ohne Hinweis auf das richtige Passwort

#### Scenario: Passwort nicht konfiguriert
- **WHEN** die Umgebungsvariable für das Passwort fehlt oder leer ist
- **THEN** startet die Anwendung nicht

### Requirement: Sitzungs-Cookie
Das Cookie `__session` SHALL ein signiertes JWT mit begrenzter Laufzeit enthalten und mit `HttpOnly`, `Secure`, `SameSite=Lax` und `Path=/` gesetzt werden. Das System SHALL nur dieses Cookie zur Authentifizierung auswerten.

#### Scenario: Cookie-Eigenschaften
- **WHEN** der Login erfolgreich ist
- **THEN** hat das Cookie `__session` die Attribute `HttpOnly`, `Secure`, `SameSite=Lax` und eine Laufzeit von wenigen Stunden

#### Scenario: Abgelaufene oder manipulierte Sitzung
- **WHEN** ein Client ein abgelaufenes, manipuliertes oder fehlendes Cookie sendet
- **THEN** antwortet das System auf geschützte Endpunkte mit HTTP 401

### Requirement: Begrenzung der Login-Versuche
Das System SHALL Login-Versuche pro Client-IP auf 5 pro Minute begrenzen und darüber hinaus mit HTTP 429 und `Retry-After` antworten.

#### Scenario: Zu viele Fehlversuche
- **WHEN** eine IP mehr als 5 Login-Versuche innerhalb einer Minute macht
- **THEN** antwortet das System mit HTTP 429
- **AND** prüft das Passwort für diese Anfragen nicht mehr

#### Scenario: Begrenzung läuft ab
- **WHEN** nach einer Sperre die Minute abgelaufen ist
- **THEN** werden neue Versuche wieder normal geprüft

### Requirement: Altes QR-Token-Login entfällt
Das System SHALL `/api/auth/validate` und die Konfiguration `ACCESS_TOKEN` nicht mehr anbieten.

#### Scenario: Alter Endpunkt
- **WHEN** ein Client `/api/auth/validate` aufruft
- **THEN** antwortet das System mit HTTP 404 oder 401
