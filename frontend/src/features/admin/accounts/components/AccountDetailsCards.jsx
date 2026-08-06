import Card from "../../../../shared/components/ui/Card";
import StatusBadge from "../../../../shared/components/ui/StatusBadge";
import { formatAccountNumber } from "../../../../shared/utils/formatAccountNumber";
import { formatCurrency } from "../../../../shared/utils/formatCurrency";

function AccountDetailsCards({ account }) {
    return (
        <div className="grid gap-5 lg:grid-cols-2">
            <Card className="min-h-64 shadow-none sm:min-h-[17.5rem]">
                <h2 className="text-xl font-semibold text-brand-text">
                    Account
                </h2>

                <p className="mt-4 break-words text-xl font-bold tracking-tight text-brand-text sm:text-2xl">
                    {formatAccountNumber(account.accountNumber)}
                </p>

                <div className="mt-4">
                    <StatusBadge status={account.accountStatus} />
                </div>

                <p className="mt-7 text-sm text-brand-muted">
                    Current balance
                </p>
                <p className="mt-2 text-2xl font-bold tracking-tight text-brand-text sm:text-[28px]">
                    {formatCurrency(account.balance, {
                        minimumFractionDigits: 2,
                    })}
                </p>
            </Card>

            <Card className="min-h-64 shadow-none sm:min-h-[17.5rem]">
                <h2 className="text-xl font-semibold text-brand-text">
                    Account holder
                </h2>

                <div className="mt-4 space-y-2">
                    <p className="text-lg font-bold text-brand-text">
                        {account.holderName}
                    </p>
                    <p className="break-all text-sm text-brand-muted">
                        {account.holderEmail}
                    </p>
                    <p className="text-sm text-brand-muted">
                        {account.holderAddress}
                    </p>
                </div>

                <div className="mt-7">
                    <StatusBadge status={account.approvalStatus} />
                </div>
            </Card>
        </div>
    );
}

export default AccountDetailsCards;
