package com.curea.document.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "document_content")
public class DocumentContent {

    @Id
    @Column(name = "public_id", length = 36)
    private String publicId;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "content", nullable = false, columnDefinition = "bytea")
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.VARBINARY)
    private byte[] content;

    protected DocumentContent() {
    }

    public DocumentContent(String publicId, byte[] content) {
        this.publicId = publicId;
        this.content = content;
    }

    public String getPublicId() { return publicId; }
    public byte[] getContent() { return content; }
}
