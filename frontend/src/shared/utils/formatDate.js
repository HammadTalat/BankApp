const displayDateFormatter = new Intl.DateTimeFormat("en-GB", {
    day: "2-digit",
    month: "short",
    year: "numeric",
    timeZone: "UTC",
});

export function getIsoDatePart(value) {
    const match = String(value ?? "").match(/^(\d{4})-(\d{2})-(\d{2})/);

    return match ? match[0] : "";
}

export function formatDate(value) {
    const isoDate = getIsoDatePart(value);

    if (!isoDate) {
        return "—";
    }

    const [year, month, day] = isoDate.split("-").map(Number);
    const date = new Date(Date.UTC(year, month - 1, day));

    return displayDateFormatter.format(date);
}
