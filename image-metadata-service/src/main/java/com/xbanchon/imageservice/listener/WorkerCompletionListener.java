package com.xbanchon.imageservice.listener;


import com.xbanchon.imageservice.event.ProcessingCompletedEvent;
import com.xbanchon.imageservice.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkerCompletionListener {

    private final ImageService service;

    @RabbitListener(queues = "image.completed.queue")
    public void handleProcessingCompletion(ProcessingCompletedEvent event) {
        if (event.success()) {
            service.updateStatusToCompleted(event.imageId(), event.userId(), event.finalStorageUrl());
            log.info("Successfully updated status to COMPLETED for image: {}", event.imageId());
        } else {
            service.updateStatusToFailed(event.imageId(), event.userId(), event.errorMessage());
            log.error("Worker failed to process image {}: {}", event.imageId(), event.errorMessage());
        }
    }
}
