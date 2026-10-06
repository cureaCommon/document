package com.curea.document.web;

import com.curea.document.domain.StoredDocument;

public record AttachedDocument(String id, String ownerId) {

    public static AttachedDocument of(StoredDocument document) {
        return new AttachedDocument(document.getPublicId(), document.getOwnerId());
    }
}
