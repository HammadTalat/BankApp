function readBoolean(value, defaultValue) {
    if (value === undefined || value === null || value === "") {
        return defaultValue;
    }

    return String(value).toLowerCase() === "true";
}

export const config = {
    baseUrl: process.env.E2E_BASE_URL || "http://localhost:5173",
    apiBaseUrl: process.env.E2E_API_BASE_URL || "http://localhost:8081",
    adminEmail: process.env.E2E_ADMIN_EMAIL || "admin@bank.local",
    adminPassword: process.env.E2E_ADMIN_PASSWORD || "",
    headless: readBoolean(process.env.E2E_HEADLESS, true),
    chatTimeoutMs: Number(process.env.E2E_CHAT_TIMEOUT_MS || 180000),
    stepTimeoutMs: Number(process.env.E2E_STEP_TIMEOUT_MS || 240000),
    defaultPassword: "Password123!",
    defaultAddress: "123 Test Street, Karachi, Pakistan",
};
