import { Navigate, useNavigate } from "react-router";

import SignupForm from "../components/SignupForm";
import RedMathBrand from "../components/RedMathBrand";
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

    return (
        <main className="grid min-h-screen place-items-center bg-brand-background px-5 py-10">
            <section className="w-full max-w-md rounded-xl border border-brand-border bg-brand-surface p-7 shadow-sm sm:p-9">
                <RedMathBrand />
                <div className="mt-8 border-t border-brand-border pt-7">
                    <h1 className="text-2xl font-bold tracking-tight text-brand-text">
                        Create an account
                    </h1>
                    <div className="mt-6">
                        <SignupForm onSubmit={handleSignup} />
                    </div>
                </div>
            </section>
        </main>
    );
}

export default SignupPage;
