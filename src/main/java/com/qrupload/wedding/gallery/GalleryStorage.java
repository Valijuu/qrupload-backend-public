package com.qrupload.wedding.gallery;

import java.net.URI;
import java.time.Duration;

/** Zugriff auf den privaten Bucket: Index lesen und kurzlebige signierte Links erzeugen. */
public interface GalleryStorage {

    GalleryIndex loadIndex();

    /** Link zum Anzeigen eines Objekts. */
    URI presignView(String objectKey, Duration validity);

    /** Link, der das Objekt als Anhang mit dem angegebenen Dateinamen ausliefert. */
    URI presignDownload(String objectKey, Duration validity, String downloadFileName);
}
