package com.example.semilio.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface S3Service {
    List<String> uploadImages(List<MultipartFile> files, String folder);
    String uploadImage(MultipartFile file, String folder);
    void deleteFile(String fileUrl);
}
