import { useId, useState } from "react";
import { Eye, EyeOff } from "lucide-react";

function PasswordInput({ label = "Password", error, helperText, ...props }) {
    const generatedId = useId();
    const id = props.id || props.name || generatedId;
    const [isVisible, setIsVisible] = useState(false);

    return (
        <div>
            <label htmlFor={id} className="mb-2 block text-sm font-medium text-brand-text">
                {label}
            </label>
            <div className="relative">
                <input
                    {...props}
                    id={id}
                    type={isVisible ? "text" : "password"}
                    aria-invalid={error ? "true" : undefined}
                    aria-describedby={error ? `${id}-error` : undefined}
                    className={`w-full rounded-xl border bg-white px-3.5 py-3 pr-12 text-sm text-brand-text outline-none transition placeholder:text-slate-400 ${error ? "border-brand-danger focus:ring-4 focus:ring-red-100" : "border-brand-border focus:border-brand-primary focus:ring-4 focus:ring-blue-100"}`}
                />
                <button
                    type="button"
                    onClick={() => setIsVisible((visible) => !visible)}
                    className="absolute inset-y-0 right-0 flex w-11 items-center justify-center text-brand-muted hover:text-brand-text"
                    aria-label={isVisible ? "Hide password" : "Show password"}
                >
                    {isVisible ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
            </div>
            {error && <p id={`${id}-error`} className="mt-1.5 text-sm text-brand-danger">{error}</p>}
            {!error && helperText && <p className="mt-1.5 text-sm text-brand-muted">{helperText}</p>}
        </div>
    );
}

export default PasswordInput;
