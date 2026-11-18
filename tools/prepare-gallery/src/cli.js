import { parseArgs } from 'node:util';
import { BYTES_PER_MB, parseMaxZipMb } from './options.js';
import { prepare } from './prepare.js';
import { rcloneCommands, runRclone } from './upload.js';
import { DEFAULT_MAX_ZIP_BYTES } from './zips.js';

const USAGE = `Aufruf: node src/cli.js <fotoordner> <ausgabeordner> [--remote r2:bucket] [--max-zip-mb 1000]

Erzeugt Thumbnails, Handy-Versionen, ZIP-Pakete und index.json im Ausgabeordner.
Mit --remote laedt rclone anschliessend alles in den Bucket (Zugangsdaten stehen in der rclone-Konfiguration).`;

const EXPECTED_POSITIONAL_COUNT = 2;
const USAGE_ERROR_EXIT_CODE = 1;
const MAX_ZIP_MB_OPTION = 'max-zip-mb';
const DEFAULT_MAX_ZIP_MB = String(DEFAULT_MAX_ZIP_BYTES / BYTES_PER_MB);

const { values, positionals } = parseArgs({
  allowPositionals: true,
  options: {
    remote: { type: 'string' },
    [MAX_ZIP_MB_OPTION]: { type: 'string', default: DEFAULT_MAX_ZIP_MB },
  },
});

if (positionals.length !== EXPECTED_POSITIONAL_COUNT) {
  console.error(USAGE);
  process.exit(USAGE_ERROR_EXIT_CODE);
}

const [inputDir, outDir] = positionals;
let maxZipBytes;
try {
  maxZipBytes = parseMaxZipMb(values[MAX_ZIP_MB_OPTION]);
} catch (error) {
  console.error(error.message);
  process.exit(USAGE_ERROR_EXIT_CODE);
}
const report = await prepare({ inputDir, outDir, maxZipBytes });
console.log(`Fertig: ${report.created} neu erzeugt, ${report.skipped} unveraendert uebersprungen.`);

if (values.remote) {
  await runRclone(rcloneCommands({ inputDir, outDir, remote: values.remote }));
} else {
  console.log('Kein --remote angegeben, es wurde nichts hochgeladen.');
}
