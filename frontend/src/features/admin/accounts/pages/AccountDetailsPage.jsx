import { useState } from "react";
import { Landmark } from "lucide-react";
import {
    useNavigate,
    useOutletContext,
    useParams,
} from "react-router";

import Alert from "../../../../shared/components/feedback/Alert";
import EmptyState from "../../../../shared/components/feedback/EmptyState";
import Button from "../../../../shared/components/ui/Button";
import Card from "../../../../shared/components/ui/Card";
import PageHeader from "../../../../shared/components/ui/PageHeader";
import { ROUTES } from "../../../../routes/routePaths";
import AccountActions from "../components/AccountActions";
import AccountDetailsCards from "../components/AccountDetailsCards";
import CloseAccountModal from "../components/CloseAccountModal";
import EditAccountHolderModal from "../components/EditAccountHolderModal";

function AccountDetailsContent({ account, updateAccount }) {
    const [activeModal, setActiveModal] = useState(null);
    const [feedback, setFeedback] = useState(null);

    function handleSaveHolder(holder) {
        updateAccount(account.accountNumber, {
            holderName: holder.name,
            holderEmail: holder.email,
            holderAddress: holder.address,
        });
        setActiveModal(null);
        setFeedback({
            type: "success",
            title: "Holder details updated",
            message: `${holder.name}'s information was saved for this account.`,
        });
    }

    function handleCloseAccount() {
        if (account.accountStatus !== "ACTIVE" || Number(account.balance) !== 0) {
            return;
        }

        updateAccount(account.accountNumber, {
            accountStatus: "CLOSED",
        });
        setActiveModal(null);
        setFeedback({
            type: "success",
            title: "Account closed",
            message: "The bank account was closed with a final balance of PKR 0.00.",
        });
    }

    function showDeferredAction(action) {
        const actionMessages = {
            transactions:
                "The reusable account-transactions page will be added in Step 4.",
            credit:
                "Credit Account is ready visually, but the backend does not yet expose an admin credit endpoint.",
            debit:
                "Debit Account is ready visually, but the backend does not yet expose an admin debit endpoint.",
        };

        setFeedback({
            type: "info",
            title: "Action coming next",
            message: actionMessages[action],
        });
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

            <AccountDetailsCards account={account} />

            <AccountActions
                accountStatus={account.accountStatus}
                onViewTransactions={() => showDeferredAction("transactions")}
                onEditHolder={() => setActiveModal("edit")}
                onCreditAccount={() => showDeferredAction("credit")}
                onDebitAccount={() => showDeferredAction("debit")}
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
    const { accounts, updateAccount } = useOutletContext();
    const { accountNumber } = useParams();
    const account = accounts.find(
        (account) => account.accountNumber === accountNumber,
    );

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
        />
    );
}

export default AccountDetailsPage;
