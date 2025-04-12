package dev.gfxv.blps.service;

import io.minio.*;
import io.minio.errors.MinioException;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.UUID;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StorageService {

    @NonFinal
    @Value("${minio.bucket.name}")
    String bucketName;

    MinioClient minioClient;

    @Autowired
    public StorageService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @PostConstruct
    public void init() {
        if (bucketName == null || bucketName.isEmpty()) {
            System.out.println("Bucket Name is not initialized!");
        }
    }

    public String uploadVideo(String fileName, InputStream inputStream, String contentType) throws Exception {
        try {
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
            String objectName = UUID.randomUUID() + "-" + fileName;
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, inputStream.available(), -1)
                            .contentType(contentType)
                            .build()
            );
            return objectName;
        } catch (Exception e) {
            throw new Exception("Failed to upload video to MinIO: " + e.getMessage(), e);
        }
    }

    public InputStream downloadVideo(String fileName) throws Exception {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );
        } catch (MinioException e) {
            throw new Exception("Error downloading video from MinIO: " + e.getMessage(), e);
        }
    }

    public void deleteVideo(String fileName) throws Exception {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );
        } catch (Exception e) {
            throw new Exception("Failed to delete video from MinIO: " + e.getMessage(), e);
        }
    }
}
