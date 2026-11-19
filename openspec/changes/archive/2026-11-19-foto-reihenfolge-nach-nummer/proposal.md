# Proposal

## Why

Die Fotos der Hochzeit tragen ihre Reihenfolge als Nummer am Dateinamenende (`…-109.jpg`), aber mit verschiedenen Namensanfängen (`Erste-Einblicke-…`, `Sabine&Valeri-…`). Das Skript sortiert nach dem ganzen Namen, die Galerie würde die Fotos deshalb gruppiert nach Namensanfang zeigen statt in der Aufnahmereihenfolge.

## What Changes

- Das Skript sortiert Fotos nach der Zahl am Ende des Dateinamens (vor der Endung). Bei gleicher Zahl entscheidet der Dateiname natürlich sortiert. Dateien ohne Endzahl folgen am Ende nach Namen.

## Capabilities

### New Capabilities

_keine_

### Modified Capabilities

- `gallery-asset-preparation`: neue Requirement zur Reihenfolge der Fotos.

## Impact

- Code: `tools/prepare-gallery/src/photos.js` (`listOriginals`), Test `photos.test.js`.
- Die Reihenfolge bestimmt `index.json` und die ZIP-Pakete. Schon hochgeladene Dateien bleiben gültig, die Kennungen hängen am Dateinamen.
