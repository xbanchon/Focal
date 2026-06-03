package com.xbanchon.processingservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.file.Path;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3StorageService {

    private final S3Client s3Client;

    @Value("${supabase.bucket-name:images}")
    private String bucketName;

    public void downloadFile(String fileKey, Path destinationPath) {
        log.info("Downloading {} from S3...", fileKey);

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fileKey)
                .build();

        s3Client.getObject(getObjectRequest, destinationPath);
    }

    public String uploadFile(String destinationKey, Path sourceFile, String mimeType) {
        log.info("Uploading processed image to S3 as {}...", destinationKey);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(destinationKey)
                .contentType(mimeType)
                .build();

        s3Client.putObject(putObjectRequest, sourceFile);

        return bucketName + "/" + destinationKey;
    }
}
