import { useState, useEffect } from "react";
import { useNavigate } from "react-router";
import { ROUTES } from "../../../../routes/routePaths.js";
import { useAuth } from "../../../auth/context/useAuth.js";
import { transferService } from "../api/transferService.js";
import AccountHeader from "../../../../shared/components/navigation/AccountHeader.jsx";
import AccountSidebar from "../../../../shared/components/navigation/AccountSidebar.jsx";

export const TransferPage = () => {
    const navigate = useNavigate();
    const { user } = useAuth();

    const [senderAccount, setSenderAccount] = useState("");
    const [isFetchingAccount, setIsFetchingAccount] = useState(true);
    const [accountError, setAccountError] = useState("");

    const [recipientAccount, setRecipientAccount] = useState("");
    const [amount, setAmount] = useState("");
    const [description, setDescription] = useState("");

    const [recipient, setRecipient] = useState(null);
    const [isSearching, setIsSearching] = useState(false);
    const [lookupError, setLookupError] = useState("");

    const [isSubmitting, setIsSubmitting] = useState(false);
    const [transferError, setTransferError] = useState("");
    const [transferSuccess, setTransferSuccess] = useState(false);

    useEffect(() => {
        let isMounted = true;

        transferService
            .getAccountDetails()
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

    const handleRecipientAccountChange = (e) => {
        const sanitizedValue = e.target.value.replace(/\s+/g, "").slice(0, 16);
        setRecipientAccount(sanitizedValue);
        setRecipient(null);
        setLookupError("");
    };

    useEffect(() => {
        const cleanAccount = recipientAccount.trim();

        if (cleanAccount.length < 16) return undefined;
        if (cleanAccount === senderAccount) return undefined;

        let isCancelled = false;
        const timeoutId = setTimeout(() => {
            setIsSearching(true);
            setLookupError("");
            setRecipient(null);

            transferService
                .lookupRecipient(cleanAccount)
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
                        setLookupError(
                            err.message || "Could not verify recipient account."
                        );
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

    const ownAccountSelected = recipientAccount.trim() === senderAccount;
    const visibleLookupError = ownAccountSelected
        ? "You cannot transfer money to your own account."
        : lookupError;

    const handleLogout = async () => {
        try {
            await transferService.logout();
        } catch {
            // Client-side cleanup still completes when the logout request fails.
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
            await transferService.transferFunds({
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
        <div className="flex min-h-screen bg-brand-background">
            <AccountSidebar />

            <div className="flex min-w-0 flex-1 flex-col">
                <AccountHeader accountProfile={user} onLogout={handleLogout} />
                <main className="flex-1 overflow-y-auto px-6 py-6 sm:px-8 sm:py-8">
                    <div className="mx-auto w-full max-w-[1600px]">
                        <div className="mx-auto max-w-3xl">
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
                                                <span className="font-mono font-bold text-gray-800">
                          {senderAccount}
                        </span>
                                            </div>
                                        )}

                                        {/* Recipient Account Input */}
                                        <div>
                                            <label htmlFor="recipient-account" className="block text-xs font-bold text-gray-800 mb-2">
                                                Recipient account number
                                            </label>
                                            <input
                                                id="recipient-account"
                                                type="text"
                                                maxLength={16}
                                                placeholder="Enter 16-digit account number"
                                                value={recipientAccount}
                                                onChange={handleRecipientAccountChange}
                                                disabled={isFetchingAccount}
                                                className="w-full px-4 py-3 border border-gray-200 rounded-xl text-sm focus:outline-none focus:border-brand-primary text-gray-800 placeholder-gray-400 disabled:bg-gray-100"
                                            />
                                        </div>

                                        {/* Lookup Feedback */}
                                        {isSearching && (
                                            <div className="bg-[#EEF9F5] p-5 rounded-2xl text-xs text-emerald-600 font-medium">
                                                Verifying account details...
                                            </div>
                                        )}

                                        {visibleLookupError && !isSearching && (
                                            <div className="bg-red-50 p-4 rounded-2xl text-xs text-red-500 font-medium">
                                                {visibleLookupError}
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
                                            <label htmlFor="transfer-amount" className="block text-xs font-bold text-gray-800 mb-2">
                                                Amount
                                            </label>
                                            <input
                                                id="transfer-amount"
                                                type="number"
                                                step="0.01"
                                                min="0.01"
                                                placeholder="PKR 0.00"
                                                value={amount}
                                                onChange={(e) => setAmount(e.target.value)}
                                                className="w-full px-4 py-3 border border-gray-200 rounded-xl text-sm focus:outline-none focus:border-brand-primary text-gray-800 placeholder-gray-400"
                                            />
                                        </div>

                                        {/* Description Input */}
                                        <div>
                                            <label htmlFor="transfer-description" className="block text-xs font-bold text-gray-800 mb-2">
                                                Description{" "}
                                                <span className="font-normal text-gray-400">
                          (Optional)
                        </span>
                                            </label>
                                            <input
                                                id="transfer-description"
                                                type="text"
                                                maxLength={255}
                                                placeholder="What is this transfer for?"
                                                value={description}
                                                onChange={(e) => setDescription(e.target.value)}
                                                className="w-full px-4 py-3 border border-gray-200 rounded-xl text-sm focus:outline-none focus:border-brand-primary text-gray-800 placeholder-gray-400"
                                            />
                                        </div>

                                        {/* Submit Button */}
                                        <button
                                            type="submit"
                                            disabled={
                                                isSubmitting ||
                                                !recipient ||
                                                isFetchingAccount ||
                                                !senderAccount
                                            }
                                            className="w-full bg-brand-primary hover:bg-brand-primary-hover disabled:opacity-50 text-white font-semibold py-3.5 px-4 rounded-xl text-sm transition-colors shadow-sm cursor-pointer disabled:cursor-not-allowed"
                                        >
                                            {isSubmitting ? "Processing..." : "Submit Transfer"}
                                        </button>
                                    </form>
                                )}
                            </div>
                        </div>
                    </div>
                </main>
            </div>
        </div>
    );
};

export default TransferPage;
