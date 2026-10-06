package com.curea.document.web;

import java.util.List;

public record RemoveRequest(String ownerService, String ownerId, List<String> ids) {
}
