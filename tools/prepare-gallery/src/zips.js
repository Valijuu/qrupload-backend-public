import { createWriteStream } from 'node:fs';
import path from 'node:path';
import { pipeline } from 'node:stream/promises';
import yazl from 'yazl';
import { writeAtomically } from './atomic-write.js';
import { VARIANT_EXTENSION } from './variants.js';

const ZIP_EXTENSION = '.zip';
const ORIGINAL_ZIP_PREFIX = 'originale-';
export const MOBILE_ZIP_NAME = `handy${ZIP_EXTENSION}`;
export const ZIP_KIND = { original: 'original', mobile: 'mobile' };
export const DEFAULT_MAX_ZIP_BYTES = 1_000_000_000;

export function originalZipName(groupIndex) {
  return `${ORIGINAL_ZIP_PREFIX}${groupIndex + 1}${ZIP_EXTENSION}`;
}

/** Teilt die Dateien in ihrer Reihenfolge in Gruppen, die moeglichst unter maxBytes bleiben. */
export function planOriginalZips(files, maxBytes) {
  const groups = [];
  let current = [];
  let currentSize = 0;
  for (const file of files) {
    if (current.length > 0 && currentSize + file.size > maxBytes) {
      groups.push(current);
      current = [];
      currentSize = 0;
    }
    current.push(file);
    currentSize += file.size;
  }
  if (current.length > 0) {
    groups.push(current);
  }
  return groups;
}

/** Eindeutige Namen fuer die Handy-Fotos im ZIP: Originalname als .jpg, bei Doppelten mit Kennung. */
export function mobileEntryNames(photos) {
  const used = new Set();
  return photos.map((photo) => {
    const base = path.parse(photo.file).name;
    const name = used.has(`${base}${VARIANT_EXTENSION}`)
      ? `${base}-${photo.id}${VARIANT_EXTENSION}`
      : `${base}${VARIANT_EXTENSION}`;
    used.add(name);
    return name;
  });
}

/**
 * Packt Dateien ohne weitere Kompression (JPEGs sind schon komprimiert) in ein ZIP.
 * Eintraege sind Pfade oder { path, name }. Geschrieben wird atomar (siehe writeAtomically).
 */
export async function buildZip(entries, target) {
  const zip = new yazl.ZipFile();
  for (const entry of entries) {
    const { path: filePath, name } = typeof entry === 'string' ? { path: entry, name: path.basename(entry) } : entry;
    zip.addFile(filePath, name, { compress: false });
  }
  zip.end();
  await writeAtomically(target, (partial) => pipeline(zip.outputStream, createWriteStream(partial)));
}
