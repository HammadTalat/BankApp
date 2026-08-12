import {
    createContext,
    useCallback,
    useEffect,
    useMemo,
    useState,
} from "react";

import * as authApi from "../api/authApi";

// The hook consumes this context from a separate module.
// eslint-disable-next-line react-refresh/only-export-components
export const AuthContext = createContext(null);

function isProfileIncomplete(address) {
    return !address?.trim()
        || address.trim().toLowerCase() === "not provided";
}

function toUser(response = {}, profile = null) {
    const role = profile?.role || response?.role;
    const address = profile?.address;

    return {
        email: profile?.email || response?.email,
        name: profile?.name || response?.name,
        role,
        address,
        approvalStatus: profile?.approvalStatus,
        needsProfileCompletion: response?.redirectPath === "/complete-profile"
            || Boolean(
                profile
                && role === "ACCOUNT_HOLDER"
                && isProfileIncomplete(address),
            ),
    };
}

export function AuthProvider({ children }) {
    const [user, setUser] = useState(null);
    const [isInitializing, setIsInitializing] = useState(true);

    const signOut = useCallback(async (userEmail) => {
        try {
            await authApi.logout();
        } finally {
            // Clear chat history for the user who is signing out.
            if (userEmail) {
                localStorage.removeItem(`ai-chat-history:${userEmail}`);
            }
            localStorage.removeItem("ACCESS_TOKEN");
            setUser(null);
        }
    }, []);

    const refreshProfile = useCallback(async () => {
        try {
            const profile = await authApi.getCurrentUser();
            const nextUser = toUser({}, profile);
            setUser(nextUser);
            return nextUser;
        } catch (error) {
            if (error?.status === 401) setUser(null);
            throw error;
        }
    }, []);

    useEffect(() => {
        let isActive = true;

        async function restoreSession() {
            try {
                await refreshProfile();
            } catch {
                if (isActive) setUser(null);
            } finally {
                if (isActive) setIsInitializing(false);
            }
        }

        restoreSession().catch(() => {
            if (isActive) {
                setUser(null);
                setIsInitializing(false);
            }
        });
        return () => { isActive = false; };
    }, [refreshProfile]);

    const signIn = useCallback(async (credentials) => {
        const response = await authApi.login(credentials);
        const profile = await authApi.getCurrentUser();
        const nextUser = toUser(response, profile);
        setUser(nextUser);
        return nextUser;
    }, []);

    const register = useCallback((payload) => authApi.signup(payload), []);

    const finishProfile = useCallback(async (address) => {
        const profile = await authApi.completeProfile(address);
        const nextUser = {
            ...toUser({}, profile),
            needsProfileCompletion: false,
        };
        setUser(nextUser);
        return nextUser;
    }, []);

    const value = useMemo(() => ({
        user,
        isAuthenticated: Boolean(user),
        isInitializing,
        signIn,
        signOut,
        register,
        refreshProfile,
        finishProfile,
    }), [finishProfile, isInitializing, refreshProfile, register, signIn, signOut, user]);

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
