package com.curea.document.repository;

import com.curea.document.domain.StoredDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface StoredDocumentRepository extends JpaRepository<StoredDocument, Long> {

    Optional<StoredDocument> findByPublicIdAndDeletedFalse(String publicId);

    List<StoredDocument> findByPublicIdInAndDeletedFalse(Collection<String> publicIds);

    List<StoredDocument> findByOwnerServiceAndOwnerIdAndDeletedFalse(String ownerService, String ownerId);

    List<StoredDocument> findTop200ByOwnerServiceIsNullAndCreatedOnBeforeAndDeletedFalse(Instant before);
}
