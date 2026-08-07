import { useState, useEffect } from "react";
import { Link, useNavigate } from "react-router";
import { ROUTES } from "../../../routes/routePaths.js";
import { httpClient } from "../../../api/httpClient.js";

export const ProfilePage = () => {
    const navigate = useNavigate();

    // User Profile State
    const [profile, setProfile] = useState({
        name: "",
        email: "",
        address: "",
        role: "",
        approvalStatus: "PENDING",
    });

    // UI States
    const [isLoading, setIsLoading] = useState(true);
    const [errorMessage, setErrorMessage] = useState("");

    // Fetch logged-in user profile from /api/v1/me
    useEffect(() => {
        let isMounted = true;
        setIsLoading(true);

        httpClient
            .get("/api/v1/me")
            .then((data) => {
                if (isMounted && data) {
                    setProfile({
                        name: data.name || "",
                        email: data.email || "",
                        address: data.address || "",
                        role: data.role || "",
                        approvalStatus: data.approvalStatus || "PENDING",
                    });
                }
            })
            .catch((err) => {
                if (isMounted) {
                    setErrorMessage(err?.message || "Failed to load profile details.");
                }
            })
            .finally(() => {
                if (isMounted) setIsLoading(false);
            });

        return () => {
            isMounted = false;
        };
    }, []);

    const handleLogout = async () => {
        try {
            await httpClient.post("/api/v1/auth/logout");
        } catch (err) {
            console.error("Logout API error:", err);
        } finally {
            localStorage.removeItem("ACCESS_TOKEN");
            navigate(ROUTES.HOME);
        }
    };

    const getStatusBadgeStyle = (status) => {
        switch (status) {
            case "APPROVED":
                return "bg-emerald-50 text-emerald-600 border border-emerald-200";
            case "REJECTED":
                return "bg-red-50 text-red-600 border border-red-200";
            default:
                return "bg-amber-50 text-amber-600 border border-amber-200";
        }
    };

    return (
        <div className="flex min-h-screen bg-[#F8FAFC]">
            {/* Sidebar Navigation */}
            <aside className="w-64 bg-[#0F2942] flex flex-col justify-between py-8 px-6 text-white shrink-0">
                <div>
                    <div className="mb-10">
                        <h1 className="text-xl font-bold tracking-tight text-white">NexaBank</h1>
                        <p className="text-xs text-gray-400 mt-0.5">Personal Banking</p>
                    </div>

                    <nav className="space-y-2">
                        <Link
                            to={ROUTES.ACCOUNT_HOME}
                            className="flex items-center px-4 py-3 text-sm font-medium text-gray-300 hover:text-white hover:bg-white/5 rounded-lg transition-colors"
                        >
                            Dashboard
                        </Link>
                        <Link
                            to={ROUTES.ACCOUNT_DEPOSIT}
                            className="flex items-center px-4 py-3 text-sm font-medium text-gray-300 hover:text-white hover:bg-white/5 rounded-lg transition-colors"
                        >
                            Deposit Money
                        </Link>
                        <Link
                            to={ROUTES.ACCOUNT_TRANSFERS}
                            className="flex items-center px-4 py-3 text-sm font-medium text-gray-300 hover:text-white hover:bg-white/5 rounded-lg transition-colors"
                        >
                            Transfer Money
                        </Link>
                        <Link
                            to={ROUTES.ACCOUNT_TRANSACTIONS}
                            className="flex items-center px-4 py-3 text-sm font-medium text-gray-300 hover:text-white hover:bg-white/5 rounded-lg transition-colors"
                        >
                            Transactions
                        </Link>
                        <Link
                            to={ROUTES.ACCOUNT_PROFILE}
                            className="flex items-center px-4 py-3 text-sm font-semibold text-white bg-white/10 rounded-lg transition-colors"
                        >
                            Profile
                        </Link>
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

            {/* Main Content */}
            <main className="flex-1 p-10 overflow-y-auto">
                <div className="max-w-3xl">
                    <h2 className="text-3xl font-bold text-gray-900">Profile</h2>
                    <p className="text-gray-500 text-sm mt-1">
                        View your registered account and personal details.
                    </p>

                    <div className="mt-8 bg-white rounded-2xl border border-gray-100 shadow-sm p-8">
                        {isLoading ? (
                            <div className="flex items-center justify-center py-8">
                                <p className="text-xs text-gray-400">Loading profile details...</p>
                            </div>
                        ) : (
                            <div className="space-y-6">
                                {errorMessage && (
                                    <div className="p-4 rounded-xl text-xs font-medium bg-red-50 text-red-600 border border-red-200">
                                        {errorMessage}
                                    </div>
                                )}

                                {/* Account Status & Role Banner */}
                                <div className="flex items-center justify-between p-4 bg-gray-50 rounded-xl border border-gray-100">
                                    <div>
                                        <p className="text-xs font-bold text-gray-800">Account Status</p>
                                        <p className="text-xs text-gray-400 mt-0.5">
                                            Role: <span className="font-semibold text-gray-600">{profile.role || "N/A"}</span>
                                        </p>
                                    </div>
                                    <span
                                        className={`text-xs font-bold px-3 py-1 rounded-full uppercase tracking-wider ${getStatusBadgeStyle(
                                            profile.approvalStatus
                                        )}`}
                                    >
                                        {profile.approvalStatus}
                                    </span>
                                </div>

                                {/* Detail Display Fields */}
                                <div className="grid grid-cols-1 gap-6">
                                    <div>
                                        <label className="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-1">
                                            Full Name
                                        </label>
                                        <div className="w-full px-4 py-3 bg-gray-50 border border-gray-200/60 rounded-xl text-sm font-medium text-gray-800">
                                            {profile.name || "—"}
                                        </div>
                                    </div>

                                    <div>
                                        <label className="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-1">
                                            Email Address
                                        </label>
                                        <div className="w-full px-4 py-3 bg-gray-50 border border-gray-200/60 rounded-xl text-sm font-medium text-gray-800">
                                            {profile.email || "—"}
                                        </div>
                                    </div>

                                    <div>
                                        <label className="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-1">
                                            Residential Address
                                        </label>
                                        <div className="w-full px-4 py-3 bg-gray-50 border border-gray-200/60 rounded-xl text-sm font-medium text-gray-800 min-h-[72px] whitespace-pre-wrap">
                                            {profile.address || "No address provided."}
                                        </div>
                                    </div>
                                </div>
                            </div>
                        )}
                    </div>
                </div>
            </main>
        </div>
    );
};

export default ProfilePage;