import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router";
import { beforeEach, describe, expect, it, vi } from "vitest";

import ApplicationStatusPage from "./ApplicationStatusPage";

const mockUseAuth = vi.fn();

vi.mock("../../auth/context/useAuth", () => ({
    useAuth: () => mockUseAuth(),
}));

function renderPage() {
    return render(
        <MemoryRouter>
            <ApplicationStatusPage />
        </MemoryRouter>,
    );
}

describe("ApplicationStatusPage", () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it("loads and displays a pending application", async () => {
        const refreshProfile = vi.fn().mockResolvedValue({});
        mockUseAuth.mockReturnValue({
            user: {
                name: "Ayesha Khan",
                email: "ayesha@example.com",
                role: "ACCOUNT_HOLDER",
                approvalStatus: "PENDING",
            },
            refreshProfile,
        });

        renderPage();

        expect(screen.getByText("Checking your application status...")).toBeInTheDocument();
        expect(await screen.findByText("Your application is under review")).toBeInTheDocument();
        expect(refreshProfile).toHaveBeenCalledOnce();
    });

    it("shows a retry option when the profile cannot be loaded", async () => {
        const refreshProfile = vi.fn().mockRejectedValue(new Error("Service unavailable"));
        const user = userEvent.setup();
        mockUseAuth.mockReturnValue({ user: null, refreshProfile });

        renderPage();

        expect(await screen.findByText("Service unavailable")).toBeInTheDocument();

        await user.click(screen.getByRole("button", { name: "Retry" }));

        await waitFor(() => expect(refreshProfile).toHaveBeenCalledTimes(2));
    });
});
