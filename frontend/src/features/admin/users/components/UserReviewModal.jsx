import Button from "../../../../shared/components/ui/Button";
import Modal from "../../../../shared/components/ui/Modal";
import StatusBadge from "../../../../shared/components/ui/StatusBadge";

const reviewDetails = {
    approve: {
        title: "Approve account holder",
        confirmLabel: "Approve user",
        confirmVariant: "primary",
        message:
            "Approval will create a new active bank account for this user with an initial balance of PKR 0.00.",
    },
    reject: {
        title: "Reject application",
        confirmLabel: "Reject user",
        confirmVariant: "danger",
        message:
            "The application will be marked as rejected and the user will not receive a bank account.",
    },
};

function UserReviewModal({
    isOpen,
    user,
    action,
    onClose,
    onConfirm,
}) {
    const selectedReview = reviewDetails[action] || reviewDetails.approve;

    return (
        <Modal
            isOpen={isOpen}
            onClose={onClose}
            title={selectedReview.title}
            size="sm"
            footer={(
                <>
                    <Button
                        variant="secondary"
                        onClick={onClose}
                        className="w-full sm:w-auto"
                    >
                        Cancel
                    </Button>
                    <Button
                        variant={selectedReview.confirmVariant}
                        onClick={onConfirm}
                        className="w-full sm:w-auto"
                    >
                        {selectedReview.confirmLabel}
                    </Button>
                </>
            )}
        >
            <p className="text-sm leading-6 text-brand-muted">
                {selectedReview.message}
            </p>

            {user && (
                <dl className="mt-5 grid gap-4 rounded-xl border border-brand-border bg-slate-50 p-4 sm:grid-cols-2">
                    <div>
                        <dt className="text-xs font-medium uppercase tracking-wide text-brand-muted">
                            Name
                        </dt>
                        <dd className="mt-1 text-sm font-semibold text-brand-text">
                            {user.name}
                        </dd>
                    </div>
                    <div>
                        <dt className="text-xs font-medium uppercase tracking-wide text-brand-muted">
                            Status
                        </dt>
                        <dd className="mt-1">
                            <StatusBadge status={user.approvalStatus} />
                        </dd>
                    </div>
                    <div className="sm:col-span-2">
                        <dt className="text-xs font-medium uppercase tracking-wide text-brand-muted">
                            Email
                        </dt>
                        <dd className="mt-1 break-all text-sm font-semibold text-brand-text">
                            {user.email}
                        </dd>
                    </div>
                    <div className="sm:col-span-2">
                        <dt className="text-xs font-medium uppercase tracking-wide text-brand-muted">
                            Address
                        </dt>
                        <dd className="mt-1 text-sm font-semibold text-brand-text">
                            {user.address}
                        </dd>
                    </div>
                </dl>
            )}
        </Modal>
    );
}

export default UserReviewModal;
