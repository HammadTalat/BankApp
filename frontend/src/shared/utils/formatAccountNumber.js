export function formatAccountNumber(accountNumber) {
    const compactAccountNumber = String(accountNumber ?? "")
        .replace(/\s+/g, "");

    return compactAccountNumber
        .replace(/(.{4})(?=.)/g, "$1 ")
        .trim();
}
