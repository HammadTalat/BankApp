import { transactionHistoryMock } from "../../../../shared/mocks/transactionHistoryMock";

// Account-scoped dummy data until an admin transaction-history endpoint exists.
export const accountTransactionsMock = {
    "5839201746382915": transactionHistoryMock,
    "7301946285019472": [
        {
            id: 2001,
            operationId: "ae848768-0914-4e5c-923d-0ed513d2cc35",
            description: "Salary deposit",
            amount: 12000,
            indicator: "CREDIT",
            transactionDate: "2026-08-02T09:00:00",
            accountId: "7301946285019472",
            recipientAccountId: null,
        },
        {
            id: 2002,
            operationId: "9f0048b4-b82f-4999-99a5-d850479b1a80",
            description: "Utility bill",
            amount: 3800,
            indicator: "DEBIT",
            transactionDate: "2026-08-03T16:20:00",
            accountId: "7301946285019472",
            recipientAccountId: "9021574638104471",
        },
    ],
    "1947520183642278": [
        {
            id: 3001,
            operationId: "e3354395-683f-4ed7-887d-1021fbdfd297",
            description: "Closing transfer",
            amount: 1850,
            indicator: "DEBIT",
            transactionDate: "2026-08-02T08:45:00",
            accountId: "1947520183642278",
            recipientAccountId: "5839201746382915",
        },
    ],
};
