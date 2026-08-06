import { useState, useEffect } from "react";
import { Link, useNavigate } from "react-router";
import { ROUTES } from "../../../routes/routePaths.js";
import { httpClient } from "../../../api/httpClient.js";

// PRODUCTION BEST PRACTICE NOTE:
// 1. JWT Tokens should be handled via HttpOnly Cookies sent automatically by the browser with `credentials: 'include'`.
// 2. User info (name, account number) should be provided by an Auth Context / central store or fetched via a GET /user/me endpoint.

export const DashboardPage = () => {
    const navigate = useNavigate();

    // UI & State Management
    const [copied, setCopied] = useState(false);
    const [transactions, setTransactions] = useState([]);
    const [loadingTransactions, setLoadingTransactions] = useState(true);

    // Balance state
    const [balance, setBalance] = useState("25500.00");
    const [refreshingBalance, setRefreshingBalance] = useState(false);

    /* ========================================================================
       1. PRODUCTION AUTH & USER STATE (Commented out until Auth integration)
       ========================================================================
       In a production app, user details shouldn't be hardcoded. You would
       consume user data from an Auth Context or an authenticated profile endpoint.

       const { user, logout } = useAuth(); // Custom hook accessing Auth Context

       OR fetch user data on mount:
       const [userData, setUserData] = useState(null);
       useEffect(() => {
           httpClient.get("/api/v1/user/me")
               .then((data) => setUserData(data))
               .catch((err) => console.error("Failed to fetch user profile", err));
       }, []);
    */

    // HARDCODED FALLBACK USER DATA (Temporary for UI development)
    const userData = {
        name: "Ali Khan",
        accountNumber: "5839 2017 4638 2915",
        status: "ACTIVE"
    };

    // Fetch account balance from backend
    const fetchBalance = () => {
        setRefreshingBalance(true);
        httpClient
            .get("/api/v1/account/balance")
            .then((data) => {
                if (data?.amount !== undefined) {
                    setBalance(data.amount);
                }
            })
            .catch((err) => {
                console.error("Failed to refresh balance:", err);
            })
            .finally(() => setRefreshingBalance(false));
    };

    // Fetch recent transactions on mount
    useEffect(() => {
        let isMounted = true; // Cleanup flag to prevent memory leaks on unmount

        httpClient
            .get("/api/v1/transaction/get-transactions?page=0&size=5")
            .then((data) => {
                if (isMounted && data?.transactions) {
                    setTransactions(data.transactions);
                }
            })
            .catch((err) => {
                if (isMounted) console.error("Failed to load transactions:", err);
            })
            .finally(() => {
                if (isMounted) setLoadingTransactions(false);
            });

        fetchBalance();

        return () => {
            isMounted = false;
        };
    }, []);

    const handleCopyAccount = () => {
        if (!userData?.accountNumber) return;

        const cleanAccountNumber = userData.accountNumber.replace(/\s+/g, "");
        navigator.clipboard.writeText(cleanAccountNumber);
        setCopied(true);
        setTimeout(() => setCopied(false), 2000);
    };

    const handleLogout = () => {
        /* ========================================================================
           2. PRODUCTION LOGOUT LOGIC (Commented out until Auth integration)
           ========================================================================
           Instead of deleting local-storage tokens manually, call the backend
           logout endpoint to clear the HttpOnly session cookie, clear in-memory state,
           and redirect.

           httpClient.post("/api/v1/auth/logout")
               .then(() => {
                   // Clear Auth Context / state here if using a provider
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
            {/* Sidebar */}
            <aside className="w-64 bg-[#0F2942] flex flex-col justify-between py-8 px-6 text-white shrink-0">
                <div>
                    <div className="mb-10">
                        <h1 className="text-xl font-bold tracking-tight text-white">
                            NexaBank
                        </h1>
                        <p className="text-xs text-gray-400 mt-0.5">
                            Personal Banking
                        </p>
                    </div>

                    <nav className="space-y-2">
                        <Link
                            to={ROUTES.ACCOUNT_HOME}
                            className="flex items-center px-4 py-3 text-sm font-semibold text-white bg-white/10 rounded-lg transition-colors"
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
                            className="flex items-center px-4 py-3 text-sm font-medium text-gray-300 hover:text-white hover:bg-white/5 rounded-lg transition-colors"
                        >
                            Profile
                        </Link>
                    </nav>
                </div>

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
                <div className="mb-8">
                    <h2 className="text-3xl font-bold text-gray-900">
                        Account Dashboard
                    </h2>
                    <p className="text-gray-500 text-sm mt-1">
                        Welcome back, {userData.name}
                    </p>
                </div>

                {/* Top Cards Row */}
                <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">
                    {/* Available Balance Card */}
                    <div className="lg:col-span-2 bg-[#2563EB] rounded-2xl p-8 text-white flex flex-col justify-between shadow-sm">
                        <div>
                            <div className="flex items-center justify-between">
                                <p className="text-blue-100 text-sm font-medium">
                                    Available Balance
                                </p>
                                <button
                                    onClick={fetchBalance}
                                    disabled={refreshingBalance}
                                    title="Refresh Balance"
                                    className="p-1.5 rounded-lg bg-white/10 hover:bg-white/20 text-blue-100 hover:text-white transition-colors disabled:opacity-50"
                                >
                                    <svg
                                        className={`w-4 h-4 ${
                                            refreshingBalance ? "animate-spin" : ""
                                        }`}
                                        fill="none"
                                        stroke="currentColor"
                                        viewBox="0 0 24 24"
                                    >
                                        <path
                                            strokeLinecap="round"
                                            strokeLinejoin="round"
                                            strokeWidth="2"
                                            d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"
                                        />
                                    </svg>
                                </button>
                            </div>

                            <h3 className="text-4xl font-extrabold mt-3 tracking-tight">
                                PKR {Number(balance).toLocaleString("en-US", {
                                minimumFractionDigits: 2,
                                maximumFractionDigits: 2,
                            })}
                            </h3>
                            <p className="text-blue-200 text-xs mt-3">
                                Updated a few moments ago
                            </p>
                        </div>

                        <div className="flex gap-4 mt-8">
                            <button
                                onClick={() => navigate(ROUTES.ACCOUNT_TRANSFERS)}
                                className="bg-white text-blue-600 px-6 py-2.5 rounded-lg text-sm font-semibold hover:bg-blue-50 transition-colors shadow-sm"
                            >
                                Transfer Money
                            </button>
                            <button
                                onClick={() => navigate(ROUTES.ACCOUNT_TRANSACTIONS)}
                                className="bg-white/20 text-white border border-white/30 px-6 py-2.5 rounded-lg text-sm font-semibold hover:bg-white/30 transition-colors"
                            >
                                View Transactions
                            </button>
                        </div>
                    </div>

                    {/* Account Details Card */}
                    <div className="bg-white rounded-2xl p-6 border border-gray-100 shadow-sm flex flex-col justify-between">
                        <div>
                            <p className="text-xs font-medium text-gray-400">
                                Account number
                            </p>
                            <p className="text-xl font-bold text-gray-900 mt-2 tracking-wide">
                                {userData.accountNumber}
                            </p>

                            <div className="mt-4">
                                <span className="inline-block bg-emerald-50 text-emerald-600 text-xs font-bold px-3 py-1 rounded-full uppercase tracking-wider">
                                    {userData.status}
                                </span>
                            </div>

                            <p className="text-sm font-medium text-gray-700 mt-4">
                                {userData.name}
                            </p>
                        </div>

                        <div className="mt-6">
                            <button
                                onClick={handleCopyAccount}
                                className="w-full bg-gray-50 hover:bg-gray-100 text-gray-700 border border-gray-200 py-2.5 px-4 rounded-lg text-xs font-semibold transition-colors text-center"
                            >
                                {copied ? "Copied!" : "Copy account number"}
                            </button>
                        </div>
                    </div>
                </div>

                {/* Recent Transactions */}
                <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-8">
                    <h3 className="text-lg font-bold text-gray-900 mb-6">
                        Recent transactions
                    </h3>

                    {loadingTransactions ? (
                        <p className="text-sm text-gray-500 py-4">Loading transactions...</p>
                    ) : transactions.length === 0 ? (
                        <p className="text-sm text-gray-500 py-4">No recent transactions found.</p>
                    ) : (
                        <div className="divide-y divide-gray-100">
                            {transactions.map((tx) => {
                                const isDebit = tx.indicator === "DEBIT";
                                const formattedDate = tx.transactionDate
                                    ? new Date(tx.transactionDate).toLocaleDateString("en-GB", {
                                        day: "2-digit",
                                        month: "short",
                                        year: "numeric",
                                    })
                                    : "N/A";

                                return (
                                    <div key={tx.id || tx.operationId} className="py-5 flex items-center justify-between">
                                        <div>
                                            <p className="text-sm font-semibold text-gray-900">
                                                {tx.description || "Transfer"}
                                            </p>
                                            <p className="text-xs text-gray-400 mt-1">
                                                {formattedDate}
                                            </p>
                                        </div>

                                        <div className="flex items-center gap-16">
                                            <span
                                                className={`text-xs font-bold uppercase tracking-wider ${
                                                    isDebit ? "text-red-500" : "text-emerald-600"
                                                }`}
                                            >
                                                {tx.indicator}
                                            </span>
                                            <span
                                                className={`text-sm font-bold min-w-[100px] text-right ${
                                                    isDebit ? "text-red-500" : "text-emerald-600"
                                                }`}
                                            >
                                                {isDebit ? "-" : "+"} PKR {tx.amount?.toLocaleString()}
                                            </span>
                                        </div>
                                    </div>
                                );
                            })}
                        </div>
                    )}
                </div>
            </main>
        </div>
    );
};

export default DashboardPage;