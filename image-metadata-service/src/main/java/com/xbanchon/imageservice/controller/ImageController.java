package com.xbanchon.imageservice.controller;

import com.xbanchon.imageservice.dto.ImageResponse;
import com.xbanchon.imageservice.dto.ProcessingInstructions;
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
    private final ImageService imageService;

    @PostMapping
    public ResponseEntity<ImageResponse> initiateUpload(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody UploadRequest request
            ) {
        ImageResponse response = imageService.initiateUpload(
                userId,
                request
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/{imageId}/confirm")
    public ResponseEntity<Void> confirmUpload(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String imageId,
            @RequestBody ProcessingInstructions instructions
    ) {
        imageService.confirmUpload(UUID.fromString(imageId), UUID.fromString(userId), instructions);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<ImageResponse>> getUserImages(
            @RequestHeader("X-User-Id") UUID userId
    ) {
        List<ImageResponse> responses = imageService.getUserImages(userId).stream()
                .map(ImageResponse::fromEntity)
                .toList();

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{imageId}")
    public ResponseEntity<ImageResponse> getImageDetails(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID imageId
    ) {
        Image image = imageService.getImageDetails(imageId, userId);
        return ResponseEntity.ok(ImageResponse.fromEntity(image));
    }
}
