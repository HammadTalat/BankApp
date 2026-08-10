import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";

vi.mock("../../api/authApi", () => ({
    login: vi.fn(),
    logout: vi.fn(),
    signup: vi.fn(),
    getCurrentUser: vi.fn(),
    completeProfile: vi.fn(),
}));

import * as authApi from "../../api/authApi";
import { AuthProvider } from "../../context/AuthContext";
import { useAuth } from "../../context/useAuth";

function AuthProbe() {
    const {
        user,
        isAuthenticated,
        isInitializing,
        signIn,
        signOut,
        register,
        finishProfile,
    } = useAuth();

    if (isInitializing) {
        return <p>loading</p>;
    }

    return (
        <div>
            <p data-testid="auth-state">
                {isAuthenticated ? `signed-in:${user.email}` : "signed-out"}
            </p>
            <p data-testid="profile-flag">
                {String(Boolean(user?.needsProfileCompletion))}
            </p>
            <button type="button" onClick={() => signIn({ email: "a@b.com", password: "x" })}>
                sign-in
            </button>
            <button type="button" onClick={() => signOut()}>
                sign-out
            </button>
            <button
                type="button"
                onClick={() => register({ email: "a@b.com", password: "secret12" })}
            >
                register
            </button>
            <button type="button" onClick={() => finishProfile("123 Main St")}>
                finish-profile
            </button>
        </div>
    );
}

describe("AuthProvider", () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it("restores a session on mount", async () => {
        authApi.getCurrentUser.mockResolvedValue({
            email: "a@b.com",
            name: "Ada",
            role: "ACCOUNT_HOLDER",
            address: "123 Main St",
            approvalStatus: "APPROVED",
        });

        render(
            <AuthProvider>
                <AuthProbe />
            </AuthProvider>,
        );

        expect(screen.getByText("loading")).toBeInTheDocument();
        expect(await screen.findByTestId("auth-state")).toHaveTextContent("signed-in:a@b.com");
        expect(screen.getByTestId("profile-flag")).toHaveTextContent("false");
    });

    it("marks account holders with missing address as needing profile completion", async () => {
        authApi.getCurrentUser.mockResolvedValue({
            email: "a@b.com",
            name: "Ada",
            role: "ACCOUNT_HOLDER",
            address: "not provided",
            approvalStatus: "PENDING",
        });

        render(
            <AuthProvider>
                <AuthProbe />
            </AuthProvider>,
        );

        expect(await screen.findByTestId("profile-flag")).toHaveTextContent("true");
    });

    it("signs in, registers, finishes profile, and signs out", async () => {
        const user = userEvent.setup();

        authApi.getCurrentUser
            .mockRejectedValueOnce(Object.assign(new Error("Unauthorized"), { status: 401 }))
            .mockResolvedValue({
                email: "a@b.com",
                name: "Ada",
                role: "ACCOUNT_HOLDER",
                address: "123 Main St",
                approvalStatus: "APPROVED",
            });
        authApi.login.mockResolvedValue({ email: "a@b.com" });
        authApi.signup.mockResolvedValue({ ok: true });
        authApi.completeProfile.mockResolvedValue({
            email: "a@b.com",
            name: "Ada",
            role: "ACCOUNT_HOLDER",
            address: "123 Main St",
            approvalStatus: "PENDING",
        });
        authApi.logout.mockResolvedValue({});

        render(
            <AuthProvider>
                <AuthProbe />
            </AuthProvider>,
        );

        expect(await screen.findByTestId("auth-state")).toHaveTextContent("signed-out");

        await user.click(screen.getByRole("button", { name: "register" }));
        expect(authApi.signup).toHaveBeenCalledWith({
            email: "a@b.com",
            password: "secret12",
        });

        await user.click(screen.getByRole("button", { name: "sign-in" }));
        await waitFor(() => {
            expect(screen.getByTestId("auth-state")).toHaveTextContent("signed-in:a@b.com");
        });
        expect(authApi.login).toHaveBeenCalledWith({ email: "a@b.com", password: "x" });

        await user.click(screen.getByRole("button", { name: "finish-profile" }));
        expect(authApi.completeProfile).toHaveBeenCalledWith("123 Main St");
        await waitFor(() => {
            expect(screen.getByTestId("profile-flag")).toHaveTextContent("false");
        });

        await user.click(screen.getByRole("button", { name: "sign-out" }));
        await waitFor(() => {
            expect(screen.getByTestId("auth-state")).toHaveTextContent("signed-out");
        });
        expect(authApi.logout).toHaveBeenCalled();
    });
});

describe("useAuth", () => {
    it("throws when used outside AuthProvider", () => {
        expect(() => render(<AuthProbe />)).toThrow(
            "useAuth must be used within an AuthProvider.",
        );
    });
});
