import { spawn } from 'node:child_process';

const ORIGINALS_FOLDER = 'originals';
const RCLONE_BINARY = 'rclone';
const RCLONE_COPY = 'copy';
const RCLONE_PROGRESS_FLAG = '--progress';
const CREDENTIAL_MARKERS = ['://', '@'];
const SUCCESS_EXIT_CODE = 0;

/** Die rclone-Aufrufe: Originale nach originals/, alles andere in den Bucket-Stamm. */
export function rcloneCommands({ inputDir, outDir, remote }) {
  if (CREDENTIAL_MARKERS.some((marker) => remote.includes(marker))) {
    throw new Error('Remote darf keine Zugangsdaten enthalten, sie gehoeren in die rclone-Konfiguration');
  }
  return [
    [RCLONE_COPY, inputDir, `${remote}/${ORIGINALS_FOLDER}`],
    [RCLONE_COPY, outDir, remote],
  ];
}

export async function runRclone(commands) {
  for (const args of commands) {
    await new Promise((resolve, reject) => {
      const child = spawn(RCLONE_BINARY, [...args, RCLONE_PROGRESS_FLAG], { stdio: 'inherit' });
      child.on('error', reject);
      child.on('exit', (code) => (code === SUCCESS_EXIT_CODE ? resolve() : reject(new Error(`${RCLONE_BINARY} ${args[0]} endete mit Code ${code}`))));
    });
  }
}
