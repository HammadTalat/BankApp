import { useState, useEffect, useCallback } from "react";
import { useNavigate } from "react-router";
import { ROUTES } from "../../../../routes/routePaths.js";
import { useAuth } from "../../../auth/context/useAuth.js";
import AccountHeader from "../../../../shared/components/navigation/AccountHeader.jsx";
import AccountSidebar from "../../../../shared/components/navigation/AccountSidebar.jsx";
import { dashboardService } from "../api/dashboardService.js";

export const DashboardPage = () => {
    const { user, signOut, loading: authLoading } = useAuth();
    const navigate = useNavigate();

    // Component State
    const [copied, setCopied] = useState(false);
    const [transactions, setTransactions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [refreshingBalance, setRefreshingBalance] = useState(false);

    const [accountDetails, setAccountDetails] = useState({
        accountNumber: "",
        status: "ACTIVE",
    });
    const [balance, setBalance] = useState("0.00");

    // Fetch only balance (for manual refresh)
    const fetchBalance = async () => {
        setRefreshingBalance(true);
        try {
            const data = await dashboardService.getAccountBalance();
            if (data?.amount !== undefined) {
                setBalance(data.amount);
            }
        } catch (err) {
            console.error("Failed to refresh balance:", err);
        } finally {
            setRefreshingBalance(false);
        }
    };

    // Initial dashboard data load
    const loadDashboardData = useCallback(async () => {
        setLoading(true);
        try {
            const [accountData, balanceData, transactionsData] = await Promise.all([
                dashboardService.getAccountDetails().catch((err) => {
                    console.error("Failed to load account details:", err);
                    return null;
                }),
                dashboardService.getAccountBalance().catch((err) => {
                    console.error("Failed to load balance:", err);
                    return null;
                }),
                dashboardService.getRecentTransactions(0, 5).catch((err) => {
                    console.error("Failed to load transactions:", err);
                    return null;
                }),
            ]);

            if (accountData) {
                setAccountDetails({
                    accountNumber: accountData.accountNumber || "",
                    status: accountData.status || "ACTIVE",
                });
            }

            if (balanceData?.amount !== undefined) {
                setBalance(balanceData.amount);
            }

            if (transactionsData?.transactions) {
                setTransactions(transactionsData.transactions);
            }
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        if (!authLoading) {
            loadDashboardData();
        }
    }, [loadDashboardData, authLoading]);
    // Actions
    const handleCopyAccount = () => {
        if (!accountDetails.accountNumber) return;
        navigator.clipboard.writeText(accountDetails.accountNumber.replace(/\s+/g, ""));
        setCopied(true);
        setTimeout(() => setCopied(false), 2000);
    };

    const handleLogout = () => {
        if (signOut) {
            signOut();
        } else {
            localStorage.removeItem("ACCESS_TOKEN");
            navigate(ROUTES.HOME);
        }
    };

    const formatAccountNumber = (num) => {
        if (!num) return "Loading...";
        return num.replace(/(.{4})/g, "$1 ").trim();
    };

    if (authLoading) {
        return (
            <div className="flex min-h-screen items-center justify-center bg-brand-background">
                <p className="text-gray-500 font-medium">Loading session...</p>
            </div>
        );
    }

    return (
        <div className="flex min-h-screen bg-brand-background">
            <AccountSidebar />

            <div className="flex min-w-0 flex-1 flex-col">
                <AccountHeader accountProfile={user} onLogout={handleLogout} />

                <main className="flex-1 overflow-y-auto px-6 py-6 sm:px-8 sm:py-8">
                    <div className="mx-auto w-full max-w-[1600px]">
                        <div className="mb-8">
                            <h2 className="text-3xl font-bold text-gray-900">
                                Account Dashboard
                            </h2>
                            <p className="text-gray-500 text-sm mt-1">
                                Welcome back, {user?.name || "User"}
                            </p>
                        </div>

                        {/* Top Cards Row */}
                        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">
                            {/* Available Balance Card */}
                            <div className="lg:col-span-2 bg-brand-primary rounded-xl p-8 text-white flex flex-col justify-between shadow-sm">
                                <div>
                                    <div className="flex items-center justify-between">
                                        <p className="text-red-100 text-sm font-medium">
                                            Available Balance
                                        </p>
                                        <button
                                            onClick={fetchBalance}
                                            disabled={refreshingBalance}
                                            title="Refresh Balance"
                                            className="p-1.5 rounded-lg bg-white/10 hover:bg-white/20 text-red-100 hover:text-white transition-colors disabled:opacity-50"
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
                                        PKR{" "}
                                        {Number(balance).toLocaleString("en-US", {
                                            minimumFractionDigits: 2,
                                            maximumFractionDigits: 2,
                                        })}
                                    </h3>
                                    <p className="text-red-100 text-xs mt-3">
                                        Updated a few moments ago
                                    </p>
                                </div>

                                <div className="flex gap-4 mt-8">
                                    <button
                                        onClick={() => navigate(ROUTES.ACCOUNT_TRANSFERS)}
                                        className="bg-white text-brand-primary px-6 py-2.5 rounded-lg text-sm font-semibold hover:bg-red-50 transition-colors shadow-sm"
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
                            <div className="bg-white rounded-xl p-6 border border-brand-border shadow-sm flex flex-col justify-between">
                                <div>
                                    <p className="text-xs font-medium text-gray-400">
                                        Account number
                                    </p>
                                    <p className="text-xl font-bold text-gray-900 mt-2 tracking-wide">
                                        {formatAccountNumber(accountDetails.accountNumber)}
                                    </p>

                                    <div className="mt-4">
                    <span
                        className={`inline-block text-xs font-bold px-3 py-1 rounded-full uppercase tracking-wider ${
                            accountDetails.status === "ACTIVE"
                                ? "bg-emerald-50 text-emerald-600"
                                : "bg-red-50 text-red-600"
                        }`}
                    >
                      {accountDetails.status}
                    </span>
                                    </div>

                                    <p className="text-sm font-medium text-gray-700 mt-4">
                                        {user?.name || "Account Holder"}
                                    </p>
                                </div>

                                <div className="mt-6">
                                    <button
                                        onClick={handleCopyAccount}
                                        disabled={!accountDetails.accountNumber}
                                        className="w-full bg-gray-50 hover:bg-gray-100 text-gray-700 border border-gray-200 py-2.5 px-4 rounded-lg text-xs font-semibold transition-colors text-center disabled:opacity-50"
                                    >
                                        {copied ? "Copied!" : "Copy account number"}
                                    </button>
                                </div>
                            </div>
                        </div>

                        {/* Recent Transactions */}
                        <div className="bg-white rounded-xl border border-brand-border shadow-sm p-8">
                            <h3 className="text-lg font-bold text-gray-900 mb-6">
                                Recent transactions
                            </h3>

                            {loading ? (
                                <p className="text-sm text-gray-500 py-4">
                                    Loading transactions...
                                </p>
                            ) : transactions.length === 0 ? (
                                <p className="text-sm text-gray-500 py-4">
                                    No recent transactions found.
                                </p>
                            ) : (
                                <div className="divide-y divide-gray-100">
                                    {transactions.map((tx) => {
                                        const isDebit = tx.indicator === "DEBIT";
                                        const formattedDate = tx.transactionDate
                                            ? new Date(tx.transactionDate).toLocaleDateString(
                                                "en-GB",
                                                {
                                                    day: "2-digit",
                                                    month: "short",
                                                    year: "numeric",
                                                }
                                            )
                                            : "N/A";

                                        return (
                                            <div
                                                key={tx.id || tx.operationId}
                                                className="py-5 flex items-center justify-between"
                                            >
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
                            {isDebit ? "-" : "+"} PKR{" "}
                                                        {tx.amount?.toLocaleString()}
                          </span>
                                                </div>
                                            </div>
                                        );
                                    })}
                                </div>
                            )}
                        </div>
                    </div>
                </main>
            </div>
        </div>
    );
};

export default DashboardPage;