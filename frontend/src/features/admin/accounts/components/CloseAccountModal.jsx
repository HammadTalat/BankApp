import Alert from "../../../../shared/components/feedback/Alert";
import Button from "../../../../shared/components/ui/Button";
import Modal from "../../../../shared/components/ui/Modal";
import { formatAccountNumber } from "../../../../shared/utils/formatAccountNumber";
import { formatCurrency } from "../../../../shared/utils/formatCurrency";

function CloseAccountModal({ account, onClose, onConfirm }) {
    const hasZeroBalance = Number(account.balance) === 0;
    const canClose = account.accountStatus === "ACTIVE" && hasZeroBalance;

    return (
        <Modal
            isOpen
            onClose={onClose}
            title="Close bank account"
            size="sm"
            footer={(
                <>
                    <Button
                        variant="secondary"
                        onClick={onClose}
                        autoFocus
                        className="w-full sm:w-auto"
                    >
                        Cancel
                    </Button>
                    <Button
                        variant="danger"
                        onClick={onConfirm}
                        disabled={!canClose}
                        className="w-full sm:w-auto"
                    >
                        Close account
                    </Button>
                </>
            )}
        >
            <p className="text-sm leading-6 text-brand-muted">
                You are about to close account {" "}
                <span className="font-semibold text-brand-text">
                    {formatAccountNumber(account.accountNumber)}
                </span>
                . This action cannot be reversed.
            </p>

            <dl className="my-5 grid gap-4 rounded-xl border border-brand-border bg-slate-50 p-4 sm:grid-cols-2">
                <div>
                    <dt className="text-xs font-medium uppercase tracking-wide text-brand-muted">
                        Account holder
                    </dt>
                    <dd className="mt-1 text-sm font-semibold text-brand-text">
                        {account.holderName}
                    </dd>
                </div>
                <div>
                    <dt className="text-xs font-medium uppercase tracking-wide text-brand-muted">
                        Current balance
                    </dt>
                    <dd className="mt-1 text-sm font-semibold text-brand-text">
                        {formatCurrency(account.balance, {
                            minimumFractionDigits: 2,
                        })}
                    </dd>
                </div>
            </dl>

            {!hasZeroBalance ? (
                <Alert
                    type="warning"
                    title="Balance must be zero"
                >
                    Debit or transfer the remaining balance before closing this account.
                </Alert>
            ) : (
                <Alert
                    type="warning"
                    title="Confirm permanent closure"
                >
                    The account will no longer accept credits, debits, or transfers.
                </Alert>
            )}
        </Modal>
    );
}

export default CloseAccountModal;
