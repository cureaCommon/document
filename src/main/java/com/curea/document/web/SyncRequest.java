package com.curea.document.web;

import com.curea.document.service.DocumentAccessRequest;

import java.util.List;
import java.util.Set;

public record SyncRequest(String ownerService, String ownerId, List<DocumentAccessRequest> documents,
                          Long actorId, Set<String> actorRoles) {
}
