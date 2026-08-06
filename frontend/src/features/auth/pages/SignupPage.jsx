import { Landmark } from "lucide-react";
import { Navigate, useNavigate } from "react-router";

import SignupForm from "../components/SignupForm";
import { useAuth } from "../context/useAuth";
import { getPostAuthRoute } from "../../../routes/authRouting";
import { ROUTES } from "../../../routes/routePaths";

function SignupPage() {
    const { isAuthenticated, isInitializing, register, user } = useAuth();
    const navigate = useNavigate();

    if (isInitializing) {
        return (
            <main className="grid min-h-screen place-items-center bg-brand-background text-brand-muted">
                Restoring your session…
            </main>
        );
    }

    if (isAuthenticated) {
        return <Navigate to={getPostAuthRoute(user)} replace />;
    }

    async function handleSignup(values) {
        await register(values);
        navigate(ROUTES.LOGIN, { replace: true });
    }

    return <main className="grid min-h-screen bg-brand-background lg:grid-cols-2"><section className="hidden bg-brand-navy p-12 text-white lg:flex lg:flex-col lg:justify-between"><div className="flex items-center gap-3 text-xl font-bold"><Landmark size={30} /> RedMath Bank</div><div><p className="text-sm font-semibold uppercase tracking-[0.22em] text-blue-300">Banking made personal</p><h1 className="mt-4 max-w-lg text-5xl font-bold leading-tight">Your money deserves a clearer future.</h1><p className="mt-6 max-w-md text-lg leading-8 text-slate-300">Secure, straightforward banking for every step ahead.</p></div><p className="text-sm text-slate-400">© {new Date().getFullYear()} RedMath Bank</p></section><section className="flex items-center justify-center px-5 py-10"><div className="w-full max-w-md rounded-3xl border border-brand-border bg-white p-7 shadow-xl shadow-slate-200/70 sm:p-10"><div className="mb-8 lg:hidden"><div className="flex items-center gap-2 font-bold text-brand-navy"><Landmark size={24} /> RedMath Bank</div></div><p className="text-sm font-semibold text-brand-primary">OPEN AN ACCOUNT</p><h1 className="mt-2 text-3xl font-bold tracking-tight text-brand-text">Banking starts here</h1><p className="mt-3 text-sm leading-6 text-brand-muted">Create your account. An administrator will review your application before banking is available.</p><div className="mt-7"><SignupForm onSubmit={handleSignup} /></div></div></section></main>;
}

export default SignupPage;
