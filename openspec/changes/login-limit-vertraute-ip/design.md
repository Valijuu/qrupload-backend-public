# Design

## Context

`ClientIpResolver` gibt heute den ersten `X-Forwarded-For`-Eintrag zurück, `LoginRateLimiter` zählt Versuche in einem festen Fenster von einer Minute pro IP (im Speicher, `ConcurrentHashMap.compute`). Erfolgreiche Logins geben ihren Versuch mit `release` frei. Das Backend läuft noch nicht. Wie viele Einträge Firebase Hosting und Cloud Run an den Header anhängen, ist deshalb nicht gemessen. Motivation siehe proposal.md.

## Goals / Non-Goals

**Goals:**
- Kein Header-Inhalt, den der Client selbst setzen kann, entscheidet über den Zähler.

**Non-Goals:**
- Kein verteilter Zähler (Redis o. Ä.). Cloud Run läuft mit wenigen Instanzen; jede hat ihr eigenes Limit, das reicht für den Anlass.
- Kein Auswerten von `Forwarded` (RFC 7239) oder von Anbieter-Headern wie `X-Appengine-User-Ip`.
- Keine Sperre über die Minute hinaus und keine Benachrichtigung.

## Decisions

**1. Vertraute Einträge von rechts zählen.** Jeder Proxy hängt die Adresse an, von der er die Anfrage bekam. Nur die rechten Einträge stammen also sicher von unseren Proxys. Bei `n` vertrauten Proxys ist der Eintrag an Position `länge − n` (von 0 gezählt) die Adresse, die der äußerste vertraute Proxy gesehen hat. Mehrere `X-Forwarded-For`-Header werden in ihrer Reihenfolge zu einer Liste verbunden; leere Einträge werden verworfen.
- Alternative: Weiter den ersten Eintrag nehmen und nur ein globales Limit ergänzen. Verworfen, weil das Limit pro IP dann wirkungslos bliebe.
- Alternative: `server.forward-headers-strategy` bzw. `RemoteIpValve` von Spring/Tomcat. Verworfen: Das Valve vertraut über Regex auf interne Adressbereiche, die Google-Proxys haben aber öffentliche Adressen. Eine feste Zahl ist leichter zu prüfen und zu testen.

**2. Standard `TRUSTED_PROXY_HOPS=1`, Fehlkonfiguration fällt sicher aus.** Ist die Zahl zu klein, landet die Proxy-Adresse von Firebase im Zähler: Alle Gäste teilen sich dann ein Limit, sicher, aber grob. Ist sie zu groß, landet ein vom Client gesetzter Eintrag im Zähler, und das ist unsicher. Deshalb ist der Standard der kleinste Wert. Der richtige Wert wird beim ersten Deployment gemessen (Aufgabe im Livegang). Werte unter 1 verhindern den Start (`@Validated`-Properties mit `@Min(1)`), damit ein Tippfehler nicht still den ersten Eintrag wieder vertraut macht.
- Hat der Header weniger als `n` Einträge, kam die Anfrage nicht über die erwartete Kette. Dann gilt `getRemoteAddr()` und nicht der linkeste Eintrag, denn der wäre vom Client gesetzt.

**3. Kein globales Limit.** Ein Limit über alle IPs würde Rateversuche zusätzlich bremsen, aber ein Angreifer könnte damit alle Gäste aussperren, solange er sendet. Verworfen. Stattdessen: vertraute IP (5 Versuche/Minute) und ein langes Passwort; bei 5 Versuchen pro Minute ist ein Passwort aus vier Wörtern praktisch nicht zu erraten.

**4. Konfiguration.** `gallery.trusted-proxy-hops=${TRUSTED_PROXY_HOPS:1}` in `application.properties`, gebunden an einen Properties-Record, den `ClientIpResolver` per Konstruktor bekommt.

## Risks / Trade-offs

- [Bis zur Messung (Standard 1) teilen sich alle Gäste eine IP; 5 Fehlversuche von irgendwem sperren alle für eine Minute] → Proxy-Kette direkt beim Deployment messen, bevor der Link an die Gäste geht.
- [Falscher Wert für `TRUSTED_PROXY_HOPS` im Betrieb] → Standard fällt sicher aus (Entscheidung 2). Die Livegang-Aufgabe misst den Header einmal.
- [Mehrere Cloud-Run-Instanzen vervielfachen das Limit] → Mit `max-instances` klein halten. Steht in der README.

## Migration Plan

1. Deployen mit dem Standard `TRUSTED_PROXY_HOPS=1`.
2. Einen Login über die echte Firebase-URL machen und im Log nachsehen, wie der `X-Forwarded-For`-Header aufgebaut ist (vorübergehende Debug-Ausgabe der Eintragszahl und der Adressen, kein Passwort).
3. Den Wert so setzen, dass die eigene öffentliche IP herauskommt. Debug-Ausgabe wieder entfernen.
Rückweg: Der Standardwert ist immer sicher. Ein Zurücksetzen der Variable reicht.

## Open Questions

- Wie viele Einträge hängen Firebase Hosting und Cloud Run tatsächlich an (erwartet: 2)? Das wird im Migrationsschritt 2 gemessen. Am Code und an den Specs ändert es nichts, nur am Wert der Variable.
