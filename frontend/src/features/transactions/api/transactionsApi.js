import { httpClient } from "../../../api/httpClient.js";

export const transactionsApi = {
    /**
     * Fetch user transactions with optional pagination and date range filters
     * @param {Object} params
     * @param {number} [params.page=0]
     * @param {number} [params.size=20]
     * @param {string} [params.startDate]
     * @param {string} [params.endDate]
     */
    getTransactions: async ({ page = 0, size = 20, startDate, endDate } = {}) => {
        const queryParams = new URLSearchParams({
            page: String(page),
            size: String(size),
        });

        if (startDate) queryParams.append("startDate", startDate);
        if (endDate) queryParams.append("endDate", endDate);

        return await httpClient.get(
            `/api/v1/transaction/get-transactions?${queryParams.toString()}`
        );
    },
};

export default transactionsApi;
