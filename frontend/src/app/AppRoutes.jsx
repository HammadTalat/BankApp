import {
    Route,
    Routes,
} from "react-router";

import ApplicationStatusPage from "../features/application-status/pages/ApplicationStatusPage";
import AccountDetailsPage from "../features/admin/accounts/pages/AccountDetailsPage";
import AccountTransactionsPage from "../features/admin/accounts/pages/AccountTransactionsPage";
import AccountsPage from "../features/admin/accounts/pages/AccountsPage";
import AdminDashboardPage from "../features/admin/dashboard/pages/AdminDashboardPage";
import LoginPage from "../features/auth/pages/LoginPage";
import SignupPage from "../features/auth/pages/SignupPage";
import CompleteGoogleProfilePage from "../features/auth/pages/CompleteGoogleProfilePage";
import PendingUsersPage from "../features/admin/users/pages/PendingUsersPage";
import DashboardPage from "../features/account/dashboard/pages/DashboardPage";
import TransactionsPage from "../features/account/transactions/pages/TransactionsPage";
import TransferPage from "../features/account/transfers/pages/TransferPage";
import AdminLayout from "../layouts/AdminLayout";
import NotFoundPage from "../routes/NotFoundPage";
import ProtectedRoute from "../routes/ProtectedRoute";
import { ROUTES } from "../routes/routePaths";
import ComponentShowcasePage from "./ComponentShowcasePage";
import HomePage from "./HomePage";
import DepositPage from "../features/account/deposit/pages/DepositPage.jsx"


function AppRoutes() {
    return (
        <Routes>
            <Route
                path={ROUTES.HOME}
                element={<HomePage />}
            />

            <Route path={ROUTES.LOGIN} element={<LoginPage />} />
            <Route path={ROUTES.SIGNUP} element={<SignupPage />} />
            <Route
                element={(
                    <ProtectedRoute
                        allowedRoles={["ACCOUNT_HOLDER"]}
                        allowProfileCompletion
                    />
                )}
            >
                <Route
                    path={ROUTES.COMPLETE_GOOGLE_PROFILE}
                    element={<CompleteGoogleProfilePage />}
                />
            </Route>

            <Route element={<ProtectedRoute />}>
                <Route
                    path={ROUTES.COMPONENTS}
                    element={<ComponentShowcasePage />}
                />
            </Route>
            <Route
                element={(
                    <ProtectedRoute
                        allowedRoles={["ACCOUNT_HOLDER"]}
                        allowedApprovalStatuses={["PENDING", "REJECTED"]}
                    />
                )}
            >
                <Route
                    path={ROUTES.APPLICATION_STATUS}
                    element={<ApplicationStatusPage />}
                />
            </Route>

            <Route
                element={(
                    <ProtectedRoute
                        allowedRoles={["ACCOUNT_HOLDER"]}
                        allowedApprovalStatuses={["APPROVED"]}
                    />
                )}
            >
                <Route
                    path={ROUTES.ACCOUNT_HOME}
                    element={<DashboardPage />}
                />

                <Route
                    path={ROUTES.ACCOUNT_TRANSFERS}
                    element={<TransferPage />}
                />

                <Route
                    path={ROUTES.ACCOUNT_TRANSACTIONS}
                    element={<TransactionsPage />}
                />

                <Route
                    path={ROUTES.ACCOUNT_DEPOSIT}
                    element={<DepositPage />}
                />

            </Route>

            <Route
                element={(
                    <ProtectedRoute
                        allowedRoles={["ADMIN"]}
                    />
                )}
            >
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

                    <Route
                        path="accounts/:accountNumber"
                        element={<AccountDetailsPage />}
                    />

                    <Route
                        path="accounts/:accountNumber/transactions"
                        element={<AccountTransactionsPage />}
                    />
                </Route>
            </Route>

            <Route
                path="*"
                element={<NotFoundPage />}
            />
        </Routes>
    );
}

export default AppRoutes;
