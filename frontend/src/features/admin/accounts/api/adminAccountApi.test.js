import { beforeEach, describe, expect, it, vi } from "vitest";

import { httpClient } from "../../../../api/httpClient.js";
import {
    closeAdminAccount,
    getAdminAccounts,
    getAdminAccountTransactions,
    updateAccountHolder,
} from "./adminAccountApi.js";

vi.mock("../../../../api/httpClient.js", () => ({
    httpClient: {
        get: vi.fn(),
        post: vi.fn(),
        patch: vi.fn(),
    },
}));

describe("admin accounts API", () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it("builds a filtered account request", () => {
        getAdminAccounts({ search: "  Ayesha  ", status: "ACTIVE", page: 2, size: 5 });

        expect(httpClient.get).toHaveBeenCalledWith(
            "/api/v1/admin/accounts?search=Ayesha&status=ACTIVE&page=2&size=5",
        );
    });

    it("encodes an account number when requesting transactions", () => {
        getAdminAccountTransactions("ACC/001", {
            startDate: "2026-01-01",
            endDate: "2026-01-31",
            page: 1,
            size: 20,
        });

        expect(httpClient.get).toHaveBeenCalledWith(
            "/api/v1/admin/accounts/ACC%2F001/transactions?startDate=2026-01-01&endDate=2026-01-31&page=1&size=20",
        );
    });

    it("sends close and holder-update requests to their endpoints", () => {
        const holder = { name: "Ayesha Khan" };

        closeAdminAccount("ACC-1");
        updateAccountHolder(7, holder);

        expect(httpClient.post).toHaveBeenCalledWith("/api/v1/admin/accounts/ACC-1/close");
        expect(httpClient.patch).toHaveBeenCalledWith("/api/v1/admin/users/7", holder);
    });
});
