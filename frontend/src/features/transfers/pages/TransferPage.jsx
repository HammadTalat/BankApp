import { useState, useEffect } from "react";
import { useNavigate } from "react-router";
import { ROUTES } from "../../../routes/routePaths.js";
import { transfersApi } from "../api/transferApi.js";

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

        const loadSenderAccount = async () => {
            try {
                const data = await transfersApi.getSenderAccount();
                if (!isMounted) return;
                if (data?.accountNumber) {
                    setSenderAccount(data.accountNumber);
                } else {
                    setAccountError("Could not retrieve your account information.");
                }
            } catch (err) {
                if (isMounted) {
                    setAccountError(err.message || "Failed to load account details.");
                }
            } finally {
                if (isMounted) setIsFetchingAccount(false);
            }
        };

        loadSenderAccount();

        return () => {
            isMounted = false;
        };
    }, []);

    // Helper to sanitize account input: remove spaces and slice to max 16 chars
    const handleRecipientAccountChange = (e) => {
        const sanitizedValue = e.target.value.replace(/\s+/g, "").slice(0, 16);
        setRecipientAccount(sanitizedValue);
        // Reset recipient lookup feedback when input changes
        setRecipient(null);
        setLookupError("");
    };

    // 2. Debounced recipient lookup - triggers when account length is 16
    useEffect(() => {
        const cleanAccount = recipientAccount.trim();

        if (cleanAccount.length < 16) {
            return;
        }

        if (cleanAccount === senderAccount) {
            return;
        }

        let isCancelled = false;
        const timeoutId = setTimeout(async () => {
            setIsSearching(true);

            try {
                const data = await transfersApi.lookupRecipient(cleanAccount);
                if (isCancelled) return;

                if (data?.accountNumber && data?.status === "ACTIVE") {
                    setRecipient(data);
                    setLookupError("");
                } else if (data?.status === "CLOSED") {
                    setRecipient(null);
                    setLookupError("This account is currently closed.");
                } else {
                    setRecipient(null);
                    setLookupError("Account not found.");
                }
            } catch (err) {
                if (!isCancelled) {
                    setRecipient(null);
                    setLookupError(err.message || "Could not verify recipient account.");
                }
            } finally {
                if (!isCancelled) setIsSearching(false);
            }
        }, 500);

        return () => {
            isCancelled = true;
            clearTimeout(timeoutId);
        };
    }, [recipientAccount, senderAccount]);

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
            await transfersApi.executeTransfer({
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

    // Calculate self-transfer error notice
    const isSelfTransfer = recipientAccount.trim().length === 16 && recipientAccount.trim() === senderAccount;

    return (
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
                                maxLength={16}
                                placeholder="Enter 16-digit account number"
                                value={recipientAccount}
                                onChange={handleRecipientAccountChange}
                                disabled={isFetchingAccount}
                                className="w-full px-4 py-3 border border-gray-200 rounded-xl text-sm focus:outline-none focus:border-blue-500 text-gray-800 placeholder-gray-400 disabled:bg-gray-100 font-mono"
                            />
                        </div>

                        {/* Lookup Feedback */}
                        {isSearching && (
                            <div className="bg-[#EEF9F5] p-5 rounded-2xl text-xs text-emerald-600 font-medium">
                                Verifying account details...
                            </div>
                        )}

                        {isSelfTransfer && !isSearching && (
                            <div className="bg-red-50 p-4 rounded-2xl text-xs text-red-500 font-medium">
                                You cannot transfer money to your own account.
                            </div>
                        )}

                        {lookupError && !isSearching && !isSelfTransfer && (
                            <div className="bg-red-50 p-4 rounded-2xl text-xs text-red-500 font-medium">
                                {lookupError}
                            </div>
                        )}

                        {recipient && !isSearching && !isSelfTransfer && (
                            <div className="bg-[#EEF9F5] p-5 rounded-2xl">
                                <p className="text-sm font-bold text-gray-900">
                                    {recipient.accountHolderName}
                                </p>
                                <p className="text-xs text-gray-500 mt-1 font-mono">
                                    Account: {recipient.accountNumber}
                                </p>
                            </div>
                        )}

                        {/* Amount Input */}
                        <div>
                            <label className="block text-xs font-bold text-gray-800 mb-2">
                                Amount (PKR)
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
                            disabled={isSubmitting || !recipient || isFetchingAccount || !senderAccount || isSelfTransfer}
                            className="w-full bg-[#2563EB] hover:bg-blue-700 disabled:opacity-50 text-white font-semibold py-3.5 px-4 rounded-xl text-sm transition-colors shadow-sm cursor-pointer disabled:cursor-not-allowed"
                        >
                            {isSubmitting ? "Processing..." : "Submit Transfer"}
                        </button>
                    </form>
                )}
            </div>
        </div>
    );
};

export default TransferPage;