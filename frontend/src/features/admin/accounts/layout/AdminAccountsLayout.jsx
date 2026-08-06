import { useState } from "react";
import { Outlet } from "react-router";

import { accountDetailsMock } from "../mocks/accountDetailsMock";

function AdminAccountsLayout() {
    const [accounts, setAccounts] = useState(() =>
        accountDetailsMock.map((account) => ({ ...account })),
    );

    function updateAccount(accountNumber, changes) {
        setAccounts((currentAccounts) =>
            currentAccounts.map((account) =>
                account.accountNumber === accountNumber
                    ? {
                        ...account,
                        ...changes,
                    }
                    : account,
            ),
        );
    }

    return (
        <Outlet
            context={{
                accounts,
                updateAccount,
            }}
        />
    );
}

export default AdminAccountsLayout;
