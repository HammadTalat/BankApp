import { render, screen, waitFor, fireEvent } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import TransferPage from "./TransferPage.jsx";
import { useAuth } from "../../../auth/context/useAuth.js";
import { transferService } from "../api/transferService.js";

const mockNavigate = vi.fn();

vi.mock("react-router", () => ({
    useNavigate: () => mockNavigate,
}));

vi.mock("../../../auth/context/useAuth.js", () => ({
    useAuth: vi.fn(),
}));

vi.mock("../api/transferService.js", () => ({
    transferService: {
        getAccountDetails: vi.fn(),
        lookupRecipient: vi.fn(),
        transferFunds: vi.fn(),
        logout: vi.fn(),
    },
}));

vi.mock("../../../../routes/routePaths.js", () => ({
    ROUTES: {
        HOME: "/",
        ACCOUNT_HOME: "/account",
    },
}));

vi.mock("../../../../shared/components/navigation/AccountHeader.jsx", () => ({
    default: ({ onLogout }) => (
        <div data-testid="account-header">
            <button onClick={onLogout}>Logout</button>
        </div>
    ),
}));

vi.mock("../../../../shared/components/navigation/AccountSidebar.jsx", () => ({
    default: () => <div data-testid="account-sidebar" />,
}));

describe("TransferPage Component", () => {
    const mockUser = { name: "Jane Doe", email: "jane@example.com" };
    const mockSenderAccount = "1234567890123456";

    beforeEach(() => {
        vi.clearAllMocks();
        localStorage.clear();

        useAuth.mockReturnValue({
            user: mockUser,
        });

        transferService.getAccountDetails.mockResolvedValue({
            accountNumber: mockSenderAccount,
        });
    });

    afterEach(() => {
        vi.useRealTimers();
    });

    it("fetches sender account details on mount and renders sender info banner", async () => {
        render(<TransferPage />);

        await waitFor(() => {
            expect(transferService.getAccountDetails).toHaveBeenCalledTimes(1);
            expect(
                screen.getByText("Transferring from account:")
            ).toBeInTheDocument();
            expect(screen.getByText(mockSenderAccount)).toBeInTheDocument();
        });
    });

    it("displays error banner when fetching sender account details fails", async () => {
        transferService.getAccountDetails.mockRejectedValueOnce(
            new Error("Failed to load account details.")
        );

        render(<TransferPage />);

        await waitFor(() => {
            expect(
                screen.getByText("Failed to load account details.")
            ).toBeInTheDocument();
        });
    });


    it("shows error when entering own account number as recipient", async () => {
        render(<TransferPage />);

        await waitFor(() => {
            expect(screen.getByText(mockSenderAccount)).toBeInTheDocument();
        });

        const recipientInput = screen.getByPlaceholderText(
            /Enter 16-digit account number/i
        );

        fireEvent.change(recipientInput, {
            target: { value: mockSenderAccount },
        });

        await waitFor(() => {
            expect(
                screen.getByText("You cannot transfer money to your own account.")
            ).toBeInTheDocument();
        });

        expect(transferService.lookupRecipient).not.toHaveBeenCalled();
    });


    it("executes logout flow and redirects to HOME", async () => {
        const user = userEvent.setup();
        localStorage.setItem("ACCESS_TOKEN", "mock-token");
        transferService.logout.mockResolvedValueOnce({});

        render(<TransferPage />);

        await waitFor(() => {
            expect(screen.getByText(mockSenderAccount)).toBeInTheDocument();
        });

        const logoutBtn = screen.getByRole("button", { name: /Logout/i });
        await user.click(logoutBtn);

        expect(transferService.logout).toHaveBeenCalledTimes(1);
        expect(localStorage.getItem("ACCESS_TOKEN")).toBeNull();
        expect(mockNavigate).toHaveBeenCalledWith("/");
    });
});
