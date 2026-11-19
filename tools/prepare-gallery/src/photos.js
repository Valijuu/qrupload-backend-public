import { createHash } from 'node:crypto';
import { readdir, stat } from 'node:fs/promises';
import path from 'node:path';

export const IMAGE_EXTENSION_NAMES = ['jpg', 'jpeg', 'png', 'webp', 'tif', 'tiff'];
const IMAGE_EXTENSIONS = new Set(IMAGE_EXTENSION_NAMES.map((name) => `.${name}`));
// Natuerliche Reihenfolge: IMG_2 vor IMG_10
const NATURAL_ORDER = new Intl.Collator('de', { numeric: true, sensitivity: 'variant' });
// Die Aufnahmereihenfolge steckt als Zahl am Ende des Namens (vor der Endung), z. B. Name-109.jpg
const TRAILING_NUMBER = /(\d+)\.[^.]+$/;
const ID_LENGTH = 8;
const HASH_ALGORITHM = 'sha256';
const HASH_ENCODING = 'hex';

function trailingNumber(name) {
  const match = TRAILING_NUMBER.exec(name);
  return match ? Number(match[1]) : Infinity;
}

// Erst nach Endzahl, bei gleicher Zahl (und ohne Zahl) natuerlich nach Namen
function compareByPosition(a, b) {
  return trailingNumber(a) - trailingNumber(b) || NATURAL_ORDER.compare(a, b);
}

/** Bilddateien eines Ordners in Galerie-Reihenfolge: nach der Zahl am Ende des Dateinamens. */
export async function listOriginals(dir) {
  const names = (await readdir(dir)).filter((name) => IMAGE_EXTENSIONS.has(path.extname(name).toLowerCase()));
  names.sort(compareByPosition);
  return Promise.all(names.map(async (name) => {
    const file = path.join(dir, name);
    const { size, mtimeMs } = await stat(file);
    return { name, path: file, size, modified: mtimeMs };
  }));
}

/** Stabile, kurze Kennung aus dem Dateinamen: bleibt gleich, auch wenn spaeter Fotos dazukommen. */
export function photoId(fileName) {
  return createHash(HASH_ALGORITHM).update(fileName).digest(HASH_ENCODING).slice(0, ID_LENGTH);
}

/** Zwei Fotos mit gleicher Kennung wuerden sich gegenseitig ueberschreiben: lieber abbrechen. */
export function assertUniqueIds(photos) {
  const seen = new Map();
  for (const photo of photos) {
    const earlier = seen.get(photo.id);
    if (earlier) {
      throw new Error(`Gleiche Kennung ${photo.id} fuer ${earlier} und ${photo.file}, bitte eine Datei umbenennen`);
    }
    seen.set(photo.id, photo.file);
  }
}
