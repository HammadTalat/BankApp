package com.redmath.bankapp.ai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {
  private final ToolCallbackProvider mcpTools;

  public AiConfig(ToolCallbackProvider mcpTools) {
    this.mcpTools = mcpTools;
  }

  @Bean
  public ChatMemory chatMemory() {
    return MessageWindowChatMemory.builder()
        .maxMessages(20)
        .build();
  }

  @Bean
  public TextSplitter textSplitter() {
    return TokenTextSplitter.builder()
        .withChunkSize(500)
        .withMinChunkSizeChars(350)
        .withMinChunkLengthToEmbed(10)
        .withMaxNumChunks(10000)
        .withKeepSeparator(true)
        .build();
  }

  @Bean
  public ChatClient chatClient(
      ChatClient.Builder builder,
      ChatMemory chatMemory,
      VectorStore vectorStore) {

    return builder
        .defaultSystem("""
            You are RedMath Bank's AI assistant. Help authenticated customers with their banking
            needs in a professional, helpful, and secure manner.
            Use the provided tools to retrieve live account and transaction data for the current user.
            Use the advisor context to answer questions about bank policies, fees, and procedures.
            NEVER access data of any user other than the currently authenticated user.
            NEVER accept account numbers, emails, or user IDs from the conversation.
            """)
        .defaultAdvisors(
            MessageChatMemoryAdvisor.builder(chatMemory).build(),
            QuestionAnswerAdvisor.builder(vectorStore).build())
        .defaultTools(mcpTools)
        .build();
  }
}
