import { useEffect, useRef, useState } from "react";
import { Bot, Send } from "lucide-react";
import { useNavigate } from "react-router";

import AccountHeader from "../../../../shared/components/navigation/AccountHeader.jsx";
import AccountSidebar from "../../../../shared/components/navigation/AccountSidebar.jsx";
import { ROUTES } from "../../../../routes/routePaths.js";
import { useAuth } from "../../../auth/context/useAuth.js";
import { chatService } from "../api/chatService.js";

const INITIAL_MESSAGE = {
    id: "welcome",
    role: "assistant",
    content:
        "Hello! I am RedMath Bank's AI assistant. Ask me about your account balance, "
        + "recent transactions, or bank policies and fees.",
};

function ChatMessage({ message }) {
    const isUser = message.role === "user";

    return (
        <div className={`flex ${isUser ? "justify-end" : "justify-start"}`}>
            <div
                className={[
                    "max-w-[85%] rounded-2xl px-4 py-3 text-sm leading-relaxed shadow-sm",
                    isUser
                        ? "bg-brand-primary text-white"
                        : "border border-brand-border bg-brand-surface text-brand-text",
                ].join(" ")}
            >
                {!isUser && (
                    <div className="mb-2 flex items-center gap-2 text-xs font-semibold text-brand-primary">
                        <Bot size={14} aria-hidden="true" />
                        RedMath Assistant
                    </div>
                )}
                <p className="whitespace-pre-wrap">{message.content}</p>
            </div>
        </div>
    );
}

export function ChatbotPage() {
    const navigate = useNavigate();
    const { user } = useAuth();
    const messagesEndRef = useRef(null);

    const [messages, setMessages] = useState([INITIAL_MESSAGE]);
    const [input, setInput] = useState("");
    const [isSending, setIsSending] = useState(false);
    const [error, setError] = useState("");

    useEffect(() => {
        messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
    }, [messages, isSending]);

    const handleLogout = async () => {
        try {
            await chatService.logout();
        } catch {
            // Proceed with client cleanup regardless of server response
        } finally {
            localStorage.removeItem("ACCESS_TOKEN");
            navigate(ROUTES.HOME);
        }
    };

    const handleSubmit = async (event) => {
        event.preventDefault();

        const trimmedMessage = input.trim();
        if (!trimmedMessage || isSending) {
            return;
        }

        const userMessage = {
            id: `user-${Date.now()}`,
            role: "user",
            content: trimmedMessage,
        };

        setMessages((current) => [...current, userMessage]);
        setInput("");
        setError("");
        setIsSending(true);

        try {
            const data = await chatService.sendMessage(trimmedMessage);
            const assistantMessage = {
                id: `assistant-${Date.now()}`,
                role: "assistant",
                content: data?.response || "I could not generate a response. Please try again.",
            };
            setMessages((current) => [...current, assistantMessage]);
        } catch (err) {
            setError(err.message || "Failed to send your message. Please try again.");
        } finally {
            setIsSending(false);
        }
    };

    return (
        <div className="flex min-h-screen bg-brand-background">
            <AccountSidebar />

            <div className="flex min-w-0 flex-1 flex-col">
                <AccountHeader accountProfile={user} onLogout={handleLogout} />

                <main className="flex flex-1 flex-col overflow-hidden px-6 py-6 sm:px-8 sm:py-8">
                    <div className="mx-auto flex h-full w-full max-w-[1200px] flex-col">
                        <div className="mb-6">
                            <h2 className="text-3xl font-bold text-gray-900">AI Assistant</h2>
                            <p className="mt-1 text-sm text-gray-500">
                                Ask about your account, transactions, or RedMath Bank policies.
                            </p>
                        </div>

                        {error && (
                            <div className="mb-4 rounded-xl border border-red-200 bg-red-50 p-4 text-xs font-medium text-red-600">
                                {error}
                            </div>
                        )}

                        <div className="flex min-h-0 flex-1 flex-col rounded-xl border border-brand-border bg-brand-surface shadow-sm">
                            <div className="flex-1 space-y-4 overflow-y-auto p-5 sm:p-6">
                                {messages.map((message) => (
                                    <ChatMessage key={message.id} message={message} />
                                ))}

                                {isSending && (
                                    <div className="flex justify-start">
                                        <div className="rounded-2xl border border-brand-border bg-brand-surface px-4 py-3 text-sm text-brand-muted shadow-sm">
                                            RedMath Assistant is typing...
                                        </div>
                                    </div>
                                )}

                                <div ref={messagesEndRef} />
                            </div>

                            <form
                                onSubmit={handleSubmit}
                                className="border-t border-brand-border p-4 sm:p-5"
                            >
                                <div className="flex items-end gap-3">
                                    <textarea
                                        value={input}
                                        onChange={(event) => setInput(event.target.value)}
                                        onKeyDown={(event) => {
                                            if (event.key === "Enter" && !event.shiftKey) {
                                                event.preventDefault();
                                                handleSubmit(event);
                                            }
                                        }}
                                        rows={2}
                                        placeholder="Type your question here..."
                                        disabled={isSending}
                                        className="min-h-[52px] flex-1 resize-none rounded-xl border border-gray-200 px-4 py-3 text-sm text-gray-700 focus:border-brand-primary focus:outline-none disabled:cursor-not-allowed disabled:opacity-60"
                                    />
                                    <button
                                        type="submit"
                                        disabled={isSending || !input.trim()}
                                        className="inline-flex h-[52px] min-w-[52px] items-center justify-center rounded-xl bg-brand-primary px-4 text-white transition-colors hover:bg-brand-primary-hover disabled:cursor-not-allowed disabled:opacity-50"
                                        aria-label="Send message"
                                    >
                                        <Send size={18} aria-hidden="true" />
                                    </button>
                                </div>
                            </form>
                        </div>
                    </div>
                </main>
            </div>
        </div>
    );
}

export default ChatbotPage;
