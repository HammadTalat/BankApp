import { describe, it, expect, vi, beforeEach } from "vitest";
import { transferService } from "./transferService.js";
import { httpClient } from "../../../../api/httpClient.js";

// Mock the httpClient module
vi.mock("../../../../api/httpClient.js", () => ({
    httpClient: {
        get: vi.fn(),
        post: vi.fn(),
    },
}));

describe("transferService", () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    describe("getAccountDetails", () => {
        it("should call GET on /api/v1/account and return user account details", async () => {
            const mockAccountData = {
                accountNumber: "PK1234567890",
                balance: 25000,
                currency: "PKR",
            };
            httpClient.get.mockResolvedValueOnce(mockAccountData);

            const result = await transferService.getAccountDetails();

            expect(httpClient.get).toHaveBeenCalledTimes(1);
            expect(httpClient.get).toHaveBeenCalledWith("/api/v1/account");
            expect(result).toEqual(mockAccountData);
        });

        it("should propagate errors when getAccountDetails fails", async () => {
            const mockError = new Error("Failed to fetch account details");
            httpClient.get.mockRejectedValueOnce(mockError);

            await expect(transferService.getAccountDetails()).rejects.toThrow(
                "Failed to fetch account details"
            );
        });
    });

    describe("lookupRecipient", () => {
        it("should call GET on /api/v1/transaction/lookup with URI-encoded accountID parameter", async () => {
            const mockRecipientData = {
                accountNumber: "PK9876543210",
                accountHolderName: "Jane Doe",
                status: "ACTIVE",
            };
            httpClient.get.mockResolvedValueOnce(mockRecipientData);

            const accountId = "PK987/654 3210";
            const result = await transferService.lookupRecipient(accountId);

            expect(httpClient.get).toHaveBeenCalledTimes(1);
            expect(httpClient.get).toHaveBeenCalledWith(
                `/api/v1/transaction/lookup?accountID=${encodeURIComponent(accountId)}`
            );
            expect(result).toEqual(mockRecipientData);
        });

        it("should propagate errors when lookupRecipient fails", async () => {
            const mockError = new Error("Account not found");
            httpClient.get.mockRejectedValueOnce(mockError);

            await expect(
                transferService.lookupRecipient("INVALID_ACCOUNT")
            ).rejects.toThrow("Account not found");
        });
    });

    describe("transferFunds", () => {
        it("should call POST on /api/v1/transaction/transfer with payload", async () => {
            const mockResponse = {
                transactionId: "TX123456",
                status: "SUCCESS",
                message: "Transfer completed successfully",
            };
            httpClient.post.mockResolvedValueOnce(mockResponse);

            const payload = {
                senderAccountNumber: "PK1234567890",
                receiverAccountNumber: "PK9876543210",
                amount: 5000,
                description: "Rent Payment",
            };

            const result = await transferService.transferFunds(payload);

            expect(httpClient.post).toHaveBeenCalledTimes(1);
            expect(httpClient.post).toHaveBeenCalledWith(
                "/api/v1/transaction/transfer",
                payload
            );
            expect(result).toEqual(mockResponse);
        });

        it("should propagate errors when transferFunds fails", async () => {
            const mockError = new Error("Insufficient funds");
            httpClient.post.mockRejectedValueOnce(mockError);

            const payload = {
                senderAccountNumber: "PK1234567890",
                receiverAccountNumber: "PK9876543210",
                amount: 1000000,
                description: "Large transfer",
            };

            await expect(transferService.transferFunds(payload)).rejects.toThrow(
                "Insufficient funds"
            );
        });
    });

    describe("logout", () => {
        it("should call POST on /api/v1/auth/logout", async () => {
            httpClient.post.mockResolvedValueOnce({ success: true });

            const result = await transferService.logout();

            expect(httpClient.post).toHaveBeenCalledTimes(1);
            expect(httpClient.post).toHaveBeenCalledWith("/api/v1/auth/logout");
            expect(result).toEqual({ success: true });
        });

        it("should propagate errors when logout fails", async () => {
            const mockError = new Error("Network error during logout");
            httpClient.post.mockRejectedValueOnce(mockError);

            await expect(transferService.logout()).rejects.toThrow(
                "Network error during logout"
            );
        });
    });
});