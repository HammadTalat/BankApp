import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";

import GoogleLoginButton from "../../components/GoogleLoginButton";

vi.mock("../../api/authApi", () => ({
    getGoogleLoginUrl: () => "http://localhost/oauth2/authorization/google",
}));

describe("GoogleLoginButton", () => {
    beforeEach(() => {
        Object.defineProperty(window, "location", {
            configurable: true,
            value: { assign: vi.fn() },
        });
    });

    it("redirects to the Google login URL", async () => {
        const user = userEvent.setup();

        render(<GoogleLoginButton />);

        await user.click(screen.getByRole("button", { name: /continue with google/i }));

        expect(window.location.assign).toHaveBeenCalledWith(
            "http://localhost/oauth2/authorization/google",
        );
    });
});
