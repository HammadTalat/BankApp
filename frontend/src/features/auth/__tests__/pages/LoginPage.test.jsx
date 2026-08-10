import { render, screen } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router";
import { beforeEach, describe, expect, it, vi } from "vitest";

import LoginPage from "../../pages/LoginPage";

vi.mock("../../context/useAuth", () => ({
    useAuth: vi.fn(),
}));

import { useAuth } from "../../context/useAuth";

function renderLoginPage() {
    return render(
        <MemoryRouter initialEntries={["/login"]}>
            <Routes>
                <Route path="/login" element={<LoginPage />} />
                <Route path="/account" element={<p>account home</p>} />
            </Routes>
        </MemoryRouter>,
    );
}

describe("LoginPage", () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it("shows a loading message while restoring the session", () => {
        useAuth.mockReturnValue({
            isInitializing: true,
            isAuthenticated: false,
            signIn: vi.fn(),
            user: null,
        });

        renderLoginPage();

        expect(screen.getByText(/restoring your session/i)).toBeInTheDocument();
    });

    it("shows the login form when the user is signed out", () => {
        useAuth.mockReturnValue({
            isInitializing: false,
            isAuthenticated: false,
            signIn: vi.fn(),
            user: null,
        });

        renderLoginPage();

        expect(screen.getByRole("heading", { name: /sign in to your account/i })).toBeInTheDocument();
        expect(screen.getByRole("button", { name: /sign in/i })).toBeInTheDocument();
    });

    it("redirects authenticated users to their post-auth route", () => {
        useAuth.mockReturnValue({
            isInitializing: false,
            isAuthenticated: true,
            signIn: vi.fn(),
            user: {
                role: "ACCOUNT_HOLDER",
                approvalStatus: "APPROVED",
                needsProfileCompletion: false,
            },
        });

        renderLoginPage();

        expect(screen.getByText("account home")).toBeInTheDocument();
    });
});
