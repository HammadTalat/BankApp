import Button from "../../../../shared/components/ui/Button";
import StatusBadge from "../../../../shared/components/ui/StatusBadge";
import TableContainer from "../../../../shared/components/ui/TableContainer";
import { formatAccountNumber } from "../../../../shared/utils/formatAccountNumber";
import { formatCurrency } from "../../../../shared/utils/formatCurrency";

function AccountsTable({ accounts, onView }) {
    return (
        <TableContainer
            aria-label="Bank accounts"
            className="min-h-[32rem] shadow-none"
        >
            <ul className="divide-y divide-brand-border px-4 md:hidden">
                {accounts.map((account) => (
                    <li
                        key={account.accountNumber}
                        className="py-5"
                    >
                        <div className="flex items-start justify-between gap-4">
                            <div className="min-w-0">
                                <p className="text-xs font-medium uppercase tracking-wide text-brand-muted">
                                    Account number
                                </p>
                                <p className="mt-1 whitespace-nowrap font-medium text-brand-text">
                                    {formatAccountNumber(account.accountNumber)}
                                </p>
                            </div>

                            <StatusBadge status={account.accountStatus} />
                        </div>

                        <div className="mt-4 grid gap-3 sm:grid-cols-2">
                            <div>
                                <p className="text-xs font-medium uppercase tracking-wide text-brand-muted">
                                    Account holder
                                </p>
                                <p className="mt-1 font-semibold text-brand-text">
                                    {account.holderName}
                                </p>
                                <p className="mt-1 break-all text-sm text-brand-muted">
                                    {account.holderEmail}
                                </p>
                            </div>
                            <div>
                                <p className="text-xs font-medium uppercase tracking-wide text-brand-muted">
                                    Balance
                                </p>
                                <p className="mt-1 font-semibold text-brand-text">
                                    {formatCurrency(account.balance)}
                                </p>
                            </div>
                        </div>

                        <Button
                            variant="secondary"
                            fullWidth
                            className="mt-5"
                            onClick={() => onView(account)}
                            aria-label={`View account ${formatAccountNumber(account.accountNumber)}`}
                        >
                            View
                        </Button>
                    </li>
                ))}
            </ul>

            <div className="hidden p-5 md:block">
                <table className="w-full min-w-[900px] border-separate border-spacing-0 text-left text-sm">
                    <caption className="sr-only">
                        Bank accounts matching the selected filters
                    </caption>

                    <thead>
                        <tr className="text-xs text-brand-muted">
                            <th
                                scope="col"
                                className="rounded-l-xl bg-slate-50 px-5 py-3.5 font-semibold"
                            >
                                Account Number
                            </th>
                            <th
                                scope="col"
                                className="bg-slate-50 px-5 py-3.5 font-semibold"
                            >
                                Account Holder
                            </th>
                            <th
                                scope="col"
                                className="bg-slate-50 px-5 py-3.5 font-semibold"
                            >
                                Email
                            </th>
                            <th
                                scope="col"
                                className="bg-slate-50 px-5 py-3.5 text-center font-semibold"
                            >
                                Status
                            </th>
                            <th
                                scope="col"
                                className="bg-slate-50 px-5 py-3.5 font-semibold"
                            >
                                Balance
                            </th>
                            <th
                                scope="col"
                                className="rounded-r-xl bg-slate-50 px-5 py-3.5 font-semibold"
                            >
                                <span className="sr-only">Actions</span>
                            </th>
                        </tr>
                    </thead>

                    <tbody>
                        {accounts.map((account) => (
                            <tr
                                key={account.accountNumber}
                                className="text-brand-text transition-colors hover:bg-slate-50/70"
                            >
                                <th
                                    scope="row"
                                    className="whitespace-nowrap px-5 py-7 font-normal text-brand-muted"
                                >
                                    {formatAccountNumber(account.accountNumber)}
                                </th>
                                <td className="px-5 py-7 font-semibold">
                                    {account.holderName}
                                </td>
                                <td className="px-5 py-7 text-brand-muted">
                                    {account.holderEmail}
                                </td>
                                <td className="px-5 py-7 text-center">
                                    <StatusBadge status={account.accountStatus} />
                                </td>
                                <td className="whitespace-nowrap px-5 py-7 font-semibold">
                                    {formatCurrency(account.balance)}
                                </td>
                                <td className="px-5 py-7 text-right">
                                    <Button
                                        variant="secondary"
                                        className="min-w-20"
                                        onClick={() => onView(account)}
                                        aria-label={`View account ${formatAccountNumber(account.accountNumber)}`}
                                    >
                                        View
                                    </Button>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </TableContainer>
    );
}

export default AccountsTable;
