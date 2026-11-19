---
title: "Login-Limit-Fix: Proxy-Kette per Log messen statt per gefälschtem Header"
author: Valiju
date: 2026-11-19
branch: main
id: l-d4988d4f
scope: repo
status: active
src: agent
touches:
  - src/main/java/com/qrupload/wedding/service/ClientIpResolver.java
---

## Decided
Die Proxy-Zahl für das Login-Limit wird im Betrieb mit einer kurzen Log-Zeile gemessen, dann wieder entfernt. Ergebnis: Firebase Hosting hängt zwei Einträge an, die Zahl ist 2.

## Tried and ruled out
Einen gefälschten X-Forwarded-For-Header über Firebase Hosting an das Backend zu schicken, um die Proxy-Zahl zu bestimmen. Hosting lässt den vom Client gesetzten Eintrag nicht durch, der Test sagt also nichts über die Kette aus. Die Messung braucht die echten Einträge aus dem Log.

## Watch out
In the code: ClientIpResolver.resolve, Umgebungsvariable TRUSTED_PROXY_HOPS (Cloud Run, Wert 2). Die temporäre Log-Zeile hat die Client-IP ins Cloud-Logging geschrieben; Logs bleiben etwa 30 Tage. Bei einer Änderung der Proxy-Kette (z. B. neuer Load Balancer) muss der Wert neu gemessen werden.
