import { useState, useEffect, useCallback } from "react";
import { Link, useNavigate } from "react-router";
import { ROUTES } from "../../../routes/routePaths.js";
import { httpClient } from "../../../api/httpClient.js";

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
            await httpClient.post("/api/v1/auth/logout");
        } catch {
            // Proceed with client cleanup
        } finally {
            localStorage.removeItem("ACCESS_TOKEN");
            navigate(ROUTES.HOME);
        }
    };

    // Fetch user transactions matching backend specification
    const fetchTransactions = useCallback(async () => {
        setLoading(true);
        setError("");

        try {
            const queryParams = new URLSearchParams({
                page: String(page),
                size: String(pageSize),
            });

            if (appliedFilters.fromDate) {
                queryParams.append("startDate", appliedFilters.fromDate);
            }
            if (appliedFilters.toDate) {
                queryParams.append("endDate", appliedFilters.toDate);
            }

            const data = await httpClient.get(
                `/api/v1/transaction/get-transactions?${queryParams.toString()}`
            );

            if (data?.transactions) {
                setTransactions(data.transactions);
                setTotalPages(data.totalPages || 1);
            } else {
                setTransactions([]);
            }
        } catch (err) {
            console.error("Failed to fetch transactions:", err);
            setError(err.message || "Failed to load transactions.");
            setTransactions([]);
        } finally {
            setLoading(false);
        }
    }, [page, pageSize, appliedFilters]);

    useEffect(() => {
        fetchTransactions();
    }, [fetchTransactions]);

    const handleApplyFilters = () => {
        setPage(0);
        setAppliedFilters({
            fromDate,
            toDate,
            typeFilter,
        });
    };

    const handleClearFilters = () => {
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
        <div className="flex min-h-screen bg-[#F8FAFC]">
            {/* Dark Blue Sidebar */}
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
                            className="flex items-center px-4 py-3 text-sm font-semibold text-white bg-white/10 rounded-lg transition-colors"
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
                        className="text-sm font-medium text-pink-300 hover:text-pink-200 transition-colors cursor-pointer"
                    >
                        Logout
                    </button>
                </div>
            </aside>

            {/* Main Content Area */}
            <main className="flex-1 p-10 overflow-y-auto">
                <div className="max-w-5xl">
                    <h2 className="text-3xl font-bold text-gray-900">
                        Transactions
                    </h2>
                    <p className="text-gray-500 text-sm mt-1">
                        Review credits, debits, and transfers across your account.
                    </p>

                    {/* Filter Control Panel */}
                    <div className="mt-8 bg-white rounded-2xl border border-gray-100 shadow-sm p-6 flex flex-wrap items-center gap-4">
                        <div>
                            <label className="text-xs text-gray-500 block mb-1 font-medium">From:</label>
                            <input
                                type="date"
                                value={fromDate}
                                onChange={(e) => setFromDate(e.target.value)}
                                className="border border-gray-200 rounded-xl px-4 py-2.5 text-xs text-gray-700 font-medium focus:outline-none focus:border-blue-500 bg-white"
                            />
                        </div>

                        <div>
                            <label className="text-xs text-gray-500 block mb-1 font-medium">To:</label>
                            <input
                                type="date"
                                value={toDate}
                                onChange={(e) => setToDate(e.target.value)}
                                className="border border-gray-200 rounded-xl px-4 py-2.5 text-xs text-gray-700 font-medium focus:outline-none focus:border-blue-500 bg-white"
                            />
                        </div>

                        <div>
                            <label className="text-xs text-gray-500 block mb-1 font-medium">Type:</label>
                            <select
                                value={typeFilter}
                                onChange={(e) => setTypeFilter(e.target.value)}
                                className="border border-gray-200 rounded-xl px-4 py-2.5 text-xs text-gray-700 font-medium focus:outline-none focus:border-blue-500 bg-white"
                            >
                                <option value="ALL">All types</option>
                                <option value="CREDIT">Credit</option>
                                <option value="DEBIT">Debit</option>
                            </select>
                        </div>

                        <div className="flex items-center gap-3 mt-auto">
                            <button
                                onClick={handleApplyFilters}
                                className="bg-[#2563EB] hover:bg-blue-700 text-white text-xs font-semibold px-6 py-2.5 rounded-xl transition-colors shadow-sm cursor-pointer"
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
                    <div className="mt-8 bg-white rounded-2xl border border-gray-100 shadow-sm p-8">
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
            </main>
        </div>
    );
};

export default TransactionsPage;