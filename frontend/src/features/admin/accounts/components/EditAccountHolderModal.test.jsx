import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";

import EditAccountHolderModal from "./EditAccountHolderModal";

const account = {
    holderName: "Ayesha Khan",
    holderEmail: "ayesha@example.com",
    holderAddress: "Karachi",
};

describe("EditAccountHolderModal", () => {
    it("shows validation errors instead of saving incomplete information", async () => {
        const onSave = vi.fn();
        const user = userEvent.setup();

        render(<EditAccountHolderModal account={account} onClose={vi.fn()} onSave={onSave} />);

        await user.clear(screen.getByLabelText("Full name"));
        await user.click(screen.getByRole("button", { name: "Save changes" }));

        expect(screen.getByText("Name is required.")).toBeInTheDocument();
        expect(onSave).not.toHaveBeenCalled();
    });

    it("trims holder values and normalizes the email before saving", async () => {
        const onSave = vi.fn();
        const user = userEvent.setup();

        render(<EditAccountHolderModal account={account} onClose={vi.fn()} onSave={onSave} />);

        await user.clear(screen.getByLabelText("Full name"));
        await user.type(screen.getByLabelText("Full name"), "  Ayesha Malik  ");
        await user.clear(screen.getByLabelText("Email address"));
        await user.type(screen.getByLabelText("Email address"), "AYESHA@EXAMPLE.COM");
        await user.clear(screen.getByLabelText("Address"));
        await user.type(screen.getByLabelText("Address"), "  Lahore  ");
        await user.click(screen.getByRole("button", { name: "Save changes" }));

        expect(onSave).toHaveBeenCalledWith({
            name: "Ayesha Malik",
            email: "ayesha@example.com",
            address: "Lahore",
        });
    });
});
