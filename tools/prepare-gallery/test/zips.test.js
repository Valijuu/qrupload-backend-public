import assert from 'node:assert/strict';
import { writeFile } from 'node:fs/promises';
import path from 'node:path';
import { test } from 'node:test';
import yauzl from 'yauzl';
import { buildZip, mobileEntryNames, originalZipName, planOriginalZips } from '../src/zips.js';
import { tempDir } from './helpers.js';

const GB = 1_000_000_000;

function zipEntryNames(zipFile) {
  return new Promise((resolve, reject) => {
    yauzl.open(zipFile, { lazyEntries: true }, (error, zip) => {
      if (error) return reject(error);
      const names = [];
      zip.on('entry', (entry) => { names.push(entry.fileName); zip.readEntry(); });
      zip.on('end', () => resolve(names));
      zip.readEntry();
    });
  });
}

test('planOriginalZips teilt in Reihenfolge in Pakete bis zur Maximalgroesse', () => {
  const files = Array.from({ length: 1000 }, (_, i) => ({ name: `f${i}.jpg`, size: 8_000_000 }));

  const groups = planOriginalZips(files, GB);

  assert.equal(groups.length, 8);
  assert.ok(groups.every((group) => group.reduce((sum, file) => sum + file.size, 0) <= GB));
  assert.deepEqual(groups.flat().map((file) => file.name), files.map((file) => file.name));
});

test('ein einzelnes Foto ueber der Grenze bekommt ein eigenes Paket', () => {
  const groups = planOriginalZips([{ name: 'a', size: 10 }, { name: 'riesig', size: 500 }, { name: 'b', size: 10 }], 100);

  assert.deepEqual(groups.map((group) => group.map((file) => file.name)), [['a'], ['riesig'], ['b']]);
});

test('Paketnamen zaehlen ab 1', () => {
  assert.equal(originalZipName(0), 'originale-1.zip');
  assert.equal(originalZipName(7), 'originale-8.zip');
});

test('buildZip packt die Dateien unter ihrem Namen', async () => {
  const dir = await tempDir();
  const first = path.join(dir, 'a.jpg');
  const second = path.join(dir, 'b.jpg');
  await writeFile(first, 'aaa');
  await writeFile(second, 'bbb');
  const target = path.join(dir, 'paket.zip');

  await buildZip([first, second], target);

  assert.deepEqual(await zipEntryNames(target), ['a.jpg', 'b.jpg']);
});

test('Handy-Fotos im ZIP heissen wie das Original, bei Doppelten mit Kennung', () => {
  const names = mobileEntryNames([
    { id: '11111111', file: 'hochzeit.jpg' },
    { id: '22222222', file: 'hochzeit.png' },
    { id: '33333333', file: 'feier.jpeg' },
  ]);

  assert.deepEqual(names, ['hochzeit.jpg', 'hochzeit-22222222.jpg', 'feier.jpg']);
});
