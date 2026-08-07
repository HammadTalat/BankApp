import { httpClient } from "../../../../api/httpClient.js";



export function getAdminAccounts({
                                     search = "",
                                     status = "",
                                     page = 0,
                                     size = 10,
                                 } = {}) {
    const params = new URLSearchParams();

    if (search.trim()) {
        params.set("search", search.trim());
    }

    if (status) {
        params.set("status", status);
    }

    params.set("page", String(page));
    params.set("size", String(size));

    return httpClient.get(
        `/api/v1/admin/accounts?${params.toString()}`,
    );
}

export function getAdminAccountDetails(accountNumber) {
    return httpClient.get(
        `/api/v1/admin/accounts/${accountNumber}`,
    );
}

export function closeAdminAccount(accountNumber) {
    return httpClient.post(
        `/api/v1/admin/accounts/${accountNumber}/close`,
    );
}
export function updateAccountHolder(userId, holder) {
    return httpClient.patch(
        `/api/v1/admin/users/${userId}`,
        holder,
    );
}
export function getAdminAccountTransactions(
    accountNumber,
    {
        startDate = "",
        endDate = "",
        page = 0,
        size = 10,
    } = {},
) {
    const params = new URLSearchParams();

    if (startDate) {
        params.set("startDate", startDate);
    }

    if (endDate) {
        params.set("endDate", endDate);
    }

    params.set("page", String(page));
    params.set("size", String(size));

    return httpClient.get(
        `/api/v1/admin/accounts/${encodeURIComponent(accountNumber)}/transactions?${params.toString()}`,
    );
}
