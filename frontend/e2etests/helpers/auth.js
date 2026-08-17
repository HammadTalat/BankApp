/* global process */
import { expect } from "@playwright/test";

export const adminCredentials = {
    email: process.env.E2E_ADMIN_EMAIL || "admin@bank.local",
    password: process.env.E2E_ADMIN_PASSWORD || "11111111",
};

export function createApplicant() {
    const runId = `${Date.now()}-${Math.floor(Math.random() * 1_000_000)}`;

    return {
        name: `E2E Applicant ${runId}`,
        email: `e2e-applicant-${runId}@redmath.test`,
        address: "42 Test Street, Karachi",
        password: "E2E-password-123!",
    };
}

export async function login(page, credentials, expectedPath) {
    await page.goto("/login");
    await expect(
        page.getByRole("heading", { name: "Sign in to your account" }),
    ).toBeVisible();

    await page.getByLabel("Email address").fill(credentials.email);
    await page.getByLabel("Password", { exact: true }).fill(credentials.password);
    await page.getByRole("button", { name: "Sign in" }).click();

    await expect(page).toHaveURL(new RegExp(`${expectedPath}$`));
}

export async function signUp(page, applicant) {
    await page.goto("/signup");
    await expect(
        page.getByRole("heading", { name: "Banking starts here" }),
    ).toBeVisible();

    await page.getByLabel("Full name").fill(applicant.name);
    await page.getByLabel("Email address").fill(applicant.email);
    await page.getByLabel("Home address").fill(applicant.address);
    await page.getByLabel("Password", { exact: true }).fill(applicant.password);
    await page.getByLabel("Confirm password").fill(applicant.password);
    await page.getByRole("button", { name: "Create account" }).click();

    await expect(page).toHaveURL(/\/login$/);
}

export async function approveApplicant(browser, applicant) {
    const adminContext = await browser.newContext();
    const adminPage = await adminContext.newPage();

    try {
        await login(adminPage, adminCredentials, "/admin");
        await adminPage
            .getByLabel("Admin navigation")
            .getByRole("link", { name: "Pending Users", exact: true })
            .click();
        await expect(
            adminPage.getByRole("heading", { name: "Pending Users" }),
        ).toBeVisible();

        const approveButton = adminPage.getByRole("button", {
            name: `Approve ${applicant.name}`,
        });
        await expect(approveButton).toBeVisible();
        await approveButton.click();

        await expect(
            adminPage.getByRole("dialog", { name: "Approve account holder" }),
        ).toBeVisible();
        await adminPage.getByRole("button", { name: "Approve user" }).click();

        await expect(adminPage.getByText("User approved")).toBeVisible();
        await expect(approveButton).toHaveCount(0);
    } finally {
        await adminContext.close();
    }
}

export async function createApprovedAccountHolder(browser) {
    const applicant = createApplicant();
    const registrationContext = await browser.newContext();
    const registrationPage = await registrationContext.newPage();

    try {
        await signUp(registrationPage, applicant);
    } finally {
        await registrationContext.close();
    }

    await approveApplicant(browser, applicant);
    return applicant;
}

export async function depositMoney(page, { amount, description }) {
    await page.getByRole("link", { name: "Deposit Money" }).click();
    await expect(
        page.getByRole("heading", { name: "Deposit Money" }),
    ).toBeVisible();

    const amountInput = page.getByLabel("Deposit Amount (PKR)");
    await expect(amountInput).toBeEnabled();
    await amountInput.fill(String(amount));
    await page.getByLabel("Transaction Description").fill(description);
    await page.getByRole("button", { name: "Confirm Deposit" }).click();

    await expect(
        page.getByRole("heading", { name: "Deposit Successful" }),
    ).toBeVisible();
}
