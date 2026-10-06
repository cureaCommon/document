package com.curea.document.service;

import com.curea.document.config.CurrentUser;
import com.curea.document.domain.StoredDocument;
import com.curea.document.domain.Visibility;
import com.curea.document.repository.StoredDocumentRepository;
import com.curea.document.storage.DocumentStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentService.class);

    private final StoredDocumentRepository repository;
    private final DocumentStorage storage;
    private final long maxFileBytes;
    private final long orphanRetentionHours;

    public DocumentService(StoredDocumentRepository repository, DocumentStorage storage,
                           @Value("${document.max-file-bytes:20971520}") long maxFileBytes,
                           @Value("${document.orphan-retention-hours:24}") long orphanRetentionHours) {
        this.repository = repository;
        this.storage = storage;
        this.maxFileBytes = maxFileBytes;
        this.orphanRetentionHours = orphanRetentionHours;
    }

    @Transactional
    public StoredDocument upload(CurrentUser user, String originalName, byte[] content) {
        if (!user.isAuthenticated()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED");
        }
        if (content == null || content.length == 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "FILE_REQUIRED");
        }
        if (content.length > maxFileBytes) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "FILE_TOO_LARGE");
        }
        String fileName = FileTypes.cleanName(originalName);
        String mimeType = FileTypes.detect(content, fileName);
        if (mimeType == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "UNSUPPORTED_FILE_TYPE");
        }
        StoredDocument document = new StoredDocument();
        document.setPublicId(UUID.randomUUID().toString());
        document.setFileName(fileName);
        document.setMimeType(mimeType);
        document.setSizeBytes((long) content.length);
        document.setSha256(sha256(content));
        document.setUploadedBy(user.id());
        document.setCreatedOn(Instant.now());
        document.setVisibility(Visibility.PRIVATE);
        StoredDocument saved = repository.save(document);
        storage.put(saved.getPublicId(), content);
        return saved;
    }

    @Transactional(readOnly = true)
    public StoredDocument findVisible(CurrentUser user, String publicId) {
        StoredDocument document = repository.findByPublicIdAndDeletedFalse(publicId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "DOCUMENT_NOT_FOUND"));
        if (!canSee(user, document)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "DOCUMENT_NOT_FOUND");
        }
        return document;
    }

    @Transactional(readOnly = true)
    public byte[] content(StoredDocument document) {
        byte[] content = storage.get(document.getPublicId());
        if (content == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "DOCUMENT_NOT_FOUND");
        }
        return content;
    }

    @Transactional(readOnly = true)
    public List<StoredDocument> findMetadata(List<String> publicIds) {
        if (publicIds == null || publicIds.isEmpty()) {
            return List.of();
        }
        return repository.findByPublicIdInAndDeletedFalse(publicIds);
    }

    @Transactional
    public List<StoredDocument> sync(String ownerService, String ownerId, List<DocumentAccessRequest> documents,
                                     Long actorId, Set<String> actorRoles) {
        if (ownerService == null || ownerService.isBlank() || ownerId == null || ownerId.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST");
        }
        boolean actorIsAdmin = actorRoles != null && actorRoles.contains(CurrentUser.ADMIN);
        List<DocumentAccessRequest> requested = documents == null ? List.of() : documents;
        Map<String, DocumentAccessRequest> byId = new LinkedHashMap<>();
        for (DocumentAccessRequest request : requested) {
            if (request == null || request.id() == null || request.visibility() == null) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST");
            }
            byId.put(request.id(), request);
        }

        List<StoredDocument> found = byId.isEmpty() ? List.of() : repository.findByPublicIdInAndDeletedFalse(byId.keySet());
        if (found.size() != byId.size()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "DOCUMENT_NOT_FOUND");
        }
        Instant now = Instant.now();
        for (StoredDocument document : found) {
            if (document.isAttached()) {
                if (!ownerService.equals(document.getOwnerService()) || !ownerId.equals(document.getOwnerId())) {
                    throw new ApiException(HttpStatus.BAD_REQUEST, "DOCUMENT_OWNED_BY_ANOTHER_RECORD");
                }
            } else {
                if (!actorIsAdmin && (actorId == null || !actorId.equals(document.getUploadedBy()))) {
                    throw new ApiException(HttpStatus.BAD_REQUEST, "DOCUMENT_NOT_UPLOADED_BY_USER");
                }
                document.setOwnerService(ownerService);
                document.setOwnerId(ownerId);
                document.setAttachedOn(now);
            }
            DocumentAccessRequest request = byId.get(document.getPublicId());
            document.setVisibility(request.visibility());
            document.setAllowedRoles(request.visibility() == Visibility.ROLES ? joinRoles(request.roles()) : null);
            document.setSyncedOn(now);
        }

        for (StoredDocument existing : repository.findByOwnerServiceAndOwnerIdAndDeletedFalse(ownerService, ownerId)) {
            if (!byId.containsKey(existing.getPublicId())) {
                existing.setVisibility(Visibility.PRIVATE);
                existing.setAllowedRoles(null);
                existing.setSyncedOn(now);
            }
        }
        return found;
    }

    @Transactional(readOnly = true)
    public List<StoredDocument> findAttachedSyncedBefore(String ownerService, Instant before) {
        if (ownerService == null || ownerService.isBlank() || before == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST");
        }
        return repository.findAttachedSyncedBefore(ownerService, before);
    }

    @Transactional
    public int removeOwned(String ownerService, String ownerId, Collection<String> publicIds) {
        if (ownerService == null || ownerService.isBlank() || ownerId == null || ownerId.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST");
        }
        if (publicIds == null || publicIds.isEmpty()) {
            return 0;
        }
        int removed = 0;
        for (StoredDocument document : repository.findByPublicIdInAndDeletedFalse(publicIds)) {
            if (ownerService.equals(document.getOwnerService()) && ownerId.equals(document.getOwnerId())) {
                remove(document);
                removed++;
            }
        }
        if (removed > 0) {
            logger.info("Removed {} documents no longer used by {} {}", removed, ownerService, ownerId);
        }
        return removed;
    }

    @Scheduled(fixedDelayString = "PT1H", initialDelayString = "PT5M")
    @Transactional
    public int removeOrphans() {
        Instant before = Instant.now().minus(Duration.ofHours(orphanRetentionHours));
        List<StoredDocument> orphans = repository.findTop200ByOwnerServiceIsNullAndCreatedOnBeforeAndDeletedFalse(before);
        orphans.forEach(this::remove);
        if (!orphans.isEmpty()) {
            logger.info("Removed {} unattached documents older than {} hours", orphans.size(), orphanRetentionHours);
        }
        return orphans.size();
    }

    private void remove(StoredDocument document) {
        document.setDeleted(Boolean.TRUE);
        storage.delete(document.getPublicId());
    }

    static boolean canSee(CurrentUser user, StoredDocument document) {
        if (user.isAuthenticated() && (user.isAdmin() || user.id().equals(document.getUploadedBy()))) {
            return true;
        }
        if (!document.isAttached()) {
            return false;
        }
        return switch (document.getVisibility()) {
            case PUBLIC -> true;
            case AUTHENTICATED -> user.isAuthenticated();
            case ROLES -> user.isAuthenticated() && user.hasAnyRole(splitRoles(document.getAllowedRoles()));
            case PRIVATE -> false;
        };
    }

    private static String joinRoles(List<String> roles) {
        if (roles == null) return null;
        String joined = roles.stream().filter(Objects::nonNull).map(String::trim).filter(r -> !r.isEmpty())
                .distinct().collect(Collectors.joining(","));
        return joined.isEmpty() ? null : joined;
    }

    private static Set<String> splitRoles(String roles) {
        if (roles == null || roles.isBlank()) return Set.of();
        return Arrays.stream(roles.split(",")).map(String::trim).filter(r -> !r.isEmpty()).collect(Collectors.toSet());
    }

    private static String sha256(byte[] content) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
