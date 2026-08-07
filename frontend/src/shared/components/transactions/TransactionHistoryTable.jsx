import { ReceiptText } from "lucide-react";

import EmptyState from "../feedback/EmptyState";
import LoadingSpinner from "../feedback/LoadingSpinner";
import StatusBadge from "../ui/StatusBadge";
import { maskAccountSuffix } from "../../utils/formatAccountNumber";
import { formatCurrency } from "../../utils/formatCurrency";
import { formatDate } from "../../utils/formatDate";

function getAmountClasses(indicator) {
    return indicator === "DEBIT"
        ? "text-brand-danger"
        : "text-brand-success";
}

function TransactionHistoryTable({ transactions, loading = false }) {
    if (loading) {
        return (
            <div className="flex min-h-80 items-center justify-center">
                <LoadingSpinner message="Loading transactions..." />
            </div>
        );
    }

    if (transactions.length === 0) {
        return (
            <div className="flex min-h-80 items-center justify-center">
                <EmptyState
                    icon={ReceiptText}
                    title="No transactions found"
                    description="No transactions match the selected date range."
                />
            </div>
        );
    }

    return (
        <>
            <ul className="divide-y divide-brand-border md:hidden">
                {transactions.map((transaction) => (
                    <li
                        key={transaction.id || transaction.operationId}
                        className="py-5"
                    >
                        <div className="flex items-start justify-between gap-4">
                            <p className="text-sm text-brand-muted">
                                {formatDate(transaction.transactionDate)}
                            </p>
                            <StatusBadge
                                status={transaction.indicator}
                                className="min-w-24 justify-center"
                            />
                        </div>

                        <p className="mt-3 font-semibold text-brand-text">
                            {transaction.description || "Transaction"}
                        </p>

                        <div className="mt-4 flex items-end justify-between gap-4">
                            <div>
                                <p className="text-xs font-medium uppercase tracking-wide text-brand-muted">
                                    Related account
                                </p>
                                <p className="mt-1 text-sm text-brand-muted">
                                    {maskAccountSuffix(transaction.recipientAccountId)}
                                </p>
                            </div>
                            <p className={`whitespace-nowrap font-semibold ${getAmountClasses(transaction.indicator)}`}>
                                {formatCurrency(transaction.amount)}
                            </p>
                        </div>
                    </li>
                ))}
            </ul>

            <div className="hidden overflow-x-auto md:block">
                <table className="w-full min-w-[760px] table-fixed text-left text-sm">
                    <caption className="sr-only">
                        Transaction history
                    </caption>
                    <thead className="sr-only">
                        <tr>
                            <th scope="col">Date</th>
                            <th scope="col">Description</th>
                            <th scope="col">Related account</th>
                            <th scope="col">Indicator</th>
                            <th scope="col">Amount</th>
                        </tr>
                    </thead>
                    <tbody>
                        {transactions.map((transaction) => (
                            <tr key={transaction.id || transaction.operationId}>
                                <td className="w-[17%] py-7 pr-4 text-brand-muted">
                                    {formatDate(transaction.transactionDate)}
                                </td>
                                <th
                                    scope="row"
                                    className="w-[30%] px-4 py-7 font-semibold text-brand-text"
                                >
                                    {transaction.description || "Transaction"}
                                </th>
                                <td className="w-[22%] px-4 py-7 text-brand-muted">
                                    {maskAccountSuffix(transaction.recipientAccountId)}
                                </td>
                                <td className="w-[16%] px-4 py-7">
                                    <StatusBadge
                                        status={transaction.indicator}
                                        className="min-w-24 justify-center"
                                    />
                                </td>
                                <td className={`w-[15%] whitespace-nowrap py-7 pl-4 font-semibold ${getAmountClasses(transaction.indicator)}`}>
                                    {formatCurrency(transaction.amount)}
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </>
    );
}

export default TransactionHistoryTable;
