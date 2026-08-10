import { render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";

import CloseAccountModal from "./CloseAccountModal";

const account = {
    accountNumber: "123456789012",
    holderName: "Ayesha Khan",
    accountStatus: "ACTIVE",
    balance: 0,
};

describe("CloseAccountModal", () => {
    it("allows closing an active account with zero balance", () => {
        render(<CloseAccountModal account={account} onClose={vi.fn()} onConfirm={vi.fn()} />);

        expect(screen.getByRole("button", { name: "Close account" })).toBeEnabled();
        expect(screen.getByText("Confirm permanent closure")).toBeInTheDocument();
    });

    it("prevents closing an account that still has a balance", () => {
        render(
            <CloseAccountModal
                account={{ ...account, balance: 500 }}
                onClose={vi.fn()}
                onConfirm={vi.fn()}
            />,
        );

        expect(screen.getByRole("button", { name: "Close account" })).toBeDisabled();
        expect(screen.getByText("Balance must be zero")).toBeInTheDocument();
    });
});
