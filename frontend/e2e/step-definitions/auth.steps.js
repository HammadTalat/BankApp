import { Given, Then, When } from "@cucumber/cucumber";
import assert from "node:assert/strict";

import { LoginPage } from "../pages/LoginPage.js";
import { SignupPage } from "../pages/SignupPage.js";
import { DashboardPage } from "../pages/DashboardPage.js";
import { ApplicationStatusPage } from "../pages/ApplicationStatusPage.js";
import { ProfileMenu } from "../pages/ProfileMenu.js";
import { CompleteProfilePage } from "../pages/CompleteProfilePage.js";
import {
    approveUserViaApi,
    createApprovedAccountHolder,
    createPendingAccountHolder,
} from "../support/api-client.js";
import { config } from "../support/config.js";

Given("I am on the login page", async function () {
    const loginPage = new LoginPage(this.driver);
    await loginPage.open();
});

Given("I am on the signup page", async function () {
    const signupPage = new SignupPage(this.driver);
    await signupPage.open();
});

Given("an approved account holder exists", async function () {
    this.testUser = await createApprovedAccountHolder();
});

Given("a pending account holder exists", async function () {
    this.testUser = await createPendingAccountHolder();
});

Given("I am logged in as the test account holder", async function () {
    if (!this.testUser) {
        this.testUser = await createApprovedAccountHolder();
    }

    const loginPage = new LoginPage(this.driver);
    await loginPage.open();
    await loginPage.login(this.testUser.email, this.testUser.password);

    const dashboardPage = new DashboardPage(this.driver);
    await dashboardPage.waitForLoad();
});

Given("I am logged in as a pending account holder", async function () {
    if (!this.testUser) {
        this.testUser = await createPendingAccountHolder();
    }

    const loginPage = new LoginPage(this.driver);
    await loginPage.open();
    await loginPage.login(this.testUser.email, this.testUser.password);

    const applicationStatusPage = new ApplicationStatusPage(this.driver);
    await applicationStatusPage.waitForLoad();
});

When("I register a new account", async function () {
    const uniqueSuffix = `${Date.now()}-${Math.floor(Math.random() * 10_000)}`;
    this.testUser = {
        name: `Signup User ${uniqueSuffix}`,
        email: `signup.user.${uniqueSuffix}@redmath.test`,
        address: config.defaultAddress,
        password: config.defaultPassword,
    };

    const signupPage = new SignupPage(this.driver);
    await signupPage.open();
    await signupPage.signup(this.testUser);

    await this.driver.wait(async () => {
        const currentUrl = await this.driver.getCurrentUrl();
        return currentUrl.includes("/login");
    }, 15_000);
});

When("an administrator approves my account", async function () {
    assert.ok(this.testUser?.email, "Expected a registered test user before approval");
    await approveUserViaApi(this.testUser.email);
});

When("I log in with the new account credentials", async function () {
    assert.ok(this.testUser, "Expected test user credentials");

    const loginPage = new LoginPage(this.driver);
    await loginPage.open();
    await loginPage.login(this.testUser.email, this.testUser.password);
});

When("I log in with email {string} and password {string}", async function (email, password) {
    const loginPage = new LoginPage(this.driver);
    await loginPage.open();
    await loginPage.login(email, password);
});

When("I open my profile menu", async function () {
    const profileMenu = new ProfileMenu(this.driver);
    await profileMenu.open();
});

When("I log out from the profile menu", async function () {
    const profileMenu = new ProfileMenu(this.driver);
    await profileMenu.logout();
});

When("I try to visit {string} while logged out", async function (path) {
    await this.driver.get(`${config.baseUrl}${path}`);
});

When("I try to visit the chat page while logged out", async function () {
    await this.driver.get(`${config.baseUrl}/account/chatbot`);
});

When("I try to visit the chat page as a pending user", async function () {
    await this.driver.get(`${config.baseUrl}/account/chatbot`);
});

Then("I should see the account dashboard", async function () {
    const dashboardPage = new DashboardPage(this.driver);
    await dashboardPage.waitForLoad();
    assert.equal(await dashboardPage.isVisible(), true);
});

Then("I should see a login error", async function () {
    const loginPage = new LoginPage(this.driver);
    const message = await loginPage.getErrorMessage();
    assert.match(message.toLowerCase(), /invalid|unable|password|credentials/);
});

Then("I should see my profile details", async function () {
    const profileMenu = new ProfileMenu(this.driver);
    const details = await profileMenu.getProfileDetails();

    assert.ok(this.testUser?.email, "Expected test user email");
    assert.match(details, new RegExp(this.testUser.email, "i"));
    assert.match(details, new RegExp(this.testUser.name, "i"));
});

Then("I should be redirected to the login page", async function () {
    await this.driver.wait(async () => {
        const currentUrl = await this.driver.getCurrentUrl();
        return currentUrl.includes("/login");
    }, 15_000);
});

Then("I should see the application status page", async function () {
    const applicationStatusPage = new ApplicationStatusPage(this.driver);
    await applicationStatusPage.waitForLoad();

    const statusText = await applicationStatusPage.getStatusText();
    assert.match(statusText.toUpperCase(), /PENDING|APPLICATION/);
});

Then("I should remain on the application status page", async function () {
    await this.driver.wait(async () => {
        const currentUrl = await this.driver.getCurrentUrl();
        return /application-status/.test(currentUrl);
    }, 15_000);

    const applicationStatusPage = new ApplicationStatusPage(this.driver);
    await applicationStatusPage.waitForLoad();
});

Then("I should see the complete profile page", async function () {
    const completeProfilePage = new CompleteProfilePage(this.driver);
    await completeProfilePage.waitForLoad();
});

When("I complete my profile with a valid address", async function () {
    const completeProfilePage = new CompleteProfilePage(this.driver);
    await completeProfilePage.completeProfile(config.defaultAddress);
});
