/* global process */
// @ts-check
import { defineConfig, devices } from '@playwright/test';

const interactiveMode = process.env.E2E_HEADED === 'true';

/**
 * Read environment variables from file.
 * https://github.com/motdotla/dotenv
 */
// import dotenv from 'dotenv';
// import path from 'path';
// dotenv.config({ path: path.resolve(__dirname, '.env') });

/**
 * @see https://playwright.dev/docs/test-configuration
 */
export default defineConfig({
  testDir: './e2etests',
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
    /* Vite runs locally on 5173; override for a Docker/Nginx environment. */
    // API_BASE_URL defaults to http://localhost:8081, whose CORS policy allows
    // http://localhost:5173. Do not substitute 127.0.0.1 here.
    baseURL: process.env.E2E_BASE_URL || 'http://localhost:5173',
    // Use `E2E_HEADED=true` locally to watch each browser action.
    headless: !interactiveMode,
    launchOptions: interactiveMode ? { slowMo: 600 } : undefined,

    /* Keep useful evidence when a browser flow fails. */
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
  },

  /* Configure projects for major browsers */
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

  /* Start Vite for local E2E work. BankApp and the E2E database must be running. */
  webServer: process.env.E2E_BASE_URL
    ? undefined
    : {
        command: 'npm run dev -- --host 127.0.0.1',
        url: 'http://localhost:5173',
        reuseExistingServer: !process.env.CI,
      },
});
