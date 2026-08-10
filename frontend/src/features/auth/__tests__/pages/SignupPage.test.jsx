import { render, screen } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router";
import { beforeEach, describe, expect, it, vi } from "vitest";

import SignupPage from "../../pages/SignupPage";

vi.mock("../../context/useAuth", () => ({
    useAuth: vi.fn(),
}));

import { useAuth } from "../../context/useAuth";

function renderSignupPage() {
    return render(
        <MemoryRouter initialEntries={["/signup"]}>
            <Routes>
                <Route path="/signup" element={<SignupPage />} />
                <Route path="/account" element={<p>account home</p>} />
            </Routes>
        </MemoryRouter>,
    );
}

describe("SignupPage", () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it("shows a loading message while restoring the session", () => {
        useAuth.mockReturnValue({
            isInitializing: true,
            isAuthenticated: false,
            register: vi.fn(),
            user: null,
        });

        renderSignupPage();

        expect(screen.getByText(/restoring your session/i)).toBeInTheDocument();
    });

    it("shows the signup form when the user is signed out", () => {
        useAuth.mockReturnValue({
            isInitializing: false,
            isAuthenticated: false,
            register: vi.fn(),
            user: null,
        });

        renderSignupPage();

        expect(screen.getByRole("heading", { name: /banking starts here/i })).toBeInTheDocument();
        expect(screen.getByRole("button", { name: /create account/i })).toBeInTheDocument();
    });

    it("redirects authenticated users to their post-auth route", () => {
        useAuth.mockReturnValue({
            isInitializing: false,
            isAuthenticated: true,
            register: vi.fn(),
            user: {
                role: "ACCOUNT_HOLDER",
                approvalStatus: "APPROVED",
                needsProfileCompletion: false,
            },
        });

        renderSignupPage();

        expect(screen.getByText("account home")).toBeInTheDocument();
    });
});
