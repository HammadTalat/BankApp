import { useState } from "react";
import { Link } from "react-router";

import Alert from "../../../shared/components/feedback/Alert";
import GoogleLoginButton from "./GoogleLoginButton";
import PasswordInput from "./PasswordInput";

function LoginForm({ onSubmit, isSubmitting = false }) {
    const [values, setValues] = useState({ email: "", password: "" });
    const [error, setError] = useState("");

    async function handleSubmit(event) {
        event.preventDefault();
        setError("");
        try {
            await onSubmit(values);
        } catch (submitError) {
            setError(submitError.message || "Unable to sign in.");
        }
    }

    return (
        <form onSubmit={handleSubmit} className="space-y-5" noValidate data-testid="login-form">
            {error && <Alert type="error" data-testid="login-error">{error}</Alert>}
            <div>
                <label htmlFor="login-email" className="mb-2 block text-sm font-medium text-brand-text">Email address</label>
                <input id="login-email" name="email" type="email" autoComplete="email" required value={values.email} onChange={(event) => setValues({ ...values, email: event.target.value })} className="w-full rounded-xl border border-brand-border bg-white px-3.5 py-3 text-sm outline-none transition focus:border-brand-primary focus:ring-4 focus:ring-red-100" placeholder="you@example.com" />
            </div>
            <PasswordInput id="login-password" name="password" autoComplete="current-password" required value={values.password} onChange={(event) => setValues({ ...values, password: event.target.value })} />
            <button type="submit" disabled={isSubmitting} className="w-full rounded-xl bg-brand-primary px-4 py-3 font-semibold text-white shadow-sm transition hover:bg-brand-primary-hover disabled:cursor-not-allowed disabled:opacity-60">{isSubmitting ? "Signing in…" : "Sign in"}</button>
            <div className="relative py-1 text-center before:absolute before:inset-x-0 before:top-1/2 before:border-t before:border-brand-border"><span className="relative bg-white px-3 text-xs font-medium uppercase tracking-wide text-brand-muted">or</span></div>
            <GoogleLoginButton />
            <p className="text-center text-sm text-brand-muted">New to RedMath Bank? <Link to="/signup" className="font-semibold text-brand-primary hover:underline">Create an account</Link></p>
        </form>
    );
}

export default LoginForm;
