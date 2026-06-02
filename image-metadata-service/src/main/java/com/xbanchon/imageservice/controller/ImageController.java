package com.xbanchon.imageservice.controller;

import com.xbanchon.imageservice.dto.ImageResponse;
import com.xbanchon.imageservice.dto.UploadRequest;
import com.xbanchon.imageservice.entity.Image;
import com.xbanchon.imageservice.service.ImageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
public class ImageController {
    private final ImageService service;

    @PostMapping
    public ResponseEntity<ImageResponse> startUpload(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody UploadRequest request
            ) {
        Image image = service.startUpload(
                userId,
                request.filename(),
                request.fileSizeBytes(),
                request.mimeType()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ImageResponse.fromEntity(image));
    }

    @GetMapping
    public ResponseEntity<List<ImageResponse>> getUserImages(
            @RequestHeader("X-User-Id") UUID userId
    ) {
        List<ImageResponse> responses = service.getUserImages(userId).stream()
                .map(ImageResponse::fromEntity)
                .toList();

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{imageId}")
    public ResponseEntity<ImageResponse> getImageDetails(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID imageId
    ) {
        Image image = service.getImageDetails(imageId, userId);
        return ResponseEntity.ok(ImageResponse.fromEntity(image));
    }
}
