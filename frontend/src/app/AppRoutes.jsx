import {
    Route,
    Routes,
} from "react-router";

import ApplicationStatusPage from "../features/application-status/pages/ApplicationStatusPage";
import AccountsPage from "../features/admin/accounts/pages/AccountsPage";
import AdminDashboardPage from "../features/admin/dashboard/pages/AdminDashboardPage";
import PendingUsersPage from "../features/admin/users/pages/PendingUsersPage";
import AdminLayout from "../layouts/AdminLayout";
import NotFoundPage from "../routes/NotFoundPage";
import { ROUTES } from "../routes/routePaths";
import ComponentShowcasePage from "./ComponentShowcasePage";
import HomePage from "./HomePage";

function AppRoutes() {
    return (
        <Routes>
            <Route
                path={ROUTES.HOME}
                element={<HomePage />}
            />

            <Route
                path={ROUTES.COMPONENTS}
                element={<ComponentShowcasePage />}
            />

            <Route
                path={ROUTES.APPLICATION_STATUS}
                element={<ApplicationStatusPage />}
            />

            <Route
                path={ROUTES.ADMIN_HOME}
                element={<AdminLayout />}
            >
                <Route
                    index
                    element={<AdminDashboardPage />}
                />

                <Route
                    path="pending-users"
                    element={<PendingUsersPage />}
                />

                <Route
                    path="accounts"
                    element={<AccountsPage />}
                />
            </Route>

            <Route
                path="*"
                element={<NotFoundPage />}
            />
        </Routes>
    );
}

export default AppRoutes;
