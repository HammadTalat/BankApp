    import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";

import { ApproveUser, getPendingUsers } from "../api/adminUsersApi.js";
import PendingUsersPage from "./PendingUsersPage";

vi.mock("../api/adminUsersApi.js", () => ({
    getPendingUsers: vi.fn(),
    ApproveUser: vi.fn(),
    RejectUser: vi.fn(),
}));

describe("PendingUsersPage", () => {
    it("removes an approved user and shows confirmation feedback", async () => {
        const testUser = userEvent.setup();
        getPendingUsers.mockResolvedValue([
            {
                id: 7,
                name: "Ayesha Khan",
                email: "ayesha@example.com",
                address: "Karachi",
                approvalStatus: "PENDING",
            },
        ]);
        ApproveUser.mockResolvedValue({});

        render(<PendingUsersPage />);

        const approveButtons = await screen.findAllByRole("button", {
            name: "Approve Ayesha Khan",
        });
        await testUser.click(approveButtons[0]);
        await testUser.click(screen.getByRole("button", { name: "Approve user" }));

        expect(await screen.findByText("User approved")).toBeInTheDocument();
        expect(ApproveUser).toHaveBeenCalledWith(7);
        expect(screen.getByText("No pending users")).toBeInTheDocument();
    });
});
