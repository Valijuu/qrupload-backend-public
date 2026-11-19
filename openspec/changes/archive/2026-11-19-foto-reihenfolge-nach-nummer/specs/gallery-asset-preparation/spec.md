# Spec Delta

## ADDED Requirements

### Requirement: Fotoreihenfolge nach Endnummer
Das Skript SHALL die Fotos nach der Zahl am Ende des Dateinamens (vor der Dateiendung) aufsteigend ordnen. Bei gleicher Zahl SHALL der Dateiname natürlich sortiert entscheiden, Dateien ohne Endzahl SHALL am Ende nach Namen folgen.

#### Scenario: Verschiedene Namensanfänge
- **WHEN** der Ordner `Erste-Einblicke-A-100.jpg`, `A-109.jpg` und `Erste-Einblicke-A-120.jpg` enthält
- **THEN** lautet die Reihenfolge `Erste-Einblicke-A-100.jpg`, `A-109.jpg`, `Erste-Einblicke-A-120.jpg`

#### Scenario: Zahlen werden als Zahlen verglichen
- **WHEN** der Ordner `A-9.jpg` und `A-10.jpg` enthält
- **THEN** steht `A-9.jpg` vor `A-10.jpg`

#### Scenario: Datei ohne Endzahl
- **WHEN** der Ordner `A-1.jpg` und `Titel.jpg` enthält
- **THEN** steht `Titel.jpg` nach `A-1.jpg`
