# Tasks

## 1. Client-IP aus vertrauten Einträgen

- [x] 1.1 Properties-Record für `gallery.trusted-proxy-hops` (Standard 1, `@Min(1)`, `@Validated`) und Eintrag `${TRUSTED_PROXY_HOPS:1}` in `application.properties` anlegen. Verifikation: Test, dass die Anwendung mit `0` nicht startet und ohne Variable mit 1 läuft
- [x] 1.2 `ClientIpResolver` auf das Zählen von rechts umstellen (mehrere Header verbinden, leere Einträge verwerfen, zu kurzer oder fehlender Header → Verbindungsadresse). Verifikation: `ClientIpResolverTest` deckt die Szenarien „Zwei vertraute Proxys“, „Header kürzer als die Proxy-Kette“, „Kein Header“ und den Fall mit einem Proxy ab
- [x] 1.3 `clientIpKommtAusErstemXForwardedForEintrag` in `LoginRateLimitTest` durch das Szenario „Gefälschte Einträge vorne im Header“ ersetzen. Verifikation: Test grün über MockMvc

## 2. Doku

- [x] 2.1 README: `TRUSTED_PROXY_HOPS`, die Empfehlung für ein langes Passwort, `max-instances` und die Prüfung der Proxy-Kette vor dem Livegang beschreiben; Hinweis auf Issue #1 entfernen. `CHANGELOG.md` unter Unreleased/Fixed ergänzen. Verifikation: README nennt Variable, Standard und Prüfschritte

## Workflow follow-up

- Livegang: Proxy-Kette nach dem ersten Deployment messen und `TRUSTED_PROXY_HOPS` setzen (siehe design.md, Migration Plan). Diese Aufgabe wird vor dem Archivieren als GitHub-Issue angelegt, weil sie erst im Betrieb möglich ist.
- Review (Opus, danach CodeRabbit gegen `main`), `openspec validate --all --strict`, `/opsx:archive`, Rebase auf `main`, Branch löschen, Issue #1 schließen.
