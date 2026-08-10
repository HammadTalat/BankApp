import { httpClient } from "../../../api/httpClient.js";

export const depositApi = {
    /**
     * Get user primary account info
     */
    getAccountDetails: async () => {
        return await httpClient.get("/api/v1/account");
    },

    /**
     * Submit deposit
     * @param {Object} payload
     * @param {string} payload.accountNumber
     * @param {number} payload.amount
     * @param {string} payload.description
     */
    deposit: async (payload) => {
        return await httpClient.post("/api/v1/transaction/deposit", payload);
    },
};

export default depositApi;
