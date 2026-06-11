package com.xbanchon.processingservice.listener;

import com.xbanchon.processingservice.event.ImageProcessingReqEvent;
import com.xbanchon.processingservice.event.ProcessingCompletedEvent;
import com.xbanchon.processingservice.service.ImageProcessingEngine;
import com.xbanchon.processingservice.service.S3StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImageProcessingListener {
    private final ImageProcessingEngine engine;
    private final S3StorageService storageService;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = "image.process.queue")
    public void processImage(ImageProcessingReqEvent event) throws Exception {
        log.info("Received processing job for Image ID: {} (User: {})", event.imageId(), event.userId());

        Path tempInput = null;
        Path tempOutput = null;

        try {
            tempInput = Files.createTempFile("raw_", "_" + event.imageId());
            tempOutput = Files.createTempFile("processed_", "_" + event.imageId());

            // Download raw file from object storage
            String rawFileKey = event.imageId().toString();
            storageService.downloadFile(rawFileKey, tempInput);

            // Execute processing pipeline
            engine.processImage(
                    tempInput.toAbsolutePath().toString(),
                    tempOutput.toAbsolutePath().toString(),
                    event.processingInstructions()
            );

            // Upload processed file
            String processedFileKey = "processed/" + event.imageId();
            String finalStorageUrl = storageService.uploadFile(processedFileKey, tempOutput, event.mimeType());

            // Notify Image Service
            sendCompletionEvent(event, finalStorageUrl, true, null);

            log.info("Successfully processed image: {}", event.originalFilename());

        } finally { // No catch block to force Spring's retry mechanisms (up to 3 times before routing req to DLQ)
            deleteTempFile(tempInput);
            deleteTempFile(tempOutput);
        }

    }

    private void sendCompletionEvent(ImageProcessingReqEvent event, String url, boolean success, String error){
        ProcessingCompletedEvent completionEvent = new ProcessingCompletedEvent(
                event.imageId(),
                event.userId(),
                url,
                success,
                error
        );

        rabbitTemplate.convertAndSend(
                "image.exchange",
                "image.process.completed",
                completionEvent
        );
    }

    private void deleteTempFile(Path path) {
        if (path != null) {
            try {
                Files.deleteIfExists(path);
            } catch (Exception e) {
                log.warn("Failed to delete temp file: {}", path);
            }
        }
    }
}
