import { useEffect, useRef, useState } from "react";
import { Bot, Send } from "lucide-react";

import AccountHeader from "../../../../shared/components/navigation/AccountHeader.jsx";
import AccountSidebar from "../../../../shared/components/navigation/AccountSidebar.jsx";
import { useAuth } from "../../../auth/context/useAuth.js";
import { chatService } from "../api/chatService.js";

const INITIAL_MESSAGE = {
    id: "welcome",
    role: "assistant",
    content:
        "Hello! I am RedMath Bank's AI assistant. Ask me about your account balance, "
        + "recent transactions, or bank policies and fees.",
};
const MAX_STORED_MESSAGES = 150;

function getStorageKey(userEmail) {
    return `ai-chat-history:${userEmail}`;
}

function loadMessages(storageKey) {
    try {
        const saved = localStorage.getItem(storageKey);
        if (!saved) return null;
        const parsed = JSON.parse(saved);
        if (Array.isArray(parsed) && parsed.length > 0) return parsed;
    } catch {
        // fall through
    }
    return null;
}

function saveMessages(storageKey, messages) {
    // Never persist in-flight empty assistant placeholders.
    const toSave = messages.filter((m) => m.content && m.content.trim());
    try {
        localStorage.setItem(storageKey, JSON.stringify(toSave.slice(-MAX_STORED_MESSAGES)));
    } catch {
        // Ignore storage failures; chat continues working in-memory.
    }
}

function ChatMessage({ message }) {
    // Do not render anything while an assistant message is still loading.
    if (!message.content || !message.content.trim()) {
        return null;
    }

    const isUser = message.role === "user";

    return (
        <div
            className={`flex ${isUser ? "justify-end" : "justify-start"}`}
            data-testid={`chat-message-${message.role}`}
        >
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
                <p className="whitespace-pre-wrap" data-testid="chat-message-content">{message.content}</p>
            </div>
        </div>
    );
}

export function ChatbotPage() {
    const { user, signOut } = useAuth();
    const messagesEndRef = useRef(null);

    // Tracks whether we have loaded from localStorage for the current user.
    const loadedForRef = useRef(null);

    const [messages, setMessages] = useState([INITIAL_MESSAGE]);
    const [input, setInput] = useState("");
    const [isSending, setIsSending] = useState(false);
    const [error, setError] = useState("");

    // Load history once per user email (not on every render).
    useEffect(() => {
        if (!user?.email) return;
        if (loadedForRef.current === user.email) return;

        loadedForRef.current = user.email;
        const restored = loadMessages(getStorageKey(user.email));
        setMessages(restored ?? [INITIAL_MESSAGE]);
    }, [user?.email]);

    // Save history whenever messages change, but only after initial load.
    useEffect(() => {
        if (!user?.email) return;
        if (loadedForRef.current !== user.email) return;

        saveMessages(getStorageKey(user.email), messages);
    }, [messages, user?.email]);

    useEffect(() => {
        messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
    }, [messages, isSending]);

    const handleLogout = () => signOut(user?.email);

    const handleClearConversation = () => {
        setMessages([INITIAL_MESSAGE]);
        setError("");
        if (user?.email) {
            localStorage.removeItem(getStorageKey(user.email));
        }
    };

    const handleSubmit = async (event) => {
        event.preventDefault();

        const trimmedMessage = input.trim();
        if (!trimmedMessage || isSending) return;

        const userMessage = {
            id: `user-${Date.now()}`,
            role: "user",
            content: trimmedMessage,
        };

        // Append user message and an empty placeholder for the assistant.
        const assistantMessageId = `assistant-${Date.now()}`;
        setMessages((current) => [
            ...current,
            userMessage,
            { id: assistantMessageId, role: "assistant", content: "" },
        ]);
        setInput("");
        setError("");
        setIsSending(true);

        try {
            const data = await chatService.sendMessage(trimmedMessage);
            setMessages((current) => current.map((message) => (
                message.id === assistantMessageId
                    ? {
                        ...message,
                        content: data?.response?.trim()
                            || "I could not generate a response. Please try again.",
                    }
                    : message
            )));
        } catch (err) {
            // Remove the empty placeholder so no empty bubble lingers.
            setMessages((current) => current.filter((message) => message.id !== assistantMessageId));
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
                        <div className="mb-6 flex items-start justify-between gap-4">
                            <div>
                                <h2 className="text-3xl font-bold text-gray-900" data-testid="chatbot-heading">AI Assistant</h2>
                                <p className="mt-1 text-sm text-gray-500">
                                    Ask about your account, transactions, or RedMath Bank policies.
                                </p>
                            </div>
                            <button
                                type="button"
                                onClick={handleClearConversation}
                                disabled={isSending}
                                className="bg-brand-primary hover:bg-brand-primary-hover text-white font-semibold text-sm px-8 py-3 rounded-xl transition-colors shadow-sm disabled:opacity-50 flex items-center gap-2 cursor-pointer"
                            >
                                Clear conversation
                            </button>
                        </div>

                        {error && (
                            <div className="mb-4 rounded-xl border border-red-200 bg-red-50 p-4 text-xs font-medium text-red-600">
                                {error}
                            </div>
                        )}

                        <div className="flex min-h-0 flex-1 flex-col rounded-xl border border-brand-border bg-brand-surface shadow-sm">
                            <div className="flex-1 space-y-4 overflow-y-auto p-5 sm:p-6" data-testid="chat-messages">
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
                                        data-testid="chat-input"
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
                                        data-testid="chat-send-button"
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
