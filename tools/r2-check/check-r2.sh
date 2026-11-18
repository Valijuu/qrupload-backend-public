#!/usr/bin/env bash
# Prüft die rclone-Einrichtung für den R2-Bucket der Galerie.
# Aufruf: tools/r2-check/check-r2.sh [remote] [bucket]
# Standard: r2 und wedding. Schreibt nur in einen Testordner und räumt ihn wieder auf.
set -euo pipefail

REMOTE="${1:-r2}"
BUCKET="${2:-wedding}"
TEST_DIR="${REMOTE}:${BUCKET}/_rights-check"
TMP_FILE="$(mktemp)"
trap 'rm -f "$TMP_FILE"' EXIT

fail() { echo "FEHLER: $1" >&2; exit 1; }

command -v rclone >/dev/null || fail "rclone ist nicht installiert"

echo "1/3 Lesen: Bucket ${BUCKET} auf Remote ${REMOTE} auflisten"
rclone ls "${REMOTE}:${BUCKET}" >/dev/null 2>&1 \
  || fail "Lesen nicht möglich. Access Key, Endpoint oder Bucketname prüfen (rclone config show ${REMOTE})"
echo "    OK"

echo "2/3 Schreiben: Testdatei hochladen"
echo "rechte-check $(date -u +%FT%TZ)" > "$TMP_FILE"
rclone copy --s3-no-check-bucket "$TMP_FILE" "$TEST_DIR/" >/dev/null 2>&1 \
  || fail "Schreiben nicht möglich. Der Token braucht Object Read & Write auf diesen Bucket"
echo "    OK"

echo "3/3 Testdatei lesen und wieder löschen"
rclone ls "$TEST_DIR" | grep -q "$(basename "$TMP_FILE")" \
  || fail "Testdatei nach dem Hochladen nicht gefunden"
rclone delete "$TEST_DIR" >/dev/null 2>&1 || fail "Testdatei konnte nicht gelöscht werden"
echo "    OK"

echo
echo "Lesen und Schreiben funktionieren für ${REMOTE}:${BUCKET}."
