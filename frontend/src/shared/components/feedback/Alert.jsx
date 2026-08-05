import {
    CircleAlert,
    CircleCheck,
    Info,
    TriangleAlert,
} from "lucide-react";

const alertStyles = {
    success: {
        icon: CircleCheck,
        classes: "border-emerald-200 bg-emerald-50 text-brand-success",
    },
    error: {
        icon: CircleAlert,
        classes: "border-red-200 bg-red-50 text-brand-danger",
    },
    warning: {
        icon: TriangleAlert,
        classes: "border-amber-200 bg-amber-50 text-brand-warning",
    },
    info: {
        icon: Info,
        classes: "border-blue-200 bg-blue-50 text-brand-primary",
    },
};

function Alert({
    type = "info",
    title,
    children,
    className = "",
}) {
    const selectedStyle = alertStyles[type] || alertStyles.info;
    const Icon = selectedStyle.icon;

    return (
        <div
            role={type === "error" ? "alert" : "status"}
            className={`flex items-start gap-3 rounded-xl border p-4 ${selectedStyle.classes} ${className}`}
        >
            <Icon
                size={20}
                className="mt-0.5 shrink-0"
                aria-hidden="true"
            />

            <div className="min-w-0">
                {title && (
                    <p className="font-semibold">
                        {title}
                    </p>
                )}

                {children && (
                    <div className={`${title ? "mt-1" : ""} text-sm leading-6`}>
                        {children}
                    </div>
                )}
            </div>
        </div>
    );
}

export default Alert;
