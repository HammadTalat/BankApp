const statusClasses = {
    PENDING: "bg-amber-50 text-brand-warning ring-amber-200",
    APPROVED: "bg-emerald-50 text-brand-success ring-emerald-200",
    REJECTED: "bg-red-50 text-brand-danger ring-red-200",
    ACTIVE: "bg-emerald-50 text-brand-success ring-emerald-200",
    CLOSED: "bg-slate-100 text-slate-600 ring-slate-200",
    CREDIT: "bg-emerald-50 text-brand-success ring-emerald-200",
    DEBIT: "bg-red-50 text-brand-danger ring-red-200",
};

const fallbackClasses = "bg-slate-100 text-slate-600 ring-slate-200";

function StatusBadge({ status }) {
    const normalizedStatus = String(status ?? "")
        .trim()
        .toUpperCase();
    const label = normalizedStatus || "UNKNOWN";
    const colorClasses = statusClasses[normalizedStatus] || fallbackClasses;

    return (
        <span
            className={`inline-flex items-center rounded-full px-2.5 py-1 text-xs font-semibold ring-1 ring-inset ${colorClasses}`}
        >
            {label}
        </span>
    );
}

export default StatusBadge;
