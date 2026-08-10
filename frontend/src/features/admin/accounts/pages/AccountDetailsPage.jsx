import { useState, useEffect } from "react";
import { Landmark } from "lucide-react";
import {
    useNavigate,
    useParams,
} from "react-router";

import Alert from "../../../../shared/components/feedback/Alert";
import EmptyState from "../../../../shared/components/feedback/EmptyState";
import Button from "../../../../shared/components/ui/Button";
import Card from "../../../../shared/components/ui/Card";
import PageHeader from "../../../../shared/components/ui/PageHeader";
import LoadingSpinner from "../../../../shared/components/feedback/LoadingSpinner";

import {
    getAdminAccountTransactionsPath,
    ROUTES,
} from "../../../../routes/routePaths";
import AccountActions from "../components/AccountActions";
import AccountDetailsCards from "../components/AccountDetailsCards";
import CloseAccountModal from "../components/CloseAccountModal";
import EditAccountHolderModal from "../components/EditAccountHolderModal";
import  {getAdminAccountDetails , closeAdminAccount , updateAccountHolder} from "../api/adminAccountApi.js"

function AccountDetailsContent({
    account,
    updateAccount,
    onViewTransactions,
}) {
    const [, setActionLoading] =
        useState(false);

    const [actionError, setActionError] =
        useState("");

    const [feedback, setFeedback] =
        useState(null);
    const [activeModal,setActiveModal] = useState(null);

    async function handleSaveHolder(holder) {
        try {
            setActionLoading(true);
            setActionError("");
            setFeedback(null);

            await updateAccountHolder(
                account.userId,
                holder,
            );

            const refreshedAccount =
                await getAdminAccountDetails(
                    account.accountNumber,
                );

            updateAccount(refreshedAccount);

            setActiveModal(null);

            setFeedback({
                type: "success",
                title: "Holder details updated",
                message:
                    `${refreshedAccount.holderName}'s information was updated successfully.`,
            });
        } catch (requestError) {
            console.error(
                "Unable to update account holder:",
                requestError,
            );

            setActionError(
                requestError.message
                || "Unable to update account holder.",
            );
        } finally {
            setActionLoading(false);
        }
    }

    async function handleCloseAccount() {
        try {
            setActionLoading(true);
            setActionError("");
            setFeedback(null);

            await closeAdminAccount(
                account.accountNumber,
            );

            const refreshedAccount =
                await getAdminAccountDetails(
                    account.accountNumber,
                );

            updateAccount(refreshedAccount);

            setActiveModal(null);

            setFeedback({
                type: "success",
                title: "Account closed",
                message:
                    "The bank account was closed successfully with a final balance of PKR 0.00.",
            });
        } catch (requestError) {
            console.error(
                "Unable to close account:",
                requestError,
            );

            setActionError(
                requestError.message
                || "Unable to close this account.",
            );
        } finally {
            setActionLoading(false);
        }
    }
    return (
        <section className="space-y-7">
            <PageHeader
                title="Account Details"
                description="Review account information and perform controlled actions."
            />

            {feedback && (
                <Alert
                    type={feedback.type}
                    title={feedback.title}
                >
                    {feedback.message}
                </Alert>
            )}

            {actionError && (
                <Alert
                    type="error"
                    title="Action failed"
                >
                    {actionError}
                </Alert>
            )}

            <AccountDetailsCards account={account} />

            <AccountActions
                accountStatus={account.accountStatus}
                onViewTransactions={onViewTransactions}
                onEditHolder={() => setActiveModal("edit")}
                onCloseAccount={() => setActiveModal("close")}
            />

            {activeModal === "edit" && (
                <EditAccountHolderModal
                    account={account}
                    onClose={() => setActiveModal(null)}
                    onSave={handleSaveHolder}
                />
            )}

            {activeModal === "close" && (
                <CloseAccountModal
                    account={account}
                    onClose={() => setActiveModal(null)}
                    onConfirm={handleCloseAccount}
                />
            )}
        </section>
    );
}

function AccountDetailsPage() {
    const navigate = useNavigate();
    const { accountNumber } = useParams();
    const [account, updateAccount] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        async function loadAccountDetails() {
            try {
                setLoading(true);
                setError("");

                const response =
                    await getAdminAccountDetails(
                        accountNumber,
                    );

                console.log(
                    "Account details response:",
                    response,
                );

                updateAccount(response);

            } catch (requestError) {

                console.error(
                    "Unable to load account details:",
                    requestError,
                );

                setError(
                    requestError.message
                    || "Unable to load account details.",
                );

            } finally {

                setLoading(false);

            }
        }

        loadAccountDetails();

    }, [accountNumber]);

    if (loading) {
        return (
            <div className="flex min-h-72 items-center justify-center">

                <LoadingSpinner
                    message="Loading account details..."
                />

            </div>
        );
    }

    if (error) {
        return (
            <Alert
                type="error"
                title="Account could not be loaded"
            >
                {error}
            </Alert>
        );
    }
    if (!account) {
        return (
            <section className="space-y-6">
                <PageHeader
                    title="Account Details"
                    description="Review account information and perform controlled actions."
                />

                <Card className="shadow-none">
                    <EmptyState
                        icon={Landmark}
                        title="Account not found"
                        description="This account is not available in the current dummy account data."
                        action={(
                            <Button
                                variant="secondary"
                                onClick={() => navigate(ROUTES.ADMIN_ACCOUNTS)}
                            >
                                Back to bank accounts
                            </Button>
                        )}
                    />
                </Card>
            </section>
        );
    }

    return (
        <AccountDetailsContent
            account={account}
            updateAccount={updateAccount}
            onViewTransactions={() =>
                navigate(getAdminAccountTransactionsPath(account.accountNumber))
            }
        />
    );
}

export default AccountDetailsPage;
