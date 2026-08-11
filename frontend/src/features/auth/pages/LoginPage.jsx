import { Navigate, useNavigate } from "react-router";

import LoginForm from "../components/LoginForm";
import RedMathBrand from "../components/RedMathBrand";
import { useAuth } from "../context/useAuth";
import { getPostAuthRoute } from "../../../routes/authRouting";

function LoginPage() {
    const {
        isAuthenticated,
        isInitializing,
        signIn,
        user,
    } = useAuth();
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

    async function handleLogin(values) {
        const authenticatedUser = await signIn(values);
        navigate(getPostAuthRoute(authenticatedUser), { replace: true });
    }

    return (
        <main className="grid min-h-screen place-items-center bg-brand-background px-5 py-10">
            <section className="w-full max-w-md rounded-xl border border-brand-border bg-brand-surface p-7 shadow-sm sm:p-9">
                <RedMathBrand />
                <div className="mt-8 border-t border-brand-border pt-7">
                    <h1 className="text-2xl font-bold tracking-tight text-brand-text">
                        Sign in to your account
                    </h1>
                    <div className="mt-6">
                        <LoginForm onSubmit={handleLogin} />
                    </div>
                </div>
            </section>
        </main>
    );
}

export default LoginPage;
