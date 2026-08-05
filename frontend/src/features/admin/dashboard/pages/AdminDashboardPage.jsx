import {
    Building2,
    LayoutDashboard,
    UsersRound,
} from "lucide-react";

import { Link } from "react-router";

import { ROUTES } from "../../../../routes/routePaths";

function AdminDashboardPage() {
    return (
        <main className="min-h-screen bg-brand-background">
            <div className="mx-auto w-full max-w-[1600px] px-5 py-8 lg:px-8 xl:px-10">
                <Link
                    to={ROUTES.HOME}
                    className="text-sm font-semibold text-brand-primary"
                >
                    ← Return home
                </Link>

                <div className="mt-6">
                    <p className="text-sm font-semibold text-brand-primary">
                        RM - BANK
                    </p>

                    <h1 className="mt-2 text-3xl font-bold text-brand-text">
                        Admin dashboard
                    </h1>

                    <p className="mt-2 text-brand-muted">
                        The responsive admin layout will be created
                        in its own feature branch.
                    </p>
                </div>

                <section className="mt-8 grid grid-cols-1 gap-5 lg:grid-cols-2 xl:grid-cols-3">
                    <article className="rounded-2xl border border-brand-border bg-white p-6 shadow-sm">
                        <LayoutDashboard className="text-brand-primary" />

                        <p className="mt-6 text-sm font-medium text-brand-muted">
                            Frontend status
                        </p>

                        <p className="mt-2 text-2xl font-bold text-brand-text">
                            Foundation ready
                        </p>
                    </article>

                    <article className="rounded-2xl border border-brand-border bg-white p-6 shadow-sm">
                        <UsersRound className="text-brand-warning" />

                        <p className="mt-6 text-sm font-medium text-brand-muted">
                            Pending users
                        </p>

                        <p className="mt-2 text-2xl font-bold text-brand-text">
                            Coming next
                        </p>
                    </article>

                    <article className="rounded-2xl border border-brand-border bg-white p-6 shadow-sm">
                        <Building2 className="text-brand-success" />

                        <p className="mt-6 text-sm font-medium text-brand-muted">
                            Accounts
                        </p>

                        <p className="mt-2 text-2xl font-bold text-brand-text">
                            API not connected
                        </p>
                    </article>
                </section>
            </div>
        </main>
    );
}

export default AdminDashboardPage;