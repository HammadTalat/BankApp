import { httpClient } from "../../../../api/httpClient.js";


/**
 * Service handling account transaction-related operations.
 */
export const transactionService = {
    /**
     * Fetches paginated transaction history based on filter parameters.
     * @param {Object} params
     * @param {number} params.page - Zero-based page index.
     * @param {number} params.pageSize - Number of records per page.
     * @param {string} [params.fromDate] - Start date filter (YYYY-MM-DD).
     * @param {string} [params.toDate] - End date filter (YYYY-MM-DD).
     * @returns {Promise<{ transactions: Array, totalPages: number }>}
     */
    getTransactions: async ({ page, pageSize, fromDate, toDate }) => {
        const queryParams = new URLSearchParams({
            page: String(page),
            size: String(pageSize),
        });

        if (fromDate) queryParams.append("startDate", fromDate);
        if (toDate) queryParams.append("endDate", toDate);

        return await httpClient.get(
            `/api/v1/transaction/get-transactions?${queryParams.toString()}`
        );
    },

    /**
     * Logs out the currently authenticated user session.
     * @returns {Promise<void>}
     */
    logout: async () => {
        return await httpClient.post("/api/v1/auth/logout");
    },
};