import { useState } from "react";
import { UsersRound } from "lucide-react";

import Alert from "../shared/components/feedback/Alert";
import EmptyState from "../shared/components/feedback/EmptyState";
import LoadingSpinner from "../shared/components/feedback/LoadingSpinner";
import Button from "../shared/components/ui/Button";
import Card from "../shared/components/ui/Card";
import Input from "../shared/components/ui/Input";
import Modal from "../shared/components/ui/Modal";
import PageHeader from "../shared/components/ui/PageHeader";
import Pagination from "../shared/components/ui/Pagination";
import Select from "../shared/components/ui/Select";
import StatusBadge from "../shared/components/ui/StatusBadge";
import TableContainer from "../shared/components/ui/TableContainer";

const accountStatusOptions = [
    {
        value: "ACTIVE",
        label: "Active",
    },
    {
        value: "CLOSED",
        label: "Closed",
    },
];

const supportedStatuses = [
    "PENDING",
    "APPROVED",
    "REJECTED",
    "ACTIVE",
    "CLOSED",
    "CREDIT",
    "DEBIT",
];

function SectionTitle({ children, description }) {
    return (
        <div className="mb-4">
            <h2 className="text-xl font-semibold text-brand-text">
                {children}
            </h2>
            {description && (
                <p className="mt-1 text-sm text-brand-muted">
                    {description}
                </p>
            )}
        </div>
    );
}

