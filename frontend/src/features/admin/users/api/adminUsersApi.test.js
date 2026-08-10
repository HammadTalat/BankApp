import { beforeEach, describe, expect, it, vi } from "vitest";

import { httpClient } from "../../../../api/httpClient.js";
import { ApproveUser, getPendingUsers, RejectUser } from "./adminUsersApi.js";

vi.mock("../../../../api/httpClient.js", () => ({
    httpClient: {
        get: vi.fn(),
        post: vi.fn(),
    },
}));

describe("admin users API", () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it("requests only pending users", () => {
        getPendingUsers();

        expect(httpClient.get).toHaveBeenCalledWith(
            "/api/v1/admin/users?approvalStatus=PENDING",
        );
    });

    it("sends approval and rejection requests for the selected user", () => {
        ApproveUser(12);
        RejectUser(15);

        expect(httpClient.post).toHaveBeenNthCalledWith(
            1,
            "/api/v1/admin/users/12/approve",
        );
        expect(httpClient.post).toHaveBeenNthCalledWith(
            2,
            "/api/v1/admin/users/15/reject",
        );
    });
});
