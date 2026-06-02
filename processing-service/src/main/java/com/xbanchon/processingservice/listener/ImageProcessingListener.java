package com.xbanchon.processingservice.listener;

import com.xbanchon.processingservice.dto.ImageProcessingReqEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ImageProcessingListener {

    @RabbitListener
    public void processImage(ImageProcessingReqEvent event) {
        log.info("Received processing job for Image ID: {} (User: {})", event.imageId(), event.userId());

        try {
            // TODO: 1. Download original raw image from Supabase
            // TODO: 2. Process/Resize the image using Thumbnailator
            // TODO: 3. Upload the processed image back to Supabase
            // TODO: 4. Publish a "Completed" event back to RabbitMQ

            log.info("Successfully processed image: {}", event.originalFilename());

        } catch (Exception e) {
            log.error("Failed to process image {}: {}", event.imageId(), e.getMessage());
            // If an exception is thrown here, RabbitMQ can automatically put the message
            // back in the queue to retry, or send it to a Dead Letter Queue.
        }

        ProcessingCompletedEvent completedEvent = new ProcessingCompletedEvent(
                event.imageId(),
                newSupabaseUrl,
                true,
                null
        );

        rabbitTemplate.convertAndSend(
                "image.exchange",
                "image.process.completed",
                completedEvent
        );
    }
}
