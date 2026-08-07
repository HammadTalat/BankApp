import { useState } from "react";

import Alert from "../feedback/Alert";
import Card from "../ui/Card";
import PageHeader from "../ui/PageHeader";
import { getIsoDatePart } from "../../utils/formatDate";
import TransactionDateFilters from "./TransactionDateFilters";
import TransactionHistoryTable from "./TransactionHistoryTable";

function transactionMatchesDates(transaction, appliedDates) {
    const transactionDate = getIsoDatePart(transaction.transactionDate);

    if (!transactionDate) {
        return false;
    }

    const matchesFrom =
        !appliedDates.from || transactionDate >= appliedDates.from;
    const matchesTo =
        !appliedDates.to || transactionDate <= appliedDates.to;

    return matchesFrom && matchesTo;
}

function TransactionHistoryView({
    title,
    description,
    transactions = [],
    loading = false,
    initialFromDate = "2026-08-01",
    initialToDate = "2026-08-04",
}) {
    const [fromDate, setFromDate] = useState(initialFromDate);
    const [toDate, setToDate] = useState(initialToDate);
    const [appliedDates, setAppliedDates] = useState({
        from: "",
        to: "",
    });
    const [dateError, setDateError] = useState("");

    const visibleTransactions = transactions.filter((transaction) =>
        transactionMatchesDates(transaction, appliedDates),
    );

    function handleApplyDates(event) {
        event.preventDefault();

        if (fromDate && toDate && fromDate > toDate) {
            setDateError("The From date must be before or equal to the To date.");
            return;
        }

        setDateError("");
        setAppliedDates({
            from: fromDate,
            to: toDate,
        });
    }

    function handleClearDates() {
        setFromDate("");
        setToDate("");
        setAppliedDates({
            from: "",
            to: "",
        });
        setDateError("");
    }

    return (
        <section className="space-y-7">
            <PageHeader
                title={title}
                description={description}
            />

            <TransactionDateFilters
                fromDate={fromDate}
                toDate={toDate}
                onFromDateChange={setFromDate}
                onToDateChange={setToDate}
                onApply={handleApplyDates}
                onClear={handleClearDates}
            />

            {dateError && (
                <Alert
                    type="error"
                    title="Invalid date range"
                >
                    {dateError}
                </Alert>
            )}

            <Card className="min-h-[35rem] shadow-none">
                <h2 className="text-xl font-semibold text-brand-text">
                    Transaction history
                </h2>

                <div className="mt-5">
                    <TransactionHistoryTable
                        transactions={visibleTransactions}
                        loading={loading}
                    />
                </div>
            </Card>
        </section>
    );
}

export default TransactionHistoryView;
