@ai
Feature: Chatbot flows
  System tests for MCP tools (account summary, recent transactions) and RAG policy answers.

  Background:
    Given an approved account holder exists
    And I am logged in as the test account holder

  Scenario: Ask for account balance uses get_account_summary tool
    When I open the AI chat assistant
    And I ask the assistant "What's my balance?"
    Then I should see an account summary in the chat response

  Scenario: Ask for recent transactions uses get_recent_transactions tool
    When I open the AI chat assistant
    And I ask the assistant "Show my last 5 transactions"
    Then I should see recent transactions in the chat response

  Scenario: Ask about overdraft fees uses RAG policy context
    When I open the AI chat assistant
    And I ask the assistant "What are the overdraft fees?"
    Then I should see overdraft policy information in the chat response

  Scenario: Combined balance and policy question
    When I open the AI chat assistant
    And I ask the assistant "What's my balance and overdraft fees?"
    Then I should see both balance and overdraft information in the chat response

  Scenario: Chat history persists after page refresh
    When I open the AI chat assistant
    And I ask the assistant "What's my balance?"
    And I refresh the chat page
    Then the chat should remember my previous conversation
