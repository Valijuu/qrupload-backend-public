import assert from 'node:assert/strict';
import { test } from 'node:test';
import { rcloneCommands } from '../src/upload.js';

test('rclone laedt Originale nach originals/ und den Rest in den Bucket-Stamm', () => {
  const commands = rcloneCommands({ inputDir: '/fotos', outDir: '/ergebnis', remote: 'r2:hochzeit' });

  assert.deepEqual(commands, [
    ['copy', '/fotos', 'r2:hochzeit/originals'],
    ['copy', '/ergebnis', 'r2:hochzeit'],
  ]);
});

test('der Remote-Name enthaelt keine Zugangsdaten', () => {
  assert.throws(() => rcloneCommands({ inputDir: '/f', outDir: '/o', remote: 'https://key:secret@host/bucket' }),
    /Remote/);
});
