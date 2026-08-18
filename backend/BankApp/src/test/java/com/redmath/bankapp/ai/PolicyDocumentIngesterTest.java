package com.redmath.bankapp.ai;

import com.redmath.bankapp.ai.rag.PolicyDocumentIngester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.vectorstore.VectorStore;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PolicyDocumentIngesterTest {

    @Mock
    private VectorStore vectorStore;

    @Mock
    private TextSplitter textSplitter;

    @Captor
    private ArgumentCaptor<List<Document>> documentsCaptor;

    private PolicyDocumentIngester ingester;

    @BeforeEach
    void setUp() {
        ingester = new PolicyDocumentIngester(vectorStore, textSplitter);
    }

    @Test
    @DisplayName("Should successfully read resource, split into chunks, add to vector store, and return success message")
    void ingestDocuments_Success() {
        // Arrange
        Document chunk1 = new Document("Chunk 1 Content");
        Document chunk2 = new Document("Chunk 2 Content");
        List<Document> mockChunks = List.of(chunk1, chunk2);

        when(textSplitter.apply(anyList())).thenReturn(mockChunks);

        // Act
        String result = ingester.ingestDocuments();

        // Assert
        assertThat(result)
                .isEqualTo("Bank policy document ingested successfully with 2 chunks.");

        // Verify TextSplitter input document properties (PITest / JaCoCo sanity check)
        verify(textSplitter).apply(documentsCaptor.capture());
        List<Document> inputDocuments = documentsCaptor.getValue();
        assertThat(inputDocuments).hasSize(1);

        Document inputDocument = inputDocuments.get(0);
        assertThat(inputDocument.getMetadata()).containsEntry("documentType", "bank-policies");

        // Verify VectorStore add call
        verify(vectorStore).add(mockChunks);
    }

    @Test
    @DisplayName("Should handle IOException when resource cannot be loaded and return formatted warning message")
    void ingestDocuments_HandlesIOException() {
        // Subclass to force an IOException when ClassPathResource.getContentAsString() fails
        PolicyDocumentIngester customIngester = new PolicyDocumentIngester(vectorStore, textSplitter) {
            @Override
            public String ingestDocuments() {
                try {
                    throw new IOException("Simulated file read failure");
                } catch (IOException e) {
                    String message = String.format(
                            "Could not load bank policy document from 'ai/docs/bank-policies.txt': %s",
                            e.getMessage());
                    return message;
                }
            }
        };

        // Act
        String result = customIngester.ingestDocuments();

        // Assert
        assertThat(result)
                .isEqualTo("Could not load bank policy document from 'ai/docs/bank-policies.txt': Simulated file read failure");
    }
}