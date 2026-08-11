import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import { DashboardPage } from "./DashboardPage";
import { useAuth } from "../../../auth/context/useAuth.js";
import { dashboardService } from "../api/dashboardService.js";
import { ROUTES } from "../../../../routes/routePaths.js";

// Mock React Router
const mockNavigate = vi.fn();
vi.mock("react-router", () => ({
    useNavigate: () => mockNavigate,
}));

// Mock Route Paths
vi.mock("../../../../routes/routePaths.js", () => ({
    ROUTES: {
        HOME: "/",
        ACCOUNT_TRANSFERS: "/transfers",
        ACCOUNT_TRANSACTIONS: "/transactions",
    },
}));

// Mock Auth Hook
vi.mock("../../../auth/context/useAuth.js", () => ({
    useAuth: vi.fn(),
}));

// Mock Dashboard Service
vi.mock("../api/dashboardService.js", () => ({
    dashboardService: {
        getAccountDetails: vi.fn(),
        getAccountBalance: vi.fn(),
        getRecentTransactions: vi.fn(),
    },
}));

// Mock Sub-Components to keep unit tests focused
vi.mock("../../../../shared/components/navigation/AccountHeader.jsx", () => ({
    default: ({ accountProfile, onLogout }) => (
        <header data-testid="account-header">
            <span>{accountProfile?.name}</span>
            <button onClick={onLogout}>Logout</button>
        </header>
    ),
}));

vi.mock("../../../../shared/components/navigation/AccountSidebar.jsx", () => ({
    default: () => <aside data-testid="account-sidebar">Sidebar</aside>,
}));

