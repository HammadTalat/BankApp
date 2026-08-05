function PageHeader({
    eyebrow,
    title,
    description,
    action,
}) {
    return (
        <header className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
            <div className="min-w-0">
                {eyebrow && (
                    <p className="text-xs font-semibold uppercase tracking-[0.16em] text-brand-primary">
                        {eyebrow}
                    </p>
                )}

                <h1 className="mt-1.5 text-2xl font-bold tracking-tight text-brand-text sm:text-[32px] sm:leading-tight">
                    {title}
                </h1>

                {description && (
                    <p className="mt-2 max-w-3xl text-sm leading-6 text-brand-muted sm:text-base">
                        {description}
                    </p>
                )}
            </div>

            {action && (
                <div className="flex shrink-0 flex-wrap items-center gap-3 lg:justify-end">
                    {action}
                </div>
            )}
        </header>
    );
}

export default PageHeader;
