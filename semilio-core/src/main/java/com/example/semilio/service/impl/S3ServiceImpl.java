package com.example.semilio.service.impl;

import com.example.semilio.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {

    private final S3Client s3Client;

    @Value("${aws.bucket.name}")
    private String bucketName;

    @Override
    public String uploadImage(MultipartFile file, String folder) {
        if (file.isEmpty()) {
            throw new RuntimeException("File cannot be empty");
        }

        try {
            String key = folder + "/" + UUID.randomUUID() + "-" + file.getOriginalFilename();

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

            return "https://" + bucketName + ".s3.amazonaws.com/" + key;

        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file " + file.getOriginalFilename(), e);
        }
    }

    // --- REFAKTORYZACJA LISTY (OPCJONALNIE) ---
    // Teraz uploadImages może korzystać z metody powyżej, żeby nie dublować kodu:
    @Override
    public List<String> uploadImages(List<MultipartFile> files, String folder) {
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            urls.add(uploadImage(file, folder));
        }
        return urls;
    }

    @Override
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        String key = extractKeyFromUrl(fileUrl);

        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        s3Client.deleteObject(deleteObjectRequest);
    }

    private String extractKeyFromUrl(String url) {
        String prefix = "https://" + bucketName + ".s3.amazonaws.com/";

        if (url.startsWith(prefix)) {
            String key = url.substring(prefix.length());
            return URLDecoder.decode(key, StandardCharsets.UTF_8);
        }

        return url.substring(url.lastIndexOf("/") + 1);
    }
}