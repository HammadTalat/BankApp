import { useId } from "react";

function Select({
    id,
    name,
    label,
    options = [],
    placeholder,
    error,
    disabled = false,
    className = "",
    containerClassName = "",
    ...selectProps
}) {
    const generatedId = useId();
    const selectId = id || name || generatedId;
    const errorId = `${selectId}-error`;

    const selectClasses = [
        "w-full rounded-xl border bg-brand-surface px-3.5 py-2.5 text-sm text-brand-text shadow-sm outline-none transition",
        "disabled:cursor-not-allowed disabled:bg-slate-100 disabled:text-brand-muted",
        error
            ? "border-brand-danger focus:border-brand-danger focus:ring-4 focus:ring-red-100"
            : "border-brand-border focus:border-brand-primary focus:ring-4 focus:ring-red-100",
        className,
    ]
        .filter(Boolean)
        .join(" ");

    return (
        <div className={`min-w-0 ${containerClassName}`}>
            {label && (
                <label
                    htmlFor={selectId}
                    className="mb-2 block text-sm font-medium text-brand-text"
                >
                    {label}
                </label>
            )}

            <select
                {...selectProps}
                id={selectId}
                name={name}
                disabled={disabled}
                aria-invalid={error ? "true" : undefined}
                aria-describedby={error ? errorId : undefined}
                className={selectClasses}
            >
                {placeholder && (
                    <option value="">
                        {placeholder}
                    </option>
                )}

                {options.map((option) => (
                    <option
                        key={option.value}
                        value={option.value}
                    >
                        {option.label}
                    </option>
                ))}
            </select>

            {error && (
                <p
                    id={errorId}
                    className="mt-1.5 text-sm text-brand-danger"
                >
                    {error}
                </p>
            )}
        </div>
    );
}

export default Select;
