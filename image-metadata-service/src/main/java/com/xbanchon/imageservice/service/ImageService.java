package com.xbanchon.imageservice.service;

import com.xbanchon.imageservice.config.RabbitMQConfig;
import com.xbanchon.imageservice.dto.ImageResponse;
import com.xbanchon.imageservice.dto.UploadRequest;
import com.xbanchon.imageservice.entity.Image;
import com.xbanchon.imageservice.dto.ProcessingInstructions;
import com.xbanchon.imageservice.event.ImageProcessingReqEvent;
import com.xbanchon.imageservice.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {
    private final ImageRepository repository;
    private final RabbitTemplate rabbitTemplate;
    private final S3Presigner s3Presigner;

    @Value("${supabase.bucket-name:images}")
    private String bucketName;

    @Transactional
    public ImageResponse initiateUpload(UUID userId, UploadRequest request) {
        log.info("Initiating image upload for user: {}, filename: {}", userId, request.originalFilename());

        Image image = new Image();
        image.setUserId(userId);
        image.setOriginalFilename(request.originalFilename());
        image.setStatus(Image.ProcessingStatus.PENDING);
        image.setMimeType(request.mimeType());
        image.setFileSizeBytes(request.fileSizeBytes());

        image = repository.save(image);

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(image.getId().toString())
                .contentType(image.getMimeType())
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(15))
                .putObjectRequest(objectRequest)
                .build();

        String uploadUrl = s3Presigner.presignPutObject(presignRequest).url().toString();

        return ImageResponse.fromEntity(image, uploadUrl);
    }

    @Transactional
    public void confirmUpload(UUID imageId, UUID userId, ProcessingInstructions instructions) {
        Image image = repository.findByIdAndUserId(imageId, userId)
                .orElseThrow(() -> new RuntimeException("Image not found"));

        // 2. Update status
        image.setStatus(Image.ProcessingStatus.PROCESSING);
        repository.save(image);

        ImageProcessingReqEvent event = new ImageProcessingReqEvent(
                image.getId(),
                image.getUserId(),
                image.getOriginalFilename(),
                image.getMimeType(),
                instructions
        );

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY,
                event
        );

        log.info("Published processing event for image: {}", image.getId());
    }

    @Transactional
    public void updateStatusToCompleted(UUID imageId, UUID userId, String finalStorageUrl) {
        log.info("Updating status to COMPLETED for image: {}", imageId);

        Image image = repository.findByIdAndUserId(imageId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Image not found or unauthorized access"));

        image.setStatus(Image.ProcessingStatus.COMPLETED);
        image.setStorageUrl(finalStorageUrl);

        log.info("Image {} for User {} successfully marked as COMPLETED", imageId, userId);
    }

    @Transactional
    public void updateStatusToFailed(UUID imageId, UUID userId, String errorMessage) {
        log.error("Updating status to FAILED for image: {} due to error: {}", imageId, errorMessage);

        Image image = repository.findByIdAndUserId(imageId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Image not found or unauthorized access"));

        // Update the status to FAILED
        image.setStatus(Image.ProcessingStatus.FAILED);

        repository.save(image);

        log.info("Image {} for User {} successfully marked as FAILED", imageId, userId);
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
