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
            "Your registration is complete. An administrator must approve your account before banking features become available.",
    },
    REJECTED: {
        icon: ShieldX,
        iconClasses: "bg-red-50 text-brand-danger",
        label: "APPLICATION REJECTED",
        labelClasses: "text-brand-danger",
        title: "Your application was not approved",
        message:
            "We are sorry, but access to banking features cannot be provided at this time. You can refresh the page if your application is reviewed again.",
    },
    APPROVED: {
        icon: CircleCheck,
        iconClasses: "bg-emerald-50 text-brand-success",
        label: "APPLICATION APPROVED",
        labelClasses: "text-brand-success",
        title: "Your account is ready",
        message:
            "Your application has been approved. We are taking you to your account now.",
    },
    UNKNOWN: {
        icon: CircleHelp,
        iconClasses: "bg-slate-100 text-slate-600",
        label: "APPLICATION STATUS UNAVAILABLE",
        labelClasses: "text-slate-600",
        title: "We could not read your application status",
        message:
            "Your account information is available, but its approval status is missing. Please refresh and try again.",
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
            className="w-full max-w-xl p-7 text-center sm:p-10"
        >
            <div
                className={`mx-auto flex h-16 w-16 items-center justify-center rounded-2xl ${selectedStatus.iconClasses}`}
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

            <h1 className="mt-3 text-2xl font-bold text-brand-text sm:text-3xl">
                {selectedStatus.title}
            </h1>

            <p className="mx-auto mt-4 max-w-md text-sm leading-6 text-brand-muted sm:text-base sm:leading-7">
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
                <p className="mt-6 text-sm text-brand-muted">
                    This page checks your status automatically every five seconds.
                </p>
            )}
        </Card>
    );
}

export default ApplicationStatusCard;
