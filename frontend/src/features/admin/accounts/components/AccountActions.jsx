import Button from "../../../../shared/components/ui/Button";
import Card from "../../../../shared/components/ui/Card";

function AccountActions({
    accountStatus,
    onViewTransactions,
    onEditHolder,
    onCreditAccount,
    onDebitAccount,
    onCloseAccount,
}) {
    const isClosed = accountStatus === "CLOSED";

    return (
        <Card className="shadow-none">
            <h2 className="text-xl font-semibold text-brand-text">
                Account actions
            </h2>

            <div className="mt-8 flex flex-col gap-3 sm:flex-row sm:flex-wrap">
                <Button
                    onClick={onViewTransactions}
                    className="w-full sm:w-auto"
                >
                    View Transactions
                </Button>
                <Button
                    variant="secondary"
                    onClick={onEditHolder}
                    className="w-full sm:w-auto"
                >
                    Edit Holder
                </Button>
                <Button
                    variant="secondary"
                    onClick={onCreditAccount}
                    disabled={isClosed}
                    className="w-full sm:w-auto"
                >
                    Credit Account
                </Button>
                <Button
                    variant="secondary"
                    onClick={onDebitAccount}
                    disabled={isClosed}
                    className="w-full sm:w-auto"
                >
                    Debit Account
                </Button>
                <Button
                    variant="danger"
                    onClick={onCloseAccount}
                    disabled={isClosed}
                    className="w-full sm:w-auto"
                >
                    Close Account
                </Button>
            </div>

            {isClosed && (
                <p className="mt-4 text-sm leading-6 text-brand-muted">
                    This account is closed. Transactions and further account-status changes are unavailable.
                </p>
            )}
        </Card>
    );
}

export default AccountActions;
