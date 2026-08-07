const statusClasses = {
    PENDING: "bg-amber-50 text-brand-warning",
    APPROVED: "bg-emerald-50 text-brand-success",
    REJECTED: "bg-red-50 text-brand-danger",
    ACTIVE: "bg-emerald-50 text-brand-success",
    CLOSED: "bg-slate-100 text-slate-600",
    CREDIT: "bg-emerald-50 text-brand-success",
    DEBIT: "bg-red-50 text-brand-danger",
};

const fallbackClasses = "bg-slate-100 text-slate-600";

function StatusBadge({ status, className = "" }) {
    const normalizedStatus = String(status ?? "")
        .trim()
        .toUpperCase();
    const label = normalizedStatus || "UNKNOWN";
    const colorClasses = statusClasses[normalizedStatus] || fallbackClasses;

    return (
        <span
            className={`inline-flex items-center rounded-full px-3 py-1 text-xs font-semibold ${colorClasses} ${className}`}
        >
            {label}
        </span>
    );
}

export default StatusBadge;
