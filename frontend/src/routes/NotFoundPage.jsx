import { Link } from "react-router";

import { ROUTES } from "./routePaths";

function NotFoundPage() {
    return (
        <main className="flex min-h-screen items-center justify-center bg-brand-background px-5">
            <section className="w-full max-w-lg rounded-2xl border border-brand-border bg-white p-10 text-center shadow-sm">
                <p className="text-sm font-semibold text-brand-primary">
                    ERROR 404
                </p>

                <h1 className="mt-3 text-3xl font-bold text-brand-text">
                    Page not found
                </h1>

                <p className="mt-3 text-brand-muted">
                    The page you requested does not exist.
                </p>

                <Link
                    to={ROUTES.HOME}
                    className="mt-7 inline-block rounded-xl bg-brand-primary px-5 py-3 font-semibold text-white hover:bg-brand-primary-hover"
                >
                    Return home
                </Link>
            </section>
        </main>
    );
}

export default NotFoundPage;
