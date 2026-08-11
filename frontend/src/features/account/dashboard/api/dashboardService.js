import { httpClient } from "../../../../api/httpClient.js";

/**
 * Service handling dashboard API requests
 */
export const dashboardService = {
    /**
     * Fetches account details like account number and status.
     */
    getAccountDetails: async () => {
        return await httpClient.get("/api/v1/account");
    },

    /**
     * Fetches the current account balance.
     */
    getAccountBalance: async () => {
        return await httpClient.get("/api/v1/account/balance");
    },

    /**
     * Fetches the latest transactions with pagination.
     * @param {number} page
     * @param {number} size
     */
    getRecentTransactions: async (page = 0, size = 5) => {
        return await httpClient.get(
            `/api/v1/transaction/get-transactions?page=${page}&size=${size}`
        );
    },
};