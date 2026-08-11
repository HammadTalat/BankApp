    import { LoaderCircle } from "lucide-react";

const variantClasses = {
    primary:
        "border-transparent bg-brand-primary text-white hover:bg-brand-primary-hover",
    secondary:
        "border-brand-border bg-brand-surface text-brand-text hover:bg-slate-50",
    danger:
        "border-transparent bg-brand-danger text-white hover:bg-red-700",
    ghost:
        "border-transparent bg-transparent text-brand-text hover:bg-slate-100",
};

const sizeClasses = {
    sm: "min-h-9 px-3 py-2 text-sm",
    md: "min-h-11 px-4 py-2.5 text-sm",
    lg: "min-h-12 px-5 py-3 text-base",
};

const spinnerSizes = {
    sm: 16,
    md: 18,
    lg: 20,
};

function Button({
    children,
    type = "button",
    variant = "primary",
    size = "md",
    loading = false,
    disabled = false,
    fullWidth = false,
    className = "",
    ...buttonProps
}) {
    const selectedVariant = variantClasses[variant] || variantClasses.primary;
    const selectedSize = sizeClasses[size] || sizeClasses.md;
    const spinnerSize = spinnerSizes[size] || spinnerSizes.md;

    const buttonClasses = [
        "inline-flex items-center justify-center gap-2 rounded-xl border font-semibold shadow-sm transition-colors",
        "focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-primary focus-visible:ring-offset-2",
        "disabled:cursor-not-allowed disabled:opacity-55",
        selectedVariant,
        selectedSize,
        fullWidth ? "w-full" : "",
        className,
    ]
        .filter(Boolean)
        .join(" ");

    return (
        <button
            {...buttonProps}
            type={type}
            disabled={disabled || loading}
            aria-busy={loading || undefined}
            className={buttonClasses}
        >
            {loading && (
                <LoaderCircle
                    size={spinnerSize}
                    className="shrink-0 animate-spin"
                    aria-hidden="true"
                />
            )}
            {children}
        </button>
    );
}

export default Button;
