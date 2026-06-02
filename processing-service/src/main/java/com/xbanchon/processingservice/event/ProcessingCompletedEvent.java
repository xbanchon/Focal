package com.xbanchon.processingservice.dto;

import java.util.UUID;

public Record ProcessingCompletedEvent(
        UUID imageId,
        UUID userId,
        String finalStorageUrl,
        boolean success,
        String errorMessage // Null if successful
) {

}