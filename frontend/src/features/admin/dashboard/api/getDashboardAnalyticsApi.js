import { httpClient } from "../../../../api/httpClient.js";

export function getDashboardAnalytics() {
    return httpClient.get("/api/v1/admin/dashboard");
}
