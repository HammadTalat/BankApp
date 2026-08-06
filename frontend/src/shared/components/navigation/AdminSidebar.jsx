import {
    Building2,
    Landmark,
    LayoutDashboard,
    UsersRound,
    X,
} from "lucide-react";
import { NavLink } from "react-router";

import { ROUTES } from "../../../routes/routePaths";

const navigationItems = [
    {
        label: "Dashboard",
        path: ROUTES.ADMIN_HOME,
        icon: LayoutDashboard,
        end: true,
    },
    {
        label: "Pending Users",
        path: ROUTES.ADMIN_PENDING_USERS,
        icon: UsersRound,
    },
    {
        label: "Bank Accounts",
        path: ROUTES.ADMIN_ACCOUNTS,
        icon: Building2,
    },
];

function AdminSidebar({
    isOpen = false,
    onClose,
}) {
    const sidebarClasses = [
        "fixed inset-y-0 left-0 z-50 flex w-72 max-w-[85vw] flex-col bg-brand-navy text-white shadow-xl",
        "transition-transform duration-200 ease-out lg:w-64 lg:max-w-none lg:translate-x-0 lg:shadow-none",
        isOpen ? "translate-x-0" : "-translate-x-full",
    ].join(" ");

    return (
        <aside
            id="admin-sidebar"
            aria-label="Admin navigation"
            className={sidebarClasses}
        >
            <div className="flex min-h-20 items-center gap-3 border-b border-white/10 px-5">
                <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-brand-primary text-white shadow-sm">
                    <Landmark
                        size={23}
                        aria-hidden="true"
                    />
                </div>

                <div className="min-w-0 flex-1">
                    <p className="truncate text-lg font-bold">
                        NexaBank
                    </p>
                    <p className="text-xs font-medium text-slate-300">
                        Admin Portal
                    </p>
                </div>

                <button
                    type="button"
                    aria-label="Close admin navigation"
                    className="inline-flex h-10 w-10 shrink-0 items-center justify-center rounded-xl text-slate-300 transition-colors hover:bg-white/10 hover:text-white focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-white/70 lg:hidden"
                    onClick={onClose}
                >
                    <X
                        size={21}
                        aria-hidden="true"
                    />
                </button>
            </div>

            <nav className="flex-1 space-y-1 overflow-y-auto p-4">
                {navigationItems.map((item) => {
                    const Icon = item.icon;

                    return (
                        <NavLink
                            key={item.path}
                            to={item.path}
                            end={item.end}
                            onClick={onClose}
                            className={({ isActive }) => [
                                "flex min-h-11 items-center gap-3 rounded-xl px-3.5 py-2.5 text-sm font-semibold transition-colors",
                                "focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-white/70",
                                isActive
                                    ? "bg-brand-primary text-white shadow-sm"
                                    : "text-slate-300 hover:bg-white/10 hover:text-white",
                            ].join(" ")}
                        >
                            <Icon
                                size={19}
                                className="shrink-0"
                                aria-hidden="true"
                            />
                            <span>{item.label}</span>
                        </NavLink>
                    );
                })}
            </nav>

        </aside>
    );
}

export default AdminSidebar;
