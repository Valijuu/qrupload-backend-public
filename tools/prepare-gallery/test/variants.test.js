import assert from 'node:assert/strict';
import path from 'node:path';
import { test } from 'node:test';
import { createMobile, createThumb } from '../src/variants.js';
import { dimensions, tempDir, writeJpeg } from './helpers.js';

const EXIF_ROTATED_90 = 6;

/** Legt ein Quellbild an, erzeugt die Variante damit und liefert deren Pixelgroesse. */
async function variantDimensions(createVariant, { width, height, orientation }) {
  const dir = await tempDir();
  const source = await writeJpeg(dir, 'quelle.jpg', width, height, orientation);
  const target = path.join(dir, 'variante.jpg');
  await createVariant(source, target);
  return dimensions(target);
}

test('Thumbnail hat eine lange Kante von 400 px', async () => {
  assert.deepEqual(await variantDimensions(createThumb, { width: 4000, height: 3000 }), { width: 400, height: 300 });
});

test('Handy-Version hat eine lange Kante von hoechstens 2560 px', async () => {
  assert.deepEqual(await variantDimensions(createMobile, { width: 4000, height: 3000 }), { width: 2560, height: 1920 });
});

test('kleine Bilder werden nicht vergroessert', async () => {
  assert.deepEqual(await variantDimensions(createMobile, { width: 800, height: 600 }), { width: 800, height: 600 });
});

test('EXIF-Drehung wird beruecksichtigt (Hochformat bleibt Hochformat)', async () => {
  const rotated = { width: 4000, height: 3000, orientation: EXIF_ROTATED_90 };
  assert.deepEqual(await variantDimensions(createThumb, rotated), { width: 300, height: 400 });
});
