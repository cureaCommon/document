package com.curea.document.repository;

import com.curea.document.domain.DocumentContent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentContentRepository extends JpaRepository<DocumentContent, String> {
}
