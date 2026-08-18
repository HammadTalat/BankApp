/* global process */
// @ts-check
import { defineConfig, devices } from '@playwright/test';

const interactiveMode = process.env.E2E_HEADED === 'true';

/**
 * Read environment variables from file.
 * https://github.com/motdotla/dotenv
 */


/**
 * @see https://playwright.dev/docs/test-configuration
 */
export default defineConfig({
  testDir: './e2etests',
  // A workflow creates a user, signs in twice, gets admin approval, and may
  // post a transaction. Slow headed mode intentionally makes this longer.
  timeout: 90_000,
  /*
   * These tests create users, approve applications, and post transactions.
   * Keep the local suite serial until it runs against an isolated E2E database.
   */
  fullyParallel: false,
  /* Fail the build on CI if you accidentally left test.only in the source code. */
  forbidOnly: !!process.env.CI,
  /* Retry on CI only */
  retries: process.env.CI ? 2 : 0,
  workers: 1,
  /* Reporter to use. See https://playwright.dev/docs/test-reporters */
  reporter: 'html',
  /* Shared settings for all the projects below. See https://playwright.dev/docs/api/class-testoptions. */
  use: {

    baseURL: process.env.E2E_BASE_URL || 'http://localhost:5173',
    headless: !interactiveMode,
    launchOptions: interactiveMode ? { slowMo: 600 } : undefined,

    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
  },

  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },

    /* Test against mobile viewports. */
    // {
    //   name: 'Mobile Chrome',
    //   use: { ...devices['Pixel 5'] },
    // },
    // {
    //   name: 'Mobile Safari',
    //   use: { ...devices['iPhone 12'] },
    // },

    /* Test against branded browsers. */
    // {
    //   name: 'Microsoft Edge',
    //   use: { ...devices['Desktop Edge'], channel: 'msedge' },
    // },
    // {
    //   name: 'Google Chrome',
    //   use: { ...devices['Desktop Chrome'], channel: 'chrome' },
    // },
  ],

  webServer: process.env.E2E_BASE_URL
    ? undefined
    : {
        command: 'npm run dev -- --host 127.0.0.1',
        url: 'http://localhost:5173',
        reuseExistingServer: !process.env.CI,
      },
});
