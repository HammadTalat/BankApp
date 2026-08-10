import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";

import ApplicationStatusCard from "./ApplicationStatusCard";

describe("ApplicationStatusCard", () => {
    it("shows pending details and refreshes the status", async () => {
        const onRefresh = vi.fn();
        const user = userEvent.setup();

        render(
            <ApplicationStatusCard
                status="pending"
                userName="Ayesha Khan"
                userEmail="ayesha@example.com"
                onRefresh={onRefresh}
            />,
        );

        expect(screen.getByText("APPLICATION PENDING")).toBeInTheDocument();
        expect(screen.getByText("Ayesha Khan")).toBeInTheDocument();
        expect(screen.getByText("ayesha@example.com")).toBeInTheDocument();

        await user.click(screen.getByRole("button", { name: "Refresh status" }));

        expect(onRefresh).toHaveBeenCalledOnce();
    });

    it("shows a redirect message instead of actions when approved", () => {
        render(<ApplicationStatusCard status="APPROVED" />);

        expect(screen.getByText("Your account is ready")).toBeInTheDocument();
        expect(screen.getByText("Redirecting to your account...")).toBeInTheDocument();
        expect(screen.queryByRole("button", { name: "Refresh status" })).not.toBeInTheDocument();
    });
});
