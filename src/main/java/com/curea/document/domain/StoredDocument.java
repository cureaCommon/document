package com.curea.document.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "stored_document", indexes = {
        @Index(name = "ix_stored_document_public_id", columnList = "public_id", unique = true),
        @Index(name = "ix_stored_document_owner", columnList = "owner_service,owner_id")
})
public class StoredDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", nullable = false, length = 36, updatable = false)
    private String publicId;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "mime_type", nullable = false, length = 120)
    private String mimeType;

    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    @Column(name = "sha256", nullable = false, length = 64)
    private String sha256;

    @Column(name = "uploaded_by", nullable = false)
    private Long uploadedBy;

    @Column(name = "created_on", nullable = false)
    private Instant createdOn;

    @Column(name = "owner_service", length = 40)
    private String ownerService;

    @Column(name = "owner_id", length = 64)
    private String ownerId;

    @Column(name = "attached_on")
    private Instant attachedOn;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false, length = 20)
    private Visibility visibility = Visibility.PRIVATE;

    @Column(name = "allowed_roles", length = 255)
    private String allowedRoles;

    @Column(name = "is_deleted", nullable = false)
    private Boolean deleted = Boolean.FALSE;

    @Column(name = "synced_on")
    private Instant syncedOn;

    public Long getId() { return id; }
    public String getPublicId() { return publicId; }
    public void setPublicId(String publicId) { this.publicId = publicId; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public Long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(Long sizeBytes) { this.sizeBytes = sizeBytes; }
    public String getSha256() { return sha256; }
    public void setSha256(String sha256) { this.sha256 = sha256; }
    public Long getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(Long uploadedBy) { this.uploadedBy = uploadedBy; }
    public Instant getCreatedOn() { return createdOn; }
    public void setCreatedOn(Instant createdOn) { this.createdOn = createdOn; }
    public String getOwnerService() { return ownerService; }
    public void setOwnerService(String ownerService) { this.ownerService = ownerService; }
    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    public Instant getAttachedOn() { return attachedOn; }
    public void setAttachedOn(Instant attachedOn) { this.attachedOn = attachedOn; }
    public Instant getSyncedOn() { return syncedOn; }
    public void setSyncedOn(Instant syncedOn) { this.syncedOn = syncedOn; }
    public Visibility getVisibility() { return visibility; }
    public void setVisibility(Visibility visibility) { this.visibility = visibility; }
    public String getAllowedRoles() { return allowedRoles; }
    public void setAllowedRoles(String allowedRoles) { this.allowedRoles = allowedRoles; }
    public Boolean getDeleted() { return deleted; }
    public void setDeleted(Boolean deleted) { this.deleted = deleted; }

    public boolean isAttached() {
        return ownerService != null && ownerId != null;
    }
}
