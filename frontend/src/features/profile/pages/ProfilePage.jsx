import { useAuth } from "../../auth/context/useAuth.js";

export const ProfilePage = () => {
    const { user, isInitializing } = useAuth();

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
        <div className="max-w-3xl">
            <h2 className="text-3xl font-bold text-gray-900">Profile</h2>
            <p className="text-gray-500 text-sm mt-1">
                View your registered account and personal details.
            </p>

            <div className="mt-8 bg-white rounded-2xl border border-gray-100 shadow-sm p-8">
                {isInitializing ? (
                    <div className="flex items-center justify-center py-8">
                        <p className="text-xs text-gray-400">Loading profile details...</p>
                    </div>
                ) : (
                    <div className="space-y-6">
                        {/* Account Status & Role Banner */}
                        <div className="flex items-center justify-between p-4 bg-gray-50 rounded-xl border border-gray-100">
                            <div>
                                <p className="text-xs font-bold text-gray-800">Account Status</p>
                                <p className="text-xs text-gray-400 mt-0.5">
                                    Role: <span className="font-semibold text-gray-600">{user?.role || "N/A"}</span>
                                </p>
                            </div>
                            <span
                                className={`text-xs font-bold px-3 py-1 rounded-full uppercase tracking-wider ${getStatusBadgeStyle(
                                    user?.approvalStatus || "PENDING"
                                )}`}
                            >
                                {user?.approvalStatus || "PENDING"}
                            </span>
                        </div>

                        {/* Read-Only Profile Details */}
                        <div className="grid grid-cols-1 gap-6">
                            <div>
                                <label className="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-1">
                                    Full Name
                                </label>
                                <div className="w-full px-4 py-3 bg-gray-50 border border-gray-200/60 rounded-xl text-sm font-medium text-gray-800">
                                    {user?.name || "—"}
                                </div>
                            </div>

                            <div>
                                <label className="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-1">
                                    Email Address
                                </label>
                                <div className="w-full px-4 py-3 bg-gray-50 border border-gray-200/60 rounded-xl text-sm font-medium text-gray-800">
                                    {user?.email || "—"}
                                </div>
                            </div>

                            <div>
                                <label className="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-1">
                                    Residential Address
                                </label>
                                <div className="w-full px-4 py-3 bg-gray-50 border border-gray-200/60 rounded-xl text-sm font-medium text-gray-800 min-h-[72px] whitespace-pre-wrap">
                                    {user?.address || "No address provided."}
                                </div>
                            </div>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default ProfilePage;