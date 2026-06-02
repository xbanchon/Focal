package com.xbanchon.imageservice.event;

import java.util.UUID;

public record ImageProcessingReqEvent(
        UUID imageId,
        UUID userId,
        String originalFilename,
        String mimeType
) {}
