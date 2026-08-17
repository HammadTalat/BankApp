/* global process */
import { expect, test } from "@playwright/test";

import {
    createApprovedAccountHolder,
    depositMoney,
    login,
} from "../helpers/auth.js";

const recipientAccountNumber =
    process.env.E2E_RECIPIENT_ACCOUNT || "CD364D1F2CEE4AC8";

test("an approved account holder can transfer available funds to a verified recipient", async ({
    browser,
}) => {
    const applicant = await createApprovedAccountHolder(browser);
    const customerContext = await browser.newContext();
    const customerPage = await customerContext.newPage();
    const transferDescription = `E2E transfer ${applicant.email}`;

    try {
        await login(customerPage, applicant, "/account");
        await depositMoney(customerPage, {
            amount: 1000,
            description: `Funding for ${transferDescription}`,
        });

        await customerPage.getByRole("link", { name: "Transfer Money" }).click();
        await expect(
            customerPage.getByRole("heading", { name: "Transfer Money" }),
        ).toBeVisible();

        await customerPage
            .getByLabel("Recipient account number")
            .fill(recipientAccountNumber);
        await expect(
            customerPage.getByText(`Account: ${recipientAccountNumber}`),
        ).toBeVisible();
        await expect(
            customerPage.getByRole("button", { name: "Submit Transfer" }),
        ).toBeEnabled();

        await customerPage.getByLabel("Amount").fill("250");
        await customerPage.getByLabel("Description (Optional)").fill(transferDescription);
        await customerPage.getByRole("button", { name: "Submit Transfer" }).click();

        await expect(
            customerPage.getByText("Transfer completed successfully! Redirecting to dashboard..."),
        ).toBeVisible();
    } finally {
        await customerContext.close();
    }
});
