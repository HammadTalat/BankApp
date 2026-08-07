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
import TransactionsPage from "../features/transactions/pages/TransactionsPage.jsx";
import TransferPage from "../features/transfers/pages/TransferPage.jsx";
import ProfilePage from "../features/proifle/pages/ProfilePage.jsx";
import DashboardPage from "../features/dashboard/pages/DashboardPage.jsx";
import DepositPage from "../features/deposit/pages/DepositPage.jsx";

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

            {/*Transaction*/}
            <Route path={ROUTES.ACCOUNT_TRANSACTIONS} element={<TransactionsPage />} />
            <Route path={ROUTES.ACCOUNT_TRANSFERS} element={<TransferPage />} />

            {/*Profile */}
            <Route path={ROUTES.ACCOUNT_PROFILE} element={<ProfilePage />} />

            {/*Dashboard*/}
            <Route path={ROUTES.ACCOUNT_HOME} element={<DashboardPage />} />

            {/*Deposit*/}
            <Route path={ROUTES.ACCOUNT_DEPOSIT} element={<DepositPage />} />

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
