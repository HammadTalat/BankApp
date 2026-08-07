import { useState, useEffect } from "react";
import { Link, useNavigate } from "react-router";
import { ROUTES } from "../../../routes/routePaths.js";
import { httpClient } from "../../../api/httpClient.js";

export const TransferPage = () => {
    const navigate = useNavigate();

    // Sender state
    const [senderAccount, setSenderAccount] = useState("");
    const [isFetchingAccount, setIsFetchingAccount] = useState(true);
    const [accountError, setAccountError] = useState("");

    // Form inputs
    const [recipientAccount, setRecipientAccount] = useState("");
    const [amount, setAmount] = useState("");
    const [description, setDescription] = useState("");

    // Recipient lookup state
    const [recipient, setRecipient] = useState(null);
    const [isSearching, setIsSearching] = useState(false);
    const [lookupError, setLookupError] = useState("");

    // Transfer submission state
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [transferError, setTransferError] = useState("");
    const [transferSuccess, setTransferSuccess] = useState(false);

    // 1. Fetch current logged-in user's account details
    useEffect(() => {
        let isMounted = true;
        setIsFetchingAccount(true);

        httpClient
            .get("/api/v1/account")
            .then((data) => {
                if (!isMounted) return;
                if (data?.accountNumber) {
                    setSenderAccount(data.accountNumber);
                } else {
                    setAccountError("Could not retrieve your account information.");
                }
            })
            .catch((err) => {
                if (isMounted) {
                    setAccountError(err.message || "Failed to load account details.");
                }
            })
            .finally(() => {
                if (isMounted) setIsFetchingAccount(false);
            });

        return () => {
            isMounted = false;
        };
    }, []);

    // 2. Debounced recipient lookup with cancelation/race-condition handling
    useEffect(() => {
        const cleanAccount = recipientAccount.trim();

        if (!cleanAccount) {
            setRecipient(null);
            setLookupError("");
            return;
        }

        if (cleanAccount === senderAccount) {
            setRecipient(null);
            setLookupError("You cannot transfer money to your own account.");
            return;
        }

        let isCancelled = false;
        const timeoutId = setTimeout(() => {
            setIsSearching(true);
            setLookupError("");
            setRecipient(null);

            httpClient
                .get(`/api/v1/transaction/lookup?accountID=${encodeURIComponent(cleanAccount)}`)
                .then((data) => {
                    if (isCancelled) return;

                    if (data?.accountNumber && data?.status === "ACTIVE") {
                        setRecipient(data);
                    } else if (data?.status === "CLOSED") {
                        setLookupError("This account is currently closed.");
                    } else {
                        setLookupError("Account not found.");
                    }
                })
                .catch((err) => {
                    if (!isCancelled) {
                        setLookupError(err.message || "Could not verify recipient account.");
                    }
                })
                .finally(() => {
                    if (!isCancelled) setIsSearching(false);
                });
        }, 500);

        return () => {
            isCancelled = true;
            clearTimeout(timeoutId);
        };
    }, [recipientAccount, senderAccount]);

    const handleLogout = async () => {
        try {
            await httpClient.post("/api/v1/auth/logout");
        } catch {
            // Proceed with local cleanup regardless of network status
        } finally {
            localStorage.removeItem("ACCESS_TOKEN");
            navigate(ROUTES.HOME);
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setTransferError("");

        const parsedAmount = parseFloat(amount);
        if (isNaN(parsedAmount) || parsedAmount < 0.01) {
            setTransferError("Minimum transfer amount is PKR 0.01.");
            return;
        }

        if (!recipient) {
            setTransferError("Please enter a valid recipient account.");
            return;
        }

        if (!senderAccount) {
            setTransferError("Sender account details are missing. Please refresh.");
            return;
        }

        setIsSubmitting(true);

        try {
            await httpClient.post("/api/v1/transaction/transfer", {
                senderAccountNumber: senderAccount,
                receiverAccountNumber: recipientAccount.trim(),
                amount: parsedAmount,
                description: description.trim(),
            });

            setTransferSuccess(true);
            setTimeout(() => {
                navigate(ROUTES.ACCOUNT_HOME);
            }, 2000);
        } catch (err) {
            setTransferError(err.message || "Failed to execute transfer.");
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="flex min-h-screen bg-[#F8FAFC]">
            {/* Sidebar */}
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
                            className="flex items-center px-4 py-3 text-sm font-semibold text-white bg-white/10 rounded-lg transition-colors"
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

            {/* Main Content */}
            <main className="flex-1 p-10 overflow-y-auto">
                <div className="max-w-3xl">
                    <h2 className="text-3xl font-bold text-gray-900">Transfer Money</h2>
                    <p className="text-gray-500 text-sm mt-1">
                        Verify the recipient, enter the amount, then review before sending.
                    </p>

                    {accountError && (
                        <div className="mt-4 bg-red-50 border border-red-200 text-red-600 p-4 rounded-xl text-sm font-medium">
                            {accountError}
                        </div>
                    )}

                    <div className="mt-8 bg-white rounded-2xl border border-gray-100 shadow-sm p-8">
                        {transferSuccess ? (
                            <div className="bg-emerald-50 border border-emerald-200 text-emerald-700 p-4 rounded-xl text-center font-medium">
                                Transfer completed successfully! Redirecting to dashboard...
                            </div>
                        ) : (
                            <form onSubmit={handleSubmit} className="space-y-6">
                                {transferError && (
                                    <div className="bg-red-50 border border-red-200 text-red-600 p-3 rounded-xl text-sm font-medium">
                                        {transferError}
                                    </div>
                                )}

                                {/* Sender Account Banner */}
                                {senderAccount && (
                                    <div className="bg-gray-50 border border-gray-200 p-3.5 rounded-xl text-xs text-gray-600 flex justify-between items-center">
                                        <span>Transferring from account:</span>
                                        <span className="font-mono font-bold text-gray-800">{senderAccount}</span>
                                    </div>
                                )}

                                {/* Recipient Account Input */}
                                <div>
                                    <label className="block text-xs font-bold text-gray-800 mb-2">
                                        Recipient account number
                                    </label>
                                    <input
                                        type="text"
                                        placeholder="Enter recipient account number"
                                        value={recipientAccount}
                                        onChange={(e) => setRecipientAccount(e.target.value)}
                                        disabled={isFetchingAccount}
                                        className="w-full px-4 py-3 border border-gray-200 rounded-xl text-sm focus:outline-none focus:border-blue-500 text-gray-800 placeholder-gray-400 disabled:bg-gray-100"
                                    />
                                </div>

                                {/* Lookup Feedback */}
                                {isSearching && (
                                    <div className="bg-[#EEF9F5] p-5 rounded-2xl text-xs text-emerald-600 font-medium">
                                        Verifying account details...
                                    </div>
                                )}

                                {lookupError && !isSearching && (
                                    <div className="bg-red-50 p-4 rounded-2xl text-xs text-red-500 font-medium">
                                        {lookupError}
                                    </div>
                                )}

                                {recipient && !isSearching && (
                                    <div className="bg-[#EEF9F5] p-5 rounded-2xl">
                                        <p className="text-sm font-bold text-gray-900">
                                            {recipient.accountHolderName}
                                        </p>
                                        <p className="text-xs text-gray-500 mt-1">
                                            Account: {recipient.accountNumber}
                                        </p>
                                    </div>
                                )}

                                {/* Amount Input */}
                                <div>
                                    <label className="block text-xs font-bold text-gray-800 mb-2">
                                        Amount
                                    </label>
                                    <input
                                        type="number"
                                        step="0.01"
                                        min="0.01"
                                        placeholder="PKR 0.00"
                                        value={amount}
                                        onChange={(e) => setAmount(e.target.value)}
                                        className="w-full px-4 py-3 border border-gray-200 rounded-xl text-sm focus:outline-none focus:border-blue-500 text-gray-800 placeholder-gray-400"
                                    />
                                </div>

                                {/* Description Input */}
                                <div>
                                    <label className="block text-xs font-bold text-gray-800 mb-2">
                                        Description <span className="font-normal text-gray-400">(Optional)</span>
                                    </label>
                                    <input
                                        type="text"
                                        maxLength={255}
                                        placeholder="What is this transfer for?"
                                        value={description}
                                        onChange={(e) => setDescription(e.target.value)}
                                        className="w-full px-4 py-3 border border-gray-200 rounded-xl text-sm focus:outline-none focus:border-blue-500 text-gray-800 placeholder-gray-400"
                                    />
                                </div>

                                {/* Submit Button */}
                                <button
                                    type="submit"
                                    disabled={isSubmitting || !recipient || isFetchingAccount || !senderAccount}
                                    className="w-full bg-[#2563EB] hover:bg-blue-700 disabled:opacity-50 text-white font-semibold py-3.5 px-4 rounded-xl text-sm transition-colors shadow-sm cursor-pointer disabled:cursor-not-allowed"
                                >
                                    {isSubmitting ? "Processing..." : "Submit Transfer"}
                                </button>
                            </form>
                        )}
                    </div>
                </div>
            </main>
        </div>
    );
};

export default TransferPage;