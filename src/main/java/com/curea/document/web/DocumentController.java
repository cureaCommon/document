package com.curea.document.web;

import com.curea.document.config.CurrentUser;
import com.curea.document.domain.StoredDocument;
import com.curea.document.service.ApiException;
import com.curea.document.service.DocumentService;
import com.curea.document.service.FileTypes;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService service;

    public DocumentController(DocumentService service) {
        this.service = service;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DocumentMetadata upload(@RequestParam(value = "file", required = false) MultipartFile file,
                                   HttpServletRequest request) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "FILE_REQUIRED");
        }
        StoredDocument saved = service.upload(CurrentUser.from(request), file.getOriginalFilename(), file.getBytes());
        return DocumentMetadata.of(saved);
    }

    @GetMapping("/{id}")
    public DocumentMetadata metadata(@PathVariable String id, HttpServletRequest request) {
        return DocumentMetadata.of(service.findVisible(CurrentUser.from(request), id));
    }

    @GetMapping("/{id}/content")
    public ResponseEntity<byte[]> content(@PathVariable String id,
                                          @RequestParam(value = "download", defaultValue = "false") boolean download,
                                          HttpServletRequest request) {
        StoredDocument document = service.findVisible(CurrentUser.from(request), id);
        byte[] content = service.content(document);
        boolean inline = !download && FileTypes.isInline(document.getMimeType());
        ContentDisposition disposition = (inline ? ContentDisposition.inline() : ContentDisposition.attachment())
                .filename(document.getFileName(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(document.getMimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .header("X-Content-Type-Options", "nosniff")
                .header(HttpHeaders.CACHE_CONTROL, "private, no-store")
                .body(content);
    }
}
