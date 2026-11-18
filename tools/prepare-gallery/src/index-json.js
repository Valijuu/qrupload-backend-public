/** Das Format, das das Backend aus index.json liest (siehe GalleryIndexParser). */
export function buildIndex(photos, zips) {
  return {
    photos: photos.map(({ id, file }) => ({ id, file })),
    zips: zips.map(({ name, bytes, kind }) => ({ name, bytes, kind })),
  };
}
