import { Inbox } from "lucide-react";

function EmptyState({
    icon: Icon = Inbox,
    title,
    description,
    action,
}) {
    return (
        <div className="flex flex-col items-center px-5 py-10 text-center sm:py-12">
            <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-red-50 text-brand-primary">
                <Icon size={24} aria-hidden="true" />
            </div>

            <h3 className="mt-4 text-base font-semibold text-brand-text">
                {title}
            </h3>

            {description && (
                <p className="mt-2 max-w-md text-sm leading-6 text-brand-muted">
                    {description}
                </p>
            )}

            {action && (
                <div className="mt-5">
                    {action}
                </div>
            )}
        </div>
    );
}

export default EmptyState;
