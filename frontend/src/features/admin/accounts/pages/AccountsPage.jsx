import { useState,useEffect} from "react";
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
import {getAdminAccounts} from "../api/adminAccountApi.js"
import Alert from "../../../../shared/components/feedback/Alert";
import LoadingSpinner from "../../../../shared/components/feedback/LoadingSpinner";
import Pagination from "../../../../shared/components/ui/Pagination";

const emptyFilters = {
    search: "",
    status: "",
};



function AccountsPage() {
    const [accounts, setAccounts] = useState([]);

    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);


    const [search, setSearch] = useState("");
    const [status, setStatus] = useState("");
    const [appliedFilters, setAppliedFilters] = useState(emptyFilters);
    const navigate = useNavigate();

    useEffect(() => {
        async function loadAccounts() {
            try {
                setLoading(true);
                setError("");

                const response = await getAdminAccounts({
                    search: appliedFilters.search,
                    status: appliedFilters.status,
                    page,
                    size: 10,
                });

                console.log(
                    "Accounts response:",
                    response,
                );

                setAccounts(response.content);
                setTotalPages(response.totalPages);
                setTotalElements(response.totalElements);
            } catch (requestError) {
                console.error(
                    "Unable to load accounts:",
                    requestError,
                );

                setError(
                    requestError.message
                    || "Unable to load bank accounts.",
                );
            } finally {
                setLoading(false);
            }
        }

        loadAccounts();
    }, [appliedFilters, page]);

    function handleSubmit(event) {
        event.preventDefault();
        setPage(0);
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

            {loading ? (

                <TableContainer className="min-h-[32rem] shadow-none">
                    <div className="flex min-h-[32rem] items-center justify-center">
                        <LoadingSpinner
                            message="Loading bank accounts..."
                        />
                    </div>
                </TableContainer>

            ) : accounts.length > 0 ? (

                <>
                    <AccountsTable
                        accounts={accounts}
                        onView={handleViewAccount}
                    />

                    <div className="flex flex-col gap-3">

                        <p className="text-sm text-brand-muted">
                            {totalElements} account
                            {totalElements === 1
                                ? ""
                                : "s"} found
                        </p>

                        <Pagination
                            page={page}
                            totalPages={totalPages}
                            onPageChange={setPage}
                            disabled={loading}
                        />

                    </div>
                </>

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
                                    onClick={
                                        handleClearFilters
                                    }
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