function ComponentShowcasePage() {
    const [accountStatus, setAccountStatus] = useState("ACTIVE");
    const [page, setPage] = useState(0);
    const [isModalOpen, setIsModalOpen] = useState(false);

    function closeModal() {
        setIsModalOpen(false);
    }

    return (
        <main className="min-h-screen bg-brand-background px-5 py-8 lg:px-8 lg:py-10">
            <div className="mx-auto w-full max-w-[1440px]">
                <PageHeader
                    eyebrow="Bank App design system"
                    title="Shared component showcase"
                    description="A preview of the reusable controls, feedback messages and data-display components used across the banking application."
                    action={(
                        <Button
                            variant="secondary"
                            onClick={() => setPage(0)}
                        >
                            Reset pagination
                        </Button>
                    )}
                />

                <div className="mt-8 grid gap-6">
                    <Card>
                        <SectionTitle description="Consistent actions for normal, destructive and quiet workflows.">
                            Buttons
                        </SectionTitle>

                        <div className="flex flex-wrap items-center gap-3">
                            <Button>Primary button</Button>
                            <Button variant="secondary">
                                Secondary button
                            </Button>
                            <Button variant="danger">
                                Danger button
                            </Button>
                            <Button variant="ghost">
                                Ghost button
                            </Button>
                            <Button loading>
                                Saving
                            </Button>
                        </div>
                    </Card>

                    <div className="grid gap-6 lg:grid-cols-2">
                        <Card>
                            <SectionTitle description="Labels, helper text and validation remain visually consistent.">
                                Form controls
                            </SectionTitle>

                            <div className="grid gap-5 sm:grid-cols-2">
                                <Input
                                    id="showcase-name"
                                    name="name"
                                    label="Account holder name"
                                    defaultValue="Ali Khan"
                                    helperText="Enter the customer's full name."
                                />

                                <Input
                                    id="showcase-email"
                                    name="email"
                                    type="email"
                                    label="Email address"
                                    defaultValue="ali@"
                                    error="Enter a valid email address."
                                />

                                <Select
                                    id="showcase-status"
                                    name="status"
                                    label="Account status"
                                    value={accountStatus}
                                    onChange={(event) => {
                                        setAccountStatus(event.target.value);
                                    }}
                                    options={accountStatusOptions}
                                    placeholder="Select a status"
                                    containerClassName="sm:col-span-2"
                                />
                            </div>
                        </Card>

                        <Card>
                            <SectionTitle description="Statuses use color carefully while keeping the text readable.">
                                Status badges
                            </SectionTitle>

                            <div className="flex flex-wrap gap-3">
                                {supportedStatuses.map((status) => (
                                    <StatusBadge
                                        key={status}
                                        status={status}
                                    />
                                ))}
                            </div>
                        </Card>
                    </div>

                    <Card>
                        <SectionTitle description="Feedback styles for the main application outcomes.">
                            Alerts
                        </SectionTitle>

                        <div className="grid gap-4 lg:grid-cols-2">
                            <Alert
                                type="success"
                                title="User approved"
                            >
                                The bank account was created successfully.
                            </Alert>

                            <Alert
                                type="error"
                                title="Account could not be closed"
                            >
                                The account balance must be zero before closing.
                            </Alert>

                            <Alert
                                type="warning"
                                title="Approval pending"
                            >
                                This application still needs an administrator review.
                            </Alert>

                            <Alert
                                type="info"
                                title="Information"
                            >
                                Account details were last updated a few moments ago.
                            </Alert>
                        </div>
                    </Card>

                    <section>
                        <SectionTitle description="Wide banking tables stay inside the page and scroll horizontally when needed.">
                            Account table
                        </SectionTitle>

                        <TableContainer aria-label="Example bank accounts">
                            <table className="w-full min-w-[900px] text-left text-sm">
                                <thead className="border-b border-brand-border bg-slate-50 text-xs uppercase tracking-wide text-brand-muted">
                                    <tr>
                                        <th scope="col" className="px-5 py-3.5 font-semibold">
                                            Account holder
                                        </th>
                                        <th scope="col" className="px-5 py-3.5 font-semibold">
                                            Email
                                        </th>
                                        <th scope="col" className="px-5 py-3.5 font-semibold">
                                            Account number
                                        </th>
                                        <th scope="col" className="px-5 py-3.5 font-semibold">
                                            Balance
                                        </th>
                                        <th scope="col" className="px-5 py-3.5 font-semibold">
                                            Status
                                        </th>
                                    </tr>
                                </thead>
                                <tbody className="divide-y divide-brand-border">
                                    <tr className="text-brand-text hover:bg-slate-50/70">
                                        <td className="whitespace-nowrap px-5 py-4 font-medium">
                                            Ali Khan
                                        </td>
                                        <td className="px-5 py-4 text-brand-muted">
                                            ali@example.com
                                        </td>
                                        <td className="whitespace-nowrap px-5 py-4 font-mono text-brand-muted">
                                            5839201746382915
                                        </td>
                                        <td className="whitespace-nowrap px-5 py-4 font-medium">
                                            PKR 125,000.00
                                        </td>
                                        <td className="px-5 py-4">
                                            <StatusBadge status="ACTIVE" />
                                        </td>
                                    </tr>
                                </tbody>
                            </table>
                        </TableContainer>

                        <div className="mt-4">
                            <Pagination
                                page={page}
                                totalPages={4}
                                onPageChange={setPage}
                            />
                        </div>
                    </section>

                    <div className="grid gap-6 lg:grid-cols-2">
                        <Card>
                            <SectionTitle>
                                Loading state
                            </SectionTitle>

                            <div className="rounded-xl border border-dashed border-brand-border px-5 py-12">
                                <LoadingSpinner message="Loading accounts..." />
                            </div>
                        </Card>

                        <Card padding={false}>
                            <EmptyState
                                icon={UsersRound}
                                title="No pending users"
                                description="There are currently no applications waiting for approval."
                                action={(
                                    <Button variant="secondary" size="sm">
                                        Refresh list
                                    </Button>
                                )}
                            />
                        </Card>
                    </div>

                    <Card>
                        <SectionTitle description="Use confirmation modals for important banking actions.">
                            Modal
                        </SectionTitle>

                        <Button onClick={() => setIsModalOpen(true)}>
                            Open approval modal
                        </Button>
                    </Card>
                </div>
            </div>

            <Modal
                isOpen={isModalOpen}
                onClose={closeModal}
                title="Approve account holder"
                footer={(
                    <>
                        <Button
                            variant="secondary"
                            onClick={closeModal}
                            className="w-full sm:w-auto"
                        >
                            Cancel
                        </Button>
                        <Button
                            onClick={closeModal}
                            className="w-full sm:w-auto"
                        >
                            Approve
                        </Button>
                    </>
                )}
            >
                <p className="text-sm leading-6 text-brand-muted">
                    Approving Ali Khan will create a new active bank account
                    with an initial balance of PKR 0.00.
                </p>

                <dl className="mt-5 grid gap-4 rounded-xl bg-slate-50 p-4 sm:grid-cols-2">
                    <div>
                        <dt className="text-xs font-medium uppercase tracking-wide text-brand-muted">
                            Name
                        </dt>
                        <dd className="mt-1 text-sm font-semibold text-brand-text">
                            Ali Khan
                        </dd>
                    </div>
                    <div>
                        <dt className="text-xs font-medium uppercase tracking-wide text-brand-muted">
                            Email
                        </dt>
                        <dd className="mt-1 break-all text-sm font-semibold text-brand-text">
                            ali@example.com
                        </dd>
                    </div>
                </dl>
            </Modal>
        </main>
    );
}

export default ComponentShowcasePage;
