package com.curea.document.web;

import com.curea.document.service.DocumentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/internal/documents")
public class InternalDocumentController {

    private final DocumentService service;

    public InternalDocumentController(DocumentService service) {
        this.service = service;
    }

    @PutMapping("/sync")
    public List<DocumentMetadata> sync(@RequestBody SyncRequest request) {
        return service.sync(request.ownerService(), request.ownerId(), request.documents(),
                request.actorId(), request.actorRoles()).stream().map(DocumentMetadata::of).toList();
    }

    @PostMapping("/metadata")
    public List<DocumentMetadata> metadata(@RequestBody List<String> ids) {
        return service.findMetadata(ids).stream().map(DocumentMetadata::of).toList();
    }
}
