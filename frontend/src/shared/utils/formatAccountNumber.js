export function formatAccountNumber(accountNumber) {
    const compactAccountNumber = String(accountNumber ?? "")
        .replace(/\s+/g, "");

    return compactAccountNumber
        .replace(/(.{4})(?=.)/g, "$1 ")
        .trim();
}

export function maskAccountNumber(accountNumber) {
    const compactAccountNumber = String(accountNumber ?? "")
        .replace(/\s+/g, "");

    if (compactAccountNumber.length <= 8) {
        return formatAccountNumber(compactAccountNumber);
    }

    const hiddenCharacterCount = compactAccountNumber.length - 8;
    const hiddenGroupCount = Math.max(
        1,
        Math.ceil(hiddenCharacterCount / 4),
    );

    return [
        compactAccountNumber.slice(0, 4),
        ...Array(hiddenGroupCount).fill("••••"),
        compactAccountNumber.slice(-4),
    ].join(" ");
}

export function maskAccountSuffix(accountNumber) {
    const compactAccountNumber = String(accountNumber ?? "")
        .replace(/\s+/g, "");

    return compactAccountNumber
        ? `•••• ${compactAccountNumber.slice(-4)}`
        : "—";
}
