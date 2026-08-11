import { describe, it, expect, vi, beforeEach } from "vitest";
import { transactionService } from "./transactionService.js";
import { httpClient } from "../../../../api/httpClient.js";

// Mock the HTTP client module
vi.mock("../../../../api/httpClient.js", () => ({
    httpClient: {
        get: vi.fn(),
        post: vi.fn(),
    },
}));

describe("transactionService", () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    describe("getTransactions", () => {
        it("should fetch paginated transactions with required pagination parameters", async () => {
            const mockResponse = {
                transactions: [
                    { id: "tx1", amount: 1000, type: "DEPOSIT" },
                    { id: "tx2", amount: 500, type: "WITHDRAWAL" },
                ],
                totalPages: 5,
            };
            httpClient.get.mockResolvedValueOnce(mockResponse);

            const params = { page: 0, pageSize: 10 };
            const result = await transactionService.getTransactions(params);

            expect(httpClient.get).toHaveBeenCalledTimes(1);
            expect(httpClient.get).toHaveBeenCalledWith(
                "/api/v1/transaction/get-transactions?page=0&size=10"
            );
            expect(result).toEqual(mockResponse);
        });

        it("should append fromDate as startDate and toDate as endDate when provided", async () => {
            const mockResponse = { transactions: [], totalPages: 0 };
            httpClient.get.mockResolvedValueOnce(mockResponse);

            const params = {
                page: 1,
                pageSize: 20,
                fromDate: "2026-01-01",
                toDate: "2026-01-31",
            };

            await transactionService.getTransactions(params);

            expect(httpClient.get).toHaveBeenCalledWith(
                "/api/v1/transaction/get-transactions?page=1&size=20&startDate=2026-01-01&endDate=2026-01-31"
            );
        });

        it("should handle optional date filters independently", async () => {
            const mockResponse = { transactions: [], totalPages: 0 };
            httpClient.get.mockResolvedValueOnce(mockResponse);

            // Only fromDate provided
            await transactionService.getTransactions({
                page: 0,
                pageSize: 5,
                fromDate: "2026-02-01",
            });

            expect(httpClient.get).toHaveBeenCalledWith(
                "/api/v1/transaction/get-transactions?page=0&size=5&startDate=2026-02-01"
            );
        });

        it("should propagate errors when fetching transactions fails", async () => {
            const mockError = new Error("Failed to fetch transactions");
            httpClient.get.mockRejectedValueOnce(mockError);

            await expect(
                transactionService.getTransactions({ page: 0, pageSize: 10 })
            ).rejects.toThrow("Failed to fetch transactions");
        });
    });

    describe("logout", () => {
        it("should post to /api/v1/auth/logout", async () => {
            httpClient.post.mockResolvedValueOnce({});

            await transactionService.logout();

            expect(httpClient.post).toHaveBeenCalledTimes(1);
            expect(httpClient.post).toHaveBeenCalledWith("/api/v1/auth/logout");
        });

        it("should propagate errors when logout fails", async () => {
            const mockError = new Error("Session expired");
            httpClient.post.mockRejectedValueOnce(mockError);

            await expect(transactionService.logout()).rejects.toThrow("Session expired");
        });
    });
});