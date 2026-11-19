import assert from 'node:assert/strict';
import { writeFile } from 'node:fs/promises';
import path from 'node:path';
import { test } from 'node:test';
import { assertUniqueIds, listOriginals, photoId } from '../src/photos.js';
import { tempDir } from './helpers.js';

test('listOriginals liefert nur Bilddateien, nach Namen sortiert', async () => {
  const dir = await tempDir();
  for (const name of ['b.jpg', 'a.JPG', 'c.png', 'Thumbs.db', 'notizen.txt']) {
    await writeFile(path.join(dir, name), 'x');
  }

  const files = await listOriginals(dir);

  assert.deepEqual(files.map((file) => file.name), ['a.JPG', 'b.jpg', 'c.png']);
  assert.equal(files[0].size, 1);
});

test('photoId ist stabil, kurz und je Dateiname verschieden', () => {
  assert.equal(photoId('IMG_0001.jpg'), photoId('IMG_0001.jpg'));
  assert.notEqual(photoId('IMG_0001.jpg'), photoId('IMG_0002.jpg'));
  assert.match(photoId('IMG_0001.jpg'), /^[0-9a-f]{8}$/);
});

test('listOriginals sortiert Zahlen natuerlich (IMG_2 vor IMG_10)', async () => {
  const dir = await tempDir();
  for (const name of ['IMG_10.jpg', 'IMG_2.jpg', 'IMG_1.jpg']) {
    await writeFile(path.join(dir, name), 'x');
  }

  assert.deepEqual((await listOriginals(dir)).map((file) => file.name), ['IMG_1.jpg', 'IMG_2.jpg', 'IMG_10.jpg']);
});

test('doppelte Kennungen brechen mit beiden Dateinamen ab', () => {
  assert.throws(() => assertUniqueIds([{ id: 'aa', file: 'a.jpg' }, { id: 'aa', file: 'b.jpg' }]), /a\.jpg.*b\.jpg/);
  assert.doesNotThrow(() => assertUniqueIds([{ id: 'aa', file: 'a.jpg' }, { id: 'bb', file: 'b.jpg' }]));
});

test('listOriginals ordnet nach der Zahl am Ende, auch bei verschiedenen Namensanfaengen', async () => {
  const dir = await tempDir();
  for (const name of ['Erste-Einblicke-A-120.jpg', 'A-109.jpg', 'Erste-Einblicke-A-100.jpg', 'A-9.jpg', 'A-10.jpg']) {
    await writeFile(path.join(dir, name), 'x');
  }

  assert.deepEqual((await listOriginals(dir)).map((file) => file.name), [
    'A-9.jpg', 'A-10.jpg', 'Erste-Einblicke-A-100.jpg', 'A-109.jpg', 'Erste-Einblicke-A-120.jpg',
  ]);
});

test('listOriginals stellt Dateien ohne Endzahl ans Ende', async () => {
  const dir = await tempDir();
  for (const name of ['Titel.jpg', 'A-1.jpg', 'Anfang.jpg']) {
    await writeFile(path.join(dir, name), 'x');
  }

  assert.deepEqual((await listOriginals(dir)).map((file) => file.name), ['A-1.jpg', 'Anfang.jpg', 'Titel.jpg']);
});
