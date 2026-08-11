import { afterEach, describe, expect, it, vi } from "vitest";

import { getCurrentMonthDateRange } from "./getCurrentMonthDateRange";

describe("getCurrentMonthDateRange", () => {
    afterEach(() => {
        vi.useRealTimers();
    });

    it("defaults to the first day of the current month through today", () => {
        vi.useFakeTimers();
        vi.setSystemTime(new Date("2026-08-11T12:00:00"));

        expect(getCurrentMonthDateRange()).toEqual({
            startDate: "2026-08-01",
            endDate: "2026-08-11",
        });
    });
});
