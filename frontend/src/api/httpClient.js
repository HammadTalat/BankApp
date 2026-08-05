import { API_BASE_URL } from "./apiConfig";

async function readResponse(response) {
    if (response.status === 204) {
        return null;
    }

    const contentType =
        response.headers.get("content-type") || "";

    if (contentType.includes("application/json")) {
        return response.json();
    }

    return response.text();
}

function getErrorMessage(data, response) {
    if (typeof data === "string" && data.trim()) {
        return data;
    }

    if (data?.message) {
        return data.message;
    }

    if (response.status === 401) {
        return "Your session has expired. Please sign in again.";
    }

    if (response.status === 403) {
        return "You are not allowed to perform this action.";
    }

    if (response.status === 404) {
        return "The requested information could not be found.";
    }

    return "Something went wrong. Please try again.";
}

async function request(path, options = {}) {
    const headers = new Headers(options.headers);

    const requestHasBody = options.body !== undefined;
    const bodyIsFormData = options.body instanceof FormData;

    if (requestHasBody && !bodyIsFormData) {
        headers.set("Content-Type", "application/json");
    }

    const response = await fetch(
        `${API_BASE_URL}${path}`,
        {
            ...options,
            headers,
            credentials: "include",
        },
    );

    const data = await readResponse(response);

    if (!response.ok) {
        const error = new Error(
            getErrorMessage(data, response),
        );

        error.status = response.status;
        error.data = data;

        throw error;
    }

    return data;
}

export const httpClient = {
    get(path) {
        return request(path, {
            method: "GET",
        });
    },

    post(path, body) {
        return request(path, {
            method: "POST",
            body:
                body === undefined
                    ? undefined
                    : JSON.stringify(body),
        });
    },

    patch(path, body) {
        return request(path, {
            method: "PATCH",
            body: JSON.stringify(body),
        });
    },

    delete(path) {
        return request(path, {
            method: "DELETE",
        });
    },
};
