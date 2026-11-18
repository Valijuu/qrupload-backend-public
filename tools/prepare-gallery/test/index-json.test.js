import assert from 'node:assert/strict';
import { test } from 'node:test';
import { buildIndex } from '../src/index-json.js';

test('buildIndex liefert Fotos und Pakete im Format, das das Backend liest', () => {
  const index = buildIndex(
    [{ id: 'aabbccdd', file: 'IMG_1.jpg' }],
    [{ name: 'originale-1.zip', bytes: 123, kind: 'original' }, { name: 'handy.zip', bytes: 45, kind: 'mobile' }],
  );

  assert.deepEqual(index, {
    photos: [{ id: 'aabbccdd', file: 'IMG_1.jpg' }],
    zips: [
      { name: 'originale-1.zip', bytes: 123, kind: 'original' },
      { name: 'handy.zip', bytes: 45, kind: 'mobile' },
    ],
  });
});
