import { httpClient } from "../../../../api/httpClient.js";


/**
 * Service handling account-to-account money transfers and recipient lookups.
 */
export const transferService = {
    /**
     * Fetches the current user's primary account details.
     * @returns {Promise<{ accountNumber: string, [key: string]: any }>}
     */
    getAccountDetails: async () => {
        return await httpClient.get("/api/v1/account");
    },

    /**
     * Looks up a recipient account by account ID/number.
     * @param {string} accountId - The target account number to look up.
     * @returns {Promise<{ accountNumber: string, accountHolderName: string, status: string }>}
     */
    lookupRecipient: async (accountId) => {
        return await httpClient.get(
            `/api/v1/transaction/lookup?accountID=${encodeURIComponent(accountId)}`
        );
    },

    /**
     * Executes a fund transfer between sender and recipient accounts.
     * @param {Object} payload
     * @param {string} payload.senderAccountNumber
     * @param {string} payload.receiverAccountNumber
     * @param {number} payload.amount
     * @param {string} [payload.description]
     * @returns {Promise<any>}
     */
    transferFunds: async ({
                              senderAccountNumber,
                              receiverAccountNumber,
                              amount,
                              description,
                          }) => {
        return await httpClient.post("/api/v1/transaction/transfer", {
            senderAccountNumber,
            receiverAccountNumber,
            amount,
            description,
        });
    },

    /**
     * Logs out the currently authenticated user session.
     * @returns {Promise<void>}
     */
    logout: async () => {
        return await httpClient.post("/api/v1/auth/logout");
    },
};