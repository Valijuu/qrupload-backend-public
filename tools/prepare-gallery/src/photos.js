import { createHash } from 'node:crypto';
import { readdir, stat } from 'node:fs/promises';
import path from 'node:path';

const IMAGE_EXTENSIONS = new Set(['.jpg', '.jpeg', '.png', '.webp', '.tif', '.tiff']);
const ID_LENGTH = 8;
const HASH_ALGORITHM = 'sha256';
const HASH_ENCODING = 'hex';

/** Bilddateien eines Ordners, nach Dateiname sortiert (das ist die Reihenfolge in der Galerie). */
export async function listOriginals(dir) {
  const names = (await readdir(dir)).filter((name) => IMAGE_EXTENSIONS.has(path.extname(name).toLowerCase()));
  names.sort();
  return Promise.all(names.map(async (name) => {
    const file = path.join(dir, name);
    return { name, path: file, size: (await stat(file)).size };
  }));
}

/** Stabile, kurze Kennung aus dem Dateinamen: bleibt gleich, auch wenn spaeter Fotos dazukommen. */
export function photoId(fileName) {
  return createHash(HASH_ALGORITHM).update(fileName).digest(HASH_ENCODING).slice(0, ID_LENGTH);
}
