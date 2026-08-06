import Button from "../../../../shared/components/ui/Button";
import StatusBadge from "../../../../shared/components/ui/StatusBadge";
import TableContainer from "../../../../shared/components/ui/TableContainer";

function PendingUsersTable({
    users,
    onApprove,
    onReject,
}) {
    return (
        <TableContainer
            aria-label="Pending user applications"
            className="min-h-[32rem] shadow-none"
        >
            <ul className="divide-y divide-brand-border px-4 md:hidden">
                {users.map((user) => (
                    <li
                        key={user.id}
                        className="py-5"
                    >
                        <div className="flex items-start justify-between gap-4">
                            <div className="min-w-0">
                                <p className="font-semibold text-brand-text">
                                    {user.name}
                                </p>
                                <p className="mt-1 break-all text-sm text-brand-muted">
                                    {user.email}
                                </p>
                            </div>

                            <StatusBadge status={user.approvalStatus} />
                        </div>

                        <p className="mt-3 text-sm leading-6 text-brand-muted">
                            {user.address}
                        </p>

                        <div className="mt-4 grid grid-cols-2 gap-3">
                            <Button
                                fullWidth
                                onClick={() => onApprove(user)}
                                aria-label={`Approve ${user.name}`}
                            >
                                Approve
                            </Button>
                            <Button
                                fullWidth
                                variant="danger"
                                onClick={() => onReject(user)}
                                aria-label={`Reject ${user.name}`}
                            >
                                Reject
                            </Button>
                        </div>
                    </li>
                ))}
            </ul>

            <div className="hidden p-5 md:block">
                <table className="w-full min-w-[880px] border-separate border-spacing-0 text-left text-sm">
                    <caption className="sr-only">
                        Account-holder applications waiting for administrator review
                    </caption>

                    <thead>
                        <tr className="text-xs text-brand-muted">
                            <th
                                scope="col"
                                className="rounded-l-xl bg-slate-50 px-5 py-3.5 font-semibold"
                            >
                                Name
                            </th>
                            <th
                                scope="col"
                                className="bg-slate-50 px-5 py-3.5 font-semibold"
                            >
                                Email
                            </th>
                            <th
                                scope="col"
                                className="bg-slate-50 px-5 py-3.5 font-semibold"
                            >
                                Address
                            </th>
                            <th
                                scope="col"
                                className="bg-slate-50 px-5 py-3.5 text-center font-semibold"
                            >
                                Status
                            </th>
                            <th
                                scope="col"
                                className="rounded-r-xl bg-slate-50 px-5 py-3.5 font-semibold"
                            >
                                Actions
                            </th>
                        </tr>
                    </thead>

                    <tbody>
                        {users.map((user) => (
                            <tr
                                key={user.id}
                                className="text-brand-text transition-colors hover:bg-slate-50/70"
                            >
                                <th
                                    scope="row"
                                    className="px-5 py-7 font-semibold"
                                >
                                    {user.name}
                                </th>
                                <td className="px-5 py-7 text-brand-muted">
                                    {user.email}
                                </td>
                                <td className="px-5 py-7 text-brand-muted">
                                    {user.address}
                                </td>
                                <td className="px-5 py-7 text-center">
                                    <StatusBadge status={user.approvalStatus} />
                                </td>
                                <td className="px-5 py-7">
                                    <div className="flex items-center gap-2.5">
                                        <Button
                                            className="min-w-24"
                                            onClick={() => onApprove(user)}
                                            aria-label={`Approve ${user.name}`}
                                        >
                                            Approve
                                        </Button>
                                        <Button
                                            variant="danger"
                                            className="min-w-20"
                                            onClick={() => onReject(user)}
                                            aria-label={`Reject ${user.name}`}
                                        >
                                            Reject
                                        </Button>
                                    </div>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </TableContainer>
    );
}

export default PendingUsersTable;
