    import {
        Ban,
        CircleCheckBig,
        Clock3,
        Landmark,
        UserRoundCheck,
        UserRoundX,
    } from "lucide-react";

    import { ROUTES } from "../../../../routes/routePaths";
    import PageHeader from "../../../../shared/components/ui/PageHeader";
    import DashboardMetricCard from "../components/DashboardMetricCard";
    import {getDashboardAnalytics} from "../api/getDashboardAnalyticsApi.js";
    import {useState, useEffect} from "react";



    function AdminDashboardPage() {

        const [dashboard,setdashboard] = useState(null);
        const [loading, setLoading] = useState(true);

        useEffect(() => {
            async function loadDashboardOnMount() {
                const response = await getDashboardAnalytics();
                setdashboard(response);
                setLoading(false);

            }
            loadDashboardOnMount();
            console.log("hjereee");
        }, []);

        if (loading) {
            return (
                <p className="p-6">
                    Loading dashboard...
                </p>
            );
        }
        const dashboardMetrics = [
            {
                label: "Pending Users",
                value: dashboard.pendingUsers,
                description: "Applications waiting for administrator review.",
                icon: Clock3,
                iconClasses: "bg-amber-50 text-brand-warning",
                to: ROUTES.ADMIN_PENDING_USERS,
            },
            {
                label: "Approved Users",
                value: dashboard.approvedUsers,
                description: "Users whose applications have been approved.",
                icon: UserRoundCheck,
                iconClasses: "bg-emerald-50 text-brand-success",
                to: ROUTES.ADMIN_ACCOUNTS,
            },
            {
                label: "Rejected Users",
                value: dashboard.rejectedUsers,
                description: "Applications that were not approved.",
                icon: UserRoundX,
                iconClasses: "bg-red-50 text-brand-danger",
            },
            {
                label: "Total Accounts",
                value: dashboard.totalAccounts,
                description: "All bank accounts created in the system.",
                icon: Landmark,
                iconClasses: "bg-blue-50 text-brand-primary",
                to: ROUTES.ADMIN_ACCOUNTS,
            },
            {
                label: "Active Accounts",
                value: dashboard.activeAccounts,
                description: "Accounts that can currently perform transactions.",
                icon: CircleCheckBig,
                iconClasses: "bg-emerald-50 text-brand-success",
                to: ROUTES.ADMIN_ACCOUNTS,
            },
            {
                label: "Closed Accounts",
                value: dashboard.closedAccounts,
                description: "Accounts that are no longer active.",
                icon: Ban,
                iconClasses: "bg-slate-100 text-slate-600",
                to: ROUTES.ADMIN_ACCOUNTS,
            },
        ];


        return (
            <section className="space-y-6">
                <PageHeader
                    eyebrow="NexaBank Admin"
                    title="Dashboard"
                    description="View user applications and bank-account information from one place."
                />

                <div className="grid auto-rows-fr grid-cols-1 gap-5 md:grid-cols-2 xl:grid-cols-3">
                    {dashboardMetrics.map((metric) => (
                        <DashboardMetricCard
                            key={metric.label}
                            label={metric.label}
                            value={metric.value}
                            description={metric.description}
                            icon={metric.icon}
                            iconClasses={metric.iconClasses}
                            to={metric.to}
                        />
                    ))}
                </div>
            </section>
        );
    }

    export default AdminDashboardPage;
