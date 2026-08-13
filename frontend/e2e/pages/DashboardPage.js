import { By, until } from "selenium-webdriver";

export class DashboardPage {
    constructor(driver) {
        this.driver = driver;
    }

    async waitForLoad() {
        await this.driver.wait(
            until.elementLocated(By.css('[data-testid="dashboard-heading"]')),
            15_000,
        );
    }

    async isVisible() {
        const elements = await this.driver.findElements(By.css('[data-testid="dashboard-heading"]'));
        return elements.length > 0;
    }
}
