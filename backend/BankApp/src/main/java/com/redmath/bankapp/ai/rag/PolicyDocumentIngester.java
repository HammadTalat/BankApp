package com.redmath.bankapp.ai.rag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PolicyDocumentIngester {

  private static final String POLICY_DOC_PATH = "ai/docs/bank-policies.txt";

  private final VectorStore vectorStore;
  private final TextSplitter textSplitter;


  public String ingestDocuments() {
    try {
      ClassPathResource resource = new ClassPathResource(POLICY_DOC_PATH);
      String content = resource.getContentAsString(StandardCharsets.UTF_8);
      Document document = new Document(content, Map.of("documentType", "bank-policies"));
      List<Document> chunks = textSplitter.apply(List.of(document));
      vectorStore.add(chunks);
      String message = String.format(
          "Bank policy document ingested successfully with %d chunks.",
          chunks.size());
      log.info(message);
      return message;
    } catch (IOException e) {
      String message = String.format(
          "Could not load bank policy document from '%s': %s",
          POLICY_DOC_PATH,
          e.getMessage());
      log.warn(message);
      return message;
    }
  }
}
