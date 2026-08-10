import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it } from "vitest";

import PasswordInput from "../../components/PasswordInput";

describe("PasswordInput", () => {
    it("toggles password visibility", async () => {
        const user = userEvent.setup();

        render(<PasswordInput id="password" name="password" />);

        const input = screen.getByLabelText(/^password$/i);
        expect(input).toHaveAttribute("type", "password");

        await user.click(screen.getByRole("button", { name: /show password/i }));
        expect(input).toHaveAttribute("type", "text");
        expect(screen.getByRole("button", { name: /hide password/i })).toBeInTheDocument();
    });

    it("shows helper text when there is no error", () => {
        render(
            <PasswordInput
                id="password"
                name="password"
                helperText="Use at least 8 characters."
            />,
        );

        expect(screen.getByText("Use at least 8 characters.")).toBeInTheDocument();
    });

    it("shows an error and hides helper text", () => {
        render(
            <PasswordInput
                id="password"
                name="password"
                error="Password is required"
                helperText="Use at least 8 characters."
            />,
        );

        expect(screen.getByText("Password is required")).toBeInTheDocument();
        expect(screen.queryByText("Use at least 8 characters.")).not.toBeInTheDocument();
        expect(screen.getByLabelText(/^password$/i)).toHaveAttribute("aria-invalid", "true");
    });
});
