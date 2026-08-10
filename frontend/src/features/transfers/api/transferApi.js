import { httpClient } from "../../../api/httpClient.js";

/**
 * @typedef {Object} TransferRequest
 * @property {string} senderAccountNumber - Sender's account number
 * @property {string} receiverAccountNumber - Recipient's account number
 * @property {number} amount - Transfer amount (minimum: 0.01)
 * @property {string} [description] - Optional payment reference (max 255 chars)
 */

/**
 * @typedef {Object} TransferResponse
 * @property {string} [operationId]
 * @property {'PENDING'|'COMPLETED'|'FAILED'|'REJECTED'} [status]
 * @property {number} [amount]
 * @property {string} [senderAccountNumber]
 * @property {string} [receiverAccountNumber]
 * @property {string} [description]
 * @property {string} [timestamp]
 */

/**
 * @typedef {Object} AccountLookupResponse
 * @property {string} [accountNumber]
 * @property {string} [accountHolderName]
 * @property {'ACTIVE'|'CLOSED'} [status]
 */

/**
 * Helper to build query parameter strings cleanly
 * @param {Object} params
 * @returns {string}
 */
const buildQueryString = (params = {}) => {
    const query = new URLSearchParams();
    Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined && value !== null && value !== "") {
            query.append(key, value);
        }
    });
    const queryString = query.toString();
    return queryString ? `?${queryString}` : "";
};

export const transfersApi = {
    /**
     * Look up primary sender account details
     * Endpoint: GET /api/v1/account
     */
    getSenderAccount: async () => {
        return await httpClient.get("/api/v1/account");
    },

    /**
     * Look up an account by account ID before initiating a transfer
     * Endpoint: GET /api/v1/transaction/lookup
     *
     * @param {string} accountID - Account identifier to look up
     * @returns {Promise<AccountLookupResponse>}
     */
    lookupRecipient: async (accountID) => {
        const queryString = buildQueryString({ accountID });
        return await httpClient.get(`/api/v1/transaction/lookup${queryString}`);
    },

    /**
     * Execute a financial transfer between accounts
     * Endpoint: POST /api/v1/transaction/transfer
     *
     * @param {TransferRequest} payload
     * @returns {Promise<TransferResponse>}
     */
    executeTransfer: async (payload) => {
        return await httpClient.post("/api/v1/transaction/transfer", payload);
    },

    /**
     * Fetch paginated transaction history for the authenticated user
     * Endpoint: GET /api/v1/transaction/get-transactions
     *
     * @param {Object} [params]
     * @param {number} [params.page=0] - 0-indexed page number
     * @param {number} [params.size=20] - Number of items per page
     * @param {string} [params.sort] - Sort criteria (e.g., 'transactionDate,desc')
     * @returns {Promise<{ transactions: Array, currentPage: number, totalPages: number, totalElements: number, isLast: boolean }>}
     */
    getUserTransactions: async ({ page = 0, size = 20, sort } = {}) => {
        const queryString = buildQueryString({ page, size, sort });
        return await httpClient.get(`/api/v1/transaction/get-transactions${queryString}`);
    },
};

export default transfersApi;