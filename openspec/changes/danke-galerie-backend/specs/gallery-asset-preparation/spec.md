# Spec Delta

## Purpose

Bereitet die Originalfotos lokal für die Galerie vor: Vorschaubilder, Handy-Qualität und ZIP-Pakete, und lädt sie in den Bucket.

## ADDED Requirements

### Requirement: Vorschau und Handy-Qualität erzeugen
Das Skript SHALL aus jedem Original ein Vorschaubild (lange Kante ca. 400 px) und eine Handy-Version (lange Kante 2560 px, JPEG-Qualität ca. 85) erzeugen, mit korrekter Ausrichtung nach den EXIF-Daten und ohne Original zu verändern.

#### Scenario: Hochformat-Foto
- **WHEN** das Skript ein Foto im Hochformat mit EXIF-Rotation verarbeitet
- **THEN** sind Vorschau und Handy-Version richtig ausgerichtet
- **AND** die Handy-Version hat eine lange Kante von höchstens 2560 px

#### Scenario: Original bleibt unverändert
- **WHEN** das Skript durchgelaufen ist
- **THEN** sind die Originaldateien unverändert

### Requirement: ZIP-Pakete erzeugen
Das Skript SHALL die Originale in der Fotoreihenfolge in ZIP-Pakete von etwa 1 GB aufteilen und alle Handy-Versionen in ein ZIP packen.

#### Scenario: Aufteilung
- **WHEN** 1000 Originale mit zusammen 8 GB vorliegen
- **THEN** entstehen etwa 8 Original-Pakete und ein Handy-Paket
- **AND** jedes Foto liegt in genau einem Original-Paket

### Requirement: Upload in den Bucket und Wiederholbarkeit
Das Skript SHALL die Ergebnisse unter `originals/`, `thumbs/`, `mobile/` und `zips/` in den Bucket laden und bei erneutem Lauf bereits fertige Dateien nicht erneut erzeugen oder hochladen.

#### Scenario: Erneuter Lauf
- **WHEN** das Skript mit denselben Fotos erneut gestartet wird
- **THEN** werden keine Dateien doppelt erzeugt oder hochgeladen

#### Scenario: Nur Bilder werden hochgeladen
- **WHEN** im Fotoordner auch andere Dateien oder Unterordner liegen
- **THEN** lädt das Skript nur die Bilddateien des Ordners hoch, ohne Zwischen- und Hilfsdateien

#### Scenario: Nur der aktuelle Stand
- **WHEN** im Ausgabeordner Reste früherer Läufe oder fremde Dateien liegen
- **THEN** werden Reste entfernt und fremde Dateien nicht hochgeladen

#### Scenario: index.json zuletzt
- **WHEN** das Skript hochlädt
- **THEN** lädt es `index.json` erst, nachdem alle Bilder und Pakete oben sind

#### Scenario: Zugangsdaten
- **WHEN** das Skript Zugangsdaten braucht
- **THEN** liest es sie aus der Umgebung oder der rclone-Konfiguration und schreibt sie nicht in Dateien im Repo
