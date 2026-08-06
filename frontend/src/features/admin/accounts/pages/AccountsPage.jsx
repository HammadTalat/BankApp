import { useState } from "react";
import { Landmark } from "lucide-react";
import {
    useNavigate,
    useOutletContext,
} from "react-router";

import EmptyState from "../../../../shared/components/feedback/EmptyState";
import Button from "../../../../shared/components/ui/Button";
import PageHeader from "../../../../shared/components/ui/PageHeader";
import TableContainer from "../../../../shared/components/ui/TableContainer";
import { getAdminAccountDetailsPath } from "../../../../routes/routePaths";
import AccountFilters from "../components/AccountFilters";
import AccountsTable from "../components/AccountsTable";

const emptyFilters = {
    search: "",
    status: "",
};

function accountMatchesFilters(account, filters) {
    const normalizedSearch = filters.search
        .trim()
        .toLowerCase();
    const compactSearch = normalizedSearch.replace(/\s+/g, "");
    const matchesStatus =
        !filters.status || account.accountStatus === filters.status;

    if (!normalizedSearch) {
        return matchesStatus;
    }

    const matchesSearch =
        account.accountNumber.toLowerCase().includes(compactSearch) ||
        account.holderName.toLowerCase().includes(normalizedSearch) ||
        account.holderEmail.toLowerCase().includes(normalizedSearch);

    return matchesStatus && matchesSearch;
}

function AccountsPage() {
    const navigate = useNavigate();
    const { accounts } = useOutletContext();
    const [search, setSearch] = useState("");
    const [status, setStatus] = useState("");
    const [appliedFilters, setAppliedFilters] = useState(emptyFilters);

    const filteredAccounts = accounts.filter((account) =>
        accountMatchesFilters(account, appliedFilters),
    );

    function handleSubmit(event) {
        event.preventDefault();
        setAppliedFilters({
            search: search.trim(),
            status,
        });
    }

    function handleClearFilters() {
        setSearch("");
        setStatus("");
        setAppliedFilters(emptyFilters);
    }

    function handleViewAccount(account) {
        navigate(getAdminAccountDetailsPath(account.accountNumber));
    }

    return (
        <section className="space-y-6">
            <PageHeader
                title="Bank Accounts"
                description="Search by account number, holder name, or email, then filter by status."
            />

            <AccountFilters
                search={search}
                status={status}
                onSearchChange={setSearch}
                onStatusChange={setStatus}
                onSubmit={handleSubmit}
                onClear={handleClearFilters}
            />

            {filteredAccounts.length > 0 ? (
                <AccountsTable
                    accounts={filteredAccounts}
                    onView={handleViewAccount}
                />
            ) : (
                <TableContainer className="min-h-[32rem] shadow-none">
                    <div className="flex min-h-[32rem] items-center justify-center">
                        <EmptyState
                            icon={Landmark}
                            title="No bank accounts found"
                            description="No accounts match the current search and status filters."
                            action={(
                                <Button
                                    variant="secondary"
                                    onClick={handleClearFilters}
                                >
                                    Clear filters
                                </Button>
                            )}
                        />
                    </div>
                </TableContainer>
            )}
        </section>
    );
}

export default AccountsPage;
