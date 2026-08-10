import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router";
import { describe, expect, it, vi } from "vitest";

import SignupForm from "../../components/SignupForm";

async function fillValidSignup(user) {
    await user.type(screen.getByLabelText(/full name/i), "Jane Doe");
    await user.type(screen.getByLabelText(/email address/i), "jane@example.com");
    await user.type(screen.getByLabelText(/home address/i), "123 Main St");
    await user.type(screen.getByLabelText(/^password$/i), "password1");
    await user.type(screen.getByLabelText(/confirm password/i), "password1");
}

function renderSignupForm(props = {}) {
    return render(
        <MemoryRouter>
            <SignupForm onSubmit={vi.fn()} {...props} />
        </MemoryRouter>,
    );
}

describe("SignupForm", () => {
    it("submits signup values without confirmPassword", async () => {
        const user = userEvent.setup();
        const onSubmit = vi.fn().mockResolvedValue(undefined);

        renderSignupForm({ onSubmit });
        await fillValidSignup(user);
        await user.click(screen.getByRole("button", { name: /create account/i }));

        expect(onSubmit).toHaveBeenCalledWith({
            name: "Jane Doe",
            email: "jane@example.com",
            address: "123 Main St",
            password: "password1",
        });
    });

    it("shows an error when passwords do not match", async () => {
        const user = userEvent.setup();
        const onSubmit = vi.fn();

        renderSignupForm({ onSubmit });

        await user.type(screen.getByLabelText(/full name/i), "Jane Doe");
        await user.type(screen.getByLabelText(/email address/i), "jane@example.com");
        await user.type(screen.getByLabelText(/home address/i), "123 Main St");
        await user.type(screen.getByLabelText(/^password$/i), "password1");
        await user.type(screen.getByLabelText(/confirm password/i), "password2");
        await user.click(screen.getByRole("button", { name: /create account/i }));

        expect(await screen.findByRole("alert")).toHaveTextContent("Passwords do not match.");
        expect(onSubmit).not.toHaveBeenCalled();
    });

    it("shows an error when password is too short", async () => {
        const user = userEvent.setup();
        const onSubmit = vi.fn();

        renderSignupForm({ onSubmit });

        await user.type(screen.getByLabelText(/full name/i), "Jane Doe");
        await user.type(screen.getByLabelText(/email address/i), "jane@example.com");
        await user.type(screen.getByLabelText(/home address/i), "123 Main St");
        await user.type(screen.getByLabelText(/^password$/i), "short");
        await user.type(screen.getByLabelText(/confirm password/i), "short");
        await user.click(screen.getByRole("button", { name: /create account/i }));

        expect(await screen.findByRole("alert")).toHaveTextContent(
            "Password must contain at least 8 characters.",
        );
        expect(onSubmit).not.toHaveBeenCalled();
    });

    it("shows an error when submit fails", async () => {
        const user = userEvent.setup();
        const onSubmit = vi.fn().mockRejectedValue(new Error("Email already used"));

        renderSignupForm({ onSubmit });
        await fillValidSignup(user);
        await user.click(screen.getByRole("button", { name: /create account/i }));

        expect(await screen.findByRole("alert")).toHaveTextContent("Email already used");
    });
});
