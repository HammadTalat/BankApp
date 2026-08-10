import { beforeEach, describe, expect, it, vi } from "vitest";

import { httpClient } from "../../../api/httpClient";
import { getApplicationStatus } from "./applicationStatusApi";

vi.mock("../../../api/httpClient", () => ({
    httpClient: {
        get: vi.fn(),
    },
}));

describe("getApplicationStatus", () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it("requests the current user's application status", () => {
        getApplicationStatus();

        expect(httpClient.get).toHaveBeenCalledWith("/api/v1/me");
    });
});
