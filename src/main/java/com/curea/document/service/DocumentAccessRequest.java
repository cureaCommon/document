package com.curea.document.service;

import com.curea.document.domain.Visibility;

import java.util.List;

public record DocumentAccessRequest(String id, Visibility visibility, List<String> roles) {
}
