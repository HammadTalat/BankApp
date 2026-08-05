function TableContainer({
    children,
    className = "",
    ...containerProps
}) {
    const containerClasses = [
        "w-full min-w-0 overflow-x-auto rounded-2xl border border-brand-border bg-brand-surface shadow-sm",
        className,
    ]
        .filter(Boolean)
        .join(" ");

    return (
        <div
            {...containerProps}
            className={containerClasses}
        >
            {children}
        </div>
    );
}

export default TableContainer;
