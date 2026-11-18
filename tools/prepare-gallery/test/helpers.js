import { mkdtemp, writeFile } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import path from 'node:path';
import sharp from 'sharp';

export async function tempDir() {
  return mkdtemp(path.join(tmpdir(), 'prepare-gallery-'));
}

/** Schreibt ein JPEG mit der gegebenen Pixelgroesse, optional mit EXIF-Ausrichtung. */
export async function writeJpeg(dir, name, width, height, orientation) {
  const file = path.join(dir, name);
  let image = sharp({ create: { width, height, channels: 3, background: { r: 200, g: 120, b: 80 } } });
  image = image.jpeg();
  if (orientation) {
    image = image.withMetadata({ orientation });
  }
  await writeFile(file, await image.toBuffer());
  return file;
}

export async function dimensions(file) {
  const { width, height } = await sharp(file).metadata();
  return { width, height };
}
