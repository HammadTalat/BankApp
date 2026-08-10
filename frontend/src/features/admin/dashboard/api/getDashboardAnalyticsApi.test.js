import { beforeEach, describe, expect, it, vi } from "vitest";

import { httpClient } from "../../../../api/httpClient.js";
import { getDashboardAnalytics } from "./getDashboardAnalyticsApi.js";

vi.mock("../../../../api/httpClient.js", () => ({
    httpClient: { get: vi.fn() },
}));

describe("getDashboardAnalytics", () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it("requests the admin dashboard endpoint", () => {
        getDashboardAnalytics();

        expect(httpClient.get).toHaveBeenCalledWith("/api/v1/admin/dashboard");
    });
});
