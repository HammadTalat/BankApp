import { Globe2 } from "lucide-react";

import { getGoogleLoginUrl } from "../api/authApi";

function GoogleLoginButton() {
    return (
        <button
            type="button"
            onClick={() => window.location.assign(getGoogleLoginUrl())}
            className="flex w-full items-center justify-center gap-3 rounded-xl border border-brand-border bg-white px-4 py-3 text-sm font-semibold text-brand-text shadow-sm transition hover:bg-slate-50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-primary"
        >
            <Globe2 size={19} className="text-[#4285f4]" aria-hidden="true" />
            Continue with Google
        </button>
    );
}

export default GoogleLoginButton;
