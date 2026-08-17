import { expect, test } from "@playwright/test";

import {
    createApprovedAccountHolder,
    depositMoney,
    login,
} from "../helpers/auth.js";

test("transaction history shows a deposit and applies credit and debit filters", async ({
    browser,
}) => {
    const applicant = await createApprovedAccountHolder(browser);
    const customerContext = await browser.newContext();
    const customerPage = await customerContext.newPage();
    const description = `E2E transaction ${applicant.email}`;

    try {
        await login(customerPage, applicant, "/account");
        await depositMoney(customerPage, { amount: 1000, description });

        await customerPage.getByRole("link", { name: "Transactions" }).click();
        await expect(
            customerPage.getByRole("heading", { name: "Transactions" }),
        ).toBeVisible();
        await expect(customerPage.getByText(description)).toBeVisible();
        await expect(customerPage.getByText("CREDIT", { exact: true })).toBeVisible();

        await customerPage.getByLabel("Type:").selectOption("DEBIT");
        await customerPage.getByRole("button", { name: "Apply filters" }).click();
        await expect(
            customerPage.getByText("No transactions found matching your criteria."),
        ).toBeVisible();

        await customerPage.getByLabel("Type:").selectOption("CREDIT");
        await customerPage.getByRole("button", { name: "Apply filters" }).click();
        await expect(customerPage.getByText(description)).toBeVisible();
    } finally {
        await customerContext.close();
    }
});
