import { describe, it, expect, vi, beforeEach } from "vitest";
import { depositService } from "./depositService.js";
import { httpClient } from "../../../../api/httpClient.js";

// Mock the HTTP client module
vi.mock("../../../../api/httpClient.js", () => ({
    httpClient: {
        get: vi.fn(),
        post: vi.fn(),
    },
}));

describe("depositService", () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    describe("getAccountDetails", () => {
        it("should fetch account details from /api/v1/account", async () => {
            const mockResponse = { accountNumber: "9876543210" };
            httpClient.get.mockResolvedValueOnce(mockResponse);

            const result = await depositService.getAccountDetails();

            expect(httpClient.get).toHaveBeenCalledTimes(1);
            expect(httpClient.get).toHaveBeenCalledWith("/api/v1/account");
            expect(result).toEqual(mockResponse);
        });

        it("should propagate errors when fetching account details fails", async () => {
            const mockError = new Error("Network Error");
            httpClient.get.mockRejectedValueOnce(mockError);

            await expect(depositService.getAccountDetails()).rejects.toThrow("Network Error");
            expect(httpClient.get).toHaveBeenCalledWith("/api/v1/account");
        });
    });

    describe("processDeposit", () => {
        it("should post deposit data with provided description to /api/v1/transaction/deposit", async () => {
            const mockResponse = { message: "Deposit successful" };
            httpClient.post.mockResolvedValueOnce(mockResponse);

            const payload = {
                accountNumber: "9876543210",
                amount: 5000,
                description: "Salary Bonus",
            };

            const result = await depositService.processDeposit(payload);

            expect(httpClient.post).toHaveBeenCalledTimes(1);
            expect(httpClient.post).toHaveBeenCalledWith("/api/v1/transaction/deposit", {
                accountNumber: "9876543210",
                amount: 5000,
                description: "Salary Bonus",
            });
            expect(result).toEqual(mockResponse);
        });

        it("should fallback to 'Deposit' if description is empty or only whitespace", async () => {
            const mockResponse = { message: "Deposit successful" };
            httpClient.post.mockResolvedValueOnce(mockResponse);

            const payload = {
                accountNumber: "9876543210",
                amount: 1000,
                description: "   ",
            };

            await depositService.processDeposit(payload);

            expect(httpClient.post).toHaveBeenCalledWith("/api/v1/transaction/deposit", {
                accountNumber: "9876543210",
                amount: 1000,
                description: "Deposit",
            });
        });

        it("should trim leading and trailing whitespace from custom description", async () => {
            const mockResponse = { message: "Deposit successful" };
            httpClient.post.mockResolvedValueOnce(mockResponse);

            const payload = {
                accountNumber: "9876543210",
                amount: 2500,
                description: "  Freelance payment  ",
            };

            await depositService.processDeposit(payload);

            expect(httpClient.post).toHaveBeenCalledWith("/api/v1/transaction/deposit", {
                accountNumber: "9876543210",
                amount: 2500,
                description: "Freelance payment",
            });
        });

        it("should propagate errors when processDeposit request fails", async () => {
            const mockError = new Error("Insufficient permissions or invalid account");
            httpClient.post.mockRejectedValueOnce(mockError);

            const payload = {
                accountNumber: "9876543210",
                amount: 5000,
                description: "Test",
            };

            await expect(depositService.processDeposit(payload)).rejects.toThrow(
                "Insufficient permissions or invalid account"
            );
        });
    });
});