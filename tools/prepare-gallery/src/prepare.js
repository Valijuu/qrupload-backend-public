import { mkdir, stat, writeFile } from 'node:fs/promises';
import path from 'node:path';
import { writeAtomically } from './atomic-write.js';
import { buildZipIfChanged, createIfStale } from './incremental.js';
import { buildIndex } from './index-json.js';
import { listOriginals, photoId } from './photos.js';
import { createMobile, createThumb, VARIANT_EXTENSION } from './variants.js';
import {
  DEFAULT_MAX_ZIP_BYTES, MOBILE_ZIP_NAME, ZIP_KIND, mobileEntryNames, originalZipName, planOriginalZips,
} from './zips.js';

const FOLDERS = { thumbs: 'thumbs', mobile: 'mobile', zips: 'zips' };
const INDEX_FILE = 'index.json';
const INDEX_JSON_INDENT = 2;

function newReport() {
  const report = { created: 0, skipped: 0 };
  const record = (wasCreated) => { report[wasCreated ? 'created' : 'skipped'] += 1; };
  return { report, record };
}

function variantPath(outDir, folder, photo) {
  return path.join(outDir, folder, `${photo.id}${VARIANT_EXTENSION}`);
}

async function createVariants(photos, outDir, record) {
  for (const photo of photos) {
    record(await createIfStale(photo.source.path, variantPath(outDir, FOLDERS.thumbs, photo), createThumb));
    record(await createIfStale(photo.source.path, variantPath(outDir, FOLDERS.mobile, photo), createMobile));
  }
}

async function createOriginalZips(originals, zipDir, maxZipBytes, record) {
  const groups = planOriginalZips(originals, maxZipBytes);
  for (const [position, group] of groups.entries()) {
    const name = originalZipName(position);
    record(await buildZipIfChanged(group.map((file) => file.path), path.join(zipDir, name), group));
  }
  return groups.map((_, position) => ({ name: originalZipName(position), kind: ZIP_KIND.original }));
}

async function createMobileZip(photos, outDir, zipDir, record) {
  const names = mobileEntryNames(photos);
  const entries = photos.map((photo, position) => ({
    path: variantPath(outDir, FOLDERS.mobile, photo),
    name: names[position],
  }));
  const manifestEntries = photos.map((photo, position) => ({ name: names[position], size: photo.source.size }));
  record(await buildZipIfChanged(entries, path.join(zipDir, MOBILE_ZIP_NAME), manifestEntries));
  return { name: MOBILE_ZIP_NAME, kind: ZIP_KIND.mobile };
}

async function writeIndex(outDir, photos, zipDir, zips) {
  const sizedZips = await Promise.all(zips.map(async (zip) => ({
    ...zip,
    bytes: (await stat(path.join(zipDir, zip.name))).size,
  })));
  const content = JSON.stringify(buildIndex(photos, sizedZips), null, INDEX_JSON_INDENT);
  await writeAtomically(path.join(outDir, INDEX_FILE), (partial) => writeFile(partial, content));
}

export async function prepare({ inputDir, outDir, maxZipBytes = DEFAULT_MAX_ZIP_BYTES }) {
  const originals = await listOriginals(inputDir);
  const photos = originals.map((file) => ({ id: photoId(file.name), file: file.name, source: file }));
  const { report, record } = newReport();
  const zipDir = path.join(outDir, FOLDERS.zips);

  await Promise.all(Object.values(FOLDERS).map((folder) => mkdir(path.join(outDir, folder), { recursive: true })));
  await createVariants(photos, outDir, record);
  const originalZips = await createOriginalZips(originals, zipDir, maxZipBytes, record);
  const mobileZip = await createMobileZip(photos, outDir, zipDir, record);
  await writeIndex(outDir, photos, zipDir, [...originalZips, mobileZip]);

  return report;
}
