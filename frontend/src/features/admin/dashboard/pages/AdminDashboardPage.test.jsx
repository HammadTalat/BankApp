import { render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router";
import { describe, expect, it, vi } from "vitest";

import { getDashboardAnalytics } from "../api/getDashboardAnalyticsApi.js";
import AdminDashboardPage from "./AdminDashboardPage";

vi.mock("../api/getDashboardAnalyticsApi.js", () => ({
    getDashboardAnalytics: vi.fn(),
}));

describe("AdminDashboardPage", () => {
    it("displays the dashboard metrics returned by the API", async () => {
        getDashboardAnalytics.mockResolvedValue({
            pendingUsers: 2,
            approvedUsers: 8,
            rejectedUsers: 1,
            totalAccounts: 10,
            activeAccounts: 9,
            closedAccounts: 1,
        });

        render(
            <MemoryRouter>
                <AdminDashboardPage />
            </MemoryRouter>,
        );

        expect(screen.getByText("Loading dashboard...")).toBeInTheDocument();
        expect(await screen.findByText("Pending Users")).toBeInTheDocument();
        expect(screen.getByText("Total Accounts")).toBeInTheDocument();
        expect(screen.getByText("10")).toBeInTheDocument();
    });
});
