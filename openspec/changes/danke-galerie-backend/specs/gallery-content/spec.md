# Spec Delta

## Purpose

Liefert angemeldeten Gästen die Liste der Fotos und ZIP-Pakete und leitet für jedes Bild auf einen kurzlebigen signierten Link im privaten Bucket weiter.

## ADDED Requirements

### Requirement: Liste der Fotos und Pakete
Das System SHALL über `GET /api/gallery` für angemeldete Clients die Fotos (Kennung und Reihenfolge) und die ZIP-Pakete (Name, Größe, Art Original oder Handy-Qualität) aus dem Bucket liefern.

#### Scenario: Liste abrufen
- **WHEN** ein angemeldeter Client `GET /api/gallery` aufruft
- **THEN** erhält er HTTP 200 mit den Fotos in stabiler Reihenfolge
- **AND** den ZIP-Paketen mit Name, Größe und Art

#### Scenario: Ohne Anmeldung
- **WHEN** ein nicht angemeldeter Client `GET /api/gallery` aufruft
- **THEN** antwortet das System mit HTTP 401

### Requirement: Weiterleitung auf signierte Links
Das System SHALL für Vorschau, Handy-Qualität, Original und ZIP über `GET /api/photos/{id}/{variant}` bzw. `GET /api/zips/{name}` mit HTTP 302 auf einen signierten, nur lesenden Bucket-Link weiterleiten, der nach höchstens 5 Minuten abläuft und nur für dieses eine Objekt gilt. Originale und ZIPs SHALL als Download ausgeliefert werden (`Content-Disposition: attachment`).

#### Scenario: Vorschau abrufen
- **WHEN** ein angemeldeter Client die Vorschau eines bekannten Fotos anfordert
- **THEN** erhält er HTTP 302 auf einen signierten Link mit höchstens 5 Minuten Gültigkeit

#### Scenario: Original als Download
- **WHEN** ein angemeldeter Client das Original eines Fotos anfordert
- **THEN** enthält der signierte Link die Anweisung, die Datei als Anhang auszuliefern

#### Scenario: Unbekanntes Objekt
- **WHEN** ein Client eine Kennung oder einen Paketnamen anfordert, der nicht in der Bucket-Liste steht
- **THEN** antwortet das System mit HTTP 404

### Requirement: Schutz vor Pfad-Manipulation
Das System SHALL Objektschlüssel ausschließlich aus der Bucket-Liste bilden und Kennungen und Namen aus Anfragen nur gegen diese Liste abgleichen.

#### Scenario: Pfadangriff
- **WHEN** ein Client eine Kennung wie `../geheim` anfordert
- **THEN** antwortet das System mit HTTP 404 und signiert keinen Link

### Requirement: Privater Bucket und keine Geheimnisse in Antworten
Das System SHALL den Bucket nur mit serverseitigen Zugangsdaten ansprechen. Antworten und Fehler SHALL keine Zugangsdaten und internen Details enthalten.

#### Scenario: Bucket nicht erreichbar
- **WHEN** der Bucket nicht erreichbar ist
- **THEN** antwortet das System mit HTTP 502 oder 503 und einer allgemeinen Meldung ohne interne Details
