package com.xbanchon.imageservice.dto;

import com.xbanchon.imageservice.entity.Image;
import java.time.Instant;
import java.util.UUID;

public record ImageResponse(
        UUID id,
        String originalFilename,
        String storageUrl,
        String status,
        Instant createdAt
) {
    public static ImageResponse fromEntity(Image entity){
        return new ImageResponse(
                entity.getId(),
                entity.getOriginalFilename(),
                entity.getStorageUrl(),
                entity.getStatus().name(),
                entity.getCreatedAt()
        );
    }
}
