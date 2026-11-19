# gallery-auth Specification

## Purpose
Schützt die Galerie mit einem gemeinsamen Passwort und einer Cookie-Sitzung und begrenzt Rate-Versuche gegen das Durchprobieren.

## Requirements

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
Das System SHALL fehlgeschlagene Login-Versuche pro Client-IP auf 5 pro Minute begrenzen und danach mit HTTP 429 und `Retry-After` antworten. Erfolgreiche Logins SHALL nicht zählen, weil sich viele Gäste eine IP teilen können (Hotel-WLAN, Mobilfunk-NAT).

#### Scenario: Zu viele Fehlversuche
- **WHEN** eine IP mehr als 5 Login-Versuche innerhalb einer Minute macht
- **THEN** antwortet das System mit HTTP 429
- **AND** prüft das Passwort für diese Anfragen nicht mehr

#### Scenario: Parallele Anfragen
- **WHEN** von einer IP gleichzeitig viele Login-Versuche eintreffen
- **THEN** werden höchstens 5 davon auf das Passwort geprüft

#### Scenario: Viele Gäste hinter einer IP
- **WHEN** von einer IP innerhalb einer Minute mehr als 5 erfolgreiche Logins kommen
- **THEN** werden alle angenommen

#### Scenario: Begrenzung läuft ab
- **WHEN** nach einer Sperre die Minute abgelaufen ist
- **THEN** werden neue Versuche wieder normal geprüft

### Requirement: Altes QR-Token-Login entfällt
Das System SHALL `/api/auth/validate` und die Konfiguration `ACCESS_TOKEN` nicht mehr anbieten.

#### Scenario: Alter Endpunkt
- **WHEN** ein Client `/api/auth/validate` aufruft
- **THEN** antwortet das System mit HTTP 404 oder 401

### Requirement: Client-IP aus vertrauten Proxy-Einträgen
Das System SHALL die Client-IP für das Login-Limit aus dem `X-Forwarded-For`-Eintrag nehmen, der von rechts gezählt an der Stelle der konfigurierten Zahl vertrauter Proxys steht (Standard 1). Einträge links davon SHALL es ignorieren. Fehlt der Header oder hat er weniger Einträge, SHALL die Verbindungsadresse gelten.

#### Scenario: Gefälschte Einträge vorne im Header
- **GIVEN** ein vertrauter Proxy
- **WHEN** ein Client mehrere Login-Versuche sendet, jeweils mit einem anderen selbst gesetzten ersten Eintrag vor demselben letzten Eintrag
- **THEN** zählen alle Versuche für die IP aus dem letzten Eintrag
- **AND** nach 5 Fehlversuchen antwortet das System mit HTTP 429

#### Scenario: Zwei vertraute Proxys
- **GIVEN** zwei vertraute Proxys
- **WHEN** der Header `198.51.100.1, 203.0.113.5, 10.0.0.1` lautet
- **THEN** gilt `203.0.113.5` als Client-IP

#### Scenario: Header kürzer als die Proxy-Kette
- **GIVEN** zwei vertraute Proxys
- **WHEN** der Header nur einen Eintrag hat
- **THEN** gilt die Verbindungsadresse als Client-IP

#### Scenario: Kein Header
- **WHEN** eine Anfrage ohne `X-Forwarded-For` eintrifft
- **THEN** gilt die Verbindungsadresse als Client-IP

#### Scenario: Ungültige Proxy-Zahl
- **WHEN** die Zahl der vertrauten Proxys kleiner als 1 konfiguriert ist
- **THEN** startet die Anwendung nicht
