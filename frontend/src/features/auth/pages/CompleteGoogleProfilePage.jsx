import { useState } from "react";
import { MapPin } from "lucide-react";
import { Navigate, useNavigate } from "react-router";

import Alert from "../../../shared/components/feedback/Alert";
import RedMathBrand from "../components/RedMathBrand";
import { useAuth } from "../context/useAuth";
import { getPostAuthRoute } from "../../../routes/authRouting";
import { ROUTES } from "../../../routes/routePaths";

function CompleteGoogleProfilePage() {
    const {
        isAuthenticated,
        isInitializing,
        user,
        finishProfile,
        signOut,
    } = useAuth();
    const [address, setAddress] = useState("");
    const [error, setError] = useState("");
    const [isSubmitting, setIsSubmitting] = useState(false);
    const navigate = useNavigate();

    if (isInitializing) {
        return (
            <main className="grid min-h-screen place-items-center bg-brand-background text-brand-muted">
                Restoring your session…
            </main>
        );
    }

    if (!isAuthenticated) return <Navigate to={ROUTES.LOGIN} replace />;
    if (!user?.needsProfileCompletion) {
        return <Navigate to={getPostAuthRoute(user)} replace />;
    }

    async function handleSubmit(event) {
        event.preventDefault();
        setError("");
        setIsSubmitting(true);

        try {
            const completedUser = await finishProfile(address);
            navigate(getPostAuthRoute(completedUser), { replace: true });
        } catch (requestError) {
            setError(requestError.message || "Unable to save your profile.");
        } finally {
            setIsSubmitting(false);
        }
    }
    return (
        <main className="grid min-h-screen place-items-center bg-brand-background px-5 py-10">
            <section className="w-full max-w-md rounded-xl border border-brand-border bg-brand-surface p-7 shadow-sm sm:p-9">
                <RedMathBrand />
                <div className="mt-8 border-t border-brand-border pt-7">
                    <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-red-50 text-brand-primary">
                        <MapPin size={21} />
                    </div>
                    <h1 className="mt-5 text-2xl font-bold tracking-tight text-brand-text">
                        Complete your profile
                    </h1>
                    <p className="mt-2 text-sm text-brand-muted">
                        Welcome, {user?.name}.
                    </p>
                    <form onSubmit={handleSubmit} className="mt-6 space-y-5">
                        {error && <Alert type="error">{error}</Alert>}
                        <div>
                            <label htmlFor="google-address" className="mb-2 block text-sm font-medium text-brand-text">Home address</label>
                            <input id="google-address" value={address} onChange={(event) => setAddress(event.target.value)} placeholder="Street, city, and country" required autoComplete="street-address" className="w-full rounded-xl border border-brand-border px-3.5 py-3 text-sm outline-none transition focus:border-brand-primary focus:ring-4 focus:ring-red-100" />
                        </div>
                        <button type="submit" disabled={isSubmitting} className="w-full rounded-xl bg-brand-primary px-4 py-3 font-semibold text-white hover:bg-brand-primary-hover disabled:opacity-60">{isSubmitting ? "Saving…" : "Save and continue"}</button>
                        <button type="button" onClick={async () => { await signOut(); navigate(ROUTES.LOGIN); }} className="w-full text-sm font-semibold text-brand-muted hover:text-brand-text">Use a different account</button>
                    </form>
                </div>
            </section>
        </main>
    );
}

export default CompleteGoogleProfilePage;
