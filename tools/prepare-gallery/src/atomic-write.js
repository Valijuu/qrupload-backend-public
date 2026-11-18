import { rename } from 'node:fs/promises';

const PARTIAL_SUFFIX = '.part';

/** Schreibt erst in eine .part-Datei und benennt sie danach um: ein abgebrochener Lauf hinterlaesst nichts Halbfertiges. */
export async function writeAtomically(target, write) {
  const partial = `${target}${PARTIAL_SUFFIX}`;
  await write(partial);
  await rename(partial, target);
}
