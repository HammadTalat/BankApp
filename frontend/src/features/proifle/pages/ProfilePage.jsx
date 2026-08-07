import { useState, useEffect } from "react";
import { Link, useNavigate } from "react-router";
import { ROUTES } from "../../../routes/routePaths.js";
import { httpClient } from "../../../api/httpClient.js";
export const ProfilePage = () => {
    const navigate = useNavigate();

    // User details state
    const [userId] = useState("1"); // Replace with actual logged-in user ID or context state
    const [name, setName] = useState("Ali Khan");
    const [email, setEmail] = useState("ali.khan@example.com");
    const [address, setAddress] = useState("123 Main Street, Lahore, Pakistan");
    const [approvalStatus, setApprovalStatus] = useState("APPROVED");

    // UI state
    const [isLoading, setIsLoading] = useState(false);
    const [isSaving, setIsSaving] = useState(false);
    const [message, setMessage] = useState({ type: "", text: "" });

    // Fetch user details on component mount
    useEffect(() => {
        setIsLoading(true);
        httpClient
            .get(`/api/v1/admin/users/${userId}`)
            .then((data) => {
                if (data) {
                    setName(data.name || "");
                    setEmail(data.email || "");
                    setAddress(data.address || "");
                    setApprovalStatus(data.approvalStatus || "APPROVED");
                }
            })
            .catch((err) => {
                console.error("Failed to load user profile:", err);
            })
            .finally(() => setIsLoading(false));
    }, [userId]);

    const handleLogout = () => {
        localStorage.removeItem("ACCESS_TOKEN");
        navigate(ROUTES.HOME);
    };

    const handleSaveProfile = async (e) => {
        e.preventDefault();
        setIsSaving(true);
        setMessage({ type: "", text: "" });

        try {
            const updatedUser = await httpClient.patch(`/api/v1/admin/users/${userId}`, {
                name: name.trim(),
                email: email.trim(),
                address: address.trim(),
            });

            if (updatedUser) {
                setName(updatedUser.name);
                setEmail(updatedUser.email);
                setAddress(updatedUser.address);
            }

            setMessage({ type: "success", text: "Profile details updated successfully." });
        } catch (err) {
            setMessage({
                type: "error",
                text: err.message || "Failed to update profile details.",
            });
        } finally {
            setIsSaving(false);
        }
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
                        View and update your account information.
                    </p>

                    {/* Profile Information Card */}
                    <div className="mt-8 bg-white rounded-2xl border border-gray-100 shadow-sm p-8">
                        {isLoading ? (
                            <p className="text-xs text-gray-400 py-4">Loading profile information...</p>
                        ) : (
                            <form onSubmit={handleSaveProfile} className="space-y-6">
                                {message.text && (
                                    <div
                                        className={`p-4 rounded-xl text-xs font-medium ${
                                            message.type === "success"
                                                ? "bg-emerald-50 text-emerald-700 border border-emerald-200"
                                                : "bg-red-50 text-red-600 border border-red-200"
                                        }`}
                                    >
                                        {message.text}
                                    </div>
                                )}

                                {/* Account Status Indicator */}
                                <div className="flex items-center justify-between p-4 bg-gray-50 rounded-xl border border-gray-100">
                                    <div>
                                        <p className="text-xs font-bold text-gray-800">Account Status</p>
                                        <p className="text-xs text-gray-400 mt-0.5">
                                            Status verified by NexaBank administration
                                        </p>
                                    </div>
                                    <span className="inline-block bg-emerald-50 text-emerald-600 text-xs font-bold px-3 py-1 rounded-full uppercase tracking-wider">
                                        {approvalStatus}
                                    </span>
                                </div>

                                {/* Full Name Field */}
                                <div>
                                    <label className="block text-xs font-bold text-gray-800 mb-2">
                                        Full Name
                                    </label>
                                    <input
                                        type="text"
                                        value={name}
                                        onChange={(e) => setName(e.target.value)}
                                        required
                                        className="w-full px-4 py-3 border border-gray-200 rounded-xl text-sm focus:outline-none focus:border-blue-500 text-gray-800"
                                    />
                                </div>

                                {/* Email Field */}
                                <div>
                                    <label className="block text-xs font-bold text-gray-800 mb-2">
                                        Email Address
                                    </label>
                                    <input
                                        type="email"
                                        value={email}
                                        onChange={(e) => setEmail(e.target.value)}
                                        required
                                        className="w-full px-4 py-3 border border-gray-200 rounded-xl text-sm focus:outline-none focus:border-blue-500 text-gray-800"
                                    />
                                </div>

                                {/* Residential Address Field */}
                                <div>
                                    <label className="block text-xs font-bold text-gray-800 mb-2">
                                        Residential Address
                                    </label>
                                    <textarea
                                        rows={3}
                                        value={address}
                                        onChange={(e) => setAddress(e.target.value)}
                                        className="w-full px-4 py-3 border border-gray-200 rounded-xl text-sm focus:outline-none focus:border-blue-500 text-gray-800 resize-none"
                                    />
                                </div>

                                {/* Submit Button */}
                                <div className="pt-2">
                                    <button
                                        type="submit"
                                        disabled={isSaving}
                                        className="w-full bg-[#2563EB] hover:bg-blue-700 disabled:opacity-50 text-white font-semibold py-3.5 px-4 rounded-xl text-sm transition-colors shadow-sm"
                                    >
                                        {isSaving ? "Saving..." : "Save Changes"}
                                    </button>
                                </div>
                            </form>
                        )}
                    </div>
                </div>
            </main>
        </div>
    );
};

export default ProfilePage;