import { LoaderCircle } from "lucide-react";

const spinnerSizes = {
    sm: {
        icon: 18,
        text: "text-sm",
    },
    md: {
        icon: 26,
        text: "text-sm",
    },
    lg: {
        icon: 36,
        text: "text-base",
    },
};

function LoadingSpinner({
    size = "md",
    message = "Loading...",
}) {
    const selectedSize = spinnerSizes[size] || spinnerSizes.md;

    return (
        <div
            role="status"
            aria-live="polite"
            aria-label={message || "Loading"}
            className={`flex items-center justify-center gap-3 text-brand-muted ${selectedSize.text}`}
        >
            <LoaderCircle
                size={selectedSize.icon}
                className="shrink-0 animate-spin text-brand-primary"
                aria-hidden="true"
            />
            {message && <span>{message}</span>}
        </div>
    );
}

export default LoadingSpinner;
