import assert from 'node:assert/strict';
import { createHash } from 'node:crypto';
import { readFile, readdir, stat } from 'node:fs/promises';
import path from 'node:path';
import { test } from 'node:test';
import { prepare } from '../src/prepare.js';
import { dimensions, tempDir, writeJpeg } from './helpers.js';

const SMALL_ZIP_LIMIT = 40_000;
const EXIF_ROTATED_90 = 6;

async function setup() {
  const inputDir = await tempDir();
  const outDir = await tempDir();
  await writeJpeg(inputDir, 'a.jpg', 3000, 2000);
  await writeJpeg(inputDir, 'b.jpg', 2000, 3000, EXIF_ROTATED_90);
  await writeJpeg(inputDir, 'c.jpg', 3000, 2000);
  return { inputDir, outDir };
}

async function hashOf(file) {
  return createHash('sha256').update(await readFile(file)).digest('hex');
}

test('prepare erzeugt Thumbnails, Handy-Versionen, ZIPs und index.json', async () => {
  const { inputDir, outDir } = await setup();

  await prepare({ inputDir, outDir, maxZipBytes: SMALL_ZIP_LIMIT });

  const index = JSON.parse(await readFile(path.join(outDir, 'index.json'), 'utf8'));
  assert.deepEqual(index.photos.map((photo) => photo.file), ['a.jpg', 'b.jpg', 'c.jpg']);
  assert.ok(index.zips.filter((zip) => zip.kind === 'original').length >= 1);
  assert.equal(index.zips.filter((zip) => zip.kind === 'mobile').length, 1);
  assert.equal(index.zips.find((zip) => zip.kind === 'mobile').name, 'handy.zip');
  const first = index.photos[0].id;
  assert.deepEqual(await dimensions(path.join(outDir, 'thumbs', `${first}.jpg`)), { width: 400, height: 267 });
  assert.equal((await dimensions(path.join(outDir, 'mobile', `${first}.jpg`))).width, 2560);
  for (const zip of index.zips) {
    assert.equal((await stat(path.join(outDir, 'zips', zip.name))).size, zip.bytes);
  }
});

test('prepare veraendert die Originale nicht', async () => {
  const { inputDir, outDir } = await setup();
  const before = await hashOf(path.join(inputDir, 'a.jpg'));

  await prepare({ inputDir, outDir, maxZipBytes: SMALL_ZIP_LIMIT });

  assert.equal(await hashOf(path.join(inputDir, 'a.jpg')), before);
  assert.deepEqual((await readdir(inputDir)).sort(), ['a.jpg', 'b.jpg', 'c.jpg']);
});

test('ein zweiter Lauf erzeugt nichts neu', async () => {
  const { inputDir, outDir } = await setup();
  const first = await prepare({ inputDir, outDir, maxZipBytes: SMALL_ZIP_LIMIT });

  const second = await prepare({ inputDir, outDir, maxZipBytes: SMALL_ZIP_LIMIT });

  assert.ok(first.created > 0);
  assert.equal(second.created, 0);
  assert.equal(second.skipped, first.created);
});
