// src/app/App.jsx
import { Routes, Route } from "react-router";
import { ROUTES } from "../routes/routePaths.js";
import { DashboardPage } from "../features/dashboard/pages/DashboardPage.jsx";
import ComponentShowcasePage from "./ComponentShowcasePage.jsx";
import TransferPage from "../features/transfers/pages/TransferPage.jsx";
import TransactionsPage from "../features/transactions/pages/TransactionsPage.jsx";
import ProfilePage from "../features/proifle/pages/ProfilePage.jsx"; // Adjust path as needed

export default function App() {
  return (
      <Routes>
        <Route path={ROUTES.ACCOUNT_HOME} element={<DashboardPage />} />
          <Route path={ROUTES.COMPONENTS} element={<ComponentShowcasePage/>} />
          <Route path={ROUTES.ACCOUNT_TRANSFERS} element={<TransferPage/>} />
          <Route path={ROUTES.ACCOUNT_TRANSACTIONS} element={<TransactionsPage/>} />
          <Route path={ROUTES.ACCOUNT_PROFILE} element={<ProfilePage/>} />
        {/* Add remaining routes here */}
      </Routes>
  );
}