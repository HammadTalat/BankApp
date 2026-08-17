import { Link, useNavigate, useLocation } from "react-router";
import { useAuth } from "../../context/AuthContext.jsx"; // Adjust path based on your folder structure
import { ROUTES } from "../../routes/routePaths.js";

export const Sidebar = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const { signOut } = useAuth();

    const handleLogout = async () => {
        try {
            await signOut();
        } catch (err) {
            console.error("Logout error:", err);
        } finally {
            navigate(ROUTES.HOME);
        }
    };

    const navItems = [
        { label: "Dashboard", path: ROUTES.ACCOUNT_HOME },
        { label: "Deposit Money", path: ROUTES.ACCOUNT_DEPOSIT },
        { label: "Transfer Money", path: ROUTES.ACCOUNT_TRANSFERS },
        { label: "Transactions", path: ROUTES.ACCOUNT_TRANSACTIONS },
    ];

    return (
        <aside className="w-64 bg-brand-navy flex flex-col justify-between py-8 px-6 text-white shrink-0">
            <div>
                <div className="mb-10">
                    <h1 lang="ur" dir="rtl" className="text-xl font-bold tracking-tight text-white">ریڈ میتھ بینک</h1>
                    <p className="text-xs text-gray-400 mt-0.5">Secure Banking</p>
                </div>

                <nav className="space-y-2">
                    {navItems.map((item) => {
                        const isActive = location.pathname === item.path;
                        return (
                            <Link
                                key={item.path}
                                to={item.path}
                                className={`flex items-center px-4 py-3 text-sm rounded-lg transition-colors ${
                                    isActive
                                        ? "font-semibold text-white bg-white/10"
                                        : "font-medium text-gray-300 hover:text-white hover:bg-white/5"
                                }`}
                            >
                                {item.label}
                            </Link>
                        );
                    })}
                </nav>
            </div>

            <div>
                <button
                    onClick={handleLogout}
                    className="text-sm font-medium text-pink-300 hover:text-pink-200 transition-colors cursor-pointer"
                >
                    Logout
                </button>
            </div>
        </aside>
    );
};

export default Sidebar;
