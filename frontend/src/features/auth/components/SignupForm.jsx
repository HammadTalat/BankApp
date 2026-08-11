import { useState } from "react";
import { Link } from "react-router";

import Alert from "../../../shared/components/feedback/Alert";
import PasswordInput from "./PasswordInput";

const initialValues = { name: "", email: "", address: "", password: "", confirmPassword: "" };

function SignupForm({ onSubmit, isSubmitting = false }) {
    const [values, setValues] = useState(initialValues);
    const [error, setError] = useState("");

    function update(field, value) { setValues({ ...values, [field]: value }); }
    async function handleSubmit(event) {
        event.preventDefault();
        setError("");
        if (values.password !== values.confirmPassword) {
            setError("Passwords do not match.");
            return;
        }
        if (values.password.length < 8) {
            setError("Password must contain at least 8 characters.");
            return;
        }
        try {
            await onSubmit({ name: values.name, email: values.email, address: values.address, password: values.password });
        } catch (submitError) {
            setError(submitError.message || "Unable to create your account.");
        }
    }

    return (
        <form onSubmit={handleSubmit} className="space-y-4" noValidate>
            {error && <Alert type="error">{error}</Alert>}
            {[['name', 'Full name', 'text', 'Your full name'], ['email', 'Email address', 'email', 'you@example.com'], ['address', 'Home address', 'text', 'Street, city, and country']].map(([field, label, type, placeholder]) => <div key={field}><label htmlFor={`signup-${field}`} className="mb-2 block text-sm font-medium text-brand-text">{label}</label><input id={`signup-${field}`} name={field} type={type} autoComplete={field === 'name' ? 'name' : field === 'email' ? 'email' : 'street-address'} required value={values[field]} onChange={(event) => update(field, event.target.value)} placeholder={placeholder} className="w-full rounded-xl border border-brand-border bg-white px-3.5 py-3 text-sm outline-none transition focus:border-brand-primary focus:ring-4 focus:ring-red-100" /></div>)}
            <PasswordInput id="signup-password" name="password" autoComplete="new-password" value={values.password} onChange={(event) => update("password", event.target.value)} helperText="Use at least 8 characters." required />
            <PasswordInput id="signup-confirm-password" name="confirmPassword" label="Confirm password" autoComplete="new-password" value={values.confirmPassword} onChange={(event) => update("confirmPassword", event.target.value)} required />
            <button type="submit" disabled={isSubmitting} className="w-full rounded-xl bg-brand-primary px-4 py-3 font-semibold text-white shadow-sm transition hover:bg-brand-primary-hover disabled:cursor-not-allowed disabled:opacity-60">{isSubmitting ? "Creating account…" : "Create account"}</button>
            <p className="text-center text-sm text-brand-muted">Already have an account? <Link to="/login" className="font-semibold text-brand-primary hover:underline">Sign in</Link></p>
        </form>
    );
}

export default SignupForm;
