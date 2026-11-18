import { spawn } from 'node:child_process';
import path from 'node:path';
import { IMAGE_EXTENSION_NAMES } from './photos.js';

const ORIGINALS_FOLDER = 'originals';
const INDEX_FILE = 'index.json';
const RCLONE_BINARY = 'rclone';
const RCLONE_COPY = 'copy';
const RCLONE_COPY_FILE = 'copyto';
const RCLONE_PROGRESS_FLAG = '--progress';
const CREDENTIAL_MARKERS = ['://', '@'];
const SUCCESS_EXIT_CODE = 0;
// Zwischenstaende abgebrochener Laeufe und Hilfsdateien gehoeren nicht in den Bucket
const LOCAL_ONLY_PATTERNS = ['*.part', '*.manifest'];

/**
 * Die rclone-Aufrufe in dieser Reihenfolge:
 * 1. nur die Bilddateien aus dem Fotoordner (keine Unterordner, keine anderen Dateien) nach originals/,
 * 2. Vorschaubilder, Handy-Versionen und ZIPs,
 * 3. zuletzt index.json, damit das Backend nur Eintraege zeigt, deren Dateien schon oben sind.
 */
export function rcloneCommands({ inputDir, outDir, remote }) {
  if (CREDENTIAL_MARKERS.some((marker) => remote.includes(marker))) {
    throw new Error('Remote darf keine Zugangsdaten enthalten, sie gehoeren in die rclone-Konfiguration');
  }
  return [
    [RCLONE_COPY, inputDir, `${remote}/${ORIGINALS_FOLDER}`,
      '--max-depth', '1', '--ignore-case', '--include', `*.{${IMAGE_EXTENSION_NAMES.join(',')}}`],
    [RCLONE_COPY, outDir, remote,
      ...[INDEX_FILE, ...LOCAL_ONLY_PATTERNS].flatMap((pattern) => ['--exclude', pattern])],
    [RCLONE_COPY_FILE, path.join(outDir, INDEX_FILE), `${remote}/${INDEX_FILE}`],
  ];
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
