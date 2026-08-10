import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";

import UserReviewModal from "./UserReviewModal";

const user = {
    name: "Ayesha Khan",
    email: "ayesha@example.com",
    address: "Karachi",
    approvalStatus: "PENDING",
};

describe("UserReviewModal", () => {
    it("explains an approval and confirms it", async () => {
        const onConfirm = vi.fn();
        const testUser = userEvent.setup();

        render(
            <UserReviewModal
                isOpen
                user={user}
                action="approve"
                onClose={vi.fn()}
                onConfirm={onConfirm}
            />,
        );

        expect(screen.getByRole("dialog", { name: "Approve account holder" })).toBeInTheDocument();
        expect(screen.getByText("Ayesha Khan")).toBeInTheDocument();

        await testUser.click(screen.getByRole("button", { name: "Approve user" }));

        expect(onConfirm).toHaveBeenCalledOnce();
    });
});
