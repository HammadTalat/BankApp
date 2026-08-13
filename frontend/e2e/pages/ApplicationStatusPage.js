import { By, until } from "selenium-webdriver";

export class ApplicationStatusPage {
    constructor(driver) {
        this.driver = driver;
    }

    async waitForLoad() {
        await this.driver.wait(
            until.elementLocated(By.css('[data-testid="application-status-card"]')),
            15_000,
        );
    }

    async getStatusText() {
        const card = await this.driver.findElement(By.css('[data-testid="application-status-card"]'));
        return card.getText();
    }
}
