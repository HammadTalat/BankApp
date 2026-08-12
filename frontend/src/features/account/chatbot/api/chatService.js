import { httpClient } from "../../../../api/httpClient.js";

/**
 * Service for AI chatbot interactions.
 */
export const chatService = {
    /**
     * Sends a message to the banking AI assistant.
     * @param {string} message - The user's chat message.
     * @returns {Promise<{ response: string }>}
     */
    sendMessage: async (message) => {
        return await httpClient.post("/api/v1/ai/chat", { message });
    },

    /**
     * Logs out the currently authenticated user session.
     * @returns {Promise<void>}
     */
    logout: async () => {
        return await httpClient.post("/api/v1/auth/logout");
    },
};
