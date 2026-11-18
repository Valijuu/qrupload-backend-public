import assert from 'node:assert/strict';
import { test } from 'node:test';
import { parseMaxZipMb } from '../src/options.js';

test('gueltige Paketgroesse in MB wird zu Bytes', () => {
  assert.equal(parseMaxZipMb('1000'), 1_000_000_000);
  assert.equal(parseMaxZipMb('250'), 250_000_000);
});

test('ungueltige Paketgroesse bricht mit verstaendlicher Meldung ab', () => {
  for (const value of ['abc', '0', '-5', '']) {
    assert.throws(() => parseMaxZipMb(value), /max-zip-mb/);
  }
});
