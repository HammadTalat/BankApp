import {
    Route,
    Routes,
} from "react-router";

import ApplicationStatusPage from "../features/application-status/pages/ApplicationStatusPage";
import AdminDashboardPage from "../features/admin/dashboard/pages/AdminDashboardPage";
import LoginPage from "../features/auth/pages/LoginPage";
import SignupPage from "../features/auth/pages/SignupPage";
import CompleteGoogleProfilePage from "../features/auth/pages/CompleteGoogleProfilePage";
import NotFoundPage from "../routes/NotFoundPage";
import ProtectedRoute from "../routes/ProtectedRoute";
import { ROUTES } from "../routes/routePaths";
import ComponentShowcasePage from "./ComponentShowcasePage";
import HomePage from "./HomePage";
import AuthFlowTestPage from "./temp/AuthFlowTestPage";

function AppRoutes() {
    return (
        <Routes>
            <Route
                path={ROUTES.HOME}
                element={<HomePage />}
            />

            <Route path={ROUTES.LOGIN} element={<LoginPage />} />
            <Route path={ROUTES.SIGNUP} element={<SignupPage />} />
            <Route path={ROUTES.COMPLETE_GOOGLE_PROFILE} element={<CompleteGoogleProfilePage />} />

            <Route
                path={ROUTES.COMPONENTS}
                element={<ComponentShowcasePage />}
            />

            <Route
                path={ROUTES.APPLICATION_STATUS}
                element={<ApplicationStatusPage />}
            />

            <Route element={<ProtectedRoute allowedRoles={["ADMIN"]} />}>
                <Route path={ROUTES.ADMIN_HOME} element={<AdminDashboardPage />} />
            </Route>

            <Route element={<ProtectedRoute />}>
                <Route path={ROUTES.AUTH_TEST} element={<AuthFlowTestPage />} />
            </Route>

            <Route
                path="*"
                element={<NotFoundPage />}
            />
        </Routes>
    );
}

export default AppRoutes;
