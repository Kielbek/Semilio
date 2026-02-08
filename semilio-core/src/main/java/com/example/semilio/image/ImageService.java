package com.example.semilio.image;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ImageService {

    Image createImage(MultipartFile file, String folderName);

    List<Image> createImages(List<MultipartFile> files, String folderName);

    List<Image> updateImages(List<Image> currentImages,
                             List<String> remainingUrls,
                             List<MultipartFile> newFiles,
                             String folderName);

    void deleteImages(List<Image> images);

}
