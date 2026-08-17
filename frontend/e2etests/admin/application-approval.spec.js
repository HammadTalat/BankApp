import { expect, test } from "@playwright/test";

import {
    approveApplicant,
    createApplicant,
    login,
    signUp,
} from "../helpers/auth.js";

test("a new account holder is pending until an administrator approves the application", async ({
    browser,
}) => {
    const applicant = createApplicant();
    const applicantContext = await browser.newContext();
    const applicantPage = await applicantContext.newPage();

    try {
        await signUp(applicantPage, applicant);
        await login(applicantPage, applicant, "/application-status");

        await expect(
            applicantPage.getByText("Your application is under review"),
        ).toBeVisible();

        await approveApplicant(browser, applicant);
    } finally {
        await applicantContext.close();
    }

    const approvedContext = await browser.newContext();
    const approvedPage = await approvedContext.newPage();

    try {
        await login(approvedPage, applicant, "/account");
        await expect(
            approvedPage.getByRole("link", { name: "Deposit Money" }),
        ).toBeVisible();
    } finally {
        await approvedContext.close();
    }
});
