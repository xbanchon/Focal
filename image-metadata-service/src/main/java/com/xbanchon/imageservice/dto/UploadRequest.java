package com.xbanchon.imageservice.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotBlank;

public record UploadRequest(
    @NotBlank(message = "Filename cannot be empty")
    String filename,

    @Positive(message = "File size must be greater than zero")
    long fileSizeBytes,

    @NotBlank(message = "Mime type is required")
    String mimeType
) {}
