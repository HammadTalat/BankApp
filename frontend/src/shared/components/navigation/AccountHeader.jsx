import AdminHeader from "./AdminHeader";

function AccountHeader({ accountProfile, onLogout, loggingOut = false }) {
    return (
        <AdminHeader
            adminProfile={accountProfile}
            onLogout={onLogout}
            loggingOut={loggingOut}
            portalLabel="Personal Banking"
            profileType="account"
            profileDescription="Account holder"
            showMenuButton={false}
        />
    );
}

export default AccountHeader;
