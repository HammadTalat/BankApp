import { Navigate } from "react-router";

import { useAuth } from "../features/auth/context/useAuth";
import { getPostAuthRoute } from "../routes/authRouting";

function HomePage() {
    const { isInitializing, user } = useAuth();

    if (isInitializing) {
        return (
            <main className="grid min-h-screen place-items-center bg-brand-background text-brand-muted">
                Restoring your session…
            </main>
        );
    }

    return <Navigate to={getPostAuthRoute(user)} replace />;
}

export default HomePage;