describe("DashboardPage Component", () => {
    const mockUser = { name: "Jane Doe", email: "jane@example.com" };
    const mockSignOut = vi.fn();

    const mockAccountDetails = {
        accountNumber: "1234567890123456",
        status: "ACTIVE",
    };

    const mockBalance = { amount: 50000.5 };

    const mockTransactions = {
        transactions: [
            {
                id: "tx-1",
                description: "Salary Deposit",
                indicator: "CREDIT",
                amount: 25000,
                transactionDate: "2026-08-01T10:00:00Z",
            },
            {
                id: "tx-2",
                description: "Utility Bill",
                indicator: "DEBIT",
                amount: 3500,
                transactionDate: "2026-08-05T14:30:00Z",
            },
        ],
    };

    beforeEach(() => {
        vi.clearAllMocks();

        // Default auth hook state
        useAuth.mockReturnValue({
            user: mockUser,
            signOut: mockSignOut,
            loading: false,
        });

        // Default API resolves
        dashboardService.getAccountDetails.mockResolvedValue(mockAccountDetails);
        dashboardService.getAccountBalance.mockResolvedValue(mockBalance);
        dashboardService.getRecentTransactions.mockResolvedValue(mockTransactions);

        // Mock clipboard API
        Object.assign(navigator, {
            clipboard: {
                writeText: vi.fn().mockResolvedValue(undefined),
            },
        });
    });

    afterEach(() => {
        vi.restoreAllMocks();
    });

    it("renders loading state when auth state is loading", () => {
        useAuth.mockReturnValue({
            user: null,
            signOut: mockSignOut,
            loading: true,
        });

        render(<DashboardPage />);

        expect(screen.getByText("Loading session...")).toBeInTheDocument();
        expect(dashboardService.getAccountDetails).not.toHaveBeenCalled();
    });

    it("fetches and renders account details, balance, and recent transactions on mount", async () => {
        render(<DashboardPage />);

        // Verify loading message during initial fetch
        expect(screen.getByText("Loading transactions...")).toBeInTheDocument();

        // Verify header and user greeting
        expect(screen.getByText("Welcome back, Jane Doe")).toBeInTheDocument();

        // Wait for API data to resolve and populate UI
        await waitFor(() => {
            // Check account number formatting (spaced every 4 digits)
            expect(screen.getByText("1234 5678 9012 3456")).toBeInTheDocument();

            // Check formatted balance (PKR 50,000.50)
            expect(screen.getByText(/50,000\.50/)).toBeInTheDocument();

            // Check transaction list items
            expect(screen.getByText("Salary Deposit")).toBeInTheDocument();
            expect(screen.getByText("Utility Bill")).toBeInTheDocument();
            expect(screen.getByText("+ PKR 25,000")).toBeInTheDocument();
            expect(screen.getByText("- PKR 3,500")).toBeInTheDocument();
        });

        expect(dashboardService.getAccountDetails).toHaveBeenCalledTimes(1);
        expect(dashboardService.getAccountBalance).toHaveBeenCalledTimes(1);
        expect(dashboardService.getRecentTransactions).toHaveBeenCalledWith(0, 5);
    });

    it("renders empty transaction state when no transactions exist", async () => {
        dashboardService.getRecentTransactions.mockResolvedValue({ transactions: [] });

        render(<DashboardPage />);

        await waitFor(() => {
            expect(screen.getByText("No recent transactions found.")).toBeInTheDocument();
        });
    });

    it("handles API rejection gracefully during initial load", async () => {
        dashboardService.getAccountDetails.mockRejectedValue(new Error("API Error"));
        dashboardService.getAccountBalance.mockRejectedValue(new Error("API Error"));
        dashboardService.getRecentTransactions.mockRejectedValue(new Error("API Error"));

        render(<DashboardPage />);

        await waitFor(() => {
            expect(screen.getByText("Loading...")).toBeInTheDocument(); // Account number fallback
            expect(screen.getByText(/0\.00/)).toBeInTheDocument(); // Balance fallback
            expect(screen.getByText("No recent transactions found.")).toBeInTheDocument();
        });
    });

    it("refreshes balance when refresh button is clicked", async () => {
        render(<DashboardPage />);

        await waitFor(() => screen.getByText(/50,000\.50/));

        const updatedBalance = { amount: 62000.0 };
        dashboardService.getAccountBalance.mockResolvedValueOnce(updatedBalance);

        const refreshBtn = screen.getByTitle("Refresh Balance");
        fireEvent.click(refreshBtn);

        await waitFor(() => {
            expect(screen.getByText(/62,000\.00/)).toBeInTheDocument();
        });

        expect(dashboardService.getAccountBalance).toHaveBeenCalledTimes(2);
    });

    it("copies formatted account number to clipboard on button click", async () => {
        render(<DashboardPage />);

        await waitFor(() => screen.getByText("1234 5678 9012 3456"));

        const copyBtn = screen.getByRole("button", { name: /copy account number/i });
        fireEvent.click(copyBtn);

        expect(navigator.clipboard.writeText).toHaveBeenCalledWith("1234567890123456");
        expect(screen.getByRole("button", { name: /copied!/i })).toBeInTheDocument();
    });

    it("navigates to transfers page when 'Transfer Money' button is clicked", async () => {
        render(<DashboardPage />);

        const transferBtn = screen.getByRole("button", { name: /transfer money/i });
        fireEvent.click(transferBtn);

        expect(mockNavigate).toHaveBeenCalledWith(ROUTES.ACCOUNT_TRANSFERS);
    });

    it("navigates to transactions page when 'View Transactions' button is clicked", async () => {
        render(<DashboardPage />);

        const viewTxBtn = screen.getByRole("button", { name: /view transactions/i });
        fireEvent.click(viewTxBtn);

        expect(mockNavigate).toHaveBeenCalledWith(ROUTES.ACCOUNT_TRANSACTIONS);
    });

    it("triggers signOut from useAuth on header logout", async () => {
        render(<DashboardPage />);

        const logoutBtn = screen.getByText("Logout");
        fireEvent.click(logoutBtn);

        expect(mockSignOut).toHaveBeenCalledTimes(1);
    });

    it("falls back to clearing localStorage and navigating home if signOut is unavailable", async () => {
        useAuth.mockReturnValue({
            user: mockUser,
            signOut: null,
            loading: false,
        });

        const removeItemSpy = vi.spyOn(Storage.prototype, "removeItem");

        render(<DashboardPage />);

        const logoutBtn = screen.getByText("Logout");
        fireEvent.click(logoutBtn);

        expect(removeItemSpy).toHaveBeenCalledWith("ACCESS_TOKEN");
        expect(mockNavigate).toHaveBeenCalledWith(ROUTES.HOME);
    });
});