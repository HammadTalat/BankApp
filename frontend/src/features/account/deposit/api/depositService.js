import { httpClient } from "../../../../api/httpClient.js";

/**
 * Service handling account deposit operations and data fetching.
 */
export const depositService = {
    /**
     * Fetches primary account information.
     * @returns {Promise<{ accountNumber: string }>}
     */
    getAccountDetails: async () => {
        return await httpClient.get("/api/v1/account");
    },

    /**
     * Submits a deposit request.
     * @param {Object} payload
     * @param {string} payload.accountNumber
     * @param {number} payload.amount
     * @param {string} payload.description
     * @returns {Promise<{ message?: string }>}
     */
    processDeposit: async ({ accountNumber, amount, description }) => {
        return await httpClient.post("/api/v1/transaction/deposit", {
            accountNumber,
            amount,
            description: description.trim() || "Deposit",
        });
    },
};