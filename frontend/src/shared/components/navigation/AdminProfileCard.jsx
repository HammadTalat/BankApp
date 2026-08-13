import {
    LogOut,
    Mail,
    MapPin,
    ShieldCheck,
    UserRound,
    X,
} from "lucide-react";

import StatusBadge from "../ui/StatusBadge";

function AdminProfileCard({
    id,
    cardRef,
    profile,
    onClose,
    onLogout,
    loggingOut = false,
    profileDescription = "Predefined administrator",
    profileRegionLabel = "Administrator profile",
}) {
    return (
        <div
            id={id}
            ref={cardRef}
            data-testid="profile-card"
            role="region"
            aria-label={profileRegionLabel}
            tabIndex={-1}
            className="absolute right-0 top-[calc(100%+0.75rem)] z-50 w-72 max-w-[calc(100vw-2rem)] overflow-hidden rounded-xl border border-brand-border bg-brand-surface text-left shadow-md outline-none sm:w-80"
        >
            <div className="relative border-b border-brand-border bg-red-50 px-5 py-5">
                <button
                    type="button"
                    onClick={onClose}
                    className="absolute right-3 top-3 inline-flex h-9 w-9 items-center justify-center rounded-lg text-brand-muted transition hover:bg-white hover:text-brand-primary focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-primary"
                    aria-label={`Close ${profileRegionLabel.toLowerCase()}`}
                >
                    <X size={18} aria-hidden="true" />
                </button>

                <div className="flex items-center gap-3 pr-8">
                    <div className="flex h-12 w-12 shrink-0 items-center justify-center rounded-full bg-brand-primary text-white shadow-sm">
                        <UserRound size={24} aria-hidden="true" />
                    </div>
                    <div className="min-w-0">
                        <p className="truncate text-lg font-semibold text-brand-text">
                            {profile.name}
                        </p>
                        <p className="truncate text-xs text-brand-muted">
                            {profileDescription}
                        </p>
                    </div>
                </div>
            </div>

            <div className="p-5">
                <div className="flex items-start gap-3">
                    <Mail
                        size={18}
                        className="mt-0.5 shrink-0 text-brand-muted"
                        aria-hidden="true"
                    />
                    <div className="min-w-0">
                        <p className="text-xs font-medium uppercase tracking-wide text-brand-muted">
                            Email
                        </p>
                        <p className="mt-1 break-all text-sm font-semibold text-brand-text">
                            {profile.email}
                        </p>
                    </div>
                </div>

                <div className="mt-5 flex items-start gap-3">
                    <MapPin
                        size={18}
                        className="mt-0.5 shrink-0 text-brand-muted"
                        aria-hidden="true"
                    />
                    <div className="min-w-0">
                        <p className="text-xs font-medium uppercase tracking-wide text-brand-muted">
                            Address
                        </p>
                        <p className="mt-1 text-sm font-semibold text-brand-text">
                            {profile.address}
                        </p>
                    </div>
                </div>

                <div className="mt-5 flex items-center justify-between gap-4 border-t border-brand-border pt-4">
                    <div className="flex items-center gap-2 text-sm font-semibold text-brand-text">
                        <ShieldCheck
                            size={18}
                            className="text-brand-primary"
                            aria-hidden="true"
                        />
                        {profile.role}
                    </div>
                    <StatusBadge status={profile.approvalStatus} />
                </div>

                <button
                    type="button"
                    data-testid="profile-logout-button"
                    onClick={onLogout}
                    disabled={loggingOut}
                    className="mt-4 inline-flex w-full items-center justify-center gap-2 rounded-xl border border-brand-border bg-brand-surface px-4 py-2.5 text-sm font-semibold text-brand-text transition hover:bg-red-50 hover:text-brand-primary focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-primary focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-60"
                >
                    <LogOut size={18} aria-hidden="true" />
                    {loggingOut ? "Logging out…" : "Log out"}
                </button>
            </div>
        </div>
    );
}

export default AdminProfileCard;
