import {
    Menu,
    UserRound,
} from "lucide-react";

function AdminHeader({
    onOpenSidebar,
    isSidebarOpen = false,
    adminName = "Administrator",
    adminEmail = "admin@nexabank.com",
}) {
    return (
        <header className="sticky top-0 z-30 border-b border-brand-border bg-brand-surface shadow-sm">
            <div className="mx-auto flex min-h-20 w-full max-w-[1600px] items-center gap-3 px-4 sm:px-6 lg:px-8">
                <button
                    type="button"
                    aria-label="Open admin navigation"
                    aria-controls="admin-sidebar"
                    aria-expanded={isSidebarOpen}
                    className="inline-flex h-11 w-11 shrink-0 items-center justify-center rounded-xl border border-brand-border text-brand-text transition-colors hover:bg-slate-50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-primary focus-visible:ring-offset-2 lg:hidden"
                    onClick={onOpenSidebar}
                >
                    <Menu
                        size={22}
                        aria-hidden="true"
                    />
                </button>

                <div className="min-w-0 flex-1">
                    <p className="truncate text-sm font-semibold text-brand-text sm:text-base">
                        Welcome back, {adminName}
                    </p>
                    <p className="hidden truncate text-sm text-brand-muted sm:block">
                        Manage your banking administration workspace.
                    </p>
                </div>

                <div className="flex shrink-0 items-center gap-3">
                    <div className="flex h-11 w-11 items-center justify-center rounded-full bg-blue-50 text-brand-primary ring-1 ring-blue-100">
                        <UserRound
                            size={21}
                            aria-hidden="true"
                        />
                    </div>

                    <div className="hidden min-w-0 text-right sm:block">
                        <p className="max-w-44 truncate text-sm font-semibold text-brand-text">
                            {adminName}
                        </p>
                        <p className="max-w-44 truncate text-xs text-brand-muted">
                            {adminEmail}
                        </p>
                    </div>
                </div>
            </div>
        </header>
    );
}

export default AdminHeader;
