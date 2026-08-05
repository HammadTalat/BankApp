import {
    useEffect,
    useState,
} from "react";
import { RefreshCw } from "lucide-react";
import { useNavigate } from "react-router";

import ApplicationStatusCard from "../components/ApplicationStatusCard";
import Alert from "../../../shared/components/feedback/Alert";
import LoadingSpinner from "../../../shared/components/feedback/LoadingSpinner";
import Button from "../../../shared/components/ui/Button";
import Card from "../../../shared/components/ui/Card";
import { ROUTES } from "../../../routes/routePaths";

const DEFAULT_ERROR_MESSAGE =
    "We could not check your application status. Please try again.";

function normalizeValue(value) {
    return String(value ?? "")
        .trim()
        .toUpperCase();
}

function getFriendlyErrorMessage(error) {
    const message =
        typeof error?.message === "string"
            ? error.message.trim()
            : "";
    const looksTechnical =
        /<[^>]+>|exception|stack\s*trace|org\.springframework|java\.|failed to fetch/i.test(
            message,
        );

    if (message && message.length <= 180 && !looksTechnical) {
        return message;
    }

    return DEFAULT_ERROR_MESSAGE;
}

async function requestApplicationStatus() {
    const currentUser = {
        name : "Ali",
        email : "temp@gmail.com",
        address: "123 wapda",
        role : "ACCOUNT_HOLDER",
        approvalStatus : "PENDING"
    };

    if (
        !currentUser ||
        typeof currentUser !== "object" ||
        Array.isArray(currentUser)
    ) {
        throw new Error(DEFAULT_ERROR_MESSAGE);
    }

    return currentUser;
}

function ApplicationStatusPage() {
    const navigate = useNavigate();
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);
    const [refreshing, setRefreshing] = useState(false);
    const [error, setError] = useState("");

    const approvalStatus = normalizeValue(user?.approvalStatus);
    const role = normalizeValue(user?.role);

    useEffect(() => {
        let cancelled = false;

        async function loadInitialStatus() {
            try {
                const currentUser = await requestApplicationStatus();

                if (!cancelled) {
                    setUser(currentUser);
                    setError("");
                }
            } catch (requestError) {
                if (!cancelled) {
                    setError(getFriendlyErrorMessage(requestError));
                }
            } finally {
                if (!cancelled) {
                    setLoading(false);
                }
            }
        }

        loadInitialStatus();

        return () => {
            cancelled = true;
        };
    }, []);

    useEffect(() => {
        if (role === "ADMIN") {
            navigate(ROUTES.ADMIN_HOME, {
                replace: true,
            });
            return;
        }

        if (
            role === "ACCOUNT_HOLDER" &&
            approvalStatus === "APPROVED"
        ) {
            navigate(ROUTES.ACCOUNT_HOME, {
                replace: true,
            });
        }
    }, [approvalStatus, navigate, role]);

    useEffect(() => {
        if (approvalStatus !== "PENDING") {
            return undefined;
        }

        let cancelled = false;

        const intervalId = window.setInterval(async () => {
            try {
                const currentUser = await requestApplicationStatus();

                if (!cancelled) {
                    setUser(currentUser);
                    setError("");
                }
            } catch (requestError) {
                if (!cancelled) {
                    setError(getFriendlyErrorMessage(requestError));
                }
            }
        }, 5000);

        return () => {
            cancelled = true;
            window.clearInterval(intervalId);
        };
    }, [approvalStatus]);

    async function handleRefresh() {
        setRefreshing(true);
        setError("");

        try {
            const currentUser = await requestApplicationStatus();
            setUser(currentUser);
        } catch (requestError) {
            setError(getFriendlyErrorMessage(requestError));
        } finally {
            setRefreshing(false);
        }
    }

    if (loading) {
        return (
            <main className="flex min-h-screen items-center justify-center bg-brand-background px-5 py-10">
                <LoadingSpinner
                    size="lg"
                    message="Checking your application status..."
                />
            </main>
        );
    }

    if (!user) {
        return (
            <main className="flex min-h-screen items-center justify-center bg-brand-background px-5 py-10">
                <Card className="w-full max-w-lg text-center">
                    <h1 className="text-2xl font-bold text-brand-text">
                        Application status unavailable
                    </h1>

                    <Alert
                        type="error"
                        title="We could not check your status"
                        className="mt-5 text-left"
                    >
                        {error || DEFAULT_ERROR_MESSAGE}
                    </Alert>

                    <Button
                        className="mt-6"
                        onClick={handleRefresh}
                        loading={refreshing}
                    >
                        {!refreshing && (
                            <RefreshCw
                                size={18}
                                aria-hidden="true"
                            />
                        )}
                        Retry
                    </Button>
                </Card>
            </main>
        );
    }

    return (
        <main className="flex min-h-screen items-center justify-center bg-brand-background px-5 py-10">
            <div className="flex w-full max-w-xl flex-col gap-4">
                {error && (
                    <Alert
                        type="error"
                        title="Status check failed"
                    >
                        {error}
                    </Alert>
                )}

                <ApplicationStatusCard
                    status={user.approvalStatus}
                    userName={user.name}
                    userEmail={user.email}
                    onRefresh={handleRefresh}
                    refreshing={refreshing}
                />
            </div>
        </main>
    );
}

export default ApplicationStatusPage;
