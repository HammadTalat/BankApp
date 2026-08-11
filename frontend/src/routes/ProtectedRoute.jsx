import { Navigate, Outlet, useLocation } from "react-router";

import { useAuth } from "../features/auth/context/useAuth";
import {
    getPostAuthRoute,
    normalizeApprovalStatus,
    normalizeRole,
} from "./authRouting";
import { ROUTES } from "./routePaths";

function includesNormalized(values, currentValue, normalize) {
    return !values?.length
        || values.some((value) => normalize(value) === normalize(currentValue));
}

function ProtectedRoute({
    allowedRoles,
    allowedApprovalStatuses,
    allowProfileCompletion = false,
}) {
    const { isAuthenticated, isInitializing, user } = useAuth();
    const location = useLocation();

    if (isInitializing) {
        return (
            <main className="grid min-h-screen place-items-center bg-brand-background text-brand-muted">
                Restoring your session…
            </main>
        );
    }

    if (!isAuthenticated) {
        return <Navigate to={ROUTES.LOGIN} replace state={{ from: location }} />;
    }

    const fallbackRoute = getPostAuthRoute(user);

    if (user?.needsProfileCompletion && !allowProfileCompletion) {
        return <Navigate to={ROUTES.COMPLETE_GOOGLE_PROFILE} replace />;
    }

    if (!includesNormalized(allowedRoles, user?.role, normalizeRole)) {
        return <Navigate to={fallbackRoute} replace />;
    }

    if (!includesNormalized(
        allowedApprovalStatuses,
        user?.approvalStatus,
        normalizeApprovalStatus,
    )) {
        return <Navigate to={fallbackRoute} replace />;
    }

    return <Outlet />;
}

export default ProtectedRoute;
