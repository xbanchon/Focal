package com.xbanchon.processingservice.event;

import java.util.Map;
import java.util.UUID;

public record ImageProcessingReqEvent(
   UUID imageId,
   UUID userId,
   String originalFilename,
   String mimeType,
   Map<String, Object> processingInstructions
) {}
