
import { test, expect } from '@playwright/test';

test('test', async ({ page }) => {
    await page.goto('http://localhost:3000/login');
    await page.getByRole('textbox', { name: 'Email address' }).click();
    await page.getByRole('textbox', { name: 'Email address' }).fill('ahmed@gmail.com');
    await page.getByRole('textbox', { name: 'Email address' }).press('Tab');
    await page.getByRole('textbox', { name: 'Password' }).fill('12345678');
    await page.getByRole('textbox', { name: 'Password' }).press('Enter');
    await page.getByRole('button', { name: 'Sign in' }).click();
    await page.getByRole('link', { name: 'Deposit Money' }).click();
    await page.getByRole('spinbutton', { name: 'Deposit Amount (PKR)' }).click();
    await page.getByRole('spinbutton', { name: 'Deposit Amount (PKR)' }).fill('1000');
    await page.getByRole('main').click();
    await page.getByRole('button', { name: 'Confirm Deposit' }).click();
    await page.getByText('Successfully deposited PKR 1,').click();
    await expect(page.getByRole('main')).toContainText('Successfully deposited PKR 1,000.00 into your account.');
});