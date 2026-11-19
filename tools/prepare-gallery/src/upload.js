import { spawn } from 'node:child_process';
import path from 'node:path';
import { IMAGE_EXTENSION_NAMES } from './photos.js';

const ORIGINALS_FOLDER = 'originals';
const INDEX_FILE = 'index.json';
const RCLONE_BINARY = 'rclone';
const RCLONE_COPY = 'copy';
const RCLONE_COPY_FILE = 'copyto';
const RCLONE_PROGRESS_FLAG = '--progress';
// Object-Tokens (nur Lesen/Schreiben im Bucket) duerfen keinen Bucket anlegen; ohne diese Option
// versucht rclone das vor jedem Upload und scheitert mit 403
const NO_BUCKET_CHECK_FLAG = '--s3-no-check-bucket';
// R2 lehnt die von rclone mitgeschickten Checksum-Header bei Uploads ab (NotImplemented, Versuch wiederholt sich nicht)
const NO_CHECKSUM_FLAG = '--s3-disable-checksum';
const CREDENTIAL_MARKERS = ['://', '@'];
const SUCCESS_EXIT_CODE = 0;
// Nur diese Ergebnisse gehoeren in den Bucket: keine Zwischenstaende (*.part), Hilfsdateien (*.manifest)
// oder fremde Dateien, die im Ausgabeordner liegen. index.json folgt als letzter Schritt.
const RESULT_PATTERNS = ['thumbs/*.jpg', 'mobile/*.jpg', 'zips/*.zip'];

/**
 * Die rclone-Aufrufe in dieser Reihenfolge:
 * 1. nur die Bilddateien aus dem Fotoordner (keine Unterordner, keine anderen Dateien) nach originals/,
 * 2. nur Vorschaubilder, Handy-Versionen und ZIPs aus dem Ausgabeordner,
 * 3. zuletzt index.json, damit das Backend nur Eintraege zeigt, deren Dateien schon oben sind.
 */
export function rcloneCommands({ inputDir, outDir, remote }) {
  if (CREDENTIAL_MARKERS.some((marker) => remote.includes(marker))) {
    throw new Error('Remote darf keine Zugangsdaten enthalten, sie gehoeren in die rclone-Konfiguration');
  }
  const commands = [
    [RCLONE_COPY, inputDir, `${remote}/${ORIGINALS_FOLDER}`,
      '--max-depth', '1', '--ignore-case', '--include', `*.{${IMAGE_EXTENSION_NAMES.join(',')}}`],
    [RCLONE_COPY, outDir, remote, ...RESULT_PATTERNS.flatMap((pattern) => ['--include', pattern])],
    [RCLONE_COPY_FILE, path.join(outDir, INDEX_FILE), `${remote}/${INDEX_FILE}`],
  ];
  return commands.map((command) => [...command, NO_BUCKET_CHECK_FLAG, NO_CHECKSUM_FLAG]);
}

export async function runRclone(commands) {
  for (const args of commands) {
    await new Promise((resolve, reject) => {
      const child = spawn(RCLONE_BINARY, [...args, RCLONE_PROGRESS_FLAG], { stdio: 'inherit' });
      child.on('error', reject);
      child.on('exit', (code) => (code === SUCCESS_EXIT_CODE
        ? resolve()
        : reject(new Error(`${RCLONE_BINARY} ${args[0]} endete mit Code ${code}`))));
    });
  }
}
