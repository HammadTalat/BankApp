import { expect, test } from "@playwright/test";

import { adminCredentials, login } from "../helpers/auth.js";

test("an administrator can sign in and view dashboard metrics", async ({ page }) => {
    await login(page, adminCredentials, "/admin");

    const dashboard = page.getByRole("main");
    await expect(dashboard.getByRole("heading", { name: "Dashboard" })).toBeVisible();
    await expect(dashboard.getByText("Pending Users", { exact: true })).toBeVisible();
    await expect(dashboard.getByText("Total Accounts", { exact: true })).toBeVisible();
});

test("invalid credentials keep the visitor on the login page", async ({ page }) => {
    await page.goto("/login");
    await page.getByLabel("Email address").fill("unknown@redmath.test");
    await page.getByLabel("Password", { exact: true }).fill("not-the-password");
    await page.getByRole("button", { name: "Sign in" }).click();

    await expect(page).toHaveURL(/\/login$/);
    await expect(page.getByRole("alert")).toBeVisible();
});
