import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router";
import { describe, expect, it, vi } from "vitest";

import LoginForm from "../../components/LoginForm";

function renderLoginForm(props = {}) {
    return render(
        <MemoryRouter>
            <LoginForm onSubmit={vi.fn()} {...props} />
        </MemoryRouter>,
    );
}

describe("LoginForm", () => {
    it("submits email and password", async () => {
        const user = userEvent.setup();
        const onSubmit = vi.fn().mockResolvedValue(undefined);

        renderLoginForm({ onSubmit });

        await user.type(screen.getByLabelText(/email address/i), "user@example.com");
        await user.type(screen.getByLabelText(/^password$/i), "secret123");
        await user.click(screen.getByRole("button", { name: /sign in/i }));

        expect(onSubmit).toHaveBeenCalledWith({
            email: "user@example.com",
            password: "secret123",
        });
    });

    it("shows an error when submit fails", async () => {
        const user = userEvent.setup();
        const onSubmit = vi.fn().mockRejectedValue(new Error("Invalid credentials"));

        renderLoginForm({ onSubmit });

        await user.type(screen.getByLabelText(/email address/i), "user@example.com");
        await user.type(screen.getByLabelText(/^password$/i), "wrong");
        await user.click(screen.getByRole("button", { name: /sign in/i }));

        expect(await screen.findByRole("alert")).toHaveTextContent("Invalid credentials");
    });

    it("disables the submit button while submitting", () => {
        renderLoginForm({ isSubmitting: true });

        expect(screen.getByRole("button", { name: /signing in/i })).toBeDisabled();
    });

    it("links to the signup page", () => {
        renderLoginForm();

        expect(screen.getByRole("link", { name: /create an account/i })).toHaveAttribute(
            "href",
            "/signup",
        );
    });
});
