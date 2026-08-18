import { Landmark } from "lucide-react";

function RedMathBrand({ className = "" }) {
    return (
        <div className={`flex items-center gap-3 ${className}`}>
            <span className="flex h-11 w-11 items-center justify-center rounded-lg bg-brand-primary text-white shadow-sm">
                <Landmark size={22} aria-hidden="true" />
            </span>
            <div>
                <p
                    lang="ur"
                    dir="rtl"
                    className="rm-urdu-brand text-xl font-bold text-brand-navy"
                >
                    بینک ایپ
                </p>
                <p className="mt-0.5 text-xs font-semibold tracking-[0.06em] text-brand-muted">
                    Bank App
                </p>
            </div>
        </div>
    );
}

export default RedMathBrand;
