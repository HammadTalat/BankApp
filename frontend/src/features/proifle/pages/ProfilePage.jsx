import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router";
import { ROUTES } from "../../../routes/routePaths.js";
import { httpClient } from "../../../api/httpClient.js";

// PRODUCTION BEST PRACTICE NOTE:
// 1. JWT Tokens should be handled via HttpOnly Cookies sent automatically by the browser with `credentials: 'include'`.
// 2. Profile details should be fetched from a dedicated self-service user endpoint (e.g., GET /api/v1/users/me or Auth Context)
//    rather than calling administrative endpoints (/api/v1/admin/users/:id) from a client application.

export const ProfilePage = () => {
    const navigate = useNavigate();

    // UI & State Management
    const [isLoading, setIsLoading] = useState(false);

    // HARDCODED FALLBACK USER DATA (Temporary for UI development)
    const [profile, setProfile] = useState({
        name: "Ali Khan",
        email: "ali.khan@example.com",
        address: "123 Main Street, Lahore, Pakistan",
        approvalStatus: "APPROVED",
        accountNumber: "5839 2017 4638 2915",
    });

    // Fetch user details on component mount
    useEffect(() => {
        let isMounted = true;
        setIsLoading(true);

        /* ========================================================================
           PRODUCTION API CALL (Commented out until Auth & User endpoints are ready)
           ========================================================================
           httpClient
               .get("/api/v1/users/me")
               .then((data) => {
                   if (isMounted && data) {
                       setProfile({
                           name: data.name || "",
                           email: data.email || "",
                           address: data.address || "",
                           approvalStatus: data.approvalStatus || "APPROVED",
                           accountNumber: data.accountNumber || "N/A",
                       });
                   }
               })
               .catch((err) => {
                   if (isMounted) console.error("Failed to load user profile:", err);
               })
               .finally(() => {
                   if (isMounted) setIsLoading(false);
               });
        */

        // TEMPORARY DELAY TO SIMULATE API LOADING
        const timer = setTimeout(() => {
            if (isMounted) setIsLoading(false);
        }, 300);

        return () => {
            isMounted = false;
            clearTimeout(timer);
        };
    }, []);

    const handleLogout = () => {
        /* ========================================================================
           PRODUCTION LOGOUT LOGIC (Commented out until Auth integration)
           ========================================================================
           httpClient.post("/api/v1/auth/logout")
               .then(() => {
                   navigate(ROUTES.HOME);
               })
               .catch((err) => console.error("Logout failed:", err));
        */

        // TEMPORARY LOGOUT FALLBACK
        localStorage.removeItem("ACCESS_TOKEN");
        navigate(ROUTES.HOME);
    };

    return (
        <div className="flex min-h-screen bg-[#F8FAFC]">
            {/* Dark Blue Sidebar */}
            <aside className="w-64 bg-[#0F2942] flex flex-col justify-between py-8 px-6 text-white shrink-0">
                <div>
                    {/* Brand Header */}
                    <div className="mb-10">
                        <h1 className="text-xl font-bold tracking-tight text-white">NexaBank</h1>
                        <p className="text-xs text-gray-400 mt-0.5">Personal Banking</p>
                    </div>

                    {/* Navigation */}
                    <nav className="space-y-2">
                        <Link
                            to={ROUTES.ACCOUNT_HOME}
                            className="flex items-center px-4 py-3 text-sm font-medium text-gray-300 hover:text-white hover:bg-white/5 rounded-lg transition-colors"
                        >
                            Dashboard
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

                {/* Logout Action */}
                <div>
                    <button
                        onClick={handleLogout}
                        className="text-sm font-medium text-pink-300 hover:text-pink-200 transition-colors"
                    >
                        Logout
                    </button>
                </div>
            </aside>

            {/* Main Content Area */}
            <main className="flex-1 p-10 overflow-y-auto">
                <div className="max-w-3xl">
                    {/* Page Header */}
                    <h2 className="text-3xl font-bold text-gray-900">Profile</h2>
                    <p className="text-gray-500 text-sm mt-1">
                        View your registered personal and account information.
                    </p>

                    {/* Read-Only Profile Card */}
                    <div className="mt-8 bg-white rounded-2xl border border-gray-100 shadow-sm p-8">
                        {isLoading ? (
                            <p className="text-xs text-gray-400 py-4">Loading profile information...</p>
                        ) : (
                            <div className="space-y-6">
                                {/* Account Status Badge */}
                                <div className="flex items-center justify-between p-4 bg-gray-50 rounded-xl border border-gray-100">
                                    <div>
                                        <p className="text-xs font-bold text-gray-800">Account Status</p>
                                        <p className="text-xs text-gray-400 mt-0.5">
                                            Status verified by NexaBank administration
                                        </p>
                                    </div>
                                    <span className="inline-block bg-emerald-50 text-emerald-600 text-xs font-bold px-3 py-1 rounded-full uppercase tracking-wider">
                                        {profile.approvalStatus}
                                    </span>
                                </div>

                                {/* Full Name Field */}
                                <div className="pb-4 border-b border-gray-100">
                                    <p className="text-xs font-bold text-gray-400 uppercase tracking-wider mb-1">
                                        Full Name
                                    </p>
                                    <p className="text-sm font-semibold text-gray-900">
                                        {profile.name}
                                    </p>
                                </div>

                                {/* Email Address Field */}
                                <div className="pb-4 border-b border-gray-100">
                                    <p className="text-xs font-bold text-gray-400 uppercase tracking-wider mb-1">
                                        Email Address
                                    </p>
                                    <p className="text-sm font-semibold text-gray-900">
                                        {profile.email}
                                    </p>
                                </div>

                                {/* Account Number Field */}
                                <div className="pb-4 border-b border-gray-100">
                                    <p className="text-xs font-bold text-gray-400 uppercase tracking-wider mb-1">
                                        Account Number
                                    </p>
                                    <p className="text-sm font-semibold text-gray-900 tracking-wide">
                                        {profile.accountNumber}
                                    </p>
                                </div>

                                {/* Residential Address Field */}
                                <div>
                                    <p className="text-xs font-bold text-gray-400 uppercase tracking-wider mb-1">
                                        Residential Address
                                    </p>
                                    <p className="text-sm font-semibold text-gray-900 leading-relaxed">
                                        {profile.address}
                                    </p>
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