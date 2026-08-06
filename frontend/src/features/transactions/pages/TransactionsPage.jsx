import { Link, useNavigate } from "react-router";

import { ACCESS_TOKEN_KEY } from "../../../api/apiConfig";
import { ROUTES } from "../../../routes/routePaths";
import TransactionHistoryView from "../../../shared/components/transactions/TransactionHistoryView";
import { transactionHistoryMock } from "../../../shared/mocks/transactionHistoryMock";

function TransactionsPage() {
    const navigate = useNavigate();

    function handleLogout() {
        localStorage.removeItem(ACCESS_TOKEN_KEY);
        navigate(ROUTES.HOME);
    }

    return (
        <div className="flex min-h-screen bg-brand-background">
            <aside className="hidden w-64 shrink-0 flex-col justify-between bg-brand-navy px-6 py-8 text-white lg:flex">
                <div>
                    <div className="mb-10">
                        <h1 className="text-xl font-bold tracking-tight text-white">
                            NexaBank
                        </h1>
                        <p className="mt-0.5 text-xs text-slate-400">
                            Personal Banking
                        </p>
                    </div>

                    <nav className="space-y-2">
                        <Link
                            to={ROUTES.ACCOUNT_HOME}
                            className="flex items-center rounded-lg px-4 py-3 text-sm font-medium text-slate-300 transition-colors hover:bg-white/5 hover:text-white"
                        >
                            Dashboard
                        </Link>
                        <Link
                            to={ROUTES.ACCOUNT_TRANSFERS}
                            className="flex items-center rounded-lg px-4 py-3 text-sm font-medium text-slate-300 transition-colors hover:bg-white/5 hover:text-white"
                        >
                            Transfer Money
                        </Link>
                        <Link
                            to={ROUTES.ACCOUNT_TRANSACTIONS}
                            className="flex items-center rounded-lg bg-white/10 px-4 py-3 text-sm font-semibold text-white"
                        >
                            Transactions
                        </Link>
                        <Link
                            to={ROUTES.ACCOUNT_PROFILE}
                            className="flex items-center rounded-lg px-4 py-3 text-sm font-medium text-slate-300 transition-colors hover:bg-white/5 hover:text-white"
                        >
                            Profile
                        </Link>
                    </nav>
                </div>

                <button
                    type="button"
                    onClick={handleLogout}
                    className="text-left text-sm font-medium text-pink-300 transition-colors hover:text-pink-200"
                >
                    Logout
                </button>
            </aside>

            <main className="min-w-0 flex-1 px-4 py-6 sm:px-6 lg:p-10">
                <div className="mx-auto w-full max-w-5xl">
                    <TransactionHistoryView
                        title="Transactions"
                        description="Review credits, debits, and transfers across your account."
                        transactions={transactionHistoryMock}
                    />
                </div>
            </main>
        </div>
    );
}

export default TransactionsPage;
