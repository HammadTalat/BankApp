import { ArrowUpRight } from "lucide-react";
import { Link } from "react-router";

import Card from "../../../../shared/components/ui/Card";

function DashboardMetricCard({
    label,
    value,
    description,
    icon: Icon,
    iconClasses = "bg-blue-50 text-brand-primary",
    to,
}) {
    const card = (
        <Card
            className={`flex h-full min-h-56 flex-col ${
                to
                    ? "transition duration-150 group-hover:-translate-y-0.5 group-hover:shadow-md"
                    : ""
            }`}
        >
            <div className="flex items-start justify-between gap-4">
                <div
                    className={`flex h-12 w-12 shrink-0 items-center justify-center rounded-xl ${iconClasses}`}
                >
                    {Icon && (
                        <Icon
                            size={23}
                            aria-hidden="true"
                        />
                    )}
                </div>

                {to && (
                    <ArrowUpRight
                        size={18}
                        className="shrink-0 text-brand-muted transition-colors group-hover:text-brand-primary"
                        aria-hidden="true"
                    />
                )}
            </div>

            <p className="mt-5 text-sm font-semibold text-brand-text">
                {label}
            </p>

            <p className="mt-1.5 text-3xl font-bold tabular-nums tracking-tight text-brand-navy">
                {value}
            </p>

            <p className="mt-auto pt-4 text-sm leading-6 text-brand-muted">
                {description}
            </p>
        </Card>
    );

    if (!to) {
        return card;
    }

    return (
        <Link
            to={to}
            className="group block h-full rounded-2xl focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-primary focus-visible:ring-offset-2"
        >
            {card}
        </Link>
    );
}

export default DashboardMetricCard;
