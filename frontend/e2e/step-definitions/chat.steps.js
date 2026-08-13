import { Then, When } from "@cucumber/cucumber";
import assert from "node:assert/strict";
import { By } from "selenium-webdriver";

import { ChatPage } from "../pages/ChatPage.js";

When("I open the AI chat assistant", async function () {
    const chatPage = new ChatPage(this.driver);
    await chatPage.openFromSidebar();
});

When("I ask the assistant {string}", async function (question) {
    const chatPage = new ChatPage(this.driver);
    this.assistantMessageCountBefore = await chatPage.countAssistantMessages();
    await chatPage.ask(question);
    this.lastAssistantMessage = await chatPage.waitForLatestAssistantMessage(
        this.assistantMessageCountBefore,
    );
});

When("I refresh the chat page", async function () {
    const chatPage = new ChatPage(this.driver);
    await chatPage.refreshPage();
});

Then("I should see an account summary in the chat response", async function () {
    const response = String(this.lastAssistantMessage || "").toLowerCase();
    assert.ok(
        /balance|account|pkr|available/.test(response),
        `Expected account summary details, got: ${this.lastAssistantMessage}`,
    );
});

Then("I should see recent transactions in the chat response", async function () {
    const response = String(this.lastAssistantMessage || "").toLowerCase();
    assert.ok(
        /transaction|debit|credit|transfer|deposit|withdraw/.test(response),
        `Expected transaction details, got: ${this.lastAssistantMessage}`,
    );
});

Then("I should see overdraft policy information in the chat response", async function () {
    const response = String(this.lastAssistantMessage || "").toLowerCase();
    assert.ok(
        /overdraft/.test(response) && /35|\$35|fee/.test(response),
        `Expected overdraft policy details, got: ${this.lastAssistantMessage}`,
    );
});

Then("I should see both balance and overdraft information in the chat response", async function () {
    const response = String(this.lastAssistantMessage || "").toLowerCase();
    assert.ok(/balance|account|pkr/.test(response), `Expected balance info, got: ${this.lastAssistantMessage}`);
    assert.ok(/overdraft/.test(response), `Expected overdraft info, got: ${this.lastAssistantMessage}`);
});

Then("the chat should remember my previous conversation", async function () {
    const chatPage = new ChatPage(this.driver);
    const messages = await this.driver.findElements(
        By.css(
            '[data-testid="chat-message-user"] [data-testid="chat-message-content"],'
            + '[data-testid="chat-message-assistant"] [data-testid="chat-message-content"]',
        ),
    );

    assert.ok(messages.length >= 2, "Expected persisted chat history after refresh");

    const combinedText = [];
    for (const message of messages) {
        combinedText.push(await message.getText());
    }

    const history = combinedText.join("\n").toLowerCase();
    assert.match(history, /balance/);
});
