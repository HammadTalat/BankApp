import { expect, test } from "@playwright/test";

import { adminCredentials, login } from "../helpers/auth.js";

test("an administrator can sign in and view dashboard metrics", async ({ page }) => {
    await login(page, adminCredentials, "/admin");

    await expect(page.getByRole("heading", { name: "Dashboard" })).toBeVisible();
    await expect(page.getByText("Pending Users", { exact: true })).toBeVisible();
    await expect(page.getByText("Total Accounts", { exact: true })).toBeVisible();
});

test("invalid credentials keep the visitor on the login page", async ({ page }) => {
    await page.goto("/login");
    await page.getByLabel("Email address").fill("unknown@redmath.test");
    await page.getByLabel("Password", { exact: true }).fill("not-the-password");
    await page.getByRole("button", { name: "Sign in" }).click();

    await expect(page).toHaveURL(/\/login$/);
    await expect(page.getByRole("alert")).toBeVisible();
});
