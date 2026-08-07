import Button from "../ui/Button";
import Card from "../ui/Card";

function DateControl({ label, value, onChange }) {
    return (
        <label className="flex min-h-11 w-full cursor-pointer items-center gap-2 rounded-xl border border-brand-border bg-brand-surface px-4 shadow-sm transition focus-within:border-brand-primary focus-within:ring-4 focus-within:ring-blue-100 sm:w-56">
            <span className="shrink-0 text-sm font-semibold text-brand-muted">
                {label}:
            </span>
            <input
                type="date"
                value={value}
                onChange={(event) => onChange(event.target.value)}
                className="min-w-0 flex-1 cursor-pointer bg-transparent text-sm font-semibold text-brand-text outline-none"
                aria-label={`${label} date`}
            />
        </label>
    );
}

function TransactionDateFilters({
    fromDate,
    toDate,
    onFromDateChange,
    onToDateChange,
    onApply,
    onClear,
}) {
    return (
        <Card className="shadow-none">
            <form
                className="flex flex-col gap-3 sm:flex-row sm:flex-wrap sm:items-center"
                onSubmit={onApply}
            >
                <DateControl
                    label="From"
                    value={fromDate}
                    onChange={onFromDateChange}
                />
                <DateControl
                    label="To"
                    value={toDate}
                    onChange={onToDateChange}
                />
                <Button
                    type="submit"
                    className="w-full sm:w-36"
                >
                    Apply dates
                </Button>
                <Button
                    variant="secondary"
                    className="w-full sm:w-28"
                    onClick={onClear}
                >
                    Clear
                </Button>
            </form>
        </Card>
    );
}

export default TransactionDateFilters;
