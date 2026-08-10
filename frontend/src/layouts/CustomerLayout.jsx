import { Outlet } from "react-router";
import Sidebar from "../shared/components/layout/Sidebar";

export const CustomerLayout = () => {
    return (
        <div className="flex min-h-screen bg-brand-background">
            <Sidebar />
            <main className="flex-1 p-6 sm:p-8 lg:p-10 overflow-y-auto min-w-0">
                <div className="mx-auto w-full max-w-[1600px]">
                    <Outlet />
                </div>
            </main>
        </div>
    );
};

export default CustomerLayout;
