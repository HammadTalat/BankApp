import { API_BASE_URL } from "../../../api/apiConfig";
import { httpClient } from "../../../api/httpClient";

const AUTH_BASE_PATH = "/api/v1/auth";

function toMessage(data, fallback) {
    return typeof data === "string"
        ? data
        : data?.message || fallback;
}

export function signup(payload) {
    return httpClient.post(`${AUTH_BASE_PATH}/signup`, payload);
}

export async function login({ email, password }) {
    const body = new URLSearchParams({
        username: email,
        password,
    });
    const response = await fetch(`${API_BASE_URL}${AUTH_BASE_PATH}/login`, {
        method: "POST",
        body,
        credentials: "include",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
        },
    });
    const data = await response.json().catch(() => null);

    if (!response.ok) {
        const error = new Error(toMessage(data, "Unable to sign in."));
        error.status = response.status;
        error.data = data;
        throw error;
    }

    return data;
}

export function getCurrentUser() {
    return httpClient.get("/api/v1/me");
}

export function logout() {
    return httpClient.post(`${AUTH_BASE_PATH}/logout`);
}

export function completeProfile(address) {
    return httpClient.post("/api/v1/me/complete-profile", { address });
}

export function getGoogleLoginUrl() {
    return `${API_BASE_URL}/oauth2/authorization/google`;
}
