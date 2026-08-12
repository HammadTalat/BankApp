package com.redmath.bankapp.ai.controller;

import com.redmath.bankapp.ai.rag.PolicyDocumentIngester;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai/documents")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class DocumentIngestionController {

  private final PolicyDocumentIngester policyDocumentIngester;

  @PostMapping("/ingest")
  public ResponseEntity<String> ingestPolicies() {
    return ResponseEntity.ok(policyDocumentIngester.ingestDocuments());
  }
}
