# Proposal

## Why

Das Login-Limit (5 Versuche pro Minute und IP) nimmt die Client-IP aus dem ersten `X-Forwarded-For`-Eintrag. Diesen Eintrag kann jeder Client selbst setzen, weil Firebase Hosting und Cloud Run mitgeschickte Einträge nicht entfernen. Mit einem neuen gefälschten Eintrag pro Anfrage lässt sich das gemeinsame Galerie-Passwort ohne Bremse durchprobieren (Issue #1). Das muss vor dem ersten Livegang behoben sein.

## What Changes

- Die Client-IP kommt aus dem `X-Forwarded-For`-Eintrag, den der äußerste vertraute Proxy angehängt hat, von rechts gezählt. Die Zahl der vertrauten Proxys ist einstellbar (Umgebungsvariable `TRUSTED_PROXY_HOPS`, Standard `1`). Einträge links davon werden ignoriert. **BREAKING** gegenüber der bisherigen Spec, die den ersten Eintrag vorschreibt.
- Hat der Header weniger Einträge als vertraute Proxys, gilt die Verbindungsadresse.
- Kein globales Limit: Es könnte alle Gäste aussperren. Schutz kommt aus der vertrauten IP plus einem langen Galerie-Passwort (README).
- README: Konfiguration und die Prüfung der Proxy-Kette vor dem Livegang beschreiben, den Hinweis auf Issue #1 entfernen.

## Capabilities

### New Capabilities

_keine_

### Modified Capabilities

- `gallery-auth`: Neu ist die Requirement, wie die Client-IP bestimmt wird (vertraute Proxys statt erster Eintrag).

## Impact

- Code: `ClientIpResolver`, `application.properties`.
- Tests: `ClientIpResolverTest`, `LoginRateLimitTest` (u. a. `clientIpKommtAusErstemXForwardedForEintrag` wird ersetzt).
- Betrieb: neue optionale Umgebungsvariable `TRUSTED_PROXY_HOPS` auf Cloud Run. Ihr Wert muss beim ersten Deployment an der echten Proxy-Kette geprüft werden.
- Frontend: keine Änderung. Auf HTTP 429 reagiert es schon.
