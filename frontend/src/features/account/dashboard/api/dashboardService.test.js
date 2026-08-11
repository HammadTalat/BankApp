import { describe, it, expect, vi, beforeEach } from "vitest";
import { dashboardService } from "./dashboardService.js";
import { httpClient } from "../../../../api/httpClient.js";

// Mock the HTTP client module
vi.mock("../../../../api/httpClient.js", () => ({
    httpClient: {
        get: vi.fn(),
    },
}));

describe("dashboardService", () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    describe("getAccountDetails", () => {
        it("should fetch account details from /api/v1/account", async () => {
            const mockResponse = {
                accountNumber: "1234567890",
                status: "ACTIVE",
            };
            httpClient.get.mockResolvedValueOnce(mockResponse);

            const result = await dashboardService.getAccountDetails();

            expect(httpClient.get).toHaveBeenCalledTimes(1);
            expect(httpClient.get).toHaveBeenCalledWith("/api/v1/account");
            expect(result).toEqual(mockResponse);
        });

        it("should propagate errors when the request fails", async () => {
            const mockError = new Error("Network Error");
            httpClient.get.mockRejectedValueOnce(mockError);

            await expect(dashboardService.getAccountDetails()).rejects.toThrow(
                "Network Error"
            );
            expect(httpClient.get).toHaveBeenCalledWith("/api/v1/account");
        });
    });

    describe("getAccountBalance", () => {
        it("should fetch account balance from /api/v1/account/balance", async () => {
            const mockResponse = { amount: 50000.5 };
            httpClient.get.mockResolvedValueOnce(mockResponse);

            const result = await dashboardService.getAccountBalance();

            expect(httpClient.get).toHaveBeenCalledTimes(1);
            expect(httpClient.get).toHaveBeenCalledWith("/api/v1/account/balance");
            expect(result).toEqual(mockResponse);
        });

        it("should propagate errors when the request fails", async () => {
            const mockError = new Error("Unauthorized");
            httpClient.get.mockRejectedValueOnce(mockError);

            await expect(dashboardService.getAccountBalance()).rejects.toThrow(
                "Unauthorized"
            );
            expect(httpClient.get).toHaveBeenCalledWith("/api/v1/account/balance");
        });
    });

    describe("getRecentTransactions", () => {
        it("should fetch recent transactions with default pagination parameters (page=0, size=5)", async () => {
            const mockResponse = {
                transactions: [{ id: "tx-1", amount: 100 }],
                totalPages: 1,
            };
            httpClient.get.mockResolvedValueOnce(mockResponse);

            const result = await dashboardService.getRecentTransactions();

            expect(httpClient.get).toHaveBeenCalledTimes(1);
            expect(httpClient.get).toHaveBeenCalledWith(
                "/api/v1/transaction/get-transactions?page=0&size=5"
            );
            expect(result).toEqual(mockResponse);
        });

        it("should fetch recent transactions with custom page and size parameters", async () => {
            const mockResponse = {
                transactions: [{ id: "tx-2", amount: 250 }],
                totalPages: 3,
            };
            httpClient.get.mockResolvedValueOnce(mockResponse);

            const result = await dashboardService.getRecentTransactions(2, 10);

            expect(httpClient.get).toHaveBeenCalledTimes(1);
            expect(httpClient.get).toHaveBeenCalledWith(
                "/api/v1/transaction/get-transactions?page=2&size=10"
            );
            expect(result).toEqual(mockResponse);
        });

        it("should propagate errors when the request fails", async () => {
            const mockError = new Error("Internal Server Error");
            httpClient.get.mockRejectedValueOnce(mockError);

            await expect(dashboardService.getRecentTransactions(0, 5)).rejects.toThrow(
                "Internal Server Error"
            );
        });
    });
});