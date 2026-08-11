import { useState, useEffect } from "react";
import { useNavigate } from "react-router";
import { ROUTES } from "../../../../routes/routePaths.js";
import { useAuth } from "../../../auth/context/useAuth.js";
import { transactionService } from "../api/transactionService.js";
import AccountHeader from "../../../../shared/components/navigation/AccountHeader.jsx";
import AccountSidebar from "../../../../shared/components/navigation/AccountSidebar.jsx";

const getStartOfMonth = () => {
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, "0");
    return `${year}-${month}-01`;
};

const getToday = () => {
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, "0");
    const day = String(now.getDate()).padStart(2, "0");
    return `${year}-${month}-${day}`;
};

export const TransactionsPage = () => {
    const navigate = useNavigate();
    const { user } = useAuth();

    // Default filters
    const [fromDate, setFromDate] = useState(getStartOfMonth);
    const [toDate, setToDate] = useState(getToday);
    const [typeFilter, setTypeFilter] = useState("ALL"); // ALL, CREDIT, DEBIT

    // Applied filter states to trigger API calls on submit
    const [appliedFilters, setAppliedFilters] = useState({
        fromDate: getStartOfMonth(),
        toDate: getToday(),
        typeFilter: "ALL",
    });

    // Pagination state
    const [page, setPage] = useState(0);
    const [pageSize] = useState(20);
    const [totalPages, setTotalPages] = useState(1);

    // Data states
    const [transactions, setTransactions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    const handleLogout = async () => {
        try {
            await transactionService.logout();
        } catch {
            // Proceed with client cleanup regardless of server response
        } finally {
            localStorage.removeItem("ACCESS_TOKEN");
            navigate(ROUTES.HOME);
        }
    };

    useEffect(() => {
        let isActive = true;

        async function loadTransactions() {
            try {
                const data = await transactionService.getTransactions({
                    page,
                    pageSize,
                    fromDate: appliedFilters.fromDate,
                    toDate: appliedFilters.toDate,
                });

                if (!isActive) return;

                setError("");
                setTransactions(data?.transactions || []);
                setTotalPages(data?.totalPages || 1);
            } catch (err) {
                if (!isActive) return;

                console.error("Failed to fetch transactions:", err);
                setError(err.message || "Failed to load transactions.");
                setTransactions([]);
            } finally {
                if (isActive) setLoading(false);
            }
        }

        loadTransactions();

        return () => {
            isActive = false;
        };
    }, [page, pageSize, appliedFilters]);

    const handleApplyFilters = () => {
        setLoading(true);
        setPage(0);
        setAppliedFilters({
            fromDate,
            toDate,
            typeFilter,
        });
    };

    const handleClearFilters = () => {
        setLoading(true);
        setFromDate("");
        setToDate("");
        setTypeFilter("ALL");
        setPage(0);
        setAppliedFilters({
            fromDate: "",
            toDate: "",
            typeFilter: "ALL",
        });
    };

    // Client-side indicator filter application on fetched page dataset
    const displayedTransactions = transactions.filter((tx) => {
        if (appliedFilters.typeFilter === "ALL") return true;
        return tx.indicator === appliedFilters.typeFilter;
    });

    return (
        <div className="flex min-h-screen bg-brand-background">
            <AccountSidebar />

            <div className="flex min-w-0 flex-1 flex-col">
                <AccountHeader accountProfile={user} onLogout={handleLogout} />
                <main className="flex-1 overflow-y-auto px-6 py-6 sm:px-8 sm:py-8">
                    <div className="mx-auto w-full max-w-[1600px]">
                        <div className="w-full space-y-6">
                            <h2 className="text-3xl font-bold text-gray-900">
                                Transactions
                            </h2>
                            <p className="text-gray-500 text-sm mt-1">
                                Review credits, debits, and transfers across your account.
                            </p>

                            {/* Filter Control Panel */}
                            <div className="rounded-xl border border-brand-border bg-brand-surface p-6 shadow-sm flex flex-wrap items-center gap-4">
                                <div>
                                    <label className="text-xs text-gray-500 block mb-1 font-medium">From:</label>
                                    <input
                                        type="date"
                                        value={fromDate}
                                        onChange={(e) => setFromDate(e.target.value)}
                                        className="border border-gray-200 rounded-xl px-4 py-2.5 text-xs text-gray-700 font-medium focus:outline-none focus:border-brand-primary bg-white"
                                    />
                                </div>

                                <div>
                                    <label className="text-xs text-gray-500 block mb-1 font-medium">To:</label>
                                    <input
                                        type="date"
                                        value={toDate}
                                        onChange={(e) => setToDate(e.target.value)}
                                        className="border border-gray-200 rounded-xl px-4 py-2.5 text-xs text-gray-700 font-medium focus:outline-none focus:border-brand-primary bg-white"
                                    />
                                </div>

                                <div>
                                    <label className="text-xs text-gray-500 block mb-1 font-medium">Type:</label>
                                    <select
                                        value={typeFilter}
                                        onChange={(e) => setTypeFilter(e.target.value)}
                                        className="border border-gray-200 rounded-xl px-4 py-2.5 text-xs text-gray-700 font-medium focus:outline-none focus:border-brand-primary bg-white"
                                    >
                                        <option value="ALL">All types</option>
                                        <option value="CREDIT">Credit</option>
                                        <option value="DEBIT">Debit</option>
                                    </select>
                                </div>

                                <div className="flex items-center gap-3 mt-auto">
                                    <button
                                        onClick={handleApplyFilters}
                                        className="bg-brand-primary hover:bg-brand-primary-hover text-white text-xs font-semibold px-6 py-2.5 rounded-xl transition-colors shadow-sm cursor-pointer"
                                    >
                                        Apply filters
                                    </button>
                                    <button
                                        onClick={handleClearFilters}
                                        className="border border-gray-200 text-gray-600 hover:bg-gray-50 text-xs font-semibold px-6 py-2.5 rounded-xl transition-colors cursor-pointer"
                                    >
                                        Clear
                                    </button>
                                </div>
                            </div>

                            {/* Error Banner */}
                            {error && (
                                <div className="mt-4 bg-red-50 border border-red-200 text-red-600 p-4 rounded-xl text-xs font-medium">
                                    {error}
                                </div>
                            )}

                            {/* Transactions List */}
                            <div className="rounded-xl border border-brand-border bg-brand-surface p-8 shadow-sm">
                                <h3 className="text-lg font-bold text-gray-900 mb-6">
                                    Transaction history
                                </h3>

                                {loading ? (
                                    <p className="text-xs text-gray-400 py-6">Loading transactions...</p>
                                ) : displayedTransactions.length === 0 ? (
                                    <p className="text-xs text-gray-400 py-6">No transactions found matching your criteria.</p>
                                ) : (
                                    <div className="divide-y divide-gray-100">
                                        {displayedTransactions.map((tx, idx) => {
                                            const isDebit = tx.indicator === "DEBIT";

                                            const dateObj = new Date(tx.transactionDate);
                                            const formattedDate = !isNaN(dateObj.getTime())
                                                ? dateObj.toLocaleDateString("en-GB", {
                                                    day: "2-digit",
                                                    month: "short",
                                                })
                                                : "N/A";

                                            const rawAccount = tx.recipientAccountId || tx.accountId;
                                            const accountMask = rawAccount
                                                ? `•••• ${String(rawAccount).slice(-4)}`
                                                : "—";

                                            return (
                                                <div
                                                    key={tx.operationId || tx.id || idx}
                                                    className="py-6 grid grid-cols-12 items-center text-sm font-semibold"
                                                >
                                                    <div className="col-span-2 text-xs text-gray-400 font-medium">
                                                        {formattedDate}
                                                    </div>

                                                    <div className="col-span-4 text-gray-900 font-bold">
                                                        {tx.description || "Transfer"}
                                                    </div>

                                                    <div className="col-span-3 text-xs text-gray-400 font-mono">
                                                        {accountMask}
                                                    </div>

                                                    <div className="col-span-1 text-xs text-gray-400 font-medium tracking-wide">
                                                        {tx.indicator}
                                                    </div>

                                                    <div
                                                        className={`col-span-2 text-right font-bold ${
                                                            isDebit ? "text-red-500" : "text-emerald-600"
                                                        }`}
                                                    >
                                                        {isDebit ? "- " : "+ "}PKR {tx.amount?.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                                                    </div>
                                                </div>
                                            );
                                        })}
                                    </div>
                                )}

                                {/* Pagination Controls */}
                                {totalPages > 1 && (
                                    <div className="mt-6 flex justify-between items-center pt-4 border-t border-gray-100 text-xs">
                                        <button
                                            disabled={page === 0 || loading}
                                            onClick={() => setPage((prev) => Math.max(prev - 1, 0))}
                                            className="px-4 py-2 border border-gray-200 rounded-lg disabled:opacity-50 hover:bg-gray-50 transition-colors cursor-pointer disabled:cursor-not-allowed"
                                        >
                                            Previous
                                        </button>
                                        <span className="text-gray-500">
                      Page {page + 1} of {totalPages}
                    </span>
                                        <button
                                            disabled={page >= totalPages - 1 || loading}
                                            onClick={() => setPage((prev) => prev + 1)}
                                            className="px-4 py-2 border border-gray-200 rounded-lg disabled:opacity-50 hover:bg-gray-50 transition-colors cursor-pointer disabled:cursor-not-allowed"
                                        >
                                            Next
                                        </button>
                                    </div>
                                )}
                            </div>
                        </div>
                    </div>
                </main>
            </div>
        </div>
    );
};

export default TransactionsPage;