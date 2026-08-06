import { Landmark } from "lucide-react";
import {
    useNavigate,
    useOutletContext,
    useParams,
} from "react-router";

import EmptyState from "../../../../shared/components/feedback/EmptyState";
import TransactionHistoryView from "../../../../shared/components/transactions/TransactionHistoryView";
import Button from "../../../../shared/components/ui/Button";
import Card from "../../../../shared/components/ui/Card";
import PageHeader from "../../../../shared/components/ui/PageHeader";
import { ROUTES } from "../../../../routes/routePaths";
import { maskAccountNumber } from "../../../../shared/utils/formatAccountNumber";
import { accountTransactionsMock } from "../mocks/accountTransactionsMock";

function AccountTransactionsPage() {
    const navigate = useNavigate();
    const { accounts } = useOutletContext();
    const { accountNumber } = useParams();
    const account = accounts.find(
        (currentAccount) => currentAccount.accountNumber === accountNumber,
    );

    if (!account) {
        return (
            <section className="space-y-6">
                <PageHeader
                    title="Account Transactions"
                    description="Review account transaction history."
                />
                <Card className="shadow-none">
                    <EmptyState
                        icon={Landmark}
                        title="Account not found"
                        description="This account is not available in the current dummy account data."
                        action={(
                            <Button
                                variant="secondary"
                                onClick={() => navigate(ROUTES.ADMIN_ACCOUNTS)}
                            >
                                Back to bank accounts
                            </Button>
                        )}
                    />
                </Card>
            </section>
        );
    }

    return (
        <TransactionHistoryView
            title="Account Transactions"
            description={`Transactions for account ${maskAccountNumber(account.accountNumber)} — ${account.holderName}`}
            transactions={accountTransactionsMock[account.accountNumber] || []}
        />
    );
}

export default AccountTransactionsPage;
