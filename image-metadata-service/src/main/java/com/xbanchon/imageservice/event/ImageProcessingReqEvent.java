package com.xbanchon.imageservice.event;

import com.xbanchon.imageservice.dto.ProcessingInstructions;

import java.util.Map;
import java.util.UUID;

public record ImageProcessingReqEvent(
        UUID imageId,
        UUID userId,
        String originalFilename,
        String mimeType,
        ProcessingInstructions processingInstructions
) {}
