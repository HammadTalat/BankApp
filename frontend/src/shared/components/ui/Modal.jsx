import { useEffect, useId } from "react";
import { X } from "lucide-react";

const sizeClasses = {
    sm: "max-w-md",
    md: "max-w-xl",
    lg: "max-w-3xl",
};

function Modal({
    isOpen,
    onClose,
    title,
    children,
    footer,
    size = "md",
    closeOnBackdrop = true,
}) {
    const titleId = useId();

    useEffect(() => {
        if (!isOpen) {
            return undefined;
        }

        const previousOverflow = document.body.style.overflow;

        function handleKeyDown(event) {
            if (event.key === "Escape") {
                onClose?.();
            }
        }

        document.body.style.overflow = "hidden";
        document.addEventListener("keydown", handleKeyDown);

        return () => {
            document.body.style.overflow = previousOverflow;
            document.removeEventListener("keydown", handleKeyDown);
        };
    }, [isOpen, onClose]);

    if (!isOpen) {
        return null;
    }

    function handleBackdropClick(event) {
        if (closeOnBackdrop && event.target === event.currentTarget) {
            onClose?.();
        }
    }

    const selectedSize = sizeClasses[size] || sizeClasses.md;

    return (
        <div
            className="fixed inset-0 z-50 flex items-center justify-center overflow-y-auto bg-brand-navy/55 p-4 sm:p-6"
            onMouseDown={handleBackdropClick}
        >
            <div
                role="dialog"
                aria-modal="true"
                aria-labelledby={titleId}
                className={`flex max-h-[calc(100vh-2rem)] w-full flex-col overflow-hidden rounded-xl border border-brand-border bg-brand-surface shadow-lg ${selectedSize}`}
            >
                <header className="flex shrink-0 items-start justify-between gap-4 border-b border-brand-border px-5 py-4 sm:px-6">
                    <h2
                        id={titleId}
                        className="text-lg font-semibold text-brand-text"
                    >
                        {title}
                    </h2>

                    <button
                        type="button"
                        onClick={onClose}
                        className="inline-flex h-9 w-9 shrink-0 items-center justify-center rounded-lg text-brand-muted transition hover:bg-slate-100 hover:text-brand-text focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-primary"
                        aria-label="Close modal"
                    >
                        <X size={20} aria-hidden="true" />
                    </button>
                </header>

                <div className="min-h-0 flex-1 overflow-y-auto px-5 py-5 sm:px-6">
                    {children}
                </div>

                {footer && (
                    <footer className="flex shrink-0 flex-col-reverse gap-3 border-t border-brand-border px-5 py-4 sm:flex-row sm:justify-end sm:px-6">
                        {footer}
                    </footer>
                )}
            </div>
        </div>
    );
}

export default Modal;
