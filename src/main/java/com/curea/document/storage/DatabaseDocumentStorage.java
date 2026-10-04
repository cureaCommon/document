package com.curea.document.storage;

import com.curea.document.domain.DocumentContent;
import com.curea.document.repository.DocumentContentRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(name = "document.storage", havingValue = "database", matchIfMissing = true)
public class DatabaseDocumentStorage implements DocumentStorage {

    private final DocumentContentRepository repository;

    public DatabaseDocumentStorage(DocumentContentRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void put(String publicId, byte[] content) {
        repository.save(new DocumentContent(publicId, content));
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] get(String publicId) {
        return repository.findById(publicId).map(DocumentContent::getContent).orElse(null);
    }

    @Override
    @Transactional
    public void delete(String publicId) {
        repository.deleteById(publicId);
    }
}
