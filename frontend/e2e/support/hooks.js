import { After, Before, setDefaultTimeout } from "@cucumber/cucumber";
import { Builder } from "selenium-webdriver";
import chrome from "selenium-webdriver/chrome.js";

import { config } from "./config.js";

setDefaultTimeout(config.stepTimeoutMs);

function buildDriver() {
    const options = new chrome.Options();

    if (config.headless) {
        options.addArguments("--headless=new");
    }

    options.addArguments(
        "--no-sandbox",
        "--disable-dev-shm-usage",
        "--window-size=1440,900",
    );

    return new Builder()
        .forBrowser("chrome")
        .setChromeOptions(options)
        .build();
}

Before(async function () {
    this.driver = buildDriver();
    await this.driver.manage().setTimeouts({
        implicit: 5_000,
        pageLoad: 30_000,
        script: 30_000,
    });
});

After(async function () {
    if (this.driver) {
        await this.driver.quit();
        this.driver = null;
    }
});
