import { useState } from "react";
import { Outlet } from "react-router";

import AdminHeader from "../shared/components/navigation/AdminHeader";
import AdminSidebar from "../shared/components/navigation/AdminSidebar";
import { adminProfileMock } from "../features/admin/profile/mocks/adminProfileMock";

function AdminLayout() {
    const [sidebarOpen, setSidebarOpen] = useState(false);

    function openSidebar() {
        setSidebarOpen(true);
    }

    function closeSidebar() {
        setSidebarOpen(false);
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
                    adminProfile={adminProfileMock}
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
