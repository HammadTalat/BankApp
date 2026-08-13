import { By, until } from "selenium-webdriver";

export class ProfileMenu {
    constructor(driver) {
        this.driver = driver;
    }

    async isOpen() {
        const cards = await this.driver.findElements(By.css('[data-testid="profile-card"]'));
        return cards.length > 0;
    }

    async ensureOpen() {
        if (!(await this.isOpen())) {
            const trigger = await this.driver.wait(
                until.elementLocated(By.css('[data-testid="profile-menu-trigger"]')),
                10_000,
            );
            await trigger.click();
        }

        await this.driver.wait(
            until.elementLocated(By.css('[data-testid="profile-card"]')),
            10_000,
        );
    }

    async open() {
        await this.ensureOpen();
    }

    async logout() {
        await this.ensureOpen();
        const logoutButton = await this.driver.wait(
            until.elementLocated(By.css('[data-testid="profile-logout-button"]')),
            10_000,
        );
        await logoutButton.click();
    }

    async getProfileDetails() {
        await this.ensureOpen();
        const region = await this.driver.findElement(By.css('[data-testid="profile-card"]'));
        return region.getText();
    }
}
