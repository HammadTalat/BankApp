import { By, until } from "selenium-webdriver";

import { config } from "../support/config.js";

export class ChatPage {
    constructor(driver) {
        this.driver = driver;
    }

    async open() {
        await this.driver.get(`${config.baseUrl}/account/chatbot`);
        await this.waitForLoad();
    }

    async openFromSidebar() {
        const link = await this.driver.wait(
            until.elementLocated(By.css('[data-testid="nav-ai-assistant"]')),
            10_000,
        );
        await link.click();
        await this.waitForLoad();
    }

    async waitForLoad() {
        await this.driver.wait(
            until.elementLocated(By.css('[data-testid="chatbot-heading"]')),
            15_000,
        );
    }

    async ask(question) {
        const input = await this.driver.findElement(By.css('[data-testid="chat-input"]'));
        await input.clear();
        await input.sendKeys(question);
        await this.driver.findElement(By.css('[data-testid="chat-send-button"]')).click();
    }

    async waitForLatestAssistantMessage(previousCount = 0) {
        await this.driver.wait(async () => {
            const messages = await this.driver.findElements(
                By.css('[data-testid="chat-message-assistant"] [data-testid="chat-message-content"]'),
            );
            if (messages.length <= previousCount) {
                return false;
            }

            const latest = messages[messages.length - 1];
            const text = await latest.getText();
            return text.trim().length > 0;
        }, config.chatTimeoutMs);

        const messages = await this.driver.findElements(
            By.css('[data-testid="chat-message-assistant"] [data-testid="chat-message-content"]'),
        );
        return messages[messages.length - 1].getText();
    }

    async countAssistantMessages() {
        const messages = await this.driver.findElements(
            By.css('[data-testid="chat-message-assistant"] [data-testid="chat-message-content"]'),
        );
        return messages.length;
    }

    async refreshPage() {
        await this.driver.navigate().refresh();
        await this.waitForLoad();
    }
}
