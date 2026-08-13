import {
    ArrowLeftRight,
    Bot,
    Building2,
    HandCoins,
    LayoutDashboard,
    ReceiptText,
} from "lucide-react";
import { NavLink } from "react-router";

import { ROUTES } from "../../../routes/routePaths";

const navigationItems = [
    { label: "Dashboard", path: ROUTES.ACCOUNT_HOME, icon: LayoutDashboard, end: true },
    { label: "Deposit Money", path: ROUTES.ACCOUNT_DEPOSIT, icon: HandCoins },
    { label: "Transfer Money", path: ROUTES.ACCOUNT_TRANSFERS, icon: ArrowLeftRight },
    { label: "Transactions", path: ROUTES.ACCOUNT_TRANSACTIONS, icon: ReceiptText },
    { label: "AI Assistant", path: ROUTES.ACCOUNT_CHATBOT, icon: Bot },
];

function AccountSidebar() {
    return (
        <aside className="flex w-64 shrink-0 flex-col border-r border-brand-border bg-brand-surface">
            <div className="flex min-h-24 items-center gap-3 border-b border-brand-border px-5">
                <span className="flex h-11 w-11 shrink-0 items-center justify-center rounded-lg bg-brand-primary text-white">
                    <Building2 size={21} aria-hidden="true" />
                </span>
                <div className="min-w-0">
                    <p lang="ur" dir="rtl" className="rm-urdu-brand text-lg font-bold text-brand-navy">
                        ریڈ میتھ بینک
                    </p>
                    <p className="mt-0.5 text-xs font-semibold text-brand-muted">Personal Banking</p>
                </div>
            </div>

            <nav className="flex-1 space-y-1 p-4" aria-label="Account navigation">
                {navigationItems.map((item) => {
                    const Icon = item.icon;

                    return (
                        <NavLink
                            key={item.path}
                            to={item.path}
                            end={item.end}
                            data-testid={`nav-${item.label.toLowerCase().replace(/\s+/g, "-")}`}
                            className={({ isActive }) => [
                                "flex min-h-11 items-center gap-3 rounded-xl px-3.5 py-2.5 text-sm font-semibold transition-colors",
                                "focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-primary",
                                isActive
                                    ? "bg-brand-primary text-white shadow-sm"
                                    : "text-brand-muted hover:bg-red-50 hover:text-brand-primary",
                            ].join(" ")}
                        >
                            <Icon size={19} aria-hidden="true" />
                            {item.label}
                        </NavLink>
                    );
                })}
            </nav>

        </aside>
    );
}

export default AccountSidebar;
