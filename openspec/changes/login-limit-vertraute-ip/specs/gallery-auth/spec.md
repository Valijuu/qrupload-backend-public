# Spec Delta

## ADDED Requirements

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
