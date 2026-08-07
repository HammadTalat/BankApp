import { useState } from "react";
import { Outlet, useNavigate } from "react-router";

import { useAuth } from "../features/auth/context/useAuth";
import AdminHeader from "../shared/components/navigation/AdminHeader";
import AdminSidebar from "../shared/components/navigation/AdminSidebar";
import { adminProfileMock } from "../features/admin/profile/mocks/adminProfileMock";
import { ROUTES } from "../routes/routePaths";

function AdminLayout() {
    const [sidebarOpen, setSidebarOpen] = useState(false);
    const [loggingOut, setLoggingOut] = useState(false);
    const { signOut, user } = useAuth();
    const navigate = useNavigate();

    function openSidebar() {
        setSidebarOpen(true);
    }

    function closeSidebar() {
        setSidebarOpen(false);
    }

    async function handleLogout() {
        if (loggingOut) return;

        setLoggingOut(true);

        try {
            await signOut();
        } finally {
            navigate(ROUTES.LOGIN, { replace: true });
            setLoggingOut(false);
        }
    }

    return (
        <div className="min-h-screen overflow-x-hidden bg-brand-background">
            <AdminSidebar
                isOpen={sidebarOpen}
                onClose={closeSidebar}
            />

            {sidebarOpen && (
                <button
                    type="button"
                    aria-label="Close admin navigation overlay"
                    className="fixed inset-0 z-40 cursor-default bg-slate-950/50 lg:hidden"
                    onClick={closeSidebar}
                />
            )}

            <div className="min-h-screen min-w-0 lg:pl-64">
                <AdminHeader
                    onOpenSidebar={openSidebar}
                    isSidebarOpen={sidebarOpen}
                    adminProfile={user || adminProfileMock}
                    onLogout={handleLogout}
                    loggingOut={loggingOut}
                />

                <main className="px-4 py-6 sm:px-6 lg:px-8 lg:py-8">
                    <div className="mx-auto w-full max-w-[1600px]">
                        <Outlet />
                    </div>
                </main>
            </div>
        </div>
    );
}

export default AdminLayout;
