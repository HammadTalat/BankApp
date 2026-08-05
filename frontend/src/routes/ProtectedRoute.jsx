import { Navigate, Outlet, useLocation } from "react-router";

import { useAuth } from "../features/auth/context/useAuth";
import { ROUTES } from "./routePaths";

function ProtectedRoute({ allowedRoles }) {
    const { isAuthenticated, isInitializing, user } = useAuth();
    const location = useLocation();
    if (isInitializing) return <main className="grid min-h-screen place-items-center bg-brand-background text-brand-muted">Restoring your session…</main>;
    if (!isAuthenticated) return <Navigate to={ROUTES.LOGIN} replace state={{ from: location }} />;
    if (user?.needsProfileCompletion) return <Navigate to={ROUTES.COMPLETE_GOOGLE_PROFILE} replace />;
    if (allowedRoles?.length && !allowedRoles.includes(user?.role)) return <Navigate to={ROUTES.HOME} replace />;
    return <Outlet />;
}

export default ProtectedRoute;
