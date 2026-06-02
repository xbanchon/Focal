package com.xbanchon.imageservice.service;

import com.xbanchon.imageservice.entity.Image;
import com.xbanchon.imageservice.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {
    private final ImageRepository repository;

    @Transactional
    public Image startUpload(UUID userId, String filename, long bytes, String mimeType) {
        log.info("Initiating image upload for user: {}, filename: {}", userId, filename);

        Image image = Image.builder()
                .userId(userId)
                .originalFilename(filename)
                .fileSizeBytes(bytes)
                .mimeType(mimeType)
                .status(Image.ProcessingStatus.PENDING)
                .storageUrl("")
                .build();
        return  repository.save(image);
    }

    @Transactional
    public Image updateStatusToCompleted(UUID imageId, UUID userId, String finalStorageUrl) {
        log.info("Updating status to COMPLETED for image: {}", imageId);

        Image image = repository.findByIdAndUserId(imageId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Image not found or unauthorized access"));

        image.setStatus(Image.ProcessingStatus.COMPLETED);
        image.setStorageUrl(finalStorageUrl);

        return image;
    }

    @Transactional(readOnly = true)
    public List<Image> getUserImages(UUID userId){
        return repository.findAllByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public Image getImageDetails(UUID imageId, UUID userId){
        return repository.findByIdAndUserId(imageId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Image not found or unauthorized access"));
    }
}
