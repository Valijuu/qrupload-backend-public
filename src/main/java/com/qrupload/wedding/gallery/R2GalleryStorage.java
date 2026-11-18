package com.qrupload.wedding.gallery;

import org.springframework.http.ContentDisposition;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import org.springframework.http.ContentDisposition;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.net.URISyntaxException;
import java.time.Duration;

public class R2GalleryStorage implements GalleryStorage, AutoCloseable {

    private static final String INDEX_KEY = "index.json";
    // R2 kennt keine Regionen, das SDK verlangt trotzdem eine
    private static final String R2_REGION_NAME = "auto";
    private static final Region R2_REGION = Region.of(R2_REGION_NAME);
    private static final String INDEX_LOAD_FAILED_MESSAGE = "Galerie-Index konnte nicht geladen werden";
    private static final String SIGNING_FAILED_MESSAGE = "Link konnte nicht signiert werden";
    // Ein haengender Bucket soll Anfragen nicht minutenlang blockieren
    private static final Duration API_CALL_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration API_CALL_ATTEMPT_TIMEOUT = Duration.ofSeconds(4);

    private final R2Properties properties;
    private final GalleryIndexParser parser = new GalleryIndexParser();
    private final S3Client client;
    private final S3Presigner presigner;

    public R2GalleryStorage(R2Properties properties) {
        this.properties = properties;
        StaticCredentialsProvider credentials = StaticCredentialsProvider.create(
                AwsBasicCredentials.create(properties.accessKey(), properties.secretKey()));
        S3Configuration pathStyle = S3Configuration.builder().pathStyleAccessEnabled(true).build();
        URI endpoint = URI.create(properties.endpoint());
        this.client = S3Client.builder()
                .endpointOverride(endpoint).region(R2_REGION)
                .credentialsProvider(credentials).serviceConfiguration(pathStyle)
                .overrideConfiguration(timeouts -> timeouts
                        .apiCallTimeout(API_CALL_TIMEOUT)
                        .apiCallAttemptTimeout(API_CALL_ATTEMPT_TIMEOUT))
                .build();
        this.presigner = S3Presigner.builder()
                .endpointOverride(endpoint).region(R2_REGION)
                .credentialsProvider(credentials).serviceConfiguration(pathStyle).build();
    }

    @Override
    public GalleryIndex loadIndex() {
        try {
            String json = client.getObjectAsBytes(GetObjectRequest.builder()
                    .bucket(properties.bucket()).key(INDEX_KEY).build()).asUtf8String();
            return parser.parse(json);
        } catch (GalleryStorageException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new GalleryStorageException(INDEX_LOAD_FAILED_MESSAGE, e);
        }
    }

    @Override
    public URI presignView(String objectKey, Duration validity) {
        return presign(request(objectKey).build(), validity);
    }

    @Override
    public URI presignDownload(String objectKey, Duration validity, String downloadFileName) {
        GetObjectRequest request = request(objectKey)
                .responseContentDisposition(attachment(downloadFileName)).build();
        return presign(request, validity);
    }

    private GetObjectRequest.Builder request(String objectKey) {
        return GetObjectRequest.builder().bucket(properties.bucket()).key(objectKey);
    }

    private URI presign(GetObjectRequest request, Duration validity) {
        try {
            return presigner.presignGetObject(GetObjectPresignRequest.builder()
                    .signatureDuration(validity).getObjectRequest(request).build()).url().toURI();
        } catch (URISyntaxException | RuntimeException e) {
            throw new GalleryStorageException(SIGNING_FAILED_MESSAGE, e);
        }
    }

    // Maskiert Anfuehrungszeichen und setzt filename* fuer Umlaute (RFC 6266)
    private static String attachment(String fileName) {
        return ContentDisposition.attachment().filename(fileName, StandardCharsets.UTF_8).build().toString();
    }

    @Override
    public void close() {
        presigner.close();
        client.close();
    }
}
