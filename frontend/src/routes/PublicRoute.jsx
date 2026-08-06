import { Navigate, Outlet } from "react-router";

import { useAuth } from "../features/auth/context/useAuth";
import { ROUTES } from "./routePaths";

/**
 * PublicRoute — wraps routes that should only be accessible to
 * unauthenticated visitors (login, signup, complete-profile, etc.).
 *
 * • While the session is still being restored we show a neutral
 *   loading screen so we never flash the form to an already-logged-in user.
 * • Once initialisation is complete, authenticated users are redirected
 *   to the appropriate destination based on their state:
 *     – needs profile completion  → /complete-profile
 *     – fully authenticated        → /application-status (or HOME for admins)
 */
function PublicRoute() {
    const { isAuthenticated, isInitializing, user } = useAuth();

    if (isInitializing) {
        return (
            <main className="grid min-h-screen place-items-center bg-brand-background text-brand-muted">
                Restoring your session…
            </main>
        );
    }

    if (isAuthenticated) {
        if (user?.needsProfileCompletion) {
            return <Navigate to={ROUTES.COMPLETE_GOOGLE_PROFILE} replace />;
        }
        return <Navigate to={ROUTES.APPLICATION_STATUS} replace />;
    }

    return <Outlet />;
}

export default PublicRoute;
