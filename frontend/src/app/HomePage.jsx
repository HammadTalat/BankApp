import {
    Clock3,
    LogOut,
    RefreshCw,
} from "lucide-react";

import { Link } from "react-router";

import { ROUTES } from "../routes/routePaths";

function HomePage() {
    return (
        <main className="flex min-h-screen items-center justify-center bg-brand-background px-5 py-10">
            <section className="w-full max-w-xl rounded-3xl border border-brand-border bg-white p-8 text-center shadow-sm sm:p-12">
                <div className="mx-auto flex h-16 w-16 items-center justify-center rounded-2xl bg-amber-50 text-brand-warning">
                    <Clock3 size={31} />
                </div>

                <p className="mt-7 text-sm font-semibold text-brand-warning">
                    APPLICATION PENDING
                </p>

                <h1 className="mt-3 text-3xl font-bold text-brand-text">
                    Your application is under review
                </h1>

                <p className="mx-auto mt-4 max-w-md leading-7 text-brand-muted">
                    Your registration was completed successfully.
                    An administrator must approve your account before
                    banking features become available.
                </p>

                <div className="mt-8 flex flex-col gap-3 sm:flex-row sm:justify-center">
                    <button
                        type="button"
                        className="inline-flex items-center justify-center gap-2 rounded-xl bg-brand-primary px-5 py-3 font-semibold text-white transition hover:bg-blue-700"
                    >
                        <RefreshCw size={18} />
                        Refresh status
                    </button>

                    <Link
                        to={ROUTES.HOME}
                        className="inline-flex items-center justify-center gap-2 rounded-xl border border-brand-border px-5 py-3 font-semibold text-brand-text transition hover:bg-slate-50"
                    >
                        <LogOut size={18} />
                        Return home
                    </Link>
                </div>

                <p className="mt-7 text-sm text-brand-muted">
                    API connection will be added in the application-status branch.
                </p>
            </section>
        </main>
    );
}

export default HomePage;
