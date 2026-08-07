import Button from "../../../../shared/components/ui/Button";
import Card from "../../../../shared/components/ui/Card";
import Input from "../../../../shared/components/ui/Input";
import Select from "../../../../shared/components/ui/Select";

const accountStatusOptions = [
    {
        value: "ACTIVE",
        label: "Active Accounts",
    },
    {
        value: "CLOSED",
        label: "Closed Accounts",
    },
];

function AccountFilters({
    search,
    status,
    onSearchChange,
    onStatusChange,
    onSubmit,
    onClear,
}) {
    return (
        <Card className="shadow-none">
            <form
                className="grid gap-3 md:grid-cols-2 xl:grid-cols-[minmax(0,1fr)_150px_130px_120px]"
                role="search"
                onSubmit={onSubmit}
            >
                <Input
                    id="account-search"
                    name="search"
                    type="search"
                    value={search}
                    onChange={(event) => onSearchChange(event.target.value)}
                    placeholder="Search by account number, name, or email"
                    aria-label="Search bank accounts"
                    autoComplete="off"
                    containerClassName="md:col-span-2 xl:col-span-1"
                    className="min-h-11"
                />

                <Select
                    id="account-status"
                    name="status"
                    value={status}
                    onChange={(event) => onStatusChange(event.target.value)}
                    options={accountStatusOptions}
                    placeholder="All Accounts"
                    aria-label="Filter accounts by status"
                    className="min-h-11"
                />

                <Button
                    type="button"
                    variant="secondary"
                    fullWidth
                    onClick={onClear}
                >
                    Clear filters
                </Button>

                <Button
                    type="submit"
                    fullWidth
                >
                    Search
                </Button>
            </form>
        </Card>
    );
}

export default AccountFilters;
