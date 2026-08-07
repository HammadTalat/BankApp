import {
    useEffect,
    useState,
} from "react";

import {
    useNavigate,
    useParams,
} from "react-router";

import Alert
    from "../../../../shared/components/feedback/Alert";
import LoadingSpinner
    from "../../../../shared/components/feedback/LoadingSpinner";

import Card
    from "../../../../shared/components/ui/Card";
import PageHeader
    from "../../../../shared/components/ui/PageHeader";
import Pagination
    from "../../../../shared/components/ui/Pagination";

import TransactionDateFilters
    from "../../../../shared/components/transactions/TransactionDateFilters";
import TransactionHistoryTable
    from "../../../../shared/components/transactions/TransactionHistoryTable";

import {
    maskAccountNumber,
} from "../../../../shared/utils/formatAccountNumber";

import {
    getAdminAccountDetails,
    getAdminAccountTransactions,
} from "../api/adminAccountApi.js";

const PAGE_SIZE = 10;


function AccountTransactionsPage() {
    const { accountNumber } = useParams();
    const navigate = useNavigate();

    const [account, setAccount] = useState(null);
    const [transactions, setTransactions] = useState([]);

    const [loadingAccount, setLoadingAccount] = useState(true);
    const [loadingTransactions, setLoadingTransactions] = useState(true);

    const [error, setError] = useState("");

    const [fromDate, setFromDate] = useState("");
    const [toDate, setToDate] = useState("");

    const [appliedDates, setAppliedDates] = useState({
        startDate: "",
        endDate: "",
    });

    const [dateError, setDateError] = useState("");

    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);
    useEffect(() => {
        async function loadAccount() {
            try {
                setLoadingAccount(true);

                const response =
                    await getAdminAccountDetails(
                        accountNumber,
                    );

                setAccount(response);
            } catch (requestError) {
                setError(
                    requestError.message
                    || "Unable to load account information.",
                );
            } finally {
                setLoadingAccount(false);
            }
        }

        loadAccount();
    }, [accountNumber]);
    useEffect(() => {
        async function loadTransactions() {
            try {
                setLoadingTransactions(true);
                setError("");

                const response =
                    await getAdminAccountTransactions(
                        accountNumber,
                        {
                            startDate:
                            appliedDates.startDate,
                            endDate:
                            appliedDates.endDate,
                            page,
                            size: 10,
                        },
                    );

                console.log(
                    "Admin transactions:",
                    response,
                );

                setTransactions(
                    response.transactions ?? [],
                );

                setTotalPages(
                    response.totalPages ?? 0,
                );

                setTotalElements(
                    response.totalElements ?? 0,
                );
            } catch (requestError) {
                setError(
                    requestError.message
                    || "Unable to load transactions.",
                );
            } finally {
                setLoadingTransactions(false);
            }
        }

        loadTransactions();
    }, [
        accountNumber,
        appliedDates,
        page,
    ]);
    function handleApplyDates(event) {
        event.preventDefault();

        if (
            fromDate
            && toDate
            && fromDate > toDate
        ) {
            setDateError(
                "The From date must be before or equal to the To date.",
            );

            return;
        }

        setDateError("");
        setPage(0);

        setAppliedDates({
            startDate: fromDate,
            endDate: toDate,
        });
    }
    function handleClearDates() {
        setFromDate("");
        setToDate("");
        setDateError("");
        setPage(0);

        setAppliedDates({
            startDate: "",
            endDate: "",
        });
    }

    if (loadingAccount) {
        return (
            <div className="flex min-h-72 items-center justify-center">
                <LoadingSpinner
                    message="Loading account..."
                />
            </div>
        );
    }

    return (
        <section className="space-y-7">
            <PageHeader
                title="Account Transactions"
                description={
                    account
                        ? `Transactions for account ${maskAccountNumber(account.accountNumber)} — ${account.holderName}`
                        : "Review account transaction history."
                }
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

            {error && (
                <Alert
                    type="error"
                    title="Transactions could not be loaded"
                >
                    {error}
                </Alert>
            )}

            <Card className="min-h-[35rem] shadow-none">
                <h2 className="text-xl font-semibold text-brand-text">
                    Transaction history
                </h2>

                <p className="mt-1 text-sm text-brand-muted">
                    {totalElements} transaction
                    {totalElements === 1 ? "" : "s"} found
                </p>

                <div className="mt-5">
                    <TransactionHistoryTable
                        transactions={transactions}
                        loading={loadingTransactions}
                    />
                </div>
            </Card>

            <Pagination
                page={page}
                totalPages={totalPages}
                onPageChange={setPage}
                disabled={loadingTransactions}
            />
        </section>
    );
}

export default AccountTransactionsPage;
