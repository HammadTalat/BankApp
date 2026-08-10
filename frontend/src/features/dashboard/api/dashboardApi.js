import { httpClient } from "../../../api/httpClient.js";

export const dashboardApi = {
    /**
     * Fetch primary account details (account number and status)
     */
    getAccountDetails: async () => {
        return await httpClient.get("/api/v1/account");
    },

    /**
     * Fetch current account balance
     */
    getBalance: async () => {
        return await httpClient.get("/api/v1/account/balance");
    },

    /**
     * Fetch recent transactions
     * @param {number} size
     */
    getRecentTransactions: async (size = 5) => {
        return await httpClient.get(`/api/v1/transaction/get-transactions?page=0&size=${size}`);
    },
};

export default dashboardApi;
