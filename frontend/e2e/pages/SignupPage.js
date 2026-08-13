import { By, until } from "selenium-webdriver";

import { config } from "../support/config.js";

export class SignupPage {
    constructor(driver) {
        this.driver = driver;
    }

    async open() {
        await this.driver.get(`${config.baseUrl}/signup`);
        await this.driver.wait(until.elementLocated(By.css('[data-testid="signup-form"]')), 10_000);
    }

    async signup({ name, email, address, password }) {
        await this.driver.findElement(By.id("signup-name")).sendKeys(name);
        await this.driver.findElement(By.id("signup-email")).sendKeys(email);
        await this.driver.findElement(By.id("signup-address")).sendKeys(address);
        await this.driver.findElement(By.id("signup-password")).sendKeys(password);
        await this.driver.findElement(By.id("signup-confirm-password")).sendKeys(password);
        await this.driver.findElement(By.css('[data-testid="signup-form"] button[type="submit"]')).click();
    }
}
