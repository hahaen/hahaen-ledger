package com.hahaen.ledger.file.service;

import io.minio.MinioClient;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Service
public class MinioStorageService {
    private final MinioClient client;
    private final String bucket;
    public MinioStorageService(@Value("${hahaen.minio.endpoint}") String endpoint, @Value("${hahaen.minio.access-key}") String accessKey, @Value("${hahaen.minio.secret-key}") String secretKey, @Value("${hahaen.minio.bucket}") String bucket) { this.client=MinioClient.builder().endpoint(endpoint).credentials(accessKey,secretKey).build(); this.bucket=bucket; }
    public String presignedUploadUrl(String objectKey) throws Exception { return client.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder().method(Method.PUT).bucket(bucket).object(objectKey).expiry(10, TimeUnit.MINUTES).build()); }
    public String presignedViewUrl(String objectKey) throws Exception { return client.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder().method(Method.GET).bucket(bucket).object(objectKey).expiry(10, TimeUnit.MINUTES).build()); }
    public String bucketName() { return bucket; }
    public boolean bucketExists() throws Exception { return client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build()); }
    public void ensureBucket() throws Exception { if (!bucketExists()) client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build()); }
}
