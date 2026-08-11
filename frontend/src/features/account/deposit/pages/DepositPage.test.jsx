import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, it, expect, vi, beforeEach } from "vitest";
import { DepositPage } from "./DepositPage.jsx";
import { useAuth } from "../../../auth/context/useAuth.js";
import { depositService } from "../api/depositService.js";

const mockNavigate = vi.fn();

// Mock dependencies
vi.mock("react-router", () => ({
    useNavigate: () => mockNavigate,
}));

vi.mock("../../../auth/context/useAuth.js", () => ({
    useAuth: vi.fn(),
}));

vi.mock("../api/depositService.js", () => ({
    depositService: {
        getAccountDetails: vi.fn(),
        processDeposit: vi.fn(),
    },
}));

vi.mock("../../../../routes/routePaths.js", () => ({
    ROUTES: {
        HOME: "/",
        ACCOUNT_HOME: "/dashboard",
    },
}));

// Mock layout sub-components
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

describe("DepositPage Component", () => {
    const mockUser = { name: "John Doe", email: "john@example.com" };
    const mockSignOut = vi.fn();

    beforeEach(() => {
        vi.clearAllMocks();
        useAuth.mockReturnValue({
            user: mockUser,
            signOut: mockSignOut,
            loading: false,
        });
        depositService.getAccountDetails.mockResolvedValue({
            accountNumber: "PK1234567890",
        });
    });

    it("renders loading state when auth state is loading", () => {
        useAuth.mockReturnValue({
            user: null,
            signOut: mockSignOut,
            loading: true,
        });

        render(<DepositPage />);

        expect(screen.getByText("Loading session...")).toBeInTheDocument();
    });

    it("fetches and renders account details on mount", async () => {
        render(<DepositPage />);

        const accountInput = screen.getByLabelText(/Destination Account/i);
        expect(accountInput).toHaveValue("Loading account details...");

        await waitFor(() => {
            expect(depositService.getAccountDetails).toHaveBeenCalledTimes(1);
            expect(accountInput).toHaveValue("PK1234567890");
        });
    });

    it("displays error message if fetching account details fails", async () => {
        depositService.getAccountDetails.mockRejectedValueOnce(
            new Error("Network Error")
        );

        render(<DepositPage />);

        await waitFor(() => {
            expect(
                screen.getByText(
                    "Unable to retrieve account details. Please try reloading."
                )
            ).toBeInTheDocument();
        });
    });

    it("populates deposit amount input when preset amount buttons are clicked", async () => {
        const user = userEvent.setup();
        render(<DepositPage />);

        await waitFor(() => {
            expect(screen.getByLabelText(/Destination Account/i)).toHaveValue("PK1234567890");
        });

        const presetButton = screen.getByRole("button", { name: /\+ PKR 5,000/i });
        await user.click(presetButton);

        const amountInput = screen.getByLabelText(/Deposit Amount \(PKR\)/i);
        expect(amountInput).toHaveValue(5000);
    });

    it("submits deposit successfully and shows success message", async () => {
        const user = userEvent.setup();
        depositService.processDeposit.mockResolvedValueOnce({
            message: "Successfully deposited PKR 10,000.00 into your account.",
        });

        render(<DepositPage />);

        await waitFor(() => {
            expect(screen.getByLabelText(/Destination Account/i)).toHaveValue("PK1234567890");
        });

        const amountInput = screen.getByLabelText(/Deposit Amount \(PKR\)/i);
        const descriptionInput = screen.getByLabelText(/Transaction Description/i);
        const submitBtn = screen.getByRole("button", { name: /Confirm Deposit/i });

        await user.type(amountInput, "10000");
        await user.clear(descriptionInput);
        await user.type(descriptionInput, "Monthly Savings");
        await user.click(submitBtn);

        expect(depositService.processDeposit).toHaveBeenCalledWith({
            accountNumber: "PK1234567890",
            amount: 10000,
            description: "Monthly Savings",
        });

        await waitFor(() => {
            expect(
                screen.getByText("Successfully deposited PKR 10,000.00 into your account.")
            ).toBeInTheDocument();
        });

        expect(amountInput).toHaveValue(null);
    });

    it("displays error banner when processDeposit API fails", async () => {
        const user = userEvent.setup();
        depositService.processDeposit.mockRejectedValueOnce({
            response: { data: { message: "Account locked" } },
        });

        render(<DepositPage />);

        await waitFor(() => {
            expect(screen.getByLabelText(/Destination Account/i)).toHaveValue("PK1234567890");
        });

        const amountInput = screen.getByLabelText(/Deposit Amount \(PKR\)/i);
        const submitBtn = screen.getByRole("button", { name: /Confirm Deposit/i });

        await user.type(amountInput, "500");
        await user.click(submitBtn);

        await waitFor(() => {
            expect(screen.getByText("Account locked")).toBeInTheDocument();
        });
    });

    it("navigates back to dashboard when clicking 'Back to Dashboard' button", async () => {
        const user = userEvent.setup();
        render(<DepositPage />);

        const backBtn = screen.getByRole("button", { name: /Back to Dashboard/i });
        await user.click(backBtn);

        expect(mockNavigate).toHaveBeenCalledWith("/dashboard");
    });

    it("calls signOut when logging out via AccountHeader", async () => {
        const user = userEvent.setup();
        render(<DepositPage />);

        const logoutBtn = screen.getByRole("button", { name: /Logout/i });
        await user.click(logoutBtn);

        expect(mockSignOut).toHaveBeenCalledTimes(1);
    });
});