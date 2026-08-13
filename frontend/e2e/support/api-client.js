import { config } from "./config.js";

function getSetCookieHeaders(response) {
    if (typeof response.headers.getSetCookie === "function") {
        return response.headers.getSetCookie();
    }

    const singleHeader = response.headers.get("set-cookie");
    return singleHeader ? [singleHeader] : [];
}

function extractCookies(setCookieHeaders = []) {
    return setCookieHeaders
        .map((header) => header.split(";")[0])
        .join("; ");
}

async function loginViaApi(email, password) {
    const body = new URLSearchParams({
        username: email,
        password,
    });

    const response = await fetch(`${config.apiBaseUrl}/api/v1/auth/login`, {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
        },
        body,
    });

    if (!response.ok) {
        const errorBody = await response.text();
        throw new Error(`API login failed for ${email}: ${response.status} ${errorBody}`);
    }

    const cookieHeader = getSetCookieHeaders(response);
    return extractCookies(cookieHeader);
}

export async function signupViaApi(user) {
    const response = await fetch(`${config.apiBaseUrl}/api/v1/auth/signup`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            name: user.name,
            email: user.email,
            address: user.address,
            password: user.password,
        }),
    });

    if (!response.ok) {
        const errorBody = await response.text();
        throw new Error(`Signup failed: ${response.status} ${errorBody}`);
    }
}

export async function approveUserViaApi(email) {
    if (!config.adminPassword) {
        throw new Error(
            "E2E_ADMIN_PASSWORD is required to approve users. "
            + "Set it in frontend/e2e/.env or your shell environment.",
        );
    }

    const adminCookie = await loginViaApi(config.adminEmail, config.adminPassword);

    const pendingResponse = await fetch(
        `${config.apiBaseUrl}/api/v1/admin/users?approvalStatus=PENDING`,
        {
            headers: { Cookie: adminCookie },
        },
    );

    if (!pendingResponse.ok) {
        const errorBody = await pendingResponse.text();
        throw new Error(`Failed to list pending users: ${pendingResponse.status} ${errorBody}`);
    }

    const pendingUsers = await pendingResponse.json();
    const targetUser = pendingUsers.find((user) => user.email === email);

    if (!targetUser) {
        throw new Error(`No pending user found with email ${email}`);
    }

    const approveResponse = await fetch(
        `${config.apiBaseUrl}/api/v1/admin/users/${targetUser.id}/approve`,
        {
            method: "POST",
            headers: { Cookie: adminCookie },
        },
    );

    if (!approveResponse.ok) {
        const errorBody = await approveResponse.text();
        throw new Error(`Failed to approve user ${email}: ${approveResponse.status} ${errorBody}`);
    }

    return approveResponse.json();
}

export async function createApprovedAccountHolder() {
    const uniqueSuffix = `${Date.now()}-${Math.floor(Math.random() * 10_000)}`;
    const user = {
        name: `E2E User ${uniqueSuffix}`,
        email: `e2e.user.${uniqueSuffix}@redmath.test`,
        address: config.defaultAddress,
        password: config.defaultPassword,
    };

    await signupViaApi(user);
    await approveUserViaApi(user.email);

    return user;
}

export async function createPendingAccountHolder() {
    const uniqueSuffix = `${Date.now()}-${Math.floor(Math.random() * 10_000)}`;
    const user = {
        name: `Pending User ${uniqueSuffix}`,
        email: `e2e.pending.${uniqueSuffix}@redmath.test`,
        address: config.defaultAddress,
        password: config.defaultPassword,
    };

    await signupViaApi(user);
    return user;
}
