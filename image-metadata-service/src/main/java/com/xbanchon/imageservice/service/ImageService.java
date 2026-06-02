package com.xbanchon.imageservice.service;

import com.xbanchon.imageservice.config.RabbitMQConfig;
import com.xbanchon.imageservice.entity.Image;
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
    public String initiateUpload(Image image) {
        log.info("Initiating image upload for user: {}, filename: {}", image.getUserId(), image.getOriginalFilename());
        repository.save(image);

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(image.getId().toString())
                .contentType(image.getMimeType())
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(15))
                .putObjectRequest(objectRequest)
                .build();

        return s3Presigner.presignPutObject(presignRequest).url().toString();
    }

    @Transactional
    public void confirmUpload(UUID imageId, UUID userId) {
        Image image = repository.findByIdAndUserId(imageId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Image not found"));

        ImageProcessingReqEvent event = new ImageProcessingReqEvent(
                image.getId(),
                image.getUserId(),
                image.getOriginalFilename(),
                image.getMimeType()
        );

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY,
                event
        );

        log.info("Published processing event for image: {}", image.getId());
    }

    @Transactional
    public Image updateStatusToCompleted(UUID imageId, UUID userId, String finalStorageUrl) {
        log.info("Updating status to COMPLETED for image: {}", imageId);

        Image image = repository.findByIdAndUserId(imageId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Image not found or unauthorized access"));

        image.setStatus(Image.ProcessingStatus.COMPLETED);
        image.setStorageUrl(finalStorageUrl);

        log.info("Image {} for User {} successfully marked as COMPLETED", imageId, userId);

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
