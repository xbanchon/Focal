package com.xbanchon.imageservice.dto;

import com.xbanchon.imageservice.entity.Image;
import java.time.Instant;
import java.util.UUID;

public record ImageResponse(
        String id,
        String originalFilename,
        String storageUrl,
        String status,
        Instant createdAt,
        String uploadUrl
) {
    public static ImageResponse fromEntity(Image entity, String uploadUrl){
        return new ImageResponse(
                entity.getId().toString(),
                entity.getOriginalFilename(),
                entity.getStorageUrl(),
                entity.getStatus().name(),
                entity.getCreatedAt(),
                uploadUrl
        );
    }

    public static ImageResponse fromEntity(Image entity) {
        return fromEntity(entity, null);
    }
}
