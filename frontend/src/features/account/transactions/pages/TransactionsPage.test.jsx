import { render, screen, waitFor, fireEvent } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, it, expect, vi, beforeEach } from "vitest";
import { TransactionsPage } from "./TransactionsPage.jsx";
import { useAuth } from "../../../auth/context/useAuth.js";
import { transactionService } from "../api/transactionService.js";

const mockNavigate = vi.fn();

// Mock dependencies
vi.mock("react-router", () => ({
    useNavigate: () => mockNavigate,
}));

vi.mock("../../../auth/context/useAuth.js", () => ({
    useAuth: vi.fn(),
}));

vi.mock("../api/transactionService.js", () => ({
    transactionService: {
        getTransactions: vi.fn(),
        logout: vi.fn(),
    },
}));

vi.mock("../../../../routes/routePaths.js", () => ({
    ROUTES: {
        HOME: "/",
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

describe("TransactionsPage Component", () => {
    const mockUser = { name: "Jane Doe", email: "jane@example.com" };

    const mockTransactions = [
        {
            id: "tx-1",
            operationId: "op-1",
            transactionDate: "2026-03-15T10:00:00Z",
            description: "Salary Deposit",
            accountId: "PK9876543210",
            indicator: "CREDIT",
            amount: 50000.0,
        },
        {
            id: "tx-2",
            operationId: "op-2",
            transactionDate: "2026-03-16T14:30:00Z",
            description: "Utility Bill Payment",
            recipientAccountId: "PK1234567890",
            indicator: "DEBIT",
            amount: 4500.5,
        },
    ];

    beforeEach(() => {
        vi.clearAllMocks();
        localStorage.clear();

        useAuth.mockReturnValue({
            user: mockUser,
        });

        transactionService.getTransactions.mockResolvedValue({
            transactions: mockTransactions,
            totalPages: 1,
        });
    });

    it("fetches and renders transactions on mount with default filters", async () => {
        render(<TransactionsPage />);

        expect(screen.getByText("Loading transactions...")).toBeInTheDocument();

        await waitFor(() => {
            expect(transactionService.getTransactions).toHaveBeenCalledTimes(1);
            expect(screen.getByText("Salary Deposit")).toBeInTheDocument();
            expect(screen.getByText("Utility Bill Payment")).toBeInTheDocument();
        });

        // Check account masking
        expect(screen.getByText("•••• 3210")).toBeInTheDocument();
        expect(screen.getByText("•••• 7890")).toBeInTheDocument();

        // Check formatted amount and debit styling
        expect(screen.getByText("+ PKR 50,000.00")).toBeInTheDocument();
        expect(screen.getByText("- PKR 4,500.50")).toBeInTheDocument();
    });

    it("displays empty state message when no transactions are returned", async () => {
        transactionService.getTransactions.mockResolvedValueOnce({
            transactions: [],
            totalPages: 1,
        });

        render(<TransactionsPage />);

        await waitFor(() => {
            expect(
                screen.getByText("No transactions found matching your criteria.")
            ).toBeInTheDocument();
        });
    });

    it("displays error banner when transaction fetch fails", async () => {
        transactionService.getTransactions.mockRejectedValueOnce(
            new Error("Failed to fetch transaction data.")
        );

        render(<TransactionsPage />);

        await waitFor(() => {
            expect(
                screen.getByText("Failed to fetch transaction data.")
            ).toBeInTheDocument();
        });
    });

    it("applies date and type filters correctly when 'Apply filters' is clicked", async () => {
        const user = userEvent.setup();
        render(<TransactionsPage />);

        await waitFor(() => {
            expect(screen.getByText("Salary Deposit")).toBeInTheDocument();
        });

        const fromInput = screen.getByLabelText(/From:/i);
        const toInput = screen.getByLabelText(/To:/i);
        const typeSelect = screen.getByLabelText(/Type:/i);
        const applyBtn = screen.getByRole("button", { name: /Apply filters/i });

        // Change filter values using fireEvent for date inputs
        fireEvent.change(fromInput, { target: { value: "2026-03-01" } });
        fireEvent.change(toInput, { target: { value: "2026-03-31" } });
        await user.selectOptions(typeSelect, "DEBIT");

        await user.click(applyBtn);

        await waitFor(() => {
            expect(transactionService.getTransactions).toHaveBeenLastCalledWith({
                page: 0,
                pageSize: 20,
                fromDate: "2026-03-01",
                toDate: "2026-03-31",
            });
        });

        // Client-side filter should display only DEBIT transactions
        expect(screen.queryByText("Salary Deposit")).not.toBeInTheDocument();
        expect(screen.getByText("Utility Bill Payment")).toBeInTheDocument();
    });

    it("clears filters when 'Clear' button is clicked", async () => {
        const user = userEvent.setup();
        render(<TransactionsPage />);

        await waitFor(() => {
            expect(screen.getByText("Salary Deposit")).toBeInTheDocument();
        });

        const clearBtn = screen.getByRole("button", { name: /Clear/i });
        await user.click(clearBtn);

        await waitFor(() => {
            expect(transactionService.getTransactions).toHaveBeenLastCalledWith({
                page: 0,
                pageSize: 20,
                fromDate: "",
                toDate: "",
            });
        });

        expect(screen.getByLabelText(/From:/i)).toHaveValue("");
        expect(screen.getByLabelText(/To:/i)).toHaveValue("");
        expect(screen.getByLabelText(/Type:/i)).toHaveValue("ALL");
    });

    it("handles pagination controls correctly", async () => {
        const user = userEvent.setup();
        transactionService.getTransactions.mockResolvedValue({
            transactions: mockTransactions,
            totalPages: 3,
        });

        render(<TransactionsPage />);

        await waitFor(() => {
            expect(screen.getByText("Page 1 of 3")).toBeInTheDocument();
        });

        const prevBtn = screen.getByRole("button", { name: /Previous/i });
        const nextBtn = screen.getByRole("button", { name: /Next/i });

        expect(prevBtn).toBeDisabled();
        expect(nextBtn).not.toBeDisabled();

        // Go to next page
        await user.click(nextBtn);

        await waitFor(() => {
            expect(transactionService.getTransactions).toHaveBeenLastCalledWith(
                expect.objectContaining({ page: 1 })
            );
            expect(screen.getByText("Page 2 of 3")).toBeInTheDocument();
        });

        expect(prevBtn).not.toBeDisabled();
    });

    it("executes logout flow and redirects to HOME", async () => {
        const user = userEvent.setup();
        localStorage.setItem("ACCESS_TOKEN", "mock-token-123");
        transactionService.logout.mockResolvedValueOnce({});

        render(<TransactionsPage />);

        await waitFor(() => {
            expect(screen.getByText("Salary Deposit")).toBeInTheDocument();
        });

        const logoutBtn = screen.getByRole("button", { name: /Logout/i });
        await user.click(logoutBtn);

        expect(transactionService.logout).toHaveBeenCalledTimes(1);
        expect(localStorage.getItem("ACCESS_TOKEN")).toBeNull();
        expect(mockNavigate).toHaveBeenCalledWith("/");
    });

    it("cleans up token and redirects to HOME even if logout API fails", async () => {
        const user = userEvent.setup();
        localStorage.setItem("ACCESS_TOKEN", "mock-token-123");
        transactionService.logout.mockRejectedValueOnce(new Error("Logout error"));

        render(<TransactionsPage />);

        await waitFor(() => {
            expect(screen.getByText("Salary Deposit")).toBeInTheDocument();
        });

        const logoutBtn = screen.getByRole("button", { name: /Logout/i });
        await user.click(logoutBtn);

        expect(localStorage.getItem("ACCESS_TOKEN")).toBeNull();
        expect(mockNavigate).toHaveBeenCalledWith("/");
    });
});