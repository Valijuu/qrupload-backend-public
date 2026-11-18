import assert from 'node:assert/strict';
import { test } from 'node:test';
import { rcloneCommands } from '../src/upload.js';

const commands = rcloneCommands({ inputDir: '/fotos', outDir: '/ergebnis', remote: 'r2:hochzeit' });

test('Originale: nur Bilddateien aus dem Fotoordner selbst, keine Unterordner', () => {
  const [originals] = commands;
  assert.deepEqual(originals.slice(0, 3), ['copy', '/fotos', 'r2:hochzeit/originals']);
  assert.ok(originals.includes('--max-depth') && originals[originals.indexOf('--max-depth') + 1] === '1');
  assert.ok(originals.includes('--ignore-case'));
  const include = originals[originals.indexOf('--include') + 1];
  assert.equal(include, '*.{jpg,jpeg,png,webp,tif,tiff}');
});

test('Ergebnisse: nur Vorschaubilder, Handy-Versionen und ZIPs, keine fremden Dateien', () => {
  const [, results] = commands;
  assert.deepEqual(results.slice(0, 3), ['copy', '/ergebnis', 'r2:hochzeit']);
  const included = results.filter((_, i) => results[i - 1] === '--include');
  assert.deepEqual(included.sort(), ['mobile/*.jpg', 'thumbs/*.jpg', 'zips/*.zip'].sort());
  assert.ok(!results.includes('--exclude'), 'nur positive Liste, kein Ausschluss-Muster das etwas durchlaesst');
});

test('index.json kommt als letzter Schritt, wenn alle Bilder und Pakete oben sind', () => {
  assert.equal(commands.length, 3);
  assert.deepEqual(commands[2], ['copyto', '/ergebnis/index.json', 'r2:hochzeit/index.json']);
});

test('der Remote-Name enthaelt keine Zugangsdaten', () => {
  assert.throws(() => rcloneCommands({ inputDir: '/f', outDir: '/o', remote: 'https://key:secret@host/bucket' }),
    /Remote/);
});
