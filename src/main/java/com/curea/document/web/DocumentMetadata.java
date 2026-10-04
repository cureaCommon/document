package com.curea.document.web;

import com.curea.document.domain.StoredDocument;

import java.time.Instant;

public record DocumentMetadata(String id, String name, String mimeType, Long size, Instant createdOn) {

    public static DocumentMetadata of(StoredDocument document) {
        return new DocumentMetadata(document.getPublicId(), document.getFileName(), document.getMimeType(),
                document.getSizeBytes(), document.getCreatedOn());
    }
}
