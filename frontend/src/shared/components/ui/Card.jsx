function Card({
    children,
    padding = true,
    className = "",
    ...cardProps
}) {
    const cardClasses = [
        "rounded-2xl border border-brand-border bg-brand-surface shadow-sm",
        padding ? "p-5 sm:p-6" : "",
        className,
    ]
        .filter(Boolean)
        .join(" ");

    return (
        <div
            {...cardProps}
            className={cardClasses}
        >
            {children}
        </div>
    );
}

export default Card;
