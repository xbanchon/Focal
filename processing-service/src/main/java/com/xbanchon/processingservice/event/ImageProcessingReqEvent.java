package com.xbanchon.processingservice.dto;

import java.util.UUID;

public record ImageProcessingReqEvent(
   UUID imageId,
   UUID userId,
   String originalFilename,
   String mimeType
) {}
