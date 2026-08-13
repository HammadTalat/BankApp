import { By, until } from "selenium-webdriver";

import { config } from "../support/config.js";

export class LoginPage {
    constructor(driver) {
        this.driver = driver;
    }

    async open() {
        await this.driver.get(`${config.baseUrl}/login`);
        await this.driver.wait(until.elementLocated(By.css('[data-testid="login-form"]')), 10_000);
    }

    async login(email, password) {
        await this.driver.findElement(By.id("login-email")).clear();
        await this.driver.findElement(By.id("login-email")).sendKeys(email);
        await this.driver.findElement(By.id("login-password")).clear();
        await this.driver.findElement(By.id("login-password")).sendKeys(password);
        await this.driver.findElement(By.css('[data-testid="login-form"] button[type="submit"]')).click();
    }

    async getErrorMessage() {
        const error = await this.driver.wait(
            until.elementLocated(By.css('[data-testid="login-error"]')),
            10_000,
        );
        return error.getText();
    }
}
