import { By, until } from "selenium-webdriver";

import { config } from "../support/config.js";

export class CompleteProfilePage {
    constructor(driver) {
        this.driver = driver;
    }

    async waitForLoad() {
        await this.driver.wait(
            until.elementLocated(By.css('[data-testid="complete-profile-form"]')),
            15_000,
        );
    }

    async completeProfile(address) {
        await this.driver.findElement(By.id("google-address")).clear();
        await this.driver.findElement(By.id("google-address")).sendKeys(address);
        await this.driver.findElement(
            By.css('[data-testid="complete-profile-form"] button[type="submit"]'),
        ).click();
    }

    async open() {
        await this.driver.get(`${config.baseUrl}/complete-profile`);
        await this.waitForLoad();
    }
}
