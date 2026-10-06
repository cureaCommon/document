package com.curea.document.web;

import com.curea.document.service.DocumentService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

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

    @GetMapping("/attached")
    public List<AttachedDocument> attached(@RequestParam String ownerService,
                                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant syncedBefore) {
        return service.findAttachedSyncedBefore(ownerService, syncedBefore).stream().map(AttachedDocument::of).toList();
    }

    @PostMapping("/remove")
    public Map<String, Integer> remove(@RequestBody RemoveRequest request) {
        return Map.of("removed", service.removeOwned(request.ownerService(), request.ownerId(), request.ids()));
    }

    @PostMapping("/metadata")
    public List<DocumentMetadata> metadata(@RequestBody List<String> ids) {
        return service.findMetadata(ids).stream().map(DocumentMetadata::of).toList();
    }
}
