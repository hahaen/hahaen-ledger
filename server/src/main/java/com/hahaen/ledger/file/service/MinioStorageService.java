package com.hahaen.ledger.file.service;

import io.minio.MinioClient;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.GetObjectArgs;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
public class MinioStorageService {
    private final MinioClient client;
    private final URI internalEndpoint;
    private final String publicUrlPrefix;
    private final String bucket;

    public MinioStorageService(
            Environment environment,
            @Value("${hahaen.minio.endpoint}") String endpoint,
            @Value("${hahaen.minio.public-url-prefix:}") String publicUrlPrefix,
            @Value("${hahaen.minio.access-key}") String accessKey,
            @Value("${hahaen.minio.secret-key}") String secretKey,
            @Value("${hahaen.minio.bucket}") String bucket) {
        String normalizedEndpoint = stripTrailingSlashes(endpoint);
        boolean prodProfile = Arrays.asList(environment.getActiveProfiles()).contains("prod");
        if (prodProfile && (publicUrlPrefix == null || publicUrlPrefix.isBlank())) {
            throw new IllegalStateException("MINIO_PUBLIC_URL_PREFIX must be configured for the prod profile");
        }
        this.internalEndpoint = URI.create(normalizedEndpoint);
        this.publicUrlPrefix = publicUrlPrefix == null || publicUrlPrefix.isBlank()
                ? normalizedEndpoint
                : stripTrailingSlashes(publicUrlPrefix);
        validatePublicUrlPrefix(this.publicUrlPrefix);
        this.client = MinioClient.builder().endpoint(normalizedEndpoint).credentials(accessKey, secretKey).build();
        this.bucket = bucket;
    }

    public String presignedUploadUrl(String objectKey) throws Exception {
        ensureBucket();
        return toPublicUrl(client.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                .method(Method.PUT).bucket(bucket).object(objectKey).expiry(10, TimeUnit.MINUTES).build()));
    }

    public String presignedViewUrl(String objectKey) throws Exception {
        return toPublicUrl(client.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                .method(Method.GET).bucket(bucket).object(objectKey).expiry(10, TimeUnit.MINUTES).build()));
    }

    public StatObjectResponse statObject(String objectKey) throws Exception { return client.statObject(StatObjectArgs.builder().bucket(bucket).object(objectKey).build()); }
    /** The caller must close the returned stream. */
    public InputStream getObject(String objectKey) throws Exception { return client.getObject(GetObjectArgs.builder().bucket(bucket).object(objectKey).build()); }
    public void removeObject(String objectKey) throws Exception { client.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(objectKey).build()); }
    public String bucketName() { return bucket; }
    public boolean bucketExists() throws Exception { return client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build()); }
    public void ensureBucket() throws Exception { if (!bucketExists()) client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build()); }

    private String toPublicUrl(String signedUrl) {
        if (publicUrlPrefix.equals(internalEndpoint.toString())) {
            return signedUrl;
        }

        URI signedUri = URI.create(signedUrl);
        if (!sameOrigin(internalEndpoint, signedUri)) {
            throw new IllegalStateException("MinIO returned a signed URL with an unexpected origin");
        }

        return publicUrlPrefix + signedUri.getRawPath()
                + (signedUri.getRawQuery() == null ? "" : "?" + signedUri.getRawQuery());
    }

    private static boolean sameOrigin(URI expected, URI actual) {
        return Objects.equals(lower(expected.getScheme()), lower(actual.getScheme()))
                && Objects.equals(lower(expected.getHost()), lower(actual.getHost()))
                && effectivePort(expected) == effectivePort(actual);
    }

    private static int effectivePort(URI uri) {
        if (uri.getPort() >= 0) return uri.getPort();
        return "https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80;
    }

    private static String lower(String value) {
        return value == null ? null : value.toLowerCase(java.util.Locale.ROOT);
    }

    private static String stripTrailingSlashes(String value) {
        int end = value.length();
        while (end > 0 && value.charAt(end - 1) == '/') end--;
        return value.substring(0, end);
    }

    private static void validatePublicUrlPrefix(String value) {
        URI uri = URI.create(value);
        boolean supportedScheme = "http".equalsIgnoreCase(uri.getScheme())
                || "https".equalsIgnoreCase(uri.getScheme());
        if (!supportedScheme || uri.getHost() == null || uri.getRawQuery() != null || uri.getRawFragment() != null) {
            throw new IllegalArgumentException(
                    "MINIO_PUBLIC_URL_PREFIX must be an absolute HTTP(S) URL without query or fragment");
        }
    }
}
