import { useState } from "react";
import { LogOut, RefreshCw, ShieldCheck } from "lucide-react";
import { useNavigate } from "react-router";

import Alert from "../../shared/components/feedback/Alert";
import { useAuth } from "../../features/auth/context/useAuth";
import { ROUTES } from "../../routes/routePaths";

function AuthFlowTestPage() {
    const { user, refreshProfile, signOut } = useAuth();
    const navigate = useNavigate();
    const [isRefreshing, setIsRefreshing] = useState(false);
    const [error, setError] = useState("");

    async function handleRefresh() {
        setError("");
        setIsRefreshing(true);

        try {
            await refreshProfile();
        } catch (requestError) {
            setError(requestError.message || "Unable to refresh your profile.");
        } finally {
            setIsRefreshing(false);
        }
    }

    async function handleLogout() {
        await signOut();
        navigate(ROUTES.LOGIN, { replace: true });
    }

    return (
        <main className="flex min-h-screen items-center justify-center bg-brand-background px-5 py-10">
            <section className="w-full max-w-xl rounded-3xl border border-brand-border bg-white p-8 shadow-xl shadow-slate-200/70 sm:p-10">
                <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-emerald-50 text-brand-success">
                    <ShieldCheck size={29} />
                </div>

                <p className="mt-6 text-sm font-semibold text-brand-success">
                    AUTH FLOW TEST PAGE
                </p>
                <h1 className="mt-2 text-3xl font-bold text-brand-text">
                    Signed in successfully
                </h1>
                <p className="mt-3 leading-7 text-brand-muted">
                    This temporary page confirms that the HTTP-only cookie authenticated your request to <code>/api/v1/me</code>.
                </p>

                {error && <Alert type="error" className="mt-6">{error}</Alert>}

                <dl className="mt-7 divide-y divide-brand-border overflow-hidden rounded-2xl border border-brand-border">
                    {[
                        ["Name", user?.name || "—"],
                        ["Email", user?.email || "—"],
                        ["Role", user?.role || "—"],
                        ["Approval status", user?.approvalStatus || "—"],
                    ].map(([label, value]) => (
                        <div key={label} className="flex items-center justify-between gap-4 px-4 py-3.5">
                            <dt className="text-sm text-brand-muted">{label}</dt>
                            <dd className="text-right text-sm font-semibold text-brand-text">{value}</dd>
                        </div>
                    ))}
                </dl>

                <div className="mt-7 flex flex-col gap-3 sm:flex-row">
                    <button type="button" onClick={handleRefresh} disabled={isRefreshing} className="inline-flex flex-1 items-center justify-center gap-2 rounded-xl border border-brand-border px-4 py-3 font-semibold text-brand-text hover:bg-slate-50 disabled:opacity-60">
                        <RefreshCw size={18} className={isRefreshing ? "animate-spin" : ""} />
                        {isRefreshing ? "Refreshing…" : "Refresh profile"}
                    </button>
                    <button type="button" onClick={handleLogout} className="inline-flex flex-1 items-center justify-center gap-2 rounded-xl bg-brand-danger px-4 py-3 font-semibold text-white hover:bg-red-700">
                        <LogOut size={18} />
                        Log out
                    </button>
                </div>
            </section>
        </main>
    );
}

export default AuthFlowTestPage;
