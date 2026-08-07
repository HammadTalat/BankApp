import { useState, useEffect } from "react";
import { Link, useNavigate } from "react-router";
import { ROUTES } from "../../../routes/routePaths.js";
import { httpClient } from "../../../api/httpClient.js";
import { useAuth } from "../../auth/context/useAuth.js";

export const DepositPage = () => {
    const { user, signOut, loading: authLoading } = useAuth();
    const navigate = useNavigate();

    // Account & form state
    const [accountNumber, setAccountNumber] = useState("");
    const [fetchingAccount, setFetchingAccount] = useState(true);
    const [amount, setAmount] = useState("");
    const [description, setDescription] = useState("Initial Deposit");

    // Status & UI state
    const [submitting, setSubmitting] = useState(false);
    const [successMessage, setSuccessMessage] = useState("");
    const [errorMessage, setErrorMessage] = useState("");

    // Quick deposit presets
    const PRESET_AMOUNTS = [1000, 5000, 10000, 25000, 50000];

    // Fetch and lock user's account number on load
    useEffect(() => {
        httpClient
            .get("/api/v1/account")
            .then((data) => {
                if (data?.accountNumber) {
                    setAccountNumber(data.accountNumber);
                }
            })
            .catch((err) => {
                console.error("Failed to fetch account number:", err);
                setErrorMessage("Unable to retrieve account details. Please try reloading.");
            })
            .finally(() => {
                setFetchingAccount(false);
            });
    }, []);

    const handleDeposit = async (e) => {
        e.preventDefault();
        setSuccessMessage("");
        setErrorMessage("");

        const parsedAmount = parseFloat(amount);
        if (!accountNumber) {
            setErrorMessage("Account number could not be resolved.");
            return;
        }
        if (isNaN(parsedAmount) || parsedAmount <= 0) {
            setErrorMessage("Please enter a valid deposit amount greater than 0.");
            return;
        }

        setSubmitting(true);

        try {
            // Target backend deposit endpoint with full payload schema
            const response = await httpClient.post("/api/v1/transaction/deposit", {
                accountNumber: accountNumber,
                amount: parsedAmount,
                description: description.trim() || "Deposit"
            });

            setSuccessMessage(
                response?.message || `Successfully deposited PKR ${parsedAmount.toLocaleString("en-US", { minimumFractionDigits: 2 })} into your account.`
            );
            setAmount(""); // Clear input on success
        } catch (err) {
            setErrorMessage(
                err?.response?.data?.message || err?.message || "Failed to process deposit. Please try again."
            );
        } finally {
            setSubmitting(false);
        }
    };

    const handleLogout = () => {
        if (signOut) {
            signOut();
        } else {
            localStorage.removeItem("ACCESS_TOKEN");
            navigate(ROUTES.HOME);
        }
    };

    if (authLoading) {
        return (
            <div className="flex min-h-screen items-center justify-center bg-[#F8FAFC]">
                <p className="text-gray-500 font-medium">Loading session...</p>
            </div>
        );
    }

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
                            className="flex items-center px-4 py-3 text-sm font-medium text-gray-300 hover:text-white hover:bg-white/5 rounded-lg transition-colors"
                        >
                            Dashboard
                        </Link>
                        <Link
                            to={ROUTES.ACCOUNT_DEPOSIT || "/account/deposit"}
                            className="flex items-center px-4 py-3 text-sm font-semibold text-white bg-white/10 rounded-lg transition-colors"
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
                        Deposit Money
                    </h2>
                    <p className="text-gray-500 text-sm mt-1">
                        Add funds directly into your bank account
                    </p>
                </div>

                <div className="max-w-2xl bg-white rounded-2xl border border-gray-100 shadow-sm p-8">
                    {/* Success Inline Banner */}
                    {successMessage && (
                        <div className="mb-6 p-4 rounded-xl bg-emerald-50 border border-emerald-200 flex items-start gap-3">
                            <svg className="w-5 h-5 text-emerald-600 shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                            </svg>
                            <div>
                                <h4 className="text-sm font-semibold text-emerald-800">Deposit Successful</h4>
                                <p className="text-xs text-emerald-700 mt-0.5">{successMessage}</p>
                            </div>
                        </div>
                    )}

                    {/* Error Inline Banner */}
                    {errorMessage && (
                        <div className="mb-6 p-4 rounded-xl bg-red-50 border border-red-200 flex items-start gap-3">
                            <svg className="w-5 h-5 text-red-600 shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                            </svg>
                            <div>
                                <h4 className="text-sm font-semibold text-red-800">Deposit Failed</h4>
                                <p className="text-xs text-red-700 mt-0.5">{errorMessage}</p>
                            </div>
                        </div>
                    )}

                    <form onSubmit={handleDeposit} className="space-y-6">
                        {/* Destination Account (Locked / Read-Only) */}
                        <div>
                            <label htmlFor="accountNumber" className="block text-xs font-semibold text-gray-700 uppercase tracking-wider mb-2">
                                Destination Account
                            </label>
                            <div className="relative">
                                <input
                                    id="accountNumber"
                                    type="text"
                                    value={fetchingAccount ? "Loading account details..." : accountNumber}
                                    readOnly
                                    tabIndex={-1}
                                    className="w-full px-4 py-3 bg-gray-100 border border-gray-200 rounded-xl text-sm text-gray-600 font-semibold cursor-not-allowed focus:outline-none select-all"
                                />
                                <div className="absolute right-4 top-1/2 -translate-y-1/2 text-xs font-bold text-gray-400 bg-gray-200/60 px-2 py-0.5 rounded">
                                    LOCKED
                                </div>
                            </div>
                            <p className="text-xs text-gray-400 mt-1.5">
                                Deposits automatically target your authenticated primary account.
                            </p>
                        </div>

                        {/* Amount */}
                        <div>
                            <label htmlFor="amount" className="block text-xs font-semibold text-gray-700 uppercase tracking-wider mb-2">
                                Deposit Amount (PKR)
                            </label>
                            <div className="relative">
                                <span className="absolute left-4 top-1/2 -translate-y-1/2 text-sm font-bold text-gray-400">
                                    PKR
                                </span>
                                <input
                                    id="amount"
                                    type="number"
                                    step="any"
                                    min="0.01"
                                    value={amount}
                                    onChange={(e) => setAmount(e.target.value)}
                                    placeholder="0.00"
                                    disabled={fetchingAccount || !accountNumber}
                                    className="w-full pl-14 pr-4 py-3 bg-gray-50 border border-gray-200 rounded-xl text-sm font-bold text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-600 focus:bg-white transition-all disabled:opacity-50"
                                    required
                                />
                            </div>
                        </div>

                        {/* Description */}
                        <div>
                            <label htmlFor="description" className="block text-xs font-semibold text-gray-700 uppercase tracking-wider mb-2">
                                Transaction Description
                            </label>
                            <input
                                id="description"
                                type="text"
                                value={description}
                                onChange={(e) => setDescription(e.target.value)}
                                placeholder="e.g., Initial Deposit, Monthly Savings"
                                disabled={fetchingAccount || !accountNumber}
                                className="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl text-sm text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-600 focus:bg-white transition-all disabled:opacity-50"
                            />
                        </div>

                        {/* Preset Buttons */}
                        <div>
                            <p className="text-xs font-semibold text-gray-400 mb-2.5">
                                Select Quick Amount
                            </p>
                            <div className="flex flex-wrap gap-2">
                                {PRESET_AMOUNTS.map((preset) => (
                                    <button
                                        key={preset}
                                        type="button"
                                        disabled={fetchingAccount || !accountNumber}
                                        onClick={() => setAmount(preset.toString())}
                                        className={`px-3.5 py-2 text-xs font-semibold rounded-lg border transition-colors disabled:opacity-50 ${
                                            amount === preset.toString()
                                                ? "bg-blue-50 border-blue-600 text-blue-600"
                                                : "bg-gray-50 border-gray-200 text-gray-600 hover:bg-gray-100"
                                        }`}
                                    >
                                        + PKR {preset.toLocaleString()}
                                    </button>
                                ))}
                            </div>
                        </div>

                        {/* Action Buttons */}
                        <div className="pt-4 flex items-center gap-4">
                            <button
                                type="submit"
                                disabled={submitting || fetchingAccount || !accountNumber}
                                className="bg-[#2563EB] hover:bg-blue-700 text-white font-semibold text-sm px-8 py-3 rounded-xl transition-colors shadow-sm disabled:opacity-50 flex items-center gap-2"
                            >
                                {submitting ? (
                                    <>
                                        <svg className="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
                                            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                                            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
                                        </svg>
                                        Processing...
                                    </>
                                ) : (
                                    "Confirm Deposit"
                                )}
                            </button>
                            <button
                                type="button"
                                onClick={() => navigate(ROUTES.ACCOUNT_HOME)}
                                className="bg-gray-50 hover:bg-gray-100 text-gray-700 font-semibold text-sm px-6 py-3 rounded-xl border border-gray-200 transition-colors"
                            >
                                Back to Dashboard
                            </button>
                        </div>
                    </form>
                </div>
            </main>
        </div>
    );
};

export default DepositPage;