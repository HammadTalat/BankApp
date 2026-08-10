import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";

import AccountActions from "./AccountActions";

describe("AccountActions", () => {
    it("keeps transaction history available but disables closure for closed accounts", async () => {
        const onViewTransactions = vi.fn();
        const user = userEvent.setup();

        render(
            <AccountActions
                accountStatus="CLOSED"
                onViewTransactions={onViewTransactions}
                onEditHolder={vi.fn()}
                onCloseAccount={vi.fn()}
            />,
        );

        expect(screen.getByRole("button", { name: "Close Account" })).toBeDisabled();
        expect(screen.getByText(/transaction history remains available/i)).toBeInTheDocument();

        await user.click(screen.getByRole("button", { name: "View Transactions" }));

        expect(onViewTransactions).toHaveBeenCalledOnce();
    });
});
