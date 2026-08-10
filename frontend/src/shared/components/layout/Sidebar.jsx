import {
    ArrowDownLeft,
    ArrowUpRight,
    Landmark,
    LayoutDashboard,
    LogOut,
    ReceiptText,
    UserRound,
} from "lucide-react";
import { Link, useLocation, useNavigate } from "react-router";

import { useAuth } from "../../../features/auth/context/useAuth.js";
import { ROUTES } from "../../../routes/routePaths.js";

export const Sidebar = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const { signOut } = useAuth();

    const handleLogout = async () => {
        try {
            if (signOut) {
                await signOut();
            } else {
                localStorage.removeItem("ACCESS_TOKEN");
            }
        } catch (err) {
            console.error("Logout error:", err);
        } finally {
            navigate(ROUTES.HOME);
        }
    };

    const navItems = [
        { label: "Dashboard", path: ROUTES.ACCOUNT_HOME, icon: LayoutDashboard },
        { label: "Deposit Money", path: ROUTES.ACCOUNT_DEPOSIT, icon: ArrowDownLeft },
        { label: "Transfer Money", path: ROUTES.ACCOUNT_TRANSFERS, icon: ArrowUpRight },
        { label: "Transactions", path: ROUTES.ACCOUNT_TRANSACTIONS, icon: ReceiptText },
        { label: "Profile", path: ROUTES.ACCOUNT_PROFILE, icon: UserRound },
    ];

    return (
        <aside className="fixed inset-y-0 left-0 z-50 flex w-64 max-w-[85vw] flex-col bg-brand-navy text-white shadow-xl transition-transform duration-200 ease-out lg:static lg:w-64 lg:max-w-none lg:translate-x-0 lg:shadow-none shrink-0">
            {/* Header / Logo */}
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
                        Personal Banking
                    </p>
                </div>
            </div>

            {/* Navigation Links */}
            <nav className="flex-1 space-y-1 overflow-y-auto p-4">
                {navItems.map((item) => {
                    const Icon = item.icon;
                    const isActive = location.pathname === item.path;

                    return (
                        <Link
                            key={item.path}
                            to={item.path}
                            className={`flex min-h-11 items-center gap-3 rounded-xl px-3.5 py-2.5 text-sm font-semibold transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-white/70 ${
                                isActive
                                    ? "bg-brand-primary text-white shadow-sm"
                                    : "text-slate-300 hover:bg-white/10 hover:text-white"
                            }`}
                        >
                            <Icon
                                size={19}
                                className="shrink-0"
                                aria-hidden="true"
                            />
                            <span>{item.label}</span>
                        </Link>
                    );
                })}
            </nav>

            {/* Footer / Logout */}
            <div className="border-t border-white/10 p-4">
                <button
                    type="button"
                    onClick={handleLogout}
                    className="flex w-full min-h-11 items-center gap-3 rounded-xl px-3.5 py-2.5 text-sm font-semibold text-rose-300 transition-colors hover:bg-white/10 hover:text-rose-200 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-white/70 cursor-pointer"
                >
                    <LogOut
                        size={19}
                        className="shrink-0"
                        aria-hidden="true"
                    />
                    <span>Logout</span>
                </button>
            </div>
        </aside>
    );
};

export default Sidebar;