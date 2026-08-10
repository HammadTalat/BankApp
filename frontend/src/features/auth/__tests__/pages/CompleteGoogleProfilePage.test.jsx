import { render, screen } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router";
import { beforeEach, describe, expect, it, vi } from "vitest";

import CompleteGoogleProfilePage from "../../pages/CompleteGoogleProfilePage";

vi.mock("../../context/useAuth", () => ({
    useAuth: vi.fn(),
}));

import { useAuth } from "../../context/useAuth";

function renderCompleteProfilePage() {
    return render(
        <MemoryRouter initialEntries={["/complete-profile"]}>
            <Routes>
                <Route path="/complete-profile" element={<CompleteGoogleProfilePage />} />
                <Route path="/login" element={<p>login page</p>} />
                <Route path="/application-status" element={<p>application status</p>} />
            </Routes>
        </MemoryRouter>,
    );
}

describe("CompleteGoogleProfilePage", () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it("shows a loading message while restoring the session", () => {
        useAuth.mockReturnValue({
            isInitializing: true,
            isAuthenticated: false,
            user: null,
            finishProfile: vi.fn(),
            signOut: vi.fn(),
        });

        renderCompleteProfilePage();

        expect(screen.getByText(/restoring your session/i)).toBeInTheDocument();
    });

    it("redirects unauthenticated users to login", () => {
        useAuth.mockReturnValue({
            isInitializing: false,
            isAuthenticated: false,
            user: null,
            finishProfile: vi.fn(),
            signOut: vi.fn(),
        });

        renderCompleteProfilePage();

        expect(screen.getByText("login page")).toBeInTheDocument();
    });

    it("shows the profile form when completion is required", () => {
        useAuth.mockReturnValue({
            isInitializing: false,
            isAuthenticated: true,
            user: {
                name: "Ada",
                role: "ACCOUNT_HOLDER",
                needsProfileCompletion: true,
            },
            finishProfile: vi.fn(),
            signOut: vi.fn(),
        });

        renderCompleteProfilePage();

        expect(screen.getByRole("heading", { name: /complete your profile/i })).toBeInTheDocument();
        expect(screen.getByText(/welcome, ada/i)).toBeInTheDocument();
        expect(screen.getByLabelText(/home address/i)).toBeInTheDocument();
    });

    it("redirects users who do not need profile completion", () => {
        useAuth.mockReturnValue({
            isInitializing: false,
            isAuthenticated: true,
            user: {
                name: "Ada",
                role: "ACCOUNT_HOLDER",
                approvalStatus: "PENDING",
                needsProfileCompletion: false,
            },
            finishProfile: vi.fn(),
            signOut: vi.fn(),
        });

        renderCompleteProfilePage();

        expect(screen.getByText("application status")).toBeInTheDocument();
    });
});
