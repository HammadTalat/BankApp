import {
    CircleCheck,
    CircleHelp,
    Clock3,
    LogOut,
    RefreshCw,
    ShieldX,
} from "lucide-react";

import Button from "../../../shared/components/ui/Button";
import Card from "../../../shared/components/ui/Card";
import LoadingSpinner from "../../../shared/components/feedback/LoadingSpinner";

const statusDetails = {
    PENDING: {
        icon: Clock3,
        iconClasses: "bg-amber-50 text-brand-warning",
        label: "APPLICATION PENDING",
        labelClasses: "text-brand-warning",
        title: "Your application is under review",
        message:
            "Your account is awaiting administrator review.",
    },
    REJECTED: {
        icon: ShieldX,
        iconClasses: "bg-red-50 text-brand-danger",
        label: "APPLICATION REJECTED",
        labelClasses: "text-brand-danger",
        title: "Your application was not approved",
        message:
            "Your application was not approved. Refresh this page if it is reviewed again.",
    },
    APPROVED: {
        icon: CircleCheck,
        iconClasses: "bg-emerald-50 text-brand-success",
        label: "APPLICATION APPROVED",
        labelClasses: "text-brand-success",
        title: "Your account is ready",
        message:
            "Redirecting you to your account.",
    },
    UNKNOWN: {
        icon: CircleHelp,
        iconClasses: "bg-slate-100 text-slate-600",
        label: "APPLICATION STATUS UNAVAILABLE",
        labelClasses: "text-slate-600",
        title: "We could not read your application status",
        message:
            "Please refresh and try again.",
    },
};

function ApplicationStatusCard({
    status,
    userName,
    userEmail,
    onRefresh,
    refreshing = false,
    onLogout,
}) {
    const normalizedStatus = String(status ?? "")
        .trim()
        .toUpperCase();
    const selectedStatus = statusDetails[normalizedStatus] || statusDetails.UNKNOWN;
    const StatusIcon = selectedStatus.icon;
    const showRefreshButton = normalizedStatus !== "APPROVED";
    const showUserInformation = Boolean(userName || userEmail);

    return (
        <Card
            padding={false}
            className="w-full max-w-xl p-7 text-center sm:p-9"
            data-testid="application-status-card"
        >
            <div
                className={`mx-auto flex h-14 w-14 items-center justify-center rounded-lg ${selectedStatus.iconClasses}`}
            >
                <StatusIcon
                    size={32}
                    strokeWidth={1.8}
                    aria-hidden="true"
                />
            </div>

            <p
                className={`mt-6 text-sm font-semibold tracking-wide ${selectedStatus.labelClasses}`}
            >
                {selectedStatus.label}
            </p>

            <h1 className="mt-3 text-2xl font-bold text-brand-text">
                {selectedStatus.title}
            </h1>

            <p className="mx-auto mt-3 max-w-md text-sm leading-6 text-brand-muted">
                {selectedStatus.message}
            </p>

            {showUserInformation && (
                <div className="mx-auto mt-6 max-w-sm rounded-xl border border-brand-border bg-brand-background px-4 py-3">
                    {userName && (
                        <p className="font-semibold text-brand-text">
                            {userName}
                        </p>
                    )}

                    {userEmail && (
                        <p className="mt-0.5 break-all text-sm text-brand-muted">
                            {userEmail}
                        </p>
                    )}
                </div>
            )}

            {normalizedStatus === "APPROVED" ? (
                <div className="mt-7">
                    <LoadingSpinner
                        size="sm"
                        message="Redirecting to your account..."
                    />
                </div>
            ) : (
                <div className="mt-7 flex flex-col justify-center gap-3 sm:flex-row">
                    {showRefreshButton && onRefresh && (
                        <Button
                            onClick={onRefresh}
                            loading={refreshing}
                        >
                            {!refreshing && (
                                <RefreshCw
                                    size={18}
                                    aria-hidden="true"
                                />
                            )}
                            Refresh status
                        </Button>
                    )}

                    {onLogout && (
                        <Button
                            variant="secondary"
                            onClick={onLogout}
                            disabled={refreshing}
                        >
                            <LogOut
                                size={18}
                                aria-hidden="true"
                            />
                            Log out
                        </Button>
                    )}
                </div>
            )}

            {normalizedStatus === "PENDING" && (
                <p className="mt-6 text-xs text-brand-muted">Status updates automatically.</p>
            )}
        </Card>
    );
}

export default ApplicationStatusCard;
