const USER_KEY = "bankapp_user";

export function getStoredUser() {
    const value = localStorage.getItem(USER_KEY);

    if (!value) {
        return null;
    }

    try {
        return JSON.parse(value);
    } catch {
        localStorage.removeItem(USER_KEY);
        return null;
    }
}

export function saveAuthSession(user) {
    localStorage.setItem(USER_KEY, JSON.stringify(user));
}

export function clearAuthSession() {
    localStorage.removeItem(USER_KEY);
    removeLegacyToken();
}

export function removeLegacyToken() {
    localStorage.removeItem("accessToken");
}
