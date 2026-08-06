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

function toUser(response, profile = null) {
    return {
        email: profile?.email || response.email,
        name: profile?.name || response.name,
        role: profile?.role || response.role,
        address: profile?.address,
        approvalStatus: profile?.approvalStatus,
        needsProfileCompletion: response?.redirectPath === "/complete-profile"
            || Boolean(profile && (!profile.address || profile.address === "Not provided")),
    };
}

export function AuthProvider({ children }) {
    const [user, setUser] = useState(null);
    const [isInitializing, setIsInitializing] = useState(true);

    const signOut = useCallback(async () => {
        try {
            await authApi.logout();
        } finally {
            setUser(null);
        }
    }, []);

    const refreshProfile = useCallback(async () => {
        const profile = await authApi.getCurrentUser();
        setUser(toUser(null, profile));
        return profile;
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
        await authApi.login(credentials);
        const profile = await authApi.getCurrentUser();
        const nextUser = toUser({}, profile);
        setUser(nextUser);
        return nextUser;
    }, []);

    const register = useCallback((payload) => authApi.signup(payload), []);

    const finishProfile = useCallback(async (address) => {
        const profile = await authApi.completeProfile(address);
        setUser((currentUser) => ({
            ...toUser(currentUser || {}, profile),
            needsProfileCompletion: false,
        }));
        return profile;
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
