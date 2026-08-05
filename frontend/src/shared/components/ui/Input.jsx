import { useId } from "react";

function Input({
    id,
    name,
    label,
    type = "text",
    error,
    helperText,
    disabled = false,
    className = "",
    containerClassName = "",
    ...inputProps
}) {
    const generatedId = useId();
    const inputId = id || name || generatedId;
    const errorId = `${inputId}-error`;
    const helperId = `${inputId}-helper`;
    const descriptionId = error
        ? errorId
        : helperText
            ? helperId
            : undefined;

    const inputClasses = [
        "w-full rounded-xl border bg-brand-surface px-3.5 py-2.5 text-sm text-brand-text shadow-sm outline-none transition",
        "placeholder:text-slate-400 disabled:cursor-not-allowed disabled:bg-slate-100 disabled:text-brand-muted",
        error
            ? "border-brand-danger focus:border-brand-danger focus:ring-4 focus:ring-red-100"
            : "border-brand-border focus:border-brand-primary focus:ring-4 focus:ring-blue-100",
        className,
    ]
        .filter(Boolean)
        .join(" ");

    return (
        <div className={`min-w-0 ${containerClassName}`}>
            {label && (
                <label
                    htmlFor={inputId}
                    className="mb-2 block text-sm font-medium text-brand-text"
                >
                    {label}
                </label>
            )}

            <input
                {...inputProps}
                id={inputId}
                name={name}
                type={type}
                disabled={disabled}
                aria-invalid={error ? "true" : undefined}
                aria-describedby={descriptionId}
                className={inputClasses}
            />

            {error ? (
                <p
                    id={errorId}
                    className="mt-1.5 text-sm text-brand-danger"
                >
                    {error}
                </p>
            ) : helperText ? (
                <p
                    id={helperId}
                    className="mt-1.5 text-sm text-brand-muted"
                >
                    {helperText}
                </p>
            ) : null}
        </div>
    );
}

export default Input;
