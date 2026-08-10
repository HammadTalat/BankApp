import { beforeEach, describe, expect, it, vi } from "vitest";

vi.mock("../../../../api/apiConfig", () => ({
    API_BASE_URL: "http://api.test",
}));

vi.mock("../../../../api/httpClient", () => ({
    httpClient: {
        get: vi.fn(),
        post: vi.fn(),
    },
}));

import { httpClient } from "../../../../api/httpClient";
import {
    completeProfile,
    getCurrentUser,
    getGoogleLoginUrl,
    login,
    logout,
    signup,
} from "../../api/authApi";

describe("authApi", () => {
    beforeEach(() => {
        vi.clearAllMocks();
        global.fetch = vi.fn();
    });

    it("signup posts to the auth signup endpoint", async () => {
        const payload = { email: "a@b.com", password: "secret" };
        httpClient.post.mockResolvedValue({ ok: true });

        await expect(signup(payload)).resolves.toEqual({ ok: true });
        expect(httpClient.post).toHaveBeenCalledWith("/api/v1/auth/signup", payload);
    });

    it("login sends form-encoded credentials and returns data", async () => {
        global.fetch.mockResolvedValue({
            ok: true,
            json: async () => ({ email: "a@b.com" }),
        });

        await expect(login({ email: "a@b.com", password: "secret" })).resolves.toEqual({
            email: "a@b.com",
        });

        expect(global.fetch).toHaveBeenCalledWith(
            "http://api.test/api/v1/auth/login",
            expect.objectContaining({
                method: "POST",
                credentials: "include",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
                },
            }),
        );

        const body = global.fetch.mock.calls[0][1].body;
        expect(body.toString()).toBe("username=a%40b.com&password=secret");
    });

    it("login throws with a message when the response is not ok", async () => {
        global.fetch.mockResolvedValue({
            ok: false,
            status: 401,
            json: async () => ({ message: "Bad credentials" }),
        });

        await expect(login({ email: "a@b.com", password: "wrong" })).rejects.toMatchObject({
            message: "Bad credentials",
            status: 401,
        });
    });

    it("getCurrentUser, logout, and completeProfile call httpClient", async () => {
        httpClient.get.mockResolvedValue({ email: "a@b.com" });
        httpClient.post.mockResolvedValue({ ok: true });

        await expect(getCurrentUser()).resolves.toEqual({ email: "a@b.com" });
        await expect(logout()).resolves.toEqual({ ok: true });
        await expect(completeProfile("123 Main St")).resolves.toEqual({ ok: true });

        expect(httpClient.get).toHaveBeenCalledWith("/api/v1/me");
        expect(httpClient.post).toHaveBeenCalledWith("/api/v1/auth/logout");
        expect(httpClient.post).toHaveBeenCalledWith("/api/v1/me/complete-profile", {
            address: "123 Main St",
        });
    });

    it("getGoogleLoginUrl returns the OAuth authorization URL", () => {
        expect(getGoogleLoginUrl()).toBe("http://api.test/oauth2/authorization/google");
    });
});
