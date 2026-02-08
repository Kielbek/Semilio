package com.example.semilio.image;

import com.example.semilio.comon.validation.FileValidator;
import com.example.semilio.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.awt.image.BufferedImage;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private final S3Service s3Service;
    private final FileValidator fileValidator;

    @Override
    public Image createImage(MultipartFile file, String folderName) {
        fileValidator.validateImage(file);

        return processSingleFile(file, 0, folderName);
    }

    @Override
    public List<Image> createImages(List<MultipartFile> files, String folderName) {
        if (files == null || files.isEmpty()) {
            return new ArrayList<>();
        }

        List<Image> images = new ArrayList<>();
        int sortOrder = 0;

        for (MultipartFile file : files) {
            Image image = processSingleFile(file, sortOrder++, folderName);
            images.add(image);
        }

        return images;
    }

    @Override
    public List<Image> updateImages(List<Image> currentImages,
                                    List<String> remainingUrls,
                                    List<MultipartFile> newFiles,
                                    String folderName) {

        Map<String, Image> currentImagesMap = currentImages.stream()
                .collect(Collectors.toMap(Image::getUrl, Function.identity()));

        List<Image> updatedImages = new ArrayList<>();
        Set<String> keptUrlsSet = new HashSet<>();

        int currentSortOrder = 0;

        if (remainingUrls != null) {
            for (String url : remainingUrls) {
                Image existingImage = currentImagesMap.get(url);

                if (existingImage != null) {
                    existingImage.setSortOrder(currentSortOrder++);
                    updatedImages.add(existingImage);
                    keptUrlsSet.add(url);
                }
            }
        }

        for (Image image : currentImages) {
            if (!keptUrlsSet.contains(image.getUrl())) {
                s3Service.deleteFile(image.getUrl());
            }
        }

        if (newFiles != null && !newFiles.isEmpty()) {
            for (MultipartFile file : newFiles) {
                Image newImage = processSingleFile(file, currentSortOrder++, folderName);
                updatedImages.add(newImage);
            }
        }

        return updatedImages;
    }

    @Override
    public void deleteImages(List<Image> images) {
        if (images == null || images.isEmpty()) return;

        for (Image image : images) {
            s3Service.deleteFile(image.getUrl());
        }
    }

    private Image processSingleFile(MultipartFile file,
                                    int sortOrder,
                                    String folderName) {

        fileValidator.validateImage(file);

        int width = 0;
        int height = 0;
        try {
            BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
            if (bufferedImage != null) {
                width = bufferedImage.getWidth();
                height = bufferedImage.getHeight();
            }
        } catch (IOException e) {
            log.warn("Nie udało się odczytać wymiarów obrazka: {}", file.getOriginalFilename());
        }

        String url = s3Service.uploadImage(file, folderName);

        return Image.builder()
                .url(url)
                .sortOrder(sortOrder)
                .width(width > 0 ? width : null)
                .height(height > 0 ? height : null)
                .createdAt(LocalDateTime.now())
                .build();
    }
}