package com.xbanchon.processingservice.event;

import java.util.UUID;

public record ProcessingCompletedEvent(
        UUID imageId,
        UUID userId,
        String finalStorageUrl,
        boolean success,
        String errorMessage // Null if successful
) {

}