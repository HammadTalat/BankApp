// @ts-check
import { test, expect } from '@playwright/test';


test('test', async ({ page }) => {
    await page.goto('http://localhost:3000/login');
    await page.getByRole('textbox', { name: 'Email address' }).click();
    await page.getByRole('textbox', { name: 'Email address' }).fill('ahmed@gmail.com');
    await page.getByRole('textbox', { name: 'Email address' }).press('Tab');
    await page.getByRole('textbox', { name: 'Password' }).fill('12345678');
    await page.getByRole('button', { name: 'Sign in' }).click();
    await expect(page.getByRole('banner')).toContainText('Welcome back, ahmed');
});