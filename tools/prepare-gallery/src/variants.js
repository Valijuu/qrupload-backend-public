import sharp from 'sharp';

/** Endung aller erzeugten Bildvarianten (Thumbnails und Handy-Versionen). */
export const VARIANT_EXTENSION = '.jpg';
const FIT_WITHIN_BOX = 'inside';
const THUMB_LONG_EDGE = 400;
const THUMB_QUALITY = 80;
const MOBILE_LONG_EDGE = 2560;
const MOBILE_QUALITY = 85;

async function resizeTo(source, target, longEdge, quality) {
  await sharp(source)
    .rotate() // EXIF-Ausrichtung anwenden, danach werden die Metadaten verworfen
    .resize({ width: longEdge, height: longEdge, fit: FIT_WITHIN_BOX, withoutEnlargement: true })
    .jpeg({ quality, mozjpeg: true })
    .toFile(target);
}

export function createThumb(source, target) {
  return resizeTo(source, target, THUMB_LONG_EDGE, THUMB_QUALITY);
}

export function createMobile(source, target) {
  return resizeTo(source, target, MOBILE_LONG_EDGE, MOBILE_QUALITY);
}
