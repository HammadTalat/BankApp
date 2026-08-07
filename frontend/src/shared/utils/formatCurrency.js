export function formatCurrency(amount, options = {}) {
    const numericAmount = Number(amount);

    if (!Number.isFinite(numericAmount)) {
        return "—";
    }

    const {
        currency = "PKR",
        minimumFractionDigits =
            numericAmount === 0 || !Number.isInteger(numericAmount)
                ? 2
                : 0,
        maximumFractionDigits = 2,
    } = options;

    const formattedAmount = numericAmount.toLocaleString("en-US", {
        minimumFractionDigits,
        maximumFractionDigits,
    });

    return `${currency} ${formattedAmount}`;
}
