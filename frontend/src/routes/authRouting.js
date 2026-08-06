import { ROUTES } from "./routePaths.js";

export function normalizeRole(role) {
    return String(role ?? "")
        .trim()
        .toUpperCase()
        .replace(/^ROLE_/, "");
}

export function normalizeApprovalStatus(status) {
    return String(status ?? "")
        .trim()
        .toUpperCase();
}

export function getPostAuthRoute(user) {
    if (!user) return ROUTES.LOGIN;

    const role = normalizeRole(user.role);
    const approvalStatus = normalizeApprovalStatus(user.approvalStatus);

    if (role === "ADMIN") return ROUTES.ADMIN_HOME;

    if (role === "ACCOUNT_HOLDER" && user.needsProfileCompletion) {
        return ROUTES.COMPLETE_GOOGLE_PROFILE;
    }

    if (role === "ACCOUNT_HOLDER" && approvalStatus === "APPROVED") {
        return ROUTES.ACCOUNT_HOME;
    }

    if (role === "ACCOUNT_HOLDER") return ROUTES.APPLICATION_STATUS;

    return ROUTES.LOGIN;
}
