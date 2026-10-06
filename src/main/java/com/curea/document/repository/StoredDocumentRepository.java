package com.curea.document.repository;

import com.curea.document.domain.StoredDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface StoredDocumentRepository extends JpaRepository<StoredDocument, Long> {

    Optional<StoredDocument> findByPublicIdAndDeletedFalse(String publicId);

    List<StoredDocument> findByPublicIdInAndDeletedFalse(Collection<String> publicIds);

    List<StoredDocument> findByOwnerServiceAndOwnerIdAndDeletedFalse(String ownerService, String ownerId);

    List<StoredDocument> findTop200ByOwnerServiceIsNullAndCreatedOnBeforeAndDeletedFalse(Instant before);

    @Query("select d from StoredDocument d where d.ownerService = :ownerService and d.deleted = false "
            + "and coalesce(d.syncedOn, d.attachedOn) < :before order by d.id")
    List<StoredDocument> findAttachedSyncedBefore(@Param("ownerService") String ownerService, @Param("before") Instant before);
}
