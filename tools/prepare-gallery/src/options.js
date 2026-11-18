export const BYTES_PER_MB = 1_000_000;

/** --max-zip-mb pruefen: eine positive Zahl, sonst landen alle Originale in einem Paket. */
export function parseMaxZipMb(value) {
  const megabytes = Number(value);
  if (value === '' || !Number.isFinite(megabytes) || megabytes <= 0) {
    throw new Error(`--max-zip-mb muss eine positive Zahl sein, war: "${value}"`);
  }
  return megabytes * BYTES_PER_MB;
}
