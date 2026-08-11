import {
    useEffect,
    useId,
    useRef,
    useState,
} from "react";
import {
    ChevronDown,
    Menu,
    UserRound,
} from "lucide-react";

import AdminProfileCard from "./AdminProfileCard";

function AdminHeader({
    onOpenSidebar,
    isSidebarOpen = false,
    adminProfile,
    onLogout,
    loggingOut = false,
    portalLabel = "Administration",
    profileType = "administrator",
    profileDescription = "Predefined administrator",
    showMenuButton = true,
}) {
    const [profileOpen, setProfileOpen] = useState(false);
    const profileContainerRef = useRef(null);
    const profileCardRef = useRef(null);
    const profileTriggerRef = useRef(null);
    const profileCardId = useId();
    const profile = {
        name: adminProfile?.name || "Admin",
        email: adminProfile?.email || "admin@bank.local",
        address: adminProfile?.address || "System",
        role: adminProfile?.role || "ADMIN",
        approvalStatus: adminProfile?.approvalStatus || "APPROVED",
    };

    useEffect(() => {
        if (!profileOpen) {
            return undefined;
        }

        const focusFrame = window.requestAnimationFrame(() => {
            profileCardRef.current?.focus();
        });

        function handlePointerDown(event) {
            if (!profileContainerRef.current?.contains(event.target)) {
                setProfileOpen(false);
            }
        }

        function handleKeyDown(event) {
            if (event.key === "Escape") {
                setProfileOpen(false);
                profileTriggerRef.current?.focus();
            }
        }

        document.addEventListener("pointerdown", handlePointerDown);
        document.addEventListener("keydown", handleKeyDown);

        return () => {
            window.cancelAnimationFrame(focusFrame);
            document.removeEventListener("pointerdown", handlePointerDown);
            document.removeEventListener("keydown", handleKeyDown);
        };
    }, [profileOpen]);

    function handleCloseProfile() {
        setProfileOpen(false);
        profileTriggerRef.current?.focus();
    }

    return (
        <header className="sticky top-0 z-30 border-b border-brand-border bg-brand-surface shadow-sm">
            <div className="mx-auto flex min-h-20 w-full max-w-[1600px] items-center gap-3 px-4 sm:px-6 lg:px-8">
                {showMenuButton && (
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
                )}

                <div className="min-w-0 flex-1">
                    <p className="truncate text-sm font-semibold text-brand-text sm:text-base">
                        Welcome back, {profile.name}
                    </p>
                    <p className="hidden truncate text-sm text-brand-muted sm:block">
                        {portalLabel}
                    </p>
                </div>

                <div
                    ref={profileContainerRef}
                    className="relative shrink-0"
                >
                    <button
                        ref={profileTriggerRef}
                        type="button"
                        onClick={() => setProfileOpen((isOpen) => !isOpen)}
                        aria-label={`${profileOpen ? "Close" : "Open"} ${profileType} profile`}
                        aria-expanded={profileOpen}
                        aria-controls={profileCardId}
                        className="flex items-center gap-3 rounded-xl p-1.5 text-left transition hover:bg-slate-50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-primary focus-visible:ring-offset-2"
                    >
                        <span className="flex h-11 w-11 shrink-0 items-center justify-center rounded-full bg-red-50 text-brand-primary ring-1 ring-red-100">
                            <UserRound
                                size={21}
                                aria-hidden="true"
                            />
                        </span>

                        <span className="hidden min-w-0 sm:block">
                            <span className="block max-w-44 truncate text-sm font-semibold text-brand-text">
                                {profile.name}
                            </span>
                            <span className="block max-w-44 truncate text-xs text-brand-muted">
                                {profile.email}
                            </span>
                        </span>

                        <ChevronDown
                            size={16}
                            className={`hidden shrink-0 text-brand-muted transition-transform sm:block ${profileOpen ? "rotate-180" : ""}`}
                            aria-hidden="true"
                        />
                    </button>

                    {profileOpen && (
                        <AdminProfileCard
                            id={profileCardId}
                            cardRef={profileCardRef}
                            profile={profile}
                            onClose={handleCloseProfile}
                            onLogout={onLogout}
                            loggingOut={loggingOut}
                            profileDescription={profileDescription}
                            profileRegionLabel={`${profileType} profile`}
                        />
                    )}
                </div>
            </div>
        </header>
    );
}

export default AdminHeader;
