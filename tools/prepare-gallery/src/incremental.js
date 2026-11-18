import { createHash } from 'node:crypto';
import { readFile, stat, writeFile } from 'node:fs/promises';
import { writeAtomically } from './atomic-write.js';
import { buildZip } from './zips.js';

export const MANIFEST_SUFFIX = '.manifest';
const TEXT_ENCODING = 'utf8';
const HASH_ALGORITHM = 'sha256';
const HASH_ENCODING = 'hex';
const MANIFEST_FIELD_SEPARATOR = ':';
const MANIFEST_LINE_SEPARATOR = '\n';

async function modifiedAt(file) {
  try {
    return (await stat(file)).mtimeMs;
  } catch {
    return null;
  }
}

/** Erzeugt target nur, wenn es fehlt oder aelter als die Quelle ist. Liefert true, wenn erzeugt wurde. */
export async function createIfStale(source, target, create) {
  const targetTime = await modifiedAt(target);
  if (targetTime !== null && targetTime >= (await stat(source)).mtimeMs) {
    return false;
  }
  await writeAtomically(target, (partial) => create(source, partial));
  return true;
}

function manifestOf(entries) {
  const lines = entries.map((entry) => [entry.name, entry.size, entry.modified].join(MANIFEST_FIELD_SEPARATOR));
  return createHash(HASH_ALGORITHM).update(lines.join(MANIFEST_LINE_SEPARATOR)).digest(HASH_ENCODING);
}

/** Baut das ZIP nur neu, wenn es fehlt oder sich eine Datei (Name, Groesse, Aenderungszeit) geaendert hat. */
export async function buildZipIfChanged(entries, target, manifestEntries) {
  const manifestFile = `${target}${MANIFEST_SUFFIX}`;
  const manifest = manifestOf(manifestEntries);
  const previous = await readFile(manifestFile, TEXT_ENCODING).catch(() => null);
  if (previous === manifest && (await modifiedAt(target)) !== null) {
    return false;
  }
  await buildZip(entries, target);
  await writeFile(manifestFile, manifest);
  return true;
}
