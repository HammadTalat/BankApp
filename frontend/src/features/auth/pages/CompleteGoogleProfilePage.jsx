import { useState } from "react";
import { Landmark, MapPin } from "lucide-react";
import { Navigate, useNavigate } from "react-router";

import Alert from "../../../shared/components/feedback/Alert";
import { useAuth } from "../context/useAuth";
import { ROUTES } from "../../../routes/routePaths";

function CompleteGoogleProfilePage() {
    const { isAuthenticated, user, finishProfile, signOut } = useAuth();
    const [address, setAddress] = useState("");
    const [error, setError] = useState("");
    const [isSubmitting, setIsSubmitting] = useState(false);
    const navigate = useNavigate();
    if (!isAuthenticated) return <Navigate to={ROUTES.LOGIN} replace />;
    if (!user?.needsProfileCompletion) return <Navigate to={ROUTES.HOME} replace />;
    async function handleSubmit(event) { event.preventDefault(); setError(""); setIsSubmitting(true); try { await finishProfile(address); navigate(ROUTES.APPLICATION_STATUS, { replace: true }); } catch (requestError) { setError(requestError.message || "Unable to save your profile."); } finally { setIsSubmitting(false); } }
    return <main className="grid min-h-screen bg-brand-background lg:grid-cols-2"><section className="hidden bg-brand-navy p-12 text-white lg:flex lg:flex-col lg:justify-between"><div className="flex items-center gap-3 text-xl font-bold"><Landmark size={30} /> RedMath Bank</div><div><p className="text-sm font-semibold uppercase tracking-[0.22em] text-blue-300">Banking made personal</p><h1 className="mt-4 max-w-lg text-5xl font-bold leading-tight">Your money deserves a clearer future.</h1><p className="mt-6 max-w-md text-lg leading-8 text-slate-300">Secure, straightforward banking for every step ahead.</p></div><p className="text-sm text-slate-400">© {new Date().getFullYear()} RedMath Bank</p></section><section className="flex items-center justify-center px-5 py-10"><div className="w-full max-w-md rounded-3xl border border-brand-border bg-white p-7 shadow-xl shadow-slate-200/70 sm:p-10"><div className="mb-8 lg:hidden"><div className="flex items-center gap-2 font-bold text-brand-navy"><Landmark size={24} /> RedMath Bank</div></div><div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-blue-50 text-brand-primary"><MapPin size={24} /></div><p className="mt-5 text-sm font-semibold text-brand-primary">ONE MORE STEP</p><h1 className="mt-2 text-3xl font-bold tracking-tight text-brand-text">Complete your profile</h1><p className="mt-3 leading-7 text-brand-muted">Welcome, {user?.name}. Please add your home address so we can submit your application for review.</p><form onSubmit={handleSubmit} className="mt-7 space-y-5">{error && <Alert type="error">{error}</Alert>}<div><label htmlFor="google-address" className="mb-2 block text-sm font-medium text-brand-text">Home address</label><input id="google-address" value={address} onChange={(event) => setAddress(event.target.value)} placeholder="Street, city, and country" required autoComplete="street-address" className="w-full rounded-xl border border-brand-border px-3.5 py-3 text-sm outline-none transition focus:border-brand-primary focus:ring-4 focus:ring-blue-100" /></div><button type="submit" disabled={isSubmitting} className="w-full rounded-xl bg-brand-primary px-4 py-3 font-semibold text-white hover:bg-blue-700 disabled:opacity-60">{isSubmitting ? "Saving…" : "Save and continue"}</button><button type="button" onClick={async () => { await signOut(); navigate(ROUTES.LOGIN); }} className="w-full text-sm font-semibold text-brand-muted hover:text-brand-text">Use a different account</button></form></div></section></main>;
}

export default CompleteGoogleProfilePage;
