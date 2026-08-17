import { expect, test } from "@playwright/test";

import {
    createApprovedAccountHolder,
    depositMoney,
    login,
} from "../helpers/auth.js";

test("an approved account holder can deposit money and find it in transaction history", async ({
    browser,
}) => {
    const applicant = await createApprovedAccountHolder(browser);

    const customerContext = await browser.newContext();
    const customerPage = await customerContext.newPage();
    const depositDescription = `E2E deposit ${applicant.email}`;

    try {
        await login(customerPage, applicant, "/account");
        await depositMoney(customerPage, {
            amount: 1000,
            description: depositDescription,
        });

        await expect(customerPage.getByText(/Successfully deposited PKR 1,000/)).toBeVisible();
    } finally {
        await customerContext.close();
    }
});
